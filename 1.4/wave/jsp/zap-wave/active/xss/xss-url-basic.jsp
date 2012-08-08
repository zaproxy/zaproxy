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
<title>OWASP ZAP WAVE - XSS URL Basic</title>
</head>
<body>
<H2>OWASP ZAP WAVE - Simple XSS in a URL parameter</H2>
<H3>Description</H3>
The 'name' parameter in the URL is written to the page without being sanitised and so is vulnerable to a simple script injection attack.<br>
Only a GET is vulnerable, not a POST.
<H3>Example</H3>
<%
	// Standard bit of code to ensure any session ID is protected using HTTPOnly
	String sessionid = request.getSession().getId();
	if (sessionid != null && sessionid.length() > 0) {
		response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionid + "; HttpOnly");
	}
	String name = request.getParameter("name");
	if (request.getMethod().equals("GET") && name != null) {
		out.println("The URL 'name' parameter is " + name + "<br/><br/>");
	}
%>
<p>
Example link: <A HREF="xss-url-basic.jsp?name=test">Simple XSS in a URL parameter</A>

</body>

