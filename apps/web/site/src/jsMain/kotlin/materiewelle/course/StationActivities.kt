package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.data.LabAsset
import materiewelle.pages.number
import materiewelle.state.LabSession
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private const val EVIDENCE_TEXT_LIMIT = 1000

/**
 * A source photograph, a purpose-built schema and a computed teaching model are different media.
 */
@Composable
fun PhotoMediaComparison() {
    val teachingModel =
        FiniteSlitModel(
            Voltage(150.0),
            Length(10.0),
            Length(2e-6),
            Length(.5e-6),
            WavelengthApproximation.NONRELATIVISTIC,
        )
    Section(
        attrs = {
            elementId(StationActivityId.MEDIA)
            stationActivityStyles(StationActivityClass.ROOT)
        }
    ) {
        H3 { Text("Foto · Schema · berechnete Kurve") }
        P {
            Text(
                "Vergleiche Herkunft und Zweck. Eine ähnliche Streifenform macht die drei Darstellungen nicht zu derselben Art von Beleg."
            )
        }
        Details(attrs = { styles(LabClass.NOTES) }) {
            Summary { Text("Foto, Schema und Modellkurve genauer vergleichen") }
            Div(attrs = { stationActivityStyles(StationActivityClass.MEDIA_GRID) }) {
                Section(attrs = { stationActivityStyles(StationActivityClass.MEDIA_CARD) }) {
                    P(attrs = { stationActivityStyles(StationActivityClass.KIND) }) {
                        Text("01 · FOTO / QUELLENREPRODUKTION")
                    }
                    Div(attrs = { stationActivityStyles(StationActivityClass.MEDIA_FRAME) }) {
                        Img(
                            src = LabAsset.ORIGINAL_IMAGE.path,
                            alt =
                                "Unveränderte digitale Quellenreproduktion des Jönsson-Doppelspaltbildes, 703 mal 242 native Pixel; kein Modellbild.",
                            attrs = {
                                elementId(StationActivityId.MEDIA_PHOTO)
                                stationActivityStyles(StationActivityClass.PHOTO)
                            },
                        )
                    }
                    H4 { Text("Was wurde aufgenommen?") }
                    P {
                        Text(
                            "Ein räumlicher Bildbefund aus Jönssons Arbeit. Die Rasterdatei wird hier verkleinert angezeigt; ihre Helligkeitswerte sind keine kalibrierten Elektronenzählraten."
                        )
                    }
                    P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
                        Text("J1 · Quellenbild, keine zeitliche Bahnaufnahme.")
                    }
                }
                Section(attrs = { stationActivityStyles(StationActivityClass.MEDIA_CARD) }) {
                    P(attrs = { stationActivityStyles(StationActivityClass.KIND) }) {
                        Text("02 · SCHEMA / FUNKTIONSHILFE")
                    }
                    Div(attrs = { stationActivityStyles(StationActivityClass.MEDIA_FRAME) }) {
                        schematicOpenings()
                    }
                    H4 { Text("Wie hängen Bauteile zusammen?") }
                    P {
                        Text(
                            "Das Schema wählt Quelle, zwei Öffnungen und Beobachtung aus. Es erklärt eine Funktion und lässt Einzelheiten absichtlich weg."
                        )
                    }
                    P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
                        Text(
                            "Eigene Skizze · nicht maßstäblich und keine vollständige Jönsson-Apparatur."
                        )
                    }
                }
                Section(attrs = { stationActivityStyles(StationActivityClass.MEDIA_CARD) }) {
                    P(attrs = { stationActivityStyles(StationActivityClass.KIND) }) {
                        Text("03 · KURVE / MODELLVORHERSAGE")
                    }
                    Div(
                        attrs = {
                            elementId(StationActivityId.MEDIA_MODEL)
                            stationActivityStyles(StationActivityClass.MEDIA_FRAME)
                        }
                    ) {
                        compactTeachingCurve(teachingModel)
                    }
                    H4 { Text("Was folgt aus den Annahmen?") }
                    P {
                        Text(
                            "Die Kurve wird aus dem endlichen Doppelspaltmodell berechnet: 150 V, L = 10 m, g = 2 µm, b = 0,5 µm; nichtrelativistischer Fernfeld-Lehrfall."
                        )
                    }
                    P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
                        Text(
                            "Synthetisch · relative Modellintensität, kein historischer Intensitätsscan und kein Fit des Fotos."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun compactTeachingCurve(model: FiniteSlitModel) {
    val extentM = .0015
    val path =
        (0..180).joinToString(" ") { sample ->
            val positionM = -extentM + 2 * extentM * sample / 180.0
            val x = 39 + 304 * sample / 180.0
            val y = 132 - 102 * model.relativeIntensity(positionM)
            "${if (sample == 0) "M" else "L"}$x $y"
        }
    ActivitySvg(
        StationActivityId.MEDIA_MODEL_PLOT,
        360.0,
        166.0,
        "Tatsächlich berechnete relative Doppelspalt-Modellintensität zwischen minus 1,5 und plus 1,5 Millimeter; synthetischer nichtrelativistischer Lehrfall, kein historischer Intensitätsscan.",
    ) {
        ActivityText(12.0, 18.0, "I/I(0)")
        ActivityText(344.0, 18.0, "Modell", TextAnchor.END)
        ActivityLine(39.0, 30.0, 39.0, 132.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(39.0, 132.0, 343.0, 132.0, StationActivityClass.DRAWING_LINE)
        ActivityText(27.0, 35.0, "1", TextAnchor.END)
        ActivityText(27.0, 136.0, "0", TextAnchor.END)
        ActivityPath(path, StationActivityClass.DRAWING_TRACE)
        ActivityText(39.0, 157.0, "−1,5", TextAnchor.MIDDLE)
        ActivityText(191.0, 157.0, "0", TextAnchor.MIDDLE)
        ActivityText(343.0, 157.0, "1,5 mm", TextAnchor.END)
    }
}

@Composable
private fun schematicOpenings() {
    ActivitySvg(
        StationActivityId.MEDIA_SCHEMA,
        360.0,
        166.0,
        "Vereinfachtes Funktionsschema: Quelle, Blende mit zwei Öffnungen, Beobachtung. Keine Einzelbahnen und keine maßstäbliche historische Apparatur.",
    ) {
        ActivityRect(24.0, 65.0, 49.0, 40.0, StationActivityClass.DRAWING_SURFACE)
        ActivityText(48.0, 89.0, "Quelle", TextAnchor.MIDDLE)
        ActivityLine(82.0, 85.0, 145.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(137.0, 79.0, 145.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(137.0, 91.0, 145.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityRect(160.0, 31.0, 18.0, 108.0, StationActivityClass.DRAWING_SURFACE)
        ActivityRect(160.0, 56.0, 18.0, 15.0, StationActivityClass.DRAWING_ACCENT)
        ActivityRect(160.0, 98.0, 18.0, 15.0, StationActivityClass.DRAWING_ACCENT)
        ActivityText(169.0, 158.0, "2 Öffnungen", TextAnchor.MIDDLE)
        ActivityLine(193.0, 85.0, 254.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(246.0, 79.0, 254.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(246.0, 91.0, 254.0, 85.0, StationActivityClass.DRAWING_LINE)
        ActivityRect(275.0, 35.0, 13.0, 101.0, StationActivityClass.DRAWING_SURFACE)
        ActivityText(281.0, 19.0, "Beobachtung", TextAnchor.MIDDLE)
    }
}

@Composable
fun LightQuantumComparison() {
    Section(
        attrs = {
            elementId(StationActivityId.LIGHT)
            stationActivityStyles(StationActivityClass.ROOT)
        }
    ) {
        H3 { Text("Zwei Schritte der Quantenidee") }
        Div(attrs = { stationActivityStyles(StationActivityClass.CARD_GRID) }) {
            Section(attrs = { stationActivityStyles(StationActivityClass.CARD) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("1900 · MAX PLANCK")
                }
                H4 { Text("Energieelemente für materielle Resonatoren") }
                P { Text("Gegenstand: schwingungsfähige Systeme im Ansatz zur Wärmestrahlung.") }
                Details(attrs = {
                    elementId(StationActivityId.LIGHT_PLANCK_ENERGY)
                    styles(LabClass.NOTES)
                }) {
                    Summary { Text("Plancks Energiebeziehung ansehen") }
                    P(attrs = { stationActivityStyles(StationActivityClass.EQUATION) }) {
                        Text("Eₙ = n h f")
                    }
                    P {
                        Text(
                            "Schulische Kurzform: Energie in Vielfachen von hf. n ist eine nichtnegative ganze Zahl, f die Frequenz und h das Plancksche Wirkungsquantum."
                        )
                    }
                }
            }
            Section(attrs = { stationActivityStyles(StationActivityClass.CARD) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("1905 · ALBERT EINSTEIN")
                }
                H4 { Text("Lichtquanten") }
                P {
                    Text(
                        "Gegenstand: die Energie des Lichts selbst; ein Lichtquant wird heute Photon genannt."
                    )
                }
                Details(attrs = {
                    elementId(StationActivityId.LIGHT_EINSTEIN_ENERGY)
                    styles(LabClass.NOTES)
                }) {
                    Summary { Text("Einsteins Energiebeziehung ansehen") }
                    P(attrs = { stationActivityStyles(StationActivityClass.EQUATION) }) {
                        Text("Eγ = h f")
                    }
                    P {
                        Text(
                            "Ein Photon mit Frequenz f trägt die Energie hf. γ kennzeichnet das Photon. Das verknüpft eine Wellengröße mit portionsweiser Energieübertragung."
                        )
                    }
                }
            }
        }
        P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
            Text(
                "Historische Kurzfassung, keine erfundenen Zitate. Die Lichtanalogie motiviert eine Frage über Materie; sie ist noch kein experimenteller Test der Materiewellenbeziehung."
            )
        }
    }
}

@Composable
fun ReceptionTimeline() {
    Section(
        attrs = {
            elementId(StationActivityId.RECEPTION)
            stationActivityStyles(StationActivityClass.ROOT, StationActivityClass.TIMELINE)
        }
    ) {
        H3 { Text("Ein Ereignis · verschiedene Belegzeiten") }
        Section(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_LANE) }) {
            H4 { Text("Ereigniszeit · Was geschah 1924?") }
            Div(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_ENTRY) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("25. NOVEMBER 1924")
                }
                P {
                    Text(
                        "De Broglie verteidigt seine Doktorarbeit. Die Disputation ist die mündliche Prüfung."
                    )
                }
            }
            Div(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_ENTRY) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("16. DEZEMBER 1924")
                }
                P {
                    Text(
                        "In der historischen Untersuchung von Sievers wird Einsteins Brief an Langevin behandelt. Dieses Datum liegt nach der Disputation."
                    )
                }
            }
        }
        Section(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_LANE) }) {
            H4 { Text("Quellenzeit · Was liegt hier als Beleg vor?") }
            Div(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_ENTRY) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("1924 · REFERIERTE KORRESPONDENZ")
                }
                P {
                    Text(
                        "Zeitgenössische Einschätzungen werden von der späteren Untersuchung ausgewertet. Die Brieforiginale wurden für diesen Kurs nicht zusätzlich im Archiv geprüft."
                    )
                }
            }
            Div(attrs = { stationActivityStyles(StationActivityClass.TIMELINE_ENTRY) }) {
                P(attrs = { stationActivityStyles(StationActivityClass.YEAR) }) {
                    Text("SPÄTER · RÜCKBLICK UND UNTERSUCHUNG")
                }
                P {
                    Text(
                        "H1 ist de Broglies spätere Erinnerung, hier in englischer Übersetzung. H3 ist Sievers’ historische Sekundäranalyse. Eine Erinnerung und ein damaliger Brief sind verschiedene Quellenarten."
                    )
                }
            }
        }
        P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
            Text(
                "Die Vermittlung über Langevin und Einsteins positive Einschätzung erklären wissenschaftliche Aufnahme. Sie messen keine Elektronenwellenlänge. Ein exakter Versandtag des Dissertationsexemplars wird hier nicht behauptet."
            )
        }
    }
}

