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

<frameset rows="20%,*">
	<frame src="d.jsp">
	<frameset cols="25%, *">
		<frame src="b.jsp" />
		<frame src="c.jsp" />
	</frameset>
</frameset>


</html>