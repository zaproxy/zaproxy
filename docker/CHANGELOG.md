# Changelog
All notable changes to the docker containers will be documented in this file.

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
