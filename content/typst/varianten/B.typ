// Review variant B. Canonical handout and production builder remain unchanged.
#import "../vorlage.typ": papier, energie, impuls, wellenlaenge
#import "../fachwerte.typ": *

#set document(title: "Materiewellen · Handout · Variante B", author: "Materiewellen-Projekt", date: none)
#set page(paper: "a4", margin: (x: 20mm, top: 19mm, bottom: 18mm),
  header: [#set text(size: 9.5pt, fill: papier.muted)
    Materiewellen #h(1fr) Handout · Variante B
    #v(4pt)#line(length: 100%, stroke: .4pt + papier.muted)],
  footer: context [#set text(size: 9.5pt, fill: papier.muted)
    Vorschau zur Auswahl · 10.11.2026 #h(1fr) #counter(page).display() / 3])
#set text(font: "Latin Modern Roman", size: 11pt, lang: "de", fill: papier.ink, hyphenate: true)
#set par(justify: false, leading: .56em, spacing: .7em)
#set heading(numbering: none)
#show heading.where(level: 1): it => block(above: 5pt, below: 12pt, sticky: true)[#text(size: 24pt, weight: "regular")[#it.body]]
#show heading.where(level: 2): it => block(above: 12pt, below: 6pt, sticky: true)[#text(size: 12pt, weight: "bold")[#it.body]]
#show heading.where(level: 3): it => block(above: 9pt, below: 4pt, sticky: true)[#text(size: 11pt, weight: "bold")[#it.body]]
#show math.equation: set text(font: "Latin Modern Math", size: 11pt)
#set math.equation(numbering: none)
#set table(stroke: (top: none, left: none, right: none, bottom: .35pt + luma(180)), inset: (x: 4pt, y: 5pt), align: left)
#let klein(body) = text(size: 9.5pt, fill: papier.muted)[#body]
#let marke(n, body) = block(sticky: true, above: 0pt, below: 3pt)[#text(size: 10pt, fill: papier.accent, weight: "bold")[#n] #h(7pt)#klein(body)]
#let merk(body) = block(above: 7pt, below: 7pt, inset: (left: 9pt), stroke: (left: .7pt + papier.accent))[#body]
#let regel = line(length: 100%, stroke: .4pt + luma(180))
#let j-bild = fakten.jonsson.image
#let j-marken = j-bild.markers_px
#let j-intervalle = j-marken.len() - 1
#let j-abstaende = range(1, j-marken.len()).map(i => j-marken.at(i) - j-marken.at(i - 1))
#let j-mittel = (j-marken.last() - j-marken.first()) / j-intervalle

#marke("01", [Idee und Vorhersage])
= Materie mit einer Wellenlänge

De Broglies Hypothese verknüpft den *Impulsbetrag* eines Elektrons mit einer
Wellenlänge. Erst der Vergleich mit Beugung und Interferenz zeigt, ob diese
Vorhersage zum Versuch passt.

#grid(columns: (1fr, 1fr, 1fr), gutter: 15pt,
  [#text(fill: papier.accent, weight: "bold")[1900 · Planck]
   #v(4pt)
   Energieelemente für materielle Resonatoren: Die Quantenidee beginnt bei der
   Beschreibung der Wärmestrahlung.],
  [#text(fill: papier.accent, weight: "bold")[1905 · Einstein]
   #v(4pt)
   Der Lichtquantenansatz ordnet dem Licht selbst Energieportionen $E_γ = h f$ zu.],
  [#text(fill: papier.accent, weight: "bold")[1923/24 · de Broglie]
   #v(4pt)
   Die Übertragung auf Materie ist eine zusätzliche Hypothese:
   $ wellenlaenge(λ) = h / impuls(p). $])

== Einsteins Urteil: Unterstützung, kein Nachweis
Nach de Broglies späterer Erinnerung vermittelte Langevin die Dissertation an
Einstein. Dessen positive Einschätzung unterstützte die wissenschaftliche Aufnahme
der Idee. Sie war eine theoretische Beurteilung, kein experimenteller Beweis.
Die historische Einordnung folgt H1/H3; Quellen und Prüfumfang stehen auf Seite 3.

#v(4pt)
#regel
#grid(columns: (1.04fr, 1fr), gutter: 23pt,
  [== Von der Spannung zum Impuls
   *Annahmen:* Start aus Ruhe, keine Energieverluste; nichtrelativistisch, also
   langsam gegenüber der Lichtgeschwindigkeit.
   $ energie(E_"kin") = e U, quad E_"kin" = 1/2 m_e v^2 $
   Mit $p = m_e v$ folgt $E_"kin" = p^2/(2m_e)$. Gleichsetzen und die positive
   Wurzel ziehen:
   $ p^2/(2m_e) = e U $
   $ impuls(p) = sqrt(2m_e e U). $
   Erst jetzt kommt de Broglies Hypothese hinzu:
   $ wellenlaenge(λ_"NR")(U) = h/sqrt(2m_e e U). $
   #klein[$e$: positiver Betrag der Elektronenladung; $U$: Betrag der Beschleunigungsspannung;
   $m_e$: Elektronenmasse; $h$: Plancksches Wirkungsquantum. „NR“: nichtrelativistisch.]
  ],
  [== Kontrollfall: 150 Volt
   #klein[Die Zahlen sind Modellwerte, keine eigene Messung.]
   $ E_"kin" ≈ "2,4033" dot 10^(-17) thin "J" $
   $ p ≈ "6,617" dot 10^(-24) thin "kg m/s" $
   #merk[$ wellenlaenge(λ_"NR") ≈ #zahl(nr(150) * 1e12, digits: 3) thin "pm" $
   $ ≈ "0,100" thin "nm". $]
   Das ist eine *atomare Längenskala*. Ein periodisch aufgebauter Kristall
   eignet sich deshalb als Beugungsstruktur.
   Bei $600 thin "V"$ ergibt sich etwa
   $#zahl(nr(600) * 1e12, digits: 3) thin "pm"$: vierfache Spannung,
   doppelter Impulsbetrag, halbe Wellenlänge.
   #v(3pt)
   #klein[Rechenkonstanten:
   $h = "6,62607015" dot 10^(-34) thin "J s"$;
   $e = "1,602176634" dot 10^(-19) thin "C"$;
   $m_e ≈ "9,1093837139" dot 10^(-31) thin "kg"$.
   Einheitenkontrolle: $"J s" / ("kg m/s") = "m"$.]
  ])

