package materiewelle.core

import materiewelle.core.course.DocumentSection
import materiewelle.core.course.DocumentSectionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BrowserDocumentTest {
    private fun document(count: Int) = BrowserDocument(
        title = "Materiewellen · Fachfassung",
        pdf = "learning-docs/arbeitsfassung.pdf",
        pages = (1..count).map { "learning-docs/page-$it.svg" },
        sourceSha256 = "a".repeat(64),
        compiler = "Typst 0.14.0",
        textAlternative = "Aus dieser PDF extrahierter Fachtext.",
        sections = DocumentSectionId.entries.map { DocumentSection(it, it.label, 0) },
    )

    @Test fun acceptsActualVariableDocumentLength() {
        for (count in listOf(1, 2, 13, 18, 100)) {
            assertEquals(count, document(count).validate().pages.size)
        }
    }

    @Test fun rejectsMissingDuplicatedOrUnsafePageResources() {
        assertFailsWith<IllegalArgumentException> { document(0).validate() }
        assertFailsWith<IllegalArgumentException> { document(101).validate() }
        for (pages in listOf(
            listOf("learning-docs/page-2.svg"),
            listOf("learning-docs/page-1.svg", "learning-docs/page-1.svg"),
            listOf("../page-1.svg"),
            listOf("https://example.org/page-1.svg"),
        )) assertFailsWith<IllegalArgumentException> { document(2).copy(pages = pages).validate() }
        for (pdf in listOf("../document.pdf", "https://example.org/document.pdf", "learning-docs/a.svg")) {
            assertFailsWith<IllegalArgumentException> { document(3).copy(pdf = pdf).validate() }
        }
    }

    @Test fun rejectsIncompleteDocumentMetadataAndAlternative() {
        assertFailsWith<IllegalArgumentException> { document(3).copy(title = " ").validate() }
        assertFailsWith<IllegalArgumentException> { document(3).copy(sourceSha256 = "bad").validate() }
        assertFailsWith<IllegalArgumentException> { document(3).copy(compiler = "").validate() }
        assertFailsWith<IllegalArgumentException> { document(3).copy(textAlternative = " ").validate() }
    }

    @Test fun localPageTextMustMatchTheActualCompiledPageCount() {
        assertEquals(listOf("Seite 1", "Seite 2"), document(2).copy(pageText = listOf("Seite 1", "Seite 2")).validate().pageText)
        assertFailsWith<IllegalArgumentException> { document(2).copy(pageText = listOf("Nur eine Seite")).validate() }
        assertFailsWith<IllegalArgumentException> { document(2).copy(pageText = listOf("x".repeat(100001), "")).validate() }
    }

    @Test fun sectionPositionsMustBelongToThisCompiledDocument() {
        val valid = document(13)
        val positions = valid.sections.mapIndexed { index, section -> section.copy(pageIndex = index) }
        assertEquals(12, valid.copy(sections = positions).validate().sections.last().pageIndex)
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = emptyList()).validate() }
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = positions.dropLast(1)).validate() }
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = positions + positions.first()).validate() }
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = positions.reversed()).validate() }
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = positions.map { it.copy(pageIndex = 13) }).validate() }
        assertFailsWith<IllegalArgumentException> { valid.copy(sections = positions.map { it.copy(title = " ") }).validate() }
    }
}
