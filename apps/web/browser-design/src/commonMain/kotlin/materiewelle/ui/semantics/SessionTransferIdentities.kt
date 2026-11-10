package materiewelle.ui.semantics

/** Stable identities for the explicit file preview, replacement and project-only deletion. */
enum class SessionTransferId(override val value: String) : ElementId {
    ROOT("session-transfer"),
    STORED_EXPORT("session-stored-export"),
    DRAFT_EXPORT("session-draft-export"),
    STORED_DRAFT_EXPORT("session-stored-draft-export"),
    RELOAD_CURRENT("session-reload-current"),
    FILE("session-import-file"),
    PREVIEW("session-import-preview"),
    CONFIRM("session-import-confirm"),
    CANCEL("session-import-cancel"),
    STATUS("session-transfer-status"),
    BACKUP("session-import-backup"),
    DELETE_OPEN("session-delete-open"),
    DELETE_CONFIRM("session-delete-confirm"),
    DELETE_CANCEL("session-delete-cancel"),
    RELOAD("session-delete-reload"),
}
