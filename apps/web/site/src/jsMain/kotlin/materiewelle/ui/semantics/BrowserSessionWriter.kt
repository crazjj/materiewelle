package materiewelle.ui.semantics

import kotlinx.browser.window
import kotlin.js.Promise
import materiewelle.state.SessionLock
import materiewelle.state.SessionWritePermit
import materiewelle.state.SessionWriter
import org.w3c.dom.events.Event

private external interface LockNavigator { val locks: NativeLockManager? }
private external interface NativeLock
private external interface LockOptions { var mode: String; var ifAvailable: Boolean }
private external interface NativeLockManager {
    fun request(name: String, options: LockOptions, callback: (NativeLock?) -> Promise<Unit>): Promise<Unit>
}

/** Web Locks are atomic across cooperating tabs. localStorage flags are deliberately not a fallback. */
private object BrowserSessionLock : SessionLock {
    override fun request(acquired: ((() -> Unit)?) -> Unit, failed: () -> Unit): Boolean {
        val manager = window.navigator.unsafeCast<LockNavigator>().locks ?: return false
        val options = kotlin.js.json().unsafeCast<LockOptions>().apply {
            mode = "exclusive"
            ifAvailable = true
        }
        manager.request("materiewelle.personal-work.writer.v1", options) { lock ->
            if (lock == null) {
                acquired(null)
                Promise.resolve(Unit)
            } else Promise<Unit> { resolve, _ -> acquired { resolve(Unit) } }
        }.catch { failed(); Unit }
        return true
    }
}

/** pagehide releases before bfcache/freeze; return remains memory-only until an explicit fresh reload. */
class BrowserSessionWriter : SessionWritePermit {
    private val writer = SessionWriter(BrowserSessionLock)
    override val access get() = writer.access
    private var listening = false
    private val leave: (Event) -> Unit = { writer.close() }

    fun start() {
        if (!listening) {
            window.addEventListener("pagehide", leave)
            listening = true
        }
        writer.start()
    }

    fun close() {
        if (listening) window.removeEventListener("pagehide", leave)
        listening = false
        writer.close()
    }
}
