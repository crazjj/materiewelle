package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlin.math.PI
import kotlin.math.cos
import materiewelle.core.Physics
import materiewelle.core.course.InquiryState
import materiewelle.core.course.OpticsStage
import materiewelle.data.LabAsset
import materiewelle.learning.rememberReducedMotion
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private val OpticsStage.title: String
    get() =
        when (this) {
            OpticsStage.SOURCE -> "Quelle & Kondensor"
            OpticsStage.SLITS -> "Zwei Öffnungen"
            OpticsStage.OPTICS -> "Elektronenoptik"
            OpticsStage.REPRODUCTION -> "Reproduktion"
        }
private val OpticsStage.explanation: String
    get() =
        when (this) {
            OpticsStage.SOURCE ->
                "Die Quelle liefert Elektronen. Beschleunigung legt ihre Energie fest; Kondensoroptik bereitet die Beleuchtung der Spalte vor. Für deutliche Interferenz brauchen die Beiträge eine hinreichend feste Phasenbeziehung. Die Zeichnung zeigt Funktionen, keine gemessenen Einzelbahnen."
            OpticsStage.SLITS ->
                "Die zwei hellen Lücken im dunklen Träger sind die tatsächlichen Öffnungen. b bezeichnet die Breite eines Spalts; g den Abstand ihrer Mitten. Beiträge beider Öffnungen überlagern sich. g ist deshalb nicht der freie Abstand zwischen den beiden Innenkanten."
            OpticsStage.OPTICS ->
                "Die schematische Linse ordnet einer Ausbreitungsrichtung eine Stelle in der Beobachtungsebene zu: Gleich gerichtete Beiträge beider Spalte treffen dort zusammen. Die Richtungskonstruktion zeigt dieses Prinzip. Sie berechnet weder Linsenfelder noch historische Brennweiten oder einzelne Elektronenbahnen."
            OpticsStage.REPRODUCTION ->
                "Nach der Musterbildung folgen Beobachtung, weitere Vergrößerung und schließlich die digitalisierte Reproduktion. Die Ansichtsvergrößerung unten verändert nur Bildschirmgröße und Ausschnitt. Sie liefert keinen unabhängigen Meter-pro-Pixel-Maßstab der ursprünglichen Beobachtungsebene."
        }

