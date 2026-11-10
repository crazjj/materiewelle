package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.evidenceReportStyles(vararg values: EvidenceReportClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}
