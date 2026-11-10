package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.Div

/**
 * Picture steps add explanations to the ordinary document flow. Revealing text
 * does not scroll the page or move focus; earlier explanations stay fully readable
 * beside the sticky drawing without a separate, clipped scrolling region.
 */
@Composable
internal fun DgGuidedExplanation(
    panel: DgLessonId,
    label: String,
    content: @Composable () -> Unit,
) {
    Div(attrs = {
        elementId(panel)
        dgLessonStyles(DgLessonClass.EXPLANATION, DgLessonClass.GUIDED_EXPLANATION)
        role(AriaRole.REGION); ariaLabel(label)
    }) { content() }
}
