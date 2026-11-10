package materiewelle.course

/** Shared questions resolve to distinct constructions, never to interchangeable physical angles. */
enum class DgComparisonFeature(val label: String) {
    REFERENCE("Bezugslinie und Winkel"), DISTANCE("Abstand"), PATH("Gangunterschied");

    val braggFocus: BraggGeometryFocus get() = when (this) {
        REFERENCE -> BraggGeometryFocus.PLANE_AND_ANGLE
        DISTANCE -> BraggGeometryFocus.PLANE_SPACING
        PATH -> BraggGeometryFocus.TWO_EXTRA_PATHS
    }
    val surfaceFocus: SurfaceGeometryFocus get() = when (this) {
        REFERENCE -> SurfaceGeometryFocus.NORMAL_AND_ANGLE
        DISTANCE -> SurfaceGeometryFocus.ROW_SPACING
        PATH -> SurfaceGeometryFocus.OUTGOING_EXTRA_PATH
    }
}

enum class BraggGeometryFocus { PLANE_AND_ANGLE, PLANE_SPACING, TWO_EXTRA_PATHS }
enum class SurfaceGeometryFocus { NORMAL_AND_ANGLE, ROW_SPACING, OUTGOING_EXTRA_PATH }
enum class BraggPresentation { LESSON, COMPARISON }

enum class DgTrialAngle(val key: String, val label: String) {
    UNANSWERED("unanswered", "Bitte selbst zuordnen"),
    PLANE("plane", "Winkel zur betrachteten Ebene"),
    NORMAL("normal", "Winkel zur äußeren Oberflächennormalen");
    companion object { fun fromKey(key: String) = entries.firstOrNull { it.key == key } }
}
enum class DgTrialLength(val key: String, val label: String) {
    UNANSWERED("unanswered", "Bitte selbst zuordnen"),
    PLANES("planes", "Senkrechter Abstand paralleler Netzebenen"),
    ROWS("rows", "Abstand der ausgewählten Oberflächenreihen");
    companion object { fun fromKey(key: String) = entries.firstOrNull { it.key == key } }
}

sealed interface DgTrialFeedback {
    data object Incomplete : DgTrialFeedback
    data class Compared(val angleMatches: Boolean, val lengthMatches: Boolean) : DgTrialFeedback
}

/** A deliberately temporary practice response; it is neither view persistence nor historical work. */
data class DgGeometryTrial(
    val angle: DgTrialAngle = DgTrialAngle.UNANSWERED,
    val length: DgTrialLength = DgTrialLength.UNANSWERED,
    val note: String = "",
    val hintOpened: Boolean = false,
    val comparisonRequested: Boolean = false,
) {
    fun chooseAngle(value: DgTrialAngle) = copy(angle = value, comparisonRequested = false)
    fun chooseLength(value: DgTrialLength) = copy(length = value, comparisonRequested = false)
    fun writeNote(value: String) = copy(note = value.take(MAX_NOTE_LENGTH))
    fun requestHint() = copy(hintOpened = true)
    fun compare() = copy(comparisonRequested = true)

    val feedback: DgTrialFeedback? get() = when {
        !comparisonRequested -> null
        angle == DgTrialAngle.UNANSWERED || length == DgTrialLength.UNANSWERED -> DgTrialFeedback.Incomplete
        else -> DgTrialFeedback.Compared(angle == DgTrialAngle.NORMAL, length == DgTrialLength.ROWS)
    }
    companion object { const val MAX_NOTE_LENGTH = 2000 }
}
