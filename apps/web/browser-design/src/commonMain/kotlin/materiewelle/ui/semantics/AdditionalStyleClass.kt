package materiewelle.ui.semantics

/** Shared identities for formula glyphs and small layout helpers. */
enum class AdditionalStyleClass(override val cssName: String) : StyleClass {
    COMPARISON("comparison"),
    INSTRUMENT_LABEL("instrument-label"),
    PREDICTION("prediction"),
    QUIZ_FEEDBACK("quiz-feedback"),
    RANGE_ROW("range-row"),
    SELECTED_ROW("selected-row"),
    SOURCE("source"),
    TABLE_WRAP("table-wrap"),
    TYPST_ASSET("typst-asset"),
    TYPST_DARK("typst-dark"),
    TYPST_INLINE("typst-inline"),
    TYPST_LABEL_DARK("typst-label-dark"),
    TYPST_LABEL_LIGHT("typst-label-light"),
    TYPST_LIGHT("typst-light"),
    TYPST_NUMBER("typst-number"),
    TYPST_NUMBER_DEFINITIONS("typst-number-definitions"),
    TYPST_READING("typst-reading"),
}
