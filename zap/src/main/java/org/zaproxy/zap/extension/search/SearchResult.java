/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

package org.zaproxy.zap.extension.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.HttpPanel;

public class SearchResult {


	private ExtensionSearch.Type type;
	private String customSearcherName;
	private HttpMessage message;
	private String regEx;
	private String stringFound;
	private List<SearchMatch> matches = null;
	private SearchMatch lastMatch = null;

	public SearchResult(ExtensionSearch.Type type, String regEx, String stringFound, SearchMatch sm) { 
		this(type, null, regEx, stringFound, sm);
	}

	public SearchResult(ExtensionSearch.Type type, String customSearcherName, String regEx, String stringFound, SearchMatch sm) { 
		this.message = sm.getMessage();
		this.type = type;
		this.customSearcherName = customSearcherName;
		this.regEx = regEx;
		this.stringFound = stringFound;
		matches = new ArrayList<>(1);
		matches.add(sm);
	}
	
	public SearchResult(HttpMessage message, ExtensionSearch.Type type, String regEx, String stringFound) {
		super();
		this.message = message;
		this.type = type;
		this.regEx = regEx;
		this.stringFound = stringFound;
	}

	public String getRegEx() {
		return regEx;
	}

	public String getStringFound() {
		return stringFound;
	}

	public void setStringFound(String stringFound) {
		this.stringFound = stringFound;
	}
	
	public HttpMessage getMessage() {
		return message;
	}

	public void setMessage(HttpMessage message) {
		this.message = message;
	}

	public ExtensionSearch.Type getType() {
		return type;
	}
	
	public String getCustomSearcherName() {
		return customSearcherName;
	}

	public void setType(ExtensionSearch.Type type) {
		this.type = type;
	}

	public SearchMatch getFirstMatch(HttpPanel reqPanel, HttpPanel resPanel) {
		if (matches == null) {
			enumerateMatches(reqPanel, resPanel);
		}
		if (matches.size() > 0) {
			lastMatch = matches.get(0);
			return lastMatch;
		}
		return null;
	}

	public SearchMatch getLastMatch(HttpPanel reqPanel, HttpPanel resPanel) {
		if (matches == null) {
			enumerateMatches(reqPanel, resPanel);
		}
		if (matches.size() > 0) {
			lastMatch = matches.get(matches.size() - 1);
			return lastMatch;
		}
		return null;
	}
	
	public SearchMatch getNextMatch() {
		if (lastMatch != null) {
			int i = matches.indexOf(lastMatch);
			if (i >= 0 && i < matches.size()-1) {
				lastMatch = matches.get(i+1);
				return lastMatch;
			}
		}
		return null;
	}

	public SearchMatch getPrevMatch() {
		if (lastMatch != null) {
			int i = matches.indexOf(lastMatch);
			if (i >= 1) {
				lastMatch = matches.get(i-1);
				return lastMatch;
			}
		}
		return null;
	}

	private void enumerateMatches(HttpPanel reqPanel, HttpPanel resPanel) {
		matches = new ArrayList<>();

		Pattern p = Pattern.compile(regEx, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
		
		// URL
		if (ExtensionSearch.Type.URL.equals(type)) {
			// TODO: handle multiple matches in the url?
			reqPanel.headerSearch(p, matches);
			return;
		}
		// Request Header
		if (ExtensionSearch.Type.Header.equals(type)) {
			reqPanel.headerSearch(p, matches);
			return;
		}
		// All or Request
		if (ExtensionSearch.Type.All.equals(type) || ExtensionSearch.Type.Request.equals(type)) {
			reqPanel.headerSearch(p, matches);
			reqPanel.bodySearch(p, matches);
		}
		// All or response
		if (ExtensionSearch.Type.All.equals(type) || ExtensionSearch.Type.Response.equals(type)) {
			resPanel.headerSearch(p, matches);
			resPanel.bodySearch(p, matches);
		}
	}

}