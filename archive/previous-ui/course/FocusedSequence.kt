package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.data.LabAsset
import materiewelle.presentation.styles.*
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/** One learning action at a time; every phase remains freely reachable and locally retained. */
@Composable
fun FocusedSequence(
    id: StationId,
    session: LabSession,
    data: ScientificData?,
    navigate: (AppRoute) -> Unit,
) {
    require(id.usesLearningPhases)
    val work = session.snapshot.stationWork(id)
    val phase = work.learningPhase
    var phaseRequest by remember(id) { mutableStateOf(0) }
    fun choose(next: LearningPhase) {
        if (next == phase) return
        session.editWork(id) { it.copy(learningPhase = next) }
        phaseRequest++
    }
    DisposableEffect(id, phaseRequest) {
        val cancel = if (phaseRequest > 0) requestPanelFocus(Id.FOCUS_PANEL) else ({})
        onDispose { cancel() }
    }
    Section(
        attrs = {
            elementId(Id.FOCUS_LEARNING)
            styles(LabClass.FOCUS_SEQUENCE)
            data(LabData.LEARNING_PHASE, phase)
        }
    ) {
        Div(
            attrs = {
                styles(LabClass.FOCUS_PHASES)
                role(AriaRole.GROUP)
                ariaLabel("Lernschritte frei auswählen")
            }
        ) {
            LearningPhase.entries.forEach { candidate ->
                Button(
                    attrs = {
                        elementId(ElementId.LearningPhaseButton(candidate))
                        styles(LabClass.SECONDARY)
                        ariaPressed(phase == candidate)
                        onClick { choose(candidate) }
                    }
                ) {
                    Text("${candidate.ordinal + 1} · ${candidate.label}")
                }
            }
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Eine Frage pro Phase. Du kannst jederzeit zurückgehen; Vorhersage, Rechnung und eigene Marken bleiben erhalten."
            )
        }
        key(phase) {
            Div(attrs = {
                elementId(Id.FOCUS_PANEL)
                styles(LabClass.FOCUS_PANEL, LabClass.REVEAL_ENTER)
                focusIndex(-1)
                role(AriaRole.REGION)
                ariaLabel("Lernphase: ${phase.label}")
            }) {
                when (phase) {
                    LearningPhase.PREDICT -> {
                        H2 {
                            Text(
                                if (id == StationId.S05_DERIVATION) "Erst eine Vermutung festhalten"
                                else "Bildgröße oder Bildkoordinate?"
                            )
                        }
                        if (id == StationId.S14_MEASUREMENT) OwnImagePreview(session, data)
                        P {
                            Text(
                                if (id == StationId.S05_DERIVATION)
                                    "Ein Elektron startet aus Ruhe. Die Beschleunigungsspannung steigt von 150 V auf 600 V. Wird seine Wellenlänge größer oder kleiner? Um welchen Faktor – und warum?"
                                else
                                    "Du betrachtest dieselbe Aufnahme bei 100 % und 200 %. Was ändert sich am Abstand auf dem Bildschirm, was an den nativen Pixelkoordinaten? Was könnte man aus diesen Pixeln allein bestimmen?"
                            )
                        }
                        FieldLabel(Id.FOCUS_PREDICTION) {
                            Text("Deine Vorhersage vor der Untersuchung")
                        }
                        TextArea(
                            value = work.prediction,
                            attrs = {
                                elementId(Id.FOCUS_PREDICTION)
                                styles(LabClass.COURSE_NOTE)
                                textRows(3)
                                maxLength(1000)
                                onInput { event ->
                                    session.editWork(id) {
                                        it.copy(prediction = event.value.take(1000))
                                    }
                                }
                            },
                        )
                        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                            Text(
                                "Eine begründete Vermutung darf falsch sein. Sie wird lokal gespeichert und später deiner Erklärung gegenübergestellt; freie Antworten werden nicht automatisch bewertet."
                            )
                        }
                    }
                    LearningPhase.EXPLORE ->
                        if (id == StationId.S05_DERIVATION) {
                            ProgressiveDerivation(id, session)
                            VoltageComparison(session)
                        } else {
                            H2 { Text("Eine eigene Auswahl am Original treffen") }
                            OwnImagePreview(session, data)
                            P {
                                Text(
                                    "Öffne die Messwerkstatt: Band wählen und begründen, benachbarte Maxima selbst markieren, erste Auswahl sichern. Erst danach die dokumentierte Referenz zum Vergleich einblenden."
                                )
                            }
                            MeasurementReadout(session, data)
                            P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
                                Text(
                                    "Das Grauwertprofil beschreibt Druck und Scan dieser Reproduktion. Es ist keine kalibrierte Elektronenintensität. Referenzmarken sind eine heutige dokumentierte Auswertung, keine historischen Rohdaten."
                                )
                            }
                        }
                    LearningPhase.EVALUATE -> {
                        H2 { Text("Vorhersage und Ergebnis abgleichen") }
                        PredictionRecall(work.prediction)
                        if (id == StationId.S05_DERIVATION) {
                            VoltageComparison(session)
                            P {
                                Text(
                                    "Bei 600 V statt 150 V führt die Kette 4-fache Energie → 2-facher Impuls → halbe Wellenlänge. Die Reglerkurve setzt λ = h/p voraus und bestätigt diese Hypothese nicht unabhängig."
                                )
                            }
                            InlineFormula(FormulaId.SCALING)
                        } else {
                            MeasurementReadout(session, data)
                            if (data?.jonsson != null) PixelSensitivityTool(session)
                        }
                    }
                    LearningPhase.EXPLAIN -> {
                        H2 { Text("Jetzt ohne Vorlage erklären") }
                        PredictionRecall(work.prediction)
                        P {
                            Text(
                                if (id == StationId.S05_DERIVATION)
                                    "Erkläre die drei Brücken: elektrische Arbeit → kinetische Energie, Energie → Impuls und Impuls → Wellenlänge. Welche davon führt die neue Hypothese ein? Was ändert Anfangsenergie E_kin,0 > 0?"
                                else
                                    "Erkläre deine Band- und Markenwahl, die Anzahl der Intervalle und die Wirkung des Zooms. Trenne den eigenen Pixelbefund von der Referenz und einer Wellenlängenvorhersage. Welche unabhängige Information fehlt für Meter?"
                            )
                        }
                        P {
                            Text(
                                "Beantworte die kurze Verständnisfrage und notiere darunter deine eigene Erklärung. Die vollständige Begründung bleibt bei Bedarf aufklappbar."
                            )
                        }
                        Button(
                            attrs = {
                                elementId(Id.FOCUS_TUTOR)
                                styles(LabClass.SECONDARY)
                                onClick { navigate(AppRoute.Tutor) }
                            }
                        ) {
                            Text("Mit dem eigenen Tutor weiterfragen ↗")
                        }
                    }
                }
            }
        }
        phase.next?.let { next ->
            Button(
                attrs = {
                    elementId(Id.FOCUS_CONTINUE)
                    styles(LabClass.PRIMARY)
                    onClick { choose(next) }
                }
            ) {
                Text("Weiter · ${next.label} →")
            }
        }
    }
}

