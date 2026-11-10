package materiewelle.ui.semantics

import materiewelle.core.Experiment
import materiewelle.core.ExperimentMode

/** Closed DOM identities. Strings are exposed only to HTML, CSS/SVG and browser APIs. */
sealed interface ElementId {
    val value: String
    val fragment: String
        get() = "#$value"

    val svgReference: String
        get() = "url($fragment)"

    enum class Fixed(override val value: String) : ElementId {
        FOCUS_LEARNING("focus-learning"),
        FOCUS_PANEL("focus-panel"),
        FOCUS_PREDICTION("focus-prediction"),
        FOCUS_CONTINUE("focus-continue"),
        FOCUS_VOLTAGE("focus-voltage"),
        FOCUS_FOURFOLD("focus-fourfold"),
        FOCUS_SCALING("focus-scaling"),
        FOCUS_PERTURBATION("focus-perturbation"),
        FOCUS_OFFSET("focus-offset"),
        FOCUS_SENSITIVITY("focus-sensitivity"),
        FOCUS_PREVIEW("focus-preview"),
        FOCUS_TUTOR("focus-tutor"),
        TYPST_SECTION_SELECT("typst-section-select"),
        TYPST_RETURN_STATION("typst-return-station"),
        TYPST_SECTION_STATUS("typst-section-status"),
        COURSE_DOCUMENT_SECTION("course-document-section"),
        COURSE_DOCUMENT_SOURCES("course-document-sources"),
        COURSE_STAGE_PROGRESS("course-stage-progress"),
        REFLECTION_EXPORT("reflection-export"),
        REFLECTION_SCOPE("reflection-scope"),
        REFLECTION_SUMMARY("reflection-summary"),
        REFLECTION_PREVIEW("reflection-preview"),
        REFLECTION_DOWNLOAD("reflection-download"),
        REFLECTION_COPY_PROMPT("reflection-copy-prompt"),
        REFLECTION_COPY_STATUS("reflection-copy-status"),
        REFLECTION_PROMPT_PREVIEW("reflection-prompt-preview"),
        REFLECTION_PROMPT_TEXT("reflection-prompt-text"),
        REFLECTION_INCLUDE_MEASUREMENT("reflection-include-measurement"),
        VOICE_FILE("voice-file"),
        VOICE_PLAYER("voice-player"),
        VOICE_STATUS("voice-status"),
        VOICE_CLEAR("voice-clear"),
        PRESENTATION_PDF("presentation-pdf"),
        DG_VIEW_BRAGG("dg-view-bragg"),
        DG_VIEW_SURFACE("dg-view-surface"),
        DG_GEOMETRY_STEP("dg-geometry-step"),
        DG_GEOMETRY_NEXT("dg-geometry-next"),
        DG_MODEL_ANGLE("dg-model-angle"),
        DG_MODEL_VOLTAGE("dg-model-voltage"),
        DG_RESET_MODEL("dg-reset-model"),
        DG_SIGNED_DEVIATION("dg-signed-deviation"),
        DG_STRUCTURE("dg-structure"),
        JONSSON_JUSTIFICATION("jonsson-justification"),
        JONSSON_SEAL_SELECTION("jonsson-seal-selection"),
        JONSSON_SHOW_REFERENCE("jonsson-show-reference"),
        JONSSON_REFERENCE_STATUS("jonsson-reference-status"),
        JONSSON_EXPORT_JSON("jonsson-export-json"),
        JONSSON_EXPORT_TEXT("jonsson-export-text"),
        JONSSON_OPTICS("jonsson-optics"),
        MARKER_NEW_X("marker-new-x"),
        TUTOR_INCLUDE_COURSE("tutor-include-course"),
        TUTOR_COURSE_SUMMARY("tutor-course-summary"),
        COURSE_TRANSFER_ANSWER("course-transfer-answer"),
        COURSE_TRANSFER_CHECK("course-transfer-check"),
        COURSE_TRANSFER_FEEDBACK("course-transfer-feedback"),
        COURSE_CLASSICAL_LAMBDA("course-classical-lambda"),
        COURSE_CLASSICAL_AMPLITUDE("course-classical-amplitude"),
        COURSE_QUESTION_HINT("course-question-hint"),
        COURSE_STAGE_PLAY("course-stage-play"),
        COURSE_MOMENTUM("course-momentum"),
        COURSE_NUMERIC_CHECK("course-numeric-check"),
        COURSE_NUMERIC_FEEDBACK("course-numeric-feedback"),
        COURSE_NAV("course-nav"),
        COURSE_DOCUMENT_NAV("course-document-nav"),
        COURSE_TUTOR_NAV("course-tutor-nav"),
        COURSE_HOME("course-home"),
        COURSE_TITLE("course-title"),
        COURSE_START("course-start"),
        COURSE_NEXT("course-next"),
        COURSE_PREV("course-prev"),
        COURSE_PROGRESS("course-progress"),
        COURSE_PHASE("course-phase"),
        COURSE_HISTORY("course-history"),
        COURSE_MAIN("course-main"),
        COURSE_OPEN_LAB("course-open-lab"),
        COURSE_RETURN("course-return"),
        COURSE_TASK_ANSWER("course-task-answer"),
        COURSE_TASK_CHECK("course-task-check"),
        COURSE_TASK_FEEDBACK("course-task-feedback"),
        COURSE_NOTE("course-note"),
        COURSE_QUESTION("course-question"),
        COURSE_HINT("course-hint"),
        COURSE_HINT_CONTENT("course-hint-content"),
        COURSE_COMPLETED("course-completed"),
        COURSE_STAGE("course-stage"),
        COURSE_STAGE_NEXT("course-stage-next"),
        COURSE_STAGE_PREV("course-stage-prev"),
        COURSE_LAB_MODE("course-lab-mode"),
        COURSE_NOTE_SAVE("course-note-save"),
        COURSE_EXPORT("course-export"),
        COURSE_STORAGE_STATUS("course-storage-status"),
        COURSE_RESET("course-reset"),
        COURSE_STORAGE_EXPORT("course-storage-export"),
        COURSE_REPEAT("course-repeat"),
        COURSE_MODEL_G("course-model-g"),
        COURSE_MODEL_B("course-model-b"),
        COURSE_MODEL_PLOT("course-model-plot"),
        COURSE_MODEL_RESULT("course-model-result"),
        COURSE_BAND_COMPARE("course-band-compare"),
        COURSE_NUMERIC_UNIT("course-numeric-unit"),
        COURSE_TASK_PROMPT("course-task-prompt"),
        COURSE_TOUR("course-tour"),
        COURSE_SOURCES("course-sources"),
        COURSE_SNAPSHOT("course-snapshot"),
        COURSE_HISTORY_KNOWN("course-history-known"),
        COURSE_HISTORY_OPEN("course-history-open"),
        COURSE_HISTORY_NEW("course-history-new"),
        ANIMATION_DERIVATION("animation-derivation"),
        ANIMATION_EXPLANATION("animation-explanation"),
        ANIMATION_INPUT_STATUS("animation-input-status"),
        ANIMATION_MOTION_STATUS("animation-motion-status"),
        ANIMATION_NEXT("animation-next"),
        ANIMATION_PLAY("animation-play"),
        ANIMATION_PREV("animation-prev"),
        ANIMATION_PROGRESS("animation-progress"),
        ANIMATION_RESET("animation-reset"),
        ANIMATION_RESULT("animation-result"),
        ANIMATION_STAGE("animation-stage"),
        ANIMATION_SWEEP("animation-sweep"),
        ANIMATION_VOLTAGE("animation-voltage"),
        CHART_COORDINATES("chart-coordinates"),
        CHART_KEYBOARD_HINT("chart-keyboard-hint"),
        CLEAR_COMPARISON("clear-comparison"),
        COMPARISON_RATIO("comparison-ratio"),
        COMPARISON_VOLTAGE("comparison-voltage"),
        COMPARISON_WAVELENGTH_PM("comparison-wavelength-pm"),
        DEBROGLIE_BACK("debroglie-back"),
        DEBROGLIE_CHART("debroglie-chart"),
        DEBROGLIE_COMPARISON("debroglie-comparison"),
        DEBROGLIE_NEXT("debroglie-next"),
        DEBROGLIE_STEP("debroglie-step"),
        DG_BACK("dg-back"),
        DG_CLEAR_COMPARISON("dg-clear-comparison"),
        DG_COMPARISON("dg-comparison"),
        DG_COORDINATES("dg-coordinates"),
        DG_DATA_STATUS("dg-data-status"),
        DG_DEVIATION("dg-deviation"),
        DG_DIFFRACTION("dg-diffraction"),
        DG_INCOMING_ARROW("dg-incoming-arrow"),
        DG_MODEL_LIMIT("dg-model-limit"),
        DG_NEXT("dg-next"),
        DG_NICKEL_HATCH("dg-nickel-hatch"),
        DG_PREDICTED("dg-predicted"),
        DG_SAVE_COMPARISON("dg-save-comparison"),
        DG_STEP("dg-step"),
        DG_VIEW_COMPARISON("dg-view-comparison"),
        DG_VIEW_GEOMETRY("dg-view-geometry"),
        GRAY_PROFILE_METHOD("gray-profile-method"),
        GRAY_PROFILE_STATUS("gray-profile-status"),
        IMAGE_ZOOM("image-zoom"),
        JONSSON_BACK("jonsson-back"),
        JONSSON_COMPARE_CLEAR("jonsson-compare-clear"),
        JONSSON_COMPARE_SAVE("jonsson-compare-save"),
        JONSSON_COMPARISON("jonsson-comparison"),
        JONSSON_COUNT("jonsson-count"),
        JONSSON_CURSOR("jonsson-cursor"),
        JONSSON_EXPLANATION("jonsson-explanation"),
        JONSSON_GRAY_PROFILE("jonsson-gray-profile"),
        JONSSON_IMAGE_INSTRUCTIONS("jonsson-image-instructions"),
        JONSSON_IMAGE_WINDOW("jonsson-image-window"),
        JONSSON_INTERACTIVE_IMAGE("jonsson-interactive-image"),
        JONSSON_INTERVALS("jonsson-intervals"),
        JONSSON_LAMBDA("jonsson-lambda"),
        JONSSON_MEAN_EQUATION("jonsson-mean-equation"),
        JONSSON_NATIVE_IMAGE("jonsson-native-image"),
        JONSSON_NEXT("jonsson-next"),
        JONSSON_PREDICTION("jonsson-prediction"),
        JONSSON_SOURCE_STATUS("jonsson-source-status"),
        JONSSON_SPACING("jonsson-spacing"),
        JONSSON_STEP("jonsson-step"),
        JONSSON_VIEW_RESET("jonsson-view-reset"),
        JONSSON_WHOLE_IMAGE("jonsson-whole-image"),
        LAB_TOUR("lab-tour"),
        LABORATORY("laboratory"),
        MARKER_ADD("marker-add"),
        MARKER_MINUS("marker-minus"),
        MARKER_PLUS("marker-plus"),
        MARKER_REMOVE("marker-remove"),
        MARKER_RESET("marker-reset"),
        MARKER_STATE("marker-state"),
        MARKER_WORKSHOP("marker-workshop"),
        MARKER_X("marker-x"),
        MARKER_X_ERROR("marker-x-error"),
        MEASUREMENT_PROFILE("measurement-profile"),
        MEASUREMENT_SELECTION_STATUS("measurement-selection-status"),
        MEDIA_EXPORT("media-export"),
        MEDIA_PLAY("media-play"),
        MEDIA_RESET("media-reset"),
        MEDIA_TIMELINE("media-timeline"),
        MEDIA_CAPTION("media-caption"),
        MEDIA_RESULT("media-result"),
        MEDIA_VTT_EXPORT("media-vtt-export"),
        MEDIA_TRANSCRIPT_EXPORT("media-transcript-export"),
        MEDIA_PANEL("media-panel"),
        MEDIA_PREVIEW("media-preview"),
        MEDIA_SCRIPT("media-script"),
        MEDIA_SPEAK("media-speak"),
        MEDIA_STOP("media-stop"),
        PORTABLE_TUTOR("portable-tutor"),
        PROFILE_DESCRIPTION("profile-description"),
        PROFILE_SAMPLE("profile-sample"),
        PROFILE_SAMPLE_X("profile-sample-x"),
        PROFILE_STATUS("profile-status"),
        QUIZ_ANSWER("quiz-answer"),
        QUIZ_AUTHORING_EXPORT("quiz-authoring-export"),
        QUIZ_AUTHORING_PREVIEW("quiz-authoring-preview"),
        QUIZ_CALIBRATION_FEEDBACK("quiz-calibration-feedback"),
        QUIZ_CHECK("quiz-check"),
        QUIZ_DRAFT("quiz-draft"),
        QUIZ_FEEDBACK("quiz-feedback"),
        QUIZ_IMPORT("quiz-import"),
        QUIZ_IMPORT_CHECK("quiz-import-check"),
        QUIZ_IMPORT_STATUS("quiz-import-status"),
        QUIZ_NEW("quiz-new"),
        QUIZ_PANEL("quiz-panel"),
        QUIZ_QUESTION("quiz-question"),
        RESET("reset"),
        SAVE_COMPARISON("save-comparison"),
        SELECTED_MARKER("selected-marker"),
        SETUP_DISTANCE("setup-distance"),
        SETUP_EXPLANATION("setup-explanation"),
        SETUP_INTENSITY_PROFILE("setup-intensity-profile"),
        SETUP_PART_SCREEN("setup-part-screen"),
        SETUP_PART_SLITS("setup-part-slits"),
        SETUP_PART_SOURCE("setup-part-source"),
        SETUP_PARTS("setup-parts"),
        SETUP_PLAY("setup-play"),
        SETUP_RESET("setup-reset"),
        SETUP_RESULT("setup-result"),
        SETUP_SCHEMATIC("setup-schematic"),
        SETUP_SPACING("setup-spacing"),
        SETUP_STEP("setup-step"),
        SETUP_VOLTAGE("setup-voltage"),
        SHOW_MARKERS("show-markers"),
        SPEECH_STATUS("speech-status"),
        SPOTLIGHT_DIALOG("spotlight-dialog"),
        TEACHING_WAVES("teaching-waves"),
        THEME_PREFERENCE("theme-preference"),
        TOUR_BACK("tour-back"),
        TOUR_CLOSE("tour-close"),
        TOUR_CONNECTOR("tour-connector"),
        TOUR_DESCRIPTION("tour-description"),
        TOUR_NEXT("tour-next"),
        TOUR_TITLE("tour-title"),
        TRY_600("try-600"),
        TUTOR_CONTEXT_SUMMARY("tutor-context-summary"),
        TUTOR_COPY_START("tutor-copy-start"),
        TUTOR_COPY_STATUS("tutor-copy-status"),
        TUTOR_DOWNLOAD_CONTEXT("tutor-download-context"),
        TUTOR_DOWNLOAD_SKILL("tutor-download-skill"),
        TUTOR_DOWNLOAD_START("tutor-download-start"),
        TUTOR_EXPLANATION("tutor-explanation"),
        TUTOR_HINT("tutor-hint"),
        TUTOR_LEVEL("tutor-level"),
        TUTOR_MODE("tutor-mode"),
        TUTOR_OWN_QUESTION("tutor-own-question"),
        TUTOR_PACKAGE_ERROR("tutor-package-error"),
        TUTOR_PACKAGE_LOADING("tutor-package-loading"),
        TUTOR_PACKAGE_RETRY("tutor-package-retry"),
        TUTOR_PACKAGE_VERSION("tutor-package-version"),
        TUTOR_PANEL("tutor-panel"),
        TUTOR_REVEAL("tutor-reveal"),
        TUTOR_START_PREVIEW("tutor-start-preview"),
        TUTOR_START_TEXT("tutor-start-text"),
        TUTOR_TOPIC("tutor-topic"),
        TYPST_LOAD_ERROR("typst-load-error"),
        TYPST_NEXT("typst-next"),
        TYPST_PAGE_STATUS("typst-page-status"),
        TYPST_PDF("typst-pdf"),
        TYPST_PREV("typst-prev"),
        TYPST_TEXT_ALTERNATIVE("typst-text-alternative"),
        TYPST_TEXT_DOWNLOAD("typst-text-download"),
        TYPST_PAGE_TEXT_DOWNLOAD("typst-page-text-download"),
        TYPST_VECTOR_PAGE("typst-vector-page"),
        TYPST_VIEWPORT("typst-viewport"),
        TYPST_ZOOM("typst-zoom"),
        VOLTAGE("voltage"),
        VOLTAGE_ERROR("voltage-error"),
        VOLTAGE_RANGE("voltage-range"),
        VOLTAGE_SLIDER("voltage-slider"),
        WAVELENGTH_PM("wavelength-pm"),
        WORKSHOP_ANIMATION("workshop-animation"),
        WORKSHOP_DOCUMENT("workshop-document"),
        WORKSHOP_SETUP("workshop-setup"),
        WORKSHOP_TABS("workshop-tabs"),
        WORKSHOP_TOUR("workshop-tour"),
        WORKSHOP_TUTOR("workshop-tutor"),
    }

