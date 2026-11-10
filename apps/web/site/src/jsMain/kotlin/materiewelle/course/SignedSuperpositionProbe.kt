package materiewelle.course

import androidx.compose.runtime.*
import kotlin.math.abs
import materiewelle.core.ClassicalSuperposition
import materiewelle.pages.number
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** A tiny optional action at one common point/time; not an electron medium or intensity sum. */
@Composable
fun SignedSuperpositionProbe() {
    var later by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    val sample = ClassicalSuperposition(2.0, 8.0, .25).sample(2.5, if (later) .25 else .0)
    Details(attrs = { elementId(LearningHelpId.SUM_PROBE); styles(LabClass.NOTES) }) {
        Summary { Text("Kleine Teilprobe: Auslenkungen mit Vorzeichen addieren") }
        P { Text("Beide Beiträge gelten am selben Ort und zur selben Zeit. Die Mittellage ist null; oberhalb positiv, unterhalb negativ. Die Zeichenwerte sind frei gewählt, keine Elektronenmessung.") }
        ActivitySvg(LearningHelpId.SUM_PLOT, 400.0, 210.0, "Zwei klassische Auslenkungen am selben Ort und Zeitpunkt: erster Beitrag ${number(sample.first)}, zweiter Beitrag ${number(sample.second)}. Ihre Summe wird vor der Prüfung nicht gezeigt.") {
            ActivityLine(25.0, 105.0, 370.0, 105.0, StationActivityClass.DRAWING_LINE)
            listOf(sample.first, sample.second).forEachIndexed { index, value ->
                val top = 105.0 - value.coerceAtLeast(0.0) * 30.0
                ActivityRect(85.0 + index * 150.0, top, 38.0, abs(value) * 30.0, StationActivityClass.DRAWING_ACCENT)
                ActivityText(104.0 + index * 150.0, 184.0, "u${if (index == 0) "₁" else "₂"} = ${number(value)}", TextAnchor.MIDDLE)
            }
            ActivityText(27.0, 97.0, "0")
        }
        FieldLabel(LearningHelpId.SUM_ANSWER) { Text("Welche Summe ergibt sich? · auf zwei Stellen gerundet") }
        Input(InputType.Text, attrs = {
            elementId(LearningHelpId.SUM_ANSWER); value(answer); maxLength(40)
            onInput { answer = it.value.take(40); checked = false }
        })
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            Button(attrs = { elementId(LearningHelpId.SUM_CHECK); styles(LabClass.SECONDARY); onClick { checked = true } }) { Text("Addition prüfen") }
            Button(attrs = {
                elementId(LearningHelpId.SUM_TIME); styles(LabClass.SECONDARY)
                onClick { later = !later; answer = ""; checked = false }
            }) { Text(if (later) "Zur ersten Zeit" else "Andere Zeit · gleicher Ort") }
        }
        if (checked) P(attrs = { elementId(LearningHelpId.SUM_FEEDBACK); role(AriaRole.STATUS) }) {
            val value = answer.replace(',', '.').toDoubleOrNull()
            Text(if (value != null && value.isFinite() && abs(value - sample.sum) <= .03)
                "Die Addition passt zu den gerundeten Beiträgen. Erkläre die Vorzeichen und prüfe dann denselben Ort zur anderen Zeit."
                else "Addiere die beiden angezeigten Werte mit ihren Vorzeichen. Negativ bedeutet hier Auslenkung unterhalb der Mittellage, keine negative Intensität.")
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text("Freiwillige ungespeicherte Teilprobe. Kehre danach zur Wellenzeichnung zurück und untersuche den Wegversatz; diese Balken sind keine Intensitäten.") }
    }
}
