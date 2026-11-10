package materiewelle.course

import androidx.compose.runtime.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import materiewelle.NativeSvg
import materiewelle.core.EqualIntensityInterferenceProbe
import materiewelle.core.LensProbeRay
import materiewelle.core.ParaxialLensProbe
import materiewelle.core.Physics
import materiewelle.ui.FormulaId
import materiewelle.ui.InlineFormula
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/** Optional local investigations. They neither gate the course nor overwrite personal work. */
enum class GeometryBridgeTopic { STRUCTURE, BRAGG }

private enum class FootChoice(val label: String) {
    PERPENDICULAR("Punkt P"),
    OBLIQUE("Punkt Q"),
}

/** Intended below the existing three crystal views (S09) or four constructions (S10). */
@Composable
fun GeometryProjectionBridge(topic: GeometryBridgeTopic = GeometryBridgeTopic.STRUCTURE) {
    var rotated by remember(topic) { mutableStateOf(false) }
    var choice by remember(topic) { mutableStateOf<FootChoice?>(null) }
    var revealed by remember(topic) { mutableStateOf(false) }
    var planes by remember(topic) { mutableStateOf(false) }
    val bragg = topic == GeometryBridgeTopic.BRAGG
    Details(attrs = { styles(LabClass.NOTES); ariaLabel("Geometrische Ergänzungsprobe") }) {
        Summary { Text("Lot und Zusatzweg selbst ergänzen") }
        P {
            Text(
                if (bragg)
                    "Ergänze von A aus das Lot auf den einlaufenden Strahl durch B. Welcher Endpunkt passt? Begründe deine Wahl zuerst am rechten Winkel."
                else
                    "Ergänze von A aus den senkrechten Abstand zur anderen Linie. Welcher Endpunkt passt? Die Skizze ergänzt die Kristallansichten; sie ist kein maßstäblicher Nickelbaustein."
            )
        }
        if (!bragg) {
            BridgeActions("Bezugslinien der Abstandsprobe") {
                BridgeButton("Oberflächenreihen", !planes) {
                    planes = false; choice = null; revealed = false
                }
                BridgeButton("Netzebenen im Schnitt", planes) {
                    planes = true; choice = null; revealed = false
                }
            }
        }
        BridgeDiagram(
            if (bragg) projectionProbe(rotated, revealed)
            else distanceProbe(rotated, planes, revealed)
        )
        BridgeActions("Endpunkt des Lots wählen") {
            FootChoice.entries.forEach { candidate ->
                BridgeButton(candidate.label, choice == candidate) {
                    choice = candidate
                    revealed = false
                }
            }
            BridgeButton("Bezugslinien kontrollieren", revealed) { revealed = true }
            BridgeButton("Zeichnung drehen", rotated) {
                rotated = !rotated
                choice = null
                revealed = false
            }
        }
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
            Text("Die Kontrolle ist auch ohne Auswahl erreichbar. Nach dem Drehen bleiben Punkte und Strecken dieselben; zeige das Lot anhand seiner Bezugslinien, nicht anhand seiner Lage auf dem Bildschirm.")
        }
        if (revealed) {
            Div(attrs = { styles(LabClass.COURSE_RESULT); role(AriaRole.STATUS) }) {
                P {
                    Text(
                        when (choice) {
                            FootChoice.PERPENDICULAR -> "P passt: Die Verbindung von A nach P steht senkrecht auf der Bezugslinie."
                            FootChoice.OBLIQUE -> "Q liegt ebenfalls auf der Bezugslinie, aber AQ steht nicht senkrecht darauf. Das Lot endet in P."
                            null -> "Das Lot endet in P. Prüfe den eingezeichneten rechten Winkel selbst."
                        }
                    )
                }
                if (bragg) {
                    P { Text("Im rechtwinkligen Dreieck ist AB die Hypotenuse und PB die Gegenkathete zum markierten θB. Der gleiche Winkel liegt am Strahl zur Ebene. Der erste Zusatzweg ist deshalb:") }
                    InlineFormula(FormulaId.BRAGG_LEG)
                    P { Text("Auf der Ausfallsseite kommt BR mit gleicher Länge hinzu. Erst beide Zusatzwege ergeben die Bragg-Bedingung:") }
                    InlineFormula(FormulaId.BRAGG)
                } else {
                    P {
                        Text(
                            if (planes) "Hier sind die Linien Spuren zweier paralleler (111)-Netzebenen. AP ist d111, der Abstand senkrecht zwischen den Ebenen."
                            else "Hier sind die Linien zwei ausgewählte Reihen innerhalb der Oberfläche. AP ist D, die Reihenhöhe innerhalb dieser Fläche."
                        )
                    }
                    P { Text("Der Wechsel von Oberflächenaufsicht zu Ebenenschnitt ändert die Bezugslinien. Gleiche Hilfspunktnamen machen D und d111 nicht zum gleichen Abstand.") }
                }
            }
        }
        if (bragg) SurfaceAzimuthReminder()
        P {
            Text(
                if (bragg)
                    "Eigene Gegenprobe: Zeichne das gedrehte Dreieck auf Papier. Zeige Hypotenuse, Gegenkathete und beide Zusatzwege. Wechsle dann zur vorhandenen Oberflächenzeichnung und definiere Abstand und Winkel neu."
                else
                    "Eigene Gegenprobe: Zeige in den vorhandenen Kristallansichten die Endpunkte von D und d111. Zeichne eine eigene gedrehte Skizze und benenne jeweils, wozu das Lot senkrecht steht."
            )
        }
    }
}

