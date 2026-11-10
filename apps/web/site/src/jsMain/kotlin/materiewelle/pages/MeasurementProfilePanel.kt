package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.*
import materiewelle.core.LoadState
import materiewelle.core.MeasurementSelection
import materiewelle.core.MeasurementSession
import materiewelle.presentation.styles.*
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*


@Composable
internal fun MeasurementProfilePanel(data: JonssonData, measurement: MeasurementSession, profile: MeasurementProfile?, profiles: LoadState<List<MeasurementProfile>>, viewState: JonssonUiState) {
    var sampleX by viewState.sampleX
    val profileProbe = profile?.resolveProbe(sampleX)
    val effectiveProfilesState = profiles
    val markers = measurement.markers.map { it.xPx.toDouble() }
    val nativeBand = measurement.nativeBand(data)
    val bandLabel = "${nativeBand.topInclusive} ≤ y < ${nativeBand.bottomExclusive}"
    Div(attrs = { styles(LabClass.JONSSON_PROFILE_HEADING) }) {
        H3 { MathText("Grauwertprofil") }
    }
    when (val state = effectiveProfilesState) {
        LoadState.Loading ->
            P(
                attrs = {
                    elementId(Id.GRAY_PROFILE_STATUS)
                    role(AriaRole.STATUS)
                }
            ) {
                MathText("Profil wird geladen …")
            }
        is LoadState.Failed ->
            Div(
                attrs = {
                    elementId(Id.GRAY_PROFILE_STATUS)
                    styles(LabClass.SOURCE_WARNING)
                    role(AriaRole.STATUS)
                }
            ) {
                MathText(
                    "Profil und Referenz sind nicht verfügbar. Deine eigenen Marken bleiben erhalten."
                )
                Details {
                    Summary { MathText("Fehlerdetails") }
                    P { MathText(state.message) }
                }
            }
        is LoadState.Ready ->
            if (profile == null || profileProbe == null) {
                P(
                    attrs = {
                        elementId(Id.GRAY_PROFILE_STATUS)
                        styles(LabClass.SOURCE_WARNING)
                        role(AriaRole.STATUS)
                    }
                ) {
                    MathText(
                        "Für das Band „${measurement.band.label}“ ($bandLabel) ist kein geprüftes Profil verfügbar. Deine Marken bleiben erhalten; eine Referenz aus einem anderen Band wird nicht eingesetzt."
                    )
                }
            } else
                GrayProfile(profile, markers, profileProbe, viewState.referenceVisible(measurement)) { value ->
                    sampleX = value
                    viewState.pinnedSampleX.value = value
                }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED); elementId(Id.GRAY_PROFILE_METHOD) }) {
        MathText("Mittlere Bildhelligkeit im gewählten Band. Helle Streifen erscheinen als Gipfel; suche benachbarte Maxima.")
    }
    Details {
        Summary { MathText("Wie wird das Profil berechnet und gelesen?") }
        P {
            MathText("Gemittelt werden die unveränderten 8-Bit-Grauwerte über $bandLabel. ")
            profile?.let { MathText("Der verfügbare Bereich ist x = ${it.profileStartX}…${it.profileStartX + it.grayscale.lastIndex}. ") }
            MathText("Es wird nicht zusätzlich geglättet. Die Grauwerte sind keine kalibrierte Elektronenintensität. Die gestrichelte Profilprobe zeigt einen einzelnen Wert und verschiebt keine eigene Marke.")
        }
    }
}
