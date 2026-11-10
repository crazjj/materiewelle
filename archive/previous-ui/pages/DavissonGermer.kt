package materiewelle.pages

import androidx.compose.runtime.Composable
import materiewelle.*
import materiewelle.core.DgSetting
import materiewelle.core.Experiment
import materiewelle.core.ExperimentMode
import materiewelle.core.SurfaceNormalAngle
import materiewelle.core.Voltage
import materiewelle.core.course.DgDiagram
import materiewelle.core.course.DgSession
import materiewelle.data.LabAsset
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private fun modelAngle(raw: String): Double? =
    raw.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.1..90.0 }

/**
 * The free editor is bounded; an explicitly imported, validated source voltage remains restorable.
 */
private fun modelVoltage(raw: String, data: ScientificData): Double? =
    Physics.inputVoltage(raw)
        ?: raw.trim().replace(',', '.').toDoubleOrNull()?.takeIf { value ->
            value.isFinite() && value > 0.0 && data.points.any { it.voltageV == value }
        }

/** A controlled experiment: raw text, accepted values, comparison and view live in the session. */
@Composable
fun DgSection(
    data: ScientificData?,
    error: String?,
    session: DgSession,
    onSessionChange: ((DgSession) -> DgSession) -> Unit,
) {
    Div(attrs = { styles(LabClass.SECTION_HEADING) }) {
        Div {
            H1 { Text("Impuls trifft Beugungsgeometrie") }
            P(attrs = { styles(LabClass.SUBTITLE) }) {
                Text(
                    "Ein belegter Sekundärbefund, zwei unabhängige Rechenwege und ein klar benanntes Modell."
                )
            }
        }
        Span(attrs = { styles(LabClass.STATUS_TAG, LabClass.REFERENCE_TEXT) }) {
            Text("D2 · SEKUNDÄRBEFUND")
        }
    }
    if (data == null) {
        P(
            attrs = {
                styles(if (error == null) LabClass.LOADING_STATE else LabClass.SOURCE_WARNING)
                role(if (error == null) AriaRole.STATUS else AriaRole.ALERT)
            }
        ) {
            Text(error ?: "Datengrundlage wird geladen …")
        }
        return
    }
    val reported = data.points.firstOrNull { it.id == session.selectedId } ?: data.points.first()
    val point =
        DgSetting(Voltage(session.validModelVoltage), SurfaceNormalAngle(session.validModelAngle))
    val sourceCase =
        point.voltageV == reported.voltageV && point.angleDegrees == reported.angleDegrees
    val predicted = Physics.electronWavelength(point.voltage).meters
    val diffraction = Physics.surfaceDiffraction(data.spacing, point.surfaceAngle).meters
    val difference = Physics.relativeDifferencePercent(diffraction, predicted)
    val signedDifference = Physics.signedRelativeDifferencePercent(diffraction, predicted)
    val comparison =
        session.comparisonVoltage?.let { volts ->
            session.comparisonAngle?.let { angle ->
                DgSetting(Voltage(volts), SurfaceNormalAngle(angle))
            }
        }
    val validVoltage = modelVoltage(session.modelVoltageRaw, data) != null
    val validAngle = modelAngle(session.modelAngleRaw) != null
    fun resetToSource() = onSessionChange { state ->
        state.copy(
            selectedId = reported.id,
            modelVoltageRaw = reported.voltageV.toString(),
            modelAngleRaw = reported.angleDegrees.toString(),
            validModelVoltage = reported.voltageV,
            validModelAngle = reported.angleDegrees,
        )
    }

    P(
        attrs = {
            styles(LabClass.SOURCE_WARNING)
            elementId(Id.DG_DATA_STATUS)
        }
    ) {
        Text(data.dgWarning)
    }
    if (predicted > data.spacingM) {
        P(
            attrs = {
                styles(LabClass.MODEL_LIMIT)
                elementId(Id.DG_MODEL_LIMIT)
                role(AriaRole.STATUS)
            }
        ) {
            Text(
                "Modellgrenze: λdB > D. Diese Modellvariation hat bei normalem Einfall keine reelle Richtung erster Ordnung. Daraus folgt kein historischer Messpunkt."
            )
        }
    }
    Div(attrs = { styles(LabClass.DG_WORKSPACE, LabClass.LAB_WORKSPACE) }) {
        Section(
            attrs = {
                styles(LabClass.GRAPH_AREA, LabClass.LAB_PANEL)
                ariaLabel("Davisson–Germer Versuchsdarstellung")
            }
        ) {
            Div(attrs = { styles(LabClass.PANEL_TOOLBAR) }) {
                ModeSwitch(Experiment.DAVISSON_GERMER, session.mode) { mode ->
                    onSessionChange { state -> state.copy(mode = mode) }
                }
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY, LabClass.COMPACT)
                        elementId(Id.DG_SAVE_COMPARISON)
                        onClick {
                            onSessionChange { state ->
                                state.copy(
                                    comparisonVoltage = state.validModelVoltage,
                                    comparisonAngle = state.validModelAngle,
                                )
                            }
                        }
                    }
                ) {
                    Text("Als B merken")
                }
            }
            Div(
                attrs = {
                    styles(LabClass.PLOT_SWITCH)
                    role(AriaRole.GROUP)
                    ariaLabel("Beschriftete DG-Darstellungen")
                }
            ) {
                Button(
                    attrs = {
                        elementId(Id.DG_VIEW_GEOMETRY)
                        ariaPressed(session.view == DgDiagram.SETUP)
                        onClick { onSessionChange { state -> state.copy(view = DgDiagram.SETUP) } }
                    }
                ) {
                    Text("Aufbau")
                }
                Button(
                    attrs = {
                        elementId(Id.DG_VIEW_BRAGG)
                        ariaPressed(session.view == DgDiagram.BRAGG)
                        onClick { onSessionChange { state -> state.copy(view = DgDiagram.BRAGG) } }
                    }
                ) {
                    Text("Bragg")
                }
                Button(
                    attrs = {
                        elementId(Id.DG_VIEW_SURFACE)
                        ariaPressed(session.view == DgDiagram.SURFACE)
                        onClick {
                            onSessionChange { state -> state.copy(view = DgDiagram.SURFACE) }
                        }
                    }
                ) {
                    Text("Ni(111)")
                }
                Button(
                    attrs = {
                        elementId(Id.DG_VIEW_COMPARISON)
                        ariaPressed(session.view == DgDiagram.WAVELENGTHS)
                        onClick {
                            onSessionChange { state -> state.copy(view = DgDiagram.WAVELENGTHS) }
                        }
                    }
                ) {
                    Text("λ-Vergleich")
                }
            }
            when (session.view) {
                DgDiagram.SETUP ->
                    NativeSvg(detectorChart(point.angleDegrees, comparison?.angleDegrees))
                DgDiagram.BRAGG ->
                    NativeSvg(braggGeometryChart(session.geometryStep.coerceAtMost(2)))
                DgDiagram.SURFACE ->
                    NativeSvg(
                        nickelSurfaceChart(
                            point.angleDegrees,
                            data.dgStructure.latticeConstant.nanometers,
                        )
                    )
                DgDiagram.WAVELENGTHS ->
                    NativeSvg(
                        dgWavelengthChart(
                            listOf(point) + listOfNotNull(comparison),
                            data.spacingM,
                            0,
                            comparison?.let { 1 },
                        )
                    )
            }
            if (session.view == DgDiagram.BRAGG) {
                Div(attrs = { styles(LabClass.STEP_CONTROLS) }) {
                    P(
                        attrs = {
                            elementId(Id.DG_GEOMETRY_STEP)
                            ariaLive(LiveMode.POLITE)
                        }
                    ) {
                        Text(
                            when (session.geometryStep.coerceAtMost(2)) {
                                0 -> "Konstruktion: gleiche Bezugsebenen vor und nach der Streuung."
                                1 -> "Einlaufend: PB = d sin θᴮ ist der erste zusätzliche Weg."
                                else ->
                                    "Auslaufend: BQ = d sin θᴮ kommt hinzu. Zusammen: 2d sin θᴮ."
                            }
                        )
                    }
                    Button(
                        attrs = {
                            styles(LabClass.SECONDARY)
                            elementId(Id.DG_GEOMETRY_NEXT)
                            onClick {
                                onSessionChange { state ->
                                    state.copy(geometryStep = (state.geometryStep + 1) % 3)
                                }
                            }
                        }
                    ) {
                        Text("Nächster Konstruktionsschritt")
                    }
                }
            }
            Div(
                attrs = {
                    styles(LabClass.CHART_READOUT)
                    elementId(Id.DG_COORDINATES)
                    data(LabData.VOLTAGE_V, point.voltageV)
                    data(LabData.ANGLE_DEG, point.angleDegrees)
                }
            ) {
                Span { Text(if (sourceCase) "D2-FALL · A" else "MODELLVARIATION · A") }
                Span { Text("U ${number(point.voltageV, 1)} V") }
                Span { Text("φ ${if (sourceCase) "≈ " else ""}${number(point.angleDegrees, 1)}°") }
                Span { Text("D ${number(data.spacing.nanometers, 3)} nm") }
            }
            P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
                Text(
                    when (session.view) {
                        DgDiagram.SETUP ->
                            "Schema, keine Intensitätsaufnahme. Normaler Einfall auf Ni(111); φ zur äußeren Normalen. Die drehbare Detektorrichtung wird mit dem Winkel verändert. Bei fester Spannung wurde eine bevorzugte Streurichtung berichtet; eine vollständige historische Winkelkurve ist hier nicht digitalisiert."
                        DgDiagram.BRAGG ->
                            "Allgemeine Bragg-Konstruktion: θᴮ zur Ebene, d zwischen Netzebenen. Die beiden zusätzlichen Wege addieren sich zur Phasenpassung. Die gezeichneten 30° dienen nur der Konstruktion; keine Umdeutung des DG-50°-Winkels."
                        DgDiagram.SURFACE ->
                            "Gewähltes Oberflächenmodell: ausgewählte parallele Atomreihen und Streuebene senkrecht dazu. Einlaufend kein Gangunterschied; auslaufend AC = D sin φ. Azimut und Oberfläche sind Teil der Annahme."
                        DgDiagram.WAVELENGTHS ->
                            "Linie: de-Broglie-Modell λ(U). Kreise: aus den gewählten Einstellungen berechnetes D sin φ. Verbindungsstücke zeigen Rechendifferenzen, keine Fehlerbalken oder gemessene Intensität."
                    }
                )
            }
            comparison?.let { saved ->
                Div(
                    attrs = {
                        styles(LabClass.SAVED_SETTING)
                        elementId(Id.DG_COMPARISON)
                        data(LabData.VOLTAGE_V, saved.voltageV)
                        data(LabData.ANGLE_DEG, saved.angleDegrees)
                    }
                ) {
                    Span(attrs = { styles(LabClass.REFERENCE_TEXT) }) {
                        Text("B · GESPEICHERTE EINSTELLUNG")
                    }
                    Span {
                        Text("${number(saved.voltageV, 1)} V / ${number(saved.angleDegrees, 1)}°")
                    }
                    Span {
                        Text(
                            "λgeo ${number(Physics.surfaceDiffraction(data.spacing, saved.surfaceAngle).nanometers, 3)} nm"
                        )
                    }
                    Span {
                        Text(
                            "λdB ${number(Physics.electronWavelength(saved.voltage).nanometers, 3)} nm"
                        )
                    }
                    Button(
                        attrs = {
                            styles(LabClass.ICON_BUTTON)
                            elementId(Id.DG_CLEAR_COMPARISON)
                            ariaLabel("DG-Vergleich B entfernen")
                            onClick {
                                onSessionChange { state ->
                                    state.copy(comparisonVoltage = null, comparisonAngle = null)
                                }
                            }
                        }
                    ) {
                        Text("×")
                    }
                }
            }
        }
        Section(attrs = { styles(LabClass.DG_DATA, LabClass.LAB_PANEL) }) {
            P(attrs = { styles(LabClass.PANEL_LABEL) }) { Text("BEFUND → STRUKTUR → RECHNUNG") }
            Div(attrs = { styles(LabClass.CONTROL_BODY) }) {
                Div(
                    attrs = {
                        styles(LabClass.DATA_POINTS)
                        role(AriaRole.GROUP)
                        ariaLabel("Dokumentierten Quellenfall übernehmen")
                    }
                ) {
                    Button(
                        attrs = {
                            styles(LabClass.DATA_CHOICE)
                            if (sourceCase) styles(LabClass.CHOSEN)
                            ariaPressed(sourceCase)
                            data(LabData.DG_INDEX, 0)
                            onClick { resetToSource() }
                        }
                    ) {
                        Text("D2 · 54 eV / ≈ 50°")
                    }
                }
                FieldLabel(forId = Id.DG_MODEL_VOLTAGE) { Text("Modellspannung U / V") }
                Input(
                    InputType.Text,
                    attrs = {
                        elementId(Id.DG_MODEL_VOLTAGE)
                        value(session.modelVoltageRaw)
                        inputMode(LabInputMode.DECIMAL)
                        ariaInvalid(!validVoltage)
                        onInput { event ->
                            onSessionChange { state ->
                                state.copy(
                                    modelVoltageRaw = event.value.take(120),
                                    validModelVoltage =
                                        modelVoltage(event.value, data) ?: state.validModelVoltage,
                                )
                            }
                        }
                    },
                )
                FieldLabel(forId = Id.DG_MODEL_ANGLE) {
                    Text("Detektorwinkel φ zur äußeren Normalen / °")
                }
                Input(
                    InputType.Text,
                    attrs = {
                        elementId(Id.DG_MODEL_ANGLE)
                        value(session.modelAngleRaw)
                        inputMode(LabInputMode.DECIMAL)
                        ariaInvalid(!validAngle)
                        onInput { event ->
                            onSessionChange { state ->
                                state.copy(
                                    modelAngleRaw = event.value.take(120),
                                    validModelAngle =
                                        modelAngle(event.value) ?: state.validModelAngle,
                                )
                            }
                        }
                    },
                )
                if (!validVoltage || !validAngle)
                    P(
                        attrs = {
                            styles(LabClass.ERROR)
                            role(AriaRole.ALERT)
                        }
                    ) {
                        Text(
                            "Freier Modellbereich: 20–1 000 V und 0,1–90°; ausdrücklich geladene Quellenspannungen bleiben zusätzlich erlaubt. Ungültiger Text bleibt bearbeitbar; Grafik und Rechnung behalten die letzten gültigen Werte."
                        )
                    }
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        elementId(Id.DG_RESET_MODEL)
                        onClick { resetToSource() }
                    }
                ) {
                    Text("D2-Quellenfall wiederherstellen")
                }
                if (!sourceCase)
                    P(attrs = { styles(LabClass.SOURCE_WARNING) }) {
                        Text(
                            "Eigene Modellvariation: Die geänderten Zahlen sind kein neu belegter historischer Messpunkt."
                        )
                    }
                if (session.mode == ExperimentMode.GUIDED) {
                    P(
                        attrs = {
                            styles(LabClass.GUIDED_PROGRESS)
                            elementId(Id.DG_STEP)
                            ariaLive(LiveMode.POLITE)
                        }
                    ) {
                        Text("Schritt ${session.step + 1} von 4")
                    }
                    when (session.step) {
                        0 -> {
                            H2 { Text("Zwei unabhängige Eingänge") }
                            P {
                                Text(
                                    "D2 berichtet E_kin = 54 eV und φ ≈ 50°. Im Beschleunigungsmodell E_kin = eU entspricht das 54 V. Der Strukturwert a = 0,352 nm wird unabhängig zur Berechnung von D verwendet; wir passen D nicht an λ an."
                                )
                            }
                        }
                        1 -> DgDiffraction(point, data.spacingM, diffraction)
                        2 -> DgPrediction(point, predicted)
                        else -> {
                            DgDifference(difference, signedDifference)
                            P {
                                Text(
                                    "Beide Werte liegen bei etwa 0,17 nm. Der grobe Winkel und fehlende historische Unsicherheiten erlauben Größenordnungskonsistenz, keine statistische Gütebewertung."
                                )
                            }
                        }
                    }
                    Div(attrs = { styles(LabClass.STEP_CONTROLS) }) {
                        Button(
                            attrs = {
                                styles(LabClass.SECONDARY)
                                elementId(Id.DG_BACK)
                                if (session.step == 0) disabled()
                                onClick {
                                    onSessionChange { state ->
                                        state.copy(step = (state.step - 1).coerceAtLeast(0))
                                    }
                                }
                            }
                        ) {
                            Text("Zurück")
                        }
                        Button(
                            attrs = {
                                styles(LabClass.PRIMARY)
                                elementId(Id.DG_NEXT)
                                onClick {
                                    onSessionChange { state ->
                                        state.copy(step = (state.step + 1) % 4)
                                    }
                                }
                            }
                        ) {
                            Text(if (session.step == 3) "Erneut beginnen" else "Weiter")
                        }
                    }
                } else {
                    DgDiffraction(point, data.spacingM, diffraction)
                    DgPrediction(point, predicted)
                    DgDifference(difference, signedDifference)
                }
            }
        }
    }
    Section(
        attrs = {
            styles(LabClass.EVIDENCE_STRIP)
            elementId(Id.DG_STRUCTURE)
        }
    ) {
        H2 { Text("Warum D kein passend gewählter Atomabstand ist") }
        P {
            Text(
                "Die angenommene fcc-Ni(111)-Oberfläche bildet ein Dreiecksnetz. Ihr nächster Nachbarabstand ist a/√2 ≈ ${number(data.dgStructure.neighborDistance.nanometers, 3)} nm. Der senkrechte Abstand der ausgewählten Reihen folgt aus der Höhe des gleichseitigen Dreiecks: D = (a/√2)·sin 60° = a√(3/8) ≈ ${number(data.spacing.nanometers, 3)} nm. Mit ungerundetem D wird weitergerechnet."
            )
        }
        P {
            Text(
                "Dagegen ist d₁₁₁ = a/√3 ≈ ${number(data.dgStructure.planeSpacing.nanometers, 3)} nm ein Abstand paralleler Ebenen. D ≠ d₁₁₁. Ein anderer Azimut kann eine andere wirksame Oberflächenperiodizität haben; daher gilt das ausgewählte Modell nicht für beliebige Kristallrichtungen."
            )
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Strukturannahme: ${data.dgStructure.evidence.sourceId}, ${data.dgStructure.evidence.location}. ${data.dgStructure.evidence.scope}"
            )
        }
    }
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text("Phasenpassung, Bragg und Grenzen") }
        P {
            Text(
                "Konstruktive Überlagerung verlangt ganzzahlige Wellenlängen im Gangunterschied Δs = nλ. Bei Bragg sind es zwei Zusatzstrecken d sin θᴮ. Beim normal einfallenden Oberflächenmodell ist es eine auslaufende Projektion D sin φ. Gleiche Idee, unterschiedliche geometrische Größen und Winkelbezüge."
            )
        }
        P {
            Text(
                "Der Nickel-Einkristall liefert eine regelmäßige Anordnung mit fester Orientierung. Ein ungeordnetes Material hat nicht dieselbe gerichtete Phasenpassung. Die hier ausgewählte zweidimensionale Lehrbeschreibung ersetzt keine vollständige Theorie der Elektronenstreuung im Festkörper."
            )
        }
        P {
            Text(
                "Das Modell gibt erlaubte Richtungen, keine Intensitätskurve. Der Vergleich nutzt φ ≈ 50° aus D2, nicht den für perfekte Übereinstimmung aus λ rückgerechneten Winkel. Historische Unsicherheiten sind unbekannt; zusätzliche Nachkommastellen dienen allein der Rechenkontrolle."
            )
        }
    }
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text("Quellen und historischer Prüfstatus") }
        P {
            Text(
                "Befund: " +
                    data.dgEvidence.sourceId +
                    ", " +
                    data.dgEvidence.location +
                    ". " +
                    data.dgEvidence.scope
            )
        }
        P {
            Text("Suzuki/Suzuki (2013), didaktische Sekundäranalyse, arXiv:1307.6049v1. ")
            A(href = "https://arxiv.org/abs/1307.6049", attrs = { externalLink() }) {
                Text("D2 öffnen")
            }
        }
        P {
            Text(
                "Davisson/Germer (1927), Phys. Rev. 30, 705: bibliografisch identifiziert, D1 im dokumentierten Zugriff nicht eingesehen. Die früheren sechs KI-Planpaare sind aus der aktiven Belegbasis entfernt. "
            )
            A(href = "https://doi.org/10.1103/PhysRev.30.705", attrs = { externalLink() }) {
                Text("D1-DOI")
            }
        }
        A(href = LabAsset.SCIENTIFIC_DATA.path, attrs = { externalLink() }) {
            Text("Versionierten Datensatz mit Fundstellen öffnen")
        }
    }
}

