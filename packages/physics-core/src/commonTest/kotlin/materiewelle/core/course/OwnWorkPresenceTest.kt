package materiewelle.core.course

import kotlin.test.*
import materiewelle.core.*

class OwnWorkPresenceTest {
    private fun PersonalEvidenceReport.row(section: OwnWorkSection) = ownWorkPresence().single { it.section == section }
    private fun SessionSnapshot.row(section: OwnWorkSection) = personalEvidenceReport().row(section)
    private fun work(station: StationId, work: StationWork) = SessionSnapshot(work = mapOf(station to work))
    private fun measurement(markers: List<Int> = listOf(551, 564), reason: String = "Eigene benachbarte Maxima") =
        MeasurementSession(markers = markers.map(::NativeImageMark), justification = reason)
    private fun measurementReport(own: MeasurementSession = measurement(), binding: ReportImageBinding = ReportImageBinding.MATCHED) =
        SessionSnapshot(measurement = own).personalEvidenceReport().copy(imageBinding = binding)

    @Test fun emptyAndControlOnlyAreOpenEvenWithCalculatedModelsAndMatchedImage() {
        val state = SessionSnapshot(work = mapOf(
            StationId.S06_CALCULATION to StationWork(checked = true, solutionViewed = true, transferAnswer = "50 pm"),
            StationId.S11_DG_EVALUATION to StationWork(solutionViewed = true),
        ))
        val report = state.personalEvidenceReport().copy(imageBinding = ReportImageBinding.MATCHED)
        assertEquals(OwnWorkSection.entries, report.ownWorkPresence().map { it.section })
        assertTrue(report.ownWorkPresence().all { it.presence == OwnWorkPresence.OPEN })
    }

    @Test fun primaryAnswerAndItsNoteAreBothRequiredButCorrectnessIsIndependent() {
        val partial = work(StationId.S06_CALCULATION, StationWork(numericAnswer = "999 nm", checked = true))
        assertEquals(OwnWorkPresence.PARTIAL, partial.row(OwnWorkSection.CALCULATION).presence)
        assertEquals(listOf(OwnWorkField.CALCULATION_NOTE), partial.row(OwnWorkSection.CALCULATION).missing)
        val complete = work(StationId.S06_CALCULATION, StationWork(numericAnswer = "999 nm", note = "Mein falscher Rechenweg", checked = true))
        val entry = complete.row(OwnWorkSection.CALCULATION)
        assertEquals(OwnWorkPresence.PRESENT, entry.presence)
        assertEquals(NumericAnswerStatus.INCORRECT, entry.numericFeedback?.status)
        assertTrue(entry.missing.isEmpty())
        assertTrue(complete.personalEvidenceReport().readableText().contains("999 nm"))
    }

    @Test fun unrelatedNotesPredictionsAndTransferWorkDoNotFillPrimaryAnswerOrNote() {
        val state = SessionSnapshot(work = mapOf(
            StationId.S05_DERIVATION to StationWork(note = "Andere Notiz"),
            StationId.S06_CALCULATION to StationWork(prediction = "Vorhersage", transferAnswer = "50 pm"),
        ))
        assertEquals(OwnWorkPresence.OPEN, state.row(OwnWorkSection.CALCULATION).presence)
        assertEquals(OwnWorkPresence.PARTIAL, work(StationId.S06_CALCULATION, StationWork(note = "Nur eigener Rechenweg")).row(OwnWorkSection.CALCULATION).presence)
    }

    @Test fun dgRequiresBothOwnRoutesAndInterpretationWithoutGradingThem() {
        val partial = work(StationId.S11_DG_EVALUATION, StationWork(prediction = "999 nm", numericAnswer = "anders"))
        assertEquals(listOf(OwnWorkField.DG_INTERPRETATION), partial.row(OwnWorkSection.DG).missing)
        assertEquals(OwnWorkPresence.PARTIAL, partial.row(OwnWorkSection.DG).presence)
        val complete = work(StationId.S11_DG_EVALUATION, StationWork(prediction = "999 nm", numericAnswer = "anders", note = "Noch falsche Schlussfolgerung"))
        assertEquals(OwnWorkPresence.PRESENT, complete.row(OwnWorkSection.DG).presence)
        assertNull(complete.row(OwnWorkSection.DG).numericFeedback)
    }

