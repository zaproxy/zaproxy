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
package org.parosproxy.paros.extension.trap;

import java.awt.EventQueue;

import javax.swing.JTextArea;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.parosproxy.paros.view.View;



/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyListenerTrap implements ProxyListener {
	
    // ZAP: Added logger
    private static Log log = LogFactory.getLog(ProxyListenerTrap.class);

	private static java.lang.Object semaphore = new java.lang.Object();
	private TrapPanel trapPanel = null;
	private TrapParam trapParam = null;
	private Model model = null;

	public ProxyListenerTrap(Model model, TrapParam trapParam) {
	    this.model = model;
	    this.trapParam = trapParam;
	}
	
	/**
	 * @return Returns the trapPanel.
	 */
	public TrapPanel getTrapPanel() {
		return trapPanel;
	}
	/**
	 * @param trapPanel The trapPanel to set.
	 */
	public void setTrapPanel(TrapPanel trapPanel) {
		this.trapPanel = trapPanel;
	}
	/* (non-Javadoc)
	 * @see com.proofsecure.paros.proxy.ProxyHandler#onHttpRequestReceived(com.proofsecure.paros.network.HttpMessage)
	 */
	public void onHttpRequestSend(HttpMessage msg) {
	    
	    
		if (!getTrapPanel().getChkTrapRequest().isSelected()) {
			return;
		}
		
		if (isSkipImage(msg.getRequestHeader())) {
			return;
		}

		if (isSkipFilter(msg)) return;

		synchronized(semaphore) {
			setTrapDisplay(msg, true);
			waitUntilContinue(msg, true);
		}
	}
	

	private void setTrapDisplay(final HttpMessage msg, boolean isRequest) {
		setHttpDisplay(getTrapPanel(), msg, isRequest);
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					View.getSingleton().getMainFrame().toFront();
				}
			});
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
		}
	}
	
	private void setHttpDisplay(final HttpPanel httpPanel, final HttpMessage msg, final boolean isRequest) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					httpPanel.setMessage(msg, isRequest);
				}
			});
		} catch (Exception e) {
        	// ZAP: Log exceptions
        	log.warn(e.getMessage(), e);
		}
		
	}
	
	private void waitUntilContinue(final HttpMessage msg, final boolean isRequest) {
		getTrapPanel().setContinue(false);
		while (!getTrapPanel().isContinue()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
            	// ZAP: Log exceptions
            	log.warn(e.getMessage(), e);
			}
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
				    getTrapPanel().getMessage(msg, isRequest);
					getTrapPanel().setMessage(null, isRequest);
				}
			});
		} catch (Exception ie) {
        	// ZAP: Log exceptions
        	log.warn(ie.getMessage(), ie);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.proofsecure.paros.proxy.ProxyHandler#onHttpResponseSend(com.proofsecure.paros.network.HttpMessage)
	 */
	public void onHttpResponseReceive(HttpMessage msg) {

		if (!getTrapPanel().getChkTrapResponse().isSelected()) {
			return;
		}

		if (isSkipImage(msg.getRequestHeader())|| isSkipImage(msg.getResponseHeader())) {
			return;
		}

		if (isSkipFilter(msg)) return;
        
		synchronized(semaphore) {
			setTrapDisplay(msg, false);
			waitUntilContinue(msg, false);
		}
		
	}
	
	public String getHeaderFromJTextArea(JTextArea txtArea) {
		
		String msg = txtArea.getText();
		String result = msg.replaceAll("\\n", "\r\n");
		result = result.replaceAll("(\\r\\n)*\\z", "") + "\r\n\r\n";
		return result;
	}
	
	public String replaceHeaderForJTextArea(String msg) {
		return msg.replaceAll("\\r\\n", "\n");
	}
	
	public boolean isSkipImage(HttpHeader header) {
		if (header.isImage() && !model.getOptionsParam().getViewParam().isProcessImages()) {
			return true;
		}
		
		return false;
			
	}

	private boolean isSkipFilter(HttpMessage msg) {
	    
	    try {
			URI uri = (URI) msg.getRequestHeader().getURI().clone();
		    uri.setQuery(null);
		    String sUri = uri.toString();
    		if (trapParam.isExclude(sUri)) {
    		    return true;
    		}

    		if (!trapParam.isInclude(sUri)) {
    		    return true;
    		}

    		
        } catch (URIException e) {
        }

        return false;
	}
	
		
}