@Composable
private fun SurfaceAzimuthReminder() {
    Details {
        Summary { Text("Warum die Oberflächenzeichnung einen eigenen Schnitt braucht") }
        BridgeDiagram(surfaceAzimuthProbe())
        P { Text("Die Pfeile zeigen nur die Projektion einer Detektorrichtung in die Oberfläche. Im gewählten Azimut verläuft sie quer zu den Reihen. D ist deren Abstand; φ wird im dazugehörigen senkrechten Schnitt von der äußeren Oberflächennormalen gemessen.") }
        InlineFormula(FormulaId.SURFACE)
        P { Text("Bei normalem Einfall gibt es zwischen diesen Reihen keinen entsprechenden Ankunftswegversatz. Im gewählten Ausfallschnitt bleibt D sin φ. Entlang derselben Reihe trägt der Versatz in diesem Modell keinen zusätzlichen auslaufenden Weg bei. Bei anderem Azimut muss die Projektion neu geprüft werden.") }
        P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
            Text("Die Draufsicht zeigt keinen räumlichen Detektorwinkel. φ ist nicht automatisch der halbe Braggwinkel oder dessen Doppeltes; auch D und d111 dürfen nicht gleichgesetzt werden. Begrenzte kinematische Lehrgeometrie, keine vollständige Elektronenstreutheorie.")
        }
    }
}

