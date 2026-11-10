package materiewelle.ui

enum class ThemePreference(val key: String, val label: String) {
    LIGHT("light", "Hell"),
    DARK("dark", "Dunkel");

    fun toggled(): ThemePreference = if (this == DARK) LIGHT else DARK

    companion object {
        /**
         * First visit, legacy System and unrecognized storage all resolve to the requested dark
         * default.
         */
        fun fromStorage(value: String?): ThemePreference =
            entries.firstOrNull { it.key == value } ?: DARK
    }
}
