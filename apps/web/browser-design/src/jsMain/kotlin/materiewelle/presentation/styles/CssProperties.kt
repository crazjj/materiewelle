package materiewelle.presentation.styles

import org.jetbrains.compose.web.css.*

/**
 * CSS spellings terminate here. Definition files use native DSL APIs or closed property identities.
 */
enum class CssProperty(val key: String) {
    WEBKIT_TAP_HIGHLIGHT_COLOR("-webkit-tap-highlight-color"),
    ACCENT_COLOR("accent-color"),
    ALIGN_ITEMS("align-items"),
    ALIGN_SELF("align-self"),
    ANIMATION("animation"),
    ANIMATION_DELAY("animation-delay"),
    ANIMATION_DURATION("animation-duration"),
    ANIMATION_PLAY_STATE("animation-play-state"),
    BACKGROUND("background"),
    BORDER("border"),
    BORDER_BLOCK("border-block"),
    BORDER_BOTTOM("border-bottom"),
    BORDER_COLLAPSE("border-collapse"),
    BORDER_COLOR("border-color"),
    BORDER_LEFT("border-left"),
    BORDER_RADIUS("border-radius"),
    BORDER_TOP("border-top"),
    BORDER_TOP_STYLE("border-top-style"),
    BORDER_WIDTH("border-width"),
    BOTTOM("bottom"),
    BOX_SHADOW("box-shadow"),
    BOX_SIZING("box-sizing"),
    CLIP_PATH("clip-path"),
    COLOR("color"),
    COLOR_SCHEME("color-scheme"),
    COLUMN_GAP("column-gap"),
    CONTENT("content"),
    CURSOR("cursor"),
    DISPLAY("display"),
    FILL("fill"),
    FILL_OPACITY("fill-opacity"),
    FILTER("filter"),
    FLEX("flex"),
    FLEX_BASIS("flex-basis"),
    FLEX_DIRECTION("flex-direction"),
    FLEX_SHRINK("flex-shrink"),
    FLEX_WRAP("flex-wrap"),
    FONT("font"),
    FONT_FAMILY("font-family"),
    FONT_SIZE("font-size"),
    FONT_STYLE("font-style"),
    FONT_VARIANT_NUMERIC("font-variant-numeric"),
    FONT_WEIGHT("font-weight"),
    GAP("gap"),
    GRID_COLUMN("grid-column"),
    GRID_ROW("grid-row"),
    GRID_TEMPLATE_COLUMNS("grid-template-columns"),
    HEIGHT("height"),
    IMAGE_RENDERING("image-rendering"),
    INSET("inset"),
    ISOLATION("isolation"),
    JUSTIFY_CONTENT("justify-content"),
    JUSTIFY_SELF("justify-self"),
    LEFT("left"),
    LETTER_SPACING("letter-spacing"),
    LINE_HEIGHT("line-height"),
    LIST_STYLE("list-style"),
    MARGIN("margin"),
    MARGIN_BOTTOM("margin-bottom"),
    MARGIN_INLINE("margin-inline"),
    MARGIN_LEFT("margin-left"),
    MARGIN_RIGHT("margin-right"),
    MARGIN_TOP("margin-top"),
    MAX_HEIGHT("max-height"),
    MAX_WIDTH("max-width"),
    MIN_HEIGHT("min-height"),
    MIN_WIDTH("min-width"),
    OBJECT_FIT("object-fit"),
    OBJECT_POSITION("object-position"),
    OPACITY("opacity"),
    ORDER("order"),
    OUTLINE("outline"),
    OUTLINE_COLOR("outline-color"),
    OUTLINE_OFFSET("outline-offset"),
    OVERFLOW("overflow"),
    OVERFLOW_WRAP("overflow-wrap"),
    OVERFLOW_X("overflow-x"),
    OVERFLOW_Y("overflow-y"),
    OVERSCROLL_BEHAVIOR("overscroll-behavior"),
    OVERSCROLL_BEHAVIOR_INLINE("overscroll-behavior-inline"),
    OVERSCROLL_BEHAVIOR_X("overscroll-behavior-x"),
    PADDING("padding"),
    PADDING_BOTTOM("padding-bottom"),
    PADDING_LEFT("padding-left"),
    PADDING_RIGHT("padding-right"),
    PADDING_TOP("padding-top"),
    PAINT_ORDER("paint-order"),
    PLACE_ITEMS("place-items"),
    POINTER_EVENTS("pointer-events"),
    POSITION("position"),
    RESIZE("resize"),
    RIGHT("right"),
    SCROLL_BEHAVIOR("scroll-behavior"),
    SCROLL_MARGIN_TOP("scroll-margin-top"),
    SCROLL_PADDING_TOP("scroll-padding-top"),
    SCROLLBAR_COLOR("scrollbar-color"),
    SCROLLBAR_WIDTH("scrollbar-width"),
    STROKE("stroke"),
    STROKE_DASHARRAY("stroke-dasharray"),
    STROKE_DASHOFFSET("stroke-dashoffset"),
    STROKE_LINECAP("stroke-linecap"),
    STROKE_LINEJOIN("stroke-linejoin"),
    STROKE_WIDTH("stroke-width"),
    TEXT_ALIGN("text-align"),
    TEXT_DECORATION("text-decoration"),
    TEXT_SHADOW("text-shadow"),
    TEXT_TRANSFORM("text-transform"),
    TEXT_UNDERLINE_OFFSET("text-underline-offset"),
    TOP("top"),
    TOUCH_ACTION("touch-action"),
    TRANSFORM("transform"),
    TRANSFORM_BOX("transform-box"),
    TRANSFORM_ORIGIN("transform-origin"),
    TRANSITION("transition"),
    VECTOR_EFFECT("vector-effect"),
    VERTICAL_ALIGN("vertical-align"),
    WHITE_SPACE("white-space"),
    WIDTH("width"),
    Z_INDEX("z-index"),
}

