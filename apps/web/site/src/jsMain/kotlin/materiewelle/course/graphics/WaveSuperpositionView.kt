package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import materiewelle.core.ClassicalSuperposition
import materiewelle.core.Physics
import materiewelle.core.course.InquiryState
import materiewelle.learning.TimelineClock
import materiewelle.learning.rememberReducedMotion
import materiewelle.ui.LocalLabMotion
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private const val WAVE_LEFT = 38.0
private const val WAVE_RIGHT = 616.0

/** Classical bridge: equally frequent coherent contributions, a shared x and t, actual sum. */
@Composable
fun WaveSuperpositionView(
    state: InquiryState,
    onChange: ((InquiryState) -> InquiryState) -> Unit,
    active: Boolean = true,
) {
    val reduced = rememberReducedMotion()
    var running by remember { mutableStateOf(false) }
    var inView by remember { mutableStateOf(false) }
    var autoplayConsumed by remember { mutableStateOf(false) }
    val motion = LocalLabMotion.current
    var timeTurns by remember { mutableStateOf(0.0) }
    var probe by remember { mutableStateOf(135.0) }
    val model =
        remember(state.waveAmplitude, state.waveWavelength, state.phaseTurns) {
            ClassicalSuperposition(state.waveAmplitude, state.waveWavelength, state.phaseTurns)
        }
    LaunchedEffect(active, reduced, inView, motion.active) {
        if (!active || reduced) running = false
        if (reduced) autoplayConsumed = true
        if (active && inView && motion.active && !reduced && !autoplayConsumed) {
            autoplayConsumed = true
            running = true
        }
    }
    TimelineClock(
        running,
        active && inView,
        reduced,
        { delta -> timeTurns = (timeTurns + delta / 4.0) % 1.0 },
        { running = false },
    )
    val sample = model.sample(probe, timeTurns)
    fun chooseOffset(offset: Double) {
        autoplayConsumed = true
        running = false
        onChange { current -> current.copy(phaseTurns = offset, superpositionRevealed = false) }
    }
    Div(
        attrs = {
            elementId(OpticsWavesId.WAVES)
            opticsWavesStyles(OpticsWavesClass.INVESTIGATION)
        }
    ) {
        H2 { Text("Was ändern Höhe, Abstand und Zusatzweg?") }
        P {
            Text(
                "Zuerst zwei klassische Wellenformen vergleichen. Danach zwei gleichfrequente Beiträge am selben Ort und zur selben Zeit addieren."
            )
        }
        Div(attrs = { opticsWavesStyles(OpticsWavesClass.BLOCK) }) {
            H3 { Text("1 · Amplitude und Wellenlänge unterscheiden") }
            Div(
                attrs = {
                    opticsWavesStyles(OpticsWavesClass.FIGURE)
                    focusIndex(0)
                    role(AriaRole.GROUP)
                    ariaLabel(
                        "Wellenvergleich mit A- und Wellenlängenpfeilen; seitlich verschiebbar"
                    )
                }
            ) {
                ComparisonDiagram(state.waveAmplitude, state.waveWavelength)
            }
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.CONTROLS) }) {
                Div {
                    FieldLabel(OpticsWavesId.WAVE_AMPLITUDE) {
                        Text(
                            "Amplitude A · ${Physics.decimal(state.waveAmplitude, 0)} Schemaeinheiten"
                        )
                    }
                    Input(
                        InputType.Range,
                        attrs = {
                            elementId(OpticsWavesId.WAVE_AMPLITUDE)
                            minimum(10)
                            maximum(45)
                            increment(1)
                            value(state.waveAmplitude)
                            onInput { event ->
                                event.value
                                    ?.toDouble()
                                    ?.takeIf { it.isFinite() && it in 10.0..45.0 }
                                    ?.let {
                                        onChange { current -> current.copy(waveAmplitude = it) }
                                    }
                            }
                        },
                    )
                }
                Div {
                    FieldLabel(OpticsWavesId.WAVE_WAVELENGTH) {
                        Text(
                            "Obere Wellenlänge λ · ${Physics.decimal(state.waveWavelength, 0)} Schemaeinheiten"
                        )
                    }
                    Input(
                        InputType.Range,
                        attrs = {
                            elementId(OpticsWavesId.WAVE_WAVELENGTH)
                            minimum(60)
                            maximum(180)
                            increment(1)
                            value(state.waveWavelength)
                            onInput { event ->
                                event.value
                                    ?.toDouble()
                                    ?.takeIf { it.isFinite() && it in 60.0..180.0 }
                                    ?.let {
                                        onChange { current -> current.copy(waveWavelength = it) }
                                    }
                            }
                        },
                    )
                }
            }
            P(attrs = { opticsWavesStyles(OpticsWavesClass.NOTE) }) {
                Text(
                    "A ist die Höhe über der Ruhelage, λ der Abstand zweier benachbarter Wellenberge. Ändere nur A: Die Wellenberge bleiben an denselben Orten. Die untere Vergleichswelle besitzt 2λ; diese beiden verschieden langen Wellen werden hier nicht als fest gleichphasiges Paar addiert."
                )
            }
        }
        Div(attrs = { opticsWavesStyles(OpticsWavesClass.BLOCK) }) {
            H3 { Text("2 · Zwei gleiche Beiträge, ein Zusatzweg") }
            P {
                Text(
                    "Jetzt haben beide Beiträge dieselbe Amplitude A und dieselbe Wellenlänge λ. Der zweite legt gedanklich einen zusätzlichen Weg Δs zurück. Dadurch ist sein Signal gegenüber Beitrag 1 zeitlich verzögert. Phase meint seine Stelle im Schwingungszyklus."
                )
            }
            FieldLabel(OpticsWavesId.WAVE_OFFSET) {
                Text(
                    "Zusatzweg Δs/λ = ${Physics.decimal(state.phaseTurns, 2)} · zuerst vorhersagen, dann Summe prüfen"
                )
            }
            Input(
                InputType.Range,
                attrs = {
                    elementId(OpticsWavesId.WAVE_OFFSET)
                    minimum(0)
                    maximum(1)
                    increment(.05)
                    value(state.phaseTurns)
                    onInput { event ->
                        event.value
                            ?.toDouble()
                            ?.takeIf { it.isFinite() && it in 0.0..1.0 }
                            ?.let(::chooseOffset)
                    }
                },
            )
            Div(
                attrs = {
                    opticsWavesStyles(OpticsWavesClass.ACTIONS)
                    role(AriaRole.GROUP)
                    ariaLabel("Zusatzweg auswählen")
                }
            ) {
                WaveOffsetControl.entries.forEach { preset ->
                    Button(
                        attrs = {
                            elementId(preset)
                            styles(LabClass.SECONDARY)
                            if (abs(preset.turns - state.phaseTurns) < 1e-8)
                                opticsWavesStyles(OpticsWavesClass.SELECTED)
                            ariaPressed(abs(preset.turns - state.phaseTurns) < 1e-8)
                            onClick { chooseOffset(preset.turns) }
                        }
                    ) {
                        Text(preset.label)
                    }
                }
            }
            Div(
                attrs = {
                    opticsWavesStyles(OpticsWavesClass.FIGURE)
                    focusIndex(0)
                    role(AriaRole.GROUP)
                    ariaLabel(
                        "Beiträge und Summe am gemeinsamen Ort und zur gemeinsamen Zeit; seitlich verschiebbar"
                    )
                    elementId(MotionId.WAVE_EXPLORER)
                    motionInView(inView)
                    ref { element ->
                        val stop = observeMotionSurface(element) { inView = it }
                        onDispose { stop() }
                    }
                }
            ) {
                SuperpositionDiagram(model, timeTurns, probe, state.superpositionRevealed)
            }
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.LEGEND) }) {
                Span(attrs = { opticsWavesStyles(OpticsWavesClass.FIRST) }) { Text("━ Beitrag 1") }
                Span(attrs = { opticsWavesStyles(OpticsWavesClass.SECOND) }) { Text("┄ Beitrag 2") }
                Span(attrs = { opticsWavesStyles(OpticsWavesClass.SUM) }) {
                    Text("═ Summe · erst nach Freigabe")
                }
            }
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.CONTROLS) }) {
                Div {
                    FieldLabel(OpticsWavesId.WAVE_PROBE) {
                        Text(
                            "Gemeinsamer Beobachtungsort x · ${Physics.decimal(probe, 0)} Schemaeinheiten"
                        )
                    }
                    Input(
                        InputType.Range,
                        attrs = {
                            elementId(OpticsWavesId.WAVE_PROBE)
                            minimum(0)
                            maximum(560)
                            increment(5)
                            value(probe)
                            onInput { event ->
                                event.value
                                    ?.toDouble()
                                    ?.takeIf { it.isFinite() && it in 0.0..560.0 }
                                    ?.let { probe = it }
                            }
                        },
                    )
                }
                Div {
                    FieldLabel(OpticsWavesId.WAVE_TIME) {
                        Text("Gemeinsame Zeit t/T · ${Physics.decimal(timeTurns, 2)} Zyklen")
                    }
                    Input(
                        InputType.Range,
                        attrs = {
                            elementId(OpticsWavesId.WAVE_TIME)
                            minimum(0)
                            maximum(1)
                            increment(.025)
                            value(timeTurns)
                            onInput { event ->
                                event.value
                                    ?.toDouble()
                                    ?.takeIf { it.isFinite() && it in 0.0..1.0 }
                                    ?.let {
                                        running = false
                                        autoplayConsumed = true
                                        timeTurns = it
                                    }
                            }
                        },
                    )
                }
            }
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.ACTIONS) }) {
                Button(
                    attrs = {
                        elementId(OpticsWavesId.WAVE_PLAY)
                        styles(LabClass.SECONDARY)
                        ariaPressed(running)
                        if (reduced) disabled()
                        onClick {
                            autoplayConsumed = true
                            running = !running
                        }
                    }
                ) {
                    Text(if (running) "Bewegung pausieren" else "Gemeinsame Zeit abspielen")
                }
                Button(
                    attrs = {
                        elementId(OpticsWavesId.WAVE_STEP)
                        styles(LabClass.SECONDARY)
                        onClick {
                            running = false
                            autoplayConsumed = true
                            timeTurns = (timeTurns + .125) % 1.0
                        }
                    }
                ) {
                    Text("⅛ Zyklus weiter")
                }
            }
            P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
                Text(
                    if (reduced)
                        "Reduzierte Bewegung: automatische Wiedergabe aus. Zeitregler und Einzelschritte zeigen alle Zustände statisch."
                    else
                        "Bewegt sich beim ersten Sichtbarwerden automatisch; jederzeit pausierbar. Außerhalb des Bildschirms ruht die Zeit. Zeitregler und Einzelschritte übernehmen die Kontrolle. Die Zeitlupe hat keine reale Periodendauer; alle drei Zeilen zeigen denselben Ort. Deine Vorhersage gibt die Summe frei."
                )
            }
            FieldLabel(OpticsWavesId.WAVE_PREDICTION) {
                Text(
                    "Deine Vorhersage: Wie groß kann die Summe bei diesem Zusatzweg werden? Warum?"
                )
            }
            TextArea(
                value = state.superpositionPrediction,
                attrs = {
                    elementId(OpticsWavesId.WAVE_PREDICTION)
                    styles(LabClass.COURSE_NOTE)
                    textRows(3)
                    maxLength(1000)
                    onInput {
                        onChange { current ->
                            current.copy(superpositionPrediction = it.value.take(1000))
                        }
                    }
                },
            )
            P(attrs = { opticsWavesStyles(OpticsWavesClass.NOTE) }) {
                Text(
                    "Die Notiz bleibt beim Fallwechsel erhalten. Prüfe sie für den neuen Zusatzweg; die Summe wird beim Wechsel erneut verdeckt. Deine Erklärung wird gespeichert, hier jedoch nicht automatisch bewertet."
                )
            }
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.ACTIONS) }) {
                if (!state.superpositionRevealed) {
                    Button(
                        attrs = {
                            elementId(OpticsWavesId.WAVE_REVEAL)
                            styles(LabClass.PRIMARY)
                            if (state.superpositionPrediction.isBlank()) disabled()
                            onClick {
                                onChange { current -> current.copy(superpositionRevealed = true) }
                            }
                        }
                    ) {
                        Text("Vorhersage festhalten & Summe zeigen")
                    }
                } else
                    Button(
                        attrs = {
                            elementId(OpticsWavesId.WAVE_HIDE)
                            styles(LabClass.SECONDARY)
                            onClick {
                                onChange { current -> current.copy(superpositionRevealed = false) }
                            }
                        }
                    ) {
                        Text("Summe wieder verdecken")
                    }
            }
            if (state.superpositionRevealed) {
                Div(
                    attrs = {
                        elementId(OpticsWavesId.WAVE_RESULT)
                        opticsWavesStyles(OpticsWavesClass.FEEDBACK)
                        waveOffset(state.phaseTurns)
                        waveSample(
                            sample.first,
                            sample.second,
                            sample.sum,
                            model.resultantAmplitude,
                        )
                    }
                ) {
                    P {
                        Text(
                            "Am markierten Ort x = ${Physics.decimal(probe, 0)} und zur gemeinsamen Zeit t/T = ${Physics.decimal(timeTurns, 2)}:"
                        )
                    }
                    Div(attrs = { opticsWavesStyles(OpticsWavesClass.VALUES) }) {
                        P(attrs = { opticsWavesStyles(OpticsWavesClass.FIRST) }) {
                            Text("u₁ = ${Physics.decimal(sample.first, 2)}")
                        }
                        P(attrs = { opticsWavesStyles(OpticsWavesClass.SECOND) }) {
                            Text("u₂ = ${Physics.decimal(sample.second, 2)}")
                        }
                        P(attrs = { opticsWavesStyles(OpticsWavesClass.SUM) }) {
                            Text("u₁ + u₂ = ${Physics.decimal(sample.sum, 2)}")
                        }
                    }
                    P {
                        Text(
                            "Maximale Summenhöhe über einen ganzen Zyklus: AΣ = ${Physics.decimal(model.resultantAmplitude, 2)} Schemaeinheiten · ${amplitudeExplanation(state.phaseTurns)}"
                        )
                    }
                    P {
                        Text(
                            "Eine momentane Summe von null ist noch keine dauerhafte Auslöschung: Prüfe auch andere Zeiten. Vergleiche dann deine Begründung mit den beiden Einzelbeiträgen."
                        )
                    }
                }
            }
        }
        Details(attrs = { opticsWavesStyles(OpticsWavesClass.NOTE) }) {
            Summary() { Text("Textalternative und Grenze dieser klassischen Brücke") }
            P {
                Text(
                    "Oben stehen zwei räumliche Wellenformen mit gleicher Höhe A und den Wellenlängen λ und 2λ. Ihre A-Pfeile reichen von der Ruhelage zum Wellenberg; der λ-Pfeil verbindet benachbarte Wellenberge. Unten sind beide Beiträge gleichfrequent und gleich groß. Der Zusatzweg verschiebt Beitrag 2 relativ zu Beitrag 1. Alle Zeilen verwenden denselben Ort x, dieselbe Zeit t und dieselbe vertikale Einheit. Die dritte Zeile addiert die beiden momentanen Auslenkungen."
                )
            }
            P {
                Text(
                    "Vorzeichenkonvention: u₁ = A sin[2π(x/λ − t/T)]. Ein zusätzlicher Weg verzögert Beitrag 2 um τ/T = Δs/λ: u₂(x,t) = u₁(x,t − τ). Deshalb steht hier in der zweiten Sinusphase +2πΔs/λ. Beispielsweise gilt bei x = 0, t = 0 und Δs/λ = ¼: u₁ = 0 und u₂ = +A."
                )
            }
            P {
                Text(
                    "Klassisches harmonisches Lehrmodell mit gleichen, kohärenten Beiträgen. Schemaeinheiten sind keine Meter oder historischen Messwerte. Die Kurven sind keine Elektronenbahnen und setzen kein Elektronenmedium voraus. Bei Materiewellen übernimmt man hier zunächst nur die Idee von Überlagerung und Phasenunterschied; diese Zeichnung ist kein Beweis für de Broglies Hypothese und keine vollständige Quantentheorie."
                )
            }
        }
    }
}

