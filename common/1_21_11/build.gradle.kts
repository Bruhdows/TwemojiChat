import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.10"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()
val parentDir = project.parent!!.projectDir

extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

extensions.configure<BasePluginExtension> {
    archivesName.set(prop("mod_id"))
}

val prepareVersionedJava = tasks.register<Sync>("prepareVersionedJava") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/java"))
    from(parentDir.resolve("src/main/java"))
    from(parentDir.resolve("src/12111/java"))
}

val prepareVersionedResources = tasks.register<Sync>("prepareVersionedResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    into(layout.buildDirectory.dir("generated/versionedMain/resources"))
    from(parentDir.resolve("src/generated/resources"))
    from(parentDir.resolve("src/main/resources"))
    from(parentDir.resolve("src/12111/resources"))
}

sourceSets.main {
    java.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/java")))
    resources.setSrcDirs(listOf(layout.buildDirectory.dir("generated/versionedMain/resources")))
}

sourceSets.test {
    java.setSrcDirs(listOf(parentDir.resolve("src/test/java")))
    resources.setSrcDirs(listOf(parentDir.resolve("src/test/resources")))
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(prepareVersionedJava)
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.named("processResources") {
    dependsOn(prepareVersionedResources)
}

tasks.named("sourcesJar") {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
}

extensions.configure<IdeaModel> {
    module {
        sourceDirs = sourceDirs + setOf(parentDir.resolve("src/main/java"), parentDir.resolve("src/12111/java"))
        generatedSourceDirs = generatedSourceDirs + setOf(layout.buildDirectory.dir("generated/versionedMain/java").get().asFile)
    }
}

tasks.matching { it.name == "ideaSyncTask" }.configureEach {
    dependsOn(prepareVersionedJava, prepareVersionedResources)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

tasks.withType<Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}
