pluginManagement { repositories { gradlePluginPortal(); mavenCentral(); google() } }
dependencyResolutionManagement { repositories { mavenCentral(); google() } }
rootProject.name = "materiewellen-kobweb"
include(":site")
include(":physics-core")
include(":browser-design", ":browser-bootstrap")
project(":physics-core").projectDir = file("../../packages/physics-core")
