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
package org.zaproxy.zap.extension.brk;

import java.awt.EventQueue;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;

public class ProxyListenerBreak implements ProxyListener {

    private static final Logger log = Logger.getLogger(ProxyListenerBreak.class);
    
    //Should be the last one before the listener that saves the HttpMessage to the DB, this way 
	//the HttpMessage will be correctly shown to the user (to edit it) because it could have been changed 
	//by other ProxyListener.
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER -1;
	
	private static java.lang.Object semaphore = new java.lang.Object();
	private BreakPanel breakPanel = null;
	private Model model = null;
	private ExtensionBreak extension = null;

	public ProxyListenerBreak(Model model, ExtensionBreak extension) {
	    this.model = model;
	    this.extension = extension;
	}
	
	/**
	 * @return Returns the breakPanel.
	 */
	public BreakPanel getBreakPanel() {
		return breakPanel;
	}
	
	/**
	 * @param breakPanel The breakPanel to set.
	 */
	public void setBreakPanel(BreakPanel breakPanel) {
		this.breakPanel = breakPanel;
	}
	
	@Override
	public int getProxyListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}
	
	/* (non-Javadoc)
	 * @see com.proofsecure.paros.proxy.ProxyHandler#onHttpRequestReceived(com.proofsecure.paros.network.HttpMessage)
	 */
	public boolean onHttpRequestSend(HttpMessage msg) {
	    
		if (isSkipImage(msg.getRequestHeader())) {
			return true;
		}

		if ( ! isBreakPoint(msg, true)) {
			return true;
		}
		// Do this outside of the semaphore loop so that the 'continue' button can apply to all queued break points
		// but be reset when the next break point is hit
		getBreakPanel().breakPointHit();

		synchronized(semaphore) {
			if (getBreakPanel().isHoldMessage()) {
				setBreakDisplay(msg, true);
				waitUntilContinue(msg, true);
			}
		}
		clearAndDisableRequest();
		return ! getBreakPanel().isToBeDropped();
	}

	private void setBreakDisplay(final HttpMessage msg, boolean isRequest) {
		setHttpDisplay(getBreakPanel(), msg, isRequest);
		getBreakPanel().breakPointDisplayed();
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					View.getSingleton().getMainFrame().toFront();
				}
			});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	private void setHttpDisplay(final BreakPanel breakPanel, final HttpMessage msg, final boolean isRequest) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					breakPanel.setMessage(msg, isRequest);
				}
			});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		
	}
	
	private void waitUntilContinue(final HttpMessage msg, final boolean isRequest) {
		// Note that multiple requests and responses can get built up, so pressing continue only
		// releases the current break, not all of them.
		//getBreakPanel().setContinue(false);
		while (getBreakPanel().isHoldMessage()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					getBreakPanel().getMessage(msg, isRequest);
				}
			});
		} catch (Exception ie) {
			log.warn(ie.getMessage(), ie);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.proofsecure.paros.proxy.ProxyHandler#onHttpResponseSend(com.proofsecure.paros.network.HttpMessage)
	 */
	public boolean onHttpResponseReceive(HttpMessage msg) {
		if (isSkipImage(msg.getRequestHeader())|| isSkipImage(msg.getResponseHeader())) {
			return true;
		}

		if (! isBreakPoint(msg, false)) {
			return true;
		}
        
		// Do this outside of the semaphore loop so that the 'continue' button can apply to all queued break points
		// but be reset when the next break point is hit
		getBreakPanel().breakPointHit();

		synchronized(semaphore) {
			//getBreakPanel().breakPointHit();
			if (getBreakPanel().isHoldMessage()) {
				setBreakDisplay(msg, false);
				waitUntilContinue(msg, false);
			}
		}
		clearAndDisableResponse();

		return ! getBreakPanel().isToBeDropped();
	}
	
	public boolean isSkipImage(HttpHeader header) {
		if (header.isImage() && !model.getOptionsParam().getViewParam().isProcessImages()) {
			return true;
		}
		
		return false;
	}

	private boolean isBreakPoint(HttpMessage msg, boolean request) {
		if (request && getBreakPanel().isBreakRequest()) {
			// Break on all requests
			return true;
		} else if ( ! request && getBreakPanel().isBreakResponse()) {
			// Break on all responses
			return true;
		} else if (getBreakPanel().isStepping()) {
			// Stopping through all requests and responses
			return true;
		}
		
	    try {
	    	List<BreakPoint> breakPoints = extension.getBreakPointsList();
		    
	    	if (breakPoints.isEmpty()) {
		    	// No break points
		    	return false;
		    }
		    
			URI uri = (URI) msg.getRequestHeader().getURI().clone();
		    uri.setQuery(null);
		    String sUri = uri.getURI();
		    
		    // match against the break points
		    
			synchronized (breakPoints) {
				Iterator<BreakPoint> it = breakPoints.iterator();
				
				while(it.hasNext()) {
					BreakPoint breakPoint = it.next();
					
					if (breakPoint.isEnabled() && breakPoint.match(sUri)) {
						return true;
					}
				}
			}
        } catch (URIException e) {
			log.warn(e.getMessage(), e);
        }

        return false;
	}
	
	private void clearAndDisableRequest() {
		if (EventQueue.isDispatchThread()) {
			getBreakPanel().clearAndDisableRequest();
		} else {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						getBreakPanel().clearAndDisableRequest();
					}
				});
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
	}
	
	private void clearAndDisableResponse() {
		if (EventQueue.isDispatchThread()) {
			getBreakPanel().clearAndDisableResponse();
		} else {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run() {
						getBreakPanel().clearAndDisableResponse();
					}
				});
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
	}
}
