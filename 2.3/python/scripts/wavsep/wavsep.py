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

# This script tests ZAP against wavsep: http://code.google.com/p/wavsep/
# Note wavsep has to be installed somewhere - the above link is to the 
# project not the test suite!
#
# To this script:
# * Install the ZAP Python API: 
#     Use 'pip install python-owasp-zap-v2' or
#     download from http://code.google.com/p/zaproxy/downloads/list
# * Start ZAP (as this is for testing purposes you might not want the
#     'standard' ZAP to be started)
# * Access wavsep via your browser, proxying through ZAP
# * Vist all of the wavsep top level URLs, eg
#     http://localhost:8080/wavsep/index-active.jsp
#     http://localhost:8080/wavsep/index-passive.jsp
# * Run the Spider against http://localhost:8080
# * Run the Active Scanner against http://localhost:8080/wavsep
# * Run this script
# * Open the report.html file generated in your browser
#
# Notes:
# This has only been tested against wavsep 1.1.1
# It will be updated to later versions of wavsep asap after their release
# And it only scores the active vulnerabiolities, not the passive ones.

from zapv2 import ZAPv2
import datetime

# Change this if your version of ZAP is running on a different host and/or port:
zapUrl = 'http://127.0.0.1:8090'

# Dictionary of abbreviation to keep the output a bit shorter
abbrev = {'Cross Site Scripting (Reflected)' : 'RXSS',\
		'Absence of Anti-CSRF Tokens' : 'NoCSRF',\
		'Anti CSRF tokens scanner' : 'ACSRF',\
		'Cookie set without HttpOnly flag' : 'HttpOnly',\
		'Cross Site Request Forgery' : 'CSRF',\
		'HTTP Parameter Override' : 'ParamOver',\
		'Information disclosure - database error messages' : 'InfoDb',\
		'Information disclosure - debug error messages' : 'InfoDebug',\
		'Information disclosure - sensitive informations in URL' : 'InfoUrl',\
		'None. Warning only.' : 'NoCSRF2',\
		'Password Autocomplete in browser' : 'Auto',\
		'Path Traversal' : 'PathTrav',\
		'Remote File Inclusion' : 'RFI',\
		'SQL Injection' : 'SQLi',\
		'SQL Injection - MySQL' : 'SqlMySql',\
		'SQL Injection - Generic SQL RDBMS' : 'SqlGen',\
		'SQL Injection - Boolean Based' : 'SqlBool',\
		'SQL Injection - Error Based - Generic SQL RDBMS' : 'SqlGenE',\
		'SQL Injection - Error Based - MySQL' : 'SqlMySqlE',\
		'SQL Injection - Error Based - Java' : 'SqlJavaE',\
		'SQL Injection (Hypersonic SQL) - Time Based' : 'SqlHyperT',\
		'SQL Injection (MySQL) - Time Based' : 'SqlMySqTl',\
		'SQL Injection (Oracle) - Time Based' : 'SqlOracleT',\
		'SQL Injection (PostgreSQL) - Time Based' : 'SqlPostgreT',\
		'URL Redirector Abuse' : 'UrlRedir',\
		'Viewstate without MAC signature (Unsure)' : 'ViewstateNoMac',\
		'Weak Authentication Method' : 'WeakAuth',\
		'X-Content-Type-Options header missing' : 'XContent',\
		'X-Frame-Options header not set' : 'XFrame'}

