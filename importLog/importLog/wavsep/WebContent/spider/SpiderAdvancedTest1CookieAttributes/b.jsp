<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<head>
<title>Spider Advanced Test</title>
</head>
<body>


	<h2>Test 1</h2>

	<p>This page should be visited by the crawler, which should also
		provide a cookie.</p>
	<br />

	<%
		//Do the checking
		Cookie[] cookies = request.getCookies();
		boolean good = false, bad = false;
		for (Cookie c : cookies) {
			if (c.getName().equals("cool_cookie")
					&& c.getValue().equals("everywhere"))
				good = true;
			if (c.getName().equals("uncool_cookie"))
				bad = true;
		}
		if (good && !bad)
			SpiderStatistics.addVisited(request.getServletPath());
	%>

</body>
