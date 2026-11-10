# Fachkontext für den eigenen KI-Chat

Das Fachpaket 3.3.0 enthält vier kurze Starttexte: De Broglie, Davisson–Germer,
Jönsson und Fazit. Die Website verbindet den passenden Text mit der gewählten
Hilfeform, einer optionalen eigenen Frage und ausdrücklich ausgewählten Angaben aus dem Kapitel. Sie
kontaktiert keinen KI-Anbieter und erzeugt selbst keine Chatantwort. Erst das
Einfügen und Absenden im eigenen Chat übermittelt diese Nachricht an den Anbieter.

Die Vorschau zeigt den gesamten Kopiertext. Eigene Notizen und Messungen bleiben
ohne Auswahl draußen; eine Änderung des Arbeitskontexts setzt die Auswahl zurück.
Bei gesperrter Zwischenablage kann der Text manuell markiert und kopiert werden.
**Das Jönsson-Bild wird nicht mitkopiert.** Der Jönsson-Starttext bietet Methodenhilfe
ohne Referenzkoordinaten oder Referenzmittelwert und ersetzt keine eigene Markierung.

Die aktuelle Frage und die Hilfeform bestimmen die Antwort: **Hinweis** gibt einen
nächsten Schritt, **Erklärung** erklärt den Zusammenhang, **Ansatz prüfen** prüft
die tatsächlich vorgelegte Bearbeitung. Eine ausdrückliche Bitte in der Frage hat
bei einem Widerspruch Vorrang. Ohne zusätzliche Frage beginnt die Hilfe beim
Kapitel und den vorhandenen Angaben; fehlende Prüfbasis wird gezielt erfragt.
Der Datenanhang liefert Fakten zum Arbeitsstand und zu prüfende Beiträge, keine
weiteren Antwortaufträge. Fachliche Grenzen gelten unabhängig von der Hilfeform:
Ein nicht beigefügtes Bild erlaubt beispielsweise keine visuelle Maximalprüfung.

## Bauen und prüfen

Aus dem Repository-Stamm, nur mit der Python-Standardbibliothek:

```bash
python3 content/tutor/build-compact-pack.py --output-dir /tmp/materiewelle-tutor
python3 content/tutor/test-compact-pack.py
```

Die gemeinsamen Anweisungen und vier Fachreferenzen liegen in
[materiewellen-fachtutor](materiewellen-fachtutor/). Der Build erzeugt vier
`START-<thema>.md`-Texte, den De-Broglie-Alias `START-IM-CHAT.md`, ein
deterministisches ZIP und ein Manifest mit Größen, Version und SHA-256.
Es gibt keine Mindestwortzahl; gemeinsamer Antwortauftrag und Fachreferenz müssen
vorhanden sein. Die Obergrenze von 1200 Wörtern begrenzt den Kapitelkontext.
Die Tests prüfen tatsächliche Dateien, gemeinsame Quellen, kurze und leere
Referenzen, fehlende Grenzen zwischen Skill und Kopiertext sowie den Schutz vor
Überschreiben der verfassten Quellen. Die Wortzahl im Manifest beschreibt den
erzeugten Text und wird zusammen mit Dateigröße und Hash geprüft.

Das ZIP ist ein zusätzlicher Weg für Clients mit einem bekannten passenden
Importverfahren. Die Website setzt keinen nativen Skillimport voraus. Pakettests
belegen Dateiintegrität und Reproduzierbarkeit, keine allgemeine Antwortqualität
oder Lernwirkung eines externen Chats. Der
[Quellenstand](../sources/README.md) bleibt von Modellrechnungen und eigenen
Angaben getrennt. Vorschläge des Chats müssen auf Fachbegriffe, Einheiten,
Quellen und Modellgrenzen geprüft werden.
