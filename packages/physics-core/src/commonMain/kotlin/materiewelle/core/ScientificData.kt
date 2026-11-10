package materiewelle.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Forward-compatible metadata, strict declared fields and no dynamic JavaScript objects. */
internal val scientificJson = Json {
    ignoreUnknownKeys = true
    isLenient = false
    allowSpecialFloatingPointValues = false
    coerceInputValues = false
}

enum class MaterialRole {
    SECONDARY_REPORT, INDEPENDENT_STRUCTURE_ASSUMPTION, MODEL_CALCULATION,
    DOCUMENTED_REFERENCE_MARKS, OWN_MEASUREMENT
}

enum class EvidenceReview { CHECKED_IN_SCOPE, NOT_CHECKED }

data class ScientificEvidence(
    val role: MaterialRole, val review: EvidenceReview, val sourceId: String,
    val location: String, val scope: String
)

data class Nickel111Structure(val latticeConstant: Length, val evidence: ScientificEvidence) {
    val rowSpacing: Length get() = Physics.nickel111RowSpacing(latticeConstant)
    val neighborDistance: Length get() = Physics.nickel111NearestNeighbor(latticeConstant)
    val planeSpacing: Length get() = Physics.nickel111PlaneSpacing(latticeConstant)
}

/** Editable model settings have no historical ID or source claim. */
data class DgSetting(val voltage: Voltage, val surfaceAngle: SurfaceNormalAngle) {
    val voltageV: Double get() = voltage.volts
    val angleDegrees: Double get() = surfaceAngle.degrees
}

data class DgPoint(val id: String, val voltageV: Double, val angleDegrees: Double, val approximateAngle: Boolean = true) {
    val voltage: Voltage get() = Voltage(voltageV)
    val angle: Angle get() = Angle(angleDegrees)
    val surfaceAngle: SurfaceNormalAngle get() = SurfaceNormalAngle(angleDegrees)
    val setting: DgSetting get() = DgSetting(voltage, surfaceAngle)
}

@Serializable
enum class SourceMaterialKind {
    @SerialName("provided_assignment") PROVIDED_ASSIGNMENT,
    @SerialName("provided_project_plan") EXCLUDED_PROJECT_PLAN,
    @SerialName("didactic_secondary_analysis") DIDACTIC_SECONDARY_ANALYSIS,
    @SerialName("abridged_english_translation_of_historical_report") ABRIDGED_HISTORICAL_TRANSLATION
}

@Serializable
data class SourceReference(val status: String, val url: String? = null, val path: String? = null, val kind: SourceMaterialKind? = null)

