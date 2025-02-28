plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless")
    id("org.zaproxy.common")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

spotless {
    kotlinGradle {
        ktlint()
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (JavaVersion.current().getMajorVersion() >= "21") {
       options.compilerArgs = options.compilerArgs + "-Xlint:-this-escape"
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.15.21")
    implementation("com.github.zafarkhaja:java-semver:0.9.0")
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("commons-configuration:commons-configuration:1.10")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("commons-io:commons-io:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    val jgitVersion = "7.1.0.202411261347-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation("org.kohsuke:github-api:1.326")
    // Include annotations used by the above library to avoid compiler warnings.
    compileOnly("com.google.code.findbugs:findbugs-annotations:3.0.1")
    compileOnly("com.infradna.tool:bridge-method-annotation:1.18") {
        exclude(group = "org.jenkins-ci")
    }
    // Gradle Plugins
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
    implementation("de.undercouch:gradle-download-task:5.6.0")
    implementation("edu.sc.seis.launch4j:launch4j:3.0.6")
    implementation("gradle.plugin.install4j.install4j.buildtools:gradle_publish:10.0.8")
    implementation("me.champeau.gradle:japicmp-gradle-plugin:0.4.6")
    implementation("org.cyclonedx:cyclonedx-gradle-plugin:2.2.0")
}
