package materiewelle.pages

import androidx.compose.runtime.Composable
import materiewelle.JonssonData
import materiewelle.core.*
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*

/** Read-only view of the existing comparison contract; never writes or reconstructs a snapshot. */
@Composable
fun MeasurementComparison(data: JonssonData, measurement: MeasurementSession) {
    val focusResult = rememberMeasurementResultFocus()
    val saved = measurement.comparison
    val current = measurement.selection as? MeasurementSelection.Ready
    val currentBand = measurement.nativeBand(data)
    Section(
        attrs = {
            elementId(MeasurementComparisonId.ROOT)
            measurementComparisonStyles(MeasurementComparisonClass.ROOT)
            labelledBy(MeasurementComparisonId.TITLE)
            focusIndex(-1)
        }
    ) {
        Div(attrs = { measurementComparisonStyles(MeasurementComparisonClass.HEADER) }) {
            H3(attrs = { elementId(MeasurementComparisonId.TITLE) }) {
                Text("Zwei eigene Messungen vergleichen")
            }
            if (saved != null) {
                P(attrs = { elementId(MeasurementElement.COMPARISON_SAVED); role(AriaRole.STATUS) }) {
                    Text("A gespeichert: ${saved.markers.size} Marken im Band „${saved.band.label}“. B zeigt deine aktuelle Auswahl.")
                }
                Button(attrs = {
                    elementId(MeasurementElement.COMPARISON_EDIT)
                    styles(LabClass.SECONDARY)
                    onClick { focusResult(Id.JONSSON_INTERACTIVE_IMAGE) }
                }) { Text("Marken weiter bearbeiten") }
            }
        }
        if (saved == null) {
            P {
                Text(if (current == null)
                    "Setze zuerst eigene Bildmarken und begründe deine Auswahl. Danach kannst du einen Stand zum Vergleichen festhalten."
                    else "Speichere die aktuelle Auswahl als A. Verschiebe danach eine Marke und vergleiche die Einzelabstände und das Mittel.")
            }
            return@Section
        }
        Div(attrs = { measurementComparisonStyles(MeasurementComparisonClass.GRID) }) {
            SnapshotCard(MeasurementCase.SAVED, saved)
            MeasurementCaseCard(
                case = MeasurementCase.CURRENT,
                band = measurement.band,
                bandRange = currentBand,
                markers = measurement.markers,
                intervals = current?.intervalsPx ?: emptyList(),
                mean = current?.meanPx,
                justification = measurement.justification,
                justificationMatchesBand = measurement.justificationMatchesBand,
                referenceViewed = measurement.referenceViewed,
                materials = measurement.referenceMaterialsOpened,
                imageSha256 = data.imageSha256,
                nativeWidth = data.width,
                nativeHeight = data.height,
                currentJustificationBand = measurement.justificationBand,
            )
        }
        MeasurementDifference(data, measurement, saved, current)
        Details(attrs = { measurementComparisonStyles(MeasurementComparisonClass.DETAILS) }) {
            Summary { Text("Was bei einer Auswahl mitgespeichert wird") }
            P(
                attrs = {
                    elementId(MeasurementComparisonId.CONTACT_NOTE)
                    measurementComparisonStyles(MeasurementComparisonClass.CONTACT_NOTE)
                }
            ) {
                Text(
                    "Mit einer Auswahl werden die bis dahin vermerkten Hilfen und Dateianfragen festgehalten. Spätere Ereignisse ändern diesen Eintrag nicht. Daraus folgt weder, dass Inhalte gelesen oder übernommen wurden, noch dass eine angeforderte Datei erfolgreich heruntergeladen wurde. Referenzmarken sind keine eigene zweite Messung."
                )
            }
        }
        Details(
            attrs = {
                elementId(MeasurementComparisonId.OPTIONAL_PROBE)
                measurementComparisonStyles(
                    MeasurementComparisonClass.DETAILS,
                    MeasurementComparisonClass.OPTIONAL,
                )
            }
        ) {
            Summary { Text("Freiwillige Gegenprobe · gleiches Mittel, andere Einzelabstände?") }
            P {
                Text(
                    "Für diese Gegenprobe brauchst du mindestens drei Marken, damit eine davon zwischen den Endpunkten liegt."
                )
            }
            P {
                Text(
                    "Nach deiner eigenen Bildmessung: Speichere einen begründeten Stand als A. Sage für B zuerst voraus, was eine Verschiebung einer inneren Marke um einen Pixel verändert. Vergleiche die beiden Nachbarabstände und das Mittel; lass die Endpunkte fest."
                )
            }
            P {
                Text(
                    "Verschiebe danach stattdessen die letzte Marke um einen Pixel nach rechts. Die Änderung verteilt sich im Mittel auf die Anzahl der Intervalle: bei vier Marken also auf drei Intervalle. Ist die gewählte Marke am Bildrand oder würde sie eine andere kreuzen, wähle einen zulässigen Gegenversuch."
                )
            }
            P {
                Text(
                    "Erkläre anschließend in deinen eigenen Worten, warum derselbe Mittelwert unterschiedliche Markensetzungen verdecken kann. Ein unverändertes Mittel bestätigt die Wahl von Maxima nicht. Eine qualitative Erklärung an den zwei Nachbarintervallen genügt; dies ist keine zusätzliche Pflichtprüfung und wird hier nicht automatisch bewertet."
                )
            }
        }
    }
}

