package materiewelle.ui.semantics

import kotlin.js.Promise
import materiewelle.presentation.AppTypeface

private external interface BrowserFontFace
private external interface BrowserFontFaceSet {
    fun load(font: String): Promise<Array<BrowserFontFace>>
}
private external interface DocumentWithFonts {
    val fonts: BrowserFontFaceSet?
}
@JsName("document") private external val fontDocument: DocumentWithFonts

private enum class FontMetricsState { UNREQUESTED, LOADING, SETTLED }
private var fontMetricsState = FontMetricsState.UNREQUESTED
private val pendingFontLayouts = mutableSetOf<() -> Unit>()

/** One cold-load correction for SVG prose advances; font failures retain readable fallbacks. */
internal fun afterFontMetricsReady(layout: () -> Unit): () -> Unit {
    if (fontMetricsState == FontMetricsState.SETTLED) return {}
    pendingFontLayouts.add(layout)
    if (fontMetricsState == FontMetricsState.UNREQUESTED) {
        fontMetricsState = FontMetricsState.LOADING
        fun settled() {
            fontMetricsState = FontMetricsState.SETTLED
            val layouts = pendingFontLayouts.toList()
            pendingFontLayouts.clear()
            layouts.forEach { it() }
        }
        val fonts = fontDocument.fonts
        if (fonts == null) settled()
        else fonts.load("16px '${AppTypeface.TEXT.family}'").then {
            fonts.load("16px '${AppTypeface.MONO.family}'")
        }.then { settled() }.catch { settled() }
    }
    return { pendingFontLayouts.remove(layout); Unit }
}
