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
<title>OWASP ZAP WAVE - XSS Form basic</title>
</head>
<body>
<H2>OWASP ZAP WAVE - Simple XSS in a Form parameter</H2>
<H3>Description</H3>
The 'zap-xss-cookie-basic' cookie is written to a the page without being sanitised and so is vulnerable to a simple script injection attack.<br>
The cookie can be set via the 'name' parameter with a POST, view the page again to see its value.
<H3>Example</H3>
<%
	// Standard bit of code to ensure any session ID is protected using HTTPOnly
	String sessionid = request.getSession().getId();
	if (sessionid != null && sessionid.length() > 0) {
		response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionid + "; HttpOnly");
	}

	// Set the 'zap-xss-cookie-basic' to the name parameter (if suppplied)
	String cookieName = "zap-xss-cookie-basic";
	String name = request.getParameter("name");
	if (request.getMethod().equals("POST") && name != null) {
	/*
		Date now = new Date();
		String timestamp = now.toString();
		Cookie cookie = new Cookie (cookieName,name);
		cookie.setMaxAge(365 * 24 * 60 * 60);
		cookie
		response.addCookie(cookie);
		*/
		response.setHeader("Set-Cookie", cookieName + "=" + name + "; HTTPOnly");
	}

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

<form method="POST">
	<table>
	<tr>
	<td>Name:</td>
	<td><input id="name" name="name"></input></td>
	</tr>
	<td></td><td><input id="submit" type="submit" value="Submit"></input></td>
	</tr>
	</table>
</form>

</body>

