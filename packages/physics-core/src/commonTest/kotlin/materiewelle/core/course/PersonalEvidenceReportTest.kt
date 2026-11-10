package materiewelle.core.course

import kotlin.test.*
import materiewelle.core.*

class PersonalEvidenceReportTest {
    @Test
    fun dgExportPreservesBothIndependentRoutesAndInterpretationExactlyOnce() {
        val work = StationWork(prediction = "QA: Spannung 0,167 nm", numericAnswer = "QA: Geometrie 0,165 nm", note = "QA: Unabhängige Struktur vorausgesetzt", solutionViewed = true)
        val report = SessionSnapshot(work = mapOf(StationId.S11_DG_EVALUATION to work)).personalEvidenceReport()
        assertTrue(report.hasOwnWork)
        val comparison = assertNotNull(report.dgComparison)
        assertTrue(comparison.controlViewed)
        assertEquals(work.prediction, comparison.prediction)
        assertEquals(work.numericAnswer, comparison.geometricAnswer)
        assertEquals(work.note, comparison.interpretation)
        for (value in listOf(work.prediction, work.numericAnswer, work.note)) {
            assertEquals(1, report.readableText().windowed(value.length).count { it == value })
        }
        assertTrue(report.notes.none { it.station == StationId.S11_DG_EVALUATION })
        assertTrue(report.calculations.isEmpty()) // No unrelated wavelength checker is applied.
    }

    @Test
    fun dgGeometryOnlyIsOwnWorkButOpenedControlDoesNotInventAnAnswer() {
        val geometryOnly = SessionSnapshot(work = mapOf(StationId.S11_DG_EVALUATION to StationWork(numericAnswer = "Mein geometrisches Ergebnis"))).personalEvidenceReport()
        assertTrue(geometryOnly.hasOwnWork)
        assertEquals("Mein geometrisches Ergebnis", assertNotNull(geometryOnly.dgComparison).geometricAnswer)
        assertTrue(geometryOnly.readableText().contains("Vorhersage aus der Spannung: noch offen"))
        val controlOnly = SessionSnapshot(work = mapOf(StationId.S11_DG_EVALUATION to StationWork(solutionViewed = true))).personalEvidenceReport()
        assertFalse(controlOnly.hasOwnWork)
        assertFalse(assertNotNull(controlOnly.dgComparison).hasOwnInputs)
        assertTrue(controlOnly.readableText().contains("Ergebnis aus der Beugungsgeometrie: noch offen"))
        assertNull(SessionSnapshot().personalEvidenceReport().dgComparison)
    }

    @Test
    fun courseSourcesSupplementWithoutReplacingLoadedProvenance() {
        val courseSources = mapOf(
            "H1" to SourceReference("Spätere Erinnerung, Druck-S. 22; keine zeitgenössische Originalprüfung", "https://example.org/history"),
            "D2" to SourceReference("Abweichende Kurszusammenfassung"),
        )
        val withData = SessionSnapshot().personalEvidenceReport(data(), courseSources)
        assertEquals(data().sources.getValue("D2"), withData.sources.getValue("D2"))
        assertEquals(courseSources.getValue("H1"), withData.sources.getValue("H1"))
        assertTrue(withData.readableText().contains("https://example.org/history"))
        assertTrue(withData.readableText().contains("keine zeitgenössische Originalprüfung"))
        assertEquals(courseSources, SessionSnapshot().personalEvidenceReport(courseSources = courseSources).sources)
        assertFalse(withData.hasOwnWork)
    }

