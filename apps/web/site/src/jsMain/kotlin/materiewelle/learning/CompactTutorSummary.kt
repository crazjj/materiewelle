package materiewelle.learning

/** Field roles stay explicit: calculation notes are never silently promoted to a question. */
internal data class TutorCopySummary(
    val questionPresent: Boolean,
    val ownWorkIncluded: Boolean,
    val ownWrittenWorkPresent: Boolean,
) {
    val questionText: String get() = if (questionPresent)
        "Deine Frage wird mitkopiert."
    else "Du kannst eine Frage ergänzen oder mit der gewählten Hilfeform beginnen."
}

private fun CompactOwnWork.hasWrittenWork(): Boolean = when (this) {
    is CompactOwnWork.DeBroglie -> requiredCalculation?.let { it.calculationNote.isNotBlank() || it.numericAnswer.isNotBlank() } == true || notes.isNotEmpty()
    is CompactOwnWork.Surface -> notes.isNotEmpty()
    is CompactOwnWork.Image -> selectionReason.isNotBlank() || notes.isNotEmpty() || measurement?.markersNativeX?.isNotEmpty() == true ||
        initialSelection != null || comparison != null || slitModel?.exploration?.notes?.values?.any { it.prediction.isNotBlank() || it.reason.isNotBlank() } == true
    is CompactOwnWork.Conclusion -> ownConclusion.isNotBlank() || claims.isNotEmpty() || relatedWork.any { it.hasWrittenWork() }
}

internal fun tutorCopySummary(question: String, own: CompactOwnWork, includeOwn: Boolean): TutorCopySummary =
    TutorCopySummary(question.isNotBlank(), includeOwn, own.hasWrittenWork())

internal fun tutorOwnWorkSelectionLabel(point: TutorHelpPoint? = null): String = when (point) {
    null -> "Meine Angaben aus diesem Kapitel hinzufügen"
    TutorHelpPoint.CALCULATION -> "Meine Rechnung bei 150 Volt hinzufügen"
    TutorHelpPoint.DE_BROGLIE_MODEL -> "Meine aktuellen und gemerkten Spannungswerte hinzufügen"
    TutorHelpPoint.DG_COMPARISON -> "Meine Angaben zum festen DG-Quellenvergleich hinzufügen"
    TutorHelpPoint.DG_MODEL -> "Meine aktuellen und gemerkten DG-Modellwerte hinzufügen"
    TutorHelpPoint.SLIT_MODEL -> "Meine Spaltmodellwerte und Erkundungsnotizen hinzufügen"
    TutorHelpPoint.IMAGE_MEASUREMENT -> "Meine Bildmessung und ihre Begründung hinzufügen"
    TutorHelpPoint.CONCLUSION -> "Mein Fazit und meine Belegfelder hinzufügen"
}

