import japicmp.model.JApiChangeStatus
import me.champeau.gradle.japicmp.JapicmpTask
import org.zaproxy.gradle.spotless.ValidateImports
import org.zaproxy.zap.japicmp.AcceptMethodAbstractNowDefaultRule
import org.zaproxy.zap.tasks.GradleBuildWithGitRepos
import org.zaproxy.zap.tasks.UpdateLegalNotice
import org.zaproxy.zap.tasks.internal.JapicmpExcludedData
import java.time.LocalDate
import java.util.stream.Collectors

plugins {
    `java-library`
    jacoco
    alias(libs.plugins.spotless)
    alias(libs.plugins.japicmp)
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.zaproxy.common)
    alias(libs.plugins.crowdin)
    org.zaproxy.zap.distributions
    org.zaproxy.zap.installers
    org.zaproxy.zap.`github-releases`
    org.zaproxy.zap.jflex
    org.zaproxy.zap.publish
    org.zaproxy.zap.spotless
    org.zaproxy.zap.test
}

group = "org.zaproxy"
val versionBC = project.property("zap.japicmp.baseversion") as String

// Classpath holding the java2ts annotation processor, used only by the generateZapApiTypes task.
val zapApiTypesProcessor: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

// Patched java2typescript build (../java2typescript, built with `mvn -pl core,processor -am install`).
// The patch loads classes without initializing them (avoids ExceptionInInitializerError on ZAP
// classes whose static initializers need a running ZAP). metainf-services/javax.json are optional
// deps the used code path never touches, so only these two jars are required.
val java2tsHome = rootDir.parentFile.resolve("java2typescript")
val java2tsJars =
    files(
        java2tsHome.resolve("core/target/java2ts-processor-core-2.0-20241009.jar"),
        java2tsHome.resolve("processor/target/java2ts-processor-2.0-20241009.jar"),
    )


val versionLangFile = "1"
val creationDate by extra { project.findProperty("creationDate") ?: LocalDate.now().toString() }
val distDir = file("src/main/dist/")

fun isZapRelease() = System.getenv("ZAP_RELEASE") != null

val zapJavaVersion by extra { if (isZapRelease()) JavaVersion.toVersion(System.getenv("ZAP_JAVA_VERSION")) else JavaVersion.VERSION_17 }

java {
    // Compile with appropriate Java version when building ZAP releases.
    if (isZapRelease()) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(zapJavaVersion.majorVersion))
        }
    } else {
        sourceCompatibility = zapJavaVersion
        targetCompatibility = zapJavaVersion
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

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}

