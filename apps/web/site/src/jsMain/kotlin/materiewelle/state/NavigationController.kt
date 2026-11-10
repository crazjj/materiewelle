package materiewelle.state

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import materiewelle.core.LoadState
import materiewelle.core.ScientificData
import materiewelle.core.course.*
import materiewelle.ui.semantics.*
import kotlin.math.max

/** Route changes are requests, never a side effect of a scientific input change. */
class NavigationController(initialHash: String) {
    var route by mutableStateOf(AppRoute.parse(initialHash) ?: AppRoute.Overview); private set
    var unknown by mutableStateOf(AppRoute.parse(initialHash) == null); private set
    var request by mutableStateOf(0); private set
    private var requestedFocus: ElementId? = null
    private var positioned = if (route == AppRoute.Overview ||
        route == AppRoute.Laboratory(materiewelle.core.Experiment.DE_BROGLIE)) 0 else -1

    fun choose(next: AppRoute, focusTarget: ElementId? = null) {
        requestedFocus = focusTarget
        route = next; unknown = false; request++
        if (window.location.hash != next.hash) window.location.hash = next.hash.removePrefix("#")
    }
    fun acceptHash(hash: String): Boolean {
        val parsed = AppRoute.parse(hash)
        val next = parsed ?: AppRoute.Overview
        val nextUnknown = parsed == null
        if (next != route || nextUnknown != unknown) {
            requestedFocus = null
            route = next; unknown = nextUnknown; request++
            return true
        }
        return false
    }
    internal fun needsPosition() = positioned != request
    internal fun didPosition() { positioned = request; requestedFocus = null }
    internal fun target(): ElementId {
        requestedFocus?.let { return it }
        if ((route as? AppRoute.DocumentAt)?.section == DocumentSectionId.SOURCES) return ConclusionId.SOURCES
        return when (val current = route.explanationRoute()) {
            AppRoute.Overview -> OverviewId.TITLE
            is AppRoute.DgLesson -> current.step.elementId()
            is AppRoute.Course -> when(current.station) {
                StationId.S01_OBSERVATION, StationId.S02_CLASSICAL -> ArticleId.ENTRY
                StationId.S03_LIGHT -> ArticleId.HISTORY
                StationId.S04_HYPOTHESIS -> ArticleId.HYPOTHESIS
                StationId.S05_DERIVATION -> ArticleId.DERIVATION
                StationId.S06_CALCULATION -> ArticleId.CALCULATION
                StationId.S07_RECEPTION -> ArticleId.RECEPTION
                StationId.S08_DG_SETUP -> DgArticleId.SETUP
                StationId.S09_CRYSTAL -> DgArticleId.OBSERVATION
                StationId.S10_GEOMETRY -> DgArticleId.BRAGG
                StationId.S11_DG_EVALUATION -> DgArticleId.COMPARISON
                StationId.S12_JONSSON_SETUP -> JonssonArticleId.SETUP
                StationId.S13_INTERFERENCE -> JonssonArticleId.MODEL
                StationId.S14_MEASUREMENT -> JonssonArticleId.MEASUREMENT
                StationId.S15_LIMITS -> JonssonArticleId.LIMITS
                StationId.S16_CONCLUSION -> ConclusionId.TITLE
            }
            is AppRoute.Laboratory -> when (current.experiment) {
                materiewelle.core.Experiment.DE_BROGLIE -> ArticleId.TITLE
                materiewelle.core.Experiment.DAVISSON_GERMER -> DgArticleId.TITLE
                materiewelle.core.Experiment.JONSSON -> if (current.focus == LabFocus.MEASUREMENT) JonssonArticleId.MEASUREMENT else JonssonArticleId.TITLE
            }
            AppRoute.Tutor -> CompactTutorId.PANEL
            else -> ArticleId.TITLE
        }
    }

}

/** Wait for the visible composed target; no fixed timeout and no scrolling on input updates. */
@Composable
fun PositionNavigation(navigation: NavigationController, data: LoadState<ScientificData>) {
    DisposableEffect(navigation.request, data) {
        var frame = 0
        var disposed = false
        val token = navigation.request
        fun position() {
            if (disposed || token != navigation.request || !navigation.needsPosition()) return
            // The tutor mounts its question asynchronously and owns its opening focus.
            if (navigation.route == AppRoute.Tutor) { navigation.didPosition(); return }
            val section = AppSection.forRoute(navigation.route)
            val needsData = section == AppSection.DAVISSON_GERMER || section == AppSection.JONSSON
            if (needsData && data == LoadState.Loading) return
            val requested = navigation.target()
            val target = when {
                needsData && data is LoadState.Failed -> Id.LABORATORY
                requested == Id.JONSSON_INTERACTIVE_IMAGE && data is LoadState.Ready && data.value.jonsson == null ->
                    JonssonArticleId.MEASUREMENT
                else -> requested
            }
            val element = document.findHtmlElement(target)
            if (element != null && element.getClientRects().length > 0) {
                element.focus()
                element.scrollIntoView(true)
                val header = document.findHtmlElement(ShellId.HEADER)
                val sticky = header?.let { window.getComputedStyle(it).position in setOf("sticky", "fixed") } == true
                val headerBottom = if (sticky) max(0.0, header!!.getBoundingClientRect().bottom) else 0.0
                val chapterNavigation = if (section == AppSection.DAVISSON_GERMER && target != DgArticleId.TITLE)
                    document.findHtmlElement(DgLessonId.NAV) else null
                val chapterBottom = chapterNavigation?.takeIf { window.getComputedStyle(it).position == "sticky" }
                    ?.getBoundingClientRect()?.bottom ?: 0.0
                // Keep the short historical heading above these titles visible as well.
                val titleClearance = if (target in setOf(ArticleId.TITLE, DgArticleId.TITLE, JonssonArticleId.TITLE)) 44.0 else 16.0
                window.scrollBy(0.0, element.getBoundingClientRect().top - max(headerBottom, chapterBottom) - titleClearance)
                navigation.didPosition()
            } else frame = window.requestAnimationFrame { position() }
        }
        frame = window.requestAnimationFrame { position() }
        onDispose { disposed = true; window.cancelAnimationFrame(frame) }
    }
}
