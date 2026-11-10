package materiewelle.learning

import kotlin.test.*
import materiewelle.core.*
import materiewelle.core.course.*

class CompactTutorMessageTest {
    private val guide = "De Broglies Hypothese ist λ = h/p. Die Rechnung setzt Start aus Ruhe voraus."

    @Test fun messageStartsWithTheActualQuestionAndOmitsInternalMetadata() {
        val question = "Warum brauche ich hier eine Wurzel?\nBitte gib mir zuerst einen Hinweis."
        val text = compactTutorMessage(AppSection.DE_BROGLIE, question, guide, null)
        assertContains(text, "Kapitel: De Broglie")
        assertContains(text, "## Meine Frage\n$question")
        assertTrue(text.indexOf(question) < text.indexOf(guide))
        assertContains(text, "Keine zusätzlichen Angaben aus den Arbeitsfeldern beigefügt")
        assertTrue(text.indexOf("## Meine freigegebenen Angaben") < text.indexOf("## Fachlicher Kontext"))
        for (hidden in listOf("schemaVersion", "packageVersion", "Paketversion", "courseRevision", "sourceRevision", "ownWork", "```json", "references/", "lokale Dateien"))
            assertFalse(text.contains(hidden), hidden)
    }

    @Test fun personalWorkOnlyAppearsWhenDeliberatelySelected() {
        val secret = "Mein eigener noch unvollständiger Rechenweg."
        val snapshot = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(note = secret)))
        val own = compactOwnWork(snapshot, AppSection.DE_BROGLIE)
        assertFalse(compactTutorMessage(AppSection.DE_BROGLIE, "Hilf mir", guide, null).contains(secret))
        assertContains(compactTutorMessage(AppSection.DE_BROGLIE, "Hilf mir", guide, own), secret)
        assertEquals(secret, snapshot.stationWork(StationId.S06_CALCULATION).note)
    }

    @Test fun requiredAnswerAndModelVariationHaveSeparateReadableCasesAndComputedExpectations() {
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "600", lastValidVoltage = 600.0),
            work = mapOf(StationId.S06_CALCULATION to StationWork(numericAnswer = "100 pm", note = "Meine eigene 150-V-Rechnung")),
        )
        val before = snapshot.json()
        val text = compactTutorMessage(AppSection.DE_BROGLIE, "Ist mein Ergebnis richtig?", guide, compactOwnWork(snapshot, AppSection.DE_BROGLIE))
        val task = text.substringAfter("### Meine feste Aufgabe bei 150 V").substringBefore("### Aktuelle Einstellung des Spannungsmodells")
        assertContains(task, "Mein Ergebnis:\n  100 pm")
        assertContains(task, "100,137 pm")
        assertFalse(task.contains("600 V"))
        val model = text.substringAfter("### Aktuelle Einstellung des Spannungsmodells")
        assertContains(model, "Aktuelle Einstellung B · im Modell verwendet: 600 V.")
        assertContains(model, "50,069 pm")
        assertContains(model, "ändert die feste 150-V-Aufgabe nicht")
        assertEquals(before, snapshot.json())
    }

    @Test fun unfinishedInputIsNotPresentedAsAcceptedModelValue() {
        val own = compactOwnWork(SessionSnapshot(deBroglie = DeBroglieSession(raw = "6e", lastValidVoltage = 150.0)), AppSection.DE_BROGLIE)
        val text = compactTutorMessage(AppSection.DE_BROGLIE, "Warum?", guide, own)
        assertContains(text, "im Modell verwendet: 150 V.")
        assertContains(text, "Eingabetext des Spannungsfelds:\n  6e")
        assertContains(text, "noch nicht als neuer Modellwert übernommen")
    }

    @Test fun dgCopiesActualAnswersAndDistinctModelCheckValues() {
        val snapshot = SessionSnapshot(dg = DgSession(modelVoltageRaw = "54", validModelVoltage = 54.0, modelAngleRaw = "50", validModelAngle = 50.0),
            work = mapOf(StationId.S11_DG_EVALUATION to StationWork(numericAnswer = "0,165 nm", prediction = "Meine Vorhersage: 0,167 nm")))
        val text = compactTutorMessage(AppSection.DAVISSON_GERMER, "Wie passt das zusammen?", guide, compactOwnWork(snapshot, AppSection.DAVISSON_GERMER))
        assertContains(text, "Mein Ergebnis:\n  0,165 nm")
        assertContains(text, "Meine Vorhersage: 0,167 nm")
        assertContains(text, "0,166895 nm")
        assertContains(text, "0,165125 nm")
        assertContains(text, "äußeren Oberflächennormale")
        assertContains(text, "Kein statistischer Übereinstimmungsnachweis ohne Messunsicherheiten")
        assertFalse(text.contains("150-V-Aufgabe"))
    }

    @Test fun unchangedPresetIsCurrentContextWithoutInventedPersonalEdits() {
        val snapshot = SessionSnapshot()
        val before = snapshot.json()
        val selected = compactOwnWork(snapshot, AppSection.DAVISSON_GERMER, TutorHelpPoint.DG_MODEL)
        val text = compactTutorMessage(AppSection.DAVISSON_GERMER, "", guide, selected, point = TutorHelpPoint.DG_MODEL)
        assertContains(text, "### Aktuelle Einstellung des Davisson–Germer-Modells")
        assertContains(text, "Unveränderte Modellvorgaben sind keine selbst erarbeiteten Ergebnisse")
        assertFalse(text.contains("Mein frei verändertes"))
        assertContains(text, "Aktuelle Einstellung B: 54 V und 50°")
        assertFalse(compactTutorMessage(AppSection.DAVISSON_GERMER, "", guide, null).contains("Aktuelle Einstellung B:"))
        assertEquals(before, snapshot.json())
    }

    @Test fun unsupportedDgContextDoesNotInventANonrelativisticCheckValue() {
        val snapshot = SessionSnapshot(dg = DgSession(modelVoltageRaw = "50000", validModelVoltage = 50000.0))
        val text = compactTutorMessage(AppSection.DAVISSON_GERMER, "Stimmt das?", guide, compactOwnWork(snapshot, AppSection.DAVISSON_GERMER))
        assertContains(text, "50000 V")
        assertContains(text, "kein Kontrollwert beigefügt")
        assertFalse(text.contains("λ_NR ≈"))
    }

    @Test fun imageMessagePreservesLongReasonAndSeparatesCurrentInitialAndComparison() {
        val reason = "Meine begründete Maximalwahl. ".repeat(80)
        val first = MeasurementSnapshot(MeasurementBand.BASE, listOf(NativeImageMark(551), NativeImageMark(564)), reason, false, SessionSnapshot.IMAGE_SHA256, 703, 242)
        val comparison = first.copy(markers = listOf(NativeImageMark(551), NativeImageMark(566)))
        val snapshot = SessionSnapshot(measurement = MeasurementSession(markers = listOf(NativeImageMark(552), NativeImageMark(564)),
            justification = reason, initialSelection = first, comparison = comparison, referenceViewed = true))
        val text = compactTutorMessage(AppSection.JONSSON, "Wie sicher ist meine Auswahl?", guide, compactOwnWork(snapshot, AppSection.JONSSON))
        val current = text.substringAfter("### Meine aktuelle Bildauswahl").substringBefore("### Meine zuerst festgehaltene Auswahl")
        assertContains(current, "703 × 242 Originalpixel")
        assertContains(current, "100 ≤ y < 220")
        assertContains(current, "Mittlerer Abstand: 12,000 Originalpixel")
        assertContains(current, reason)
        assertContains(text.substringAfter("### Meine zuerst festgehaltene Auswahl").substringBefore("### Mein gespeicherter Messvergleich A"), "13,000 Originalpixel")
        assertContains(text.substringAfter("### Mein gespeicherter Messvergleich A"), "15,000 Originalpixel")
        assertContains(text, "Das Bild selbst ist nicht beigefügt")
        assertContains(text, "keine absolute Wellenlänge")
        assertContains(text, "keine kalibrierte Elektronenintensität")
        assertFalse(text.contains("measurement.selectionReason"))
    }

    @Test fun emptyImageAndConclusionStayUnknownRatherThanInventingWork() {
        val image = compactTutorMessage(AppSection.JONSSON, "", guide, compactOwnWork(SessionSnapshot(), AppSection.JONSSON))
        assertContains(image, "aktuell keine gesetzt")
        assertContains(image, "kein mittlerer Abstand bestimmt")
        assertFalse(image.contains("Mittlerer Abstand: 0"))
        val conclusion = compactTutorMessage(AppSection.CONCLUSION, "", guide, compactOwnWork(SessionSnapshot(), AppSection.CONCLUSION))
        assertContains(conclusion, TutorIntent.EXPLANATION.defaultQuestion)
        assertContains(conclusion, "Mein Text:\n  (noch nicht eingetragen)")
        assertContains(conclusion, "Eigene Aussagen, Belege und Grenzen sind noch nicht eingetragen")
    }

    @Test fun eachIntentProvidesOneExplicitInstructionAndAnOptionalQuestion() {
        for (intent in TutorIntent.entries) {
            val text = compactTutorMessage(AppSection.DE_BROGLIE, " \n ", guide, null,
                intent, TutorHelpPoint.CALCULATION)
            assertContains(text, "## Meine Frage\n${intent.defaultQuestion}")
            assertContains(text, "## Gewünschte Hilfe: ${intent.label}\n${intent.instruction}")
            assertEquals(1, text.split(intent.instruction).size - 1)
            assertContains(text, "Aufgabenkontext: ${TutorHelpPoint.CALCULATION.heading}")
            assertTrue(text.indexOf(intent.instruction) < text.indexOf("## Meine freigegebenen Angaben"))
            assertTrue(text.indexOf("## Meine freigegebenen Angaben") < text.indexOf(guide))
            assertFalse(text.contains("Noch keine konkrete Frage"))
        }
        val ownQuestion = "Mein Rechenweg steht direkt hier: eU = p²/(2m)."
        val text = compactTutorMessage(AppSection.DE_BROGLIE, ownQuestion, guide, null, TutorIntent.CHECK)
        assertContains(text, ownQuestion)
        assertContains(text, "Maßgeblich sind nur die tatsächlich in dieser Nachricht enthaltenen Angaben")
        assertFalse(text.contains("Es liegt keine eigene Rechnung"))
    }

    @Test fun rememberedModelValuesAreCopiedSeparatelyFromCurrentValuesAndLocalTasks() {
        val snapshot = SessionSnapshot(
            deBroglie = DeBroglieSession(raw = "777", lastValidVoltage = 777.0, comparisonVoltage = 333.0),
            dg = DgSession(modelVoltageRaw = "777", validModelVoltage = 777.0,
                modelAngleRaw = "61", validModelAngle = 61.0, comparisonVoltage = 333.0, comparisonAngle = 42.0),
            work = mapOf(StationId.S11_DG_EVALUATION to StationWork(prediction = "Meine feste Vorhersage")),
        )
        val before = snapshot.json()
        val db = compactTutorMessage(AppSection.DE_BROGLIE, "Vergleiche A und B", guide,
            compactOwnWork(snapshot, AppSection.DE_BROGLIE, TutorHelpPoint.DE_BROGLIE_MODEL))
        assertContains(db, "Aktuelle Einstellung B · im Modell verwendet: 777 V")
        assertContains(db, "Gemerkte Einstellung A: 333 V")
        assertFalse(db.contains("### Meine feste Aufgabe"))
        val dg = compactTutorMessage(AppSection.DAVISSON_GERMER, "Vergleiche A und B", guide,
            compactOwnWork(snapshot, AppSection.DAVISSON_GERMER, TutorHelpPoint.DG_MODEL))
        assertContains(dg, "Aktuelle Einstellung B: 777 V und 61°")
        assertContains(dg, "Gemerkte Einstellung A: 333 V und 42°")
        assertFalse(dg.contains("Meine feste Vorhersage"))
        val task = compactTutorMessage(AppSection.DAVISSON_GERMER, "Prüfe den Quellenvergleich", guide,
            compactOwnWork(snapshot, AppSection.DAVISSON_GERMER, TutorHelpPoint.DG_COMPARISON))
        assertContains(task, "Meine feste Vorhersage")
        assertFalse(task.contains("777 V"))
        assertFalse(task.contains("333 V"))
        assertEquals(before, snapshot.json())
    }

    @Test fun slitHandoffContainsBothSettingsAndActualExplorationWithoutImageData() {
        val snapshot = SessionSnapshot(modelSlitSpacingUm = 4.7, modelSlitWidthUm = .8,
            inquiry = InquiryState(slitComparison = SlitModelComparison(spacingUm = 3.3, widthUm = .4)))
        val extras = CompactTutorExtras(CompactSlitExploration(
            CompactSlitConfiguration.RIGHT_ONLY, CompactSlitTask.WIDTH,
            mapOf(CompactSlitTask.SPACING to CompactSlitNotes(prediction = "Abstandsprognose"),
                CompactSlitTask.WIDTH to CompactSlitNotes(reason = "Breitenerklärung")), .002,
        ))
        val text = compactTutorMessage(AppSection.JONSSON, "Vergleiche meine Modelle", guide,
            compactOwnWork(snapshot, AppSection.JONSSON, TutorHelpPoint.SLIT_MODEL, extras))
        assertContains(text, "Aktuelle Einstellung B: g = 4,7 µm; b = 0,8 µm; U = 150 V; L = 10 m")
        assertContains(text, "Gemerkte Einstellung A: g = 3,3 µm; b = 0,4 µm")
        assertContains(text, "nur rechter Spalt")
        assertContains(text, "Spaltbreite verändern")
        assertContains(text, "2,000000 mm")
        assertContains(text, "Abstandsprognose")
        assertContains(text, "Breitenerklärung")
        assertContains(text, "weder Parameter der historischen Aufnahme noch ein Bildmaßstab")
        assertFalse(text.contains("### Meine aktuelle Bildauswahl"))
        assertFalse(text.contains("nur aus diesem Tab"))
        val image = compactTutorMessage(AppSection.JONSSON, "Meine Messung", guide,
            compactOwnWork(snapshot, AppSection.JONSSON, TutorHelpPoint.IMAGE_MEASUREMENT, extras))
        assertFalse(image.contains("Abstandsprognose"))
        assertFalse(image.contains("4,7 µm"))
    }

    @Test fun conclusionPreservesRowNumbersAndEarlierWorkRequiresSeparateInclusion() {
        val snapshot = SessionSnapshot(
            work = mapOf(StationId.S06_CALCULATION to StationWork(note = "Meine frühere Rechnung")),
            inquiry = InquiryState(evidenceRows = listOf(EvidenceRow(), EvidenceRow(claim = "Mein zweiter Gedanke"),
                EvidenceRow(evidence = "Mein dritter Beleg"))),
        )
        val onlyConclusion = compactTutorMessage(AppSection.CONCLUSION, "Prüfe meine Argumentation", guide,
            compactOwnWork(snapshot, AppSection.CONCLUSION))
        assertContains(onlyConclusion, "Aussage 2:\nBehauptung:\n  Mein zweiter Gedanke")
        assertContains(onlyConclusion, "Aussage 3:")
        assertFalse(onlyConclusion.contains("Aussage 1:"))
        assertFalse(onlyConclusion.contains("Meine frühere Rechnung"))
        val withEarlier = compactTutorMessage(AppSection.CONCLUSION, "Prüfe meine Argumentation", guide,
            compactOwnWork(snapshot, AppSection.CONCLUSION, includeEarlierResults = true))
        assertContains(withEarlier, "Zusätzlich freigegebene frühere Ergebnisse")
        assertContains(withEarlier, "Meine frühere Rechnung")
    }

    @Test fun savedReferenceFlagDoesNotClaimToDescribeTheReadersCurrentLocalView() {
        val text = compactTutorMessage(AppSection.JONSSON, "Meine lokal geöffnete Referenz", guide,
            compactOwnWork(SessionSnapshot(), AppSection.JONSSON, TutorHelpPoint.IMAGE_MEASUREMENT))
        assertContains(text, "Öffnung der Referenz im gespeicherten Arbeitsstand vermerkt: nein")
        assertContains(text, "erfasst keine zusätzliche lokale Öffnung in einem schreibgeschützten Tab")
        assertFalse(text.contains("Dokumentierte Referenz in der Website geöffnet: nein"))
    }
}
