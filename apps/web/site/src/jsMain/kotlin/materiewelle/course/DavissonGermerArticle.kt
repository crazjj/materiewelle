package materiewelle.course

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import materiewelle.NativeSvg
import materiewelle.DgSetupFocus
import materiewelle.detectorChart
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.learning.CompactTutorUiState
import materiewelle.pages.NickelSurfaceView
import materiewelle.pages.braggGeometryChart
import materiewelle.pages.nickelSurfaceChart
import materiewelle.pages.number
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Six authored steps reuse the scientific session. A visited step is not a learning assessment. */
@Composable
fun DavissonGermerArticle(
    session: LabSession,
    data: ScientificData,
    onNavigate: (AppRoute) -> Unit,
    tutorView: CompactTutorUiState,
    route: AppRoute,
    view: DgLessonView,
) {
    val current = DgLessonStep.forRoute(route) ?: view.step
    val nextLabel = when {
        current == DgLessonStep.SETUP && view.setupStage == 0 -> "Nächster Bildschritt: Kristall"
        current == DgLessonStep.SETUP && view.setupStage == 1 -> "Nächster Bildschritt: Detektor"
        current == DgLessonStep.BRAGG && view.braggStage == 0 -> "Nächster Bildschritt: erster Zusatzweg"
        current == DgLessonStep.BRAGG && view.braggStage == 1 -> "Nächster Bildschritt: zweiter Zusatzweg"
        else -> current.next?.let { "Weiter zum Abschnitt: ${it.label}" } ?: "Weiter zu Jönsson"
    }
    val onAdvance: () -> Unit = {
        when {
            current == DgLessonStep.SETUP && view.setupStage < DgSetupFocus.entries.lastIndex -> view.setupStage++
            current == DgLessonStep.BRAGG && view.braggStage < 2 -> showNextBraggPath(session, view)
            else -> onNavigate(current.next?.route ?: AppSection.JONSSON.route)
        }
    }
    SideEffect { view.step = current }
    DisposableEffect(Unit) { onDispose(observeDgLessonInsets()) }
    Article(attrs = { elementId(DgArticleId.ROOT); articleStyles(ArticleClass.ROOT); dgLessonStyles(DgLessonClass.ROOT) }) {
        Header(attrs = { dgLessonStyles(DgLessonClass.HEADER) }) {
            P { Text("Davisson und Germer · 1927") }
            H1(attrs = { elementId(DgArticleId.TITLE); focusIndex(-1) }) { Text("Elektronen am Nickelkristall") }
            P { Text("Wir vergleichen zwei Wellenlängen: die Vorhersage aus der Spannung und den Wert aus Winkel und Kristallstruktur.") }
            P { Text("Zunächst zeigen die Skizzen den Versuchsaufbau und das Prinzip der Beugung. Danach berechnest du beide Wellenlängen und beurteilst den Vergleich. Mit „Weiter“ öffnest du die nächsten Schritte.") }
        }
        Nav(attrs = { elementId(DgLessonId.NAV); dgLessonStyles(DgLessonClass.NAV); ariaLabel("Schritte bei Davisson–Germer") }) {
            DgLessonStep.entries.forEach { step ->
                A(href = step.route.hash, attrs = {
                    if (current == step) { dgLessonStyles(DgLessonClass.ACTIVE); currentLearningStep() }
                    onClick { it.preventDefault(); onNavigate(step.route) }
                }) {
                    Span(attrs = { dgLessonStyles(DgLessonClass.NUMBER) }) { Text("${step.ordinal + 1}") }
                    Text(step.label)
                }
            }
            Button(attrs = {
                elementId(DgLessonId.ALL); styles(LabClass.SECONDARY); ariaPressed(view.showAll)
                onClick { view.showAll = !view.showAll; onNavigate(current.route) }
            }) { Text(if (view.showAll) "Einzelnen Schritt zeigen" else "Ganzes Kapitel anzeigen") }
        }
        val visible = if (view.showAll) DgLessonStep.entries else listOf(current)
        visible.forEach { step ->
            Section(attrs = { elementId(step.elementId()); focusIndex(-1); data(LabData.DG_LESSON_STEP, step); dgLessonStyles(DgLessonClass.STAGE) }) {
                P(attrs = { dgLessonStyles(DgLessonClass.META) }) { Text("Schritt ${step.ordinal + 1} von ${DgLessonStep.entries.size} · ${step.label}") }
                H2 { Text(step.question) }
                when (step) {
                    DgLessonStep.SETUP -> DgSetup(data, view, view.showAll, nextLabel, onAdvance)
                    DgLessonStep.OBSERVATION -> DgObservation(session, data)
                    DgLessonStep.BRAGG -> DgBragg(session, view, view.showAll, nextLabel, onAdvance)
                    DgLessonStep.GEOMETRY -> DgSourceGeometry(data, view)
                    DgLessonStep.CALCULATION -> DgSourceCalculation(session, data, view, tutorView)
                    DgLessonStep.CONCLUSION -> DgConclusion(session, data)
                }
            }
        }
        if (!view.showAll) Nav(attrs = { dgLessonStyles(DgLessonClass.FOOTER); ariaLabel("Im Kapitel weitergehen") }) {
            Button(attrs = {
                elementId(DgLessonId.PREVIOUS); styles(LabClass.SECONDARY)
                onClick { onNavigate(current.previous?.route ?: AppRoute.Course(StationId.S07_RECEPTION)) }
            }) { Text(current.previous?.let { "Zum vorherigen Abschnitt: ${it.label}" } ?: "Zurück zu de Broglie") }
            when (current) {
                DgLessonStep.SETUP -> if (view.setupStage == DgSetupFocus.entries.lastIndex) {
                    DgNextButton(nextLabel, onAdvance, DgLessonId.FOOTER_NEXT)
                }
                DgLessonStep.BRAGG -> if (view.braggStage == 2) {
                    DgNextButton(nextLabel, onAdvance, DgLessonId.FOOTER_NEXT)
                }
                else -> DgNextButton(nextLabel, onAdvance)
            }
        }
        if (view.showAll) A(href = AppSection.JONSSON.route.hash, attrs = { styles(LabClass.PRIMARY); onClick { it.preventDefault(); onNavigate(AppSection.JONSSON.route) } }) { Text("Weiter zu Jönsson") }
        DgSources(data)
    }
}