private fun amplitudeExplanation(offset: Double): String =
    when {
        abs(offset) < 1e-8 || abs(offset - 1.0) < 1e-8 ->
            "Ganze Wellenlängen Zusatzweg: beide Beiträge haben denselben Zyklusstand; AΣ = 2A."
        abs(offset - .5) < 1e-8 ->
            "Eine halbe Wellenlänge Zusatzweg: die Beiträge sind zu jeder Zeit entgegengesetzt; AΣ = 0."
        else ->
            "Teilweise Verstärkung bzw. Abschwächung; hier gilt 0 < AΣ < 2A. Die maximale Höhe ist von der momentanen Auslenkung zu unterscheiden."
    }

private fun wavePath(wavelength: Double, amplitude: Double, center: Double): String =
    (0..289).joinToString(" ") { index ->
        val x = WAVE_LEFT + index * 2.0
        "${if (index == 0) "M" else "L"}$x ${center - amplitude * sin(2 * PI * (x - WAVE_LEFT) / wavelength)}"
    }

@Composable
private fun ComparisonDiagram(amplitude: Double, wavelength: Double) {
    InquirySvg(
        OpticsWavesId.WAVE_COMPARISON,
        650.0,
        310.0,
        "Zwei klassische Wellenformen gleicher Amplitude A. Oben λ, unten 2λ. Höhenpfeile kennzeichnen A; horizontale Pfeile jeweils den Abstand benachbarter Wellenberge.",
        OpticsWavesClass.WAVE_DIAGRAM,
    ) {
        listOf(wavelength, 2 * wavelength).forEachIndexed { row, period ->
            val center = 95.0 + row * 135.0
            val crest = WAVE_LEFT + period / 4.0
            val color = if (row == 0) OpticsWavesClass.FIRST else OpticsWavesClass.SECOND
            InquiryLine(WAVE_LEFT, center, WAVE_RIGHT, center, OpticsWavesClass.AXIS)
            InquiryPath(wavePath(period, amplitude, center), OpticsWavesClass.PATH, color)
            // Fixed ordinate endpoints: changing A cannot translate a crest along x.
            InquiryPath(
                "M$crest $center V${center - amplitude} M${crest - 4} ${center - 5} L$crest $center L${crest + 4} ${center - 5} M${crest - 4} ${center - amplitude + 5} L$crest ${center - amplitude} L${crest + 4} ${center - amplitude + 5}",
                OpticsWavesClass.MEASURE,
            )
            InquiryText(
                crest + 8,
                center - amplitude / 2 + 4,
                "A",
                TextAnchor.START,
                OpticsWavesClass.SMALL_TEXT,
            )
            val bracketY = center - 62.0
            InquiryPath(
                "M$crest $bracketY H${crest + period} M${crest + 6} ${bracketY - 4} L$crest $bracketY L${crest + 6} ${bracketY + 4} M${crest + period - 6} ${bracketY - 4} L${crest + period} $bracketY L${crest + period - 6} ${bracketY + 4}",
                OpticsWavesClass.MEASURE,
            )
            InquiryLine(crest, bracketY + 6, crest, center - amplitude - 3, OpticsWavesClass.GUIDE)
            InquiryLine(
                crest + period,
                bracketY + 6,
                crest + period,
                center - amplitude - 3,
                OpticsWavesClass.GUIDE,
            )
            InquiryText(
                crest + period / 2.0,
                bracketY - 9.0,
                if (row == 0) "λ" else "2λ",
                TextAnchor.MIDDLE,
            )
        }
        InquiryText(
            WAVE_RIGHT,
            300.0,
            "Ort x · gemeinsame Momentaufnahme",
            TextAnchor.END,
            OpticsWavesClass.SMALL_TEXT,
        )
    }
}

