package materiewelle.course

import materiewelle.learning.CompactTutorUiState
import materiewelle.learning.TutorHelpButton
import materiewelle.learning.TutorHelpPanel
import materiewelle.learning.TutorHelpPoint
import androidx.compose.runtime.Composable
import materiewelle.core.ScientificData
import materiewelle.core.ReferenceMaterial
import materiewelle.core.course.AppRoute
import materiewelle.core.course.StationId
import materiewelle.course.content.CourseContent
import materiewelle.course.content.ContentSource
import materiewelle.data.LabAsset
import materiewelle.state.LabSession
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private fun AttrsScope<*>.conclusionStyles(vararg values: ConclusionClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** An authored synthesis and a separate empty personal draft; never generated student work. */
@Composable
fun ConclusionArticle(session: LabSession, data: ScientificData?, tutorView: CompactTutorUiState, onOpenWork: (AppRoute, ElementId) -> Unit) {
    val work = session.snapshot.stationWork(StationId.S16_CONCLUSION)
    Article(attrs = { elementId(ConclusionId.ROOT); articleStyles(ArticleClass.ROOT) }) {
        Header {
            H1(attrs = { elementId(ConclusionId.TITLE); focusIndex(-1) }) { Text("Fazit") }
            P(attrs = { articleStyles(ArticleClass.LEAD) }) {
                Text("Von einer Hypothese zum messbaren Muster: De Broglie, Davisson–Germer und Jönsson untersuchen dieselbe Frage mit unterschiedlichen Mitteln.")
            }
        }
        ConclusionSynthesis()

        Section(attrs = { articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Dein eigener Schluss") }
            P { Text("Welche deiner Rechnungen und Beobachtungen stützen diesen Zusammenhang? Halte deine Begründung fest und nenne eine Grenze deiner Auswertung.") }
            OwnWorkSummary(session.snapshot, data, onOpenWork)
            FieldLabel(ConclusionId.DRAFT) { Text("Dein Fazit") }
            TextArea(attrs = {
                elementId(ConclusionId.DRAFT); protectPersonalWork(session.isReadOnly); textRows(6); maxLength(4000); value(work.note)
                onInput { event -> session.editWork(StationId.S16_CONCLUSION) { it.copy(note = event.value.take(4000)) } }
            })
            P(attrs = { articleStyles(ArticleClass.SMALL) }) {
                Text("Falls du KI genutzt hast, ergänze hier kurz, welche Teile du selbst nachgerechnet, an Quellen geprüft oder eigenständig erklärt hast. Beschreibe nur, was du tatsächlich gemacht hast.")
            }
            Details(attrs = { elementId(ConclusionId.EVIDENCE) }) {
                Summary { Text("Freiwillig: Aussagen mit Belegen ergänzen") }
                ConclusionEvidenceTable(session)
            }
            Details {
                Summary { Text("Deinen Arbeitsbericht lesen und kopieren") }
                P { Text("Der Arbeitsbericht fasst deine Rechnung für 150 Volt, den DG-Vergleich, deine Bildmessung und dein Fazit zusammen. Prüfe deine Angaben vor dem Kopieren. Die Fragen darunter helfen dir, die Zusammenhänge ohne Vorlage zu erklären.") }
                EvidenceReport(session, data)
            }
            Details {
                Summary { Text("Drei Fragen fürs Fachgespräch") }
                Ol {
                    Li { Text("An welcher Stelle deiner Herleitung kommt eine neue physikalische Hypothese hinzu? Erkläre den Unterschied zwischen Energieerhaltung und de Broglies Ansatz.") }
                    Li { Text("Welche Länge und welchen Winkel verwendest du beim Kristallvergleich? Zeige sie in der passenden Zeichnung und erkläre, warum du sie nicht aus dem gewünschten Ergebnis bestimmen darfst.") }
                    Li { Text("Was hast du am Jönsson-Bild selbst gemessen? Erkläre, warum stärkerer Zoom das Ergebnis nicht ändert und welche zusätzliche Information du für eine absolute Wellenlänge bräuchtest.") }
                }
                P { Text("Beantworte die Fragen zunächst ohne nachzulesen. Begründe deine Antworten mit deinen eigenen Rechenwerten und Bildmarken.") }
            }
        }

        ConclusionResources(session, tutorView)
        SourcesSection()
    }
}

/** The shared argument is visible before the learner's personal draft, never substituted for it. */
@Composable
private fun ConclusionSynthesis() {
    Section(attrs = { elementId(ConclusionId.SYNTHESIS); conclusionStyles(ConclusionClass.SYNTHESIS) }) {
        H2 { Text("Wie die drei Kapitel zusammenhängen") }
        Ol(attrs = { conclusionStyles(ConclusionClass.CHAIN) }) {
            ConclusionStep("Eine Wellenlänge vorhersagen", "De Broglie · 1923/24") {
                P {
                    Text("Aus der Beschleunigungsspannung berechnen wir den Impuls. Die zusätzliche Hypothese ")
                    MathText("λ = h/p")
                    Text(" ordnet ihm eine Wellenlänge zu. Sie folgt nicht aus der Energieerhaltung allein und muss am Versuch geprüft werden.")
                }
                P { Text("Einsteins Zustimmung trug zur wissenschaftlichen Aufnahme der Idee bei. Sie war eine theoretische Einschätzung, noch kein experimenteller Nachweis.") }
            }
            ConclusionStep("Die Vorhersage prüfen", "Davisson–Germer · 1927") {
                P { Text("Davisson und Germer beobachteten eine bevorzugte Streurichtung am Nickelkristall. Aus dieser Richtung und der unabhängig bekannten Kristallstruktur lässt sich im gewählten Modell eine Wellenlänge bestimmen. Für den hier betrachteten, in D2 berichteten Fall liegt sie nahe an de Broglies Vorhersage aus der Spannung.") }
            }
            ConclusionStep("Interferenz an Spalten untersuchen", "Jönsson · 1961") {
                P { Text("Jönsson beobachtete Interferenzstreifen hinter künstlich hergestellten Spalten. Damit zeigte sich das Wellenverhalten auch an einer gezielt hergestellten Struktur außerhalb des Kristallgitters. Die historische Aufnahme ermöglicht eine Messung der Streifenabstände in Pixeln; das getrennte Lehrmodell erklärt den Einfluss von Wellenlänge und Spaltgeometrie.") }
            }
        }
        P(attrs = { conclusionStyles(ConclusionClass.STATEMENT) }) {
            Text("Die Versuche stützen eine Wellenbeschreibung von Elektronen: Ihr Impuls bestimmt die zugeordnete Wellenlänge, die Versuchsgeometrie das Beugungs- und Interferenzmuster.")
        }
        P {
            Text("Ein Streifenbild zeigt keine wellenförmige Flugbahn. Die Bildauswertung in diesem Kapitel liefert Abstände in Originalpixeln. Ohne einen unabhängig belegten Bildmaßstab lässt sich daraus keine absolute Wellenlänge bestimmen.")
        }
        Details {
            Summary { Text("Was lässt sich mit den verwendeten Quellen belegen?") }
            P { Text("Der DG-Befund wird in der Sekundäranalyse D2 berichtet. Die beiden berechneten Wellenlängen liegen nahe beieinander. Ob sie innerhalb der damaligen Messgenauigkeit übereinstimmen, lässt sich ohne historische Messunsicherheiten jedoch nicht beurteilen. Der Strukturabstand muss unabhängig von der vorhergesagten Wellenlänge feststehen.") }
            P { Text("Die reproduzierte Jönsson-Aufnahme dokumentiert das historische Streifenmuster. Die interaktive Spaltdarstellung berechnet dagegen ein Modellmuster. Sie erklärt dessen Abhängigkeit von den Modellparametern, liefert aber keinen nachträglichen Maßstab für die historische Aufnahme.") }
        }
    }
}

@Composable
private fun ConclusionStep(title: String, historicalContext: String, content: @Composable () -> Unit) {
    Li(attrs = { conclusionStyles(ConclusionClass.STEP) }) {
        Div {
            H3 { Text(title) }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text(historicalContext) }
        }
        Div { content() }
    }
}

@Composable
private fun ConclusionResources(session: LabSession, tutorView: CompactTutorUiState) {
    Section(attrs = { elementId(ConclusionId.RESOURCES); articleStyles(ArticleClass.SECTION) }) {
        H2 { Text("Zum Nachschlagen und Weiterfragen") }
        P { Text("Zum Nachlesen fasst das Handout Herleitung, Versuche und Aussagegrenzen auf drei Seiten zusammen.") }
        Div(attrs = { conclusionStyles(ConclusionClass.ACTIONS) }) {
            A(href = LabAsset.HANDOUT_PDF.path, attrs = {
                elementId(ConclusionId.HANDOUT); styles(LabClass.PRIMARY)
                downloadFile("materiewellen-handout.pdf")
                onClick { session.updateMeasurement { it.recordReferenceMaterial(ReferenceMaterial.HANDOUT_REQUESTED) } }
            }) { Text("Handout herunterladen · 3 Seiten PDF") }
        }
        P(attrs = { articleStyles(ArticleClass.SMALL) }) {
            Text("Deine hier eingetragenen Rechnungen, Bildmessungen und Texte kannst du im Abschnitt „Dein eigener Schluss“ als Arbeitsbericht kopieren. Das allgemeine Handout enthält diese Eingaben nicht.")
        }
        H3 { Text("Eine offene Frage klären") }
        P { Text("Die KI-Hilfe bereitet zu deiner Frage eine Nachricht mit Fachkontext vor. Kopiere sie vollständig in deinen eigenen ChatGPT- oder Claude-Chat und sende sie dort ab. Prüfe die Antwort und überarbeite danach die betreffende Begründung in deinem Fazit. Die Website erhält keine Chatantwort.") }
        Div(attrs = { conclusionStyles(ConclusionClass.ACTIONS) }) {
            TutorHelpButton(TutorHelpPoint.CONCLUSION, tutorView)
        }
        TutorHelpPanel(TutorHelpPoint.CONCLUSION, session, tutorView)
    }
}

@Composable
fun SourcesSection() {
    Section(attrs = { elementId(ConclusionId.SOURCES); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
        H2 { Text("Quellen und Arbeitsweise") }
        P { Text("Fachtexte, Code und Zeichnungen wurden mit KI-Unterstützung entwickelt und anhand der angegebenen Quellen und Rechnungen geprüft. Eigene Antworten und Bildmarken sind davon getrennte Eingaben. Eine Softwareprüfung ersetzt kein persönliches Fachgespräch.") }
        P { Text("Der DG-Zahlenfall stammt aus einer Sekundäranalyse; das historische Original wurde hier nicht eingesehen. Beim Jönsson-Bild sind physikalische Kalibrierung und Aufnahmezuordnung nicht vollständig geklärt. Die Grundlage der öffentlichen Bildnutzung bleibt als Quellenfrage offen.") }
        CourseContent.literature.forEach { SourceEntry(it) }
        Details {
            Summary { Text("Aufgabenblatt, eigene Modelle und Bilddatei") }
            CourseContent.workingMaterials.forEach { SourceEntry(it) }
            P {
                A(href = LabAsset.SCIENTIFIC_DATA.path) { Text("Versuchsdaten (JSON)") }
                Text(" · Fachdaten der Modelle und Quellen, ohne deine persönlichen Antworten.")
            }
        }
        Details {
            Summary { Text("Was beim Handout-Aufruf gespeichert wird") }
            P { Text("Der Arbeitsstand vermerkt den Klick auf den Handout-Link. Ob die PDF heruntergeladen oder gelesen wurde, wird nicht erfasst. Direkte PDF-Aufrufe und frühere Zugriffe außerhalb dieser Erfassung fehlen im Protokoll.") }
        }
    }
}

@Composable
private fun SourceEntry(source: ContentSource) {
    H3 { Text("${source.title} · ${source.id}") }
    P {
        if (source.url != null) A(href = source.url, attrs = { externalLink() }) { Text(source.location) }
        else Text(source.location)
    }
    P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text(source.status) }
}
