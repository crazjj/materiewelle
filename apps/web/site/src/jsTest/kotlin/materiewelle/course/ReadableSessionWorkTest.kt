package materiewelle.course

import kotlin.test.*
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.course.content.CourseContent
import materiewelle.state.*

class ReadableSessionWorkTest {
    private class UnavailableStorage : SessionStorageBackend {
        override fun read(key: String): String? = null
        override fun write(key: String, value: String) { error("storage unavailable") }
        override fun remove(key: String) = Unit
    }

    @Test fun rescueContainsLiveUnsavedWorkAndQuestionsWithoutNeedingScientificAssets() {
        val session = LabSession(SessionStorage(UnavailableStorage()))
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Mein noch nicht gespeicherter Rechenweg") }
        session.editWork(StationId.S16_CONCLUSION) { it.copy(note = "Mein eigenes Fazit") }
        val before = session.snapshot
        val rescue = readableSessionWork(session, mapOf(AppSection.JONSSON to "Meine Frage zum Bild"),
            "Eigene Spaltuntersuchung\nMeine Vorhersage und Begründung")
        assertContains(rescue, "Mein noch nicht gespeicherter Rechenweg")
        assertContains(rescue, "Mein eigenes Fazit")
        assertContains(rescue, "Meine Frage zum Bild")
        assertContains(rescue, "Meine Vorhersage und Begründung")
        assertContains(rescue, "wird nicht wieder in die Website eingelesen")
        assertFalse(rescue.trimStart().startsWith("{"))
        assertEquals(before, session.snapshot)
        assertEquals(StorageHealth.MEMORY_ONLY, session.health)
    }

    @Test fun successfulDeletionIsQuietButNewUnpersistedWorkHasRescue() {
        val backend = object : SessionStorageBackend {
            val values = mutableMapOf<String, String>()
            override fun read(key: String) = values[key]
            override fun write(key: String, value: String) { values[key] = value }
            override fun remove(key: String) { values.remove(key) }
        }
        val session = LabSession(SessionStorage(backend, ExclusiveTestWriter))
        val drafts = UiDraftStorage(backend, ExclusiveTestWriter)
        val initial = drafts.restore()
        assertFalse(sessionRescueNeeded(session, drafts, initial))
        assertTrue(session.deleteAllLocalData())
        assertFalse(sessionRescueNeeded(session, drafts, initial))
        session.visit(StationId.S06_CALCULATION)
        assertFalse(sessionRescueNeeded(session, drafts, initial))
        val question = initial.copy(questions = mapOf(AppSection.JONSSON to "Neue Frage nach dem Löschen"))
        assertTrue(sessionRescueNeeded(session, drafts, question))
        session.editWork(StationId.S06_CALCULATION) { it.copy(note = "Neue Rechnung nach dem Löschen") }
        assertTrue(sessionRescueNeeded(session, drafts, initial))
        assertNull(backend.read(SessionStorage.KEY))
    }

    @Test fun loadedDataGivesDeviceCopyTheSameImageCheckAndBibliographyAsTheReport() {
        val data = scientificData()
        val session = sessionWithMeasurement(requireNotNull(data.jonsson))
        val before = session.snapshot.json()
        val report = session.snapshot.reportForReading(data)
        val copy = readableSessionWork(session, emptyMap(), data = data)

        assertEquals(ReportImageBinding.MATCHED, report.imageBinding)
        assertContains(copy, ReportImageBinding.MATCHED.label)
        assertFalse(copy.contains(ReportImageBinding.STORED_ONLY.label))
        assertContains(copy, report.readableText())
        assertEquals(CourseContent.reportBibliography.map { it.id }.toSet(), report.bibliography.keys)
        assertTrue(report.bibliography.isNotEmpty())
        report.bibliography.values.forEach { source ->
            assertContains(copy, source.status)
            source.url?.let { assertContains(copy, it) }
        }
        assertFalse(copy.contains("TECHNISCHER ANHANG"))
        assertFalse(copy.contains(SessionSnapshot.IMAGE_SHA256))
        assertFalse(copy.contains(data.sources.getValue("D2").status))

        val withAppendix = readableSessionWork(session, emptyMap(), data = data, includeTechnicalAppendix = true)
        assertContains(withAppendix, report.readableText(includeTechnicalAppendix = true))
        assertContains(withAppendix, "TECHNISCHER ANHANG")
        assertContains(withAppendix, SessionSnapshot.IMAGE_SHA256)
        assertContains(withAppendix, data.sources.getValue("D2").status)
        assertEquals(before, session.snapshot.json())
        assertEquals(StorageHealth.MEMORY_ONLY, session.health)
    }

