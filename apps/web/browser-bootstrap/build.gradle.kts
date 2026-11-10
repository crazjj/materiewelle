plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
    js {
        browser { commonWebpackConfig { outputFileName = "browser-init.js" } }
        binaries.executable()
    }
    sourceSets { jsMain.dependencies { implementation(project(":browser-design")) } }
}
