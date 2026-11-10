package materiewelle.ui

import kotlin.test.*

class TypesetTextTest {
    private fun asset(source: String) = TypesetExpression(source,
        "math-text/${"a".repeat(20)}-light.svg", "math-text/${"a".repeat(20)}-dark.svg", 1.0, 1.0, .8)
    private fun catalogue() = TypesetCatalogue(1, "Typst test fixture",
        listOf("λ", "λNR", "λ = h/p", "p", "p²/(2mₑ)", "mₑ", "m_e", "E_kin = e U", "E_kin,0", "√E_kin", "G", "Å", "unit:Å", "D", "a/√2", "xₙ", "pγ", "=", "≈", "×", "pm", "m", "unit:m").map(::asset),
        "0123456789.,−-+eE×%° ".map {
            TypesetGlyph(it.toString(), "typst-number-${it.code}", "<g/>", .5, 1.0, .8)
        }, emptyMap())
    @Test fun mathematicalRunsAreLongestMatchesAndReconstructExactAuthorText() {
        val index = TypesetIndex(catalogue().validate())
        val text = "Hypothese: λ = h/p. Danach p²/(2mₑ) und a/√2."
        val runs = index.runs(text)
        assertEquals(text, runs.joinToString("") { it.source })
        assertEquals(listOf("λ = h/p", "p²/(2mₑ)", "a/√2"), runs.filterIsInstance<TypesetRun.Formula>().map { it.source })
    }
    @Test fun sourceIdsAndGermanWordsAreNotVariableSubstrings() {
        val text = "Planck, Abstand, A1, D2, T01, Lampen."
        val runs = TypesetIndex(catalogue()).runs(text)
        assertTrue(runs.all { it is TypesetRun.Prose })
        assertEquals(text, runs.joinToString("") { it.source })
    }
    @Test fun liveValuesUseNumberGlyphsWithoutRoundingOrReinterpretingTheValue() {
        val text = "λNR ≈ 5,4847 pm; −1,00e-10"
        val runs = TypesetIndex(catalogue()).runs(text)
        assertEquals(text, runs.joinToString("") { it.source })
        assertEquals(listOf("5,4847", "−1,00e-10"), runs.filterIsInstance<TypesetRun.Number>().map { it.source })
    }
    @Test fun metreAfterLiveQuantityUsesUprightUnitButIndependentMassVariableDoesNot() {
        val index = TypesetIndex(catalogue())
        assertEquals("unit:m", index.runs("2,375 m").filterIsInstance<TypesetRun.Formula>().single().asset.source)
        assertEquals("m", index.runs("Masse m").filterIsInstance<TypesetRun.Formula>().single().asset.source)
    }
    @Test fun magnificationSignImmediatelyAfterADynamicNumberDoesNotStayText() {
        val runs = TypesetIndex(catalogue()).runs("Ansichtsvergrößerung: 2,5× · kein physikalischer Maßstab")
        assertEquals(listOf("×"), runs.filterIsInstance<TypesetRun.Formula>().map { it.source })
        assertEquals(listOf("2,5"), runs.filterIsInstance<TypesetRun.Number>().map { it.source })
    }
    @Test fun asciiIndicesAndAngstromAreTypesetInReadingViewsWithoutChangingTheirSource() {
        val text = "Eigener Wortlaut:\nE_kin = e U; m_e; G; 3,52 Å."
        val runs = TypesetIndex(catalogue()).runs(text)
        assertEquals(text, runs.joinToString("") { it.source })
        assertEquals(listOf("E_kin = e U", "m_e", "G", "Å"), runs.filterIsInstance<TypesetRun.Formula>().map { it.source })
        assertEquals("unit:Å", runs.filterIsInstance<TypesetRun.Formula>().last().asset.source)
        assertTrue(runs.filterIsInstance<TypesetRun.Prose>().none { "E_kin" in it.source || "m_e" in it.source || "Å" in it.source })
    }
    @Test fun indexedPhysicsAliasNeverMatchesAnUnrelatedLongerIdentifier() {
        val text = "metadata E_kinetic, m_example und D2 bleiben unberührt."
        val runs = TypesetIndex(catalogue()).runs(text)
        assertTrue(runs.all { it is TypesetRun.Prose })
        assertEquals(text, runs.joinToString("") { it.source })
    }
    @Test fun kineticEnergyIndexAndRootRemainSingleTypesetQuantities() {
        val text = "Anfangsenergie E_kin,0 und Wurzel √E_kin."
        val runs = TypesetIndex(catalogue()).runs(text)
        assertEquals(text, runs.joinToString("") { it.source })
        assertEquals(listOf("E_kin,0", "√E_kin"), runs.filterIsInstance<TypesetRun.Formula>().map { it.source })
        assertTrue(runs.filterIsInstance<TypesetRun.Prose>().none { "kin" in it.source })
    }
    @Test fun importedAtlasRejectsPathsDimensionsDuplicateIdsAndActiveMarkup() {
        val original = catalogue()
        assertFailsWith<IllegalArgumentException> { original.copy(entries = listOf(asset("λ").copy(light = "https://example.org/x.svg"))).validate() }
        assertFailsWith<IllegalArgumentException> { original.copy(entries = listOf(asset("λ").copy(width = Double.NaN))).validate() }
        assertFailsWith<IllegalArgumentException> { original.copy(glyphs = original.glyphs.map { it.copy(id = "typst-number-0") }).validate() }
        assertFailsWith<IllegalArgumentException> { original.copy(glyphs = original.glyphs.map { it.copy(body = "<script>alert(1)</script>") }).validate() }
        assertFailsWith<IllegalArgumentException> { original.copy(glyphs = original.glyphs.filterNot { it.character == "," }).validate() }
    }
}
