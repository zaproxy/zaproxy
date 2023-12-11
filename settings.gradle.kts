plugins {
    id("org.zaproxy.common.settings") version "0.2.0"
    id("com.diffplug.spotless") version "6.20.0" apply false
}

rootProject.name = "zaproxy"

include("zap")

rootProject.children.forEach { project -> setUpProject(settingsDir, project) }

fun setUpProject(parentDir: File, project: ProjectDescriptor) {
    project.projectDir = File(parentDir, project.name)
    project.buildFileName = "${project.name}.gradle.kts"

    require(project.projectDir.isDirectory) {
        "Project ${project.name} has no directory: ${project.projectDir}"
    }
    require(project.buildFile.isFile) {
        "Project ${project.name} has no build file: ${project.buildFile}"
    }

    project.children.forEach { it -> setUpProject(it.parent!!.projectDir, it) }
}