private data class DgEvidenceInput(
    val size: String,
    val value: String,
    val unit: String,
    val source: String,
    val role: String,
)

@Composable
fun DgEvidenceTable() {
    val lattice = Length(.352e-9)
    val rowSpacing = Physics.nickel111RowSpacing(lattice)
    val inputs =
        listOf(
            DgEvidenceInput(
                "Elektronenenergie E_kin",
                "54",
                "eV",
                "Suzuki/Suzuki 2013 · sekundär berichteter historischer Fall",
                "Legt den Energieeingang des Spannungszweigs fest.",
            ),
            DgEvidenceInput(
                "Beschleunigungsbetrag U",
                "54",
                "V",
                "Aus 54 eV für ein Elektron unter Start-aus-Ruhe-Annahme",
                "E_kin = eU → p → λNR; nicht zusätzlich am Foto abgelesen.",
            ),
            DgEvidenceInput(
                "Winkel φ zur äußeren Oberflächennormalen",
                "ungefähr 50",
                "°",
                "Suzuki/Suzuki · Richtung des berichteten Maximums",
                "Mit D und n = 1 in λgeo = D sin φ.",
            ),
            DgEvidenceInput(
                "Ni-Gitterkonstante a",
                "0,352",
                "nm",
                "Struktureingabe im ausgewählten Ni(111)-Modell von Suzuki/Suzuki",
                "Räumliche Skala vor dem λ-Vergleich festlegen.",
            ),
            DgEvidenceInput(
                "Reihenabstand D",
                number(rowSpacing.nanometers, 6),
                "nm",
                "Berechnet aus a: D = a√(3/8)",
                "Geometriezweig; kein aus λNR angepasster Abstand und nicht d111.",
            ),
        )
    Section(attrs = { stationActivityStyles(StationActivityClass.ROOT) }) {
        H3(attrs = { elementId(StationActivityId.DG_TITLE) }) { Text("Welche Zahl kommt woher?") }
        P {
            Text(
                "Diese Tabelle gilt für den sekundär berichteten 54-eV-Fall bei ungefähr 50°. Eigene Laborvariationen bleiben als Modellfälle separat."
            )
        }
        Div(
            attrs = {
                stationActivityStyles(StationActivityClass.TABLE_SCROLL)
                focusIndex(0)
                ariaLabel("Herkunftstabelle; auf schmalen Ansichten seitlich verschiebbar")
            }
        ) {
            Table(
                attrs = {
                    elementId(StationActivityId.DG_TABLE)
                    labelledBy(StationActivityId.DG_TITLE)
                    stationActivityStyles(StationActivityClass.TABLE)
                }
            ) {
                Tr {
                    listOf("Größe", "Wert", "Einheit", "Herkunft", "Rolle im Vergleich").forEach {
                        label ->
                        Th(attrs = { activityColumnHeading() }) { Text(label) }
                    }
                }
                inputs.forEach { input ->
                    Tr {
                        Td { Text(input.size) }
                        Td(attrs = { stationActivityStyles(StationActivityClass.MONO) }) {
                            Text(input.value)
                        }
                        Td(attrs = { stationActivityStyles(StationActivityClass.MONO) }) {
                            Text(input.unit)
                        }
                        Td { Text(input.source) }
                        Td { Text(input.role) }
                    }
                }
            }
        }
        P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
            Text(
                "Der Winkel ist ungefähr berichtet. Die sechs Rechenstellen von D sind interne Rechenpräzision, keine historische Messunsicherheit. Beide Rechenwege bleiben bis zum Vergleich getrennt."
            )
        }
    }
}

