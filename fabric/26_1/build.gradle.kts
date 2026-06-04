import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

configureJavaModule(25)
configureLoaderModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.19.2")
    implementation("net.fabricmc.fabric-api:fabric-api:0.150.0+26.1.2")
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 26.1 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/26_1")
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to modProp("mod_id"),
                "mod_version" to modProp("mod_version"),
                "minecraft_version" to "26.1.2",
                "fabric_loader_version" to "0.19.2",
                "java_version" to "25"
            )
        )
    }
}

configureStandardModuleTasks(25)
