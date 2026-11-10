package materiewelle.ui.semantics

/** Closed identities used by the schematic, buttons and event decoder. */
enum class SetupPart(val key: String, val controlId: Id) {
    SOURCE("source", Id.SETUP_PART_SOURCE),
    SLITS("slits", Id.SETUP_PART_SLITS),
    SCREEN("screen", Id.SETUP_PART_SCREEN);

    companion object {
        fun fromAttribute(value: String?): SetupPart? = entries.firstOrNull { it.key == value }
    }
}
