package materiewelle.ui

enum class MotionPreference(val key: String) {
    ANIMATED("animated"),
    STILL("still");

    fun toggled() = if (this == ANIMATED) STILL else ANIMATED

    companion object {
        fun fromStorage(key: String?) = entries.firstOrNull { it.key == key } ?: ANIMATED
    }
}

enum class MotionState(val key: String) {
    RUNNING("running"),
    PAUSED("paused"),
    REDUCED("reduced"),
    HIDDEN("hidden"),
}

fun resolveMotion(preference: MotionPreference, reduced: Boolean, visible: Boolean): MotionState =
    when {
        reduced -> MotionState.REDUCED
        preference == MotionPreference.STILL -> MotionState.PAUSED
        !visible -> MotionState.HIDDEN
        else -> MotionState.RUNNING
    }
