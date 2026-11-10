package materiewelle.ui.semantics

import kotlinx.browser.window
import materiewelle.core.SlitDetectionRun
import materiewelle.core.SlitDetectionDistribution
import materiewelle.presentation.styles.Tokens
import org.jetbrains.compose.web.attributes.AttrsScope
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

fun AttrsScope<*>.slitDetectionStyles(vararg values: SlitDetectionClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** One bounded raster, with drawing offsets explicitly unrelated to a physical second coordinate. */
class SlitDetectionCanvasPainter {
    private var previousSeed: Int? = null
    private var previousCount = 0
    private var previousDistribution: SlitDetectionDistribution? = null
    private var previousColors = ""

    fun render(canvas: HTMLCanvasElement, run: SlitDetectionRun) {
        val context = canvas.getContext("2d") as? CanvasRenderingContext2D ?: return
        val ratio = window.devicePixelRatio.coerceIn(1.0, 2.0)
        val pixelWidth = (WIDTH * ratio).toInt()
        val pixelHeight = (HEIGHT * ratio).toInt()
        val resized = canvas.width != pixelWidth || canvas.height != pixelHeight
        if (resized) { canvas.width = pixelWidth; canvas.height = pixelHeight }
        val style = window.getComputedStyle(canvas)
        val background = style.getPropertyValue("--${Tokens.SURFACE.name}").trim()
        val foreground = style.getPropertyValue("--${Tokens.ACCENT.name}").trim()
        val colors = "$background|$foreground"
        val redraw = resized || previousSeed != run.seed || previousDistribution !== run.distribution ||
            previousCount > run.count || previousColors != colors
        context.setTransform(ratio, 0.0, 0.0, ratio, 0.0, 0.0)
        if (redraw) {
            context.fillStyle = background
            context.fillRect(0.0, 0.0, WIDTH, HEIGHT)
        }
        context.fillStyle = foreground
        val span = run.distribution.upper.meters - run.distribution.lower.meters
        for (index in (if (redraw) 0 else previousCount) until run.count) {
            val fraction = (run.hits[index].meters - run.distribution.lower.meters) / span
            // A deterministic vertical drawing offset prevents all one-dimensional hits overlapping.
            // This offset does not sample a two-dimensional quantum model or consume its RNG stream.
            val offset = (index * 0.6180339887498949 + (run.seed % 1009) / 1009.0) % 1.0
            val x = LEFT + fraction * (RIGHT - LEFT)
            val y = 6.0 + offset * (HEIGHT - 12.0)
            context.fillRect(x - 1.0, y - 1.0, 2.0, 2.0)
        }
        previousSeed = run.seed
        previousCount = run.count
        previousDistribution = run.distribution
        previousColors = colors
    }

    companion object {
        const val WIDTH = 1000.0; const val HEIGHT = 160.0
        const val LEFT = 56.0; const val RIGHT = 980.0
    }
}
