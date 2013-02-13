<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<%@include file="/spider/must-visit-page.jsp"%>
<html>
<head>
<title>Spider Medium Test</title>
</head>
<body>
	<h2>Test 4</h2>
	<p>This page sets up 2 cookies and has a link to another page which
		checks for one of the cookies.</p>
	<%
		response.addCookie(new Cookie("id", "checked_id"));
		response.addCookie(new Cookie("id2", "not_checked_id"));
	%>
	<p>
		See our <a href="b.jsp">other page</a>.
	<p>
</body>

</html>