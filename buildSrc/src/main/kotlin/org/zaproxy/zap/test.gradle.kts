package org.zaproxy.zap

plugins {
    `java-library`
    eclipse
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register("testAll") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs all tests."

    dependsOn(tasks.withType<Test>())
}

val java: JavaPluginConvention
    get() = the<JavaPluginConvention>()

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

    // Workaround for https://github.com/eclipse/buildship/issues/476
    eclipse {
        classpath {
            plusConfigurations.plusAssign(configurations["${nameTest}CompileClasspath"])
            plusConfigurations.plusAssign(configurations["${nameTest}RuntimeClasspath"])
        }
    }

    val nameLowerCase = name.toLowerCase()
    val testTask = tasks.register<Test>(nameTest) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs the $nameLowerCase tests."

        testClassesDirs = java.sourceSets[nameTest].output.classesDirs
        classpath = java.sourceSets[nameTest].runtimeClasspath
        shouldRunAfter(tasks.test)

        reports {
            html.destination = file("${html.destination.parent}/$nameLowerCase")
            junitXml.destination = file("${junitXml.destination.parent}/$nameLowerCase")
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
