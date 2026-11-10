package materiewelle.learning

import materiewelle.ui.semantics.CompactTutorId

/** The learner chooses the depth of help; an explicit question can refine it. */
enum class TutorIntent(val id: CompactTutorId, val label: String, val instruction: String, val defaultQuestion: String) {
    HINT(CompactTutorId.HINT, "Hinweis",
        "Gib mir nur einen nächsten hilfreichen Schritt, ohne die fertige Lösung oder den Ergebniswert vorwegzunehmen. Meine konkrete Frage bestimmt, wo ich Hilfe brauche.",
        "Hilf mir mit einem ersten Schritt bei dieser Aufgabe."),
    EXPLANATION(CompactTutorId.EXPLANATION, "Erklärung",
        "Erkläre den erfragten Zusammenhang verständlich und schrittweise. Verbinde Formeln mit ihrer physikalischen Bedeutung und den nötigen Annahmen.",
        "Erkläre mir den Kern dieses Themas und wie die einzelnen Schritte zusammenhängen."),
    CHECK(CompactTutorId.CHECK, "Ansatz prüfen",
        "Prüfe meine tatsächlich beigefügten Angaben. Benenne zuerst einen konkreten richtigen Gedanken oder die erste begründbare Schwierigkeit. Erfinde keine Bearbeitung; fehlen entscheidende Angaben, frage gezielt danach.",
        "Prüfe meinen Ansatz und zeige mir, was ich als Nächstes verbessern kann."),
}
