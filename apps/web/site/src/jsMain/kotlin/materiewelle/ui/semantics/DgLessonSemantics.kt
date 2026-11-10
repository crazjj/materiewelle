package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.dgLessonStyles(vararg values: DgLessonClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

fun AttrsScope<*>.currentLearningStep() { attr("aria-current", "step") }
