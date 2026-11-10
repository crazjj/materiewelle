package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.sessionUiStyles(vararg values: SessionUiClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** Protect a control that edits personal work, without disabling adjacent reading actions. */
fun AttrsScope<*>.protectPersonalWork(readOnly: Boolean) {
    if (readOnly) {
        attr("disabled", "")
        describedBy(Id.COURSE_STORAGE_STATUS)
    }
}
