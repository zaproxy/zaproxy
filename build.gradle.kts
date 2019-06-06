plugins {
    id("com.diffplug.gradle.spotless")
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
