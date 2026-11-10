package materiewelle.learning

import androidx.compose.runtime.*
import materiewelle.NativeSvg
import materiewelle.core.Physics
import materiewelle.core.VoltageInput
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.wavelengthChart
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.*

enum class AnimationKind {
    DERIVATION,
    SWEEP,
}

@Composable
fun AnimationExample(active: Boolean, kind: AnimationKind, chooseKind: (AnimationKind) -> Unit) {
    var input by remember { mutableStateOf(VoltageInput.initial()) }
    var progress by remember { mutableStateOf(0.0) }
    var running by remember { mutableStateOf(false) }
    val reduced = rememberReducedMotion()
    TimelineClock(
        running,
        active,
        reduced,
        { delta ->
            progress = (progress + delta / 16).coerceAtMost(1.0)
            if (progress >= 1) running = false
        },
        { running = false },
    )
    val step = (progress * 4).toInt().coerceAtMost(3)
    var furthestStep by remember { mutableStateOf(0) }
    val revealed = maxOf(step, furthestStep)
    LaunchedEffect(step) { furthestStep = maxOf(furthestStep, step) }
    val reading =
        rememberDerivationReading(
            DerivationReadingScope.WORKSHOP,
            step,
            voltageDerivation.lastIndex,
            active = active && kind == AnimationKind.DERIVATION,
            next = {
                running = false
                progress = (revealed + 1) / 4.0
            },
            pause = { running = false },
        )
    val voltage = if (kind == AnimationKind.SWEEP) 150 + 450 * progress else input.lastValid.volts
    val pm = Physics.wavelengthNonRelativistic(voltage) * 1e12
    Div(attrs = { styles(LabClass.WORKSPACE, LabClass.WORKSHOP_WORKSPACE) }) {
        Section(
            attrs = {
                styles(LabClass.GRAPH_AREA)
                elementId(Id.ANIMATION_STAGE)
                data(LabData.STEP, step)
                data(LabData.PROGRESS, progress)
                data(LabData.RUNNING, running)
            }
        ) {
            Div(attrs = { styles(LabClass.PANEL_TOOLBAR) }) {
                H2 {
                    Text(
                        if (kind == AnimationKind.DERIVATION) "Vom Energieansatz zur Wellenlänge"
                        else "150 → 600 V: eine Vorhersage bewegen"
                    )
                }
                Span(attrs = { styles(LabClass.STATUS_TAG) }) { Text("MODELL") }
            }
            if (kind == AnimationKind.DERIVATION) {
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Jeder neue Schritt kommt darunter. Vorherige Gleichungen und ihre Begründungen bleiben sichtbar."
                    )
                }
                DerivationReadingControls(reading) {
                    Button(
                        attrs = {
                            elementId(Id.ANIMATION_PLAY)
                            styles(LabClass.SECONDARY, LabClass.COMPACT)
                            if (reduced || reading.mode == DerivationReadingMode.SCROLL) disabled()
                            ariaPressed(running)
                            onClick {
                                if (progress == 1.0) progress = 0.0
                                if (!running) reading.resume()
                                running = !running
                            }
                        }
                    ) {
                        Text(if (running) "Pause" else "Abspielen")
                    }
                    Button(
                        attrs = {
                            elementId(Id.ANIMATION_NEXT)
                            styles(LabClass.SECONDARY, LabClass.COMPACT)
                            if (step == voltageDerivation.lastIndex) disabled()
                            onClick {
                                running = false
                                progress = (step + 1) / 4.0
                            }
                        }
                    ) {
                        Text("Nächster Schritt")
                    }
                }
                DerivationSequence(voltageDerivation, revealed, reading, step)
                DerivationReadingFrontier(reading, revealed, voltageDerivation.lastIndex)
                P(
                    attrs = {
                        elementId(Id.ANIMATION_EXPLANATION)
                        styles(LabClass.MODEL_LIMIT)
                        ariaLive(LiveMode.POLITE)
                    }
                ) {
                    Text(
                        "${revealed + 1} von ${voltageDerivation.size} Schritten sichtbar · aktuelle Karte ${step + 1}: ${voltageDerivation[step].title}"
                    )
                }
                if (step == voltageDerivation.lastIndex)
                    P(attrs = { styles(LabClass.DERIVATION_PLAYFUL) }) {
                        Text("Die Wurzel bleibt. Deine Zwischenschritte auch.")
                    }
            } else NativeSvg(wavelengthChart(voltage, 150.0))
            Div(
                attrs = {
                    styles(LabClass.CHART_READOUT)
                    elementId(Id.ANIMATION_RESULT)
                    data(LabData.VOLTAGE_V, voltage)
                    data(LabData.WAVELENGTH_PM, pm)
                }
            ) {
                Span { Text("U ${Physics.decimal(voltage, 1)} V") }
                Span { Text("λ ${Physics.decimal(pm, 4)} pm") }
            }
            FieldLabel(forId = Id.ANIMATION_PROGRESS) {
                Text("Zeitleiste · ${Physics.decimal(progress * 100, 0)} %")
            }
            Input(
                InputType.Range,
                attrs = {
                    elementId(Id.ANIMATION_PROGRESS)
                    minimum(0)
                    maximum(1)
                    increment(0.001)
                    value(progress)
                    onInput {
                        it.value?.toDouble()?.let { p ->
                            progress = p
                            running = false
                        }
                    }
                },
            )
            Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                if (kind == AnimationKind.SWEEP)
                    Button(
                        attrs = {
                            elementId(Id.ANIMATION_PLAY)
                            styles(LabClass.PRIMARY)
                            if (
                                reduced ||
                                    kind == AnimationKind.DERIVATION &&
                                        reading.mode == DerivationReadingMode.SCROLL
                            )
                                disabled()
                            ariaPressed(running)
                            onClick {
                                if (progress == 1.0) progress = 0.0
                                if (!running) reading.resume()
                                running = !running
                            }
                        }
                    ) {
                        Text(if (running) "Pause" else "Abspielen")
                    }
                Button(
                    attrs = {
                        elementId(Id.ANIMATION_PREV)
                        styles(LabClass.SECONDARY)
                        onClick {
                            running = false
                            progress = ((step - 1).coerceAtLeast(0)) / 4.0
                        }
                    }
                ) {
                    Text("Schritt zurück")
                }
                if (kind == AnimationKind.SWEEP)
                    Button(
                        attrs = {
                            elementId(Id.ANIMATION_NEXT)
                            styles(LabClass.SECONDARY)
                            onClick {
                                running = false
                                progress = (step + 1) / 4.0
                            }
                        }
                    ) {
                        Text("Schritt weiter")
                    }
                Button(
                    attrs = {
                        elementId(Id.ANIMATION_RESET)
                        styles(LabClass.SECONDARY)
                        onClick {
                            progress = 0.0
                            furthestStep = 0
                            running = false
                        }
                    }
                ) {
                    Text("Zurücksetzen")
                }
            }
            P(
                attrs = {
                    styles(LabClass.SMALL, LabClass.MUTED)
                    elementId(Id.ANIMATION_MOTION_STATUS)
                }
            ) {
                Text(
                    if (reduced)
                        "Reduzierte Bewegung aktiv: Zeitleiste und Einzelschritte bleiben nutzbar."
                    else if (kind == AnimationKind.DERIVATION && reading.mode == DerivationReadingMode.SCROLL)
                        "Dein Scrolltempo bestimmt die Schritte. Zeitleiste und Schaltflächen bleiben nutzbar."
                    else "16 Sekunden · jederzeit pausieren oder die Zeitleiste direkt bewegen."
                )
            }
        }
        Aside(attrs = { styles(LabClass.CONTROLS) }) {
            P(attrs = { styles(LabClass.EYEBROW) }) { Text("BEISPIEL WÄHLEN") }
            Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                Button(
                    attrs = {
                        elementId(Id.ANIMATION_DERIVATION)
                        styles(LabClass.SECONDARY)
                        ariaPressed((kind == AnimationKind.DERIVATION))
                        onClick {
                            chooseKind(AnimationKind.DERIVATION)
                            progress = 0.0
                            furthestStep = 0
                            running = false
                        }
                    }
                ) {
                    Text("Herleitung")
                }
                Button(
                    attrs = {
                        elementId(Id.ANIMATION_SWEEP)
                        styles(LabClass.SECONDARY)
                        ariaPressed((kind == AnimationKind.SWEEP))
                        onClick {
                            chooseKind(AnimationKind.SWEEP)
                            progress = 0.0
                            furthestStep = 0
                            running = false
                        }
                    }
                ) {
                    Text("Diagramm")
                }
            }
            FieldLabel(forId = Id.ANIMATION_VOLTAGE) { Text("Spannung für die Herleitung / V") }
            Input(
                InputType.Text,
                attrs = {
                    elementId(Id.ANIMATION_VOLTAGE)
                    value(input.raw)
                    inputMode(LabInputMode.DECIMAL)
                    ariaInvalid((input.valid == null))
                    describedBy(Id.ANIMATION_INPUT_STATUS)
                    onInput { input = input.edit(it.value) }
                },
            )
            P(
                attrs = {
                    elementId(Id.ANIMATION_INPUT_STATUS)
                    if (input.valid == null) styles(LabClass.ERROR)
                    else styles(LabClass.SMALL, LabClass.MUTED)
                    if (input.valid == null) role(AriaRole.ALERT)
                }
            ) {
                Text(
                    if (input.valid == null)
                        "Bitte 20 bis 1 000 V eingeben. Das Modell behält den letzten gültigen Wert."
                    else "20–1 000 V · der Diagrammlauf vergleicht fest 150 mit 600 V."
                )
            }
            H3 { Text("Erst vorhersagen") }
            P {
                Text(
                    "Wenn U vervierfacht wird: Wie verändern sich Energie, Impuls und Wellenlänge?"
                )
            }
            WorkshopNotes(
                "Annahmen und Quellen",
                "De Broglie: λ = h/p. Nicht relativistische Energie-Impuls-Beziehung, Elektronenstartenergie vernachlässigt. Rechenwerte und SI-Konstanten stammen aus dem gemeinsamen Physikkern (CODATA 2022). Animation ist eine Modellrechnung; keine historischen Messwerte.",
            )
        }
    }
}
