package materiewelle.pages

import androidx.compose.runtime.mutableStateOf
import materiewelle.JonssonData
import materiewelle.core.ExperimentMode
import materiewelle.core.MeasurementSession
import materiewelle.core.NativePixel
import materiewelle.state.JonssonViewDraft

/** View state belongs to the section owner, not to an accidentally mounted DOM tree.
 * Scientific markers and provenance remain in MeasurementSession. DOM nodes and
 * active pointer gestures deliberately do not survive unmounting.
 */
class JonssonUiState(draft: JonssonViewDraft = JonssonViewDraft()) {
    val exploration = materiewelle.course.graphics.SlitExplorationUiState(draft.slit)
    internal val slitComparisonVisible = mutableStateOf(true)
    internal val mode = mutableStateOf(ExperimentMode.LAB)
    internal val step = mutableStateOf(0)
    internal val showMarkers = mutableStateOf(draft.showMarkers)
    internal val readOnlyReferenceOpen = mutableStateOf(false)
    internal val zoom = mutableStateOf(draft.zoom)
    internal val wholeImage = mutableStateOf(draft.wholeImage)
    internal val imageScrollLeft = mutableStateOf(draft.scrollLeft)
    internal val selected = mutableStateOf(draft.selected)
    internal val markerText = mutableStateOf(draft.coordinate)
    internal val editingMarkerText = mutableStateOf(draft.editingCoordinate)
    internal val newMarkerText = mutableStateOf(draft.newCoordinate)
    internal val sampleX = mutableStateOf(draft.sampleX?.let(::NativePixel))
    internal val pinnedSampleX = mutableStateOf(draft.sampleX?.let(::NativePixel))
    internal val modelVoltage = mutableStateOf(150.0)
    internal val slitWidthUm = mutableStateOf(0.5)
    internal val slitSpacingUm = mutableStateOf(2.0)
    internal val selectedOpticsStage = mutableStateOf(draft.opticsStage)
    private var imageHash: String? = draft.imageHash

    fun draft() = JonssonViewDraft(imageHash, zoom.value, wholeImage.value, imageScrollLeft.value,
        selected.value, showMarkers.value, markerText.value, editingMarkerText.value,
        newMarkerText.value, pinnedSampleX.value?.value, selectedOpticsStage.value, exploration.draft())

    internal fun referenceVisible(measurement: MeasurementSession): Boolean =
        measurement.referenceViewed || readOnlyReferenceOpen.value

    internal fun bindImage(data: JonssonData) {
        if (imageHash == data.imageSha256) return
        readOnlyReferenceOpen.value = false
        if (imageHash != null) {
            selected.value = 0
            markerText.value = ""
            editingMarkerText.value = false
            newMarkerText.value = ""
            sampleX.value = null
            pinnedSampleX.value = null
            zoom.value = 1.0
            wholeImage.value = false
            imageScrollLeft.value = 0.0
        }
        slitSpacingUm.value = data.slitSpacingM * 1e6
        imageHash = data.imageSha256
    }
}
