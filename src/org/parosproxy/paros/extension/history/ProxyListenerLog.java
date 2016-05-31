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
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2015/04/02 Issue 1582: Low memory option
// ZAP: 2015/09/07 Issue 1872: EDT accessed in daemon mode
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab

package org.parosproxy.paros.extension.history;
 
import java.awt.EventQueue;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.proxy.ConnectRequestProxyListener;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.SessionStructure;


public class ProxyListenerLog implements ProxyListener, ConnectRequestProxyListener {

	// ZAP: Added logger
    private static final Logger log = Logger.getLogger(ProxyListenerLog.class);

    // ZAP: Must be the last one of all listeners to be notified, as is the one that saves the HttpMessage 
	// to the DB and must let other listeners change ase' and testthe HttpMessage before saving it.
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
	public int getArrangeableListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
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
	
	@Override
	public boolean onHttpResponseReceive(final HttpMessage msg) {

        int type = HistoryReference.TYPE_PROXIED;
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
		        createAndAddMessage(msg, finalType);
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
    
    private void createAndAddMessage(HttpMessage msg, int type) {
        HistoryReference historyRef = createHistoryReference(msg, type);
        if (historyRef == null || (type != HistoryReference.TYPE_PROXIED && type != HistoryReference.TYPE_HIDDEN)) {
            return;
        }

        extension.addHistory(historyRef);

        addToSiteMap(historyRef, msg);
    }

    private HistoryReference createHistoryReference(HttpMessage message, int type) {
        try {
            return new HistoryReference(model.getSession(), type, message);
        } catch (Exception e) {
            // ZAP: Log exceptions
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    private void addToSiteMap(final HistoryReference ref, final HttpMessage msg) {
        if (View.isInitialised() && !EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        addToSiteMap(ref, msg);
                    }
                });
            } catch (Exception e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
            }
            return;
        }

        SessionStructure.addPath(model.getSession(), ref, msg);
        if (isFirstAccess && !Constant.isLowMemoryOptionSet()) {
            isFirstAccess = false;
            if (View.isInitialised()) {
                view.getSiteTreePanel().expandRoot();
            }
        }
    }

    @Override
    public void receivedConnectRequest(final HttpMessage connectMessage) {
        if (!model.getOptionsParam().getViewParam().isShowLocalConnectRequests()) {
            return;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HistoryReference historyRef = createHistoryReference(connectMessage, HistoryReference.TYPE_PROXY_CONNECT);
                if (historyRef != null) {
                    extension.addHistory(historyRef);
                }
            }
        });
        t.start();
    }
}