@Composable
private fun SuperpositionDiagram(
    model: ClassicalSuperposition,
    timeTurns: Double,
    probe: Double,
    revealed: Boolean,
) {
    InquirySvg(
        OpticsWavesId.WAVE_SUPERPOSITION,
        650.0,
        430.0,
        "Drei Zeilen mit gemeinsamer Ortsachse: Beitrag 1, gleichfrequenter Beitrag 2 mit Zusatzweg ${Physics.decimal(model.pathDifferenceWavelengths, 2)} Wellenlängen, ${if (revealed) "deren berechnete Summe" else "Summe noch verdeckt"}. Die senkrechte Linie kennzeichnet denselben Beobachtungsort.",
        OpticsWavesClass.WAVE_DIAGRAM,
    ) {
        val centers = listOf(73.0, 183.0, 331.0)
        val colors = listOf(OpticsWavesClass.FIRST, OpticsWavesClass.SECOND, OpticsWavesClass.SUM)
        val labels =
            listOf(
                "Beitrag 1",
                "Beitrag 2 · gleiche λ und A",
                if (revealed) "Summe u₁ + u₂" else "Summe noch verdeckt",
            )
        InquiryLine(WAVE_LEFT + probe, 28.0, WAVE_LEFT + probe, 420.0, OpticsWavesClass.GUIDE)
        centers.forEachIndexed { row, center ->
            InquiryText(
                WAVE_LEFT,
                if (row == 2) center - 97 else center - 53,
                labels[row],
                TextAnchor.START,
                OpticsWavesClass.SMALL_TEXT,
                colors[row],
            )
            InquiryLine(WAVE_LEFT, center, WAVE_RIGHT, center, OpticsWavesClass.AXIS)
            if (row < 2 || revealed) {
                val path =
                    (0..289).joinToString(" ") { index ->
                        val position = index * 2.0
                        val value = model.sample(position, timeTurns)
                        val y =
                            when (row) {
                                0 -> value.first
                                1 -> value.second
                                else -> value.sum
                            }
                        "${if (index == 0) "M" else "L"}${WAVE_LEFT + position} ${center - y}"
                    }
                InquiryPath(path, OpticsWavesClass.PATH, colors[row])
                val value = model.sample(probe, timeTurns)
                val displacement =
                    when (row) {
                        0 -> value.first
                        1 -> value.second
                        else -> value.sum
                    }
                InquiryLine(
                    WAVE_LEFT + probe,
                    center,
                    WAVE_LEFT + probe,
                    center - displacement,
                    OpticsWavesClass.MEASURE,
                )
                InquiryCircle(
                    WAVE_LEFT + probe,
                    center - displacement,
                    4.5,
                    OpticsWavesClass.SAMPLE,
                    colors[row],
                )
            }
        }
        InquiryText(
            WAVE_RIGHT,
            425.0,
            "Ort x · alle Höhen in denselben Schemaeinheiten",
            TextAnchor.END,
            OpticsWavesClass.SMALL_TEXT,
        )
    }
}