@Composable
internal fun InitialSelection(measurement: MeasurementSession) {
    val first = measurement.initialSelection ?: return
    Details(attrs = {
        elementId(MeasurementComparisonId.ORIGIN)
        measurementComparisonStyles(MeasurementComparisonClass.DETAILS, MeasurementComparisonClass.ORIGIN)
    }) {
        Summary { Text("Erste eigene Auswahl: ${first.markers.size} Marken · ≈ ${Physics.decimal(first.meanPx, 1)} px · ${first.band.label}") }
        P { Text("Diese erste Auswahl bleibt unverändert erhalten. Spätere Änderungen und der optionale Vergleich A überschreiben sie nicht. Welche Hilfen damals geöffnet waren, steht in den Details.") }
        SnapshotCard(MeasurementCase.INITIAL, first)
    }
}


@Composable
private fun SnapshotCard(case: MeasurementCase, snapshot: MeasurementSnapshot) {
    MeasurementCaseCard(
        case = case,
        band = snapshot.band,
        bandRange = NativeImageBand(snapshot.bandTopInclusivePx, snapshot.bandBottomExclusivePx),
        markers = snapshot.markers,
        intervals = snapshot.intervalsPx,
        mean = snapshot.meanPx,
        justification = snapshot.justification,
        justificationMatchesBand = true,
        referenceViewed = snapshot.referenceViewedAtCapture,
        materials = snapshot.priorReferenceMaterials,
        imageSha256 = snapshot.imageSha256,
        nativeWidth = snapshot.nativeWidthPx,
        nativeHeight = snapshot.nativeHeightPx,
    )
}

