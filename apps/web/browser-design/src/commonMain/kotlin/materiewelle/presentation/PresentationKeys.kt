package materiewelle.presentation

/** Shared protocol identities; the early Kotlin executable and application cannot drift. */
object PresentationKeys {
    const val THEME = "materiewelle.theme"
    const val MOTION = "materiewelle.motion"
    const val UI_DRAFTS = "materiewelle.ui-drafts.v1"
    const val THEME_ATTRIBUTE = "data-theme"
    const val MOTION_ATTRIBUTE = "data-motion"
    const val REDUCED_MOTION = "(prefers-reduced-motion: reduce)"
}
