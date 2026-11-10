package materiewelle.core.course

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import materiewelle.core.NativeImageMark
import materiewelle.core.ReferenceMaterial
import materiewelle.core.MeasurementBand
import materiewelle.core.MeasurementSnapshot
import materiewelle.core.Physics
import materiewelle.core.Voltage

class ReflectionBundleTest {
    private val contexts = StationId.entries.map {
        ReflectionStationContext(it, "Titel ${it.number}", "Vorhandene Leitfrage", "Vorhandene Aufgabe", "Vorhandener Transfer",
            listOf("Vorhandenes Prüfkriterium"), emptyList(), listOf(ReflectionSource("A1", "Aufgabe", "gelesen")), listOf("Vorhandene Grenze"))
    }

    @Test fun ownInvestigationChoicesExportEvenWithoutAStationNote() {
        val inquiry = InquiryState(superpositionPrediction = "Die Summe wird kleiner", phaseTurns = 0.5,
            superpositionRevealed = true, crystalAssignment = CrystalAssignmentChoice.ROWS,
            crystalRevealed = true, geometryPrediction = GeometryPathChoice.BOTH,
            opticsPrediction = OpticsStage.OPTICS, opticsRevealed = true)
        val bundle = buildReflectionBundle(SessionSnapshot(inquiry = inquiry), contexts)
        assertEquals(setOf(StationId.S02_CLASSICAL, StationId.S09_CRYSTAL, StationId.S10_GEOMETRY,
            StationId.S12_JONSSON_SETUP), bundle.stations.map { it.context.station }.toSet())
        val byStation = bundle.stations.associateBy { it.context.station }
        assertEquals("Die Summe wird kleiner", byStation.getValue(StationId.S02_CLASSICAL).inquiryWork!!.superpositionPrediction)
        assertEquals(0.5, byStation.getValue(StationId.S02_CLASSICAL).inquiryWork!!.phaseTurns)
        assertEquals(CrystalAssignmentChoice.ROWS, byStation.getValue(StationId.S09_CRYSTAL).inquiryWork!!.crystalAssignment)
        assertEquals(GeometryPathChoice.BOTH, byStation.getValue(StationId.S10_GEOMETRY).inquiryWork!!.geometryPrediction)
        assertEquals(OpticsStage.OPTICS, byStation.getValue(StationId.S12_JONSSON_SETUP).inquiryWork!!.opticsPrediction)
        assertEquals(bundle, ReflectionBundle.fromJson(bundle.json()))
        val current = buildReflectionBundle(SessionSnapshot(station = StationId.S09_CRYSTAL, inquiry = inquiry), contexts,
            ReflectionScope.CURRENT_STATION).stations.single()
        assertNull(current.inquiryWork!!.superpositionPrediction)
        assertNull(current.inquiryWork!!.geometryPrediction)
        assertNull(current.inquiryWork!!.opticsPrediction)
        assertFailsWith<IllegalArgumentException> { current.copy(inquiryWork = ReflectionInquiryWork(superpositionPrediction = "Fremder Stationsinhalt")) }
    }

