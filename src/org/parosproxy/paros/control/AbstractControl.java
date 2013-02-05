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
package org.parosproxy.paros.control;

import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractControl {

    protected ExtensionLoader loader = null;
    protected Model model = null;
    protected View view = null;
    
    public AbstractControl(Model model, View view) {
        this.model = model;
        this.view = view;
    }
    
    public ExtensionLoader getExtensionLoader() {
        if (loader == null) {
            loader = new ExtensionLoader(model, view);
        }
        return loader;
    }
    
    protected void loadExtension() {
        addCommonExtension();
        addExtension();
        
        getExtensionLoader().startLifeCycle();

    }
    /**
     * Implemented by subclass to add specific plugin for the control.
     *
     */
    protected abstract void addExtension();
    

    /**
     * Add plugin common to all control here.  This is added before all other control specific plugin.
     *
     */
    protected void addCommonExtension() {

    }
    
    public void shutdown(boolean compact) {
	    getExtensionLoader().destroyAllExtension();
		model.getDb().close(compact);
		
    }
}
