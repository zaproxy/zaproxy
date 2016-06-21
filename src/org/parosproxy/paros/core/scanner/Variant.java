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
// ZAP: 2013/07/02 Changed Vector to generic List
// ZAP: 2013/07/02 Changed API to public for future extensible Variant model
// ZAP: 2016/05/04 Add JavaDoc to getParamList()
package org.parosproxy.paros.core.scanner;

import java.util.List;

import org.parosproxy.paros.network.HttpMessage;

public interface Variant {

    public void setMessage(HttpMessage msg);

    /**
     * Gets the list of parameters handled by this variant.
     * 
     * @return a {@code List} containing the parameters
     */
    public List<NameValuePair> getParamList();

    public String setParameter(HttpMessage msg, NameValuePair originalPair, String param, String value);
    public String setEscapedParameter(HttpMessage msg, NameValuePair originalPair, String param, String value);
    
}
