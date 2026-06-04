pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "TwemojiChat"

include("common", "fabric", "neoforge")

val activeLineKey = providers.gradleProperty("mc_line").orElse("1211").get()

if (activeLineKey == "1201") {
    project(":neoforge").buildFileName = "build.legacy.gradle.kts"
}

if (activeLineKey == "261") {
    project(":common").buildFileName = "build.261.gradle.kts"
    project(":fabric").buildFileName = "build.261.gradle.kts"
}
