package materiewelle.course

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import materiewelle.state.DgViewDraft

/** View preferences and an explicitly temporary practice outlive the article in this tab. */
class DgLessonView(initial: DgViewDraft = DgViewDraft()) {
    var step by mutableStateOf(initial.step)
    var showAll by mutableStateOf(initial.showAll)
    var setupStage by mutableStateOf(initial.setupStage)
    var braggStage by mutableStateOf(initial.braggStage)
    var showPrediction by mutableStateOf(initial.showPrediction)
    var showGeometry by mutableStateOf(initial.showGeometry)

    // Not included in DgViewDraft: a reload discards this optional pilot, never historical answers.
    var comparisonFeature by mutableStateOf<DgComparisonFeature?>(null)
    var geometryTrialOpen by mutableStateOf(false)
    var geometryTrial by mutableStateOf(DgGeometryTrial())

    private var geometryTrialFocusPending = false

    fun openGeometryTrial() {
        geometryTrialFocusPending = true
        comparisonFeature = null
        geometryTrialOpen = true
    }
    fun closeGeometryTrial() {
        geometryTrialFocusPending = true
        geometryTrialOpen = false
    }
    fun takeGeometryTrialFocus(): Boolean = geometryTrialFocusPending.also { geometryTrialFocusPending = false }
    fun resetGeometryTrial() { geometryTrial = DgGeometryTrial() }

    fun draft() = DgViewDraft(step, showAll, setupStage, braggStage, showPrediction, showGeometry)
}
