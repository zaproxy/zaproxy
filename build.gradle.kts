plugins {
    id("com.diffplug.gradle.spotless")
    id("org.sonarqube") version "3.0"
}

apply(from = "$rootDir/gradle/travis-ci.gradle.kts")

allprojects {
    apply(plugin = "com.diffplug.gradle.spotless")

    repositories {
        mavenCentral()
    }

    spotless {
        project.plugins.withType(JavaPlugin::class) {
            java {
                licenseHeaderFile("$rootDir/gradle/spotless/license.java")
                googleJavaFormat().aosp()
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
