package org.zaproxy.zap

import com.install4j.gradle.Install4jTask
import java.util.regex.Pattern
import org.cyclonedx.gradle.CycloneDxTask
import org.zaproxy.zap.GitHubUser
import org.zaproxy.zap.GitHubRepo
import org.zaproxy.zap.tasks.CreateDmg
import org.zaproxy.zap.tasks.CreateGitHubRelease
import org.zaproxy.zap.tasks.CreateMainRelease
import org.zaproxy.zap.tasks.CreatePullRequest
import org.zaproxy.zap.tasks.CreateTagAndGitHubRelease
import org.zaproxy.zap.tasks.HandleMainRelease
import org.zaproxy.zap.tasks.HandleWeeklyRelease
import org.zaproxy.zap.tasks.PrepareMainRelease
import org.zaproxy.zap.tasks.PrepareNextDevIter
import org.zaproxy.zap.tasks.UploadAssetsGitHubRelease

val ghUser = GitHubUser("zapbot", "12745184+zapbot@users.noreply.github.com", System.getenv("ZAPBOT_TOKEN"))
val zaproxyRepo = GitHubRepo("zaproxy", "zaproxy", rootDir)

tasks.register<CreateTagAndGitHubRelease>("createWeeklyRelease") {
    val dateProvider = provider { project.extra["creationDate"] }
    val tagName = dateProvider.map { "w$it" }

    user.set(ghUser)
    repo.set(System.getenv("GITHUB_REPOSITORY"))
    tag.set(tagName)
    tagMessage.set(dateProvider.map { "Weekly release $it" })

    title.set(tagName)
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
}

val prepareNextDevIter by tasks.registering(PrepareNextDevIter::class) {
    propertiesFile.set(File(projectDir, "gradle.properties"))

    versionProperty.set("version")
    versionBcProperty.set("zap.japicmp.baseversion")

    japicmpExcludedDataFile.set(File(projectDir, "gradle/japicmp.yaml"))
}

val createPullRequestNextDevIter by tasks.registering(CreatePullRequest::class) {
    user.set(ghUser)
    repo.set(zaproxyRepo)
    branchName.set("bump-version")

    commitSummary.set("Prepare next dev iteration")
    commitDescription.set("Update versions and clear `japicmp` exclusions.")

    dependsOn(prepareNextDevIter)
}

val prepareMainRelease by tasks.registering(PrepareMainRelease::class) {
    propertiesFile.set(File(projectDir, "gradle.properties"))
    securityFile.set(File(rootDir, "SECURITY.md"))
    snapcraftFile.set(File(rootDir, "snap/snapcraft.yaml"))

    oldVersionProperty.set("zap.japicmp.baseversion")
    versionProperty.set("version")
}

val createPullRequestMainRelease by tasks.registering(CreatePullRequest::class) {
    user.set(ghUser)
    repo.set(zaproxyRepo)
    branchName.set("release")

    commitSummary.set("Update version to ${project.version}")
    commitDescription.set("""
    |Remove `-SNAPSHOT` from the version.
    |Update version in `SECURITY.md` file.
    |Update version for snap.
    """.trimMargin())

    pullRequestTitle.set("Release version ${project.version}")
    pullRequestDescription.set("")
}

val checksumAlg = "SHA-256"
tasks.register<CreateMainRelease>("createMainRelease") {
    val tagName = "v${project.version}"

    user.set(ghUser)
    repo.set(System.getenv("GITHUB_REPOSITORY"))
    tag.set(tagName)
    tagMessage.set("Version ${project.version}")

    title.set(tagName)
    body.set("Release notes: https://www.zaproxy.org/docs/desktop/releases/${project.version}/")
    checksumAlgorithm.set(checksumAlg)
    draft.set(true)

    if (!"${project.version}".endsWith("-SNAPSHOT")) {
        val installers by tasks.existing(Install4jTask::class)

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
            register("bom") {
                val cyclonedxBom by tasks.existing(CycloneDxTask::class)
                file.set(cyclonedxBom.map { project.layout.projectDirectory.file(File(it.destination.get(), "${it.outputName.get()}.json").absolutePath) })
                contentType.set("application/json")
            }
        }
    }
}

tasks.register<UploadAssetsGitHubRelease>("uploadMacDist") {
    val tagName = "v${project.version}"

    user.set(ghUser)
    repo.set(System.getenv("GITHUB_REPOSITORY"))
    tag.set(tagName)

    checksumAlgorithm.set(checksumAlg)

    if (!"${project.version}".endsWith("-SNAPSHOT")) {
        assets {
            register("macos") {
                file.set(tasks.named<CreateDmg>("distMac").flatMap { it.dmg })
                contentType.set("application/x-diskcopy")
            }
            register("macos-arm64") {
                file.set(tasks.named<CreateDmg>("distMacArm64").flatMap { it.dmg })
                contentType.set("application/x-diskcopy")
            }
        }
    }
}

System.getenv("GITHUB_REF")?.let { ref ->
    if ("refs/tags/" !in ref) {
        return@let
    }

    val targetTag = ref.removePrefix("refs/tags/")

    val adminRepo = GitHubRepo("zaproxy", "zap-admin")

    val handleWeeklyRelease by tasks.registering(HandleWeeklyRelease::class) {
        val targetDailyVersion = targetTag.removePrefix("w")
        downloadUrl.set("https://github.com/zaproxy/zaproxy/releases/download/w$targetDailyVersion/ZAP_WEEKLY_D-$targetDailyVersion.zip")
        checksumAlgorithm.set("SHA-256")

        gitHubUser.set(ghUser)
        gitHubRepo.set(adminRepo)

        eventType.set("daily-release")
    }

    val handleMainRelease by tasks.registering(HandleMainRelease::class) {
        version.set(targetTag.removePrefix("v"))

        gitHubUser.set(ghUser)
        gitHubRepo.set(adminRepo)

        eventType.set("main-release")
    }

    tasks.register("handleReleaseFromGitHubRef") {
        if (targetTag.startsWith("w")) {
            dependsOn(handleWeeklyRelease)
        } else if (targetTag.startsWith("v")) {
            dependsOn(handleMainRelease)
            dependsOn(createPullRequestNextDevIter)
            dependsOn("crowdinUploadSourceFiles")
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
