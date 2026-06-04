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

include(
    "common",
    "common:1_20_1",
    "common:1_21_1",
    "common:1_21_11",
    "common:26_1",
    "fabric",
    "fabric:1_20_1",
    "fabric:1_21_1",
    "fabric:1_21_11",
    "fabric:26_1",
    "neoforge",
    "neoforge:1_20_1",
    "neoforge:1_21_1",
    "neoforge:1_21_11",
    "neoforge:26_1"
)
