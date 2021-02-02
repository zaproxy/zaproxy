// Build tweaks when running in GitHub CI

fun isEnvVarTrue(envvar: String) = System.getenv(envvar) == "true"

if (isEnvVarTrue("CI") && System.getenv("GITHUB_WORKFLOW") == "Java CI") {

    allprojects {
        tasks.withType(Test::class).configureEach {
            testLogging {
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }

}