/** A paraxial thin-lens construction in arbitrary drawing units, never a historical calibration. */
@Composable
fun IdealLensBridge() {
    var direction by remember { mutableStateOf(0.0) }
    var heightShift by remember { mutableStateOf(0.0) }
    var distance by remember { mutableStateOf(1.0) }
    val model = ParaxialLensProbe(direction, heightShift, distance)
    val inFocus = model.isFocalPlane
    Details(attrs = { styles(LabClass.NOTES); ariaLabel("Ideale Linse als Funktionsmodell") }) {
        Summary { Text("Ideale Linse: Richtung wird Beobachtungsort") }
        P { Text("Erklärtes Beispiel: Zwei Beiträge kommen aus derselben Richtung und treffen die ideale dünne Linse in verschiedenen Höhen. In der hinteren Brennebene fallen ihre Treffer zusammen. Verändere zuerst nur die Eintrittshöhen und beobachte diesen Zusammenhang.") }
        P { Text("Deine neue Vorhersage: Was ändert sich, wenn die Beobachtungsebene vor der Brennebene liegt? Notiere deine Erwartung, bevor du die Ebene umstellst. Prüfe danach und erkläre den Einfluss der Eintrittshöhe.") }
        BridgeDiagram(idealLensDrawing(model))
        BridgeRange("Eintrittshöhen gemeinsam verschieben", heightShift, -0.025, 0.025, 0.005) {
            heightShift = it
        }
        BridgeRange("Einfallsrichtung ändern", direction, -0.01, 0.01, 0.002) { direction = it }
        BridgeRange("Abstand der Beobachtungsebene in Brennweiten", distance, 0.60, 1.40, 0.05) {
            distance = it
        }
        BridgeActions("Beobachtungsebene wählen") {
            BridgeButton("In die hintere Brennebene", inFocus) { distance = 1.0 }
            BridgeButton("Vor die Brennebene", abs(distance - .7) < 1e-8) { distance = .7 }
            BridgeButton("Hinter die Brennebene", abs(distance - 1.3) < 1e-8) { distance = 1.3 }
        }
        P(attrs = { styles(LabClass.COURSE_RESULT); role(AriaRole.STATUS) }) {
            Text(
                if (inFocus)
                    "In der hinteren Brennebene treffen beide Beiträge am gleichen Ort ein. Eine andere Einfallsrichtung verschiebt diesen gemeinsamen Ort; andere Eintrittshöhen ändern ihn nicht."
                else
                    "Außerhalb der hinteren Brennebene treffen diese gleichgerichteten Beiträge an verschiedenen Orten ein. Die Eintrittshöhe wirkt jetzt auf den Beobachtungsort."
            )
        }
        P { Text("Abstand der Beobachtungsebene: ${Physics.decimal(distance, 2)} Brennweiten. Querabstand der beiden Treffpunkte: ${Physics.decimal(model.spotSeparationOverF, 3)} Brennweiten. Die Querrichtung ist zur Sichtbarkeit zehnfach gegenüber der Längsrichtung gestreckt; diese Zahlen beschreiben nur das ideale Lehrmodell.") }
        Details {
            Summary { Text("Die Beziehung selbst erklären") }
            P { Text("Die Linse ändert die Richtung abhängig von der Eintrittshöhe. Genau eine Brennweite hinter ihr gleicht diese Änderung den anfänglichen Höhenunterschied aus. Eine andere Einfallsrichtung verschiebt den gemeinsamen Treffpunkt. Die Zeichnung verwendet eine paraxiale Näherung, also kleine Winkel.") }
            P { Text("Gegenprobe: Stelle die Beobachtungsebene vor die Brennebene, verschiebe die Eintrittshöhen und erkläre die Änderung. Stelle anschließend die Brennebene wieder her und ändere nur die Richtung.") }
        }
        P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
            Text("Ideale dünne Linse, schematische Beiträge, keine gemessenen Elektronenbahnen. Das ist keine Rekonstruktion von Jönssons Mehrlinsenoptik. Eine frei gewählte Brennweite und die Quellenangabe 350 mm liefern hier keinen Meter-pro-Pixel-Maßstab des Fotos.")
        }
    }
}

private enum class PhaseProbe(val label: String, val pathWavelengths: Double) {
    SAME("Gleiche Phase", 0.0),
    QUARTER("Viertelwellenversatz", 0.25),
    OPPOSITE("Gegenphase", 0.5),
}

