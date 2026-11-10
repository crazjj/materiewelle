package materiewelle

import kotlin.test.*
import materiewelle.ui.semantics.Id
import materiewelle.ui.vector.*

class VectorContractTest {
    @Test
    fun coordinatesCannotSilentlyInjectInvalidGeometry() {
        assertFailsWith<IllegalArgumentException> { vector { circle { cx(Double.NaN) } } }
        assertFailsWith<IllegalArgumentException> { vector { svg({ viewBox(0, 0, -1, 20) }) {} } }
        assertFailsWith<IllegalArgumentException> { vector { path { geometry("<script/>") } } }
        assertFailsWith<IllegalArgumentException> { vector { path { dash(-1, 2) } } }
    }

    @Test
    fun labelsRemainLiteralDataAndReferencesUseActualIds() {
        val drawing = vector {
            svg({ viewBox(0, 0, 100, 80) }) {
                group({ clip(Id.TEACHING_WAVES) }) { label("<b> & λ") }
            }
        }
        val group = drawing.nodes.single().children.single()
        assertEquals(Id.TEACHING_WAVES.svgReference, group.attributes[VectorAttribute.CLIP])
        assertEquals("<b> & λ", group.children.single().text)
    }

    @Test
    fun grayscaleMappingRejectsOutOfRangeChannels() {
        assertEquals("rgb(0,127,255)", VectorPaint.rgb(0, 127, 255).css)
        assertFailsWith<IllegalArgumentException> { VectorPaint.rgb(0, 0, 256) }
    }

    @Test
    fun unchangedScientificStateHasStableDrawingIdentity() {
        assertEquals(wavelengthChart(150.0), wavelengthChart(150.0))
        assertNotEquals(wavelengthChart(150.0), wavelengthChart(600.0))
    }

    @Test
    fun chartCoordinatesAndMathLabelsSurviveTheTypedBoundary() {
        val drawing = wavelengthChart(150.0, 300.0)
        fun walk(node: VectorNode): List<VectorNode> = listOf(node) + node.children.flatMap(::walk)
        val nodes = drawing.nodes.flatMap(::walk)
        assertTrue(nodes.any { it.tag == VectorTag.PATH })
        val heading = nodes.single { it.text == "Spannung U / V" }
        assertEquals(738.0, heading.attributes[VectorAttribute.X]?.toDouble())
        assertEquals(413.0, heading.attributes[VectorAttribute.Y]?.toDouble())
        val labels = nodes.filter { it.tag == VectorTag.LABEL }
        assertTrue(labels.all { VectorAttribute.X in it.attributes && VectorAttribute.Y in it.attributes })
        assertTrue(nodes.any { it.text?.contains("150,0 V") == true })
    }
}
