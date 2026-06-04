import org.gradle.api.tasks.testing.Test
import twemojichat.buildlogic.configureCommonModuleSources
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureStandardModuleTasks

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

configureJavaModule(17)
configureCommonModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.3")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configureStandardModuleTasks(17, useJUnit = true)
