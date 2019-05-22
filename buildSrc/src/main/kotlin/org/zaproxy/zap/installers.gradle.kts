package org.zaproxy.zap

import com.install4j.gradle.Install4jTask

plugins {
    com.install4j.gradle
    edu.sc.seis.launch4j
}

val install4jHomeDir: String? by project
val install4jLicense: String? by project

install4j {
    installDir = file("$install4jHomeDir")
    license = "$install4jLicense"
}

launch4j {
    libraryDir = ""
    copyConfigurable = project.copySpec{}

    mainClassName = "org.zaproxy.zap.ZAP"

    dontWrapJar = true

    version = "${project.version}"
    textVersion = "${project.version}"

    outfile = "ZAP.exe"
    chdir = ""
    icon = file("src/main/resources/resource/zap.ico").toString()

    jdkPreference = "preferJdk"
    maxHeapSize = 512
    maxHeapPercent = 25

    fileDescription = "OWASP Zed Attack Proxy"
    copyright = "The OWASP Zed Attack Proxy Project"
    productName = "OWASP Zed Attack Proxy"
    companyName = "OWASP"
    internalName = "ZAP"
}


val installerDataDir = file("$buildDir/installerData/")
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

val createExe by tasks.existing

val prepareWin32InstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "win32")
    from(createExe)
    from(bundledAddOns) {
        into("plugin")
        exclude(listOf(
                "*linux*.zap",
                "*macos*.zap",
                "jxbrowser*.zap"))
    }
}

val prepareWin64InstallerData by tasks.registering(Sync::class) {
    destinationDir = File(installerDataDir, "win64")
    from(createExe)
    from(bundledAddOns) {
        into("plugin")
        exclude(listOf(
                "*linux*.zap",
                "*macos*.zap"))
    }
}

tasks.register<Install4jTask>("installers") {
    group = "Distribution"
    description = "Creates the Linux and Windows installers."
    dependsOn(
            prepareCommonInstallerData,
            prepareLinuxInstallerData,
            prepareWin32InstallerData,
            prepareWin64InstallerData)

    projectFile = file("src/main/installer/zap.install4j")
    variables = mapOf("version" to version)
    destination = "$buildDir/install4j"

    doFirst {
        require(install4jHomeDir != null) {
            "The install4jHomeDir property must be set to build the installers."
        }
    }
}


