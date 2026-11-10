package materiewelle.core.course

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Optional navigation metadata, independent from persisted scientific work and proficiency. */
@Serializable
data class ReadingPosition(
    val schemaVersion: Int = 1,
    val section: AppSection,
    val station: StationId? = null,
    val dgStep: DgLessonStep? = null,
) {
    fun validate(): ReadingPosition {
        require(schemaVersion == 1)
        require(station == null || AppSection.forStation(station) == section)
        require(dgStep == null || section == AppSection.DAVISSON_GERMER && station == null)
        return this
    }
    val route: AppRoute get() = dgStep?.route ?: station?.let { AppRoute.Course(it) } ?: section.route
    fun json(): String = Json.encodeToString(validate())
    companion object {
        fun parse(raw: String): ReadingPosition = Json.decodeFromString<ReadingPosition>(raw).validate()
        fun fromRoute(route: AppRoute): ReadingPosition? {
            if (route == AppRoute.Tutor || route == AppRoute.Overview) return null
            val resolved = route.explanationRoute()
            val station = when (resolved) {
                is AppRoute.Course -> resolved.station
                is AppRoute.Laboratory -> if (resolved.focus == LabFocus.MEASUREMENT) StationId.S14_MEASUREMENT else null
                else -> null
            }
            return ReadingPosition(section = AppSection.forRoute(resolved), station = station,
                dgStep = (resolved as? AppRoute.DgLesson)?.step)
        }
    }
}

/** Authored public anchors point to existing work identities; no answer-field migration. */
enum class ChapterAnchor(val path: String, val station: StationId) {
    DG_SETUP("davisson-germer/aufbau", StationId.S08_DG_SETUP),
    DG_OBSERVATION("davisson-germer/beobachtung", StationId.S09_CRYSTAL),
    DG_BRAGG("davisson-germer/bragg", StationId.S10_GEOMETRY),
    DG_COMPARISON("davisson-germer/auswertung", StationId.S11_DG_EVALUATION),
    JONSSON_SETUP("joensson/aufbau", StationId.S12_JONSSON_SETUP),
    JONSSON_MODEL("joensson/modell", StationId.S13_INTERFERENCE),
    JONSSON_LIMITS("joensson/grenzen", StationId.S15_LIMITS);
    companion object { fun fromPath(path: String): ChapterAnchor? = entries.firstOrNull { it.path == path } }
}
