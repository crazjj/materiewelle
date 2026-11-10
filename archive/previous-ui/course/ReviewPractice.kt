package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.core.course.QuestionBank
import materiewelle.core.course.QuestionId
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** Authored follow-up identities, separate from the original multiple-choice attempts. */
private enum class ReviewVariant(
    val source: QuestionId,
    val key: String,
    val title: String,
    val hint: String,
    val control: List<String>,
) {
    MOMENTUM(
        QuestionId.Q01, "R-Q01-3P", "Impuls verändern",
        "Setze den neuen Impuls in dieselbe Beziehung ein. Vergleiche erst die beiden Brüche, bevor du eine Zahl einsetzt.",
        listOf(
            "Beim dreifachen Impuls bleibt h gleich. Der Nenner ist dreimal so groß; die zugeordnete Wellenlänge beträgt ein Drittel des Ausgangswerts.",
            "Der Vergleich betrifft die Wellenlänge. Eine Aussage über den Durchmesser des Elektrons folgt daraus nicht.",
        ),
    ),
    ENERGY_HYPOTHESIS(
        QuestionId.Q09, "R-Q09-K0", "Anfangsenergie und Hypothese",
        "Welche Größe ändert sich durch die zusätzliche Anfangsenergie? Welche Beziehung ordnet dem Impuls erst eine Wellenlänge zu?",
        listOf(
            "Die Energiebilanz enthält jetzt die Anfangsenergie zusätzlich zur elektrischen Arbeit: E_kin = E_kin,0 + eU. Danach wird im nichtrelativistischen Modell der Impuls bestimmt.",
            "λ = h/p bleibt der hypothetische Anschluss. Die geänderte Energiebilanz beweist diese Beziehung nicht.",
        ),
    ),
    VOLTAGE(
        QuestionId.Q02, "R-Q02-9U", "Drei Faktoren begründen",
        "Verfolge die Kette von Spannung zu Energie, von Energie zu Impuls und erst dann zur Wellenlänge. Die mittlere Beziehung enthält eine Quadratwurzel.",
        listOf(
            "Bei neunfacher Spannung und Start aus Ruhe ist die kinetische Energie neunmal so groß. Der Impuls wächst um den Faktor drei; die Wellenlänge beträgt ein Drittel.",
            "Die Faktoren gelten bei gleichen übrigen Bedingungen im nichtrelativistischen Modell. Eine lineare Änderung aller drei Größen wäre falsch.",
        ),
    ),
    SOURCES(
        QuestionId.Q03, "R-Q03-BELEGE", "Zwei Belege einordnen",
        "Frage bei jedem Dokument: Was wurde tatsächlich getan oder beobachtet, und was lässt sich daraus gerade nicht ableiten?",
        listOf(
            "Ein Empfehlungsschreiben belegt eine Einschätzung beziehungsweise wissenschaftliche Aufnahme, sofern Herkunft und Inhalt geprüft sind. Es enthält für sich keine Messbestätigung.",
            "Ein protokollierter Detektorscan enthält einen experimentellen Befund. Für einen Vergleich mit der Hypothese braucht er zusätzlich dokumentierte Versuchsbedingungen, Geometrie und ein passendes Auswertemodell.",
        ),
    ),
    DETECTOR(
        QuestionId.Q04, "R-Q04-MESSKETTE", "Eine Auswertekette entwerfen",
        "Beginne bei dem Signal, das registriert wird. Kennzeichne anschließend, welche Information unabhängig über die Struktur bekannt sein muss.",
        listOf(
            "Die Kette beginnt beim Detektorsignal in Abhängigkeit von der Richtung und bei der zugeordneten Beschleunigungsspannung. Ein Maximum erhält einen erklärten Winkelbezug.",
            "Aus dem Winkel und einem unabhängig begründeten Strukturabstand folgt die geometrische Wellenlänge. Separat wird aus der Spannung die de-Broglie-Vorhersage berechnet; erst dann werden die Ergebnisse verglichen.",
            "Das Signal zeigt weder unmittelbar eine Wellenlänge noch die Bahn eines einzelnen Elektrons.",
        ),
    ),
    ANGLE(
        QuestionId.Q05, "R-Q05-25GRAD", "Bezugslinien wechseln",
        "Zeichne dieselbe Ebene, ihre Normale und den Strahl. Welchen gemeinsamen rechten Winkel teilen die beiden gesuchten spitzen Winkel?",
        listOf(
            "25° zur Normalen bedeuten 65° zur gleichen Ebene: Die beiden spitzen Winkel ergänzen sich zu 90°.",
            "Die Aussage benötigt dieselbe Ebene und ihren Normalenbezug. Sie ist keine universelle Halbierung oder Umrechnung eines beliebigen Detektorwinkels.",
        ),
    ),
    INDEPENDENCE(
        QuestionId.Q06, "R-Q06-EINGANG", "Unabhängige Größen erkennen",
        "Zeichne für jede Eingangsgröße einen Herkunftspfeil. Kommt sie aus dem Versuch beziehungsweise der Struktur oder aus genau der geprüften Formel?",
        listOf(
            "U und der beobachtete Winkel φ gehören zum berichteten Versuch; a stammt aus der unabhängig gewählten Strukturannahme. Der passende Abstand wird aus dieser Struktur geometrisch bestimmt.",
            "Ein aus λ(U) zurückgerechneter Winkel enthält die Vorhersage bereits. Setzt man ihn als angeblichen Messwinkel ein, entsteht eine zirkuläre Übereinstimmung.",
            "Im verwendeten DG-Fall bleiben der sekundäre Quellenstatus und die begrenzte Genauigkeit des ungefähr angegebenen Winkels sichtbar.",
        ),
    ),
    IMAGE_ZOOM(
        QuestionId.Q07, "R-Q07-KOORDINATEN", "Koordinaten und Maßstab",
        "Vergleiche Koordinaten im Originalraster mit Abständen auf dem Bildschirm. Welche unabhängige Information wäre für eine physikalische Länge erforderlich?",
        listOf(
            "Dieselben beiden nativen Marken behalten beim Anzeigezoom ihre Koordinaten und ihre Differenz in Pixeln. Nur die Bildschirmdarstellung wird größer oder kleiner.",
            "Eine geeignete externe Kalibrierung müsste die Zuordnung der Bildpixel zu einer physikalischen Länge und die relevante Elektronenoptik beziehungsweise Vergrößerung für diese Aufnahme absichern.",
            "Zoominvarianz allein liefert weder den Maßstab noch eine absolute Wellenlänge. Prüfe die beiden Koordinaten an deinen eigenen Aufzeichnungen, statt neue Messdaten zu erfinden.",
        ),
    ),
    EXPERIMENTS(
        QuestionId.Q08, "R-Q08-ARGUMENTE", "Zwei Argumente formulieren",
        "Formuliere für jeden Versuch drei getrennte Sätze: Was ist aufgebaut, was beobachtet man, welchen begrenzten Schluss trägt das?",
        listOf(
            "DG: Elektronen treffen auf eine regelmäßige Nickelstruktur. Ein richtungsabhängiges Maximum wird mit Strukturmodell und Spannungs-Vorhersage verglichen. Der benutzte Zahlenfall ist sekundär über D2 belegt; die unbekannte historische Messunsicherheit wird nicht erfunden.",
            "Jönsson: Elektronen durchlaufen künstliche Feinspalte. Die Aufnahme zeigt charakteristische Interferenzstrukturen. J1 belegt den Versuch; eigene native Streifenabstände quantifizieren die Reproduktion, ergeben ohne unabhängigen Maßstab aber keine absolute Wellenlänge.",
            "Die beiden Argumente ergänzen sich durch unterschiedliche Strukturen und Auswertungen. Sie liefern keine direkt beobachteten Einzelbahnen.",
        ),
    ),
    INITIAL_ENERGY(
        QuestionId.T01, "R-T01-GLEICHE-ENERGIE", "Zwei Energieanteile vergleichen",
        "Vergleiche zunächst die gesamte kinetische Energie beider Fälle bei gleicher Spannung. Übertrage ihr Verhältnis über Impuls auf Wellenlänge.",
        listOf(
            "Wenn E_kin,0 = eU ist, beträgt die Endenergie 2eU statt eU beim Start aus Ruhe. Der Impuls ist um den Faktor √2 größer.",
            "Damit beträgt die Wellenlänge 1/√2 des Werts beim Start aus Ruhe und gleichem U. Die nichtrelativistische Näherung und die übrigen Annahmen müssen weiterhin gelten.",
        ),
    ),
    NORMAL_ANGLE(
        QuestionId.T02, "R-T02-20GRAD", "Eine neue Orientierung zeichnen",
        "Markiere zuerst den rechten Winkel zwischen Ebene und Normale. Trage den gegebenen Winkel an der richtigen Bezugslinie ein.",
        listOf(
            "Bei α = 20° zur Normalen derselben Ebene liegt der Strahl 70° zur Ebene. Beide spitzen Winkel ergänzen sich zu 90°.",
            "Erst der korrekt zur betrachteten Netzebene definierte Winkel gehört in die Bragg-Bedingung. Die Zeichnung begründet die Zuordnung; eine pauschale Halbierungsregel tut das nicht.",
        ),
    ),
    INNER_MARKER(
        QuestionId.T03, "R-T03-INNERE-MARKE", "Eine innere Marke verschieben",
        "Schreibe die beiden Abstände links und rechts der verschobenen Marke auf. Betrachte danach die Summe aller benachbarten Abstände.",
        listOf(
            "Wird nur eine innere Marke um +1 px nach rechts verschoben, wächst das Intervall links um 1 px und das rechts schrumpft um 1 px. Die übrigen Abstände bleiben gleich, solange die Sortierung erhalten bleibt.",
            "Bei festen Endmarken bleibt die Summe aller Intervalle gleich und damit auch ihr arithmetisches Mittel. Ein unveränderter Mittelwert beweist daher noch keine plausible Maximaauswahl.",
            "Dies ist eine Methodenvariation. Sie ist keine bekannte historische Messunsicherheit.",
        ),
    ),
    CALIBRATION(
        QuestionId.T04, "R-T04-PROTOKOLL", "Drei Herkunftsspalten anlegen",
        "Ordne zuerst Pixelmarken und Streifenabstände ein. Prüfe dann, ob ein angegebener Maßstab von außen kommt oder aus der zu prüfenden Beziehung berechnet wurde.",
        listOf(
            "Tatsächlich gemessen: zum Beispiel eigene native Pixelmarken und ihre Abstände. Unabhängig bekannt: nur tatsächlich belegte Angaben zur konkreten Aufnahme oder eine extern begründete Kalibrierung.",
            "Aus dem Modell berechnet: die Spannungs-Vorhersage und ein daraus passend gemachter Maßstab. Ein solcher Maßstab gehört nicht in die unabhängige Spalte.",
            "Fehlende Informationen bleiben als fehlend stehen. Für die aktuelle Reproduktion sind insbesondere die sichere Spannungszuordnung und der absolute Bildmaßstab offen.",
        ),
    );

    companion object {
        fun forQuestion(id: QuestionId): ReviewVariant = entries.single { it.source == id }
    }
}

