package materiewelle.learning

import androidx.compose.runtime.*
import materiewelle.NativeSvg
import materiewelle.core.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.Element

private val SetupPart.title: String
    get() =
        when (this) {
            SetupPart.SOURCE -> "Elektronenquelle"
            SetupPart.SLITS -> "Doppelspalt"
            SetupPart.SCREEN -> "Beobachtungsebene"
        }
private val SetupPart.explanation: String
    get() =
        when (this) {
            SetupPart.SOURCE ->
                "Ein monochromatischer, kohärenter Strahl wird im Modell mit U beschleunigt. Seine Wellenlänge folgt aus λ = h/p. Die Schemazeichnung zeigt keine einzelnen Teilchenbahnen."
            SetupPart.SLITS ->
                "Die zwei idealen Punktspalte besitzen den Abstand a. Die Wellenamplituden überlagern sich. Endliche Spaltbreite, Beugungshülle und partielle Kohärenz fehlen hier."
            SetupPart.SCREEN ->
                "Für kleine Winkel ist der Streifenabstand Δy = L λ/a. Das normierte Profil ist cos²(π y/Δy). Es ist eine Modellintensität, kein Grauwertprofil der Jönsson-Reproduktion."
        }

@Composable
fun SetupExample(active: Boolean) {
    var volts by remember { mutableStateOf(150.0) }
    var distance by remember { mutableStateOf(1.0) }
    var spacing by remember { mutableStateOf(1.0) }
    var part by remember { mutableStateOf(SetupPart.SLITS) }
    var phase by remember { mutableStateOf(0.0) }
    var running by remember { mutableStateOf(false) }
    val reduced = rememberReducedMotion()
    val model =
        remember(volts, distance, spacing) {
            DoubleSlitModel(Voltage(volts), Length(distance), Length(spacing * 1e-6))
        }
    val profileMarkup = remember(model) { profileSvg(model) }
    TimelineClock(running, active, reduced, { phase = (phase + it / 2) % 1 }, { running = false })
    Div(attrs = { styles(LabClass.WORKSPACE, LabClass.WORKSHOP_WORKSPACE) }) {
        Section(attrs = { styles(LabClass.GRAPH_AREA) }) {
            H2 { Text("Doppelspalt: Aufbau und Überlagerung") }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text(
                    "Unabhängiges Lehrmodell · kein maßstäblicher Nachbau des historischen Versuchs"
                )
            }
            Div(
                attrs = {
                    elementId(Id.SETUP_SCHEMATIC)
                    data(LabData.PHASE, phase)
                    data(LabData.RUNNING, running)
                    onClick { e ->
                        val selected =
                            (e.target as? Element)
                                ?.closest("[${LabData.SETUP_PART.attribute}]")
                                ?.getAttribute(LabData.SETUP_PART.attribute)
                        SetupPart.fromAttribute(selected)?.let { part = it }
                    }
                }
            ) {
                NativeSvg(setupSvg(model, phase, part))
            }
            Div(
                attrs = {
                    elementId(Id.SETUP_PARTS)
                    styles(LabClass.WORKSHOP_ACTIONS)
                    role(AriaRole.GROUP)
                    ariaLabel("Bauteil erkunden")
                }
            ) {
                SetupPart.entries.forEach { item ->
                    Button(
                        attrs = {
                            elementId(item.controlId)
                            styles(LabClass.SECONDARY)
                            if (item == part) styles(LabClass.CHOSEN)
                            ariaPressed((item == part))
                            onClick { part = item }
                        }
                    ) {
                        Text(item.title)
                    }
                }
            }
            Div(
                attrs = {
                    styles(LabClass.MODEL_LIMIT)
                    elementId(Id.SETUP_EXPLANATION)
                    ariaLive(LiveMode.POLITE)
                }
            ) {
                Strong() { Text(part.title) }
                P { Text(part.explanation) }
            }
            NativeSvg(profileMarkup)
            Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                Button(
                    attrs = {
                        elementId(Id.SETUP_PLAY)
                        styles(LabClass.PRIMARY)
                        ariaPressed(running)
                        if (reduced) disabled()
                        onClick { running = !running }
                    }
                ) {
                    Text(if (running) "Wellenfronten anhalten" else "Wellenfronten bewegen")
                }
                Button(
                    attrs = {
                        elementId(Id.SETUP_STEP)
                        styles(LabClass.SECONDARY)
                        onClick {
                            running = false
                            phase = (phase + .125) % 1
                        }
                    }
                ) {
                    Text("Ein Bild weiter")
                }
            }
            P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
                Text(
                    "Wellenfronten vergrößert und schematisch. Das Animationstempo ist keine reale Flugzeit. Das berechnete Profil zeigt relative Intensität ohne Beugungshülle; keine Kalibrierung des historischen Fotos."
                )
            }
        }
        Aside(attrs = { styles(LabClass.CONTROLS) }) {
            P(attrs = { styles(LabClass.EYEBROW) }) { Text("MODELLPARAMETER") }
            ModelSlider(Id.SETUP_VOLTAGE, "Spannung U", volts, "V", 50.0, 900.0, 10.0, 0) {
                volts = it
            }
            ModelSlider(Id.SETUP_DISTANCE, "Abstand L", distance, "m", 0.5, 2.0, 0.1, 1) {
                distance = it
            }
            ModelSlider(Id.SETUP_SPACING, "Spaltabstand a", spacing, "µm", 0.5, 2.0, 0.1, 1) {
                spacing = it
            }
            Div(
                attrs = {
                    styles(LabClass.RESULT)
                    elementId(Id.SETUP_RESULT)
                    data(LabData.PERIOD_UM, model.period.micrometers)
                    data(LabData.WAVELENGTH_PM, model.wavelength.picometers)
                }
            ) {
                P { Text("BERECHNETER STREIFENABSTAND") }
                Div(attrs = { styles(LabClass.RESULT_VALUE) }) {
                    Text("${Physics.decimal(model.period.micrometers, 3)} µm")
                }
                P { Text("λ = ${Physics.decimal(model.wavelength.picometers, 4)} pm") }
                P(attrs = { styles(LabClass.FORMULA) }) { Text("Δy = L λ / a") }
            }
            Button(
                attrs = {
                    elementId(Id.SETUP_RESET)
                    styles(LabClass.SECONDARY)
                    onClick {
                        volts = 150.0
                        distance = 1.0
                        spacing = 1.0
                        phase = 0.0
                        running = false
                    }
                }
            ) {
                Text("Modell zurücksetzen")
            }
            WorkshopNotes(
                "Modellgrenzen und historischer Bezug",
                "Gleiche kohärente Punktspalte, Fernfeld und kleine Winkel, nicht relativistische Elektronen. L = 1 m und a = 1 µm sind frei gewählte Lehrparameter, keine zu Jönssons Foto belegten Parameter. Für die Bildmessung und historische Quellenlage öffne das Jönsson-Labor.",
            )
        }
    }
}

