plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.11.0"
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

val javaVersion = JavaVersion.VERSION_11
configure<JavaPluginConvention> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlinDslPluginOptions {
    jvmTarget.set(javaVersion.toString())
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.15.21")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
    implementation("commons-codec:commons-codec:1.12")
    implementation("commons-configuration:commons-configuration:1.10")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.4")
    val jgitVersion = "5.3.1.201904271842-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation("org.kohsuke:github-api:1.95")
    // Gradle Plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.11.0")
    implementation("com.netflix.nebula:gradle-ospackage-plugin:8.5.6")
    implementation("de.undercouch:gradle-download-task:4.1.1")
    implementation("edu.sc.seis.launch4j:launch4j:2.5.0")
    implementation("gradle.plugin.install4j.install4j.buildtools:gradle_publish:10.0.3")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.1")
}
