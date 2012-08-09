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
package org.zaproxy.zap.spider;

import java.util.regex.Pattern;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * The SpiderParam wraps all the parameters that are given to the spider.
 */
public class SpiderParam extends AbstractParam {
	private static final String SPIDER_MAX_DEPTH = "spider.maxDepth";
	private static final String SPIDER_THREAD = "spider.thread";
	private static final String SPIDER_SCOPE = "spider.scope";
	private static final String SPIDER_POST_FORM = "spider.postform";
	private static final String SPIDER_PROCESS_FORM = "spider.processform";
	private static final String SPIDER_SKIP_URL = "spider.skipurl";
	private static final String SPIDER_REQUEST_WAIT = "spider.requestwait";
	private static final String SPIDER_SEND_COOKIES = "spider.sendCookies";
	private static final String SPIDER_PARSE_COMMENTS = "spider.parseComments";
	private static final String SPIDER_PARSE_ROBOTS_TXT = "spider.parseRobotsTxt";

	/** The max depth of the crawling. */
	private int maxDepth = 5;
	/** The thread count. */
	private int threadCount = 2;
	/** Whether comments should be parsed for URIs. */
	private boolean parseComments = true;
	/** Whether robots.txt file should be parsed for URIs. */
	private boolean parseRobotsTxt = false;
	/** Whether the forms are processed and submitted at all. */
	private boolean processForm = true;
	/**
	 * Whether the forms are submitted, if their method is HTTP POST. This option should not be used
	 * if the forms are not processed at all (processForm).
	 */
	private boolean postForm = false;
	/** The waiting time between new requests to server - safe from DDOS. */
	private int requestWait = 200;
	/** Which urls are skipped. */
	private String skipURL = "";
	/** The pattern for skip url. */
	private Pattern patternSkipURL = null;
	/** The regex for the scope. */
	private String scopeRegex = null;
	/** The user agent string, if different than the default one. */
	private String userAgent = null;
	/** Whether the spider sends back the cookies received from the server. */
	private boolean sendCookies = false;

	/**
	 * The simple scope text used just for caching the get for the scope. the scopeRegex is the
	 * 'regexed' version of this variable's value.
	 */
	private String simpleScopeText;

	/** The log. */
	private static final Logger log = Logger.getLogger(SpiderParam.class);

