package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.dom.GenericTag
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.svg.SVGElement

/** Missing Compose HTML 1.8 semantic builders terminate at this typed boundary. */
@Composable
fun Figure(attrs: AttrsScope<HTMLElement>.() -> Unit = {}, content: @Composable () -> Unit = {}) {
    GenericTag<HTMLElement>("figure", attrs = attrs) { content() }
}

@Composable
fun Figcaption(attrs: AttrsScope<HTMLElement>.() -> Unit = {}, content: @Composable () -> Unit = {}) {
    GenericTag<HTMLElement>("figcaption", attrs = attrs) { content() }
}

@Composable
fun Details(attrs: AttrsScope<HTMLElement>.() -> Unit = {}, content: @Composable () -> Unit = {}) {
    GenericTag<HTMLElement>("details", attrs = attrs) { content() }
}

/** Keep a native details disclosure in its owner's state without replacing its keyboard behavior. */
fun AttrsScope<HTMLElement>.detailsState(isOpen: Boolean, onChange: (Boolean) -> Unit) {
    if (isOpen) expanded()
    ref { element ->
        val listener: (Event) -> Unit = { onChange(element.hasAttribute("open")) }
        element.addEventListener("toggle", listener)
        onDispose { element.removeEventListener("toggle", listener) }
    }
}

@Composable
fun Summary(attrs: AttrsScope<HTMLElement>.() -> Unit = {}, content: @Composable () -> Unit = {}) {
    GenericTag<HTMLElement>("summary", attrs = attrs) { content() }
}

@Composable
fun Strong(attrs: AttrsScope<HTMLElement>.() -> Unit = {}, content: @Composable () -> Unit = {}) {
    GenericTag<HTMLElement>("strong", attrs = attrs) { content() }
}

enum class SvgTagName(internal val xml: String) {
    SVG("svg"),
    LINE("line"),
    PATH("path"),
    RECT("rect"),
    IMAGE("image"),
    CIRCLE("circle"),
    TITLE("title"),
    GROUP("g"),
    USE("use"),
}

/** Protocol spellings are isolated; all call sites select a closed Kotlin tag identity. */
@Composable
fun SvgTag(
    tag: SvgTagName,
    attrs: AttrsScope<SVGElement>.() -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    GenericTag<SVGElement>(tag.xml, namespace = VECTOR_NS, attrs = attrs) { content() }
}
