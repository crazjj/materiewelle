package materiewelle.ui.semantics

/** Reading and optional precision tools never own or rewrite the scientific selection. */
enum class MeasurementGuidanceId(override val value: String) : ElementId {
    COORDINATE_TOOLS("measurement-coordinate-tools"),
    RESULT_METHOD("measurement-result-method"),
    INTERMEDIATE_MAXIMA("measurement-intermediate-maxima"),
}
