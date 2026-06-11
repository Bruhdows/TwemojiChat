import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureModrinthPublishing
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("java")
    id("net.minecraftforge.gradle") version "7.0.25"
    id("net.minecraftforge.renamer")
}

configureJavaModule(17)
configureLoaderModuleSources(includeLoaderMainSources = false)

val mainSourceSet = extensions.getByType(SourceSetContainer::class.java)["main"]

minecraft {
    mappings("official", "1.20.3")

    runs {
        register("client") {
            workingDir.set(layout.projectDirectory.dir("run/forge/1_20_3"))
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            mods {
                register(modProp("mod_id")) {
                    source(mainSourceSet)
                }
            }
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    maven("https://libraries.minecraft.net/")
    maven(uri("${gradle.gradleUserHomeDir}/caches/minecraftforge/forgegradle/mavenizer/caches/maven/mojang"))
}

dependencies {
    add("implementation", minecraft.dependency("net.minecraftforge:forge:1.20.3-49.0.2"))
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.20.3",
                "minecraft_version_range" to "[1.20.3,1.21)",
                "neo_version" to "1.20.3-49.0.2",
                "neo_version_range" to "[49.0,)",
                "loader_version_range" to "[49,)",
                "mod_id" to modProp("mod_id"),
                "mod_name" to modProp("mod_name"),
                "mod_license" to modProp("mod_license"),
                "mod_version" to modProp("mod_version"),
                "mod_authors" to modProp("mod_authors"),
                "mod_description" to modProp("mod_description")
            )
        )
    }
}

configureStandardModuleTasks(17)

configureModrinthPublishing()
