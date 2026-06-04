import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile
import org.slf4j.event.Level

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.141"
}

fun prop(name: String): String = rootProject.extra[name].toString()
val activeLine = prop("active_mc_line")

val commonProject = rootProject.childProjects.getValue("common")
val commonSourceSets = commonProject.extensions.getByType<SourceSetContainer>()
val commonPrepareVersionedJava = commonProject.tasks.named("prepareVersionedJava")
val commonPrepareVersionedResources = commonProject.tasks.named("prepareVersionedResources")

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from("src/main/java")
    from("src/$activeLine/java")
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from("src/main/resources")
    from("src/$activeLine/resources")
}

dependencies {
    implementation(commonSourceSets.named("main").get().output)
}

legacyForge {
    enable {
        neoForgeVersion = prop("active_neo_version")
    }

    runs {
        create("client") {
            client()
            gameDirectory = rootProject.layout.projectDirectory.dir("run/neoforge-${prop("active_mc_line")}").asFile
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

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(prepareVersionedJava, commonPrepareVersionedJava, commonPrepareVersionedResources)
    source(commonSourceSets.named("main").get().allSource)
}

tasks.processResources {
    dependsOn(prepareVersionedResources, commonPrepareVersionedResources)
    from(commonSourceSets.named("main").get().resources)
    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "minecraft_version" to prop("active_minecraft_version"),
                "minecraft_version_range" to prop("active_minecraft_version_range"),
                "neo_version" to prop("active_neo_version"),
                "neo_version_range" to prop("active_neo_version_range"),
                "loader_version_range" to prop("active_loader_version_range"),
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

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources, commonPrepareVersionedJava, commonPrepareVersionedResources)
}

tasks.named("compileTestJava") {
    enabled = false
}

tasks.named("test") {
    enabled = false
}
