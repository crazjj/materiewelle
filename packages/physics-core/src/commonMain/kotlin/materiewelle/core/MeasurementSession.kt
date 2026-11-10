package materiewelle.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Integer coordinates of the unchanged image, never CSS pixels. */
@Serializable
data class NativeImageMark(val xPx: Int) {
    init { require(xPx >= 0) { "Eine Bildmarke benötigt eine nichtnegative native Koordinate." } }
}

@Serializable
data class NativeImageBand(val topInclusive: Int, val bottomExclusive: Int) {
    init { require(topInclusive >= 0 && topInclusive < bottomExclusive) }
}

/** A stored coordinate only has meaning together with this exact scientific raster and crop. */
@Serializable
data class MeasurementImageIdentity(
    val sha256: String,
    val nativeWidthPx: Int,
    val nativeHeightPx: Int,
    val cropLeftInclusivePx: Int,
    val cropRightExclusivePx: Int,
    val baseBand: NativeImageBand
) {
    init {
        require(Regex("[a-f0-9]{64}").matches(sha256))
        require(nativeWidthPx > 0 && nativeHeightPx > 0)
        require(cropLeftInclusivePx >= 0 && cropLeftInclusivePx < cropRightExclusivePx && cropRightExclusivePx <= nativeWidthPx)
        require(baseBand.bottomExclusive <= nativeHeightPx)
    }
    companion object {
        fun from(data: JonssonData): MeasurementImageIdentity = MeasurementImageIdentity(data.imageSha256,
            data.width, data.height, data.cropLeft.toInt(), (data.cropLeft + data.cropWidth).toInt(),
            NativeImageBand(data.cropTop.toInt(), (data.cropTop + data.cropHeight).toInt()))
    }
}

/** The second boundary is excluded, as in the actual grayscale averaging procedure. */
@Serializable
enum class MeasurementBand(val profileId: String, val topInclusive: Int, val bottomExclusive: Int, val label: String) {
    BASE("basis", 100, 220, "Basisband"),
    UPPER("oben", 20, 45, "Oberes Band"),
    LOWER("unten", 145, 220, "Unteres Band");

    companion object { fun fromProfileId(id: String): MeasurementBand? = entries.firstOrNull { it.profileId == id } }
}

sealed interface MeasurementSelection {
    data object Empty : MeasurementSelection
    data class Partial(val mark: NativeImageMark) : MeasurementSelection
    data class Ready(val intervalsPx: List<Int>, val meanPx: Double) : MeasurementSelection
}

@Serializable
enum class MeasurementStatus {
    @SerialName("empty") EMPTY,
    @SerialName("partial") PARTIAL,
    @SerialName("ready") READY
}

@Serializable
enum class MeasurementProvenance {
    @SerialName("own_present_day_pixel_measurement_of_historical_reproduction") OWN_PIXEL_MEASUREMENT
}

@Serializable
enum class ImageCalibrationStatus {
    @SerialName("no_independent_absolute_image_scale") MISSING_ABSOLUTE_SCALE
}

/** Material contacts can influence a choice; a request is not proof of delivery or reading. */
@Serializable
enum class ReferenceMaterial(val label: String) {
    PDF_REFERENCE("Fachfassung mit Referenzangaben"),
    TUTOR_CONTEXT("Tutor-Material mit Referenzangaben"),
    @SerialName("HANDOUT_REQUESTED") HANDOUT_REQUESTED("Handout-Zugriff angefordert"),
    UNKNOWN_LEGACY("Referenzkontakt aus älterem Lernstand, Zuordnung unbekannt")
}