internal fun tutorOwnWorkSummary(own: CompactOwnWork, summary: TutorCopySummary): String {
    if (!summary.ownWorkIncluded) return "Deine eigenen Angaben werden nicht mitkopiert."
    return when (own) {
        is CompactOwnWork.DeBroglie -> buildString {
            append("Enthalten: ")
            val items = buildList {
                own.requiredCalculation?.let { task ->
                    add(if (task.numericAnswer.isBlank() && task.calculationNote.isBlank()) "die noch leere 150-V-Aufgabe" else "deine 150-V-Rechnung")
                }
                own.modelVariation?.let { model ->
                    add("Werte des freien Modells: aktuell ${compactNumber(model.acceptedVoltageV)} V" +
                        (model.rememberedVoltageV?.let { ", gemerkt ${compactNumber(it)} V" } ?: ", keine gemerkte Einstellung"))
                }
                if (own.notes.isNotEmpty()) add("${own.notes.size} weitere Kapitelnotizen")
            }
            append(items.joinToString("; ")); append(".")
            own.modelVariation?.let { model ->
                if (model.editedVoltage.trim().replace(',', '.').toDoubleOrNull() != model.acceptedVoltageV)
                    append(" Eingegeben: ${model.editedVoltage.ifBlank { "leer" }}; im Modell verwendet: ${compactNumber(model.acceptedVoltageV)} V.")
            }
        }
        is CompactOwnWork.Surface -> buildString {
            append("Enthalten: ")
            val items = buildList {
                own.modelVariation?.let { model ->
                    add("aktuelle Modellwerte ${compactNumber(model.acceptedVoltageV)} V / ${compactNumber(model.acceptedAngleDegrees)}°")
                    model.remembered?.let { add("gemerkte Modellwerte ${compactNumber(it.voltageV)} V / ${compactNumber(it.angleDegrees)}°") }
                }
                if (own.notes.isNotEmpty()) add("deine Angaben zum DG-Vergleich")
                else if (own.modelVariation == null) add("noch keine eigenen Eingaben zum dokumentierten Davisson–Germer-Zahlenfall")
            }
            append(items.joinToString("; ")); append(".")
        }
        is CompactOwnWork.Image -> buildString {
            val items = buildList {
                own.slitModel?.let { model ->
                    add("Spaltmodell aktuell g=${compactNumber(model.current.spacingUm)} µm, b=${compactNumber(model.current.widthUm)} µm")
                    model.remembered?.let { add("gemerkt g=${compactNumber(it.spacingUm)} µm, b=${compactNumber(it.widthUm)} µm") }
                    if (!model.exploration?.notes.isNullOrEmpty()) add("deine Erkundungsnotizen")
                }
                if (own.includeMeasurement) {
                    own.measurement?.let { m ->
                        add("aktuelles Bildband ${m.bandYStart}–${m.bandYEndExclusive} px und ${m.markersNativeX.size} aktuelle Marken")
                        if (m.referenceViewed || !m.referenceMaterialsOpened.isNullOrEmpty()) add("gespeicherte Referenzvermerke")
                    }
                    if (own.selectionReason.isNotBlank()) add("deine aktuelle Begründung")
                    own.initialSelection?.let { add("deine festgehaltene Auswahl (${it.measurement.markersNativeX.size} Marken)") }
                    own.comparison?.let { add("dein gespeicherter Messvergleich (${it.measurement.markersNativeX.size} Marken)") }
                }
                if (own.notes.isNotEmpty()) add("deine zugehörigen Jönsson-Notizen")
            }
            if (items.isEmpty()) append("Du hast hier noch keine eigenen Angaben eingetragen.")
            else { append("Enthalten: "); append(items.joinToString("; ")); append(".") }
            if (own.includeMeasurement) {
                if (own.measurement == null) append(" Für dieses Bild ist kein passender Messkontext verfügbar.")
                else if (own.measurement.markersNativeX.isEmpty() && own.selectionReason.isBlank())
                    append(" Die aktuelle Bildauswahl ist leer; gespeicherte Auswahlen werden getrennt bezeichnet.")
                append(" Das Bild selbst wird nicht mitkopiert.")
            }
        }
        is CompactOwnWork.Conclusion -> buildString {
            val items = buildList {
                if (own.ownConclusion.isNotBlank()) add("dein Fazit")
                if (own.claims.isNotEmpty()) add("deine Belegfelder ${own.claims.joinToString { it.number.toString() }}")
                if (own.relatedWork.isNotEmpty()) add("die zusätzlich freigegebenen früheren Ergebnisse")
            }
            if (items.isEmpty()) append("Du hast hier noch keine eigenen Angaben eingetragen.")
            else { append("Enthalten: "); append(items.joinToString(" und ")); append(".") }
        }
    }
}

