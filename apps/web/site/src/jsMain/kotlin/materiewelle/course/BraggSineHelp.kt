package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.ui.FormulaPresentation
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.semantics.Details
import org.jetbrains.compose.web.dom.P
import materiewelle.ui.semantics.Summary
import org.jetbrains.compose.web.dom.Text

/** Optional recall tied to the labelled projection triangle in braggGeometryChart. */
@Composable
internal fun BraggSineHelp() {
    Details {
        Summary { Text("Sinus im Dreieck auffrischen") }
        P {
            Text("Im markierten Dreieck liegt der rechte Winkel bei ")
            InlineFormula(FormulaId.GEOMETRY_POINT_P, FormulaPresentation.TEXT_SYMBOL)
            Text(". Die gegenüberliegende Seite ")
            InlineFormula(FormulaId.GEOMETRY_SEGMENT_AB, FormulaPresentation.TEXT_SYMBOL)
            Text(" ist die Hypotenuse: der senkrechte Ebenenabstand. Die cyan markierte Strecke ")
            InlineFormula(FormulaId.GEOMETRY_SEGMENT_PB, FormulaPresentation.TEXT_SYMBOL)
            Text(" liegt dem Winkel bei ")
            InlineFormula(FormulaId.GEOMETRY_POINT_A, FormulaPresentation.TEXT_SYMBOL)
            Text(" gegenüber und ist seine Gegenkathete.")
        }
        P {
            Text("Der Winkel bei ")
            InlineFormula(FormulaId.GEOMETRY_POINT_A, FormulaPresentation.TEXT_SYMBOL)
            Text(" ist ebenfalls der Braggwinkel. Seine Schenkel stehen jeweils senkrecht auf der Netzebene und dem einlaufenden Strahl; dadurch bleibt der Winkel gleich.")
        }
        P { Text("Sinus bedeutet Gegenkathete geteilt durch Hypotenuse:") }
        InlineFormula(FormulaId.BRAGG_SINE)
        P {
            Text("Mit dem Ebenenabstand multiplizieren: So erhältst du die erste Zusatzstrecke. Der zweite Zusatzweg kommt erst beim auslaufenden Strahl hinzu.")
        }
    }
}
