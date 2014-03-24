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
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Removed redundant public modifiers from interface method declarations
package org.parosproxy.paros.extension.filter;

import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;

public interface Filter {

    /**
     * @param model
     */
    void init(Model model);
    
    void initView(ViewDelegate view);
   
    int getId();
    
    String getName();
    
    void onHttpRequestSend(HttpMessage httpMessage);
   
    void onHttpResponseReceive(HttpMessage httpMessage);
    
    /**
     * Filter to be destroyed when extension destroy
     *
     */
    void destroy();
    
    
    /**
     * @return Returns the enabled.
     */
    boolean isEnabled();
    
    /**
     * @param enabled The enabled to set.
     */
    void setEnabled(boolean enabled);
    
    void timer();
    
    boolean isPropertyExists();
    
    void editProperty();
}
