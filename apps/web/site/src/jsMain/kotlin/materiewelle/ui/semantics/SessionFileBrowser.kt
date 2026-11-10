package materiewelle.ui.semantics

import materiewelle.core.course.SessionSnapshot
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader

fun AttrsScope<HTMLInputElement>.acceptSessionFile() {
    attr("accept", ".json,application/json")
}

/** Bounded local read only; return a cancellation action for replacement/unmount. */
fun readSessionFile(file: File, deliver: (String?) -> Unit): () -> Unit {
    if (file.size.toDouble() <= 0 || file.size.toDouble() > SessionSnapshot.MAX_JSON_LENGTH * 4.0) {
        deliver(null)
        return {}
    }
    val reader = FileReader()
    reader.onload = {
        deliver((reader.result as? String)?.takeIf { it.length <= SessionSnapshot.MAX_JSON_LENGTH })
        null
    }
    reader.onerror = { deliver(null); null }
    try { reader.readAsText(file, "UTF-8") } catch (_: Throwable) { deliver(null) }
    return {
        reader.onload = null
        reader.onerror = null
        runCatching { reader.abort() }
        Unit
    }
}
