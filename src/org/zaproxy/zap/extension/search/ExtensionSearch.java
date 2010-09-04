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

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionSearch extends ExtensionAdaptor implements SessionChangedListener {

	public enum Type {All, URL, Request, Response};

	private SearchPanel searchPanel = null;
    private JMenuItem menuSearch = null;
    private JMenuItem menuNext = null;
    private JMenuItem menuPrev = null;

	/**
     * 
     */
    public ExtensionSearch() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionSearch(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionSearch");
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookView().addStatusPanel(getSearchPanel());
	        extensionHook.getHookMenu().addEditMenuItem(getMenuSearch());
	        extensionHook.getHookMenu().addEditMenuItem(getMenuNext());
	        extensionHook.getHookMenu().addEditMenuItem(getMenuPrev());
	        
	        getSearchPanel().setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
	    }
	}
	
	private SearchPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new SearchPanel();
			searchPanel.setExtension(this);
		}
		return searchPanel;
	}
	

	public void sessionChanged(final Session session)  {
	    if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);

	    } else {
	        
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	            
	        }
	    }
	}
	
	private void sessionChangedEventHandler(Session session) {
	}
	
	@SuppressWarnings("unchecked")
	public String search(String filter, Type reqType){
	    String result="";
	    Session session = getModel().getSession();
        Pattern pattern = Pattern.compile(filter, Pattern.MULTILINE| Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		
		this.searchPanel.resetSearchResults();
		
	    synchronized (this) {
	        try {
	            List list = getModel().getDb().getTableHistory().getHistoryList(session.getSessionId(), HistoryReference.TYPE_MANUAL);
	            int last = list.size();
	            for (int index=0;index < last;index++){
	                int v = ((Integer)(list.get(index))).intValue();
	                try {
                        HttpMessage message = getModel().getDb().getTableHistory().read(v).getHttpMessage();

                        if (Type.URL.equals(reqType)) {
                            // URL
                            matcher = pattern.matcher(message.getRequestHeader().getURI().toString());
                            if (matcher.find()) {
                		        this.searchPanel.addSearchResult(
                		        		new SearchResult(message, reqType, 
                		        				filter, matcher.group()));
                            }
						}
                        if (Type.Request.equals(reqType) ||
                        		Type.All.equals(reqType)) {
                            // Request Header 
                            matcher = pattern.matcher(message.getRequestHeader().toString());    
                            if (matcher.find()) {
                		        this.searchPanel.addSearchResult(
                		        		new SearchResult(message, reqType, 
                		        				filter, matcher.group()));
                            }
                            // Request Body
                            matcher = pattern.matcher(message.getRequestBody().toString());    
                            if (matcher.find()) {
                		        this.searchPanel.addSearchResult(
                		        		new SearchResult(message, reqType, 
                		        				filter, matcher.group()));
                            }
                        }
                        if (Type.Response.equals(reqType) ||
                        		Type.All.equals(reqType)) {
                            // Response header
                            matcher = pattern.matcher(message.getResponseHeader().toString());    
                            if (matcher.find()) {
                		        this.searchPanel.addSearchResult(
                		        		new SearchResult(message, reqType, 
                		        				filter, matcher.group())); 
                            }
                            // Response body
                            matcher = pattern.matcher(message.getResponseBody().toString());    
                            if (matcher.find()) {
                		        this.searchPanel.addSearchResult(
                		        		new SearchResult(message, reqType, 
                		        				filter, matcher.group())); 
                            }
                        }
                        
                    } catch (HttpMalformedHeaderException e1) {
                        e1.printStackTrace();
                    }	               
	            }	            
	        } catch (SQLException e) {
	        	// Ignore
	        }

	    }
	    return result;
	}

	private JMenuItem getMenuSearch() {
        if (menuSearch == null) {
        	menuSearch = new JMenuItem();
        	menuSearch.setText("Search...");
        	menuSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        			java.awt.event.KeyEvent.VK_H, java.awt.Event.CTRL_MASK, false));

        	menuSearch.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    searchPanel.searchFocus();
                }
            });
        }
        return menuSearch;
    }

    private JMenuItem getMenuNext() {
        if (menuNext == null) {
        	menuNext = new JMenuItem();
        	menuNext.setText("Next");
        	
        	menuNext.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        			java.awt.event.KeyEvent.VK_G, java.awt.Event.CTRL_MASK, false));

        	menuNext.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    searchPanel.highlightNextResult();
                }
            });
        }
        return menuNext;
    }

    private JMenuItem getMenuPrev() {
        if (menuPrev == null) {
        	menuPrev = new JMenuItem();
        	menuPrev.setText("Previous");

        	menuPrev.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    searchPanel.highlightPrevResult();
                }
            });
        }
        return menuPrev;
    }
  }