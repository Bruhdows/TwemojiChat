package twemojichat.buildlogic

data class VersionLine(
    val projectName: String,
    val sourceSetDirectory: String?,
    val loaders: Set<LoaderKind>,
    val minecraftVersion: String,
    val minecraftVersionRange: String,
    val javaVersion: Int,
    val fabricLoaderVersion: String,
    val fabricApiVersion: String,
    val fabricPluginId: String,
    val neoVersion: String,
    val neoVersionRange: String,
    val loaderVersionRange: String,
    val usesLegacyForge: Boolean
)

enum class LoaderKind {
    FABRIC,
    FORGE,
    NEOFORGE
}

val SUPPORTED_VERSION_LINES = listOf(
    VersionLine(
        projectName = "1_20_1",
        sourceSetDirectory = "1201",
        loaders = setOf(LoaderKind.FABRIC, LoaderKind.FORGE),
        minecraftVersion = "1.20.1",
        minecraftVersionRange = "[1.20.1,1.21)",
        javaVersion = 17,
        fabricLoaderVersion = "0.16.14",
        fabricApiVersion = "0.92.9+1.20.1",
        fabricPluginId = "net.fabricmc.fabric-loom-remap",
        neoVersion = "1.20.1-47.4.0",
        neoVersionRange = "[47.4,)",
        loaderVersionRange = "[47,)",
        usesLegacyForge = true
    ),
    VersionLine(
        projectName = "1_21_1",
        sourceSetDirectory = "1211",
        loaders = setOf(LoaderKind.FABRIC, LoaderKind.NEOFORGE),
        minecraftVersion = "1.21.1",
        minecraftVersionRange = "[1.21.1,1.22)",
        javaVersion = 21,
        fabricLoaderVersion = "0.16.14",
        fabricApiVersion = "0.116.12+1.21.1",
        fabricPluginId = "net.fabricmc.fabric-loom-remap",
        neoVersion = "21.1.233",
        neoVersionRange = "[21.1,)",
        loaderVersionRange = "[4,)",
        usesLegacyForge = false
    ),
    VersionLine(
        projectName = "1_21_11",
        sourceSetDirectory = "12111",
        loaders = setOf(LoaderKind.FABRIC, LoaderKind.NEOFORGE),
        minecraftVersion = "1.21.11",
        minecraftVersionRange = "[1.21.11,1.22)",
        javaVersion = 21,
        fabricLoaderVersion = "0.18.4",
        fabricApiVersion = "0.141.4+1.21.11",
        fabricPluginId = "net.fabricmc.fabric-loom-remap",
        neoVersion = "21.11.42",
        neoVersionRange = "[21.11,)",
        loaderVersionRange = "[4,)",
        usesLegacyForge = false
    ),
    VersionLine(
        projectName = "26_1",
        sourceSetDirectory = "261",
        loaders = setOf(LoaderKind.FABRIC, LoaderKind.NEOFORGE),
        minecraftVersion = "26.1.2",
        minecraftVersionRange = "[26.1,26.2)",
        javaVersion = 25,
        fabricLoaderVersion = "0.19.2",
        fabricApiVersion = "0.150.0+26.1.2",
        fabricPluginId = "net.fabricmc.fabric-loom",
        neoVersion = "26.1.2.72",
        neoVersionRange = "[26.1,)",
        loaderVersionRange = "[4,)",
        usesLegacyForge = false
    )
)

val SUPPORTED_VERSION_LINE_MAP = SUPPORTED_VERSION_LINES.associateBy(VersionLine::projectName)

fun versionLine(projectName: String): VersionLine = SUPPORTED_VERSION_LINE_MAP.getValue(projectName)
