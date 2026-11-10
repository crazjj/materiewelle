package materiewelle.ui

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import materiewelle.core.LoadState
import materiewelle.data.LabAsset
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.dom.Text as PlainText
import org.jetbrains.compose.web.attributes.*

@Serializable
data class TypesetExpression(val source: String, val light: String, val dark: String,
                             val width: Double, val height: Double, val baseline: Double)
@Serializable
data class TypesetGlyph(val character: String, val id: String, val body: String,
                        val width: Double, val height: Double, val baseline: Double)
@Serializable
data class TypesetCatalogue(val schemaVersion: Int, val compiler: String,
                            val entries: List<TypesetExpression>, val glyphs: List<TypesetGlyph>,
                            val inputs: Map<String, String>) {
    fun validate(): TypesetCatalogue {
        require(schemaVersion == 1 && compiler.startsWith("Typst"))
        require(entries.size in 1..3000 && entries.map { it.source }.distinct().size == entries.size)
        entries.forEach { entry ->
            require(entry.source.isNotBlank() && entry.source.length < 2000)
            require(listOf(entry.width, entry.height, entry.baseline).all { it.isFinite() && it >= 0 })
            require(entry.height > 0 && entry.width < 200 && entry.height < 20 && entry.baseline <= entry.height)
            require(listOf(entry.light, entry.dark).all { Regex("math-text/[a-f0-9]{20}-(light|dark)\\.svg").matches(it) })
        }
        require(glyphs.map { it.character }.distinct().size == glyphs.size && glyphs.size in 10..50)
        require("0123456789.,−-+eE×%° ".all { ch -> glyphs.any { it.character == ch.toString() } })
        require(glyphs.map { it.id }.distinct().size == glyphs.size)
        glyphs.forEach {
            require(it.character.length == 1 && it.id == "typst-number-${it.character.single().code}")
            require(it.body.length < 100_000 && !it.body.contains("<script") && !it.body.contains("<text"))
            require(listOf(it.width, it.height, it.baseline).all { v -> v.isFinite() && v >= 0 })
        }
        return this
    }
}

sealed interface TypesetRun {
    val source: String
    data class Prose(override val source: String) : TypesetRun
    data class Formula(val asset: TypesetExpression, override val source: String) : TypesetRun
    data class Number(override val source: String) : TypesetRun
}

/** Longest reviewed notation wins. Whitespace may vary; source IDs and prose words stay intact. */
class TypesetIndex(val catalogue: TypesetCatalogue) {
    private class Node {
        val children = mutableMapOf<Char, Node>()
        var asset: TypesetExpression? = null
    }
    private val root = Node()
    val glyphs = catalogue.glyphs.associateBy { it.character.single() }
    private val units = catalogue.entries.filter { it.source.startsWith("unit:") }.associateBy { it.source.removePrefix("unit:") }
    init {
        catalogue.entries.filterNot { it.source.startsWith("unit:") || it.source.all { c -> c.isDigit() || c == '.' || c == ',' } }.forEach { asset ->
            var node = root
            asset.source.filterNot(Char::isWhitespace).forEach { ch -> node = node.children.getOrPut(ch) { Node() } }
            // Equivalent whitespace spellings share the same mathematical layout.
            if (node.asset == null) node.asset = asset
        }
    }
    private fun word(c: Char) = c.isLetterOrDigit() || c == '_' || c in '₀'..'₉'
    private val numeric = Regex("[+−-]?[0-9]+(?:[.,][0-9]+)?(?:[eE][+−-]?[0-9]+)?")
    fun runs(text: String): List<TypesetRun> {
        val result = mutableListOf<TypesetRun>(); var i = 0; var prose = 0
        fun flush(end: Int) { if (end > prose) result += TypesetRun.Prose(text.substring(prose, end)) }
        while (i < text.length) {
            if (text[i].isWhitespace()) { i++; continue }
            if (i > 0 && word(text[i - 1]) && word(text[i])) { i++; continue }
            var node = root; var j = i; var best: TypesetExpression? = null; var end = i
            while (j < text.length) {
                if (text[j].isWhitespace()) { j++; continue }
                node = node.children[text[j]] ?: break
                j++
                node.asset?.let { candidate ->
                    if (j == text.length || !word(text[j]) || !word(text[j - 1])) { best = candidate; end = j }
                }
            }
            if (best != null) {
                flush(i)
                val preceding = result.asReversed().firstOrNull { it !is TypesetRun.Prose || it.source.isNotBlank() }
                val asset = if (preceding is TypesetRun.Number) units[best!!.source] ?: best!! else best!!
                result += TypesetRun.Formula(asset, text.substring(i, end)); i = end; prose = i
            } else {
                val number = numeric.find(text, i)?.takeIf { it.range.first == i }
                if (number != null && (number.range.last + 1 == text.length || !word(text[number.range.last + 1]))) {
                    flush(i); result += TypesetRun.Number(number.value); i += number.value.length; prose = i
                } else i++
            }
        }
        flush(text.length)
        return result
    }
}

