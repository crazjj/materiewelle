package materiewelle.course

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import materiewelle.NativeSvg
import materiewelle.core.ScientificData
import materiewelle.pages.NickelSurfaceView
import materiewelle.pages.braggGeometryChart
import materiewelle.pages.nickelSurfaceChart
import materiewelle.pages.number
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Reading controls and the optional response have no access to LabSession mutation. */
@Composable
fun DgGeometryComparison(data: ScientificData, view: DgLessonView) {
    DisposableEffect(view.geometryTrialOpen) {
        val cancel = if (view.takeGeometryTrialFocus()) requestDgComparisonFocus(
            if (view.geometryTrialOpen) DgComparisonId.TRIAL_TITLE else DgComparisonId.START,
        ) else ({})
        onDispose { cancel() }
    }
    Details(attrs = { elementId(DgComparisonId.ROOT); dgComparisonStyles(DgComparisonClass.ROOT) }) {
        Summary { Text("Beide Geometrien gegenüberstellen") }
        if (view.geometryTrialOpen) {
            DgGeometryTrialView(view)
        } else {
            P {
                Text("Zwei getrennte Konstruktionen: Das Braggbeispiel verwendet "); MathText("30°")
                Text(" zur Ebene, der ausgewertete Oberflächenfall ungefähr ")
                MathText("${number(data.points.first().angleDegrees, 0)}°")
                Text(" zur äußeren Normalen. Die Auswahl unten zeigt jeweils den passenden Bezug; sie setzt die Winkel nicht gleich.")
            }
            Div(attrs = { dgComparisonStyles(DgComparisonClass.CONTROLS); role(AriaRole.GROUP); ariaLabel("Geometrischen Bezug in beiden Skizzen hervorheben") }) {
                DgComparisonFeature.entries.forEach { feature ->
                    Button(attrs = {
                        elementId(feature.controlId())
                        styles(LabClass.SECONDARY)
                        if (view.comparisonFeature == feature) dgComparisonStyles(DgComparisonClass.ACTIVE)
                        ariaPressed(view.comparisonFeature == feature)
                        onClick { view.comparisonFeature = if (view.comparisonFeature == feature) null else feature }
                    }) { Text(feature.label) }
                }
            }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) {
                Text("Kräftige Konturen zeigen die ausgewählten Strecken. Erneutes Betätigen hebt die Auswahl auf; alle Beziehungen bleiben lesbar.")
            }
            Div(attrs = { dgComparisonStyles(DgComparisonClass.FIGURES) }) {
                Figure(attrs = { elementId(DgComparisonId.BRAGG); dgComparisonStyles(DgComparisonClass.FIGURE) }) {
                    H3 { Text("Bragg: zwei Netzebenen") }
                    Div(attrs = { dgComparisonStyles(DgComparisonClass.DRAWING) }) {
                        NativeSvg(braggGeometryChart(2, view.comparisonFeature?.braggFocus, BraggPresentation.COMPARISON))
                    }
                    Div(attrs = { elementId(DgComparisonId.BRAGG_FORMULA); dgComparisonStyles(DgComparisonClass.FORMULA) }) {
                        InlineFormula(when (view.comparisonFeature) {
                            DgComparisonFeature.REFERENCE -> FormulaId.BRAGG_FOCUS_ANGLE
                            DgComparisonFeature.DISTANCE -> FormulaId.BRAGG_FOCUS_SPACING
                            DgComparisonFeature.PATH -> FormulaId.BRAGG_FOCUS_PATH
                            null -> FormulaId.BRAGG
                        })
                    }
                    Figcaption(attrs = { dgComparisonStyles(DgComparisonClass.CAPTION) }) {
                        P { MathText("θB = 30°"); Text(" zur Netzebene; allgemeines Erklärbeispiel, kein Winkelwert des ausgewerteten DG-Befunds.") }
                        P { BraggFocusExplanation(view.comparisonFeature) }
                    }
                }
                Figure(attrs = { elementId(DgComparisonId.SURFACE); dgComparisonStyles(DgComparisonClass.FIGURE) }) {
                    H3 { Text("Oberfläche: ausgewählte Reihen") }
                    Div(attrs = { dgComparisonStyles(DgComparisonClass.DRAWING) }) {
                        NativeSvg(nickelSurfaceChart(data.points.first().angleDegrees, data.dgStructure.latticeConstant.nanometers,
                            NickelSurfaceView.COMPARISON, view.comparisonFeature?.surfaceFocus))
                    }
                    Div(attrs = { elementId(DgComparisonId.SURFACE_FORMULA); dgComparisonStyles(DgComparisonClass.FORMULA) }) {
                        InlineFormula(when (view.comparisonFeature) {
                            DgComparisonFeature.REFERENCE -> FormulaId.SURFACE_FOCUS_ANGLE
                            DgComparisonFeature.DISTANCE -> FormulaId.SURFACE_FOCUS_SPACING
                            DgComparisonFeature.PATH -> FormulaId.SURFACE_FOCUS_PATH
                            null -> FormulaId.SURFACE
                        })
                    }
                    Figcaption(attrs = { dgComparisonStyles(DgComparisonClass.CAPTION) }) {
                        P { MathText("φ ≈ ${number(data.points.first().angleDegrees, 0)}°"); Text(" zur äußeren Normalen; ausgewähltes Oberflächenmodell nach D2. Normaler Einfall, erste Ordnung.") }
                        P { SurfaceFocusExplanation(view.comparisonFeature) }
                    }
                }
            }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Die Skizzen haben unterschiedliche Darstellungsmaßstäbe. Netzebenenabstand und Reihenabstand sind verschiedene Strukturgrößen; kein Winkel wird hier pauschal halbiert.") }
            Button(attrs = {
                elementId(DgComparisonId.START); styles(LabClass.SECONDARY)
                onClick { view.openGeometryTrial() }
            }) { Text("Zuordnung selbst prüfen") }
        }
    }
}

