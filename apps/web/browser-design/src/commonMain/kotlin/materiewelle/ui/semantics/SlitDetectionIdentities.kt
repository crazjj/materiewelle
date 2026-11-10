package materiewelle.ui.semantics

enum class SlitDetectionId(override val value: String) : ElementId {
    PANEL("slit-detection-panel"), CANVAS("slit-detection-canvas"), HISTOGRAM("slit-detection-histogram"),
    COUNT("slit-detection-count"), ONE("slit-detection-one"), HUNDRED("slit-detection-hundred"),
    THOUSAND("slit-detection-thousand"), PLAY("slit-detection-play"), NEW("slit-detection-new"),
    REPLAY("slit-detection-replay"), NOTICE("slit-detection-notice"), DATA("slit-detection-data"),
    SEED("slit-detection-seed"), TABLE("slit-detection-table"), BODY("slit-detection-body"),
}

data class SlitDetectionBinId(val index: Int) : ElementId {
    init { require(index in 0..199) }
    override val value: String get() = "slit-detection-bin-$index"
}

enum class SlitDetectionClass(override val cssName: String) : StyleClass {
    ROOT("slit-detection"), ACTIONS("slit-detection-actions"), CANVAS("slit-detection-canvas"),
    FIGURE("slit-detection-figure"), CAPTION("slit-detection-caption"), TABLE("slit-detection-table"),
    TABLE_SCROLL("slit-detection-table-scroll"), COUNT("slit-detection-count"),
}
