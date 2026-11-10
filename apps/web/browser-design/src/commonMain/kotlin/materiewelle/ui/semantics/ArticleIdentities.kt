package materiewelle.ui.semantics

enum class ArticleId(override val value: String) : ElementId {
    ROOT("debroglie-article"), TITLE("article-title"), ENTRY("article-einstieg"),
    HISTORY("article-geschichte"), HYPOTHESIS("article-hypothese"),
    DERIVATION("article-herleitung"), CALCULATION("article-rechnung"),
    RECEPTION("article-rezeption"), SOURCES("article-quellen"),
    NEXT("article-next-line"), ALL("article-all-lines"), PLAY("article-play"),
    VOLTAGE("article-voltage"), RANGE("article-voltage-range"), RESULT("article-result"),
    REPLAY("article-replay"), FOLLOW("article-follow"),
    MODEL("article-model"), MODEL_DISCLOSURE("article-model-disclosure"), SKETCH("article-setup"), COMPARE("article-compare"), NOTES("article-notes"),
}
data class ArticleLineId(val index: Int) : ElementId {
    init { require(index >= 0) }
    override val value = "article-line-$index"
}
enum class ArticleClass(override val cssName: String) : StyleClass {
    ROOT("article"), LEAD("article-lead"), EYEBROW("article-eyebrow"),
    GRID("article-grid"), TEXT("article-text"), FIGURE("article-figure"),
    SECTION("article-section"), FORMULA("article-formula"),
    LINE("article-line"), ACTIONS("article-actions"), MODEL("article-model"), MODEL_DISCLOSURE("article-model-disclosure"),
    RESULT("article-result"), RULE("article-rule"), STRIP("article-strip"),
    SOURCE("article-source"), SVG("article-svg"), PLATE("article-plate"),
    BEAM("article-beam"), ELECTRON("article-electron"), WAVE("article-wave"),
    DRAW_LABEL("article-draw-label"), SMALL("article-small"),
    CONTENTS("article-contents"), REFERENCE_TABLE("article-reference-table"),
    CURRENT_LINE("article-current-line"),
    EQUATION("article-equation"), EQUATION_LEFT("article-equation-left"), EQUATION_RELATION("article-equation-relation"),
}
