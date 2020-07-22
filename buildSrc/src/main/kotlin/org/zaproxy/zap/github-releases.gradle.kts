package org.zaproxy.zap

import com.install4j.gradle.Install4jTask
import com.netflix.gradle.plugins.deb.Deb
import org.zaproxy.zap.tasks.CreateGitHubRelease

System.getenv("GITHUB_REF")?.let { ref ->
    if ("refs/tags/" !in ref) {
        return@let
    }

    val targetTag = ref.removePrefix("refs/tags/")

    tasks.register<CreateGitHubRelease>("createMainReleaseFromGitHubRef") {
        val targetVersion = targetTag.removePrefix("v")

        authToken.set(System.getenv("GITHUB_TOKEN"))
        repo.set(System.getenv("GITHUB_REPOSITORY"))
        tag.set(targetTag)

        title.set("$targetTag")
        body.set("")
        checksumAlgorithm.set("SHA-256")
        draft.set(true)

        val distDebian by tasks.existing(Deb::class)
        val installers by tasks.existing(Install4jTask::class)

        // Depend explicitly, not being auto wired.
        dependsOn(distDebian)
        dependsOn(installers)

        val installersFileTree: Provider<FileTree> = installers.map { fileTree(it.destination!!) }

        assets {
            register("core") {
                file.set(tasks.named<Zip>("distCore").flatMap { it.archiveFile })
                contentType.set("application/zip")
            }
            register("crossplatform") {
                file.set(tasks.named<Zip>("distCrossplatform").flatMap { it.archiveFile })
                contentType.set("application/zip")
            }
            register("debian") {
                file.set(distDebian.flatMap { it.archiveFile })
                contentType.set("application/vnd.debian.binary-package")
            }
            register("linux") {
                file.set(tasks.named<Tar>("distLinux").flatMap { it.archiveFile })
                contentType.set("application/gzip")
            }
            register("linux-installer") {
                file.set(mapToFile(installersFileTree, "ZAP_${version.toString().replace('.', '_')}_unix.sh"))
                contentType.set("application/x-shellscript")
            }
            register("windows-installer") {
                file.set(mapToFile(installersFileTree, "ZAP_${version.toString().replace('.', '_')}_windows.exe"))
                contentType.set("application/x-ms-dos-executable")
            }
            register("windows32-installer") {
                file.set(mapToFile(installersFileTree, "ZAP_${version.toString().replace('.', '_')}_windows-x32.exe"))
                contentType.set("application/x-ms-dos-executable")
            }
        }

        doFirst {
            require(project.version == targetVersion) {
                "Version of the tag $targetVersion does not match the declared version ${project.version}"
            }
        }
    }

    tasks.register<CreateGitHubRelease>("createWeeklyReleaseFromGitHubRef") {
        val targetDailyVersion = targetTag.removePrefix("w")

        authToken.set(System.getenv("GITHUB_TOKEN"))
        repo.set(System.getenv("GITHUB_REPOSITORY"))
        tag.set(targetTag)

        title.set("$targetTag")
        body.set("")
        checksumAlgorithm.set("SHA-256")
        draft.set(true)
        prerelease.set(true)

        assets {
            register("weekly") {
                file.set(tasks.named<Zip>("distWeekly").flatMap { it.archiveFile })
                contentType.set("application/zip")
            }
        }

        doFirst {
            val creationDate = project.extra["creationDate"]
            require(creationDate == targetDailyVersion) {
                "Version of the tag $targetDailyVersion does not match the creation date $creationDate"
            }
        }
    }
}

fun mapToFile(fileTree: Provider<FileTree>, fileName: String): Provider<RegularFile> {
    return project.layout.projectDirectory.file(
        fileTree.map {
            it.filter { f: File ->
                f.name == fileName
            }.files.first().absolutePath
        }
    )
}