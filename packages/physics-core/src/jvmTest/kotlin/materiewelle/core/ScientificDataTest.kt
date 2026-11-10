package materiewelle.core

import java.io.File
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScientificDataTest {
    private val source = File(System.getProperty("materiewelle.scientificData")).readText()
    private val profiles = File(System.getProperty("materiewelle.measurementProfiles")).readText()
    private val document get() = scientificJson.parseToJsonElement(source)
    private val profileDocument get() = scientificJson.parseToJsonElement(profiles)

    private fun JsonElement.replace(path: List<String>, value: JsonElement): JsonElement {
        if (path.isEmpty()) return value
        val key = path.first()
        return if (this is JsonObject) JsonObject(toMutableMap().apply { put(key, getValue(key).replace(path.drop(1), value)) })
        else JsonArray(jsonArray.mapIndexed { index, child -> if (index == key.toInt()) child.replace(path.drop(1), value) else child })
    }
    private fun mutate(vararg path: String, value: JsonElement) = document.replace(path.toList(), value).toString()
    private fun mutateProfile(vararg path: String, value: JsonElement) = profileDocument.replace(path.toList(), value).toString()

    @Test fun canonicalDocumentsDecodeWithHistoricalStatusIntact() {
        val data = ScientificData.fromJson(source)
        assertEquals(1, data.points.size)
        assertFalse(data.verified)
        assertTrue(data.dgWarning.contains("D2-Sekundärbefund"))
        assertEquals(MaterialRole.SECONDARY_REPORT, data.dgEvidence.role)
        assertEquals("D2", data.dgEvidence.sourceId)
        assertEquals(MaterialRole.INDEPENDENT_STRUCTURE_ASSUMPTION, data.dgStructure.evidence.role)
        assertEquals(54.0, data.points.single().voltage.volts)
        assertEquals(50.0, data.points.single().surfaceAngle.degrees)
        assertTrue(data.points.single().approximateAngle)
        assertEquals(2.1555509736491968e-10, data.spacing.meters, 1e-24)
        assertNotNull(data.sources["D1"])
        val image = assertNotNull(data.jonsson)
        assertEquals(703, image.width)
        assertEquals(242, image.height)
        assertEquals(96.0 / 7.0, PixelMeasurement.nativeMean(image.nativeMarkers).value, 1e-14)
        val methods = MeasurementProfiles.fromJson(profiles, image)
        assertEquals(setOf("basis", "oben", "unten"), methods.map { it.id }.toSet())
        val basis = methods.first { it.id == "basis" }
        assertEquals(545, basis.profileStartX)
        assertEquals(82.51666666666667, basis.grayscale.first())
        assertTrue(basis.grayscale.size > 100)
    }

    @Test fun noImageIsAnExplicitSuccessfulDataState() {
        val data = ScientificData.fromJson(mutate("jonsson", "image", "available", value = JsonPrimitive(false)))
        assertNull(data.jonsson)
        assertEquals(1, data.points.size)
    }

    @Test fun validSmallDatasetsAreNotNarrowedToTheCurrentReferenceFixture() {
        val small = document
            .replace(listOf("davisson_germer", "rows", "0", "voltage_V"), JsonPrimitive(0.5))
            .replace(listOf("davisson_germer", "rows", "0", "kinetic_energy_eV"), JsonPrimitive(0.5))
            .replace(listOf("jonsson", "image", "markers_px"), JsonArray(listOf(JsonPrimitive(551), JsonPrimitive(647))))
            .replace(listOf("jonsson", "image", "native_height_px"), JsonPrimitive(80))
            .replace(listOf("jonsson", "image", "marker_y_px"), JsonPrimitive(40))
            .replace(listOf("jonsson", "image", "display_crop_native_px"), JsonArray(listOf(490, 0, 703, 80).map(::JsonPrimitive)))
            .replace(listOf("jonsson", "image", "measurement_band_y_px"), JsonArray(listOf(20, 60).map(::JsonPrimitive)))
        val data = ScientificData.fromJson(small.toString())
        assertEquals(0.5, data.points.first().voltage.volts)
        val image = assertNotNull(data.jonsson)
        assertEquals(80, image.height)
        assertEquals(2, image.nativeMarkers.size)
        assertEquals(96.0, PixelMeasurement.nativeMean(image.nativeMarkers).value)
    }

    @Test fun malformedAndUnsupportedDocumentsFail() {
        listOf("{", "[]", "null", "{}", mutate("schema_version", value = JsonPrimitive("1.1")),
            mutate("davisson_germer", "geometry", "diffraction_order", value = JsonPrimitive(2)),
            mutate("davisson_germer", "geometry", "lattice_constant_m", value = JsonPrimitive(0))).forEach { assertFails { ScientificData.fromJson(it) } }
    }

    @Test fun finiteInputsWithUnrepresentableDerivedResultsAreRejectedDuringImport() {
        listOf(
            mutate("davisson_germer", "geometry", "lattice_constant_m", value = JsonPrimitive(1e308)),
            mutate("jonsson", "parameter_model", "observation_distance_m", value = JsonPrimitive(1e308)),
            mutate("jonsson", "parameter_model", "observation_distance_m", value = JsonPrimitive(1e306)),
            mutate("jonsson", "parameter_model", "slit_center_spacing_m", value = JsonPrimitive(Double.MIN_VALUE)),
            mutate("jonsson", "parameter_model", "slit_center_spacing_m", value = JsonPrimitive(1e308))
        ).forEach { assertFails { ScientificData.fromJson(it) } }
    }

    @Test fun rowsRequireUniqueIdsValidVoltageAndMeaningfulAngle() {
        listOf(
            mutate("davisson_germer", "rows", "0", "id", value = JsonPrimitive("")),
            mutate("davisson_germer", "rows", "0", "voltage_V", value = JsonPrimitive(-1)),
            mutate("davisson_germer", "rows", "0", "phi_deg", value = JsonPrimitive(0)),
            mutate("davisson_germer", "rows", "0", "phi_deg", value = JsonPrimitive(91)),
            mutate("davisson_germer", "rows", value = JsonArray(emptyList()))
        ).forEach { assertFails { ScientificData.fromJson(it) } }
    }

    @Test fun numericFieldsRejectJsonStringsInsteadOfSilentlyCoercingThem() {
        listOf(
            mutate("davisson_germer", "rows", "0", "voltage_V", value = JsonPrimitive("54")),
            mutate("davisson_germer", "geometry", "lattice_constant_m", value = JsonPrimitive("3.52e-10")),
            mutate("jonsson", "image", "native_width_px", value = JsonPrimitive("703")),
            mutate("jonsson", "image", "markers_px", "0", value = JsonPrimitive("551"))
        ).forEach { assertFails { ScientificData.fromJson(it) } }
    }

    @Test fun evidenceAndGeometryCannotSilentlyChangeTheirMeaning() {
        listOf(
            mutate("davisson_germer", "evidence", "role", value = JsonPrimitive("model_calculation")),
            mutate("davisson_germer", "evidence", "review_status", value = JsonPrimitive("not_checked")),
            mutate("davisson_germer", "evidence", "source_id", value = JsonPrimitive("unknown")),
            mutate("davisson_germer", "evidence", "source_id", value = JsonPrimitive("P2")),
            mutate("davisson_germer", "evidence", "location", value = JsonPrimitive("")),
            mutate("davisson_germer", "geometry", "angle_reference", value = JsonPrimitive("bragg_plane")),
            mutate("davisson_germer", "geometry", "structure_evidence", "role", value = JsonPrimitive("secondary_report")),
            mutate("davisson_germer", "rows", "0", "kinetic_energy_eV", value = JsonPrimitive(50)),
            mutate("davisson_germer", "rows", "0", "angle_approximate", value = JsonPrimitive(false)),
            mutate("davisson_germer", "original_verified", value = JsonPrimitive(true))
        ).forEach { assertFails { ScientificData.fromJson(it) } }
        val row = document.jsonObject.getValue("davisson_germer").jsonObject.getValue("rows").jsonArray.single()
        assertFails { ScientificData.fromJson(mutate("davisson_germer", "rows", value = JsonArray(listOf(row, row)))) }
    }

    @Test fun nativeRasterBoundsCannotBeReinterpretedAsScreenPixels() {
        listOf(
            mutate("jonsson", "image", "native_width_px", value = JsonPrimitive(703.5)),
            mutate("jonsson", "image", "markers_px", "1", value = JsonPrimitive(550)),
            mutate("jonsson", "image", "markers_px", "7", value = JsonPrimitive(703)),
            mutate("jonsson", "image", "marker_y_px", value = JsonPrimitive(220)),
            mutate("jonsson", "image", "measurement_band_y_px", "1", value = JsonPrimitive(243)),
            mutate("jonsson", "image", "sha256", value = JsonPrimitive("wrong"))
        ).forEach { assertFails { ScientificData.fromJson(it) } }
    }

    @Test fun profilesRequireMatchingImageIdentityAndNumericalClaims() {
        val image = assertNotNull(ScientificData.fromJson(source).jonsson)
        listOf(
            mutateProfile("source_image_sha256", value = JsonPrimitive("wrong")),
            mutateProfile("native_width_px", value = JsonPrimitive(704)),
            mutateProfile("profiles", "0", "integer_summary", "endpoint_mean_spacing_px", value = JsonPrimitive(14)),
            mutateProfile("profiles", "0", "integer_markers_x_px", "0", value = JsonPrimitive(551.5)),
            mutateProfile("profiles", "0", "band_stop_exclusive", value = JsonPrimitive(false)),
            mutateProfile("profiles", "0", "band_y_px", "1", value = JsonPrimitive(243)),
            mutateProfile("profiles", "0", "profile_mean_grayscale", "0", value = JsonPrimitive(256)),
            mutateProfile("profiles", "0", "profile_mean_grayscale", value = JsonArray(emptyList())),
            mutateProfile("profiles", "0", "profile_x_start_px", value = JsonPrimitive(702))
        ).forEach { assertFails { MeasurementProfiles.fromJson(it, image) } }
    }
    @Test fun aProfileBandCannotBeRelabeledOrCollapsedToOneSample() {
        val image = assertNotNull(ScientificData.fromJson(source).jonsson)
        listOf(
            mutateProfile("profiles", "0", "band_y_px", value = JsonArray(listOf(101, 220).map(::JsonPrimitive))),
            mutateProfile("profiles", "1", "band_y_px", value = JsonArray(listOf(21, 45).map(::JsonPrimitive))),
            mutateProfile("profiles", "2", "band_y_px", value = JsonArray(listOf(146, 220).map(::JsonPrimitive))),
            mutateProfile("profiles", "0", "profile_mean_grayscale", value = JsonArray(listOf(JsonPrimitive(100))))
        ).forEach { assertFails { MeasurementProfiles.fromJson(it, image) } }
    }

}
