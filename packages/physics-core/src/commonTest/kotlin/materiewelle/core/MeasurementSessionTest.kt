package materiewelle.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MeasurementSessionTest {
    @Test fun nonbaseBandCannotMasqueradeAsBaseOnASmallerRaster() {
        val image = MeasurementImageIdentity("a".repeat(64), 703, 80, 490, 703, NativeImageBand(20, 60))
        val invalid = MeasurementSession(band = MeasurementBand.LOWER)
        assertFailsWith<IllegalArgumentException> { invalid.nativeBand(image) }
        assertFailsWith<IllegalArgumentException> { invalid.validate(image) }
        assertEquals(NativeImageBand(20, 60), MeasurementSession().nativeBand(image))
    }

    private val image = JonssonData("jonsson-page6-xref33.png", 703, 242,
        listOf(551.0, 564.0, 578.0, 592.0, 606.0, 620.0, 634.0, 647.0), 160.0,
        50_000.0, 2e-6, 0.35, "Keine Fotokalibrierung", 490.0, 100.0, 213.0, 120.0,
        "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6")

    private fun selection(vararg positions: Int) = positions.fold(MeasurementSession()) { session, x -> session.addMarker(x, image) }
        .copy(justification = "Breites Basisband; aufeinanderfolgende helle Maxima, Randartefakte ausgelassen.")

    @Test fun firstAttemptIsEmptyAndDoesNotShowReferenceOrFakeZero() {
        val empty = MeasurementSession()
        assertEquals(MeasurementSelection.Empty, empty.selection)
        assertFalse(empty.referenceViewed)
        assertFalse(empty.canSave)
        val protocol = empty.protocol(image)
        assertEquals(MeasurementStatus.EMPTY, protocol.state)
        assertNull(protocol.meanSpacingPx)
        assertTrue(protocol.intervalsPx.isEmpty())
        assertTrue(protocol.nativeMarkers.isEmpty())
    }

    @Test fun aSingleOwnMarkIsPartialAndTwoDefineOneInterval() {
        val first = MeasurementSession().addMarker(550, image)
        assertTrue(first.selection is MeasurementSelection.Partial)
        assertNull(first.protocol(image).meanSpacingPx)
        val second = first.addMarker(565, image)
        val ready = second.selection as MeasurementSelection.Ready
        assertEquals(listOf(15), ready.intervalsPx)
        assertEquals(15.0, ready.meanPx)
        assertFalse(second.canSave) // A method justification is a separate, required own contribution.
    }

    @Test fun nonReferenceSelectionsAreValidMeasurements() {
        val own = selection(549, 565, 579)
        assertEquals(15.0, (own.selection as MeasurementSelection.Ready).meanPx)
        assertTrue(own.canSave)
        assertEquals(own.markers, own.saveInitialSelection(image).initialSelection!!.markers)
    }

    @Test fun referenceCannotBeRevealedWithoutReasonedSavedFirstDecision() {
        assertFailsWith<IllegalArgumentException> { MeasurementSession().revealReference() }
        assertFailsWith<IllegalArgumentException> { MeasurementSession().saveInitialSelection(image) }
        assertFailsWith<IllegalArgumentException> { selection(550).saveInitialSelection(image) }
        assertFailsWith<IllegalArgumentException> { selection(550, 564).copy(justification = " ").saveInitialSelection(image) }
        val secured = selection(550, 564).saveInitialSelection(image)
        assertTrue(secured.canViewReference)
        assertFalse(secured.referenceViewed)
        val viewed = secured.revealReference()
        assertTrue(viewed.referenceViewed)
        assertFalse(viewed.initialSelection!!.referenceViewedAtCapture)
        assertFailsWith<IllegalArgumentException> { viewed.saveInitialSelection(image) }
    }

    @Test fun firstDecisionDoesNotChangeWithEditingOrBandChoice() {
        val first = selection(551, 564, 578).saveInitialSelection(image)
        val changed = first.moveMarker(1, 565, image).changeBand(MeasurementBand.UPPER)
            .copy(justification = "Oberes schmales Band zum Methodenvergleich.")
        assertEquals(listOf(551, 564, 578), changed.initialSelection!!.markers.map { it.xPx })
        assertEquals(MeasurementBand.BASE, changed.initialSelection!!.band)
        assertEquals(MeasurementBand.UPPER, changed.band)
        assertEquals(listOf(551, 565, 578), changed.markers.map { it.xPx })
    }

    @Test fun bandChangeKeepsAllOwnCoordinatesAndDoesNotCopyReference() {
        val own = selection(550, 565, 580)
        MeasurementBand.entries.forEach { band ->
            val changed = own.changeBand(band)
            assertEquals(own.markers, changed.markers)
            assertEquals(15.0, changed.protocol(image).meanSpacingPx)
            assertEquals(band.topInclusive, changed.protocol(image).bandTopInclusivePx)
            assertEquals(band.bottomExclusive, changed.protocol(image).bandBottomExclusivePx)
        }
    }

    @Test fun comparisonIsAnIndependentImmutableSnapshot() {
        val saved = selection(551, 564, 578).saveComparison(image)
        val changed = saved.moveMarker(2, 579, image).changeBand(MeasurementBand.LOWER)
        assertEquals(13.5, changed.comparison!!.meanPx)
        assertEquals(14.0, changed.protocol(image).meanSpacingPx)
        assertEquals(MeasurementBand.BASE, changed.comparison!!.band)
        assertNull(changed.clearComparison().comparison)
        assertEquals(changed.markers, changed.clearComparison().markers)
    }

    @Test fun clearingMeasurementPreservesReferenceHistoryAndSavedEvidence() {
        val original = selection(551, 564).saveInitialSelection(image).revealReference().saveComparison(image)
        val cleared = original.clearMeasurement()
        assertEquals(MeasurementSelection.Empty, cleared.selection)
        assertTrue(cleared.referenceViewed)
        assertNotNull(cleared.initialSelection)
        assertNotNull(cleared.comparison)
        assertTrue(cleared.comparison!!.referenceViewedAtCapture)
        assertEquals("", cleared.justification)
    }

    @Test fun sortingCrossingDuplicatesAndBoundsAreValidated() {
        val own = selection(565, 550)
        assertEquals(listOf(550, 565), own.markers.map { it.xPx })
        assertFailsWith<IllegalArgumentException> { own.addMarker(550, image) }
        assertFailsWith<IllegalArgumentException> { own.addMarker(489, image) }
        assertFailsWith<IllegalArgumentException> { own.addMarker(703, image) }
        assertFailsWith<IllegalArgumentException> { own.moveMarker(0, 565, image) }
        assertFailsWith<IllegalArgumentException> { own.moveMarker(0, 566, image) }
        assertFailsWith<IllegalArgumentException> { MeasurementSession(markers = listOf(NativeImageMark(600), NativeImageMark(550))) }
        assertFailsWith<IllegalArgumentException> { NativeImageMark(-1) }
        assertFailsWith<IllegalArgumentException> { MeasurementSession(markers = listOf(NativeImageMark(489))).validate() }
        val full = (0 until MeasurementSession.MAX_MARKERS).fold(MeasurementSession()) { state, i -> state.addMarker(500 + i * 2, image) }
        assertFailsWith<IllegalArgumentException> { full.addMarker(600, image) }
    }

    @Test fun removingMarksCanReachAnHonestEmptyState() {
        val first = selection(550, 565).removeMarker(0)
        assertTrue(first.selection is MeasurementSelection.Partial)
        val empty = first.removeMarker(0)
        assertEquals(MeasurementSelection.Empty, empty.selection)
        assertFailsWith<IllegalArgumentException> { empty.removeMarker(0) }
    }

    @Test fun endpointAndInternalVariationsHaveDifferentMeanEffects() {
        val own = selection(551, 564, 578, 592, 606, 620, 634, 647)
        assertEquals(96.0 / 7, own.protocol(image).meanSpacingPx!!, 1e-14)
        assertEquals(96.0 / 7, own.moveMarker(3, 593, image).protocol(image).meanSpacingPx!!, 1e-14)
        assertEquals(1.0 / 7, own.moveMarker(7, 648, image).protocol(image).meanSpacingPx!! - own.protocol(image).meanSpacingPx!!, 1e-14)
    }

    @Test fun serializableRoundtripRetainsReferenceHistoryComparisonAndFirstDecision() {
        val state = selection(551, 564, 578).saveInitialSelection(image).revealReference().saveComparison(image).changeBand(MeasurementBand.UPPER)
        val restored = Json.decodeFromString<MeasurementSession>(Json.encodeToString(state)).validate(image)
        assertEquals(state, restored)
        assertFailsWith<IllegalArgumentException> { MeasurementSession(referenceViewed = true) }
        assertFailsWith<IllegalArgumentException> { restored.copy(initialSelection = restored.initialSelection!!.copy(referenceViewedAtCapture = true)) }
    }

    @Test fun snapshotsBelongToTheExactImageAndHalfOpenBand() {
        val state = selection(551, 564).saveInitialSelection(image)
        val snapshot = state.initialSelection!!
        assertEquals(image.imageSha256, snapshot.imageSha256)
        assertEquals(703, snapshot.nativeWidthPx)
        assertEquals(242, snapshot.nativeHeightPx)
        assertEquals(100, snapshot.bandTopInclusivePx)
        assertEquals(220, snapshot.bandBottomExclusivePx)
        assertFailsWith<IllegalArgumentException> { snapshot.copy(bandBottomExclusivePx = 243) }
        assertFailsWith<IllegalArgumentException> { state.validate(image.copy(imageSha256 = "a".repeat(64))) }
        assertFailsWith<IllegalArgumentException> { state.validate(imageWidth = 704) }
    }

    @Test fun exportIsReadableAndKeepsMeasurementDistinctFromCalibration() {
        val protocol = selection(551, 564, 578).saveInitialSelection(image).protocol(image)
        val json = Json.parseToJsonElement(protocol.json()).jsonObject
        assertEquals("ready", json.getValue("state").jsonPrimitive.content)
        assertEquals("own_present_day_pixel_measurement_of_historical_reproduction", json.getValue("provenance").jsonPrimitive.content)
        assertEquals("no_independent_absolute_image_scale", json.getValue("calibrationStatus").jsonPrimitive.content)
        assertFalse(json.keys.any { it.contains("wavelength", ignoreCase = true) || it.contains("zoom", ignoreCase = true) })
        assertTrue(protocol.readable(includeTechnicalAppendix = true).contains(image.imageSha256))
        assertTrue(protocol.readable().contains("100 ≤ y < 220"))
        assertTrue(protocol.readable().contains("keine absolute Wellenlänge"))
        assertEquals(protocol, Json.decodeFromString<MeasurementProtocol>(protocol.json()))
    }

    @Test fun importedSmallerRasterKeepsItsOwnDeclaredBandAndDoesNotCrash() {
        val smaller = image.copy(height = 80, markerY = 40.0, cropTop = 20.0, cropHeight = 40.0,
            imageSha256 = "a".repeat(64))
        val empty = MeasurementSession().protocol(smaller)
        assertEquals(20, empty.bandTopInclusivePx)
        assertEquals(60, empty.bandBottomExclusivePx)
        val own = MeasurementSession().addMarker(551, smaller).addMarker(564, smaller)
            .copy(justification = "Eigenes kleines Prüfraster mit seinem erklärten Band.").saveInitialSelection(smaller)
        assertEquals(20, own.initialSelection!!.bandTopInclusivePx)
        assertEquals(60, own.initialSelection!!.bandBottomExclusivePx)
        assertEquals(13.0, own.protocol(smaller).meanSpacingPx)
    }
    @Test fun aBandChangeNeverRelabelsTheOldReasoning() {
        val base = selection(564, 578, 592).saveInitialSelection(image)
        val upper = base.changeBand(MeasurementBand.UPPER)
        assertEquals(base.justification, upper.justification)
        assertEquals(MeasurementBand.BASE, upper.justificationBand)
        assertFalse(upper.canSave)
        assertFailsWith<IllegalArgumentException> { upper.saveComparison(image) }
        val confirmed = upper.confirmJustification()
        assertTrue(confirmed.canSave)
        assertEquals(MeasurementBand.UPPER, confirmed.justificationBand)
        assertEquals(MeasurementBand.UPPER, confirmed.saveComparison(image).comparison!!.band)
        assertEquals(MeasurementBand.BASE, confirmed.initialSelection!!.band)
        val rewritten = upper.updateJustification("Schmales oberes Band; aufeinanderfolgende Maxima eigenständig geprüft.")
        assertTrue(rewritten.canSave)
        assertEquals(MeasurementBand.UPPER, rewritten.justificationBand)
    }

    @Test fun legacyReasoningWithUnknownBandRequiresConsciousReassignment() {
        val legacy = selection(564, 578).copy(justificationBand = null)
        assertFalse(legacy.canSave)
        assertFailsWith<IllegalArgumentException> { legacy.saveInitialSelection(image) }
        assertTrue(legacy.confirmJustification().canSave)
        assertFailsWith<IllegalArgumentException> { MeasurementSession().confirmJustification() }
    }

    @Test fun externalMaterialAccessIsSeparateFromLaboratoryOverlayHistory() {
        val influenced = selection(564, 578).recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE)
        assertFalse(influenced.referenceViewed)
        assertFalse(influenced.canViewReference)
        val secured = influenced.saveInitialSelection(image)
        assertEquals(setOf(ReferenceMaterial.PDF_REFERENCE), secured.initialSelection!!.priorReferenceMaterials)
        val later = secured.recordReferenceMaterial(ReferenceMaterial.TUTOR_CONTEXT).revealReference().saveComparison(image)
        assertEquals(setOf(ReferenceMaterial.PDF_REFERENCE), later.initialSelection!!.priorReferenceMaterials)
        assertEquals(setOf(ReferenceMaterial.PDF_REFERENCE, ReferenceMaterial.TUTOR_CONTEXT), later.comparison!!.priorReferenceMaterials)
        assertEquals(later.referenceMaterialsOpened, later.clearMeasurement().referenceMaterialsOpened)
        assertTrue(later.clearMeasurement().referenceViewed)
        assertEquals(later, Json.decodeFromString<MeasurementSession>(Json.encodeToString(later)))
        val protocol = later.protocol(image)
        assertEquals("2.0", protocol.schemaVersion)
        assertEquals(later.referenceMaterialsOpened, protocol.referenceMaterialsOpened)
        assertEquals(later.justificationBand, protocol.justificationBand)
        assertTrue(protocol.readable(includeTechnicalAppendix = true).contains("Öffnungs- und Anfrageereignisse"))
        assertTrue(protocol.readable(includeTechnicalAppendix = true).contains("Sie belegen weder Lesen oder Übernahme noch eine erfolgreiche Dateizustellung."))
        assertEquals(protocol, Json.decodeFromString<MeasurementProtocol>(protocol.json()))
    }

    @Test fun requestedHandoutIsAdditiveAndNeverRetrospectivelyChangesFirstSelection() {
        val priorRequest = selection(564, 578).recordReferenceMaterial(ReferenceMaterial.HANDOUT_REQUESTED)
        val beforeCapture = priorRequest.saveInitialSelection(image)
        assertEquals(setOf(ReferenceMaterial.HANDOUT_REQUESTED), beforeCapture.initialSelection!!.priorReferenceMaterials)
        assertFalse(beforeCapture.referenceViewed)
        val captured = selection(564, 578).saveInitialSelection(image)
        val afterCapture = captured.recordReferenceMaterial(ReferenceMaterial.HANDOUT_REQUESTED).saveComparison(image)
        assertEquals(emptySet(), afterCapture.initialSelection!!.priorReferenceMaterials)
        assertEquals(captured.initialSelection, afterCapture.initialSelection)
        assertEquals(setOf(ReferenceMaterial.HANDOUT_REQUESTED), afterCapture.comparison!!.priorReferenceMaterials)
        assertEquals(afterCapture, Json.decodeFromString<MeasurementSession>(Json.encodeToString(afterCapture)))
        assertTrue(Json.encodeToString(afterCapture).contains("HANDOUT_REQUESTED"))
        assertEquals(MeasurementSession(), Json.decodeFromString<MeasurementSession>("{}"))
        assertFalse(ReferenceMaterial.HANDOUT_REQUESTED in Json.decodeFromString<MeasurementSession>("{}" ).referenceMaterialsOpened)
    }

    @Test fun aReasonedThirteenPointEightPixelSelectionIsNotRoundedToTheReference() {
        val state = selection(564, 578, 592, 606, 620, 633).saveInitialSelection(image)
        assertEquals(listOf(14, 14, 14, 14, 13), state.initialSelection!!.intervalsPx)
        assertEquals(13.8, state.protocol(image).meanSpacingPx)
        assertEquals(listOf(564, 578, 592, 606, 620, 633), state.markers.map { it.xPx })
    }

    @Test fun standardProtocolRetainsCurrentAndSavedWorkWithoutTechnicalContactHistory() {
        val state = selection(551, 564, 578, 592, 606, 620, 634, 647)
            .recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE)
            .saveInitialSelection(image).revealReference()
            .recordReferenceMaterial(ReferenceMaterial.TUTOR_CONTEXT)
            .changeBand(MeasurementBand.UPPER)
            .updateJustification("Oberes Band geprüft; benachbarte helle Maxima gewählt.")
            .moveMarker(7, 648, image).saveComparison(image)
            .moveMarker(7, 649, image)
        val text = state.protocol(image).readable()
        assertTrue(text.contains("703 × 242 Originalpixel"))
        assertTrue(text.contains("20 ≤ y < 45; x-Ausschnitt: 490 ≤ x < 703"))
        assertTrue(text.contains("Marken x / Originalpixel: 551, 564, 578, 592, 606, 620, 634, 649"))
        assertTrue(text.contains("Abstände / Originalpixel: 13, 14, 14, 14, 14, 14, 15"))
        assertTrue(text.contains("Arithmetischer mittlerer Abstand / Originalpixel: 14,000"))
        assertTrue(text.contains("Gesicherte erste Auswahl: Basisband, 100 ≤ y < 220"))
        assertTrue(text.contains("Marken x / Originalpixel: 551, 564, 578, 592, 606, 620, 634, 647"))
        assertTrue(text.contains("Arithmetischer mittlerer Abstand / Originalpixel: 13,714"))
        assertTrue(text.contains(state.initialSelection!!.justification))
        assertTrue(text.contains("Gespeicherter eigener Vergleich: Oberes Band, 20 ≤ y < 45"))
        assertTrue(text.contains("Marken x / Originalpixel: 551, 564, 578, 592, 606, 620, 634, 648"))
        assertTrue(text.contains("Arithmetischer mittlerer Abstand / Originalpixel: 13,857"))
        assertTrue(text.contains(state.justification))
        assertTrue(text.contains("dem aktuellen Band zugeordnet"))
        assertTrue(text.contains("keine unabhängigen historischen Wiederholungen oder belegten Fehlergrenzen"))
        assertTrue(text.contains("keine absolute Wellenlänge aus Pixeln"))
        assertTrue(text.contains("Anzeigezoom und Bildschirmgröße sind keine Messparameter"))
        listOf("TECHNISCHER ANHANG", image.imageSha256, "SHA-256", "READY", "true", "false",
            ReferenceMaterial.PDF_REFERENCE.label, ReferenceMaterial.TUTOR_CONTEXT.label,
            "Öffnen der Referenz vermerkt", state.initialSelection!!.meanPx.toString())
            .forEach { assertFalse(text.contains(it), "Standardprotokoll enthält technische Angabe: $it") }
    }

    @Test fun protocolStatesExplainCalculabilityWithoutClaimingCorrectMeasurement() {
        val empty = MeasurementSession().protocol(image).readable()
        assertTrue(empty.contains("Messstand: Noch keine Marke gesetzt."))
        assertTrue(empty.contains("Arithmetischer mittlerer Abstand / Originalpixel: noch nicht berechenbar"))
        assertTrue(empty.contains("noch keine Begründung eingetragen"))
        val partial = MeasurementSession().addMarker(551, image).protocol(image).readable()
        assertTrue(partial.contains("Eine Marke gesetzt; für einen Abstand fehlt eine zweite."))
        assertTrue(partial.contains("Abstände / Originalpixel: noch nicht berechenbar"))
        val readyWithoutReason = MeasurementSession().addMarker(551, image).addMarker(564, image)
        assertFalse(readyWithoutReason.canSave)
        assertTrue(readyWithoutReason.protocol(image).readable().contains(
            "Abstände berechenbar; die Auswahl der Maxima ist damit nicht geprüft."))
        val changedBand = selection(551, 564).changeBand(MeasurementBand.UPPER).protocol(image).readable()
        assertTrue(changedBand.contains("Begründung zugeordnet zu: Basisband; Zuordnung zum aktuellen Band (Oberes Band) noch zu bestätigen."))
        assertTrue(selection(551, 564).copy(justificationBand = null).protocol(image).readable()
            .contains("Begründung zugeordnet zu: unbekannt; Zuordnung zum aktuellen Band (Basisband) noch zu bestätigen."))
        listOf(empty, partial, readyWithoutReason.protocol(image).readable()).forEach { text ->
            listOf("EMPTY", "PARTIAL", "READY").forEach { assertFalse(text.contains(it)) }
        }
    }

    @Test fun optionalAppendixRetainsExactCalculationAndContactProvenanceWithoutChangingState() {
        val state = selection(551, 564, 578, 592, 606, 620, 634, 647)
            .recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE).saveInitialSelection(image)
            .recordReferenceMaterial(ReferenceMaterial.TUTOR_CONTEXT).revealReference()
            .moveMarker(7, 651, image).saveComparison(image)
            .recordReferenceMaterial(ReferenceMaterial.HANDOUT_REQUESTED)
        val protocol = state.protocol(image)
        val stateBefore = Json.encodeToString(state)
        val protocolBefore = protocol.json()
        val standard = protocol.readable()
        val detailed = protocol.readable(includeTechnicalAppendix = true)
        assertTrue(standard.contains("Arithmetischer mittlerer Abstand / Originalpixel: 14,286"))
        assertTrue(standard.contains("Mittelwerte sind auf drei Nachkommastellen gerundet. Diese Darstellung gibt keine Messgenauigkeit an."))
        assertFalse(standard.contains(protocol.meanSpacingPx.toString()))
        assertTrue(detailed.startsWith(standard))
        val appendix = detailed.substringAfter("TECHNISCHER ANHANG · RECHENWERTE UND KONTAKTVERMERKE")
        assertTrue(appendix.contains("Messzustand: READY"))
        assertTrue(appendix.contains("SHA-256: ${image.imageSha256}"))
        assertTrue(appendix.contains("Datenherkunft: OWN_PIXEL_MEASUREMENT"))
        assertTrue(appendix.contains("Kalibrierungsstatus: MISSING_ABSOLUTE_SCALE"))
        assertTrue(appendix.contains("Rechenwert ohne Anzeigerundung des mittleren Abstands / px: ${protocol.meanSpacingPx}"))
        val initialProvenance = appendix.substringAfter("Gesicherte erste Auswahl ·").substringBefore("Gespeicherter eigener Vergleich ·")
        assertTrue(initialProvenance.contains("im Protokoll gespeicherter Mittelwert: ${96.0 / 7}"))
        assertTrue(initialProvenance.contains("Referenz vor Speicherung bereits eingeblendet: nein"))
        assertTrue(initialProvenance.contains(ReferenceMaterial.PDF_REFERENCE.label))
        assertFalse(initialProvenance.contains(ReferenceMaterial.TUTOR_CONTEXT.label))
        assertFalse(initialProvenance.contains(ReferenceMaterial.HANDOUT_REQUESTED.label))
        val comparisonProvenance = appendix.substringAfter("Gespeicherter eigener Vergleich ·")
        assertTrue(comparisonProvenance.contains("im Protokoll gespeicherter Mittelwert: ${100.0 / 7}"))
        assertTrue(comparisonProvenance.contains("Referenz vor Speicherung bereits eingeblendet: ja"))
        assertTrue(comparisonProvenance.contains(ReferenceMaterial.PDF_REFERENCE.label))
        assertTrue(comparisonProvenance.contains(ReferenceMaterial.TUTOR_CONTEXT.label))
        assertFalse(comparisonProvenance.contains(ReferenceMaterial.HANDOUT_REQUESTED.label))
        assertTrue(appendix.contains(ReferenceMaterial.HANDOUT_REQUESTED.label))
        assertTrue(appendix.contains("Sie belegen weder Lesen oder Übernahme noch eine erfolgreiche Dateizustellung."))
        assertEquals(stateBefore, Json.encodeToString(state))
        assertEquals(protocolBefore, protocol.json())
        assertEquals(100.0 / 7, protocol.meanSpacingPx)
        assertEquals(96.0 / 7, protocol.initialSelectionMeanPx)
        assertEquals(100.0 / 7, protocol.comparisonMeanPx)
    }

}
