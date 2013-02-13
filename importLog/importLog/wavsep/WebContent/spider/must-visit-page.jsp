<%@page import="com.sectooladdict.spider.SpiderStatistics"%>
<%
	SpiderStatistics.addVisited(request.getServletPath());
	if (request.getQueryString() != null)
		System.out.println("\tQuery String: " + "?"
				+ request.getQueryString());
%>
<!-- This Page must be visited -->