private data class ReviewDraft(
    val answer: String = "",
    val hintOpen: Boolean = false,
    val hintViewed: Boolean = false,
    val controlViewed: Boolean = false,
)

/**
 * This view deliberately receives no LabSession: reviewing cannot clear original answers,
 * disclose their explanations, change a due date or imply a saved competence history.
 */
@Composable
internal fun ReviewPractice(initialQuestion: QuestionId, onClose: () -> Unit) {
    var variant by remember { mutableStateOf(ReviewVariant.forQuestion(initialQuestion)) }
    var drafts by remember { mutableStateOf<Map<ReviewVariant, ReviewDraft>>(emptyMap()) }
    val draft = drafts[variant] ?: ReviewDraft()
    fun edit(change: (ReviewDraft) -> ReviewDraft) {
        drafts = drafts + (variant to change(drafts[variant] ?: ReviewDraft()))
    }
    DisposableEffect(Unit) {
        val cancel = requestPanelFocus(Id.COURSE_TITLE)
        onDispose { cancel() }
    }
    Section(attrs = {
        elementId(Id.COURSE_MAIN)
        styles(LabClass.COURSE_SHELL)
        labelledBy(Id.COURSE_TITLE)
    }) {
        Button(attrs = {
            elementId(Id.COURSE_RETURN)
            styles(LabClass.SECONDARY)
            onClick { onClose() }
        }) { Text("Abruf schließen · zurück zur Übersicht") }
        P(attrs = { styles(LabClass.EYEBROW) }) { Text("OHNE OFFENE MUSTERANTWORT ERKLÄREN") }
        H1(attrs = { elementId(Id.COURSE_TITLE); focusIndex(-1) }) {
            Text("Ein eigener Anschlussfall")
        }
        P(attrs = { styles(LabClass.COURSE_LEAD) }) {
            Text("Dieser Abruf wird nicht gespeichert. Deine bisherigen Stationsantworten, Erklärschritte und Fälligkeiten bleiben unverändert.")
        }
        P {
            Text("Die Fälle verändern die ursprüngliche Auswahlfrage. Sie können dir aus dem Lernweg schon bekannt sein; ein unbekannter Erstversuch wird hier nicht behauptet. Hilfe und Kontrolle sind von Anfang an freiwillig erreichbar.")
        }
        FieldLabel(Id.COURSE_QUESTION) { Text("Abruffall auswählen") }
        Select(attrs = {
            elementId(Id.COURSE_QUESTION)
            onChange { event ->
                ReviewVariant.entries.firstOrNull { it.key == event.value }?.let { variant = it }
            }
        }) {
            ReviewVariant.entries.forEach { candidate ->
                Option(value = candidate.key, attrs = { if (candidate == variant) selected() }) {
                    Text(candidate.title)
                }
            }
        }
        Section(attrs = { styles(LabClass.COURSE_TASK); data(LabData.QUESTION, variant.source) }) {
            H2 { Text(variant.title) }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text("Fall ${variant.key} · Anschluss an ${questionLabel(variant.source)}")
            }
            P(attrs = { elementId(Id.COURSE_TASK_PROMPT) }) {
                Text(QuestionBank[variant.source].followUp)
            }
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                Text("Rechne oder skizziere bei Bedarf auf Papier. Für Bildfälle nutze deine bereits notierten nativen Koordinaten; hier werden keine Messdaten erzeugt.")
            }
            FieldLabel(Id.COURSE_TASK_ANSWER) { Text("Deine Erklärung zu diesem Fall · ungespeichert") }
            TextArea(value = draft.answer, attrs = {
                elementId(Id.COURSE_TASK_ANSWER)
                styles(LabClass.COURSE_NOTE)
                textRows(5)
                maxLength(4000)
                onInput { event -> edit { it.copy(answer = event.value.take(4000)) } }
            })
            Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
                Button(attrs = {
                    elementId(Id.COURSE_HINT)
                    styles(LabClass.SECONDARY)
                    ariaPressed(draft.hintOpen)
                    onClick { edit { it.copy(hintOpen = !it.hintOpen, hintViewed = true) } }
                }) { Text(if (draft.hintOpen) "Kleine Hilfe schließen" else "Kleine Hilfe öffnen") }
                Button(attrs = {
                    elementId(Id.COURSE_TASK_CHECK)
                    styles(LabClass.SECONDARY)
                    onClick { edit { it.copy(controlViewed = true) } }
                }) { Text(if (draft.controlViewed) "Kontrolle ist geöffnet" else "Kontrolle bewusst öffnen") }
            }
            if (draft.hintOpen) P(attrs = { elementId(Id.COURSE_HINT_CONTENT); styles(LabClass.NOTES) }) {
                Text(variant.hint)
            }
            P(attrs = { elementId(Id.COURSE_STORAGE_STATUS); styles(LabClass.SMALL, LabClass.MUTED); role(AriaRole.STATUS) }) {
                Text(when {
                    draft.controlViewed -> "Kontrolle in diesem Abruf geöffnet. Deine Antwort bleibt erhalten; eine eigenständige Lösung wird daraus nicht abgeleitet."
                    draft.hintViewed -> "Kleine Hilfe in diesem Abruf geöffnet. Deine freie Erklärung wird nicht automatisch fachlich bewertet."
                    else -> "In diesem Abruf noch keine Hilfe oder Kontrolle geöffnet. Frühere Lösungskontakte sind daraus nicht ableitbar."
                })
            }
            if (draft.controlViewed) Section(attrs = {
                elementId(Id.COURSE_TASK_FEEDBACK)
                styles(LabClass.COURSE_RESULT)
                role(AriaRole.REGION)
                ariaLabel("Prüfpunkte zum gewählten Anschlussfall")
            }) {
                H3 { Text("Mit diesen Prüfpunkten selbst vergleichen") }
                variant.control.forEach { paragraph -> P { Text(paragraph) } }
                P { Text("Welche Begründung fehlt noch in deiner Antwort? Erkläre sie anschließend mit eigenen Worten. Die App vergibt dafür kein Richtig-Urteil.") }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text("Bezug: ${QuestionBank[variant.source].sourceIds.joinToString(", ")}")
                }
            }
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("Beim Fallwechsel in dieser Ansicht bleiben deine Abrufentwürfe erhalten. Beim Schließen oder Neuladen gehen sie verloren; sie gehören nicht zum gespeicherten Lernstand oder dessen Export. Du kannst deine Erklärung vorher selbst kopieren.")
        }
    }
}
