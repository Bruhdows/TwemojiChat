import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.slf4j.event.Level
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureModrinthPublishing
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

configureJavaModule(21)
configureLoaderModuleSources(loaderSourceDirOverride = "1214")

dependencies {}

extensions.configure<NeoForgeExtension> {
    setVersion("21.5.97")

    runs {
        create("client") {
            client()
            gameDirectory.set(project.layout.projectDirectory.dir("run/neoforge/1_21_5"))
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel.set(Level.DEBUG)
        }
    }

    mods {
        create(modProp("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks.processResources {
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.21.5",
                "minecraft_version_range" to "[1.21.5,1.22)",
                "neo_version" to "21.5.97",
                "neo_version_range" to "[21.5,)",
                "loader_version_range" to "[4,)",
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

configureStandardModuleTasks(21)

configureModrinthPublishing()
