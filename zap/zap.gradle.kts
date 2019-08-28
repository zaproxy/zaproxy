import java.time.LocalDate
import java.util.stream.Collectors
import org.zaproxy.zap.tasks.GradleBuildWithGitRepos

plugins {
    `java-library`
    org.zaproxy.zap.distributions
    org.zaproxy.zap.installers
    org.zaproxy.zap.`github-releases`
    org.zaproxy.zap.publish
    org.zaproxy.zap.spotless
}

group = "org.zaproxy"
version = "2.8.1"

val versionLangFile = "1"
val creationDate by extra { project.findProperty("creationDate") ?: LocalDate.now().toString() }
val distDir = file("src/main/dist/")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api("com.fifesoft:rsyntaxtextarea:3.0.3")
    api("com.github.zafarkhaja:java-semver:0.9.0")
    api("commons-beanutils:commons-beanutils:1.9.3")
    api("commons-codec:commons-codec:1.12")
    api("commons-collections:commons-collections:3.2.2")
    api("commons-configuration:commons-configuration:1.10")
    api("commons-httpclient:commons-httpclient:3.1")
    api("commons-io:commons-io:2.6")
    api("commons-lang:commons-lang:2.6")
    api("org.apache.commons:commons-lang3:3.9")
    api("org.apache.commons:commons-text:1.6")
    api("edu.umass.cs.benchlab:harlib:1.1.2")
    api("javax.help:javahelp:2.0.05")
    api("log4j:log4j:1.2.17")
    api("net.htmlparser.jericho:jericho-html:3.4")
    api("net.sf.json-lib:json-lib:2.4:jdk15")
    api("org.apache.commons:commons-csv:1.6")
    api("org.bouncycastle:bcmail-jdk15on:1.61")
    api("org.bouncycastle:bcprov-jdk15on:1.61")
    api("org.bouncycastle:bcpkix-jdk15on:1.61")
    api("org.hsqldb:hsqldb:2.4.1")
    api("org.jfree:jfreechart:1.0.19")
    api("org.jgrapht:jgrapht-core:0.9.0")
    api("org.swinglabs.swingx:swingx-all:1.6.5-1")
    api("org.xerial:sqlite-jdbc:3.27.2.1")

    implementation("commons-validator:commons-validator:1.6")
    // Don't need its dependencies, for now.
    implementation("org.jitsi:ice4j:1.0") {
        setTransitive(false)
    }

    // The following are no longer used by core, remove once
    // all (known) add-ons are updated accordingly.
    runtimeOnly("com.googlecode.java-diff-utils:diffutils:1.2.1")
    runtimeOnly("org.jdom:jdom:1.1.3")
    // -----------------------

    runtimeOnly("commons-jxpath:commons-jxpath:1.3")
    runtimeOnly("commons-logging:commons-logging:1.2")
    runtimeOnly("com.io7m.xom:xom:1.2.10") {
        setTransitive(false)
    }

    testImplementation("junit:junit:4.11")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    testImplementation("org.mockito:mockito-all:1.10.8")

    testRuntimeOnly(files(distDir))
}

tasks.register<JavaExec>("run") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs ZAP from source, using the default dev home."

    main = "org.zaproxy.zap.ZAP"
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = distDir
}

listOf("jar", "jarDaily").forEach {
    tasks.named<Jar>(it) {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirMode = "0755".toIntOrNull(8)
        fileMode = "0644".toIntOrNull(8)

        val attrs = mapOf(
                "Main-Class" to "org.zaproxy.zap.ZAP",
                "Implementation-Version" to ToString({ archiveVersion.get() }),
                "Create-Date" to creationDate,
                "Class-Path" to ToString({ configurations.runtimeClasspath.get().files.stream().map { file -> "lib/${file.name}" }.sorted().collect(Collectors.joining(" ")) }))

        manifest {
            attributes(attrs)
        }
    }
}

tasks.named<Javadoc>("javadoc") {
    title = "OWASP Zed Attack Proxy"
    source = sourceSets["main"].allJava.matching {
        include("org/parosproxy/**")
        include("org/zaproxy/**")
    }
    (options as StandardJavadocDocletOptions).run {
        links("https://docs.oracle.com/javase/8/docs/api/")
        encoding = "UTF-8"
        source("${java.targetCompatibility}")
    }
}

val langPack by tasks.registering(Zip::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Assembles the language pack for the Core Language Files add-on."

    archiveFileName.set("$buildDir/langpack/ZAP_${project.version}_language_pack.$versionLangFile.zaplang")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    into("lang") {
        from(File(distDir, "lang"))
        from("src/main/resources/org/zaproxy/zap/resources") {
            include("Messages.properties", "vulnerabilities.xml")
        }
    }
}

tasks.register<Copy>("copyLangPack") {
    group = "ZAP Misc"
    description = "Copies the language pack into the Core Language Files add-on (assumes zap-extensions repo is in same directory as zaproxy)."

    from(langPack)
    into("$rootDir/../zap-extensions/addOns/coreLang/src/main/zapHomeFiles/lang/")
}

val copyWeeklyAddOns by tasks.registering(GradleBuildWithGitRepos::class) {
    group = "ZAP Misc"
    description = "Copies the weekly add-ons into plugin dir, built from local repos."

    repositoriesDirectory.set(rootDir.parentFile)
    repositoriesDataFile.set(file("src/main/weekly-add-ons.json"))
    cloneRepositories.set(false)
    updateRepositories.set(false)

    val outputDir = file("src/main/dist/plugin/")
    tasks {
        register("copyZapAddOn") {
            args.set(listOf("--into=$outputDir"))
        }
    }
}

val generateAllApiEndpoints by tasks.registering {
    group = "ZAP Misc"
    description = "Generates (and copies) the ZAP API endpoints for all languages."
}

listOf(
    "org.zaproxy.zap.extension.api.GoAPIGenerator",
    "org.zaproxy.zap.extension.api.JavaAPIGenerator",
    "org.zaproxy.zap.extension.api.NodeJSAPIGenerator",
    "org.zaproxy.zap.extension.api.PhpAPIGenerator",
    "org.zaproxy.zap.extension.api.PythonAPIGenerator",
    "org.zaproxy.zap.extension.api.RustAPIGenerator"
).forEach {
    val langName = it.removePrefix("org.zaproxy.zap.extension.api.").removeSuffix("APIGenerator")
    val task = tasks.register<JavaExec>("generate${langName}ApiEndpoints") {
        group = "ZAP Misc"
        description = "Generates (and copies) the ZAP API endpoints for $langName."

        main = it
        classpath = sourceSets["main"].runtimeClasspath
        workingDir = file("$rootDir")
    }

    generateAllApiEndpoints {
        dependsOn(task)
    }
}

launch4j {
    jar = tasks.named<Jar>("jar").get().archiveFileName.get()
}

class ToString(private val callable: Callable<String>) {
    override fun toString() = callable.call()
}