@Composable
private fun ModelSlider(
    id: ElementId,
    label: String,
    value: Double,
    unit: String,
    min: Double,
    max: Double,
    step: Double,
    digits: Int,
    change: (Double) -> Unit,
) {
    FieldLabel(forId = id) { Text("$label · ${Physics.decimal(value, digits)} $unit") }
    Input(
        InputType.Range,
        attrs = {
            elementId(id)
            minimum(min)
            maximum(max)
            increment(step)
            value(value)
            onInput { it.value?.toDouble()?.let(change) }
        },
    )
}

private fun setupSvg(model: DoubleSlitModel, phase: Double, selected: SetupPart): VectorFragment =
    vector {
        svg({
            viewBox(0, 0, 760, 360)
            imageRole()
            ariaLabel(
                "Schema eines Doppelspalts mit Elektronenquelle, zwei Spalten und Beobachtungsebene; frei gewählte Lehrparameter, keine historischen Messdaten."
            )
        }) {
            definitions({}) {
                clipPath({ elementId(Id.TEACHING_WAVES) }) {
                    rect({
                        atX(282)
                        atY(50)
                        width(368)
                        height(240)
                    })
                }
            }
            line({
                x1(80)
                y1(170)
                x2(650)
                y2(170)
                stroke(VectorPaint.MUTED)
                dash(4, 6)
            })
            label(
                "L = ${Physics.decimal(model.distance.meters, 1)} m",
                {
                    atX(500)
                    atY(326)
                    fill(VectorPaint.MUTED)
                    fontSize(15)
                },
            )
            line({
                x1(290)
                y1(302)
                x2(643)
                y2(302)
                stroke(VectorPaint.MUTED)
            })
            group({
                clip(Id.TEACHING_WAVES)
                fill(VectorPaint.None)
                stroke(VectorPaint.ACCENT)
                strokeWidth(1.5)
                opacity(0.45)
            }) {
                for (y in listOf(150, 190)) for (n in 0..6) run {
                    circle({
                        cx(280)
                        cy(y)
                        radius((n + phase) * 65)
                    })
                }
            }
            group({
                setupPart(SetupPart.SOURCE)
                styles(LabClass.SETUP_CLICKABLE)
            }) {
                rect({
                    atX(45)
                    atY(143)
                    width(100)
                    height(54)
                    cornerRadius(6)
                    fill(VectorPaint.SURFACE_RAISED)
                    stroke(
                        if (selected == SetupPart.SOURCE) VectorPaint.ACCENT else VectorPaint.MUTED
                    )
                    strokeWidth(3)
                })
                label(
                    "Quelle",
                    {
                        atX(95)
                        atY(175)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.TEXT)
                        fontSize(16)
                    },
                )
                path({
                    geometry("M146 170 H257 M244 160 L257 170 L244 180")
                    fill(VectorPaint.None)
                    stroke(VectorPaint.ACCENT)
                    strokeWidth(2.5)
                })
            }
            label(
                "${Physics.decimal(model.voltage.volts, 0)} V",
                {
                    atX(50)
                    atY(120)
                    fill(VectorPaint.ACCENT)
                    fontSize(15)
                },
            )
            group({
                setupPart(SetupPart.SLITS)
                styles(LabClass.SETUP_CLICKABLE)
                stroke(if (selected == SetupPart.SLITS) VectorPaint.ACCENT else VectorPaint.MUTED)
                strokeWidth(7)
            }) {
                path({ geometry("M280 50 V142 M280 158 V182 M280 198 V290") })
                rect({
                    atX(265)
                    atY(50)
                    width(30)
                    height(240)
                    fill(VectorPaint.TRANSPARENT)
                    stroke(VectorPaint.None)
                })
            }
            label(
                "a = ${Physics.decimal(model.slitSpacing.micrometers, 1)} µm",
                {
                    atX(300)
                    atY(41)
                    fill(VectorPaint.TEXT)
                    fontSize(15)
                },
            )
            group({
                setupPart(SetupPart.SCREEN)
                styles(LabClass.SETUP_CLICKABLE)
            }) {
                rect({
                    atX(648)
                    atY(50)
                    width(32)
                    height(240)
                    fill(VectorPaint.SURFACE_RAISED)
                    stroke(
                        if (selected == SetupPart.SCREEN) VectorPaint.ACCENT else VectorPaint.MUTED
                    )
                    strokeWidth(2)
                })
                for (n in 0..119) {
                    val intensity = model.relativeIntensity((n / 119.0 - .5) * 600e-6)
                    run {
                        line({
                            x1(651)
                            x2(677)
                            y1(50 + n * 2)
                            y2(50 + n * 2)
                            stroke(VectorPaint.ACCENT)
                            strokeWidth(2)
                            opacity(intensity)
                        })
                    }
                }
            }
            label(
                "Schirm",
                {
                    atX(684)
                    atY(171)
                    fill(VectorPaint.TEXT)
                    fontSize(13)
                },
            )
            label(
                "Abstände im Aufbau nicht maßstäblich · normiertes Lehrmodell",
                {
                    atX(50)
                    atY(344)
                    fill(VectorPaint.MUTED)
                    fontSize(14)
                },
            )
        }
    }

