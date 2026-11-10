package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

/**
 * Closed identities for the crystal investigation, retained constructions and coupled slit view.
 */

/** Feature classes terminate at this DOM adapter, rather than becoming free-form UI strings. */
fun AttrsScope<*>.geometryStyles(vararg names: GeometrySlitsClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}
