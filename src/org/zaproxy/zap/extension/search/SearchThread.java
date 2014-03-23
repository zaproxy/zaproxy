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

	private static final String THREAD_NAME = "ZAP-SearchThread";

	private String filter;
	private Type reqType;
	private SearchListenner searchListenner;
	private boolean stopSearch = false;
	private boolean inverse = false;
	private boolean searchJustInScope = false;
	private String baseUrl;
	private PaginationConstraintsChecker pcc;
	
    private boolean searchAllOccurrences;

    private static Logger log = Logger.getLogger(SearchThread.class);
	
    public SearchThread(String filter, Type reqType, SearchListenner searchListenner, boolean inverse, boolean searchJustInScope,
    		String baseUrl, int start, int count) {
        this(filter, reqType, searchListenner, inverse, searchJustInScope, baseUrl, start, count, true);
    }

    public SearchThread(String filter, Type reqType, SearchListenner searchListenner, boolean inverse, boolean searchJustInScope,
    		String baseUrl, int start, int count, boolean searchAllOccurrences) {
        this(filter, reqType, searchListenner, inverse, searchJustInScope, baseUrl, start, count, searchAllOccurrences, -1);
    }

    public SearchThread(String filter, Type reqType, SearchListenner searchListenner, boolean inverse, boolean searchJustInScope,
                String baseUrl, int start, int count, boolean searchAllOccurrences, int maxOccurrences) {
		super(THREAD_NAME);
		this.filter = filter;
		this.reqType = reqType;
		this.searchListenner = searchListenner;
		this.inverse = inverse;
		this.searchJustInScope = searchJustInScope;
		this.baseUrl = baseUrl;
		pcc = new PaginationConstraintsChecker(start, count, maxOccurrences);
		
		this.searchAllOccurrences = searchAllOccurrences;
	}

    public void stopSearch() {
    	this.stopSearch = true;
    }

	@Override
	public void run() {
		try {
			search();
		} finally {
			this.searchListenner.searchComplete();
		}
	}

	private void search() {
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
        		    int length = pcc.hasMaximumMatches()
        		            ? Math.min(fuzzResults.size(), pcc.getMaximumMatches())
        		            : fuzzResults.size();
        			for (SearchResult sr : fuzzResults.subList(0, length)) {
        				searchListenner.addSearchResult(sr);
        			}
        		}
        		return;
        	}

			List<Integer> list = Model.getSingleton().getDb().getTableHistory().getHistoryList(session.getSessionId());
			int last = list.size();
			int currentRecordId = 0;
			for (int index=0;index < last;index++){
				if (stopSearch) {
					break;
				}
			    int v = list.get(index).intValue();
			    try {
			    	RecordHistory hr = Model.getSingleton().getDb().getTableHistory().read(v);
			        if (hr.getHistoryType() == HistoryReference.TYPE_PROXIED || 
			                hr.getHistoryType() == HistoryReference.TYPE_ZAP_USER || 
			        		hr.getHistoryType() == HistoryReference.TYPE_SPIDER) {
			            currentRecordId = index;
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
				            if (inverse && !pcc.allMatchesProcessed()) {
					            if (! matcher.find()) {
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.REQUEST_HEAD); 
					            }
				            } else {
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()); 
					            	
							        if (!searchAllOccurrences) {
							            break;
							        }
					            }
				            }
						}
				        if (Type.Header.equals(reqType)) {
				            // Header
				        	// Request header
				            matcher = pattern.matcher(message.getRequestHeader().toString());
				            if (inverse && !pcc.allMatchesProcessed()) {
					            if (! matcher.find()) {
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.REQUEST_HEAD); 
					            }
				            } else {
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
				            }
				        	// Response header
				            matcher = pattern.matcher(message.getResponseHeader().toString());
				            if (inverse && !pcc.allMatchesProcessed()) {
					            if (! matcher.find()) {
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.RESPONSE_HEAD); 
					            }
				            } else {
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.RESPONSE_HEAD, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
				            }
						}
				        if (Type.Request.equals(reqType) ||
				        		Type.All.equals(reqType)) {
				            if (inverse && !pcc.allMatchesProcessed()) {
					            // Check for no matches in either Request Header or Body 
					            if (! pattern.matcher(message.getRequestHeader().toString()).find() && 
					            		! pattern.matcher(message.getRequestBody().toString()).find()) {    
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.REQUEST_HEAD); 
					            }
				            } else {
					            // Request Header 
					            matcher = pattern.matcher(message.getRequestHeader().toString());    
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.REQUEST_HEAD, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
					            // Request Body
					            matcher = pattern.matcher(message.getRequestBody().toString());    
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.REQUEST_BODY, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
				            }
				        }
				        if (Type.Response.equals(reqType) ||
				        		Type.All.equals(reqType)) {
				            if (inverse && !pcc.allMatchesProcessed()) {
					            // Check for no matches in either Response Header or Body 
					            if (! pattern.matcher(message.getResponseHeader().toString()).find() && 
					            		! pattern.matcher(message.getResponseBody().toString()).find()) {    
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.RESPONSE_HEAD); 
					            }
				            } else {
					            // Response header
					            matcher = pattern.matcher(message.getResponseHeader().toString());    
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.RESPONSE_HEAD, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
					            // Response body
					            matcher = pattern.matcher(message.getResponseBody().toString());    
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.RESPONSE_BODY, 
							        						matcher.start(), matcher.end()); 
								    if (!searchAllOccurrences) {
								    	break;
								    }
					            }
				            }
				        }
			        }
			        
			    } catch (HttpMalformedHeaderException e1) {
			        log.error(e1.getMessage(), e1);
			    }
			    if (pcc.hasPageEnded()) {
			    	break;
			    }
			}	            
		} catch (SQLException e) {
	        log.error(e.getMessage(), e);
		}
	}

	private void notifyInverseMatchFound(int currentRecordId, HttpMessage message, SearchMatch.Location location) {
		notifyMatchFound(currentRecordId, "", message, location, 0, 0);
	}

	private void notifyMatchFound(int currentRecordId, String stringFound, HttpMessage message, SearchMatch.Location location, int start, int end) {
	    pcc.recordProcessed(currentRecordId);
		if (!pcc.hasPageStarted()) {
			// Before the specified start
			return;
		}

		pcc.matchProcessed();
		searchListenner.addSearchResult(new SearchResult(reqType, filter, stringFound, new SearchMatch(
				message,
				location,
				start,
				end)));
	}

	private static class PaginationConstraintsChecker {

		private boolean pageStarted;
		private boolean pageEnded;
		private final int startRecord;
		private final boolean hasEnd;
		private final int finalRecord;
		private int recordsProcessed;
		private int currentRecordId;

		private final int maximumMatches;
		private final boolean hasMaximumMatches;
		private boolean allMatchesProcessed;
		private int matchesProcessed;

		public PaginationConstraintsChecker(int start, int count, int matches) {
			recordsProcessed = 0;
			matchesProcessed = 0;
			currentRecordId = -1;

			if (start > 0) {
				pageStarted = false;
				startRecord = start;
			} else {
				pageStarted = true;
				startRecord = 0;
			}

			if (count > 0) {
				hasEnd = true;
				finalRecord = !pageStarted ? start + count - 1 : count;
			} else {
				hasEnd = false;
				finalRecord = 0;
			}
			pageEnded = false;
			allMatchesProcessed = false;
			maximumMatches = matches;
			hasMaximumMatches = maximumMatches > 0;
		}

		public boolean hasMaximumMatches() {
			return hasMaximumMatches;
		}

		public int getMaximumMatches() {
			return maximumMatches;
		}

		public void recordProcessed(int recordId) {
			if (currentRecordId == recordId) {
				return;
			}
			currentRecordId = recordId;

			++recordsProcessed;

			if (!pageStarted) {
				pageStarted = recordsProcessed >= startRecord;
			}

			if (hasEnd && !pageEnded) {
				pageEnded = recordsProcessed >= finalRecord;
			}
		}

		public void matchProcessed() {
			++matchesProcessed;

			if (hasMaximumMatches && matchesProcessed >= maximumMatches) {
				allMatchesProcessed = true;
				pageEnded = true;
			}
		}

		public boolean hasPageStarted() {
			return pageStarted;
		}

		public boolean hasPageEnded() {
			return pageEnded;
		}

		public boolean allMatchesProcessed() {
			return allMatchesProcessed;
		}
	}
}