/** Functional optical diagram; all lengths in this drawing are explicitly schematic. */
@Composable
fun OpticsView(
    state: InquiryState,
    onChange: ((InquiryState) -> InquiryState) -> Unit,
    active: Boolean = true,
) {
    val reduced = rememberReducedMotion()
    var transitions by remember { mutableStateOf(true) }
    val revealed = state.opticsRevealed
    Div(
        attrs = {
            elementId(OpticsWavesId.OPTICS)
            opticsWavesStyles(OpticsWavesClass.INVESTIGATION)
            if (!transitions || reduced || !active) opticsWavesStyles(OpticsWavesClass.STILL)
            opticsStage(state.opticsStage)
        }
    ) {
        H2 { Text("Wie wird ein sehr feines Muster zugänglich?") }
        P {
            Text(
                "Verfolge zuerst die Funktionskette. Die Zeichnung ist ein Lehrschema, kein maßstäblicher Nachbau von Jönssons Apparatur."
            )
        }
        Div(
            attrs = {
                opticsWavesStyles(OpticsWavesClass.FIGURE)
                focusIndex(0)
                role(AriaRole.GROUP)
                ariaLabel("Jönsson-Funktionsschema; auf schmalen Ansichten seitlich verschiebbar")
            }
        ) {
            OpticsDiagram(state.opticsStage)
        }
        P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
            Text(
                "Richtungskonstruktion und schematisches Muster · keine beobachteten Teilchenbahnen oder historischen Intensitätswerte. Auf kleinen Bildschirmen ist das Schema seitlich verschiebbar."
            )
        }
        if (!revealed) {
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.PANEL) }) {
                H3 { Text("Deine erste Zuordnung") }
                P {
                    Text(
                        "Welche Funktion ordnet den Ausbreitungsrichtungen nach den Spalten Stellen in der Beobachtungsebene zu?"
                    )
                }
                Div(
                    attrs = {
                        opticsWavesStyles(OpticsWavesClass.STAGES)
                        role(AriaRole.GROUP)
                        ariaLabel("Eigene Funktionszuordnung")
                    }
                ) {
                    OpticsPredictionControl.entries.forEach { choice ->
                        Button(
                            attrs = {
                                elementId(choice)
                                styles(LabClass.SECONDARY)
                                opticsWavesStyles(OpticsWavesClass.CHOICE)
                                if (state.opticsPrediction == choice.stage)
                                    opticsWavesStyles(OpticsWavesClass.SELECTED)
                                ariaPressed(state.opticsPrediction == choice.stage)
                                onClick {
                                    onChange { current ->
                                        current.copy(opticsPrediction = choice.stage)
                                    }
                                }
                            }
                        ) {
                            Text(choice.stage.title)
                        }
                    }
                }
                Div(attrs = { opticsWavesStyles(OpticsWavesClass.ACTIONS) }) {
                    Button(
                        attrs = {
                            elementId(OpticsWavesId.OPTICS_CHECK)
                            styles(LabClass.PRIMARY)
                            if (state.opticsPrediction == null) disabled()
                            onClick {
                                onChange { current ->
                                    current.copy(
                                        opticsRevealed = true,
                                        opticsStage = OpticsStage.OPTICS,
                                    )
                                }
                            }
                        }
                    ) {
                        Text("Zuordnung prüfen")
                    }
                    Button(
                        attrs = {
                            elementId(OpticsWavesId.OPTICS_EXPLORE)
                            styles(LabClass.SECONDARY)
                            onClick { onChange { current -> current.copy(opticsRevealed = true) } }
                        }
                    ) {
                        Text("Direkt erkunden · ohne eigene Zuordnung")
                    }
                }
            }
        } else {
            P(
                attrs = {
                    elementId(OpticsWavesId.OPTICS_FEEDBACK)
                    opticsWavesStyles(OpticsWavesClass.FEEDBACK)
                    ariaLive(LiveMode.POLITE)
                }
            ) {
                Text(
                    when (state.opticsPrediction) {
                        null ->
                            "Du erkundest ohne geprüfte eigene Zuordnung. Die Elektronenoptik übernimmt in diesem Schema die Zuordnung von Richtung zu Beobachtungsstelle."
                        OpticsStage.OPTICS ->
                            "Deine Zuordnung passt zum Schema: Die Elektronenoptik führt gleich gerichtete Beiträge an einer Beobachtungsstelle zusammen. Das ist eine Funktionszuordnung, noch keine vollständige Erklärung des Experiments."
                        else ->
                            "Deine erste Zuordnung bleibt gespeichert: ${state.opticsPrediction?.title}. Hier übernimmt die Elektronenoptik die Zuordnung von Richtung zu Beobachtungsstelle. Vergleiche ihre Aufgabe mit der deiner Auswahl."
                    }
                )
            }
        }
        Div(
            attrs = {
                opticsWavesStyles(OpticsWavesClass.STAGES)
                role(AriaRole.GROUP)
                ariaLabel("Funktion im Schema hervorheben")
            }
        ) {
            OpticsStageControl.entries.forEach { choice ->
                Button(
                    attrs = {
                        elementId(choice)
                        styles(LabClass.SECONDARY)
                        opticsWavesStyles(OpticsWavesClass.CHOICE)
                        if (state.opticsStage == choice.stage)
                            opticsWavesStyles(OpticsWavesClass.SELECTED)
                        ariaPressed(state.opticsStage == choice.stage)
                        onClick { onChange { current -> current.copy(opticsStage = choice.stage) } }
                    }
                ) {
                    Text(choice.stage.title)
                }
            }
        }
        if (revealed) {
            Div(
                attrs = {
                    opticsWavesStyles(OpticsWavesClass.PANEL)
                    ariaLive(LiveMode.POLITE)
                }
            ) {
                H3 { Text(state.opticsStage.title) }
                P { Text(state.opticsStage.explanation) }
            }
        } else
            P(attrs = { opticsWavesStyles(OpticsWavesClass.NOTE) }) {
                Text(
                    "Die Auswahl hebt den Gegenstand im Bild hervor. Die ausführliche Erklärung öffnest du nach deiner Zuordnung oder über „Direkt erkunden“."
                )
            }
        if (state.opticsStage == OpticsStage.REPRODUCTION && revealed) {
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.BLOCK) }) {
                H3 { Text("Gleiches Raster, größere Ansicht") }
                FieldLabel(OpticsWavesId.OPTICS_ZOOM) {
                    Text(
                        "Ansichtsvergrößerung: ${Physics.decimal(state.reproductionScale, 1)}× · kein physikalischer Maßstab"
                    )
                }
                Input(
                    InputType.Range,
                    attrs = {
                        elementId(OpticsWavesId.OPTICS_ZOOM)
                        minimum(1)
                        maximum(3)
                        increment(.25)
                        value(state.reproductionScale)
                        onInput { event ->
                            event.value
                                ?.toDouble()
                                ?.takeIf { it.isFinite() && it in 1.0..3.0 }
                                ?.let {
                                    onChange { current -> current.copy(reproductionScale = it) }
                                }
                        }
                    },
                )
                Div(
                    attrs = {
                        elementId(OpticsWavesId.OPTICS_REPRODUCTION)
                        opticsWavesStyles(OpticsWavesClass.REPRODUCTION_WINDOW)
                        reproductionScale(state.reproductionScale)
                        focusIndex(0)
                        ariaLabel(
                            "Historische Rasterreproduktion; vergrößerte Ansicht kann seitlich verschoben werden"
                        )
                    }
                ) {
                    Img(
                        src = LabAsset.ORIGINAL_IMAGE.path,
                        alt =
                            "J1: digitalisierte Reproduktion, links Einzelspaltaufnahme, rechts Doppelspaltstreifen. Vergrößerung der Ansicht, keine absolute Kalibrierung.",
                        attrs = { opticsWavesStyles(OpticsWavesClass.REPRODUCTION_IMAGE) },
                    )
                }
                P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
                    Text(
                        "Historischer Bildbeleg J1, Übersetzung 1974, Abb. 7/8. Der Regler ändert das geladene Raster nicht. Native Pixelabstände bleiben gleich; zum Messen öffne die eigene Bildmessung in Station 14."
                    )
                }
            }
        }
        Button(
            attrs = {
                elementId(OpticsWavesId.OPTICS_MOTION)
                styles(LabClass.SECONDARY)
                ariaPressed(transitions && !reduced)
                if (reduced) disabled()
                onClick { transitions = !transitions }
            }
        ) {
            Text(
                if (reduced) "Reduzierte Bewegung · statische Ansicht"
                else if (transitions) "Übergänge ausschalten" else "Übergänge einschalten"
            )
        }
        Details(attrs = { opticsWavesStyles(OpticsWavesClass.NOTE) }) {
            Summary() { Text("Textalternative, Quellenstatus und Grenzen") }
            P {
                Text(
                    "Von links nach rechts: Quelle und Kondensor, Träger mit zwei Öffnungen, schematische Elektronenlinse, Beobachtungsebene, spätere Reproduktion. b steht an einer einzelnen Öffnung; g reicht von einer Spaltmitte zur anderen. Parallel ausgehende Beiträge beider Spalte werden im Funktionsschema an derselben Stelle der Beobachtungsebene zusammengeführt."
                )
            }
            P {
                Text(
                    "J1 beschreibt Elektronenoptik, eine Beobachtungsebene bei 350 mm und weitere Vergrößerung. Diese Angabe allein ist kein Foto- oder Scanmaßstab. J1 nennt im Text 50 kV, Abb. 6 ist mit 40 kV beschriftet. Für die verwendete einzelne Aufnahme ist keiner dieser Spannungsfälle sicher zugeordnet. Die schematischen Linsenabstände, Strichabstände und Strichhelligkeiten sind keine historischen Messdaten."
                )
            }
        }
    }
}

