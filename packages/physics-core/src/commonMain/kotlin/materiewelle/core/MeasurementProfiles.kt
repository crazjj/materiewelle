package materiewelle.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

data class MeasurementProfile(
    val id: String, val label: String, val markers: List<Double>, val bandTop: Double,
    val bandBottom: Double, val markerY: Double, val profileStartX: Int = 0, val grayscale: List<Double> = emptyList()
) {
    val nativeMarkers: List<NativePixel> get() = markers.map(::NativePixel)
}

/** Derived scan profiles remain distinct from canonical source data and electron intensities. */
object MeasurementProfiles {
    fun fromJson(text: String, data: JonssonData): List<MeasurementProfile> {
        val doc = scientificJson.decodeFromString<MeasurementProfileDocument>(text)
        require(doc.schemaVersion == "1.0") { "Nicht unterstützte Profilspezifikation." }
        require(doc.kind == "derived_method_sensitivity_not_new_canonical_data")
        require(doc.sourceImageSha256 == data.imageSha256) { "Profil und Originalbild haben unterschiedliche Prüfwerte." }
        require(doc.width == data.width && doc.height == data.height) { "Profilmaße passen nicht zum nativen Bild." }
        val result = doc.profiles.map { row ->
            val markers = row.integerMarkers
            require(markers.size == 8 && markers.all { it.isFinite() && it == it.toInt().toDouble() && it >= data.cropLeft && it < data.cropLeft + data.cropWidth })
            val mean = PixelMeasurement.mean(markers)
            require(row.integerSummary.mean.isFinite() && abs(mean - row.integerSummary.mean) < 1e-10) { "Deklarierter Pixelmittelwert stimmt nicht mit den Marken überein." }
            val band = row.band
            require(band.size == 2 && band[0] >= 0 && band[0] < band[1] && band[1] <= data.height)
            require(row.bandStopExclusive)
            val method = requireNotNull(MeasurementBand.fromProfileId(row.id)) { "Unbekannte Auswerteband-ID." }
            val expectedBand = if (method == MeasurementBand.BASE) MeasurementImageIdentity.from(data).baseBand
                else NativeImageBand(method.topInclusive, method.bottomExclusive)
            require(band[0] == expectedBand.topInclusive && band[1] == expectedBand.bottomExclusive) {
                "Profil-ID und deklarierter nativer y-Bereich passen nicht zusammen."
            }
            require(row.markerY.isFinite() && row.markerY >= band[0] && row.markerY < band[1])
            require(row.profileStartX >= data.cropLeft && row.grayscale.size >= 2 && row.profileStartX + row.grayscale.size <= data.cropLeft + data.cropWidth)
            require(row.grayscale.all { it.isFinite() && it in 0.0..255.0 }) { "Grauwertprofil muss endliche Bildwerte zwischen 0 und 255 enthalten." }
            require(markers.all { it >= row.profileStartX && it < row.profileStartX + row.grayscale.size })
            require(row.label.isNotBlank())
            MeasurementProfile(row.id, row.label, markers, band[0].toDouble(), band[1].toDouble(), row.markerY, row.profileStartX, row.grayscale)
        }
        require(result.map { it.id }.toSet() == setOf("basis", "oben", "unten") && result.size == 3)
        require(result.first { it.id == "basis" }.markers == data.markers) { "Basismethode muss den dokumentierten Referenzmarken entsprechen." }
        return result
    }
}

@Serializable
private data class MeasurementProfileDocument(
    @SerialName("schema_version") val schemaVersion: String,
    val kind: String,
    @SerialName("source_image_sha256") val sourceImageSha256: String,
    @SerialName("native_width_px") val width: JsonInteger,
    @SerialName("native_height_px") val height: JsonInteger,
    val profiles: List<ProfileDocument>
)

@Serializable
private data class ProfileDocument(
    val id: String,
    @SerialName("label_de") val label: String,
    @SerialName("integer_markers_x_px") val integerMarkers: List<JsonNumber>,
    @SerialName("integer_summary") val integerSummary: ProfileSummaryDocument,
    @SerialName("band_y_px") val band: List<JsonInteger>,
    @SerialName("band_stop_exclusive") val bandStopExclusive: Boolean,
    @SerialName("marker_y_px") val markerY: JsonNumber,
    @SerialName("profile_x_start_px") val profileStartX: JsonInteger,
    @SerialName("profile_mean_grayscale") val grayscale: List<JsonNumber>
)

@Serializable
private data class ProfileSummaryDocument(@SerialName("endpoint_mean_spacing_px") val mean: JsonNumber)
