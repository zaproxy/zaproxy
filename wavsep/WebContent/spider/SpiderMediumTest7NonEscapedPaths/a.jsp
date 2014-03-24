<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<head>
<title>Spider Medium Test</title>
</head>

<%
	if (request.getParameter("a") != null
			&& request.getParameter("a").equals("1")
			&& request.getParameter("b") != null
			&& request.getParameter("b").equals("2")) {
%>
<%@include file="/spider/must-visit-page.jsp"%>
<body>
	<h2>Test 7</h2>
	<p>This page should be visited by the crawler.</p>
	<br />
</body>
<%
	} else {
%>
<body>
	<h2>Test 7</h2>
	<p>This page should NOT be visited by the crawler.</p>
	<br />
</body>
<%
	}
%>

