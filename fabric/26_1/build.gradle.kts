import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureModrinthPublishing
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp
import twemojichat.buildlogic.versionLineForProject

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

val vl = versionLineForProject()

configureJavaModule(vl.javaVersion)
configureLoaderModuleSources()

dependencies {
    minecraft("com.mojang:minecraft:${vl.minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${vl.fabricLoaderVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${vl.fabricApiVersion}")
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
                "minecraft_version" to vl.minecraftVersion,
                "fabric_loader_version" to vl.fabricLoaderVersion,
                "java_version" to "${vl.javaVersion}"
            )
        )
    }
}

configureStandardModuleTasks(vl.javaVersion)

configureModrinthPublishing()