fun StyleScope.set(key: CssProperty, value: StylePropertyValue) {
    property(key.key, value)
}

fun StyleScope.set(key: CssProperty, value: Number) {
    property(key.key, value)
}

/** Narrow compatibility escape for compound grammar not exposed by Compose HTML 1.8. */
internal fun StyleScope.composite(key: CssProperty, expression: String) {
    require(expression.isNotBlank() && expression.none { it == ';' || it == '{' || it == '}' })
    property(key.key, expression)
}

enum class WebkitTapHighlightColorValue(val css: String) {
    TRANSPARENT("transparent")
}

fun StyleScope.setWebkitTapHighlightColor(value: WebkitTapHighlightColorValue) {
    property(CssProperty.WEBKIT_TAP_HIGHLIGHT_COLOR.key, value.css)
}

enum class AnimationValue(val css: String) {
    NONE("none")
}

fun StyleScope.setAnimation(value: AnimationValue) {
    property(CssProperty.ANIMATION.key, value.css)
}

enum class AnimationPlayStateValue(val css: String) {
    PAUSED("paused")
}

fun StyleScope.setAnimationPlayState(value: AnimationPlayStateValue) {
    property(CssProperty.ANIMATION_PLAY_STATE.key, value.css)
}

enum class BorderCollapseValue(val css: String) {
    COLLAPSE("collapse")
}

fun StyleScope.setBorderCollapse(value: BorderCollapseValue) {
    property(CssProperty.BORDER_COLLAPSE.key, value.css)
}

enum class BorderTopStyleValue(val css: String) {
    DASHED("dashed")
}

fun StyleScope.setBorderTopStyle(value: BorderTopStyleValue) {
    property(CssProperty.BORDER_TOP_STYLE.key, value.css)
}

enum class BoxShadowValue(val css: String) {
    NONE("none")
}

fun StyleScope.setBoxShadow(value: BoxShadowValue) {
    property(CssProperty.BOX_SHADOW.key, value.css)
}

enum class BoxSizingValue(val css: String) {
    BORDER_BOX("border-box")
}

fun StyleScope.setBoxSizing(value: BoxSizingValue) {
    property(CssProperty.BOX_SIZING.key, value.css)
}

enum class ClipPathValue(val css: String) {
    NONE("none")
}

fun StyleScope.setClipPath(value: ClipPathValue) {
    property(CssProperty.CLIP_PATH.key, value.css)
}

enum class ColorValue(val css: String) {
    INHERIT("inherit")
}

fun StyleScope.setColor(value: ColorValue) {
    property(CssProperty.COLOR.key, value.css)
}

enum class ColorSchemeValue(val css: String) {
    DARK("dark"),
    LIGHT("light"),
}

fun StyleScope.setColorScheme(value: ColorSchemeValue) {
    property(CssProperty.COLOR_SCHEME.key, value.css)
}

enum class CursorValue(val css: String) {
    CROSSHAIR("crosshair"),
    NOT_ALLOWED("not-allowed"),
    POINTER("pointer"),
}

fun StyleScope.setCursor(value: CursorValue) {
    property(CssProperty.CURSOR.key, value.css)
}

enum class FillValue(val css: String) {
    NONE("none")
}

fun StyleScope.setFill(value: FillValue) {
    property(CssProperty.FILL.key, value.css)
}

enum class FilterValue(val css: String) {
    NONE("none")
}

