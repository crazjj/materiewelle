package materiewelle.course

import materiewelle.core.course.StationId

/** Optional conceptual help and criteria for a learner's own explanation, not automatic grading. */
data class StationGuidance(
    val conceptBridges: List<String>,
    val selfCheck: List<String>,
)

/** No numerical solution to a new transfer case belongs in this always accessible help. */
object StationGuidances {
    private val entries = mapOf(
        StationId.S01_OBSERVATION to StationGuidance(
            conceptBridges = listOf(
                "Ein Befund ist etwas Beobachtetes. Ein Modell macht Annahmen und erlaubt daraus Vorhersagen.",
                "Ein Schema lässt Einzelheiten absichtlich weg, um Funktionen oder Beziehungen sichtbar zu machen.",
                "Ein Pixel ist ein Bildpunkt der digitalen Datei. Seine Position ist zunächst eine Bildkoordinate, keine Länge in Metern.",
            ),
            selfCheck = listOf(
                "Kann eine andere Person deinen Beobachtungssatz direkt am Foto prüfen?",
                "Hast du deine vermutete Ursache als Vermutung kenntlich gemacht?",
                "Kannst du für Foto, Schema und Modellkurve jeweils die Herkunft und eine sinnvolle Verwendung nennen?",
            ),
        ),
        StationId.S02_CLASSICAL to StationGuidance(
            conceptBridges = listOf(
                "Amplitude ist die größte Auslenkung aus der Mittellage; Wellenlänge ist ein räumlicher Wiederholungsabstand.",
                "Phase bezeichnet den Zustand innerhalb eines Schwingungszyklus. Gleiche Auslenkung allein genügt nicht: Auch die Bewegungsrichtung kann verschieden sein.",
                "Überlagerung heißt im linearen klassischen Modell: die Auslenkungen beider Beiträge am selben Ort und zur selben Zeit addieren.",
            ),
            selfCheck = listOf(
                "Kannst du A und λ am Bild in zwei verschiedenen Richtungen zeigen?",
                "Hast du vor der Summe beide Beiträge an derselben Stelle verglichen?",
                "Beschreibst du eine klassische Auslenkung und vermeidest dabei eine Behauptung über einzelne Elektronenbahnen?",
            ),
        ),
        StationId.S03_LIGHT to StationGuidance(
            conceptBridges = listOf(
                "Ein Resonator ist ein schwingungsfähiges System. Plancks Ansatz zur Wärmestrahlung betrifft den Energieaustausch solcher Systeme.",
                "Ein Photon ist ein Lichtquant. Seine Energie hängt über Eγ = hf mit der Lichtfrequenz zusammen.",
                "Eine Analogie schlägt eine neue Frage oder Hypothese vor. Ein Test verlangt dazu eine unabhängig prüfbare Vorhersage.",
            ),
            selfCheck = listOf(
                "Hast du für 1900 und 1905 jeweils den Gegenstand der Quantenidee benannt?",
                "Kannst du h, f und n in den beiden Energieaussagen erklären?",
                "Trennst du die Motivation durch Licht von einem Beweis für die Elektronenbeziehung?",
            ),
        ),
        StationId.S04_HYPOTHESIS to StationGuidance(
            conceptBridges = listOf(
                "Für dieselbe Masse bedeutet höheres Tempo nichtrelativistisch einen größeren Impulsbetrag p = mv.",
                "Umgekehrt proportional bedeutet: Das Produkt beider Größen bleibt konstant. Hier gilt λp = h.",
                "Ein normierter Graph zeigt Verhältnisse zu einem Ausgangsfall. Der Wert 1 bedeutet den Ausgangswert, nicht automatisch 1 kg m/s oder 1 m.",
            ),
            selfCheck = listOf(
                "Hast du zuerst ein Verhältnis vorhergesagt und erst danach am Regler geprüft?",
                "Kannst du an den Achsen erklären, welche Größe eingestellt und welche berechnet wird?",
                "Nennst du λ eine Wellenlänge, ohne sie mit Durchmesser oder Auslenkung zu verwechseln?",
            ),
        ),
        StationId.S05_DERIVATION to StationGuidance(
            conceptBridges = listOf(
                "Spannung ist übertragene Energie pro Ladungsmenge: 1 V = 1 J/C. Der Betrag e der Elektronenladung ist positiv.",
                "E_kin bezeichnet kinetische Energie. Die Energiebilanz bestimmt zunächst E_kin, noch keine Wellenlänge.",
                "Der Wurzelschritt: Wird das Quadrat einer Größe viermal so groß, wird die positive Größe selbst doppelt so groß. Beim Impuls verbindet genau dieser Schritt die kinetische Energie mit der Wellenlänge.",
                "Nichtrelativistisch bedeutet hier: Die kinetische Energie ist klein gegenüber der Ruheenergie mₑc². Dann sind E_kin = p²/(2mₑ) und p = mₑv passende Näherungen.",
            ),
            selfCheck = listOf(
                "Kannst du Startannahme, Energiebilanz, Impulsumstellung und neue Hypothese jeweils einzeln benennen?",
                "Bleiben die Einheiten beim Übergang von E_kin zu p und dann zu λ richtig?",
                "Kannst du die Rechnung ohne Vorlage rekonstruieren und sagen, welcher Schritt nicht aus klassischer Energieerhaltung folgt?",
            ),
        ),
        StationId.S06_CALCULATION to StationGuidance(
            conceptBridges = listOf(
                "Eine Längenzahl braucht eine Einheit. Piko bedeutet 10⁻¹², Nano 10⁻⁹; 1 nm entspricht 1000 pm.",
                "Ein Zahlenverhältnis kann die Rechnung prüfen: Vergleiche zuerst die Spannungen und nutze dann die Wurzelabhängigkeit.",
                "Rechenstellen geben die Genauigkeit der Auswertung unter den gewählten Eingaben an. Eine Lerntoleranz ist keine historische Messunsicherheit.",
            ),
            selfCheck = listOf(
                "Stehen Ansatz, eingesetzte SI-Einheiten und abschließende Längeneinheit in deinem Rechenweg?",
                "Passen Richtung und Größenordnung deines Resultats zu deiner Verhältnisvorhersage?",
                "Hast du den 150-V-Pflichtfall, das geführte 600-V-Beispiel und einen neuen Zahlenfall auseinandergehalten?",
            ),
        ),
        StationId.S07_RECEPTION to StationGuidance(
            conceptBridges = listOf(
                "Rezeption bedeutet, wie eine Idee aufgenommen, diskutiert und weitergetragen wird.",
                "Eine Disputation ist die mündliche Verteidigung einer Doktorarbeit.",
                "Ereigniszeit nennt den Zeitpunkt des Geschehens. Quellenzeit nennt den Zeitpunkt eines Briefs, Interviews oder späteren Berichts darüber.",
            ),
            selfCheck = listOf(
                "Kannst du zur betrachteten Aussage Ereignis, Quellenart und zeitlichen Abstand nennen?",
                "Ist deine historische Reihenfolge mit November und Dezember 1924 vereinbar?",
                "Erklärst du, was wissenschaftliche Zustimmung beiträgt und was zusätzlich für einen experimentellen Test nötig wäre?",
            ),
        ),
        StationId.S08_DG_SETUP to StationGuidance(
            conceptBridges = listOf(
                "Ein Detektor registriert ein Signal für eine festgelegte Beobachtungsrichtung. Eine Wellenlänge wird hier erst mithilfe von Struktur und Geometrie abgeleitet.",
                "Ein Elektronenvolt, eV, ist eine Energieeinheit: der Energiegewinn einer Elementarladung beim Durchlaufen von 1 V. eV und V sind verschiedene Größen.",
                "Eine geänderte Laboreinstellung erzeugt eine neue Modellrechnung. Sie erzeugt keine zusätzliche historische Messung.",
            ),
            selfCheck = listOf(
                "Kannst du Quelle, Beschleunigung, Kristall und Detektor funktional verbinden?",
                "Unterscheidest du eingestellte, registrierte und berechnete Größen?",
                "Bleibt der sekundär berichtete 54-eV-/ungefähr-50°-Fall von deiner eigenen Winkelvariation getrennt?",
            ),
        ),
        StationId.S09_CRYSTAL to StationGuidance(
            conceptBridges = listOf(
                "Periodisch heißt: Ein räumliches Muster wiederholt sich. Ein Einkristall behält dabei eine gemeinsame Orientierung.",
                "fcc bedeutet kubisch flächenzentriert: Atompositionen an Würfelecken und Flächenmitten. a ist die Kantenlänge des Gitterbausteins.",
                "Ein senkrechter Abstand wird entlang eines Lots gemessen. Die Frage ‚senkrecht wozu?‘ unterscheidet den Reihenabstand D vom Netzebenenabstand d111.",
            ),
            selfCheck = listOf(
                "Kannst du dieselbe (111)-Fläche im Würfel und in ihrer Aufsicht wiedererkennen?",
                "Zeigst du für a, D und d111 jeweils die passenden Endpunkte und Bezugslinien?",
                "Wurde die räumliche Skala unabhängig von der zu prüfenden λ-Vorhersage festgelegt?",
            ),
        ),
        StationId.S10_GEOMETRY to StationGuidance(
            conceptBridges = listOf(
                "Gangunterschied ist die Differenz zweier verglichener Weglängen. Gleiche Phase ist bei einem ganzzahligen Vielfachen von λ möglich.",
                "Die Normale steht senkrecht auf einer Ebene. Ein Winkelwert ist erst mit seinen beiden Bezugslinien eindeutig.",
                "Eine Projektionshilfe überträgt den Abstand auf die Wegesrichtung. Das erklärt den Sinusfaktor und in der Braggkonstruktion die beiden Zusatzwege.",
            ),
            selfCheck = listOf(
                "Sind Ebene, Normale und beide Winkelschenkel in deiner Skizze erkennbar?",
                "Kannst du beide Bragg-Zusatzwege einzeln begründen, bevor du ihre Summe verwendest?",
                "Hast du beim Wechsel zur Oberfläche Abstand und Winkel neu definiert, statt Symbole ungeprüft zu übernehmen?",
            ),
        ),
        StationId.S11_DG_EVALUATION to StationGuidance(
            conceptBridges = listOf(
                "Zwei Rechenwege prüfen sich nur dann gegenseitig, wenn ihre Eingaben nicht passend aus dem gewünschten Ergebnis erzeugt wurden.",
                "Bei einer relativen Differenz legt der Nenner fest, auf welchen Wert sich die Prozentangabe bezieht.",
                "Ein ungefähr berichteter Winkel rechtfertigt einen Größenordnungsvergleich. Viele Rechnerstellen ergänzen keine fehlenden Messangaben.",
            ),
            selfCheck = listOf(
                "Kannst du für U, φ und a Herkunft und Verwendung angeben?",
                "Sind Struktur-/Winkelzweig und Spannungszweig bis zum Vergleich voneinander getrennt?",
                "Nennen dein Urteil und deine Prozentzahl sowohl den Bezugswert als auch eine Daten- oder Modellgrenze?",
            ),
        ),
        StationId.S12_JONSSON_SETUP to StationGuidance(
            conceptBridges = listOf(
                "Elektronenoptik lenkt Elektronen mithilfe von Feldern. Eine gezeichnete Optiklinie veranschaulicht die Funktion, keine gemessene Einzelbahn.",
                "b bezeichnet die Breite einer Öffnung, g den Abstand der beiden Öffnungsmitten.",
                "Kalibrieren heißt hier: eine Bildkoordinate mit einem unabhängig bekannten physikalischen Längenmaß verknüpfen. Aufbauabstand und Foto-Pixelmaßstab liegen auf verschiedenen Ebenen.",
            ),
            selfCheck = listOf(
                "Kannst du für jede ausgewählte Stufe sagen, was hineingeht, was sie verändert und was danach beobachtbar ist?",
                "Unterscheidest du Austrittsrichtung, Position im Beobachtungsbild und Position in der digitalen Reproduktion?",
                "Erklärst du, welche zusätzliche Zuordnung für Meter pro Pixel fehlen würde, statt nur ‚350 mm reichen nicht‘ zu wiederholen?",
            ),
        ),
        StationId.S13_INTERFERENCE to StationGuidance(
            conceptBridges = listOf(
                "Die Hülle ist der gröbere Verlauf, der die feineren Interferenzstreifen begrenzt. Sie entsteht hier durch die endliche Spaltbreite.",
                "Normiert bedeutet: Der höchste Modellwert wird auf 1 gesetzt. So vergleichst du Formen, nicht unmittelbar gemessene Ströme.",
                "Fernfeld und kleine Winkel sind Näherungen für den ausgewählten Lehrfall. Ein berechneter Modellstreifen hat eine andere Herkunft als ein Grauwert im Quellenfoto.",
            ),
            selfCheck = listOf(
                "Hast du bei jeder Gegenprobe nur eine Größe verändert und die anderen benannt?",
                "Vergleichst du die feine Interferenzskala getrennt von der Breite der Hülle?",
                "Unterscheidest du Maxima des Interferenzfaktors, lokales Gesamtprofil und fotografische Grauwerte?",
            ),
        ),
        StationId.S14_MEASUREMENT to StationGuidance(
            conceptBridges = listOf(
                "Native Koordinaten beziehen sich auf die unveränderte 703 × 242-Pixel-Datei. Eine größere Anzeige vergrößert das Bild, nicht die Anzahl seiner ursprünglichen Pixel.",
                "Ein Auswerteband legt fest, welche Bildzeilen pro x-Position gemittelt werden. Die Auswahl gehört deshalb zum Messprotokoll.",
                "Eine Referenzwahl ist eine dokumentierte heutige Vergleichsauswahl. Sie ersetzt weder deine eigene Begründung noch einen unabhängigen physikalischen Bildmaßstab.",
            ),
            selfCheck = listOf(
                "Sind Band, eigene Marken, Referenzkontakt und Auswahlbegründung dokumentiert?",
                "Hast du benachbarte Einzelabstände geprüft und die Anzahl der Intervalle von der Anzahl der Marken unterschieden?",
                "Bleibt dasselbe Ergebnis bei anderem Zoom gleich, und hast du nach einem Bandwechsel die übernommenen Marken neu begründet?",
            ),
        ),
        StationId.S15_LIMITS to StationGuidance(
            conceptBridges = listOf(
                "Eine Modellgrenze betrifft die Angemessenheit einer Rechnung. Eine Quellenfrage betrifft die belegte Zuordnung einer Angabe.",
                "Eine Kalibrierungsfrage betrifft die unabhängige Übersetzung von Bildkoordinaten in physikalische Größen.",
                "Mehr Rechengenauigkeit kann eine Näherung verbessern. Sie ergänzt keine fehlende Quellenzuordnung oder Fotoeichung.",
            ),
            selfCheck = listOf(
                "Kannst du für jede Grenze erklären, welche konkrete Schlussaussage sie einschränkt?",
                "Hast du beim 50-kV-Modellvergleich dieselben Eingaben und den richtigen Prozentnenner verwendet?",
                "Trennst du einen berechneten Nominalfall von sicher bekannten Parametern einer Einzelaufnahme?",
            ),
        ),
        StationId.S16_CONCLUSION to StationGuidance(
            conceptBridges = listOf(
                "Eine Behauptung sagt, was du folgerst. Ein Beleg zeigt, worauf du dich stützt. Eine Annahme oder Grenze nennt die Bedingungen dafür.",
                "Deine Rechnung, eine Quelle und dein Messprotokoll sind unterschiedliche Belegarten. Ein guter Schluss erklärt, wofür jede davon gebraucht wird.",
                "Ein neuer Transferfall verändert eine Voraussetzung. Eine erneute richtige Antwort auf denselben bekannten Fall ist dagegen eine Übungswiederholung.",
            ),
            selfCheck = listOf(
                "Kann eine andere Person deine Schlussfolgerungen zu konkreten Belegen zurückverfolgen?",
                "Verknüpfst du Hypothese, Vorhersage und beide Versuche, ohne Foto und Modell oder Zustimmung und Experiment gleichzusetzen?",
                "Kannst du eine Grenze mit ihrer Konsequenz erklären und einen veränderten Fall ohne Vorlage bearbeiten?",
            ),
        ),
    )

    init {
        require(entries.keys == StationId.entries.toSet())
        require(entries.values.all { it.conceptBridges.isNotEmpty() && it.selfCheck.isNotEmpty() })
    }

    operator fun get(id: StationId): StationGuidance = entries.getValue(id)
}
