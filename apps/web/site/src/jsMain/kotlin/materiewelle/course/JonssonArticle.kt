package materiewelle.course

import materiewelle.learning.CompactTutorUiState
import materiewelle.learning.TutorHelpButton
import materiewelle.learning.TutorHelpPanel
import materiewelle.learning.TutorPanelPlacement
import materiewelle.learning.TutorHelpPoint
import androidx.compose.runtime.Composable
import materiewelle.core.*
import materiewelle.core.course.SlitModelComparison
import materiewelle.course.graphics.*
import materiewelle.core.course.AppRoute
import materiewelle.core.course.AppSection
import materiewelle.pages.*
import materiewelle.state.LabSession
import materiewelle.ui.InlineFormula
import materiewelle.ui.FormulaId
import materiewelle.ui.MathText
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*

/** The historical experiment, ideal model and native image measurement have distinct roles. */
@Composable
fun JonssonArticle(
    session: LabSession,
    data: ScientificData,
    profiles: LoadState<List<MeasurementProfile>>,
    viewState: JonssonUiState,
    onNavigate: (AppRoute) -> Unit,
    tutorView: CompactTutorUiState,
) {
    Article(attrs = { elementId(JonssonArticleId.ROOT); articleStyles(ArticleClass.ROOT) }) {
        Header {
            P(attrs = { articleStyles(ArticleClass.EYEBROW) }) { Text("Claus Jönsson · 1961") }
            H1(attrs = { elementId(JonssonArticleId.TITLE); focusIndex(-1) }) {
                Text("Elektronen am Doppelspalt")
            }
            P(attrs = { articleStyles(ArticleClass.LEAD) }) {
                Text("Am Kristall haben wir Beugung an einer regelmäßigen Struktur untersucht. Jönsson ließ Elektronen durch künstlich hergestellte Spalte treten. Dabei entstanden helle und dunkle Streifen, wie man sie auch von der Interferenz mit Licht kennt.")
            }
            P {
                Text("Das Kapitel beginnt mit dem Versuchsaufbau. Danach zeigt ein Modell, wie Spaltbreite und Spaltabstand das Muster beeinflussen. Anschließend misst du Streifenabstände in der historischen Aufnahme. Mit „Weiter“ kannst du die Bauteile der Skizze nacheinander ansehen; freiwillige Vertiefungen stehen am Kapitelende.")
            }
        }
        ArticleContents(AppSection.JONSSON, onNavigate)
        Section(attrs = { elementId(JonssonArticleId.SETUP); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Wie wird das Muster sichtbar?") }
            P {
                Text("Elektronen werden beschleunigt und auf die Spalte gelenkt. Hinter den Öffnungen überlagern sich die Wellenbeiträge aus beiden Spalten. Die Elektronenoptik bildet die dadurch entstehende feine Intensitätsverteilung ab und vergrößert sie. Die Skizze erklärt die Funktion der Bauteile; Größen und Abstände sind nicht maßstäblich dargestellt.")
            }
            JonssonOptics(viewState, showHeading = false)
            H3 { Text("Die technischen Schwierigkeiten") }
            Ul {
                Li { Text("Die Spalte müssen sehr schmal und parallel sein. Bei ihrer Herstellung müssen die Öffnungen für Elektronen frei bleiben.") }
                Li { Text("Die Beleuchtung beider Spalte muss eine hinreichend stabile relative Phasenbeziehung ermöglichen. Das nennt man kohärente Beleuchtung. Eine zu große Streuung der Einfallsrichtungen verwischt die Streifen.") }
                Li { Text("Der Aufbau muss genau justiert und mechanisch stabil sein. Die Elektronenoptik muss die feinen Streifen auflösen können.") }
            }
        }
        Section(attrs = { elementId(JonssonArticleId.LIGHT); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Der Vergleich mit Licht") }
            P {
                Text("Die Phase bezeichnet die Stelle innerhalb einer Schwingung. Wellenbeiträge gleicher Phase verstärken sich. Zwei gleich starke Beiträge mit einem halben Schwingungszyklus Versatz löschen sich aus. Kohärent heißt, dass ihre relative Phasenbeziehung stabil bleibt.")
            }
            P {
                Text("Beim Doppelspalt liegen feine helle und dunkle Streifen innerhalb einer breiteren Beugungshülle. Bei gleicher Wellenlänge und Abbildungsgeometrie bestimmt der Abstand der Spaltmitten den rechnerischen Streifenabstand. Die Breite der einzelnen Spalte bestimmt die Breite der Hülle. Im Modell kannst du beide Einflüsse getrennt untersuchen.")
            }
            P {
                Text("Die entsprechende räumliche Struktur erscheint auch im Elektronenversuch. Das stützt die Wellenbeschreibung. Ein Streifenbild zeigt jedoch keine wellenförmige Bahn eines Elektrons und verrät nicht, durch welchen Spalt es flog.")
            }
            JonssonFringeDerivation()
            Details {
                Summary { Text("Was bedeuten kleine Winkel und Fernfeld?") }
                P { Text("Kleine Winkel heißen: Wir betrachten Richtungen nahe der optischen Achse. Im Fernfeld ist die Beobachtungsebene so weit entfernt, dass sich die Beugungsverteilung näherungsweise nach Richtungen beschreiben lässt. Eine geeignet eingestellte Linse kann diese Richtungen auch in einer näheren Ebene auf verschiedene Stellen abbilden; das nennt man hier Fourierabbildung.") }
                P { Text("Bei einer solchen Fourierabbildung beschreibt "); MathText("L"); Text(" die bekannte Umrechnung vom Streuwinkel zur Bildposition. Es ist nicht automatisch der Abstand vom Spalt bis zum Foto. Ohne gesicherte Abbildungsgeometrie lässt sich die Formel nicht zur Kalibrierung eines vergrößerten Fotos verwenden. Das folgende Lehrmodell ist ein frei gewählter Fernfeldfall; die historische Aufnahme wird getrennt ausgewertet.") }
            }
        }
        Section(attrs = { elementId(JonssonArticleId.MODEL); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Spaltabstand und Spaltbreite verändern") }
            P {
                Text("Zeichnung, Kurve und Streifenbild zeigen dasselbe Modell. Öffne „Mit einer Vorhersage beginnen“ und notiere deine Vermutung. Verändere danach einen Parameter und vergleiche das Ergebnis mit ihr.")
            }
            P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Mit „Einstellung merken“ speicherst du einen Ausgangsfall für den Vergleich. Du kannst die Vergleichskurve aus- und wieder einblenden, während du die Regler weiter veränderst.") }
            JonssonLearningModel(session, viewState)
            materiewelle.learning.TutorHelpEntry(TutorHelpPoint.SLIT_MODEL, session, tutorView)
        }
        Section(attrs = { elementId(JonssonArticleId.MEASUREMENT); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Streifenabstand im Bild messen") }
            P {
                Text("Markiere zunächst vier benachbarte helle Streifenmaxima. Die vier Marken ergeben drei Abstände; dazwischen darf kein weiteres Maximum liegen.")
            }
            val image = data.jonsson
            if (image == null) {
                P(attrs = { role(AriaRole.ALERT); styles(LabClass.SOURCE_WARNING) }) {
                    Text("Für diesen Datensatz steht keine geprüfte Aufnahme zur Verfügung. Eine Bildmessung kann deshalb nicht angeboten werden.")
                }
            } else {
                JonssonMeasurement(
                    data = image,
                    owner = session,
                    profiles = profiles,
                    viewState = viewState,
                    embedded = true,
                    imageCaption = {
                        P {
                            Strong { Text("Historische Aufnahme aus Jönssons Veröffentlichung (J1). ") }
                            Text(if (viewState.wholeImage.value) "Gesamtbild: links Einspalt (Abb. 7), rechts Doppelspalt (Abb. 8). "
                                else "Hier: Doppelspaltausschnitt (Abb. 8). ")
                            Text("Reproduktion, keine Simulation und keine unverarbeiteten Detektordaten.")
                        }
                    },
                    help = { TutorHelpButton(TutorHelpPoint.IMAGE_MEASUREMENT, tutorView) },
                    helpPanel = {
                        TutorHelpPanel(TutorHelpPoint.IMAGE_MEASUREMENT, session, tutorView,
                            TutorPanelPlacement.BELOW_WORKSPACE)
                    },
                )
            }
            P {
                Text("Prüfe die einzelnen Abstände und lies ihren Mittelwert ab. Begründe die Auswahl des Bildstreifens und der Maxima. Mit „Erste Auswahl festhalten“ speicherst du diese Auswahl vor dem Referenzvergleich. Du kannst auch eine andere Anzahl benachbarter Maxima wählen, wenn du deine Entscheidung begründest.")
            }
            P {
                Text("Wiederhole die Auswahl in einem anderen horizontalen Bildstreifen, um ihre Empfindlichkeit zu prüfen. Dabei wertest du dieselbe Reproduktion erneut aus; es handelt sich nicht um eine unabhängige Wiederholungsmessung.")
            }
        }
        Section(attrs = { elementId(JonssonArticleId.LIMITS); focusIndex(-1); articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Was lässt sich daraus schließen?") }
            P { Text("1927 lieferte ein Kristall die periodische Struktur; 1961 waren es gezielt hergestellte Spalte. Damit ist Elektronenbeugung nicht auf die Anordnung der Atome in einem Kristall beschränkt. Die neue technische Leistung liegt darin, die winzigen künstlichen Öffnungen herzustellen, passend zu beleuchten und ihr Muster sichtbar zu machen.") }
            P {
                Text("Die Bildmessung bestimmt den mittleren Abstand der ausgewählten Streifen in Originalpixeln. Sie beschreibt damit die Periodizität des Musters in dieser Reproduktion. Für die Umrechnung in eine physikalische Länge fehlt ein unabhängiger Bildmaßstab. Um daraus eine Wellenlänge zu bestimmen, müssten außerdem die Abbildungsgeometrie und die zugehörigen Aufnahmeparameter gesichert sein.")
            }
            Details {
                Summary { Text("Warum bei hoher Spannung relativistisch rechnen?") }
                val voltage = Voltage(50_000.0)
                val nr = Physics.electronWavelength(voltage)
                val relativistic = Physics.relativisticElectronWavelength(voltage)
                val relativeDifference = Physics.relativeDifferencePercent(nr.meters, relativistic.meters)
                P {
                    Text("Für einen gesonderten Rechenfall bei "); MathText("50 kV")
                    Text(" liefert die nichtrelativistische Formel ungefähr "); MathText("${Physics.decimal(nr.picometers, 3)} pm")
                    Text(", die relativistische Rechnung ungefähr "); MathText("${Physics.decimal(relativistic.picometers, 3)} pm")
                    Text(". Die Überschätzung beträgt etwa "); MathText("${Physics.decimal(relativeDifference, 2)} %")
                    Text(" bezogen auf den relativistischen Wert. Die genaue Spannung dieser Aufnahme ist damit nicht nachgewiesen; Text und Aufbauzeichnung nennen unterschiedliche Fälle.")
                }
                Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.RELATIVISTIC) }
            }
            A(href = AppRoute.Course(materiewelle.core.course.StationId.S16_CONCLUSION).hash, attrs = {
                styles(LabClass.PRIMARY)
                onClick { it.preventDefault(); onNavigate(AppRoute.Course(materiewelle.core.course.StationId.S16_CONCLUSION)) }
            }) { Text("Zum Fazit") }
        }
        Section(attrs = { elementId(JonssonArticleId.SOURCES); focusIndex(-1); articleStyles(ArticleClass.SECTION, ArticleClass.SMALL) }) {
            H2 { Text("Woher stammt die Aufnahme?") }
            P {
                Text("J1: C. Jönsson, Electron Diffraction at Multiple Slits, American Journal of Physics 42 (1974), S. 4–11, insbesondere S. 9, Abb. 8. Gekürzte englische Fassung der Arbeit von 1961. Die hier verwendete Reproduktion stammt unverändert aus PDF-Seite 6, Bildobjekt 33; links liegt die Einspaltaufnahme, rechts die Zweispaltaufnahme.")
                Text(" "); A(href = "https://doi.org/10.1119/1.1987592") { Text("DOI und bibliografischer Nachweis") }
            }
            P {
                Text("Historischer Befund, berechnetes Lehrmodell und heutige eigene Bildauswertung sind getrennte Aussagen. Referenzmarken sind eine dokumentierte heutige Auswertung, keine automatisch selbst erbrachte Messung. Die Nutzungsgrundlage der historischen Reproduktion bleibt im Quellenregister als offener Punkt verzeichnet.")
            }
            data.jonsson?.let { JonssonSources(it, viewState.referenceVisible(session.snapshot.measurement)) }
        }
        Section(attrs = { articleStyles(ArticleClass.SECTION) }) {
            H2 { Text("Freiwillige Vertiefungen") }
            P { Text("Die folgenden Ansichten ergänzen das Lehrmodell. Für die historische Bildauswertung sind sie nicht erforderlich; die Trefferansicht geht über den Aufgabenrahmen hinaus.") }
            JonssonModelExtensions(session, viewState.exploration)
        }
    }
}

@Composable
private fun JonssonFringeDerivation() {
    H3 { Text("Vom Gangunterschied zum Streifenabstand") }
    P {
        Text("Wir betrachten zwei identische, gleichphasig beleuchtete Spalte im Fernfeld. ")
        MathText("g"); Text(" ist der Abstand ihrer Mitten, "); MathText("θ")
        Text(" der Winkel zur optischen Achse. Der Gangunterschied der beiden Wellenbeiträge beträgt ")
        MathText("g sin θ"); Text(". Für konstruktive Überlagerung muss er eine ganze Zahl ")
        MathText("n"); Text(" von Wellenlängen betragen:")
    }
    Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { MathText("Δs = g sin θ = nλ") }
    P {
        Text("Bei freier Ausbreitung ist "); MathText("L")
        Text(" der Abstand zwischen Spalten und Beobachtungsebene. Die Koordinate "); MathText("y")
        Text(" misst den Abstand von der Mitte. Für kleine Winkel sind Tangens und Sinus näherungsweise gleich:")
    }
    Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { MathText("y = L tan θ ≈ L sin θ") }
    P {
        Text("Beim nächsten Interferenzmaximum steigt "); MathText("n"); Text(" um eins. Damit wächst ")
        MathText("sin θ"); Text(" um "); MathText("λ/g")
        Text(". Für den Abstand benachbarter Interferenzmaxima folgt:")
    }
    Div(attrs = { articleStyles(ArticleClass.FORMULA) }) { InlineFormula(FormulaId.FRINGE_SPACING) }
    P(attrs = { articleStyles(ArticleClass.SMALL) }) {
        Text("Die Formel beschreibt den Interferenzfaktor. Die Beugungshülle kann Maxima des gesamten Musters verschieben oder unterdrücken. Im Modell darunter heißt die waagerechte Koordinate ")
        MathText("x"); Text("; der entsprechende Streifenabstand wird dort mit "); MathText("Δx")
        Text(" bezeichnet. Die spätere Bildmessung liefert dagegen Abstände in Originalpixeln und hat keinen gesicherten physikalischen Maßstab.")
    }
}

private fun jonssonSlitModel(session: LabSession) = FiniteSlitModel(
    Voltage(150.0), Length(10.0), Length(session.snapshot.modelSlitSpacingUm * 1e-6),
    Length(session.snapshot.modelSlitWidthUm * 1e-6), WavelengthApproximation.NONRELATIVISTIC,
)

/** Reuses the already persisted S13 model fields; no second copy of the experiment. */
@Composable
private fun JonssonLearningModel(session: LabSession, viewState: JonssonUiState) {
    val view = viewState.exploration
    val snapshot = session.snapshot
    val model = jonssonSlitModel(session)
    val visibleComparison = snapshot.inquiry.slitComparison.takeIf { viewState.slitComparisonVisible.value }
    SlitInvestigationView(model, snapshot.inquiry.slitComparison, view, readOnly = session.isReadOnly)
    Div(attrs = { elementId(Id.COURSE_MODEL_PLOT) }) {
        SlitComparisonView(model, visibleComparison, view.configuration, view.position, view::inspect) {
            Div(attrs = { slitLabStyles(SlitLabClass.TOOLBAR) }) {
                FieldLabel(SlitLabId.CONFIGURATION) { Text("Spalte") }
                Select(attrs = {
                    elementId(SlitLabId.CONFIGURATION)
                    onChange { event -> SlitConfiguration.entries.firstOrNull { it.name == event.value }?.let { view.configuration = it } }
                }) {
                    SlitConfiguration.entries.forEach { choice ->
                        Option(value = choice.name, attrs = { if (choice == view.configuration) selected() }) { Text(choice.label()) }
                    }
                }
            }
            FieldLabel(Id.COURSE_MODEL_G) {
                Text("Mittenabstand "); MathText("g"); Text(": ")
                MathText("${Physics.decimal(snapshot.modelSlitSpacingUm, 1)} µm")
            }
            Input(InputType.Range, attrs = {
                elementId(Id.COURSE_MODEL_G); protectPersonalWork(session.isReadOnly); minimum(1); maximum(5); increment(.1)
                value(snapshot.modelSlitSpacingUm)
                onInput { event -> event.value?.toDouble()?.let { g -> session.update { it.copy(modelSlitSpacingUm = g) } } }
            })
            FieldLabel(Id.COURSE_MODEL_B) {
                Text("Spaltbreite "); MathText("b"); Text(": ")
                MathText("${Physics.decimal(snapshot.modelSlitWidthUm, 1)} µm")
            }
            Input(InputType.Range, attrs = {
                elementId(Id.COURSE_MODEL_B); protectPersonalWork(session.isReadOnly); minimum(.1); maximum(.9); increment(.1)
                value(snapshot.modelSlitWidthUm)
                onInput { event -> event.value?.toDouble()?.let { b -> session.update { it.copy(modelSlitWidthUm = b) } } }
            })
            Div(attrs = { articleStyles(ArticleClass.ACTIONS) }) {
                Button(attrs = {
                    elementId(CourseControl.SLIT_SAVE); protectPersonalWork(session.isReadOnly); styles(LabClass.SECONDARY)
                    if (view.configuration != SlitConfiguration.BOTH_COHERENT) disabled()
                    onClick {
                        viewState.slitComparisonVisible.value = true
                        session.update { current -> current.copy(inquiry = current.inquiry.copy(
                            slitComparison = SlitModelComparison(spacingUm = current.modelSlitSpacingUm, widthUm = current.modelSlitWidthUm),
                        )) }
                    }
                }) { Text("Einstellung merken") }
                Button(attrs = {
                    elementId(CourseControl.SLIT_CLEAR); styles(LabClass.SECONDARY)
                    if (snapshot.inquiry.slitComparison == null) disabled()
                    ariaPressed(viewState.slitComparisonVisible.value && snapshot.inquiry.slitComparison != null)
                    onClick { viewState.slitComparisonVisible.value = !viewState.slitComparisonVisible.value }
                }) { Text(if (viewState.slitComparisonVisible.value) "Vergleich ausblenden" else "Vergleich anzeigen") }
            }
        }
    }
    P(attrs = { articleStyles(ArticleClass.SMALL) }) {
        Text("Die lang gestrichelte Kurve zeigt die Beugungshülle. An einer Hüllnullstelle wird diese Kurve null. ")
        MathText("Δx"); Text(" ist der für kleine Winkel berechnete Abstand benachbarter Maxima des Interferenzfaktors beim kohärenten Doppelspalt.")
    }
    if (view.configuration != SlitConfiguration.BOTH_COHERENT) P(attrs = { articleStyles(ArticleClass.SMALL) }) {
        if (snapshot.inquiry.slitComparison != null) Text("A bleibt der gespeicherte kohärente Zweispaltfall. ")
        Text("Zum Merken eines neuen Parametervergleichs wähle „Beide kohärent“. „Ohne Interferenz“ addiert Einzelintensitäten; es ist kein Modell eines bestimmten Welcher-Weg-Detektors.")
    }
    P(attrs = { elementId(SlitLabId.POSITION); slitLabStyles(SlitLabClass.POSITION) }) {
        Text("Wähle einen Ort in der Kurve oder im aktuellen Streifenband. Mit der Tastatur verschieben die Pfeiltasten die Auswahl; Pos1 wählt die Mitte. Ausgewählt: ")
        MathText("x = ${number(view.position.millimeters, 3)} mm")
    }
    P(attrs = { articleStyles(ArticleClass.SMALL) }) { Text("Die Zeigeransicht am Kapitelende erklärt, warum die ausgewählte Stelle im Modell hell oder dunkel ist.") }
}

@Composable
private fun JonssonModelExtensions(session: LabSession, view: SlitExplorationUiState) {
    val model = jonssonSlitModel(session)
    Details(attrs = { elementId(SlitLabId.PHASE_PANEL); detailsState(view.phaseOpen) { view.phaseOpen = it } }) {
        Summary { Text("Freiwillige Vertiefung · Warum ist diese Stelle hell oder dunkel?") }
        SlitPhaseView(model, view.position, view.configuration, view::inspect)
    }
    SlitDetectionView(model, view.configuration, view.detections)
}
