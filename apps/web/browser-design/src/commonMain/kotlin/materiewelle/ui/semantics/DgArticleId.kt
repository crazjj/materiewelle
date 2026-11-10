package materiewelle.ui.semantics

enum class DgArticleId(override val value: String) : ElementId {
    ROOT("dg-article"), TITLE("dg-article-title"), SETUP("dg-aufbau"),
    OBSERVATION("dg-beobachtung"), BRAGG("dg-bragg"), STRUCTURE("dg-oberflaeche"),
    COMPARISON("dg-vergleich"), SOURCES("dg-quellen"),
    BRAGG_NEXT("dg-bragg-next"), BRAGG_ALL("dg-bragg-all"),
    VOLTAGE("dg-article-voltage"), ANGLE("dg-article-angle"),
    GEOMETRIC("dg-article-geometric"), PREDICTED("dg-article-predicted"),
    DIFFERENCE("dg-article-difference"), MODEL_LIMIT("dg-article-model-limit"), SAVE("dg-article-save"),
    RESET("dg-article-reset"), SAVED("dg-article-saved"), CLEAR("dg-article-clear"),
}
