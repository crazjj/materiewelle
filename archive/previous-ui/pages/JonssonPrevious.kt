package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.FiniteSlitModel
import materiewelle.core.Length
import materiewelle.core.LoadState
import materiewelle.core.MeasurementBand
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.core.Voltage
import materiewelle.core.WavelengthApproximation
import materiewelle.data.LabAsset
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.*

import materiewelle.state.MeasurementOwner

/** Controlled native measurement: one scientific owner, explicit view state, local tools. */
@Composable
private fun JonssonGuided(data: JonssonData, step: Int, session: MeasurementSession) {
    P(
        attrs = {
            styles(LabClass.GUIDED_PROGRESS)
            elementId(Id.JONSSON_STEP)
            ariaLive(LiveMode.POLITE)
        }
    ) {
        MathText("Schritt ${step + 1} von 4")
    }
    when (step) {
        0 -> {
            H2 { MathText("1 · Beobachten") }
            P {
                MathText(
                    "Wiederkehrende helle und dunkle Streifen sind Bildbefund. Spannung und Wellenlänge stehen nicht im Raster. Wähle vor dem Referenzvergleich selbst ein Band und aufeinanderfolgende Maxima."
                )
            }
        }
        1 -> {
            H2 { MathText("2 · Eigene Messung begründen") }
            val ready = session.selection as? MeasurementSelection.Ready
            P {
                MathText(
                    ready?.let {
                        "Deine ${session.markers.size} Marken ergeben ${it.intervalsPx.size} Intervalle und ${number(it.meanPx, 4)} native Pixel im Mittel. Sichere und begründe diese Auswahl, bevor du die Referenz betrachtest."
                    }
                        ?: "Deine Messung beginnt leer. Ohne mindestens zwei eigene Marken gibt es keinen Abstand. Lege die Auswahl unten im Bild oder über Zahlenfelder selbst fest."
                )
            }
            Formula("Mittel = (xₙ − x₁) / (n − 1)")
            P { MathText("Zoom verändert ausschließlich die Anzeige, niemals die native Messgröße.") }
        }
        2 -> {
            H2 { MathText("3 · Modellrechnung trennen") }
            P {
                MathText(
                    "Der textbasierte 50-kV-Fall unten berechnet eine Vorhersage. Die µm aus diesem Modell sind nicht aus deinen Bildpixeln gemessen."
                )
            }
        }
        else -> {
            H2 { MathText("4 · Unbekannt und begrenzt") }
            P {
                MathText(
                    "Keine unabhängige Fotokalibrierung: Eine vorhergesagte µm-Länge durch den Pixelabstand zu teilen, definiert nur einen Modellmaßstab und bestätigt λ = h/p nicht unabhängig."
                )
            }
            P {
                MathText(
                    "Für eine absolute Prüfung fehlen der externe Maßstab genau dieser Aufnahme und deren eindeutig zugeordnete Versuchsparameter. Eine Ablesevariation oder ein anderes y-Band ist keine bekannte historische Messunsicherheit."
                )
            }
        }
    }
}

