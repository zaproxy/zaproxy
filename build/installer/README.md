Building
========

The installers for Linux, macOS, and Windows are built using install4j's Gradle plugin (but Ant is required to build ZAP's distribution files included in the installers). The Windows executable bundled in the Windows installers is built with Gradle plugin [gradle-launch4j](https://github.com/TheBoegl/gradle-launch4j).

Following the steps to build the installers:
1. Run Ant target `full-release`, to build ZAP's distribution files;
2. Run Gradle task `buildInstallers`, to build the installers.

Once the build is finished the installers will be located in the directory `build/install4j`.

NOTE: The following properties must be defined (e.g. in file `GRADLE_HOME/gradle.properties` ) to successfully and properly build the installers:
 - `install4jHomeDir` - install4j installation directory;
 - `install4jLicense` - install4j license key.
