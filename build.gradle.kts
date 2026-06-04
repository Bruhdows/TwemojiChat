import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Exec
import org.gradle.plugins.ide.idea.model.IdeaModel
import twemojichat.buildlogic.LoaderKind
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

    tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

spotless {
    java {
        target("common/src/**/*.java", "fabric/src/**/*.java", "forge/src/**/*.java", "neoforge/src/**/*.java")
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
    buildList {
        add(":common:${line.projectName}")
        if (LoaderKind.FABRIC in line.loaders) {
            add(":fabric:${line.projectName}")
        }
        if (LoaderKind.FORGE in line.loaders) {
            add(":forge:${line.projectName}")
        }
        if (LoaderKind.NEOFORGE in line.loaders) {
            add(":neoforge:${line.projectName}")
        }
    }
}

tasks.register("compileMatrix") {
    dependsOn(versionedModules.map { "$it:compileJava" })
}

tasks.register("buildMatrix") {
    dependsOn(versionedModules.map { "$it:build" })
}

tasks.named("assemble") {
    dependsOn(versionedModules.map { "$it:assemble" })
}

tasks.named("build") {
    dependsOn(versionedModules.map { "$it:build" })
}

tasks.register("modrinth") {
    val loaderModules = SUPPORTED_VERSION_LINES.flatMap { line ->
        buildList {
            if (LoaderKind.FABRIC in line.loaders) add(":fabric:${line.projectName}")
            if (LoaderKind.FORGE in line.loaders) add(":forge:${line.projectName}")
            if (LoaderKind.NEOFORGE in line.loaders) add(":neoforge:${line.projectName}")
        }
    }
    dependsOn(loaderModules.map { "$it:modrinth" })
}
