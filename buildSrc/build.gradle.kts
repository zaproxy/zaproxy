plugins {
    `kotlin-dsl`
    id("com.diffplug.gradle.spotless") version "3.28.1"
}

repositories {
    jcenter()
    gradlePluginPortal()
}

spotless {
    java {
        licenseHeaderFile(file("../gradle/spotless/license.java"))
        googleJavaFormat().aosp()
    }

    kotlinGradle {
        ktlint()
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.15.21")
    implementation("commons-codec:commons-codec:1.12")
    implementation("com.google.code.gson:gson:2.8.5")
    val jgitVersion = "5.3.1.201904271842-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation("org.kohsuke:github-api:1.95")
    // Gradle Plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:3.28.1")
    implementation("com.netflix.nebula:gradle-ospackage-plugin:8.2.0")
    implementation("de.undercouch:gradle-download-task:3.4.3")
    implementation("edu.sc.seis.launch4j:launch4j:2.4.6")
    implementation("gradle.plugin.install4j.install4j:gradle_plugin:8.0.7")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.2.9")
}
