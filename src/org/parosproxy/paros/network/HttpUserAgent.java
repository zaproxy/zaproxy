package org.parosproxy.paros.network;

import java.io.*;

import org.apache.log4j.Logger;
//import org.apache.tools.ant.types.resources.Files;


public final class HttpUserAgent {
	
	private static Logger logger = Logger.getLogger(HttpUserAgent.class);
	
	public static final String FireFox = "firefox";
	public static final String InternetExplorer = "internet explorer";
	private static final String MSIE = "msie";
	public static final String Chrome = "chrome";
	public static final String Safari = "safari";
	
	private static String searchForInternetExplorerVersion (String userAgent) {
		String line = null;
		String UserAgentListFile = "xml/internet-explorer-user-agents.txt";
		String browserVersion = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(UserAgentListFile));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.toLowerCase().equals(userAgent.toLowerCase())) {
					return browserVersion;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
		}
		return "-1";
	}
	
	private static String searchForFirefoxVersion (String userAgent) {
		String line = null;
		String UserAgentListFile = "xml/firefox-user-agents.txt";
		String browserVersion = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(UserAgentListFile));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.equals(userAgent)) {
					return browserVersion;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
		}
		return "-1";
	}
	
	private static String searchForChromeVersion (String userAgent) {
		String line = null;
		String UserAgentListFile = "xml/firefox-user-agents.txt";
		String browserVersion = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(UserAgentListFile));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.equals(userAgent)) {
					return browserVersion;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
		}
		return "-1";
	}
	
	private static String searchForSafariVersion (String userAgent) {
		String line = null;
		String UserAgentListFile = "xml/firefox-user-agents.txt";
		String browserVersion = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(UserAgentListFile));
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.equals(userAgent)) {
					return browserVersion;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading IE user agent file. Error:" + e.getMessage());
		}
		return "-1";
	}
	
	/**
	 * Return what type of browser is used based on the user-agent
	 * @param userAgent
	 * @return Returns the following values for Firefox, Internet Explorer, Chrome, and Safari:<br/>
	 * - firefox<br/>
	 * - internet explorer<br/>
	 * - chrome<br/>
	 * - safari<br/>
	 * <br/>
	 * If the browser is not know, the error string "-1" will be returned
	 */
	public static String getBrowser(String userAgent) {
		if (userAgent.toLowerCase().contains(FireFox)){
				return FireFox;
		} else if (userAgent.toLowerCase().contains(MSIE)){
				return InternetExplorer;
		} else if (userAgent.toLowerCase().contains(Chrome) || (userAgent.toLowerCase().contains(Chrome) && userAgent.toLowerCase().contains(Safari))) {
				return Chrome;
		} else if (userAgent.toLowerCase().contains(Safari))
				return Safari;
		
		return "-1";
	}
	
	/**
	 * Return the version of the browser used based on the user-agent
	 * @param userAgent
	 * @return Returns the browser version<br/>
	 * <br/>
	 * If the browser is not know, the error string "-1" will be returned
	 */
	public static String getBrowserVersion(String userAgent) {
		if (userAgent.toLowerCase().contains(FireFox)){
			return searchForFirefoxVersion(userAgent);
		} else if (userAgent.toLowerCase().contains(MSIE)){
				return searchForInternetExplorerVersion(userAgent);
		} else if (userAgent.toLowerCase().contains(Chrome) || (userAgent.toLowerCase().contains(Chrome) && userAgent.toLowerCase().contains(Safari))) {
				return searchForChromeVersion(userAgent);
		} else if (userAgent.toLowerCase().contains(Safari))
				return searchForSafariVersion(userAgent);
		
		return "-1";
	}
}
