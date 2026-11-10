package materiewelle.ui.semantics

enum class ArticleReadingId(override val value: String) : ElementId {
    SURFACE("article-reading-surface"), NAVIGATION("article-reading-navigation"),
    FORMULAS("article-reading-formulas"),
}

data class ArticleReadingChoiceId(val index: Int) : ElementId {
    init { require(index in 0..5) }
    override val value = "article-reading-choice-$index"
}

enum class ArticleReadingClass(override val cssName: String) : StyleClass {
    SURFACE("article-reading"), ENHANCED("article-reading-enhanced"),
    NAVIGATION("article-reading-navigation"), LAYOUT("article-reading-layout"),
    STEPS("article-reading-steps"), STEP("article-reading-step"), STEP_CONTENT("article-reading-step-content"),
    LAST_STEP("article-reading-last-step"),
    CURRENT("article-reading-current"), INLINE_FORMULA("article-reading-inline-formula"),
    FORMULAS("article-reading-formulas"), FORMULA_ROW("article-reading-formula-row"),
    FORMULA_CURRENT("article-reading-formula-current"), HYPOTHESIS("article-reading-hypothesis"),
}

enum class ArticleReadingProperty(val cssName: String) {
    TOP("--article-reading-top"), FORMULA_HEIGHT("--article-reading-formula-height"),
}

object ArticleReadingLayout {
    const val MIN_WIDTH = 900
    const val MIN_HEIGHT = 650
}
