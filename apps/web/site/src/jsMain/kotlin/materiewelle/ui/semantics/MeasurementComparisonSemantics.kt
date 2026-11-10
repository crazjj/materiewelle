package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

/** Kotlin identities terminate at the browser boundary. */
fun AttrsScope<*>.measurementComparisonStyles(vararg names: MeasurementComparisonClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}

fun AttrsScope<*>.measurementCase(value: MeasurementCase) {
    attr("data-measurement-case", value.key)
}
