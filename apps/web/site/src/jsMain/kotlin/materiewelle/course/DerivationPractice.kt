package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private fun AttrsScope<*>.practiceStyles(vararg values: DerivationPracticeClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** The authored example is hidden only after an explicit choice to rehearse. */
@Composable
internal fun DerivationLearningArea(view: DeBroglieUiState, example: @Composable () -> Unit) {
    var practice by view.practice
    var revisionFocusRequest by remember { mutableIntStateOf(0) }
    DisposableEffect(practice.active) {
        val cancel = if (view.takePracticeFocus()) requestTutorFocus(DerivationPracticeId.TITLE) else ({})
        onDispose { cancel() }
    }
    DisposableEffect(revisionFocusRequest) {
        val cancel = if (revisionFocusRequest > 0) requestTutorFocus(DerivationPracticeId.ANSWER) else ({})
        onDispose { cancel() }
    }
    if (!practice.active) {
        example()
        Button(attrs = {
            elementId(DerivationPracticeId.OPEN); styles(LabClass.SECONDARY)
            onClick { view.openPractice() }
        }) { Text("Eine Zeile selbst ergänzen") }
    } else {
        DerivationPractice(view)
        Div(attrs = { practiceStyles(DerivationPracticeClass.ACTIONS) }) {
            Button(attrs = {
                elementId(DerivationPracticeId.EXAMPLE); styles(LabClass.SECONDARY)
                ariaExpanded(practice.exampleOpen); controls(DerivationPracticeId.EXAMPLE_PANEL)
                onClick { practice = practice.copy(exampleOpen = !practice.exampleOpen) }
            }) { Text(if (practice.exampleOpen) "Beispielherleitung zuklappen" else "Beispielherleitung nachlesen") }
        }
        if (practice.exampleOpen) Div(attrs = { elementId(DerivationPracticeId.EXAMPLE_PANEL) }) { example() }
        if (practice.comparisonOpen || practice.exampleOpen) {
            P { Text("Schließe das Beispiel und erkläre den Weg noch einmal in eigenen Worten: Welche Beziehung liefert den Impuls, und welche kommt als Hypothese hinzu? Deine bisherigen Notizen bleiben stehen.") }
            Button(attrs = {
                elementId(DerivationPracticeId.REVISE); styles(LabClass.SECONDARY)
                onClick {
                    practice = practice.copy(comparisonOpen = false, exampleOpen = false)
                    revisionFocusRequest++
                }
            }) { Text("Zur eigenen Erklärung zurück") }
        }
    }
}

@Composable
private fun DerivationPractice(view: DeBroglieUiState) {
    var state by view.practice
    Section(attrs = {
        elementId(DerivationPracticeId.PANEL); practiceStyles(DerivationPracticeClass.ROOT)
        labelledBy(DerivationPracticeId.TITLE)
    }) {
        H3(attrs = { elementId(DerivationPracticeId.TITLE); focusIndex(-1) }) { Text("Eine Zeile selbst ergänzen") }
        P(attrs = { elementId(DerivationPracticeId.TEMPORARY); styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("Übungsnotizen – nur in diesem Tab: Sie bleiben bis zum Neuladen erhalten und stehen nicht im Arbeitsbericht.")
        }
        P { Text("Ein Elektron startet aus Ruhe; wir rechnen nichtrelativistisch und ohne Energieverluste. Nach dem Gleichsetzen gilt:") }
        Div(attrs = { practiceStyles(DerivationPracticeClass.EQUATION) }) { InlineFormula(FormulaId.KINETIC_BALANCE) }
        P(attrs = { elementId(DerivationPracticeId.GAP); practiceStyles(DerivationPracticeClass.GAP) }) {
            Text("Hier fehlt die Beziehung für den Impulsbetrag. Wie löst du danach auf?")
        }
        P(attrs = { elementId(DerivationPracticeId.INPUT_HELP); styles(LabClass.SMALL) }) {
            Text("Erkläre zuerst mündlich oder notiere deinen Gedanken. Worte genügen; du musst keine Formel mit der Tastatur schreiben.")
        }
        FieldLabel(DerivationPracticeId.ANSWER) { Text("Dein Weg zum Impuls · optional") }
        TextArea(attrs = {
            elementId(DerivationPracticeId.ANSWER); textRows(2); maxLength(DerivationPracticeState.TEXT_LIMIT)
            describedBy(DerivationPracticeId.INPUT_HELP, DerivationPracticeId.TEMPORARY); value(state.answer)
            onInput { state = state.copy(answer = it.value.take(DerivationPracticeState.TEXT_LIMIT)) }
        })
        FieldLabel(DerivationPracticeId.HYPOTHESIS) { Text("Welche Beziehung ergänzt de Broglie als Hypothese? · optional") }
        TextArea(attrs = {
            elementId(DerivationPracticeId.HYPOTHESIS); textRows(2); maxLength(DerivationPracticeState.TEXT_LIMIT)
            describedBy(DerivationPracticeId.INPUT_HELP, DerivationPracticeId.TEMPORARY); value(state.hypothesis)
            onInput { state = state.copy(hypothesis = it.value.take(DerivationPracticeState.TEXT_LIMIT)) }
        })
        Div(attrs = { practiceStyles(DerivationPracticeClass.ACTIONS) }) {
            Button(attrs = {
                elementId(DerivationPracticeId.HINT); styles(LabClass.SECONDARY)
                if (state.hint == DerivationHint.PARTIAL_STEP) disabled()
                onClick { state = state.nextHint() }
            }) { Text(when (state.hint) {
                DerivationHint.NONE -> "Einen Hinweis geben"
                DerivationHint.QUESTION -> "Einen Teilschritt zeigen"
                DerivationHint.PARTIAL_STEP -> "Beide Hinweise sichtbar"
            }) }
            Button(attrs = {
                elementId(DerivationPracticeId.CHOICES); styles(LabClass.SECONDARY)
                ariaExpanded(state.choicesVisible); controls(DerivationPracticeId.CHOICES_PANEL)
                if (state.choicesVisible) disabled()
                onClick { state = state.copy(choicesVisible = true) }
            }) { Text(if (state.choicesVisible) "Auswahlhilfe sichtbar" else "Beziehungen zur Auswahl zeigen") }
        }
        if (state.hint != DerivationHint.NONE) Div(attrs = {
            elementId(DerivationPracticeId.HINT_ONE); practiceStyles(DerivationPracticeClass.HELP)
        }) {
            P { Strong { Text("Erster Hinweis: ") }; Text("Wie entfernst du den Nenner, damit links das Quadrat des Impulses allein steht?") }
        }
        if (state.hint == DerivationHint.PARTIAL_STEP) Div(attrs = {
            elementId(DerivationPracticeId.HINT_TWO); practiceStyles(DerivationPracticeClass.HELP)
        }) {
            P { Text("Multipliziere beide Seiten mit der doppelten Elektronenmasse:") }
            MathText("p² = 2mₑeU")
            P { Text("Gesucht ist ein Betrag. Welche Wurzel passt dazu?") }
        }
        if (state.choicesVisible) DerivationChoices(state) { state = state.choose(it) }
        Div(attrs = { practiceStyles(DerivationPracticeClass.ACTIONS) }) {
            Button(attrs = {
                elementId(DerivationPracticeId.COMPARE); styles(LabClass.PRIMARY)
                ariaExpanded(state.comparisonOpen); controls(DerivationPracticeId.COMPARISON)
                if (state.comparisonOpen) disabled()
                onClick { state = state.copy(comparisonOpen = true) }
            }) { Text(if (state.comparisonOpen) "Vergleich geöffnet" else "Mit dem Beispiel vergleichen") }
            Button(attrs = {
                elementId(DerivationPracticeId.CLOSE); styles(LabClass.SECONDARY)
                onClick { view.closePractice() }
            }) { Text("Zurück zur Herleitung") }
        }
        if (state.comparisonOpen) Div(attrs = {
            elementId(DerivationPracticeId.COMPARISON); practiceStyles(DerivationPracticeClass.EXPLANATION)
        }) {
            H4 { Text("Prüfe deinen Gedankengang") }
            InlineFormula(FormulaId.MOMENTUM)
            P { Text("Nach dem Multiplizieren liefert die positive Quadratwurzel den Impulsbetrag. Bis hierher wurden Energieerhaltung und nichtrelativistische Mechanik verwendet.") }
            InlineFormula(FormulaId.DE_BROGLIE)
            P { Text("Diese Zuordnung einer Wellenlänge zum Impuls ist die zusätzliche Hypothese. Energieerhaltung allein beweist sie nicht.") }
            P { Text("Eine andere algebraisch gültige Reihenfolge ist ebenso möglich. Du kannst die Hypothese vorher notieren und den Impuls danach einsetzen. Entscheidend sind gültige Beziehungen und ihre Voraussetzungen.") }
            P(attrs = { styles(LabClass.SMALL) }) { Text("Dein freier Text wird nicht automatisch bewertet. Vergleiche selbst, ob du die beiden Schritte jetzt begründen kannst.") }
        }
    }
}

@Composable
private fun DerivationChoices(state: DerivationPracticeState, select: (DerivationPracticeChoice) -> Unit) {
    Div(attrs = {
        elementId(DerivationPracticeId.CHOICES_PANEL); practiceStyles(DerivationPracticeClass.HELP)
    }) {
        P { Text("Auswahlhilfe: Welche Beziehung gibt den gesuchten Impulsbetrag direkt an? Alle drei Beziehungen sind gültig, erfüllen hier aber verschiedene Aufgaben.") }
        Div(attrs = { practiceStyles(DerivationPracticeClass.CHOICES); role(AriaRole.GROUP); ariaLabel("Beziehung für den Impulsbetrag auswählen") }) {
            DerivationPracticeChoice.entries.forEach { choice ->
                val (id, formula) = when (choice) {
                    DerivationPracticeChoice.MOMENTUM -> DerivationPracticeId.CHOICE_MOMENTUM to FormulaId.MOMENTUM
                    DerivationPracticeChoice.ENERGY -> DerivationPracticeId.CHOICE_ENERGY to FormulaId.KINETIC_MOMENTUM
                    DerivationPracticeChoice.HYPOTHESIS -> DerivationPracticeId.CHOICE_HYPOTHESIS to FormulaId.DE_BROGLIE
                }
                Button(attrs = {
                    elementId(id); styles(LabClass.SECONDARY); practiceStyles(DerivationPracticeClass.CHOICE)
                    if (state.choice == choice) practiceStyles(DerivationPracticeClass.SELECTED)
                    ariaPressed(state.choice == choice); ariaLabel(formula.alternative)
                    onClick { select(choice) }
                }) { InlineFormula(formula) }
            }
        }
        state.choice?.let { choice ->
            P(attrs = { elementId(DerivationPracticeId.CHOICE_FEEDBACK); role(AriaRole.STATUS) }) {
                Text(when (choice) {
                    DerivationPracticeChoice.MOMENTUM -> "Diese Beziehung gibt den Impulsbetrag direkt an. Erkläre auch, weshalb die positive Wurzel verwendet wird."
                    DerivationPracticeChoice.ENERGY -> "Diese Energiebeziehung gilt. Für die konkrete Lücke musst du sie noch nach dem Impulsbetrag auflösen."
                    DerivationPracticeChoice.HYPOTHESIS -> "Diese Beziehung ist de Broglies Hypothese. Sie ordnet dem Impuls eine Wellenlänge zu; den Impuls aus der Energie hast du damit noch nicht berechnet."
                })
            }
        }
    }
}
