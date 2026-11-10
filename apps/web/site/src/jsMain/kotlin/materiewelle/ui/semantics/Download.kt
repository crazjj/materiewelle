package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.dom.url.URL

enum class TextDownloadType(val mediaType: String) { JSON("application/json;charset=utf-8"), TEXT("text/plain;charset=utf-8") }

/** Object URL lifetime is bounded; filenames are controlled by the caller, never source HTML. */
fun downloadText(content: String, filename: String, type: TextDownloadType = TextDownloadType.TEXT) {
    require(Regex("[A-Za-z0-9._-]+").matches(filename))
    val url = URL.createObjectURL(Blob(arrayOf(content), BlobPropertyBag(type = type.mediaType)))
    val link = document.createElement("a") as HTMLAnchorElement
    link.href = url; link.download = filename
    document.body?.appendChild(link); link.click(); link.remove()
    window.setTimeout({ URL.revokeObjectURL(url) }, 1000)
}
