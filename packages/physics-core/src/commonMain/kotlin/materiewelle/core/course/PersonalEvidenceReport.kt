package materiewelle.core.course

import materiewelle.core.*

enum class CalculationRole(val label: String) {
    REQUIRED("Pflichtrechnung"),
    GUIDED("Geführter Vergleich"),
    PRACTICE("Zusätzlicher Rechenfall"),
}

/** An evaluation belongs to the current submitted text; it is never a competence assessment. */
data class OwnCalculationEvidence(
    val voltage: Voltage,
    val role: CalculationRole,
    val answer: String,
    val check: NumericAnswerEvaluation?,
    val solutionViewed: Boolean,
    val firstSubmission: String? = null,
)

data class OwnStationEvidence(
    val station: StationId,
    val prediction: String,
    val explanation: String,
)

/** The two DG routes must travel together; neither text is an automatic numeric grade. */
data class OwnDgComparisonEvidence(
    val prediction: String,
    val geometricAnswer: String,
    val interpretation: String,
    val controlViewed: Boolean,
) {
    val hasOwnInputs: Boolean
        get() = prediction.isNotBlank() || geometricAnswer.isNotBlank() || interpretation.isNotBlank()
}

data class StationHelpContact(
    val station: StationId,
    val displayedHintCount: Int,
    val solutionViewed: Boolean,
    val transferSolutionViewed: Boolean,
)

enum class ReportImageBinding(val label: String) {
    MATCHED("Die Bildzuordnung im Arbeitsstand stimmt mit dem geladenen Original überein."),
    STORED_ONLY(
        "Die Bilddatei ist im Arbeitsstand vermerkt. Für diesen Bericht liegt kein Abgleich mit einer aktuell geladenen Datei vor."
    ),
    MISMATCH(
        "Geladenes Bild und Bildzuordnung im Arbeitsstand unterscheiden sich; die Marken werden nicht auf das neue Bild übertragen."
    ),
    NO_IMAGE("Keine zugeordnete Bildidentität vorhanden."),
}

/**
 * A read-only view of existing work. This format is not another journal or a saved session schema.
 */
