import net.fabricmc.loom.task.RemapJarTask
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureModrinthPublishing
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp
import twemojichat.buildlogic.versionLineForProject

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

val vl = versionLineForProject()

configureJavaModule(vl.javaVersion)
configureLoaderModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:${vl.minecraftVersion}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${vl.fabricLoaderVersion}")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${vl.fabricApiVersion}")
    modImplementation(fabricApi.module("fabric-message-api-v1", vl.fabricApiVersion))
    modImplementation(fabricApi.module("fabric-screen-api-v1", vl.fabricApiVersion))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", vl.fabricApiVersion))
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 1.20.3 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/1_20_3")
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to modProp("mod_id"),
                "mod_version" to modProp("mod_version"),
                "minecraft_version" to vl.minecraftVersion,
                "fabric_loader_version" to vl.fabricLoaderVersion,
                "java_version" to "${vl.javaVersion}"
            )
        )
    }
}

configureStandardModuleTasks(vl.javaVersion)

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

configureModrinthPublishing()
