package materiewelle.ui.semantics

enum class DataLoadState(val htmlValue: String) {
    LOADING("loading"),
    READY("ready"),
    FAILED("failed"),
}

enum class ResolvedTheme(val htmlValue: String) {
    LIGHT("light"),
    DARK("dark"),
}

/** Attribute key and value type travel together; encoding is a browser boundary. */
object LabData {
    val DG_LESSON_STEP = Key<materiewelle.core.course.DgLessonStep>("data-dg-lesson-step") { it.key }
    val LEARNING_PHASE =
        Key<materiewelle.core.course.LearningPhase>("data-learning-phase") { it.key }
    val ENERGY_FACTOR = number("data-energy-factor")
    val MOMENTUM_FACTOR = number("data-momentum-factor")
    val WAVELENGTH_FACTOR = number("data-wavelength-factor")
    val MEAN_CHANGE_PX = number("data-mean-change-px")
    val DOCUMENT_SECTION =
        Key<materiewelle.core.course.DocumentSectionId>("data-document-section") { it.key }

    class Key<T> internal constructor(val attribute: String, val encode: (T) -> String)

    private fun flag(name: String) = Key<Boolean>(name) { it.toString() }

    private fun integer(name: String) = Key<Int>(name) { it.toString() }

    private fun number(name: String) =
        Key<Double>(name) {
            require(it.isFinite())
            it.toString()
        }

    private fun text(name: String) = Key<String>(name) { it }

    private fun loadState(name: String) = Key<DataLoadState>(name) { it.htmlValue }

    val SETUP_PART = Key<SetupPart>("data-setup-part") { it.key }
    val THEME =
        Key<ResolvedTheme>(materiewelle.presentation.PresentationKeys.THEME_ATTRIBUTE) {
            it.htmlValue
        }
    val STATION = Key<materiewelle.core.course.StationId>("data-station") { it.key }
    val QUESTION = Key<materiewelle.core.course.QuestionId>("data-question") { it.name }
    val REVIEW_DUE = integer("data-review-due")
    val ANSWER_STATUS =
        Key<materiewelle.core.course.NumericAnswerStatus>("data-answer-status") { it.name }
    val ANGLE_DEG = number("data-angle-deg")
    val ANSWER_PM = number("data-answer-pm")
    val CORRECT = flag("data-correct")
    val DG_INDEX = integer("data-dg-index")
    val EQUATION = integer("data-equation")
    val GRAY_VALUE = number("data-gray-value")
    val INTERVAL = integer("data-interval")
    val LOAD_STATE = loadState("data-load-state")
    val MARKER_INDEX = integer("data-marker-index")
    val NATIVE_HEIGHT = integer("data-native-height")
    val NATIVE_WIDTH = integer("data-native-width")
    val NATIVE_X = number("data-native-x")
    val NATIVE_Y = number("data-native-y")
    val OWN_MARKER = integer("data-own-marker")
    val PERIOD_UM = number("data-period-um")
    val PHASE = number("data-phase")
    val PROFILE_MAX = number("data-profile-max")
    val PROFILE_MIN = number("data-profile-min")
    val PROFILE_SAMPLE_COUNT = integer("data-profile-sample-count")
    val PROFILE_START_X = integer("data-profile-start-x")
    val PROGRESS = number("data-progress")
    val RATIO = number("data-ratio")
    val REFERENCE_MARKER = integer("data-reference-marker")
    val RUNNING = flag("data-running")
    val SCENE = integer("data-scene")
    val CLIP_SECONDS = number("data-clip-seconds")
    val SPACING_PX = number("data-spacing-px")
    val STEP = integer("data-step")
    val TOUR_STEP = integer("data-tour-step")
    val VALID = flag("data-valid")
    val LITERAL_EXPORT = flag("data-literal-export")
    val VALUE_NM = number("data-value-nm")
    val VALUE_PERCENT = number("data-value-percent")
    val VALUE_PM = number("data-value-pm")
    val VALUE_PX = number("data-value-px")
    val VERSION = text("data-version")
    val VOLTAGE_V = number("data-voltage-v")
    val WAVELENGTH_PM = number("data-wavelength-pm")
}
