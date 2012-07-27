<%@page import="com.sectooladdict.spider.Utils"%>
<%@ page contentType="text/plain; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/spider/must-visit-page.jsp"%>
Spider Medium Test 8

<%
	String urlBase = Utils.getBaseUrl(request);
%>
<%=urlBase + "a.jsp"%>