private data class LimitAssignment(
    val id: LimitCaseId,
    val statement: String,
    val expected: LimitKind,
    val explanation: String,
)

private val limitAssignments =
    listOf(
        LimitAssignment(
            LimitCaseId.NR_50KV,
            "Bei 50 kV verändert die relativistische Impulsbeziehung die berechnete Wellenlänge, obwohl dieselbe Beschleunigungsspannung eingesetzt wird.",
            LimitKind.MODEL,
            "Hier wird die Angemessenheit einer Rechennäherung geprüft. Bei gleicher Eingabe liefert die relativistische Erweiterung einen anderen Impuls und damit eine andere λ-Vorhersage.",
        ),
        LimitAssignment(
            LimitCaseId.SOURCE_VOLTAGE,
            "Das verwendete Foto lässt sich aus den vorliegenden Angaben nicht sicher dem 40-kV- oder dem 50-kV-Fall zuordnen.",
            LimitKind.SOURCE,
            "Hier geht es darum, welche Angabe durch welchen Quellenbeleg dieser Aufnahme zugeordnet ist. Eine genauere Formel entscheidet nicht, welche Spannung zur Aufnahme gehört.",
        ),
        LimitAssignment(
            LimitCaseId.SCAN_SCALE,
            "Ein Abstand von 14 nativen Pixeln soll in Meter übersetzt werden; eine unabhängige Zuordnung zwischen Raster und Beobachtungsebene fehlt.",
            LimitKind.CALIBRATION,
            "Hier fehlt der unabhängige physikalische Bildmaßstab. Eine bekannte Rastergröße oder der Aufbauabstand allein übersetzt Pixel noch nicht in Meter.",
        ),
    )

