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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added type arguments to generic types, removed unused class
// variable and added @Override annotation to all appropriate methods.
// ZAP: 2012/05/04 Catch CloneNotSupportedException whenever an Uri is cloned,
// as introduced with version 3.1 of HttpClient
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.filter;

import java.util.Vector;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

public class FilterLogCookie extends FilterAdaptor {

    private static final String DELIM = "\t";
    // ZAP: Removed unused class variable (CRLF).
    // ZAP: Added type argument.
    private Vector<String> cookieList = null;
    
    @Override
    public int getId() {
        return 100;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.logcookies.name");
        
    }

    public void init() {
		// ZAP: Added type argument.
		cookieList = new Vector<>();
     	
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg) {
        HttpRequestHeader header = msg.getRequestHeader();
        
        if (header != null ) {
            String cookie = header.getHeader("Cookie");
            synchronized (cookieList){
                if (cookie != null && cookieList.indexOf(cookie)==-1){
                    try {
                    	// ZAP: catch CloneNotSupportedException as introduced with version 3.1 of HttpClient
                        URI uri;
						try {
							uri = (URI) header.getURI().clone();
						} catch (CloneNotSupportedException e) {
							throw new URIException(e.getMessage());
						}
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

    @Override
    public void onHttpResponseReceive(HttpMessage httpMessage) {
        
    }
  }