data class PersonalEvidenceReport(
    val courseRevision: String,
    val dataRevision: String,
    val calculations: List<OwnCalculationEvidence>,
    val notes: List<OwnStationEvidence>,
    val helpContacts: List<StationHelpContact>,
    val measurement: MeasurementSession?,
    val currentReferenceMaterials: Set<ReferenceMaterial>,
    val laboratoryReferenceViewed: Boolean,
    val image: MeasurementImageIdentity?,
    val imageBinding: ReportImageBinding,
    val claims: List<EvidenceRow>,
    val conclusion: String,
    val sources: Map<String, SourceReference>,
    val dgComparison: OwnDgComparisonEvidence? = null,
    /** Reader-facing citations; independent of imported technical data provenance in sources. */
    val bibliography: Map<String, SourceReference> = emptyMap(),
) {
    /** Optional rows with actual content, retaining their editor indices and original text. */
    val enteredClaims: List<IndexedValue<EvidenceRow>>
        get() = claims.withIndex().filter { (_, row) ->
            row.claim.isNotBlank() || row.evidence.isNotBlank() || row.limitation.isNotBlank()
        }

    val hasOwnWork: Boolean
        get() =
            calculations.any { it.answer.isNotBlank() } ||
                dgComparison?.hasOwnInputs == true ||
                notes.isNotEmpty() ||
                measurement != null ||
                enteredClaims.isNotEmpty() ||
                conclusion.isNotBlank()

    val emptyNotesMessage: String
        get() = "Noch keine zusätzlichen Vorhersagen oder Erklärungen eingetragen. Dein Fazit steht im eigenen Abschnitt."

    fun readableText(includeTechnicalAppendix: Boolean = false): String = buildString {
        appendLine("MATERIEWELLE · DEIN ARBEITSBERICHT")
        appendLine("Deine Rechnungen, Messentscheidungen und Schlussfolgerungen in vier Kapiteln, ergänzt um Quellen." +
            if (includeTechnicalAppendix) " Der technische Anhang ist beigefügt." else "")
        appendLine("Deine Eingaben aus diesem Browser. Freie Erklärungen werden nicht automatisch bewertet.")
        appendLine()
        appendLine("1 · DE BROGLIE — VORHERSAGE UND EIGENE RECHNUNG")
        appendLine("Aufgabe: Aus der Beschleunigungsspannung die Wellenlänge vorhersagen, für 150 V berechnen und die Spannungsabhängigkeit erklären.")
        appendOwnNotes(notes.filter { AppSection.forStation(it.station) == AppSection.DE_BROGLIE })
        if (calculations.isEmpty())
            appendLine("Noch keine eigene Zahlenantwort oder angeforderte Kontrolle vorhanden.")
        calculations.forEach { entry ->
            appendLine("${entry.role.label} · ${Physics.decimal(entry.voltage.volts, 0)} V")
            appendLine("Eingabe: ${entry.answer.ifBlank { "noch offen" }}")
            appendLine("Aktuelle Zahlenprüfung: ${entry.check?.feedback ?: "nicht angefordert"}")
            appendLine()
        }
        appendLine("Freie Rechenwege werden nicht automatisch bewertet. Die Spannungsformel setzt Beschleunigung aus Ruhe und nichtrelativistische Mechanik voraus.")
        appendLine("Quellen: M (Herleitung unter den genannten Annahmen), H1/H3 (historische Einordnung). Einsteins theoretische Zustimmung ist keine experimentelle Bestätigung.")
        appendLine()
        appendLine("2 · DAVISSON–GERMER — ZWEI WEGE VERGLEICHEN")
        appendLine("Aufgabe: Die Vorhersage aus der Spannung mit dem geometrisch erschlossenen Wert des dokumentierten Befunds vergleichen und den Winkelbezug erklären.")
        appendOwnNotes(notes.filter { AppSection.forStation(it.station) == AppSection.DAVISSON_GERMER })
        val dg = dgComparison
        appendLine("Vorhersage aus der Spannung: ${dg?.prediction?.ifBlank { "noch offen" } ?: "noch offen"}")
        appendLine("Ergebnis aus der Beugungsgeometrie: ${dg?.geometricAnswer?.ifBlank { "noch offen" } ?: "noch offen"}")
        appendLine("Eigene Einordnung: ${dg?.interpretation?.ifBlank { "noch offen" } ?: "noch offen"}")
        appendLine("Dein Wortlaut bleibt erhalten; freie Rechenwege werden nicht automatisch bewertet.")
        appendLine("Quellen: D2 berichtet den historischen Zahlenfall sekundär. Das Original D1 wurde nicht eingesehen. Eigene Modellvariationen sind keine historischen Messdaten; die verwendete Strukturgröße muss unabhängig bekannt sein.")
        appendLine()
        appendLine("3 · JÖNSSON — EIGENE RELATIVE BILDAUSWERTUNG")
        appendLine("Aufgabe: Benachbarte helle Maxima auswählen, Intervalle und Mittelwert bestimmen, die Auswahl begründen und die Aussagegrenze nennen.")
        appendOwnNotes(notes.filter { AppSection.forStation(it.station) == AppSection.JONSSON })
        appendLine(imageBinding.label)
        val m = measurement
        if (m == null) {
            appendLine("Noch keine eigene Bildauswahl oder Begründung eingetragen.")
        } else {
            appendLine("Aktueller Arbeitsstand · ${m.band.label}")
            image?.let { id ->
                val band = m.nativeBand(id)
                appendLine("Band: ${band.topInclusive} ≤ y < ${band.bottomExclusive}")
            }
            appendMarks(m.markers)
            appendLine("Begründung: ${m.justification.ifBlank { "noch offen" }}")
            appendLine("Begründung dem aktuellen Band zugeordnet: ${if (m.justificationMatchesBand) "ja" else "nein; erneut prüfen"}")
            m.initialSelection?.let { appendSelection("Gesicherte erste Auswahl", it) }
            m.comparison?.let { appendSelection("Gespeicherter eigener Vergleich", it) }
        }
        appendLine("Quelle: J1, digitalisierte Reproduktion aus der gekürzten englischen Übersetzung (1974) des Versuchsberichts von 1961.")
        appendLine("Marken und Grauwerte gehören zur digitalisierten Reproduktion. Keine kalibrierte Elektronenzählrate, keine absolute Wellenlängenmessung.")
        appendLine("Ein gleiches Mittel beweist noch keine passende Maximalwahl. Veränderte Bänder oder Marken prüfen deine Auswertungsmethode, nicht die historische Messunsicherheit.")
        appendLine()
        appendLine("4 · DEIN FAZIT")
        appendLine("Aufgabe: Eine eigene Schlussfolgerung mit Belegen aus den Rechnungen und Versuchen begründen und ihre Grenze benennen.")
        appendLine("Persönliches Fazit: ${conclusion.ifBlank { "noch offen" }}")
        val optionalClaims = enteredClaims
        if (optionalClaims.isNotEmpty()) {
            appendLine()
            appendLine("Freiwillige Aussagen mit Belegen")
            optionalClaims.forEach { (index, row) ->
                appendLine("Aussage ${index + 1}: ${row.claim.ifBlank { "(nicht ausgefüllt)" }}")
                appendLine("Beleg: ${row.evidence.ifBlank { "(nicht ausgefüllt)" }}")
                appendLine("Annahme/Grenze: ${row.limitation.ifBlank { "(nicht ausgefüllt)" }}")
            }
        }
        if (notes.isEmpty()) appendLine(emptyNotesMessage)
        appendLine("Halte fest, wofür du KI genutzt und was du selbst nachgerechnet, an Quellen geprüft und erklärt hast. Der Bericht allein belegt diese Arbeit nicht.")
        appendLine()
        appendLine("QUELLEN ZUM NACHLESEN")
        if (bibliography.isEmpty())
            appendLine("Literaturangaben sind in diesem Bericht nicht mitgegeben. Du findest sie in den Quellenbereichen der Website.")
        bibliography.entries.sortedBy { it.key }.forEach { (id, source) ->
            appendLine("$id · ${source.status}")
            source.url?.let { appendLine(it) }
            appendLine()
        }
        if (!includeTechnicalAppendix) return@buildString
        appendLine("TECHNISCHER ANHANG · ARBEITSSTAND UND HERKUNFT")
        appendLine("Fachstand: $courseRevision · Datenstand: $dataRevision")
        appendLine("Protokollierte Hilfen auf Stationsebene")
        if (helpContacts.isEmpty())
            appendLine("Keine stationären Hilfestufen oder Kontrolllösungen protokolliert; keine vollständige Hilfegeschichte.")
        helpContacts.forEach { help ->
            appendLine("Station ${help.station.number}: Hinweiszähler ${help.displayedHintCount} (begrenzt auf 10); Hauptkontrolle als geöffnet protokolliert: ${help.solutionViewed}; Vergleichskontrolle als geöffnet protokolliert: ${help.transferSolutionViewed}.")
        }
        appendLine("Hilfenutzung ist erlaubt. Diese Einträge zeigen weder den Zeitpunkt noch die vollständige Reihenfolge der Arbeit. Ohne vermerkte Öffnung können Hilfen trotzdem schon bekannt gewesen sein; frühere Syntaxhinweise konnten Kontrollwerte ohne Eintrag zeigen.")
        calculations.forEach { entry ->
            appendLine("${entry.role.label} · ${Physics.decimal(entry.voltage.volts, 0)} V: Kontrolllösung ${if (entry.solutionViewed) "Öffnung protokolliert" else "keine Öffnung protokolliert"}.")
            entry.firstSubmission?.let { appendLine("Erste protokollierte Abgabe dieses Zusatzfalls: $it") }
            if (entry.role != CalculationRole.PRACTICE)
                appendLine("Keine getrennte Erstabgabe oder vollständige Antwortfolge für diesen Fall vermerkt.")
        }
        dg?.let { appendLine("DG: Mindestens eine Kontrolle geöffnet: ${it.controlViewed}; keine vollständige Hilfereihenfolge.") }
        image?.let { appendLine("Bild SHA-256: ${it.sha256}; ${it.nativeWidthPx} × ${it.nativeHeightPx} Originalpixel") }
        appendLine("Aktueller Referenzkontakt: ${if (laboratoryReferenceViewed) "Öffnung der Laborreferenz protokolliert" else "keine Öffnung der Laborreferenz protokolliert"}")
        appendLine("Weitere vermerkte Hilfen und Dateianfragen: ${currentReferenceMaterials.contactLabels()}")
        appendLine("Ein angeforderter Handout-Zugriff belegt weder einen erfolgreichen Download noch Lesen oder Verständnis. Direkte PDF-Aufrufe, außerhalb der App gelesene Materialien und Zugriffe vor Einführung dieses Protokolls bleiben unbeobachtbar.")
        measurement?.initialSelection?.let { appendSnapshotProvenance("Gesicherte erste Auswahl", it) }
        measurement?.comparison?.let { appendSnapshotProvenance("Gespeicherter eigener Vergleich", it) }
        appendLine()
        appendLine("Technische Datenprovenienz · unveränderte Prüfvermerke")
        if (sources.isEmpty()) appendLine("Keine technischen Quellenmetadaten geladen.")
        sources.entries.sortedBy { it.key }.forEach { (id, source) ->
            appendLine("$id · ${source.status}")
            source.url?.let { appendLine(it) }
            source.path?.let { appendLine("Fundstelle im Projekt: $it") }
        }
    }

}