#merk[Die Wellenlänge ist weder Elektronendurchmesser noch wellenförmige Flugbahn.
Energieerhaltung und nichtrelativistische Mechanik liefern den Impuls;
erst die zusätzliche Hypothese liefert eine Wellenlänge.]

#pagebreak()
#marke("02", [Zwei Versuche im Vergleich])
= Vom Kristall zu künstlichen Spalten

*Gemeinsamer Schluss.* Beugung am Kristall und Interferenz an künstlichen
Spalten stützen die Wellenbeschreibung des Elektrons. Historischer Befund,
Modellrechnung und heutige Bildauswertung bleiben unterschiedliche Aussagen.

#grid(columns: (1fr, 1fr), gutter: 22pt,
 [== Davisson–Germer · 1927
  #klein[Periodische Struktur: Nickel-Einkristall]
  #block(height: 42mm)[
    #place(dx: 8mm, dy: 3mm)[#klein[Quelle / Spannung $U$]]
    #place(dx: 32mm, dy: 10mm)[#line(end: (0pt, 24mm), stroke: 1pt + papier.accent)]
    #place(dx: 32mm, dy: 34mm)[#line(end: (25mm, -20.5mm), stroke: 1pt + papier.accent)]
    #place(dx: 7mm, dy: 34mm)[#line(length: 63mm, stroke: 1.1pt + papier.muted)]
    #place(dx: 21mm, dy: 36mm)[#klein[Ni(111)-Oberfläche]]
    #place(dx: 51mm, dy: 5mm)[#klein[Detektor]]
    #place(dx: 56mm, dy: 12mm)[#circle(radius: 1.3mm, fill: papier.accent)]
    #place(dx: 32mm, dy: 25mm)[#image(bytes(
      "<svg xmlns='http://www.w3.org/2000/svg' width='10' height='5' viewBox='-0.2 -0.2 10 5'><path d='M0 0 A9 9 0 0 1 6.96 3.30' fill='none' stroke='#875600' stroke-width='.25'/></svg>"),
      width: 10mm, height: 5mm)]
    #place(dx: 36mm, dy: 20mm)[#klein[$φ$]]
    #place(dx: 2mm, dy: 17mm)[#klein[senkrechter#linebreak()Einfall]]
  ]
  #klein[Eigenes Aufbauschema. $φ$: Winkel zur äußeren Oberflächennormalen.]

  Eine Quelle liefert beschleunigte Elektronen. Der Einkristall streut sie;
  ein beweglicher Detektor untersucht verschiedene Richtungen. D2 berichtet
  ein Maximum bei *54 eV* und *ungefähr 50°*.

  === Zwei unabhängige Rechenwege
  Aus $a = "0,352" thin "nm"$ folgt im ausgewählten Oberflächenmodell
  $D ≈ #zahl(d-reihe * 1e9, digits: 4) thin "nm"$. Für erste Ordnung:
  $ λ_"geo" = D sin φ $
  $ ≈ #zahl(dg-geo * 1e9, digits: 3) thin "nm". $
  Aus der kinetischen Energie, entsprechend $54 thin "V"$ bei Start aus Ruhe:
  $ λ_"NR" ≈ #zahl(dg-nr * 1e9, digits: 3) thin "nm". $
  Die relative Differenz
  $(λ_"geo" - λ_"NR")/λ_"NR"$ beträgt etwa
  $#zahl(dg-prozent, digits: 1) thin "%"$.

  #merk[*Aussagegrenze:* Der Abstand wird aus der Nickelstruktur bestimmt,
  nicht an die vorhergesagte Wellenlänge angepasst. Historische Unsicherheiten
  sind hier nicht belegt; der Vergleich ist kein statistischer
  Übereinstimmungsnachweis.]
  #klein[D2 ist eine Sekundäranalyse. D1, die Originalveröffentlichung,
  wurde im Projekt nicht eingesehen. Die beiden Geometrien stehen auf Seite 3.]
 ],
 [== Jönsson · 1961
  #klein[Periodische Struktur: künstliche Feinspalte]
  #block(above: 9pt, below: 9pt)[
    #grid(columns: (1fr, 1fr), gutter: 10pt,
      [*Quelle und Beleuchtung*#linebreak()beschleunigte Elektronen],
      [*Feinspalte*#linebreak()schmal, parallel, durchlässig],
      [*Elektronenoptik*#linebreak()Felder bilden ab und vergrößern],
      [*Aufnahme*#linebreak()helle und dunkle Streifen])
  ]
  #klein[Funktionsübersicht, keine maßstäbliche Rekonstruktion.]

  Feine Spalte, präzise Ausrichtung und stabile Beleuchtung sind technische
  Herausforderungen. *Kohärent* bedeutet hier: Die relative Phase der Beiträge
  bleibt während der Beobachtung hinreichend stabil.

  Wie beim Licht-Doppelspalt überlagern sich die Beiträge. Der Spaltmittenabstand
  $g$ bestimmt die feinen Interferenzstreifen, die Breite $b$ jedes Spalts die
  Beugungshülle. Im paraxialen Fernfeld gilt
  $ Δ y ≈ L λ/g. $
  $L$ bezeichnet hier die freie Entfernung zur Beobachtungsebene.
  Dieses Modell ist keine Kalibrierung eines vergrößerten Fotos.

  === Quantitative Größe im Scan
  J1 enthält links ein Einspaltbild (Abb. 7), rechts ein Zweispaltbild (Abb. 8).
  Das gemeinsame Raster hat $#j-bild.native_width_px × #j-bild.native_height_px$ Originalpixel.
  Die dokumentierte Referenzauswahl umfasst acht helle Maxima und sieben Abstände:
  $ overline(Δ x) = (647-551)/7 $
  $ = 96/7 ≈ #zahl(j-mittel, digits: 1) thin "px". $
  #klein[Band: $#j-bild.measurement_band_y_px.first() ≤ y < #j-bild.measurement_band_y_px.last()$.
  Marken: $x = #j-marken.map(str).join(", ") thin "px"$.
  Abstände: $#j-abstaende.map(str).join(", ") thin "px"$.]

  #merk[*Aussagegrenze:* Digitale Grauwerte sind keine kalibrierte
  Elektronenintensität. Ohne unabhängigen Bildmaßstab und gesicherte
  Abbildungsgeometrie folgt aus Pixeln keine absolute Wellenlänge.
  Zoom ändert die Originalpixelkoordinaten nicht.]
 ])

#pagebreak()
#marke("03", [Geometrie, Näherungen und Quellen])
= Was die Zahlen voraussetzen

#grid(columns: (1fr, 1fr), gutter: 22pt,
 [== Bragg: zwei Netzebenen
  #image("../handoutassets/bragg-summary.svg", width: 100%)
  #klein[$A$, $B$: Streupunkte; $P$, $Q$: Lotfußpunkte.
  $d$: senkrechter Ebenenabstand; $θ_B$: Winkel zur Netzebene.]
  Die beiden Zusatzwege betragen je $d sin θ_B$. Verstärkung verlangt einen
  gesamten Gangunterschied von $n λ$:
  $ P B = B Q = d sin θ_B, $
  $ 2d sin θ_B = n λ. $
 ],
 [== Oberflächenreihen: der D2-Fall
  Bei normalem Einfall sind die einlaufenden Wege zu den ausgewählten
  Atomreihen gleich lang. Auslaufend bleibt ein Zusatzweg:
  $ D sin φ = n λ. $
  $φ$ liegt zur *äußeren Oberflächennormalen*, $D$ ist der senkrechte Abstand
  der ausgewählten Reihen.
  Das Ni(111)-Dreiecksnetz liefert bei kubischer Gitterkonstante $a$:
  $ D = (a/sqrt(2)) sin 60 degree $
  $ = a sqrt(3/8). $
  $n = 1$ bezeichnet die hier verwendete erste Ordnung.
 ])
