import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("fabric-loom") version "1.8-SNAPSHOT"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()

val commonProject = rootProject.childProjects.getValue("common")
val commonSourceSets = commonProject.extensions.getByType<SourceSetContainer>()

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("fabric_api_version")}")

    implementation(commonSourceSets.named("main").get().output)
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run/fabric")
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    source(commonSourceSets.named("main").get().allSource)
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.processResources {
    from(commonSourceSets.named("main").get().resources)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to prop("mod_id"),
                "mod_version" to prop("mod_version"),
                "minecraft_version" to prop("minecraft_version"),
                "fabric_loader_version" to prop("fabric_loader_version")
            )
        )
    }
}

tasks.named("compileTestJava") {
    enabled = false
}

tasks.named("test") {
    enabled = false
}
