import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
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
    java.setSrcDirs(
        listOf(
            commonDir.resolve("src/main/java"),
            commonDir.resolve("src/12111/java"),
            parentDir.resolve("src/main/java"),
            parentDir.resolve("src/12111/java"),
        )
    )
    resources.setSrcDirs(
        listOf(
            commonDir.resolve("src/12111/resources"),
            parentDir.resolve("src/12111/resources"),
            commonDir.resolve("src/generated/resources"),
            commonDir.resolve("src/main/resources"),
            parentDir.resolve("src/main/resources"),
        )
    )
}

dependencies {}

extensions.configure<NeoForgeExtension> {
    setVersion("21.11.42")

    runs {
        create("client") {
            client()
            gameDirectory.set(project.layout.projectDirectory.dir("run/neoforge/1_21_11"))
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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.21.11",
                "minecraft_version_range" to "[1.21.11,1.22)",
                "neo_version" to "21.11.42",
                "neo_version_range" to "[21.11,)",
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
