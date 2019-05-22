plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("commons-codec:commons-codec:1.12")
    // Gradle Plugins
    implementation("com.netflix.nebula:gradle-ospackage-plugin:6.2.0")
    implementation("de.undercouch:gradle-download-task:3.4.3")
    implementation("edu.sc.seis.launch4j:launch4j:2.4.6")
    implementation("gradle.plugin.install4j.install4j:gradle_plugin:6.1.6")
}