private val LimitKind.activityLabel: String
    get() =
        when (this) {
            LimitKind.MODEL -> "Rechenmodell"
            LimitKind.SOURCE -> "Quellenzuordnung"
            LimitKind.CALIBRATION -> "Bildkalibrierung"
        }

@Composable
fun LimitsExercise(session: LabSession) {
    val state = session.snapshot.inquiry
    val complete = limitAssignments.all { it.id in state.limitAssignments }
    Section(
        attrs = {
            elementId(StationActivityId.LIMITS)
            stationActivityStyles(StationActivityClass.ROOT)
        }
    ) {
        H3 { Text("Drei Aussagen · drei Prüfaufgaben") }
        P(attrs = { elementId(StationActivityId.LIMITS_HELP) }) {
            Text(
                "Ordne jede Aussage einer Kategorie zu. Geprüft wird nur diese Auswahl. Begründe anschließend eine Zuordnung im eigenen Notizfeld; dein Freitext wird hier nicht automatisch bewertet."
            )
        }
        limitAssignments.forEachIndexed { index, task ->
            val selectedKind = state.limitAssignments[task.id]
            Section(attrs = { stationActivityStyles(StationActivityClass.LIMIT_CASE) }) {
                H4 { Text("Aussage ${index + 1}") }
                P { Text(task.statement) }
                FieldLabel(LimitAssignmentId(task.id)) {
                    Text("Kategorie für Aussage ${index + 1}")
                }
                Select(
                    attrs = {
                        elementId(LimitAssignmentId(task.id))
                        describedBy(StationActivityId.LIMITS_HELP)
                        onChange { event ->
                            val nextKind = LimitKind.entries.firstOrNull { it.name == event.value }
                            session.update { snapshot ->
                                snapshot.copy(
                                    inquiry =
                                        snapshot.inquiry.copy(
                                            limitAssignments =
                                                if (nextKind == null)
                                                    snapshot.inquiry.limitAssignments - task.id
                                                else
                                                    snapshot.inquiry.limitAssignments +
                                                        (task.id to nextKind),
                                            limitsChecked = false,
                                        )
                                )
                            }
                        }
                    }
                ) {
                    Option(
                        value = ActivityOption.UNSELECTED.value,
                        attrs = { if (selectedKind == null) selected() },
                    ) {
                        Text("Bitte auswählen")
                    }
                    LimitKind.entries.forEach { kind ->
                        Option(
                            value = kind.name,
                            attrs = { if (selectedKind == kind) selected() },
                        ) {
                            Text(kind.activityLabel)
                        }
                    }
                }
                if (state.limitsChecked)
                    P(attrs = { stationActivityStyles(StationActivityClass.FEEDBACK) }) {
                        Text(
                            "${if (selectedKind == task.expected) "Zuordnung passt" else "Zuordnung überdenken"}: ${task.expected.activityLabel}. ${task.explanation}"
                        )
                    }
            }
        }
        Button(
            attrs = {
                elementId(StationActivityId.LIMITS_CHECK)
                styles(LabClass.PRIMARY)
                if (!complete) disabled()
                onClick {
                    session.update { it.copy(inquiry = it.inquiry.copy(limitsChecked = true)) }
                }
            }
        ) {
            Text("Drei Zuordnungen prüfen")
        }
        if (state.limitsChecked)
            P(
                attrs = {
                    elementId(StationActivityId.LIMITS_FEEDBACK)
                    role(AriaRole.STATUS)
                    stationActivityStyles(StationActivityClass.FEEDBACK)
                }
            ) {
                val matching =
                    limitAssignments.count { state.limitAssignments[it.id] == it.expected }
                Text(
                    "$matching von 3 Kategorien stimmen mit den Kriterien überein. Das prüft die Zuordnung, nicht deine freie Erklärung oder den gesamten Lernstand."
                )
            }
        else if (!complete)
            P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
                Text("Wähle für alle drei Aussagen eine Kategorie, bevor du prüfst.")
            }
    }
}

