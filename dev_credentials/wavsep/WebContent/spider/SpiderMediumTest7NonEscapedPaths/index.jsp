<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<%@include file="/spider/must-visit-page.jsp"%>
<head>
<title>Spider Medium Test</title>
</head>
<body>

	<h2>Test 7</h2>

	<p>Pages in this section should by used for testing crawlers and
		spiders. The spider should explore the links that are provided below.</p>

	<p>This test is for pages containing URIs to resources with
		non-ASCII characters or not valid URIs</p>
	<br />

	<a href="Liste_des_Wikipédias.jsp">URI with UTF8 characters</a>
	<a href="a ' + 33">Illegal URI</a>
	<a href="a.jsp?a=1&amp;b=2">URI with &amp;amp;</a>
	<br />



</body>
