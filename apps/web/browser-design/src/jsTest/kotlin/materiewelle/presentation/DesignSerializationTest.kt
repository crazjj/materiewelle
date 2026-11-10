package materiewelle.presentation

import kotlin.test.*
import materiewelle.presentation.styles.*
import org.jetbrains.compose.web.css.*

class DesignSerializationTest {
    @Test
    fun generatedSheetRetainsPrepaintAndMotionContracts() {
        val css = designStyles()
        assertTrue(css.contains("@font-face"))
        assertTrue(css.contains("fonts/lmroman10-regular.woff2"))
        assertTrue(css.contains("fonts/lmroman10-bolditalic.woff2"))
        assertTrue(css.contains("fonts/lmmono10-regular.woff2"))
        assertFalse(css.contains("IBM Plex"))
        assertTrue(css.contains("data-theme=dark"))
        assertTrue(css.contains("prefers-reduced-motion"))
        assertTrue(css.contains("animation-play-state: paused"))
        assertTrue(css.contains("@keyframes lab-wave-travel"))
        assertFalse(css.contains("@import"))
        assertFalse(css.contains("undefined"))
    }

    @Test
    fun fontShorthandPreservesResetAndValidatedValues() {
        assertEquals("500 13px/1.5 var(--font-mono)", FontSpec(13.px, FontStack.MONO, 500, 1.5).css())
        assertFailsWith<IllegalArgumentException> { FontSpec(13.px, FontStack.MONO, 1001) }
        assertFailsWith<IllegalArgumentException> {
            FontSpec(13.px, FontStack.MONO, lineHeight = Double.NaN)
        }
    }

    @Test
    fun gridModelSerializesNestedResponsiveTracks() {
        val value =
            GridTrack.Repeat(3, GridTrack.MinMax(GridTrack.Fixed(0.px), GridTrack.Fraction(1)))
        assertEquals("repeat(3,minmax(0px,1fr))", value.css)
        assertFailsWith<IllegalArgumentException> { GridTrack.Repeat(0, GridTrack.Auto) }
        assertFailsWith<IllegalArgumentException> { GridTrack.Fraction(Double.NaN) }
    }

    @Test
    fun inlineTokenUsesTheActualDomCustomPropertyName() {
        val style = StyleScopeBuilder().apply { setToken(Tokens.MATH_WIDTH, 3.em) }
        assertEquals("--math-width", style.properties.single().name)
        assertEquals("3em", style.properties.single().value.toString())
        assertTrue(style.variables.isEmpty())
    }

    @Test
    fun tokensRetainTheirBrowserIdentity() {
        assertEquals("var(--accent)", Tokens.ACCENT.value().toString())
        assertEquals("var(--math-width)", Tokens.MATH_WIDTH.value().toString())
    }
}
