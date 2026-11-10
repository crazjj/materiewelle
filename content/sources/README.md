# Quellen und Herkunft

Die Quellenangaben und Prüfstatus sind aus dem privaten Ausgangsprojekt
übernommen. „Geprüft“ bezeichnet den jeweils beschriebenen früheren Prüfumfang,
keinen neuen vollständigen Quellenreview bei der Auskopplung. Die öffentliche
Datenbasis liegt in [versuchsdaten.json](../data/versuchsdaten.json); die
[Methodenbeschreibung](../measurement/README.md) erklärt die reproduzierbare
relative Bildauswertung. Externe Dokumente werden über ihre Fundstellen benannt;
ihre Volltexte und die privaten Aufgaben-/Planunterlagen sind im aktuellen
Dateibaum des Hauptbranches nicht enthalten. Alte GitHub-Verweise können frühere
Stände weiterhin erreichbar machen; siehe [Entstehung](../../docs/ENTSTEHUNG.md).

| ID | Quelle | Dokumentierter Prüfumfang und Grenzen |
|---|---|---|
| D1 | C. Davisson, L. H. Germer, *Diffraction of Electrons by a Crystal of Nickel*, Physical Review **30**, 705–740 (1927), [DOI](https://doi.org/10.1103/PhysRev.30.705) | Original nicht eingesehen. Die protokollierten Abrufe scheiterten (Verlag 403, offizieller Harvest-Endpunkt zuletzt 401). Keine originalgeprüfte Messreihe behaupten. |
| D2 | M. Suzuki, I. S. Suzuki, *A proper understanding of the Davisson and Germer experiments for undergraduate modern physics course*, [arXiv:1307.6049v1](https://arxiv.org/abs/1307.6049v1), 23.07.2013 | Didaktische Sekundäranalyse: S. 2, Abb. 2, und S. 5, letzter Absatz, für 54 eV / ungefähr 50°; S. 5, Abb. 6 und Gl. (33)–(34), für das Ni(111)-Strukturmodell. Keine eigene Primärtranskription, vollständige Intensitätskurve oder historische Unsicherheit. |
| J1 | C. Jönsson, *Electron Diffraction at Multiple Slits*, American Journal of Physics **42**, 4–11 (1974), Übers. D. Brandt/S. Hirschi, [DOI](https://doi.org/10.1119/1.1987592), [Universitätskopie](https://web.pdx.edu/~pmoeck/pdf/ElectronJonsson.PDF) | Gekürzte englische Übersetzung: ausgewählte Textstellen und Bilder geprüft. Druck-S. 5/8: 50 kV; S. 8: Spaltbreite und Mittenabstand; S. 9: Beobachtungsebene, Elektronenoptik und Abb. 7/8. |
| J0 | C. Jönsson, *Elektroneninterferenzen an mehreren künstlich hergestellten Feinspalten*, Zeitschrift für Physik **161**, 454–474 (1961), [DOI](https://doi.org/10.1007/BF01342460) | Bibliografisch identifiziert, nicht zusätzlich gelesen. J1 ist keine vollständige Eins-zu-eins-Übersetzung. |
| J2 | Land Sachsen-Anhalt, *Abiturprüfung 2025, Physik, erhöhtes Anforderungsniveau*, [öffentliche PDF](https://www.bildung-lsa.de/files/7b813937649657c8710d2f97f06bb735/2025_Phy_eAN_Aufgaben_V.pdf) | S. 18: b, g, L und Hinweis auf fehlenden Maßstab geprüft. Historischer Auszug und Bildfelder sind in dieser öffentlichen Fassung leer; 50 kV dort nicht nachgelesen. |
| H1 | F. Kubli, *An interview with Louis de Broglie*, Annales de la Fondation Louis de Broglie **48** (2023–2024), 21–43, [PDF](https://fondationlouisdebroglie.org/AFLB-481/aflb481m1018.pdf) | Druck-S. 21–22, 30 und 36: Erinnerungen, Lichtquanten, Disputation und Langevins Vermittlerrolle. Retrospektives Interview, keine zeitgenössische Quelle von 1924. |
| H3 | H. Sievers, *Louis de Broglie und die Quantenmechanik*, 1997/1998, [arXiv:physics/9807012](https://arxiv.org/abs/physics/9807012) | Druck-S. 23–24 und 29–31: Dissertation, Einstein-Korrespondenz und Chronologie. Historische Sekundäranalyse; ursprüngliche Briefe nicht zusätzlich im Archiv geprüft. |

H2 (Académie des sciences, Archivausstellung zu Einsteins Verbindungen nach
Frankreich) wurde nur bibliografisch aus dem Plan übernommen und nicht eingesehen.
Die Nobelvorträge von de Broglie und Davisson wurden wegen fehlgeschlagener
Abrufe nicht als gelesen verwendet. Für die Elektronenmasse bleibt der Hinweis
im Datenkanon bestehen: verwendeter CODATA-2022-Wert, NIST-Quelle in der
dokumentierten Prüfung nicht geöffnet.

<a id="j1-image"></a>
## J1-Bild: Identität und Aussagegrenzen

[jonsson-page6-xref33.png](jonsson-page6-xref33.png) ist das unverändert übernommene
Rasterobjekt xref 33 aus PDF-Seite 6, Druckseite 9 der J1-Universitätskopie.
Die frühere Extraktion mit PyMuPDF ergab 703 × 242 Pixel; das Raster enthält
Abb. 7 (Einspalt) links und Abb. 8 (Doppelspalt) rechts. Ausgewertet wird nur
der rechte Teil. Die Quelle enthält keine unabhängige absolute Kalibrierung
für dieses Raster.

- SHA-256 der Rasterdatei: `d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6`.
- SHA-256 der damals verwendeten PDF (hier nicht enthalten): `2ebd8adfbf2141a78113cf3f1c56bf813619dea4467eaf4c7cc7f883bbc7e3b3`.
- Ursprung der nativen Koordinaten: links oben im gesamten Raster, x nach rechts,
  y nach unten. Anzeigeausschnitte müssen ihren Versatz erhalten.

J1 nennt nominal b = 0,5 µm, g = 2,0 µm und 50 kV. Das Schema in Abb. 6 nennt
40 kV. Eine sichere Spannung für die einzelne ausgewertete Aufnahme ist daraus
nicht belegt. Die 350 mm beschreiben eine Beobachtungsebene mit Elektronenoptik
und nachfolgender Vergrößerung, keinen automatisch kalibrierten Fotomaßstab.
Die relative Streifenmessung und die separate 50-kV-Modellrechnung bleiben
fachlich getrennt.

**Nutzungsgrundlage offen:** Für das historische Bild ist die Grundlage einer
öffentlichen Weiterverbreitung im Projekt noch nicht abschließend dokumentiert.
Dieser Quellennachweis, die öffentliche Erreichbarkeit der PDF und der Hash
begründen keine neue Lizenz. Es wird weder Gemeinfreiheit noch eine freie Lizenz
für das Bild behauptet. Die Datei wird hier als eindeutig identifizierter Bestand
des öffentlichen Repositorys geführt; die Nutzungsgrundlage bleibt gesondert
zu klären.

## Private Provenienzangaben

A1 bezeichnet die private Aufgabenstellung (*Materiewellen und das Quantenobjekt
Elektron*, 3 Seiten), P2 den verworfenen KI-Projektplan (Fassung 2, 12.10.2026).
Ihre historischen Pfade und Hashes bleiben in `local_source_sha256` und
`source_ids` des Datenkanons erhalten. Ebenso verweisen einzelne Angaben auf
die damalige J1-PDF und das erste Bildmessungsskript. Pfade unter `referenzen/`
und `vorarbeit/` sind ausdrücklich **Bezeichner im privaten Projektarchiv** und
keine fehlenden öffentlichen Builddateien. Keine personenbezogenen
Aufgabenunterlagen oder alten Projektpläne sind Teil dieses aktuellen Dateibaums.

Der Plan ist keine fachliche Autorität; seine sechs DG-Paare sind aus der aktiven
Auswertung ausgeschlossen. Der öffentliche Bildvertrag und sein Erzeugerskript
unter `content/measurement/` reproduzieren die dokumentierte relative Messung
unabhängig von diesen privaten Dateien.

## Begrenzte Nachprüfung am 10.11.2026

Der erneute Abruf des APS-Volltextendpunkts für D1 und der AIP-Artikelseite
für J1 scheiterte am Netzwerkzugriff (HTTP 403 beim Verbindungsaufbau). Daraus
folgt weder eine Aussage über die wissenschaftlichen Inhalte noch eine
Nutzungserlaubnis. Es wurden keine neuen historischen Messdaten, Unsicherheiten
oder Bildrechte ergänzt. Der oben beschriebene Prüfstatus bleibt bestehen.