#merk[Beide Konstruktionen erklären Verstärkung durch Gangunterschiede.
$D$ ist nicht automatisch der Bragg-Ebenenabstand $d$; der $50 degree$-Winkel
ist kein Braggwinkel. Eine allgemeine Halbierung des Detektorwinkels ist unzulässig.]

== Hohe Spannung: relativistische Korrektur
Bei $50 thin "kV"$ liefert die nichtrelativistische Näherung einen etwas zu großen
Wert. „rel“ bezeichnet die relativistische Rechnung:
#table(columns: (1fr, 1fr, 1.1fr),
 [*Nichtrelativistisch*], [*Relativistisch*], [*Relative Überschätzung*],
 [$λ_"NR" ≈ #zahl(j-nr * 1e12, digits: 3) thin "pm"$],
 [$λ_"rel" ≈ #zahl(j-rel * 1e12, digits: 3) thin "pm"$],
 [$(λ_"NR"/λ_"rel"-1) ≈ #zahl(j-prozent, digits: 2) thin "%"$])
Der Artikel nennt einen 50-kV-Textfall, ein Schema zeigt 40 kV.
Die Spannung der ausgewerteten Aufnahme ist damit nicht eindeutig belegt:
Der Zahlenvergleich bleibt eine *getrennte Modellrechnung*.

== Quellen und Reichweite
#set par(leading: .45em)
#klein[
*H1 / H3 – historische Einordnung.* F. Kubli, _An interview with Louis de Broglie_,
AFLB 48 (2023–2024), Druck-S. 30/36; H. Sievers, _Louis de Broglie und die
Quantenmechanik_ (1998), #link("https://arxiv.org/abs/physics/9807012")[arXiv:physics/9807012],
Druck-S. 29–31. Spätere Erinnerung und Sekundäranalyse, keine neue Archivprüfung.

*D2 / D1 – Nickelbeugung.* M. Suzuki / I. S. Suzuki, _A proper understanding of
the Davisson and Germer experiments for undergraduate modern physics course_
(2013), #link("https://arxiv.org/abs/1307.6049")[arXiv:1307.6049v1], S. 2, 5–6,
Abb. 6/7, Gl. 31–34. D1: Davisson/Germer, _Phys. Rev._ 30 (1927), 705–740,
#link("https://doi.org/10.1103/PhysRev.30.705")[DOI 10.1103/PhysRev.30.705]; Original nicht eingesehen.

*J1 – Feinspalte und Reproduktion.* C. Jönsson, _Electron Diffraction at Multiple
Slits_, _Am. J. Phys._ 42 (1974), 4–11,
#link("https://doi.org/10.1119/1.1987592")[DOI 10.1119/1.1987592], Druck-S. 8–9.
Gekürzte englische Fassung des Berichts von 1961. Die Pixelwerte sind ein dokumentiertes
Referenzbeispiel der Projektauswertung, keine eigene Messung des Lesers.
Die öffentliche Nutzungsgrundlage der Foto-Reproduktion ist noch nicht abschließend
dokumentiert; dieses Handout verwendet nur eigene Schemata.
]
#block(above: 9pt)[#klein[Texte, Code und Schemata sind KI-unterstützt entstanden.
Musterwerte und Modellannahmen bleiben von historischer Beobachtung und heutiger
Bildauswertung getrennt. Interaktive Darstellung und ausführliche Quellen:
#link("https://materiewelle.vercel.app/")[materiewelle.vercel.app].]]
