# Building
ZAP is built with [Gradle], the following sections explain how to use it to build and run ZAP.
The Gradle tasks are expected to be executed with the provided [Gradle Wrapper].

## IDEs
The following page provides in depth guides on how to import, build, and run ZAP (core and add-ons) with commonly used IDEs:
https://www.zaproxy.org/docs/developer/

## Run ZAP
To run ZAP directly from the source run the task `:zap:run`. It will use any add-ons available in the [zap/src/main/dist/plugin/] directory.

**NOTE:** No add-on is included in the repository, they need to be built/copied separately into the `plugin` directory.

### Tests
To execute the tests run the task `:zap:test`.

## Distributions
The distributions bundle ZAP and its dependencies, all necessary to run ZAP standalone. By default the distributions of development
versions (SNAPSHOT) bundle the add-ons present in the dist `plugin` directory, main versions (non-SNAPSHOT) bundle a [predefined
list of add-ons] (downloaded automatically when the distribution is built).

The distributions are built into `zap/build/distributions/`.

### Daily
A zip package with a day stamped version, does not target any specific OS, it bundles all add-ons present in the `plugin` directory always.

To build it run the task `:zap:distDaily`.

(This distribution is built by default, it is a dependency of `assemble` task.)

### Weekly
A zip package with a day stamped version, does not target any specific OS, it bundles only [weekly add-ons] (built automatically from
source when the distribution is built).
This distribution is used for weekly releases.

To build it run the task `:zap:distWeekly`.

The build also provides the task `:zap:copyWeeklyAddOns` which builds and copies the weekly add-ons into the plugin directory,
using existing repositories in the file system at the same level as zaproxy.

### Cross Platform
A zip package, does not target any specific OS.

To build it run the task `:zap:distCrossplatform`.

### Core
Same as cross platform distribution but with just the essential add-ons, making the distribution smaller.

To build it run the task `:zap:distCore`.

### Linux
A tar.gz package, the macOS/Windows specific add-ons are excluded from this distribution.

To build it run the task `:zap:distLinux`.

#### Debian
A deb package, bundling ZAP and its dependencies. Does not bundle any add-ons, they are expected to be installed separately, for example, from marketplace.

To build it run the task `:zap:distDebian`.

### macOS
A dmg bundling ZAP, its dependencies, and a JRE. The Linux/Windows specific add-ons are excluded from this distribution.

To build the macOS distributions run the tasks `:zap:distMac` and `:zap:distMacArm64`, for the architectures `x64` and `aarch64` respectively.

**NOTE:** Needs to be executed on macOS, it requires `hdiutil`.

## Installers
The installers for Linux and Windows are built with [install4j]. The Windows executable is built with the [launch4j], invoked with Gradle plugin [gradle-launch4j].

To build the installers run the task `:zap:installers`.

Once the build is finished the installers will be located in the directory `zap/build/install4j/`.

**NOTE:** The following properties must be defined (e.g. in file `GRADLE_HOME/gradle.properties` ) to successfully and properly build the installers:
 - `install4jHomeDir` - install4j installation directory;
 - `install4jLicense` - install4j license key.

[Gradle]: https://gradle.org/
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[zap/src/main/dist/plugin/]: zap/src/main/dist/plugin/
[predefined list of add-ons]: zap/src/main/add-ons.txt
[weekly add-ons]: zap/src/main/weekly-add-ons.json
[install4j]: https://www.ej-technologies.com/products/install4j/overview.html
[launch4j]: http://launch4j.sourceforge.net/
[gradle-launch4j]: https://github.com/TheBoegl/gradle-launch4j
