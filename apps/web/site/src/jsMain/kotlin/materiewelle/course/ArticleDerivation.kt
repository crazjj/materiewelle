package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.core.course.StationId
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.LocalLabMotion
import materiewelle.ui.MathText
import materiewelle.ui.MotionState
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement

private data class DerivationLine(val title: String, val formula: FormulaId, val reason: String, val navigationLabel: String)
private val derivationLines = listOf(
    DerivationLine("Energiegewinn", FormulaId.ENERGY_GAIN,
        "Das Elektron startet aus der Ruhe. Ohne Energieverluste entspricht seine kinetische Energie nach der Beschleunigung der verrichteten elektrischen Arbeit.", "Energie"),
    DerivationLine("Energie und Impuls", FormulaId.KINETIC_MOMENTUM,
        "Die nichtrelativistische Mechanik verknüpft die kinetische Energie mit dem Impuls und der Elektronenmasse.", "Impuls"),
    DerivationLine("Gleichsetzen", FormulaId.KINETIC_BALANCE,
        "Beide Ausdrücke beschreiben dieselbe kinetische Energie nach der Beschleunigung. Deshalb dürfen wir sie gleichsetzen.", "Gleichsetzen"),
    DerivationLine("Nach dem Impuls auflösen", FormulaId.MOMENTUM,
        "Multipliziere beide Seiten mit der doppelten Elektronenmasse und ziehe die Quadratwurzel. Wir wählen den positiven Wert, weil p hier den Betrag des Impulses bezeichnet.", "Wurzel"),
    DerivationLine("De Broglies Hypothese", FormulaId.DE_BROGLIE,
        "Erst jetzt ordnen wir dem Impuls eine Wellenlänge zu. Das ist eine zusätzliche physikalische Hypothese, keine Umformung der Energieerhaltung.", "Hypothese"),
    DerivationLine("Den Impuls einsetzen", FormulaId.WAVELENGTH_VOLTAGE,
        "Setze den berechneten Impuls in de Broglies Beziehung ein. Damit lässt sich die Wellenlänge aus der Spannung vorhersagen.", "Einsetzen"),
)

/** A complete reading sequence. Scroll observation only changes the accompanying formula board. */
@Composable
internal fun ArticleDerivation(session: LabSession, view: DeBroglieUiState) {
    val work = session.snapshot.stationWork(StationId.S05_DERIVATION)
    val known = maxOf(work.explanationStep, work.explanationFurthestStep).coerceIn(0, derivationLines.lastIndex)
    val active = (view.visibleStep.value ?: known).coerceIn(0, derivationLines.lastIndex)
    val furthest = maxOf(known, view.furthestStep.value, active)
    var surface by remember { mutableStateOf<HTMLElement?>(null) }
    var enhanced by remember { mutableStateOf(false) }
    var layoutReady by remember { mutableStateOf(false) }
    val motion = LocalLabMotion.current
    // Backgrounding pauses updates without changing the page geometry or the reader's scroll position.
    val observe = motion.state in setOf(MotionState.RUNNING, MotionState.HIDDEN) && !view.practice.value.active
    val visit: (Int) -> Unit = { index ->
        view.visitStep(index)
        val current = session.snapshot.stationWork(StationId.S05_DERIVATION)
        if (index > maxOf(current.explanationStep, current.explanationFurthestStep)) {
            session.editWork(StationId.S05_DERIVATION) {
                it.copy(explanationStep = index, explanationFurthestStep = maxOf(it.explanationFurthestStep, index))
            }
        }
    }
    val currentVisit by rememberUpdatedState(visit)
    DisposableEffect(surface, observe) {
        layoutReady = false
        val stop = surface?.let { element ->
            observeArticleDerivation(element, observe, { enhanced = it; layoutReady = true }) {
                // A remount must not replace the explicit return destination with its temporary scroll position.
                if (view.readingReturnStep.value == null) currentVisit(it)
            }
        }
        onDispose { stop?.invoke() }
    }
    val returnStep = view.readingReturnStep.value
    DisposableEffect(layoutReady, enhanced, returnStep) {
        val cancel = if (layoutReady && returnStep != null) requestArticleDerivationStep(returnStep) {
            view.visitStep(returnStep)
            view.finishReadingReturn()
        } else ({})
        onDispose { cancel() }
    }
    Div(attrs = {
        elementId(ArticleReadingId.SURFACE)
        articleReadingStyles(ArticleReadingClass.SURFACE)
        if (enhanced) articleReadingStyles(ArticleReadingClass.ENHANCED)
        ref { surface = it; onDispose { surface = null } }
    }) {
        P(attrs = { articleStyles(ArticleClass.SMALL) }) {
            Text(if (enhanced)
                "Beim Weiterlesen bleibt der Rechenweg daneben sichtbar."
                else "Lies die sechs Schritte der Reihe nach. Über die Schritttasten kannst du eine Stelle direkt aufrufen.")
        }
        Div(attrs = {
            elementId(ArticleReadingId.NAVIGATION)
            articleReadingStyles(ArticleReadingClass.NAVIGATION)
            role(AriaRole.GROUP); ariaLabel("Schritt der Herleitung aufrufen")
        }) {
            derivationLines.forEachIndexed { index, line ->
                Button(attrs = {
                    elementId(ArticleReadingChoiceId(index)); styles(LabClass.SECONDARY)
                    ariaLabel("Schritt ${index + 1}: ${line.title}")
                    if (active == index) currentLearningStep()
                    onClick { visit(index); goToArticleDerivationStep(index) }
                }) { Text("${index + 1}"); Span { Text(line.navigationLabel) } }
            }
        }
        Div(attrs = { articleReadingStyles(ArticleReadingClass.LAYOUT) }) {
            Div(attrs = { articleReadingStyles(ArticleReadingClass.STEPS) }) {
                derivationLines.forEachIndexed { index, line ->
                    Section(attrs = {
                        elementId(ArticleLineId(index)); focusIndex(-1)
                        articleReadingStyles(ArticleReadingClass.STEP)
                        if (index == derivationLines.lastIndex) articleReadingStyles(ArticleReadingClass.LAST_STEP)
                        if (index == active) articleReadingStyles(ArticleReadingClass.CURRENT)
                        if (line.formula == FormulaId.DE_BROGLIE) articleReadingStyles(ArticleReadingClass.HYPOTHESIS)
                    }) {
                        Div(attrs = { articleReadingStyles(ArticleReadingClass.STEP_CONTENT) }) {
                            H3 { Text("${index + 1} · ${line.title}") }
                            Div(attrs = { articleReadingStyles(ArticleReadingClass.INLINE_FORMULA) }) {
                                InlineFormula(line.formula)
                            }
                            P { Text(line.reason) }
                            ArticleDerivationContext(line.formula)
                        }
                    }
                }
            }
            // Duplicate visual aid only: the full formula sequence remains in semantic step order above.
            Aside(attrs = {
                elementId(ArticleReadingId.FORMULAS); articleReadingStyles(ArticleReadingClass.FORMULAS)
                ariaHidden(true)
            }) {
                H3 { Text("Der Rechenweg") }
                derivationLines.take(furthest + 1).forEachIndexed { index, line ->
                    Div(attrs = {
                        articleReadingStyles(ArticleReadingClass.FORMULA_ROW)
                        if (index == active) articleReadingStyles(ArticleReadingClass.FORMULA_CURRENT)
                        if (line.formula == FormulaId.DE_BROGLIE) articleReadingStyles(ArticleReadingClass.HYPOTHESIS)
                    }) {
                        Span { Text("${index + 1}") }
                        InlineFormula(line.formula)
                    }
                }
            }
        }
    }
}

