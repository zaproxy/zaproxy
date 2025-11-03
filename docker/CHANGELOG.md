# Changelog
All notable changes to the docker containers will be documented in this file.

### 2025-11-03
- Set statsId and readonly for scan policies.

### 2025-10-31
- Added config options for recording which packaged scan is being run.

### 2025-03-25
- Update ZAP API scan to support host override for local OpenAPI spec.

### 2025-02-20
- Added jq to all images except bare.

### 2025-01-16
- Stop publishing images under the `softwaresecurityproject` Docker Hub org, superseded by `zaproxy`, which should be used instead.

### 2025-01-10
- Change stable image to use `debian:bookworm-slim` instead of `bullseye-slim`, it will now start using Java 17.
- Change bare image to use `eclipse-temurin:17-jre-alpine` instead of `11-jre-alpine`.

### 2024-12-10
- Updated to use Webswing 24.2.2.

### 2024-09-13
- Update the zap.sh script to use the cgroup memory limit when the IS_CONTAINERIZED environment variable is set to "true".

### 2024-08-30
- Updated the API-Minimal scan policy.

### 2024-08-28
- Change Weekly Docker image to use `debian:bookworm-slim` instead of `bullseye-slim`, it will now start using Java 17.

### 2024-07-16
- Fallback to usage of Graal.js script engine if Nashorn is not available, in the API packaged scan.

### 2024-07-15
- Change Nightly Docker image to use `debian:bookworm-slim` instead of `bullseye-slim`, it will now start using Java 17.

### 2024-06-19
- Alert_on_Unexpected_Content_Types.js > Now handles JSON, YAML, and XML related types more generically (Issue 8522).

### 2024-06-06
- Updated to use Webswing 24.

### 2024-04-30
- Push Docker images to https://hub.docker.com/u/zaproxy

### 2024-03-19
- Alert_on_Unexpected_Content_Types.js > Added Content-Type application/yaml to the list of expected types (Issue 8366).

### 2024-03-05
- ZAP images no longer pushed to the OWASP Docker Hub.

### 2024-01-23
- Allow host_override to be a URL (Issue 8312).

### 2023-12-14
- Updated to use Webswing 23.2.2 (Issue 8244).

### 2023-12-12
- Parsing the config file ignores empty lines that are containing whitespaces only (Issue 8237).

### 2023-12-07
- Give better error on failing to parse the config file.

### 2023-12-06
- Alert_on_Unexpected_Content_Types.js > Added Content-Type text/xml to the list of expected types (Issue 8226).

### 2023-10-30
- Add the ZAP client profile to stable, weekly, and live images.

### 2023-08-23
- Python 3.6 and 3.7 are no longer supported.

### 2023-08-09
- Install the newer Python ZAP API client directly, `python-owasp-zap-v2.4` was renamed to `zaproxy`.

### 2023-08-07
- The default name for ZAP's Root CA certificate and key was changed from `owasp_zap_root_ca` to `zap_root_ca`, in the Webswing script (`zap-webswing.sh`).

### 2023-08-04
- The packaged scans, when executed directly, will now use the image from the GitHub Container Registry.

### 2023-08-02
- Start publishing images under the `softwaresecurityproject` organization on Docker Hub, in addition to the existing images.

### 2023-07-07
- Remove checks for CFU initiator in HTTP Sender scripts, no longer needed.

### 2023-06-08
- Start publishing images to the GitHub Container Registry. Use tags instead of image names for various flavours of the
  images; some examples are:
  - `ghcr.io/zaproxy/zaproxy:20230608-stable` instead of `owasp/zap2docker-stable:s2023-06-08`
  - `ghcr.io/zaproxy/zaproxy:weekly` instead of `owasp/zap2docker-weekly:latest`
  - `ghcr.io/zaproxy/zaproxy:nightly` instead of `owasp/zap2docker-live:latest`