@Composable
private fun DgNextButton(label: String, onAdvance: () -> Unit, id: DgLessonId = DgLessonId.NEXT) {
    Button(attrs = { elementId(id); styles(LabClass.PRIMARY); onClick { onAdvance() } }) { Text(label) }
}

private val setupExplanationIds = listOf(DgLessonId.SETUP_SOURCE, DgLessonId.SETUP_CRYSTAL, DgLessonId.SETUP_DETECTOR)
private val braggExplanationIds = listOf(DgLessonId.BRAGG_REFERENCE, DgLessonId.BRAGG_INCOMING, DgLessonId.BRAGG_OUTGOING)

@Composable
private fun DgSetup(data: ScientificData, view: DgLessonView, complete: Boolean, nextLabel: String, onAdvance: () -> Unit) {
    val focus = DgSetupFocus.entries[view.setupStage]
    Div(attrs = { dgLessonStyles(DgLessonClass.SCENE); if (complete) dgLessonStyles(DgLessonClass.COMPLETE_SCENE) }) {
        Figure(attrs = { dgLessonStyles(DgLessonClass.FIGURE); data(LabData.STEP, view.setupStage) }) {
            if (!complete) Div(attrs = { dgLessonStyles(DgLessonClass.FIGURE_ACTIONS) }) {
                DgNextButton(nextLabel, onAdvance)
                Button(attrs = {
                    elementId(DgLessonId.SETUP_REPLAY); styles(LabClass.SECONDARY)
                    if (view.setupStage == 0) disabled()
                    onClick { view.setupStage = 0 }
                }) { Text("Aufbau von vorn zeigen") }
            }
            NativeSvg(detectorChart(data.points.first().angleDegrees, focus = if (complete) null else focus, showAngle = false))
            Figcaption {
                Text("Eigenes Schema: Der Strahl trifft senkrecht auf die Oberfläche. Der bewegliche Detektor untersucht verschiedene Streurichtungen.")
            }
        }
        if (complete) Div(attrs = { dgLessonStyles(DgLessonClass.EXPLANATION, DgLessonClass.SETUP_OVERVIEW) }) {
            // Reading all explanations does not advance the guided state.
            DgSetupFocus.entries.forEach { part -> Section { DgSetupExplanation(part) } }
        } else DgGuidedExplanation(DgLessonId.SETUP_EXPLANATION, "Erklärungen zum Versuchsaufbau") {
            DgSetupFocus.entries.take(view.setupStage + 1).forEach { part ->
                Section(attrs = {
                    elementId(setupExplanationIds[part.ordinal]); dgLessonStyles(DgLessonClass.EXPLANATION_STEP)
                    if (part == focus) dgLessonStyles(DgLessonClass.EXPLANATION_ACTIVE)
                }) { DgSetupExplanation(part) }
            }
        }
    }
}