private fun profileSvg(model: DoubleSlitModel): VectorFragment = vector {
    svg({
        elementId(Id.SETUP_INTENSITY_PROFILE)
        viewBox(0, 0, 760, 190)
        imageRole()
        ariaLabel(
            "Berechnete relative Intensität des idealen Doppelspalts über der Schirmkoordinate, minus300 bis plus300 Mikrometer."
        )
    }) {
        title("Normierte Modellintensität, keine Bildgrauwerte")
        label(
            "I / Imax",
            {
                atX(52)
                atY(20)
                fill(VectorPaint.MUTED)
                fontSize(13)
            },
        )
        for (v in listOf(-300, -150, 0, 150, 300)) {
            val x = 52 + (v + 300) / 600.0 * 656
            run {
                line({
                    x1(x)
                    x2(x)
                    y1(30)
                    y2(135)
                    stroke(VectorPaint.GRID)
                })
                label(
                    v,
                    {
                        atX(x)
                        atY(158)
                        anchor(TextAnchor.MIDDLE)
                        fill(VectorPaint.MUTED)
                        fontSize(13)
                    },
                )
            }
        }
        val points =
            (0..500).joinToString(" ") { n ->
                val x = 52 + n / 500.0 * 656
                val y = 135 - model.relativeIntensity((n / 500.0 - .5) * 600e-6) * 95
                "${if (n == 0) "M" else "L"}$x,$y"
            }
        line({
            x1(52)
            x2(708)
            y1(135)
            y2(135)
            stroke(VectorPaint.MUTED)
        })
        path({
            geometry(points)
            fill(VectorPaint.None)
            stroke(VectorPaint.ACCENT)
            strokeWidth(2)
        })
        label(
            "1",
            {
                atX(40)
                atY(45)
                anchor(TextAnchor.END)
                fill(VectorPaint.MUTED)
                fontSize(13)
            },
        )
        label(
            "0",
            {
                atX(40)
                atY(135)
                anchor(TextAnchor.END)
                fill(VectorPaint.MUTED)
                fontSize(13)
            },
        )
        label(
            "Schirmkoordinate y / µm",
            {
                atX(708)
                atY(184)
                anchor(TextAnchor.END)
                fill(VectorPaint.MUTED)
                fontSize(13)
            },
        )
    }
}