    @Test fun preservesFullOwnTextAndAttachesReviewedAnswersWithoutInventingGrade() {
        val explanation = "Meine Begründung mit ä und λ: " + "a".repeat(3800)
        val session = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = explanation, numericAnswer = "100 pm", transferAnswer = "50 pm")),
            questions = mapOf(QuestionId.Q02 to QuestionAttempt(QuestionOptionId.A, checked = true, firstSelected = QuestionOptionId.C, contentRevision = QuestionAttempt.CURRENT_CONTENT_REVISION)))
        val bundle = buildReflectionBundle(session, contexts)
        val station = bundle.stations.single()
        assertEquals(explanation, station.ownResponse.explanation)
        assertEquals("100 pm", station.ownResponse.numericAnswer)
        assertEquals(100.13726074475609, station.numericExpectations.first().wavelengthPm, 1e-9)
        assertEquals(QuestionOptionId.A, station.questions.single().ownSelectedOption)
        assertEquals(QuestionOptionId.B, station.questions.single().expectedOption)
        assertEquals(QuestionOptionId.C, station.questions.single().firstSelected)
        assertTrue(station.questions.single().localCheckRequested)
        assertFalse(station.questions.single().isHistorical)
        assertEquals(bundle, ReflectionBundle.fromJson(bundle.json()))
        assertFalse(bundle.json().contains("\"grade\""))
        assertNull(bundle.measurement)
    }

    @Test fun selectsOnlyExistingResponsesAndKeepsCurrentEmptyStationExplicit() {
        val empty = SessionSnapshot(visited = StationId.entries.toSet())
        assertTrue(buildReflectionBundle(empty, contexts).stations.isEmpty())
        val current = buildReflectionBundle(empty, contexts, ReflectionScope.CURRENT_STATION)
        assertEquals(StationId.S01_OBSERVATION, current.stations.single().context.station)
        assertEquals("", current.stations.single().ownResponse.explanation)
        assertTrue(current.stations.single().numericExpectations.isEmpty())
        val choiceOnly = empty.copy(questions = mapOf(QuestionId.Q03 to QuestionAttempt(QuestionOptionId.B)))
        assertEquals(StationId.S07_RECEPTION, buildReflectionBundle(choiceOnly, contexts).stations.single().context.station)
        val predictionOnly = empty.copy(work = mapOf(StationId.S05_DERIVATION to StationWork(prediction = "Meine ursprüngliche Vermutung")))
        val predicted = buildReflectionBundle(predictionOnly, contexts).stations.single()
        assertEquals("Meine ursprüngliche Vermutung", predicted.ownResponse.prediction)
        assertEquals("", predicted.ownResponse.explanation)
        assertEquals(QuestionId.Q09, predicted.questions.single().id)
    }

    @Test fun personalConclusionUsesReviewedCriteriaAndOwnUnchangedWords() {
        val own = "Mein eigenes vorläufiges Fazit, noch ohne vollständigen DG-Beleg."
        val session = SessionSnapshot(station = StationId.S16_CONCLUSION, work = mapOf(StationId.S16_CONCLUSION to StationWork(note = own)))
        val entry = buildReflectionBundle(session, contexts).stations.single()
        assertEquals(ReflectionExpectationKind.PERSONAL_CONCLUSION_RUBRIC, entry.expectationKind)
        assertEquals(own, entry.ownResponse.explanation)
        assertEquals(contexts.last().reviewedCriteria, entry.context.reviewedCriteria)
    }

    @Test fun measurementIsExplicitOptionalAndKeepsNativeImageAndCompleteReason() {
        val original = SessionSnapshot()
        val session = original.copy(measurement = original.measurement.copy(markers = listOf(NativeImageMark(551), NativeImageMark(564)), justification = "b".repeat(4000)))
        assertNull(buildReflectionBundle(session, contexts).measurement)
        val selected = buildReflectionBundle(session, contexts, includeMeasurement = true)
        assertEquals(listOf(551, 564), selected.measurement!!.ownMeasurement.markers.map { it.xPx })
        assertEquals(4000, selected.measurement.ownMeasurement.justification.length)
        assertFalse(selected.measurement.ownMeasurement.referenceViewed)
        assertEquals(selected, ReflectionBundle.fromJson(selected.json()))
    }


    @Test fun practiceOnlyWorkExportsBothNewCasesWithTheirOwnFirstSubmissionAndSources() {
        val session = SessionSnapshot(practice = mapOf(
            PracticeCaseId.U240 to PracticeAttempt().edit("100").submit().edit("79 pm").submit().revealSolution(),
            PracticeCaseId.U375 to PracticeAttempt().revealSolution(),
        ))
        val bundle = buildReflectionBundle(session, contexts)
        assertEquals(2, bundle.schemaVersion)
        val station = bundle.stations.single()
        assertEquals(StationId.S06_CALCULATION, station.context.station)
        assertEquals("", station.ownResponse.explanation)
        assertEquals("", station.ownResponse.numericAnswer)
        val practice = station.practice.associateBy { it.caseId }
        val first = practice.getValue(PracticeCaseId.U240)
        assertEquals("100", first.firstSubmission)
        assertEquals("79 pm", first.draft)
        assertTrue(first.localCheckRequested)
        assertTrue(first.solutionViewed)
        assertEquals(240.0, first.numericExpectation.voltageV)
        assertEquals(79.1654557, first.numericExpectation.wavelengthPm, 1e-6)
        assertEquals(contexts.single { it.station == StationId.S06_CALCULATION }.sources.map { it.id }, first.numericExpectation.sourceIds)
        assertTrue(first.numericExpectation.meaning.contains("keine Messunsicherheit"))
        val helped = practice.getValue(PracticeCaseId.U375)
        assertNull(helped.firstSubmission)
        assertEquals("", helped.draft)
        assertFalse(helped.localCheckRequested)
        assertTrue(helped.solutionViewed)
        assertEquals(63.3323645, helped.numericExpectation.wavelengthPm, 1e-6)
        assertEquals(bundle, ReflectionBundle.fromJson(bundle.json()))
    }

    @Test fun deliberatelyOpenedOldSolutionsAreExportedWithoutClaimingAnOwnAnswer() {
        val session = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(
            solutionViewed = true, transferSolutionViewed = true)))
        val own = buildReflectionBundle(session, contexts).stations.single().ownResponse
        assertTrue(own.solutionViewed)
        assertTrue(own.transferSolutionViewed)
        assertEquals("", own.numericAnswer)
        assertFalse(own.numericCheckRequested)
        assertFalse(own.transferCheckRequested)
    }

    @Test fun historicalChecksCannotAppearAsChecksOfTheCurrentQuestion() {
        val session = SessionSnapshot.fromJson("""{"questions":{"Q09":{"selected":"C","checked":true,"attempts":1}}}""")
        val question = buildReflectionBundle(session, contexts).stations.single().questions.single()
        assertEquals(QuestionOptionId.C, question.ownSelectedOption)
        assertNull(question.firstSelected)
        assertEquals(1, question.contentRevision)
        assertTrue(question.isHistorical)
        assertFalse(question.localCheckRequested)
        assertTrue(question.historicalCheckRequested)
        assertNull(question.selectedOptionFeedback)
        assertEquals(QuestionOptionId.C, question.expectedOption)
    }

    @Test fun aReopenedQuestionKeepsItsFirstChoiceWithoutInventingANewSelection() {
        val attempt = QuestionAttempt(selected = null, firstSelected = QuestionOptionId.A,
            attempts = 1, contentRevision = QuestionAttempt.CURRENT_CONTENT_REVISION)
        val session = SessionSnapshot(questions = mapOf(QuestionId.Q01 to attempt))
        val question = buildReflectionBundle(session, contexts).stations.single().questions.single()
        assertEquals(QuestionOptionId.A, question.firstSelected)
        assertNull(question.ownSelectedOption)
        assertNull(question.ownSelectedText)
        assertFalse(question.localCheckRequested)
        assertFalse(question.isHistorical)
    }

    @Test fun limitAndEvidenceOnlyWorkIsIncludedAndCurrentScopeDoesNotLeakOtherOwnTexts() {
        val rows = listOf(EvidenceRow("Meine DG-Behauptung", "Mein Winkelbeleg", "Strukturmodell"), EvidenceRow(), EvidenceRow())
        val session = SessionSnapshot(station = StationId.S15_LIMITS,
            inquiry = InquiryState(limitAssignments = mapOf(LimitCaseId.NR_50KV to LimitKind.SOURCE),
                limitsChecked = true, evidenceRows = rows),
            practice = mapOf(PracticeCaseId.U240 to PracticeAttempt(draft = "Privater Entwurf")))
        val all = buildReflectionBundle(session, contexts)
        assertEquals(setOf(StationId.S06_CALCULATION, StationId.S15_LIMITS, StationId.S16_CONCLUSION), all.stations.map { it.context.station }.toSet())
        val limits = all.stations.single { it.context.station == StationId.S15_LIMITS }
        assertEquals(mapOf(LimitCaseId.NR_50KV to LimitKind.SOURCE), limits.inquiryWork!!.limitAssignments)
        assertTrue(limits.inquiryWork!!.limitsCheckRequested)
        assertEquals("", limits.ownResponse.explanation) // A classification is not an invented explanation.
        val conclusion = all.stations.single { it.context.station == StationId.S16_CONCLUSION }
        assertEquals(rows, conclusion.inquiryWork!!.evidenceRows)
        assertEquals(ReflectionExpectationKind.PERSONAL_CONCLUSION_RUBRIC, conclusion.expectationKind)
        val current = buildReflectionBundle(session, contexts, ReflectionScope.CURRENT_STATION)
        assertEquals(StationId.S15_LIMITS, current.stations.single().context.station)
        assertTrue(current.stations.single().practice.isEmpty())
        assertFalse(current.json().contains("Meine DG-Behauptung"))
        assertFalse(current.json().contains("Privater Entwurf"))
        assertEquals(all, ReflectionBundle.fromJson(all.json()))
    }

    @Test fun optionalMeasurementKeepsExternalContactAndUnknownJustificationBand() {
        val first = MeasurementSnapshot(MeasurementBand.BASE, listOf(NativeImageMark(551), NativeImageMark(564)),
            "Meine ursprüngliche Auswahl", false, SessionSnapshot.IMAGE_SHA256, 703, 242,
            priorReferenceMaterials = setOf(ReferenceMaterial.UNKNOWN_LEGACY))
        val original = SessionSnapshot()
        val state = original.copy(measurement = original.measurement.copy(initialSelection = first,
            markers = first.markers, justification = "Aktueller Text", justificationBand = null,
            referenceMaterialsOpened = setOf(ReferenceMaterial.PDF_REFERENCE, ReferenceMaterial.TUTOR_CONTEXT)))
        val bundle = buildReflectionBundle(state, contexts, includeMeasurement = true)
        val measurement = bundle.measurement!!.ownMeasurement
        assertNull(measurement.justificationBand)
        assertEquals(setOf(ReferenceMaterial.PDF_REFERENCE, ReferenceMaterial.TUTOR_CONTEXT), measurement.referenceMaterialsOpened)
        assertEquals(setOf(ReferenceMaterial.UNKNOWN_LEGACY), measurement.initialSelection!!.priorReferenceMaterials)
        assertFalse(measurement.referenceViewed)
        assertEquals(bundle, ReflectionBundle.fromJson(bundle.json()))
    }

    @Test fun newReflectionFieldsAreBoundedAndCannotRelabelAResultOrHistoricalCheck() {
        assertFailsWith<IllegalArgumentException> {
            ReflectionPractice(PracticeCaseId.U240, "79 pm", null, true, false,
                ReflectionNumericExpectation(375.0, Physics.electronWavelength(Voltage(375.0)).picometers, .5))
        }
        assertFailsWith<IllegalArgumentException> { ReflectionInquiryWork(evidenceRows = listOf(EvidenceRow())) }
        assertFailsWith<IllegalArgumentException> { ReflectionInquiryWork(evidenceRows = listOf(EvidenceRow(claim = "x".repeat(1001)), EvidenceRow(), EvidenceRow())) }
        val current = SessionSnapshot(questions = mapOf(QuestionId.Q01 to QuestionAttempt(QuestionOptionId.A,
            checked = true, contentRevision = QuestionAttempt.CURRENT_CONTENT_REVISION)))
        val question = buildReflectionBundle(current, contexts).stations.single().questions.single()
        assertFailsWith<IllegalArgumentException> { question.copy(isHistorical = true) }
        val raw = buildReflectionBundle(current, contexts).json()
        assertFailsWith<IllegalArgumentException> { ReflectionBundle.fromJson(raw.replace("\"schemaVersion\": 2", "\"schemaVersion\": 1")) }
    }

    @Test fun rejectsOversizedMalformedAndUnreviewedContext() {
        assertFailsWith<IllegalArgumentException> { buildReflectionBundle(SessionSnapshot(), contexts.dropLast(1)) }
        assertFailsWith<IllegalArgumentException> { buildReflectionBundle(SessionSnapshot(), contexts.dropLast(1) + contexts.first()) }
        assertFailsWith<IllegalArgumentException> { ReflectionOwnResponse("x".repeat(4001), "", "", false, false, false) }
        assertFailsWith<IllegalArgumentException> { ReflectionBundle.fromJson("x".repeat(ReflectionBundle.MAX_BYTES + 1)) }
        val raw = buildReflectionBundle(SessionSnapshot(), contexts).json()
        assertFailsWith<IllegalArgumentException> { ReflectionBundle.fromJson(raw.replace("own-responses-with-reviewed-expectations", "automatic-grade")) }
        assertFailsWith<IllegalArgumentException> { ReflectionNumericExpectation(150.0, 999.0, .5) }
    }
}
