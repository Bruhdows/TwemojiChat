import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile
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
    id("net.minecraftforge.renamer") version "1.1.0" apply false
    id("net.minecraftforge.gradle") version "7.0.25" apply false
}

fun prop(name: String): String = providers.gradleProperty(name).get()

version = prop("mod_version")
group = prop("mod_group_id")

val syncForgeJtracyToM2 =
    tasks.register<Sync>("syncForgeJtracyToM2") {
        // ForgeGradle's run tasks still resolve this Mojang native out of ~/.m2 on newer lines.
        val jtracyCache =
            gradle.gradleUserHomeDir.resolve(
                "caches/minecraftforge/forgegradle/mavenizer/caches/maven/mojang/com/mojang/jtracy"
            )
        from(jtracyCache)
        into(file("${System.getProperty("user.home")}/.m2/repository/com/mojang/jtracy"))
        includeEmptyDirs = false
        onlyIf { jtracyCache.exists() }
    }

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
        maven("https://maven.minecraftforge.net/")
        maven("https://libraries.minecraft.net/")
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

    pluginManager.withPlugin("net.minecraftforge.gradle") {
        tasks.withType<JavaCompile>().configureEach {
            dependsOn(rootProject.tasks.named("syncForgeJtracyToM2"))
        }

        tasks.matching {
                it.name == "runClient" ||
                    it.name == "runServer" ||
                    it.name == "runData" ||
                    it.name == "runClientData" ||
                    it.name == "runGameTestServer"
            }
            .configureEach {
                dependsOn(rootProject.tasks.named("syncForgeJtracyToM2"))
            }
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
