package materiewelle.pages

import androidx.compose.runtime.*
import materiewelle.core.syntheticMeasurementHeight
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private enum class CandidateShape(val label: String, val id: MeasurementCandidateId) {
    PEAK("Gipfelmitte", MeasurementCandidateId.KIND_PEAK),
    FLANK("Flanke", MeasurementCandidateId.KIND_FLANK),
    PLATEAU("Plateau", MeasurementCandidateId.KIND_PLATEAU),
}

private enum class SyntheticCandidate(
    val label: String,
    val position: Int,
    val shape: CandidateShape,
    val id: MeasurementCandidateId,
    val explanation: String,
) {
    A(
        "A", 14, CandidateShape.FLANK, MeasurementCandidateId.CANDIDATE_A,
        "Hier steigt das Profil nach rechts weiter an. Der rechte Nachbar ist höher: Diese Stelle liegt auf einer Flanke und ist noch kein Maximum. Gehe bis zum Gipfel und prüfe wieder beide Seiten.",
    ),
    B(
        "B", 24, CandidateShape.PEAK, MeasurementCandidateId.CANDIDATE_B,
        "Beide direkten Nachbarn sind niedriger. Dieser einzelne Gipfelpunkt ist im synthetischen Profil ein begründeter Kandidat für die Streifenmitte.",
    ),
    C(
        "C", 61, CandidateShape.PLATEAU, MeasurementCandidateId.CANDIDATE_C,
        "Mehrere benachbarte Stellen sind gleich hoch. Es gibt keinen eindeutig höchsten Einzelpunkt. Begründe zuerst die Grenzen des flachen Bereichs und anschließend seine Mitte; eine scharfe Spitze wäre hier vorgetäuscht.",
    ),
}

private data class CandidateAttempt(
    val shape: CandidateShape? = null,
    val reason: String = "",
    val checked: Boolean = false,
)

