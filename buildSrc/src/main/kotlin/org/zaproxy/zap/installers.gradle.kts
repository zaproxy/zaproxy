package org.zaproxy.zap

import com.install4j.gradle.Install4jTask
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import edu.sc.seis.launch4j.tasks.Launch4jLibraryTask
import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    com.install4j.gradle
    de.undercouch.download
    edu.sc.seis.launch4j
}

var install4jHomeDirValidated = false
val install4jHomeDir: String? by project
val install4jLicense: String? by project

val install4jVersion = "10.0.8"

install4j {
    installDir = file("$install4jHomeDir")
    license = System.getenv("INSTALL4J_LICENSE") ?: install4jLicense
}

launch4j {
    libraryDir.set(".")
    copyConfigurable.set(listOf<String>())

    mainClassName.set("org.zaproxy.zap.ZAP")

    dontWrapJar.set(true)

    version.set("${project.version}")
    textVersion.set("${project.version}")

    outfile.set("ZAP.exe")
    chdir.set("")
    icon.set(file("src/main/resources/resource/zap.ico").toString())

    maxHeapSize.set(512)
    maxHeapPercent.set(25)

    fileDescription.set("Zed Attack Proxy")
    copyright.set("The Zed Attack Proxy Project")
    productName.set("Zed Attack Proxy")
    companyName.set("ZAP")
    internalName.set("ZAP")

    downloadUrl.set("https://adoptium.net/")
}

val installerDataDir = layout.buildDirectory.dir("installerData").get().asFile
val bundledAddOns: Any = provider {
    if (version.toString().endsWith("SNAPSHOT")) {
        file("src/main/dist/plugin")
    } else {
        tasks.named("downloadMainAddOns")
    }
}

val prepareCommonInstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "common")
    from(tasks.named("distFiles")) {
        exclude("plugin")
    }
}

val prepareLinuxInstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "linux")
    from(bundledAddOns) {
        into("plugin")
        exclude(listOf(
                "*macos*.zap",
                "*windows*.zap"))
    }
    from(file("src/main/resources/resource/zap1024x1024.png"))
}

val createExe by tasks.existing(Launch4jLibraryTask::class) {
    mustRunAfter("distFiles")
}
val zapExe = createExe.map { it.dest }

val prepareWin32InstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "win32")
    from(zapExe)
    from(bundledAddOns) {
        into("plugin")
        exclude(listOf(
                "*linux*.zap",
                "*macos*.zap"))
    }
}

val prepareWin64InstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "win64")
    from(zapExe)
    from(bundledAddOns) {
        into("plugin")
        exclude(listOf(
                "*linux*.zap",
                "*macos*.zap"))
    }
}

val installers by tasks.registering(Install4jTask::class) {
    group = "Distribution"
    description = "Creates the Linux and Windows installers."
    dependsOn(
            prepareCommonInstallerData,
            prepareLinuxInstallerData,
            prepareWin32InstallerData,
            prepareWin64InstallerData)

    projectFile = file("src/main/installer/zap.install4j")
    variables = mapOf("version" to version)
    destination = layout.buildDirectory.dir("install4j").get().asFile

    doFirst {
        require(install4jHomeDirValidated || install4jHomeDir != null) {
            "The install4jHomeDir property must be set to build the installers."
        }
    }
}

if (install4jHomeDir == null && Os.isFamily(Os.FAMILY_UNIX)) {
    val install4jVersionUnderscores = install4jVersion.replace('.', '_')
    val install4jBinDir = layout.buildDirectory.dir("install4jBin").get().asFile
    val install4jBinUnpackDir = File(install4jBinDir, "unpacked")
    val install4jBinFile = File(install4jBinDir, "install4j.tar.gz")
    val install4jDir = File(install4jBinUnpackDir, "install4j$install4jVersion")

    val downloadInstall4jBin by tasks.registering(Download::class) {
        src("https://download.ej-technologies.com/install4j/install4j_linux-x64_$install4jVersionUnderscores.tar.gz")
        dest(install4jBinFile)
        connectTimeout(60_000)
        readTimeout(60_000)
        onlyIfModified(true)
    }

    val verifyInstall4jBin by tasks.registering(Verify::class) {
        dependsOn(downloadInstall4jBin)
        src(install4jBinFile)
        algorithm("SHA-256")
        checksum("3d64c4a28d2b11ea26ae3cb8f1b0288de76811c1e0f37e06a2a4d8498b8130e7")
    }

    val unpackInstall4jBin by tasks.registering(Copy::class) {
        dependsOn(verifyInstall4jBin)
        from(tarTree(install4jBinFile))
        into(install4jBinUnpackDir)
        doFirst {
            delete(install4jBinUnpackDir)
        }
    }

    installers {
        dependsOn(unpackInstall4jBin)
    }

    install4j {
        installDir = file("$install4jDir")
    }

    install4jHomeDirValidated = true
}

