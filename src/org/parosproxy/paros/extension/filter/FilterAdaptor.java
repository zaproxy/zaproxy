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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
package org.parosproxy.paros.extension.filter;

import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;


public abstract class FilterAdaptor implements Filter {

    private boolean enabled = false;
    private ViewDelegate view = null;
    
    @Override
    public void init(Model model) {
    }
    
    @Override
    public void initView(ViewDelegate view) {
        this.view = view;
    }

    @Override
    public abstract int getId();
    
    @Override
    public abstract String getName();
    
    @Override
    public abstract void onHttpRequestSend(HttpMessage httpMessage);

    @Override
    public abstract void onHttpResponseReceive(HttpMessage httpMessage);
    
    /**
     * Filter to be destroyed when extension destroy
     *
     */
    @Override
    public void destroy() {
        
    }
    
    
    /**
     * @return Returns the enabled.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    /**
     * @param enabled The enabled to set.
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void timer() {
        
    }
    
    @Override
    public boolean isPropertyExists() {
        return false;
    }
    
    @Override
    public void editProperty() {
    }
    
    public ViewDelegate getView() {
        return view;
    }
}
