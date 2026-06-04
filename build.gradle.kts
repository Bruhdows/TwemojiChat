import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
    idea
    id("net.fabricmc.fabric-loom-remap") version "1.14.10" apply false
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply false
}

fun prop(name: String): String = providers.gradleProperty(name).get()
fun setActive(name: String, value: Any) {
    extra[name] = value
}

data class VersionLine(
    val minecraftVersion: String,
    val minecraftVersionRange: String,
    val javaVersion: Int,
    val fabricLoaderVersion: String,
    val fabricApiVersion: String,
    val fabricLoomPluginId: String,
    val fabricUsesRemap: Boolean,
    val neoVersion: String,
    val neoVersionRange: String,
    val loaderVersionRange: String
)

val versionLines = mapOf(
    "1201" to VersionLine(
        minecraftVersion = "1.20.1",
        minecraftVersionRange = "[1.20.1,1.21)",
        javaVersion = 17,
        fabricLoaderVersion = "0.16.14",
        fabricApiVersion = "0.92.9+1.20.1",
        fabricLoomPluginId = "net.fabricmc.fabric-loom-remap",
        fabricUsesRemap = true,
        neoVersion = "1.20.1-47.1.106",
        neoVersionRange = "[47.1,)",
        loaderVersionRange = "[47,)"
    ),
    "1211" to VersionLine(
        minecraftVersion = "1.21.1",
        minecraftVersionRange = "[1.21.1,1.22)",
        javaVersion = 21,
        fabricLoaderVersion = "0.16.14",
        fabricApiVersion = "0.116.12+1.21.1",
        fabricLoomPluginId = "net.fabricmc.fabric-loom-remap",
        fabricUsesRemap = true,
        neoVersion = "21.1.233",
        neoVersionRange = "[21.1,)",
        loaderVersionRange = "[4,)"
    ),
    "12111" to VersionLine(
        minecraftVersion = "1.21.11",
        minecraftVersionRange = "[1.21.11,1.22)",
        javaVersion = 21,
        fabricLoaderVersion = "0.18.4",
        fabricApiVersion = "0.141.4+1.21.11",
        fabricLoomPluginId = "net.fabricmc.fabric-loom-remap",
        fabricUsesRemap = true,
        neoVersion = "21.11.42",
        neoVersionRange = "[21.11,)",
        loaderVersionRange = "[4,)"
    ),
    "261" to VersionLine(
        minecraftVersion = "26.1.2",
        minecraftVersionRange = "[26.1,26.2)",
        javaVersion = 25,
        fabricLoaderVersion = "0.19.2",
        fabricApiVersion = "0.150.0+26.1.2",
        fabricLoomPluginId = "net.fabricmc.fabric-loom",
        fabricUsesRemap = false,
        neoVersion = "26.1.2.72",
        neoVersionRange = "[26.1,)",
        loaderVersionRange = "[4,)"
    )
)

val activeLineKey = providers.gradleProperty("mc_line").orElse("1211").get()
val activeLine = versionLines[activeLineKey]
    ?: error("Unsupported mc_line '$activeLineKey'. Expected one of ${versionLines.keys}.")

setActive("active_mc_line", activeLineKey)
setActive("active_minecraft_version", activeLine.minecraftVersion)
setActive("active_minecraft_version_range", activeLine.minecraftVersionRange)
setActive("active_java_version", activeLine.javaVersion.toString())
setActive("active_fabric_loader_version", activeLine.fabricLoaderVersion)
setActive("active_fabric_api_version", activeLine.fabricApiVersion)
setActive("active_fabric_loom_plugin_id", activeLine.fabricLoomPluginId)
setActive("active_fabric_uses_remap", activeLine.fabricUsesRemap.toString())
setActive("active_neo_version", activeLine.neoVersion)
setActive("active_neo_version_range", activeLine.neoVersionRange)
setActive("active_loader_version_range", activeLine.loaderVersionRange)

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
        toolchain.languageVersion.set(JavaLanguageVersion.of(rootProject.extra["active_java_version"].toString().toInt()))
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(rootProject.extra["active_java_version"].toString().toInt())
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
