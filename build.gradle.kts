import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
    idea
}

fun prop(name: String): String = providers.gradleProperty(name).get()

version = prop("mod_version")
group = prop("mod_group_id")

allprojects {
    version = rootProject.version
    group = rootProject.group

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.parchmentmc.org")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(prop("java_version").toInt()))
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(prop("java_version").toInt())
    }

    tasks.withType<org.gradle.api.publish.tasks.GenerateModuleMetadata>().configureEach {
        enabled = false
    }

    extensions.configure<org.gradle.api.plugins.BasePluginExtension> {
        archivesName.set(prop("mod_id"))
    }
}

tasks.register<Exec>("syncTwemoji") {
    workingDir = project.projectDir
    commandLine("python3", "tools/sync_twemoji.py")
}

tasks.named("assemble") {
    dependsOn(":fabric:assemble", ":neoforge:assemble")
}

tasks.named("build") {
    dependsOn(":fabric:build", ":neoforge:build")
}
