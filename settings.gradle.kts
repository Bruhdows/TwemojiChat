pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "TwemojiChat"

include(
    "common:1_20_1",
    "common:1_20_2",
    "common:1_20_3",
    "common:1_20_4",
    "common:1_20_5",
    "common:1_20_6",
    "common:1_21",
    "common:1_21_1",
    "common:1_21_2",
    "common:1_21_3",
    "common:1_21_4",
    "common:1_21_5",
    "common:1_21_6",
    "common:1_21_7",
    "common:1_21_8",
    "common:1_21_9",
    "common:1_21_10",
    "common:1_21_11",
    "common:26_1",
    "fabric:1_20_1",
    "fabric:1_20_2",
    "fabric:1_20_3",
    "fabric:1_20_4",
    "fabric:1_20_5",
    "fabric:1_20_6",
    "fabric:1_21",
    "fabric:1_21_1",
    "fabric:1_21_2",
    "fabric:1_21_3",
    "fabric:1_21_4",
    "fabric:1_21_5",
    "fabric:1_21_6",
    "fabric:1_21_7",
    "fabric:1_21_8",
    "fabric:1_21_9",
    "fabric:1_21_10",
    "fabric:1_21_11",
    "fabric:26_1",
    "forge:1_20_1",
    "forge:1_20_2",
    "forge:1_20_3",
    "forge:1_20_4",
    "neoforge:1_20_6",
    "neoforge:1_21",
    "neoforge:1_21_1",
    "neoforge:1_21_2",
    "neoforge:1_21_3",
    "neoforge:1_21_4",
    "neoforge:1_21_5",
    "neoforge:1_21_6",
    "neoforge:1_21_7",
    "neoforge:1_21_8",
    "neoforge:1_21_9",
    "neoforge:1_21_10",
    "neoforge:1_21_11",
    "neoforge:26_1"
)
