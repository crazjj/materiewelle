package materiewelle.presentation.styles

import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.css.SelectorsScope
import org.jetbrains.compose.web.css.selectors.CSSSelector

enum class SelectorTag(val css: String) {
    A("a"),
    AUDIO("audio"),
    BODY("body"),
    BUTTON("button"),
    CIRCLE("circle"),
    DD("dd"),
    DETAILS("details"),
    DIV("div"),
    DL("dl"),
    DT("dt"),
    FIGCAPTION("figcaption"),
    H1("h1"),
    H2("h2"),
    H3("h3"),
    H4("h4"),
    HEADER("header"),
    HTML("html"),
    IMG("img"),
    INPUT("input"),
    LABEL("label"),
    LI("li"),
    NAV("nav"),
    P("p"),
    PATH("path"),
    SECTION("section"),
    SELECT("select"),
    SPAN("span"),
    STRONG("strong"),
    SUMMARY("summary"),
    SVG("svg"),
    TABLE("table"),
    TD("td"),
    TEXT("text"),
    TEXTAREA("textarea"),
    TH("th"),
    TR("tr"),
    UL("ul"),
}

enum class SelectorAttribute(val css: String) {
    ARIA_INVALID("aria-invalid"),
    ARIA_PRESSED("aria-pressed"),
    DATA_GREETING("data-greeting"),
    DATA_MOTION("data-motion"),
    DATA_MOTION_ENTERED("data-motion-entered"),
    DATA_MOTION_IN_VIEW("data-motion-in-view"),
    DATA_THEME("data-theme"),
    HIDDEN("hidden"),
    OPEN("open"),
    ROLE("role"),
    STROKE_DASHARRAY("stroke-dasharray"),
    TABINDEX("tabindex"),
    TYPE("type"),
}

enum class AttributeValue(val css: String) {
    ALERT("alert"),
    CHECKBOX("checkbox"),
    DARK("dark"),
    FALSE("false"),
    FILE("file"),
    RANGE("range"),
    RUNNING("running"),
    TRUE("true"),
}

enum class SelectorPseudo(val css: String) {
    ACTIVE("active"),
    AFTER("after"),
    BEFORE("before"),
    DISABLED("disabled"),
    FIRST_CHILD("first-child"),
    FIRST_OF_TYPE("first-of-type"),
    FOCUS("focus"),
    FOCUS_VISIBLE("focus-visible"),
    FOCUS_WITHIN("focus-within"),
    HOVER("hover"),
    LAST_CHILD("last-child"),
    ONLY_OF_TYPE("only-of-type"),
    ROOT("root"),
}

private object SelectorSyntax : SelectorsScope

private fun Selector(text: String): CSSSelector = SelectorSyntax.selector(text)

internal val universal: CSSSelector = Selector("*")

internal fun t(tag: SelectorTag): CSSSelector = Selector(tag.css)

internal fun c(style: StyleClass): CSSSelector = Selector(".${style.cssName}")

internal fun i(id: ElementId): CSSSelector = Selector("#${id.value}")

internal fun pseudo(value: SelectorPseudo): CSSSelector = Selector(":" + value.css)

internal fun attribute(key: SelectorAttribute, value: AttributeValue? = null): CSSSelector =
    Selector("[${key.css}${value?.let { "=${it.css}" } ?: ""}]")

internal fun allOf(vararg selectors: CSSSelector): CSSSelector =
    Selector(selectors.joinToString("") { it.toString() })

internal fun anyOf(vararg selectors: CSSSelector): CSSSelector =
    Selector(selectors.joinToString(",") { it.toString() })

internal fun desc(parent: CSSSelector, child: CSSSelector): CSSSelector =
    Selector("${parent.toString()} ${child.toString()}")

internal fun child(parent: CSSSelector, child: CSSSelector): CSSSelector =
    Selector("${parent.toString()}>${child.toString()}")

internal fun adjacent(parent: CSSSelector, child: CSSSelector): CSSSelector =
    Selector("${parent.toString()}+${child.toString()}")

internal fun sibling(parent: CSSSelector, child: CSSSelector): CSSSelector =
    Selector("${parent.toString()}~${child.toString()}")

internal fun negate(selector: CSSSelector): CSSSelector = Selector(":not(${selector.toString()})")

internal fun matchesAny(selector: CSSSelector): CSSSelector =
    Selector(":is(${selector.toString()})")

internal fun nthOfType(index: String): CSSSelector {
    require(index.toIntOrNull()?.let { it > 0 } == true)
    return Selector(":nth-of-type($index)")
}

internal fun nthChild(expression: String): CSSSelector {
    require(Regex("[0-9]+n|[0-9]+").matches(expression))
    return Selector(":nth-child($expression)")
}
