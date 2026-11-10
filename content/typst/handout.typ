#import "vorlage.typ": papier, energie, impuls, wellenlaenge
#import "fachwerte.typ": *

#set document(title: "Materiewellen · Handout", author: "Materiewellen-Projekt", date: none)
#set page(paper: "a4", margin: (x: 20mm, top: 19mm, bottom: 18mm),
  header: [#set text(size: 9.5pt, fill: papier.muted)
    Materiewellen #h(1fr) Physik-LK · Handout
    #v(3pt)#line(length: 100%, stroke: .4pt + papier.muted)],
  footer: context [#set text(size: 9.5pt, fill: papier.muted)
    Stand 10.11.2026 · v1.7 #h(1fr) #counter(page).display() / 3])
#set text(font: "Latin Modern Roman", size: 11pt, lang: "de", fill: papier.ink, hyphenate: true)
#set par(justify: false, leading: .62em, spacing: .8em)
#set heading(numbering: none)
#show heading.where(level: 1): it => block(above: 5pt, below: 13pt, sticky: true)[#text(size: 22pt, weight: "regular")[#it.body]]
#show heading.where(level: 2): it => block(above: 15pt, below: 7pt, sticky: true)[#text(size: 12pt, weight: "bold")[#it.body]]
#show math.equation: set text(font: "Latin Modern Math", size: 11pt)
#set math.equation(numbering: none)
#set table(stroke: (top: none, left: none, right: none, bottom: .35pt + luma(180)), inset: (x: 4pt, y: 5pt), align: left)
#let quelle(body) = block(above: 9pt)[
  #set par(leading: .45em)
  #text(size: 9.5pt, fill: papier.muted)[#body]]
#let merk(body) = block(above: 6pt, below: 7pt, inset: (left: 9pt), stroke: (left: .7pt + papier.accent))[#body]
#let etikett(body) = text(size: 9.5pt, fill: papier.muted, hyphenate: false)[#body]
#let kapitel(nummer, body) = block(above: 0pt, below: 3pt, sticky: true)[
  #text(size: 10pt, fill: papier.accent, weight: "bold")[#nummer] #h(6pt)#etikett(body)]
#let j-bild = fakten.jonsson.image
#let j-marken = j-bild.markers_px
#let j-intervalle = j-marken.len() - 1
#let j-abstaende = range(1, j-marken.len()).map(i => j-marken.at(i) - j-marken.at(i - 1))
#let j-mittel = (j-marken.last() - j-marken.first()) / j-intervalle

#kapitel("01", [Vorhersage · de Broglie, 1923/24])
= Eine Wellenlänge für Elektronen

#etikett[Nachschlagehandout mit Musterwerten · Erst selbst rechnen und messen, dann vergleichen.]

Elektronen lassen sich als geladene Teilchen beschleunigen. Beugungsversuche
zeigen zugleich ein Verhalten, für dessen Erklärung eine Wellenlänge benötigt wird.
De Broglies Hypothese von 1923/24 verbindet beide Beschreibungen:
$ wellenlaenge(lambda) = h / impuls(p). $
Dabei sind $p$ der Impulsbetrag und $h$ das Plancksche Wirkungsquantum.
Die Wellenlänge ist weder Elektronendurchmesser noch eine wellenförmige Bahn.

== Vorgeschichte und Einsteins Einschätzung
Planck führte 1900 Energieelemente für materielle Resonatoren ein;
Einsteins Lichtquantenansatz von 1905 ordnete dem Licht selbst Energieportionen
$E_γ = h f$ zu. Die Übertragung auf Materie war eine zusätzliche Hypothese.
Langevin vermittelte de Broglies Dissertation an Einstein. Dessen positive
Einschätzung unterstützte die theoretische Aufnahme; sie war kein experimenteller Beweis.

== Von der Beschleunigungsspannung zur Vorhersage
#grid(columns: (1fr, 1fr), gutter: 12pt,
  [*Elektrische Arbeit:* Start aus Ruhe, verlustfreie Beschleunigung. $e$ und $U$ sind positive Beträge.
   $ energie(E_"kin") = e U $],
  [*Mechanik:* langsam gegenüber der Lichtgeschwindigkeit; $m_e$ ist die Elektronenmasse.
   $ E_"kin" = 1/2 m_e v^2, quad p = m_e v $])
Aus $v = p/m_e$ folgt $E_"kin" = p^2/(2m_e)$. Gleichsetzen und nach dem positiven
Impulsbetrag auflösen:
$ p^2 / (2m_e) = e U quad => quad impuls(p) = sqrt(2m_e e U). $
Erst jetzt wird de Broglies Hypothese eingesetzt; „NR“ steht für nichtrelativistisch:
$ wellenlaenge(lambda_("NR"))(U) = h / sqrt(2m_e e U). $
Einheitenkontrolle: $"J s" / ("kg m/s") = "m"$.

== Kontrollrechnung und Größenordnung
Mit $h = "6,62607015" dot 10^(-34) thin "J s"$, $e = "1,602176634" dot 10^(-19) thin "C"$
und $m_e ≈ "9,1093837139" dot 10^(-31) thin "kg"$ ergibt sich bei $U = 150 thin "V"$:
$ E_"kin" ≈ "2,4033" dot 10^(-17) thin "J", quad p ≈ "6,617" dot 10^(-24) thin "kg m/s", $
$ wellenlaenge(lambda_("NR")) ≈ #zahl(nr(150) * 1e12, digits: 3) thin "pm" ≈ "0,100" thin "nm". $
#merk[Eine atomare Längenskala: Ein periodischer Kristall kann als Beugungsstruktur
dienen. Vierfache Spannung ergibt doppelten Impuls und halbe Wellenlänge:
bei $600 thin "V"$ etwa $#zahl(nr(600) * 1e12, digits: 3) thin "pm"$.]
Die Rechnung liefert eine *Modellvorhersage*. Energieerhaltung allein beweist
die Materiewellenhypothese nicht; ihre Prüfung benötigt experimentelle Befunde.

#quelle[Historische Einordnung: H1 F. Kubli, _An interview with Louis de Broglie_,
Annales de la Fondation Louis de Broglie 48 (2023–2024), Druck-S. 30/36;
H3 H. Sievers, _Louis de Broglie und die Quantenmechanik_ (1998),
#link("https://arxiv.org/abs/physics/9807012")[arXiv:physics/9807012], Druck-S. 29–31.
Spätere Erinnerung und Sekundäranalyse; keine neue Archivprüfung.]

