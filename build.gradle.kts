plugins {
    id("com.diffplug.spotless")
    id("org.sonarqube") version "3.1.1"
    id("com.github.ben-manes.versions") version "0.38.0"
}

apply(from = "$rootDir/gradle/ci.gradle.kts")

allprojects {
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    spotless {
        project.plugins.withType(JavaPlugin::class) {
            java {
                licenseHeaderFile("$rootDir/gradle/spotless/license.java")
                googleJavaFormat("1.7").aosp()
            }
        }

        kotlinGradle {
            ktlint()
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
        options.compilerArgs = listOf("-Xlint:all", "-Xlint:-options", "-Werror", "-parameters")
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "zaproxy_zaproxy")
        property("sonar.organization", "zaproxy")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