/** Fixed common intensity scale: each isolated contribution is 1, sum 2, coherent range 0..4. */
@Composable
fun IntensityEvidenceBridge() {
    var probe by remember { mutableStateOf(PhaseProbe.OPPOSITE) }
    var revealed by remember { mutableStateOf(false) }
    val model = EqualIntensityInterferenceProbe(probe.pathWavelengths)
    Details(attrs = { styles(LabClass.NOTES); ariaLabel("Intensitätsvergleich zweier Modelle") }) {
        Summary { Text("Zwei Beiträge: Summe oder Interferenz?") }
        P { Text("Betrachte einen festen Beobachtungsort. Jede Öffnung für sich liefert dort dieselbe positive Einzelintensität: jeweils 1 Modelleinheit. Was sagt das einfache Modell unabhängiger Beiträge voraus? Kann seine bloße Summe bei Gegenphase null werden?") }
        BridgeDiagram(intensityComparisonDrawing(probe.pathWavelengths, revealed))
        BridgeActions("Relative Phase am Beobachtungsort wählen") {
            PhaseProbe.entries.forEach { selected ->
                BridgeButton(selected.label, selected == probe) { probe = selected }
            }
            BridgeButton("Kohärentes Modell vergleichen", revealed) { revealed = !revealed }
        }
        P { Text("Aktueller Zusatzweg: Δs/λ = ${Physics.decimal(probe.pathWavelengths, 2)}. Beide Einzelintensitäten bleiben jeweils 1; ihre bloße Summe bleibt 2. Die waagerechte Achse zeigt den Zusatzweg in Wellenlängen, keinen Ort im historischen Foto.") }
        if (revealed) {
            Div(attrs = { styles(LabClass.COURSE_RESULT); role(AriaRole.STATUS) }) {
                P { Text("Ideales kohärentes Modell an derselben Probe: ${Physics.decimal(model.coherentIntensity, 2)} Modelleinheiten. Beide Modelle benutzen dieselbe feste Intensitätsskala; die Kurven werden nicht einzeln auf ihren Höchstwert normiert.") }
                P {
                    Text(
                        if (probe == PhaseProbe.OPPOSITE)
                            "Bei Gegenphase liefert das ideale kohärente Modell ein Nullminimum, obwohl beide Einzelintensitäten positiv sind. Die bloße Summe bleibt positiv. Genau dieser Vergleich unterscheidet die beiden erklärten Modelle."
                        else
                            "Prüfe auch Gegenphase: Ein beliebiges lokales Minimum würde zur Unterscheidung nicht genügen, denn auch eine bloße Summe kann lokale Minima besitzen. Entscheidend ist hier die ideale Null bei gleichen positiven Einzelbeiträgen."
                    )
                }
            }
        }
        Details {
            Summary { Text("Warum die Intensität keine negative Auslenkung ist") }
            P { Text("Die Einzelintensitäten sind nichtnegative Größen. Im kohärenten Modell werden zunächst die Beiträge mit ihrer relativen Phase überlagert; erst daraus folgt die Intensität. Die Vorzeichenrechnung der klassischen Wellenbrücke ist daher keine direkte Addition von Intensitäten.") }
            P { Text("Der Lehrfall setzt gleiche Beiträge und eine stabile relative Phase voraus. Bei veränderlicher relativer Phase muss zeitlich gemittelt werden; hier wird kein vollkommen schwarzes Minimum im gedruckten Scan behauptet.") }
        }
        P(attrs = { styles(LabClass.MODEL_LIMIT) }) {
            Text("Synthetischer Modellvergleich, keine neuen Messdaten. Er begrenzt das einfache Modell unabhängiger Beiträge, nicht jede denkbare klassische Anordnung. Die historischen Einzel- und Doppelspaltbilder sind keine kontrolliert gleich normierte Messserie; ihre Grauwerte werden nicht voneinander abgezogen.")
        }
        P { Text("Formuliere nun selbst in deiner Belegtabelle: Was trägt der DG-Vergleich aus Struktur, Winkel und Spannung bei? Was zeigt Jönsson mit künstlichen Öffnungen? Was quantifizieren deine Pixelabstände, und welche unabhängige Information fehlt für eine absolute Wellenlänge? Die drei persönlichen Aussagen bleiben deine eigene Arbeit.") }
    }
}

@Composable
private fun BridgeActions(label: String, content: @Composable () -> Unit) {
    Div(attrs = {
        role(AriaRole.GROUP)
        ariaLabel(label)
        styles(LabClass.COURSE_ACTIONS)
    }) { content() }
}

@Composable
private fun BridgeButton(label: String, active: Boolean, action: () -> Unit) {
    Button(attrs = { styles(LabClass.SECONDARY); ariaPressed(active); onClick { action() } }) {
        Text(label)
    }
}

/** Nested labels provide native input names without inventing untyped global element identities. */
@Composable
private fun BridgeRange(label: String, current: Double, min: Double, max: Double, step: Double, change: (Double) -> Unit) {
    Label(attrs = { style { display(DisplayStyle.Block); marginTop(10.px) } }) {
        Text(label)
        Input(InputType.Range, attrs = {
            ariaLabel(label)
            minimum(min); maximum(max); increment(step); value(current)
            style { display(DisplayStyle.Block); width(100.percent) }
            onInput { event ->
                event.value?.toDouble()?.takeIf { it.isFinite() && it in min..max }?.let(change)
            }
        })
    }
}

