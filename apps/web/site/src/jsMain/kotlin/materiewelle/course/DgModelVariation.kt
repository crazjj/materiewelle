package materiewelle.course

import androidx.compose.runtime.Composable
import materiewelle.NativeSvg
import materiewelle.detectorChart
import materiewelle.core.*
import materiewelle.pages.number
import materiewelle.state.LabSession
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

@Composable
internal fun DgModelVariation(session: LabSession, data: ScientificData) {
    val model = session.snapshot.dg
    val reported = data.points.first()
    val point = DgSetting(Voltage(model.validModelVoltage), SurfaceNormalAngle(model.validModelAngle))
    val predicted = Physics.electronWavelength(point.voltage).nanometers
    val geometric = Physics.surfaceDiffraction(data.spacing, point.surfaceAngle).nanometers
    val difference = Physics.signedRelativeDifferencePercent(geometric, predicted)
    val sourceCase = point.voltageV == reported.voltageV && point.angleDegrees == reported.angleDegrees
    val validVoltage = dgArticleVoltage(model.modelVoltageRaw, data) != null
    val validAngle = dgArticleAngle(model.modelAngleRaw) != null
    Div(attrs = { articleStyles(ArticleClass.MODEL) }) {
        Div {
            FieldLabel(DgArticleId.VOLTAGE) { Text("Modellspannung in Volt") }
            Input(InputType.Text, attrs = {
                elementId(DgArticleId.VOLTAGE); protectPersonalWork(session.isReadOnly); value(model.modelVoltageRaw); maxLength(120); inputMode(LabInputMode.DECIMAL); ariaInvalid(!validVoltage)
                onInput { event ->
                    session.update { snapshot ->
                        snapshot.copy(
                            dg = snapshot.dg.copy(
                                modelVoltageRaw = event.value.take(120),
                                validModelVoltage = dgArticleVoltage(event.value, data)
                                    ?: snapshot.dg.validModelVoltage,
                            ),
                        )
                    }
                }
            })
            FieldLabel(DgArticleId.ANGLE) { Text("Detektorwinkel zur äußeren Normalen in Grad") }
            Input(InputType.Text, attrs = {
                elementId(DgArticleId.ANGLE); protectPersonalWork(session.isReadOnly); value(model.modelAngleRaw); maxLength(120); inputMode(LabInputMode.DECIMAL); ariaInvalid(!validAngle)
                onInput { event ->
                    session.update { snapshot ->
                        snapshot.copy(
                            dg = snapshot.dg.copy(
                                modelAngleRaw = event.value.take(120),
                                validModelAngle = dgArticleAngle(event.value)
                                    ?: snapshot.dg.validModelAngle,
                            ),
                        )
                    }
                }
            })
            if (!validVoltage || !validAngle) P(attrs = { styles(LabClass.ERROR); role(AriaRole.ALERT) }) {
                if (!validVoltage) Text("Spannung: Gib einen Wert von 20 bis 1000 Volt ein. ")
                if (!validAngle) Text("Detektorwinkel: Gib einen Wert von 0,1 bis 90 Grad ein. ")
                Text("Grafik und Ergebnisse zeigen noch die letzten gültigen Werte.")
            }
        }
        Div(attrs = { elementId(DgArticleId.GEOMETRIC); data(LabData.VALUE_NM, geometric) }) {
            H3 { Text("Aus der Beugungsgeometrie") }
            InlineFormula(FormulaId.SURFACE)
            P { MathText("≈ ${number(geometric, 3)} nm") }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Aus dem Reihenabstand der Nickelstruktur und dem Winkel zur Normalen.") }
        }
        Div(attrs = { elementId(DgArticleId.PREDICTED); data(LabData.VALUE_NM, predicted) }) {
            H3 { Text("Aus de Broglies Hypothese") }
            InlineFormula(FormulaId.WAVELENGTH_VOLTAGE)
            P { MathText("≈ ${number(predicted, 3)} nm") }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Aus der Beschleunigungsspannung, ohne den Streuwinkel einzusetzen.") }
        }
    }
    P(attrs = { articleStyles(ArticleClass.SOURCE) }) { Text(if (sourceCase) "Quellenfall D2: 54 eV, ungefähr 50°. Beide Rechenwerte bleiben Modellwerte aus den bezeichneten Eingaben." else "Eigene Modellvariation; kein zusätzlicher historischer Messpunkt.") }
    if (predicted > data.spacing.nanometers) P(attrs = {
        elementId(DgArticleId.MODEL_LIMIT); styles(LabClass.SOURCE_WARNING); role(AriaRole.STATUS)
    }) {
        Text("Bei dieser Spannung ist "); MathText("λdB > D")
        Text(". Im gewählten Oberflächenmodell gibt es dann keine reelle Richtung der ersten Ordnung. Eine andere Detektorrichtung kann diese Modellgrenze nicht aufheben.")
    }
    NativeSvg(detectorChart(point.angleDegrees, model.comparisonAngle))
    P(attrs = { elementId(DgArticleId.DIFFERENCE); data(LabData.VALUE_PERCENT, difference) }) {
        Text("Rechnerische Differenz bezogen auf de Broglies Vorhersage: ")
        MathText("${number(difference, 1)} %")
        Text(". Das Vorzeichen gehört zu geometrischem Wert minus Vorhersage.")
    }
    Details {
        Summary { Text("Rechenwerte mit zusätzlichen Stellen") }
        P { Text("Geometrisch: "); MathText("${number(geometric, 6)} nm"); Text("; de Broglie: "); MathText("${number(predicted, 6)} nm"); Text(". Diese Stellen dienen der Rechenkontrolle und sind keine belegte historische Messgenauigkeit. Bezugswert der relativen Differenz ist die de-Broglie-Vorhersage.") }
    }
    Div(attrs = { articleStyles(ArticleClass.ACTIONS) }) {
        Button(attrs = {
            elementId(DgArticleId.RESET)
            protectPersonalWork(session.isReadOnly)
            styles(LabClass.SECONDARY)
            onClick {
                session.update { snapshot ->
                    snapshot.copy(
                        dg = snapshot.dg.copy(
                            selectedId = reported.id,
                            modelVoltageRaw = reported.voltageV.toString(),
                            modelAngleRaw = reported.angleDegrees.toString(),
                            validModelVoltage = reported.voltageV,
                            validModelAngle = reported.angleDegrees,
                        ),
                    )
                }
            }
        }) { MathText("${number(reported.voltageV, 0)} V"); Text(" und "); MathText("${number(reported.angleDegrees, 0)}°"); Text(" einsetzen") }
        Button(attrs = {
            elementId(DgArticleId.SAVE)
            protectPersonalWork(session.isReadOnly)
            styles(LabClass.SECONDARY)
            onClick {
                session.update { snapshot ->
                    snapshot.copy(
                        dg = snapshot.dg.copy(
                            comparisonVoltage = snapshot.dg.validModelVoltage,
                            comparisonAngle = snapshot.dg.validModelAngle,
                        ),
                    )
                }
            }
        }) { Text("Einstellung merken") }
    }
    model.comparisonVoltage?.let { voltage ->
        val angle = model.comparisonAngle ?: return@let
        Details(attrs = { elementId(DgArticleId.SAVED) }) {
            Summary { Text("Gespeicherte Einstellung vergleichen") }
            P { MathText("${number(voltage, 1)} V"); Text(" und "); MathText("${number(angle, 1)}°"); Text(": "); MathText("${number(Physics.surfaceDiffraction(data.spacing, SurfaceNormalAngle(angle)).nanometers, 6)} nm"); Text(" geometrisch, "); MathText("${number(Physics.electronWavelength(Voltage(voltage)).nanometers, 6)} nm"); Text(" nach de Broglie.") }
            Button(attrs = {
                elementId(DgArticleId.CLEAR)
                protectPersonalWork(session.isReadOnly)
                styles(LabClass.SECONDARY)
                onClick {
                    session.update { snapshot ->
                        snapshot.copy(
                            dg = snapshot.dg.copy(
                                comparisonVoltage = null,
                                comparisonAngle = null,
                            ),
                        )
                    }
                }
            }) { Text("Gespeicherte Einstellung entfernen") }
        }
    }
}

private fun dgArticleAngle(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.1..90.0 }
private fun dgArticleVoltage(raw: String, data: ScientificData): Double? = Physics.inputVoltage(raw)
    ?: raw.trim().replace(',', '.').toDoubleOrNull()?.takeIf { value -> value.isFinite() && value > 0.0 && data.points.any { point -> point.voltageV == value } }
