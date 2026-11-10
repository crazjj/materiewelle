package materiewelle.data

import materiewelle.JonssonData
import materiewelle.MeasurementProfile
import materiewelle.MeasurementProfiles
import materiewelle.core.LoadState

/** HTTP is confined to this adapter; validation and image-derived values belong to the shared core. */
object MeasurementProfileRepository {
    fun load(data: JonssonData, deliver: (LoadState<List<MeasurementProfile>>) -> Unit) {
        loadAsset(LabAsset.MEASUREMENT_PROFILES, { text -> MeasurementProfiles.fromJson(text, data) },
            { reason -> "${reason.description()} Eigene Pixelmessung bleibt möglich; eine passende Profilreferenz wird erst nach erfolgreicher Prüfung gezeigt." }, deliver)
    }
}
