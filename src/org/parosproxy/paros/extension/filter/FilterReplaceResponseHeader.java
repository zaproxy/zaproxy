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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.

package org.parosproxy.paros.extension.filter;

import java.util.regex.Matcher;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterReplaceResponseHeader extends FilterAbstractReplace {

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#getId()
     */
    @Override
    public int getId() {
        return 70;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#getName()
     */
    @Override
    public String getName() {
        return Constant.messages.getString("filter.replacerespheaser.name");
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#onHttpRequestSend(org.parosproxy.paros.network.HttpMessage)
     */
    @Override
    public void onHttpRequestSend(HttpMessage msg) {

        
        
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.filter.FilterAdaptor#onHttpResponseReceive(org.parosproxy.paros.network.HttpMessage)
     */
    @Override
    public void onHttpResponseReceive(HttpMessage msg) {
        
        if (getPattern() == null) {
            return;
        } else if (msg.getResponseHeader().isEmpty()) {
            return;
        }
        
        Matcher matcher = getPattern().matcher(msg.getResponseHeader().toString());
        String result = matcher.replaceAll(getReplaceText());
        try {
            msg.getResponseHeader().setMessage(result);
        } catch (HttpMalformedHeaderException e) {

        }
            
    }
}
