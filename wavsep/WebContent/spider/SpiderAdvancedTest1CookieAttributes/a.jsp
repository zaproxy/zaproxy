<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<%@include file="/spider/must-visit-page.jsp"%>
<html>
<head>
<title>Spider Advanced Test</title>
</head>
<body>
	<h2>Test 1</h2>
	<p>This page sets up 2 cookies and has links to another page which
		checks if only the required cookies are checked cookies (using path
		attribute).</p>
	<%
		String urlBase = Utils.getStrippedUrl(request.getRequestURI()).toString();
		response.addCookie(new Cookie("cool_cookie", "everywhere"));
	
		Cookie c = new Cookie("uncool_cookie", "Only in test");
		c.setPath(urlBase + "test");
		response.addCookie(c);
	%>
	<p>
		See our <a href="b.jsp">other page</a>. And also this <a
			href="test/c.jsp">one</a>.
	<p>
</body>

</html>