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
// ZAP: 2012/02/21 Added logging
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/08/21 Added a new encoding/decoding model for a correct parameter value interpretation
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/02/08 Used the same constants used in ScanParam Target settings
// ZAP: 2016/05/04 Changed to use setParameters(int, List<NameValuePair>)

package org.parosproxy.paros.core.scanner;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HtmlParameter.Type;

public class VariantURLQuery extends VariantAbstractQuery {

    private static final Logger log = Logger.getLogger(VariantURLQuery.class);

    public VariantURLQuery() {
        super();
    }

    /**
     * Encode the parameter for a correct URL introduction
     * @param msg the message object
     * @param value the value that need to be encoded
     * @return the Encoded value
     */
    @Override
    protected String getEscapedValue(HttpMessage msg, String value) {
        // ZAP: unfortunately the method setQuery() defined inside the httpclient Apache component
        // create trouble when special characters like ?+? are set inside the parameter, 
        // because this method implementation simply doesn?t encode them.
        // So we have to explicitly encode values using the URLEncoder component before setting it.
        return (value != null) ? 
                AbstractPlugin.getURLEncode(value) : "";
    }
    
    @Override
    protected String getUnescapedValue(String value) {
        //return value;
        return (value != null) ? AbstractPlugin.getURLDecode(value) : "";
    }

    @Override
    public void setMessage(HttpMessage msg) {
       	this.setParameters(NameValuePair.TYPE_QUERY_STRING, Model.getSingleton().getSession().getParameters(msg, Type.url));
    }

    @Override
    protected void buildMessage(HttpMessage msg, String query) {
        try {
            // ZAP: encoding has been decided before inside the VariantAbstractQuery
            // implementation so now we have only to set a raw query string
            msg.getRequestHeader().getURI().setEscapedQuery(query);
            
        } catch (URIException e) {
            log.error(e.getMessage() + query, e);
        }
    }
}
