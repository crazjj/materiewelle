package materiewelle.course

import androidx.compose.runtime.*
import materiewelle.core.*
import materiewelle.core.course.*
import materiewelle.course.content.CourseContent
import materiewelle.state.LabSession
import materiewelle.ui.PlainTextCopy
import materiewelle.ui.PlainTextCopyIds
import materiewelle.ui.MathText as Text
import materiewelle.ui.semantics.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

/**
 * A live reading view of the same work; no parallel journal and no generated personal conclusion.
 */
@Composable
fun EvidenceReport(session: LabSession, data: ScientificData?) {
    val report = session.snapshot.reportForReading(data)
    var includeTechnicalAppendix by remember { mutableStateOf(false) }
    val copyText = report.readableText(includeTechnicalAppendix)
    Section(
        attrs = {
            elementId(EvidenceReportId.ROOT)
            evidenceReportStyles(EvidenceReportClass.ROOT)
        }
    ) {
        H3 { Text("Dein Arbeitsbericht") }
        P { Text("Der Bericht enthält deine eingetragenen Rechnungen, Bildmarken und Texte sowie die Quellenangaben.") }
        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(0.5.cssRem); minHeight(44.px) } }) {
            Input(InputType.Checkbox, attrs = {
                checked(includeTechnicalAppendix)
                onChange { includeTechnicalAppendix = it.value }
            })
            Text("Technischen Anhang mitkopieren und herunterladen")
        }
        P(attrs = { styles(LabClass.SMALL) }) { Text("Der freiwillige Anhang enthält technische Prüfvermerke, Dateikennungen und erfasste Hilfeaufrufe. Er bewertet deine Arbeit nicht.") }
        Div(attrs = { evidenceReportStyles(EvidenceReportClass.ACTIONS) }) {
            PlainTextCopy(
                text = copyText,
                ids = PlainTextCopyIds(EvidenceReportId.COPY, EvidenceReportId.COPY_STATUS,
                    EvidenceReportId.COPY_PREVIEW, EvidenceReportId.COPY_SELECT),
                label = "Arbeitsbericht kopieren",
                previewLabel = "Kopiertext ansehen",
                successMessage = "Arbeitsbericht kopiert.",
                primary = true,
            )
            Button(
                attrs = {
                    elementId(EvidenceReportId.DOWNLOAD)
                    styles(LabClass.SECONDARY)
                    onClick { downloadText(copyText, EvidenceReportDownload.FILENAME) }
                }
            ) {
                Text("Als Textdatei herunterladen")
            }
        }
        P(attrs = { evidenceReportStyles(EvidenceReportClass.STATUS) }) {
            Text("Deine Eingaben bleiben in diesem Browser; der Website-Link enthält sie nicht. Zum Aufbewahren oder Teilen kannst du den Bericht kopieren oder herunterladen.")
        }
        EvidenceReportContents(report)
    }
}

/** Both report and local rescue use the same available bibliography and verification data. */
internal fun SessionSnapshot.reportForReading(data: ScientificData? = null): PersonalEvidenceReport {
    val courseSources = CourseContent.allSources.associate {
        it.id to SourceReference(status = "${it.location.trimEnd('.')}. ${it.status}", url = it.url)
    }
    val bibliography = CourseContent.reportBibliography.associate {
        it.id to SourceReference(status = "${it.location.trimEnd('.')}. ${it.status}", url = it.url)
    }
    return personalEvidenceReport(data, courseSources, bibliography)
}