spotless {
    java {
        bumpThisNumberIfACustomStepChanges(2)
        custom(
            "validateImports",
            ValidateImports(
                mapOf(
                    "import org.apache.commons.lang." to
                        "Import/use classes from Commons Lang 3, instead of Lang 2.",
                    "import org.apache.commons.codec." to
                        "Do not use import org.apache.commons.codec.",
                ),
            ),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = options.compilerArgs + "-parameters"
    val javaVersion = if (isZapRelease()) zapJavaVersion else JavaVersion.current()
    if (javaVersion >= JavaVersion.VERSION_21) {
        options.compilerArgs = options.compilerArgs + "-Xlint:-this-escape"
    }
}

dependencies {
    api(libs.rsyntaxtextarea)
    api(libs.javaSemver)
    implementation(libs.commons.beanutils)
    implementation(libs.commons.codec)
    api(libs.commons.collections)
    api(libs.commons.configuration)
    api(libs.commons.httpclient)
    api(libs.commons.io)
    api(libs.commons.lang)
    api(libs.commons.lang3)
    api(libs.commons.text)
    implementation(libs.harlib)
    api(libs.javahelp)
    api(libs.log4j.api)
    api(libs.log4j.api12)
    implementation(libs.log4j.core)
    implementation(libs.log4j.jul)
    api(libs.jerichoHtml)
    api(variantOf(libs.jsonlib) { classifier("jdk15") })
    api(libs.commons.csv)
    api(libs.hsqldb)
    api(libs.jfreechart)
    api(libs.jgraphtcore)
    api(libs.swingx)

    implementation(libs.flatlaf)
    implementation(libs.flatlaf.swingx)

    // The processor (and the @Java2TS/@Type annotations it defines) is used only by the dedicated
    // generateZapApiTypes task; the main compile has no dependency on java2typescript.
    zapApiTypesProcessor(java2tsJars)


    runtimeOnly(libs.commons.logging)
    runtimeOnly(libs.xom) {
        setTransitive(false)
    }

    // Include annotations used by Log4j2 Core library to avoid compiler warnings.
    compileOnly(libs.bndAnnotation)
    compileOnly(libs.findbugsAnnotations)
    testCompileOnly(libs.bndAnnotation)
    testCompileOnly(libs.findbugsAnnotations)

    testImplementation(libs.bytebuddy)
    testImplementation(libs.assertj.core)
    testImplementation(libs.hamcrest.core)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platformLauncher)
    testImplementation(libs.mockito)
    testImplementation(libs.log4j.slf4j)

    testRuntimeOnly(files(distDir))

    testGuiImplementation(libs.assertj.swing)
}

tasks.register<JavaExec>("run") {
    group = ApplicationPlugin.APPLICATION_GROUP
    description = "Runs ZAP from source, using the default dev home."

    mainClass.set("org.zaproxy.zap.ZAP")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = distDir
}

// ---------------------------------------------------------------------------------------------
// ZAP GraalJS API TypeScript declaration generation (bsorrentino/java2typescript)
// ---------------------------------------------------------------------------------------------
//
// The processor resolves the types to declare via Class.forName() at processing time, i.e. from its
// OWN annotation-processor classpath. Custom ZAP classes therefore have to be already compiled and
// present on that classpath. This is impossible inside the main compileJava task (it is what
// produces those classes), so generation runs as a separate compilation that depends on the main
// output and puts the full runtime classpath + compiled classes on the annotation-processor path.
//
// Which types get declared is decided by the processor's -Ats.scan option: it reads the compiled
// bytecode and keeps the public API surface (public types whose enclosing types are public too).
// The list of included/skipped classes lands in build/generated/zap-api-types/j2ts/zap-api-scan.txt.

val zapApiTypesOutputDir = layout.buildDirectory.dir("generated/zap-api-types")

tasks.register<JavaCompile>("generateZapApiTypes") {
    group = "build"
    description = "Generates TypeScript declarations for the ZAP GraalJS API."

    val main = sourceSets["main"]
    dependsOn(tasks.named("classes"))

    // Static trigger source: it carries the @Java2TS annotation javac needs to invoke the processor
    // at all, plus the handful of JDK types to declare. The ZAP types come from -Ats.scan below.
    source = fileTree("src/zapApiTypes/java")

    // Compiled ZAP classes + all runtime dependencies must be resolvable so that both javac (to
    // compile the @Type(...) references) and the processor's Class.forName() can find them.
    val resolveClasspath = files(main.output, main.runtimeClasspath, zapApiTypesProcessor)
    classpath = resolveClasspath
    options.annotationProcessorPath = resolveClasspath

    // Roots handed to -Ats.scan below. Also declared as an input: the compiler args are only built
    // in doFirst, so nothing else tells Gradle that the ZAP classes drive this task's output.
    val scanRoots = main.output.classesDirs
    inputs.files(scanRoots).withPropertyName("zapClasses")

    options.generatedSourceOutputDirectory.set(zapApiTypesOutputDir)
    // Nothing is actually compiled to .class with -proc:only, but JavaCompile still needs a target.
    destinationDirectory.set(layout.buildDirectory.dir("classes/zap-api-types"))

    // The org.zaproxy.common convention plugin sets ["-Xlint:all", "-Werror", "-parameters"] on
    // every JavaCompile via a withType(JavaCompile).configureEach { } action. Because that runs on
    // task realization, a config-time assignment here is unreliable. Set the args in doFirst so it
    // wins at execution time: annotation processing emits notes and warnings of its own (and the
    // declared JDK types include deprecated members), so -Werror + -Xlint:all would fail the build.
    doFirst {
        options.compilerArgs =
            listOf(
                "-proc:only",
                "-processor",
                "org.bsc.processor.TypescriptProcessor",
                "-Ats.outfile=zap-api",
                "-Ats.registry=zapApi",
                // Declare every public type found in the compiled ZAP classes. The processor loads
                // them from its own (annotation processor) classpath, which is why the same files
                // are both scanned here and set as annotationProcessorPath above.
                "-Ats.scan=" + scanRoots.joinToString(File.pathSeparator),
                // Suppress all lint (deprecation/removal/processing/...) and do not fail on it.
                "-Xlint:none",
                "-nowarn",
            )
    }
}

listOf("jar", "jarDaily", "jarWithBom").forEach {
    tasks.named<Jar>(it) {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirPermissions {
            unix("0755")
        }
        filePermissions {
            unix("0644")
        }

        val attrs =
            mapOf(
                "Main-Class" to "org.zaproxy.zap.ZAP",
                "Implementation-Version" to ToString({ archiveVersion.get() }),
                "Create-Date" to creationDate,
                "Class-Path" to
                    ToString({
                        configurations.runtimeClasspath.get().files.stream().map {
                                file ->
                            "lib/${file.name}"
                        }.sorted().collect(Collectors.joining(" "))
                    }),
            )

        manifest {
            attributes(attrs)
        }

        if (System.getenv("ZAP_CHALK") != null) {
            doLast {
                providers.exec {
                    workingDir(rootDir)
                    executable("chalk")
                    args("insert", archiveFile.get().asFile)
                }
            }
        }
    }
}

val japicmp by tasks.registering(JapicmpTask::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks ${project.name}.jar binary compatibility with latest version ($versionBC)."

    oldClasspath.from(zapJar(versionBC))
    newClasspath.from(tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME))
    ignoreMissingClasses.set(true)

    var excludedDataFile = "$projectDir/gradle/japicmp.yaml"
    inputs.file(excludedDataFile)

    var excludedData = JapicmpExcludedData.from(excludedDataFile)
    packageExcludes.set(excludedData.packageExcludes)
    fieldExcludes.set(excludedData.fieldExcludes)
    classExcludes.set(excludedData.classExcludes)
    methodExcludes.set(excludedData.methodExcludes)

    richReport {
        destinationDir.set(layout.buildDirectory.dir("reports/japicmp"))
        reportName.set("japi.html")
        addDefaultRules.set(true)
        addRule(JApiChangeStatus.MODIFIED, AcceptMethodAbstractNowDefaultRule::class.java)
    }
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(japicmp)
}