@Composable
private fun MeasurementCaseCard(
    case: MeasurementCase,
    band: MeasurementBand,
    bandRange: NativeImageBand,
    markers: List<NativeImageMark>,
    intervals: List<Int>,
    mean: Double?,
    justification: String,
    justificationMatchesBand: Boolean,
    referenceViewed: Boolean,
    materials: Set<ReferenceMaterial>,
    imageSha256: String,
    nativeWidth: Int,
    nativeHeight: Int,
    currentJustificationBand: MeasurementBand? = null,
) {
    val title =
        when (case) {
            MeasurementCase.SAVED -> "A · Eigener gespeicherter Vergleich"
            MeasurementCase.CURRENT -> "B · Aktueller eigener Arbeitsstand"
            MeasurementCase.INITIAL -> "Erste festgehaltene Messung"
        }
    val captured = case != MeasurementCase.CURRENT
    Section(
        attrs = {
            elementId(MeasurementCaseId(case, MeasurementCaseField.CARD))
            measurementCase(case)
            measurementComparisonStyles(MeasurementComparisonClass.CARD)
            if (!captured) measurementComparisonStyles(MeasurementComparisonClass.CURRENT)
            labelledBy(MeasurementCaseId(case, MeasurementCaseField.TITLE))
        }
    ) {
        H4(
            attrs = {
                elementId(MeasurementCaseId(case, MeasurementCaseField.TITLE))
                measurementComparisonStyles(MeasurementComparisonClass.CASE_LABEL)
            }
        ) {
            Text(title)
        }
        P(
            attrs = {
                elementId(MeasurementCaseId(case, MeasurementCaseField.BAND))
                measurementComparisonStyles(MeasurementComparisonClass.FIELD)
            }
        ) {
            Strong { Text(band.label) }
            Text(" · ${bandRange.topInclusive} ≤ y < ${bandRange.bottomExclusive} Originalpixel")
        }
        Div(attrs = { measurementComparisonStyles(MeasurementComparisonClass.MEAN) }) {
            Span { Text("Mittlerer Abstand") }
            Strong(
                attrs = {
                    elementId(MeasurementCaseId(case, MeasurementCaseField.MEAN))
                    mean?.let { data(LabData.SPACING_PX, it) }
                }
            ) {
                Text(mean?.let { "≈ ${Physics.decimal(it, 3)} px" } ?: "Mindestens zwei Marken nötig")
            }
            Span { Text("${markers.size} Marken im Originalbild · ${intervals.size} benachbarte Abstände") }
        }
        Div(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD) }) {
            P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD_LABEL) }) {
                Text("Marken im Originalbild · von links nach rechts")
            }
            P(
                attrs = {
                    elementId(MeasurementCaseId(case, MeasurementCaseField.MARKERS))
                    measurementComparisonStyles(MeasurementComparisonClass.COORDINATES)
                }
            ) {
                Text(
                    markers
                        .joinToString(" · ") { "${it.xPx} px" }
                        .ifEmpty { "Noch keine eigenen Marken gesetzt." }
                )
            }
        }
        Div(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD) }) {
            P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD_LABEL) }) {
                Text("Einzelabstände · aus benachbarten Marken")
            }
            if (intervals.isEmpty())
                P {
                    Text(
                        "Setze mindestens zwei Marken, um einen Abstand zu berechnen."
                    )
                }
            Ol(
                attrs = {
                    elementId(MeasurementCaseId(case, MeasurementCaseField.INTERVALS))
                    measurementComparisonStyles(MeasurementComparisonClass.INTERVALS)
                    ariaLabel("Einzelabstände von $title in Originalpixeln")
                }
            ) {
                intervals.forEachIndexed { index, value ->
                    Li(
                        attrs = {
                            data(LabData.INTERVAL, index)
                            data(LabData.VALUE_PX, value.toDouble())
                        }
                    ) {
                        Span { Text("${index + 1}–${index + 2}") }
                        Strong { Text("$value px") }
                    }
                }
            }
        }
        Div(
            attrs = {
                elementId(MeasurementCaseId(case, MeasurementCaseField.JUSTIFICATION))
                measurementComparisonStyles(MeasurementComparisonClass.FIELD)
            }
        ) {
            P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD_LABEL) }) {
                Text(if (captured) "Begründung beim Speichern" else "Aktuelle eigene Begründung")
            }
            P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.JUSTIFICATION) }) {
                Text(justification.ifBlank { "Noch keine eigene Begründung eingetragen." })
            }
            if (!captured && justification.isNotBlank() && !justificationMatchesBand) {
                P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.WARNING) }) {
                    Text(
                        "Die Begründung gehört ${currentJustificationBand?.let { "zum Band „${it.label}“" } ?: "zu einem unbekannten früheren Band"}. Prüfe oder bestätige sie oben für das Band „${band.label}“. Die übernommenen Marken allein begründen den Bandwechsel nicht."
                    )
                }
            }
        }
        Details(
            attrs = {
                elementId(MeasurementCaseId(case, MeasurementCaseField.CONTACT))
                measurementComparisonStyles(MeasurementComparisonClass.CONTACT)
            }
        ) {
            Summary { Text("Referenz und bereits geöffnete Hilfen") }
            P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.FIELD_LABEL) }) {
                Text(
                    if (captured) "Vermerkte Hilfen beim Festhalten"
                    else "Aktuell vermerkte Hilfen"
                )
            }
            P {
                Text(
                    if (referenceViewed) {
                        if (captured) "Laborreferenz war beim Speichern bereits eingeblendet."
                        else "Laborreferenz ist bereits eingeblendet worden."
                    } else {
                        if (captured)
                            "Für diesen Stand ist kein Öffnen der Referenz vermerkt."
                        else "Bisher ist kein Öffnen der Referenz vermerkt."
                    }
                )
            }
            P {
                Text(
                    if (materials.isEmpty())
                        "Keine weiteren Hilfen oder Dateianfragen vermerkt."
                    else
                        "Vermerkte Hilfen und Dateianfragen: ${ReferenceMaterial.entries.filter { it in materials }.joinToString { it.label }}."
                )
            }
            if (ReferenceMaterial.UNKNOWN_LEGACY in materials)
                P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.WARNING) }) {
                    Text(
                        "Altkontakt ist bekannt, seine Zuordnung ist unbekannt. Daraus lässt sich keine unbeeinflusste Auswahl ableiten."
                    )
                }
        }
        Details(
            attrs = {
                elementId(MeasurementCaseId(case, MeasurementCaseField.IMAGE))
                measurementComparisonStyles(MeasurementComparisonClass.IMAGE_ID)
            }
        ) {
            Summary { Text("Bildidentität · $nativeWidth × $nativeHeight Originalpixel") }
            P { Text("SHA-256: $imageSha256") }
            P {
                Text(
                    "Marken und Band gehören zu genau diesem Raster. Bildschirmgröße und Anzeigezoom sind keine Messparameter."
                )
            }
        }
    }
}

