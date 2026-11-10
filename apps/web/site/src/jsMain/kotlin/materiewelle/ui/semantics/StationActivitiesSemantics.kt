package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.dom.Text

/** Station-only identities, independent of translated labels and the position of DOM elements. */
fun AttrsScope<*>.stationActivityStyles(vararg values: StationActivityClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** The native table attribute spelling stays at the DOM boundary. */
fun AttrsScope<*>.activityColumnHeading() {
    attr("scope", "col")
}

private const val ACTIVITY_SVG_NAMESPACE = "http://www.w3.org/2000/svg"

@Composable
fun ActivitySvg(
    id: ElementId,
    width: Double,
    height: Double,
    label: String,
    content: @Composable () -> Unit,
) {
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            elementId(id)
            viewBox(0, 0, width, height)
            role(AriaRole.IMAGE)
            ariaLabel(label)
            stationActivityStyles(StationActivityClass.SVG)
        },
    ) {
        SvgTag(SvgTagName.TITLE) { Text(label) }
        content()
    }
}

@Composable
fun ActivityRect(
    x: Double,
    y: Double,
    width: Double,
    height: Double,
    vararg styles: StationActivityClass,
) {
    SvgTag(
        SvgTagName.RECT,
        attrs = {
            svgNumber(SvgNumber.X, x)
            svgNumber(SvgNumber.Y, y)
            svgNumber(SvgNumber.WIDTH, width)
            svgNumber(SvgNumber.HEIGHT, height)
            stationActivityStyles(*styles)
        },
    )
}

@Composable
fun ActivityLine(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    vararg styles: StationActivityClass,
) {
    SvgTag(
        SvgTagName.LINE,
        attrs = {
            svgNumber(SvgNumber.X1, x1)
            svgNumber(SvgNumber.Y1, y1)
            svgNumber(SvgNumber.X2, x2)
            svgNumber(SvgNumber.Y2, y2)
            stationActivityStyles(*styles)
        },
    )
}

@Composable
fun ActivityPath(path: String, vararg styles: StationActivityClass) {
    SvgTag(
        SvgTagName.PATH,
        attrs = {
            svgPath(path)
            stationActivityStyles(*styles)
        },
    )
}

@Composable
fun ActivityText(x: Double, y: Double, text: String, anchor: TextAnchor = TextAnchor.START) {
    TypesetSvgLabel(
        namespace = ACTIVITY_SVG_NAMESPACE,
        x = x,
        y = y,
        attrs = {
            textAnchor(anchor)
            stationActivityStyles(StationActivityClass.DRAWING_LABEL)
        },
        text = text,
    )
}
