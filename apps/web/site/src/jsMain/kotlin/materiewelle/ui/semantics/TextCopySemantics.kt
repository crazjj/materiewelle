package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.textCopyStyles(vararg values: TextCopyClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