@Composable
private fun PredictionRecall(prediction: String) {
    Div(attrs = { styles(LabClass.FOCUS_OUTLINE) }) {
        H3 { Text("Deine ursprüngliche Vorhersage") }
        P {
            Text(
                prediction.ifBlank {
                    "Noch keine Vorhersage notiert. Du kannst im ersten Schritt eine eigene Vermutung ergänzen."
                }
            )
        }
    }
}

@Composable
private fun VoltageComparison(session: LabSession) {
    val reference = Voltage(150.0)
    val changed = Voltage(session.snapshot.deBroglie.lastValidVoltage)
    val scaling = VoltageScaling(reference, changed)
    H3 { Text("Die Spannung verändern, den Rechenweg prüfen") }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "Nichtrelativistisches Rechenmodell, Start aus Ruhe, keine Energieverluste. A bleibt bei 150 V; B teilt die Spannung mit dem freien de-Broglie-Labor. Keine Messpunkte."
        )
    }
    FieldLabel(Id.FOCUS_VOLTAGE) { Text("Spannung B: ${Physics.decimal(changed.volts, 2)} V") }
    Input(
        InputType.Range,
        attrs = {
            elementId(Id.FOCUS_VOLTAGE)
            minimum(20)
            maximum(1000)
            continuousStep()
            value(changed.volts)
            onInput { event ->
                event.value
                    ?.toDouble()
                    ?.takeIf { it.isFinite() && it in 20.0..1000.0 }
                    ?.let { voltage ->
                        session.update {
                            it.copy(deBroglie = it.deBroglie.edit(voltage.toString()))
                        }
                    }
            }
        },
    )
    Button(
        attrs = {
            elementId(Id.FOCUS_FOURFOLD)
            styles(LabClass.SECONDARY)
            onClick { session.update { it.copy(deBroglie = it.deBroglie.edit("600")) } }
        }
    ) {
        Text("Vierfache Spannung · 600 V")
    }
    Div(
        attrs = {
            elementId(Id.FOCUS_SCALING)
            styles(LabClass.FOCUS_COMPARISON)
            data(LabData.ENERGY_FACTOR, scaling.energyFactor)
            data(LabData.MOMENTUM_FACTOR, scaling.momentumFactor)
            data(LabData.WAVELENGTH_FACTOR, scaling.wavelengthFactor)
        }
    ) {
        ScalingMetric("Energie E_kin", scaling.energyFactor, LabClass.SEMANTIC_ENERGY)
        ScalingMetric("Impuls p", scaling.momentumFactor, LabClass.SEMANTIC_MOMENTUM)
        ScalingMetric("Wellenlänge λ", scaling.wavelengthFactor, LabClass.SEMANTIC_WAVE)
    }
    listOf("A · 150 V" to 1.0, "B · ${Physics.decimal(changed.volts, 2)} V" to scaling.wavelengthFactor).forEach {
        (label, factor) ->
        P(attrs = { styles(LabClass.SMALL) }) { Text("$label · Verhältnis zu A: × ${Physics.decimal(factor, 3)}") }
        Div(
            attrs = {
                styles(LabClass.SCALING_TRACK)
                ariaHidden(true)
            }
        ) {
            Div(
                attrs = {
                    styles(LabClass.SCALING_BAR)
                    style { width((100 * factor / maxOf(1.0, scaling.wavelengthFactor)).percent) }
                }
            )
        }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "Balkenlängen vergleichen berechnete Wellenlängenverhältnisse. Sie zeigen weder Elektronengrößen noch Flugbahnen. Den absoluten 150-V-Wert berechnest du in Station 6 selbst."
        )
    }
}