@Composable
private fun DgSetupExplanation(focus: DgSetupFocus) {
    when (focus) {
        DgSetupFocus.SOURCE -> {
            H3 { Text("1 · Elektronen beschleunigen") }
            P { Text("Eine Quelle liefert Elektronen. Die Beschleunigungsspannung bestimmt ihren Energiegewinn und damit die vorhergesagte Wellenlänge.") }
            P { Text("Aus der Beschleunigungsspannung lässt sich mit de Broglies Hypothese eine Wellenlänge vorhersagen. Der Kristallversuch ermöglicht einen davon unabhängigen Vergleich.") }
        }
        DgSetupFocus.CRYSTAL -> {
            H3 { Text("2 · Auf eine regelmäßige Struktur treffen") }
            P { Text("Der Strahl trifft auf einen Nickel-Einkristall. Die Abstände seiner regelmäßig angeordneten Atome liegen auf derselben Größenskala wie die vorhergesagte Elektronenwellenlänge.") }
            P { Text("In einem Einkristall ist das Gitter im untersuchten Bereich einheitlich ausgerichtet. Dadurch lassen sich die Streurichtungen auf die Orientierung des Kristalls beziehen. Der Versuch zeigt, in welchen Richtungen besonders viele Elektronen registriert werden.") }
        }
        DgSetupFocus.DETECTOR -> {
            H3 { Text("3 · Verschiedene Richtungen untersuchen") }
            P { Text("Der bewegliche Detektor registriert gestreute Elektronen. Seine Richtung wird verändert, während die Beschleunigungsspannung für den betrachteten Fall festliegt.") }
            P { Text("Die gestrichelte Normale steht senkrecht auf der Oberfläche. Von ihr aus messen wir den Detektorwinkel.") }
        }
    }
}

