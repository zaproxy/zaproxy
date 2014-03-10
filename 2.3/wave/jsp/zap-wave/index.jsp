<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!--
    This file is part of the OWASP Zed Attack Proxy (ZAP) project (http://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)
    ZAP is an HTTP/HTTPS proxy for assessing web application security.
    
    Author: psiinon@gmail.com
    
    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 
    You may obtain a copy of the License at 
    
      http://www.apache.org/licenses/LICENSE-2.0 
      
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License. 
-->
<head>
<title>OWASP ZAP WAVE - Index</title>
</head>
<body>
<%
	// Standard bit of code to ensure any session ID is protected using HTTPOnly
	String sessionid = request.getSession().getId();
	if (sessionid != null && sessionid.length() > 0) {
		response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionid + "; HttpOnly");
	}
%>
<H2>OWASP ZAP Web Application Vulnerability Examples</H2>

The OWASP Zed Attack Proxy - Web Application Vulnerability Examples (WAVE) are a set of pages which exhibit known vulnerabilities.
<p>
The vulnerabilities included are only those that can (or should be) detectable via automated scanners. <br/>
They have been developed to test OWASP ZAP but can be used for any other legitimate purpose.
<p>
Each page should contain only one vulnerability, and should be as simple as possible an example.<br/>
Only essential links should be included so that automated spidering can be constrained as easily as possible.
<p>
See source code for licence details.

<H3>Categories</H3>
<UL>
<LI><A HREF="active/index.jsp">Active vulnerabilities</A>, which can (probably;) only be tested by active scanners
<LI><A HREF="passive/index.jsp">Passive vulnerabilities</A>, which can be tested by passive scanners
<LI><A HREF="falsepos/index.jsp">False positives</A>, which are not vulnerabilites, but look like them
</UL>

</body>
