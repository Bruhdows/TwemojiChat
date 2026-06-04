import org.gradle.api.tasks.Exec
import org.gradle.plugins.ide.idea.model.IdeaModel
import twemojichat.buildlogic.SUPPORTED_VERSION_LINES

plugins {
    base
    idea
    id("com.diffplug.spotless") version "8.5.1"
    id("net.fabricmc.fabric-loom-remap") version "1.14.10" apply false
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply false
}

fun prop(name: String): String = providers.gradleProperty(name).get()

version = prop("mod_version")
group = prop("mod_group_id")

allprojects {
    apply(plugin = "idea")

    version = rootProject.version
    group = rootProject.group

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.parchmentmc.org")
    }

    extensions.configure<IdeaModel> {
        module {
            isDownloadSources = true
            isDownloadJavadoc = false
        }
    }
}

spotless {
    java {
        target("common/src/**/*.java", "fabric/src/**/*.java", "neoforge/src/**/*.java")
        targetExclude("**/build/**")
        googleJavaFormat()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.md", ".gitignore", ".github/**/*.yml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register<Exec>("syncTwemoji") {
    workingDir = project.projectDir
    commandLine("python3", "tools/sync_twemoji.py")
}

val versionedModules = SUPPORTED_VERSION_LINES.flatMap { line ->
    listOf(
        ":common:${line.projectName}",
        ":fabric:${line.projectName}",
        ":neoforge:${line.projectName}"
    )
}

val overlayModules = SUPPORTED_VERSION_LINES
    .filter { it.sourceSetDirectory != null }
    .flatMap { line ->
        listOf(
            ":common:${line.projectName}",
            ":fabric:${line.projectName}",
            ":neoforge:${line.projectName}"
        )
    }

tasks.register("compileMatrix") {
    dependsOn(versionedModules.map { "$it:compileJava" })
}

tasks.register("buildMatrix") {
    dependsOn(versionedModules.map { "$it:build" })
}

tasks.register("prepareIdeSources") {
    dependsOn(
        overlayModules.flatMap { module ->
            listOf("$module:prepareVersionedJava", "$module:prepareVersionedResources")
        }
    )
}

tasks.named("assemble") {
    dependsOn(versionedModules.map { "$it:assemble" })
}

tasks.named("build") {
    dependsOn(versionedModules.map { "$it:build" })
}