@Composable
private fun DgObservation(session: LabSession, data: ScientificData) {
    val source = data.points.first()
    Div(attrs = { dgLessonStyles(DgLessonClass.SCENE) }) {
        Figure(attrs = { dgLessonStyles(DgLessonClass.FIGURE) }) {
            NativeSvg(detectorChart(source.angleDegrees))
            Figcaption { Text("Die eingezeichnete Richtung gehört zum berichteten Befund. Das Schema zeigt keine historische Intensitätskurve.") }
        }
        Div(attrs = { dgLessonStyles(DgLessonClass.EXPLANATION) }) {
            P { Text("In manchen Richtungen wurden besonders viele Elektronen registriert. Die Sekundäranalyse D2 von Suzuki und Suzuki beschreibt folgenden Fall:") }
            Div(attrs = { dgLessonStyles(DgLessonClass.SOURCE_CASE) }) {
                Div { Div(attrs = { dgLessonStyles(DgLessonClass.QUANTITY) }) { MathText("${number(source.voltageV, 0)} eV") }; P { Text("Elektronenenergie") } }
                Div { Div(attrs = { dgLessonStyles(DgLessonClass.QUANTITY) }) { MathText("≈ ${number(source.angleDegrees, 0)}°") }; P { Text("Detektorrichtung zur Normalen") } }
            }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) {
                Text("Elektronenvolt (eV) ist eine Energieeinheit: Bei einer Beschleunigungsspannung von "); MathText("1 V")
                Text(" gewinnt ein Elektron "); MathText("1 eV"); Text(" an kinetischer Energie.")
            }
            P { Text("Für den Wellenlängenvergleich betrachten wir die elastisch gestreuten Elektronen. Ihre kinetische Energie bleibt bei der Streuung erhalten.") }
            P { Text("Eine bevorzugte Richtung allein reicht für unseren Vergleich noch nicht aus. Wir brauchen einen Zusammenhang zwischen Richtung, Kristallstruktur und Wellenlänge.") }
            P { Text("Dazu betrachten wir zunächst die Bragg-Konstruktion mit parallelen Netzebenen. Sie erklärt das Prinzip der Wegunterschiede. Für den berichteten Zahlenfall verwenden wir anschließend ein Oberflächenmodell mit Atomreihen; dafür gelten andere Abstände und Winkelbezüge.") }
            P(attrs = { articleStyles(ArticleClass.SOURCE) }) { Text("D2 · S. 2 und 5–6, Abb. 6 und 7. Der Winkel ist ungefähr angegeben; historische Messunsicherheiten sind hier nicht belegt.") }
        }
    }
    Div(attrs = { dgLessonStyles(DgLessonClass.NOTE) }) {
        FieldLabel(DgLessonId.OBSERVATION_NOTE) { Text("Was wurde gemessen – und was müsste erst erklärt werden?") }
        TextArea(attrs = { elementId(DgLessonId.OBSERVATION_NOTE); protectPersonalWork(session.isReadOnly); textRows(2); maxLength(4000)
            value(session.snapshot.stationWork(StationId.S09_CRYSTAL).note)
            onInput { event -> session.editWork(StationId.S09_CRYSTAL) { it.copy(note = event.value.take(4000)) } }
        })
    }
}

@Composable
private fun DgBragg(session: LabSession, view: DgLessonView, complete: Boolean, nextLabel: String, onAdvance: () -> Unit) {
    val shownStage = if (complete) 2 else view.braggStage
    Div(attrs = { dgLessonStyles(DgLessonClass.SCENE) }) {
        Figure(attrs = { dgLessonStyles(DgLessonClass.FIGURE) }) {
            if (!complete) Div(attrs = { dgLessonStyles(DgLessonClass.FIGURE_ACTIONS) }) {
                DgNextButton(nextLabel, onAdvance)
                Div {
                    Button(attrs = { elementId(DgArticleId.BRAGG_ALL); styles(LabClass.SECONDARY)
                        onClick { view.braggStage = 2; session.update { it.copy(dg = it.dg.copy(geometryStep = maxOf(it.dg.geometryStep, 2))) } }
                    }) { Text("Beide Zusatzwege zeigen") }
                    Button(attrs = {
                        elementId(DgLessonId.BRAGG_REPLAY); styles(LabClass.SECONDARY)
                        if (view.braggStage == 0) disabled()
                        onClick { view.braggStage = 0 }
                    }) { Text("Zusatzwege von vorn zeigen") }
                }
            }
            NativeSvg(braggGeometryChart(shownStage))
            Figcaption {
                Text("Bragg-Beispiel mit "); MathText("30°"); Text(" zur Netzebene; anderer Winkelbezug als im ")
                MathText("50°"); Text("-Befund.")
            }
        }
        if (complete) Div(attrs = { dgLessonStyles(DgLessonClass.EXPLANATION) }) {
            DgBraggExplanation(shownStage, false)
        } else DgGuidedExplanation(DgLessonId.BRAGG_EXPLANATION, "Erklärungen zum Gangunterschied") {
            DgBraggExplanation(shownStage, true)
        }
    }
}

