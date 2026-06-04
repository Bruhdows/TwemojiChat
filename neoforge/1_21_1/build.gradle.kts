import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.slf4j.event.Level

plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir
val commonDir = project.rootProject.projectDir.resolve("common")

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

sourceSets.main {
    java.setSrcDirs(listOf(commonDir.resolve("src/main/java"), parentDir.resolve("src/main/java")))
    resources.setSrcDirs(listOf(commonDir.resolve("src/main/resources"), commonDir.resolve("src/generated/resources"), parentDir.resolve("src/main/resources")))
}

dependencies {}

extensions.configure<NeoForgeExtension> {
    setVersion("21.1.233")

    runs {
        create("client") {
            client()
            gameDirectory.set(project.layout.projectDirectory.dir("run/neoforge/1_21_1"))
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel.set(Level.DEBUG)
        }
    }

    mods {
        create(prop("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks.processResources {
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.21.1",
                "minecraft_version_range" to "[1.21.1,1.22)",
                "neo_version" to "21.1.233",
                "neo_version_range" to "[21.1,)",
                "loader_version_range" to "[4,)",
                "mod_id" to prop("mod_id"),
                "mod_name" to prop("mod_name"),
                "mod_license" to prop("mod_license"),
                "mod_version" to prop("mod_version"),
                "mod_authors" to prop("mod_authors"),
                "mod_description" to prop("mod_description")
            )
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

extensions.configure<IdeaModel> {
    module {
        sourceDirs =
            sourceDirs + setOf(commonDir.resolve("src/main/java"), parentDir.resolve("src/main/java"))
    }
}
