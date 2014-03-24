<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<%@include file="/spider/must-visit-page.jsp"%>
<head>
<title>Spider Medium Test</title>
</head>
<body>

	<h2>Test 6</h2>

	<p>Pages in this section should by used for testing crawlers and
		spiders. The spider should explore the links that are provided below.</p>

	<p>This test is for pages containing an HTML form with POST method.</p>
	<br />

	<form action="a.jsp" method="post">
		<h2>Form with parameters</h2>
		<input name="field1" type="text" />
		<input name="field2" type="text" value="test" /> 
		<input name="submit" type="submit" value="Go 2" />
	</form>



</body>