tasks.named<Javadoc>("javadoc") {
    title = "Zed Attack Proxy"
    source =
        sourceSets["main"].allJava.matching {
            include("ch/**")
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

    archiveFileName.set(
        layout.buildDirectory.file("langpack/ZAP_${project.version}_language_pack.$versionLangFile.zaplang").get().asFile.absolutePath,
    )
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    into("lang") {
        from(File(distDir, "lang"))
        from("src/main/resources/org/zaproxy/zap/resources") {
            include("Messages.properties")
        }
    }
}

tasks.register<Copy>("copyLangPack") {
    group = "ZAP Misc"
    description = (
        "Copies the language pack into the Core Language Files add-on " +
            "(assumes zap-extensions repo is in same directory as zaproxy)."
    )

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
    "org.zaproxy.zap.extension.api.DotNetAPIGenerator",
    "org.zaproxy.zap.extension.api.GoAPIGenerator",
    "org.zaproxy.zap.extension.api.JavaAPIGenerator",
    "org.zaproxy.zap.extension.api.NodeJSAPIGenerator",
    "org.zaproxy.zap.extension.api.PhpAPIGenerator",
    "org.zaproxy.zap.extension.api.PythonAPIGenerator",
    "org.zaproxy.zap.extension.api.RustAPIGenerator",
    "org.zaproxy.zap.extension.api.WikiAPIGenerator",
).forEach {
    val langName = it.removePrefix("org.zaproxy.zap.extension.api.").removeSuffix("APIGenerator")
    val task =
        tasks.register<JavaExec>("generate${langName}ApiEndpoints") {
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
    setJarTask(tasks.named<Jar>("jar").get())
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

tasks.register<UpdateLegalNotice>("updateLegalNotice") {
    group = "ZAP Misc"
    description = "Updates the third-party library table in LEGALNOTICE.md."

    configuration.set(configurations.runtimeClasspath)
    licensesFile.set(file("gradle/thirdPartyLicenses.yaml"))
    legalNoticeFile.set(rootProject.file("LEGALNOTICE.md"))
}