data class ScientificData(
    val points: List<DgPoint>, val spacingM: Double, val dgWarning: String, val verified: Boolean,
    val jonsson: JonssonData?, val verificationNotice: String = "", val sources: Map<String, SourceReference> = emptyMap(),
    val dgEvidence: ScientificEvidence, val dgStructure: Nickel111Structure
) {
    val spacing: Length get() = Length(spacingM)

    companion object {
        fun fromJson(text: String): ScientificData {
            val header = scientificJson.decodeFromString<ScientificHeader>(text)
            require(header.schemaVersion == "2.0") { "Nicht unterstützte Datenspezifikation: erwartet 2.0. Alte Planreihen sind keine aktive Belegbasis." }
            val doc = scientificJson.decodeFromString<ScientificDocument>(text)
            val dg = doc.davissonGermer
            require(dg.geometry.model == SurfaceModel.NI111_NORMAL_INCIDENCE && dg.geometry.angleReference == AngleReference.OUTWARD_SURFACE_NORMAL)
            require(dg.geometry.diffractionOrder == 1.0) { "Diese Darstellung gilt ausdrücklich für erste Ordnung." }
            fun EvidenceDocument.checked(expectedRole: MaterialRoleDocument): ScientificEvidence {
                require(role == expectedRole && review == EvidenceReviewDocument.CHECKED_IN_SCOPE) { "Ungeeignete oder ungeprüfte aktive Evidenzrolle." }
                require(sourceId.isNotBlank() && sourceId in doc.sources && location.isNotBlank() && scope.isNotBlank()) { "Evidenz benötigt bekannte Quelle, Fundstelle und Prüfbereich." }
                require(doc.sources.getValue(sourceId).kind == SourceMaterialKind.DIDACTIC_SECONDARY_ANALYSIS) { "Die aktive DG-Belegbasis muss eine benannte didaktische Sekundäranalyse sein; ein Projektplan ist ausgeschlossen." }
                return ScientificEvidence(when (role) {
                    MaterialRoleDocument.SECONDARY_REPORT -> MaterialRole.SECONDARY_REPORT
                    MaterialRoleDocument.INDEPENDENT_STRUCTURE -> MaterialRole.INDEPENDENT_STRUCTURE_ASSUMPTION
                }, EvidenceReview.CHECKED_IN_SCOPE, sourceId, location, scope)
            }
            val evidence = dg.evidence.checked(MaterialRoleDocument.SECONDARY_REPORT)
            val structure = Nickel111Structure(Length(dg.geometry.latticeConstantM), dg.geometry.structureEvidence.checked(MaterialRoleDocument.INDEPENDENT_STRUCTURE))
            val spacing = structure.rowSpacing
            require(!dg.originalVerified) { "D2 ist eine Sekundäranalyse und darf nicht als Originalprüfung deklariert werden." }
            val points = dg.rows.map { row ->
                require(row.id.isNotBlank()) { "DG-ID fehlt." }
                val voltage = Voltage(row.voltageV)
                require(row.kind == MaterialRoleDocument.SECONDARY_REPORT && row.kineticEnergyEv == voltage.volts) { "Elektronenfall aus Ruhe verlangt E_kin/eV = U/V und den dokumentierten Sekundärstatus." }
                require(row.approximateAngle) { "Diese Belegbasis liefert nur einen groben Winkel, keine zusätzliche historische Präzision." }
                require(row.phiDegrees.isFinite() && row.phiDegrees > 0.0 && row.phiDegrees <= 90.0) { "DG-Winkel muss größer als 0° und höchstens 90° sein." }
                DgPoint(row.id, voltage.volts, row.phiDegrees, row.approximateAngle)
            }
            require(points.isNotEmpty() && points.map { it.id }.distinct().size == points.size) { "DG-Datensatz leer oder IDs nicht eindeutig." }
            require(spacing.nanometers.isFinite()) { "D liegt außerhalb des endlichen nm-Darstellungsbereichs." }
            points.forEach { point ->
                val predicted = Physics.electronWavelength(point.voltage)
                val observed = Physics.surfaceDiffraction(spacing, point.surfaceAngle)
                require(predicted.picometers.isFinite() && observed.picometers.isFinite()) { "DG-Rechnung liegt außerhalb des endlichen Darstellungsbereichs." }
                Physics.relativeDifferencePercent(observed.meters, predicted.meters)
            }
            val measurement = doc.jonsson.image.takeIf { it.available }?.let { image ->
                val filename = requireNotNull(image.file).substringAfterLast('/')
                require(Regex("^[a-zA-Z0-9_.-]+$").matches(filename) && filename !in setOf(".", "..")) { "Ungültiger Bilddateiname." }
                val width = requireNotNull(image.width)
                val height = requireNotNull(image.height)
                require(width > 0 && height > 0) { "Bildmaße müssen positive ganze Zahlen sein." }
                val markers = requireNotNull(image.markers)
                require(markers.size >= 2 && markers.all { it.isFinite() && it >= 0 && it < width })
                PixelMeasurement.intervals(markers)
                val markerY = requireNotNull(image.markerY)
                require(markerY.isFinite() && markerY >= 0 && markerY < height)
                val crop = requireNotNull(image.crop)
                val band = requireNotNull(image.band)
                require(crop.size == 4 && band.size == 2 && crop.all { it.isFinite() } && band.all { it.isFinite() })
                require(crop[0] >= 0 && crop[2] <= width && crop[0] < crop[2] && crop[1] >= 0 && crop[3] <= height && crop[1] < crop[3])
                require(band[0] >= crop[1] && band[1] <= crop[3] && band[0] < band[1] && markerY >= band[0] && markerY < band[1])
                require(markers.all { it >= crop[0] && it < crop[2] })
                val model = requireNotNull(doc.jonsson.parameterModel)
                val voltage = Voltage(model.voltageV)
                val slit = Length(model.slitCenterSpacingM)
                val distance = Length(model.observationDistanceM)
                require(slit.meters > 0 && distance.meters > 0)
                val wavelength = Physics.relativisticElectronWavelength(voltage)
                val nonrelativisticWavelength = Physics.electronWavelength(voltage)
                val fringe = Physics.fringeSpacing(distance, wavelength, slit)
                require(wavelength.picometers.isFinite() && nonrelativisticWavelength.picometers.isFinite() && fringe.micrometers.isFinite() && slit.micrometers.isFinite() && (distance.meters * 1000.0).isFinite()) {
                    "Parameterrechnung liegt außerhalb des endlichen Darstellungsbereichs."
                }
                Physics.relativeDifferencePercent(nonrelativisticWavelength.meters, wavelength.meters)
                val sha256 = requireNotNull(image.sha256)
                require(Regex("^[a-f0-9]{64}$").matches(sha256)) { "Ungültiger Bild-Prüfwert." }
                JonssonData(filename, width, height, markers, markerY, voltage.volts, slit.meters, distance.meters,
                    doc.jonsson.displayWarning, crop[0], band[0], crop[2] - crop[0], band[1] - band[0], sha256)
            }
            return ScientificData(points, spacing.meters, dg.displayWarning, dg.originalVerified, measurement, doc.verificationNotice, doc.sources, evidence, structure)
        }
    }
}