@Composable
private fun MeasurementDifference(
    data: JonssonData,
    measurement: MeasurementSession,
    saved: MeasurementSnapshot,
    current: MeasurementSelection.Ready?,
) {
    val sameRaster =
        saved.imageSha256 == data.imageSha256 &&
            saved.nativeWidthPx == data.width &&
            saved.nativeHeightPx == data.height
    val band = measurement.nativeBand(data)
    val sameBand =
        saved.bandTopInclusivePx == band.topInclusive &&
            saved.bandBottomExclusivePx == band.bottomExclusive
    Div(
        attrs = {
            elementId(MeasurementComparisonId.DIFFERENCE)
            measurementComparisonStyles(MeasurementComparisonClass.DIFFERENCE)
        }
    ) {
        H4 { Text("Was hat sich von A zu B geändert?") }
        when {
            !sameRaster ->
                P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.WARNING) }) {
                    Text(
                        "A und B beziehen sich auf unterschiedliche Bildidentitäten. Ihre Pixelabstände werden hier nicht als Änderung derselben Messung verrechnet."
                    )
                }
            current == null ->
                P {
                    Text(
                        "B benötigt mindestens zwei eigene Marken. A bleibt gespeichert; eine Differenz wird erst für eine vollständige aktuelle Auswahl angegeben."
                    )
                }
            else -> {
                val delta = current.meanPx - saved.meanPx
                P {
                    Text(
                        "Mittlerer Abstand B minus A: ${if (delta > 0) "+" else ""}${Physics.decimal(delta, 4)} Originalpixel. Vergleiche dazu die Einzelabstände beider Karten; die Mittelwertdifferenz allein beurteilt keine Maximalwahl."
                    )
                }
                if (sameBand)
                    P {
                        Text(
                            "Beide Entscheidungen verwenden dasselbe Bild und dieselben Bandgrenzen im Originalbild. Andere Marken oder Begründungen können trotzdem zu unterschiedlichen Entscheidungen gehören."
                        )
                    }
                else
                    P(attrs = { measurementComparisonStyles(MeasurementComparisonClass.WARNING) }) {
                        Text(
                            "Das Auswerteband wurde geändert: A verwendet ${saved.bandTopInclusivePx} ≤ y < ${saved.bandBottomExclusivePx}, B verwendet ${band.topInclusive} ≤ y < ${band.bottomExclusive}. Die Differenz vermischt gegebenenfalls Band- und Markenwahl; sie ist keine reine Verschiebungsprobe. Prüfe die Maxima und begründe B im aktuellen Band."
                        )
                    }
                if (saved.markers.size == measurement.markers.size) {
                    val changed =
                        saved.intervalsPx.zip(current.intervalsPx).withIndex().filter { (_, values)
                            ->
                            values.first != values.second
                        }
                    if (changed.isEmpty())
                        P {
                            Text(
                                "Die Einzelabstände sind unverändert. Das allein sagt nicht, ob passende aufeinanderfolgende Maxima gewählt wurden."
                            )
                        }
                    else {
                        P { Text("Veränderte benachbarte Abstände nach Reihenfolge · A → B:") }
                        Ul(
                            attrs = {
                                measurementComparisonStyles(MeasurementComparisonClass.CHANGES)
                            }
                        ) {
                            changed.forEach { (index, values) ->
                                Li {
                                    Text(
                                        "${index + 1}–${index + 2}: ${values.first} px → ${values.second} px"
                                    )
                                }
                            }
                        }
                        if (
                            saved.markers.first() == measurement.markers.first() &&
                                saved.markers.last() == measurement.markers.last()
                        )
                            P {
                                Text(
                                    "Anzahl und Endpunkte sind gleich geblieben: Innere Verschiebungen verändern Nachbarabstände gegensinnig und lassen dieses arithmetische Mittel unverändert. Das ist kein Gütesiegel für die Markensetzung."
                                )
                            }
                        if (
                            saved.markers.dropLast(1) == measurement.markers.dropLast(1) &&
                                measurement.markers.last().xPx - saved.markers.last().xPx == 1
                        )
                            P {
                                Text(
                                    "Nur die letzte Marke wurde um einen Pixel nach rechts verschoben. Der zusätzliche Pixel vergrößert nur den letzten Einzelabstand; im Mittel verteilt er sich auf ${current.intervalsPx.size} benachbarte Abstände. Genau deshalb ist die Mittelwertänderung ein Pixel geteilt durch diese Anzahl."
                                )
                            }
                    }
                } else
                    P {
                        Text(
                            "A und B enthalten unterschiedlich viele Marken. Die Einzelabstände sind deshalb nicht automatisch paarweise dieselben Intervalle; vergleiche ihre zugehörigen Koordinaten."
                        )
                    }
            }
        }
        P {
            Text(
                "Es handelt sich um eigene heutige Bildentscheidungen an einer Reproduktion. Band- und Markenvariationen prüfen die Methodenempfindlichkeit; sie sind keine historischen Wiederholungen oder belegten Fehlergrenzen. Aus Pixeln folgt ohne unabhängigen Bildmaßstab keine absolute Wellenlänge; Grauwerte sind keine kalibrierte Elektronenintensität."
            )
        }
    }
}
