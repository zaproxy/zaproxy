package org.zaproxy.zap

import java.nio.file.Files
import java.nio.file.Paths
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.apache.tools.ant.filters.ReplaceTokens
import org.apache.tools.ant.taskdefs.condition.Os
import org.cyclonedx.gradle.CycloneDxTask
import org.zaproxy.zap.tasks.internal.Utils
import org.zaproxy.zap.tasks.CreateDmg
import org.zaproxy.zap.tasks.DownloadMainAddOns
import org.zaproxy.zap.tasks.GradleBuildWithGitRepos
import org.zaproxy.zap.tasks.UpdateMainAddOns

plugins {
    de.undercouch.download
}

val dailyVersion = provider { "D-${extra["creationDate"]}" }

val distDir = file("src/main/dist/")
val bundledResourcesPath = "src/main/resources/org/zaproxy/zap/resources"

val cyclonedxRuntimeBom by tasks.registering(CycloneDxTask::class) {
    setIncludeConfigs(listOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME))
    setDestination(file("$buildDir/reports/bom-runtime"))
    setOutputFormat("json")
}

val jar by tasks.existing(Jar::class)
val jarWithBom by tasks.registering(Jar::class) {
    destinationDirectory.set(file("$buildDir/libs/withBom/"))

    from(jar.map { it.source }) {
        exclude("MANIFEST.MF")
    }
    from(cyclonedxRuntimeBom)
}

tasks.named<CycloneDxTask>("cyclonedxBom") {
    setDestination(file("$buildDir/reports/bom-all"))
}

val mainAddOnsFile = file("src/main/main-add-ons.yml")

val downloadMainAddOns by tasks.registering(DownloadMainAddOns::class) {
    group = "build"
    description = "Downloads the add-ons included in main (non-SNAPSHOT) releases."

    addOnsData.set(mainAddOnsFile)
    outputDir.set(file("$buildDir/mainAddOns"))
}

val updateMainAddOns by tasks.registering(UpdateMainAddOns::class) {
    group = "build"
    description = "Updates the main add-ons from a ZapVersions.xml file."

    addOnsData.set(mainAddOnsFile)
    addOnsDataUpdated.set(mainAddOnsFile)
}

val bundledAddOns: Any = provider {
    if (version.toString().endsWith("SNAPSHOT")) {
        file("src/main/dist/plugin")
    } else {
        downloadMainAddOns
    }
}

val distFiles by tasks.registering(Sync::class) {
    destinationDir = file("$buildDir/distFiles")
    from(jarWithBom)
    from(distDir) {
        filesMatching(listOf("zap.bat", "zap.sh")) {
            filter<ReplaceTokens>("tokens" to mapOf("zapJar" to jarWithBom.get().archiveFileName.get()))
        }
        exclude("README.weekly")
        exclude("plugin/*.zap")
    }
    from("src/main/resources/resource/zap.ico")
    from(configurations.named("runtimeClasspath")) {
        into("lib")
    }
    from("$bundledResourcesPath/xml") {
        into("xml")
    }
    from(bundledResourcesPath) {
        include("config.xml", "log4j.properties")
        into("xml")
    }
    from(bundledResourcesPath) {
        include("Messages.properties", "vulnerabilities.xml")
        into("lang")
    }
    from(bundledResourcesPath) {
        include("zapdb.script")
        into("db")
    }
    from(bundledResourcesPath) {
        include("ApacheLicense-2.0.txt")
        into("license")
    }
}

tasks.register<Zip>("distCrossplatform") {
    group = "Distribution"
    description = "Bundles the crossplatform distribution."

    archiveFileName.set("ZAP_${project.version}_Crossplatform.zip")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val topLevelDir = "ZAP_${project.version}"
    from(distFiles) {
        into(topLevelDir)
    }
    from(bundledAddOns) {
        into("$topLevelDir/plugin")
        exclude("Readme.txt")
    }
}

val copyCoreAddOns by tasks.registering {
    inputs.files(mainAddOnsFile)
    if (version.toString().endsWith("SNAPSHOT")) {
        inputs.files(bundledAddOns)
    } else {
        dependsOn(bundledAddOns)
    }

    val outputDir = file("$buildDir/coreAddOns")
    outputs.dir(outputDir)

    doLast {
        val coreAddOns = Utils.parseData(mainAddOnsFile.toPath()).addOns.filter { it -> it.isCore() }.map { it -> it.id }

        sync {
            from(bundledAddOns) {
                exclude { details: FileTreeElement ->
                         !details.path.endsWith(".zap") ||
                         details.file.name.split("-")[0] !in coreAddOns
                }
            }
            into(outputDir)
        }
    }
}

