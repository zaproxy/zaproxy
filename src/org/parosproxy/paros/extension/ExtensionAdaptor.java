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
// ZAP: 2011/11/20 Support for extension factory
// ZAP: 2011/12/14 Support for extension dependencies

package org.parosproxy.paros.extension;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ExtensionAdaptor implements Extension {

    private String name = this.getClass().getName();
    private String description;
    private Model model = null;
    private ViewDelegate view = null;
    private ExtensionHook extensionHook = null;
    private int order = 0;
    private boolean enabled = true;
    
    public ExtensionAdaptor() {
    }

    public ExtensionAdaptor(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.plugin.Plugin#initPlugin()
     */
    public void init() {
      
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.plugin.Plugin#initModel(com.proofsecure.paros.model.Model)
     */
    public void initModel(Model model) {
        this.model = model;
    }
    
    public void initView(ViewDelegate view) {
        this.view = view;
    }
    
    public void initXML(Session session, OptionsParam options) {
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.plugin.Plugin#getPluginView(com.proofsecure.paros.view.View)
     */
    public ExtensionHookView getExtensionView() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.plugin.Plugin#initMenu()
     */
    public ExtensionHookMenu getExtensionMenu() {
        return null;
    }
    
    public void start() {
        
    }
    
    public void stop() {
        
    }

    public void destroy() {
    }
    
    public ViewDelegate getView() {
        return view;
    }
    
    public Model getModel() {
        return model;
    }

    public void hook(ExtensionHook extensionHook) {
        this.extensionHook = extensionHook;
    }

    @Override
    public boolean isDepreciated () {
    	return false;
    }

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getDescription() {
		if (this.description == null || this.description.length() == 0) {
			return this.getName();
		}
		return this.description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}
	
	public List<Class<?>> getDependencies() {
		return new ArrayList<Class<?>>();
	}


}