@Composable
fun JonssonParameters(data: JonssonData, viewState: JonssonUiState = remember { JonssonUiState() }) {
    val lambda = Physics.relativisticElectronWavelength(data.voltage)
    val lambdaNr = Physics.electronWavelength(data.voltage)
    val fringe = Physics.fringeSpacing(data.distance, lambda, data.slitSpacing)
    H2 { MathText("Gesonderte Parameterrechnung") }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Belegter Parameterfall: U = ${number(data.voltageV / 1000, 0)} kV · g = ${number(data.slitSpacingM * 1e6, 1)} µm · L = ${number(data.distanceM * 1000, 0)} mm. Die genaue Zuordnung zu dieser Aufnahme ist offen."
        )
    }
    Div(attrs = { styles(LabClass.JONSSON_MODEL_RESULTS) }) {
        Div {
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                MathText("Relativistische Wellenlänge")
            }
            Div(
                attrs = {
                    styles(LabClass.PARAMETER_VALUE)
                    elementId(Id.JONSSON_LAMBDA)
                }
            ) {
                MathText("λ ≈ ${number(lambda.picometers, 4)} pm")
            }
        }
        Div {
            P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { MathText("Formaler Modellmaßstab") }
            Div(
                attrs = {
                    styles(LabClass.PARAMETER_VALUE)
                    elementId(Id.JONSSON_PREDICTION)
                }
            ) {
                MathText("Δx ≈ ${number(fringe.micrometers, 4)} µm")
            }
        }
        Formula("Δx ≈ Lλ / g")
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Nichtrelativistisch: ${number(lambdaNr.picometers, 4)} pm; Überschätzung ${number(Physics.relativeDifferencePercent(lambdaNr.meters, lambda.meters), 2)} % bezogen auf λrel. Der Streifenmaßstab setzt gesondert eine ideale Fourierabbildung mit Skalenlänge L voraus. L = 350 mm begründet hier keine freie Fernfeldausbreitung und ist keine endgültige Fotoschirmdistanz. Modellwerte, keine aus dem Foto gewonnenen Längen. Text: 50 kV; Skizze Abb. 6: 40 kV; einzelne Aufnahme nicht eindeutig zugeordnet."
        )
    }
    JonssonFiniteSlitModel(data, viewState)
}

/**
 * Computed ideal model, using SI coordinates; deliberately separate from photographic grayscale.
 */
