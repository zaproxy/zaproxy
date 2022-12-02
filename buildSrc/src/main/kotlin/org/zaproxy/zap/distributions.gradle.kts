package org.zaproxy.zap

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.apache.tools.ant.filters.ReplaceTokens
import org.apache.tools.ant.taskdefs.condition.Os
import org.zaproxy.zap.tasks.DownloadMainAddOns
import org.zaproxy.zap.tasks.GradleBuildWithGitRepos
import org.zaproxy.zap.tasks.UpdateMainAddOns

plugins {
    de.undercouch.download
    nebula.ospackage
}

val dailyVersion = provider { "D-${extra["creationDate"]}" }

val distDir = file("src/main/dist/")
val bundledResourcesPath = "src/main/resources/org/zaproxy/zap/resources"

val jar by tasks.existing(Jar::class)


val downloadMainAddOns by tasks.registering(DownloadMainAddOns::class) {
    group = "build"
    description = "Downloads the add-ons included in main (non-SNAPSHOT) releases."

    addOnsData.set(file("src/main/main-add-ons.yml"))
    outputDir.set(file("$buildDir/mainAddOns"))
}

val updateMainAddOns by tasks.registering(UpdateMainAddOns::class) {
    group = "build"
    description = "Updates the main add-ons from a ZapVersions.xml file."

    addOnsData.set(file("src/main/main-add-ons.yml"))
    addOnsDataUpdated.set(file("src/main/main-add-ons.yml"))
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
    from(jar)
    from(distDir) {
        filesMatching(listOf("zap.bat", "zap.sh")) {
            filter<ReplaceTokens>("tokens" to mapOf("zapJar" to jar.get().archiveFileName.get()))
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

apply(from = "gradle/debian-package.gradle")

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

tasks.register<Zip>("distCore") {
    group = "Distribution"
    description = "Bundles the core distribution."

    archiveFileName.set("ZAP_${project.version}_Core.zip")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    val liteAddOns = listOf(
            "ascanrules",
            "bruteforce",
            "callhome",
            "commonlib",
            "coreLang",
            "diff",
            "gettingStarted",
            "help",
            "invoke",
            "network",
            "onlineMenu",
            "plugnhack",
            "pscanrules",
            "quickstart",
            "reports",
            "reveal",
            "tips")
    val topLevelDir = "ZAP_${project.version}"

    from(distFiles) {
        into(topLevelDir)
    }
    from(bundledAddOns) {
        into("$topLevelDir/plugin")
        exclude("Readme.txt")
        exclude { details: FileTreeElement ->
                details.path.endsWith(".zap") &&
                details.file.name.split("-")[0] !in liteAddOns
        }
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
    MacArch("", "", "", "x64", "66508958b5da6e36021d960b65490ab70cc7d44851c98301a3d6d3552f305674"),
    MacArch("Arm64", "_aarch64", " (ARM64)", "aarch64", "2a5cbc2888f8e382c7d0a5bf2c6d2c3bad120ceff51b00d3a62805dae4473c02")
).forEach { it ->

    val macOsJreDir = file("$buildDir/macOsJre${it.suffix}")
    val macOsJreUnpackDir = File(macOsJreDir, "unpacked")
    val macOsJreVersion = "11.0.17+8"
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
            into("OWASP ZAP.app/Contents/PlugIns/")
        }
        from("src/main/macOS/") {
            filesMatching("**/Info.plist") {
                filter<ReplaceTokens>(
                    "tokens" to mapOf(
                        "JREDIR" to macOsJreUnpackDir.listFiles()[0].name,
                        "SHORT_VERSION_STRING" to "$version",
                        "VERSION_STRING" to "2",
                        "ZAPJAR" to jar.get().archiveFileName.get()
                    )
                )
            }
        }
        from("src/main/resources/resource/ZAP.icns") {
            into("OWASP ZAP.app/Contents/Resources/")
        }
        val zapDir = "OWASP ZAP.app/Contents/Java/"
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
        doLast {
            ant.withGroovyBuilder {
                "symlink"(mapOf("link" to "$macOsDistDataDir/Applications", "resource" to "/Applications"))
            }
        }
    }

    tasks.register<Exec>("distMac${it.suffix}") {
        group = "Distribution"
        description = "Bundles the macOS${it.taskDesc} distribution."

        dependsOn(prepareDistMac)

        val outputDir = file("$buildDir/distributions")
        workingDir = macOsDistDataDir
        executable = "hdiutil"
        args(
            listOf(
                "create",
                "-format", "UDBZ",
                "-megabytes", "800",
                "-fs", "HFS+",
                "-srcfolder", macOsDistDataDir,
                "-volname", "OWASP ZAP",
                "$outputDir/ZAP_$version${it.fileNameSuffix}.dmg"
            )
        )

        doFirst {
            mkdir(outputDir)
        }
    }
}

val jarDaily by tasks.registering(Jar::class) {
    archiveVersion.set(dailyVersion)

    from(jar.map { it.source }) {
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
        exclude(jar.get().archiveFileName.get())
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
        exclude(jar.get().archiveFileName.get())
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
