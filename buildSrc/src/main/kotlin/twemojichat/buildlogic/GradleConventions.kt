package twemojichat.buildlogic

import com.modrinth.minotaur.ModrinthExtension
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.modProp(name: String): String = rootProject.providers.gradleProperty(name).get()

fun Project.versionLineForProject(): VersionLine = versionLine(project.name)

fun Project.sharedCommonDir() = rootProject.projectDir.resolve("common")

fun Project.moduleSourceDir() = versionLineForProject().sourceSetDirectory

fun Project.configureJavaModule(javaVersion: Int) {
    extensions.configure(JavaPluginExtension::class.java) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        if (project.parent?.name != "common") {
            withSourcesJar()
        }
    }

    extensions.configure(BasePluginExtension::class.java) {
        val loader = project.parent?.name
        archivesName.set(
            if (loader != null) {
                val mcVersion = versionLineForProject().minecraftVersion
                "${modProp("mod_id")}-$loader-$mcVersion"
            } else {
                modProp("mod_id")
            }
        )
    }
}

fun Project.configureMainSources(javaDirs: List<java.io.File>, resourceDirs: List<java.io.File>) {
    extensions.getByType(SourceSetContainer::class.java).named("main").configure {
        java.setSrcDirs(javaDirs)
        resources.setSrcDirs(resourceDirs)
    }
}

fun Project.configureTestSources(javaDirs: List<java.io.File>, resourceDirs: List<java.io.File>) {
    extensions.getByType(SourceSetContainer::class.java).named("test").configure {
        java.setSrcDirs(javaDirs)
        resources.setSrcDirs(resourceDirs)
    }
}

fun Project.configureCommonModuleSources() {
    val parentDir = project.parent!!.projectDir
    val sourceDir = moduleSourceDir()
    val javaDirs = buildList {
        add(parentDir.resolve("src/main/java"))
        if (sourceDir != null) {
            add(parentDir.resolve("src/$sourceDir/java"))
        }
    }
    val resourceDirs = buildList {
        if (sourceDir != null) {
            add(parentDir.resolve("src/$sourceDir/resources"))
        }
        add(parentDir.resolve("src/generated/resources"))
        add(parentDir.resolve("src/main/resources"))
    }
    configureMainSources(javaDirs, resourceDirs)
    configureTestSources(
        listOf(parentDir.resolve("src/test/java")),
        listOf(parentDir.resolve("src/test/resources"))
    )
}

fun Project.configureLoaderModuleSources(
    includeLoaderMainSources: Boolean = true,
    loaderSourceDirOverride: String? = null
) {
    val commonDir = sharedCommonDir()
    val parentDir = project.parent!!.projectDir
    val commonSourceDir = moduleSourceDir()
    val loaderSourceDir = loaderSourceDirOverride ?: commonSourceDir

    val javaDirs = buildList {
        add(commonDir.resolve("src/main/java"))
        if (commonSourceDir != null) {
            add(commonDir.resolve("src/$commonSourceDir/java"))
        }
        if (includeLoaderMainSources) {
            add(parentDir.resolve("src/main/java"))
        }
        if (loaderSourceDir != null) {
            add(parentDir.resolve("src/$loaderSourceDir/java"))
        }
    }

    val resourceDirs = buildList {
        if (commonSourceDir != null) {
            add(commonDir.resolve("src/$commonSourceDir/resources"))
        }
        if (loaderSourceDir != null) {
            add(parentDir.resolve("src/$loaderSourceDir/resources"))
        }
        add(commonDir.resolve("src/generated/resources"))
        add(commonDir.resolve("src/main/resources"))
        if (includeLoaderMainSources) {
            add(parentDir.resolve("src/main/resources"))
        }
    }

    configureMainSources(javaDirs, resourceDirs)
}

fun Project.configureStandardModuleTasks(javaVersion: Int, useJUnit: Boolean = false) {
    tasks.named("processResources", ProcessResources::class.java).configure {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }

    if (useJUnit) {
        tasks.withType(Test::class.java).configureEach {
            useJUnitPlatform()
        }
    }

    tasks.withType(GenerateModuleMetadata::class.java).configureEach {
        enabled = false
    }

    tasks.withType(Jar::class.java).configureEach {
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
    }
}

fun Project.configureModrinthPublishing() {
    project.plugins.apply("com.modrinth.minotaur")

    val vl = versionLineForProject()
    val loaderName = project.parent?.name ?: return

    extensions.configure(ModrinthExtension::class.java) {
        projectId.set(modProp("modrinth_id"))
        versionNumber.set(modProp("mod_version"))
        versionType.set("release")
        gameVersions.set(listOf(vl.minecraftVersion))
        loaders.set(listOf(loaderName))

        when (loaderName) {
            "fabric" -> {
                if (tasks.names.contains("remapJar")) {
                    uploadFile.set(tasks.named("remapJar"))
                } else {
                    uploadFile.set(tasks.named("jar"))
                }
            }
            "forge", "neoforge" -> {
                uploadFile.set(tasks.named("jar"))
            }
        }
    }
}