/**
 * Derive only entered work and existing provenance. No expected answer is copied into a blank
 * field.
 */
fun SessionSnapshot.personalEvidenceReport(
    data: ScientificData? = null,
    courseSources: Map<String, SourceReference> = emptyMap(),
    readerBibliography: Map<String, SourceReference> = courseSources,
): PersonalEvidenceReport {
    validate()
    val ownCalculations = buildList {
        fun addWork(
            station: StationWork,
            voltage: Double,
            role: CalculationRole,
            transfer: Boolean = false,
        ) {
            val answer = if (transfer) station.transferAnswer else station.numericAnswer
            val checked = if (transfer) station.transferChecked else station.checked
            val solution = if (transfer) station.transferSolutionViewed else station.solutionViewed
            if (answer.isNotBlank() || checked || solution)
                add(
                    OwnCalculationEvidence(
                        Voltage(voltage),
                        role,
                        answer,
                        if (checked) NumericWavelengthQuestion(Voltage(voltage)).evaluate(answer)
                        else null,
                        solution,
                    )
                )
        }
        val required = stationWork(StationId.S06_CALCULATION)
        addWork(required, 150.0, CalculationRole.REQUIRED)
        addWork(required, 600.0, CalculationRole.GUIDED, transfer = true)
        PracticeCaseId.entries.forEach { case ->
            val attempt = practice[case] ?: return@forEach
            if (
                attempt.draft.isNotBlank() ||
                    attempt.checked ||
                    attempt.solutionViewed ||
                    attempt.firstSubmission != null
            )
                add(
                    OwnCalculationEvidence(
                        case.voltage,
                        CalculationRole.PRACTICE,
                        attempt.draft,
                        if (attempt.checked) case.question.evaluate(attempt.draft) else null,
                        attempt.solutionViewed,
                        attempt.firstSubmission,
                    )
                )
        }
    }
    val ownNotes =
        StationId.entries
            .filter { it != StationId.S16_CONCLUSION && it != StationId.S11_DG_EVALUATION }
            .mapNotNull { station ->
                val work = stationWork(station)
                if (work.prediction.isBlank() && work.note.isBlank()) null
                else OwnStationEvidence(station, work.prediction, work.note)
            }
    val helpContacts =
        StationId.entries.mapNotNull { station ->
            val work = stationWork(station)
            if (work.hintsOpened == 0 && !work.solutionViewed && !work.transferSolutionViewed) null
            else
                StationHelpContact(
                    station,
                    work.hintsOpened,
                    work.solutionViewed,
                    work.transferSolutionViewed,
                )
        }
    val loaded = data?.jonsson?.let(MeasurementImageIdentity::from)
    val binding =
        when {
            imageIdentity == null -> ReportImageBinding.NO_IMAGE
            loaded == null -> ReportImageBinding.STORED_ONLY
            loaded == imageIdentity -> ReportImageBinding.MATCHED
            else -> ReportImageBinding.MISMATCH
        }
    val ownMeasurement =
        measurement.takeIf {
            it.markers.isNotEmpty() ||
                it.justification.isNotBlank() ||
                it.initialSelection != null ||
                it.comparison != null
        }
    return PersonalEvidenceReport(
        courseRevision,
        dataRevision,
        ownCalculations,
        ownNotes,
        helpContacts,
        ownMeasurement,
        measurement.referenceMaterialsOpened.toSet(),
        measurement.referenceViewed,
        imageIdentity,
        binding,
        inquiry.evidenceRows.toList(),
        stationWork(StationId.S16_CONCLUSION).note,
        // Course-only history/model sources supplement, never overwrite, loaded data provenance.
        courseSources + data?.sources.orEmpty(),
        stationWork(StationId.S11_DG_EVALUATION).let { work ->
            OwnDgComparisonEvidence(work.prediction, work.numericAnswer, work.note, work.solutionViewed)
                .takeIf { it.hasOwnInputs || it.controlViewed }
        },
        bibliography = readerBibliography.toMap(),
    )
}

