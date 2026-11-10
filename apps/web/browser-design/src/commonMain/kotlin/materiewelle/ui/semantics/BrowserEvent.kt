package materiewelle.ui.semantics

/** Event names are browser protocol values, not ad-hoc feature strings. */
enum class BrowserEvent(val eventName: String) {
    HASH_CHANGE("hashchange"),
    POINTER_DOWN("pointerdown"),
    POINTER_MOVE("pointermove"),
    POINTER_UP("pointerup"),
    POINTER_CANCEL("pointercancel"),
    RESIZE("resize"),
    SCROLL("scroll"),
    WHEEL("wheel"),
    TOUCH_MOVE("touchmove"),
    FOCUS_IN("focusin"),
    KEY_DOWN("keydown"),
    CHANGE("change"),
    ERROR("error"),
    VOICES_CHANGED("voiceschanged"),
    VISIBILITY_CHANGE("visibilitychange"),
}
