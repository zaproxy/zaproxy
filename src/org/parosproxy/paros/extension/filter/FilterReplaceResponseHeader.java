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
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.filter;

import java.util.regex.Matcher;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


public class FilterReplaceResponseHeader extends FilterAbstractReplace {

    @Override
    public int getId() {
        return 70;
    }

    @Override
    public String getName() {
        return Constant.messages.getString("filter.replacerespheaser.name");
    }

    @Override
    public void onHttpRequestSend(HttpMessage msg) {

        
        
    }

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
