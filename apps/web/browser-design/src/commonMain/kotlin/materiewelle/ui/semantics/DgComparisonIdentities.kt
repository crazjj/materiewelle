package materiewelle.ui.semantics

enum class DgComparisonId(override val value: String) : ElementId {
    ROOT("dg-geometry-comparison"),
    REFERENCE("dg-comparison-reference"), DISTANCE("dg-comparison-distance"), PATH("dg-comparison-path"),
    BRAGG("dg-comparison-bragg"), SURFACE("dg-comparison-surface"),
    BRAGG_HIGHLIGHT("dg-comparison-bragg-highlight"), SURFACE_HIGHLIGHT("dg-comparison-surface-highlight"),
    BRAGG_FORMULA("dg-comparison-bragg-formula"), SURFACE_FORMULA("dg-comparison-surface-formula"),
    TRIAL("dg-geometry-trial"), TRIAL_TITLE("dg-trial-title"), START("dg-trial-start"),
    ANGLE("dg-trial-angle"), LENGTH("dg-trial-length"), NOTE("dg-trial-note"),
    HINT("dg-trial-hint"), COMPARE("dg-trial-compare"), FEEDBACK("dg-trial-feedback"),
    CLOSE("dg-trial-close"), RESET("dg-trial-reset"),
}

enum class DgComparisonClass(override val cssName: String) : StyleClass {
    ROOT("dg-geometry-comparison"), CONTROLS("dg-comparison-controls"), FIGURES("dg-comparison-figures"),
    FIGURE("dg-comparison-figure"), DRAWING("dg-comparison-drawing"), ACTIVE("dg-comparison-active"),
    HIGHLIGHT("dg-comparison-highlight"), CAPTION("dg-comparison-caption"),
    FORMULA("dg-comparison-formula"), TRIAL("dg-geometry-trial"), FIELDS("dg-trial-fields"),
    FIELD("dg-trial-field"), FEEDBACK("dg-trial-feedback"),
}
