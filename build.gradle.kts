import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless")
    id("org.sonarqube") version "4.3.0.3225"
    id("com.github.ben-manes.versions") version "0.50.0"
    id("net.ltgt.errorprone") version "3.1.0"
}

apply(from = "$rootDir/gradle/ci.gradle.kts")

allprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "net.ltgt.errorprone")

    repositories {
        mavenCentral()
    }

    spotless {
        project.plugins.withType(JavaPlugin::class) {
            java {
                licenseHeaderFile("$rootDir/gradle/spotless/license.java")
                googleJavaFormat("1.17.0").aosp()
            }
        }

        kotlinGradle {
            ktlint()
        }
    }

    project.plugins.withType(JavaPlugin::class) {
        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:2.23.0")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
        options.compilerArgs = listOf("-Xlint:all", "-Werror", "-parameters")
        options.errorprone {
            disableAllChecks.set(true)
            error(
                "MissingOverride",
                "WildcardImport",
            )
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "zaproxy_zaproxy")
        property("sonar.organization", "zaproxy")
        property("sonar.host.url", "https://sonarcloud.io")
        // Workaround https://sonarsource.atlassian.net/browse/SONARGRADL-126
        property("sonar.exclusions", "**/*.gradle.kts")
    }
}
