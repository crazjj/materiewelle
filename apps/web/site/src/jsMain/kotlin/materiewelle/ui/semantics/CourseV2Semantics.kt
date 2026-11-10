package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.courseStyles(vararg values: CourseStyle) {
    classes(*values.map { it.cssName }.toTypedArray())
}