# The rules to apply:
# Column 1:	String to match against an alert URL
# Column 2: Alert abbreviation to match
# Column 3: pass, fail, ignore
# 
rules = [ \
		# All these appear to be valid ;)
		['-', 'InfoDebug', 'ignore'], \
		['-', 'InfoUrl', 'ignore'], \
		['-', 'ACSRF', 'ignore'], \
		['-', 'NoCSRF', 'ignore'], \
		['-', 'NoCSRF2', 'ignore'], \
		['-', 'ParamOver', 'ignore'], \
		['-', 'XFrame', 'ignore'], \
		['-', 'XContent', 'ignore'], \
		['LFI-', 'RXSS', 'ignore'], \
		['RFI-', 'RXSS', 'ignore'], \
		['SInjection-', 'RXSS', 'ignore'], \
		['LoginBypass', 'Auto', 'ignore'], \
		['CrlfRemovalInHttpHeader', 'HttpOnly', 'ignore'], \
		['Tag2HtmlPageScopeValidViewstateRequired', 'ViewstateNoMac', 'ignore'], \
		#
		['LFI-Detection-Evaluation', 'PathTrav', 'pass'], \
		['LFI-FalsePositives', 'PathTrav', 'fail'], \
		['RFI-Detection-Evaluation', 'RFI', 'pass'], \
		['RFI-FalsePositives', 'RFI', 'fail'], \
		['RXSS-Detection-Evaluation', 'RXSS', 'pass'], \
		['RXSS-FalsePositives-GET', 'RXSS', 'fail'], \
		
		['SInjection-Detection-Evaluation', 'SQLfp', 'pass'], \
		['SInjection-Detection-Evaluation', 'SQLi', 'pass'], \
		#['SInjection-Detection-Evaluation', 'SqlHyper', 'pass'], \
		['SInjection-Detection-Evaluation', 'SqlBool', 'pass'], \
		['SInjection-Detection-Evaluation', 'SqlGen', 'pass'], \
		['SInjection-Detection-Evaluation', 'SqlGenE', 'pass'], \
		['SInjection-Detection-Evaluation', 'SqlMySql', 'pass'], \
		['SInjection-Detection-Evaluation', 'SqlMySqlE', 'pass'], \
		#['SInjection-Detection-Evaluation', 'SqlOracle', 'pass'], \
		#['SInjection-Detection-Evaluation', 'SqlPostgre', 'pass'], \
		['SInjection-FalsePositives', 'SQLfp', 'fail'], \
		['SInjection-FalsePositives', 'SQLi', 'fail'], \
		['SInjection-FalsePositives', 'SqlBool', 'fail'], \
		['SInjection-FalsePositives', 'SqlGen', 'fail'], \
		['SInjection-FalsePositives', 'SqlGenE', 'fail'], \
		['SInjection-FalsePositives', 'SqlMySql', 'fail'], \
		['SInjection-FalsePositives', 'SqlMySqlE', 'fail'], \
		['SInjection-FalsePositives', 'SqlHyper', 'fail'], \
		['SInjection-FalsePositives', 'SqlMySql', 'fail'], \
		['SInjection-FalsePositives', 'SqlOracle', 'fail'], \
		['SInjection-FalsePositives', 'SqlPostgre', 'fail'], \
		
		['info-cookie-no-httponly', 'HttpOnly', 'pass'], \
		['session-password-autocomplete', 'Auto', 'pass'], \
		['weak-authentication-basic', 'WeakAuth', 'pass'], \
		]

zap = ZAPv2(proxies={'http': zapUrl, 'https': zapUrl})


uniqueUrls = set([])
# alertsPerUrl is a disctionary of urlsummary to a dictionary of type to set of alertshortnames ;)
alertsPerUrl = {}
plugins = set([])

alertPassCount = {}
alertFailCount = {}
alertIgnoreCount = {}
alertOtherCount = {}

totalAlerts = 0
offset = 0
page = 100
# Page through the alerts as otherwise ZAP can hang...
alerts = zap.core.alerts('', offset, page)
while len(alerts['alerts']) > 0:
	totalAlerts += len(alerts['alerts'])
	for alert in alerts['alerts']:
		url = alert.get('url')
		# Grab the url before any '?'
		url = url.split('?')[0]
		#print 'URL: ' + url
		urlEl = url.split('/')
		if (len(urlEl) > 6):
			#print 'URL 4:' + urlEl[4] + ' 6:' + urlEl[6].split('-')[0]
			if (urlEl[3] != 'wavsep'):
				# print 'Ignoring non wavsep URL 4:' + urlEl[4] + ' URL 5:' + urlEl[5]  + ' URL 6:' + urlEl[6]
				continue
				
			if (urlEl[6].split('-')[0][:9] == 'index.jsp'):
				#print 'Ignoring index URL 4:' + urlEl[4] + ' URL 5:' + urlEl[5]  + ' URL 6:' + urlEl[6]
				continue
			if (urlEl[6] == 'active'):
				if (urlEl[6].split('-')[0][:4] != 'Case'):
					#print 'Ignoring index URL 4:' + urlEl[4] + ' URL 5:' + urlEl[5]  + ' URL 6:' + urlEl[6]
					continue
				urlSummary = urlEl[4] + ' : ' + urlEl[5] + ' : ' + urlEl[6].split('-')[0]
			else:
				# Passive URLs have different format
				urlSummary = urlEl[4] + ' : ' + urlEl[5] + ' : ' + urlEl[6]
				
			short = abbrev.get(alert.get('alert'))
			if (short is None):
				short = 'UNKNOWN'
				print 'Unknown alert: ' + alert.get('alert')
			aDict = alertsPerUrl.get(urlSummary, {'pass' : set([]), 'fail' : set([]), 'ignore' : set([]), 'other' : set([])})
			added = False
			for rule in rules:
				if (rule[0] in urlSummary and rule[1] == short):
					aDict[rule[2]].add(short)
					# Counts per alert
					if (rule[2] == 'pass'):
						alertPassCount[short] = alertPassCount.get(short, 0) + 1
					elif (rule[2] == 'fail'):
						alertFailCount[short] = alertFailCount.get(short, 0) + 1
					elif (rule[2] == 'ignore'):
						alertIgnoreCount[short] = alertIgnoreCount.get(short, 0) + 1
					added = True
					break
			if (not added):
				aDict['other'].add(short)
				alertOtherCount[short] = alertOtherCount.get(short, 0) + 1
			alertsPerUrl[urlSummary] = aDict
			plugins.add(alert.get('alert'))
		uniqueUrls.add(url)
	offset += page
	alerts = zap.core.alerts('', offset, page)
	
