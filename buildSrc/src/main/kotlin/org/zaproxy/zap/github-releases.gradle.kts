package org.zaproxy.zap

import com.install4j.gradle.Install4jTask
import com.netflix.gradle.plugins.deb.Deb
import java.util.regex.Pattern
import org.zaproxy.zap.GitHubUser
import org.zaproxy.zap.GitHubRepo
import org.zaproxy.zap.tasks.CreateGitHubRelease
import org.zaproxy.zap.tasks.CreateMainRelease
import org.zaproxy.zap.tasks.CreatePullRequest
import org.zaproxy.zap.tasks.CreateTagAndGitHubRelease
import org.zaproxy.zap.tasks.HandleMainRelease
import org.zaproxy.zap.tasks.HandleWeeklyRelease
import org.zaproxy.zap.tasks.PrepareMainRelease
import org.zaproxy.zap.tasks.PrepareNextDevIter

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

val buildFileVersionPattern = Pattern.compile("""version = "([^"]+)"""")

val prepareNextDevIter by tasks.registering(PrepareNextDevIter::class) {
    buildFile.set(File(projectDir, "zap.gradle.kts"))

    versionPattern.set(buildFileVersionPattern)
    versionBcPattern.set(Pattern.compile("""val versionBC = "([^"]+)""""))

    val listOfExpression = """(?sm)listOf\((.*?)\)$"""
    clearDataPatterns.set(listOf(
        Pattern.compile("packageExcludes = $listOfExpression"),
        Pattern.compile("fieldExcludes = $listOfExpression"),
        Pattern.compile("classExcludes = $listOfExpression"),
        Pattern.compile("methodExcludes = $listOfExpression")))
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
    buildFile.set(File(projectDir, "zap.gradle.kts"))

    versionPattern.set(buildFileVersionPattern)
}

val createPullRequestMainRelease by tasks.registering(CreatePullRequest::class) {
    user.set(ghUser)
    repo.set(zaproxyRepo)
    branchName.set("release")

    commitSummary.set("Update version to ${project.version}")
    commitDescription.set("Remove `-SNAPSHOT` from the version.")

    pullRequestTitle.set("Release version ${project.version}")
    pullRequestDescription.set("")
}

tasks.register<CreateMainRelease>("createMainRelease") {
    val tagName = "v${project.version}"

    user.set(ghUser)
    repo.set(System.getenv("GITHUB_REPOSITORY"))
    tag.set(tagName)
    tagMessage.set("Version ${project.version}")

    title.set(tagName)
    body.set("Release notes: https://www.zaproxy.org/docs/desktop/releases/${project.version}/")
    checksumAlgorithm.set("SHA-256")
    draft.set(true)

    if (!"${project.version}".endsWith("-SNAPSHOT")) {
        val distDebian by tasks.existing(Deb::class)
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
