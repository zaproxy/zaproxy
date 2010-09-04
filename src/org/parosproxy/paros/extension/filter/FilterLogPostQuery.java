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

import java.util.Hashtable;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterLogPostQuery extends FilterLogGetQuery {

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getId()
     */
    public int getId() {
        return 30;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.AbstractFilter#getName()
     */
    public String getName() {
        return "Log unique POST queries into file (" + getLogFileName() + ")";
        
    }

    protected String getLogFileName() {
        return "filter/post.xls";
    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage httpMessage) {

        HttpRequestHeader reqHeader = httpMessage.getRequestHeader();
        
        if (reqHeader != null && reqHeader.isText() && !reqHeader.isImage()){
            if (reqHeader.getMethod().equalsIgnoreCase(HttpRequestHeader.POST)){
                try{
                    
                    URI uri = reqHeader.getURI();
                    
                    int pos;
                    
                    String firstline;
                    
                    URI newURI = (URI) uri.clone();
                    String query = httpMessage.getRequestBody().toString();
                    if (query != null) {
                        newURI.setQuery(null);
                        firstline = newURI.toString();
                        Hashtable param = parseParameter(query);
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
