plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "5.12.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

spotless {
    java {
        licenseHeaderFile(file("../gradle/spotless/license.java"))
        googleJavaFormat("1.7").aosp()
    }

    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.15.21")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
    implementation("commons-codec:commons-codec:1.12")
    implementation("com.google.code.gson:gson:2.8.5")
    val jgitVersion = "5.3.1.201904271842-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation("org.kohsuke:github-api:1.95")
    // Gradle Plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.12.1")
    implementation("com.netflix.nebula:gradle-ospackage-plugin:8.5.6")
    implementation("de.undercouch:gradle-download-task:4.1.1")
    implementation("edu.sc.seis.launch4j:launch4j:2.5.0")
    if (JavaVersion.current() != JavaVersion.VERSION_1_8)
        implementation("gradle.plugin.install4j.install4j.buildtools:gradle_publish:9.0.2")
    else
        // Just for compilation, not actually used (installers task is replaced).
        implementation("gradle.plugin.install4j.install4j:gradle_plugin:8.0.11")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.2.9")
}
