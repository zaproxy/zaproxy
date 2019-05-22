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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate method.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/07/01 Added content-type checking to allow special POST management by other Variants
// ZAP: 2013/08/21 Added a new encoding/decoding model for a correct parameter value interpretation
// ZAP: 2013/12/06 Constrained the data content handling to application/x-www-form-urlencoded
// ZAP: 2013/12/09 Solved NullPointerException when the request header doesn't contain "Content-Type" header field
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/02/08 Used the same constants used in ScanParam Target settings
// ZAP: 2016/05/04 Changed to use setParameters(int, List<NameValuePair>)

package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;


public class VariantFormQuery extends VariantAbstractQuery {

    private static final String WWW_APP_URL_ENCODED = "application/x-www-form-urlencoded";

    public VariantFormQuery() {
        super();
    }
    
    @Override
    public void setMessage(HttpMessage msg) {
        String contentType = msg.getRequestHeader().getHeader(HttpHeader.CONTENT_TYPE);
        // ZAP: added control for null contentType
        if (contentType != null && contentType.startsWith(WWW_APP_URL_ENCODED)) {
        	this.setParameters(NameValuePair.TYPE_POST_DATA, Model.getSingleton().getSession().getParameters(msg, Type.form));
        }
    }
            
    @Override
    protected void buildMessage(HttpMessage msg, String query) {
        msg.getRequestBody().setBody(query);
    }
    
    @Override
    protected String getEscapedValue(HttpMessage msg, String value) {
        String encoded = "";
        
        if (value != null) {
            encoded = AbstractPlugin.getURLEncode(value);
        }
        
        return encoded;
    }

    @Override
    protected String getUnescapedValue(String value) {
        //return value;
        return (value != null) ? AbstractPlugin.getURLDecode(value) : "";
    }
}
