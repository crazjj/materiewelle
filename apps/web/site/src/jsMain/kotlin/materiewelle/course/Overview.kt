package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.core.course.*
import materiewelle.data.LabAsset
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private fun AttrsScope<*>.overviewStyles(vararg values: OverviewClass) {
    classes(*values.map { it.cssName }.toTypedArray())
}

/** A short orientation, independent of scientific work and of the formula catalogue. */
@Composable
fun Overview(readingPosition: ReadingPosition?, tutorSection: AppSection, onNavigate: (AppRoute) -> Unit) {
    Article(attrs = { elementId(OverviewId.ROOT); overviewStyles(OverviewClass.ROOT) }) {
        Div(attrs = { overviewStyles(OverviewClass.INTRO) }) {
            Div {
                P { Text("Materiewellen · eine Untersuchung in vier Kapiteln") }
                H1(attrs = { elementId(OverviewId.TITLE); focusIndex(-1) }) {
                    Text("Wie zeigt Materie Welleneigenschaften?")
                }
                P(attrs = { overviewStyles(OverviewClass.LEAD) }) {
                    Text("Elektronen können Streifenmuster erzeugen, wie wir sie von Licht kennen. De Broglies Hypothese ordnet Elektronen eine Wellenlänge zu. Wie lässt sie sich berechnen, und wie kann ein Versuch diese Vorhersage prüfen?")
                }
                P {
                    Text("Mit de Broglies Hypothese berechnest du die Wellenlänge eines beschleunigten Elektrons. Du vergleichst die Vorhersage mit einem Kristallversuch und misst Streifenabstände in einer historischen Doppelspaltaufnahme. Zum Schluss begründest du, was diese Befunde über Elektronen aussagen.")
                }
                Div(attrs = { overviewStyles(OverviewClass.ACTIONS) }) {
                    val target = readingPosition?.route ?: AppSection.DE_BROGLIE.route
                    A(href = target.hash, attrs = {
                        elementId(if (readingPosition == null) OverviewId.START else ConclusionId.RESUME)
                        styles(LabClass.PRIMARY)
                        onClick { it.preventDefault(); onNavigate(target) }
                    }) {
                        Text(if (readingPosition == null) "Mit De Broglie beginnen" else "Weiter bei: ${readingPosition.section.title}")
                    }
                    if (readingPosition != null) A(href = AppSection.DE_BROGLIE.route.hash, attrs = {
                        elementId(OverviewId.START); styles(LabClass.SECONDARY)
                        onClick { it.preventDefault(); onNavigate(AppSection.DE_BROGLIE.route) }
                    }) { Text("Zum Anfang") }
                    A(href = AppRoute.Tutor.hash, attrs = {
                        styles(LabClass.SECONDARY)
                        onClick { it.preventDefault(); onNavigate(AppRoute.Tutor) }
                    }) { Text("Hilfe mit deinem KI-Chat") }
                }
                P {
                    Text("Für einen Hinweis, eine Erklärung oder die Prüfung deines Ansatzes kannst du deinen eigenen ChatGPT- oder Claude-Chat nutzen. Diese Website bereitet die passende Nachricht dafür vor.")
                }
                P(attrs = { overviewStyles(OverviewClass.HELP_CONTEXT) }) {
                    Text("Die KI-Hilfe startet hier beim Kapitel „${tutorSection.title}“. Zu einer anderen Aufgabe öffnest du die Hilfe direkt im jeweiligen Kapitel.")
                }
            }
            Figure(attrs = { overviewStyles(OverviewClass.IMAGE) }) {
                Img(src = LabAsset.ORIGINAL_IMAGE.path,
                    alt = "Historische Elektronenaufnahme mit abwechselnd hellen und dunklen Streifen")
                Figcaption {
                    Text("Jönssons Elektronenaufnahme, 1961 · digitale Reproduktion aus Quelle J1. Im dritten Kapitel markierst du darin benachbarte Streifenmaxima.")
                }
            }
        }
        Ol(attrs = { elementId(OverviewId.CHAPTERS); overviewStyles(OverviewClass.CHAPTERS); ariaLabel("Die vier Kapitel") }) {
            AppSection.entries.forEachIndexed { index, section ->
                Li(attrs = { overviewStyles(OverviewClass.CHAPTER) }) {
                    Span(attrs = { overviewStyles(OverviewClass.NUMBER); ariaHidden(true) }) { Text("${index + 1}") }
                    Div {
                        A(href = section.route.hash, attrs = { onClick { it.preventDefault(); onNavigate(section.route) } }) { Text(section.title) }
                        P { Text(when (section) {
                            AppSection.DE_BROGLIE -> "Welche Wellenlänge sagen wir für ein beschleunigtes Elektron voraus?"
                            AppSection.DAVISSON_GERMER -> "Passt diese Vorhersage zur Beugung am Nickelkristall?"
                            AppSection.JONSSON -> "Wie hängt das Streifenmuster von den Spalten ab, und was lässt sich im Bild messen?"
                            AppSection.CONCLUSION -> "Welche Schlussfolgerungen lassen deine Rechnungen und Messungen zu, und wo liegen ihre Grenzen?"
                        }) }
                    }
                }
            }
        }
        Section(attrs = { elementId(OverviewId.HELP); overviewStyles(OverviewClass.SUPPORT) }) {
            H2 { Text("So nutzt du die KI-Hilfe") }
            Ol {
                Li {
                    Text("Wähle „Hinweis“, „Erklärung“ oder „Ansatz prüfen“ und ergänze bei Bedarf deine Frage. Der passende Fachkontext wird automatisch hinzugefügt.")
                }
                Li {
                    Text("Kopiere die ganze vorbereitete Nachricht, füge sie in deinen eigenen ChatGPT- oder Claude-Chat ein und sende sie dort ab. Die Antwort und deine Folgefragen bleiben in diesem Chat.")
                }
                Li {
                    Text("Komm danach hierher zurück und arbeite an deiner Rechnung, Messung oder Begründung weiter. Die Website erhält keine Chatantwort.")
                }
            }
            Details {
                Summary { Text("Vorkenntnisse und Arbeitsweise") }
                P { Text("Der Lernpfad knüpft an Energie und Impuls aus der Mechanik sowie den Sinus im rechtwinkligen Dreieck an. Kurze Hilfen erklären die nötigen Zusammenhänge direkt bei den Rechenschritten.") }
                P { Text("Du hältst eine Rechnung, begründete Bildmarken und ein Fazit fest. Modelle und Hilfen stehen direkt bei den Aufgaben. Die freiwilligen Phasen- und Trefferansichten findest du am Ende des Jönsson-Kapitels, das dreiseitige Handout im Fazit.") }
                P { Text("Laptop oder iPad im Querformat eignen sich zum Arbeiten.") }
            }
        }
    }
}