    data class LearningPhaseButton(val phase: materiewelle.core.course.LearningPhase) : ElementId {
        override val value: String
            get() = "focus-phase-${phase.key}"
    }

    data class RepeatQuestion(val question: materiewelle.core.course.QuestionId) : ElementId {
        override val value: String
            get() = "repeat-${question.name}"
    }

    data class StationLink(val station: materiewelle.core.course.StationId) : ElementId {
        override val value: String
            get() = "station-${station.key}"
    }

    data class CourseOption(
        val question: materiewelle.core.course.QuestionId,
        val option: materiewelle.core.course.QuestionOptionId,
    ) : ElementId {
        override val value: String
            get() = "course-option-${question.name}-${option.name.lowercase()}"
    }

    data class CoursePhase(val phase: materiewelle.core.course.ArgumentPhase) : ElementId {
        override val value: String
            get() = "course-phase-${phase.name.lowercase()}"
    }

    data class CoursePanel(val key: CoursePanelId) : ElementId {
        override val value: String
            get() = key.htmlId
    }

    data class Navigation(val destination: LabDestination) : ElementId {
        override val value: String
            get() = "nav-${destination.key}"
    }

    data class Panel(val destination: LabDestination) : ElementId {
        override val value: String
            get() = "panel-${destination.key}"
    }

