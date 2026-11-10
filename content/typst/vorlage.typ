// Shared print identity: selected K2 T3 L2 S2 N3 Q1 F3, plus semantic math roles.
#let vorlage = json("auswahl.json")
#let papier = (
  ink: rgb("242628"), muted: rgb("5b6064"), accent: rgb("006c80"),
  energy: rgb("875600"), momentum: rgb("6d48a4"), wave: rgb("006c80"),
)
#let energie(body) = text(fill: papier.energy, body)
#let impuls(body) = text(fill: papier.momentum, body)
#let wellenlaenge(body) = text(fill: papier.wave, body)
#let nummer(n) = text(font: "Latin Modern Mono", fill: papier.accent, size: 8.5pt)[(#n)]
#let online-station(key) = link("https://crazj.me/materiewelle/#lernen/" + key)[Lernstation öffnen ↗]
#let rollenlegende = [Farbmarkierungen: #energie[Energie (K, eU)], #impuls[Impuls (p)], #wellenlaenge[Wellenlänge (λ)]. Die Formelzeichen tragen die Zuordnung auch ohne Farbe.]

#let artikel-satz(body) = {
  set page(paper: "a4", margin: (x: vorlage.margin_mm * 1mm, top: 20mm, bottom: 21mm),
    header: {
      set text(font: "Latin Modern Roman", size: 8pt, fill: papier.muted)
      [MATERIEWELLEN #h(1fr) FACHFASSUNG]
      v(4pt)
      line(length: 100%, stroke: vorlage.rule_pt * 1pt + papier.muted)
    },
    footer: context {
      set text(font: "Latin Modern Roman", size: 8pt, fill: papier.muted)
      [Materiewellen · KI-vorbereitete Fachfassung #h(1fr) #counter(page).display()]
    },
  )
  set text(font: "Latin Modern Roman", size: vorlage.body_pt * 1pt, lang: "de", hyphenate: true, fill: papier.ink)
  set par(justify: vorlage.justify, leading: vorlage.leading_em * 1em, spacing: vorlage.spacing_em * 1em)
  set heading(numbering: none)
  show heading.where(level: 1): it => block(above: 4pt, below: 5pt, sticky: true)[
    #text(size: vorlage.title_pt * 1pt, weight: "regular", hyphenate: false)[#it.body]
  ]
  show heading.where(level: 2): it => {
    counter("artikel-abschnitt").step()
    context {
      let n = counter("artikel-abschnitt").get().first()
      block(above: 11pt, below: 5pt, sticky: true)[
        #grid(columns: (vorlage.heading_column_mm * 1mm, 1fr), gutter: vorlage.heading_gutter_mm * 1mm,
          text(font: "Latin Modern Mono", fill: papier.accent, size: 10pt)[#if n < 10 { "0" }#n],
          text(size: 11.4pt, weight: "bold")[#it.body],
        )
      ]
    }
  }
  show math.equation: set text(font: "Latin Modern Math", size: 11.5pt)
  set math.equation(numbering: nummer)
  show ref: set text(fill: papier.accent)
  set table(stroke: (top: none, left: none, right: none, bottom: vorlage.rule_pt * 1pt + papier.muted), inset: (x: 5pt, y: 5pt), align: left)
  set list(indent: 11pt)
  set enum(indent: 12pt)
  body
}

#let dokumenttitel(code, title, section: none, station: none) = block(sticky: true, above: 0pt, below: 10pt)[
  #if section != none {
    context [#metadata((id: section, title: title, page: counter(page).get().first())) <document-section>]
  }
  #heading(level: 1, title)
  #text(size: 9.3pt, fill: papier.muted)[#code #if station != none { h(1fr); online-station(station) }]
]

#let ergebnisrand(title, body) = block(above: 8pt, below: 9pt, inset: (left: 10pt), stroke: (left: vorlage.rule_pt * 1pt + papier.accent))[
  #text(size: 9pt, weight: "bold")[#title]
  #v(3pt)
  #body
]