#pagebreak()
#kapitel("02", [Kristallbeugung · Davisson–Germer, 1927])
= Elektronen am Nickelkristall

Eine Elektronenquelle liefert beschleunigte Elektronen. Ein Nickel-Einkristall
streut sie; ein beweglicher Detektor registriert das Signal in verschiedenen
Richtungen. Die Sekundäranalyse D2 berichtet ein Maximum bei $54 thin "eV"$ und etwa
$50 degree$. Aus Richtung und Kristallstruktur folgt eine Wellenlänge zum Vergleich
mit de Broglies Vorhersage.

#grid(columns: (1fr, 1fr), gutter: 12pt,
  [#block(height: 43mm)[
    #place(dx: 22mm, dy: 2mm)[#rect(width: 25mm, height: 8mm, stroke: .5pt + papier.muted, radius: 2pt)[#align(center + horizon)[#etikett[Quelle / $U$]]]]
    #place(dx: 34.5mm, dy: 10mm)[#line(start: (0pt, 0pt), end: (0pt, 24mm), stroke: 1pt + papier.accent)]
    #place(dx: 34.5mm, dy: 34mm)[#line(start: (0pt, 0pt), end: (27mm, -22mm), stroke: 1pt + papier.accent)]
    #place(dx: 60mm, dy: 10.5mm)[#circle(radius: 1.5mm, fill: papier.accent)]
    #place(dx: 53mm, dy: 2mm)[#etikett[Detektor]]
    #place(dx: 8mm, dy: 34mm)[#line(length: 65mm, stroke: 1.3pt + papier.muted)]
    #place(dx: 21mm, dy: 36mm)[#etikett[Ni(111)-Oberfläche]]
    #place(dx: 34.5mm, dy: 23mm)[#image(bytes(
      "<svg xmlns='http://www.w3.org/2000/svg' width='10' height='5' viewBox='-0.2 -0.2 10 5'><path d='M0 0 A11 11 0 0 1 8.53 4.05' fill='none' stroke='#b06b1d' stroke-width='.25'/></svg>"),
      width: 10mm, height: 5mm)]
    #place(dx: 39mm, dy: 18mm)[#etikett[$φ$]]
    #place(dx: 1mm, dy: 16mm)[#etikett[normaler#linebreak()Einfall]]
   ]
   #etikett[Aufbau: normaler Einfall auf Ni(111). $φ$ misst die Detektorrichtung gegen die äußere Normale (senkrechte Linie).]],
  [#block(height: 43mm)[#image("handoutassets/bragg-summary.svg", width: 100%)]
   #etikett[Bragg-Konstruktion: $A$ und $B$ sind Streupunkte; $P$ und $Q$ sind Lotfußpunkte auf den unteren Strahlabschnitten. $P B$ und $B Q$ sind Zusatzwege. $d$: Ebenenabstand; $θ_B$: Winkel zur Ebene.]])

== Bragg-Bedingung: warum bestimmte Richtungen?
Die beiden Zusatzwege betragen je $d sin θ_B$. Konstruktive Überlagerung verlangt
einen gesamten Gangunterschied von einem ganzzahligen Vielfachen der Wellenlänge:
$ P B = B Q = d sin θ_B, quad 2d sin θ_B = n λ. $
Das ist eine idealisierte Ebenenkonstruktion. $θ_B$ wird *zur Netzebene* gemessen.

== Der gewählte Zahlenfall: Oberflächenreihen
Bragg erklärt das Prinzip der Wegunterschiede. Den $54 thin "eV"$-/$50 degree$-Befund
rechnen wir in der von D2 angegebenen *Oberflächengeometrie*: $φ$ liegt *zur äußeren
Normalen*, $D$ ist der Abstand der betrachteten Atomreihen in der gewählten Richtung.
Bei normalem Einfall sind die einlaufenden Wege gleich lang. Auslaufend bleibt
$D sin φ$; bei Bragg waren es zwei Zusatzwege. Für erste Ordnung gilt:
$ D = a sqrt(3/8) ≈ #zahl(d-reihe * 1e9, digits: 4) thin "nm", quad λ_("geo") = D sin φ. $
Die kubische Gitterkonstante von Nickel ist unabhängig gesetzt: $a = "0,352" thin "nm"$.
Der Oberflächenreihenabstand $D$ ist nicht automatisch der Bragg-Ebenenabstand $d$;
auch eine allgemeine Halbierung des Detektorwinkels ist unzulässig.

#table(columns: (1fr, 1fr),
  [*Aus Geometrie und Struktur*], [*Aus de Broglies Hypothese*],
  [$λ_("geo") ≈ #zahl(dg-geo * 1e9, digits: 3) thin "nm"$],
  [$λ_("NR")(54 thin "V") ≈ #zahl(dg-nr * 1e9, digits: 3) thin "nm"$])
#merk[Die relative Differenz $(λ_("geo") - λ_("NR"))/λ_("NR")$ beträgt etwa
$#zahl(dg-prozent, digits: 1) thin "%"$. Das ist ein Größenvergleich, kein statistischer
Übereinstimmungsnachweis: historische Unsicherheiten sind nicht belegt.]

#quelle[D2: M. Suzuki / I. S. Suzuki, _A proper understanding of the Davisson and
Germer experiments for undergraduate modern physics course_,
#link("https://arxiv.org/abs/1307.6049")[arXiv:1307.6049v1],
S. 2, 5–6, Abb. 6/7, Gl. 31–34. Berichteter Befund und erklärtes Strukturmodell.
D1: Davisson/Germer, _Phys. Rev._ 30 (1927), 705–740,
#link("https://doi.org/10.1103/PhysRev.30.705")[DOI 10.1103/PhysRev.30.705]; Original im Projekt nicht eingesehen.]

#pagebreak()
#kapitel("03", [Künstliche Spalte · Jönsson, 1961])
= Elektronen am Doppelspalt

Anders als beim Kristall werden die Öffnungen gezielt hergestellt. Feine,
parallele Spalte, kohärente Beleuchtung und präzise Ausrichtung machen die
Interferenz sichtbar. Elektrische oder magnetische Felder wirken als
Elektronenlinsen und vergrößern das Bild der feinen Verteilung.

#block(height: 33mm)[
  #place(dx: 7mm, dy: 0mm)[#etikett[Spaltblende · von vorn]]
  #place(dx: 8mm, dy: 7mm)[#rect(width: 42mm, height: 15mm, fill: luma(93%), stroke: .5pt + papier.muted)]
  #for x in (20mm, 30mm) [
    #place(dx: x, dy: 9mm)[#rect(width: 2mm, height: 11mm, fill: white, stroke: .6pt + papier.accent)]
  ]
  // b is the opening width; g runs between the centres, not between inner edges.
  #place(dx: 20mm, dy: 5mm)[#line(length: 2mm, stroke: .5pt + papier.ink)]
  #for x in (20mm, 22mm) [#place(dx: x, dy: 4mm)[#line(end: (0pt, 2mm), stroke: .5pt + papier.ink)]]
  #place(dx: 15mm, dy: 3mm)[#etikett[$b$]]
  #place(dx: 21mm, dy: 25mm)[#line(length: 10mm, stroke: .5pt + papier.ink)]
  #for x in (21mm, 31mm) [#place(dx: x, dy: 23mm)[#line(end: (0pt, 4mm), stroke: .5pt + papier.ink)]]
  #place(dx: 25mm, dy: 27mm)[#etikett[$g$]]
  #place(dx: 59mm, dy: 12mm)[#etikett[Überlagerung]]
  #place(dx: 68mm, dy: 17mm)[$arrow.r$]
  #place(dx: 102mm, dy: 0mm)[#etikett[Modell · Streifenbild]]
  // Finite-slit teaching pattern: sinc²(b/g · t) cos²(t), b/g = 2/10.
  // One continuous gradient avoids PDF antialiasing seams between narrow strips.
  // It is a front-view illustration, not a scale or a fit for the historical scan.
  #let stops = range(251).map(i => {
    let t = calc.pi * (i / 250 * 50 - 25) / 10
    let u = .2 * t
    let envelope = if calc.abs(u) < 1e-9 { 1 } else { calc.pow(calc.sin(u) / u, 2) }
    let brightness = envelope * calc.pow(calc.cos(t), 2)
    let gray = str(calc.round((.12 + .88 * brightness) * 255))
    "<stop offset='" + str(i / 250 * 100) + "%' stop-color='rgb(" + gray + "," + gray + "," + gray + ")'/>"
  }).join()
  #let pattern = "<svg xmlns='http://www.w3.org/2000/svg' width='250' height='75'><defs><linearGradient id='intensity'>" + stops + "</linearGradient></defs><rect width='250' height='75' fill='url(#intensity)'/></svg>"
  #place(dx: 102mm, dy: 7mm)[#image(bytes(pattern), width: 50mm, height: 15mm)]
  #place(dx: 127mm, dy: 25mm)[#line(length: 10mm, stroke: .5pt + papier.ink)]
  #for x in (127mm, 137mm) [#place(dx: x, dy: 23mm)[#line(end: (0pt, 4mm), stroke: .5pt + papier.ink)]]
  #place(dx: 130mm, dy: 27mm)[#etikett[$Δ y$]]
]
#etikett[Eigene Skizze, nicht maßstäblich. $b$: Spaltbreite; $g$: Mittenabstand; $Δ y$: Streifenabstand.]

