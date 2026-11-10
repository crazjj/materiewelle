package materiewelle.ui.semantics

/** Fileless local work and its directly reachable rescue action. */
enum class SessionUiId(override val value: String) : ElementId {
    ROOT("session-status"),
    ALERT("session-storage-alert"),
    COPY("session-work-copy"), COPY_STATUS("session-work-copy-status"),
    PREVIEW("session-work-preview"), SELECT("session-work-select"),
    EDITING_BOUNDARY("session-editing-boundary"),
}

enum class SessionUiClass(override val cssName: String) : StyleClass {
    ROOT("session-status"),
    ALERT("session-storage-alert"),
    EDITING_BOUNDARY("session-editing-boundary"),
}
