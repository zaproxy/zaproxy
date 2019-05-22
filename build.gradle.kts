buildDir = file("buildGradle")

apply(from = "$rootDir/gradle/travis-ci.gradle.kts")

allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "utf-8"
        options.compilerArgs = listOf("-Xlint:all", "-Xlint:-options", "-Werror", "-parameters")
    }
}
