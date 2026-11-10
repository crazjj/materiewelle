package materiewelle.core.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Stable content identities; page positions come from the current Typst compilation. */
@Serializable
enum class DocumentSectionId(val key: String, val label: String, val station: StationId?) {
    @SerialName("einstieg") INTRODUCTION("einstieg", "Einstieg und klassische Bilder", StationId.S01_OBSERVATION),
    @SerialName("herleitung") DERIVATION("herleitung", "Spannung → Impuls → Wellenlänge", StationId.S05_DERIVATION),
    @SerialName("rechnung") CALCULATION("rechnung", "150 V und Transfer", StationId.S06_CALCULATION),
    @SerialName("rezeption") RECEPTION("rezeption", "Einsteins Einschätzung", StationId.S07_RECEPTION),
    @SerialName("dg-aufbau") DG_SETUP("dg-aufbau", "DG: Aufbau und Kristall", StationId.S08_DG_SETUP),
    @SerialName("dg-geometrie") DG_GEOMETRY("dg-geometrie", "DG: zwei Winkelbezüge", StationId.S10_GEOMETRY),
    @SerialName("dg-vergleich") DG_COMPARISON("dg-vergleich", "DG: quantitativer Vergleich", StationId.S11_DG_EVALUATION),
    @SerialName("joensson-aufbau") JONSSON_SETUP("joensson-aufbau", "Jönsson: Spalte und Modell", StationId.S12_JONSSON_SETUP),
    @SerialName("joensson-messung") JONSSON_MEASUREMENT("joensson-messung", "Jönsson: native Bildmessung", StationId.S14_MEASUREMENT),
    @SerialName("modellgrenzen") LIMITS("modellgrenzen", "Modell, Quelle und Kalibrierung", StationId.S15_LIMITS),
    @SerialName("fazit") CONCLUSION("fazit", "Begründetes Urteil", StationId.S16_CONCLUSION),
    @SerialName("quellen") SOURCES("quellen", "Quellen und Aussagearten", null),
    @SerialName("protokoll") PROTOCOL("protokoll", "Mess- und Eigenleistungsprotokoll", StationId.S14_MEASUREMENT);

    companion object {
        fun fromKey(key: String): DocumentSectionId? = entries.firstOrNull { it.key == key }
        fun forStation(station: StationId): DocumentSectionId = when (station) {
            StationId.S01_OBSERVATION, StationId.S02_CLASSICAL, StationId.S03_LIGHT -> INTRODUCTION
            StationId.S04_HYPOTHESIS, StationId.S05_DERIVATION -> DERIVATION
            StationId.S06_CALCULATION -> CALCULATION
            StationId.S07_RECEPTION -> RECEPTION
            StationId.S08_DG_SETUP, StationId.S09_CRYSTAL -> DG_SETUP
            StationId.S10_GEOMETRY -> DG_GEOMETRY
            StationId.S11_DG_EVALUATION -> DG_COMPARISON
            StationId.S12_JONSSON_SETUP, StationId.S13_INTERFERENCE -> JONSSON_SETUP
            StationId.S14_MEASUREMENT -> JONSSON_MEASUREMENT
            StationId.S15_LIMITS -> LIMITS
            StationId.S16_CONCLUSION -> CONCLUSION
        }
    }
}

@Serializable
data class DocumentSection(val id: DocumentSectionId, val title: String, val pageIndex: Int) {
    fun validate(pageCount: Int): DocumentSection {
        require(title.isNotBlank() && title.length <= 180)
        require(pageIndex in 0 until pageCount)
        return this
    }
}
