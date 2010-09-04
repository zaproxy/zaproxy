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

import java.util.regex.Matcher;

import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterReplaceRequestHeader extends FilterAbstractReplace {

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.FilterAdaptor#getId()
     */
    public int getId() {
        return 50;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.FilterAdaptor#getName()
     */
    public String getName() {
        return "Replace HTTP request header using defined pattern.";
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.FilterAdaptor#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpRequestSend(HttpMessage msg) {

        if (getPattern() == null || msg.getRequestHeader().isEmpty()) {
            return;
        }
        
        Matcher matcher = getPattern().matcher(msg.getRequestHeader().toString());
        String result = matcher.replaceAll(getReplaceText());
        try {
            msg.getRequestHeader().setMessage(result);
        } catch (HttpMalformedHeaderException e) {

        }
        
        
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.extension.filter.FilterAdaptor#onHttpResponseReceive(com.proofsecure.paros.network.HttpMessage)
     */
    public void onHttpResponseReceive(HttpMessage msg) {
            
    }
}
