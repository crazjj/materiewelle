package materiewelle.learning

import androidx.compose.runtime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import materiewelle.NativeSvg
import materiewelle.core.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.semantics.Details
import materiewelle.ui.semantics.Summary
import materiewelle.wavelengthChart
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private val mediaJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

@Composable
fun MediaExample(active: Boolean, voltage: Voltage) {
    val brief = remember(voltage) { videoBrief(voltage) }
    val timeline = remember(brief) { StoryboardTimeline(brief) }
    var seconds by remember(voltage) { mutableStateOf(0.0) }
    var running by remember(voltage) { mutableStateOf(false) }
    val reduced = rememberReducedMotion()
    LaunchedEffect(active) { if (!active) running = false }
    TimelineClock(
        running,
        active,
        reduced,
        { delta ->
            seconds = (seconds + delta).coerceAtMost(timeline.durationSeconds.toDouble())
            if (seconds >= timeline.durationSeconds) running = false
        },
        { running = false },
    )
    val position = timeline.at(seconds)
    val scene = position.sceneIndex
    val shownVoltage =
        if (scene == 2) voltage.volts * (1 + 3 * position.sceneProgress) else voltage.volts
    val wavelengthPm = Physics.electronWavelength(Voltage(shownVoltage)).picometers
    val json = remember(brief) { mediaJson.encodeToString(brief) }
    Section(
        attrs = {
            styles(LabClass.PANEL, LabClass.MEDIA_PANEL)
            elementId(Id.MEDIA_PANEL)
        }
    ) {
        Div(attrs = { styles(LabClass.SECTION_HEADING) }) {
            Div {
                P(attrs = { styles(LabClass.EYEBROW) }) {
                    Text("ANIMIERTE ERKLÄRUNG / DREI SZENEN")
                }
                H2 { Text("Vom Energieansatz zum Vergleich") }
            }
            Span(attrs = { styles(LabClass.STATUS_TAG) }) { Text("PROGRAMMIERTE CLIP-VORSCHAU") }
        }
        Div(attrs = { styles(LabClass.MEDIA_LAYOUT) }) {
            Div {
                Div(
                    attrs = {
                        styles(LabClass.STORYBOARD_VISUAL)
                        elementId(Id.MEDIA_PREVIEW)
                        data(LabData.SCENE, scene)
                        data(LabData.CLIP_SECONDS, seconds)
                        data(LabData.RUNNING, running)
                    }
                ) {
                    Span(attrs = { styles(LabClass.EYEBROW) }) {
                        Text("SZENE 0${scene + 1} / ${brief.scenes[scene].seconds} s GEPLANT")
                    }
                    Div(attrs = { styles(LabClass.EQUATION) }) {
                        Text(
                            listOf("e U = p² / (2 m)", "λ = h / √(2 m e U)", "4 U → 2 p → λ / 2")[
                                scene]
                        )
                    }
                    if (scene == 2) {
                        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                            Text(
                                "Wellenlänge λ / pm gegen Spannung U / V. Diagramm bei Bedarf horizontal verschieben."
                            )
                        }
                        Div(
                            attrs = {
                                styles(LabClass.MEDIA_CHART)
                                focusIndex(0)
                                ariaLabel(
                                    "Berechneter Wellenlängenvergleich; auf schmalen Bildschirmen horizontal scrollbar"
                                )
                            }
                        ) {
                            NativeSvg(wavelengthChart(shownVoltage, voltage.volts))
                        }
                    }
                    Div(
                        attrs = {
                            elementId(Id.MEDIA_RESULT)
                            styles(LabClass.CHART_READOUT)
                            data(LabData.VOLTAGE_V, shownVoltage)
                            data(LabData.WAVELENGTH_PM, wavelengthPm)
                        }
                    ) {
                        Span { Text("U ${Physics.decimal(shownVoltage, 1)} V") }
                        Span { Text("λ ${Physics.decimal(wavelengthPm, 4)} pm") }
                    }
                    P { Text(brief.scenes[scene].visual) }
                }
                FieldLabel(forId = Id.MEDIA_TIMELINE) {
                    Text(
                        "Clipzeit · ${Physics.decimal(seconds, 1)} / ${timeline.durationSeconds} s"
                    )
                }
                Input(
                    InputType.Range,
                    attrs = {
                        elementId(Id.MEDIA_TIMELINE)
                        minimum(0)
                        maximum(timeline.durationSeconds)
                        increment(.01)
                        value(seconds)
                        onInput { event ->
                            event.value
                                ?.toDouble()
                                ?.takeIf { it.isFinite() }
                                ?.let {
                                    seconds = timeline.at(it).seconds
                                    running = false
                                }
                        }
                    },
                )
                Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                    Button(
                        attrs = {
                            elementId(Id.MEDIA_PLAY)
                            styles(LabClass.PRIMARY)
                            ariaPressed(running)
                            if (reduced) disabled()
                            onClick {
                                if (seconds >= timeline.durationSeconds) seconds = 0.0
                                running = !running
                            }
                        }
                    ) {
                        Text(if (running) "Pause" else "Clip abspielen")
                    }
                    Button(
                        attrs = {
                            elementId(Id.MEDIA_RESET)
                            styles(LabClass.SECONDARY)
                            onClick {
                                seconds = 0.0
                                running = false
                            }
                        }
                    ) {
                        Text("Neu starten")
                    }
                    brief.scenes.forEachIndexed { index, _ ->
                        Button(
                            attrs = {
                                elementId(ElementId.MediaScene(UiIndex(index)))
                                styles(LabClass.SECONDARY)
                                ariaPressed(scene == index)
                                onClick {
                                    seconds = timeline.sceneStart(index).toDouble()
                                    running = false
                                }
                            }
                        ) {
                            Text("Szene ${index + 1}")
                        }
                    }
                }
                if (reduced)
                    P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
                        Text(
                            "Reduzierte Bewegung: Wähle Szenen und Clipzeit selbst; automatische Wiedergabe bleibt aus."
                        )
                    }
            }
            Div {
                P(attrs = { styles(LabClass.EYEBROW) }) { Text("UNTERTITEL / SPRECHERTEXT") }
                Div(
                    attrs = {
                        elementId(Id.MEDIA_CAPTION)
                        styles(LabClass.MODEL_LIMIT)
                    }
                ) {
                    P(
                        attrs = {
                            elementId(Id.MEDIA_SCRIPT)
                            ariaLive(if (running) LiveMode.OFF else LiveMode.POLITE)
                        }
                    ) {
                        Text(brief.scenes[scene].narration)
                    }
                }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Stumme Vorschau mit geplanter Zeit. Die Gerätestimme liest eine Szene separat und kann länger sprechen; sie ist keine synchronisierte Tonspur."
                    )
                }
                Details(attrs = { styles(LabClass.NOTES) }) {
                    Summary() { Text("Optionale Gerätestimme · begrenzte Qualität") }
                    SpeechExample(brief.scenes[scene].narration, active && !running)
                }
                ProducedAudio(active)
                Div(attrs = { styles(LabClass.WORKSHOP_ACTIONS) }) {
                    A(
                        href = "data:application/json;charset=utf-8,${encodeURIComponent(json)}",
                        attrs = {
                            elementId(Id.MEDIA_EXPORT)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewelle-video-brief.json")
                        },
                    ) {
                        Text("Produktionsbrief / JSON")
                    }
                    A(
                        href =
                            "data:text/vtt;charset=utf-8,${encodeURIComponent(timeline.webVtt())}",
                        attrs = {
                            elementId(Id.MEDIA_VTT_EXPORT)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewelle-untertitel.vtt")
                        },
                    ) {
                        Text("Untertitel / VTT")
                    }
                    A(
                        href =
                            "data:text/plain;charset=utf-8,${encodeURIComponent(timeline.transcript())}",
                        attrs = {
                            elementId(Id.MEDIA_TRANSCRIPT_EXPORT)
                            styles(LabClass.SECONDARY)
                            downloadFile("materiewelle-sprechertext.txt")
                        },
                    ) {
                        Text("Sprechertext / TXT")
                    }
                }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text(
                        "Die Clipzeit steuert Modellparameter, keine Elektronenflugzeit. VTT-Zeiten müssen für eine echte Tonaufnahme angepasst werden. Noch kein exportierter Film und keine KI-Generierung."
                    )
                }
            }
        }
        WorkshopNotes(
            "KI-Video, Avatar oder Fachanimation?",
            "Für Formeln, Skalen und Versuchsgeometrie bleibt die programmierte Fachgrafik maßgeblich. Eine vorab erzeugte TTS-Aufnahme kann mit den exportierten Szenen geschnitten werden. Generatives Video eignet sich für ergänzende Atmosphäre; ein Avatar kann den redigierten Text sprechen. Die bereitgestellten 6-/7-/7-Sekunden-Szenen sind ein Storyboard und müssen an die unterstützten Clipdauern und die tatsächliche Audiodauer angepasst werden. Anbieter werden hier nicht aufgerufen. Der Expertentutor bleibt im eigenen Chat.",
        )
    }
}

@JsName("encodeURIComponent") private external fun encodeURIComponent(text: String): String
