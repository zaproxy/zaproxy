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
<%@ page import="java.util.*" %>
<head>
<title>OWASP ZAP WAVE - Info Cookie no HTTPOnly flag</title>
</head>
<body>
<H2>OWASP ZAP WAVE - Setting a cookie without the HTTPOnly flag</H2>
<H3>Description</H3>
The 'zap-info-cookie-no-http-only' cookie is set without the HTTPOnly flag being set.<br>
This means that the cookie can be accessed by client side scripts, which is usually a bad idea.<br>
Note that some servers may add this flag anyway, depending on their configuration! 
<H3>Example</H3>
<%
	String cookieName = "zap-info-cookie-no-http-only";
	response.setHeader("Set-Cookie", cookieName + "=test");

	// Display the cookie, if supplied
	Cookie cookies [] = request.getCookies ();
	Cookie myCookie = null;
	if (cookies != null) {
		for (int i = 0; i < cookies.length; i++) {
			if (cookies [i].getName().equals (cookieName)) {
				myCookie = cookies[i];
				out.println("The cookie '" + cookieName + "' is " + myCookie.getValue() + "<br/><br/>");
				break;
			}
		}
	}
%>

</body>