tasks.register<Zip>("distCore") {
    group = "Distribution"
    description = "Bundles the core distribution."

    archiveFileName.set("ZAP_${project.version}_Core.zip")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val topLevelDir = "ZAP_${project.version}"

    from(distFiles) {
        into(topLevelDir)
    }
    from(copyCoreAddOns) {
        into("$topLevelDir/plugin")
        exclude("Readme.txt")
    }
}

tasks.register<Tar>("distLinux") {
    group = "Distribution"
    description = "Bundles the Linux distribution."

    archiveFileName.set("ZAP_${project.version}_Linux.tar.gz")
    compression = Compression.GZIP
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val topLevelDir = "ZAP_${project.version}"
    from(distFiles) {
        into(topLevelDir)
    }
    from(bundledAddOns) {
        into("$topLevelDir/plugin")
        exclude(listOf("Readme.txt", "*macos*.zap", "*windows*.zap"))
    }
}

listOf(
    MacArch("", "", "", "x64", "c5a4003a579ea0633a4830bc5eae2f3aacf9990354bbac0393d1744e06907383"),
    MacArch("Arm64", "_aarch64", " (ARM64)", "aarch64", "733e7efdb2f0557859c36f3f40762782f4946c123b8a4bea851175db3b24b2f0")
).forEach { it ->

    val volumeName = "ZAP"
    val appName = "$volumeName.app"
    val macOsJreDir = file("$buildDir/macOsJre${it.suffix}")
    val macOsJreUnpackDir = File(macOsJreDir, "unpacked")
    val macOsJreVersion = "11.0.20.1+1"
    val macOsJreFile = File(macOsJreDir, "jdk$macOsJreVersion-jre.tar.gz")

    val downloadMacOsJre = tasks.register<Download>("downloadMacOsJre${it.suffix}") {
        src("https://api.adoptium.net/v3/binary/version/jdk-$macOsJreVersion/mac/${it.arch}/jre/hotspot/normal/eclipse?project=jdk")
        dest(macOsJreFile)
        connectTimeout(60_000)
        readTimeout(60_000)
        onlyIfModified(true)
        doFirst {
            require(Os.isFamily(Os.FAMILY_MAC)) {
                "To build the macOS distribution the OS must be macOS."
            }
        }
    }

    val verifyMacOsJre = tasks.register<Verify>("verifyMacOsJre${it.suffix}") {
        dependsOn(downloadMacOsJre)
        src(macOsJreFile)
        algorithm("SHA-256")
        checksum(it.checksum)
    }

    val unpackMacOSJre = tasks.register<Copy>("unpackMacOSJre${it.suffix}") {
        dependsOn(verifyMacOsJre)
        from(tarTree(macOsJreFile))
        into(macOsJreUnpackDir)
        doFirst {
            delete(macOsJreUnpackDir)
        }
        doLast {
            // Rename top level dir to start with "jre" to match the
            // expectations of zap.sh script.
            val dirName = macOsJreUnpackDir.listFiles()[0].name
            ant.withGroovyBuilder {
                "move"(mapOf("file" to "$macOsJreUnpackDir/$dirName", "tofile" to "$macOsJreUnpackDir/jre-$dirName"))
            }
        }
    }

    val macOsDistDataDir = file("$buildDir/macOsDistData${it.suffix}")
    val prepareDistMac = tasks.register<Copy>("prepareDistMac${it.suffix}") {
        destinationDir = macOsDistDataDir
        from(unpackMacOSJre) {
            into("$appName/Contents/PlugIns/")
        }
        from("src/main/macOS/") {
            filesMatching("**/Info.plist") {
                filter<ReplaceTokens>(
                    "tokens" to mapOf(
                        "JREDIR" to macOsJreUnpackDir.listFiles()[0].name,
                        "SHORT_VERSION_STRING" to "$version",
                        "VERSION_STRING" to "2",
                        "ZAPJAR" to jarWithBom.get().archiveFileName.get()
                    )
                )
            }
        }
        from("src/main/resources/resource/ZAP.icns") {
            into("$appName/Contents/Resources/")
        }
        val zapDir = "$appName/Contents/Java/"
        from(distFiles) {
            into(zapDir)
            exclude(listOf("zap.bat", "zap.ico"))
        }
        from(bundledAddOns) {
            into("$zapDir/plugin")
            exclude(listOf("Readme.txt", "*linux*.zap", "*windows*.zap"))
        }

        doFirst {
            delete(macOsDistDataDir)
        }
    }

    tasks.register<CreateDmg>("distMac${it.suffix}") {
        group = "Distribution"
        description = "Bundles the macOS${it.taskDesc} distribution."

        dependsOn(prepareDistMac)

        volname.set(volumeName)
        workingDir.set(macOsDistDataDir)
        dmg.set(file("$buildDir/distributions/${volumeName}_$version${it.fileNameSuffix}.dmg"))

        doFirst {
            val symlink = Paths.get("$macOsDistDataDir/Applications")
            if (Files.notExists(symlink)) {
                Files.createSymbolicLink(symlink, Paths.get("/Applications"))
            }
        }
    }
}

