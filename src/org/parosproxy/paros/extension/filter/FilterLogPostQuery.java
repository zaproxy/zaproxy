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
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2016/06/07 Use Constant.FOLDER_FILTER

package org.parosproxy.paros.extension.filter;

import java.nio.file.Paths;
import java.util.Hashtable;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;


public class FilterLogPostQuery extends FilterLogGetQuery {

    private static final String LOG_FILE = Paths.get(Constant.FOLDER_FILTER, "post.xls").toString();
    private static final Logger logger = Logger.getLogger(FilterLogPostQuery.class);

    @Override
    public int getId() {
        return 30;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.logposts.name") + getLogFileName();
        
    }

    @Override
    protected String getLogFileName() {
        return LOG_FILE;
    }
    
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
                    
                    
                    
                }catch(Exception e){
                    logger.error(e.getMessage(), e);
                }
            }
            
        }
    }
    
}