@Composable
private fun BridgeDiagram(drawing: VectorFragment) {
    Div(attrs = {
        styles(LabClass.COURSE_DIAGRAM)
        focusIndex(0)
        ariaLabel("Ergänzungszeichnung; auf schmalen Ansichten seitlich verschiebbar")
    }) { NativeSvg(drawing) }
}

private data class BridgePoint(val x: Double, val y: Double)

private fun BridgePoint.rotated(degrees: Double, center: BridgePoint = BridgePoint(250.0, 155.0)): BridgePoint {
    val angle = degrees * PI / 180.0
    val dx = x - center.x
    val dy = y - center.y
    return BridgePoint(center.x + dx * cos(angle) - dy * sin(angle), center.y + dx * sin(angle) + dy * cos(angle))
}

private fun VectorBuilder.bridgeLine(a: BridgePoint, b: BridgePoint, color: VectorPaint = VectorPaint.MUTED, width: Double = 2.0, dashed: Boolean = false) {
    line({ x1(a.x); y1(a.y); x2(b.x); y2(b.y); stroke(color); strokeWidth(width); if (dashed) dash(5, 4) })
}

private fun VectorBuilder.bridgePoint(point: BridgePoint, name: String, color: VectorPaint = VectorPaint.TEXT) {
    circle({ cx(point.x); cy(point.y); radius(4); fill(color) })
    label(name, { atX(point.x + 10); atY(point.y - 8); fill(color); fontSize(16) })
}

private fun VectorBuilder.rightAngle(vertex: BridgePoint, towardFirst: BridgePoint, towardSecond: BridgePoint) {
    fun unit(point: BridgePoint): BridgePoint {
        val dx = point.x - vertex.x
        val dy = point.y - vertex.y
        val length = kotlin.math.sqrt(dx * dx + dy * dy)
        return BridgePoint(dx / length * 10, dy / length * 10)
    }
    val u = unit(towardFirst)
    val v = unit(towardSecond)
    path({
        geometry("M${vertex.x + u.x} ${vertex.y + u.y} L${vertex.x + u.x + v.x} ${vertex.y + u.y + v.y} L${vertex.x + v.x} ${vertex.y + v.y}")
        fill(VectorPaint.None); stroke(VectorPaint.TEXT); strokeWidth(1.5)
    })
}

private fun distanceProbe(rotated: Boolean, planes: Boolean, revealed: Boolean): VectorFragment = vector {
    val angle = if (rotated) 58.0 else 0.0
    fun p(x: Double, y: Double) = BridgePoint(x, y).rotated(angle)
    val a = p(220.0, 100.0)
    val foot = p(220.0, 210.0)
    val other = p(320.0, 210.0)
    svg({ viewBox(0, 0, 500, 320); imageRole(); ariaLabel("Zwei parallele ${if (planes) "Netzebenen im Schnitt" else "Oberflächenreihen"}. A liegt auf der ersten Linie, P und Q auf der anderen. " + if (revealed) "AP ist das Lot mit rechtem Winkel, AQ wäre schräg." else "Wähle den Endpunkt des Lots anhand der Bezugslinien; die Kontrolle ist noch geschlossen.") }) {
        title("Ein senkrechter Abstand braucht zwei Bezugslinien")
        listOf(100.0, 210.0).forEach { row ->
            bridgeLine(p(115.0, row), p(385.0, row))
            if (!planes) (0..6).forEach { point ->
                val atom = p(115.0 + point * 45.0, row)
                circle({ cx(atom.x); cy(atom.y); radius(3); fill(VectorPaint.MUTED) })
            }
        }
        bridgePoint(a, "A")
        bridgePoint(foot, "P")
        bridgePoint(other, "Q")
        if (revealed) {
            bridgeLine(a, foot, VectorPaint.ACCENT, 4.0)
            rightAngle(foot, a, other)
            label(if (planes) "d111" else "D", { atX((a.x + foot.x) / 2 - 22); atY((a.y + foot.y) / 2); fill(VectorPaint.ACCENT); fontSize(18) })
        }
        label(if (planes) "Ebenenschnitt" else "Oberflächenaufsicht", { atX(20); atY(27); fill(VectorPaint.MUTED); fontSize(16) })
    }
}