== Vergleich mit Licht und Modellrechnung
Beide Spalte tragen zum Muster bei. Im idealisierten paraxialen Fernfeld gilt
für den Streifenabstand $Δ y ≈ L λ/g$. Hier ist $L$ der Abstand von der Spaltebene
zur Beobachtungsebene. Größere Wellenlänge oder größeres $L$ vergrößern den Abstand;
größerer Spaltmittenabstand $g$ verkleinert ihn. Die Spaltbreite prägt die
Beugungshülle. Dieses freie Fernfeldmodell ist keine Fotoeichung.

== Was lässt sich am vorhandenen Scan messen?
Die *historische Aufnahme* aus J1 enthält Einspaltbild (links, Abb. 7) und
Zweispaltbild (rechts, Abb. 8) in $#fakten.jonsson.image.native_width_px × #fakten.jonsson.image.native_height_px$ Originalpixeln. Die Website zeigt zunächst den Zweispaltausschnitt.
Im Band $#j-bild.measurement_band_y_px.first() ≤ y < #j-bild.measurement_band_y_px.last()$ sind als
*dokumentiertes Referenzbeispiel* acht Maxima bei
$x = #j-marken.map(str).join(", ")$ markiert.
Sie umfassen sieben Intervalle:
$ overline(Δ x) = (x_N - x_0)/N = (#j-marken.last() - #j-marken.first())/#j-intervalle = #(j-marken.last() - j-marken.first())/#j-intervalle ≈ #zahl(j-mittel, digits: 1) thin "px". $
Die Einzelabstände sind $#j-abstaende.map(str).join(", ") thin "px"$.
Begründe bei einer eigenen Messung die Auswahl der Maxima und benenne das Band.
Zoom verändert die Originalpixelkoordinaten nicht.
#merk[Ein aus dem Bild berechnetes Profil zeigt *digitale Grauwerte*, keine
kalibrierte Elektronenintensität. Ohne unabhängigen Bildmaßstab und gesicherte
Abbildungsgeometrie folgt aus dem Pixelabstand keine absolute Wellenlänge.]

