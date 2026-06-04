import org.gradle.api.tasks.SourceSetContainer
import org.slf4j.event.Level

plugins {
    id("net.neoforged.moddev") version "2.0.141"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()

val commonProject = rootProject.childProjects.getValue("common")
val commonSourceSets = commonProject.extensions.getByType<SourceSetContainer>()

neoForge {
    version = prop("neo_version")

    runs {
        create("client") {
            client()
            gameDirectory = project.file("../run/neoforge")
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG
        }
    }

    mods {
        create(prop("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    source(commonSourceSets.named("main").get().allSource)
}

tasks.processResources {
    from(commonSourceSets.named("main").get().resources)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to prop("minecraft_version"),
                "minecraft_version_range" to prop("minecraft_version_range"),
                "neo_version" to prop("neo_version"),
                "neo_version_range" to prop("neo_version_range"),
                "loader_version_range" to prop("loader_version_range"),
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

tasks.named("compileTestJava") {
    enabled = false
}

tasks.named("test") {
    enabled = false
}
