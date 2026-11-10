package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.MeasurementSession
import materiewelle.core.NativePixel
import materiewelle.presentation.styles.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

import materiewelle.state.MeasurementOwner

/** Editing text is allowed to be incomplete; accepted coordinates remain integer image pixels. */
internal fun parseNativeCoordinate(raw: String, data: JonssonData): NativePixel? =
    raw.trim()
        .replace(',', '.')
        .toDoubleOrNull()
        ?.takeIf {
            it.isFinite() &&
                it % 1.0 == 0.0 &&
                it >= data.cropLeft &&
                it < data.cropLeft + data.cropWidth
        }
        ?.let(::NativePixel)

internal fun parseMarkerCoordinate(
    raw: String,
    positions: List<Double>,
    index: Int,
    data: JonssonData,
): NativePixel? {
    if (index !in positions.indices) return null
    return parseNativeCoordinate(raw, data)?.takeIf {
        PixelMeasurement.canMove(
            positions,
            index,
            it.value - positions[index],
            data.cropLeft,
            data.cropLeft + data.cropWidth,
        )
    }
}

internal enum class NewMarkerIssue { EMPTY, INVALID_COORDINATE, DUPLICATE, LIMIT_REACHED }

internal fun newMarkerIssue(raw: String, measurement: MeasurementSession, data: JonssonData): NewMarkerIssue? {
    if (measurement.markers.size >= MeasurementSession.MAX_MARKERS) return NewMarkerIssue.LIMIT_REACHED
    if (raw.isBlank()) return NewMarkerIssue.EMPTY
    val coordinate = parseNativeCoordinate(raw, data) ?: return NewMarkerIssue.INVALID_COORDINATE
    return if (measurement.markers.any { it.xPx == coordinate.value.toInt() }) NewMarkerIssue.DUPLICATE else null
}

/** Coordinates and gestures mutate only the accepted measurement owned by LabSession. */
internal class MeasurementEditor(val data: JonssonData, private val owner: MeasurementOwner, val view: JonssonUiState) {
    private var selected by view.selected
    private var markerText by view.markerText
    private var editingMarkerText by view.editingMarkerText
    private var newMarkerText by view.newMarkerText
    private var sampleX by view.sampleX
    private var showMarkers by view.showMarkers
    private val undo = mutableStateListOf<MeasurementSession>()
    private var gesture: MeasurementSession? = null
    val canEditMeasurement: Boolean get() = owner.canEditMeasurement
    val canUndo: Boolean get() = canEditMeasurement && undo.isNotEmpty()
    val measurement: MeasurementSession get() = owner.measurement
    val referenceVisible: Boolean get() = view.referenceVisible(measurement)
    val validCoordinate: NativePixel? get() = parseMarkerCoordinate(markerText, measurement.markers.map { it.xPx.toDouble() }, selected, data)
    val newCoordinateIssue: NewMarkerIssue? get() = newMarkerIssue(newMarkerText, measurement, data)
    private fun editableChanged(a: MeasurementSession, b: MeasurementSession) = a.markers != b.markers || a.justification != b.justification || a.band != b.band || a.justificationBand != b.justificationBand
    private fun rememberEdit(before: MeasurementSession, after: MeasurementSession) {
        if (editableChanged(before, after)) { if (undo.size == 20) undo.removeAt(0); undo.add(before) }
    }
    fun update(transform: (MeasurementSession) -> MeasurementSession): MeasurementSession {
        if (!canEditMeasurement) return owner.measurement
        val before = owner.measurement
        owner.updateMeasurement(transform)
        if (gesture == null) rememberEdit(before, owner.measurement)
        return owner.measurement
    }
    fun interaction(active: Boolean) {
        if (!canEditMeasurement) { gesture = null; return }
        if (active) gesture = owner.measurement
        else { gesture?.let { rememberEdit(it, owner.measurement) }; gesture = null }
    }
    fun undoEdit() {
        if (!canEditMeasurement) return
        val before = undo.removeLastOrNull() ?: return
        owner.updateMeasurement { current ->
            current.copy(band = before.band, markers = before.markers, justification = before.justification,
                justificationBand = before.justificationBand)
        }
        selected = selected.coerceAtMost(owner.measurement.markers.lastIndex).coerceAtLeast(0)
        markerText = owner.measurement.markers.getOrNull(selected)?.xPx?.toString().orEmpty()
        editingMarkerText = false
        owner.measurement.markers.getOrNull(selected)?.let {
            sampleX = NativePixel(it.xPx.toDouble())
            view.pinnedSampleX.value = sampleX
        }
    }
    fun commitMarker(index: Int, coordinate: NativePixel, normalizeText: Boolean) {
        if (!canEditMeasurement) return
        var accepted = false
        update { latest ->
            val positions = latest.markers.map { it.xPx.toDouble() }
            if (
                index in positions.indices &&
                    PixelMeasurement.canMove(
                        positions,
                        index,
                        coordinate.value - positions[index],
                        data.cropLeft,
                        data.cropLeft + data.cropWidth,
                    )
            ) {
                accepted = true
                latest.moveMarker(index, coordinate.value.toInt(), data)
            } else latest
        }
        if (accepted) {
            if (normalizeText) {
                markerText = coordinate.value.toInt().toString()
                editingMarkerText = false
            }
            sampleX = coordinate
            view.pinnedSampleX.value = coordinate
            showMarkers = true
        }
    }
    fun setMarker(index: Int, coordinate: NativePixel) = commitMarker(index, coordinate, true)
    fun selectMarker(index: Int) {
        val positions = owner.measurement.markers
        if (index !in positions.indices) return
        selected = index
        markerText = positions[index].xPx.toString()
        editingMarkerText = false
        sampleX = NativePixel(positions[index].xPx.toDouble())
        view.pinnedSampleX.value = sampleX
        showMarkers = true
    }
    fun addMarker(point: NativePixel) {
        if (!canEditMeasurement) return
        val x = point.value.toInt()
        var accepted = false
        val next = update { latest ->
            if (
                latest.markers.size < MeasurementSession.MAX_MARKERS &&
                    latest.markers.none { it.xPx == x } &&
                    x >= data.cropLeft &&
                    x < data.cropLeft + data.cropWidth
            ) {
                accepted = true
                latest.addMarker(x, data)
            } else latest
        }
        if (accepted) {
            selected = next.markers.indexOfFirst { it.xPx == x }
            markerText = x.toString()
            editingMarkerText = false
            sampleX = NativePixel(x.toDouble())
            view.pinnedSampleX.value = sampleX
            showMarkers = true
        }
    }
    fun nudge(delta: Double) {
        if (!canEditMeasurement) return
        val index = selected
        val next = update { latest ->
            val positions = latest.markers.map { it.xPx.toDouble() }
            if (
                index in positions.indices &&
                    PixelMeasurement.canMove(
                        positions,
                        index,
                        delta,
                        data.cropLeft,
                        data.cropLeft + data.cropWidth,
                    )
            )
                latest.moveMarker(index, (positions[index] + delta).toInt(), data)
            else latest
        }
        next.markers.getOrNull(index)?.let { marker ->
            markerText = marker.xPx.toString()
            editingMarkerText = false
            sampleX = NativePixel(marker.xPx.toDouble())
            view.pinnedSampleX.value = sampleX
            showMarkers = true
        }
    }
}
