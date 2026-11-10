package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.NativeSvg
import materiewelle.core.course.GeometryPathChoice
import materiewelle.core.course.StationId
import materiewelle.learning.*
import materiewelle.pages.braggGeometryChart
import materiewelle.pages.nickelSurfaceChart
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

private val courseVoltageDerivation = voltageDerivation

private val braggDerivation =
    listOf(
        DerivationEntry(
            DerivationStage.BRAGG_REFERENCE,
            "Zuerst die Bezugslinien",
            "d steht senkrecht zwischen den Netzebenen. Der Winkel θB wird zur Ebene gemessen, nicht zu ihrer Normalen. Beide Vergleichsstrahlen und ihre Hilfslinien bleiben beim nächsten Schritt sichtbar.",
            emptyList(),
        ),
        DerivationEntry(
            DerivationStage.BRAGG_PATH,
            "Die erste Zusatzstrecke",
            "Der Weg zur tieferen Ebene besitzt auf der Einfallsseite eine zusätzliche Strecke. Die Projektion liefert d sin θB. Der erste Beitrag allein ist noch nicht der gesamte Gangunterschied.",
            listOf(FormulaId.BRAGG_LEG),
        ),
        DerivationEntry(
            DerivationStage.BRAGG_SUM,
            "Beide Zusatzwege zusammennehmen",
            "Auf der Ausfallsseite kommt eine zweite Strecke d sin θB hinzu. Die Summe passt konstruktiv, wenn sie ein ganzzahliges Vielfaches der Wellenlänge ist.",
            listOf(FormulaId.BRAGG),
            "Zwei Beiträge, ein Gangunterschied. Kein Winkel wird ohne Zeichnung halbiert.",
        ),
        DerivationEntry(
            DerivationStage.SURFACE,
            "Jetzt eine getrennte Oberflächengeometrie",
            "Neue Zeichnung, neue Definitionen: D ist der ausgewählte Ni(111)-Oberflächenreihenabstand, φ liegt zur äußeren Normale. Für normal einfallende Beiträge gilt hier D sin φ = nλ. D ist kein Bragg-Netzebenenabstand d.",
            listOf(FormulaId.SURFACE_SPACING, FormulaId.SURFACE),
            "Die Bragg-Begründung bleibt als Karte erhalten und ihre Zeichnung ist erneut anwählbar. Sie wird nicht in diese Geometrie umgedeutet.",
        ),
    )