/** Prerequisites stay beside their step in both the enhanced and normal reading layouts. */
@Composable
private fun ArticleDerivationContext(formula: FormulaId) {
    when (formula) {
        FormulaId.ENERGY_GAIN -> {
            P { MathText("E_kin"); Text(" ist die kinetische Energie. "); MathText("e"); Text(" bezeichnet den positiven Betrag der Elektronenladung, "); MathText("U"); Text(" den Betrag der Beschleunigungsspannung.") }
            Details {
                Summary { Text("Warum ist der Energiegewinn trotz negativer Ladung positiv?") }
                P { Text("Das Elektron bewegt sich bei der Beschleunigung zum höheren elektrischen Potenzial. Dabei gewinnt es kinetische Energie. In der Rechnung verwenden wir für Ladung und Spannung positive Beträge.") }
            }
        }
        FormulaId.KINETIC_MOMENTUM -> {
            P { MathText("mₑ"); Text(" ist die Elektronenmasse. Diese Beziehung gilt näherungsweise, solange die Geschwindigkeit klein gegenüber der Lichtgeschwindigkeit bleibt.") }
            Details {
                Summary { Text("Wie wird aus Geschwindigkeit ein Impuls?") }
                P { Text("Der Impuls hängt von Masse und Geschwindigkeit ab. Bei gleicher Masse bedeutet eine größere Geschwindigkeit auch einen größeren Impulsbetrag.") }
                P { Text("Mit der Geschwindigkeit "); MathText("v"); Text(" gilt "); MathText("E_kin = mₑ v² / 2"); Text(" und "); MathText("p = mₑ v"); Text(". Aus der zweiten Beziehung folgt "); MathText("v = p / mₑ"); Text(". Einsetzen in die Energieformel ergibt "); MathText("E_kin = p²/(2mₑ)"); Text(".") }
            }
        }
        FormulaId.KINETIC_BALANCE -> Details {
            Summary { Text("Was wäre bei einer Anfangsenergie anders?") }
            P { Text("Die elektrische Arbeit käme zur vorhandenen kinetischen Energie hinzu. Für unser Gleichsetzen sind deshalb der Start aus Ruhe und vernachlässigbare Energieverluste wichtig.") }
        }
        else -> Unit
    }
}