fun StyleScope.setFilter(value: FilterValue) {
    property(CssProperty.FILTER.key, value.css)
}

enum class FontValue(val css: String) {
    INHERIT("inherit")
}

fun StyleScope.setFont(value: FontValue) {
    property(CssProperty.FONT.key, value.css)
}

enum class FontStyleValue(val css: String) {
    ITALIC("italic"),
    NORMAL("normal"),
}

fun StyleScope.setFontStyle(value: FontStyleValue) {
    property(CssProperty.FONT_STYLE.key, value.css)
}

enum class FontVariantNumericValue(val css: String) {
    TABULAR_NUMS("tabular-nums")
}

fun StyleScope.setFontVariantNumeric(value: FontVariantNumericValue) {
    property(CssProperty.FONT_VARIANT_NUMERIC.key, value.css)
}

enum class ImageRenderingValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setImageRendering(value: ImageRenderingValue) {
    property(CssProperty.IMAGE_RENDERING.key, value.css)
}

enum class IsolationValue(val css: String) {
    ISOLATE("isolate")
}

fun StyleScope.setIsolation(value: IsolationValue) {
    property(CssProperty.ISOLATION.key, value.css)
}

enum class JustifySelfValue(val css: String) {
    CENTER("center")
}

fun StyleScope.setJustifySelf(value: JustifySelfValue) {
    property(CssProperty.JUSTIFY_SELF.key, value.css)
}

enum class ListStyleValue(val css: String) {
    NONE("none")
}

fun StyleScope.setListStyle(value: ListStyleValue) {
    property(CssProperty.LIST_STYLE.key, value.css)
}

enum class MarginInlineValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setMarginInline(value: MarginInlineValue) {
    property(CssProperty.MARGIN_INLINE.key, value.css)
}

enum class MarginLeftValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setMarginLeft(value: MarginLeftValue) {
    property(CssProperty.MARGIN_LEFT.key, value.css)
}

enum class MarginRightValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setMarginRight(value: MarginRightValue) {
    property(CssProperty.MARGIN_RIGHT.key, value.css)
}

enum class MaxWidthValue(val css: String) {
    NONE("none")
}

fun StyleScope.setMaxWidth(value: MaxWidthValue) {
    property(CssProperty.MAX_WIDTH.key, value.css)
}

enum class ObjectFitValue(val css: String) {
    CONTAIN("contain")
}

fun StyleScope.setObjectFit(value: ObjectFitValue) {
    property(CssProperty.OBJECT_FIT.key, value.css)
}

enum class OrderValue(val css: String) {
    INITIAL("initial")
}

fun StyleScope.setOrder(value: OrderValue) {
    property(CssProperty.ORDER.key, value.css)
}

enum class OverflowValue(val css: String) {
    AUTO("auto"),
    HIDDEN("hidden"),
    VISIBLE("visible"),
}

fun StyleScope.setOverflow(value: OverflowValue) {
    property(CssProperty.OVERFLOW.key, value.css)
}

enum class OverflowWrapValue(val css: String) {
    ANYWHERE("anywhere")
}

fun StyleScope.setOverflowWrap(value: OverflowWrapValue) {
    property(CssProperty.OVERFLOW_WRAP.key, value.css)
}

enum class OverflowXValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setOverflowX(value: OverflowXValue) {
    property(CssProperty.OVERFLOW_X.key, value.css)
}

enum class OverflowYValue(val css: String) {
    HIDDEN("hidden")
}

fun StyleScope.setOverflowY(value: OverflowYValue) {
    property(CssProperty.OVERFLOW_Y.key, value.css)
}

enum class OverscrollBehaviorValue(val css: String) {
    CONTAIN("contain")
}

fun StyleScope.setOverscrollBehavior(value: OverscrollBehaviorValue) {
    property(CssProperty.OVERSCROLL_BEHAVIOR.key, value.css)
}

enum class OverscrollBehaviorInlineValue(val css: String) {
    CONTAIN("contain")
}

fun StyleScope.setOverscrollBehaviorInline(value: OverscrollBehaviorInlineValue) {
    property(CssProperty.OVERSCROLL_BEHAVIOR_INLINE.key, value.css)
}

enum class OverscrollBehaviorXValue(val css: String) {
    CONTAIN("contain")
}

fun StyleScope.setOverscrollBehaviorX(value: OverscrollBehaviorXValue) {
    property(CssProperty.OVERSCROLL_BEHAVIOR_X.key, value.css)
}

enum class PlaceItemsValue(val css: String) {
    CENTER("center")
}

fun StyleScope.setPlaceItems(value: PlaceItemsValue) {
    property(CssProperty.PLACE_ITEMS.key, value.css)
}