@Composable
fun JonssonFiniteSlitModel(data: JonssonData, viewState: JonssonUiState = remember { JonssonUiState() }) {
    var voltageV by viewState.modelVoltage
    var widthUm by viewState.slitWidthUm
    var spacingUm by viewState.slitSpacingUm
    val model =
        FiniteSlitModel(
            Voltage(voltageV),
            Length(10.0),
            Length(spacingUm * 1e-6),
            Length(widthUm * 1e-6),
            WavelengthApproximation.NONRELATIVISTIC,
        )
    val extentM = 3.0e-3
    val left = 64.0
    val plotWidth = 660.0
    val top = 22.0
    val height = 180.0
    val path =
        (0..800).joinToString(" ") { index ->
            val positionM = -extentM + 2 * extentM * index / 800
            "${if (index == 0) "M" else "L"}${left + plotWidth * index / 800},${top + height * (1 - model.relativeIntensity(positionM))}"
        }
    val envelopePath =
        (0..800).joinToString(" ") { index ->
            val positionM = -extentM + 2 * extentM * index / 800
            "${if (index == 0) "M" else "L"}${left + plotWidth * index / 800},${top + height * (1 - model.relativeEnvelope(positionM))}"
        }
    H3 { MathText("Synthetischer Fernfeld-Lehrfall") }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Frei gewählter Lehrfall mit L = 10 m und zunächst U = 150 V; λ wird hier nichtrelativistisch wie in der Herleitung berechnet. Keine Rekonstruktion von Jönssons Elektronenoptik. Identische kohärent beleuchtete Rechteckspalte, monochromatisch, ideale Fraunhofer-Näherung. Dieser Lehrfall ist vom nominalen 50-kV-Parameterfall und vom historischen Foto getrennt."
        )
    }
    Div(attrs = { styles(LabClass.LEARNING_CARDS) }) {
        Label {
            MathText("Spannung U: ${number(voltageV, 0)} V")
            Input(
                InputType.Range,
                attrs = {
                    minimum(100)
                    maximum(600)
                    increment(10)
                    value(voltageV)
                    ariaLabel("Lehrfallspannung U in V")
                    onInput { it.value?.toDouble()?.let { voltageV = it } }
                },
            )
        }
        Label {
            MathText("Spaltbreite b: ${number(widthUm, 2)} µm")
            Input(
                InputType.Range,
                attrs = {
                    minimum(0.25)
                    maximum(1.0)
                    increment(0.05)
                    value(widthUm)
                    ariaLabel("Modellspaltbreite b in µm")
                    onInput { it.value?.toDouble()?.let { widthUm = it } }
                },
            )
        }
        Label {
            MathText("Mittenabstand g: ${number(spacingUm, 1)} µm")
            Input(
                InputType.Range,
                attrs = {
                    minimum(1)
                    maximum(4)
                    increment(0.1)
                    value(spacingUm)
                    ariaLabel("Modellmittenabstand g in µm")
                    onInput { it.value?.toDouble()?.let { spacingUm = it } }
                },
            )
        }
    }
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            viewBox(0, 0, 780, 260)
            role(AriaRole.IMAGE)
            ariaLabel(
                "Berechnete relative Intensität idealer endlicher Doppelspalte im synthetischen Fernfeld-Lehrfall von minus drei bis plus drei Millimeter, nicht fotografischer Grauwert"
            )
            style {
                width(100.percent)
                height(auto)
                display(DisplayStyle.Block)
            }
        },
    ) {
        listOf(0.0, 0.5, 1.0).forEach { level ->
            SvgTag(
                SvgTagName.LINE,
                attrs = {
                    svgNumber(SvgNumber.X1, left)
                    svgNumber(SvgNumber.X2, left + plotWidth)
                    svgNumber(SvgNumber.Y1, top + height * (1 - level))
                    svgNumber(SvgNumber.Y2, top + height * (1 - level))
                    styles(LabClass.PROFILE_GRID)
                },
            )
            TypesetSvgLabel(
                namespace = SVG_NS,
                x = 52,
                y = top + height * (1 - level) + 4,
                attrs = {
                    textAnchor(TextAnchor.END)
                    styles(LabClass.PROFILE_LABEL)
                },
                text = number(level, 1),
            )
        }
        listOf(-3, -2, -1, 0, 1, 2, 3).forEach { position ->
            TypesetSvgLabel(
                namespace = SVG_NS,
                x = left + plotWidth * (position + 3) / 6,
                y = 230,
                attrs = {
                    textAnchor(TextAnchor.MIDDLE)
                    styles(LabClass.PROFILE_LABEL)
                },
                text = "$position",
            )
        }
        SvgTag(
            SvgTagName.PATH,
            attrs = {
                svgPath(envelopePath)
                styles(LabClass.PROFILE_REFERENCE)
                style { setFill(FillValue.NONE) }
            },
        )
        SvgTag(
            SvgTagName.PATH,
            attrs = {
                svgPath(path)
                styles(LabClass.PROFILE_CURVE)
            },
        )
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Durchgezogen: ideales Zweispaltprofil. Gestrichelt: berechnete Einspalthülle. Horizontale: x / mm in der 10-m-Lehrebene. Vertikale: I / I(0), keine Elektronenzählrate und keine Bildgrauwerte. Paraxialer Streifenabstand: ${number(model.paraxialFringeSpacing.micrometers / 1000, 4)} mm."
        )
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        MathText(
            "Fernfeldkontrolle mit voller Öffnungsbreite g + b: F = (g + b)² / (λL) = ${number(model.fullApertureFresnelNumber, 4)}. Hier ist F klein; verlangt wird F ≪ 1, keine scharfe universelle Grenzzahl. Diese Kontrolle prüft weder Kohärenz noch historische Elektronenoptik oder Fotokalibrierung."
        )
    }
    Formula("I / I(0) = (sin α / α)² · cos² β,   α = π b sin θ / λ,   β = π g sin θ / λ")
    P {
        MathText(
            "Verdoppeltes g halbiert näherungsweise den Streifenabstand. Größeres b verengt die Beugungshülle. Größeres U verkleinert λ und damit beide charakteristischen Abstände. Der Kurvenmittelpunkt wird mit sin α / α → 1 berechnet. Die Gleichung beschreibt hier ein ideales Beugungsmodell; eine Wellenfunktionstheorie wird nicht eingeführt."
        )
    }
}
