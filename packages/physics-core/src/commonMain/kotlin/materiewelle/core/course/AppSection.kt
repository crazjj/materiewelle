package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.Experiment

/** Public chapters are separate from the stable sixteen persisted work identities. */
@Serializable
enum class AppSection(val key: String, val title: String) {
    DE_BROGLIE("debroglie", "De Broglie"),
    DAVISSON_GERMER("davisson-germer", "Davisson–Germer"),
    JONSSON("joensson", "Jönsson"),
    CONCLUSION("fazit", "Fazit");

    val route: AppRoute get() = when (this) {
        DE_BROGLIE -> AppRoute.Laboratory(Experiment.DE_BROGLIE)
        DAVISSON_GERMER -> AppRoute.Laboratory(Experiment.DAVISSON_GERMER)
        JONSSON -> AppRoute.Laboratory(Experiment.JONSSON)
        CONCLUSION -> AppRoute.Course(StationId.S16_CONCLUSION)
    }

    companion object {
        fun fromKey(key: String): AppSection? = entries.firstOrNull { it.key == key }
        fun forStation(station: StationId): AppSection = when (station) {
            StationId.S01_OBSERVATION, StationId.S02_CLASSICAL, StationId.S03_LIGHT,
            StationId.S04_HYPOTHESIS, StationId.S05_DERIVATION, StationId.S06_CALCULATION,
            StationId.S07_RECEPTION -> DE_BROGLIE
            StationId.S08_DG_SETUP, StationId.S09_CRYSTAL, StationId.S10_GEOMETRY,
            StationId.S11_DG_EVALUATION -> DAVISSON_GERMER
            StationId.S12_JONSSON_SETUP, StationId.S13_INTERFERENCE, StationId.S14_MEASUREMENT,
            StationId.S15_LIMITS -> JONSSON
            StationId.S16_CONCLUSION -> CONCLUSION
        }
        fun forRoute(route: AppRoute): AppSection = when (val resolved = route.explanationRoute()) {
            is AppRoute.Course -> forStation(resolved.station)
            is AppRoute.DgLesson -> DAVISSON_GERMER
            is AppRoute.Laboratory -> when (resolved.experiment) {
                Experiment.DE_BROGLIE -> DE_BROGLIE
                Experiment.DAVISSON_GERMER -> DAVISSON_GERMER
                Experiment.JONSSON -> JONSSON
            }
            else -> DE_BROGLIE
        }
    }
}
