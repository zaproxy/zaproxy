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
	<h2>Test 3</h2>
	<p>This page contains both an Iframe and another link.</p>
	<iframe src="b.jsp" width="40%" height="300px" align="right">
		<p>
			See our <a href="c.jsp">other page</a>.
		</p>
	</iframe>
</body>

</html>