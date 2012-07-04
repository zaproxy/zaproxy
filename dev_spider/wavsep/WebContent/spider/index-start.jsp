<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<head>
<title>Spider Testing Pages</title>
<link rel="stylesheet" type="text/css" href="style.css" />
</head>
<body>

	<h2>Spider Testing Start Page</h2>

	Pages in this section should by used for testing crawlers and spiders.
	The spider should start crawling and explore the following links:

	<h3>Tests: </h3>
	<%=SpiderStatistics.buildLinkTable() %>
</body>
