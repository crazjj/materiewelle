package materiewelle.presentation

import materiewelle.presentation.styles.designStyles

@JsModule("node:fs")
@JsNonModule
private external object NodeFiles {
    fun writeFileSync(path: String, data: String)
}

private external val process: NodeProcess

private external interface NodeProcess {
    val argv: Array<String>
}

/** Build tool only: neither Node IO nor the style generator executes in the website. */
fun main() {
    val path = process.argv.getOrNull(2) ?: error("Output stylesheet path required")
    NodeFiles.writeFileSync(path, designStyles())
    println("Kotlin design compiled to $path")
}
