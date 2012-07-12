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
// ZAP: 2012/04/25 Added type arguments to generic type, removed unused
// variable and added @Override annotation to all appropriate methods.

package org.parosproxy.paros.extension.filter;

import java.util.Hashtable;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterLogPostQuery extends FilterLogGetQuery {

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.AbstractFilter#getId()
     */
    @Override
    public int getId() {
        return 30;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.AbstractFilter#getName()
     */
    @Override
    public String getName() {
        return Constant.messages.getString("filter.logposts.name") + getLogFileName();
        
    }

    @Override
    protected String getLogFileName() {
        return "filter/post.xls";
    }
    
    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.proxy.ProxyListener#onHttpRequestSend(org.parosproxy.paros.network.HttpMessage)
     */
    @Override
    public void onHttpRequestSend(HttpMessage httpMessage) {

        HttpRequestHeader reqHeader = httpMessage.getRequestHeader();
        
        if (reqHeader != null && reqHeader.isText() && !reqHeader.isImage()){
            if (reqHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.POST)){
                try{
                    
                    URI uri = reqHeader.getURI();
                    
                    // ZAP: Removed unused variable (int pos).
                    
                    String firstline;
                    
                    URI newURI = (URI) uri.clone();
                    String query = httpMessage.getRequestBody().toString();
                    if (query != null) {
                        newURI.setQuery(null);
                        firstline = newURI.toString();
                        // ZAP: Added type arguments.
                        Hashtable<String, String> param = parseParameter(query);
                        writeLogFile(firstline,param);
                    } else {
                        firstline = uri.toString();
                        writeLogFile(firstline,null);				
                    }
                    
                    
                    
                }catch(Exception aa){
                    aa.printStackTrace();
                }
            }
            
        }
    }
    
}
