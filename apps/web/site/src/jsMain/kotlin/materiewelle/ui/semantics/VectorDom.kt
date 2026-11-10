package materiewelle.ui.semantics

import kotlinx.browser.document
import materiewelle.ui.vector.*
import org.w3c.dom.DocumentFragment
import org.w3c.dom.Element
import org.w3c.dom.Node

internal const val VECTOR_NS = "http://www.w3.org/2000/svg"

internal fun svgElement(tag: String, vararg attributes: Pair<String, String>): Element =
    document.createElementNS(VECTOR_NS, tag).apply {
        attributes.forEach { (key, value) -> setAttribute(key, value) }
    }

/** Owned geometry reaches the DOM without serialization, XML parsing or innerHTML. */
fun renderVector(fragment: VectorFragment): DocumentFragment =
    document.createDocumentFragment().apply {
        fragment.nodes.forEach { appendChild(renderNode(it)) }
    }

private fun renderNode(node: VectorNode): Node {
    if (node.tag == VectorTag.LABEL) {
        val x = node.attributes[VectorAttribute.X]?.toDoubleOrNull() ?: 0.0
        val y = node.attributes[VectorAttribute.Y]?.toDoubleOrNull() ?: 0.0
        val size = node.attributes[VectorAttribute.FONT_SIZE]?.toDoubleOrNull() ?: 16.0
        val anchor = node.attributes[VectorAttribute.ANCHOR] ?: "start"
        val text = node.text.orEmpty()
        val font = VectorFont.entries.firstOrNull { it.css == node.attributes[VectorAttribute.FONT] }
            ?: VectorFont.TEXT
        return svgElement("g", "data-typst-label" to "true", "role" to "img", "aria-label" to text).apply {
            node.attributes
                .filterKeys {
                    it !in setOf(VectorAttribute.X, VectorAttribute.Y, VectorAttribute.ANCHOR)
                }
                .forEach { (key, value) -> setAttribute(key.xml, value) }
            appendChild(
                svgElement(
                        "g",
                        "transform" to "translate($x $y)",
                        "style" to "color:${node.attributes[VectorAttribute.FILL] ?: "var(--text)"}",
                    )
                    .apply { appendChild(typstSvgLabel(text, size, anchor, font.typeface.css)) }
            )
        }
    }
    return svgElement(node.tag.xml).apply {
        node.attributes.forEach { (key, value) -> setAttribute(key.xml, value) }
        node.text?.let { textContent = it }
        node.children.forEach { appendChild(renderNode(it)) }
    }
}