private fun Set<ReferenceMaterial>.contactLabels(): String =
    sortedBy { it.ordinal }
        .joinToString { it.label }
        .ifEmpty { "keine Zugriffe vermerkt; zuvor gelesene Materialien sind dadurch nicht ausgeschlossen" }

private fun StringBuilder.appendMarks(markers: List<NativeImageMark>) {
    appendLine("Marken im Originalraster: ${markers.joinToString { it.xPx.toString() }.ifEmpty { "keine" }}")
    val intervals = markers.zipWithNext { a, b -> b.xPx - a.xPx }
    appendLine("Einzelabstände: ${intervals.joinToString().ifEmpty { "noch nicht bestimmbar" }}")
    if (intervals.isNotEmpty())
        appendLine("Mittlerer Abstand im Originalraster: ${Physics.decimal(intervals.average(), 3)} px (gerundeter Rechenwert)")
}

private fun StringBuilder.appendSelection(title: String, snapshot: MeasurementSnapshot) {
    appendLine(title)
    appendLine(
        "${snapshot.band.label}; ${snapshot.bandTopInclusivePx} ≤ y < ${snapshot.bandBottomExclusivePx}"
    )
    appendMarks(snapshot.markers)
    appendLine("Begründung bei Speicherung: ${snapshot.justification}")
}