val jarDaily by tasks.registering(Jar::class) {
    archiveVersion.set(dailyVersion)

    from(jarWithBom.map { it.source }) {
        exclude("MANIFEST.MF")
    }
}

val distDaily by tasks.registering(Zip::class) {
    group = "Distribution"
    description = "Bundles the daily distribution."

    archiveFileName.set(dailyVersion.map { "ZAP_$it.zip" })
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val rootDir = "ZAP_${dailyVersion.get()}"
    val startScripts = listOf("zap.bat", "zap.sh")

    from(jarDaily) {
        into(rootDir)
    }
    from(distDir) {
        into(rootDir)
        include(startScripts)
        filesMatching(startScripts) {
            filter<ReplaceTokens>("tokens" to mapOf("zapJar" to jarDaily.get().archiveFileName.get()))
        }
    }
    from(File(distDir, "plugin")) {
        into("$rootDir/plugin")
        include("*.zap")
    }
    from(distDir) {
        into(rootDir)
        include("README.weekly")
        rename { "README" }
    }
    from(distFiles) {
        into(rootDir)
        exclude(jarWithBom.get().archiveFileName.get())
        exclude("README")
        exclude(startScripts)
    }
}

tasks.named("assemble") {
    dependsOn(distDaily)
}

val weeklyAddOnsDir = file("$buildDir/weeklyAddOns")
val buildWeeklyAddOns by tasks.registering(GradleBuildWithGitRepos::class) {
    group = "Distribution"
    description = "Builds the weekly add-ons from source for weekly distribution."

    repositoriesDirectory.set(temporaryDir)
    repositoriesDataFile.set(file("src/main/weekly-add-ons.json"))
    clean.set(true)
    quiet.set(System.getenv("ZAP_WEEKLY_QUIET") != "false")

    tasks {
        if (System.getenv("ZAP_WEEKLY_ADDONS_NO_TEST") != "true") {
            register("test")
        }
        register("copyZapAddOn") {
            args.set(listOf("--into=$weeklyAddOnsDir"))
        }
    }

    doFirst {
        delete(weeklyAddOnsDir)
        mkdir(weeklyAddOnsDir)
    }
}

val prepareDistWeekly by tasks.registering(Sync::class) {

    dependsOn(buildWeeklyAddOns)

    val startScripts = listOf("zap.bat", "zap.sh")

    from(jarDaily)
    from(distDir) {
        include(startScripts)
        filesMatching(startScripts) {
            filter<ReplaceTokens>("tokens" to mapOf("zapJar" to jarDaily.get().archiveFileName.get()))
        }
    }
    from(weeklyAddOnsDir) {
        into("plugin")
    }
    from(distDir) {
        include("README.weekly")
        rename { "README" }
    }
    from(distFiles) {
        exclude(jarWithBom.get().archiveFileName.get())
        exclude("README")
        exclude(startScripts)
    }
    into(file("$buildDir/distFilesWeekly"))
}

tasks.register<Zip>("distWeekly") {
    group = "Distribution"
    description = "Bundles the weekly distribution."

    archiveFileName.set(dailyVersion.map { "ZAP_WEEKLY_$it.zip" })
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    from(prepareDistWeekly) {
        into("ZAP_${dailyVersion.get()}")
    }
}

data class MacArch(val suffix: String, val fileNameSuffix: String, val taskDesc: String, val arch: String, val checksum: String)
