import net.fabricmc.loom.task.RemapJarTask
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

configureJavaModule(17)
configureLoaderModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.92.9+1.20.1")
    modImplementation(fabricApi.module("fabric-message-api-v1", "0.92.9+1.20.1"))
    modImplementation(fabricApi.module("fabric-screen-api-v1", "0.92.9+1.20.1"))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.92.9+1.20.1"))
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 1.20.1 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/1_20_1")
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to modProp("mod_id"),
                "mod_version" to modProp("mod_version"),
                "minecraft_version" to "1.20.1",
                "fabric_loader_version" to "0.16.14",
                "java_version" to "17"
            )
        )
    }
}

configureStandardModuleTasks(17)

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
