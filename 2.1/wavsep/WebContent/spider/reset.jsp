<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<head>
<title>Spider Testing Statistics Reset</title>
</head>
<body>
	<%SpiderStatistics.reset(); %>
	<h2>Spider Testing Statistics Reset</h2>

	<p>Your statistics regarding the spider testing have been reset.</p>


	<p> Please visit:</p>
	<ul>
		<li><a href="index-start.jsp">Crawling start page</a></li>
		<li><a href="statistics.jsp">View the statistics of the
				visited pages</a></li>
	</ul>

</body>
