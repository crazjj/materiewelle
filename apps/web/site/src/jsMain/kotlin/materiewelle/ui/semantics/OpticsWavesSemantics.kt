package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import materiewelle.core.course.OpticsStage
import materiewelle.presentation.styles.*
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text

/** Closed identities for the two classical/optics investigations. */
fun AttrsScope<*>.opticsWavesStyles(vararg names: OpticsWavesClass) {
    classes(*names.map { it.cssName }.toTypedArray())
}

fun AttrsScope<*>.opticsStage(stage: OpticsStage) {
    attr("data-optics-stage", stage.name)
}

fun AttrsScope<*>.waveSample(first: Double, second: Double, sum: Double, amplitude: Double) {
    require(listOf(first, second, sum, amplitude).all { it.isFinite() })
    attr("data-first", first.toString())
    attr("data-second", second.toString())
    attr("data-sum", sum.toString())
    attr("data-resultant-amplitude", amplitude.toString())
}

fun AttrsScope<*>.waveOffset(value: Double) {
    require(value.isFinite())
    attr("data-path-wavelengths", value.toString())
}

fun AttrsScope<*>.reproductionScale(value: Double) {
    require(value.isFinite() && value in 1.0..3.0)
    style { setToken(Tokens.REPRODUCTION_SCALE, value) }
}

/** All String-only SVG spellings are isolated here; feature code uses numeric geometry. */
private const val INQUIRY_SVG_NS = "http://www.w3.org/2000/svg"

@Composable
fun InquirySvg(
    id: ElementId,
    width: Double,
    height: Double,
    label: String,
    vararg classes: OpticsWavesClass,
    content: @Composable () -> Unit,
) {
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            elementId(id)
            viewBox(0, 0, width, height)
            role(AriaRole.IMAGE)
            ariaLabel(label)
            opticsWavesStyles(OpticsWavesClass.DIAGRAM, *classes)
        },
    ) {
        SvgTag(SvgTagName.TITLE) { Text(label) }
        content()
    }
}

@Composable
fun InquiryLine(x1: Double, y1: Double, x2: Double, y2: Double, vararg classes: OpticsWavesClass) {
    SvgTag(
        SvgTagName.LINE,
        attrs = {
            svgNumber(SvgNumber.X1, x1)
            svgNumber(SvgNumber.Y1, y1)
            svgNumber(SvgNumber.X2, x2)
            svgNumber(SvgNumber.Y2, y2)
            opticsWavesStyles(*classes)
        },
    )
}

@Composable
fun InquiryPath(path: String, vararg classes: OpticsWavesClass) {
    SvgTag(
        SvgTagName.PATH,
        attrs = {
            svgPath(path)
            opticsWavesStyles(*classes)
        },
    )
}

@Composable
fun InquiryRect(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    vararg classes: OpticsWavesClass,
) {
    SvgTag(
        SvgTagName.RECT,
        attrs = {
            svgNumber(SvgNumber.X, x)
            svgNumber(SvgNumber.Y, y)
            svgNumber(SvgNumber.WIDTH, width)
            svgNumber(SvgNumber.HEIGHT, height)
            opticsWavesStyles(*classes)
        },
    )
}

@Composable
fun InquiryCircle(x: Double, y: Double, radius: Double, vararg classes: OpticsWavesClass) {
    SvgTag(
        SvgTagName.CIRCLE,
        attrs = {
            svgNumber(SvgNumber.CX, x)
            svgNumber(SvgNumber.CY, y)
            svgNumber(SvgNumber.RADIUS, radius)
            opticsWavesStyles(*classes)
        },
    )
}

@Composable
fun InquiryText(
    x: Double,
    y: Double,
    text: String,
    anchor: TextAnchor = TextAnchor.START,
    vararg classes: OpticsWavesClass,
) {
    TypesetSvgLabel(
        namespace = INQUIRY_SVG_NS,
        x = x,
        y = y,
        attrs = {
            textAnchor(anchor)
            opticsWavesStyles(OpticsWavesClass.TEXT, *classes)
        },
        text = text,
        inheritColor = true,
    )
}

@Composable
fun InquiryGroup(active: Boolean, content: @Composable () -> Unit) {
    SvgTag(
        SvgTagName.GROUP,
        attrs = {
            opticsWavesStyles(OpticsWavesClass.PART)
            if (active) opticsWavesStyles(OpticsWavesClass.ACTIVE_PART)
        },
    ) {
        content()
    }
}
