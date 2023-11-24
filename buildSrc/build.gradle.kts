plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.20.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

spotless {
    java {
        licenseHeaderFile(file("../gradle/spotless/license.java"))
        googleJavaFormat("1.17.0").aosp()
    }

    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.15.21")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
    implementation("commons-codec:commons-codec:1.12")
    implementation("commons-configuration:commons-configuration:1.10")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("commons-io:commons-io:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.1")
    val jgitVersion = "6.7.0.202309050840-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation("org.kohsuke:github-api:1.95")
    // Gradle Plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.20.0")
    implementation("de.undercouch:gradle-download-task:5.4.0")
    implementation("edu.sc.seis.launch4j:launch4j:3.0.3")
    implementation("gradle.plugin.install4j.install4j.buildtools:gradle_publish:10.0.6")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.1")
    implementation("org.cyclonedx:cyclonedx-gradle-plugin:1.7.4")
}