/** A saved selection is an immutable, independent record, not a live reference to editing state. */
@Serializable
data class MeasurementSnapshot(
    val band: MeasurementBand,
    val markers: List<NativeImageMark>,
    val justification: String,
    val referenceViewedAtCapture: Boolean,
    val imageSha256: String,
    val nativeWidthPx: Int,
    val nativeHeightPx: Int,
    val bandTopInclusivePx: Int = band.topInclusive,
    val bandBottomExclusivePx: Int = band.bottomExclusive,
    val priorReferenceMaterials: Set<ReferenceMaterial> = emptySet()
) {
    init {
        validateMarkers(markers); require(markers.size >= 2); require(justification.isNotBlank() && justification.length <= 4000)
        require(Regex("[a-f0-9]{64}").matches(imageSha256)); require(nativeWidthPx > 0 && nativeHeightPx > 0)
        require(markers.all { it.xPx < nativeWidthPx })
        require(bandTopInclusivePx >= 0 && bandTopInclusivePx < bandBottomExclusivePx && bandBottomExclusivePx <= nativeHeightPx)
    }
    val intervalsPx: List<Int> get() = markers.zipWithNext { a, b -> b.xPx - a.xPx }
    val meanPx: Double get() = intervalsPx.average()
}

/** Scientific editing state; display zoom, selected marker and SVG dimensions are deliberately absent. */
@Serializable
data class MeasurementSession(
    val band: MeasurementBand = MeasurementBand.BASE,
    val markers: List<NativeImageMark> = emptyList(),
    val justification: String = "",
    val initialSelection: MeasurementSnapshot? = null,
    val referenceViewed: Boolean = false,
    val comparison: MeasurementSnapshot? = null,
    val justificationBand: MeasurementBand? = MeasurementBand.BASE,
    val referenceMaterialsOpened: Set<ReferenceMaterial> = emptySet()
) {
    init {
        validateMarkers(markers)
        require(justification.length <= 4000)
        require(!referenceViewed || initialSelection != null) { "Referenzvergleich benötigt eine gesicherte eigene Erstentscheidung." }
        require(initialSelection?.referenceViewedAtCapture != true) { "Die erste gesicherte Auswahl muss vor dem Referenzvergleich liegen." }
        require(comparison?.referenceViewedAtCapture != true || referenceViewed)
    }

    val selection: MeasurementSelection get() = when (markers.size) {
        0 -> MeasurementSelection.Empty
        1 -> MeasurementSelection.Partial(markers.single())
        else -> MeasurementSelection.Ready(markers.zipWithNext { a, b -> b.xPx - a.xPx },
            (markers.last().xPx - markers.first().xPx).toDouble() / (markers.size - 1))
    }
    val justificationMatchesBand: Boolean get() = justificationBand == band
    val canSave: Boolean get() = markers.size >= 2 && justification.isNotBlank() && justificationMatchesBand
    val canViewReference: Boolean get() = initialSelection != null

    /** BASE follows the imported raster's declared band, including valid smaller test rasters. */
    fun nativeBand(data: JonssonData): NativeImageBand = nativeBand(MeasurementImageIdentity.from(data))

    fun nativeBand(image: MeasurementImageIdentity): NativeImageBand {
        if (band == MeasurementBand.BASE) return image.baseBand
        require(band.bottomExclusive <= image.nativeHeightPx) { "Gewähltes Band liegt außerhalb des Bildrasters." }
        return NativeImageBand(band.topInclusive, band.bottomExclusive)
    }

    fun validate(data: JonssonData): MeasurementSession = validate(MeasurementImageIdentity.from(data))

    fun validate(image: MeasurementImageIdentity): MeasurementSession {
        validate(image.nativeWidthPx, image.nativeHeightPx, image.cropLeftInclusivePx, image.cropRightExclusivePx)
        fun valid(values: List<NativeImageMark>) = values.all { it.xPx >= image.cropLeftInclusivePx && it.xPx < image.cropRightExclusivePx }
        require(valid(markers) && initialSelection?.let { valid(it.markers) } != false && comparison?.let { valid(it.markers) } != false)
        require(listOfNotNull(initialSelection, comparison).all { it.imageSha256 == image.sha256 && it.nativeWidthPx == image.nativeWidthPx && it.nativeHeightPx == image.nativeHeightPx })
        return this
    }

    fun validate(imageWidth: Int = 703, imageHeight: Int = 242, cropLeftInclusivePx: Int = 490, cropRightExclusivePx: Int = imageWidth): MeasurementSession {
        require(imageWidth > 0 && imageHeight > 0)
        require(cropLeftInclusivePx >= 0 && cropLeftInclusivePx < cropRightExclusivePx && cropRightExclusivePx <= imageWidth)
        require(band == MeasurementBand.BASE || band.bottomExclusive <= imageHeight) { "Gewähltes Band liegt außerhalb des Bildrasters." }
        require(markers.all { it.xPx >= cropLeftInclusivePx && it.xPx < cropRightExclusivePx })
        require(listOfNotNull(initialSelection, comparison).all { it.nativeWidthPx == imageWidth && it.nativeHeightPx == imageHeight })
        return this
    }

    fun changeBand(next: MeasurementBand): MeasurementSession = copy(band = next)

    /** Editing the reasoning associates it with the actually visible band, never with a later one. */
    fun updateJustification(text: String): MeasurementSession = copy(justification = text, justificationBand = band)

    /** Reusing old reasoning requires a conscious confirmation after checking the new band. */
    fun confirmJustification(): MeasurementSession {
        require(justification.isNotBlank())
        return copy(justificationBand = band)
    }

    fun recordReferenceMaterial(material: ReferenceMaterial): MeasurementSession =
        copy(referenceMaterialsOpened = referenceMaterialsOpened + material)

    fun addMarker(xPx: Int, data: JonssonData): MeasurementSession {
        require(markers.size < MAX_MARKERS && markers.none { it.xPx == xPx })
        return copy(markers = (markers + NativeImageMark(xPx)).sortedBy { it.xPx }).validate(data)
    }

    fun moveMarker(index: Int, xPx: Int, data: JonssonData): MeasurementSession {
        require(index in markers.indices)
        return copy(markers = markers.mapIndexed { i, mark -> if (i == index) NativeImageMark(xPx) else mark }).validate(data)
    }

    fun removeMarker(index: Int): MeasurementSession {
        require(index in markers.indices)
        return copy(markers = markers.filterIndexed { i, _ -> i != index })
    }

    /** Clearing the current attempt never erases that a reference was already seen. */
    fun clearMeasurement(): MeasurementSession = copy(markers = emptyList(), justification = "", justificationBand = band)

    fun saveInitialSelection(data: JonssonData): MeasurementSession {
        require(initialSelection == null && !referenceViewed && canSave)
        validate(data)
        return copy(initialSelection = snapshot(data))
    }

    fun revealReference(): MeasurementSession {
        require(canViewReference)
        return copy(referenceViewed = true)
    }

    fun saveComparison(data: JonssonData): MeasurementSession { require(canSave); validate(data); return copy(comparison = snapshot(data)) }
    fun clearComparison(): MeasurementSession = copy(comparison = null)
    private fun snapshot(data: JonssonData) = nativeBand(data).let { range ->
        MeasurementSnapshot(band, markers.toList(), justification.trim(), referenceViewed, data.imageSha256, data.width, data.height,
            range.topInclusive, range.bottomExclusive, referenceMaterialsOpened.toSet())
    }

    fun protocol(data: JonssonData): MeasurementProtocol {
        validate(data)
        val ready = selection as? MeasurementSelection.Ready
        val bandRange = nativeBand(data)
        return MeasurementProtocol(imageFilename = data.imageFilename, imageSha256 = data.imageSha256,
            nativeWidthPx = data.width, nativeHeightPx = data.height,
            cropLeftInclusivePx = data.cropLeft.toInt(), cropRightExclusivePx = (data.cropLeft + data.cropWidth).toInt(),
            band = band, bandTopInclusivePx = bandRange.topInclusive, bandBottomExclusivePx = bandRange.bottomExclusive,
            nativeMarkers = markers.toList(), justification = justification,
            state = when (selection) { MeasurementSelection.Empty -> MeasurementStatus.EMPTY; is MeasurementSelection.Partial -> MeasurementStatus.PARTIAL; is MeasurementSelection.Ready -> MeasurementStatus.READY },
            intervalsPx = ready?.intervalsPx ?: emptyList(), meanSpacingPx = ready?.meanPx,
            referenceViewed = referenceViewed, initialSelection = initialSelection, comparison = comparison,
            justificationBand = justificationBand, referenceMaterialsOpened = referenceMaterialsOpened.toSet())
    }

    companion object { const val MAX_MARKERS = 16 }
}

