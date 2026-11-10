package materiewelle.course.graphics

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlin.math.ceil
import kotlin.math.max
import materiewelle.NativeSvg
import materiewelle.core.*
import materiewelle.pages.number
import materiewelle.ui.LocalLabMotion
import materiewelle.ui.LocalLabTheme
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import materiewelle.ui.vector.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement

/** An optional synthetic 1-D detection model; never a reconstruction of the historical photograph. */
@Composable
fun SlitDetectionView(model: FiniteSlitModel, configuration: SlitConfiguration, viewState: SlitDetectionUiState) {
    val distribution = remember(model, configuration) { SlitDetectionDistribution.create(model, configuration) }
    val motion = LocalLabMotion.current.active
    val latestMotion by rememberUpdatedState(motion)
    var surface by remember { mutableStateOf<HTMLElement?>(null) }
    var visible by remember { mutableStateOf(false) }
    val latestVisible by rememberUpdatedState(visible)
    val current = viewState.run.takeIf { viewState.matches(model, configuration) }
    SideEffect { viewState.bind(distribution); viewState.applyActivity(motion, visible) }
    DisposableEffect(Unit) { onDispose { viewState.pause() } }
    DisposableEffect(surface) {
        val stop = surface?.let { observeMotionSurface(it) { showing ->
            visible = showing
            if (!showing) viewState.pause()
        } }
        onDispose { stop?.invoke() }
    }
    DisposableEffect(viewState.running, current?.count, motion, visible) {
        val ticket = viewState.ticket()
        val timer = if (viewState.running && motion && visible && current?.isFull == false) window.setTimeout({
            viewState.tick(ticket, latestMotion, latestVisible)
        }, SlitDetectionUiState.TICK_DELAY_MS) else null
        onDispose { timer?.let(window::clearTimeout); viewState.cancelPending() }
    }
    Details(attrs = {
        elementId(SlitDetectionId.PANEL); slitDetectionStyles(SlitDetectionClass.ROOT)
        detailsState(viewState.expanded, viewState::setExpanded)
    }) {
        Summary { Text("Freiwillige Vertiefung: ein Muster aus einzelnen Treffern") }
        Div(attrs = {
            elementId(SlitDetectionId.BODY)
            ref { surface = it; onDispose { surface = null } }
        }) {
            P(attrs = { styles(LabClass.SMALL) }) { Text("Dieser Exkurs zur Wahrscheinlichkeitsdeutung geht über die Aufgabenstellung hinaus. Für deine Auswertung ist er nicht erforderlich.") }
            P { Text("Sammle einzelne Treffer und beobachte, wie sich das Muster aufbaut. Die Punkte werden zufällig aus dem berechneten Modell gezogen; sie sind keine Aufnahme von Jönssons Versuch.") }
            if (current == null) P { Text("Die Modellverteilung wird vorbereitet.") }
            else {
                P(attrs = { elementId(SlitDetectionId.COUNT); slitDetectionStyles(SlitDetectionClass.COUNT) }) {
                    MathText(current.count.toString()); Text(" simulierte Treffer im dargestellten Fenster")
                }
                Div(attrs = { slitDetectionStyles(SlitDetectionClass.ACTIONS) }) {
                    listOf(SlitDetectionId.ONE to 1, SlitDetectionId.HUNDRED to 100, SlitDetectionId.THOUSAND to 1000).forEach { (id, amount) ->
                        Button(attrs = {
                            elementId(id); styles(LabClass.SECONDARY)
                            if (current.isFull) disabled()
                            onClick { viewState.add(amount) }
                        }) { MathText(amount.toString()); Text(if (amount == 1) " weiterer Treffer" else " weitere Treffer") }
                    }
                    Button(attrs = {
                        elementId(SlitDetectionId.PLAY); styles(LabClass.SECONDARY)
                        ariaPressed(viewState.running)
                        if (!motion || current.isFull || viewState.showData) disabled()
                        onClick { viewState.togglePlayback(latestMotion, latestVisible) }
                    }) { Text(when {
                        !motion -> "Automatik im Ruhemodus aus"
                        viewState.showData -> "Trefferwerte in Ruhe ansehen"
                        current.isFull -> "Serie vollständig"
                        viewState.running -> "Pause"
                        else -> "Automatisch sammeln"
                    }) }
                }
                P(attrs = { elementId(SlitDetectionId.NOTICE); role(AriaRole.STATUS) }) { Text(viewState.notice) }
                DetectionFigure(current)
                if (configuration != SlitConfiguration.BOTH_COHERENT) P(attrs = { styles(LabClass.SMALL) }) {
                    Text("Bei gleicher Trefferzahl haben ein offener Spalt und die inkohärente Summe hier dieselbe normierte Verteilungsform. Ihre gemeinsame Referenzintensität im Modell oben unterscheidet sich. Dieses Histogramm bestimmt keine Emissions- oder Nachweisrate.")
                }
                Div(attrs = { slitDetectionStyles(SlitDetectionClass.ACTIONS) }) {
                    Button(attrs = { elementId(SlitDetectionId.NEW); styles(LabClass.SECONDARY); onClick { viewState.newSeries() } }) { Text("Neue Zufallsfolge") }
                    Button(attrs = { elementId(SlitDetectionId.REPLAY); styles(LabClass.SECONDARY); onClick { viewState.replay() } }) { Text("Gleiche Folge von vorn") }
                    Button(attrs = {
                        elementId(SlitDetectionId.DATA); styles(LabClass.SECONDARY)
                        ariaExpanded(viewState.showData); controls(SlitDetectionId.TABLE)
                        onClick { viewState.toggleData() }
                    }) { Text(if (viewState.showData) "Trefferwerte schließen" else "Trefferwerte anzeigen") }
                }
                P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) {
                    Text("Jede Serie ist auf "); MathText(SlitDetectionRun.MAX_HITS.toString())
                    Text(" Treffer begrenzt. Eine neue Parametereinstellung beginnt eine neue Serie. Eigene Marken am historischen Bild bleiben davon unberührt.")
                }
                if (viewState.showData) DetectionTable(current)
            }
            Details(attrs = { styles(LabClass.NOTES) }) {
                Summary { Text("Zum Modell und zu den Quellen") }
                P { Text("Die Wahrscheinlichkeitsdeutung nach Born ist eine zusätzliche Modellannahme. Diese Animation erklärt ihre Folgen; sie bestätigt sie nicht experimentell.") }
                P { Text("Die Modellserie bleibt beim Kapitelwechsel erhalten, aber nicht nach dem Neuladen. Sie gehört nicht zum Abgabebericht.") }
                P {
                    A(href = "https://web.pdx.edu/~pmoeck/pdf/ElectronJonsson.PDF#page=7") { Text("Jönsson, J1, Druckseite 10 / PDF-Seite 7") }
                    Text(" setzt die Intensität proportional zum Produkt aus Amplitude und komplex konjugierter Amplitude. ")
                    A(href = "https://arxiv.org/pdf/physics/9807012#page=34") { Text("Sievers, H3, Druckseite 26 / PDF-Seite 34") }
                    Text(" ordnet die statistische Deutung Born zu. J1 ist eine gekürzte englische Übersetzung, H3 historische Sekundärliteratur. Die Begrenzung auf das gezeigte Fenster und die Zufallsserie gehören zu unserem Lehrmodell.")
                }
            }
        }
    }
}

