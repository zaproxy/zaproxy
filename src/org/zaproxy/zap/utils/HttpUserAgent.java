/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;


public final class HttpUserAgent {
	
	private static final Logger logger = Logger.getLogger(HttpUserAgent.class);
	
	public static final String FireFox = "firefox";
	public static final String InternetExplorer = "internet explorer";
	private static final String MSIE = "msie";
	public static final String Chrome = "chrome";
	public static final String Safari = "safari";
	
	private static String searchForInternetExplorerVersion (String userAgent) {
		return searchVersionInFile(userAgent, "internet-explorer-user-agents.txt");
	}
	
	private static String searchVersionInFile(String userAgent, String file) {
		try (BufferedReader reader = Files
				.newBufferedReader(Paths.get(Constant.getZapInstall(), "xml", file), StandardCharsets.UTF_8)) {
			String browserVersion = "";
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#")) {
					browserVersion = line.substring(2, line.length()-1);
					continue;
				}
				if (line.toLowerCase().equals(userAgent)) {
					return browserVersion;
				}
			}
		} catch (IOException e) {
			logger.debug("Error on opening/reading the file: " + file, e);
		}
		return "-1";
	}
	
	private static String searchForFirefoxVersion (String userAgent) {
		return searchVersionInFile(userAgent, "firefox-user-agents.txt");
	}
	
	private static String searchForChromeVersion (String userAgent) {
		return searchVersionInFile(userAgent, "chrome-user-agents.txt");
	}
	
	private static String searchForSafariVersion (String userAgent) {
		return searchVersionInFile(userAgent, "safari-user-agents.txt");
	}
	
	/**
	 * Return what type of browser is used based on the user-agent
	 * @param userAgent the value of the {@code User-Agent} header
	 * @return a {@code String} with one of the following values depending on the browser:
	 *         <ul>
	 *         <li>Firefox - firefox</li>
	 *         <li>Internet Explorer - internet explorer</li>
	 *         <li>Chrome - chrome</li>
	 *         <li>Safari - safari</li>
	 *         </ul>
	 *         Or, if not a known browser the error string {@code -1}.
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
	 * @param userAgent the value of the {@code User-Agent} header
	 * @return a {@code String} with the browser version, or if not a known browser the error string {@code -1}.
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
