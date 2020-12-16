import japicmp.model.JApiChangeStatus
import java.time.LocalDate
import java.util.stream.Collectors
import me.champeau.gradle.japicmp.JapicmpTask
import org.zaproxy.zap.japicmp.AcceptMethodAbstractNowDefaultRule
import org.zaproxy.zap.tasks.GradleBuildWithGitRepos

plugins {
    `java-library`
    jacoco
    id("me.champeau.gradle.japicmp")
    org.zaproxy.zap.distributions
    org.zaproxy.zap.installers
    org.zaproxy.zap.`github-releases`
    org.zaproxy.zap.jflex
    org.zaproxy.zap.publish
    org.zaproxy.zap.spotless
    org.zaproxy.zap.test
}

group = "org.zaproxy"
version = "2.10.0"
val versionBC = "2.9.0"

val versionLangFile = "1"
val creationDate by extra { project.findProperty("creationDate") ?: LocalDate.now().toString() }
val distDir = file("src/main/dist/")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

jacoco {
    toolVersion = "0.8.5"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
    }
}

dependencies {
    api("com.fifesoft:rsyntaxtextarea:3.1.1")
    api("com.github.zafarkhaja:java-semver:0.9.0")
    api("commons-beanutils:commons-beanutils:1.9.4")
    api("commons-codec:commons-codec:1.15")
    api("commons-collections:commons-collections:3.2.2")
    api("commons-configuration:commons-configuration:1.10")
    api("commons-httpclient:commons-httpclient:3.1")
    api("commons-io:commons-io:2.8.0")
    api("commons-lang:commons-lang:2.6")
    api("org.apache.commons:commons-lang3:3.11")
    api("org.apache.commons:commons-text:1.9")
    api("edu.umass.cs.benchlab:harlib:1.1.2")
    api("javax.help:javahelp:2.0.05")
    val log4jVersion = "2.14.0"
    api("org.apache.logging.log4j:log4j-api:$log4jVersion")
    api("org.apache.logging.log4j:log4j-1.2-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("net.htmlparser.jericho:jericho-html:3.4")
    api("net.sf.json-lib:json-lib:2.4:jdk15")
    api("org.apache.commons:commons-csv:1.8")
    val bcVersion = "1.67"
    api("org.bouncycastle:bcmail-jdk15on:$bcVersion")
    api("org.bouncycastle:bcprov-jdk15on:$bcVersion")
    api("org.bouncycastle:bcpkix-jdk15on:$bcVersion")
    api("org.hsqldb:hsqldb:2.5.1")
    api("org.jfree:jfreechart:1.5.1")
    api("org.jgrapht:jgrapht-core:0.9.0")
    api("org.swinglabs.swingx:swingx-all:1.6.5-1")
    api("org.xerial:sqlite-jdbc:3.32.3.2")

    implementation("commons-validator:commons-validator:1.7")
    // Don't need its dependencies, for now.
    implementation("org.jitsi:ice4j:1.0") {
        setTransitive(false)
    }
    implementation("com.formdev:flatlaf:0.45")

    runtimeOnly("commons-jxpath:commons-jxpath:1.3")
    runtimeOnly("commons-logging:commons-logging:1.2")
    runtimeOnly("com.io7m.xom:xom:1.2.10") {
        setTransitive(false)
    }

    testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2") {
        // Not needed.
        exclude(group = "org.junit")
    }
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    val jupiterVersion = "5.7.0"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:3.6.28")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    testImplementation("org.nanohttpd:nanohttpd-webserver:2.3.1")

    testRuntimeOnly(files(distDir))

    testGuiImplementation("org.assertj:assertj-swing:3.17.1")
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

val japicmp by tasks.registering(JapicmpTask::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks ${project.name}.jar binary compatibility with latest version ($versionBC)."

    oldClasspath = files(zapJar(versionBC))
    newClasspath = files(tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME).map { it.archivePath })
    setIgnoreMissingClasses(true)

    packageExcludes = listOf(
        // Not intended to be used (directly) by add-ons.
        "org.zaproxy.zap.extension.httppanel.view.syntaxhighlight.lexers"
    )

    fieldExcludes = listOf(
        // Not part of the public API:
        "org.zaproxy.zap.extension.autoupdate.AddOnsTableModel#logger",
        "org.zaproxy.zap.extension.users.DialogAddUser#log",
        "org.zaproxy.zap.ZAP#JERICHO_LOGGER_PROVIDER"
    )

    classExcludes = listOf(
        "org.parosproxy.paros.db.paros.ParosTableAlert",
        "org.parosproxy.paros.db.RecordAlert",
        "org.zaproxy.zap.db.sql.SqlTableAlert",
        "org.zaproxy.zap.extension.log4j.ZapOutputWriter",
        "org.zaproxy.zap.extension.script.PacScript"
    )

    methodExcludes = listOf(
        "org.parosproxy.paros.network.HttpMessage#getParamNameSet(org.parosproxy.paros.network.HtmlParameter\$Type,java.lang.String)",
        "org.parosproxy.paros.core.scanner.Variant#getLeafName(java.lang.String,org.parosproxy.paros.network.HttpMessage)",
        "org.parosproxy.paros.core.scanner.Variant#getTreePath(org.parosproxy.paros.network.HttpMessage)",
        "org.parosproxy.paros.core.scanner.VariantScript#getLeafName(org.parosproxy.paros.core.scanner.VariantCustom,java.lang.String,org.parosproxy.paros.network.HttpMessage)",
        "org.parosproxy.paros.core.scanner.VariantScript#getTreePath(org.parosproxy.paros.core.scanner.VariantCustom,org.parosproxy.paros.network.HttpMessage)",
        "org.parosproxy.paros.db.TableAlert#write(int,int,java.lang.String,int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,int,int,int,int)",
        "org.zaproxy.zap.CommandLineBootstrap#getLogger()",
        "org.zaproxy.zap.model.ParameterParser#parseRawParameters(java.lang.String)"
    )

    richReport {
        destinationDir = file("$buildDir/reports/japicmp/")
        reportName = "japi.html"
        isAddDefaultRules = true
        addRule(JApiChangeStatus.MODIFIED, AcceptMethodAbstractNowDefaultRule::class.java)
    }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(japicmp)
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
    "org.zaproxy.zap.extension.api.RustAPIGenerator",
    "org.zaproxy.zap.extension.api.WikiAPIGenerator"
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

fun zapJar(version: String): File {
    val oldGroup = group
    try {
        // https://discuss.gradle.org/t/is-the-default-configuration-leaking-into-independent-configurations/2088/6
        group = "virtual_group_for_japicmp"
        val conf = configurations.detachedConfiguration(dependencies.create("$oldGroup:$name:$version"))
        conf.isTransitive = false
        return conf.singleFile
    } finally {
        group = oldGroup
    }
}
