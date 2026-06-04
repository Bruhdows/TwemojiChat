plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.modrinth.minotaur:Minotaur:2.+")
}

kotlin {
    jvmToolchain(21)
}