### 2023-05-05
 - Do not install/update add-ons if ZAP '-silent' option specified (Issue 4633).

### 2023-02-03
 - Alert_on_Unexpected_Content_Types.js > Added Content-Type application/hal+json to the list of expected types.

### 2023-01-27
 - Updated to use Webswing 22.2.4 (Issue 7704).

### 2023-01-10
- Rework Docker build files to not leave cached files and to not do unnecessary work.

### 2022-12-16
 - Changed the UID and GID of the `zap` user to 1000 (Issue 7655).

### 2022-12-05
 - Changed all images to use debian:bullseye-slim instead of unstable-slim.

### 2022-11-07
 - Updated packaged scans to use full path to `zap-x.sh`.

### 2022-11-04
  - Fixed `zap-x.sh` to return the exit code from `zap.sh` instead of `rm -f`

### 2022-10-27
 - Updated to use Webswing 22.2

### 2022-10-07
 - Changed stable image to use debian:unstable-slim.
 - Changed bare image to use eclipse-temurin:11-jre-alpine.
 - Removed zap-cli from stable.
 - Updated to use Webswing 22.1.5.
 - Alert_on_Unexpected_Content_Types.js > Added Content-Type application/x-ndjson to the list of expected types.

### 2022-09-28
 - Removed zap-cli from weekly/live.

### 2022-09-27
 - Fixed problem where python-owasp-zap-v2.4 was getting an older version.
 - Use curl for the weekly/live health checks.

### 2022-09-26
 - Changed weekly image to use debian:unstable-slim.

### 2022-09-20
 - Changed live image to use debian:unstable-slim.

### 2022-09-15
 - No longer include depreciated addOns job.

### 2022-08-10
 - The packaged scans will no longer warn if the default hooks file is not found.

### 2022-08-05
 - Alert_on_Unexpected_Content_Types.js > Added Content-Type text/plain to the list of expected types.
 
### 2022-07-30
 - Updated to use Webswing 22.1.3.

### 2022-06-06
 - Updated to use Webswing 22.1.2

### 2022-06-03
 - Add support for absolute file path in all scan options which require a file.

### 2022-04-11
 - Updated to use Webswing 21.2.8

### 2022-03-28
 - Added awscli to all of the docker images except 'bare'.

### 2021-12-27
 - Updated to use Webswing 21.2.5

### 2021-12-16
 - Updated to use Webswing 21.2.4

### 2021-11-03
 - Fixed issue with automation updates by install updates in a separate ZAP inline call.

### 2021-10-08
 - Changed the packaged scans to always update all add-ons on start up to avoid a bug in the automation framework breaking plans

### 2021-10-05
 - Fixed bug which caused the baseline scan to fail if a read-only mapped drive was used.

### 2021-09-21
 - Updated the Baseline scan to use the Automation Framework for the "-c config_file" and "-u config_url" options.

### 2021-09-16
 - Updated to use Webswing 21.1.5

### 2021-09-15
 - Added /zap/container file to make it easier to detect if we are running in a container like docker.

### 2021-08-11
 - Changed to enable integration tests, inc enabling the AF for the baseline `-c` option if the `--auto` flag is used before it.
 - Added Automation Framework support for * OUTOFSCOPE baseline config file option.

### 2021-07-08
 - Changed to use user's home directory for the Automation Framework files so it will work for any user

### 2021-07-05
 - Updated the baseline to use the Automation Framework by default for common options

### 2021-06-11
 - Updated the baseline to optionally use the Automation Framework for common options

### 2021-05-05
 - Updated to use Webswing 21.1

### 2021-04-30
 - Alert_on_Unexpected_Content_Types.js > Added Content-Type text/yaml to the list of expected types.

### 2021-02-10
 - Check if messages being analyzed by API scan scripts are globally excluded or not.

