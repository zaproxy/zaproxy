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
package org.parosproxy.paros.extension.filter;

import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterLogCookie extends FilterAdaptor {

    private static final String DELIM = "\t";   
    private static final String CRLF = "\r\n";
    private Vector cookieList = null;
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getId()
     */
    public int getId() {
        return 100;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getName()
     */
    public String getName() {
        return "Log cookies sent by browser.";
        
    }

    public void init() {
		cookieList = new Vector();
     	
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage msg) {
        HttpRequestHeader header = msg.getRequestHeader();
        
        if (header != null ) {
            String cookie = header.getHeader("Cookie");
            synchronized (cookieList){
                if (cookie != null && cookieList.indexOf(cookie)==-1){           		
                    URI uri = (URI) header.getURI().clone();
                    try {
                        uri.setQuery(null);
                        String sUri = uri.toString();
                        cookieList.add(cookie);
                        getView().getOutputPanel().append(sUri + DELIM + cookie + "\n");

                    } catch (URIException e) {
                    	// ZAP: Print stack trace to Output tab
                    	getView().getOutputPanel().append(e);
                    }
                }
			}
		}
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#onHttpResponseReceive(org.parosproxy.paros.network.HttpMessage)
     */
    public void onHttpResponseReceive(HttpMessage httpMessage) {
        
    }
  }