    data class Mode(val experiment: Experiment, val mode: ExperimentMode) : ElementId {
        override val value: String
            get() {
                val prefix =
                    when (experiment) {
                        Experiment.DE_BROGLIE -> "debroglie"
                        Experiment.DAVISSON_GERMER -> "dg"
                        Experiment.JONSSON -> "jonsson"
                    }
                val suffix =
                    when (mode) {
                        ExperimentMode.LAB -> "lab"
                        ExperimentMode.GUIDED -> "guided"
                    }
                return "$prefix-mode-$suffix"
            }
    }

    data class CalibrationChoice(val index: UiIndex) : ElementId {
        init {
            require(index.value in 0..1)
        }

        override val value: String
            get() = "quiz-calibration-${index.value}"
    }

    data class TutorPrompt(val index: UiIndex) : ElementId {
        override val value: String
            get() = "tutor-prompt-${index.value}"
    }

    data class MediaScene(val index: UiIndex) : ElementId {
        override val value: String
            get() = "media-scene-${index.value}"
    }
}

typealias Id = ElementId.Fixed

/** A list index cannot accidentally be supplied as an arbitrary DOM identifier. */
value class UiIndex(val value: Int) {
    init {
        require(value >= 0)
    }
}

/** Navigation identity is distinct from the identity of a DOM element. */
enum class LabDestination(val key: String, val title: String, val experiment: Experiment?) {
    DE_BROGLIE(Experiment.DE_BROGLIE.route, "De Broglie", Experiment.DE_BROGLIE),
    DAVISSON_GERMER(
        Experiment.DAVISSON_GERMER.route,
        "Davisson–Germer",
        Experiment.DAVISSON_GERMER,
    ),
    JONSSON(Experiment.JONSSON.route, "Jönsson", Experiment.JONSSON),
    WORKSHOP("werkstatt", "Lernwerkstatt", null);

