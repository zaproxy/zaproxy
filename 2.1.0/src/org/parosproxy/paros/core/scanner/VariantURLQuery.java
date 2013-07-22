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

package org.parosproxy.paros.core.scanner;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.network.HttpMessage;


public class VariantURLQuery extends VariantAbstractQuery {

    private static Logger log = Logger.getLogger(VariantURLQuery.class);

    public VariantURLQuery() {
        super();
    }
    
    @Override
    public void setMessage(HttpMessage msg) {
        try {
            parse(msg.getRequestHeader().getURI().getQuery());
        } catch (URIException e) {
        	log.error(e.getMessage(), e);
        }
    }
    
    @Override
    protected void buildMessage(HttpMessage msg, String query, boolean escaped) {
        try {
        	if (escaped) {
        		msg.getRequestHeader().getURI().setEscapedQuery(query);
        	} else {
                msg.getRequestHeader().getURI().setQuery(query);
        	}
        } catch (URIException e) {
        	log.error(e.getMessage() + query, e);
        }
    }

}
