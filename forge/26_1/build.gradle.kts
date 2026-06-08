import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import twemojichat.buildlogic.configureJavaModule
import twemojichat.buildlogic.configureLoaderModuleSources
import twemojichat.buildlogic.configureModrinthPublishing
import twemojichat.buildlogic.configureStandardModuleTasks
import twemojichat.buildlogic.modProp
import twemojichat.buildlogic.versionLineForProject

plugins {
    id("java")
    id("net.minecraftforge.gradle") version "7.0.25"
}

val vl = versionLineForProject()
val runDirName = project.name

configureJavaModule(vl.javaVersion)
configureLoaderModuleSources(includeLoaderMainSources = false, loaderSourceDirOverride = "261")

val mainSourceSet = extensions.getByType(SourceSetContainer::class.java)["main"]

minecraft {
    if (vl.minecraftVersion != "26.1.2") {
        mappings("official", vl.minecraftVersion)
    }

    runs {
        configureEach {
            workingDir.set(layout.projectDirectory.dir("run/forge/$runDirName"))
            systemProperty("eventbus.api.strictRuntimeChecks", "true")
            systemProperty("forge.enabledGameTestNamespaces", modProp("mod_id"))
        }

        create("client") {
            mods {
                register(modProp("mod_id")) {
                    source(mainSourceSet)
                }
            }
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
}

dependencies {
    add("implementation", minecraft.dependency("net.minecraftforge:forge:${requireNotNull(vl.forgeVersion)}"))
}

tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to vl.minecraftVersion,
                "minecraft_version_range" to vl.minecraftVersionRange,
                "neo_version" to requireNotNull(vl.forgeVersion),
                "neo_version_range" to requireNotNull(vl.forgeVersionRange),
                "loader_version_range" to requireNotNull(vl.forgeLoaderVersionRange),
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

configureStandardModuleTasks(vl.javaVersion)

configureModrinthPublishing()
