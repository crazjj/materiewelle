package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

/** Closed identities for the linked native-image measurement workspace. */
fun AttrsScope<*>.measurementStyles(vararg names: MeasurementClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}