@Composable
private fun DgBraggExplanation(shownStage: Int, guided: Boolean) {
    (0..shownStage).forEach { step ->
        Section(attrs = {
            elementId(braggExplanationIds[step])
            if (guided) dgLessonStyles(DgLessonClass.EXPLANATION_STEP)
            if (guided && step == shownStage) dgLessonStyles(DgLessonClass.EXPLANATION_ACTIVE)
        }) {
            when (step) {
                0 -> {
                    P { Text("Wir beschreiben die Streuung mithilfe von Elektronenwellen. Die Wellenbeiträge von verschiedenen Streuorten überlagern sich. Ihre Phase beschreibt die Stelle im Schwingungszyklus. Treffen sie mit gleicher Phase zusammen, verstärken sie sich; das nennt man konstruktive Überlagerung.") }
                    P { Text("Bei gleicher Ausgangsphase führt ein Wegunterschied von einer ganzen Zahl von Wellenlängen wieder zu gleicher Phase. Diesen Wegunterschied bestimmen wir nun aus der Geometrie der Streuwege.") }
                    P { Text("Vergleiche die beiden Strahlen an den parallelen Netzebenen. Der tiefere Strahl hat einen längeren Weg. "); MathText("d"); Text(" ist der senkrechte Ebenenabstand; "); MathText("θB"); Text(" ist der Winkel zur Ebene.") }
                }
                1 -> {
                    P { Text("Vor der Streuung kommt die cyan markierte Strecke hinzu. Die Projektion im rechtwinkligen Dreieck ergibt:") }
                    Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.BRAGG_LEG) }
                    BraggSineHelp()
                }
                2 -> {
                    P { Text("Nach der Streuung kommt eine zweite, gleich lange Strecke hinzu. Zusammen liefert das die Bragg-Bedingung:") }
                    Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.BRAGG) }
                    P { MathText("n"); Text(" bezeichnet die Beugungsordnung. Die Bedingung bestimmt die Richtungen konstruktiver Überlagerung; die dortigen Intensitäten berechnet sie nicht.") }
                }
            }
        }
    }
}

private fun showNextBraggPath(session: LabSession, view: DgLessonView) {
    view.braggStage = (view.braggStage + 1).coerceAtMost(2)
    session.update { it.copy(dg = it.dg.copy(geometryStep = maxOf(it.dg.geometryStep, view.braggStage))) }
}

