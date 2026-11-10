package materiewelle.presentation

import materiewelle.ui.*

// The pre-paint executable deliberately has no Compose, fetch or UI initialization.
// Only this small adapter knows the browser's external protocol surface.
private external val document: EarlyDocument
private external val window: EarlyWindow

private external interface EarlyDocument {
    val documentElement: EarlyElement
}

private external interface EarlyElement {
    fun setAttribute(name: String, value: String)
}

private external interface EarlyWindow {
    val localStorage: EarlyStorage

    fun matchMedia(query: String): EarlyMedia
}

private external interface EarlyStorage {
    fun getItem(key: String): String?

    fun setItem(key: String, value: String)
}

private external interface EarlyMedia {
    val matches: Boolean
}

// Storage can throw DOMExceptions, including errors from another realm. This catch is
// confined to the external browser boundary; preferences remain closed Kotlin types.
private fun readStored(key: String): String? =
    try {
        window.localStorage.getItem(key)
    } catch (_: dynamic) {
        null
    }

private fun store(key: String, value: String) {
    try {
        window.localStorage.setItem(key, value)
    } catch (_: dynamic) {}
}

fun main() {
    val storedTheme = readStored(PresentationKeys.THEME)
    val theme = ThemePreference.fromStorage(storedTheme)
    if (storedTheme != null && storedTheme != theme.key) {
        store(PresentationKeys.THEME, theme.key)
    }
    document.documentElement.setAttribute(PresentationKeys.THEME_ATTRIBUTE, theme.key)
    val preference = MotionPreference.fromStorage(readStored(PresentationKeys.MOTION))
    val reduced = window.matchMedia(PresentationKeys.REDUCED_MOTION).matches
    document.documentElement.setAttribute(
        PresentationKeys.MOTION_ATTRIBUTE,
        resolveMotion(preference, reduced, visible = true).key,
    )
}
