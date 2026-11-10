package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.attr
import com.varabyte.kobweb.compose.ui.modifiers.classNames
import com.varabyte.kobweb.compose.ui.modifiers.id
import materiewelle.presentation.styles.*
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Label
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLabelElement

// Browser standard names and free-form CSS/SVG expressions terminate here.
internal object HtmlAttribute {
    const val ROLE = "role"
    const val ARIA_LABEL = "aria-label"
    const val ARIA_PRESSED = "aria-pressed"
    const val ARIA_CONTROLS = "aria-controls"
    const val ARIA_EXPANDED = "aria-expanded"
    const val ARIA_HIDDEN = "aria-hidden"
    const val ARIA_INVALID = "aria-invalid"
    const val ARIA_MODAL = "aria-modal"
    const val ARIA_LIVE = "aria-live"
    const val ARIA_DESCRIBED_BY = "aria-describedby"
    const val ARIA_LABELLED_BY = "aria-labelledby"
    const val INPUT_MODE = "inputmode"
    const val AUTOCOMPLETE = "autocomplete"
    const val TARGET = "target"
    const val REL = "rel"
    const val STYLE = "style"
    const val MIN = "min"
    const val MAX = "max"
    const val STEP = "step"
    const val ROWS = "rows"
    const val MAX_LENGTH = "maxlength"
    const val TAB_INDEX = "tabindex"
    const val DOWNLOAD = "download"
    const val ACCEPT = "accept"
}

enum class AriaRole(val htmlValue: String) {
    STATUS("status"),
    ALERT("alert"),
    GROUP("group"),
    REGION("region"),
    IMAGE("img"),
    MATH("math"),
    DIALOG("dialog"),
}

enum class LiveMode(val htmlValue: String) {
    POLITE("polite"),
    ASSERTIVE("assertive"),
    OFF("off"),
}

enum class LabInputMode(val htmlValue: String) {
    NUMERIC("numeric"),
    DECIMAL("decimal"),
}

enum class CompletionMode(val htmlValue: String) {
    OFF("off"),
    ON("on"),
}

fun AttrsScope<*>.elementId(id: ElementId) {
    id(id.value)
}

fun Modifier.elementId(id: ElementId): Modifier = id(id.value)

fun AttrsScope<*>.styles(vararg names: LabClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}

fun Modifier.styles(vararg names: LabClass): Modifier =
    classNames(*names.map { it.cssName }.toTypedArray())

fun AttrsScope<*>.role(role: AriaRole) {
    attr(HtmlAttribute.ROLE, role.htmlValue)
}

fun AttrsScope<*>.ariaLabel(text: String) {
    attr(HtmlAttribute.ARIA_LABEL, text)
}

fun AttrsScope<*>.ariaPressed(value: Boolean) {
    attr(HtmlAttribute.ARIA_PRESSED, value.toString())
}

fun Modifier.ariaPressed(value: Boolean): Modifier =
    attr(HtmlAttribute.ARIA_PRESSED, value.toString())

fun AttrsScope<*>.controls(id: ElementId) {
    attr(HtmlAttribute.ARIA_CONTROLS, id.value)
}

fun AttrsScope<*>.ariaExpanded(value: Boolean) {
    attr(HtmlAttribute.ARIA_EXPANDED, value.toString())
}

fun AttrsScope<*>.ariaHidden(value: Boolean) {
    attr(HtmlAttribute.ARIA_HIDDEN, value.toString())
}

fun AttrsScope<*>.ariaInvalid(value: Boolean) {
    attr(HtmlAttribute.ARIA_INVALID, value.toString())
}

fun AttrsScope<*>.ariaModal(value: Boolean) {
    attr(HtmlAttribute.ARIA_MODAL, value.toString())
}

fun AttrsScope<*>.ariaLive(mode: LiveMode) {
    attr(HtmlAttribute.ARIA_LIVE, mode.htmlValue)
}

fun AttrsScope<*>.describedBy(vararg ids: ElementId) {
    require(ids.isNotEmpty())
    attr(HtmlAttribute.ARIA_DESCRIBED_BY, ids.joinToString(" ") { it.value })
}

fun AttrsScope<*>.labelledBy(vararg ids: ElementId) {
    require(ids.isNotEmpty())
    attr(HtmlAttribute.ARIA_LABELLED_BY, ids.joinToString(" ") { it.value })
}

fun AttrsScope<*>.inputMode(mode: LabInputMode) {
    attr(HtmlAttribute.INPUT_MODE, mode.htmlValue)
}

fun AttrsScope<*>.autoComplete(mode: CompletionMode) {
    attr(HtmlAttribute.AUTOCOMPLETE, mode.htmlValue)
}

fun AttrsScope<*>.externalLink() {
    attr(HtmlAttribute.TARGET, "_blank")
    attr(HtmlAttribute.REL, "noopener noreferrer")
}

fun AttrsScope<*>.minimum(value: Number) {
    attr(HtmlAttribute.MIN, value.toString())
}

fun AttrsScope<*>.maximum(value: Number) {
    attr(HtmlAttribute.MAX, value.toString())
}

fun AttrsScope<*>.increment(value: Number) {
    require(value.toDouble().isFinite() && value.toDouble() > 0.0)
    attr(HtmlAttribute.STEP, value.toString())
}

fun AttrsScope<*>.continuousStep() {
    attr(HtmlAttribute.STEP, "any")
}

fun AttrsScope<*>.textRows(count: Int) {
    require(count > 0)
    attr(HtmlAttribute.ROWS, count.toString())
}

fun AttrsScope<*>.maxLength(count: Int) {
    require(count >= 0)
    attr(HtmlAttribute.MAX_LENGTH, count.toString())
}

fun AttrsScope<*>.focusIndex(index: Int) {
    require(index >= -1)
    attr(HtmlAttribute.TAB_INDEX, index.toString())
}

fun AttrsScope<*>.downloadFile(filename: String) {
    attr(HtmlAttribute.DOWNLOAD, filename)
}

fun AttrsScope<*>.acceptProducedAudio() {
    attr(HtmlAttribute.ACCEPT, ".mp3,.wav,audio/mpeg,audio/wav,audio/x-wav")
}

fun AttrsScope<*>.expanded() {
    attr("open", "")
}

/** Typed label reference; the String conversion stays inside this DOM adapter. */
@Composable
fun FieldLabel(
    forId: ElementId,
    attrs: (AttrsScope<HTMLLabelElement>.() -> Unit)? = null,
    content: ContentBuilder<HTMLLabelElement>,
) {
    Label(forId = forId.value, attrs = attrs, content = content)
}

fun Document.findElement(id: ElementId) = getElementById(id.value)

fun Document.findHtmlElement(id: ElementId) = findElement(id) as? HTMLElement

/** Fixed browser selectors; feature code uses identities rather than assembling CSS. */
internal enum class DomQuery(val selector: String) {
    SVG("svg"),
    BUTTON("button"),
    FOCUSABLE("button,input,select,textarea,a[href],summary,[tabindex]:not([tabindex='-1'])"),
}

internal object BrowserMedia {
    const val DARK_SCHEME = "(prefers-color-scheme: dark)"
    const val REDUCED_MOTION = "(prefers-reduced-motion: reduce)"
}
