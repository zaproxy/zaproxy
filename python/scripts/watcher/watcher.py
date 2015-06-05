# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2012 ZAP Development Team
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script tests ZAP against the Watcher test pages: 
#    http://www.nottrusted.com/watcher/
#
# To this script:
# * Install the ZAP Python API: 
#     Use ''pip install python-owasp-zap' or
#     download from https://github.com/zaproxy/zaproxy/wiki/Downloads
# * Start ZAP (as this is for testing purposes you might not want the
#     'standard' ZAP to be started)
# * Access http://www.nottrusted.com/watcher/ via your browser, proxying through ZAP
# * Run the Spider against http://www.nottrusted.com/watcher/
# * Run this script
# * Open the report.html file generated in your browser
#

from zap import ZAP
import datetime

# Change this if your version of ZAP is running on a different host and/or port:
zapUrl = 'http://127.0.0.1:8090'

# Dictionary of abbreviation to keep the output a bit shorter
abbrev = {
		'Cookie set without HttpOnly flag' : 'HttpOnly',\
		'Cookie set without secure flag' : 'InsecureCookie',\
		'Content-Type header missing' : 'NoContentHeader',\
		'Cross Site Request Forgery' : 'CSRF',\
		'Cross Site Scripting' : 'XSS',\
		'Cross-domain JavaScript source file inclusion' : 'CrossJS',\
		'HTTP Parameter Override' : 'HttpOverride',\
		'IE8\'s XSS protection filter not disabled' : 'IE8XSSfilter',\
		'Incomplete or no cache-control and pragma HTTPHeader set' : 'CacheControl',\
		'Information disclosure - database error messages' : 'InfoDb',\
		'Information disclosure - debug error messages' : 'InfoDebug',\
		'Information disclosure - sensitive informations in URL' : 'InfoUrl',\
		'Information disclosure - suspicious comments' : 'InfoComments',\
		'Password Autocomplete in browser' : 'Auto',\
		'SQL Injection' : 'SQLi',\
		'SQL Injection Fingerprinting' : 'SQLfp',\
		'Weak HTTP authentication over an unsecured connection' : 'WeakAuth',\
		'Weak Authentication Method' : 'WeakAuth',\
		'X-Content-Type-Options header missing' : 'XContent',\
		'X-Frame-Options header not set' : 'XFrame'}
		
# The rules to apply:
# Column 1:	String to match against an alert URL
# Column 2: Alert abbreviation to match
# Column 3: pass or fail
# 
rules = [ \
		['Check.Pasv.Cookie.HttpOnly.php', 'HttpOnly', 'pass'], \
		['Check.Pasv.Cookie.Secure.php', 'InsecureCookie', 'pass'],\
		['Check.Pasv.CrossDomain.ScriptReference.php', 'CrossJS', 'pass'], \
		['Check.Pasv.Header.ContentTypeMissing.php', 'XContent', 'pass'], \
		['Check.Pasv.Header.FrameOptions.php', 'XFrame', 'pass'],\
		['Check.Pasv.Header.IeXssProtection.php', 'IE8XSSfilter ', 'pass'], \
		['Check.Pasv.Header.CacheControl.php', 'CacheControl', 'pass'], \
		['Check.Pasv.Header.MimeSniff.php', 'NoContentHeader', 'pass'],\
		['Check.Pasv.Header.WeakAuth.php', 'WeakAuth', 'pass'], \
		['Check.Pasv.InformationDisclosure.Comments.php', 'InfoComments', 'pass'], \
		['Check.Pasv.InformationDisclosure.DatabaseErrors.php', 'InfoDb', 'pass'], \
		['Check.Pasv.InformationDisclosure.DebugErrors.php', 'InfoDebug', 'pass'], \
		['Check.Pasv.InformationDisclosure.InUrl.php', 'InfoUrl', 'pass'], \
		['watcher/Check.Pasv.Cookie.Secure.php', 'InsecureCookie', 'pass'],\
		]

zap = ZAP(proxies={'http': zapUrl, 'https': zapUrl})

alerts = zap.alerts

uniqueUrls = set([])
# alertsPerUrl is a disctionary of urlsummary to a dictionary of type to set of alertshortnames ;)
alertsPerUrl = {}
plugins = set([])

