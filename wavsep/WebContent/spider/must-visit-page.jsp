<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	SpiderStatistics.addVisited(request.getServletPath());
	if (request.getQueryString() != null)
		System.out.println("\tQuery String: " + "?"
				+ request.getQueryString());
%>
<!-- This Page must be visited -->