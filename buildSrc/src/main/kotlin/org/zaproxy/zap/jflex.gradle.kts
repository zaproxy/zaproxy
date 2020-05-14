package org.zaproxy.zap

import org.zaproxy.zap.tasks.JFlexGenerator
import org.zaproxy.zap.tasks.JFlexToRstaTokenMaker

val jflexClasspath by configurations.creating

dependencies {
    jflexClasspath("de.jflex:jflex:1.8.2")
}

val generateRstaTokenMakers by tasks.registering {
    group = "ZAP RSTA"
    description = "Generates (and copies) all RSTA token makers."
}

val flexRstaTokenMakers = fileTree("src/main/flex/") {
   include("**/*TokenMaker.flex")
}

flexRstaTokenMakers.forEach {
    val tokenMakerName = it.getName().removeSuffix(".flex")
    val jflexTask = tasks.register<JFlexGenerator>("generateJFlex${tokenMakerName}") {
        setSource(flexRstaTokenMakers.matching { include("**/${it.name}") } )
        classpath.from(jflexClasspath)
        outputDirectory.set(project.layout.buildDirectory.dir("generated/sources/jflex/${tokenMakerName}/"))
    }
    val rstaTask = tasks.register<JFlexToRstaTokenMaker>("jflexToRsta${tokenMakerName}") {
        setSource(jflexTask)
        outputDirectory.set(project.layout.buildDirectory.dir("generated/sources/rsta/${tokenMakerName}/"))
    }
    val copyTask = tasks.register<Copy>("generateRsta${tokenMakerName}") {
        group = "ZAP RSTA"
        description = "Generates (and copies) the $tokenMakerName."

        from(rstaTask)
        into(file("src/main/java/"))
    }

    generateRstaTokenMakers {
        dependsOn(copyTask)
    }
}

