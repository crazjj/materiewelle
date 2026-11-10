package materiewelle.presentation

/** Shared family names keep CSS and Canvas-measured SVG labels in the same typeface. */
enum class AppTypeface(val family: String, val css: String) {
    TEXT("Latin Modern Roman", "'Latin Modern Roman',Georgia,'Times New Roman',serif"),
    MONO("Latin Modern Mono", "'Latin Modern Mono','Courier New',monospace"),
}