@Composable
private fun DetectionFigure(run: SlitDetectionRun) {
    var canvas by remember { mutableStateOf<HTMLCanvasElement?>(null) }
    val painter = remember { SlitDetectionCanvasPainter() }
    val theme = LocalLabTheme.current.preference
    DisposableEffect(canvas, run, theme) {
        // Theme tokens are applied by the parent before this frame; no style-copy palette.
        val frame = window.requestAnimationFrame { canvas?.let { painter.render(it, run) } }
        onDispose { window.cancelAnimationFrame(frame) }
    }
    Figure(attrs = { slitDetectionStyles(SlitDetectionClass.FIGURE) }) {
        Canvas(attrs = {
            elementId(SlitDetectionId.CANVAS); slitDetectionStyles(SlitDetectionClass.CANVAS)
            role(AriaRole.IMAGE)
            ariaLabel("${run.count} simulierte Nachweise im horizontalen Modellfenster. Der vertikale Zeichenversatz dient nur der Sichtbarkeit; die Verteilung ist eindimensional.")
            ref { canvas = it; onDispose { canvas = null } }
        })
        P(attrs = { slitDetectionStyles(SlitDetectionClass.CAPTION) }) {
            Text("Oben: zufällige Modellnachweise zwischen "); MathText("−3 mm"); Text(" und "); MathText("3 mm")
            Text(". Berechnet wird nur die waagerechte Position. Die Punkte stehen in verschiedenen Höhen, damit sie sich weniger überdecken. Ihre Höhe hat keine physikalische Bedeutung; die Punkte zeigen Trefferorte, keine Elektronenbahnen.")
        }
        Div(attrs = { elementId(SlitDetectionId.HISTOGRAM) }) { NativeSvg(detectionHistogram(run)) }
        Figcaption(attrs = { slitDetectionStyles(SlitDetectionClass.CAPTION) }) {
            Text("Balken: gesammelte Treffer je Intervall. Gestrichelte Linie: erwartete Trefferzahl bei derselben Gesamtzahl. Zufällige Abweichungen sind zu erwarten. Die Modellwahrscheinlichkeit wurde über dieses Fenster auf eins normiert; Treffer außerhalb und ausgesandte Elektronen werden nicht gezählt.")
        }
    }
}

