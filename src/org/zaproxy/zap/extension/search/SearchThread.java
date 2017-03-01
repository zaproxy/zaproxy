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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
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

	private Map<String, HttpSearcher> searchers;
	private String customSearcherName;
	
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
        this(filter, reqType, null, searchListenner, inverse, searchJustInScope, baseUrl, start, count, searchAllOccurrences, maxOccurrences);
    }

    public SearchThread(String filter, Type reqType, String customSearcherName, SearchListenner searchListenner, boolean inverse,
            boolean searchJustInScope, String baseUrl, int start, int count, boolean searchAllOccurrences, int maxOccurrences) {
		super(THREAD_NAME);
		this.filter = filter;
		this.reqType = reqType;
		this.customSearcherName = customSearcherName;
		this.searchListenner = searchListenner;
		this.inverse = inverse;
		this.searchJustInScope = searchJustInScope;
		this.baseUrl = baseUrl;
		pcc = new PaginationConstraintsChecker(start, count, maxOccurrences);
		
		this.searchAllOccurrences = searchAllOccurrences;
	}

    public void setCustomSearchers(Map<String, HttpSearcher> searchers) {
        this.searchers = searchers;
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

            if (Type.Custom.equals(reqType)) {
                if (searchers != null && customSearcherName != null) {
                    HttpSearcher searcher = searchers.get(customSearcherName);
                    if (searcher != null) {
                        List<SearchResult> results;
                        if (pcc.hasMaximumMatches()) {
                            results = searcher.search(pattern, inverse, pcc.getMaximumMatches());
                        } else {
                            results = searcher.search(pattern, inverse);

                        }
                        for (SearchResult sr : results) {
                            searchListenner.addSearchResult(sr);
                        }
                    }
                }
                return;
            }

			List<Integer> list = Model.getSingleton().getDb().getTableHistory().getHistoryIdsOfHistType(session.getSessionId(),
							HistoryReference.TYPE_PROXIED, HistoryReference.TYPE_ZAP_USER, HistoryReference.TYPE_SPIDER,
							HistoryReference.TYPE_SPIDER_AJAX);
			int last = list.size();
			int currentRecordId = 0;
			for (int index=0;index < last;index++){
				if (stopSearch) {
					break;
				}
			    int historyId = list.get(index).intValue();
			    try {
			            currentRecordId = index;
			        	// Create the href to ensure the msg is set up correctly
			        	HistoryReference href = new HistoryReference(historyId);
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
				            String url = message.getRequestHeader().getURI().toString();
				            matcher = pattern.matcher(url);
				            if (inverse && !pcc.allMatchesProcessed()) {
					            if (! matcher.find()) {
							        notifyInverseMatchFound(currentRecordId, message, SearchMatch.Location.REQUEST_HEAD); 
					            }
				            } else {
					            int urlStartPos = message.getRequestHeader().getPrimeHeader().indexOf(url);
					            while (matcher.find() && !pcc.allMatchesProcessed()) {
							        notifyMatchFound(currentRecordId, matcher.group(), message, SearchMatch.Location.REQUEST_HEAD, 
							        						urlStartPos + matcher.start(), urlStartPos + matcher.end()); 
					            	
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
			        
			    } catch (HttpMalformedHeaderException e1) {
			        log.error(e1.getMessage(), e1);
			    }
			    if (pcc.hasPageEnded()) {
			    	break;
			    }
			}	            
		} catch (DatabaseException e) {
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
