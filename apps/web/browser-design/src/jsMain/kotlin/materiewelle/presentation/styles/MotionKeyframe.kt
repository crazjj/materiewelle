package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.CSSNamedKeyframes

sealed class MotionKeyframe(override val name: String) : CSSNamedKeyframes {
    data object LAB_PHASE_PULSE : MotionKeyframe("lab-phase-pulse")

    data object LAB_PHASE_SCAN : MotionKeyframe("lab-phase-scan")

    data object LAB_INK_DRAW : MotionKeyframe("lab-ink-draw")

    data object LAB_MASCOT_RIPPLE : MotionKeyframe("lab-mascot-ripple")

    data object LAB_WORKED_STAMP : MotionKeyframe("lab-worked-stamp")

    data object LAB_ARRIVE : MotionKeyframe("lab-arrive")

    data object LAB_BUTTON_SHEEN : MotionKeyframe("lab-button-sheen")

    data object LAB_FEEDBACK : MotionKeyframe("lab-feedback")

    data object LAB_ICON_WAVE : MotionKeyframe("lab-icon-wave")

    data object LAB_MASCOT_BLINK : MotionKeyframe("lab-mascot-blink")

    data object LAB_MASCOT_FLOAT : MotionKeyframe("lab-mascot-float")

    data object LAB_MASCOT_HELLO : MotionKeyframe("lab-mascot-hello")

    data object LAB_MASCOT_WINK : MotionKeyframe("lab-mascot-wink")

    data object LAB_NAV_LINE : MotionKeyframe("lab-nav-line")

    data object LAB_PHASE_LAND : MotionKeyframe("lab-phase-land")

    data object LAB_READING_UPDATE : MotionKeyframe("lab-reading-update")

    data object LAB_READY_BREATHE : MotionKeyframe("lab-ready-breathe")

    data object LAB_SIGNAL_SCAN : MotionKeyframe("lab-signal-scan")

    data object LAB_SIGNAL_STAGE : MotionKeyframe("lab-signal-stage")

    data object LAB_WAVE_TRAVEL : MotionKeyframe("lab-wave-travel")

    data object LEARNING_CARD_ARRIVE : MotionKeyframe("learning-card-arrive")

    data object LEARNING_CONNECTOR_ARRIVE : MotionKeyframe("learning-connector-arrive")

    data object LEARNING_CONTENT_ARRIVE : MotionKeyframe("learning-content-arrive")

    data object LEARNING_NUMBER_GLOW : MotionKeyframe("learning-number-glow")

    data object LEARNING_POINT_FEEDBACK : MotionKeyframe("learning-point-feedback")

    data object PANEL_ENTER : MotionKeyframe("panel-enter")
}
