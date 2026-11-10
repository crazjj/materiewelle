package materiewelle.ui.semantics

enum class JonssonArticleId(override val value: String) : ElementId {
    ROOT("joensson-article"), TITLE("joensson-article-title"),
    SETUP("joensson-aufbau"), LIGHT("joensson-lichtvergleich"),
    MODEL("joensson-lehrmodell"), MEASUREMENT("joensson-messung"),
    LIMITS("joensson-grenzen"), SOURCES("joensson-quellen"),
}
