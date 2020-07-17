# Changelog
All notable changes to the docker containers will be documented in this file.

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