/** Explicit playback reveals an accumulating prefix; it never substitutes the previous card. */
@Composable
fun ProgressiveDerivation(id: StationId, session: LabSession) {
    require(id == StationId.S05_DERIVATION || id == StationId.S10_GEOMETRY)
    if (id == StationId.S10_GEOMETRY) {
        CompactGeometryDerivation(session)
        return
    }
    val entries = if (id == StationId.S05_DERIVATION) courseVoltageDerivation else braggDerivation
    val step = session.snapshot.stationWork(id).explanationStep.coerceIn(entries.indices)
    val revealed =
        maxOf(step, session.snapshot.stationWork(id).explanationFurthestStep)
            .coerceIn(entries.indices)
    var playing by remember(id) { mutableStateOf(false) }
    var elapsed by remember(id) { mutableStateOf(0.0) }
    val reduced = rememberReducedMotion()
    fun setStep(next: Int) {
        val selected = next.coerceIn(entries.indices)
        session.editWork(id) {
            it.copy(
                explanationStep = selected,
                explanationFurthestStep =
                    maxOf(it.explanationFurthestStep, it.explanationStep, selected),
            )
        }
    }
    val reading =
        rememberDerivationReading(
            DerivationReadingScope.COURSE,
            step,
            entries.lastIndex,
            next = { setStep(revealed + 1) },
            pause = {
                playing = false
                elapsed = 0.0
            },
        )

    TimelineClock(
        playing,
        true,
        reduced,
        { delta ->
            elapsed += delta
            if (elapsed >= 3.2) {
                elapsed = 0.0
                if (step < entries.lastIndex) setStep(step + 1) else playing = false
            }
        },
        { playing = false },
    )

    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "Ein Gedanke pro Schritt. Neue Karten wachsen darunter; die Begründung darüber bleibt stehen."
        )
    }
    FieldLabel(Id.COURSE_STAGE_PROGRESS) {
        Text("Aktive Karte: ${step + 1} · ${revealed + 1} von ${entries.size} aufgedeckt")
    }
    Input(
        InputType.Range,
        attrs = {
            elementId(Id.COURSE_STAGE_PROGRESS)
            minimum(0)
            maximum(entries.lastIndex)
            increment(1)
            value(step)
            onInput { event ->
                event.value
                    ?.toDouble()
                    ?.takeIf { it.isFinite() }
                    ?.let { next ->
                        playing = false
                        elapsed = 0.0
                        setStep(next.toInt())
                    }
            }
        },
    )
    DerivationReadingControls(reading) {
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            Button(
                attrs = {
                    elementId(Id.COURSE_STAGE_PREV)
                    styles(LabClass.SECONDARY)
                    if (step == 0) disabled()
                    onClick {
                        playing = false
                        elapsed = 0.0
                        setStep(step - 1)
                    }
                }
            ) {
                Text("Schritt zurück")
            }
            Button(
                attrs = {
                    elementId(Id.COURSE_STAGE_NEXT)
                    styles(LabClass.PRIMARY)
                    if (step == entries.lastIndex) disabled()
                    onClick {
                        playing = false
                        elapsed = 0.0
                        setStep(step + 1)
                    }
                }
            ) {
                Text(
                    if (id == StationId.S10_GEOMETRY && step == 2) "Oberflächenmodell ergänzen"
                    else "Nächsten Schritt ergänzen"
                )
            }
            Button(
                attrs = {
                    elementId(Id.COURSE_STAGE_PLAY)
                    styles(LabClass.SECONDARY)
                    if (reduced || reading.mode == DerivationReadingMode.SCROLL) disabled()
                    ariaPressed(playing)
                    onClick {
                        if (step == entries.lastIndex) setStep(0)
                        if (!playing) reading.resume()
                        playing = !playing
                        elapsed = 0.0
                    }
                }
            ) {
                Text(
                    if (playing) "Pause"
                    else if (step == entries.lastIndex) "Noch einmal abspielen"
                    else "Schritte abspielen"
                )
            }
        }
    }
    DerivationSequence(entries, revealed, reading, step)
    DerivationReadingFrontier(reading, revealed, entries.lastIndex)
    P(
        attrs = {
            role(AriaRole.STATUS)
            data(LabData.STEP, step)
            styles(LabClass.SMALL, LabClass.MUTED)
        }
    ) {
        Text(
            "Schritt ${step + 1} von ${entries.size} · ${if (reduced) "reduzierte Bewegung: alle Inhalte ohne Animation erreichbar" else if (reading.mode == DerivationReadingMode.SCROLL) "dein Scrolltempo bestimmt den nächsten Schritt" else "zeitgesteuerte Wiedergabe erst nach Abspielen; jederzeit pausierbar"}"
        )
    }
}

private enum class PathChoice(
    val label: String,
    val id: GeometrySlitsId,
    val value: GeometryPathChoice,
) {
    INCOMING(
        "Nur die zusätzliche Strecke beim Einfall",
        GeometrySlitsId.GEOMETRY_CHOICE_INCOMING,
        GeometryPathChoice.INCOMING,
    ),
    OUTGOING(
        "Nur die zusätzliche Strecke beim Ausfall",
        GeometrySlitsId.GEOMETRY_CHOICE_OUTGOING,
        GeometryPathChoice.OUTGOING,
    ),
    BOTH(
        "Zusatzstrecken beim Einfall und beim Ausfall",
        GeometrySlitsId.GEOMETRY_CHOICE_BOTH,
        GeometryPathChoice.BOTH,
    ),
}

