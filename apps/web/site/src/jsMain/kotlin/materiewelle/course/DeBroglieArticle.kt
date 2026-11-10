package materiewelle.course

import materiewelle.learning.CompactTutorUiState
import materiewelle.learning.TutorHelpEntry
import materiewelle.learning.TutorHelpPoint
import androidx.compose.runtime.*
import materiewelle.core.Physics
import materiewelle.core.Voltage
import materiewelle.core.Experiment
import materiewelle.core.course.*
import materiewelle.state.LabSession
import materiewelle.course.content.CourseContent
import materiewelle.ui.*
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** One continuous explanation; persisted answers retain their original scientific identities. */
@Composable
fun DeBroglieArticle(session: LabSession, onNavigate: (AppRoute) -> Unit, viewState: DeBroglieUiState, tutorView: CompactTutorUiState) {
    val state = session.snapshot
    Article(attrs = { elementId(ArticleId.ROOT); articleStyles(ArticleClass.ROOT) }) {
        Header(attrs = { elementId(ArticleId.ENTRY); focusIndex(-1) }) {
            P(attrs = { articleStyles(ArticleClass.EYEBROW) }) { Text("Louis de Broglie · 1923/24") }
            H1(attrs = { elementId(ArticleId.TITLE); focusIndex(-1) }) { Text("Eine Wellenlänge für Elektronen") }
            P(attrs = { articleStyles(ArticleClass.LEAD) }) {
                Text("Elektronen können Beugungs- und Interferenzmuster erzeugen. Welche Wellenlänge sagt de Broglies Hypothese für ihren Impuls voraus? Und wie lässt sich diese Vorhersage experimentell prüfen?")
            }
            LivingLaboratory()
            P {
                Text("Die Herleitung führt von der Beschleunigungsspannung zur vorhergesagten Wellenlänge. Berechne anschließend den Wert für 150 Volt und unterscheide zwischen der Vorhersage und ihrer experimentellen Prüfung.")
            }
        }
        ArticleContents(AppSection.DE_BROGLIE, onNavigate)
        Div(attrs = { articleStyles(ArticleClass.GRID) }) {
            Section(attrs = { elementId(ArticleId.HYPOTHESIS); focusIndex(-1); articleStyles(ArticleClass.TEXT) }) {
                H2 { Text("Die Hypothese") }
                P { Text("De Broglie übertrug einen Zusammenhang aus der Lichtphysik auf Materie. Auch einem Elektron mit dem Impulsbetrag "); MathText("p"); Text(" soll eine Wellenlänge zugeordnet sein:") }
                Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.DE_BROGLIE) }
                P { MathText("λ"); Text(" ist die Wellenlänge, "); MathText("h"); Text(" das Plancksche Wirkungsquantum. Größerer Impuls bedeutet kleinere Wellenlänge. Gemeint sind weder der Durchmesser des Elektrons noch eine wellenförmige Flugbahn.") }
                P { Text("Die Wellenlänge bestimmt, bei welchen Wegunterschieden sich gestreute Wellenbeiträge verstärken oder abschwächen. Dadurch lässt sie sich an einem Beugungs- oder Interferenzmuster prüfen.") }
                P { Text("Das war eine überprüfbare Behauptung. Für ein beschleunigtes Elektron lässt sich der Impuls berechnen – und damit vorhersagen, auf welcher Längenskala Beugung auftreten müsste.") }
                P { Text("Im klassischen Teilchenbild beschreiben Ort und Impuls die Bewegung; eine eigene Wellenlänge gehört nicht dazu. Genau darin liegt der neue Schritt: Auch Materie soll Beugung und Interferenz zeigen können.") }
            }
            Figure(attrs = { articleStyles(ArticleClass.FIGURE) }) {
                ArticleAccelerationDrawing(Physics.electronWavelength(Voltage(150.0)).picometers, ArticleId.SKETCH)
                Figcaption { Text("Das elektrische Feld beschleunigt die Elektronen. Darunter zeigt das Schema die berechnete Wellenlänge bei 150 Volt in frei gewählter Darstellungsgröße; die gezeichnete Welle ist keine Elektronenbahn.") }
            }
        }
        Section(attrs = { elementId(ArticleId.HISTORY); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Der Ausgangspunkt: Quanten und Licht") }
            Div(attrs = { articleStyles(ArticleClass.GRID) }) {
                Div {
                    H3 { Text("Planck, 1900") }
                    P { Text("Planck untersuchte die Wärmestrahlung mit einem Modell schwingender Systeme, den Resonatoren. Er führte dafür Energieportionen ein. In der schulischen Kurzfassung tauschen diese Systeme Energie in Vielfachen einer Grundportion aus: "); MathText("Eₙ = nhf"); Text(". Die Frequenz "); MathText("f"); Text(" zählt Schwingungen pro Sekunde.") }
                }
                Div {
                    H3 { Text("Einstein, 1905") }
                    P { Text("Einstein bezog die Quantenidee auf das Licht selbst. Ein Lichtquant – heute Photon genannt – trägt die Energie "); MathText("Eγ = hf"); Text(". Licht besitzt damit Welleneigenschaften und überträgt Energie portionsweise. De Broglie fragte, ob auch Materie eine Wellenbeschreibung braucht.") }
                }
            }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Die Lichtanalogie motiviert die Idee. Sie beweist die Übertragung auf Elektronen nicht.") }
            Details {
                Summary { Text("Warum gerade der Zusammenhang zwischen Impuls und Wellenlänge?") }
                P { Text("Für ein Lichtquant gilt "); MathText("Eγ = hf"); Text(" und für seinen Impuls "); MathText("pγ = Eγ/c"); Text(". Mit der Lichtgeschwindigkeit "); MathText("c"); Text(" und "); MathText("λ = c/f"); Text(" ergibt sich "); MathText("λ = h/pγ"); Text(". De Broglie überträgt diesen Zusammenhang auf Materie. Für ein Elektron ist das eine neue Hypothese; sie folgt nicht allein aus diesen Lichtbeziehungen.") }
            }
        }
        Section(attrs = { elementId(ArticleId.DERIVATION); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Von der Spannung zur Wellenlänge") }
            P { Text("Aus der Spannung berechnen wir zunächst die Energie und den Impuls. De Broglies Hypothese ordnet diesem Impuls eine Wellenlänge zu.") }
            DerivationLearningArea(viewState) {
                ArticleDerivation(session, viewState)
                Details {
                    Summary { Text("Weshalb steht eine Wurzel im Ergebnis?") }
                    P { Text("Die kinetische Energie ist proportional zum Quadrat des Impulses. Nach dem Gleichsetzen mit der elektrischen Arbeit wird deshalb die positive Quadratwurzel gezogen. Wir berechnen den Betrag des Impulses. Die Wellenlänge ist umgekehrt proportional zu diesem Betrag.") }
                    P { Text("Einheitenkontrolle: "); MathText("Js / (kg m/s) = m"); Text(". Eine Wellenlänge ist eine Strecke.") }
                }
            }
        }
        Section(attrs = { elementId(ArticleId.CALCULATION); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Rechne für 150 Volt") }
            P { Text("Berechne die Wellenlänge für 150 Volt mit der hergeleiteten Formel. Notiere den Rechenweg und das Ergebnis mit Einheit. Mit „Kontrollrechnung öffnen“ kannst du deine Rechnung anschließend vergleichen.") }
            FieldLabel(ArticleId.NOTES) { Text("Dein Rechenweg") }
            TextArea(attrs = {
                elementId(ArticleId.NOTES); protectPersonalWork(session.isReadOnly); textRows(3); maxLength(4000)
                value(state.stationWork(StationId.S06_CALCULATION).note)
                onInput { event -> session.editWork(StationId.S06_CALCULATION) { it.copy(note = event.value.take(4000)) } }
            })
            NumericTask(session)
            TutorHelpEntry(TutorHelpPoint.CALCULATION, session, tutorView)
        }
        Section(attrs = { articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Vorhersage für 600 Volt") }
            P { Text("Wie ändern sich Energie, Impuls und Wellenlänge, wenn die Spannung von 150 auf 600 Volt steigt? Begründe deine Erwartung mit der hergeleiteten Beziehung in den Rechennotizen. Anschließend kannst du sie mit dem Modell vergleichen. Dessen Werte werden aus derselben Formel berechnet; sie sind keine neuen Messdaten.") }
            Details {
                Summary { Text("Den Zusammenhang noch einmal erklären") }
                P { Text("Vierfache Spannung ergibt vierfache kinetische Energie, doppelten Impuls und halbe Wellenlänge. Energie und Impuls wachsen nicht mit demselben Faktor, weil die kinetische Energie vom Quadrat des Impulses abhängt.") }
            }
            Details(attrs = { elementId(ArticleId.MODEL_DISCLOSURE) }) {
                Summary { Text("Berechnete Werte anzeigen und Spannung verändern") }
                ArticleModel(session)
                TutorHelpEntry(TutorHelpPoint.DE_BROGLIE_MODEL, session, tutorView)
            }
            P { Text("Bei 150 Volt liegt die Vorhersage in der Größenordnung atomarer Abstände. Ein Kristall mit periodisch angeordneten Atomen ist deshalb ein geeigneter Ort, um nach Elektronenbeugung zu suchen. Eine solche Größenordnung allein ist noch kein experimenteller Nachweis.") }
        }
        Section(attrs = { elementId(ArticleId.RECEPTION); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Wie wurde die Idee aufgenommen?") }
            P { Text("Nach de Broglies späterer Erinnerung war Paul Langevin zunächst skeptisch und bat Einstein um eine Einschätzung der Dissertation. Einsteins Zustimmung gab der Idee fachliches Gewicht.") }
            P { Text("Die historische Untersuchung von Sievers nennt Einsteins zustimmenden Brief an Langevin vom Dezember 1924 und Einsteins Verweis auf die Dissertation in einer Arbeit zur Gasstatistik Anfang 1925. So wurde die Idee weiteren Physikern bekannt. Ob die vorhergesagte Wellenlänge zu einem Beugungsbefund passt, mussten Experimente prüfen.") }
            P(attrs = { articleStyles(ArticleClass.SOURCE) }) { Text("H1: spätere Erinnerung, Kubli, Druckseite 36. H3: Sievers, Abschnitt II.5, Druckseiten 29–31; Sekundäranalyse der Rezeption.") }
            H3 { Text("Was ist bisher gezeigt?") }
            P { Text("Aus Energiegewinn und nichtrelativistischer Mechanik folgt der Impuls. Erst mit de Broglies zusätzlicher Hypothese folgt daraus eine Wellenlänge. Davisson und Germer prüfen diese Vorhersage mit Elektronen an einem Nickel-Einkristall.") }
            A(href = AppRoute.Laboratory(Experiment.DAVISSON_GERMER).hash, attrs = {
                styles(LabClass.PRIMARY); onClick { it.preventDefault(); onNavigate(AppRoute.Laboratory(Experiment.DAVISSON_GERMER)) }
            }) { Text("Zu Davisson–Germer") }
        }
        Section(attrs = { elementId(ArticleId.SOURCES); focusIndex(-1); articleStyles(ArticleClass.SECTION, ArticleClass.SMALL) }) {
            H2 { Text("Quellen und bisherige Notizen") }
            P { Text("Die historischen Aussagen folgen den geprüften Quellen H1 (Kubli, spätere Erinnerung, Druckseiten 30 und 36) und H3 (Sievers, Sekundäranalyse, Druckseiten 29–31).") }
            Ul {
                Li { A(href = "https://fondationlouisdebroglie.org/AFLB-481/aflb481m1018.pdf") { Text("F. Kubli: An interview with Louis de Broglie") }; Text(". Annales de la Fondation Louis de Broglie 48 (2023–2024), Druckseiten 30 und 36. Spätere Erinnerung (H1).") }
                Li { A(href = "https://arxiv.org/abs/physics/9807012") { Text("H. Sievers: Louis de Broglie und die Quantenmechanik (1998)") }; Text(", Druckseiten 29–31, PDF-Seiten 37–39. Sekundäranalyse (H3).") }
            }
            Details {
                Summary { Text("Bisherige Antworten zu diesem Abschnitt") }
                StationId.entries.filter { it.number <= 7 }.forEach { station ->
                    val work = state.stationWork(station)
                    if (work.note.isNotBlank() || work.numericAnswer.isNotBlank() || work.prediction.isNotBlank()) {
                        H3 { Text(CourseContent.stations.first { it.id == station }.title) }
                        if (work.note.isNotBlank()) P { MathText(work.note) }
                        if (work.numericAnswer.isNotBlank()) P { MathText(work.numericAnswer) }
                        if (work.prediction.isNotBlank()) P { MathText(work.prediction) }
                    }
                }
                if (StationId.entries.filter { it.number <= 7 }.none { state.stationWork(it).let { w -> w.note.isNotBlank() || w.numericAnswer.isNotBlank() || w.prediction.isNotBlank() } }) {
                    P { Text("Hier stehen noch keine eigenen Antworten.") }
                }
            }
        }
    }
}

@Composable
private fun ArticleModel(session: LabSession) {
    val model = session.snapshot.deBroglie
    val valid = Physics.inputVoltage(model.raw) != null
    val value = Physics.electronWavelength(Voltage(model.lastValidVoltage)).picometers
    Div(attrs = { articleStyles(ArticleClass.MODEL) }) {
        Div {
            FieldLabel(ArticleId.VOLTAGE) { Text("Beschleunigungsspannung in Volt") }
            Input(InputType.Text, attrs = { elementId(ArticleId.VOLTAGE); protectPersonalWork(session.isReadOnly); value(model.raw); ariaInvalid(!valid); maxLength(120); onInput { e -> session.update { it.copy(deBroglie = it.deBroglie.edit(e.value)) } } })
            Input(InputType.Range, attrs = { elementId(ArticleId.RANGE); protectPersonalWork(session.isReadOnly); ariaLabel("Beschleunigungsspannung"); minimum(20); maximum(1000); continuousStep(); value(model.lastValidVoltage); onInput { e -> e.value?.toDouble()?.let { selected -> session.update { it.copy(deBroglie = it.deBroglie.edit(selected.toString())) } } } })
            if (!valid) P(attrs = { styles(LabClass.ERROR); role(AriaRole.ALERT) }) { Text("Gib eine Spannung zwischen 20 und 1000 Volt ein. Die Darstellung behält den letzten gültigen Wert.") }
        }
        Div(attrs = { elementId(ArticleId.RESULT); articleStyles(ArticleClass.RESULT); role(AriaRole.STATUS) }) {
            P { Text("Vorhergesagte Wellenlänge") }
            MathText("${Physics.decimal(value, 3)} pm")
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { MathText("${Physics.decimal(model.lastValidVoltage, 0)} V"); Text(" · nichtrelativistisches Modell") }
        }
        Div(attrs = { elementId(ArticleId.COMPARE) }) {
            P { Text("Vergleich bei 600 Volt") }
            MathText("${Physics.decimal(Physics.electronWavelength(Voltage(600.0)).picometers, 3)} pm")
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { InlineFormula(FormulaId.SCALING) }
        }
    }
    ArticleAccelerationDrawing(value)
    Div(attrs = { articleStyles(ArticleClass.ACTIONS) }) {
        Button(attrs = {
            styles(LabClass.SECONDARY)
            protectPersonalWork(session.isReadOnly)
            onClick { session.update { it.copy(deBroglie = it.deBroglie.copy(comparisonVoltage = it.deBroglie.lastValidVoltage)) } }
        }) { Text("Einstellung merken") }
        model.comparisonVoltage?.let { comparison ->
            P { Text("Vergleichseinstellung: "); MathText("${Physics.decimal(comparison, 0)} V"); Text(" → "); MathText("${Physics.decimal(Physics.electronWavelength(Voltage(comparison)).picometers, 3)} pm") }
        }
    }
}