	/**
	 * Instantiates a new spider param.
	 * 
	 */
	public SpiderParam() {
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.common.FileXML#parse() */
	@Override
	protected void parse() {
		// Use try/catch for every parameter so if the parsing of one fails, it's continued for the
		// others.
		try {
			this.threadCount = getConfig().getInt(SPIDER_THREAD, 2);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.maxDepth = getConfig().getInt(SPIDER_MAX_DEPTH, 5);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.processForm = getConfig().getBoolean(SPIDER_PROCESS_FORM, false);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.postForm = getConfig().getBoolean(SPIDER_POST_FORM, false);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.requestWait = getConfig().getInt(SPIDER_REQUEST_WAIT, 200);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.sendCookies = getConfig().getBoolean(SPIDER_SEND_COOKIES, true);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.parseComments = getConfig().getBoolean(SPIDER_PARSE_COMMENTS, true);
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			this.parseRobotsTxt = getConfig().getBoolean(SPIDER_PARSE_ROBOTS_TXT, false);
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			setScopeString(getConfig().getString(SPIDER_SCOPE, ""));
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}

		try {
			setSkipURLString(getConfig().getString(SPIDER_SKIP_URL, ""));
		} catch (ConversionException e) {
			log.error("Error while parsing config file: " + e.getMessage(), e);
		}
	}

	/**
	 * Gets the max depth.
	 * 
	 * @return Returns the maxDepth.
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Sets the max depth.
	 * 
	 * @param maxDepth The maxDepth to set.
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
		getConfig().setProperty(SPIDER_MAX_DEPTH, Integer.toString(this.maxDepth));

	}

	/**
	 * Gets the text describing the text.
	 * 
	 * @return returns the scope.
	 */
	public String getScopeText() {
		return simpleScopeText;
	}

	/**
	 * Gets the scope's regex.
	 * 
	 * @return returns the scope.
	 */
	public String getScope() {
		return scopeRegex;
	}

	/**
	 * Sets the scope string.
	 * 
	 * @param scope The scope string to set.
	 */
	public void setScopeString(String scope) {
		simpleScopeText = scope;
		getConfig().setProperty(SPIDER_SCOPE, scope);
		parseScope(scope);
	}

	/**
	 * Parse the scope string and build the regex pattern.
	 * 
	 * @param scope the scope string
	 */
	private void parseScope(String scope) {

		if (scope == null) {
			return;
		}
		// Remove any non used regex special characters
		scopeRegex = scope.replaceAll("(\\\\+)|(\\[+)|(\\^+)|(\\|+)|(\\?+)|(\\(+)|(\\)+)|(\\$+)", "");
		// Escape any URL-valid regex special characters
		scopeRegex = scopeRegex.replaceAll("\\.", "\\\\.");
		scopeRegex = scopeRegex.replaceAll("\\+", "\\\\+");
		// Translate '*' to 'any character' and remove any starting or trailing ';'
		scopeRegex = scopeRegex.replaceAll("\\*", ".*?").replaceAll("(;+$)|(^;+)", "");
		// Add the required '|' characters instead of ; and prepare final regex
		scopeRegex = "(" + scopeRegex.replaceAll(";+", "|") + ")$";

	}

	/**
	 * Gets the thread count.
	 * 
	 * @return Returns the thread count
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Sets the thread count
	 * 
	 * @param thread The thread count to set.
	 */
	public void setThreadCount(int thread) {
		this.threadCount = thread;
		getConfig().setProperty(SPIDER_THREAD, Integer.toString(this.threadCount));
	}

	/**
	 * Checks if is the forms should be submitted with the HTTP POST method. This option should not
	 * be used if the forms are not processed at all (processForm).
	 * 
	 * @return true, if the forms should be posted.
	 */
	public boolean isPostForm() {
		return postForm;
	}

	/**
	 * Sets if the forms should be submitted with the HTTP POST method. This option should not be
	 * used if the forms are not processed at all (processForm).
	 * 
	 * @param postForm the new post form status
	 */
	public void setPostForm(boolean postForm) {
		this.postForm = postForm;
		getConfig().setProperty(SPIDER_POST_FORM, Boolean.toString(postForm));
	}

	/**
	 * Checks if the forms should be processed
	 * 
	 * @return true, if the forms should be processed
	 */
	public boolean isProcessForm() {
		return processForm;
	}

	/**
	 * Sets if the forms should be processed.
	 * 
	 * 
	 * @param processForm the new process form status
	 */
	public void setProcessForm(boolean processForm) {
		this.processForm = processForm;
		getConfig().setProperty(SPIDER_PROCESS_FORM, Boolean.toString(processForm));
	}

	/**
	 * Sets the skip url string. This string is being parsed into a pattern which is used to check
	 * if a url should be skipped while crawling.
	 * 
	 * @param skipURL the new skip url string
	 */
	public void setSkipURLString(String skipURL) {
		this.skipURL = skipURL;
		getConfig().setProperty(SPIDER_SKIP_URL, this.skipURL);
		parseSkipURL(this.skipURL);
	}

	/**
	 * Gets the skip url string.
	 * 
	 * @return the skip url string
	 */
	public String getSkipURLString() {
		return skipURL;
	}

	/**
	 * Checks if is this url should be skipped.
	 * 
	 * @param uri the uri
	 * @return true, if the url should be skipped
	 */
	public boolean isSkipURL(URI uri) {
		if (patternSkipURL == null || uri == null) {
			return false;
		}
		String sURI = uri.toString();
		return patternSkipURL.matcher(sURI).find();
	}

	/**
	 * Parses the skip url string.
	 * 
	 * @param skipURL the skip url string
	 */
	private void parseSkipURL(String skipURL) {
		patternSkipURL = null;

		if (skipURL == null || skipURL.equals("")) {
			return;
		}

		skipURL = skipURL.replaceAll("\\.", "\\\\.");
		skipURL = skipURL.replaceAll("\\*", ".*?").replaceAll("(\\s+$)|(^\\s+)", "");
		skipURL = "\\A(" + skipURL.replaceAll("\\s+", "|") + ")";
		patternSkipURL = Pattern.compile(skipURL, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}

	/**
	 * Gets the time between the requests sent to a server.
	 * 
	 * @return the request wait time
	 */
	public int getRequestWaitTime() {
		return requestWait;
	}

	/**
	 * Sets the time between the requests sent to a server.
	 * 
	 * @param requestWait the new request wait time
	 */
	public void setRequestWaitTime(int requestWait) {
		this.requestWait = requestWait;
		this.getConfig().setProperty(SPIDER_REQUEST_WAIT, Integer.toString(requestWait));
	}

	/**
	 * Gets the user agent.
	 * 
	 * @return the user agent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * Sets the user agent, if diferent from the default one.
	 * 
	 * @param userAgent the new user agent
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * Checks if the spider should send cookies.
	 * 
	 * @return true, if is send cookies
	 */
	public boolean isSendCookies() {
		return sendCookies;
	}

	/**
	 * Sets whether to send cookies with the requests or not.
	 * 
	 * @param sendCookies the new send cookies value
	 */
	public void setSendCookies(boolean sendCookies) {
		this.sendCookies = sendCookies;
		getConfig().setProperty(SPIDER_SEND_COOKIES, Boolean.toString(sendCookies));
	}

	/**
	 * Checks if the spider should parse the comments.
	 * 
	 * @return true, if it parses the comments
	 */
	public boolean isParseComments() {
		return parseComments;
	}

	/**
	 * Sets the whether the spider parses the comments.
	 * 
	 * @param parseComments the new parses the comments value
	 */
	public void setParseComments(boolean parseComments) {
		this.parseComments = parseComments;
		getConfig().setProperty(SPIDER_PARSE_COMMENTS, Boolean.toString(parseComments));
	}

	/**
	 * Checks if the spider should parse the robots.txt for uris (not related to following the
	 * directions).
	 * 
	 * @return true, if it parses the file
	 */
	public boolean isParseRobotsTxt() {
		return parseRobotsTxt;
	}

	/**
	 * Sets the whether the spider parses the robots.txt for uris (not related to following the
	 * directions).
	 * 
	 * @param parseRobotsTxt the new value for parseRobotsTxt
	 */
	public void setParseRobotsTxt(boolean parseRobotsTxt) {
		this.parseRobotsTxt = parseRobotsTxt;
		getConfig().setProperty(SPIDER_PARSE_ROBOTS_TXT, Boolean.toString(parseRobotsTxt));
	}

}
