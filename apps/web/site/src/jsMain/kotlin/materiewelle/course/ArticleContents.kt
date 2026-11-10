package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.Experiment
import materiewelle.core.course.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*

private data class ArticleLink(val label: String, val route: AppRoute)

/** Local destinations reuse existing scientific identities; no new stations or progress states. */
@Composable
fun ArticleContents(section: AppSection, navigate: (AppRoute) -> Unit) {
    val links = when (section) {
        AppSection.DE_BROGLIE -> listOf(
            ArticleLink("Ausgangspunkt", AppRoute.Course(StationId.S03_LIGHT)),
            ArticleLink("Herleitung Schritt für Schritt", AppRoute.Course(StationId.S05_DERIVATION)),
            ArticleLink("Selbst rechnen", AppRoute.Course(StationId.S06_CALCULATION)),
            ArticleLink("Einsteins Einschätzung", AppRoute.Course(StationId.S07_RECEPTION)))
        AppSection.DAVISSON_GERMER -> listOf(
            ArticleLink("Aufbau", AppRoute.Course(StationId.S08_DG_SETUP)),
            ArticleLink("Beobachtung", AppRoute.Course(StationId.S09_CRYSTAL)),
            ArticleLink("Geometrie", AppRoute.Course(StationId.S10_GEOMETRY)),
            ArticleLink("Auswertung", AppRoute.Course(StationId.S11_DG_EVALUATION)))
        AppSection.JONSSON -> listOf(
            ArticleLink("Aufbau", AppRoute.Course(StationId.S12_JONSSON_SETUP)),
            ArticleLink("Spalte verändern", AppRoute.Course(StationId.S13_INTERFERENCE)),
            ArticleLink("Am historischen Bild messen", AppRoute.Laboratory(Experiment.JONSSON, LabFocus.MEASUREMENT)),
            ArticleLink("Einordnung", AppRoute.Course(StationId.S15_LIMITS)))
        AppSection.CONCLUSION -> emptyList()
    }
    if (links.isNotEmpty()) Nav(attrs = { articleStyles(ArticleClass.CONTENTS); ariaLabel("In diesem Kapitel") }) {
        links.forEach { link ->
            A(href = link.route.hash, attrs = { onClick { event -> event.preventDefault(); navigate(link.route) } }) { Text(link.label) }
        }
    }
}
