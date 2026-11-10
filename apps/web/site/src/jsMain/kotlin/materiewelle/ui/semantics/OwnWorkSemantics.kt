package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.ownWorkStyles(vararg values: OwnWorkClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
