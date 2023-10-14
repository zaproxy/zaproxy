# Releasing

## Main Release

### Pre release

These tasks use checkboxes so that they can be copied into an issue.

- [ ] Update dependencies - these can be checked using the [zaproxy](https://github.com/zaproxy/zaproxy) `dependencyUpdates` gradle task
- [ ] Update the [gettingStarted](https://github.com/zaproxy/zap-extensions/tree/main/addOns/gettingStarted) ODT document and regenerate the PDF.
- [ ] Update the [MacOS JRE](https://github.com/zaproxy/zaproxy/blob/main/buildSrc/src/main/kotlin/org/zaproxy/zap/distributions.gradle.kts).
- [ ] Update the [MacOS Copyright statement](https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/macOS/ZAP.app/Contents/Info.plist).
- [ ] Update [Constant](https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/java/org/parosproxy/paros/Constant.java)#VERSION_TAG.
- [ ] Add and use a [Constant](https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/java/org/parosproxy/paros/Constant.java).upgradeFrom`<version>`() method.
- [ ] Update [common-user-agents.txt](https://github.com/zaproxy/zap-extensions/blob/main/addOns/network/src/main/resources/org/zaproxy/addon/network/internal/client/common-user-agents.txt) and [DEFAULT_DEFAULT_USER_AGENT](https://github.com/zaproxy/zap-extensions/blob/main/addOns/network/src/main/java/org/zaproxy/addon/network/ConnectionOptions.java).
- [ ] Publish a SNAPSHOT of core and update the main add-ons to use it.
- [ ] Create help release page
  - Development / bug fix issue links can be generated using the [zap-admin](https://github.com/zaproxy/zap-admin) `generateReleaseNotes` task.
  - Library changes can be determined by diffing [LEGALNOTICE.md](https://github.com/zaproxy/zaproxy/blob/main/LEGALNOTICE.md) with the version at the previous release.
- [ ] Create the [zap-admin](https://github.com/zaproxy/zap-admin) version and news files
- [ ] Prepare blog post

### Release Process

- [ ] Run the workflow [Prepare Release Main Version](https://github.com/zaproxy/zaproxy/actions/workflows/prepare-release-main-version.yml),     to prepare the release. It creates a pull request updating the version;
- [ ] Finish the following tasks in the pull request:
  - [ ] Update latest ZapVersions file in [build.gradle.kts](https://github.com/zaproxy/zap-admin/blob/master/build.gradle.kts)
  - [ ] Release add-ons.
  - [ ] Update main add-ons declared in [main-add-ons.yml](https://github.com/zaproxy/zaproxy/blob/main/zap/src/main/main-add-ons.yml):
     - [ ] Add new add-ons.
     - [ ] Remove add-ons no longer needed.
     - [ ] Update add-ons with the task mentioned in `main-add-ons.yml`.
- [ ] Merge the pull request, to create the tag and the draft release (done by [Release Main Version](https://github.com/zaproxy/zaproxy/actions/workflows/release-main-version.yml));
- [ ] Verify the draft release.
- [ ] Publish the release.
- [ ] Regenerate and publish the Weekly and Live releases.
- [ ] Update the [Linux Repos](https://software.opensuse.org/download.html?project=home%3Acabelo&package=owasp-zap)
- [ ] Update the stats scripts [github.py](https://github.com/zapbot/zap-mgmt-scripts/blob/master/stats/github.py) and [zap_services.py](https://github.com/zapbot/zap-mgmt-scripts/blob/master/stats/zap_services.py)

Once published the [Handle Release](https://github.com/zaproxy/zaproxy/actions/workflows/handle-release.yml) workflow
will trigger the update of the marketplace with the new release, it will also create a pull request preparing the next
development iteration.

### Localized Resources

The resources that require localization (e.g. `Messages.properties`, `vulnerabilities.xml`) are uploaded to the ZAP projects in
[Crowdin](https://crowdin.com/) when the main release is released, if required (for pre-translation) the resources can be uploaded manually
at any time by running the workflow [Crowdin Upload Files](https://github.com/zaproxy/zaproxy/actions/workflows/crowdin-upload-files.yml).

The resulting localized resources are added/updated in the repository periodically (through a workflow in the
[zap-admin repository](https://github.com/zaproxy/zap-admin/)).

### Post Release

- [ ] Publish blog post
- [ ] Update latest News file to point to blog / release notes?
- [ ] Announce on
  - [ ] ZAP User and Dev groups
  - [ ] @zaproxy twitter account
  - [ ] OWASP Slack
- [ ] Update and release client APIs
  - [ ] [Java](https://github.com/zaproxy/zap-api-java/blob/main/RELEASING.md)
  - [ ] [Python](https://github.com/zaproxy/zap-api-python/blob/master/RELEASING.md)
- [ ] Update major projects using ZAP
  - [ ] Kali - [new issue](https://bugs.kali.org/)
  - [ ] [Flathub](https://github.com/flathub/org.zaproxy.ZAP)
  - [ ] [Snap](https://github.com/zaproxy/zaproxy/tree/main/snap)
    - [ ] Run the workflow [Release Snap](https://github.com/zaproxy/zaproxy/actions/workflows/release-snap.yml).
- [ ] Update 3rd Party Package Managers 
  - [ ] Homebrew - [zap.rb](https://github.com/Homebrew/homebrew-cask/blob/master/Casks/z/zap.rb)
  - [ ] Scoop - [zaproxy.json](https://github.com/ScoopInstaller/Extras/blob/master/bucket/zaproxy.json)
  - [ ] Chocolatey - [zap](https://github.com/jtcmedia/chocolatey-packages/tree/master/zap)
  - [ ] winget-pkgs - [ZAP](https://github.com/microsoft/winget-pkgs/tree/master/manifests/z/ZAP/ZAP/)
- [ ] Update [bugcrowd](https://bugcrowd.com/owaspzap) scope

## Weekly Release

The following steps should be followed to release the weekly:
 1. Run the workflow [Release Weekly](https://github.com/zaproxy/zaproxy/actions/workflows/release-weekly.yml),
    to create the tag and the draft release;
 2. Verify the draft release;
 3. Publish the release.

Once published the [Handle Release](https://github.com/zaproxy/zaproxy/actions/workflows/handle-release.yml) workflow
will trigger the update of the marketplace with the new release.

## Docker Images

The Nightly image is automatically built from the default branch.

The images Weekly, Stable, and Bare are automatically built after the corresponding release to the marketplace.  
The images Stable and Bare are built at the same time.

They can still be manually built by running the corresponding workflow:
 - [Release Weekly Docker](https://github.com/zaproxy/zaproxy/actions/workflows/release-weekly-docker.yml)
 - [Release Main (and Bare) Docker](https://github.com/zaproxy/zaproxy/actions/workflows/release-main-docker.yml)

