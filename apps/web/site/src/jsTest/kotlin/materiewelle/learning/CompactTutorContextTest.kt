package materiewelle.learning

import kotlin.test.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.encodeToString
import materiewelle.core.*
import materiewelle.core.course.*

class CompactTutorContextTest {
    @Test fun required150VoltAnswerKeepsItsTaskWhenTheModelUses600Volts() {
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "600", lastValidVoltage = 600.0),
            work = mapOf(StationId.S06_CALCULATION to StationWork(
                numericAnswer = "100 pm", note = "Mein Rechenweg für die Pflichtaufgabe",
            )),
        )
        val before = snapshot.json()
        val own = compactOwnWork(snapshot, AppSection.DE_BROGLIE) as CompactOwnWork.DeBroglie
        val task = assertNotNull(own.requiredCalculation)
        val model = assertNotNull(own.modelVariation)
        assertEquals(StationId.S06_CALCULATION, task.station)
        assertEquals(150.0, task.voltageV)
        assertEquals("100 pm", task.numericAnswer)
        assertEquals("Mein Rechenweg für die Pflichtaufgabe", task.calculationNote)
        assertEquals(600.0, model.acceptedVoltageV)
        assertEquals("600", model.editedVoltage)
        assertEquals(before, snapshot.json())

        val codec = Json { classDiscriminator = "kind" }
        val payload = codec.parseToJsonElement(codec.encodeToString<CompactOwnWork>(own)).jsonObject
        assertEquals(setOf("kind", "requiredCalculation", "modelVariation"), payload.keys)
        assertEquals(150.0, payload.getValue("requiredCalculation").jsonObject.getValue("voltageV").jsonPrimitive.double)
        assertEquals(600.0, payload.getValue("modelVariation").jsonObject.getValue("acceptedVoltageV").jsonPrimitive.double)
        assertFalse("voltageV" in payload) // No ambiguous parameter shared by two different cases.
    }

    @Test fun emptyRequiredWorkStaysEmptyAndModelEditsStillInvalidateTheSelectedContext() {
        val snapshot = SessionSnapshot(deBroglie = DeBroglieSession(raw = "600", lastValidVoltage = 600.0))
        val own = compactOwnWork(snapshot, AppSection.DE_BROGLIE) as CompactOwnWork.DeBroglie
        assertEquals("", assertNotNull(own.requiredCalculation).numericAnswer)
        assertEquals("", assertNotNull(own.requiredCalculation).calculationNote)
        val edited = compactOwnWork(snapshot.copy(deBroglie = snapshot.deBroglie.edit("invalid")), AppSection.DE_BROGLIE) as CompactOwnWork.DeBroglie
        assertEquals(own.requiredCalculation, edited.requiredCalculation)
        assertEquals(600.0, assertNotNull(edited.modelVariation).acceptedVoltageV)
        assertEquals("invalid", assertNotNull(edited.modelVariation).editedVoltage)
        assertNotEquals(own, edited) // CompactTutor's existing consent invalidation compares this value.
        val changed = compactOwnWork(snapshot.copy(deBroglie = snapshot.deBroglie.edit("375")), AppSection.DE_BROGLIE) as CompactOwnWork.DeBroglie
        assertEquals(own.requiredCalculation, changed.requiredCalculation)
        assertEquals(375.0, assertNotNull(changed.modelVariation).acceptedVoltageV)
        assertNotEquals(own, changed)
    }

    @Test fun sourceCaseResultsAreIncludedOnlyInTheSelectedChapter() {
        val snapshot = SessionSnapshot(work = mapOf(
            StationId.S11_DG_EVALUATION to StationWork(numericAnswer = "0,165 nm", prediction = "0,167 nm"),
            StationId.S06_CALCULATION to StationWork(numericAnswer = "100 pm")))
        val dg = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER) as CompactOwnWork.Surface
        assertEquals(1, dg.notes.size)
        assertEquals("0,165 nm", dg.notes.single().numericAnswer)
        assertEquals("0,167 nm", dg.notes.single().prediction)
        assertEquals("dg-auswertung", dg.notes.single().station)
    }
    @Test fun projectionsIncludeOnlyCurrentTopicWork() {
        val snapshot = SessionSnapshot(work = mapOf(
            StationId.S06_CALCULATION to StationWork(note = "Rechnung eigen", numericAnswer = "100 pm"),
            StationId.S10_GEOMETRY to StationWork(note = "DG eigener Winkel"),
            StationId.S14_MEASUREMENT to StationWork(note = "J eigener Bildhinweis"),
            StationId.S16_CONCLUSION to StationWork(note = "Mein Fazit")))
        val codec = Json { classDiscriminator = "kind" }
        val db: CompactOwnWork = compactOwnWork(snapshot, AppSection.DE_BROGLIE)
        val dbText = codec.encodeToString(db)
        assertContains(dbText, "Rechnung eigen")
        assertFalse(dbText.contains("DG eigener Winkel"))
        assertFalse(dbText.contains("J eigener Bildhinweis"))
        val dg = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER) as CompactOwnWork.Surface
        assertEquals(listOf("DG eigener Winkel"), dg.notes.map { it.note })
        val conclusion = compactOwnWork(snapshot, AppSection.CONCLUSION) as CompactOwnWork.Conclusion
        assertEquals("Mein Fazit", conclusion.ownConclusion)
        assertTrue(conclusion.claims.isEmpty())
    }

    @Test fun longImageReasonIsPreservedAndReferenceViewIsNotAnImport() {
        val reason = "Meine begründete Maximalwahl. ".repeat(80)
        val marks = listOf(NativeImageMark(551), NativeImageMark(564))
        val first = MeasurementSnapshot(MeasurementBand.BASE, marks, reason, false,
            SessionSnapshot.IMAGE_SHA256, 703, 242)
        val measurement = MeasurementSession(markers = marks, justification = reason,
            initialSelection = first, referenceViewed = true)
        val snapshot = SessionSnapshot(measurement = measurement)
        val before = snapshot.json()
        val own = compactOwnWork(snapshot, AppSection.JONSSON) as CompactOwnWork.Image
        assertEquals(reason, own.selectionReason)
        assertEquals(reason, own.initialSelection!!.selectionReason)
        assertEquals(TutorMeasurementOrigin.OWN_SELECTION, own.measurement!!.origin)
        assertTrue(own.measurement.referenceViewed)
        assertEquals(listOf(551.0, 564.0), own.measurement.markersNativeX)
        assertEquals(before, snapshot.json())
    }

    @Test fun widerImportedVoltageDoesNotCrashOrSilentlyClampContext() {
        val snapshot = SessionSnapshot(dg = DgSession(modelVoltageRaw = "50000", validModelVoltage = 50000.0))
        val own = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER) as CompactOwnWork.Surface
        val model = assertNotNull(own.modelVariation)
        assertNull(model.model)
        assertEquals(50000.0, model.acceptedVoltageV)
        assertEquals("50000", model.editedVoltage)
        assertEquals(CompactModelAvailability.OUTSIDE_HANDOFF_MODEL_RANGE, model.modelAvailability)
    }

    @Test fun unsupportedRasterDoesNotBecomeCanonicalMeasurement() {
        val image = MeasurementImageIdentity("a".repeat(64), 80, 80, 0, 80, NativeImageBand(20, 60))
        val snapshot = SessionSnapshot(imageIdentity = image)
        val own = compactOwnWork(snapshot, AppSection.JONSSON) as CompactOwnWork.Image
        assertNull(own.measurement)
        assertNull(own.initialSelection)
        assertNull(own.comparison)
    }

    @Test fun fixedCalculationAndFreeModelShareOnlyTheirOwnValuesAndConsent() {
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "777", lastValidVoltage = 777.0, comparisonVoltage = 333.0),
            work = mapOf(
                StationId.S06_CALCULATION to StationWork(numericAnswer = "100 pm", note = "Pflichtrechnung"),
                StationId.S05_DERIVATION to StationWork(note = "Frühere Herleitung"),
            ),
        )
        fun context(value: SessionSnapshot, point: TutorHelpPoint) =
            compactOwnWork(value, AppSection.DE_BROGLIE, point)
        val task = assertIs<CompactOwnWork.DeBroglie>(context(snapshot, TutorHelpPoint.CALCULATION))
        val model = assertIs<CompactOwnWork.DeBroglie>(context(snapshot, TutorHelpPoint.DE_BROGLIE_MODEL))
        assertEquals(150.0, assertNotNull(task.requiredCalculation).voltageV)
        assertNull(task.modelVariation)
        assertTrue(task.notes.isEmpty())
        assertNull(model.requiredCalculation)
        assertTrue(model.notes.isEmpty())
        assertEquals(777.0, assertNotNull(model.modelVariation).acceptedVoltageV)
        assertEquals(333.0, assertNotNull(model.modelVariation).rememberedVoltageV)

        val modelChanged = snapshot.copy(deBroglie = snapshot.deBroglie.copy(comparisonVoltage = 444.0))
        assertEquals(task, context(modelChanged, TutorHelpPoint.CALCULATION))
        assertNotEquals(model, context(modelChanged, TutorHelpPoint.DE_BROGLIE_MODEL))
        val taskChanged = snapshot.copy(work = snapshot.work +
            (StationId.S06_CALCULATION to StationWork(numericAnswer = "101 pm")))
        assertNotEquals(task, context(taskChanged, TutorHelpPoint.CALCULATION))
        assertEquals(model, context(taskChanged, TutorHelpPoint.DE_BROGLIE_MODEL))

        val chapter = assertIs<CompactOwnWork.DeBroglie>(compactOwnWork(snapshot, AppSection.DE_BROGLIE))
        assertEquals(task.requiredCalculation, chapter.requiredCalculation)
        assertEquals(model.modelVariation, chapter.modelVariation)
        assertEquals(listOf("Frühere Herleitung"), chapter.notes.map { it.note })
    }

    @Test fun dgSourceAnswersAndRememberedModelSettingsHaveIndependentConsent() {
        val snapshot = SessionSnapshot(
            dg = DgSession(modelVoltageRaw = "777", validModelVoltage = 777.0,
                modelAngleRaw = "61", validModelAngle = 61.0,
                comparisonVoltage = 333.0, comparisonAngle = 42.0),
            work = mapOf(
                StationId.S10_GEOMETRY to StationWork(note = "Frühere Winkelüberlegung"),
                StationId.S11_DG_EVALUATION to StationWork(numericAnswer = "0,165 nm", prediction = "0,167 nm"),
            ),
        )
        fun context(value: SessionSnapshot, point: TutorHelpPoint) =
            compactOwnWork(value, AppSection.DAVISSON_GERMER, point)
        val task = assertIs<CompactOwnWork.Surface>(context(snapshot, TutorHelpPoint.DG_COMPARISON))
        val model = assertIs<CompactOwnWork.Surface>(context(snapshot, TutorHelpPoint.DG_MODEL))
        assertNull(task.modelVariation)
        assertEquals(listOf(StationId.S11_DG_EVALUATION.key), task.notes.map { it.station })
        assertEquals("0,165 nm", task.notes.single().numericAnswer)
        assertEquals("0,167 nm", task.notes.single().prediction)
        assertTrue(model.notes.isEmpty())
        val setting = assertNotNull(model.modelVariation)
        assertEquals(777.0, setting.acceptedVoltageV)
        assertEquals(61.0, setting.acceptedAngleDegrees)
        assertEquals(CompactSurfaceSetting(333.0, 42.0), setting.remembered)

        val modelChanged = snapshot.copy(dg = snapshot.dg.copy(comparisonAngle = 43.0))
        assertEquals(task, context(modelChanged, TutorHelpPoint.DG_COMPARISON))
        assertNotEquals(model, context(modelChanged, TutorHelpPoint.DG_MODEL))
        val taskChanged = snapshot.copy(work = snapshot.work +
            (StationId.S11_DG_EVALUATION to StationWork(prediction = "Mein neuer Rechenweg")))
        assertNotEquals(task, context(taskChanged, TutorHelpPoint.DG_COMPARISON))
        assertEquals(model, context(taskChanged, TutorHelpPoint.DG_MODEL))

        val chapter = assertIs<CompactOwnWork.Surface>(compactOwnWork(snapshot, AppSection.DAVISSON_GERMER))
        assertEquals(setting, chapter.modelVariation)
        assertEquals(listOf(StationId.S10_GEOMETRY.key, StationId.S11_DG_EVALUATION.key), chapter.notes.map { it.station })
    }

    @Test fun slitContextKeepsBothTaskNotesAndSavedParametersWithoutLeakingImageWork() {
        val remembered = SlitModelComparison(spacingUm = 2.2, widthUm = .4)
        val snapshot = SessionSnapshot(
            modelSlitSpacingUm = 3.4, modelSlitWidthUm = .7,
            inquiry = InquiryState(slitComparison = remembered),
            measurement = MeasurementSession(markers = listOf(NativeImageMark(551), NativeImageMark(564)),
                justification = "Eigene Bildbegründung"),
            work = mapOf(
                StationId.S12_JONSSON_SETUP to StationWork(note = "Aufbau"),
                StationId.S13_INTERFERENCE to StationWork(note = "Spaltüberlegung"),
                StationId.S14_MEASUREMENT to StationWork(note = "Bildnotiz"),
                StationId.S15_LIMITS to StationWork(note = "Grenze"),
            ),
        )
        val exploration = CompactSlitExploration(
            CompactSlitConfiguration.BOTH_INCOHERENT, CompactSlitTask.WIDTH,
            notes = mapOf(
                CompactSlitTask.SPACING to CompactSlitNotes("Streifen rücken zusammen", "Größerer Abstand"),
                CompactSlitTask.WIDTH to CompactSlitNotes("Hülle wird enger", "Breitere Spalte"),
            ), observationMeters = .012,
        )
        val extras = CompactTutorExtras(exploration)
        val before = snapshot.json()
        fun context(point: TutorHelpPoint?, extra: CompactTutorExtras = extras, value: SessionSnapshot = snapshot) =
            assertIs<CompactOwnWork.Image>(compactOwnWork(value, AppSection.JONSSON, point, extra))
        val slit = context(TutorHelpPoint.SLIT_MODEL)
        assertFalse(slit.includeMeasurement)
        assertNull(slit.measurement)
        assertNull(slit.initialSelection)
        assertNull(slit.comparison)
        assertEquals("", slit.selectionReason)
        assertEquals(listOf("Spaltüberlegung"), slit.notes.map { it.note })
        val model = assertNotNull(slit.slitModel)
        assertEquals(SlitModelComparison(spacingUm = 3.4, widthUm = .7), model.current)
        assertEquals(remembered, model.remembered)
        assertEquals(exploration, model.exploration)
        assertNull(assertNotNull(context(TutorHelpPoint.SLIT_MODEL, CompactTutorExtras()).slitModel).exploration)

        val image = context(TutorHelpPoint.IMAGE_MEASUREMENT)
        assertTrue(image.includeMeasurement)
        assertNull(image.slitModel)
        assertEquals(listOf(551.0, 564.0), assertNotNull(image.measurement).markersNativeX)
        assertEquals("Eigene Bildbegründung", image.selectionReason)
        assertEquals(listOf("Bildnotiz"), image.notes.map { it.note })
        val changedExtras = CompactTutorExtras(exploration.copy(configuration = CompactSlitConfiguration.LEFT_ONLY))
        assertNotEquals(slit, context(TutorHelpPoint.SLIT_MODEL, changedExtras))
        assertEquals(image, context(TutorHelpPoint.IMAGE_MEASUREMENT, changedExtras))
        val changedModel = snapshot.copy(modelSlitSpacingUm = 4.1)
        assertNotEquals(slit, context(TutorHelpPoint.SLIT_MODEL, value = changedModel))
        assertEquals(image, context(TutorHelpPoint.IMAGE_MEASUREMENT, value = changedModel))
        val changedMeasurement = snapshot.copy(measurement = snapshot.measurement.copy(justification = "Neue Bildbegründung"))
        assertEquals(slit, context(TutorHelpPoint.SLIT_MODEL, value = changedMeasurement))
        assertNotEquals(image, context(TutorHelpPoint.IMAGE_MEASUREMENT, value = changedMeasurement))

        val chapter = context(null)
        assertEquals(model, chapter.slitModel)
        assertEquals(image.measurement, chapter.measurement)
        assertEquals(listOf("Aufbau", "Spaltüberlegung", "Bildnotiz", "Grenze"), chapter.notes.map { it.note })
        assertEquals(before, snapshot.json())
    }

    @Test fun blankLocalSlitNotesDoNotInventWorkOrInvalidateUnchangedConsent() {
        val exploration = CompactSlitExploration(CompactSlitConfiguration.BOTH_COHERENT, CompactSlitTask.SPACING)
        fun context(notes: Map<CompactSlitTask, CompactSlitNotes>) = compactOwnWork(
            SessionSnapshot(), AppSection.JONSSON, TutorHelpPoint.SLIT_MODEL,
            CompactTutorExtras(exploration.copy(notes = notes)),
        )
        val empty = context(emptyMap())
        assertEquals(empty, context(mapOf(CompactSlitTask.WIDTH to CompactSlitNotes(" \n", "  "))))
        assertNotEquals(empty, context(mapOf(CompactSlitTask.WIDTH to CompactSlitNotes(reason = "Meine Begründung"))))
    }

    @Test fun conclusionKeepsOriginalClaimNumbersAndNeedsExplicitEarlierResultsConsent() {
        val snapshot = SessionSnapshot(
            work = mapOf(
                StationId.S06_CALCULATION to StationWork(numericAnswer = "100 pm"),
                StationId.S11_DG_EVALUATION to StationWork(note = "DG-Ergebnis"),
                StationId.S14_MEASUREMENT to StationWork(note = "Bild-Ergebnis"),
                StationId.S16_CONCLUSION to StationWork(note = "Mein eigener Schluss"),
            ),
            inquiry = InquiryState(evidenceRows = listOf(
                EvidenceRow(), EvidenceRow(evidence = "Beleg in Zeile zwei"),
                EvidenceRow(claim = "Behauptung in Zeile drei", limitation = "Meine Grenze"),
            )),
        )
        val before = snapshot.json()
        fun conclusion(value: SessionSnapshot = snapshot, includeEarlier: Boolean = false) =
            assertIs<CompactOwnWork.Conclusion>(compactOwnWork(value, AppSection.CONCLUSION,
                TutorHelpPoint.CONCLUSION, includeEarlierResults = includeEarlier))
        val own = conclusion()
        assertEquals(listOf(2, 3), own.claims.map { it.number })
        assertEquals(snapshot.inquiry.evidenceRows.drop(1), own.claims.map { it.row })
        assertEquals("Mein eigener Schluss", own.ownConclusion)
        assertTrue(own.relatedWork.isEmpty())
        val included = conclusion(includeEarlier = true)
        assertEquals(own.claims, included.claims)
        assertEquals(listOf(AppSection.DE_BROGLIE, AppSection.DAVISSON_GERMER, AppSection.JONSSON)
            .map { compactOwnWork(snapshot, it) }, included.relatedWork)
        assertNotEquals(own, included)

        val earlierChanged = snapshot.copy(work = snapshot.work +
            (StationId.S06_CALCULATION to StationWork(numericAnswer = "101 pm")))
        assertEquals(own, conclusion(earlierChanged))
        assertNotEquals(included, conclusion(earlierChanged, includeEarlier = true))
        assertEquals(before, snapshot.json())
    }

    @Test fun localHelpPointCannotRequestAnotherChaptersWork() {
        assertFailsWith<IllegalArgumentException> {
            compactOwnWork(SessionSnapshot(), AppSection.JONSSON, TutorHelpPoint.DG_MODEL)
        }
    }
}