@Composable
private fun BraggFocusExplanation(feature: DgComparisonFeature?) {
    when (feature) {
        DgComparisonFeature.REFERENCE -> { Text("Markiert: die Ebene als Bezugslinie und der Winkel "); MathText("θB"); Text(" zu ihr.") }
        DgComparisonFeature.DISTANCE -> { Text("Markiert: der senkrechte Abstand "); MathText("d"); Text(" zwischen den Netzebenen. Er ist kein Reihenabstand auf der Oberfläche.") }
        DgComparisonFeature.PATH -> { Text("Markiert: beide Zusatzwege "); MathText("PB + BQ = 2d sin θB"); Text(". Vor und nach der Streuung kommt je ein Beitrag hinzu.") }
        null -> { MathText("d"); Text(" steht senkrecht zwischen den Ebenen. Der Winkel wird zur Ebene gemessen; beide Zusatzwege ergeben den Gangunterschied.") }
    }
}

@Composable
private fun SurfaceFocusExplanation(feature: DgComparisonFeature?) {
    when (feature) {
        DgComparisonFeature.REFERENCE -> { Text("Markiert: die äußere Normale als Bezugslinie und der Winkel "); MathText("φ"); Text(" zum auslaufenden Strahl, nicht zur einfallenden Pfeilrichtung.") }
        DgComparisonFeature.DISTANCE -> { Text("Markiert: "); MathText("AB = D"); Text(" zwischen den ausgewählten Oberflächenreihen. Der Netzebenenabstand "); MathText("d₁₁₁"); Text(" ist eine andere Länge.") }
        DgComparisonFeature.PATH -> { Text("Markiert: nur der auslaufende Zusatzweg "); MathText("AC = D sin φ"); Text(". Beim normalen Einfall ist der einlaufende Gangunterschied null.") }
        null -> { MathText("D"); Text(" liegt zwischen den ausgewählten Reihen. Bei normalem Einfall bleibt nur der auslaufende Zusatzweg; der Winkel ist auf die äußere Normale bezogen.") }
    }
}

@Composable
internal fun GeometryComparisonTable() {
    Table(attrs = { articleStyles(ArticleClass.REFERENCE_TABLE) }) {
        Caption { Text("Winkel und Abstände im Vergleich") }
        Thead { Tr { Th { Text("Bezug") }; Th { Text("Bragg-Konstruktion") }; Th { Text("Ausgewerteter DG-Fall") } } }
        Tbody {
            Tr { Th { Text("Abstand") }; Td { MathText("d"); Text(" zwischen Netzebenen") }; Td { MathText("D"); Text(" zwischen Oberflächenreihen") } }
            Tr { Th { Text("Winkel") }; Td { MathText("θB"); Text(" zur Ebene") }; Td { MathText("φ"); Text(" zur äußeren Normalen") } }
            Tr {
                Th { Text("Zusatzwege") }
                Td { Text("Zwei, vor und nach der Streuung: "); MathText("2d sin θB") }
                Td { Text("Einer, beim Auslaufen nach senkrechtem Einfall: "); MathText("D sin φ") }
            }
        }
    }
}

