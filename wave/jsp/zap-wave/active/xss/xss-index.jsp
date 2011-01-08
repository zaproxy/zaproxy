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
<title>OWASP ZAP WAVE - XSS Index</title>
</head>
<body>
<%
	// Standard bit of code to ensure any session ID is protected using HTTPOnly
	String sessionid = request.getSession().getId();
	if (sessionid != null && sessionid.length() > 0) {
		response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionid + "; HttpOnly");
	}
%>
<H2>OWASP ZAP WAVE - Cross Site Scripting (XSS)</H2>

<H3>Examples</H3>
<UL>
<LI><A HREF="xss-form-basic.jsp">Simple XSS in a form parameter</A>
<LI><A HREF="xss-url-basic.jsp">Simple XSS in a URL parameter</A>
<LI><A HREF="xss-cookie-basic.jsp">Simple XSS in a cookie parameter</A>
<LI><A HREF="xss-form-url.jsp">Simple XSS in a form parameter if its converted to a URL parameter</A>
<LI><A HREF="xss-form-strip-script.jsp">XSS in a form parameter with the script tag being stripped out</A>
<LI><A HREF="xss-url-strip-script.jsp">XSS in a URL parameter with the script tag being stripped out</A>
</UL>

<H3>Not yet implemented</H3>
<UL>
<LI><A HREF="xss/TODO">XSS in an image tag</A>
</UL>

</body>

