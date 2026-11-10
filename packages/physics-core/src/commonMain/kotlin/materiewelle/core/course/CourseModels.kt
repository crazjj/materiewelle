package materiewelle.core.course

import kotlinx.serialization.Serializable
import materiewelle.core.Experiment

@Serializable
enum class ArgumentPhase(val label: String) { IDEA("Idee"), PREDICTION("Vorhersage"), EXPERIMENT("Experiment"), JUDGEMENT("Urteil") }

/** Stable semantic identity; order is presentation metadata, never a storage key. */
@Serializable
enum class StationId(val key: String, val number: Int, val phase: ArgumentPhase) {
    S01_OBSERVATION("ausgangsfrage", 1, ArgumentPhase.IDEA),
    S02_CLASSICAL("klassische-bilder", 2, ArgumentPhase.IDEA),
    S03_LIGHT("lichtquanten", 3, ArgumentPhase.IDEA),
    S04_HYPOTHESIS("de-broglie", 4, ArgumentPhase.IDEA),
    S05_DERIVATION("spannung-zur-vorhersage", 5, ArgumentPhase.PREDICTION),
    S06_CALCULATION("150-volt", 6, ArgumentPhase.PREDICTION),
    S07_RECEPTION("einstein-1924", 7, ArgumentPhase.IDEA),
    S08_DG_SETUP("dg-aufbau", 8, ArgumentPhase.EXPERIMENT),
    S09_CRYSTAL("nickel-kristall", 9, ArgumentPhase.EXPERIMENT),
    S10_GEOMETRY("beugungsgeometrie", 10, ArgumentPhase.EXPERIMENT),
    S11_DG_EVALUATION("dg-auswertung", 11, ArgumentPhase.EXPERIMENT),
    S12_JONSSON_SETUP("joensson-aufbau", 12, ArgumentPhase.EXPERIMENT),
    S13_INTERFERENCE("lichtvergleich", 13, ArgumentPhase.EXPERIMENT),
    S14_MEASUREMENT("bildmessung", 14, ArgumentPhase.EXPERIMENT),
    S15_LIMITS("modellgrenzen", 15, ArgumentPhase.JUDGEMENT),
    S16_CONCLUSION("fazit", 16, ArgumentPhase.JUDGEMENT);

    val previous: StationId? get() = entries.getOrNull(ordinal - 1)
    val next: StationId? get() = entries.getOrNull(ordinal + 1)
    val usesLearningPhases: Boolean get() = this == S05_DERIVATION || this == S14_MEASUREMENT
    val experiment: Experiment? get() = when (this) {
        S04_HYPOTHESIS, S05_DERIVATION, S06_CALCULATION -> Experiment.DE_BROGLIE
        S08_DG_SETUP, S09_CRYSTAL, S10_GEOMETRY, S11_DG_EVALUATION -> Experiment.DAVISSON_GERMER
        S12_JONSSON_SETUP, S13_INTERFERENCE, S14_MEASUREMENT, S15_LIMITS -> Experiment.JONSSON
        else -> null
    }
    companion object { fun fromKey(key: String): StationId? = entries.firstOrNull { it.key == key } }
}

enum class LabFocus { OVERVIEW, MEASUREMENT }

/** All parsing of public hash links terminates here. Old laboratory links stay valid. */
sealed interface AppRoute {
    data object Overview : AppRoute
    data class Course(val station: StationId) : AppRoute
    data class DgLesson(val step: DgLessonStep) : AppRoute
    data class Laboratory(val experiment: Experiment, val focus: LabFocus = LabFocus.OVERVIEW) : AppRoute {
        init { require(focus != LabFocus.MEASUREMENT || experiment == Experiment.JONSSON) }
    }
    data object Workshop : AppRoute
    data object Document : AppRoute
    data class DocumentAt(val section: DocumentSectionId) : AppRoute
    data object Tutor : AppRoute
    val hash: String get() = when (this) {
        Overview -> "#lernen"
        is Course -> "#lernen/${station.key}"
        is DgLesson -> "#davisson-germer/${step.key}"
        is Laboratory -> "#${experiment.route}" + if (focus == LabFocus.MEASUREMENT) "/messung" else ""
        Workshop -> "#werkstatt"
        Document -> "#ausarbeitung"
        is DocumentAt -> "#ausarbeitung/${section.key}"
        Tutor -> "#tutor"
    }
    companion object {
        fun parse(hash: String): AppRoute? {
            val key = hash.removePrefix("#").trim('/')
            if (key.isEmpty() || key == "lernen") return Overview
            if (key == "werkstatt") return Workshop
            if (key == "ausarbeitung") return Document
            if (key.startsWith("ausarbeitung/")) return DocumentSectionId.fromKey(key.removePrefix("ausarbeitung/"))?.let(::DocumentAt)
            if (key == "tutor") return Tutor
            if (key == "fazit") return Course(StationId.S16_CONCLUSION)
            if (key == "quellen") return DocumentAt(DocumentSectionId.SOURCES)
            ChapterAnchor.fromPath(key)?.let { return Course(it.station) }
            if (key.startsWith("davisson-germer/")) return DgLessonStep.fromKey(key.removePrefix("davisson-germer/"))?.let(::DgLesson)
            if (key.startsWith("debroglie/")) return DeBroglieSection.fromKey(key.removePrefix("debroglie/"))?.let { Course(it.station) }
            if (key == "joensson/messung") return Laboratory(Experiment.JONSSON, LabFocus.MEASUREMENT)
            if (key.startsWith("lernen/")) return StationId.fromKey(key.removePrefix("lernen/"))?.let(::Course)
            return Experiment.fromRoute(key)?.let(::Laboratory)
        }
    }
}

/** Stable article anchors reuse the existing work fields instead of migrating stored answers. */
enum class DeBroglieSection(val key: String, val station: StationId) {
    INTRODUCTION("einstieg", StationId.S01_OBSERVATION),
    HISTORY("geschichte", StationId.S03_LIGHT),
    HYPOTHESIS("hypothese", StationId.S04_HYPOTHESIS),
    DERIVATION("herleitung", StationId.S05_DERIVATION),
    CALCULATION("rechnung", StationId.S06_CALCULATION),
    RECEPTION("rezeption", StationId.S07_RECEPTION);
    companion object {
        fun fromKey(key: String) = entries.firstOrNull { it.key == key }
    }
}

/** Legacy document links now resolve to their explanation, without loading internal documents. */
fun AppRoute.explanationRoute(): AppRoute = when (this) {
    AppRoute.Document, AppRoute.Workshop -> AppRoute.Overview
    is AppRoute.DocumentAt -> AppRoute.Course(section.station ?: StationId.S16_CONCLUSION)
    else -> this
}
