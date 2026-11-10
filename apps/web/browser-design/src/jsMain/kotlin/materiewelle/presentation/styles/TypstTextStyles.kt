@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.typstTextStyles() {
    style(c(AdditionalStyleClass.TYPST_INLINE)) {
        display(DisplayStyle.InlineBlock)
        position(Position.Relative)
        composite(CssProperty.VERTICAL_ALIGN, "calc(-1 * var(--math-descent))")
        width(Tokens.MATH_WIDTH.value())
        height(Tokens.MATH_HEIGHT.value())
        maxWidth((100).percent)
        setOverflowX(OverflowXValue.AUTO)
        setOverflowY(OverflowYValue.HIDDEN)
        setScrollbarWidth(ScrollbarWidthValue.THIN)
        set(CssProperty.LINE_HEIGHT, 0)
    }
    style(anyOf(c(AdditionalStyleClass.TYPST_ASSET), c(AdditionalStyleClass.TYPST_NUMBER))) {
        important { display(DisplayStyle.InlineBlock) }
        important { width(Tokens.MATH_WIDTH.value()) }
        important { height(Tokens.MATH_HEIGHT.value()) }
        important { setMaxWidth(MaxWidthValue.NONE) }
        setVerticalAlign(VerticalAlignValue.TOP)
        important { margin(0.px) }
        important { composite(CssProperty.BORDER, "0") }
    }
    style(allOf(c(AdditionalStyleClass.TYPST_ASSET), c(AdditionalStyleClass.TYPST_DARK))) {
        important { display(DisplayStyle.None) }
    }
    style(
        desc(
            allOf(
                t(SelectorTag.HTML),
                attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
            ),
            allOf(c(AdditionalStyleClass.TYPST_ASSET), c(AdditionalStyleClass.TYPST_LIGHT)),
        )
    ) {
        important { display(DisplayStyle.None) }
    }
    style(
        desc(
            allOf(
                t(SelectorTag.HTML),
                attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
            ),
            allOf(c(AdditionalStyleClass.TYPST_ASSET), c(AdditionalStyleClass.TYPST_DARK)),
        )
    ) {
        important { display(DisplayStyle.InlineBlock) }
    }
    style(c(AdditionalStyleClass.TYPST_READING)) {
        important { position(Position.Absolute) }
        important { width((1).px) }
        important { height((1).px) }
        important { padding(0.px) }
        important { margin((-1).px) }
        important { setOverflow(OverflowValue.HIDDEN) }
        important { composite(CssProperty.CLIP_PATH, "inset(50%)") }
        important { setWhiteSpace(WhiteSpaceValue.NOWRAP) }
        important { composite(CssProperty.BORDER, "0") }
    }
    style(c(AdditionalStyleClass.TYPST_NUMBER_DEFINITIONS)) {
        position(Position.Absolute)
        width(0.px)
        height(0.px)
        setOverflow(OverflowValue.HIDDEN)
        setPointerEvents(PointerEventsValue.NONE)
    }
    style(c(AdditionalStyleClass.TYPST_NUMBER)) {
        setColor(ColorValue.INHERIT)
        set(CssProperty.FILL, Color.currentColor)
    }
    style(desc(t(SelectorTag.SVG), c(AdditionalStyleClass.TYPST_LABEL_LIGHT))) {
        display(DisplayStyle.Inline)
    }
    style(desc(t(SelectorTag.SVG), c(AdditionalStyleClass.TYPST_LABEL_DARK))) {
        display(DisplayStyle.None)
    }
    style(
        desc(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
                ),
                t(SelectorTag.SVG),
            ),
            c(AdditionalStyleClass.TYPST_LABEL_LIGHT),
        )
    ) {
        display(DisplayStyle.None)
    }
    style(
        desc(
            desc(
                allOf(
                    t(SelectorTag.HTML),
                    attribute(SelectorAttribute.DATA_THEME, AttributeValue.DARK),
                ),
                t(SelectorTag.SVG),
            ),
            c(AdditionalStyleClass.TYPST_LABEL_DARK),
        )
    ) {
        display(DisplayStyle.Inline)
    }
}