@Composable
fun DgDiffraction(point: DgSetting, spacing: Double, result: Double) {
    Div(attrs = { styles(LabClass.CALCULATION) }) {
        H2 {
            Span(attrs = { styles(LabClass.CALCULATION_NUMBER) }) { Text("01") }
            Text("Aus der Beugungsgeometrie")
        }
        P(attrs = { styles(LabClass.CALCULATION_INPUT) }) {
            Text("D ≈ ${number(spacing * 1e9, 3)} nm · φ ${number(point.angleDegrees, 1)}° · n = 1")
        }
        Div(
            attrs = {
                styles(LabClass.COMPARE_VALUE)
                elementId(Id.DG_DIFFRACTION)
                data(LabData.VALUE_NM, result * 1e9)
            }
        ) {
            Text("≈ ${number(result * 1e9, 3)} nm")
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("λgeo = D sin φ · Rechenkontrolle: ${number(result * 1e9, 6)} nm")
        }
    }
}

@Composable
fun DgPrediction(point: DgSetting, result: Double) {
    Div(attrs = { styles(LabClass.CALCULATION) }) {
        H2 {
            Span(attrs = { styles(LabClass.CALCULATION_NUMBER) }) { Text("02") }
            Text("Aus de Broglies Hypothese")
        }
        P(attrs = { styles(LabClass.CALCULATION_INPUT) }) {
            Text("U = ${number(point.voltageV, 1)} V · aus Ruhe · nichtrelativistisch")
        }
        Div(
            attrs = {
                styles(LabClass.COMPARE_VALUE)
                elementId(Id.DG_PREDICTED)
                data(LabData.VALUE_NM, result * 1e9)
            }
        ) {
            Text("≈ ${number(result * 1e9, 3)} nm")
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("λdB = h / √(2mₑeU) · Rechenkontrolle: ${number(result * 1e9, 6)} nm")
        }
    }
}

@Composable
fun DgDifference(difference: Double, signedDifference: Double) {
    Div(attrs = { styles(LabClass.DIFFERENCE) }) {
        H2 { Text("Rechnerische Differenz") }
        Strong(
            attrs = {
                elementId(Id.DG_DEVIATION)
                data(LabData.VALUE_PERCENT, difference)
            }
        ) {
            Text("Betrag ≈ ${number(difference, 1)} %")
        }
        P(
            attrs = {
                elementId(Id.DG_SIGNED_DEVIATION)
                data(LabData.VALUE_PERCENT, signedDifference)
            }
        ) {
            Text("Mit Vorzeichen: ${number(signedDifference, 2)} % · (λgeo − λdB)/λdB")
        }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "Bezogen auf die Vorhersage, keine belegte historische Fehlergrenze oder statistische Güte. Für den D2-Quellenfall mit 54 eV und ungefähr 50° wird der Vergleich auf rund 1,1 % gerundet. Dieser Hinweis beschreibt den Quellenfall; oben stehen die Werte deiner aktuellen Einstellung."
        )
    }
}