#for key, value in alertsPerUrl.iteritems():
#	print key, value

# Generate report file
reportFile = open('report.html', 'w')
reportFile.write("<html><head><title>ZAP Wavsep Report</title></head><body>\n")

reportFile.write("<h1><img src=\"http://zaproxy.googlecode.com/svn/trunk/src/resource/zap64x64.png\" align=\"middle\">OWASP ZAP wavsep results</h1>\n")
reportFile.write("Generated: " + datetime.datetime.now().strftime("%Y-%m-%d %H:%M") + "\n")

topResults = []
thisTop = ['', 0, 0]

groupResults = []
thisGroup = ['', 0, 0]
totalPass = 0
totalFail = 0

# Calculate the top level scores
for key, value in sorted(alertsPerUrl.iteritems()):
	top = key.split(' : ')[1]
	if ('-' in top):
		top = top.split('-')[0] + '-' + top.split('-')[1]
		
	if (top != thisTop[0]):
		thisTop = [top, 0, 0]	# top, pass, fail
		topResults.append(thisTop)
	if (len(value.get('pass')) > 0):
		thisTop[1] += 1
	elif (len(value.get('fail')) > 0):
		thisTop[2] += 1
	elif ('FalsePositive' in key):
		thisTop[1] += 1
	else:
		thisTop[2] += 1
		
# Calculate the group scores
for key, value in sorted(alertsPerUrl.iteritems()):
	group = key.split(' : ')[1]
	if (group != thisGroup[0]):
		thisGroup = [group, 0, 0]	# group, pass, fail
		groupResults.append(thisGroup)
	if (len(value.get('pass')) > 0):
		totalPass += 1
		thisGroup[1] += 1
	elif (len(value.get('fail')) > 0):
		totalFail += 1
		thisGroup[2] += 1
	elif ('FalsePositive' in key):
		totalPass += 1
		thisGroup[1] += 1
	else:
		totalFail += 1
		thisGroup[2] += 1

# Output the summary
reportFile.write("<h3>Total Score</h3>\n")
reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">")
for i in range (totalPass/4):
	reportFile.write("&nbsp;")
reportFile.write("</font>")
reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">")
for i in range (totalFail/4):
	reportFile.write("&nbsp;")
reportFile.write("</font>")
total = 100 * totalPass / (totalPass + totalFail)
reportFile.write(str(total) + "%<br/>")

# Output the top level table
reportFile.write("<h3>Top Level Scores</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Top Level</th><th>Pass</th><th>Fail</th><th>Score</th><th>Chart</th>\n")

