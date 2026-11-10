@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.*

/** Canonical Kotlin style definitions, using shared DOM identities and typed values. */
internal fun StyleRules.themeToggleStyles() {
    style(c(LabClass.THEME_CHOOSER)) {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        flexShrink(0)
    }
    style(desc(c(LabClass.THEME_CHOOSER), i(Id.THEME_PREFERENCE))) {
        display(DisplayStyle.LegacyInlineFlex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        gap((9).px)
        minHeight((44).px)
        minWidth((116).px)
        padding((7).px, (11).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((8).px)
        background(Tokens.SURFACE_RAISED.value())
        color(Tokens.TEXT.value())
        font(FontSpec((12).px, FontStack.TEXT, weight = 500))
        setWhiteSpace(WhiteSpaceValue.NOWRAP)
        setCursor(CursorValue.POINTER)
        setTouchAction(TouchActionValue.MANIPULATION)
    }
    style(
        desc(c(LabClass.THEME_CHOOSER), allOf(i(Id.THEME_PREFERENCE), pseudo(SelectorPseudo.HOVER)))
    ) {
        borderColor(Tokens.ACCENT.value())
        background(Tokens.ACCENT_SOFT.value())
    }
    style(
        desc(
            c(LabClass.THEME_CHOOSER),
            allOf(i(Id.THEME_PREFERENCE), pseudo(SelectorPseudo.ACTIVE)),
        )
    ) {
        transform { translateY((1).px) }
    }
    style(
        desc(
            c(LabClass.THEME_CHOOSER),
            allOf(i(Id.THEME_PREFERENCE), pseudo(SelectorPseudo.FOCUS_VISIBLE)),
        )
    ) {
        edge(CssProperty.OUTLINE, (3).px, LineStyle.Solid, Tokens.FOCUS.value())
        outlineOffset((3).px)
    }
    style(c(LabClass.THEME_TOGGLE_TRACK)) {
        display(DisplayStyle.Block)
        position(Position.Relative)
        width((38).px)
        height((24).px)
        border((1).px, LineStyle.Solid, Tokens.BORDER.value())
        borderRadius((999).px)
        background(Tokens.BG.value())
        flexShrink(0)
        setBoxSizing(BoxSizingValue.BORDER_BOX)
    }
    style(c(LabClass.THEME_TOGGLE_THUMB)) {
        position(Position.Absolute)
        display(DisplayStyle.Grid)
        setPlaceItems(PlaceItemsValue.CENTER)
        set(CssProperty.TOP, (2).px)
        set(CssProperty.LEFT, (2).px)
        width((18).px)
        height((18).px)
        borderRadius((50).percent)
        background(Tokens.ACCENT.value())
        color(Tokens.BG.value())
        fontSize((15).px)
        set(CssProperty.LINE_HEIGHT, 1)
        transform { translateX(0.px) }
        composite(CssProperty.BOX_SHADOW, "0 1px 3px #0002")
    }
    style(
        desc(
            allOf(
                i(Id.THEME_PREFERENCE),
                attribute(SelectorAttribute.ARIA_PRESSED, AttributeValue.TRUE),
            ),
            c(LabClass.THEME_TOGGLE_THUMB),
        )
    ) {
        transform {
            translateX((14).px)
            rotate((-16).deg)
        }
    }
    style(c(LabClass.THEME_TOGGLE_LABEL)) {
        display(DisplayStyle.Block)
        minWidth((42).px)
        setTextAlign(TextAlignValue.LEFT)
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_NO_PREFERENCE.query) {
        style(c(LabClass.THEME_TOGGLE_THUMB)) {
            composite(
                CssProperty.TRANSITION,
                "transform .22s cubic-bezier(.2,.8,.2,1),background-color .18s ease",
            )
        }
        style(
            anyOf(
                c(LabClass.THEME_TOGGLE_TRACK),
                desc(c(LabClass.THEME_CHOOSER), i(Id.THEME_PREFERENCE)),
            )
        ) {
            composite(CssProperty.TRANSITION, "background-color .18s ease,border-color .18s ease")
        }
    }
    media(DesignMedia.PREFERS_REDUCED_MOTION_REDUCE.query) {
        style(
            anyOf(
                c(LabClass.THEME_TOGGLE_THUMB),
                c(LabClass.THEME_TOGGLE_TRACK),
                desc(c(LabClass.THEME_CHOOSER), i(Id.THEME_PREFERENCE)),
            )
        ) {
            important { setAnimation(AnimationValue.NONE) }
            important { setTransition(TransitionValue.NONE) }
        }
    }
}
