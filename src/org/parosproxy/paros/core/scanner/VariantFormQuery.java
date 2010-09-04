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
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class VariantFormQuery extends VariantAbstractQuery {

    //private static final String APP_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final String WWW_FORM_URLENCODED = "www-form-urlencoded";

    public VariantFormQuery() {
        super();
    }
    
    public void setMessage(HttpMessage msg) {
        parse(msg.getRequestBody().toString());
    }
        
    
    protected void buildMessage(HttpMessage msg, String query) {
        msg.getRequestBody().setBody(query);
    }
    
    protected String getEncodedValue(HttpMessage msg, String value) {
        String contentType = null;
        String encoded = "";
        
        contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        if (value != null) {
            if (contentType != null && contentType.toLowerCase().endsWith(WWW_FORM_URLENCODED)) {
                encoded = AbstractPlugin.getURLEncode(value);
            }
        }
        return encoded;
    }

}
