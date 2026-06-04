import org.gradle.api.tasks.testing.Test
import twemojichat.buildlogic.configureCommonModuleSources
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureStandardModuleTasks

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

configureJavaModule(25)
configureCommonModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.19.2")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configureStandardModuleTasks(25, useJUnit = true)