for alert in alerts:
	url = alert.get('url')
	# Grab the url before any '?'
	url = url.split('?')[0]
	#print 'URL: ' + url
	urlEl = url.split('/')
	if (len(urlEl) > 4):
		if (urlEl[4][:5] != 'Check'):
			continue
		urlSummary = urlEl[4]
		short = abbrev.get(alert.get('alert'))
		if (short is None):
			print 'No abreviation for: ' + alert.get('alert')  
			short = alert.get('alert')
		aDict = alertsPerUrl.get(urlSummary, {'pass' : set([]), 'fail' : set([]), 'other' : set([])})
		added = False
		for rule in rules:
			if (rule[0] in urlSummary and rule[1] == short):
				aDict[rule[2]].add(short)
				added = True
				break
		if (not added):
			aDict['other'].add(short)
		alertsPerUrl[urlSummary] = aDict
		plugins.add(alert.get('alert'))
	uniqueUrls.add(url)
	
#for key, value in alertsPerUrl.iteritems():
#	print key, value

print "Alerts found"
print "------------"
for plugin in plugins:
	print plugin

# Generate report file
reportFile = open('report.html', 'w')
reportFile.write("<html><head><title>ZAP Wavsep Report</title></head><body>\n")

reportFile.write("<h1><img src=\"http://zaproxy.googlecode.com/svn/trunk/src/resource/zap64x64.png\" align=\"middle\">OWASP ZAP watcher results</h1>\n")
reportFile.write("Generated: " + datetime.datetime.now().strftime("%Y-%m-%d %H:%M") + "\n")

groupResults = []
thisGroup = ['', 0, 0]
totalPass = 0
totalFail = 0
total = 0

for key, value in sorted(alertsPerUrl.iteritems()):
	if (len(value.get('pass')) > 0):
		totalPass += 1
	else:
		totalFail += 1

# Output the summary
reportFile.write("<h3>Total Score</h3>\n")
reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">")
for i in range (totalPass):
	reportFile.write("&nbsp;&nbsp;")
reportFile.write("</font>")
reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">")
for i in range (totalFail):
	reportFile.write("&nbsp;&nbsp;")
reportFile.write("</font>")
total = 100 * totalPass / (totalPass + totalFail)
reportFile.write(str(total) + "%<br/>")
reportFile.write("Pass: " + str(totalPass) + "<br/>")
reportFile.write("Fail: " + str(totalFail) + "<br/>")
reportFile.write("Total: " + str(totalPass + totalFail) + "<br/>")

# Output the detail table
reportFile.write("<h3>Detailed Results</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Page</th><th>Result</th><th>Pass</th><th>Fail</th><th>Other</th>\n")

for key, value in sorted(alertsPerUrl.iteritems()):
	reportFile.write("<tr>")
	reportFile.write("<td>" + key + "</td>")
	reportFile.write("<td>")
	if (len(value.get('pass')) > 0):
		reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">&nbsp;PASS&nbsp</font>")
	elif (len(value.get('fail')) > 0):
		reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">&nbsp;FAIL&nbsp</font>")
	elif ('FalsePositive' in key):
		reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">&nbsp;PASS&nbsp</font>")
	else:
		reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">&nbsp;FAIL&nbsp</font>")
	reportFile.write("</td>")
	reportFile.write("<td>" + " ".join(value.get('pass')) + "&nbsp;</td>")
	reportFile.write("<td>" + " ".join(value.get('fail')) + "&nbsp;</td>")
	reportFile.write("<td>" + " ".join(value.get('other')) + "&nbsp;</td>")
	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

reportFile.write("<h3>Alerts Key</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Alert</th><th>Description</th>\n")

#for key, value in abbrev.items():
for (k, v) in sorted(abbrev.items(), key=lambda (k,v): v):
	reportFile.write("<tr>")
	reportFile.write("<td>" + v + "</td>")
	reportFile.write("<td>" + k + "</td>")
	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

reportFile.write("</body></html>\n")
reportFile.close()
	
#for key, value in sorted(alertsPerUrl.iteritems()):
#    print "%s: %s" % (key, value)

#print ''	
	
print ''	
print 'Got ' + str(len(alerts)) + ' alerts'
print 'Got ' + str(len(uniqueUrls)) + ' unique urls'

