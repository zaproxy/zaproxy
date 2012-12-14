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

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.search.ExtensionSearch.Type;

public class SearchThread extends Thread {

	private String filter;
	private Type reqType;
	private SearchListenner searchListenner;
	private boolean stopSearch = false;
	private boolean inverse = false;
	private boolean searchJustInScope = false;
	private String baseUrl;
	private int start;
	private int count;
	
    private static Logger log = Logger.getLogger(SearchThread.class);
	
    public SearchThread(String filter, Type reqType, SearchListenner searchListenner, boolean inverse, boolean searchJustInScope,
    		String baseUrl, int start, int count) {
		super();
		this.filter = filter;
		this.reqType = reqType;
		this.searchListenner = searchListenner;
		this.inverse = inverse;
		this.searchJustInScope = searchJustInScope;
		this.baseUrl = baseUrl;
		this.start = start;
		this.count = count;
	}

    public void stopSearch() {
    	this.stopSearch = true;
    }

	@Override
	public void run() {
	    Session session = Model.getSingleton().getSession();
        Pattern pattern = Pattern.compile(filter, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		
        try {
	        // Fuzz results handled differently from the others
        	// Its also only called from the UI, so havnt implemented the baseurl, start and count options
        	// which are (currently;) just used from the API
        	if (Type.Fuzz.equals(reqType)) {
        		ExtensionFuzz extFuzz = (ExtensionFuzz) Control.getSingleton().getExtensionLoader().getExtension(ExtensionFuzz.NAME);
        		if (extFuzz != null) {
        			List<SearchResult> fuzzResults = extFuzz.searchFuzzResults(pattern, inverse);
        			for (SearchResult sr : fuzzResults) {
        				searchListenner.addSearchResult(sr);
        			}
        		}
        		this.searchListenner.searchComplete();
        		return;
        	}

			List<Integer> list = Model.getSingleton().getDb().getTableHistory().getHistoryList(session.getSessionId());
			int last = list.size();
			int c = 0;
			for (int index=0;index < last;index++){
				if (stopSearch) {
					break;
				}
			    int v = list.get(index).intValue();
			    if (this.start > 0 && v < start) {
			    	// Before the specified start
			    	continue;
			    }
			    try {
			    	RecordHistory hr = Model.getSingleton().getDb().getTableHistory().read(v);
			        if (hr.getHistoryType() == HistoryReference.TYPE_MANUAL || 
			        		hr.getHistoryType() == HistoryReference.TYPE_SPIDER) {
			        	// Create the href to ensure the msg is set up correctly
			        	HistoryReference href = new HistoryReference(hr.getHistoryId());
			        	HttpMessage message = href.getHttpMessage();
			        	if (searchJustInScope && ! session.isInScope(message.getRequestHeader().getURI().toString())) {
			        		// Not in scope, so ignore
			        		continue;
			        	}
			        	if (this.baseUrl != null && ! message.getRequestHeader().getURI().toString().startsWith(baseUrl)) {
			        		// doesnt start with the specified baseurl
			        		continue;
			        	}
				
				        if (Type.URL.equals(reqType)) {
				            // URL
				            matcher = pattern.matcher(message.getRequestHeader().getURI().toString());
				            if (inverse) {
					            if (! matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, "", 
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						0, 0))); 
							        c++;
					            }
				            } else {
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(), 
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()))); 
							        c++;
					            	
					            }
				            }
						}
				        if (Type.Header.equals(reqType)) {
				            // Header
				        	// Request header
				            matcher = pattern.matcher(message.getRequestHeader().toString());
				            if (inverse) {
					            if (! matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, "",
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						0, 0))); 
							        c++;
					            }
				            } else {
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
				            }
				        	// Response header
				            matcher = pattern.matcher(message.getResponseHeader().toString());
				            if (inverse) {
					            if (! matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, "",
							        				new SearchMatch(message, SearchMatch.Location.RESPONSE_HEAD, 
							        						0, 0))); 
							        c++;
					            }
				            } else {
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.RESPONSE_HEAD, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
				            }
						}
				        if (Type.Request.equals(reqType) ||
				        		Type.All.equals(reqType)) {
				            if (inverse) {
					            // Check for no matches in either Request Header or Body 
					            if (! pattern.matcher(message.getRequestHeader().toString()).find() && 
					            		! pattern.matcher(message.getRequestBody().toString()).find()) {    
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, "",
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						0, 0))); 
							        c++;
					            }
				            } else {
					            // Request Header 
					            matcher = pattern.matcher(message.getRequestHeader().toString());    
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
					            // Request Body
					            matcher = pattern.matcher(message.getRequestBody().toString());    
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.REQUEST_BODY, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
				            }
				        }
				        if (Type.Response.equals(reqType) ||
				        		Type.All.equals(reqType)) {
				            if (inverse) {
					            // Check for no matches in either Response Header or Body 
					            if (! pattern.matcher(message.getResponseHeader().toString()).find() && 
					            		! pattern.matcher(message.getResponseBody().toString()).find()) {    
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, "",
							        				new SearchMatch(message, SearchMatch.Location.RESPONSE_HEAD, 
							        						0, 0))); 
							        c++;
					            }
				            } else {
					            // Response header
					            matcher = pattern.matcher(message.getResponseHeader().toString());    
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.RESPONSE_HEAD, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
					            // Response body
					            matcher = pattern.matcher(message.getResponseBody().toString());    
					            while (matcher.find()) {
							        searchListenner.addSearchResult(
							        		new SearchResult(reqType, filter, matcher.group(),
							        				new SearchMatch(message, SearchMatch.Location.RESPONSE_BODY, 
							        						matcher.start(), matcher.end()))); 
							        c++;
								    if (this.count > 0 && c >= count) {
								    	break;
								    }
					            }
				            }
				        }
			        }
			        
			    } catch (HttpMalformedHeaderException e1) {
			        log.error(e1.getMessage(), e1);
			    }
			    if (this.count > 0 && c >= count) {
			    	break;
			    }
			}	            
		} catch (SQLException e) {
	        log.error(e.getMessage(), e);
		}
		this.searchListenner.searchComplete();
	}
}
