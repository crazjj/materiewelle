package materiewelle.data

/** Same-directory URLs stay relative so root and GitHub Pages exports use one contract. */
enum class LabAsset(val path: String) {
    HANDOUT_PDF("handout/materiewellen.pdf"),
    INLINE_MATH("math-text/catalog.json"),
    ORIGINAL_IMAGE("jonsson-page6-xref33.png"),
    SCIENTIFIC_DATA("versuchsdaten.json"), MEASUREMENT_PROFILES("joensson_sensitivitaet.json"),
    BROWSER_DOCUMENT("learning-docs/document.json"), PRESENTATION_PDF("learning-docs/praesentation.pdf"), TUTOR_MANIFEST("tutor/manifest.json"),
    TUTOR_START("tutor/START-IM-CHAT.md"), TUTOR_SKILL("tutor/materiewellen-tutor.zip");

    val fileName: String get() = path.substringAfterLast('/')
}
