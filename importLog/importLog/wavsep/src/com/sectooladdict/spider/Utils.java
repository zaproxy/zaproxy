package com.sectooladdict.spider;

import javax.servlet.http.HttpServletRequest;

public class Utils {

	public static String getBaseUrl(HttpServletRequest request) {
		String urlBase = request.getRequestURL().toString();
		if (!urlBase.endsWith("/")) {
			int pos = urlBase.lastIndexOf("/");
			urlBase = urlBase.substring(0, pos + 1);
		}
		return urlBase;
	}
	
	public static String getStrippedUrl(String url){
		String urlRet=url;
		if (!urlRet.endsWith("/")) {
			int pos = urlRet.lastIndexOf("/");
			urlRet = urlRet.substring(0, pos + 1);
		}
		return urlRet;
	}
}
