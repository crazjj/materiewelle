package materiewelle.core.course

/** A read-only presence projection. These are neither grades nor persisted workflow states. */
enum class OwnWorkSection { CALCULATION, DG, MEASUREMENT, CONCLUSION }
enum class OwnWorkPresence { OPEN, PARTIAL, PRESENT }
enum class OwnWorkField {
    CALCULATION_ANSWER, CALCULATION_NOTE, DG_PREDICTION, DG_GEOMETRY, DG_INTERPRETATION,
    MEASUREMENT_MARKS, MEASUREMENT_REASON, MEASUREMENT_BINDING, CONCLUSION,
}
enum class OwnWorkNotice {
    IMAGE_UNCHECKED, IMAGE_MISMATCH, IMAGE_MISSING, REASON_OTHER_BAND,
    SELECTION_NOT_SEALED, SAVED_SELECTION_EXISTS, SAVED_COMPARISON_EXISTS, NEEDS_SECOND_MARKER,
}

data class OwnWorkEntry(
    val section: OwnWorkSection,
    val presence: OwnWorkPresence,
    val missing: List<OwnWorkField>,
    val notices: List<OwnWorkNotice> = emptyList(),
    val numericFeedback: NumericAnswerEvaluation? = null,
)

/** Reuse the report's selection of actual work, including missing/incorrect own answers. */
fun PersonalEvidenceReport.ownWorkPresence(): List<OwnWorkEntry> {
    fun entry(
        section: OwnWorkSection,
        fields: List<Pair<OwnWorkField, Boolean>>,
        anyOwnInput: Boolean,
        notices: List<OwnWorkNotice> = emptyList(),
        feedback: NumericAnswerEvaluation? = null,
    ): OwnWorkEntry {
        val missing = fields.filterNot { it.second }.map { it.first }
        return OwnWorkEntry(section, when {
            !anyOwnInput -> OwnWorkPresence.OPEN
            missing.isNotEmpty() -> OwnWorkPresence.PARTIAL
            else -> OwnWorkPresence.PRESENT
        }, missing, notices, feedback)
    }
    val calculation = calculations.firstOrNull { it.role == CalculationRole.REQUIRED }
    val answer = calculation?.answer?.isNotBlank() == true
    val note = notes.any { it.station == StationId.S06_CALCULATION && it.explanation.isNotBlank() }
    val dg = dgComparison
    val own = measurement
    val hasMeasurement = own != null && (
        own.markers.isNotEmpty() || own.justification.isNotBlank() ||
            own.initialSelection != null || own.comparison != null
        )
    val notices = buildList {
        if (hasMeasurement) {
            when (imageBinding) {
                ReportImageBinding.STORED_ONLY -> add(OwnWorkNotice.IMAGE_UNCHECKED)
                ReportImageBinding.MISMATCH -> add(OwnWorkNotice.IMAGE_MISMATCH)
                ReportImageBinding.NO_IMAGE -> add(OwnWorkNotice.IMAGE_MISSING)
                ReportImageBinding.MATCHED -> Unit
            }
            if (own != null) {
                if (own.justification.isNotBlank() && !own.justificationMatchesBand) add(OwnWorkNotice.REASON_OTHER_BAND)
                if (own.markers.size == 1) add(OwnWorkNotice.NEEDS_SECOND_MARKER)
                if (own.initialSelection == null && (own.markers.isNotEmpty() || own.justification.isNotBlank())) add(OwnWorkNotice.SELECTION_NOT_SEALED)
                if (own.initialSelection != null && own.markers.isEmpty()) add(OwnWorkNotice.SAVED_SELECTION_EXISTS)
                if (own.markers.isEmpty() && own.comparison != null) add(OwnWorkNotice.SAVED_COMPARISON_EXISTS)
            }
        }
    }
    return listOf(
        entry(OwnWorkSection.CALCULATION, listOf(
            OwnWorkField.CALCULATION_ANSWER to answer,
            OwnWorkField.CALCULATION_NOTE to note,
        ), answer || note, feedback = calculation?.check),
        entry(OwnWorkSection.DG, listOf(
            OwnWorkField.DG_PREDICTION to (dg?.prediction?.isNotBlank() == true),
            OwnWorkField.DG_GEOMETRY to (dg?.geometricAnswer?.isNotBlank() == true),
            OwnWorkField.DG_INTERPRETATION to (dg?.interpretation?.isNotBlank() == true),
        ), dg?.hasOwnInputs == true),
        entry(OwnWorkSection.MEASUREMENT, listOf(
            OwnWorkField.MEASUREMENT_MARKS to (own != null && own.markers.size >= 2),
            OwnWorkField.MEASUREMENT_REASON to (own != null && own.justification.isNotBlank() && own.justificationMatchesBand),
            OwnWorkField.MEASUREMENT_BINDING to (imageBinding == ReportImageBinding.MATCHED),
        ), hasMeasurement, notices),
        entry(OwnWorkSection.CONCLUSION, listOf(OwnWorkField.CONCLUSION to conclusion.isNotBlank()), conclusion.isNotBlank()),
    )
}
