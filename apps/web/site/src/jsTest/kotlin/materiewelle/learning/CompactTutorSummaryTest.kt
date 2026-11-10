package materiewelle.learning

import kotlin.test.*
import materiewelle.core.*
import materiewelle.core.course.*

class CompactTutorSummaryTest {
    @Test fun questionWrittenInCalculationNotesStaysOwnWorkUntilDeliberatelyIncluded() {
        val note = "Warum muss ich hier die Wurzel ziehen?"
        val snapshot = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = note)))
        val own = compactOwnWork(snapshot, AppSection.DE_BROGLIE)
        val before = snapshot.json()
        val withoutOwn = tutorCopySummary("", own, includeOwn = false)
        assertFalse(withoutOwn.questionPresent)
        assertTrue(withoutOwn.ownWrittenWorkPresent)
        assertFalse(withoutOwn.ownWorkIncluded)
        assertContains(withoutOwn.questionText, "Du kannst eine Frage ergänzen")
        assertContains(withoutOwn.questionText, "gewählten Hilfeform")
        assertContains(tutorOwnWorkSummary(own, withoutOwn), "nicht mitkopiert")
        val selected = tutorCopySummary("", own, includeOwn = true)
        assertTrue(selected.ownWorkIncluded)
        assertFalse(selected.questionPresent)
        assertEquals(note, assertNotNull((own as CompactOwnWork.DeBroglie).requiredCalculation).calculationNote)
        assertEquals(before, snapshot.json())
    }

    @Test fun summaryDistinguishesActualQuestionFromEmptyWorkAndKeepsTaskModelRoles() {
        val snapshot = SessionSnapshot(deBroglie = DeBroglieSession(raw = "600", lastValidVoltage = 600.0))
        val own = compactOwnWork(snapshot, AppSection.DE_BROGLIE)
        val written = tutorCopySummary("Bitte prüfe meinen Ansatz.", own, includeOwn = true)
        assertTrue(written.questionPresent)
        assertFalse(written.ownWrittenWorkPresent)
        assertContains(tutorOwnWorkSummary(own, written), "150-V-Aufgabe")
        assertContains(tutorOwnWorkSummary(own, written), "freien Modells")
        assertFalse(tutorCopySummary(" \n ", own, includeOwn = false).questionPresent)
    }

    @Test fun invalidEditedVoltageNeverMasqueradesAsTheAcceptedModelValue() {
        val snapshot = SessionSnapshot(deBroglie = DeBroglieSession(raw = "ungültig", lastValidVoltage = 150.0))
        val own = assertIs<CompactOwnWork.DeBroglie>(compactOwnWork(snapshot, AppSection.DE_BROGLIE))
        assertEquals("ungültig", assertNotNull(own.modelVariation).editedVoltage)
        assertEquals(150.0, assertNotNull(own.modelVariation).acceptedVoltageV)
        val summary = tutorOwnWorkSummary(own, tutorCopySummary("Warum?", own, includeOwn = true))
        assertContains(summary, "Eingegeben: ungültig")
        assertContains(summary, "im Modell verwendet: 150 V")
    }

    @Test fun emptyImageAndConclusionDoNotClaimWrittenWorkOrAnAttachedPicture() {
        val snapshot = SessionSnapshot()
        for (section in listOf(AppSection.JONSSON, AppSection.CONCLUSION)) {
            val own = compactOwnWork(snapshot, section)
            val summary = tutorCopySummary("", own, includeOwn = true)
            assertFalse(summary.ownWrittenWorkPresent)
            val text = tutorOwnWorkSummary(own, summary)
            if (section == AppSection.JONSSON) {
                assertContains(text, "Das Bild selbst wird nicht mitkopiert")
                assertContains(text, "0 aktuelle Marken")
                assertContains(text, "Die aktuelle Bildauswahl ist leer")
                assertFalse(text.contains("deine Bildmarken"))
                assertFalse(text.contains("deine Begründung"))
            } else assertContains(text, "noch keine eigenen Angaben")
        }
    }

    @Test fun closingAndReopeningDoesNotReplaceAQuestionOrPromoteScientificNotes() {
        val question = "Meine bereits formulierte eigene Frage."
        val state = CompactTutorUiState(mapOf(AppSection.DE_BROGLIE to question))
        state.openFrom(TutorHelpPoint.CALCULATION, 240.0)
        state.closeFor(AppSection.DE_BROGLIE)
        state.openFrom(TutorHelpPoint.DG_COMPARISON, 180.0)
        state.closeFor(AppSection.DAVISSON_GERMER)
        state.openFrom(TutorHelpPoint.CALCULATION, 240.0)
        assertEquals(question, state.drafts().getValue(AppSection.DE_BROGLIE))
        assertEquals(TutorHelpPoint.CALCULATION, state.originFor(AppSection.DE_BROGLIE))
        assertEquals(240.0, state.returnTop)
    }

    @Test fun localCalculationSummaryDoesNotPromiseTheSeparateFreeModel() {
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "777", lastValidVoltage = 777.0, comparisonVoltage = 333.0),
            work = mapOf(StationId.S06_CALCULATION to StationWork(numericAnswer = "100 pm", note = "Eigene Rechnung")),
        )
        val task = compactOwnWork(snapshot, AppSection.DE_BROGLIE, TutorHelpPoint.CALCULATION)
        val taskText = includedSummary(task)
        assertContains(taskText, "150-V-Rechnung")
        assertFalse(taskText.contains("777"))
        assertFalse(taskText.contains("333"))
        assertFalse(taskText.contains("freien Modells"))

        val model = compactOwnWork(snapshot, AppSection.DE_BROGLIE, TutorHelpPoint.DE_BROGLIE_MODEL)
        val modelText = includedSummary(model)
        assertContains(modelText, "777")
        assertContains(modelText, "333")
        assertFalse(modelText.contains("150-V-Aufgabe"))
        assertFalse(tutorCopySummary("", model, includeOwn = true).ownWrittenWorkPresent)
    }

    @Test fun dgSummaryNamesCurrentAndRememberedValuesOnlyForTheModelScope() {
        val snapshot = SessionSnapshot(
            dg = DgSession(modelVoltageRaw = "777", validModelVoltage = 777.0,
                modelAngleRaw = "61", validModelAngle = 61.0,
                comparisonVoltage = 333.0, comparisonAngle = 42.0),
            work = mapOf(StationId.S11_DG_EVALUATION to StationWork(note = "Mein Vergleich")),
        )
        val task = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER, TutorHelpPoint.DG_COMPARISON)
        val model = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER, TutorHelpPoint.DG_MODEL)
        val taskText = includedSummary(task)
        assertFalse(taskText.contains("Modellwerte"))
        val modelText = includedSummary(model)
        for (value in listOf("777", "61", "333", "42")) {
            assertContains(modelText, value)
            assertFalse(taskText.contains(value))
        }
        assertTrue(tutorCopySummary("", task, includeOwn = true).ownWrittenWorkPresent)
        assertFalse(tutorCopySummary("", model, includeOwn = true).ownWrittenWorkPresent)
    }

    @Test fun savedImageSelectionsRemainOwnWorkWhenCurrentMarkersHaveBeenCleared() {
        val first = MeasurementSnapshot(MeasurementBand.BASE,
            listOf(NativeImageMark(551), NativeImageMark(564)), "Meine erste Auswahl", false,
            SessionSnapshot.IMAGE_SHA256, 703, 242)
        val comparison = first.copy(markers = listOf(NativeImageMark(552), NativeImageMark(566)),
            justification = "Mein gespeicherter Vergleich")
        val snapshot = SessionSnapshot(measurement = MeasurementSession(
            initialSelection = first, comparison = comparison,
        ))
        val own = compactOwnWork(snapshot, AppSection.JONSSON, TutorHelpPoint.IMAGE_MEASUREMENT)
        val summary = tutorCopySummary("Vergleiche meine gespeicherten Auswahlen.", own, includeOwn = true)
        assertTrue(summary.ownWrittenWorkPresent)
        val text = tutorOwnWorkSummary(own, summary)
        assertContains(text, "festgehaltene Auswahl")
        assertContains(text, "gespeicherter Messvergleich")
        assertContains(text, "0 aktuelle Marken")
        assertContains(text, "festgehaltene Auswahl (2 Marken)")
        assertContains(text, "gespeicherter Messvergleich (2 Marken)")
        assertContains(text, "Die aktuelle Bildauswahl ist leer")
        assertFalse(text.contains("noch keine eigenen Angaben"))
        assertFalse(text.contains("Eigene Marken oder Notizen hast du noch nicht eingetragen"))
        assertContains(text, "Das Bild selbst wird nicht mitkopiert")
        assertEquals("Deine eigenen Angaben werden nicht mitkopiert.",
            tutorOwnWorkSummary(own, tutorCopySummary("", own, includeOwn = false)))
    }

    @Test fun localSlitNotesCountAsWrittenWorkWithoutClaimingAnAttachedImage() {
        val snapshot = SessionSnapshot(modelSlitSpacingUm = 3.4, modelSlitWidthUm = .7,
            inquiry = InquiryState(slitComparison = SlitModelComparison(spacingUm = 2.2, widthUm = .4)))
        val extras = CompactTutorExtras(CompactSlitExploration(
            CompactSlitConfiguration.LEFT_ONLY, CompactSlitTask.WIDTH,
            mapOf(CompactSlitTask.SPACING to CompactSlitNotes(reason = "Meine Abstandsbegründung")),
        ))
        val own = compactOwnWork(snapshot, AppSection.JONSSON, TutorHelpPoint.SLIT_MODEL, extras)
        val summary = tutorCopySummary("", own, includeOwn = true)
        assertTrue(summary.ownWrittenWorkPresent)
        val text = tutorOwnWorkSummary(own, summary)
        for (value in listOf("3,4", "0,7", "2,2", "0,4")) assertContains(text, value)
        assertFalse(text.contains("Bildmarken"))
        assertFalse(text.contains("Messkontext"))
    }

    @Test fun earlierWorkOnlyCountsTowardConclusionSummaryAfterExplicitInclusion() {
        val snapshot = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = "Frühere Rechnung")))
        val own = compactOwnWork(snapshot, AppSection.CONCLUSION, TutorHelpPoint.CONCLUSION)
        assertFalse(tutorCopySummary("", own, includeOwn = true).ownWrittenWorkPresent)
        assertContains(includedSummary(own), "noch keine eigenen Angaben")
        val withEarlier = compactOwnWork(snapshot, AppSection.CONCLUSION, TutorHelpPoint.CONCLUSION,
            includeEarlierResults = true)
        assertTrue(tutorCopySummary("", withEarlier, includeOwn = true).ownWrittenWorkPresent)
        assertNotEquals(includedSummary(own), includedSummary(withEarlier))
    }

    @Test fun previewDetailsShowActualComparisonValuesAndBoundOnlyTheirNoteExcerpts() {
        val note = "Mein Ansatz\n" + "Ich begründe meinen Rechenweg. ".repeat(40)
        val snapshot = SessionSnapshot(
            dg = DgSession(modelVoltageRaw = "777", validModelVoltage = 777.0,
                modelAngleRaw = "61", validModelAngle = 61.0,
                comparisonVoltage = 333.0, comparisonAngle = 42.0),
            work = mapOf(StationId.S11_DG_EVALUATION to StationWork(note = note)),
        )
        val own = assertIs<CompactOwnWork.Surface>(compactOwnWork(snapshot, AppSection.DAVISSON_GERMER))
        val details = tutorOwnWorkDetails(own)
        assertContains(details, "DG-Modell B: 777 V / 61° zur Normalen")
        assertContains(details, "Gemerkte Einstellung A: 333 V / 42°")
        assertTrue(details.any { it.contains("Mein Ansatz") && it.endsWith("…") })
        assertTrue(details.all { it.length <= 160 && '\n' !in it && '\r' !in it })
        assertEquals(note, own.notes.single().note)
        assertEquals(note, snapshot.stationWork(StationId.S11_DG_EVALUATION).note)
    }

    @Test fun taskAndModelPreviewDetailsExposeOnlyTheContextNamedByTheirCheckbox() {
        val note = "Nur meine persönliche Pflichtrechnung"
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "777", lastValidVoltage = 777.0, comparisonVoltage = 333.0),
            work = mapOf(StationId.S06_CALCULATION to StationWork(note = note, numericAnswer = "100 pm")),
        )
        val task = compactOwnWork(snapshot, AppSection.DE_BROGLIE, TutorHelpPoint.CALCULATION)
        val model = compactOwnWork(snapshot, AppSection.DE_BROGLIE, TutorHelpPoint.DE_BROGLIE_MODEL)
        val taskDetails = tutorOwnWorkDetails(task).joinToString("\n")
        val modelDetails = tutorOwnWorkDetails(model).joinToString("\n")
        assertContains(taskDetails, note)
        assertContains(taskDetails, "100 pm")
        assertFalse(taskDetails.contains("777"))
        assertFalse(taskDetails.contains("333"))
        assertContains(modelDetails, "777 V")
        assertContains(modelDetails, "333 V")
        assertFalse(modelDetails.contains(note))
        assertFalse(modelDetails.contains("150-V-Aufgabe"))
        assertEquals("Meine Rechnung bei 150 Volt hinzufügen", tutorOwnWorkSelectionLabel(TutorHelpPoint.CALCULATION))
        assertEquals("Meine aktuellen und gemerkten Spannungswerte hinzufügen", tutorOwnWorkSelectionLabel(TutorHelpPoint.DE_BROGLIE_MODEL))
    }

    private fun includedSummary(own: CompactOwnWork): String =
        tutorOwnWorkSummary(own, tutorCopySummary("", own, includeOwn = true))
}
