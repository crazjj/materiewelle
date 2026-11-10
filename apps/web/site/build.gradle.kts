import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.unsafe

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kobweb.application)
}

group = "materiewelle"
version = "0.1.0"
kobweb {
    app {
        index {
            lang.set("de")
            description.set("Materiewellen: Vorhersage, Rechnung und historische Befunde.")
            head.add {
                script {
                    unsafe {
                        +("//<![CDATA[\n" + rootProject.file("browser-bootstrap/build/kotlin-webpack/js/productionExecutable/browser-init.js")
                            .readLines().filterNot { it.startsWith("//# sourceMappingURL=") }.joinToString("\n") + "\n//]]>")
                    }
                }
                link(rel = "stylesheet", href = "app.css")
            }
        }
    }
}
kotlin {
    configAsKobwebApplication("materiewellen")
    // Pure UI contracts can be checked without a DOM or a browser download.
    js { nodejs() }
    sourceSets {
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            implementation(project(":physics-core"))
            implementation(project(":browser-design"))
            implementation(libs.serialization.json)
        }
        jsTest.dependencies { implementation(kotlin("test")) }
    }
}

// Each generator owns disjoint outputs; text edits cannot rebuild unrelated handouts or data.
val productPublic = layout.buildDirectory.dir("generated/materiewelle-product/public")
val syncScientificData by tasks.registering(Exec::class) {
    inputs.file(rootProject.file("../../content/data/versuchsdaten.json"))
    inputs.file(rootProject.file("../../content/sources/jonsson-page6-xref33.png"))
    inputs.file(rootProject.file("scripts/sync-data.py"))
    inputs.file(rootProject.file("../../content/measurement/joensson_sensitivitaet.json"))
    inputs.file(rootProject.file("../../content/measurement/sensitivitaet.py"))
    outputs.files(productPublic.map { it.file("versuchsdaten.json") },
        productPublic.map { it.file("jonsson-page6-xref33.png") },
        productPublic.map { it.file("joensson_sensitivitaet.json") })
    commandLine("python3", rootProject.file("scripts/sync-data.py").absolutePath, "--data-only")
}
val generateDisplayMath by tasks.registering(Exec::class) {
    inputs.files(rootProject.fileTree("../../content/typst/math") {
        include("*.py", "*.typ", "*.json"); exclude("manifest.json")
    })
    inputs.file(rootProject.file("../../content/typst/vorlage.typ"))
    inputs.file(rootProject.file("../../content/typst/auswahl.json"))
    inputs.files(fileTree("src/jsMain/resources/public/fonts") { include("*.otf") })
    outputs.dir(productPublic.map { it.dir("math") })
    commandLine("python3", rootProject.file("../../content/typst/math/build.py").absolutePath,
        "--output-dir", productPublic.get().dir("math").asFile.absolutePath)
}
val generateTutorPack by tasks.registering(Exec::class) {
    inputs.file(rootProject.file("../../content/tutor/build-compact-pack.py"))
    inputs.files(rootProject.fileTree("../../content/tutor/materiewellen-fachtutor") { include("**/*.md") })
    outputs.dir(productPublic.map { it.dir("tutor") })
    commandLine("python3", rootProject.file("../../content/tutor/build-compact-pack.py").absolutePath,
        "--output-dir", productPublic.get().dir("tutor").asFile.absolutePath)
}
val generateHandout by tasks.registering(Exec::class) {
    inputs.files(rootProject.files("../../content/typst/build-handout.py", "../../content/typst/handout.typ",
        "../../content/typst/fachwerte.typ", "../../content/typst/vorlage.typ", "../../content/data/versuchsdaten.json",
        "../../content/sources/jonsson-page6-xref33.png", "../../content/typst/auswahl.json"))
    inputs.files(rootProject.fileTree("../../content/typst/handoutassets") { include("*.svg") })
    inputs.files(fileTree("src/jsMain/resources/public/fonts") { include("*.otf") })
    outputs.dir(productPublic.map { it.dir("handout") })
    commandLine("python3", rootProject.file("../../content/typst/build-handout.py").absolutePath,
        "--output-dir", productPublic.get().dir("handout").asFile.absolutePath, "--replace-generated")
}
val generateInlineMath by tasks.registering(Exec::class) {
    inputs.file(rootProject.file("scripts/build-math-text.py"))
    inputs.files(fileTree("src/jsMain/kotlin") { include("**/*.kt") })
    inputs.files(fileTree("src/jsMain/resources/public/fonts") { include("*.ttf", "*.otf") })
    inputs.files(rootProject.fileTree("browser-design/src/commonMain/kotlin") { include("**/*.kt") })
    inputs.files(rootProject.fileTree("../../packages/physics-core/src/commonMain/kotlin") { include("**/*.kt") })
    inputs.files(rootProject.files("../../content/data/versuchsdaten.json", "../../content/typst/math/formulas.json"))
    outputs.dir(productPublic.map { it.dir("math-text") })
    commandLine("python3", rootProject.file("scripts/build-math-text.py").absolutePath)
}
kotlin.sourceSets.named("jsMain") {
    resources.srcDir(layout.buildDirectory.dir("generated/materiewelle-product"))
}
tasks.named("jsProcessResources") {
    dependsOn(syncScientificData, generateDisplayMath, generateTutorPack, generateHandout, generateInlineMath)
}

val verifyUiContract by tasks.registering(Exec::class) {
    inputs.files(fileTree("src/jsMain/kotlin") { include("**/*.kt") })
    inputs.files(rootProject.fileTree("browser-design/src"), rootProject.fileTree("browser-bootstrap/src"))
    inputs.file(rootProject.file("scripts/check-ui-contract.py"))
    commandLine("python3", rootProject.file("scripts/check-ui-contract.py").absolutePath)
}
tasks.named("compileKotlinJs") { dependsOn(verifyUiContract) }

val collectKotlinPresentation by tasks.registering(Sync::class) {
    dependsOn(":browser-design:jsNodeProductionRun", ":browser-bootstrap:jsBrowserProductionWebpack")
    from(project(":browser-design").layout.buildDirectory.dir("generated/public")) { into("public") }
    from(project(":browser-bootstrap").layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")) {
        include("browser-init.js"); into("public")
    }
    into(layout.buildDirectory.dir("generated/kotlin-presentation"))
}
kotlin.sourceSets.named("jsMain") { resources.srcDir(collectKotlinPresentation) }
tasks.named("jsProcessResources") { dependsOn(collectKotlinPresentation) }
tasks.named("kobwebGenSiteIndex") { dependsOn(":browser-bootstrap:jsBrowserProductionWebpack") }
