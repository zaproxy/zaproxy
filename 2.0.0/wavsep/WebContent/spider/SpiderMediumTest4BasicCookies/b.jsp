<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<head>
<title>Spider Medium Test</title>
</head>
<body>


	<h2>Test 4</h2>

	<p>This page should be visited by the crawler, which should also
		provide a cookie.</p>
	<br />

	<%
		//Do the checking
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies)
			if (c.getName().equals("id")
					&& c.getValue().equals("checked_id"))
				SpiderStatistics.addVisited(request.getServletPath());
	%>

</body>
