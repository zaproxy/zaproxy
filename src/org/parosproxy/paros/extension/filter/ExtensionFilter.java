/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/11/20 Set order and name
// ZAP: 2012/03/15 Added the method getProxyListenerOrder. Set the name of the
// extension filter thread.
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added type argument to generic type, removed unnecessary
// casts and added @Override annotation to all appropriate methods.
// ZAP: 2012/06/25 Added addFilter() plus searchFilterIndex() method, that
// allows to add some custom filter to the FilterFactory (e.g.: by third
// party extensions).
// ZAP: 2012/08/01 Issue 332: added support for Modes

package org.parosproxy.paros.extension.filter;

import java.util.List;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.WebSocketProxy.State;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionFilter extends ExtensionAdaptor implements ProxyListener, WebSocketObserver, Runnable {

	private static final Logger log = Logger.getLogger(ExtensionFilter.class);
	
	public static final String NAME = "ExtensionFilter"; 
	public static final int PROXY_LISTENER_ORDER = 0;
	
	private JMenuItem menuToolsFilter = null;
	private FilterFactory filterFactory = new FilterFactory();
	private boolean isStop = false;
	
    public ExtensionFilter() {
        super();
        this.setOrder(8);
    }
    
    @Override
    public void init() {
        this.setName(NAME);
        filterFactory.loadAllFilter();
        // ZAP: changed to init(Model)
        // ZAP: Set the name of the extension filter thread.
        Thread t = new Thread(this, "ZAP-ExtensionFilter");
        t.setDaemon(true);
        t.start();
    }

    @Override
	public void initModel(Model model) {
    	// ZAP: changed to init(Model)
		super.initModel(model);
        Filter filter = null;
        // ZAP: Added type argument.
        List<Filter> filters = filterFactory.getAllFilter();
		for (int i=0; i<filters.size(); i++) {
            // ZAP: Removed unnecessary cast.
            filter = filters.get(i);
            try {
                filter.init(model);
            } catch (Exception ignore) {
            	log.warn("Error initializing filter. Continuing.", ignore);
            }
        }
	}

	@Override
	public void initView(ViewDelegate view) {
        super.initView(view);
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            // ZAP: Removed unnecessary cast.
            filter = filterFactory.getAllFilter().get(i);
            try {
                filter.initView(view);
            } catch (Exception ignore) {
            	log.warn("Error initializing view for filter. Continuing.", ignore);
            }
        }
    }
    
	/**
	 * This method initializes menuToolsFilter	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuToolsFilter() {
		if (menuToolsFilter == null) {
			menuToolsFilter = new JMenuItem();
			menuToolsFilter.setText(Constant.messages.getString("menu.tools.filter"));	// ZAP: i18n
			menuToolsFilter.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					FilterDialog dialog = new FilterDialog(getView().getMainFrame());
				    dialog.setAllFilters(filterFactory.getAllFilter());
				    dialog.showDialog(false);
				}
			});

		}
		return menuToolsFilter;
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    if (getView() != null) {
	        extensionHook.getHookMenu().addToolsMenuItem(getMenuToolsFilter());
	    }
	    extensionHook.addProxyListener(this);
	    extensionHook.addWebSocketObserver(this);
	}

	// ZAP: Added the method from WebSocketObserver interface.
	@Override
	public int getObservingOrder() {
		return PROXY_LISTENER_ORDER;
	}

	// ZAP: Added the method from WebSocketObserver interface.
	@Override
	public boolean onMessageFrame(int channelId, WebSocketMessage message) {
		Filter filter = null;
		for (int i = 0; i < filterFactory.getAllFilter().size(); i++) {
			filter = filterFactory.getAllFilter().get(i);
			try {
				if (filter.isEnabled()) {
					filter.onWebSocketPayload(message);
				}
			} catch (Exception e) {
			}
		}
		return true;
	}
	
	@Override
	public void onStateChange(State state, WebSocketProxy proxy) {
		// no need to react on state change
	}

	// ZAP: Added the method.
	@Override
	public int getProxyListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}


    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.proxy.ProxyListener#onHttpRequestSend(org.parosproxy.paros.network.HttpMessage)
     */
    @Override
    public boolean onHttpRequestSend(HttpMessage httpMessage) {
        Filter filter = null;
        // Check mode
        switch(Control.getSingleton().getMode()) {
        case safe:	
        	// Only safe thing to do is to disable all filters
        	return true;
        case protect:
        	if (!httpMessage.isInScope()) {
        		// Target not in scope, so ignore
        		return true;
        	}
        }
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            // ZAP: Removed unnecessary cast.
            filter = filterFactory.getAllFilter().get(i);
            try {
                if (filter.isEnabled()) {
                    filter.onHttpRequestSend(httpMessage);
                }
            } catch (Exception e) {}
        }
        return true;
    }



    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.proxy.ProxyListener#onHttpResponseReceive(org.parosproxy.paros.network.HttpMessage)
     */
    @Override
    public boolean onHttpResponseReceive(HttpMessage httpMessage) {
        Filter filter = null;
        // Check mode
        switch(Control.getSingleton().getMode()) {
        case safe:	
        	return true;
        case protect:
        	if (!httpMessage.isInScope()) {
        		return true;
        	}
        }
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            // ZAP: Removed unnecessary cast.
            filter = filterFactory.getAllFilter().get(i);
            try {
                if (filter.isEnabled()) {

                    filter.onHttpResponseReceive(httpMessage);
                }
            } catch (Exception e) {}
        }
        return true;
    }

    /**
     * Destroy every filter during extension destroy.
     */
    @Override
    public void destroy() {
        isStop = true;
        Filter filter = null;
        for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
            // ZAP: Removed unnecessary cast.
            filter = filterFactory.getAllFilter().get(i);
            try {
                filter.destroy();
            } catch (Exception e) {}
        }
        
        
    }

    @Override
    public void run() {
        Filter filter = null;
        
        while (!isStop) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
            }
            for (int i=0; i<filterFactory.getAllFilter().size(); i++) {
                // ZAP: Removed unnecessary cast.
                filter = filterFactory.getAllFilter().get(i);
                try {
                    if (filter.isEnabled()) {
                        filter.timer();
                    }
                } catch (Exception e) {}
            }
        }
        
    }
    
	@Override
	public String getAuthor() {
		return Constant.PAROS_TEAM;
	}
	
	/**
	 * ZAP: New method that allows to add another filter. The
	 * {@link Filter#getId()} method is used to determine its position in the
	 * list.
	 * 
	 * @param filter
	 */
	public void addFilter(Filter filter) {
		List<Filter> filters = filterFactory.getAllFilter();
		int index = searchFilterIndex(filters, filter.getId(), 0, filters.size());
		
		if (index == -1) {
			// not found - put at the end
			filters.add(filter);
		} else {
			filters.add(index, filter);
		}
	}
	
	/**
	 * ZAP: New method for doing a binary search on the filter id. Used to
	 * determine where (index) to insert the filter to the filter's list. Basic
	 * algorithm taken from Wikipedia:
	 * http://en.wikipedia.org/wiki/Binary_search_algorithm#Recursive
	 * 
	 * @param A
	 * @param key
	 * @param imin
	 * @param imax
	 * @return
	 */
	private int searchFilterIndex(List<Filter> filters, int targetId, int min, int max)
	{
		if (max <= min) {
			// set is empty, so return value showing not found
			return max + 1;
		} else {
			// calculate midpoint to cut set in half
			int mid = (min + max) / 2;

			// three-way comparison
			int id = filters.get(mid).getId();
			if (id > targetId) {
				// id is in lower subset
				return searchFilterIndex(filters, targetId, min, mid - 1);
			} else if (id < targetId) {
				// id is in upper subset
				return searchFilterIndex(filters, targetId, mid + 1, max);
			} else {
				// index has been found
				return mid + 1;
			}
		}
	}
}
