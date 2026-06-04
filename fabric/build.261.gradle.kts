import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

fun prop(name: String): String = rootProject.extra[name].toString()
val activeLine = prop("active_mc_line")

val commonProject = rootProject.childProjects.getValue("common")
val commonSourceSets = commonProject.extensions.getByType<SourceSetContainer>()
val commonPrepareVersionedJava = commonProject.tasks.named("prepareVersionedJava")
val commonPrepareVersionedResources = commonProject.tasks.named("prepareVersionedResources")

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from("src/main/java")
    from("src/$activeLine/java")
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from("src/main/resources")
    from("src/$activeLine/resources")
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("active_minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${prop("active_fabric_loader_version")}")
    implementation(fabricApi.module("fabric-message-api-v1", prop("active_fabric_api_version")))
    implementation(fabricApi.module("fabric-screen-api-v1", prop("active_fabric_api_version")))
    implementation(fabricApi.module("fabric-resource-loader-v0", prop("active_fabric_api_version")))

    implementation(commonSourceSets.named("main").get().output)
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run/fabric-${prop("active_mc_line")}")
        }
    }
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(prepareVersionedJava, commonPrepareVersionedJava, commonPrepareVersionedResources)
    source(commonSourceSets.named("main").get().allSource)
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.processResources {
    dependsOn(prepareVersionedResources, commonPrepareVersionedResources)
    from(commonSourceSets.named("main").get().resources)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to prop("mod_id"),
                "mod_version" to prop("mod_version"),
                "minecraft_version" to prop("active_minecraft_version"),
                "fabric_loader_version" to prop("active_fabric_loader_version"),
                "java_version" to prop("active_java_version")
            )
        )
    }
}

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources, commonPrepareVersionedJava, commonPrepareVersionedResources)
}

tasks.named("compileTestJava") {
    enabled = false
}

tasks.named("test") {
    enabled = false
}
