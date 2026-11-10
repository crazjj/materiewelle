package materiewelle.ui.semantics

import org.jetbrains.compose.web.attributes.AttrsScope

fun <T> AttrsScope<*>.data(key: LabData.Key<T>, value: T) {
    attr(key.attribute, key.encode(value))
}