@Composable
private fun DgGeometryTrialView(view: DgLessonView) {
    val trial = view.geometryTrial
    Section(attrs = { elementId(DgComparisonId.TRIAL); dgComparisonStyles(DgComparisonClass.TRIAL); labelledBy(DgComparisonId.TRIAL_TITLE) }) {
        H3(attrs = { elementId(DgComparisonId.TRIAL_TITLE); focusIndex(-1) }) { Text("Welche Länge und welcher Winkel gehören zum Oberflächenmodell dieses Zahlenfalls?") }
        P { Text("Ordne Winkel und Abstand zu und begründe deine Wahl. Bei Bedarf kannst du zur Erklärung zurückkehren.") }
        P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Übung – nur in diesem Tab: Auswahl und Begründung bleiben bis zum Neuladen erhalten und stehen nicht im Arbeitsbericht.") }
        Div(attrs = { dgComparisonStyles(DgComparisonClass.FIELDS) }) {
            Div(attrs = { dgComparisonStyles(DgComparisonClass.FIELD) }) {
                FieldLabel(DgComparisonId.ANGLE) { Text("Worauf ist der berichtete Winkel bezogen?") }
                Select(attrs = {
                    elementId(DgComparisonId.ANGLE)
                    onChange { event -> event.value?.let(DgTrialAngle::fromKey)?.let { view.geometryTrial = view.geometryTrial.chooseAngle(it) } }
                }) {
                    DgTrialAngle.entries.forEach { choice ->
                        Option(value = choice.key, attrs = { if (trial.angle == choice) selected() }) { Text(choice.label) }
                    }
                }
            }
            Div(attrs = { dgComparisonStyles(DgComparisonClass.FIELD) }) {
                FieldLabel(DgComparisonId.LENGTH) { Text("Welche geometrische Länge wird verwendet?") }
                Select(attrs = {
                    elementId(DgComparisonId.LENGTH)
                    onChange { event -> event.value?.let(DgTrialLength::fromKey)?.let { view.geometryTrial = view.geometryTrial.chooseLength(it) } }
                }) {
                    DgTrialLength.entries.forEach { choice ->
                        Option(value = choice.key, attrs = { if (trial.length == choice) selected() }) { Text(choice.label) }
                    }
                }
            }
        }
        FieldLabel(DgComparisonId.NOTE) { Text("Deine Begründung für diese Zuordnung") }
        TextArea(attrs = {
            elementId(DgComparisonId.NOTE); textRows(3); maxLength(DgGeometryTrial.MAX_NOTE_LENGTH)
            value(trial.note)
            onInput { view.geometryTrial = view.geometryTrial.writeNote(it.value) }
        })
        Div(attrs = { dgComparisonStyles(DgComparisonClass.CONTROLS) }) {
            Button(attrs = { elementId(DgComparisonId.HINT); styles(LabClass.SECONDARY); onClick { view.geometryTrial = view.geometryTrial.requestHint() } }) { Text("Hinweis zur Bezugslinie") }
            Button(attrs = { elementId(DgComparisonId.COMPARE); styles(LabClass.SECONDARY); onClick { view.geometryTrial = view.geometryTrial.compare() } }) { Text("Zuordnung vergleichen") }
        }
        if (trial.hintOpened) P { Text("Welche Linie steht senkrecht auf der Oberfläche? Welche beiden Strukturen vertreten die Punkte A und B in der Seitenprojektion?") }
        trial.feedback?.let { feedback ->
            Div(attrs = { elementId(DgComparisonId.FEEDBACK); dgComparisonStyles(DgComparisonClass.FEEDBACK); role(AriaRole.STATUS) }) {
                when (feedback) {
                    DgTrialFeedback.Incomplete -> P { Text("Wähle zuerst einen Winkelbezug und eine Länge. Deine bisherige Auswahl und Begründung bleiben erhalten.") }
                    is DgTrialFeedback.Compared -> {
                        P { Text(if (feedback.angleMatches && feedback.lengthMatches) "Die Zuordnung passt zu diesem Oberflächenmodell." else "Prüfe den geometrischen Bezug deiner Zuordnung:") }
                        P {
                            Text(if (feedback.angleMatches) "Dein Winkelbezug passt: " else "Beim Winkel korrigieren: ")
                            MathText("φ"); Text(" wird zur äußeren Oberflächennormalen gemessen. Der Winkel zur Ebene gehört zum getrennten Braggbeispiel.")
                        }
                        P {
                            Text(if (feedback.lengthMatches) "Deine Längenzuordnung passt: " else "Bei der Länge korrigieren: ")
                            MathText("D"); Text(" ist der Abstand der ausgewählten Oberflächenreihen; der Netzebenenabstand ist eine andere Strukturgröße.")
                        }
                        P { Text("Deine freie Begründung wird nicht automatisch bewertet. Vergleiche selbst: Hast du Bezugslinie und Strukturen benannt und die beiden Modellfälle getrennt?") }
                    }
                }
            }
        }
        Div(attrs = { dgComparisonStyles(DgComparisonClass.CONTROLS) }) {
            Button(attrs = { elementId(DgComparisonId.CLOSE); styles(LabClass.SECONDARY); onClick { view.closeGeometryTrial() } }) { Text("Erklärung wieder ansehen") }
            Button(attrs = { elementId(DgComparisonId.RESET); styles(LabClass.SECONDARY); onClick { view.resetGeometryTrial() } }) { Text("Zuordnung und Begründung zurücksetzen") }
        }
        P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Beim Nachschlagen bleibt deine Probe erhalten. Keine Auswahl oder Hervorhebung verändert die historischen Rechnungen oder deine dortigen Antworten.") }
    }
}

private fun DgComparisonFeature.controlId(): DgComparisonId = when (this) {
    DgComparisonFeature.REFERENCE -> DgComparisonId.REFERENCE
    DgComparisonFeature.DISTANCE -> DgComparisonId.DISTANCE
    DgComparisonFeature.PATH -> DgComparisonId.PATH
}
