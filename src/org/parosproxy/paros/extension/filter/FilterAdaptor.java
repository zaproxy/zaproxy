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
package org.parosproxy.paros.extension.filter;

import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
abstract public class FilterAdaptor implements Filter {

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
    abstract public int getId();
    
    @Override
    abstract public String getName();
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpRequestSend(com.proofsecure.paros.network.HttpMessage)
     */
    @Override
    abstract public void onHttpRequestSend(HttpMessage httpMessage);

    /* (non-Javadoc)
     * @see com.proofsecure.paros.core.proxy.ProxyListener#onHttpResponseReceive(com.proofsecure.paros.network.HttpMessage)
     */
    @Override
    abstract public void onHttpResponseReceive(HttpMessage httpMessage);
    
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