enum class PointerEventsValue(val css: String) {
    NONE("none")
}

fun StyleScope.setPointerEvents(value: PointerEventsValue) {
    property(CssProperty.POINTER_EVENTS.key, value.css)
}

enum class ResizeValue(val css: String) {
    VERTICAL("vertical")
}

fun StyleScope.setResize(value: ResizeValue) {
    property(CssProperty.RESIZE.key, value.css)
}

enum class ScrollBehaviorValue(val css: String) {
    AUTO("auto")
}

fun StyleScope.setScrollBehavior(value: ScrollBehaviorValue) {
    property(CssProperty.SCROLL_BEHAVIOR.key, value.css)
}

enum class ScrollbarWidthValue(val css: String) {
    THIN("thin")
}

fun StyleScope.setScrollbarWidth(value: ScrollbarWidthValue) {
    property(CssProperty.SCROLLBAR_WIDTH.key, value.css)
}

enum class StrokeValue(val css: String) {
    NONE("none")
}

fun StyleScope.setStroke(value: StrokeValue) {
    property(CssProperty.STROKE.key, value.css)
}

enum class StrokeDasharrayValue(val css: String) {
    NONE("none")
}

fun StyleScope.setStrokeDasharray(value: StrokeDasharrayValue) {
    property(CssProperty.STROKE_DASHARRAY.key, value.css)
}

enum class StrokeLinecapValue(val css: String) {
    ROUND("round")
}

fun StyleScope.setStrokeLinecap(value: StrokeLinecapValue) {
    property(CssProperty.STROKE_LINECAP.key, value.css)
}

enum class StrokeLinejoinValue(val css: String) {
    ROUND("round")
}

fun StyleScope.setStrokeLinejoin(value: StrokeLinejoinValue) {
    property(CssProperty.STROKE_LINEJOIN.key, value.css)
}

enum class TextAlignValue(val css: String) {
    CENTER("center"),
    LEFT("left"),
    RIGHT("right"),
}

fun StyleScope.setTextAlign(value: TextAlignValue) {
    property(CssProperty.TEXT_ALIGN.key, value.css)
}

enum class TextDecorationValue(val css: String) {
    NONE("none")
}

fun StyleScope.setTextDecoration(value: TextDecorationValue) {
    property(CssProperty.TEXT_DECORATION.key, value.css)
}

enum class TextTransformValue(val css: String) {
    UPPERCASE("uppercase")
}

fun StyleScope.setTextTransform(value: TextTransformValue) {
    property(CssProperty.TEXT_TRANSFORM.key, value.css)
}

enum class TouchActionValue(val css: String) {
    MANIPULATION("manipulation"),
    NONE("none"),
    PAN_Y("pan-y"),
}

fun StyleScope.setTouchAction(value: TouchActionValue) {
    property(CssProperty.TOUCH_ACTION.key, value.css)
}

enum class TransformValue(val css: String) {
    NONE("none")
}

fun StyleScope.setTransform(value: TransformValue) {
    property(CssProperty.TRANSFORM.key, value.css)
}

enum class TransformBoxValue(val css: String) {
    FILL_BOX("fill-box")
}

fun StyleScope.setTransformBox(value: TransformBoxValue) {
    property(CssProperty.TRANSFORM_BOX.key, value.css)
}

enum class TransformOriginValue(val css: String) {
    CENTER("center")
}

fun StyleScope.setTransformOrigin(value: TransformOriginValue) {
    property(CssProperty.TRANSFORM_ORIGIN.key, value.css)
}

enum class TransitionValue(val css: String) {
    NONE("none")
}

fun StyleScope.setTransition(value: TransitionValue) {
    property(CssProperty.TRANSITION.key, value.css)
}

enum class VectorEffectValue(val css: String) {
    NON_SCALING_STROKE("non-scaling-stroke")
}

fun StyleScope.setVectorEffect(value: VectorEffectValue) {
    property(CssProperty.VECTOR_EFFECT.key, value.css)
}

enum class VerticalAlignValue(val css: String) {
    BASELINE("baseline"),
    MIDDLE("middle"),
    TOP("top"),
}

fun StyleScope.setVerticalAlign(value: VerticalAlignValue) {
    property(CssProperty.VERTICAL_ALIGN.key, value.css)
}

enum class WhiteSpaceValue(val css: String) {
    NORMAL("normal"),
    NOWRAP("nowrap"),
    PRE_WRAP("pre-wrap"),
}

fun StyleScope.setWhiteSpace(value: WhiteSpaceValue) {
    property(CssProperty.WHITE_SPACE.key, value.css)
}