/**
 * One large construction; the revealed prefix is retained even when an older drawing is selected.
 */
@Composable
private fun CompactGeometryDerivation(session: LabSession) {
    val id = StationId.S10_GEOMETRY
    val work = session.snapshot.stationWork(id)
    val revealed =
        maxOf(work.explanationStep, work.explanationFurthestStep).coerceIn(braggDerivation.indices)
    val active = work.explanationStep.coerceIn(braggDerivation.indices)
    val choice =
        PathChoice.entries.firstOrNull { it.value == session.snapshot.inquiry.geometryPrediction }
    var playing by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0.0) }
    val reduced = rememberReducedMotion()
    var drawingRequest by remember { mutableStateOf(0) }
    DisposableEffect(drawingRequest) {
        val cancel =
            if (drawingRequest > 0) requestPanelFocus(GeometrySlitsId.GEOMETRY_DRAWING)
            else ({})
        onDispose { cancel() }
    }
    fun select(next: Int) {
        val valid = next.coerceIn(braggDerivation.indices)
        session.editWork(id) {
            it.copy(explanationStep = valid, explanationFurthestStep = maxOf(revealed, valid))
        }
    }
    TimelineClock(
        playing,
        true,
        reduced,
        { delta ->
            elapsed += delta
            if (elapsed >= 4.0) {
                elapsed = 0.0
                if (active < braggDerivation.lastIndex) select(active + 1) else playing = false
            }
        },
        { playing = false },
    )
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "Eine aktive Zeichnung. Geöffnete Karten bleiben darunter erhalten; ihre Schaltflächen holen den jeweiligen Schritt zurück."
        )
    }
    if (revealed < 2) {
        Div(
            attrs = {
                geometryStyles(GeometrySlitsClass.CHOICES)
                role(AriaRole.GROUP)
                ariaLabel("Bragg-Vorhersage vor der vollständigen Wegsumme")
            }
        ) {
            P {
                Text(
                    "Bragg-Vorhersage vor der Kontrolle: Welche zusätzlichen Strecken zählen beim Vergleich des oberen mit dem unteren Strahl? Deine Auswahl bleibt als Denkversuch zur Bragg-Konstruktion stehen."
                )
            }
            PathChoice.entries.forEach { candidate ->
                Button(
                    attrs = {
                        elementId(candidate.id)
                        styles(LabClass.SECONDARY)
                        ariaPressed(choice == candidate)
                        if (choice == candidate) geometryStyles(GeometrySlitsClass.ACTIVE)
                        onClick {
                            session.editInquiry { it.copy(geometryPrediction = candidate.value) }
                        }
                    }
                ) {
                    Text(candidate.label)
                }
            }
        }
    }
    Div(
        attrs = {
            elementId(GeometrySlitsId.GEOMETRY_DRAWING)
            geometryStyles(GeometrySlitsClass.FRAME, GeometrySlitsClass.FIGURE)
            focusIndex(0)
            ariaLabel(
                "Aktive geometrische Konstruktion ${active + 1}; bei Bedarf horizontal verschiebbar"
            )
        }
    ) {
        NativeSvg(if (active < 3) braggGeometryChart(active) else nickelSurfaceChart(50.0, .352))
    }
    P(
        attrs = {
            role(AriaRole.STATUS)
            styles(LabClass.SMALL)
            data(LabData.STEP, active)
        }
    ) {
        Text("Zeichnung ${active + 1}: ${braggDerivation[active].title}")
    }
    if (active == 3) {
        P(
            attrs = {
                elementId(LearningFlowId.SURFACE_EXPLANATION)
                learningFlowStyles(LearningFlowClass.SURFACE_EXPLANATION)
            }
        ) {
            Text(
                "Oberflächenmodell bei normalem Einfall: Auf dem einlaufenden Weg entsteht zwischen den betrachteten Reihen kein Gangunterschied. Erst die auslaufenden Beiträge liefern D sin φ. Die gespeicherte Bragg-Vorhersage unten gehört zu Karte 3 und wurde nicht für diese Oberflächengeometrie geprüft."
            )
        }
    }
    FieldLabel(Id.COURSE_STAGE_PROGRESS) {
        Text("Zeichnung auswählen · ${active + 1} von ${braggDerivation.size}")
    }
    Input(
        InputType.Range,
        attrs = {
            elementId(Id.COURSE_STAGE_PROGRESS)
            minimum(0)
            maximum(braggDerivation.lastIndex)
            increment(1)
            value(active)
            onInput { event ->
                event.value
                    ?.toDouble()
                    ?.takeIf { it.isFinite() }
                    ?.let { next ->
                        playing = false
                        elapsed = 0.0
                        select(next.toInt())
                    }
            }
        },
    )
    Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
        Button(
            attrs = {
                elementId(Id.COURSE_STAGE_PREV)
                styles(LabClass.SECONDARY)
                if (active == 0) disabled()
                onClick {
                    playing = false
                    elapsed = 0.0
                    select(active - 1)
                }
            }
        ) {
            Text("Schritt zurück")
        }
        Button(
            attrs = {
                elementId(Id.COURSE_STAGE_NEXT)
                styles(LabClass.PRIMARY)
                if (active == braggDerivation.lastIndex) disabled()
                onClick {
                    playing = false
                    elapsed = 0.0
                    select(active + 1)
                }
            }
        ) {
            Text(if (active == 2) "Oberflächenmodell ergänzen" else "Nächsten Schritt ergänzen")
        }
        Button(
            attrs = {
                elementId(Id.COURSE_STAGE_PLAY)
                styles(LabClass.SECONDARY)
                if (reduced) disabled()
                ariaPressed(playing)
                onClick {
                    if (active == braggDerivation.lastIndex) select(0)
                    playing = !playing
                    elapsed = 0.0
                }
            }
        ) {
            Text(
                if (playing) "Pause"
                else if (active == braggDerivation.lastIndex) "Noch einmal abspielen"
                else "Schritte abspielen"
            )
        }
    }
    Div(
        attrs = {
            geometryStyles(GeometrySlitsClass.RETAINED)
            learningFlowStyles(LearningFlowClass.COMPACT_RETAINED)
        }
    ) {
        braggDerivation.take(revealed + 1).forEachIndexed { index, entry ->
            Section(
                attrs = {
                    styles(
                        LabClass.DERIVATION_CARD,
                        LabClass.EQUATION_STEP,
                        if (active == index) LabClass.CURRENT else LabClass.COMPLETE,
                    )
                    geometryStyles(GeometrySlitsClass.CARD)
                    learningFlowStyles(LearningFlowClass.COMPACT_CARD)
                    if (active == index) geometryStyles(GeometrySlitsClass.ACTIVE)
                    data(LabData.EQUATION, index)
                    ariaLabel("Erhaltener Herleitungsschritt ${index + 1}: ${entry.title}")
                }
            ) {
                H3 { Text("${index + 1} · ${entry.title}") }
                P { Text(entry.explanation) }
                entry.formulas.forEach { formula ->
                    Div(attrs = { styles(LabClass.FORMULA) }) { InlineFormula(formula) }
                }
                entry.note?.let {
                    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text(it) }
                }
                if (index == 2) {
                    Div(attrs = { learningFlowStyles(LearningFlowClass.BRAGG_FEEDBACK) }) {
                        H4(attrs = { elementId(LearningFlowId.BRAGG_FEEDBACK_TITLE) }) {
                            Text("Rückmeldung zur Bragg-Vorhersage · Karte 3")
                        }
                        P(
                            attrs = {
                                elementId(GeometrySlitsId.GEOMETRY_CHOICE_FEEDBACK)
                                labelledBy(LearningFlowId.BRAGG_FEEDBACK_TITLE)
                                styles(LabClass.SMALL)
                            }
                        ) {
                            Text(
                                when (choice) {
                                    PathChoice.BOTH ->
                                        "Deine Bragg-Vorhersage passt: In der Bragg-Konstruktion kommen beim unteren Strahl auf Einfalls- und Ausfallsseite je d sin θB hinzu. Diese Rückmeldung bezieht sich auf die Wegsumme in Bragg-Zeichnung 3."
                                    PathChoice.INCOMING ->
                                        "Deine Bragg-Vorhersage erfasst den Einfall. In der Bragg-Konstruktion trägt zusätzlich die Ausfallsseite d sin θB bei; beide Projektionen stehen in Bragg-Zeichnung 3."
                                    PathChoice.OUTGOING ->
                                        "Deine Bragg-Vorhersage erfasst den Ausfall. In der Bragg-Konstruktion trägt zusätzlich die Einfallsseite d sin θB bei; beide Projektionen stehen in Bragg-Zeichnung 3."
                                    null ->
                                        "Bragg-Erklärung ohne vorher festgehaltene Auswahl: In Bragg-Zeichnung 3 liefern Einfalls- und Ausfallsseite je einen Zusatzweg. Das ist eine geöffnete Bragg-Begründung, kein geprüfter Erstversuch und keine Antwort zur Oberfläche."
                                }
                            )
                        }
                    }
                }
                val identity =
                    listOf(
                        GeometrySlitsId.GEOMETRY_REFERENCE,
                        GeometrySlitsId.GEOMETRY_PATH,
                        GeometrySlitsId.GEOMETRY_SUM,
                        GeometrySlitsId.GEOMETRY_SURFACE,
                    )[index]
                Button(
                    attrs = {
                        elementId(identity)
                        styles(LabClass.SECONDARY)
                        ariaPressed(active == index)
                        onClick {
                            playing = false
                            select(index)
                            drawingRequest++
                        }
                    }
                ) {
                    Text("Zeichnung ${index + 1} anzeigen")
                }
            }
        }
    }
    if (revealed == braggDerivation.lastIndex) {
        Details(
            attrs = {
                elementId(LearningFlowId.GEOMETRY_COMPARISON)
                styles(LabClass.NOTES)
                learningFlowStyles(LearningFlowClass.GEOMETRY_COMPARISON)
            }
        ) {
            Summary { Text("Bei Bedarf vergleichen · Bragg und Oberfläche") }
            Div(attrs = { geometryStyles(GeometrySlitsClass.COMPARISON) }) {
                Section {
                    H3 { Text("Bragg · parallele Netzebenen") }
                    P {
                        Text(
                            "Abstand d senkrecht zwischen Ebenen; θᴮ zur Ebene. Einfall und Ausfall liefern je d sin θᴮ."
                        )
                    }
                    P { InlineFormula(FormulaId.BRAGG) }
                }
                Section {
                    H3 { Text("Normaler Einfall · Oberflächenreihen") }
                    P {
                        Text(
                            "Abstand D innerhalb der Oberfläche; φ zur äußeren Normalen. Einlaufend kein Unterschied, auslaufend D sin φ."
                        )
                    }
                    P { InlineFormula(FormulaId.SURFACE) }
                }
            }
        }
    }
    P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
        Text(
            "${revealed + 1} geöffnete Karten bleiben erhalten. ${if (reduced) "Reduzierte Bewegung: manuelle Auswahl vollständig nutzbar." else "Automatischer Ablauf nur nach Abspielen; jederzeit pausierbar."} Die Zeichnungen sind Lehrmodelle, keine historischen Intensitätsmessungen."
        )
    }
}
