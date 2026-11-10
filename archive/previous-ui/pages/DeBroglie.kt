package materiewelle.pages

import androidx.compose.runtime.*
import kotlin.math.round
import materiewelle.*
import materiewelle.core.Experiment
import materiewelle.core.ExperimentMode
import materiewelle.core.Voltage
import materiewelle.core.course.DeBroglieSession
import materiewelle.ui.MathText as Text
import materiewelle.ui.ReadingUpdate
import materiewelle.ui.WavelengthMotion
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

/** Raw editable text and last validated voltage are owned by the persistent experiment shell. */
@Composable
fun DeBroglie(session: DeBroglieSession, update: ((DeBroglieSession) -> DeBroglieSession) -> Unit) {
    val raw = session.raw
    val voltage = session.lastValidVoltage
    fun edit(text: String) = update { it.edit(text) }
    fun select(volts: Double) = update { it.edit(volts.toString().removeSuffix(".0")) }
    val valid = Physics.inputVoltage(raw) != null
    val current = Voltage(voltage)
    val wavelength = Physics.electronWavelength(current)
    val comparison = session.comparisonVoltage?.let(::Voltage)
    var cursor by remember { mutableStateOf<Voltage?>(null) }
    val mode = session.mode
    val step = session.step
    var chartElement by remember { mutableStateOf<HTMLElement?>(null) }
    fun inspect(clientX: Double) {
        val bounds = chartElement?.getBoundingClientRect() ?: return
        if (bounds.width <= 0.0) return
        val nativeX = (clientX - bounds.left) / bounds.width * 760.0
        cursor = Voltage(round(((nativeX - 64.0) / 674.0 * 1000.0).coerceIn(20.0, 1000.0)))
    }
    val inspected = cursor ?: current
    val reference = comparison

    Div(attrs = { styles(LabClass.SECTION_HEADING) }) {
        Div {
            H1 { Text("Wellenlänge unter Spannung") }
            P(attrs = { styles(LabClass.SUBTITLE) }) {
                Text(
                    "Wie kurz ist eine Materiewelle? Verändere die Spannung und prüfe die Vorhersage."
                )
            }
        }
        Span(attrs = { styles(LabClass.STATUS_TAG) }) { Text("MODELLRECHNUNG") }
    }
    Div(attrs = { styles(LabClass.WORKSPACE, LabClass.LAB_WORKSPACE) }) {
        Section(
            attrs = {
                styles(LabClass.GRAPH_AREA, LabClass.LAB_PANEL)
                ariaLabel("Spannungsabhängigkeit")
            }
        ) {
            Div(attrs = { styles(LabClass.PANEL_TOOLBAR) }) {
                ModeSwitch(Experiment.DE_BROGLIE, mode) { selectedMode ->
                    update { it.copy(mode = selectedMode) }
                }
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY, LabClass.COMPACT)
                        elementId(Id.SAVE_COMPARISON)
                        onClick { update { it.copy(comparisonVoltage = it.lastValidVoltage) } }
                    }
                ) {
                    Text("Als B merken")
                }
            }
            Div(attrs = { styles(LabClass.CHART_HEADING) }) {
                P(attrs = { styles(LabClass.AXIS_TITLE) }) { Text("Wellenlänge λ / pm") }
                Div(attrs = { styles(LabClass.CHART_LEGEND) }) {
                    Span(attrs = { styles(LabClass.LEGEND_CURRENT) }) {
                        Text("A · ${number(voltage, 1)} V")
                    }
                    reference?.let {
                        Span(attrs = { styles(LabClass.LEGEND_REFERENCE) }) {
                            Text("B · ${number(it.volts, 1)} V")
                        }
                        Button(
                            attrs = {
                                styles(LabClass.ICON_BUTTON)
                                elementId(Id.CLEAR_COMPARISON)
                                ariaLabel("Vergleich B entfernen")
                                onClick { update { it.copy(comparisonVoltage = null) } }
                            }
                        ) {
                            Text("×")
                        }
                    }
                }
            }
            Div(
                attrs = {
                    elementId(Id.DEBROGLIE_CHART)
                    styles(LabClass.INTERACTIVE_CHART)
                    focusIndex(0)
                    role(AriaRole.GROUP)
                    ariaLabel("Kurve erkunden: Pfeiltasten ändern die Ablesekoordinate um 10 Volt")
                    describedBy(Id.CHART_COORDINATES, Id.CHART_KEYBOARD_HINT)
                    ref {
                        chartElement = it
                        onDispose { chartElement = null }
                    }
                    onMouseMove { inspect(it.clientX.toDouble()) }
                    onClick { inspect(it.clientX.toDouble()) }
                    onMouseLeave { cursor = null }
                    onKeyDown { event ->
                        val key = LabKey.fromBrowser(event.key)
                        val delta =
                            when (key) {
                                LabKey.LEFT,
                                LabKey.DOWN -> -10.0
                                LabKey.RIGHT,
                                LabKey.UP -> 10.0
                                else -> null
                            }
                        if (delta != null) {
                            event.preventDefault()
                            cursor =
                                Voltage(((cursor ?: current).volts + delta).coerceIn(20.0, 1000.0))
                        }
                        if (key == LabKey.HOME) {
                            event.preventDefault()
                            cursor = Voltage(20.0)
                        }
                        if (key == LabKey.END) {
                            event.preventDefault()
                            cursor = Voltage(1000.0)
                        }
                        if (key == LabKey.ESCAPE) cursor = null
                    }
                }
            ) {
                NativeSvg(wavelengthChart(voltage, reference?.volts, cursor?.volts))
            }
            Div(
                attrs = {
                    styles(LabClass.CHART_READOUT)
                    elementId(Id.CHART_COORDINATES)
                    data(LabData.VOLTAGE_V, inspected.volts)
                    data(
                        LabData.WAVELENGTH_PM,
                        (Physics.electronWavelength(inspected).meters * 1e12),
                    )
                }
            ) {
                Span { Text("KURVENKOORDINATE") }
                Span { Text("U ${number(inspected.volts, 0)} V") }
                Span {
                    Text("λ ${number(Physics.electronWavelength(inspected).meters * 1e12, 4)} pm")
                }
            }
            P(
                attrs = {
                    styles(LabClass.GRAPH_CAPTION)
                    elementId(Id.CHART_KEYBOARD_HINT)
                }
            ) {
                Text(
                    "Modellkurve · λ ∝ 1/√U · Maus oder Tippen zum Ablesen; Pfeiltasten: ±10 V. Keine Messdaten."
                )
            }
            WavelengthMotion(voltage)
        }
        Section(attrs = { styles(LabClass.CONTROLS, LabClass.LAB_PANEL) }) {
            P(attrs = { styles(LabClass.PANEL_LABEL) }) { Text("STEUERUNG") }
            Div(attrs = { styles(LabClass.CONTROL_BODY) }) {
                FieldLabel(forId = Id.VOLTAGE) { Text("Beschleunigungsspannung U") }
                Div(attrs = { styles(LabClass.NUMBER_ROW) }) {
                    Input(
                        InputType.Text,
                        attrs = {
                            elementId(Id.VOLTAGE)
                            value(raw)
                            inputMode(LabInputMode.DECIMAL)
                            autoComplete(CompletionMode.OFF)
                            ariaInvalid((!valid))
                            describedBy(if (valid) Id.VOLTAGE_RANGE else Id.VOLTAGE_ERROR)
                            onInput { edit(it.value) }
                        },
                    )
                    Span(attrs = { styles(LabClass.UNIT) }) { Text("V") }
                }
                if (!valid)
                    P(
                        attrs = {
                            elementId(Id.VOLTAGE_ERROR)
                            styles(LabClass.ERROR)
                            role(AriaRole.ALERT)
                        }
                    ) {
                        Text(
                            "Bitte eine Zahl von 20 bis 1 000 V eingeben. Grafik und Ergebnis behalten den letzten gültigen Wert."
                        )
                    }
                Input(
                    InputType.Range,
                    attrs = {
                        elementId(Id.VOLTAGE_SLIDER)
                        ariaLabel("Beschleunigungsspannung in Volt")
                        minimum(20)
                        maximum(1000)
                        continuousStep()
                        value(voltage)
                        onInput { it.value?.toDouble()?.let(::select) }
                    },
                )
                Div(
                    attrs = {
                        styles(LabClass.RANGE_LABELS)
                        elementId(Id.VOLTAGE_RANGE)
                    }
                ) {
                    Span { Text("20 V") }
                    Span { Text("1 000 V") }
                }
                P(attrs = { styles(LabClass.CONTROL_LABEL) }) { Text("SCHNELLAUSWAHL") }
                Div(attrs = { styles(LabClass.QUICK_VALUES) }) {
                    Button(
                        attrs = {
                            styles(LabClass.SECONDARY)
                            elementId(Id.RESET)
                            ariaLabel("Zurücksetzen auf 150 Volt")
                            ariaPressed((voltage == 150.0 && valid))
                            onClick { select(150.0) }
                        }
                    ) {
                        Text("150 V")
                    }
                    Button(
                        attrs = {
                            styles(LabClass.SECONDARY)
                            elementId(Id.TRY_600)
                            ariaLabel("600 V ausprobieren")
                            ariaPressed((voltage == 600.0 && valid))
                            onClick { select(600.0) }
                        }
                    ) {
                        Text("600 V")
                    }
                    Button(
                        attrs = {
                            styles(LabClass.SECONDARY)
                            ariaPressed((voltage == 1000.0 && valid))
                            onClick { select(1000.0) }
                        }
                    ) {
                        Text("1 000 V")
                    }
                }
                Div(
                    attrs = {
                        styles(LabClass.RESULT)
                        ariaLive(LiveMode.POLITE)
                    }
                ) {
                    P(attrs = { styles(LabClass.CONTROL_LABEL) }) {
                        Text("A · BERECHNETE WELLENLÄNGE")
                    }
                    Div(
                        attrs = {
                            styles(LabClass.RESULT_VALUE)
                            elementId(Id.WAVELENGTH_PM)
                            data(LabData.VALUE_PM, (wavelength.meters * 1e12))
                        }
                    ) {
                        ReadingUpdate(voltage) { Text("${number(wavelength.meters * 1e12, 4)} pm") }
                    }
                    P(attrs = { styles(LabClass.SI_VALUE) }) {
                        Text(
                            "${number(wavelength.meters * 1e10, 4)} × 10⁻¹⁰ m · ${number(voltage, 1)} V"
                        )
                    }
                }
                reference?.let { saved ->
                    Div(
                        attrs = {
                            styles(LabClass.COMPARISON_DISPLAY)
                            elementId(Id.DEBROGLIE_COMPARISON)
                        }
                    ) {
                        P(attrs = { styles(LabClass.CONTROL_LABEL, LabClass.REFERENCE_TEXT) }) {
                            Text("B · GEMERKTE EINSTELLUNG")
                        }
                        Div(attrs = { styles(LabClass.COMPARISON_LINE) }) {
                            Span(
                                attrs = {
                                    elementId(Id.COMPARISON_VOLTAGE)
                                    data(LabData.VOLTAGE_V, saved.volts)
                                }
                            ) {
                                Text("${number(saved.volts, 1)} V")
                            }
                            Span(
                                attrs = {
                                    elementId(Id.COMPARISON_WAVELENGTH_PM)
                                    data(
                                        LabData.VALUE_PM,
                                        (Physics.electronWavelength(saved).meters * 1e12),
                                    )
                                }
                            ) {
                                Text(
                                    "${number(Physics.electronWavelength(saved).meters * 1e12, 4)} pm"
                                )
                            }
                        }
                        P(
                            attrs = {
                                styles(LabClass.RATIO_READOUT)
                                elementId(Id.COMPARISON_RATIO)
                                data(
                                    LabData.RATIO,
                                    (wavelength.meters / Physics.electronWavelength(saved).meters),
                                )
                            }
                        ) {
                            Text(
                                "λA / λB = ${number(wavelength.meters / Physics.electronWavelength(saved).meters, 4)}"
                            )
                        }
                    }
                }
            }
        }
    }
    if (mode == ExperimentMode.GUIDED) {
        Section(
            attrs = {
                styles(LabClass.GUIDED_PANEL)
                ariaLabel("Geführte Herleitung")
            }
        ) {
            P(
                attrs = {
                    styles(LabClass.GUIDED_PROGRESS)
                    elementId(Id.DEBROGLIE_STEP)
                    ariaLive(LiveMode.POLITE)
                }
            ) {
                Text("Schritt ${step + 1} von 3")
            }
            when (step) {
                0 ->
                    DerivationStep(
                        "01",
                        "Energie gewinnen",
                        "Das Elektron startet aus der Ruhe. Der positive Betrag der durchlaufenen Spannung liefert kinetische Energie.",
                        "eU = Eₖᵢₙ",
                    )
                1 ->
                    DerivationStep(
                        "02",
                        "Impuls bestimmen",
                        "Mit Eₖᵢₙ = p²/(2mₑ) folgt im nichtrelativistischen Modell der Betrag des Impulses.",
                        "p = √(2mₑeU)",
                    )
                else ->
                    DerivationStep(
                        "03",
                        "Hypothese einsetzen",
                        "De Broglies λ = h/p ist die zusätzliche Hypothese. Vierfache Spannung halbiert die Wellenlänge. Teste 150 V und 600 V mit Vergleich B.",
                        "λ = h / √(2mₑeU)",
                    )
            }
            Div(attrs = { styles(LabClass.STEP_CONTROLS) }) {
                Button(
                    attrs = {
                        styles(LabClass.SECONDARY)
                        elementId(Id.DEBROGLIE_BACK)
                        if (step == 0) disabled()
                        onClick { update { it.copy(step = (it.step - 1).coerceAtLeast(0)) } }
                    }
                ) {
                    Text("Zurück")
                }
                Button(
                    attrs = {
                        styles(LabClass.PRIMARY)
                        elementId(Id.DEBROGLIE_NEXT)
                        onClick { update { it.copy(step = if (it.step == 2) 0 else it.step + 1) } }
                    }
                ) {
                    Text(if (step == 2) "Erneut beginnen" else "Weiter")
                }
            }
        }
    }
    Div(attrs = { styles(LabClass.MODEL_STRIP) }) {
        Div {
            P(attrs = { styles(LabClass.CONTROL_LABEL) }) { Text("MODELLRECHNUNG") }
            P { Text("Beschleunigtes Elektron · Start aus Ruhe") }
        }
        Formula("λ = h / √(2mₑeU)")
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Intern SI-Einheiten · nichtrelativistische Näherung · keine unabhängige Bestätigung der Hypothese"
            )
        }
    }
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text("Herleitung in drei Schritten") }
        Div(attrs = { styles(LabClass.DERIVATION) }) {
            DerivationStep(
                "01",
                "Energie",
                "Die Beschleunigung aus Ruhe liefert die kinetische Energie des Elektrons.",
                "eU = Eₖᵢₙ",
            )
            DerivationStep(
                "02",
                "Impuls",
                "Aus der nichtrelativistischen Energie-Impuls-Beziehung folgt der Impulsbetrag.",
                "p = √(2mₑeU)",
            )
            DerivationStep(
                "03",
                "De Broglie",
                "Die Verbindung zwischen Impuls und Wellenlänge ist eine zusätzliche Hypothese.",
                "λ = h/p",
            )
        }
    }
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text("Annahmen, Größen und Modellgrenzen") }
        P {
            Text(
                "h: Planck-Konstante; p: Impulsbetrag; mₑ: Elektronenmasse; e: positive Elementarladung; U: positiver Betrag der Beschleunigungsspannung. Es wird ein anfangs ruhendes Elektron und die nichtrelativistische Energie angenommen. Der Eingabebereich ist auf 20–1 000 V begrenzt."
            )
        }
        P {
            Text(
                "Diese Kurve zeigt die Konsequenz der Hypothese; sie ist keine unabhängige experimentelle Bestätigung. 150 V entsprechen ungefähr 0,100 nm: eine atomare Längenskala. Mehr Nachkommastellen dienen der Rechenkontrolle, nicht einer behaupteten Messgenauigkeit."
            )
        }
    }
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text("Quellen und Prüfstatus") }
        P {
            Text(
                "h = 6,62607015 × 10⁻³⁴ J s und e = 1,602176634 × 10⁻¹⁹ C sind exakte SI-Konstanten; mₑ = 9,1093837139 × 10⁻³¹ kg nach CODATA 2022. Formel und 150-V-Aufgabe: bereitgestelltes Aufgabenblatt, S. 1. Der 600-V-Fall sowie die Kurve sind eigene Modellrechnungen."
            )
        }
        A(href = "https://physics.nist.gov/cgi-bin/cuu/Value?me", attrs = { externalLink() }) {
            Text("CODATA: Elektronenmasse")
        }
    }
}