/** Preview bullets expose the selected values, not a second hidden prompt. */
internal fun tutorOwnWorkDetails(own: CompactOwnWork): List<String> = buildList {
    fun noteDetails(notes: List<CompactNote>) {
        notes.forEach { note ->
            val title = compactNoteLabel(note)
            if (note.numericAnswer.isNotBlank()) add("$title · Ergebnis: ${note.numericAnswer}")
            if (note.prediction.isNotBlank()) add("$title · Vorhersage: ${note.prediction}")
            if (note.note.isNotBlank()) add("$title · Notiz: ${note.note}")
        }
    }
    when (own) {
        is CompactOwnWork.DeBroglie -> {
            own.requiredCalculation?.let { task ->
                add("150-V-Aufgabe · Ergebnis: ${task.numericAnswer.ifBlank { "noch leer" }}")
                add("Rechenweg: ${task.calculationNote.ifBlank { "noch leer" }}")
            }
            own.modelVariation?.let { model ->
                add("Spannungsmodell B: ${compactNumber(model.acceptedVoltageV)} V; Eingabetext: ${model.editedVoltage.ifBlank { "leer" }}")
                add(model.rememberedVoltageV?.let { "Gemerkte Einstellung A: ${compactNumber(it)} V" } ?: "Gemerkte Einstellung A: keine")
            }
            noteDetails(own.notes)
        }
        is CompactOwnWork.Surface -> {
            own.modelVariation?.let { model ->
                add("DG-Modell B: ${compactNumber(model.acceptedVoltageV)} V / ${compactNumber(model.acceptedAngleDegrees)}° zur Normalen")
                add("Eingabetexte: ${model.editedVoltage.ifBlank { "leer" }} V / ${model.editedAngle.ifBlank { "leer" }}°")
                add(model.remembered?.let { "Gemerkte Einstellung A: ${compactNumber(it.voltageV)} V / ${compactNumber(it.angleDegrees)}°" } ?: "Gemerkte Einstellung A: keine")
            }
            noteDetails(own.notes)
            if (own.modelVariation == null && own.notes.isEmpty()) add("Dokumentierter Davisson–Germer-Zahlenfall: noch keine eigenen Eingaben")
        }
        is CompactOwnWork.Image -> {
            own.slitModel?.let { model ->
                add("Spaltmodell B: g=${compactNumber(model.current.spacingUm)} µm, b=${compactNumber(model.current.widthUm)} µm, U=${compactNumber(model.current.voltageV)} V, L=${compactNumber(model.current.distanceM)} m")
                add(model.remembered?.let { "Gemerkte Einstellung A: g=${compactNumber(it.spacingUm)} µm, b=${compactNumber(it.widthUm)} µm, U=${compactNumber(it.voltageV)} V, L=${compactNumber(it.distanceM)} m" } ?: "Gemerkte Spalteinstellung A: keine")
                model.exploration?.let { exploration ->
                    add("Modellansicht: ${exploration.configuration.label()}; Aufgabe: ${exploration.task.label()}")
                    exploration.observationMeters?.let { add("Beobachtungsposition im Modell: ${compactNumber(it * 1000.0)} mm") }
                    CompactSlitTask.entries.forEach { task -> exploration.notes[task]?.let { notes ->
                        if (notes.prediction.isNotBlank()) add("${task.label()} · Vorhersage: ${notes.prediction}")
                        if (notes.reason.isNotBlank()) add("${task.label()} · Erklärung: ${notes.reason}")
                    } }
                }
            }
            if (own.includeMeasurement) {
                own.measurement?.let { m ->
                    add("Aktuelles Bildband: ${m.bandYStart} ≤ y < ${m.bandYEndExclusive} Originalpixel")
                    add(if (m.markersNativeX.isEmpty()) "Aktuelle Bildmarken: keine" else "Aktuelle Bildmarken x / px: ${m.markersNativeX.joinToString { compactNumber(it) }}")
                    add("Referenzaufruf im gespeicherten Stand: ${if (m.referenceViewed) "ja" else "nein"}")
                } ?: add("Für die aktuelle Aufnahme ist kein geprüftes Messraster verfügbar")
                if (own.selectionReason.isNotBlank()) add("Aktuelle Begründung: ${own.selectionReason}")
                own.initialSelection?.let { add("Erste festgehaltene Auswahl x / px: ${it.measurement.markersNativeX.joinToString { x -> compactNumber(x) }}") }
                own.comparison?.let { add("Gespeicherter Messvergleich A, x / px: ${it.measurement.markersNativeX.joinToString { x -> compactNumber(x) }}") }
                add("Das historische Bild selbst wird nicht mitkopiert")
            }
            noteDetails(own.notes)
        }
        is CompactOwnWork.Conclusion -> {
            add("Fazit: ${own.ownConclusion.ifBlank { "noch leer" }}")
            own.claims.forEach { claim ->
                if (claim.row.claim.isNotBlank()) add("Aussage ${claim.number}: ${claim.row.claim}")
                if (claim.row.evidence.isNotBlank()) add("Beleg ${claim.number}: ${claim.row.evidence}")
                if (claim.row.limitation.isNotBlank()) add("Grenze ${claim.number}: ${claim.row.limitation}")
            }
            own.relatedWork.forEach { related -> addAll(tutorOwnWorkDetails(related)) }
        }
    }
}.map { line ->
    val singleLine = line.replace('\n', ' ').replace('\r', ' ')
    if (singleLine.length <= 160) singleLine else singleLine.take(159) + "…"
}