object TypesetResources {
    var state by mutableStateOf<LoadState<TypesetIndex>>(LoadState.Loading); private set
    private val codec = Json { ignoreUnknownKeys = false }
    private var requested = false
    // The landing page does not mount TypesetGate. Returning to an article must
    // reuse its catalogue rather than removing an already focused reading target.
    fun loadIfNeeded() { if (!requested) load() }
    fun load() {
        requested = true
        state = LoadState.Loading
        window.fetch(LabAsset.INLINE_MATH.path).then { response ->
            if (!response.ok) error("Typst-Formelsatz konnte nicht geladen werden (${response.status}).")
            response.text()
        }.then { text ->
            require(text.length <= 4_000_000)
            val catalogue = codec.decodeFromString<TypesetCatalogue>(text).validate()
            validateTypesetGlyphs(catalogue.glyphs)
            state = LoadState.Ready(TypesetIndex(catalogue))
        }.catch {
            state = LoadState.Failed("Die Formeln sind gerade nicht verfügbar. Versuche es noch einmal.")
        }
    }
    fun ready(): TypesetIndex = (state as LoadState.Ready).value
}

/** The shell stays mounted; scientific content waits without a Unicode-math fallback. */
@Composable
fun TypesetGate(content: @Composable () -> Unit) {
    LaunchedEffect(Unit) { TypesetResources.loadIfNeeded() }
    when (val state = TypesetResources.state) {
        LoadState.Loading -> P(attrs = { elementId(TypesetId.STATUS); role(AriaRole.STATUS) }) { PlainText("Formeln werden geladen …") }
        is LoadState.Failed -> Div(attrs = { elementId(TypesetId.STATUS); styles(LabClass.ERROR); role(AriaRole.ALERT) }) {
            P { PlainText(state.message) }
            Button(attrs = { elementId(TypesetId.RETRY); styles(LabClass.SECONDARY); onClick { TypesetResources.load() } }) { PlainText("Formeln erneut laden") }
        }
        is LoadState.Ready -> {
            TypstNumberDefinitions(state.value.catalogue.glyphs)
            content()
        }
    }
}

/** Visible mathematics consists exclusively of Typst paths, with a separate accessible reading. */
@Composable
fun MathText(text: String) {
    val index = (TypesetResources.state as? LoadState.Ready)?.value
    if (index == null) return
    val runs = remember(text, index) { index.runs(text) }
    runs.forEach { run ->
        when (run) {
            is TypesetRun.Prose -> PlainText(run.source)
            is TypesetRun.Formula -> TypesetInline(run.asset, run.source)
            is TypesetRun.Number -> TypesetNumber(run.source, index)
        }
    }
}

@Composable
private fun TypesetInline(asset: TypesetExpression, source: String) {
    val dark = LocalLabTheme.current.preference == ThemePreference.DARK
    Span(attrs = { typesetInline(asset.width, asset.height, asset.baseline, source) }) {
        Span(attrs = { typesetReading(); ariaHidden(true) }) { PlainText(source) }
        Img(src = if (dark) asset.dark else asset.light, alt = "", attrs = { typesetAsset(dark); ariaHidden(true) })
    }
}

@Composable
private fun TypesetNumber(source: String, index: TypesetIndex) {
    val glyphs = source.map { requireNotNull(index.glyphs[it]) }
    val width = glyphs.sumOf { it.width }; val baseline = glyphs.maxOf { it.baseline }
    val height = baseline + glyphs.maxOf { it.height - it.baseline }
    Span(attrs = { typesetInline(width, height, baseline, source, number = true) }) {
        Span(attrs = { typesetReading(); ariaHidden(true) }) { PlainText(source) }
        TypstNumberSvg(glyphs, width, height, baseline)
    }
}