private fun projectionProbe(rotated: Boolean, revealed: Boolean): VectorFragment = vector {
    val theta = PI / 6
    val angle = if (rotated) -25.0 else 0.0
    fun p(x: Double, y: Double) = BridgePoint(x, y).rotated(angle)
    val a = p(250.0, 75.0)
    val b = p(250.0, 235.0)
    val foot = p(250 - 80 * cos(theta), 235 - 80 * sin(theta))
    val other = p(250 - 130 * cos(theta), 235 - 130 * sin(theta))
    val out = p(250 + 80 * cos(theta), 235 - 80 * sin(theta))
    fun angleArc(cx: Double, cy: Double, radius: Double, start: Double, end: Double): String =
        (0..12).joinToString(" ") { index ->
            val radians = (start + (end - start) * index / 12.0) * PI / 180.0
            val point = p(cx + radius * cos(radians), cy + radius * sin(radians))
            "${if (index == 0) "M" else "L"}${point.x} ${point.y}"
        }
    svg({ viewBox(0, 0, 500, 320); imageRole(); ariaLabel("Bragg-Projektionsprobe: A und B auf benachbarten Netzebenen. AB ist der Ebenenabstand. P und Q liegen auf dem einfallenden Strahl. " + if (revealed) "Das Lot von A endet in P, nicht Q. Beide Zusatzwege PB und BR und rechte Winkel sind eingezeichnet." else "Welcher Punkt ist der Lotfuß von A? Die Kontrolle ist noch geschlossen.") }) {
        title("Lot und zwei Zusatzwege in der Bragg-Geometrie")
        listOf(75.0, 235.0).forEach { level -> bridgeLine(p(65.0, level), p(435.0, level), VectorPaint.GRID) }
        bridgeLine(p(250 - 190 * cos(theta), 235 - 190 * sin(theta)), b)
        bridgeLine(b, p(250 + 190 * cos(theta), 235 - 190 * sin(theta)))
        bridgeLine(p(250 - 100 * cos(theta), 75 - 100 * sin(theta)), a, VectorPaint.GRID)
        bridgeLine(a, p(250 + 100 * cos(theta), 75 - 100 * sin(theta)), VectorPaint.GRID)
        bridgeLine(a, b, VectorPaint.TEXT, 2.0, true)
        bridgePoint(a, "A"); bridgePoint(b, "B"); bridgePoint(foot, "P"); bridgePoint(other, "Q")
        val angleLabel = p(295.0, 226.0)
        path({ geometry(angleArc(250.0, 235.0, 34.0, -30.0, 0.0)); fill(VectorPaint.None); stroke(VectorPaint.TEXT); strokeWidth(1.5) })
        label("θB", { atX(angleLabel.x); atY(angleLabel.y); fill(VectorPaint.TEXT); fontSize(16) })
        val dLabel = p(267.0, 125.0)
        label("d", { atX(dLabel.x); atY(dLabel.y); fill(VectorPaint.MUTED); fontSize(17) })
        if (revealed) {
            bridgeLine(a, foot, VectorPaint.ACCENT, 2.0, true)
            bridgeLine(a, out, VectorPaint.REFERENCE, 2.0, true)
            bridgeLine(foot, b, VectorPaint.ACCENT, 5.0)
            bridgeLine(b, out, VectorPaint.REFERENCE, 5.0)
            rightAngle(foot, a, b); rightAngle(out, a, b)
            bridgePoint(out, "R", VectorPaint.REFERENCE)
            val triangleAngle = p(228.0, 115.0)
            path({ geometry(angleArc(250.0, 75.0, 28.0, 90.0, 120.0)); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(1.5) })
            label("θB", { atX(triangleAngle.x); atY(triangleAngle.y); fill(VectorPaint.ACCENT); fontSize(16) })
        }
    }
}