for topResult in topResults:
    #print "%s Pass: %i Fail: %i Score: %i\%" % (topResult[0], topResult[1], topResult[2], (100*topResult[1]/topResult[1]+topResult[2]))
	reportFile.write("<tr>")
	reportFile.write("<td>" + topResult[0] + "</td>")
	reportFile.write("<td align=\"right\">" + str(topResult[1]) + "</td>")
	reportFile.write("<td align=\"right\">" + str(topResult[2]) + "</td>")
	score = 100 * topResult[1] / (topResult[1] + topResult[2])
	reportFile.write("<td align=\"right\">" + str(score) + "%</td>")
	reportFile.write("<td>")
	reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">")
	for i in range (topResult[1]/4):
		reportFile.write("&nbsp;")
	reportFile.write("</font>")
	reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">")
	for i in range (topResult[2]/4):
		reportFile.write("&nbsp;")
	reportFile.write("</font>")
	reportFile.write("</td>")
	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

reportFile.write("<h3>Alerts</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Alert</th><th>Description</th><th>Pass</th><th>Fail</th><th>Ignore</th><th>Other</th>\n")

#for key, value in abbrev.items():
for (k, v) in sorted(abbrev.items(), key=lambda (k,v): v):
	reportFile.write("<tr>")
	reportFile.write("<td>" + v + "</td>")
	reportFile.write("<td>" + k + "</td>")
	reportFile.write("<td>" + str(alertPassCount.get(v, 0)) +"&nbsp;</td>")
	reportFile.write("<td>" + str(alertFailCount.get(v, 0)) +"&nbsp;</td>")
	reportFile.write("<td>" + str(alertIgnoreCount.get(v, 0)) +"&nbsp;</td>")
	reportFile.write("<td>" + str(alertOtherCount.get(v, 0)) +"&nbsp;</td>")
	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

# Output the group table
reportFile.write("<h3>Group Scores</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Group</th><th>Pass</th><th>Fail</th><th>Score</th><th>Chart</th>\n")

for groupResult in groupResults:
    #print "%s Pass: %i Fail: %i Score: %i\%" % (groupResult[0], groupResult[1], groupResult[2], (100*groupResult[1]/groupResult[1]+groupResult[2]))
	reportFile.write("<tr>")
	reportFile.write("<td>" + groupResult[0] + "</td>")
	reportFile.write("<td align=\"right\">" + str(groupResult[1]) + "</td>")
	reportFile.write("<td align=\"right\">" + str(groupResult[2]) + "</td>")
	score = 100 * groupResult[1] / (groupResult[1] + groupResult[2])
	reportFile.write("<td align=\"right\">" + str(score) + "%</td>")
	reportFile.write("<td>")
	reportFile.write("<font style=\"BACKGROUND-COLOR: GREEN\">")
	for i in range (groupResult[1]):
		reportFile.write("&nbsp;&nbsp;")
	reportFile.write("</font>")
	reportFile.write("<font style=\"BACKGROUND-COLOR: RED\">")
	for i in range (groupResult[2]):
		reportFile.write("&nbsp;&nbsp;")
	reportFile.write("</font>")
	reportFile.write("</td>")
	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

# Output the detail table
reportFile.write("<h3>Detailed Results</h3>\n")
reportFile.write("<table border=\"1\">\n")
reportFile.write("<tr><th>Page</th><th>Result</th><th>Pass</th><th>Fail</th><th>Ignore</th><th>Other</th>\n")

for key, value in sorted(alertsPerUrl.iteritems()):
	reportFile.write("<tr>")
	reportFile.write("<td>" + key.split(':')[0] + key.split(':')[2] + "</td>")
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
	
	reportFile.write("<td>")
	if (value.get('pass') is not None):
		reportFile.write(" ".join(value.get('pass')))
	reportFile.write("&nbsp;</td>")

	reportFile.write("<td>")
	if (value.get('fail') is not None):
		reportFile.write(" ".join(value.get('fail')))
	reportFile.write("&nbsp;</td>")

	reportFile.write("<td>")
	if (value.get('ignore') is not None):
		reportFile.write(" ".join(value.get('ignore')))
	reportFile.write("&nbsp;</td>")

	reportFile.write("<td>")
	if (value.get('other') is not None):
		reportFile.write(" ".join(value.get('other')))
	reportFile.write("&nbsp;</td>")

	reportFile.write("</tr>\n")

reportFile.write("</table><br/>\n")

reportFile.write("</body></html>\n")
reportFile.close()
	
#for key, value in sorted(alertsPerUrl.iteritems()):
#    print "%s: %s" % (key, value)

#print ''	
	
print ''	
print 'Got ' + str(totalAlerts) + ' alerts'
print 'Got ' + str(len(uniqueUrls)) + ' unique urls'


