package materiewelle.ui.semantics

import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.ui.TypesetResources
import materiewelle.ui.TypesetRun
import materiewelle.presentation.AppTypeface
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.DocumentFragment
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Element
import org.w3c.dom.svg.SVGTextContentElement

private val measurementContext by lazy {
    (document.createElement("canvas") as HTMLCanvasElement).getContext("2d")
        as CanvasRenderingContext2D
}

/** Measured prose and precompiled Typst paths are assembled through native DOM methods. */
internal fun typstSvgLabel(
    text: String,
    size: Double,
    anchor: String = "start",
    font: String = AppTypeface.TEXT.css,
): DocumentFragment {
    val index = TypesetResources.ready()
    val runs = index.runs(text)
    measurementContext.font = "${size}px $font"
    fun width(run: TypesetRun): Double =
        when (run) {
            is TypesetRun.Prose -> measurementContext.measureText(run.source).width
            is TypesetRun.Formula -> run.asset.width * size
            is TypesetRun.Number -> run.source.sumOf { index.glyphs.getValue(it).width } * size
        }
    val total = runs.sumOf(::width)
    var cursor =
        when (anchor) {
            "middle" -> -total / 2
            "end" -> -total
            else -> 0.0
        }
    val line = svgElement("g", "data-typst-line" to anchor)
    line.apply {
        for (run in runs) {
            val x = 0.0
            val runGroup = svgElement(
                "g", "data-typst-run" to width(run).toString(),
                "transform" to "translate($cursor 0)",
            )
            cursor += width(run)
            appendChild(runGroup)
            runGroup.apply {
                when (run) {
                is TypesetRun.Prose ->
                    appendChild(
                        svgElement(
                                "text",
                                "x" to x.toString(),
                                "y" to "0",
                                "text-anchor" to "start",
                                "style" to "white-space:pre",
                            )
                            .apply { textContent = run.source }
                    )
                is TypesetRun.Formula -> {
                    val asset = run.asset
                    appendChild(
                        svgElement(
                                "g",
                                "data-typst-source" to run.source,
                                "data-typst-kind" to "expression",
                                "role" to "img",
                                "aria-label" to run.source,
                            )
                            .apply {
                                for ((dark, path) in
                                    listOf(false to asset.light, true to asset.dark)) {
                                    appendChild(
                                        svgElement(
                                            "image",
                                            "class" to
                                                (if (dark) AdditionalStyleClass.TYPST_LABEL_DARK
                                                    else AdditionalStyleClass.TYPST_LABEL_LIGHT)
                                                    .cssName,
                                            "href" to path,
                                            "x" to x.toString(),
                                            "y" to (-asset.baseline * size).toString(),
                                            "width" to (asset.width * size).toString(),
                                            "height" to (asset.height * size).toString(),
                                            "aria-hidden" to "true",
                                        )
                                    )
                                }
                            }
                    )
                }
                is TypesetRun.Number ->
                    appendChild(
                        svgElement(
                                "g",
                                "data-typst-source" to run.source,
                                "data-typst-kind" to "number",
                                "role" to "img",
                                "aria-label" to run.source,
                            )
                            .apply {
                                var offset = x
                                for (ch in run.source) {
                                    val glyph = index.glyphs.getValue(ch)
                                    appendChild(
                                        svgElement(
                                                "g",
                                                "transform" to
                                                    "translate($offset ${-glyph.baseline * size}) scale(${size / 16})",
                                                "aria-hidden" to "true",
                                            )
                                            .apply {
                                                appendChild(
                                                    svgElement("use", "href" to "#${glyph.id}")
                                                )
                                            }
                                    )
                                    offset += glyph.width * size
                                }
                            }
                    )
                }
            }
        }
    }
    return document.createDocumentFragment().apply { appendChild(line) }
}

/** Measure mounted SVG prose so inherited weight and user text spacing also move adjacent math.
 * Runs once after insertion and once after fonts settle, never on each animation frame.
 */
internal fun layoutTypesetLabels(root: Element) {
    val lines = root.querySelectorAll("[data-typst-line]")
    for (i in 0 until lines.length) {
        val line = lines.item(i) as? Element ?: continue
        val runs = (0 until line.children.length).mapNotNull { line.children.item(it) }
        val widths = runs.map { run ->
            val prose = run.firstElementChild as? SVGTextContentElement
            prose?.getComputedTextLength()?.toDouble()
                ?: run.getAttribute("data-typst-run")?.toDoubleOrNull() ?: 0.0
        }
        var cursor = when (line.getAttribute("data-typst-line")) {
            "middle" -> -widths.sum() / 2
            "end" -> -widths.sum()
            else -> 0.0
        }
        runs.zip(widths).forEach { (run, width) ->
            run.setAttribute("transform", "translate($cursor 0)")
            cursor += width
        }
    }
}

private external interface SvgMetricWindow { val ResizeObserver: Any? }
@JsName("window") private external val svgMetricWindow: SvgMetricWindow
@JsName("ResizeObserver")
private external class SvgMetricObserver(callback: () -> Unit) {
    fun observe(element: Element)
    fun disconnect()
}

/** Only text-size changes (e.g. user spacing) schedule another layout; no perpetual ticker. */
internal fun observeTypesetLabels(root: Element): () -> Unit {
    if (jsTypeOf(svgMetricWindow.ResizeObserver) != "function") return {}
    var frame = 0
    var disposed = false
    val observer = SvgMetricObserver {
        if (!disposed && frame == 0) frame = window.requestAnimationFrame {
            frame = 0
            if (!disposed) layoutTypesetLabels(root)
        }
    }
    val prose = root.querySelectorAll("[data-typst-line] text")
    for (i in 0 until prose.length) (prose.item(i) as? Element)?.let(observer::observe)
    return {
        disposed = true
        observer.disconnect()
        if (frame != 0) window.cancelAnimationFrame(frame)
    }
}