@Composable
private fun ScalingMetric(label: String, factor: Double, role: LabClass) {
    Div(attrs = { styles(role) }) {
        P { Text(label) }
        Span(attrs = { styles(LabClass.RESULT_VALUE) }) { Text("× ${Physics.decimal(factor, 3)}") }
        P { Text("B / A") }
    }
}

@Composable
private fun MeasurementReadout(session: LabSession, data: ScientificData?) {
    val measurement = session.snapshot.measurement
    P(
        attrs = {
            elementId(Id.COURSE_SNAPSHOT)
            styles(LabClass.COURSE_RESULT)
        }
    ) {
        Text(
            if (data?.jonsson == null)
                "Das zugehörige Bild wird geprüft; noch keine Messung auswerten."
            else
                when (val selection = measurement.selection) {
                    MeasurementSelection.Empty ->
                        "Noch keine eigenen Marken. Beginne mit einer begründeten Auswahl im Labor."
                    is MeasurementSelection.Partial ->
                        "Eine eigene Marke; ein Abstand benötigt mindestens zwei verschiedene Marken."
                    is MeasurementSelection.Ready ->
                        "${measurement.markers.size} eigene Marken · ${selection.intervalsPx.size} Intervalle · Mittel ${Physics.decimal(selection.meanPx, 3)} native px."
                }
        )
    }
    if (data?.jonsson != null) {
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Band: ${measurement.band.label} · erste Auswahl ${if (measurement.initialSelection == null) "noch nicht gesichert" else "gesichert"} · Laborreferenz ${if (measurement.referenceViewed) "eingeblendet" else "noch nicht eingeblendet"}."
            )
        }
    }
    if (measurement.referenceMaterialsOpened.isNotEmpty())
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Protokollierte Materialkontakte: ${measurement.referenceMaterialsOpened.joinToString { it.label }}. Ein angeforderter Zugriff belegt keinen erfolgreichen Download und kein Lesen; frühere Kontakte bleiben bei der gesicherten Erstwahl vermerkt."
            )
        }
    if (measurement.justification.isNotBlank() && !measurement.justificationMatchesBand)
        P(attrs = { styles(LabClass.SOURCE_WARNING) }) {
            Text(
                "Deine Begründung ist dem aktuellen Band noch nicht zugeordnet. Prüfe sie im Messlabor erneut; die bisherigen Texte und Marken bleiben erhalten."
            )
        }
}

