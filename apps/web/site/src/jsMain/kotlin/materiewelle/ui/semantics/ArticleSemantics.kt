package materiewelle.ui.semantics

import androidx.compose.runtime.Composable
import kotlin.math.PI
import kotlin.math.sin
import materiewelle.core.Physics
import materiewelle.core.Voltage
import org.jetbrains.compose.web.attributes.AttrsScope

private val referenceWavelengthPm = Physics.electronWavelength(Voltage(150.0)).picometers
private const val REFERENCE_DRAWING_PERIOD = 90.0

fun AttrsScope<*>.articleStyles(vararg values: ArticleClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** Acceleration is a classical schematic. The lower wave represents wavelength, never a trajectory. */
@Composable
fun ArticleAccelerationDrawing(wavelengthPm: Double, id: ArticleId = ArticleId.MODEL) {
    SvgTag(SvgTagName.SVG, attrs = {
        elementId(id); viewBox(0, 0, 560, 290)
        articleStyles(ArticleClass.SVG); role(AriaRole.IMAGE)
        ariaLabel("Beschleunigung zwischen negativer Quelle und positiver Anode. Darunter ein getrenntes Wellenlängenschema; keine Elektronenbahn.")
    }) {
        ArticleDrawingLine(70, 60, 70, 150, ArticleClass.PLATE)
        ArticleDrawingLine(440, 60, 440, 95, ArticleClass.PLATE)
        ArticleDrawingLine(440, 115, 440, 150, ArticleClass.PLATE)
        ArticleDrawingLine(95, 105, 500, 105, ArticleClass.BEAM)
        ArticleDrawingLine(488, 97, 500, 105, ArticleClass.BEAM)
        ArticleDrawingLine(488, 113, 500, 105, ArticleClass.BEAM)
        SvgTag(SvgTagName.CIRCLE, attrs = {
            svgNumber(SvgNumber.CX, 165); svgNumber(SvgNumber.CY, 105); svgNumber(SvgNumber.RADIUS, 6)
            articleStyles(ArticleClass.ELECTRON)
        })
        ArticleDrawingLabel(70, 38, "Kathode (−)")
        ArticleDrawingLabel(440, 38, "Anode (+)")
        ArticleDrawingLabel(280, 145, "Elektron wird beschleunigt")
        ArticleDrawingLabel(280, 188, "Zugeordnete Wellenlänge · keine Bahn")
        val period = (wavelengthPm / referenceWavelengthPm * REFERENCE_DRAWING_PERIOD).coerceIn(25.0, 260.0)
        val wave = (0..440).joinToString(" ") { i ->
            val x = 60.0 + i
            val y = 234.0 - 16.0 * sin(i / period * 2.0 * PI)
            "${if (i == 0) "M" else "L"}$x,$y"
        }
        SvgTag(SvgTagName.PATH, attrs = { svgPath(wave); articleStyles(ArticleClass.WAVE) })
        ArticleDrawingLabel(280, 280, "Wellenlängenschema, frei gewählter Zeichenmaßstab")
    }
}

@Composable
private fun ArticleDrawingLine(x1: Int, y1: Int, x2: Int, y2: Int, type: ArticleClass) {
    SvgTag(SvgTagName.LINE, attrs = {
        svgNumber(SvgNumber.X1, x1); svgNumber(SvgNumber.Y1, y1)
        svgNumber(SvgNumber.X2, x2); svgNumber(SvgNumber.Y2, y2); articleStyles(type)
    })
}

@Composable
private fun ArticleDrawingLabel(x: Int, y: Int, label: String) {
    TypesetSvgLabel("http://www.w3.org/2000/svg", x.toDouble(), y.toDouble(), attrs = {
        textAnchor(TextAnchor.MIDDLE); articleStyles(ArticleClass.DRAW_LABEL)
    }, text = label)
}
