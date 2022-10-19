ZAP Scan Rules
==============
The registry of scan rules' (passive, active, custom...) IDs.

Format: `<ID> <NAME>`

If the scan rule is no longer in use: `<ID> <NAME> [Deprecated]`

Scan rules:
```
0       Directory browsing
1       Potential File Path Manipulation
2       Private IP disclosure
3       Session ID in URL rewrite
4       Obsolete file [Deprecated]
5       Obsolete file extended check [Deprecated]
6       Directory/Path traversal
7       Remote File Inclusion

41      Source Code Disclosure - Git
42      Source Code Disclosure - SVN
43      Source Code Disclosure - File Inclusion

10000   Password Autocomplete in browser [Deprecated]
10001   Secure page browser cache [Deprecated]
10003   Retire JS

10009   In Page Banner Info Leak
10010   Cookie set without HttpOnly flag
10011   Cookie set without secure flag
10012   Password Autocomplete in browser [Deprecated]
10013   Weak HTTP authentication over an unsecured connection [Deprecated]
10014   Cross Site Request Forgery [Deprecated]
10015   Incomplete or no cache-control and pragma HTTPHeader set
10016   Web Browser XSS Protection Not Enabled [Deprecated]
10017   Cross-domain JavaScript source file inclusion
10018   Untrusted domains in JavaScript source code
10019   Content-Type header missing
10020   Anti-clickjacking Header
10021   X-Content-Type-Options header missing
10022   Information disclosure - database error messages [Deprecated]
10023   Information disclosure - debug error messages
10024   Information disclosure - sensitive information in URL
10025   Information disclosure - sensitive information on HTTP Referrer header
10026   HTTP Parameter Override
10027   Information disclosure - suspicious comments
10028   Open redirect
10029   Cookie poisoning
10030   User controllable charset
10031   User controllable HTML element attribute (potential XSS)
10032   Viewstate scanner
10033   Directory Browsing
10034   Heartbleed OpenSSL Vulnerability
10035   Strict-Transport-Security Header Not Set
10036   Server Leaks Version Information via "Server" HTTP Response Header Field
10037   Server Leaks Information via "X-Powered-By" HTTP Response Header Field(s)
10038   Content Security Policy (CSP) Header Not Set
10039   X-Backend-Server Header Information Leak
10040   Secure pages including mixed content
10041   HTTP to HTTPS insecure transition in form post
10042   HTTPS to HTTP insecure transition in form post
10043   User controllable javascript event (XSS)
10044   Big Redirect Response
10045   Source Code Disclosure - WEB-INF folder
10046   Insecure Component [Deprecated]
10047   HTTPS Content Available via HTTP
10048   ShellShock
10049   Cacheability and Retrievability Content
10050   Retrieved from Cache
10051   Relative Path Confusion
10052   X-ChromeLogger-Data Header Information Leak
10053   Apache Range Header DoS (CVE-2011-3192) [Deprecated]
10054   Cookie set without SameSite attribute
10055   Content Security Policy Scanner
10056   X-Debug-Token Scanner
10057   Username Hash iDOR Scanner
10058   GET for POST Scanner
10059	Http Parameter Pollution (Client Side)
10060	Http Parameter Pollution (Server Side)
10061   Server Leaks Information via "X-AspNet-Version"/"X-AspNetMvc-Version" HTTP Response Header Field(s)
10062   Server Leaks PII in response body
10063   Feature Policy Scanner
10070   Use of SAML
10094   Base64 Disclosure
10095   Backup File Disclosure
10096   Timestamp Disclosure
10097   Hash Disclosure
10098   Cross-Domain Misconfiguration
10099   Source Code Disclosure

10101   Insufficient Authentication
10102   Insufficient Authorization

10103   Image Location Scanner - passive scanner finding
10104   TestUserAgent
10105   Weak Authentication Method
10106   Http Only Site
10107   Httpoxy - Proxy Header Misuse
10108   Blank link target
10109   Modern Web Application (info)
10110   Dangerous JS Function Scanner

10200   Beast (via HTTPS Info Extension)
10201   Crime (via HTTPS Info Extension)
10202   Absence of Anti-CSRF Tokens
10203   Freak (via HTTPS Info Extension)
10204   Robot (via HTTPS Info Extension)

20000   Cold Fusion default file [Deprecated]
20001   Lotus Domino default files [Deprecated]
20002   IIS default file [Deprecated]
20003   Macromedia JRun default files [Deprecated]
20004   Tomcat source file disclosure [Deprecated]
20005   BEA WebLogic example files [Deprecated]
20006   IBM WebSphere default files [Deprecated]
20010   URL Redirector Abuse
20012   Anti CSRF Tokens Scanner
20014   HTTP Parameter Pollution
20015   Heartbleed OpenSSL Vulnerability
20016   Cross-Domain Requests Permitted
20017   Source Code Disclosure - CVE-2012-1823
20018   Remote Code Execution - CVE-2012-1823
20019   External Redirect

30001  Check for buffer overflow in back end C code
30002  Check for proper format string handling in back end c code.
30003  Check for proper integer handling in back end c code.

40000   Cross site scripting [Deprecated]
40001   Cross site scripting in SCRIPT section [Deprecated]
40002   Cross site scripting without brackets [Deprecated]
40003   CRLF injection
40004   SQL Injection Fingerprinting [Deprecated]
40005   SQL Injection [Deprecated]
40006   MS SQL Injection Enumeration [Deprecated]
40007   Oracle SQL Injection Enumeration [Deprecated]
40008   Parameter tampering
40009   Server side include
40010   Cross site scripting in TAG [Deprecated]
40011   Cross Site Scripting in TAG Attribute [Deprecated]
40012   Cross Site Scripting (Reflected)
40013   Session Fixation
40014   Persistent XSS (Attack)
40015   LDAP Injection
40016   Persistent XSS (Prime)
40017   Persistent XSS (Spider)
40018   SQL Injection
40019   SQL Injection MySQL
40020   SQL Injection Hypersonic
40021   SQL Injection Oracle
40022   SQL Injection Postgresql
40023   Username Enumeration
40024   SQL Injection SQLite
40025   Proxy Disclosure
40026   Cross site scripting (DOM)
40027   SQL Injection MsSQL
40028   ELMAH Scanner
40029   trace.axd Scanner
40030   Backslash Powered Scanner
40031   Cross Site Scripting - Detection by Callback
40032   .htaccess Scanner
40033	NoSQL Injection MongoDB
40034   .env File Scanner
40035   Hidden File Scanner
40036   JWT Scanner
40037   CustomActiveScanForZAP (3rd Party)
40038   Bypassing 403
40039   Web Cache Deception
40040	CORS active scan rule
40041   FileUpload Scanner
40042   Spring Actuator Test
40043   Log4Shell (CVE-2021-44228, CVE-2021-45046)
40044   Exponential Entity Expansion (Billion Laughs Attack)
40045   Spring4Shell (CVE-2022-22965)
40046   Server Side Request Forgery
40047   Text4Shell (CVE-2022-42889)

50000   Active Scan scripts
50001   Passive Scan scripts
50002   Fuzzer HTTP Processor scripts
50003   Stats passive scan rule
50004   Zest scripts (Action - Fail)
50005   Client side Active Scan scripts
50006   Client side Passive Scan scripts

60000   Example simple passive rule
60001   Example file passive rule
60100   Example simple active rule
60101   Example file active rule
60200   HUD tutorial examples

90001   Insecure JSF ViewState
90002   Java Serialized Object
90003   Subresource Integrity Attribute Missing
90004   Cross-Origin Resource Policy
90011   Charset Mismatch
90017	XSLT Injection
90018   SQL Injection SQLMap
90019   Code Injection
90020   Command Injection
90021   Xpath Injection
90022   Application Error scanner
90023   XXE External Entity
90024   Generic Padding Oracle
90025   Expression Language Injection
90026   SOAP Action Spoofing
90027   Cookie Slack Detector
90028   Insecure HTTP Method
90029   SOAP XML Injection
90030   WSDL File Detection
90033   Loosely Scoped Cookie
90034   Cloud Metadata Attack
90035   Server Side Template Injection
90036   Server Side Template Injection (Blind)

100000  Client/Server HTTP Error Response Codes [Script]
100001  Unexpected Content Types [Script]
100002  CLACKS - GNU Terry Pratchett [Script]
100003  Cookie set without HTTPOnly Flag [Script]
100004  Content Security Policy violations reporting enabled [Script]
100005  SameSite cookie attribute protection used [Script]
100006  Information Leak - Internal IP via F5 BigIP Persistence Cookie [Script]
100007  Base64-encoded string found [Script]
100008  Information Leak - Credit Card Number [Script]
100009  Information Leak - Email address [Script]
100010  Information Leak - Hash [Script]
100011  Information Leak - HTML Comment [Script]
100012  Information Leak - IBAN [Script]
100013  Information Leak - Private IP address [Script]
100014  Reflected HTTP GET parameter [Script]
100015  HUNT (https://github.com/bugcrowd/HUNT) [Script]
100016  Multiple Security Header checker [Script]
100017  Non static site detection [Script]
100018  Relative Path Overwrite [Script]
100019  Information Leak - Server Header [Script]
100020  SQL Injection Detection [Script]
100021  Telerik Cryptographic Weakness [Script]
100022  Upload Form Discovery [Script]
100023  Information Leak - X-Powered-By Header [Script]
100024  Unauthenticated Gitlab SSRF (CVE-2021-22214) [Script]
100025  Cross Site WebSocket Hijacking [Script]
100026  JWT None Exploit [Script]
100027  Test Insecure HTTP Verbs [Script]
100028  User defined attacks [Script]
100029  File Content Disclosure (CVE-2019-5418) [Script]
100030  Good Old Files Lite [Script]
100031  DNS Email Spoofing [Script]
100032  WordPress Username Enumeration [Script]
100033  Server Side Template Injection [Script]

110000  Websocket Passive Scan scripts
110001  Application Error Disclosure [Script]
110002  Base64 Disclosure [Script]
110003  Debug Error Disclosure [Script]
110004  Email Disclosure [Script]
110005  Credit Card Disclosure [Script]
110006  Private IP Disclosure [Script]
110007  Username Disclosure [Script]
110008  Suspicious XML Comments Disclosure [Script]

322420463 Retire.js (3rd Party)

```