    @Test
    fun readableBibliographyAndImportedProvenanceAreSeparateWithoutLosingEither() {
        val citations = mapOf(
            "D1" to SourceReference("C. Davisson und L. H. Germer: Diffraction of Electrons by a Crystal of Nickel. Physical Review 30 (1927), 705–740. Original nicht eingesehen.", "https://doi.org/10.1103/PhysRev.30.705"),
            "D2" to SourceReference("M. Suzuki und I. S. Suzuki: A proper understanding of the Davisson and Germer experiments (2013). arXiv:1307.6049.", "https://arxiv.org/abs/1307.6049"),
            "J1" to SourceReference("C. Jönsson: Electron Diffraction at Multiple Slits. AJP 42 (1974), 4–11. Gekürzte Übersetzung.", "https://doi.org/10.1119/1.1987592"),
        )
        val provenance = SourceReference("Abruf HTTP 403; QUELLENAKTE unverändert", path = "private/QUELLENAKTE.md")
        val imported = data().copy(sources = mapOf("D2" to provenance))
        val state = SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(numericAnswer = "100 nm", note = "Mein möglicherweise falscher Rechenweg", hintsOpened = 1)))
        val before = state.json()
        val report = state.personalEvidenceReport(imported, citations)
        assertEquals(citations, report.bibliography)
        assertEquals(provenance, report.sources.getValue("D2"))
        val text = report.readableText(includeTechnicalAppendix = true)
        assertFalse(report.readableText().contains("TECHNISCHER ANHANG"))
        assertFalse(report.readableText().contains(provenance.status))
        val main = text.substringBefore("TECHNISCHER ANHANG")
        val appendix = text.substringAfter("TECHNISCHER ANHANG")
        citations.values.forEach { assertTrue(main.contains(it.status)) }
        assertFalse(main.contains("HTTP 403"))
        assertFalse(main.contains("QUELLENAKTE"))
        assertFalse(main.contains("SHA-256"))
        assertFalse(main.contains("Station 6"))
        assertTrue(main.contains("Eingabe: 100 nm")) // Never replace a wrong own answer.
        assertTrue(main.indexOf("Mein möglicherweise falscher Rechenweg") < main.indexOf("Eingabe: 100 nm"))
        assertTrue(appendix.contains(provenance.status))
        assertTrue(appendix.contains(provenance.path!!))
        assertTrue(appendix.contains("Hinweiszähler 1"))
        assertEquals(before, state.json())
    }

    @Test
    fun rasterDiagnosticsStayInAppendixWhenReaderBibliographyIsExplicit() {
        val citation = SourceReference("C. Jönsson: Electron Diffraction at Multiple Slits. AJP 42 (1974), 4–11.")
        val raster = SourceReference("J1-Raster; SHA-256 ${SessionSnapshot.IMAGE_SHA256}")
        val allSources = mapOf("J1" to citation, "B1" to raster)
        val report = SessionSnapshot().personalEvidenceReport(
            courseSources = allSources,
            readerBibliography = mapOf("J1" to citation),
        )
        assertEquals(allSources, report.sources)
        assertEquals(mapOf("J1" to citation), report.bibliography)
        val text = report.readableText(includeTechnicalAppendix = true)
        assertFalse(report.readableText().contains("SHA-256"))
        assertTrue(text.substringBefore("TECHNISCHER ANHANG").contains(citation.status))
        assertFalse(text.substringBefore("TECHNISCHER ANHANG").contains("SHA-256"))
        assertTrue(text.substringAfter("TECHNISCHER ANHANG").contains(raster.status))
    }

    @Test
    fun requestedHandoutAppearsAsContactWithoutInventingReadingOrOwnMeasurement() {
        val state = SessionSnapshot(measurement = MeasurementSession().recordReferenceMaterial(ReferenceMaterial.HANDOUT_REQUESTED))
        val report = state.personalEvidenceReport()
        assertFalse(report.hasOwnWork)
        assertNull(report.measurement)
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("Handout-Zugriff angefordert"))
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("weder einen erfolgreichen Download noch Lesen oder Verständnis"))
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("Direkte PDF-Aufrufe"))
    }

    @Test
    fun conclusionOnlyDoesNotBecomeAnAbsentOrDuplicatedStationExplanation() {
        val conclusion = "QA: Mein selbst geschriebenes Fazit."
        val report = SessionSnapshot(work = mapOf(
            StationId.S16_CONCLUSION to StationWork(note = conclusion)
        )).personalEvidenceReport()
        assertTrue(report.hasOwnWork)
        assertTrue(report.notes.isEmpty())
        assertTrue(report.readableText().contains(report.emptyNotesMessage))
        assertFalse(report.readableText().contains("Noch keine eigene Vorhersage oder Erklärung gespeichert."))
        assertEquals(1, report.readableText().windowed(conclusion.length).count { it == conclusion })
    }

    private fun image() =
        JonssonData(
            "raster.png",
            703,
            242,
            listOf(551.0, 564.0),
            160.0,
            50_000.0,
            2e-6,
            0.35,
            "Keine Kalibrierung",
            490.0,
            100.0,
            213.0,
            120.0,
            SessionSnapshot.IMAGE_SHA256,
        )

    private fun data(image: JonssonData = image()): ScientificData {
        val evidence =
            ScientificEvidence(
                MaterialRole.SECONDARY_REPORT,
                EvidenceReview.CHECKED_IN_SCOPE,
                "D2",
                "synthetic test",
                "test only",
            )
        val structure =
            Nickel111Structure(
                Length(.352e-9),
                evidence.copy(role = MaterialRole.INDEPENDENT_STRUCTURE_ASSUMPTION),
            )
        return ScientificData(
            emptyList(),
            structure.rowSpacing.meters,
            "test",
            false,
            image,
            sources =
                mapOf(
                    "D2" to
                        SourceReference(
                            "Sekundäranalyse; keine Originalprüfung",
                            "https://example.org/source",
                        )
                ),
            dgEvidence = evidence,
            dgStructure = structure,
        )
    }

    private fun ownMeasurement(): MeasurementSession {
        var own = MeasurementSession()
        listOf(578, 592, 606, 620).forEach { own = own.addMarker(it, image()) }
        return own.updateJustification("Vier benachbarte Maxima im Basisband selbst gewählt.")
            .saveInitialSelection(image())
            .saveComparison(image())
    }

    @Test
    fun measurementEmptyMessageDependsOnOwnWorkRatherThanOptionalComparison() {
        var current = MeasurementSession()
        listOf(578, 592, 606, 620).forEach { current = current.addMarker(it, image()) }
        current = current.updateJustification("Meine vier benachbarten Maxima; keine absolute Skala.")
        val initial = current.saveInitialSelection(image())
        val compared = initial.saveComparison(image())
        val cases = listOf(
            "empty" to MeasurementSession(),
            "current without initial selection" to current,
            "initial selection without comparison" to initial,
            "initial selection with comparison" to compared,
        )
        for ((label, measurement) in cases) {
            val state = SessionSnapshot(measurement = measurement)
            val before = state.json()
            val report = state.personalEvidenceReport(data())
            val main = report.readableText().substringBefore("TECHNISCHER ANHANG")
            val emptyMessage = "Noch keine eigene Bildauswahl oder Begründung eingetragen."
            assertEquals(label == "empty", main.contains(emptyMessage), label)
            assertEquals(measurement.initialSelection != null, main.contains("Gesicherte erste Auswahl"), label)
            assertEquals(measurement.comparison != null, main.contains("Gespeicherter eigener Vergleich"), label)
            if (label != "empty") {
                assertTrue(main.contains("Marken im Originalraster: 578, 592, 606, 620"), label)
                assertTrue(main.contains("Einzelabstände: 14, 14, 14"), label)
                assertTrue(main.contains(current.justification), label)
                assertEquals(measurement, report.measurement, label)
            }
            assertEquals(before, state.json(), label)
        }
    }

    @Test
    fun emptyWorkCannotBecomeCompletedScientificEvidence() {
        val report = SessionSnapshot().personalEvidenceReport()
        assertFalse(report.hasOwnWork)
        assertTrue(report.calculations.isEmpty())
        assertTrue(report.notes.isEmpty())
        assertNull(report.measurement)
        assertTrue(report.claims.all { it == EvidenceRow() })
        assertEquals("", report.conclusion)
        assertFalse(report.readableText().contains("100,137"))
        assertTrue(report.readableText().contains("Persönliches Fazit: noch offen"))
    }

    @Test
    fun requestedCheckIsEvaluatedOnCurrentInputAndDoesNotInventFirstSubmission() {
        val state =
            SessionSnapshot(
                work =
                    mapOf(
                        StationId.S06_CALCULATION to
                            StationWork(numericAnswer = "10 pm", checked = true)
                    )
            )
        val wrong = state.personalEvidenceReport().calculations.single()
        assertEquals(NumericAnswerStatus.INCORRECT, wrong.check!!.status)
        assertNull(wrong.firstSubmission)
        val corrected =
            state
                .copy(
                    work =
                        mapOf(
                            StationId.S06_CALCULATION to
                                StationWork(numericAnswer = "100 pm", checked = true)
                        )
                )
                .personalEvidenceReport()
                .calculations
                .single()
        assertEquals(NumericAnswerStatus.CORRECT, corrected.check!!.status)
        val edited =
            state
                .copy(
                    work =
                        mapOf(
                            StationId.S06_CALCULATION to
                                StationWork(numericAnswer = "100 pm", checked = false)
                        )
                )
                .personalEvidenceReport()
                .calculations
                .single()
        assertNull(edited.check)
        assertEquals("100 pm", edited.answer)
    }

    @Test
    fun openedControlDoesNotFillMissingOwnAnswer() {
        val state =
            SessionSnapshot(
                work = mapOf(StationId.S06_CALCULATION to StationWork(solutionViewed = true))
            )
        val report = state.personalEvidenceReport()
        val entry = report.calculations.single()
        assertEquals("", entry.answer)
        assertNull(entry.check)
        assertTrue(entry.solutionViewed)
        assertFalse(report.readableText().contains("100,137"))
        assertTrue(report.readableText().contains("Eingabe: noch offen"))
    }

    @Test
    fun practicePreservesFirstSubmittedTextAndHelpWithoutAssigningIndependence() {
        val attempt =
            PracticeAttempt().edit("10 pm").submit().revealSolution().edit("79,17 pm").submit()
        val report =
            SessionSnapshot(practice = mapOf(PracticeCaseId.U240 to attempt))
                .personalEvidenceReport()
        val entry = report.calculations.single()
        assertEquals("10 pm", entry.firstSubmission)
        assertEquals("79,17 pm", entry.answer)
        assertTrue(entry.solutionViewed)
        assertEquals(NumericAnswerStatus.CORRECT, entry.check!!.status)
    }

    @Test
    fun laterReferenceContactDoesNotRewriteComparisonOrInitialChoice() {
        val original = ownMeasurement()
        val later =
            original
                .revealReference()
                .recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE)
                .moveMarker(1, 593, image())
        val state = SessionSnapshot(measurement = later)
        val before = state.json()
        val report = state.personalEvidenceReport(data())
        assertEquals(original.initialSelection, report.measurement!!.initialSelection)
        assertEquals(original.comparison, report.measurement.comparison)
        assertFalse(report.measurement.comparison!!.referenceViewedAtCapture)
        assertTrue(report.measurement.referenceViewed)
        assertEquals(
            setOf(ReferenceMaterial.PDF_REFERENCE),
            report.measurement.referenceMaterialsOpened,
        )
        assertEquals(before, state.json())
        assertTrue(report.readableText().contains("Einzelabstände: 15, 13, 14"))
        assertTrue(
            report
                .readableText(includeTechnicalAppendix = true)
                .contains("Laborreferenz bei Speicherung: keine Öffnung protokolliert")
        )
    }

    @Test
    fun reportShowsEndMarkerEffectAndBandMismatchWithoutChangingStoredCase() {
        val original = ownMeasurement()
        val edited = original.moveMarker(3, 621, image()).changeBand(MeasurementBand.UPPER)
        val report = SessionSnapshot(measurement = edited).personalEvidenceReport(data())
        assertFalse(report.measurement!!.justificationMatchesBand)
        assertEquals(14.0, report.measurement.comparison!!.meanPx)
        assertEquals(43.0 / 3, (report.measurement.selection as MeasurementSelection.Ready).meanPx)
        assertTrue(report.readableText().contains("Begründung dem aktuellen Band zugeordnet: nein"))
    }

    @Test
    fun imageAssociationStatesAreDistinctAndNeverSilentlyRebindMarks() {
        val state = SessionSnapshot(measurement = ownMeasurement())
        assertEquals(ReportImageBinding.STORED_ONLY, state.personalEvidenceReport().imageBinding)
        assertEquals(ReportImageBinding.MATCHED, state.personalEvidenceReport(data()).imageBinding)
        val changed = data(image().copy(imageSha256 = "a".repeat(64)))
        val mismatch = state.personalEvidenceReport(changed)
        assertEquals(ReportImageBinding.MISMATCH, mismatch.imageBinding)
        assertEquals(SessionSnapshot.IMAGE_SHA256, mismatch.image!!.sha256)
        assertEquals(
            ReportImageBinding.NO_IMAGE,
            SessionSnapshot(imageIdentity = null).personalEvidenceReport().imageBinding,
        )
    }

    @Test
    fun ownClaimsAndConclusionRemainExactWithoutExpectedAnswers() {
        val claim = EvidenceRow("Meine Behauptung", "Mein begrenzter Beleg", "")
        val state =
            SessionSnapshot(
                inquiry = InquiryState(evidenceRows = listOf(claim, EvidenceRow(), EvidenceRow())),
                work =
                    mapOf(
                        StationId.S16_CONCLUSION to
                            StationWork(note = "Mein eigenes, noch zu prüfendes Fazit.")
                    ),
            )
        val report = state.personalEvidenceReport(data())
        assertEquals(claim, report.claims.first())
        assertEquals("", report.claims.first().limitation)
        assertEquals("Mein eigenes, noch zu prüfendes Fazit.", report.conclusion)
        assertTrue(report.notes.isEmpty())
        assertFalse(report.readableText().contains("Aussage 2:"))
        assertFalse(report.readableText().contains("Aussage 3:"))
        assertTrue(report.readableText().contains("Annahme/Grenze: (nicht ausgefüllt)"))
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("D2 · Sekundäranalyse; keine Originalprüfung"))
    }

    @Test
    fun blankOptionalClaimsDoNotAppearAsMissingWorkOrChangeStoredRows() {
        val rows = listOf(EvidenceRow(), EvidenceRow(" \t", "\n", " "), EvidenceRow())
        val state = SessionSnapshot(inquiry = InquiryState(evidenceRows = rows))
        val before = state.json()
        val report = state.personalEvidenceReport()

        assertEquals(rows, report.claims)
        assertTrue(report.enteredClaims.isEmpty())
        assertFalse(report.hasOwnWork)
        val text = report.readableText()
        assertFalse(text.contains("Freiwillige Aussagen mit Belegen"))
        for (number in 1..3) assertFalse(text.contains("Aussage $number:"))
        assertFalse(text.contains("Annahme/Grenze:"))
        assertTrue(text.contains("Persönliches Fazit: noch offen"))
        assertEquals(before, state.json())
    }

    @Test
    fun allVisitorResultsRemainPresentWithoutFillingOptionalClaims() {
        val calculation = StationWork(numericAnswer = "100 pm", note = "Mein Rechenweg für 150 V.")
        val dg = StationWork(
            prediction = "167 pm",
            numericAnswer = "165 pm",
            note = "Mein Vergleich mit unabhängig gewähltem Strukturabstand.",
        )
        val measurement = ownMeasurement()
        val conclusion = "Mein Schluss aus Rechnung, Kristallvergleich und eigener Bildmessung."
        val state = SessionSnapshot(
            work = mapOf(
                StationId.S06_CALCULATION to calculation,
                StationId.S11_DG_EVALUATION to dg,
                StationId.S16_CONCLUSION to StationWork(note = conclusion),
            ),
            measurement = measurement,
        )
        val before = state.json()
        val report = state.personalEvidenceReport(data())

        assertEquals(OwnWorkSection.entries.toSet(), report.ownWorkPresence().map { it.section }.toSet())
        assertTrue(report.ownWorkPresence().all { it.presence == OwnWorkPresence.PRESENT })
        assertTrue(report.enteredClaims.isEmpty())
        assertEquals(List(3) { EvidenceRow() }, report.claims)
        val text = report.readableText()
        assertFalse(text.contains("Freiwillige Aussagen mit Belegen"))
        for (number in 1..3) assertFalse(text.contains("Aussage $number:"))
        for (value in listOf(calculation.numericAnswer, calculation.note, dg.prediction,
            dg.numericAnswer, dg.note, measurement.justification, conclusion)) {
            assertTrue(text.contains(value), value)
        }
        assertTrue(text.contains("Marken im Originalraster: 578, 592, 606, 620"))
        assertEquals(before, state.json())
    }

    @Test
    fun partialOptionalClaimsKeepTheirOriginalNumberAndExactContent() {
        val cases = listOf(
            EvidenceRow(claim = "  Meine noch ungeprüfte Aussage.\nZweiter Gedanke.  "),
            EvidenceRow(evidence = "  Meine Messung:\nΔx = 14 px  "),
            EvidenceRow(limitation = "  Ohne unabhängigen Maßstab\nbleibt die Länge offen.  "),
        )
        for (row in cases) {
            val rows = listOf(EvidenceRow(), row, EvidenceRow())
            val state = SessionSnapshot(inquiry = InquiryState(evidenceRows = rows))
            val before = state.json()
            val report = state.personalEvidenceReport()

            assertTrue(report.hasOwnWork)
            assertEquals(rows, report.claims)
            assertEquals(listOf(IndexedValue(1, row)), report.enteredClaims)
            val text = report.readableText()
            assertTrue(text.contains("Freiwillige Aussagen mit Belegen"))
            assertFalse(text.contains("Aussage 1:"))
            assertTrue(text.contains("Aussage 2:"))
            assertFalse(text.contains("Aussage 3:"))
            for ((label, value) in listOf(
                "Aussage 2" to row.claim,
                "Beleg" to row.evidence,
                "Annahme/Grenze" to row.limitation,
            )) {
                if (value.isNotBlank()) {
                    assertTrue(text.contains("$label: $value\n"))
                    assertEquals(1, text.windowed(value.length).count { it == value })
                } else {
                    assertTrue(text.contains("$label: (nicht ausgefüllt)"))
                    assertFalse(text.contains("$label: noch offen"))
                }
            }
            assertEquals(before, state.json())
        }
    }

    @Test
    fun hintOnlyAndControlOnlyRemainContactWithoutInventedOwnWork() {
        val hints =
            SessionSnapshot(work = mapOf(StationId.S06_CALCULATION to StationWork(hintsOpened = 2)))
                .personalEvidenceReport()
        assertFalse(hints.hasOwnWork)
        assertEquals(2, hints.helpContacts.single().displayedHintCount)
        assertTrue(hints.readableText(includeTechnicalAppendix = true).contains("Hinweiszähler 2 (begrenzt auf 10)"))
        assertTrue(hints.readableText(includeTechnicalAppendix = true).contains("frühere Syntaxhinweise"))
        val control =
            SessionSnapshot(
                    work = mapOf(StationId.S06_CALCULATION to StationWork(solutionViewed = true))
                )
                .personalEvidenceReport()
        assertFalse(control.hasOwnWork)
        assertTrue(control.helpContacts.single().solutionViewed)
    }

    @Test
    fun materialContactBeforeAnyMeasurementRemainsVisibleAndDoesNotInventMeasurement() {
        val contact =
            MeasurementSession()
                .recordReferenceMaterial(ReferenceMaterial.PDF_REFERENCE)
                .recordReferenceMaterial(ReferenceMaterial.UNKNOWN_LEGACY)
        val report = SessionSnapshot(measurement = contact).personalEvidenceReport()
        assertNull(report.measurement)
        assertFalse(report.hasOwnWork)
        assertEquals(contact.referenceMaterialsOpened, report.currentReferenceMaterials)
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("Fachfassung mit Referenzangaben"))
        assertTrue(report.readableText(includeTechnicalAppendix = true).contains("Referenzkontakt aus älterem Lernstand"))
    }
}
