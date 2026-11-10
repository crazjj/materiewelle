package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.Physics
import materiewelle.core.Voltage
import kotlin.math.abs

/** Stable identities are independent of translated wording and list order. */
@Serializable
enum class QuestionId { Q01, Q02, Q03, Q04, Q05, Q06, Q07, Q08, Q09, T01, T02, T03, T04 }

@Serializable
enum class QuestionOptionId { A, B, C }

data class QuestionOption(
    val id: QuestionOptionId,
    val text: String,
    val feedback: String,
    val isCorrect: Boolean,
)

/** A closed question can be checked locally. This is not a grader for free explanations. */
data class ConceptQuestion(
    val id: QuestionId,
    val stationNumber: Int,
    val prompt: String,
    val options: List<QuestionOption>,
    val hints: List<String>,
    val followUp: String,
    val sourceIds: List<String>,
) {
    init {
        require(stationNumber in 1..16)
        require(options.map { it.id }.toSet().size == options.size)
        require(options.count { it.isCorrect } == 1)
        require(hints.isNotEmpty() && followUp.isNotBlank())
    }

    fun option(id: QuestionOptionId): QuestionOption = options.single { it.id == id }
}

/** Authored and mathematically checked questions, not generated at runtime. */
object QuestionBank {
    val questions: List<ConceptQuestion> = listOf(
        ConceptQuestion(QuestionId.Q01, 4,
            "Der Impuls eines Elektrons wird verdoppelt. Was folgt aus λ = h/p?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Die Wellenlänge halbiert sich; über einen Elektronendurchmesser folgt nichts.", "Richtig: h bleibt konstant. λ ist eine dem Impuls zugeordnete Wellenlänge, kein Maß für den Durchmesser.", true),
                QuestionOption(QuestionOptionId.B, "Wellenlänge und Elektronendurchmesser halbieren sich.", "Die Halbierung von λ stimmt. Aus λ = h/p folgt aber keine Aussage über einen Durchmesser des Elektrons.", false),
                QuestionOption(QuestionOptionId.C, "Die Wellenlänge verdoppelt sich.", "p steht im Nenner. Rechne zunächst das Verhältnis λ₂/λ₁ = p₁/p₂.", false),
            ), listOf("Welche Größe bleibt konstant?", "Setze für den neuen Impuls 2p ein."),
            "Ohne Vorlage: Welche Wellenlänge gehört zu dreifachem Impuls? Begründe das Verhältnis mit der Formel.", listOf("A1", "H1")),
        ConceptQuestion(QuestionId.Q09, 5,
            "Welcher Schritt der Herleitung führt die neue Materiewellenhypothese ein?",
            listOf(
                QuestionOption(QuestionOptionId.A, "E_kin = eU: elektrische Arbeit wird kinetische Energie.", "Das ist die Energiebilanz unter Start-aus-Ruhe- und Verlustannahmen. Eine Wellenlänge kommt darin noch nicht vor.", false),
                QuestionOption(QuestionOptionId.B, "E_kin = p²/(2mₑ): Energie wird als Impuls ausgedrückt.", "Das ist die nichtrelativistische Energie-Impuls-Beziehung für ein massives Teilchen. Sie bestimmt p, aber ordnet ihm noch keine Wellenlänge zu.", false),
                QuestionOption(QuestionOptionId.C, "λ = h/p: dem berechneten Impuls wird eine Wellenlänge zugeordnet.", "Richtig. Erst λ = h/p ergänzt die Materiewellenhypothese. Energieerhaltung und NR-Mechanik allein beweisen diese neue Zuordnung nicht.", true),
            ), listOf("In welcher Zeile tritt λ erstmals auf?", "Unterscheide bekannte Energiebilanz und neue Hypothese."),
            "Neuer Fall E_kin,0 > 0: Ersetze eU durch E_kin,0 + eU. Welche Beziehung bleibt dabei der hypothetische Anschluss?", listOf("A1", "Modellrechnung")),
        ConceptQuestion(QuestionId.Q02, 6,
            "Die Beschleunigungsspannung steigt von U auf 4U. Welche Faktoren gelten bei Start aus Ruhe im NR-Modell für E_kin, p und λ?",
            listOf(
                QuestionOption(QuestionOptionId.A, "E_kin: 4; p: 4; λ: 1/4.", "E_kin = eU ist linear. Wegen E_kin = p²/(2mₑ) wächst p nur mit √E_kin. Prüfe diesen mittleren Schritt.", false),
                QuestionOption(QuestionOptionId.B, "E_kin: 4; p: 2; λ: 1/2.", "Richtig: vierfache Energie ergibt doppelten Impuls und damit halbe Wellenlänge. Die Annahmen Start aus Ruhe und NR bleiben erforderlich.", true),
                QuestionOption(QuestionOptionId.C, "E_kin: 2; p: √2; λ: 1/√2.", "U wurde vervierfacht, nicht verdoppelt. Beginne mit E_kin,2/E_kin,1 = U₂/U₁ = 4.", false),
            ), listOf("Verknüpfe zuerst E_kin und U.", "Löse E_kin = p²/(2mₑ) nach p auf."),
            "Neuer Fall: Die Spannung steigt auf 9U. Bestimme die Faktoren für E_kin, p und λ und begründe sie ohne App.", listOf("A1", "Modellrechnung")),
        ConceptQuestion(QuestionId.Q03, 7,
            "Was bedeutet Einsteins positive Einschätzung von de Broglies Idee im Jahr 1924?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Damit war λ = h/p bereits experimentell gemessen.", "Eine theoretische Würdigung ist kein Messbefund. Für die experimentelle Prüfung braucht man einen Aufbau, Daten und ein Auswertemodell.", false),
                QuestionOption(QuestionOptionId.B, "Sie verbesserte die wissenschaftliche Aufnahme der Idee; sie ersetzte keinen Versuch.", "Richtig. H1 berichtet rückblickend von Langevins Vermittlung; H3 ordnet die Korrespondenz historisch ein.", true),
                QuestionOption(QuestionOptionId.C, "Der Dezemberbrief führte zur Disputation im November desselben Jahres.", "Die zeitliche Reihenfolge passt nicht. Eine Quelle vom Dezember kann keine schon im November abgeschlossene Disputation verursacht haben.", false),
            ), listOf("Welche Art von Quelle wäre eine Messbestätigung?", "Ordne November und Dezember 1924 zeitlich."),
            "Ordne zwei neue Belege begründet ein: ein Empfehlungsschreiben und ein protokollierter Detektorscan. Was kann jeder Beleg über die Hypothese aussagen?", listOf("H1", "H3")),
        ConceptQuestion(QuestionId.Q04, 8,
            "Welche Größen registriert der DG-Versuch und welche Größe wird erst mithilfe eines Modells abgeleitet?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Der Detektor zeigt unmittelbar die Wellenlänge in Nanometern an.", "Der Detektor registriert ein Signal in Abhängigkeit von seiner Richtung. Eine Wellenlänge folgt erst aus Geometrie und unabhängigem Strukturabstand.", false),
                QuestionOption(QuestionOptionId.B, "Der Detektor beobachtet die sinusförmige Bahn jedes Elektrons.", "Ein richtungsabhängiges Streusignal ist kein Bild einzelner Bahnen. Auch eine animierte Wellenlinie würde eine Bahn nicht nachweisen.", false),
                QuestionOption(QuestionOptionId.C, "Ein Signal wird Winkeln zugeordnet; aus Winkel und Strukturmodell folgt λ.", "Richtig. Registrierte Größe, Messgeometrie und daraus abgeleitete Wellenlänge bleiben unterscheidbar.", true),
            ), listOf("Welche Anzeige könnte ein Detektor direkt liefern?", "Für λgeo braucht man neben dem Winkel einen unabhängigen Strukturabstand."),
            "Entwirf ohne Vorlage eine Auswertekette vom Detektorsignal bis zum Vergleich mit λ(U). Kennzeichne gemessene, unabhängig bekannte und berechnete Größen.", listOf("D2")),
        ConceptQuestion(QuestionId.Q05, 10,
            "In einer neuen Skizze ist ein Detektorwinkel eingezeichnet. Ist er automatisch der Braggwinkel θB?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Ja, jeder Streuwinkel heißt Braggwinkel.", "Ein Winkel erhält seine Bedeutung durch beide Bezugslinien. θB ist zur betrachteten Netzebene definiert.", false),
                QuestionOption(QuestionOptionId.B, "Nein. Zuerst müssen Bezugslinien und konkrete Geometrie geklärt werden.", "Richtig: θB liegt zur Netzebene; φ im gewählten Oberflächenmodell zur äußeren Normale. Eine allgemeine Halbierungsregel reicht nicht.", true),
                QuestionOption(QuestionOptionId.C, "Man halbiert immer den eingezeichneten Winkel.", "Eine Halbierung kann nur aus einer passenden konkreten Zeichnung folgen. Ein beliebig benannter Detektorwinkel ist dafür nicht ausreichend.", false),
            ), listOf("Markiere die zwei Strahlen oder Linien, die den Winkel bilden.", "Zeichne die Netzebene und ihre Normale getrennt ein."),
            "Neuer Fall: Derselbe Strahl liegt 25° zur Ebenennormale. Bestimme seinen spitzen Winkel zur Ebene. Erkläre anhand der Bezugslinien, wann die Relation gilt.", listOf("D2", "Geometrie")),
        ConceptQuestion(QuestionId.Q06, 11,
            "Eine Simulation erzeugt Daten mit λ = h/p. Können diese Daten dieselbe Beziehung unabhängig experimentell bestätigen?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Ja, wenn die Kurve viele Punkte enthält.", "Viele berechnete Punkte ändern ihre Herkunft nicht. Die zu prüfende Beziehung wurde bei der Erzeugung bereits vorausgesetzt.", false),
                QuestionOption(QuestionOptionId.B, "Nein. Sie können die Umsetzung veranschaulichen, enthalten die Beziehung aber schon als Voraussetzung.", "Richtig. Unabhängige Bestätigung braucht Messdaten und Eingangsgrößen, die nicht aus derselben zu prüfenden Beziehung gewonnen wurden.", true),
                QuestionOption(QuestionOptionId.C, "Ja, wenn alle Punkte ohne sichtbare Abweichung auf der Kurve liegen.", "Eine perfekte Übereinstimmung ist bei aus derselben Formel erzeugten Punkten erwartbar. Sie ist kein unabhängiger experimenteller Test.", false),
            ), listOf("Woher stammt der λ-Wert in jedem Punkt?", "Unterscheide Programmprüfung und empirische Theorieprüfung."),
            "Markiere im DG-Vergleich die unabhängig eingegebenen Größen U, a und φ. Erkläre, warum ein aus λ(U) rückberechneter perfekter Winkel als unabhängiger Beleg ungeeignet wäre.", listOf("D2", "Modellkritik")),
        ConceptQuestion(QuestionId.Q07, 14,
            "Die gleichen Jönsson-Marken werden bei doppeltem Browserzoom betrachtet. Ändert sich ihr nativer Abstand, und liefern Pixel allein eine absolute λ?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Der Abstand in nativen Pixeln bleibt gleich; für eine absolute λ fehlt weiterhin die physikalische Kalibrierung.", "Richtig: Native Bildkoordinaten gehören zur Rasterdatei. Bildschirmgröße und Zoom verändern sie nicht. Ein Pixel ist noch keine bekannte Länge in der Beobachtungsebene.", true),
                QuestionOption(QuestionOptionId.B, "Der native Abstand verdoppelt sich und damit auch λ.", "Das wäre eine Verwechslung von Bildschirm- und Bildkoordinaten. Die Position im Originalraster muss unverändert bleiben.", false),
                QuestionOption(QuestionOptionId.C, "Der Abstand bleibt gleich, also ist die absolute Wellenlänge eindeutig bekannt.", "Zoominvarianz sichert die relative Pixelmessung. Für Meter oder λ fehlen trotzdem ein unabhängiger Maßstab und die passende Optikzuordnung.", false),
            ), listOf("Auf welche Datei bezieht sich das Wort nativ?", "Welche zusätzliche Information übersetzt px in eine physikalische Länge?"),
            "Lies dieselben zwei Marken vor und nach Zoomwechsel ab. Nenne anschließend eine externe Kalibrierinformation, die für eine absolute Messung nötig wäre.", listOf("J1", "Bildauswertung")),
        ConceptQuestion(QuestionId.Q08, 16,
            "Warum sind Davisson–Germer und Jönsson ergänzende Prüfungen von Materiewellen?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Weil 1927 und 1961 unterschiedliche Jahreszahlen sind.", "Die Chronologie allein erklärt keine fachliche Ergänzung. Nenne den jeweiligen Aufbau, Befund und die passende quantitative Auswertung.", false),
                QuestionOption(QuestionOptionId.B, "DG nutzt eine regelmäßige Kristallstruktur, Jönsson künstliche Spalte; beide zeigen charakteristische Strukturen mit jeweils passender Auswertung.", "Richtig. Der DG-Zahlenvergleich und die relative Jönsson-Bildmessung haben unterschiedliche Voraussetzungen und Grenzen. Ihre Ergebnisse dürfen nicht als identische Messverfahren erzählt werden.", true),
                QuestionOption(QuestionOptionId.C, "Beide zeigen direkt, durch welchen Spalt jedes Elektron fliegt.", "Keiner der hier behandelten Befunde zeigt eine einzelne unbeobachtete Elektronenbahn. Das charakteristische Muster und seine Auswertung sind die tragenden Beobachtungen.", false),
            ), listOf("Was übernimmt bei DG die Rolle der regelmäßigen Struktur?", "Welche Größe wertest du im Jönsson-Foto tatsächlich aus?"),
            "Erkläre beide Versuche in je drei Sätzen: Aufbau → Befund → begrenzter Schluss. Ergänze eine Quelle und eine Grenze pro Versuch.", listOf("D2", "J1")),
        ConceptQuestion(QuestionId.T01, 16,
            "Ein Elektron startet bereits mit bekannter kinetischer Energie E_kin,0 > 0. Welche NR-Vorhersage gilt nach zusätzlicher Beschleunigung um den positiven Betrag U?",
            listOf(
                QuestionOption(QuestionOptionId.A, "λ = h/√(2mₑeU), unverändert und unabhängig von E_kin,0.", "Diese Form setzt den Start aus Ruhe voraus. Eine bekannte Anfangsenergie muss in der Energieerhaltung berücksichtigt werden.", false),
                QuestionOption(QuestionOptionId.B, "λ = h/√(2mₑ(E_kin,0 + eU)).", "Richtig: E_kin = E_kin,0 + eU. Unter denselben übrigen NR-Annahmen wird daraus p = √(2mₑE_kin) und anschließend λ = h/p.", true),
                QuestionOption(QuestionOptionId.C, "λ = h/√(2mₑ(E_kin,0 − eU)) bei Beschleunigung.", "Eine beschleunigende Spannung erhöht hier die kinetische Energie. U ist ausdrücklich der positive Beschleunigungsbetrag.", false),
            ), listOf("Schreibe die Energiebilanz mit Anfangsenergie auf.", "Setze erst danach die neue Energie E_kin in p = √(2mₑE_kin) ein."),
            "Wenn E_kin,0 = eU ist: Bestimme das Verhältnis zur Wellenlänge beim Start aus Ruhe und gleichem U. Begründe es aus der Energiebilanz.", listOf("Energieerhaltung", "Modellrechnung")),
        ConceptQuestion(QuestionId.T02, 16,
            "Derselbe Strahl bildet einen spitzen Winkel α = 30° zur Normalen einer Netzebene. Welcher spitze Winkel zur Ebene ist θB?",
            listOf(
                QuestionOption(QuestionOptionId.A, "15°, weil jeder Detektorwinkel halbiert wird.", "Hier ist α zur Normalen derselben Ebene definiert. Die gesuchte Relation folgt aus einem rechten Winkel, nicht aus einer allgemeinen Detektorregel.", false),
                QuestionOption(QuestionOptionId.B, "30°, weil beide Winkel zum selben Strahl gehören.", "Die zweite Bezugslinie ist verschieden: Normale und Ebene stehen senkrecht aufeinander.", false),
                QuestionOption(QuestionOptionId.C, "60°, weil θB = 90° − α für diese Bezugslinien gilt.", "Richtig. Die Komplementbeziehung gilt für denselben Strahl, dieselbe Ebene und ihre Normale. Sie ist keine pauschale Umrechnung jedes DG-Detektorwinkels.", true),
            ), listOf("Zeichne zuerst Ebene und Normale als rechten Winkel.", "Die zwei spitzen Winkel ergänzen sich zu 90°."),
            "Zeichne den Fall α = 20° neu und bestimme θB. Beschrifte beide Bezugslinien, bevor du Braggs Bedingung verwendest.", listOf("Geometrie", "D2")),
        ConceptQuestion(QuestionId.T03, 16,
            "Acht sortierte Maxima bilden sieben Intervalle. Nur die letzte Marke wird um +1 nativen Pixel verschoben. Wie ändert sich das arithmetische Mittel der sieben Abstände?",
            listOf(
                QuestionOption(QuestionOptionId.A, "+1/7 px.", "Richtig: Die Summe aller benachbarten Abstände ist x₈ − x₁. Sie steigt um 1 px und wird durch sieben geteilt. Das ist ein Methodeneinfluss, keine bekannte historische Messunsicherheit.", true),
                QuestionOption(QuestionOptionId.B, "+1/8 px.", "Acht Marken begrenzen sieben Intervalle. Gezählt werden die Abstände zwischen benachbarten Marken, nicht die Zahl der Marken.", false),
                QuestionOption(QuestionOptionId.C, "+1 px für jedes Intervall.", "Nur das letzte Intervall ändert sich hier. Die sechs anderen bleiben gleich; mittlere Änderung daher 1/7 px.", false),
            ), listOf("Schreibe die sieben Differenzen hintereinander auf.", "In ihrer Summe heben sich alle inneren Marken auf."),
            "Eine innere Marke verschiebt sich um +1 px, beide Endmarken bleiben gleich. Sage voraus, welche einzelnen Intervalle und welcher Mittelwert sich ändern. Prüfe anschließend deine Vorhersage und die Plausibilität deiner Markenwahl.", listOf("J1", "Eigene Bildauswertung")),
        ConceptQuestion(QuestionId.T04, 16,
            "Ein Foto hat keinen belegten Maßstab. Die Vergrößerung wird so aus λ = h/p berechnet, dass die Streifen passen. Ist die anschließende λ-Auswertung eine unabhängige Bestätigung?",
            listOf(
                QuestionOption(QuestionOptionId.A, "Ja, weil das Foto echt ist.", "Ein echtes Bild macht einen theoretisch angepassten Maßstab nicht unabhängig. Die Herkunft der Kalibrierung bleibt entscheidend.", false),
                QuestionOption(QuestionOptionId.B, "Nein. Die zu prüfende Beziehung steckt bereits in der angepassten Kalibrierung.", "Richtig. Für einen unabhängigen absoluten Vergleich braucht man extern begründeten Maßstab und Optikzuordnung. Die relative Pixelmessung bleibt trotzdem sinnvoll.", true),
                QuestionOption(QuestionOptionId.C, "Ja, sobald genügend Nachkommastellen berechnet werden.", "Rechenpräzision ersetzt keine unabhängige Kalibrierung. Viele Stellen eines modellabhängigen Werts lösen den Zirkelschluss nicht.", false),
            ), listOf("Welche Information wurde für die Vergrößerung vorausgesetzt?", "Würde sich die Fotoauswertung ohne λ = h/p noch in Meter umrechnen lassen?"),
            "Trenne in einem neuen Auswerteprotokoll drei Spalten: tatsächlich gemessen, unabhängig bekannt, aus dem zu prüfenden Modell berechnet.", listOf("J1", "Kalibrierungskritik")),
    )

    init { require(questions.map { it.id }.toSet() == QuestionId.entries.toSet()) }

    operator fun get(id: QuestionId): ConceptQuestion = questions.single { it.id == id }
}

