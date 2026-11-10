package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.presentation.styles.*
import materiewelle.ui.PlainTextCopy
import materiewelle.ui.PlainTextCopyIds
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


@Composable
internal fun MeasurementReport(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?) {
    val markers = measurement.markers.map { it.xPx.toDouble() }
    val ready = measurement.selection as? MeasurementSelection.Ready
    var includeTechnicalAppendix by remember { mutableStateOf(false) }
    val reference = if (measurement.referenceViewed) profile?.markers.orEmpty() else emptyList()
    Section(attrs = { styles(LabClass.MEASUREMENT_RESULT, LabClass.LAB_PANEL) }) {
        Ol(
            attrs = {
                styles(LabClass.INTERVAL_LIST)
                elementId(Id.JONSSON_INTERVALS)
                ariaLabel("Einzelabstände in Originalpixeln")
            }
        ) {
            ready?.intervalsPx?.forEachIndexed { i, value ->
                Li(
                    attrs = {
                        data(LabData.INTERVAL, i)
                        data(LabData.VALUE_PX, value.toDouble())
                    }
                ) {
                    Span { MathText("${i + 1}–${i + 2}") }
                    Strong() { MathText("$value px") }
                }
            }
        }
        if (ready != null)
            Details(
                attrs = {
                    elementId(MeasurementElement.PRECISION)
                    measurementStyles(MeasurementClass.PRECISION)
                }
            ) {
                Summary() { MathText("Rechenweg und Rundung") }
                P(
                    attrs = {
                        styles(LabClass.SMALL, LabClass.MUTED)
                        elementId(Id.JONSSON_MEAN_EQUATION)
                    }
                ) {
                    MathText(
                        "(${markers.last().toInt()} − ${markers.first().toInt()}) / ${ready.intervalsPx.size} ≈ ${number(ready.meanPx, 3)} Originalpixel. Dieser Rechenwert und die kurze Hauptanzeige sind gerundet. Nachkommastellen belegen keine Messgenauigkeit."
                    )
                }
                measurement.comparison?.let { saved ->
                    P {
                        MathText(
                            when {
                                ready.meanPx > saved.meanPx -> "Der aktuelle mittlere Abstand liegt rund ${number(ready.meanPx - saved.meanPx, 3)} Originalpixel über dem gespeicherten Vergleich A."
                                ready.meanPx < saved.meanPx -> "Der aktuelle mittlere Abstand liegt rund ${number(saved.meanPx - ready.meanPx, 3)} Originalpixel unter dem gespeicherten Vergleich A."
                                else -> "Beide mittleren Abstände sind gleich. Die Wahl der Maxima ist damit noch nicht bestätigt."
                            }
                        )
                    }
                }
                if (measurement.referenceViewed && reference.size >= 2)
                    P {
                        MathText(
                            "Bandreferenz: rund ${number(PixelMeasurement.mean(reference), 3)} Originalpixel; eigene Auswahl minus Referenz: rund ${number(ready.meanPx - PixelMeasurement.mean(reference), 3)} Originalpixel."
                        )
                    }
            }
        else
            P(
                attrs = {
                    styles(LabClass.SMALL, LabClass.MUTED)
                    elementId(Id.JONSSON_MEAN_EQUATION)
                }
            ) {
                MathText(
                    "Setze mindestens zwei Marken, um einen Abstand zu berechnen."
                )
            }
        P(attrs = { styles(LabClass.MEASUREMENT_INSIGHT) }) {
            MathText(
                "Verschiebst du eine innere Marke, wird ein Abstand größer und der andere kleiner. Der Mittelwert bleibt gleich, solange Endpunkte und Anzahl der Marken gleich bleiben. Prüfe deshalb, ob jede Marke auf einem aufeinanderfolgenden hellen Maximum liegt. ±1 Pixel ist eine gewählte Variation, keine belegte historische Fehlergrenze."
            )
        }
        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(0.5.cssRem); minHeight(44.px) } }) {
            Input(InputType.Checkbox, attrs = {
                checked(includeTechnicalAppendix)
                onChange { includeTechnicalAppendix = it.value }
            })
            MathText("Technischen Anhang in das Messprotokoll aufnehmen")
        }
        P(attrs = { styles(LabClass.SMALL) }) {
            MathText("Der freiwillige Anhang enthält Dateikennungen, ungerundete Rechenwerte und vermerkte Referenzaufrufe.")
        }
        PlainTextCopy(
            text = measurement.protocol(data).readable(includeTechnicalAppendix),
            ids = PlainTextCopyIds(
                MeasurementElement.PROTOCOL_COPY,
                MeasurementElement.PROTOCOL_STATUS,
                MeasurementElement.PROTOCOL_PREVIEW,
                MeasurementElement.PROTOCOL_SELECT,
            ),
            label = "Messprotokoll kopieren",
            previewLabel = "Messprotokoll lesen",
            successMessage = "Messprotokoll kopiert.",
        )
    }
}