### 2021-02-01
 - Allow more flexibility to specify ZAP command line options when using Webswing:
  - The default options stay as `-host 0.0.0.0 -port 8090` unless
  - You specify an env var `ZAP_WEBSWING_OPTS` in which case that replaces the defaults
  - If not then if a `/zap/wrk/owasp_zap_root_ca.key` file exists then this is loaded as the ZAP root cert
  - If not then if the `/zap/wrk` is writable then ZAP will output the public and private ZAP cert into that directory
  
### 2021-01-19
 - Python 3.5 is no longer supported.

### 2020-12-23
 - Update Webswing to download prod version if valid key supplied.

### 2020-12-16
 - Update Webswing to latest version (20.2.1) to work with newer Java versions.
 - Update Java in stable image to version 11.

### 2020-12-11
 - Add `target` parameter to `ajaxSpider.scan_as_user` call. Without it ajaxSpider crawls first included in a context URL and not a target which is set.

### 2020-12-02
 - Use `ARG` command (for `DEBIAN_FRONTEND`) instead of `ENV` so that the parameter does not persist after the build process has been completed.

### 2020-11-27
 - Move logging level of Params from `info` to `debug`, as it can contain sensitive data when authenticated scans are run.
 
### 2020-11-24
 - Add support for authenticated scans.

### 2020-11-19
 - Add zap_tune function (disable all tags and limit pscan alerts to 10), zap_tuned hook and disable recovery log.

### 2020-11-16
 - Update zap-api-scan.py to add support for GraphQL.

### 2020-10-13
 - Alert_on_Unexpected_Content_Types.js > Added Content-Type application/health+json to the list of expected types.
 
### 2020-09-18
 - Fail immediately if the spider scans were not started to provide better error message.

###  2020-08-28
 - Packaged scans will use the provided context when spidering and active scanning.

###  2020-08-27
 - Updated to use webswing 2.5.12

###  2020-08-03
 - Add `IS_CONTAINERIZED` environment variable to the container image, used in the python script to check for containerized environments (e.g. containerd) without relying on container runtime specific files.

###  2020-07-17
 - Make podman compatible

###  2020-05-20
 - Make docker stable use ubuntu 20.04

###  2020-05-13
 - Make `python` command use Python 3.

### 2020-05-12
 - Removed python 2, only python 3 will be supported going forward.

### 2020-04-27
- Add `application/vnd.api+json` to the list of expected API content types.

### 2020-04-08
- Changed zap-full-scan.py and zap-api-scan.py to include the -I option to ignore only warning used by zap-baseline-scan.py

### 2020-04-06
- Make API scan policy available to the root user, otherwise it would fail to start the active scan.

### 2020-04-01
- Changed live and weekly images to use Java 11.

### 2020-02-21
 - Changed zap-full-scan.py, zap-api-scan.py, and zap-baseline-scan.py to include the missing check for markdown file.

### 2020-02-07
 - Change zap-full-scan.py and zap-api-scan.py to be Python3 compatible

### 2020-01-22
 - Change `live`, `stable`, and `weekly` images to set the locale and lang to `C.UTF-8`,
 to improve interoperability with Python 3 (e.g. `zap-cli`).

### 2019-10-16
 - Added response code after each URL reported on standard out:

```
WARN-NEW: Web Browser XSS Protection Not Enabled [10016] x 4 
	https://www.example.com/ (200 OK)
	https://www.example.com/robots.txt (404 Not Found)
	https://www.example.com (200 OK)
	https://www.example.com/sitemap.xml (404 Not Found)
```

### 2019-10-01
 - Added Python3 and the pip3 version of ZAP in preparation for Python 2 EOL: https://www.python.org/dev/peps/pep-0373/

### 2019-09-05
 - Changed zap-full-scan.py to ignore example ascan rules

### 2019-06-18
 - Changed zap-full-scan.py to always include the active scan beta rules
 - Changed zap-full-scan.py to include the active scan alpha rules when the -a switch is used
 - Fixed ownership of all files in the /zap directory
 - Added this changelog
