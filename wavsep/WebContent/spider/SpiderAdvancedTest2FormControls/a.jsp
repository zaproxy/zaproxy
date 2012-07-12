<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<!-- Created by Cosmin Stefan-Dobrin for testing of Zaproxy Spider, during GSOC 2012 -->
<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@page import="com.sectooladdict.spider.Utils"%>
<%@page import="com.mysql.jdbc.Util"%>
<head>
<title>Spider Advanced Test</title>
</head>
<body>
	<h2>Test 2</h2>
	<p>This page should be visited by the crawler with the forms fields
		set to any value.</p>
	<%
		if (request.getQueryString() != null)
			System.out.println("Processing query String: " + "?"
					+ request.getQueryString());

		boolean valid = true;
		if (request.getParameter("type-text") == null) {
			System.out.println("Missing type-text");
			valid = false;
		}
		if (request.getParameter("type-search") == null) {
			System.out.println("Missing type-search");
			valid = false;
		}
		if (request.getParameter("type-tel") == null) {
			System.out.println("Missing type-tel");
			valid = false;
		}
		if (request.getParameter("type-url") == null) {
			System.out.println("Missing type-url");
			valid = false;
		}
		if (request.getParameter("type-email") == null) {
			System.out.println("Missing type-email");
			valid = false;
		}
		if (request.getParameter("type-datetime") == null) {
			System.out.println("Missing type-datetime");
			valid = false;
		}
		if (request.getParameter("type-date") == null) {
			System.out.println("Missing type-date");
			valid = false;
		}
		if (request.getParameter("type-month") == null) {
			System.out.println("Missing type-month");
			valid = false;
		}
		if (request.getParameter("type-week") == null) {
			System.out.println("Missing type-week");
			valid = false;
		}
		if (request.getParameter("type-time") == null) {
			System.out.println("Missing type-time");
			valid = false;
		}
		if (request.getParameter("type-datetime-local") == null) {
			System.out.println("Missing type-datetime-local");
			valid = false;
		}
		if (request.getParameter("type-number") == null) {
			System.out.println("Missing type-number");
			valid = false;
		}
		if (request.getParameter("type-range") == null) {
			System.out.println("Missing type-range");
			valid = false;
		}
		if (request.getParameter("type-color") == null) {
			System.out.println("Missing type-color");
			valid = false;
		}
		//It doesn't have anything filled in
		// 		if (request.getParameter("type-checkbox") == null) {
		// 			System.out.println("Missing type-checkbox");
		// 			valid = false;
		// 		}
		if (request.getParameter("type-checkbox2") == null) {
			System.out.println("Missing type-checkbox2");
			valid = false;
		}
		if (request.getParameter("type-radio") == null) {
			System.out.println("Missing type-radio");
			valid = false;
		}
		if (request.getParameter("type-select") == null) {
			System.out.println("Missing type-select");
			valid = false;
		}
		if (request.getParameter("type-textarea") == null) {
			System.out.println("Missing type-textarea");
			valid = false;
		}
		if (request.getParameter("type-file") == null) {
			System.out.println("Missing type-file");
			valid = false;
		}

		
		if (valid)
			SpiderStatistics.addVisited(request.getServletPath());
	%>
	<br />
</body>

