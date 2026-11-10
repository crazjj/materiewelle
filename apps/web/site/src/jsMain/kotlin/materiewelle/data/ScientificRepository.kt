package materiewelle.data

import materiewelle.ScientificData
import materiewelle.core.LoadState

/** Import boundary: HTTP and decode errors never enter the calculation state. */
object ScientificRepository {
    fun load(deliver: (LoadState<ScientificData>) -> Unit) {
        loadAsset(LabAsset.SCIENTIFIC_DATA, { text -> ScientificData.fromJson(text) },
            { reason -> "${reason.description()} Bitte erneut versuchen." }, deliver)
    }
}
