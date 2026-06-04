import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir
val commonDir = project.rootProject.projectDir.resolve("common")

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from(commonDir.resolve("src/main/java"))
    from(commonDir.resolve("src/261/java"))
    from(parentDir.resolve("src/main/java"))
    from(parentDir.resolve("src/261/java"))
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from(commonDir.resolve("src/main/resources"))
    from(commonDir.resolve("src/generated/resources"))
    from(commonDir.resolve("src/261/resources"))
    from(parentDir.resolve("src/main/resources"))
    from(parentDir.resolve("src/261/resources"))
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.19.2")
    implementation("net.fabricmc.fabric-api:fabric-api:0.150.0+26.1.2")
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric 26.1 Client"
            ideConfigGenerated(true)
            runDir("run/fabric/26_1")
        }
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "mod_id" to prop("mod_id"),
                "mod_version" to prop("mod_version"),
                "minecraft_version" to "26.1.2",
                "fabric_loader_version" to "0.19.2",
                "java_version" to "25"
            )
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(prepareVersionedJava)
    options.encoding = "UTF-8"
    options.release.set(25)
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

tasks.withType<RemapJarTask>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
