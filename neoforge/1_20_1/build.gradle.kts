import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.slf4j.event.Level

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.141"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir
val commonDir = project.rootProject.projectDir.resolve("common")

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from(commonDir.resolve("src/main/java"))
    from(commonDir.resolve("src/1201/java"))
    from(parentDir.resolve("src/main/java"))
    from(parentDir.resolve("src/1201/java"))
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from(commonDir.resolve("src/main/resources"))
    from(commonDir.resolve("src/generated/resources"))
    from(commonDir.resolve("src/1201/resources"))
    from(parentDir.resolve("src/main/resources"))
    from(parentDir.resolve("src/1201/resources"))
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
    resources.exclude("META-INF/neoforge.mods.toml")
}

dependencies {}

extensions.configure<LegacyForgeExtension> {
    setVersion("1.20.1-47.4.0")

    runs {
        create("client") {
            client()
            gameDirectory.set(project.layout.projectDirectory.dir("run/neoforge/1_20_1"))
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
    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to "1.20.1",
                "minecraft_version_range" to "[1.20.1,1.21)",
                "neo_version" to "1.20.1-47.4.0",
                "neo_version_range" to "[47.4,)",
                "loader_version_range" to "[47,)",
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
    dependsOn(prepareVersionedJava)
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.named("processResources") {
    dependsOn(prepareVersionedResources)
}

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
