package materiewelle.ui.semantics

enum class MotionId(override val value: String) : ElementId {
    TOGGLE("motion-toggle"),
    HERO("motion-hero"),
    WAVE("motion-wave"),
    MASCOT("motion-mascot"),
    JOKE("motion-joke"),
    SIGNAL("motion-signal"),
    WAVE_EXPLORER("motion-wave-explorer"),
}

enum class MotionClass(override val cssName: String) : StyleClass {
    TOOLS("display-tools"),
    TOGGLE("motion-toggle"),
    ICON("motion-icon"),
    HERO_LAYOUT("motion-hero-layout"),
    HERO("motion-hero"),
    HEAD("motion-head"),
    STATUS("motion-status"),
    FIGURE("motion-figure"),
    GRID("motion-grid"),
    WAVE("motion-wave"),
    WAVE_GHOST("motion-wave-ghost"),
    WAVE_TRACE("motion-wave-trace"),
    WAVE_STRIP("motion-wave-strip"),
    WAVE_META("motion-wave-meta"),
    MASCOT("motion-mascot"),
    EYES("motion-eyes"),
    GREETING("motion-greeting"),
    MASCOT_ROW("motion-mascot-row"),
    JOKE("motion-joke"),
    SIGNAL("motion-signal"),
    SIGNAL_STAGES("motion-signal-stages"),
    SIGNAL_NODE("motion-signal-node"),
    SPARK("motion-spark"),
    DOT("motion-dot"),
    READING_UPDATE("motion-reading-update"),
    PHASE_ICON("motion-phase-icon"),
    PHASE_CURRENT("motion-phase-current"),
    PHASE_PULSE("motion-phase-pulse"),
    PHASE_SCAN("motion-phase-scan"),
    PHASE_INK("motion-phase-ink"),
    MASCOT_RIPPLE("motion-mascot-ripple"),
    CONSTRUCTION_INK("motion-construction-ink"),
    CONSTRUCTION_SECOND("motion-construction-second"),
    WORKED_BUTTON("motion-worked-button"),
}

enum class MotionSurface(val key: String) {
    HERO("hero"),
    WAVELENGTH("wavelength"),
    APPARATUS("apparatus"),
}
