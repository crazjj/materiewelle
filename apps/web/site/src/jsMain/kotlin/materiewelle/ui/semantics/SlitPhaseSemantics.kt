package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.slitPhaseStyles(vararg values: SlitPhaseClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
