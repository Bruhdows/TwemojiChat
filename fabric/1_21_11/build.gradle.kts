import net.fabricmc.loom.task.RemapJarTask
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

configureJavaModule(21)
configureLoaderModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.141.4+1.21.11")
    modImplementation(fabricApi.module("fabric-message-api-v1", "0.141.4+1.21.11"))
    modImplementation(fabricApi.module("fabric-screen-api-v1", "0.141.4+1.21.11"))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.141.4+1.21.11"))
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 1.21.11 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/1_21_11")
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to modProp("mod_id"),
                "mod_version" to modProp("mod_version"),
                "minecraft_version" to "1.21.11",
                "fabric_loader_version" to "0.18.4",
                "java_version" to "21"
            )
        )
    }
}

configureStandardModuleTasks(21)

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
