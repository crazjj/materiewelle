// One canonical input contract shared with the app and portable tutor.
// Numbers are computed here; historical roles and limitations remain prose.
#let fakten = json("../data/versuchsdaten.json")
#assert.eq(fakten.schema_version, "2.0", message: "Typst-Datenadapter für neue Schemafassung prüfen")
#let konstanten = fakten.constants
#let h-wert = konstanten.h.value
#let e-wert = konstanten.e.value
#let m-wert = konstanten.electron_mass.value
#let c-wert = konstanten.c.value
#let dg = fakten.davisson_germer
#assert.eq(dg.rows.len(), 1)
#assert.eq(dg.evidence.source_id, "D2")
#let dg-fall = dg.rows.first()
#assert.eq(dg-fall.voltage_V, 54)
#assert.eq(dg-fall.phi_deg, 50)
#let a-wert = dg.geometry.lattice_constant_m
#let d-reihe = a-wert * calc.sqrt(3 / 8)
#let nr(u) = h-wert / calc.sqrt(2 * m-wert * e-wert * u)
#let rel(u) = h-wert / calc.sqrt(2 * m-wert * e-wert * u * (1 + e-wert * u / (2 * m-wert * c-wert * c-wert)))
#let dg-geo = d-reihe * calc.sin(dg-fall.phi_deg * 1deg)
#let dg-nr = nr(dg-fall.voltage_V)
#let dg-winkel-nr = calc.asin(dg-nr / d-reihe)
#let dg-prozent = 100 * (dg-geo - dg-nr) / dg-nr
#let j-modell = fakten.jonsson.parameter_model
#assert.eq(j-modell.voltage_V, 50000)
#let j-nr = nr(j-modell.voltage_V)
#let j-rel = rel(j-modell.voltage_V)
#let j-prozent = 100 * (j-nr - j-rel) / j-rel
#let j-periode = j-modell.observation_distance_m * j-rel / j-modell.slit_center_spacing_m
#let j-fresnel = calc.pow(j-modell.slit_center_spacing_m + j-modell.slit_width_m, 2) / (j-rel * j-modell.observation_distance_m)
// Synthetic S13 teaching case: free propagation, intentionally not historical.
#let lehr-lambda = nr(150)
#let lehr-periode = 10 * lehr-lambda / 2e-6
#let lehr-fresnel-max = calc.pow(5e-6 + 0.9e-6, 2) / (lehr-lambda * 10)
#let zahl(n, digits: 3) = str(calc.round(n, digits: digits)).replace(".", ",")
