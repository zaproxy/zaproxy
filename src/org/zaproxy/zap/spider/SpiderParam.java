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
	private static final String SPIDER_SKIP_URL = "spider.skipurl";
	private static final String SPIDER_REQUEST_WAIT = "spider.requestwait";

	/** The max depth of the crawling. */
	private int maxDepth = 5;
	/** The thread count. */
	private int threadCount = 2;
	/** The scope of the crawl. */
	private String scope = "";
	/** Whether the forms are posted. */
	private boolean postForm = false;
	/** The waiting time between new requests to server - safe from DDOS. */
	private int requestWait = 200;
	/** Which urls are skipped. */
	private String skipURL = "";
	/** The pattern for skip url. */
	private Pattern patternSkipURL = null;
	/** The pattern for scope. */
	private Pattern patternScope = null;

	Logger log = Logger.getLogger(SpiderParam.class);

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
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
		}

		try {
			this.maxDepth = getConfig().getInt(SPIDER_MAX_DEPTH, 5);
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
		}

		try {
			this.postForm = getConfig().getBoolean(SPIDER_POST_FORM, false);
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
		}

		try {
			this.requestWait = getConfig().getInt(SPIDER_REQUEST_WAIT, 200);
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
		}

		try {
			setScopeString(getConfig().getString(SPIDER_SCOPE, ""));
		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
		}

		try {
			setSkipURLString(getConfig().getString(SPIDER_SKIP_URL, ""));

		} catch (Exception e) {
			log.error("Error while parsing config file: " + e.getMessage());
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
	 * Gets the scope string
	 * 
	 * @return Returns the scope.
	 */
	public String getScopeString() {
		return scope;
	}

	/**
	 * Sets the scope string.
	 * 
	 * @param scope The scope string to set.
	 */
	public void setScopeString(String scope) {
		this.scope = scope;
		getConfig().setProperty(SPIDER_SCOPE, this.scope);
		parseScope(this.scope);
	}

	/**
	 * Parse the scope string and build the regex pattern.
	 * 
	 * @param scope the scope string
	 */
	private void parseScope(String scope) {
		patternScope = null;

		if (scope == null || scope.equals("")) {
			return;
		}

		scope = scope.replaceAll("\\.", "\\\\.");
		scope = scope.replaceAll("\\*", ".*?").replaceAll("(;+$)|(^;+)", "");
		scope = "(" + scope.replaceAll(";+", "|") + ")$";
		patternScope = Pattern.compile(scope, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Check if given host name is in scope of the spider.
	 * 
	 * @param hostName host name to be checked.
	 * @return true, if is in scope
	 */
	public boolean isInScope(String hostName) {
		if (patternScope == null || hostName == null) {
			return false;
		}

		return patternScope.matcher(hostName).find();
	}

	/**
	 * Gets the thread count.
	 * 
	 * @return Returns the thread count
	 */
	public int getThread() {
		return threadCount;
	}

	/**
	 * Sets the thread count
	 * 
	 * @param thread The thread count to set.
	 */
	public void setThread(int thread) {
		this.threadCount = thread;
		getConfig().setProperty(SPIDER_THREAD, Integer.toString(this.threadCount));
	}

	/**
	 * Checks if is the forms should be posted.
	 * 
	 * @return true, if the forms should be posted.
	 */
	public boolean isPostForm() {
		return postForm;
	}

	/**
	 * Sets if the forms should be posted.
	 * 
	 * @param postForm the new post form status
	 */
	public void setPostForm(boolean postForm) {
		this.postForm = postForm;
		getConfig().setProperty(SPIDER_POST_FORM, Boolean.toString(postForm));
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

}
