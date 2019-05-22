# Building
ZAP is built with [Gradle], the following sections explain how to use it to build and run ZAP.
The Gradle tasks are expected to be executed with the provided [Gradle Wrapper].

## IDEs
The following wiki page provides an in depth guide on how to import, build, and run ZAP (core and add-ons) with commonly used IDEs:
https://github.com/zaproxy/zaproxy/wiki/Building

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
This distribution is used for weekly releases.

To build it run the task `:zap:distDaily`.

(This distribution is built by default, it is a dependency of `assemble` task.)

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
A dmg bundling ZAP, its dependencies, and JRE. The Linux/Windows specific add-ons are excluded from this distribution.

To build it run the task `:zap:distMac`.

**NOTE:** Needs to be executed on macOS, it requires `hdiutils`.

[Gradle]: https://gradle.org/
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html
[zap/src/main/dist/plugin/]: zap/src/main/dist/plugin/
[predefined list of add-ons]: zap/src/main/dist/add-ons.txt