/** Local practice only: no MeasurementSession, persistence, reference contact or marker mutation. */
@Composable
internal fun MeasurementCandidateProbe() {
    var selected by remember { mutableStateOf(SyntheticCandidate.A) }
    var attempts by remember { mutableStateOf<Map<SyntheticCandidate, CandidateAttempt>>(emptyMap()) }
    val attempt = attempts[selected] ?: CandidateAttempt()
    fun updateAttempt(change: (CandidateAttempt) -> CandidateAttempt) {
        attempts = attempts + (selected to change(attempts[selected] ?: CandidateAttempt()))
    }
    Details(
        attrs = {
            elementId(MeasurementCandidateId.ROOT)
            styles(LabClass.NOTES)
            stationActivityStyles(StationActivityClass.ROOT)
        }
    ) {
        Summary { Text("Messort prüfen · freiwillige Übungsprobe") }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text(
                "Synthetisches Übungsprofil, kein historisches Foto und keine Fotokalibrierung. Deine Antworten bleiben nur in dieser Ansicht und werden nicht im Messnachweis gespeichert. Die Originalmessung darunter bleibt unverändert erreichbar."
            )
        }
        P { Text("Wähle einen Kandidaten. Ist die markierte Stelle ein einzelner Gipfelpunkt, eine Flanke oder Teil eines flachen Plateaus?") }
        Div(attrs = { styles(LabClass.COURSE_ACTIONS) }) {
            SyntheticCandidate.entries.forEach { candidate ->
                Button(
                    attrs = {
                        elementId(candidate.id)
                        styles(LabClass.SECONDARY)
                        if (selected == candidate) styles(LabClass.CHOSEN)
                        ariaPressed(selected == candidate)
                        onClick { selected = candidate }
                    }
                ) {
                    Text("Kandidat ${candidate.label}")
                }
            }
        }
        Div(attrs = { style { maxWidth(720.px) } }) {
            CandidateProfile(selected)
        }
        P(
            attrs = {
                elementId(MeasurementCandidateId.NEIGHBORS)
                styles(LabClass.SMALL)
            }
        ) {
            Text(
                "Kandidat ${selected.label} · relative Profilhöhe: links ${number(syntheticMeasurementHeight(selected.position - 1), 3)}, am Kandidaten ${number(syntheticMeasurementHeight(selected.position), 3)}, rechts ${number(syntheticMeasurementHeight(selected.position + 1), 3)}."
            )
        }
        Div(
            attrs = {
                styles(LabClass.COURSE_ACTIONS)
                role(AriaRole.GROUP)
                ariaLabel("Form am ausgewählten Kandidaten zuordnen")
            }
        ) {
            CandidateShape.entries.forEach { shape ->
                Button(
                    attrs = {
                        elementId(shape.id)
                        styles(LabClass.SECONDARY)
                        if (attempt.shape == shape) styles(LabClass.CHOSEN)
                        ariaPressed(attempt.shape == shape)
                        onClick { updateAttempt { it.copy(shape = shape, checked = false) } }
                    }
                ) {
                    Text(shape.label)
                }
            }
        }
        Details(attrs = { elementId(MeasurementCandidateId.HINT) }) {
            Summary { Text("Wie kann ich meinen Messort begründen?") }
            P {
                Text(
                    "Vergleiche zunächst links, Mitte und rechts. Steigt das Profil weiter? Sind beide Nachbarn niedriger? Oder bleiben mehrere Stellen gleich hoch? Bei einem flachen Bereich suche zusätzlich dessen beide Ränder."
                )
            }
        }
        FieldLabel(forId = MeasurementCandidateId.REASON) { Text("Eigene Begründung (freiwillig)") }
        TextArea(
            attrs = {
                elementId(MeasurementCandidateId.REASON)
                value(attempt.reason)
                onInput { event -> updateAttempt { it.copy(reason = event.value.take(1000)) } }
            }
        )
        Button(
            attrs = {
                elementId(MeasurementCandidateId.CHECK)
                styles(LabClass.SECONDARY)
                if (attempt.shape == null) disabled()
                onClick { updateAttempt { it.copy(checked = true) } }
            }
        ) {
            Text("Zuordnung prüfen")
        }
        if (attempt.checked) {
            Div(
                attrs = {
                    elementId(MeasurementCandidateId.FEEDBACK)
                    styles(LabClass.COURSE_RESULT)
                    role(AriaRole.STATUS)
                }
            ) {
                P {
                    Text(
                        if (attempt.shape == selected.shape) "Die Zuordnung passt zum Übungsprofil."
                        else "Vergleiche deine Zuordnung noch einmal mit dem Verlauf an dieser Stelle."
                    )
                }
                P { Text(selected.explanation) }
                P {
                    Text(
                        "Dein Begründungssatz wird nicht automatisch bewertet. Auch regelmäßig beabstandete Marken oder ein passender Mittelwert bestätigen die einzelnen Messorte noch nicht. Prüfe anschließend deine eigenen Kandidaten im Originalbild und im zugehörigen Profil."
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateProfile(selected: SyntheticCandidate) {
    fun x(position: Int) = 36.0 + position / 80.0 * 568.0
    fun y(height: Double) = 182.0 - 140.0 * height
    val path =
        (0..80).joinToString(" ") { position ->
            "${if (position == 0) "M" else "L"}${x(position)},${y(syntheticMeasurementHeight(position))}"
        }
    ActivitySvg(
        MeasurementCandidateId.PLOT,
        640.0,
        222.0,
        "Berechnetes synthetisches Profil mit den Kandidaten A, B und C; ${selected.label} ist ausgewählt. Relative Profilhöhe und freie Übungsorte, keine historischen Grauwerte. Die drei Nachbarwerte stehen unter der Grafik.",
    ) {
        ActivityText(36.0, 20.0, "Relative Profilhöhe")
        ActivityLine(36.0, 42.0, 36.0, 182.0, StationActivityClass.DRAWING_LINE)
        ActivityLine(36.0, 182.0, 604.0, 182.0, StationActivityClass.DRAWING_LINE)
        ActivityText(25.0, 47.0, "1", TextAnchor.END)
        ActivityText(25.0, 187.0, "0", TextAnchor.END)
        ActivityPath(path, StationActivityClass.DRAWING_TRACE)
        SyntheticCandidate.entries.forEach { candidate ->
            if (candidate == selected) {
                SvgTag(
                    SvgTagName.LINE,
                    attrs = {
                        svgNumber(SvgNumber.X1, x(candidate.position))
                        svgNumber(SvgNumber.X2, x(candidate.position))
                        svgNumber(SvgNumber.Y1, 36)
                        svgNumber(SvgNumber.Y2, 182)
                        styles(LabClass.PROFILE_CURSOR)
                    },
                )
            }
            SvgTag(
                SvgTagName.CIRCLE,
                attrs = {
                    svgNumber(SvgNumber.CX, x(candidate.position))
                    svgNumber(SvgNumber.CY, y(syntheticMeasurementHeight(candidate.position)))
                    svgNumber(SvgNumber.RADIUS, if (candidate == selected) 6 else 4)
                    styles(LabClass.PROFILE_OWN)
                },
            )
            ActivityText(x(candidate.position), 206.0, candidate.label, TextAnchor.MIDDLE)
        }
        ActivityText(604.0, 206.0, "Übungsort", TextAnchor.END)
    }
}
