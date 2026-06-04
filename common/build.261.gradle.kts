import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
}

fun prop(name: String): String = rootProject.extra[name].toString()
val activeLine = prop("active_mc_line")

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from("src/main/java")
    from("src/$activeLine/java")
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from("src/generated/resources")
    from("src/main/resources")
    from("src/$activeLine/resources")
}

dependencies {
    minecraft("com.mojang:minecraft:${prop("active_minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${prop("active_fabric_loader_version")}")
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(prepareVersionedJava)
}

tasks.named("processResources") {
    dependsOn(prepareVersionedResources)
}

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
}

tasks.named("jar") {
    enabled = false
}
