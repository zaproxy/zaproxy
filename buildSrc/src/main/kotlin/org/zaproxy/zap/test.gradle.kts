package org.zaproxy.zap

plugins {
    `java-library`
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register("testAll") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs all tests."

    dependsOn(tasks.withType<Test>())
}

val java: JavaPluginExtension
    get() = the<JavaPluginExtension>()

fun setupTest(name: String, addToCheck: Boolean = true): TaskProvider<Test> {
    val nameTest = "test$name"

    java.sourceSets {
        register(nameTest) {
            compileClasspath += sourceSets["main"].output
            runtimeClasspath += sourceSets["main"].output
        }
    }

    configurations {
        "${nameTest}Implementation" { extendsFrom(configurations["testImplementation"]) }
        "${nameTest}RuntimeOnly" { extendsFrom(configurations["testRuntimeOnly"]) }
    }

    val nameLowerCase = name.toLowerCase()
    val testTask = tasks.register<Test>(nameTest) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs the $nameLowerCase tests."

        testClassesDirs = java.sourceSets[nameTest].output.classesDirs
        classpath = java.sourceSets[nameTest].runtimeClasspath
        shouldRunAfter(tasks.test)

        reports {
            html.outputLocation.set(file("${html.outputLocation.get().asFile.parent}/$nameLowerCase"))
            junitXml.outputLocation.set(file("${junitXml.outputLocation.get().asFile.parent}/$nameLowerCase"))
        }
    }

    if (addToCheck) {
        tasks.check {
            dependsOn(testTask)
        }
    }
    return testTask
}

setupTest("Gui", false)
