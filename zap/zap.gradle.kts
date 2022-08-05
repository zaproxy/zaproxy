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
    id("org.zaproxy.crowdin") version "0.2.1"
    org.zaproxy.zap.distributions
    org.zaproxy.zap.installers
    org.zaproxy.zap.`github-releases`
    org.zaproxy.zap.jflex
    org.zaproxy.zap.publish
    org.zaproxy.zap.spotless
    org.zaproxy.zap.test
}

group = "org.zaproxy"
version = "2.12.0-SNAPSHOT"
val versionBC = "2.11.0"

val versionLangFile = "1"
val creationDate by extra { project.findProperty("creationDate") ?: LocalDate.now().toString() }
val distDir = file("src/main/dist/")

java {
    // Compile ZAP with Java 8 when building releases.
    if (System.getenv("GITHUB_REF")?.contains("refs/tags/") ?: false) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    } else {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

crowdin {
    credentials {
        token.set(System.getenv("CROWDIN_AUTH_TOKEN"))
    }

    configuration {
        file.set(file("gradle/crowdin.yml"))
    }
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}

dependencies {
    api("com.fifesoft:rsyntaxtextarea:3.2.0")
    api("com.github.zafarkhaja:java-semver:0.9.0")
    api("commons-beanutils:commons-beanutils:1.9.4")
    api("commons-codec:commons-codec:1.15")
    api("commons-collections:commons-collections:3.2.2")
    api("commons-configuration:commons-configuration:1.10")
    api("commons-httpclient:commons-httpclient:3.1")
    api("commons-io:commons-io:2.11.0")
    api("commons-lang:commons-lang:2.6")
    api("org.apache.commons:commons-lang3:3.12.0")
    api("org.apache.commons:commons-text:1.9")
    api("edu.umass.cs.benchlab:harlib:1.1.3")
    api("javax.help:javahelp:2.0.05")
    val log4jVersion = "2.17.2"
    api("org.apache.logging.log4j:log4j-api:$log4jVersion")
    api("org.apache.logging.log4j:log4j-1.2-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("net.htmlparser.jericho:jericho-html:3.4")
    api("net.sf.json-lib:json-lib:2.4:jdk15")
    api("org.apache.commons:commons-csv:1.9.0")
    // Bouncy Castle is no longer in actual use by core, to be removed in a following version.
    val bcVersion = "1.69"
    implementation("org.bouncycastle:bcmail-jdk15on:$bcVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:$bcVersion")
    implementation("org.bouncycastle:bcpkix-jdk15on:$bcVersion")
    api("org.hsqldb:hsqldb:2.5.2")
    api("org.jfree:jfreechart:1.5.3")
    api("org.jgrapht:jgrapht-core:0.9.0")
    api("org.swinglabs.swingx:swingx-all:1.6.5-1")
    api("org.xerial:sqlite-jdbc:3.36.0.3")

    implementation("commons-validator:commons-validator:1.7")
    // Don't need its dependencies, for now.
    implementation("org.jitsi:ice4j:3.0-24-g34c2ce5") {
        setTransitive(false)
    }
    implementation("com.formdev:flatlaf:2.4")

    runtimeOnly("commons-jxpath:commons-jxpath:1.3")
    runtimeOnly("commons-logging:commons-logging:1.2")
    runtimeOnly("xom:xom:1.3.7") {
        setTransitive(false)
    }

    testImplementation("com.github.tomakehurst:wiremock-jre8:2.32.0") {
        // Not needed.
        exclude(group = "org.junit")
    }
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    val jupiterVersion = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:4.4.0")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    testImplementation("org.nanohttpd:nanohttpd-webserver:2.3.1")

    testRuntimeOnly(files(distDir))

    testGuiImplementation("org.assertj:assertj-swing:3.17.1")
}

tasks.register<JavaExec>("run") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs ZAP from source, using the default dev home."

    mainClass.set("org.zaproxy.zap.ZAP")
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

    packageExcludes = listOf()

    fieldExcludes = listOf(
        "org.parosproxy.paros.Constant#FILE_CONFIG_DEFAULT",
        "org.parosproxy.paros.Constant#VULNS_BASE",
        "org.parosproxy.paros.core.scanner.Alert#MSG_RELIABILITY",
        "org.parosproxy.paros.core.scanner.Alert#SUSPICIOUS",
        "org.parosproxy.paros.core.scanner.Alert#WARNING",
        "org.parosproxy.paros.model.HistoryReference#TYPE_RESERVED_11",
        "org.parosproxy.paros.view.View#DISPLAY_OPTION_BOTTOM_FULL",
        "org.parosproxy.paros.view.View#DISPLAY_OPTION_LEFT_FULL",
        "org.parosproxy.paros.view.View#DISPLAY_OPTION_TOP_FULL",
        "org.zaproxy.zap.extension.ascan.ActiveScanPanel#PANEL_NAME",
        "org.zaproxy.zap.extension.search.SearchPanel#PANEL_NAME"
    )

    classExcludes = listOf(
        "org.parosproxy.paros.common.FileXML",
        "org.parosproxy.paros.core.proxy.SenderThread",
        "org.parosproxy.paros.core.proxy.SenderThreadListener",
        "org.parosproxy.paros.core.proxy.StreamForwarder",
        "org.parosproxy.paros.core.scanner.AbstractDefaultFilePlugin",
        "org.parosproxy.paros.extension.history.BrowserDialog",
        "org.parosproxy.paros.extension.history.PopupMenuResend",
        "org.parosproxy.paros.extension.history.PopupMenuResendSites",
        "org.parosproxy.paros.extension.manualrequest.http.impl.ManualHttpRequestEditorDialog",
        "org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog",
        "org.parosproxy.paros.model.HistoryList",
        "org.parosproxy.paros.model.HttpMessageList",
        "org.parosproxy.paros.network.ByteVector",
        "org.parosproxy.paros.network.ProxyExcludedDomainMatcher",
        "org.zaproxy.zap.extension.brk.BreakpointMessageHandler",
        "org.zaproxy.zap.extension.brk.BreakPanel",
        "org.zaproxy.zap.extension.brk.ExtensionBreak\$DialogType",
        "org.zaproxy.zap.extension.history.PopupMenuShowInHistory",
        "org.zaproxy.zap.extension.httppanel.HttpPanel",
        "org.zaproxy.zap.extension.httppanel.HttpPanelRequest",
        "org.zaproxy.zap.extension.httppanel.HttpPanelResponse",
        "org.zaproxy.zap.extension.pscan.PassiveScanThread",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderContext",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderContextAsUser",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderDialog",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderScope",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderSite",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderSubtree",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderURL",
        "org.zaproxy.zap.extension.stdmenus.PopupMenuSpiderURLAsUser",
        "org.zaproxy.zap.httputils.RequestUtils",
        "org.zaproxy.zap.view.HistoryReferenceTableModel",
        "org.zaproxy.zap.view.messagelocation.SelectMessageLocationsPanel",
        "org.zaproxy.zap.view.MessagePanelsPositionController",
        "org.zaproxy.zap.view.PopupMenuHistoryReference",
        "org.zaproxy.zap.view.PopupMenuHttpMessage",
        "org.zaproxy.zap.view.PopupMenuSiteNode"
    )

    methodExcludes = listOf(
        "org.parosproxy.paros.CommandLine#getConfigs()",
        "org.parosproxy.paros.control.Control#createAndOpenUntitledDb()",
        "org.parosproxy.paros.core.proxy.ProxyParam#isModifyAcceptEncodingHeader()",
        "org.parosproxy.paros.core.proxy.ProxyParam#setModifyAcceptEncodingHeader(boolean)",
        "org.parosproxy.paros.core.scanner.Alert#getAlert()",
        "org.parosproxy.paros.core.scanner.Alert#getReliability()",
        "org.parosproxy.paros.core.scanner.Alert#setAlert(java.lang.String)",
        "org.parosproxy.paros.core.scanner.Alert#setDetail(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.parosproxy.paros.network.HttpMessage)",
        "org.parosproxy.paros.core.scanner.Alert#setRiskReliability(int,int)",
        "org.parosproxy.paros.core.scanner.HostProcess#setPluginRequestCount(int,int)",
        "org.parosproxy.paros.core.scanner.HostProcess#setTestCurrentCount(org.parosproxy.paros.core.scanner.Plugin,int)",
        "org.parosproxy.paros.core.scanner.Plugin#getDisplayName()",
        "org.parosproxy.paros.core.scanner.PluginFactory#loadedPlugin(java.lang.String)",
        "org.parosproxy.paros.core.scanner.PluginFactory#unloadedPlugin(java.lang.String)",
        "org.parosproxy.paros.core.scanner.VariantAbstractQuery#setParams(int,java.util.Map)",
        "org.parosproxy.paros.db.paros.ParosTableHistory#getHistoryList(long)",
        "org.parosproxy.paros.db.paros.ParosTableHistory#getHistoryList(long,int)",
        "org.parosproxy.paros.db.paros.ParosTableHistory#setHistoryTypeAsTemporary(int)",
        "org.parosproxy.paros.db.paros.ParosTableHistory#unsetHistoryTypeAsTemporary(int)",
        "org.parosproxy.paros.db.RecordAlert#getReliability()",
        "org.parosproxy.paros.db.RecordAlert#setReliability(int)",
        "org.parosproxy.paros.extension.CommandLineListener#preExecute(org.parosproxy.paros.extension.CommandLineArgument[])",
        "org.parosproxy.paros.extension.ExtensionPopupMenuItem#isSuperMenu()",
        "org.parosproxy.paros.extension.history.ExtensionHistory#clearLogPanelDisplayQueue()",
        "org.parosproxy.paros.extension.history.LogPanel#clearDisplayQueue()",
        "org.parosproxy.paros.extension.history.LogPanel#LogPanel()",
        "org.parosproxy.paros.extension.history.LogPanel#setDisplayPanel(org.zaproxy.zap.extension.httppanel.HttpPanel,org.zaproxy.zap.extension.httppanel.HttpPanel)",
        "org.parosproxy.paros.extension.option.OptionsParamView#getShowMainToolbar()",
        "org.parosproxy.paros.extension.option.OptionsParamView#setShowMainToolbar(int)",
        "org.parosproxy.paros.model.Session#addGlobalExcludeURLRegexs(java.lang.String)",
        "org.parosproxy.paros.model.Session#setGlobalExcludeURLRegexs(java.util.List)",
        "org.parosproxy.paros.network.ConnectionParam#getProxyChainSkipName()",
        "org.parosproxy.paros.network.ConnectionParam#setProxyChainSkipName(java.lang.String)",
        "org.parosproxy.paros.view.AbstractFrame#loadIconImages()",
        "org.parosproxy.paros.view.MainFrame#changeDisplayOption(int)",
        "org.parosproxy.paros.view.MainFrame#MainFrame(int)",
        "org.parosproxy.paros.view.View#getDisplayOption()",
        "org.parosproxy.paros.view.View#getMessagePanelsPositionController()",
        "org.parosproxy.paros.view.View#setDisplayOption(int)",
        "org.parosproxy.paros.view.WorkbenchPanel#changeDisplayOption(int)",
        "org.parosproxy.paros.view.WorkbenchPanel#getTabbedOldSelect()",
        "org.parosproxy.paros.view.WorkbenchPanel#getTabbedOldStatus()",
        "org.parosproxy.paros.view.WorkbenchPanel#getTabbedOldWork()",
        "org.parosproxy.paros.view.WorkbenchPanel#removeSplitPaneWork()",
        "org.parosproxy.paros.view.WorkbenchPanel#setTabbedOldSelect(org.zaproxy.zap.view.TabbedPanel2)",
        "org.parosproxy.paros.view.WorkbenchPanel#setTabbedOldStatus(org.zaproxy.zap.view.TabbedPanel2)",
        "org.parosproxy.paros.view.WorkbenchPanel#setTabbedOldWork(org.zaproxy.zap.view.TabbedPanel2)",
        "org.parosproxy.paros.view.WorkbenchPanel#splitPaneWorkWithTabbedPanel(org.parosproxy.paros.view.TabbedPanel,int)",
        "org.parosproxy.paros.view.WorkbenchPanel#WorkbenchPanel(int)",
        "org.zaproxy.zap.control.AddOn#AddOn(java.io.File)",
        "org.zaproxy.zap.control.AddOn#AddOn(java.lang.String)",
        "org.zaproxy.zap.control.AddOn#canLoad()",
        "org.zaproxy.zap.control.AddOn#isAddOn(java.io.File)",
        "org.zaproxy.zap.control.AddOn#isAddOn(java.lang.String)",
        "org.zaproxy.zap.control.ControlOverrides#getConfigs()",
        "org.zaproxy.zap.control.ControlOverrides#setConfigs(java.util.Hashtable)",
        "org.zaproxy.zap.db.sql.SqlTableHistory#setHistoryTypeAsTemporary(int)",
        "org.zaproxy.zap.db.sql.SqlTableHistory#unsetHistoryTypeAsTemporary(int)",
        "org.zaproxy.zap.extension.api.DotNetAPIGenerator#generateCSharpFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.GoAPIGenerator#generateGoFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.JavaAPIGenerator#generateJavaFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.NodeJSAPIGenerator#generateNodeJSFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.PhpAPIGenerator#generatePhpFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.PythonAPIGenerator#generatePythonFiles(java.util.List)",
        "org.zaproxy.zap.extension.api.WikiAPIGenerator#generateWikiFiles(java.util.List)",
        "org.zaproxy.zap.extension.ascan.ActiveScan#updatePluginRequestCounts()",
        "org.zaproxy.zap.extension.autoupdate.AddOnsTableModel#AddOnsTableModel(java.util.Comparator,org.zaproxy.zap.control.AddOnCollection,int)",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#canAddBreakpoint()",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#canEditBreakpoint()",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#canRemoveBreakpoint()",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#dialogClosed()",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#dialogShown(org.zaproxy.zap.extension.brk.ExtensionBreak\$DialogType)",
        "org.zaproxy.zap.extension.brk.ExtensionBreak#getBreakPanel()",
        "org.zaproxy.zap.extension.ExtensionPopupMenu#prepareShow()",
        "org.zaproxy.zap.extension.history.PopupMenuPurgeSites#purge(org.parosproxy.paros.model.SiteMap,org.parosproxy.paros.model.SiteNode)",
        "org.zaproxy.zap.extension.params.ParamScanner#setParent(org.zaproxy.zap.extension.pscan.PassiveScanThread)",
        "org.zaproxy.zap.extension.pscan.PassiveScanner#getTaskHelper()",
        "org.zaproxy.zap.extension.pscan.PassiveScanner#setTaskHelper(org.zaproxy.zap.extension.pscan.PassiveScanTaskHelper)",
        "org.zaproxy.zap.extension.pscan.ExtensionPassiveScan#addPassiveScanner(java.lang.String)",
        "org.zaproxy.zap.extension.pscan.PassiveScanThread#PassiveScanThread( org.zaproxy.zap.extension.pscan.PassiveScannerList, org.parosproxy.paros.extension.history.ExtensionHistory, org.zaproxy.zap.extension.alert.ExtensionAlert)",
        "org.zaproxy.zap.extension.search.SearchPanel#SearchPanel()",
        "org.zaproxy.zap.extension.search.SearchPanel#setDisplayPanel(org.zaproxy.zap.extension.httppanel.HttpPanelRequest,org.zaproxy.zap.extension.httppanel.HttpPanelResponse)",
        "org.zaproxy.zap.extension.spider.SpiderScan#SpiderScan( org.zaproxy.zap.extension.spider.ExtensionSpider, org.zaproxy.zap.spider.SpiderParam, org.zaproxy.zap.model.Target, org.apache.commons.httpclient.URI, org.zaproxy.zap.users.User, int)",
        "org.zaproxy.zap.extension.spider.SpiderThread#SpiderThread( org.zaproxy.zap.extension.spider.ExtensionSpider, org.zaproxy.zap.spider.SpiderParam, java.lang.String, org.zaproxy.zap.model.ScanListenner)",
        "org.zaproxy.zap.spider.Spider#Spider(org.zaproxy.zap.extension.spider.ExtensionSpider,org.zaproxy.zap.spider.SpiderParam,org.parosproxy.paros.network.ConnectionParam,org.parosproxy.paros.model.Model,org.zaproxy.zap.model.Context)",
        "org.zaproxy.zap.spider.SpiderParam#getScope()",
        "org.zaproxy.zap.spider.SpiderParam#getScopeText()",
        "org.zaproxy.zap.spider.SpiderParam#setScopeString(java.lang.String)",
        "org.zaproxy.zap.view.ContextExcludePanel#getPanelName(org.zaproxy.zap.model.Context)",
        "org.zaproxy.zap.view.ContextIncludePanel#getPanelName(org.zaproxy.zap.model.Context)",
        "org.zaproxy.zap.view.MainToolbarPanel#setDisplayOption(int)",
        "org.zaproxy.zap.view.ScanPanel2#ScanPanel2(java.lang.String, javax.swing.ImageIcon, org.zaproxy.zap.model.ScanController, org.parosproxy.paros.common.AbstractParam)",
        "org.zaproxy.zap.view.TabbedPanel2#clone(org.zaproxy.zap.view.TabbedPanel2)"
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

        mainClass.set(it)
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