    @Test fun twoMarkersAreEnoughAndUnsealedSelectionRemainsPresentWithNotice() {
        val entry = measurementReport().row(OwnWorkSection.MEASUREMENT)
        assertEquals(OwnWorkPresence.PRESENT, entry.presence)
        assertEquals(listOf(OwnWorkNotice.SELECTION_NOT_SEALED), entry.notices)
        assertTrue(entry.missing.isEmpty())
        assertEquals(OwnWorkPresence.PARTIAL, measurementReport(measurement(listOf(551))).row(OwnWorkSection.MEASUREMENT).presence)
        assertContains(measurementReport(measurement(listOf(551))).row(OwnWorkSection.MEASUREMENT).notices, OwnWorkNotice.NEEDS_SECOND_MARKER)
    }

    @Test fun uncheckedMismatchedAndMissingImageNeverClaimCompletePresence() {
        for ((binding, notice) in listOf(
            ReportImageBinding.STORED_ONLY to OwnWorkNotice.IMAGE_UNCHECKED,
            ReportImageBinding.MISMATCH to OwnWorkNotice.IMAGE_MISMATCH,
            ReportImageBinding.NO_IMAGE to OwnWorkNotice.IMAGE_MISSING,
        )) {
            val entry = measurementReport(binding = binding).row(OwnWorkSection.MEASUREMENT)
            assertEquals(OwnWorkPresence.PARTIAL, entry.presence)
            assertContains(entry.missing, OwnWorkField.MEASUREMENT_BINDING)
            assertContains(entry.notices, notice)
        }
    }

    @Test fun reasonForPreviousBandIsPresentTextButNotCurrentBandReason() {
        val own = measurement().changeBand(MeasurementBand.UPPER)
        val entry = measurementReport(own).row(OwnWorkSection.MEASUREMENT)
        assertEquals(OwnWorkPresence.PARTIAL, entry.presence)
        assertContains(entry.missing, OwnWorkField.MEASUREMENT_REASON)
        assertContains(entry.notices, OwnWorkNotice.REASON_OTHER_BAND)
        assertEquals("Eigene benachbarte Maxima", own.justification)
    }

    @Test fun savedComparisonOrFirstSelectionAreNotLostBehindAnEmptyCurrentSelection() {
        val saved = MeasurementSnapshot(MeasurementBand.BASE, listOf(NativeImageMark(551), NativeImageMark(564)),
            "Gespeicherter eigener Vergleich", false, SessionSnapshot.IMAGE_SHA256, 703, 242)
        val comparison = measurementReport(MeasurementSession(comparison = saved)).row(OwnWorkSection.MEASUREMENT)
        assertEquals(OwnWorkPresence.PARTIAL, comparison.presence)
        assertContains(comparison.notices, OwnWorkNotice.SAVED_COMPARISON_EXISTS)
        val initial = measurementReport(MeasurementSession(initialSelection = saved)).row(OwnWorkSection.MEASUREMENT)
        assertEquals(OwnWorkPresence.PARTIAL, initial.presence)
        assertContains(initial.notices, OwnWorkNotice.SAVED_SELECTION_EXISTS)
    }

    @Test fun whitespaceAndAuthoredConclusionAreNotMistakenForOwnConclusion() {
        assertEquals(OwnWorkPresence.OPEN, work(StationId.S16_CONCLUSION, StationWork(note = "  \n ")).row(OwnWorkSection.CONCLUSION).presence)
        assertEquals(OwnWorkPresence.PRESENT, work(StationId.S16_CONCLUSION, StationWork(note = "Mein vorläufiger Schluss")).row(OwnWorkSection.CONCLUSION).presence)
    }

    @Test fun projectionNeverMutatesOrReplacesOwnWork() {
        val state = SessionSnapshot(measurement = measurement(), work = mapOf(
            StationId.S06_CALCULATION to StationWork(numericAnswer = "999 nm", note = "Eigene Notiz", checked = true),
            StationId.S16_CONCLUSION to StationWork(note = "Mein Fazit"),
        ))
        val before = state.json()
        repeat(3) { state.personalEvidenceReport().ownWorkPresence() }
        assertEquals(before, state.json())
        assertEquals(state, SessionSnapshot.fromJson(before))
        assertEquals(state.personalEvidenceReport().ownWorkPresence(), SessionSnapshot.fromJson(before).personalEvidenceReport().ownWorkPresence())
    }
}