@Composable
internal fun OpticsDiagram(stage: OpticsStage) {
    InquirySvg(
        OpticsWavesId.OPTICS_DIAGRAM,
        900.0,
        535.0,
        "Funktionsschema: Quelle und Kondensor, zwei Spalte mit Breite b und Mittenabstand g, Elektronenoptik, Beobachtungsebene und getrennte Reproduktion. Hervorgehoben: ${stage.title}.",
        OpticsWavesClass.OPTICS_DIAGRAM,
    ) {
        InquiryLine(28.0, 180.0, 735.0, 180.0, OpticsWavesClass.AXIS)
        InquiryGroup(stage == OpticsStage.SOURCE) {
            InquiryRect(30.0, 153.0, 90.0, 54.0, OpticsWavesClass.SURFACE)
            InquiryText(75.0, 186.0, "Quelle", TextAnchor.MIDDLE)
            InquiryPath(
                "M170 110 Q200 180 170 250 M180 110 Q150 180 180 250",
                OpticsWavesClass.PATH,
            )
            InquiryPath(
                "M122 166 L278 151 M122 194 L278 209 M122 180 H278",
                OpticsWavesClass.DIRECTION,
            )
            InquiryText(175.0, 286.0, "Kondensor", TextAnchor.MIDDLE)
        }
        InquiryGroup(stage == OpticsStage.SLITS) {
            InquiryRect(291.0, 80.0, 18.0, 71.0, OpticsWavesClass.SLIT)
            InquiryRect(291.0, 161.0, 18.0, 38.0, OpticsWavesClass.SLIT)
            InquiryRect(291.0, 209.0, 18.0, 71.0, OpticsWavesClass.SLIT)
            InquiryLine(279.0, 156.0, 321.0, 156.0, OpticsWavesClass.GUIDE)
            InquiryLine(279.0, 204.0, 321.0, 204.0, OpticsWavesClass.GUIDE)
            InquiryText(300.0, 42.0, "Zwei Öffnungen", TextAnchor.MIDDLE)
        }
        InquiryGroup(stage == OpticsStage.OPTICS) {
            // Same slope before the lens, same point after the lens: qualitative angle-to-position
            // mapping.
            for (direction in listOf(-.24, 0.0, .24)) {
                val targetY = 180.0 + direction / .24 * 50.0
                for (slitY in listOf(156.0, 204.0)) {
                    val lensY = slitY + direction * 200.0
                    InquiryPath("M310 $slitY L500 $lensY L700 $targetY", OpticsWavesClass.DIRECTION)
                }
                InquiryCircle(700.0, targetY, 3.5, OpticsWavesClass.SAMPLE)
            }
            InquiryPath(
                "M500 74 C470 124 470 236 500 286 C530 236 530 124 500 74 Z",
                OpticsWavesClass.LIGHT,
            )
            InquiryText(500.0, 42.0, "Elektronenoptik", TextAnchor.MIDDLE)
            InquiryText(
                500.0,
                310.0,
                "Richtung → Bildposition",
                TextAnchor.MIDDLE,
                OpticsWavesClass.SMALL_TEXT,
            )
            InquiryRect(698.0, 80.0, 20.0, 200.0, OpticsWavesClass.SURFACE)
            for (i in 0..99) {
                // Deliberately synthetic periodic contrast, no historical intensity data.
                val y = 81.0 + i * 2
                val relative = cos(PI * (y - 180.0) / 25.0).let { it * it }
                if (relative > .3) InquiryLine(701.0, y, 715.0, y, OpticsWavesClass.PATTERN)
            }
            InquiryText(706.0, 42.0, "Beobachtung", TextAnchor.MIDDLE)
            InquiryText(706.0, 64.0, "(Muster)", TextAnchor.MIDDLE, OpticsWavesClass.SMALL_TEXT)
        }
        InquiryGroup(stage == OpticsStage.REPRODUCTION) {
            InquiryLine(756.0, 80.0, 756.0, 280.0, OpticsWavesClass.GUIDE)
            InquiryPath("M726 180 H788 M777 173 L788 180 L777 187", OpticsWavesClass.PATH)
            InquiryRect(800.0, 138.0, 72.0, 84.0, OpticsWavesClass.SURFACE)
            for (y in listOf(154.0, 168.0, 182.0, 196.0, 210.0)) InquiryLine(
                811.0,
                y,
                861.0,
                y,
                OpticsWavesClass.PATTERN,
            )
            InquiryText(
                835.0,
                263.0,
                "Reproduktion",
                TextAnchor.MIDDLE,
                OpticsWavesClass.SMALL_TEXT,
            )
            InquiryText(835.0, 285.0, "separater", TextAnchor.MIDDLE, OpticsWavesClass.SMALL_TEXT)
            InquiryText(835.0, 307.0, "Bildmaßstab", TextAnchor.MIDDLE, OpticsWavesClass.SMALL_TEXT)
        }
        // Dimensions have their own enlarged front view, away from the direction construction.
        // They remain legible in every stage; this inset is qualitative, never a scale for data.
        InquiryLine(30.0, 335.0, 870.0, 335.0, OpticsWavesClass.GUIDE)
        InquiryText(30.0, 365.0, "Spaltblende im Detail", TextAnchor.START)
        InquiryText(30.0, 389.0, "Vorderansicht · vergrößert", TextAnchor.START, OpticsWavesClass.SMALL_TEXT)
        InquiryRect(325.0, 365.0, 220.0, 120.0, OpticsWavesClass.SURFACE)
        InquiryRect(365.0, 380.0, 28.0, 75.0, OpticsWavesClass.LIGHT)
        InquiryRect(477.0, 380.0, 28.0, 75.0, OpticsWavesClass.LIGHT)
        InquiryLine(379.0, 350.0, 379.0, 478.0, OpticsWavesClass.GUIDE)
        InquiryLine(491.0, 350.0, 491.0, 478.0, OpticsWavesClass.GUIDE)
        InquiryPath("M365 454 V476 M393 454 V476 M365 468 H393", OpticsWavesClass.MEASURE)
        InquiryText(379.0, 499.0, "b", TextAnchor.MIDDLE)
        InquiryPath("M379 344 V360 M491 344 V360 M379 352 H491", OpticsWavesClass.MEASURE)
        InquiryText(435.0, 376.0, "g", TextAnchor.MIDDLE)
        InquiryText(585.0, 401.0, "b: Breite einer Öffnung", TextAnchor.START)
        InquiryText(585.0, 435.0, "g: Abstand der Spaltmitten", TextAnchor.START)
        InquiryText(
            30.0,
            523.0,
            "Funktionen und Richtungen schematisch · keine historischen Abstände oder Elektronenbahnen",
            TextAnchor.START,
            OpticsWavesClass.SMALL_TEXT,
        )
    }
}
