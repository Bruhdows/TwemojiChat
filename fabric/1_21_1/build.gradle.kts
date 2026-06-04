import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir
val commonDir = project.rootProject.projectDir.resolve("common")

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

sourceSets.main {
    java.setSrcDirs(
        listOf(
            commonDir.resolve("src/main/java"),
            commonDir.resolve("src/1211/java"),
            parentDir.resolve("src/main/java"),
            parentDir.resolve("src/1211/java"),
        )
    )
    resources.setSrcDirs(
        listOf(
            commonDir.resolve("src/1211/resources"),
            commonDir.resolve("src/generated/resources"),
            commonDir.resolve("src/main/resources"),
            parentDir.resolve("src/main/resources"),
        )
    )
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.12+1.21.1")
    modImplementation(fabricApi.module("fabric-message-api-v1", "0.116.12+1.21.1"))
    modImplementation(fabricApi.module("fabric-screen-api-v1", "0.116.12+1.21.1"))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.116.12+1.21.1"))
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 1.21.1 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/1_21_1")
        }
    }
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to prop("mod_id"),
                "mod_version" to prop("mod_version"),
                "minecraft_version" to "1.21.1",
                "fabric_loader_version" to "0.16.14",
                "java_version" to "21"
            )
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
