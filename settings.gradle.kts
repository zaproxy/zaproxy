
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
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

    project.children.forEach { project -> setUpProject(project.parent!!.projectDir, project) }
}
