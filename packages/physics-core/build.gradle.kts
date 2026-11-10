import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

group = "materiewelle"
version = "0.2.0"

kotlin {
    jvm { compilerOptions.jvmTarget.set(JvmTarget.JVM_17) }
    js { browser(); nodejs() }
    sourceSets {
        commonMain.dependencies { implementation(libs.serialization.json) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<Test>().configureEach {
    systemProperty("materiewelle.scientificData", project.file("../../content/data/versuchsdaten.json").absolutePath)
    systemProperty("materiewelle.measurementProfiles", project.file("../../content/measurement/joensson_sensitivitaet.json").absolutePath)
}