enum class NumericAnswerStatus { EMPTY, INVALID_NUMBER, MISSING_UNIT, UNSUPPORTED_UNIT, OUT_OF_RANGE, CORRECT, INCORRECT }

data class NumericAnswerEvaluation(
    val status: NumericAnswerStatus,
    val picometers: Double? = null,
    val feedback: String,
)

/**
 * Separate from Physics input parsing: an answer must carry an explicit unit.
 * ±0.5 pm is a teaching/rounding tolerance, not experimental uncertainty.
 */
class NumericWavelengthQuestion(val voltage: Voltage, val tolerancePm: Double = 0.5) {
    init { require(tolerancePm.isFinite() && tolerancePm > 0.0 && tolerancePm <= 1.0) }
    val answerPm: Double get() = Physics.electronWavelength(voltage).meters * 1e12

    fun evaluate(raw: String): NumericAnswerEvaluation {
        val pm = when (val input = WavelengthInputs.parse(raw)) {
            is WavelengthInput.Rejected -> return NumericAnswerEvaluation(input.problem.status, feedback = input.problem.feedback)
            is WavelengthInput.Ready -> input.length.picometers
        }
        val correct = abs(pm - answerPm) <= tolerancePm
        val roundingAllowance = tolerancePm.toString().replace('.', ',').replace('E', 'e')
        return NumericAnswerEvaluation(
            if (correct) NumericAnswerStatus.CORRECT else NumericAnswerStatus.INCORRECT,
            pm,
            if (correct) "Dein Zahlenwert passt zur Modellrechnung. Erkläre noch den Weg Energie → Impuls → Wellenlänge. Der Vergleich nutzt das nichtrelativistische Modell; ±$roundingAllowance pm sind Spielraum fürs Runden, keine Messunsicherheit."
            else "Der Wert passt noch nicht. Prüfe E_kin = eU, die Wurzel in p = √(2mₑE_kin) und die Umrechnung 1 nm = 1000 pm. Die Lösung wird nicht als Ersatz für deinen Rechenweg gewertet.",
        )
    }
}