/** Coordinates refer to the unchanged full raster; the viewport never changes measurements. */
data class JonssonData(
    val imageFilename: String, val width: Int, val height: Int, val markers: List<Double>, val markerY: Double,
    val voltageV: Double, val slitSpacingM: Double, val distanceM: Double, val warning: String,
    val cropLeft: Double, val cropTop: Double, val cropWidth: Double, val cropHeight: Double, val imageSha256: String
) {
    val nativeMarkers: List<NativePixel> get() = markers.map(::NativePixel)
    val nativeMarkerY: NativePixel get() = NativePixel(markerY)
    val voltage: Voltage get() = Voltage(voltageV)
    val slitSpacing: Length get() = Length(slitSpacingM)
    val distance: Length get() = Length(distanceM)
}

@Serializable
private data class ScientificHeader(@SerialName("schema_version") val schemaVersion: String)

@Serializable
private data class ScientificDocument(
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("davisson_germer") val davissonGermer: DavissonGermerDocument,
    val jonsson: JonssonDocument,
    @SerialName("verification_notice") val verificationNotice: String = "",
    @SerialName("source_ids") val sources: Map<String, SourceReference> = emptyMap()
)

@Serializable
private data class DavissonGermerDocument(
    val geometry: SurfaceGeometryDocument, val rows: List<DgPointDocument>,
    val evidence: EvidenceDocument,
    @SerialName("display_warning") val displayWarning: String,
    @SerialName("original_verified") val originalVerified: Boolean
)

@Serializable
private data class SurfaceGeometryDocument(
    val model: SurfaceModel,
    @SerialName("angle_reference") val angleReference: AngleReference,
    @SerialName("lattice_constant_m") val latticeConstantM: JsonNumber,
    @SerialName("structure_evidence") val structureEvidence: EvidenceDocument,
    @SerialName("diffraction_order") val diffractionOrder: JsonNumber
)

@Serializable
private data class DgPointDocument(
    val id: String,
    @SerialName("voltage_V") val voltageV: JsonNumber,
    @SerialName("phi_deg") val phiDegrees: JsonNumber,
    @SerialName("kinetic_energy_eV") val kineticEnergyEv: JsonNumber,
    @SerialName("angle_approximate") val approximateAngle: Boolean,
    val kind: MaterialRoleDocument
)

@Serializable private enum class SurfaceModel { @SerialName("ni111_surface_rows_normal_incidence") NI111_NORMAL_INCIDENCE }
@Serializable private enum class AngleReference { @SerialName("outward_surface_normal") OUTWARD_SURFACE_NORMAL }
@Serializable private enum class MaterialRoleDocument {
    @SerialName("secondary_report") SECONDARY_REPORT,
    @SerialName("independent_structure_assumption") INDEPENDENT_STRUCTURE
}
@Serializable private enum class EvidenceReviewDocument {
    @SerialName("checked_in_scope") CHECKED_IN_SCOPE,
    @SerialName("not_checked") NOT_CHECKED
}
@Serializable private data class EvidenceDocument(
    val role: MaterialRoleDocument,
    @SerialName("review_status") val review: EvidenceReviewDocument,
    @SerialName("source_id") val sourceId: String,
    val location: String, val scope: String
)

@Serializable
private data class JonssonDocument(
    val image: JonssonImageDocument,
    @SerialName("display_warning") val displayWarning: String,
    @SerialName("parameter_model") val parameterModel: JonssonModelDocument? = null
)

@Serializable
private data class JonssonImageDocument(
    val available: Boolean, val file: String? = null,
    @SerialName("native_width_px") val width: JsonInteger? = null,
    @SerialName("native_height_px") val height: JsonInteger? = null,
    @SerialName("markers_px") val markers: List<JsonNumber>? = null,
    @SerialName("marker_y_px") val markerY: JsonNumber? = null,
    @SerialName("display_crop_native_px") val crop: List<JsonNumber>? = null,
    @SerialName("measurement_band_y_px") val band: List<JsonNumber>? = null,
    val sha256: String? = null
)

@Serializable
private data class JonssonModelDocument(
    @SerialName("voltage_V") val voltageV: JsonNumber,
    @SerialName("slit_center_spacing_m") val slitCenterSpacingM: JsonNumber,
    @SerialName("observation_distance_m") val observationDistanceM: JsonNumber
)
