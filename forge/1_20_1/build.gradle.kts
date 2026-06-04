import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.slf4j.event.Level
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.141"
}

configureJavaModule(17)
configureLoaderModuleSources(includeLoaderMainSources = false)

dependencies {}

extensions.configure<LegacyForgeExtension> {
    setVersion("1.20.1-47.4.0")

    runs {
        create("client") {
            client()
            gameDirectory.set(project.layout.projectDirectory.dir("run/forge/1_20_1"))
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
    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.20.1",
                "minecraft_version_range" to "[1.20.1,1.21)",
                "neo_version" to "1.20.1-47.4.0",
                "neo_version_range" to "[47.4,)",
                "loader_version_range" to "[47,)",
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