== Hochspannung: Grenze der nichtrelativistischen Näherung
Der getrennte Modellfall bei $50 thin "kV"$ liefert etwa
$λ_("NR") = #zahl(j-nr * 1e12, digits: 3) thin "pm"$ und
$λ_("rel") = #zahl(j-rel * 1e12, digits: 3) thin "pm"$ („rel“: relativistisch).
Die NR-Abweichung beträgt $#zahl(j-prozent, digits: 2) thin "%"$ relativ zu $λ_("rel")$.
Der Artikel nennt einen 50-kV-Textfall, ein Schema zeigt 40 kV; die Zuordnung
zur ausgewerteten Aufnahme ist offen.

== Was beide Versuche zeigen
De Broglie liefert eine Hypothese und quantitative Vorhersagen. DG untersucht
Elektronenbeugung an einem Kristall, Jönsson ein Muster an künstlichen Spalten.
Die Befunde stützen eine Wellenbeschreibung des Elektrons, zeigen aber keine
wellenförmige Flugbahn. Begründe dein Fazit mit einem konkreten Befund und
nenne seine Modell- oder Messgrenze.

#quelle[J1: C. Jönsson, _Electron Diffraction at Multiple Slits_, _Am. J. Phys._ 42
(1974), 4–11, #link("https://doi.org/10.1119/1.1987592")[DOI 10.1119/1.1987592], Druck-S. 8–9: gekürzte englische Übersetzung
des Berichts von 1961. Pixelwerte: Referenzbeispiel aus der Projektauswertung,
keine eigene Messung des Lesers. Die öffentliche Nutzungsgrundlage der Foto-Reproduktion
ist im Projekt noch nicht abschließend dokumentiert; dieses Handout verwendet eigene Schemata.]
#quelle[Texte, Code und Schemata sind KI-unterstützt entstanden. Dieses Handout
enthält Musterwerte. Die Arbeitsaufträge richten sich an die Besucher der Website
und laden zum eigenen Rechnen, Messen und Begründen ein.
Interaktive Darstellung und ausführliche Quellen:
#link("https://materiewelle.vercel.app/")[materiewelle.vercel.app].]
