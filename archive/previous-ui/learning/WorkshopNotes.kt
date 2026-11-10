package materiewelle.learning

import androidx.compose.runtime.Composable
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.LabClass
import materiewelle.ui.semantics.styles
import materiewelle.ui.semantics.Details
import org.jetbrains.compose.web.dom.P
import materiewelle.ui.semantics.Summary

/** Shared notes disclosure; independent of the archived workshop host. */
@Composable
fun WorkshopNotes(title: String, text: String) {
    Details(attrs = { styles(LabClass.NOTES) }) {
        Summary() { Text(title) }
        P { Text(text) }
    }
}