private fun surfaceAzimuthProbe(): VectorFragment = vector {
    svg({ viewBox(0, 0, 500, 240); imageRole(); ariaLabel("Draufsicht: drei parallele Oberflächenreihen. Die gewählte Detektorprojektion steht quer zu ihnen; eine schräge Vergleichsprojektion zeigt einen anderen Azimut. D liegt quer zwischen den Reihen.") }) {
        title("Azimut: Richtung innerhalb der Oberfläche")
        listOf(80.0, 130.0, 180.0).forEach { row ->
            bridgeLine(BridgePoint(90.0, row), BridgePoint(390.0, row))
            (0..6).forEach { i -> circle({ cx(90 + 50 * i); cy(row); radius(3); fill(VectorPaint.MUTED) }) }
        }
        bridgeLine(BridgePoint(240.0, 180.0), BridgePoint(240.0, 38.0), VectorPaint.ACCENT, 3.0)
        bridgeLine(BridgePoint(240.0, 38.0), BridgePoint(234.0, 50.0), VectorPaint.ACCENT, 3.0)
        bridgeLine(BridgePoint(240.0, 38.0), BridgePoint(246.0, 50.0), VectorPaint.ACCENT, 3.0)
        bridgeLine(BridgePoint(240.0, 180.0), BridgePoint(350.0, 40.0), VectorPaint.REFERENCE, 2.0, true)
        bridgeLine(BridgePoint(60.0, 80.0), BridgePoint(60.0, 130.0), VectorPaint.TEXT, 3.0)
        label("D", { atX(35); atY(109); fill(VectorPaint.TEXT); fontSize(17) })
        label("gewählte Projektion", { atX(220); atY(23); anchor(TextAnchor.END); fill(VectorPaint.ACCENT); fontSize(15) })
        label("anderer Azimut", { atX(365); atY(30); fill(VectorPaint.REFERENCE); fontSize(15) })
    }
}

private fun idealLensDrawing(model: ParaxialLensProbe): VectorFragment = vector {
    val lensX = 180.0
    val unit = 145.0
    val transverseUnit = unit * 10
    val axis = 155.0
    fun position(ray: LensProbeRay, dist: Double) = BridgePoint(lensX + unit * dist, axis - transverseUnit * model.heightAtDistanceOverF(ray, dist))
    val planeX = lensX + unit * model.observationDistanceOverF
    val rays = listOf(LensProbeRay.FIRST to VectorPaint.ACCENT, LensProbeRay.SECOND to VectorPaint.REFERENCE)
    svg({ viewBox(0, 0, 460, 320); imageRole(); ariaLabel("Ideale dünne Linse mit zwei gleichgerichteten Eingangsbeiträgen in verschiedener Höhe; Querrichtung zehnfach gestreckt. Hintere Brennebene gestrichelt, bewegliche Beobachtungsebene durchgezogen. " + if (model.isFocalPlane) "Beobachtungsebene liegt in der hinteren Brennebene: beide Treffer fallen zusammen." else "Beobachtungsebene liegt außerhalb der hinteren Brennebene: zwei verschiedene Treffer.") }) {
        title("Gleiche Richtung, unterschiedliche Eintrittshöhen")
        bridgeLine(BridgePoint(20.0, axis), BridgePoint(440.0, axis), VectorPaint.GRID, 1.0, true)
        path({ geometry("M$lensX 42 Q${lensX + 20} $axis $lensX 270 Q${lensX - 20} $axis $lensX 42"); fill(VectorPaint.ACCENT_SOFT); fillOpacity(.2); stroke(VectorPaint.TEXT); strokeWidth(2) })
        bridgeLine(BridgePoint(lensX + unit, 45.0), BridgePoint(lensX + unit, 270.0), VectorPaint.MUTED, 2.0, true)
        rays.forEach { (ray, color) ->
            val before = BridgePoint(25.0, axis - transverseUnit * model.incomingHeightOverF(ray, (lensX - 25) / unit))
            val entry = BridgePoint(lensX, axis - transverseUnit * model.entryHeightOverF(ray))
            bridgeLine(before, entry, color, 2.5)
            bridgeLine(entry, position(ray, 1.6), color, 2.5)
        }
        bridgeLine(BridgePoint(planeX, 60.0), BridgePoint(planeX, 260.0), VectorPaint.TEXT, 1.0)
        rays.forEachIndexed { index, (ray, color) ->
            val spot = position(ray, model.observationDistanceOverF)
            circle({ cx(spot.x); cy(spot.y); radius(if (index == 0) 6 else 3); fill(color) })
        }
        label("Linse", { atX(lensX); atY(25); anchor(TextAnchor.MIDDLE); fill(VectorPaint.TEXT); fontSize(16) })
        label("hintere Brennebene", { atX(lensX + unit); atY(293); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(15) })
        label("Beobachtung", { atX(planeX); atY(45); anchor(TextAnchor.MIDDLE); fill(VectorPaint.TEXT); fontSize(15) })
    }
}

