package materiewelle.ui.semantics

import kotlin.js.Promise
import kotlinx.browser.window
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.dom.Element
import org.w3c.dom.Range

private external interface ServiceWindow {
    val navigator: ServiceNavigator
    val crypto: ServiceCrypto?
    fun getSelection(): BrowserSelection?
}

/** Missing from the pinned DOM bindings; only the Selection API actually used here. */
private external interface BrowserSelection {
    val isCollapsed: Boolean
    fun removeAllRanges()
    fun addRange(range: Range)
}

private external interface ServiceNavigator {
    val clipboard: BrowserClipboard?
}

private external interface BrowserClipboard {
    fun writeText(text: String): Promise<Unit>
}

private external interface ServiceCrypto {
    val subtle: BrowserSubtle?
}

private external interface BrowserSubtle {
    fun digest(algorithm: String, bytes: Uint8Array): Promise<ArrayBuffer>
}

private external class TextEncoder {
    fun encode(text: String): Uint8Array
}

/** Preserve the native promise so permission failures cannot report false success. */
fun copyBrowserText(text: String): Promise<Unit> =
    try {
        requireNotNull(window.unsafeCast<ServiceWindow>().navigator.clipboard).writeText(text)
    } catch (error: dynamic) {
        Promise.reject(error)
    }

/** Manual fallback works without Clipboard API permission and keeps the exact payload visible. */
fun selectBrowserText(id: ElementId): Boolean {
    val element = document.findHtmlElement(id) ?: return false
    val selection = window.unsafeCast<ServiceWindow>().getSelection() ?: return false
    element.focus()
    val range = document.createRange()
    range.selectNodeContents(element)
    selection.removeAllRanges()
    selection.addRange(range)
    return !selection.isCollapsed
}

fun hasBrowserDigest(): Boolean = window.unsafeCast<ServiceWindow>().crypto?.subtle != null

fun browserTextDigest(text: String): Promise<String> =
    requireNotNull(window.unsafeCast<ServiceWindow>().crypto?.subtle)
        .digest("SHA-256", TextEncoder().encode(text))
        .then { buffer ->
            val bytes = Uint8Array(buffer)
            (0 until bytes.length).joinToString("") { index ->
                (bytes[index].toInt() and 255).toString(16).padStart(2, '0')
            }
        }

/** A native dictionary object, assembled by Kotlin rather than a JavaScript snippet. */
fun Element.scrollIntoViewCentered() {
    scrollIntoView(kotlin.js.json("block" to "center", "behavior" to "instant"))
}
