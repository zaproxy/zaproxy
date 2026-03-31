import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.diffplug.spotless")
    id("org.sonarqube") version "6.0.1.5171"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("net.ltgt.errorprone") version "4.1.0"
}

apply(from = "$rootDir/gradle/ci.gradle.kts")

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.zaproxy.common")
    apply(plugin = "net.ltgt.errorprone")

    spotless {
        kotlinGradle {
            ktlint()
        }
    }

    project.plugins.withType(JavaPlugin::class) {
        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:2.36.0")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
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
        property("sonar.projectKey", System.getProperty("sonar.projectKey") ?: "csci630_zaproxy")
        property("sonar.organization", System.getProperty("sonar.organization") ?: "csci630")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.gradle.scanAll", "true")
        // Workaround https://sonarsource.atlassian.net/browse/SONARGRADL-126
        property("sonar.exclusions", "**/*.gradle.kts, examples/**")
    }
}
