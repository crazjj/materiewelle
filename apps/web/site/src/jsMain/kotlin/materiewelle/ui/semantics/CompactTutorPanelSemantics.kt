package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.compactTutorPanelStyles(vararg values: CompactTutorPanelClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
