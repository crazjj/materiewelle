package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.core.course.OpticsStage
import materiewelle.course.graphics.OpticsDiagram
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** One functional stage, not a calibrated reconstruction of the historical instrument. */
private enum class JonssonSetupStep(
    val stage: OpticsStage,
    val title: String,
    val explanation: String,
) {
    SOURCE(OpticsStage.SOURCE, "Elektronenquelle und Kondensor",
        "Die Quelle liefert Elektronen; die Beschleunigungsspannung bestimmt ihre kinetische Energie. Der Kondensor bündelt und richtet die Beleuchtung auf beide Spalte. Eine ausreichend kleine Winkelstreuung hilft, dass sich die Streifen nicht gegenseitig verwischen. Die Skizze fasst mehrere Bauteile nach ihrer Funktion zusammen."),
    SLITS(OpticsStage.SLITS, "Zwei sehr feine Spalte",
        "Die Herstellung hinreichend feiner, paralleler und durchlässiger Spalte ist die technische Herausforderung. Nominaler Parameterfall: Breite b = 0,5 µm und Mittenabstand g = 2,0 µm. Die Spaltbreite prägt die Beugungshülle, der Abstand die feinen Interferenzstreifen."),
    OPTICS(OpticsStage.OPTICS, "Elektronenoptische Abbildung",
        "Elektronenlinsen bestehen nicht aus Glas: räumlich geformte elektrische oder magnetische Felder lenken die geladenen Elektronen ab. In einer geeigneten Beugungsebene werden gleiche Ausbreitungsrichtungen auf gleiche Bildstellen abgebildet. Weitere Linsen vergrößern die feine Verteilung. Die Linien zeigen diese Richtungskonstruktion, keine gemessenen Einzelbahnen."),
    IMAGE(OpticsStage.REPRODUCTION, "Beobachtung und Reproduktion",
        "Hier siehst du eine digitale Reproduktion der historischen Aufnahme. Wir messen in den Pixeln dieser Bilddatei. Welcher Strecke in der ursprünglichen Beobachtungsebene ein Pixel entspricht, ist für diese Reproduktion nicht unabhängig belegt."),
}

@Composable
fun JonssonOptics(viewState: JonssonUiState = remember { JonssonUiState() }, showHeading: Boolean = true) {
    var selectedIndex by viewState.selectedOpticsStage
    val steps = JonssonSetupStep.entries
    val current = steps[selectedIndex.coerceIn(steps.indices)]
    var explanationRequest by remember { mutableIntStateOf(0) }
    DisposableEffect(explanationRequest) {
        val cancel = if (explanationRequest > 0) requestTutorFocus(
            OpticsWavesId.SETUP_EXPLANATION_TITLE,
            panel = OpticsWavesId.SETUP_EXPLANATION,
        ) else ({})
        onDispose { cancel() }
    }
    if (showHeading) H2 { MathText("Wie wird ein so feines Muster sichtbar?") }
    Div(attrs = {
        elementId(OpticsWavesId.SETUP)
        opticsWavesStyles(OpticsWavesClass.SETUP)
        opticsStage(current.stage)
    }) {
        Div(attrs = { opticsWavesStyles(OpticsWavesClass.SETUP_STEPS); role(AriaRole.GROUP); ariaLabel("Aufbauschritt direkt wählen") }) {
            steps.forEach { step ->
                Button(attrs = {
                    styles(LabClass.SECONDARY)
                    ariaPressed(current == step)
                    onClick { selectedIndex = step.ordinal }
                }) { MathText("${step.ordinal + 1} · ${step.title}") }
            }
        }
        Div(attrs = {
            opticsWavesStyles(OpticsWavesClass.FIGURE)
            focusIndex(0)
            role(AriaRole.GROUP)
            ariaLabel("Jönsson-Funktionsschema; bei Bedarf seitlich verschiebbar")
        }) { OpticsDiagram(current.stage) }
        // Controls precede variable explanation text, so advancing never moves its own target.
        Div(attrs = { opticsWavesStyles(OpticsWavesClass.SETUP_CONTROLS) }) {
            Div(attrs = { opticsWavesStyles(OpticsWavesClass.SETUP_READING) }) {
                P(attrs = { elementId(OpticsWavesId.SETUP_PROGRESS); role(AriaRole.STATUS) }) {
                    Text("Aufbau · Schritt ${current.ordinal + 1} von ${steps.size}")
                }
                Button(attrs = {
                    elementId(OpticsWavesId.SETUP_READ_EXPLANATION)
                    styles(LabClass.SECONDARY)
                    controls(OpticsWavesId.SETUP_EXPLANATION)
                    onClick { explanationRequest += 1 }
                }) { Text("Erklärung lesen") }
            }
            Button(attrs = {
                elementId(OpticsWavesId.SETUP_PREVIOUS)
                styles(LabClass.SECONDARY)
                if (current == steps.first()) disabled()
                onClick { selectedIndex = (current.ordinal - 1).coerceAtLeast(0) }
            }) { Text("Zurück") }
            Button(attrs = {
                elementId(OpticsWavesId.SETUP_NEXT)
                styles(LabClass.PRIMARY)
                if (current == steps.last()) disabled()
                onClick { selectedIndex = (current.ordinal + 1).coerceAtMost(steps.lastIndex) }
            }) { Text("Weiter") }
        }
        Div(attrs = { elementId(OpticsWavesId.SETUP_EXPLANATION); opticsWavesStyles(OpticsWavesClass.SETUP_EXPLANATION); ariaLive(LiveMode.POLITE) }) {
            H3(attrs = { elementId(OpticsWavesId.SETUP_EXPLANATION_TITLE); focusIndex(-1) }) { Text(current.title) }
            P { MathText(current.explanation) }
        }
    }
    Details {
        Summary { MathText("Was verändert eine Elektronenlinse – und was nicht?") }
        P { MathText("Wie eine Glaslinse bei Licht kann eine Elektronenlinse bündeln oder abbilden. Sie wirkt aber über Kräfte auf elektrische Ladungen. Elektrische Felder können Richtung und kinetische Energie verändern; ein rein magnetisches Feld lenkt ab, ohne Arbeit am Elektron zu verrichten. Das ist eine allgemeine Erklärung der Linsenwirkung, keine Festlegung auf die Feldgeometrie jedes historischen Bauteils.") }
        P { MathText("Der Kondensor bereitet die Beleuchtung vor. Das Zusammenspiel beider Spalte erzeugt die Interferenzstruktur; die nachfolgende Optik macht sie räumlich auflösbar. Mehr Vergrößerung kann größere Streifenabstände im Foto ergeben, ohne eine andere Elektronenwellenlänge zu bedeuten. Deshalb muss man Abbildungsmaßstab und Wellenlänge auseinanderhalten.") }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText("Funktionsskizze und Richtungskonstruktion nach J1: kein maßstäblicher Nachbau, keine gemessenen Einzelbahnen. Die genaue Abbildungsgeometrie und Vergrößerung dieser Reproduktion sind nicht eindeutig gesichert.")
    }
}