@Composable
private fun DgSourceGeometry(data: ScientificData, view: DgLessonView) {
    P {
        Text("Die Bragg-Konstruktion zeigt, wann sich Wellenbeiträge wegen ihres Wegunterschieds verstärken. Für den berichteten Detektorwinkel von etwa ")
        MathText("50°")
        Text(" verwenden wir nun das Oberflächenmodell aus D2. Wir betrachten ausgewählte Atomreihen an der Nickeloberfläche. Auch hier suchen wir einen Wegunterschied von einer ganzen Zahl von Wellenlängen, benötigen dafür aber den Reihenabstand und den Winkel zur äußeren Normalen.")
    }
    Div(attrs = { dgLessonStyles(DgLessonClass.SCENE) }) {
        Figure(attrs = { dgLessonStyles(DgLessonClass.FIGURE) }) {
            NativeSvg(nickelSurfaceChart(data.points.first().angleDegrees, data.dgStructure.latticeConstant.nanometers, NickelSurfaceView.PROJECTION))
            Figcaption { Text("Bei senkrechtem Einfall erreichen die Wellenbeiträge die betrachteten Reihen mit gleicher Phase. Der eingezeichnete Wegunterschied entsteht auf dem Weg zum Detektor.") }
        }
        Div(attrs = { dgLessonStyles(DgLessonClass.EXPLANATION) }) {
            P { Text("Ni(111) bezeichnet die Orientierung der betrachteten Oberfläche im Kristall. Die Zahlen sind keine Längenangabe. Welche Atomreihen das Modell aus D2 verwendet, zeigt die Skizze.") }
            P { MathText("D"); Text(" ist jetzt der Abstand dieser Reihen. "); MathText("φ"); Text(" wird zur äußeren Normalen gemessen – der Bezugslinie im Aufbaubild.") }
            P { Text("Bei senkrechtem Einfall entsteht zwischen den betrachteten Reihen kein einlaufender Wegunterschied. Es bleibt der Zusatzweg in Richtung des Detektors:") }
            Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.SURFACE) }
            P { Text("Für die erste Ordnung setzen wir "); MathText("n = 1"); Text(". Dann entspricht der Zusatzweg einer Wellenlänge.") }
        }
    }
    GeometryComparisonTable()
    P { Text("Die etwa "); MathText("50°"); Text(" werden zur äußeren Normalen gemessen. In der Bragg-Bedingung steht dagegen der Winkel zur Netzebene. Deshalb kann der Detektorwinkel weder direkt eingesetzt noch allgemein durch Halbieren umgerechnet werden.") }
    Details {
        Summary { Text("Woher kommt der Reihenabstand?") }
        P { Text("Die Gitterkonstante "); MathText("a = ${number(data.dgStructure.latticeConstant.nanometers, 3)} nm"); Text(" ist die Kantenlänge der kubischen Elementarzelle, des sich wiederholenden Gitterbausteins. Schneidet man dieses Gitter entlang der (111)-Oberfläche, liegen die Atome in einem Dreiecksnetz. Für die hier ausgewählten Reihen folgt:") }
        Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.SURFACE_SPACING) }
        P { MathText("D ≈ ${number(data.spacing.nanometers, 4)} nm"); Text(". Dieser Strukturwert wird nicht an die gesuchte Elektronenwellenlänge angepasst. Für die Rechnung behalten wir die ungerundete Zahl bei; das bedeutet keine zusätzliche Messgenauigkeit.") }
        P { Text("Die linke Skizze zeigt die Oberfläche von oben. Der gewählte Azimut ist die Richtung innerhalb dieser Oberfläche, in der wir die Streuung betrachten. "); MathText("D"); Text(" bezeichnet den senkrechten Abstand der markierten Reihen, nicht einfach den Abstand zweier nächster Nachbaratome.") }
        NativeSvg(nickelSurfaceChart(data.points.first().angleDegrees, data.dgStructure.latticeConstant.nanometers))
        P { Text("Der Netzebenenabstand ist dagegen "); MathText("d₁₁₁ = a/√3"); Text(" und damit ungefähr "); MathText("${number(data.dgStructure.planeSpacing.nanometers, 4)} nm"); Text(". Die Annahmen zu Oberfläche, normalem Einfall und ausgewählter Streuebene gehören zu diesem Modell.") }
    }
    DgGeometryComparison(data, view)

}

@Composable
private fun DgSources(data: ScientificData) {
    Details(attrs = { elementId(DgLessonId.SOURCE_NOTES) }) {
        Summary { Text("Quellen und Modellgrenzen dieses Kapitels") }
        P { Text("D2: M. Suzuki und I. S. Suzuki, A proper understanding of the Davisson and Germer experiments for undergraduate modern physics course (2013), arXiv:1307.6049v1, S. 2 und 5–6, Abb. 6 und 7, Gl. 31–34. "); A(href = "https://arxiv.org/abs/1307.6049", attrs = { externalLink() }) { Text("Sekundäranalyse öffnen") } }
        P { Text("Befund: ${data.dgEvidence.location}. Strukturannahme: ${data.dgStructure.evidence.location}. "); MathText(data.dgStructure.evidence.scope) }
        P { Text("D1: C. Davisson und L. H. Germer, Physical Review 30 (1927), 705–740. Die Originalveröffentlichung wurde hier nicht eingesehen. Der Zahlenfall stammt aus der Sekundäranalyse D2. "); A(href = "https://doi.org/10.1103/PhysRev.30.705", attrs = { externalLink() }) { Text("Original-DOI") } }
        P { Text("Die Modellrechnung erzeugt keine historische Intensitätskurve. Zusätzliche Nachkommastellen dienen der Rechenkontrolle; sie belegen keine höhere Messgenauigkeit.") }
    }
}