@Composable
private fun DetectionTable(run: SlitDetectionRun) {
    P(attrs = { elementId(SlitDetectionId.SEED) }) {
        Text("Zufallsstart für eine genaue Wiederholung: "); MathText(run.seed.toString())
        Text(". Gleiche Parameter, gleicher Start und gleiche Trefferzahl ergeben dieselbe Folge.")
    }
    Div(attrs = { slitDetectionStyles(SlitDetectionClass.TABLE_SCROLL) }) {
        Table(attrs = { elementId(SlitDetectionId.TABLE); slitDetectionStyles(SlitDetectionClass.TABLE) }) {
            Caption { Text("Treffer und Erwartungswert je gleich breitem Intervall; Erwartungswerte auf sechs Nachkommastellen gerundet.") }
            Thead { Tr { Th { Text("Bereich in Millimetern") }; Th { Text("Treffer") }; Th { Text("Erwartungswert") } } }
            Tbody {
                repeat(run.distribution.binCount) { index ->
                    val start = run.distribution.lower.meters + index * run.distribution.binWidthM
                    val end = start + run.distribution.binWidthM
                    Tr(attrs = { elementId(SlitDetectionBinId(index)) }) {
                        Td { MathText(number(start * 1000, 2)); Text(" bis "); MathText(number(end * 1000, 2)) }
                        Td { MathText(run.counts[index].toString()) }
                        Td { MathText(number(run.count * run.distribution.binProbabilities[index], 6)) }
                    }
                }
            }
        }
    }
}

private fun detectionHistogram(run: SlitDetectionRun): VectorFragment = vector {
    val left = SlitDetectionCanvasPainter.LEFT
    val right = SlitDetectionCanvasPainter.RIGHT
    val width = right - left
    val top = 40.0
    val bottom = 244.0
    val expected = run.distribution.binProbabilities.map { it * run.count }
    val maximum = ceil(max(run.counts.maxOrNull()?.toDouble() ?: 0.0, expected.maxOrNull() ?: 0.0) * 1.1).coerceAtLeast(1.0)
    val binWidth = width / run.distribution.binCount
    fun y(value: Double) = bottom - (bottom - top) * value / maximum
    svg({ viewBox(0, 0, 1000, 306); imageRole(); ariaLabel("Histogramm: ${run.count} simulierte Treffer in ${run.distribution.binCount} gleich breiten Intervallen. Balken zählen Treffer, gestrichelte Kurve zeigt erwartete Trefferzahl; Werte in der aufklappbaren Tabelle.") }) {
        title("Trefferzahl je Intervall und erwartete Trefferzahl")
        label("Treffer je Intervall", { atX(left); atY(23); fill(VectorPaint.TEXT); fontSize(17) })
        listOf(0.0, maximum / 2.0, maximum).forEach { value ->
            path({ geometry("M$left ${y(value)} H$right"); stroke(VectorPaint.GRID) })
            label(number(value, if (value % 1 == 0.0) 0 else 1), { atX(left - 10); atY(y(value) + 5); anchor(TextAnchor.END); fill(VectorPaint.MUTED); fontSize(16) })
        }
        run.counts.forEachIndexed { index, count ->
            rect({ atX(left + index * binWidth + 1); atY(y(count.toDouble())); width(binWidth - 2); height(bottom - y(count.toDouble())); fill(VectorPaint.ACCENT_SOFT); stroke(VectorPaint.ACCENT); strokeWidth(1) })
        }
        path({
            geometry(expected.mapIndexed { index, value -> "${if (index == 0) "M" else "L"}${left + (index + .5) * binWidth} ${y(value)}" }.joinToString(" "))
            fill(VectorPaint.None); stroke(VectorPaint.REFERENCE); strokeWidth(2.5); dash(7, 5)
        })
        for (mm in -3..3) {
            val x = left + (mm + 3.0) / 6.0 * width
            label(mm, { atX(x); atY(270); anchor(TextAnchor.MIDDLE); fill(VectorPaint.MUTED); fontSize(17) })
        }
        label("Horizontale Modellposition in Millimetern", { atX((left + right) / 2); atY(299); anchor(TextAnchor.MIDDLE); fill(VectorPaint.TEXT); fontSize(17) })
    }
}
