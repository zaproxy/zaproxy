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
package org.parosproxy.paros.extension.filter;

import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface Filter {

    /**
     * @param model
     */
    public void init(Model model);
    
    public void initView(ViewDelegate view);

    public int getId();
    
    public String getName();
    
    public void onHttpRequestSend(HttpMessage httpMessage);

    public void onHttpResponseReceive(HttpMessage httpMessage);
    
    /**
     * Filter to be destroyed when extension destroy
     *
     */
    public void destroy();
    
    
    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled();
    
    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled);
    
    public void timer();
    
    public boolean isPropertyExists();
    
    public void editProperty();
}
