import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
    js { nodejs(); binaries.executable() }
    sourceSets {
        commonMain.dependencies {
            api(project(":physics-core"))
            api(libs.serialization.core)
        }
        jsMain.dependencies { implementation(libs.compose.html) }
        jsTest.dependencies { implementation(kotlin("test")) }
    }
}

val generatedStyles = layout.buildDirectory.file("generated/public/app.css")
tasks.named<NodeJsExec>("jsNodeProductionRun") {
    args(generatedStyles.get().asFile.absolutePath)
    outputs.file(generatedStyles)
    doFirst { generatedStyles.get().asFile.parentFile.mkdirs() }
}