private fun intensityComparisonDrawing(probe: Double, revealed: Boolean): VectorFragment = vector {
    val left = 50.0
    val right = 450.0
    val bottom = 250.0
    fun x(value: Double) = left + (right - left) * value
    fun y(value: Double) = bottom - value * 50.0
    svg({ viewBox(0, 0, 500, 320); imageRole(); ariaLabel("Gemeinsame Intensitätsskala null bis vier. Zwei gleiche positive Einzelintensitäten sind jeweils eins; ihre bloße Summe ist zwei. " + if (revealed) "Das ideale kohärente Modell erreicht bei halbem Wellenlängenversatz null und bei gleicher Phase vier." else "Das kohärente Modell ist noch verborgen und kann freiwillig zugeschaltet werden.") }) {
        title("Synthetischer Intensitätsvergleich auf fester gemeinsamer Skala")
        (0..4).forEach { tick ->
            bridgeLine(BridgePoint(left, y(tick.toDouble())), BridgePoint(right, y(tick.toDouble())), VectorPaint.GRID, 1.0)
            label(tick, { atX(35); atY(y(tick.toDouble()) + 5); anchor(TextAnchor.END); fill(VectorPaint.MUTED); fontSize(14) })
        }
        bridgeLine(BridgePoint(left, 40.0), BridgePoint(left, bottom), VectorPaint.TEXT)
        bridgeLine(BridgePoint(left, bottom), BridgePoint(right, bottom), VectorPaint.TEXT)
        bridgeLine(BridgePoint(left, y(1.0)), BridgePoint(right, y(1.0)), VectorPaint.MUTED, 3.0)
        bridgeLine(BridgePoint(left, y(2.0)), BridgePoint(right, y(2.0)), VectorPaint.REFERENCE, 3.0, true)
        if (revealed) {
            val curve = (0..100).joinToString(" ") { sample ->
                val phase = sample / 100.0
                "${if (sample == 0) "M" else "L"}${x(phase)} ${y(EqualIntensityInterferenceProbe(phase).coherentIntensity)}"
            }
            path({ geometry(curve); fill(VectorPaint.None); stroke(VectorPaint.ACCENT); strokeWidth(3) })
            circle({ cx(x(probe)); cy(y(EqualIntensityInterferenceProbe(probe).coherentIntensity)); radius(5); fill(VectorPaint.ACCENT) })
        }
        bridgeLine(BridgePoint(x(probe), 40.0), BridgePoint(x(probe), bottom), VectorPaint.TEXT, 1.5, true)
        label("Intensität", { atX(50); atY(22); fill(VectorPaint.MUTED); fontSize(16) })
        label("je ein Beitrag", { atX(440); atY(y(1.0) - 8); anchor(TextAnchor.END); fill(VectorPaint.MUTED); fontSize(14) })
        label("bloße Summe", { atX(440); atY(y(2.0) - 8); anchor(TextAnchor.END); fill(VectorPaint.REFERENCE); fontSize(14) })
        if (revealed) label("kohärentes Modell", { atX(295); atY(52); fill(VectorPaint.ACCENT); fontSize(14) })
        listOf(0.0, .5, 1.0).forEach { value ->
            label(Physics.decimal(value, 1), { atX(x(value)); atY(276); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(14) })
        }
        label("Δs/λ", { atX(450); atY(304); anchor(TextAnchor.END); fill(VectorPaint.MUTED); fontSize(16) })
    }
}
