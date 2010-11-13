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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.search.ExtensionSearch.Type;

public class SearchThread extends Thread {

	private String filter;
	private 	Type reqType;
	private SearchPanel searchPanel;
	private boolean stopSearch = false;
    private static Log log = LogFactory.getLog(SearchThread.class);
	
    public SearchThread(String filter, Type reqType, SearchPanel searchPanel) {
		super();
		this.filter = filter;
		this.reqType = reqType;
		this.searchPanel = searchPanel;
	}

    public void stopSearch() {
    	this.stopSearch = true;
    }

	@SuppressWarnings("unchecked")
	public void run() {
	    Session session = Model.getSingleton().getSession();
        Pattern pattern = Pattern.compile(filter, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		
        try {

			List list = Model.getSingleton().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);
			int last = list.size();
			for (int index=0;index < last;index++){
				if (stopSearch) {
					break;
				}
			    int v = ((Integer)(list.get(index))).intValue();
			    try {
			        HttpMessage message = Model.getSingleton().getDb().getTableHistory().read(v).getHttpMessage();
			
			        if (Type.URL.equals(reqType)) {
			            // URL
			            matcher = pattern.matcher(message.getRequestHeader().getURI().toString());
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group()));
			            }
					}
			        if (Type.Header.equals(reqType)) {
			            // URL
			            matcher = pattern.matcher(message.getRequestHeader().toString());
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group()));
			            }
					}
			        if (Type.Request.equals(reqType) ||
			        		Type.All.equals(reqType)) {
			            // Request Header 
			            matcher = pattern.matcher(message.getRequestHeader().toString());    
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group()));
			            }
			            // Request Body
			            matcher = pattern.matcher(message.getRequestBody().toString());    
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group()));
			            }
			        }
			        if (Type.Response.equals(reqType) ||
			        		Type.All.equals(reqType)) {
			            // Response header
			            matcher = pattern.matcher(message.getResponseHeader().toString());    
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group())); 
			            }
			            // Response body
			            matcher = pattern.matcher(message.getResponseBody().toString());    
			            if (matcher.find()) {
					        searchPanel.addSearchResult(
					        		new SearchResult(message, reqType, 
					        				filter, matcher.group())); 
			            }
			        }
			        
			    } catch (HttpMalformedHeaderException e1) {
			        log.error(e1.getMessage(), e1);
			    }	               
			}	            
		} catch (SQLException e) {
	        log.error(e.getMessage(), e);
		}
	}
}
