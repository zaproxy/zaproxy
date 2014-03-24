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
<%@ page import="java.sql.*" %>
<head>
<title>OWASP ZAP WAVE - Inject SQL URL basic</title>
</head>
<body>
<H2>OWASP ZAP WAVE - Simple SQL injection in a URL parameter</H2>
<H3>Description</H3>
The 'name' parameter in the URL is put in a db table without being sanitised and not using a prepared statement and so is vulnerable to an SQL injection attack.<br>
Only a GET is vulnerable, not a POST.
<H3>Example</H3>
<%
	// Standard bit of code to ensure any session ID is protected using HTTPOnly
	String sessionid = request.getSession().getId();
	if (sessionid != null && sessionid.length() > 0) {
		response.setHeader("SET-COOKIE", "JSESSIONID=" + sessionid + "; HttpOnly");
	}
	Connection c = null;
	try {
		// Get hold of the JDBC driver
		Class.forName("org.hsqldb.jdbcDriver" );
	} catch (Exception e) {
		out.println("ERROR: failed to load HSQLDB JDBC driver.");
		return;
	}
	try {
		// Establish a connection to an in memory db
		c = DriverManager.getConnection("jdbc:hsqldb:mem:SQL", "sa", "");
	} catch (Exception e) {
		out.println("ERROR: failed to make a connection to the db.");
		return;
	}
        ResultSet rs = c.getMetaData().getColumns(null, null, "INJECT_SQL_URL_BASIC", "NAME");
        if (!rs.next()) {
		// Create the schema
		PreparedStatement stmt = c.prepareStatement("CREATE CACHED TABLE INJECT_SQL_URL_BASIC (NAME varchar default '')");
		stmt.execute();
	}
        rs.close();

	String name = request.getParameter("name");
	if (request.getMethod().equals("GET") && name != null) {
		// Add the supplied name to the table _not_ using a prepared statement
		Statement stmt = c.createStatement();
		stmt.execute("INSERT INTO INJECT_SQL_URL_BASIC (NAME) VALUES ('" + name + "')");
	}
	c.close();
%>

Example link: <A HREF="inject-sql-url-basic.jsp?name=test">Simple SQL injection in a URL parameter</A>

</body>

