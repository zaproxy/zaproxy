<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<head>
<title>Spider Medium Test</title>
</head>
<body>
	<h2>Test 6</h2>
	<p>This page should be visited by the crawler with the field1 set
		to something and field2 set to "test".</p>
	<%
		if (request.getParameter("field1") != null
				&& request.getParameter("field2") != null
				&& request.getParameter("field2").equals("test"))
			SpiderStatistics.addVisited(request.getServletPath());
		if (request.getParameterMap() != null)
			System.out.println("\tParameter Map: "
					+ request.getParameterMap());
	%>
	<br />
</body>

