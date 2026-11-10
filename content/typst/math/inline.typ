// Build-time only, owned sources. No browser compiler, network or runtime eval.
#import "../vorlage.typ": papier
#let catalog = json("formulas.json")
#let key = sys.inputs.at("formula", default: "energy-gain")
#let theme = sys.inputs.at("theme", default: "light")
#assert(theme in ("light", "dark"))
#let item = catalog.filter(it => it.id == key).first()
#let colors = if theme == "dark" {
  (ink: rgb("eaf1f5"), energy: rgb("efc16a"), momentum: rgb("c6a4f4"), wave: rgb("59d8ee"))
} else {
  (ink: rgb("182830"), energy: papier.energy, momentum: papier.momentum, wave: papier.wave)
}
#let energy(body) = text(fill: colors.energy, body)
#let momentum(body) = text(fill: colors.momentum, body)
#let wave(body) = text(fill: colors.wave, body)
#let geometry-focus(body) = text(fill: colors.energy, body)
#set document(title: item.alt, author: "Materiewellen", date: none)
#set page(width: auto, height: auto, margin: 2pt, fill: none)
#set text(font: "Latin Modern Math", size: 22pt, fill: colors.ink)
#set math.equation(block: false)
#eval("$ " + item.source + " $", mode: "markup", scope: (energy: energy, momentum: momentum, wave: wave, geofocus: geometry-focus))