private fun StringBuilder.appendSnapshotProvenance(title: String, snapshot: MeasurementSnapshot) {
    appendLine(title)
    appendLine("Bild SHA-256: ${snapshot.imageSha256}")
    appendLine(
        "Laborreferenz bei Speicherung: ${if (snapshot.referenceViewedAtCapture) "Öffnung protokolliert" else "keine Öffnung protokolliert"}"
    )
    appendLine(
        "Vor Speicherung vermerkte Hilfen und Dateianfragen: ${snapshot.priorReferenceMaterials.contactLabels()}"
    )
}

private fun StringBuilder.appendOwnNotes(notes: List<OwnStationEvidence>) {
    notes.forEach { note ->
        appendLine(note.station.reportTopic)
        if (note.prediction.isNotBlank()) appendLine("Vorhersage, dein Wortlaut: ${note.prediction}")
        if (note.explanation.isNotBlank()) appendLine("Erklärung, dein Wortlaut: ${note.explanation}")
    }
}

private val StationId.reportTopic: String
    get() = when (this) {
        StationId.S01_OBSERVATION -> "Beobachtung und Ausgangsfrage"
        StationId.S02_CLASSICAL -> "Klassische Bilder"
        StationId.S03_LIGHT -> "Lichtquanten als Ausgangspunkt"
        StationId.S04_HYPOTHESIS -> "De Broglies Hypothese"
        StationId.S05_DERIVATION -> "Eigene Herleitung"
        StationId.S06_CALCULATION -> "Eigener Rechenweg und Einordnung"
        StationId.S07_RECEPTION -> "Einsteins Rolle"
        StationId.S08_DG_SETUP -> "Aufbau von Davisson–Germer"
        StationId.S09_CRYSTAL -> "Nickelkristall und Struktur"
        StationId.S10_GEOMETRY -> "Beugungsgeometrie und Winkelbezug"
        StationId.S11_DG_EVALUATION -> "Vergleich der beiden Rechenwege"
        StationId.S12_JONSSON_SETUP -> "Jönssons Aufbau"
        StationId.S13_INTERFERENCE -> "Spaltvergleich und Interferenz"
        StationId.S14_MEASUREMENT -> "Eigene Bildmessung"
        StationId.S15_LIMITS -> "Grenzen des Modells und der Auswertung"
        StationId.S16_CONCLUSION -> "Eigener Schluss"
    }
