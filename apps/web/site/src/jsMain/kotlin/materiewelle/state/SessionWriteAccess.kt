package materiewelle.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Ownership of one origin's personal work. A lease is held for the entire writing session. */
enum class WriteAccess { INITIALIZING, EXCLUSIVE, OTHER_TAB, UNAVAILABLE, RELEASED }

interface SessionWritePermit { val access: WriteAccess }

/** Fail closed unless a caller supplies an acquired exclusive lease. */
object NoSessionWriter : SessionWritePermit { override val access = WriteAccess.UNAVAILABLE }

/** Native boundary: null means another tab owns the lock; a lease is explicitly releasable. */
interface SessionLock {
    fun request(acquired: ((() -> Unit)?) -> Unit, failed: () -> Unit): Boolean
}

/** No queued takeover: a denied/stale tab must copy its work and reload a fresh baseline. */
class SessionWriter(private val lock: SessionLock) : SessionWritePermit {
    override var access by mutableStateOf(WriteAccess.INITIALIZING); private set
    private var started = false
    private var closed = false
    private var release: (() -> Unit)? = null

    fun start() {
        if (started || closed) return
        started = true
        try {
            val available = lock.request(acquired = { lease ->
                if (closed) lease?.invoke()
                else if (lease == null) access = WriteAccess.OTHER_TAB
                else {
                    release = lease
                    access = WriteAccess.EXCLUSIVE
                }
            }, failed = { fail() })
            if (!available) fail()
        } catch (_: Throwable) { fail() }
    }

    private fun fail() {
        if (closed) return
        access = WriteAccess.UNAVAILABLE
        release?.invoke()
        release = null
    }

    fun close() {
        if (closed) return
        closed = true
        access = WriteAccess.RELEASED
        release?.invoke()
        release = null
    }
}

internal val WriteAccess.storageHealth: StorageHealth get() = when (this) {
    WriteAccess.INITIALIZING -> StorageHealth.INITIALIZING
    WriteAccess.EXCLUSIVE -> StorageHealth.SAVED
    WriteAccess.OTHER_TAB -> StorageHealth.OTHER_TAB
    WriteAccess.UNAVAILABLE, WriteAccess.RELEASED -> StorageHealth.MEMORY_ONLY
}

internal val WriteAccess.storageNotice: String get() = when (this) {
    WriteAccess.INITIALIZING -> "Lokale Speicherung wird vorbereitet."
    WriteAccess.EXCLUSIVE -> "Automatisches Speichern auf diesem Gerät ist aktiv."
    WriteAccess.OTHER_TAB -> "Deine Arbeit ist schon in einem anderen Tab geöffnet. Persönliche Antworten und Messungen sind hier schreibgeschützt; Kapitel, Erklärungen und Tutorhilfe kannst du weiter nutzen. Bearbeite deine Arbeit im anderen Tab oder schließe ihn und lade hier den gespeicherten Stand neu."
    WriteAccess.UNAVAILABLE -> "Dieser Browser kann keine sichere Schreibsitzung bereitstellen. Deine Arbeit bleibt nur in diesem Tab. Kopiere sie vor dem Schließen oder Neuladen."
    WriteAccess.RELEASED -> "Die Schreibsitzung ist beendet. Neue Änderungen bleiben nur in diesem Tab. Kopiere sie vor dem Neuladen."
}
