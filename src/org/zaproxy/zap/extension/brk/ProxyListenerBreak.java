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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.ListModel;

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
public class ProxyListenerBreak implements ProxyListener {
	
	private static java.lang.Object semaphore = new java.lang.Object();
	private BreakPanel breakPanel = null;
	private Model model = null;
	private ExtensionBreak extension = null;
    private static Log log = LogFactory.getLog(ProxyListenerBreak.class);

	public ProxyListenerBreak(Model model, ExtensionBreak extension) {
	    this.model = model;
	    this.extension = extension;
	}
	
	/**
	 * @return Returns the trapPanel.
	 */
	public BreakPanel getBreakPanel() {
		return breakPanel;
	}
	/**
	 * @param trapPanel The trapPanel to set.
	 */
	public void setBreakPanel(BreakPanel trapPanel) {
		this.breakPanel = trapPanel;
	}
	/* (non-Javadoc)
	 * @see com.proofsecure.paros.proxy.ProxyHandler#onHttpRequestReceived(com.proofsecure.paros.network.HttpMessage)
	 */
	public void onHttpRequestSend(HttpMessage msg) {
	    
	    
		if (!getBreakPanel().getChkTrapRequest().isSelected()) {
			return;
		}
		
		if (isSkipImage(msg.getRequestHeader())) {
			return;
		}

		if (isSkipFilter(msg)) return;

		synchronized(semaphore) {
			getBreakPanel().breakPointHit();
			setBreakDisplay(msg, true);
			waitUntilContinue(msg, true);
		}
	}
	

	private void setBreakDisplay(final HttpMessage msg, boolean isRequest) {
		setHttpDisplay(getBreakPanel(), msg, isRequest);
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
	
	private void setHttpDisplay(final HttpPanel httpPanel, final HttpMessage msg, final boolean isRequest) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					httpPanel.setMessage(msg, isRequest);
				}
			});
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		
	}
	
	private void waitUntilContinue(final HttpMessage msg, final boolean isRequest) {
		getBreakPanel().setContinue(false);
		while (!getBreakPanel().isContinue()) {
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
					getBreakPanel().setMessage(null, isRequest);
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
	public void onHttpResponseReceive(HttpMessage msg) {

		if (isSkipImage(msg.getRequestHeader())|| isSkipImage(msg.getResponseHeader())) {
			return;
		}

		if (isSkipFilter(msg)) return;
        
		synchronized(semaphore) {
			getBreakPanel().breakPointHit();
			setBreakDisplay(msg, false);
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
	    
		if (getBreakPanel().isBreak()) {
			// Break on everything
			return false;
		}
		
	    try {
			URI uri = (URI) msg.getRequestHeader().getURI().clone();
		    uri.setQuery(null);
		    String sUri = uri.toString();
		    
		    // match against the break points
		    
		    ListModel lm = extension.getBreakPointsModel();
		    
		    for (int i=0; i < lm.getSize(); i++) {
		    	String str = (String) lm.getElementAt(i);
		    	
		    	str = str.replaceAll("\\.", "\\\\.");
		    	str = str.replaceAll("\\*",".*?").replaceAll("(;+$)|(^;+)", "");
		    	str = "(" + str.replaceAll(";+", "|") + ")$";
				Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(sUri);
				if (m.find()) {
					return false;
				}
		    }
    		
        } catch (URIException e) {
			log.warn(e.getMessage(), e);
        }

        return true;
	}
	
		
}
