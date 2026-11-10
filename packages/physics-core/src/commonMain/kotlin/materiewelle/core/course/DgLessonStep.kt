package materiewelle.core.course

import kotlinx.serialization.Serializable

/** Reading order is independent of scientific work and never represents proficiency. */
@Serializable
enum class DgLessonStep(val key: String, val label: String, val question: String) {
    SETUP("versuch", "Aufbau", "Wie lässt sich die Vorhersage prüfen?"),
    OBSERVATION("befund", "Befund", "Was wurde beobachtet?"),
    BRAGG("interferenz", "Bragg", "Warum ist das Signal in bestimmten Richtungen besonders stark?"),
    GEOMETRY("geometrie", "Geometrie", "Welche Geometrie gehört zum Zahlenfall?"),
    CALCULATION("rechnen", "Rechnung", "Wie nah liegen die beiden Wellenlängen beieinander?"),
    CONCLUSION("einordnen", "Einordnung", "Was lässt sich aus dem Vergleich schließen?"),
    ;

    val previous get() = entries.getOrNull(ordinal - 1)
    val next get() = entries.getOrNull(ordinal + 1)
    val route get() = AppRoute.DgLesson(this)

    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key }
        fun forRoute(route: AppRoute): DgLessonStep? = when (val resolved = route.explanationRoute()) {
            is AppRoute.DgLesson -> resolved.step
            is AppRoute.Course -> when (resolved.station) {
                StationId.S08_DG_SETUP -> SETUP
                StationId.S09_CRYSTAL -> OBSERVATION
                StationId.S10_GEOMETRY -> BRAGG
                StationId.S11_DG_EVALUATION -> CALCULATION
                else -> null
            }
            else -> null
        }
    }
}
