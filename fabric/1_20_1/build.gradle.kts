import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir
val commonDir = project.rootProject.projectDir.resolve("common")

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from(commonDir.resolve("src/main/java"))
    from(commonDir.resolve("src/1201/java"))
    from(parentDir.resolve("src/main/java"))
    from(parentDir.resolve("src/1201/java"))
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from(commonDir.resolve("src/main/resources"))
    from(commonDir.resolve("src/generated/resources"))
    from(commonDir.resolve("src/1201/resources"))
    from(parentDir.resolve("src/main/resources"))
    from(parentDir.resolve("src/1201/resources"))
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

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
                "mod_id" to prop("mod_id"),
                "mod_version" to prop("mod_version"),
                "minecraft_version" to "1.20.1",
                "fabric_loader_version" to "0.16.14",
                "java_version" to "17"
            )
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(prepareVersionedJava)
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.named("processResources") {
    dependsOn(prepareVersionedResources)
}

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
}

extensions.configure<IdeaModel> {
    module {
        sourceDirs =
            sourceDirs + setOf(
                commonDir.resolve("src/main/java"),
                commonDir.resolve("src/1201/java"),
                parentDir.resolve("src/main/java"),
                parentDir.resolve("src/1201/java"),
            )
        generatedSourceDirs =
            generatedSourceDirs + setOf(layout.buildDirectory.dir("generated/versionedMain/java").get().asFile)
    }
}

tasks.matching { it.name == "ideaSyncTask" }.configureEach {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
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
