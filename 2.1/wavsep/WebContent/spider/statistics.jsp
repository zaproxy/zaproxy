<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<head>
<title>Spider Testing Statistics</title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>

	<h2>Spider Testing Statistics</h2>

	<a href="index.jsp">Back to the index page</a>
	<p>The results of the current crawling test can be viewed in the
		following table:</p>

	<br />
	<%=SpiderStatistics.buildStatisticsTable()%>


</body>