@Composable
private fun OwnImagePreview(session: LabSession, data: ScientificData?) {
    val image = data?.jonsson
    if (image == null) {
        P { Text("Originalbild wird geprüft …") }
        return
    }
    val band = session.snapshot.measurement.nativeBand(image)
    SvgTag(
        SvgTagName.SVG,
        attrs = {
            elementId(Id.FOCUS_PREVIEW)
            styles(LabClass.PIXEL_PREVIEW)
            viewBox(0, 0, image.width, image.height)
            role(AriaRole.IMAGE)
            ariaLabel(
                "Unveränderte Jönsson-Reproduktion mit den eigenen nativen Marken. Links Einzelspalt, rechts Doppelspalt. Keine Referenzkoordinaten."
            )
        },
    ) {
        SvgTag(
            SvgTagName.IMAGE,
            attrs = {
                svgHref(LabAsset.ORIGINAL_IMAGE.path)
                svgNumber(SvgNumber.WIDTH, image.width)
                svgNumber(SvgNumber.HEIGHT, image.height)
            },
        )
        session.snapshot.measurement.markers.forEach { mark ->
            SvgTag(
                SvgTagName.LINE,
                attrs = {
                    svgNumber(SvgNumber.X1, mark.xPx)
                    svgNumber(SvgNumber.X2, mark.xPx)
                    svgNumber(SvgNumber.Y1, band.topInclusive)
                    svgNumber(SvgNumber.Y2, band.bottomExclusive)
                    styles(LabClass.NATIVE_OWN)
                },
            )
        }
    }
    P(attrs = { styles(LabClass.GRAPH_CAPTION) }) {
        Text(
            "J1 · Übersetzung 1974, Abb. 7 links / Abb. 8 rechts. Originalraster ${image.width} × ${image.height} px; Linien zeigen ausschließlich deine eigenen Marken, kein absolutes Längenmaß."
        )
    }
}

@Composable
private fun PixelSensitivityTool(session: LabSession) {
    var target by remember { mutableStateOf(MarkerPerturbation.INNER) }
    var offset by remember { mutableStateOf(1) }
    val image = session.snapshot.imageIdentity ?: return
    val measurement = session.snapshot.measurement
    H3 { Text("Was bewirkt ein Pixel? Ein Gedankenversuch") }
    P {
        Text(
            "Verschiebe eine Marke nur in einer Rechenkopie deiner aktuellen Auswahl. Die tatsächlichen Marken und deine gesicherte erste Auswahl werden dabei nicht verändert."
        )
    }
    FieldLabel(Id.FOCUS_PERTURBATION) { Text("Welche Marke wird in der Kopie verschoben?") }
    Select(
        attrs = {
            elementId(Id.FOCUS_PERTURBATION)
            onChange { event ->
                MarkerPerturbation.entries
                    .firstOrNull { it.name == event.value }
                    ?.let { target = it }
            }
        }
    ) {
        MarkerPerturbation.entries.forEach { item ->
            Option(value = item.name, attrs = { if (item == target) selected() }) {
                Text(item.label)
            }
        }
    }
    FieldLabel(Id.FOCUS_OFFSET) { Text("Verschiebung der Kopie: $offset native px") }
    Input(
        InputType.Range,
        attrs = {
            elementId(Id.FOCUS_OFFSET)
            minimum(-1)
            maximum(1)
            increment(1)
            value(offset)
            onInput { event -> event.value?.toInt()?.takeIf { it in -1..1 }?.let { offset = it } }
        },
    )
    when (val result = nativePixelSensitivity(measurement.markers, image, target, offset)) {
        is PixelSensitivity.Unavailable ->
            P(
                attrs = {
                    elementId(Id.FOCUS_SENSITIVITY)
                    role(AriaRole.STATUS)
                }
            ) {
                Text(result.reason)
            }
        is PixelSensitivity.Ready ->
            Div(
                attrs = {
                    elementId(Id.FOCUS_SENSITIVITY)
                    styles(LabClass.COURSE_RESULT)
                    role(AriaRole.STATUS)
                    data(LabData.MEAN_CHANGE_PX, result.meanChangePx)
                }
            ) {
                P { Text("Intervalle vorher: ${result.originalIntervals.joinToString(", ")} px") }
                P { Text("Rechenkopie: ${result.changedIntervals.joinToString(", ")} px") }
                P {
                    Text(
                        "Mittel: ${Physics.decimal(result.originalMeanPx, 4)} → ${Physics.decimal(result.changedMeanPx, 4)} px; Änderung ${Physics.decimal(result.meanChangePx, 4)} px."
                    )
                }
                P {
                    Text(
                        if (target == MarkerPerturbation.INNER)
                            "Zwei Nachbarabstände ändern sich entgegengesetzt. Gesamtspanne und Mittelwert bleiben gleich; die Regelmäßigkeit kann sich ändern."
                        else
                            "Eine Endmarke ändert die Gesamtspanne. Die Änderung des Mittelwerts ist die Verschiebung geteilt durch die Anzahl der Intervalle."
                    )
                }
            }
    }
    P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
        Text(
            "Das zeigt Empfindlichkeit gegenüber einer Auswerteentscheidung. Es liefert weder eine historische Messunsicherheit noch die fehlende physikalische Bildkalibrierung. Für eine absolute Wellenlänge braucht man zusätzlich einen unabhängigen Maßstab und eine gesicherte Optik-/Parameterzuordnung."
        )
    }
}