/** Editable evidence rows start empty; importing one's own work changes only the evidence field. */
@Composable
fun ConclusionEvidenceTable(session: LabSession) {
    val snapshot = session.snapshot
    val calculation = ownCalculationSuggestion(snapshot)
    val measurement = ownMeasurementSuggestion(snapshot)
    fun editRow(index: Int, transform: (EvidenceRow) -> EvidenceRow) {
        session.update { current ->
            current.copy(
                inquiry =
                    current.inquiry.copy(
                        evidenceRows =
                            current.inquiry.evidenceRows.mapIndexed { rowIndex, row ->
                                if (rowIndex == index) transform(row) else row
                            }
                    )
            )
        }
    }
    Section(
        attrs = {
            elementId(StationActivityId.EVIDENCE)
            stationActivityStyles(StationActivityClass.ROOT)
        }
    ) {
        H3 { Text("Freiwillige Belegtabelle") }
        P(attrs = { elementId(StationActivityId.EVIDENCE_HELP) }) {
            Text(
                "Wenn du deinen Schluss weiter ausarbeiten möchtest, kannst du hier bis zu drei Aussagen mit Belegen und Grenzen ergänzen. Deine Rechnung oder Bildauswahl kannst du als Beleg übernehmen. Unbenutzte Zeilen erscheinen nicht im Arbeitsbericht."
            )
        }
        P(attrs = { elementId(StationActivityId.EVIDENCE_AVAILABILITY); stationActivityStyles(StationActivityClass.MUTED) }) {
            Text("Übernehmen ersetzt nur das Belegfeld der gewählten Zeile. Prüfe selbst, ob der Beleg zu deiner Aussage passt.")
            if (calculation == null) Text(" Für einen Rechenbeleg bearbeite zuerst die 150-V-Aufgabe im Kapitel De Broglie.")
            if (measurement == null) Text(" Für einen Bildbeleg halte zuerst deine begründete Auswahl im Kapitel Jönsson fest.")
        }
        snapshot.inquiry.evidenceRows.forEachIndexed { index, row ->
            Section(
                attrs = {
                    elementId(EvidenceRowId(index))
                    ariaLabel("Belegzeile ${index + 1}")
                    stationActivityStyles(StationActivityClass.EVIDENCE_ROW)
                }
            ) {
                H4 { Text("Aussage ${index + 1}") }
                Div(attrs = { stationActivityStyles(StationActivityClass.EVIDENCE_GRID) }) {
                    EvidenceCell(
                        index,
                        EvidenceField.CLAIM,
                        "Aussage · Was folgerst du?",
                        row.claim,
                        session.isReadOnly,
                    ) { value ->
                        editRow(index) { it.copy(claim = value) }
                    }
                    EvidenceCell(
                        index,
                        EvidenceField.EVIDENCE,
                        "Beleg · Worauf stützt du dich?",
                        row.evidence,
                        session.isReadOnly,
                    ) { value ->
                        editRow(index) { it.copy(evidence = value) }
                    }
                    EvidenceCell(
                        index,
                        EvidenceField.LIMITATION,
                        "Annahme oder Grenze",
                        row.limitation,
                        session.isReadOnly,
                    ) { value ->
                        editRow(index) { it.copy(limitation = value) }
                    }
                }
                Div(attrs = { stationActivityStyles(StationActivityClass.EVIDENCE_ACTIONS) }) {
                    Button(
                        attrs = {
                            elementId(EvidenceSuggestionId(index, EvidenceSuggestion.CALCULATION))
                            protectPersonalWork(session.isReadOnly)
                            describedBy(StationActivityId.EVIDENCE_AVAILABILITY)
                            styles(LabClass.SECONDARY, LabClass.COMPACT)
                            if (calculation == null) disabled()
                            onClick {
                                calculation?.let { suggestion ->
                                    editRow(index) {
                                        it.copy(evidence = suggestion.take(EVIDENCE_TEXT_LIMIT))
                                    }
                                }
                            }
                        }
                    ) {
                        Text("150-V-Antwort übernehmen")
                    }
                    Button(
                        attrs = {
                            elementId(EvidenceSuggestionId(index, EvidenceSuggestion.MEASUREMENT))
                            protectPersonalWork(session.isReadOnly)
                            describedBy(StationActivityId.EVIDENCE_AVAILABILITY)
                            styles(LabClass.SECONDARY, LabClass.COMPACT)
                            if (measurement == null) disabled()
                            onClick {
                                measurement?.let { suggestion ->
                                    editRow(index) {
                                        it.copy(evidence = suggestion.take(EVIDENCE_TEXT_LIMIT))
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Eigene Bildauswahl übernehmen")
                    }
                }
            }
        }
        Details(attrs = { styles(LabClass.NOTES) }) {
            Summary() { Text("Welche eigenen Belege sind zum Übernehmen vorhanden?") }
            P(attrs = { stationActivityStyles(StationActivityClass.SUGGESTION) }) {
                Text(
                    calculation
                        ?: "Bearbeite zuerst die eigene Rechnung im Kapitel De Broglie."
                )
            }
            P(attrs = { stationActivityStyles(StationActivityClass.SUGGESTION) }) {
                Text(
                    measurement
                        ?: "Halte zuerst deine begründete Bildauswahl im Kapitel Jönsson fest."
                )
            }
        }
        P(attrs = { stationActivityStyles(StationActivityClass.MUTED) }) {
            Text(
                "Jedes Feld fasst bis zu 1000 Zeichen. Ergänze passende Quellen und erkläre deine Argumentation anschließend ohne Bildschirm an einem veränderten Beispiel."
            )
        }
    }
}

@Composable
private fun EvidenceCell(
    index: Int,
    field: EvidenceField,
    label: String,
    value: String,
    readOnly: Boolean,
    onEdit: (String) -> Unit,
) {
    Div(attrs = { stationActivityStyles(StationActivityClass.EVIDENCE_CELL) }) {
        FieldLabel(EvidenceFieldId(index, field)) { Text(label) }
        TextArea(
            value = value,
            attrs = {
                elementId(EvidenceFieldId(index, field))
                protectPersonalWork(readOnly)
                textRows(4)
                maxLength(EVIDENCE_TEXT_LIMIT)
                describedBy(StationActivityId.EVIDENCE_HELP)
                onInput { event -> onEdit(event.value.take(EVIDENCE_TEXT_LIMIT)) }
            },
        )
    }
}

private fun ownCalculationSuggestion(snapshot: SessionSnapshot): String? {
    val work = snapshot.stationWork(StationId.S06_CALCULATION)
    if (work.numericAnswer.isBlank()) return null
    val status =
        if (!work.checked) "noch nicht zahlengeprüft"
        else
            when (NumericWavelengthQuestion(Voltage(150.0)).evaluate(work.numericAnswer).status) {
                NumericAnswerStatus.CORRECT ->
                    "lokale Zahlenprüfung passend; freie Herleitung nicht bewertet"
                else -> "lokale Zahlenprüfung noch nicht passend; Rechnung erneut prüfen"
            }
    return "Meine Antwort für U = 150 V: ${work.numericAnswer}. Status: $status. Musterrechnung ${if (work.solutionViewed) "geöffnet" else "nicht als geöffnet protokolliert"}."
}

private fun ownMeasurementSuggestion(snapshot: SessionSnapshot): String? {
    val initial = snapshot.measurement.initialSelection ?: return null
    val opened =
        initial.priorReferenceMaterials.joinToString { it.label }.ifEmpty { "keine protokolliert" }
    return "Meine gesicherte Bildauswahl: Band ${initial.bandTopInclusivePx} ≤ y < ${initial.bandBottomExclusivePx}; x / native px = ${initial.markers.joinToString { it.xPx.toString() }}; Einzelabstände / px = ${initial.intervalsPx.joinToString()}; Mittel = ${number(initial.meanPx, 3)} native px. Vor dem Referenzmarkenvergleich gesichert. Zuvor protokollierte Materialkontakte: $opened. Relative Bildmessung, keine absolute λ-Kalibrierung."
}
