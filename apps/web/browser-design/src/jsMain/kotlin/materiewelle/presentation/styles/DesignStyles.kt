package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.stringPresentation

typealias StyleRules = GenericStyleSheetBuilder<CSSStyleRuleBuilder>

/** Native Compose CSS rule tree; generated app.css is an output, never a second source of truth. */
fun designStyles(): String =
    StyleSheetBuilderImpl()
        .apply {
            typstTextStyles()
            experimentsStyles()
            jonssonStyles()
            measurementComparisonStyles()
            learningStyles()
            courseStyles()
            themeToggleStyles()
            learningMotionStyles()
            focusStyles()
            opticsWavesStyles()
            geometrySlitsStyles()
            stationActivitiesStyles()
            courseV2Styles()
            learningFlowStyles()
            evidenceReportStyles()
            motionDesignStyles()
            playfulMotionStyles()
            derivationReadingStyles()
            appStyles()
            articleStyles()
            conclusionStyles()
            ownWorkSummaryStyles()
            slitLabStyles()
            slitPhaseStyles()
            slitDetectionStyles()
            derivationPracticeStyles()
            dgLessonStyles()
            dgComparisonStyles()
            compactTutorStyles()
            shellStyles()
            overviewStyles()
            sessionToolsStyles()
            textCopyStyles()
        }
        .cssRules
        .joinToString("\n") { it.stringPresentation() }