private fun validateMarkers(markers: List<NativeImageMark>) {
    require(markers.size <= MeasurementSession.MAX_MARKERS)
    require(markers.zipWithNext().all { (a, b) -> a.xPx < b.xPx }) { "Native Marken müssen streng aufsteigend und verschieden sein." }
}

/** Export records provenance and missing calibration explicitly; there is no field for a measured λ. */
@Serializable
data class MeasurementProtocol(
    val schemaVersion: String = "2.0",
    val provenance: MeasurementProvenance = MeasurementProvenance.OWN_PIXEL_MEASUREMENT,
    val imageFilename: String,
    val imageSha256: String,
    val nativeWidthPx: Int,
    val nativeHeightPx: Int,
    val cropLeftInclusivePx: Int,
    val cropRightExclusivePx: Int,
    val band: MeasurementBand,
    val bandTopInclusivePx: Int = band.topInclusive,
    val bandBottomExclusivePx: Int = band.bottomExclusive,
    val nativeMarkers: List<NativeImageMark>,
    val justification: String,
    val state: MeasurementStatus,
    val intervalsPx: List<Int>,
    val meanSpacingPx: Double?,
    val referenceViewed: Boolean,
    val initialSelection: MeasurementSnapshot?,
    val comparison: MeasurementSnapshot?,
    val initialSelectionIntervalsPx: List<Int>? = initialSelection?.intervalsPx,
    val initialSelectionMeanPx: Double? = initialSelection?.meanPx,
    val comparisonIntervalsPx: List<Int>? = comparison?.intervalsPx,
    val comparisonMeanPx: Double? = comparison?.meanPx,
    val calibrationStatus: ImageCalibrationStatus = ImageCalibrationStatus.MISSING_ABSOLUTE_SCALE,
    val profileMeaning: String = "Mean of unchanged 8-bit image grayscale, not calibrated electron intensity",
    val justificationBand: MeasurementBand? = MeasurementBand.BASE,
    val referenceMaterialsOpened: Set<ReferenceMaterial> = emptySet()
) {
    fun json(): String = Json { prettyPrint = true; encodeDefaults = true }.encodeToString(this)

    fun readable(includeTechnicalAppendix: Boolean = false): String = buildString {
        appendLine("Jönsson · eigenes Messprotokoll")
        appendLine("Bild: $imageFilename ($nativeWidthPx × $nativeHeightPx Originalpixel)")
        appendLine("Herkunft: heutige eigene Pixelmessung einer historischen Reproduktion; keine neue historische Messung.")
        appendLine("Band: ${band.label}, $bandTopInclusivePx ≤ y < $bandBottomExclusivePx; x-Ausschnitt: $cropLeftInclusivePx ≤ x < $cropRightExclusivePx.")
        appendLine("Messstand: ${when (state) {
            MeasurementStatus.EMPTY -> "Noch keine Marke gesetzt."
            MeasurementStatus.PARTIAL -> "Eine Marke gesetzt; für einen Abstand fehlt eine zweite."
            MeasurementStatus.READY -> "Abstände berechenbar; die Auswahl der Maxima ist damit nicht geprüft."
        }}")
        appendLine("Marken x / Originalpixel: ${nativeMarkers.joinToString { it.xPx.toString() }.ifEmpty { "keine" }}")
        appendLine("Abstände / Originalpixel: ${intervalsPx.joinToString().ifEmpty { "noch nicht berechenbar" }}")
        appendLine("Arithmetischer mittlerer Abstand / Originalpixel: ${meanSpacingPx?.let { Physics.decimal(it, 3) } ?: "noch nicht berechenbar"}")
        appendLine("Eigene Begründung: ${justification.ifBlank { "noch nicht eingetragen" }}")
        appendLine("Begründung zugeordnet zu: ${justificationBand?.label ?: "unbekannt"}; ${when {
            justification.isBlank() -> "noch keine Begründung eingetragen"
            justificationBand == band -> "dem aktuellen Band zugeordnet"
            else -> "Zuordnung zum aktuellen Band (${band.label}) noch zu bestätigen"
        }}.")
        initialSelection?.let { appendReadableSelection("Gesicherte erste Auswahl", it) }
        comparison?.let { appendReadableSelection("Gespeicherter eigener Vergleich", it) }
        appendLine("Mittelwerte sind auf drei Nachkommastellen gerundet. Diese Darstellung gibt keine Messgenauigkeit an.")
        appendLine("Bandwechsel und ±1-Pixel-Variationen sind Sensitivitätsprüfungen, keine unabhängigen historischen Wiederholungen oder belegten Fehlergrenzen.")
        appendLine("Grauwert ist keine kalibrierte Elektronenintensität. Ohne unabhängigen Maßstab für genau dieses Foto folgt keine absolute Wellenlänge aus Pixeln.")
        appendLine("Anzeigezoom und Bildschirmgröße sind keine Messparameter.")
        if (!includeTechnicalAppendix) return@buildString
        appendLine()
        appendLine("TECHNISCHER ANHANG · RECHENWERTE UND KONTAKTVERMERKE")
        appendLine("Schema: $schemaVersion; Messzustand: $state; Bandkennung: ${band.name}; Begründungsband: ${justificationBand?.name ?: "null"}")
        appendLine("SHA-256: $imageSha256")
        appendLine("Datenherkunft: $provenance; Kalibrierungsstatus: $calibrationStatus")
        appendLine("Bedeutung des Grauwertprofils: $profileMeaning")
        appendLine("Rechenwert ohne Anzeigerundung des mittleren Abstands / px: ${meanSpacingPx ?: "nicht berechenbar"}")
        appendLine("Vermerkte Hilfen und Dateianfragen: ${referenceMaterialsOpened.joinToString { it.label }.ifEmpty { "keine protokolliert" }}")
        appendLine("Öffnen der Referenz vermerkt: ${if (referenceViewed) "ja; sie könnte spätere Auswahlen beeinflusst haben" else "nein; daraus folgt nicht, dass keine Hilfen verwendet wurden"}")
        initialSelection?.let { appendTechnicalSelection("Gesicherte erste Auswahl", it, initialSelectionMeanPx) }
        comparison?.let { appendTechnicalSelection("Gespeicherter eigener Vergleich", it, comparisonMeanPx) }
        appendLine("Die Vermerke erfassen Öffnungs- und Anfrageereignisse. Sie belegen weder Lesen oder Übernahme noch eine erfolgreiche Dateizustellung.")
    }

    private fun StringBuilder.appendReadableSelection(label: String, snapshot: MeasurementSnapshot) {
        appendLine("$label: ${snapshot.band.label}, ${snapshot.bandTopInclusivePx} ≤ y < ${snapshot.bandBottomExclusivePx}")
        appendLine("Marken x / Originalpixel: ${snapshot.markers.joinToString { it.xPx.toString() }}; Abstände / Originalpixel: ${snapshot.intervalsPx.joinToString()}")
        appendLine("Arithmetischer mittlerer Abstand / Originalpixel: ${Physics.decimal(snapshot.meanPx, 3)}")
        appendLine("Begründung: ${snapshot.justification}")
    }

    private fun StringBuilder.appendTechnicalSelection(label: String, snapshot: MeasurementSnapshot, recordedMean: Double?) {
        appendLine("$label · Bandkennung: ${snapshot.band.name}; SHA-256: ${snapshot.imageSha256}; ${snapshot.nativeWidthPx} × ${snapshot.nativeHeightPx} Originalpixel")
        appendLine("Rechenwert ohne Anzeigerundung des mittleren Abstands / px: ${snapshot.meanPx}; im Protokoll gespeicherter Mittelwert: ${recordedMean ?: "null"}")
        appendLine("Referenz vor Speicherung bereits eingeblendet: ${if (snapshot.referenceViewedAtCapture) "ja" else "nein"}")
        appendLine("Vor Speicherung vermerkte Hilfen und Dateianfragen: ${snapshot.priorReferenceMaterials.joinToString { it.label }.ifEmpty { "keine protokolliert" }}")
    }
}
