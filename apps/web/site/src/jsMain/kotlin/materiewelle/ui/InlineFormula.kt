package materiewelle.ui

import androidx.compose.runtime.Composable
import materiewelle.ui.semantics.LabClass
import materiewelle.ui.semantics.styles
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span

/** Reviewed expression identity; source text and paths are not invented by the UI. */
enum class FormulaId(val key: String, val alternative: String, vararg val aliases: String) {
    ENERGY_GAIN("energy-gain", "E kin gleich e mal U: elektrische Arbeit liefert kinetische Energie.", "E_kin = eU", "E_kin = e U", "Eₖᵢₙ = e U", "K = eU", "K = e U"),
    KINETIC_MOMENTUM("kinetic-momentum", "E kin gleich p Quadrat geteilt durch zweimal Elektronenmasse.", "E_kin = p²/(2mₑ)", "E_kin = p² / (2 mₑ)", "Eₖᵢₙ = p² / (2 m)", "K = p²/(2mₑ)", "K = p² / (2 mₑ)"),
    KINETIC_BALANCE("kinetic-balance", "p Quadrat geteilt durch zweimal Elektronenmasse gleich e mal U.", "p² / (2 m) = e U", "p² / (2 mₑ) = e U"),
    MOMENTUM("momentum", "p gleich Quadratwurzel aus zweimal Elektronenmasse mal e mal U.", "p = √(2mₑeU)", "p = √(2 m e U)", "p = √(2 mₑ e U)"),
    DE_BROGLIE("de-broglie", "Lambda gleich h durch p: Wellenlänge und Impuls sind umgekehrt proportional.", "λ = h/p", "λ = h / p"),
    WAVELENGTH_VOLTAGE("wavelength-voltage", "Lambda nichtrelativistisch gleich h geteilt durch Quadratwurzel aus zweimal Elektronenmasse mal e mal U.", "λNR(U) = h/√(2mₑeU)", "λ = h / √(2mₑeU)", "λ = h / √(2 m e U)"),
    SCALING("scaling", "Bei vierfacher Beschleunigungsspannung halbiert sich die nichtrelativistische Wellenlänge.", "λNR(4U) = λNR(U)/2"),
    BRAGG_LEG("bragg-leg", "Die erste Zusatzstrecke ist d mal Sinus des Braggwinkels.", "δs₁ = d sin θB"),
    BRAGG_SINE("bragg-sine", "Im rechtwinkligen Dreieck A P B: Sinus Theta B gleich Länge P B durch Länge A B, gleich Länge P B durch den Ebenenabstand d."),
    GEOMETRY_POINT_A("geometry-point-a", "Punkt A"),
    GEOMETRY_POINT_P("geometry-point-p", "Punkt P"),
    GEOMETRY_SEGMENT_AB("geometry-segment-ab", "Strecke von A nach B"),
    GEOMETRY_SEGMENT_PB("geometry-segment-pb", "Strecke von P nach B"),
    BRAGG("bragg", "Zweimal d mal Sinus des Braggwinkels gleich n mal Lambda.", "2d sin θB = nλ"),
    SURFACE("surface", "Im gewählten Oberflächenmodell: D mal Sinus Phi gleich n mal Lambda.", "Gewähltes Oberflächenmodell: D sin φ = nλ", "D sin φ = nλ"),
    BRAGG_FOCUS_ANGLE("bragg-focus-angle", "Bragg-Bedingung; hervorgehoben ist der Winkel Theta B zur Netzebene."),
    BRAGG_FOCUS_SPACING("bragg-focus-spacing", "Bragg-Bedingung; hervorgehoben ist der senkrechte Netzebenenabstand d."),
    BRAGG_FOCUS_PATH("bragg-focus-path", "Bragg-Bedingung; hervorgehoben ist der gesamte geometrische Gangunterschied aus zwei Zusatzwegen."),
    SURFACE_FOCUS_ANGLE("surface-focus-angle", "Oberflächenmodell; hervorgehoben ist Phi, der Winkel zur äußeren Oberflächennormalen."),
    SURFACE_FOCUS_SPACING("surface-focus-spacing", "Oberflächenmodell; hervorgehoben ist D, der Abstand der ausgewählten Oberflächenreihen."),
    SURFACE_FOCUS_PATH("surface-focus-path", "Oberflächenmodell bei normalem Einfall; hervorgehoben ist der auslaufende Gangunterschied."),
    SURFACE_SPACING("surface-spacing", "D gleich a mal Quadratwurzel aus drei Achteln; ausgewählter Ni-111-Oberflächenreihenabstand.", "Ni(111)-Modell: D = a√(3/8)", "D = a√(3/8)"),
    FRINGE_SPACING("fringe-spacing", "Paraxialer Streifenabstand Delta y ungefähr L mal Lambda geteilt durch Spaltmittenabstand g.", "Kleine Winkel: Δy ≈ Lλ/g", "Δy ≈ Lλ/g", "Δx ≈ Lλ / g"),
    ENVELOPE("envelope", "Erste Nullstelle der Einzelspalthülle: Spaltbreite b mal Sinus Theta gleich Lambda.", "Erste Hüllnullstelle: b sin θ = λ", "b sin θ = λ"),
    RELATIVISTIC("relativistic", "Relativistische Wellenlänge: h geteilt durch die Wurzel aus zweimal Elektronenmasse mal e U mal dem Korrekturfaktor eins plus e U durch zweimal Elektronenmasse c Quadrat.", "λrel = h / √[2mₑeU (1 + eU/(2mₑc²))]"),
    ;

    val lightPath: String get() = "math/$key-light.svg"
    val darkPath: String get() = "math/$key-dark.svg"

    companion object {
        private fun normalized(text: String) = text.filterNot(Char::isWhitespace)
        fun fromText(text: String): FormulaId? = entries.firstOrNull { id -> id.aliases.any { normalized(it) == normalized(text) } }
    }
}

/** Typographic role; prose symbols must not inherit display-equation dimensions. */
enum class FormulaPresentation { EQUATION, TEXT_SYMBOL }

/**
 * Genuine transparent Typst SVGs in normal text flow. Theme variants share a
 * viewBox. Only the selected theme is requested; bootstrap prevents a theme flash.
 * no iframe, browser compiler, provider call or opaque page background.
 * Separate image documents also keep repeated SVG glyph IDs collision-free.
 */
@Composable
fun InlineFormula(id: FormulaId, presentation: FormulaPresentation = FormulaPresentation.EQUATION) {
    val dark = LocalLabTheme.current.preference == ThemePreference.DARK
    Span(attrs = {
        when (presentation) {
            FormulaPresentation.EQUATION -> styles(LabClass.INLINE_FORMULA)
            FormulaPresentation.TEXT_SYMBOL -> styles(LabClass.INLINE_FORMULA, LabClass.INLINE_FORMULA_TEXT_SYMBOL)
        }
    }) {
        Img(src = if (dark) id.darkPath else id.lightPath, alt = id.alternative, attrs = {
            styles(LabClass.INLINE_FORMULA_ASSET, if (dark) LabClass.INLINE_FORMULA_DARK else LabClass.INLINE_FORMULA_LIGHT)
        })
    }
}

/** All authored expressions use the same Typst-only text renderer; no Unicode fallback. */
@Composable
fun InlineFormula(text: String) {
    val reviewed = FormulaId.fromText(text)
    if (reviewed != null) InlineFormula(reviewed) else MathText(text)
}