@Composable
private fun EvidenceReportContents(report: PersonalEvidenceReport) {
    val optionalClaims = report.enteredClaims
        if (!report.hasOwnWork)
            P(
                attrs = {
                    elementId(EvidenceReportId.EMPTY)
                    styles(LabClass.COURSE_RESULT)
                }
            ) {
                Text(
                    "Hier hast du noch nichts eingetragen. Beginne mit einer eigenen Rechnung oder Bildmessung in den Kapiteln."
                )
            }
        P(attrs = { evidenceReportStyles(EvidenceReportClass.STATUS) }) {
            Text(
                "Die Website bewertet deine freien Erklärungen nicht automatisch."
            )
        }
        Div {
            Section(attrs = { elementId(EvidenceReportId.CALCULATIONS) }) {
                H4 { Text("Rechnen · deine Antworten") }
                if (report.calculations.isEmpty() && report.dgComparison == null)
                    P {
                        Text(
                            "Noch keine eigene Zahlenantwort oder angeforderte Kontrolle vorhanden."
                        )
                    }
                Div(attrs = { evidenceReportStyles(EvidenceReportClass.GRID) }) {
                    report.calculations.forEachIndexed { index, entry ->
                        Section(attrs = { evidenceReportStyles(EvidenceReportClass.CARD) }) {
                            H4 {
                                Text(
                                    "${entry.role.label} · ${Physics.decimal(entry.voltage.volts, 0)} V"
                                )
                            }
                            ReportTranscript(
                                "Deine Zahlenantwort",
                                entry.answer,
                                ReportTextId(ReportTextKind.CALCULATION, index),
                            )
                            P {
                                Text(
                                    entry.check?.feedback ?: "Zahlenprüfung noch nicht angefordert."
                                )
                            }
                            Details {
                                Summary { Text("Hinweise zur Bearbeitung") }
                                P {
                                    Text(
                                        if (entry.solutionViewed)
                                            "Ein Öffnen der Kontrolllösung ist vermerkt. Das sagt nicht, ob die spätere Antwort ohne diese Hilfe entstanden ist."
                                        else
                                            "Es ist kein Öffnen der Kontrolllösung vermerkt. Zuvor bekannte Hilfen sind dadurch nicht ausgeschlossen."
                                    )
                                }
                                if (entry.role != CalculationRole.PRACTICE)
                                    P { Text("Für diesen Fall gibt es keine getrennte Erstabgabe oder vollständige Antwortfolge.") }
                            }
                        }
                    }
                }
                report.dgComparison?.let { entry ->
                    H4 { Text("Davisson–Germer · dein Vergleich des dokumentierten Befunds") }
                    ReportTranscript("Vorhersage aus der Spannung", entry.prediction.ifBlank { "noch offen" }, ReportTextId(ReportTextKind.DG_PREDICTION))
                    ReportTranscript("Ergebnis aus der Beugungsgeometrie", entry.geometricAnswer.ifBlank { "noch offen" }, ReportTextId(ReportTextKind.DG_GEOMETRY))
                    ReportTranscript("Deine Einordnung", entry.interpretation.ifBlank { "noch offen" }, ReportTextId(ReportTextKind.DG_INTERPRETATION))
                    P { Text("Dein Wortlaut bleibt erhalten; freie Rechenwege werden nicht automatisch bewertet.") }
                    Details {
                        Summary { Text("Hinweise zum Arbeitsverlauf") }
                        P { Text("Mindestens eine Kontrolle als geöffnet vermerkt: ${if (entry.controlViewed) "ja" else "nein"}. Das ist keine vollständige Hilfereihenfolge.") }
                    }
                }
                if (report.helpContacts.isNotEmpty()) {
                    Details(attrs = { styles(LabClass.NOTES) }) {
                        Summary { Text("Technischer Anhang · protokollierte Hilfen") }
                        report.helpContacts.forEach { help ->
                            P {
                                Text(
                                    "Station ${help.station.number}: Hinweiszähler ${help.displayedHintCount} (begrenzt auf 10); Hauptkontrolle als geöffnet protokolliert: ${if (help.solutionViewed) "ja" else "nein"}; Vergleichskontrolle als geöffnet protokolliert: ${if (help.transferSolutionViewed) "ja" else "nein"}."
                                )
                            }
                        }
                        P {
                            Text(
                                "Hilfenutzung ist erlaubt. Diese Einträge zeigen weder den Zeitpunkt noch die vollständige Reihenfolge der Arbeit. Ohne vermerkte Öffnung können Hilfen trotzdem schon bekannt gewesen sein; frühere Syntaxhinweise konnten Kontrollwerte ohne Eintrag zeigen."
                            )
                        }
                    }
                }
            }
            Section(attrs = { elementId(EvidenceReportId.NOTES) }) {
                H4 { Text("Vorhersagen und Erklärungen") }
                if (report.notes.isEmpty())
                    P { Text(report.emptyNotesMessage) }
                report.notes.forEach { note ->
                    Details(attrs = { styles(LabClass.NOTES) }) {
                        Summary {
                            Text(
                                CourseContent.stations.single { it.id == note.station }.title
                            )
                        }
                        if (note.prediction.isNotBlank())
                            ReportTranscript(
                                "Deine Vorhersage",
                                note.prediction,
                                ReportTextId(ReportTextKind.PREDICTION, note.station.number),
                            )
                        if (note.explanation.isNotBlank())
                            ReportTranscript(
                                "Deine Erklärung",
                                note.explanation,
                                ReportTextId(ReportTextKind.EXPLANATION, note.station.number),
                            )
                    }
                }
            }
            Section(attrs = { elementId(EvidenceReportId.MEASUREMENT) }) {
                H4 { Text("Messen · eigener Bildbefund") }
                P { Text(report.imageBinding.label) }
                val m = report.measurement
                if (m == null)
                    P { Text("Noch keine eigene Bildauswahl oder Begründung eingetragen.") }
                else {
                    P {
                        Text(
                            "Aktueller Arbeitsstand · ${m.band.label}; ${m.markers.size} eigene Marken im Originalraster."
                        )
                    }
                    val intervals = m.markers.zipWithNext { a, b -> b.xPx - a.xPx }
                    if (intervals.isNotEmpty())
                        P {
                            Text(
                                "${intervals.size} Einzelabstände: ${intervals.joinToString { Physics.decimal(it.toDouble(), 0) }} Originalpixel; Mittel rund ${Physics.decimal(intervals.average(), 3)} Originalpixel. Die Nachkommastellen belegen keine Messgenauigkeit."
                            )
                        }
                    P {
                        Text(
                            if (m.initialSelection == null)
                                "Erste eigene Auswahl noch nicht gesichert."
                            else
                                "Erste eigene Auswahl gesichert; ihr ursprünglicher Zustand bleibt getrennt erhalten."
                        )
                    }
                    P {
                        Text(
                            if (m.comparison == null) "Noch kein eigener Vergleich gespeichert."
                            else
                                "Eigener Vergleich vorhanden; der Kopiertext enthält seine ursprünglichen Marken und die Begründung. Vermerkte Hilfen und Dateianfragen stehen im optionalen technischen Anhang."
                        )
                    }
                    if (!m.justificationMatchesBand)
                        P(attrs = { styles(LabClass.ERROR) }) {
                            Text(
                                "Die Begründung ist dem aktuellen Band noch nicht zugeordnet. Prüfe sie erneut im Messlabor."
                            )
                        }
                }
                P {
                    Text(
                        "Originalpixel und Grauwerte der Reproduktion sind keine unabhängige absolute Wellenlängenmessung. Einzelabstände und Bildlage prüfen; das Mittel allein bestätigt keine Maximalwahl."
                    )
                }
                Details {
                    Summary { Text("Vermerkte Hilfen und Dateianfragen") }
                    P { Text(if (report.laboratoryReferenceViewed)
                        "Ein Öffnen der Laborreferenz ist vermerkt. Spätere Entscheidungen können dadurch beeinflusst sein."
                    else "Es ist kein Öffnen der Laborreferenz vermerkt. Zuvor bekannte Referenzen sind dadurch nicht ausgeschlossen.") }
                    P { Text(if (report.currentReferenceMaterials.isEmpty())
                        "Keine weiteren Zugriffe vermerkt."
                    else ReferenceMaterial.entries.filter { it in report.currentReferenceMaterials }.joinToString { it.label }) }
                    P { Text("Eine Dateianfrage belegt weder einen erfolgreichen Download noch Lesen. Direkte PDF-Aufrufe und ältere Zugriffe außerhalb der Erfassung fehlen im Protokoll.") }
                }
                report.image?.let {
                    Details(attrs = { styles(LabClass.NOTES) }) {
                        Summary { Text("Technischer Anhang · Bildidentität") }
                        P(attrs = { evidenceReportStyles(EvidenceReportClass.IDENTIFIER) }) {
                            Text("Bild SHA-256: ${it.sha256}")
                        }
                    }
                }
            }
            if (optionalClaims.isNotEmpty()) Section(attrs = { elementId(EvidenceReportId.CLAIMS) }) {
                H4 { Text("Freiwillige Aussagen mit Belegen") }
                optionalClaims.forEach { (index, row) ->
                    Section(attrs = { evidenceReportStyles(EvidenceReportClass.CARD) }) {
                        H4 { Text("Aussage ${index + 1}") }
                        ReportTranscript(
                            "Behauptung",
                            row.claim,
                            ReportTextId(ReportTextKind.CLAIM, index),
                            emptyValue = "(nicht ausgefüllt)",
                        )
                        ReportTranscript(
                            "Eigener Beleg",
                            row.evidence,
                            ReportTextId(ReportTextKind.EVIDENCE, index),
                            emptyValue = "(nicht ausgefüllt)",
                        )
                        ReportTranscript(
                            "Annahme oder Grenze",
                            row.limitation,
                            ReportTextId(ReportTextKind.LIMITATION, index),
                            emptyValue = "(nicht ausgefüllt)",
                        )
                    }
                }
            }
            Section(attrs = { elementId(EvidenceReportId.CONCLUSION) }) {
                H4 { Text("Persönliches Fazit") }
                ReportTranscript(
                    "Dein Fazit",
                    report.conclusion,
                    ReportTextId(ReportTextKind.CONCLUSION),
                )
                P {
                    Text(
                        "Erkläre eine deiner Aussagen anschließend ohne Bildschirm und an einem veränderten Beispiel."
                    )
                }
            }
            Section(attrs = { elementId(EvidenceReportId.SOURCES) }) {
                H4 { Text("Quellen und Prüfstatus") }
                P {
                    Text(
                        "DG verwendet den in D2 sekundär berichteten Zahlenfall. Das historische Original D1 wurde nicht eingesehen. Eigene Modellparameter sind keine historischen Messdaten; das Jönsson-Bild besitzt hier keinen unabhängigen absoluten Maßstab."
                    )
                }
                if (report.bibliography.isEmpty())
                    P { Text("Bibliografische Kursangaben derzeit nicht geladen; die Quellenbereiche der Kapitel enthalten die Nachweise.") }
                report.bibliography.entries.sortedBy { it.key }.forEach { (id, source) ->
                    P { Text("$id · ${source.status}") }
                    source.url?.let {
                        P { A(href = it, attrs = { externalLink() }) { Text("Quelle $id öffnen") } }
                    }
                }
                Details(attrs = { styles(LabClass.NOTES) }) {
                    Summary { Text("Technischer Anhang · Datenprovenienz") }
                    P { Text("Originale Prüfvermerke der Datenquelle; getrennt von den bibliografischen Angaben oben.") }
                    report.sources.entries.sortedBy { it.key }.forEach { (id, source) ->
                        P { Text("$id · ${source.status}") }
                        source.path?.let { P { Text("Fundstelle im Projekt: $it") } }
                    }
                }
                P {
                    Text(
                        "Halte fest, wofür du KI genutzt und was du selbst nachgerechnet, an Quellen geprüft und erklärt hast. Der Bericht allein belegt diese Arbeit nicht."
                    )
                }
            }
        }
}

/**
 * Read user-authored work with the same Typst renderer as the lesson. The original source stays
 * unchanged in session state and exports; it is never interpreted as trusted HTML or Typst code.
 */
@Composable
private fun ReportTranscript(label: String, value: String, id: ReportTextId, emptyValue: String = "noch offen") {
    if (value.isBlank()) P { Text("$label: $emptyValue.") }
    else {
        P(attrs = { styles(LabClass.SMALL, LabClass.MUTED) }) { Text(label) }
        Div(
            attrs = {
                elementId(id)
                evidenceReportStyles(EvidenceReportClass.TRANSCRIPT)
                role(AriaRole.GROUP)
                ariaLabel(label)
            }
        ) {
            Text(value)
        }
    }
}
