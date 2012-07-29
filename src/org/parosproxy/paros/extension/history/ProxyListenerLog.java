/*
 * Created on Jun 17, 2004
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
// ZAP: 2012/03/15 Changed the method addHistory to use the class StringBuilder 
// instead of StringBuffer. Added the method getProxyListenerOrder.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI

package org.parosproxy.paros.extension.history;
 
import java.awt.EventQueue;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyListenerLog implements ProxyListener {

	// ZAP: Added logger
    private static final Logger log = Logger.getLogger(ProxyListenerLog.class);

    // ZAP: Must be the last one of all listeners to be notified, as is the one that saves the HttpMessage 
	// to the DB and must let other listeners change the HttpMessage before saving it.
    // Note: other listeners can be notified after this one but they shouldn't change the HttpMessage 
    // as that changes will not be saved to the DB.
    public static final int PROXY_LISTENER_ORDER = 5000;
    
	private ViewDelegate view = null;
	private Model model = null;
	private boolean isFirstAccess = true;
	private ExtensionHistory extension = null;
	
	public ProxyListenerLog(Model model, ViewDelegate view, ExtensionHistory extension) {
	    this.model = model;
	    this.view = view;
	    this.extension = extension;
	}

	// ZAP: Added method.
	@Override
	public int getProxyListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.proxy.ProxyHandler#onHttpRequestReceived(org.apache.commons.httpclient.HttpMethod)
	 */
	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
//	    if (msg.getRequestHeader().isImage()) {
//	        return;
//	    }
	    
	    HttpMessage existingMsg = model.getSession().getSiteTree().pollPath(msg);

	    // check if a msg of the same type exist
	    if (existingMsg != null && !existingMsg.getResponseHeader().isEmpty()) {
	        if (HttpStatusCode.isSuccess(existingMsg.getResponseHeader().getStatusCode())) {
	            // exist, no modification necessary
	            return true;
	        }
	    }
        
	    // if not, make sure a new copy will be obtained
	    if (msg.getRequestHeader().getHeader(HttpHeader.IF_MODIFIED_SINCE) != null) {
	        msg.getRequestHeader().setHeader(HttpHeader.IF_MODIFIED_SINCE, null);
	    }
	    
	    if (msg.getRequestHeader().getHeader(HttpHeader.IF_NONE_MATCH) != null) {
	        msg.getRequestHeader().setHeader(HttpHeader.IF_NONE_MATCH, null);
	    }
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.parosproxy.paros.proxy.ProxyHandler#onHttpResponseSend(org.apache.commons.httpclient.HttpMethod, org.parosproxy.paros.proxy.HttpMessage)
	 */
	@Override
	public boolean onHttpResponseReceive(final HttpMessage msg) {

        int type = HistoryReference.TYPE_MANUAL;
		if (isSkipImage(msg.getRequestHeader()) || isSkipImage(msg.getResponseHeader())) {
            if (msg.getResponseHeader().getStatusCode() == HttpStatusCode.OK) {
                type = HistoryReference.TYPE_HIDDEN;
            } else {
                return true;
            }
		}
		final int finalType = type;
		Thread t = new Thread(new Runnable() {
		    @Override
		    public void run() {
		        addHistory(msg, finalType);
		    }
		});
		t.start();
				
		return true;
	}
	    
    public boolean isSkipImage(HttpHeader header) {
		if (header.isImage() && !model.getOptionsParam().getViewParam().isProcessImages()) {
			return true;
		}
			
		return false;
				
	}
    
    private void addHistory(HttpMessage msg, int type) {
        
        HistoryReference historyRef = null;

        try {
            historyRef = new HistoryReference(model.getSession(), type, msg);
        } catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
            return;
        }

        if (type != HistoryReference.TYPE_MANUAL && type != HistoryReference.TYPE_HIDDEN) {
            return;
        }
        
        extension.addHistory(historyRef);

        // add history to site panel.  Must use event queue because this proxylistener may not be run from event queue.
        final HistoryReference ref = historyRef;
        final HttpMessage finalMsg = msg;
        if (EventQueue.isDispatchThread()) {
            model.getSession().getSiteTree().addPath(ref, msg);
            if (isFirstAccess) {
                isFirstAccess = false;
                if (View.isInitialised()) {
                	view.getSiteTreePanel().expandRoot();
                }
            }
            
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        model.getSession().getSiteTree().addPath(ref, finalMsg);
                        if (isFirstAccess) {
                            isFirstAccess = false;
                            if (View.isInitialised()) {
                            	view.getSiteTreePanel().expandRoot();
                            }
                        }
                    }
                });
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
        }
    }
}
