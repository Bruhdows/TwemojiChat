plugins {
    id("fabric-loom") version "1.8-SNAPSHOT"
}

fun prop(name: String): String = rootProject.providers.gradleProperty(name).get()

dependencies {
    minecraft("com.mojang:minecraft:${prop("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${prop("fabric_loader_version")}")
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

tasks.named("jar") {
    enabled = false
}

tasks.named("remapJar") {
    enabled = false
}