    @Test fun unavailableDataKeepsBibliographyButDoesNotClaimAnImageVerification() {
        val data = scientificData()
        val session = sessionWithMeasurement(requireNotNull(data.jonsson))
        val before = session.snapshot.json()
        val report = session.snapshot.reportForReading()
        val questions = mapOf(AppSection.JONSSON to "Welche Bildzuordnung konnte geprüft werden?")
        val copy = readableSessionWork(session, questions)

        assertEquals(ReportImageBinding.STORED_ONLY, report.imageBinding)
        assertContains(copy, ReportImageBinding.STORED_ONLY.label)
        assertFalse(copy.contains(ReportImageBinding.MATCHED.label))
        assertContains(copy, report.readableText())
        assertEquals(session.snapshot.reportForReading(data).bibliography, report.bibliography)
        assertTrue(report.bibliography.isNotEmpty())
        report.bibliography.values.forEach { source ->
            assertContains(copy, source.status)
            source.url?.let { assertContains(copy, it) }
        }
        assertContains(copy, "Meine benachbarten hellen Maxima; keine absolute Skala.")
        assertContains(copy, questions.getValue(AppSection.JONSSON))
        assertFalse(copy.contains("TECHNISCHER ANHANG"))
        assertFalse(copy.contains(SessionSnapshot.IMAGE_SHA256))

        val withAppendix = readableSessionWork(session, questions, includeTechnicalAppendix = true)
        assertContains(withAppendix, report.readableText(includeTechnicalAppendix = true))
        assertContains(withAppendix, SessionSnapshot.IMAGE_SHA256)
        assertContains(withAppendix, ReportImageBinding.STORED_ONLY.label)
        assertFalse(withAppendix.contains(ReportImageBinding.MATCHED.label))
        assertEquals(before, session.snapshot.json())
    }

    private fun sessionWithMeasurement(image: JonssonData): LabSession {
        val session = LabSession(SessionStorage(UnavailableStorage(), ExclusiveTestWriter))
        session.updateMeasurement { current ->
            current.addMarker(578, image).addMarker(592, image).addMarker(606, image)
                .updateJustification("Meine benachbarten hellen Maxima; keine absolute Skala.")
                .saveInitialSelection(image)
        }
        return session
    }

    private fun scientificData(): ScientificData {
        val image = JonssonData(
            "raster.png", 703, 242, listOf(551.0, 564.0), 160.0,
            50_000.0, 2e-6, 0.35, "Keine Kalibrierung", 490.0, 100.0, 213.0, 120.0,
            SessionSnapshot.IMAGE_SHA256,
        )
        val evidence = ScientificEvidence(
            MaterialRole.SECONDARY_REPORT, EvidenceReview.CHECKED_IN_SCOPE,
            "D2", "synthetic test", "test only",
        )
        val structure = Nickel111Structure(
            Length(.352e-9), evidence.copy(role = MaterialRole.INDEPENDENT_STRUCTURE_ASSUMPTION),
        )
        return ScientificData(
            emptyList(), structure.rowSpacing.meters, "test", false, image,
            sources = mapOf("D2" to SourceReference("Synthetischer technischer Prüfvermerk; keine Originalprüfung")),
            dgEvidence = evidence, dgStructure = structure,
        )
    }
}