    val fragment: String
        get() = "#$key"

    companion object {
        fun fromHash(hash: String): LabDestination? {
            if (hash.removePrefix("#") == WORKSHOP.key) return WORKSHOP
            return Experiment.fromRoute(hash)?.let(::forExperiment)
        }

        fun forExperiment(experiment: Experiment): LabDestination =
            when (experiment) {
                Experiment.DE_BROGLIE -> DE_BROGLIE
                Experiment.DAVISSON_GERMER -> DAVISSON_GERMER
                Experiment.JONSSON -> JONSSON
            }
    }
}

/** Normalize the browser KeyboardEvent.key boundary once. Unknown keys remain unknown. */
enum class LabKey(val browserValue: String) {
    LEFT("ArrowLeft"),
    RIGHT("ArrowRight"),
    UP("ArrowUp"),
    DOWN("ArrowDown"),
    PAGE_UP("PageUp"),
    PAGE_DOWN("PageDown"),
    HOME("Home"),
    END("End"),
    ESCAPE("Escape"),
    TAB("Tab");

    companion object {
        fun fromBrowser(value: String): LabKey? = entries.firstOrNull { it.browserValue == value }
    }
}

/** One identity per optional top-level view. */
enum class CoursePanelId(val htmlId: String) {
    OVERVIEW("view-course-overview"),
    STATION("view-course-station"),
    DOCUMENT("view-course-document"),
    TUTOR("view-course-tutor"),
}
