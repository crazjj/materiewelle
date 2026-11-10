package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

/** Feature code chooses closed style identities; only this DOM boundary emits class tokens. */
fun AttrsScope<*>.learningFlowStyles(vararg values: LearningFlowClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
