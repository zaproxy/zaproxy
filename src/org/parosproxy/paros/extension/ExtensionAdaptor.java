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
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/12/08 Issue 428: Added support for extension specific I18N bundles, to support the marketplace

package org.parosproxy.paros.extension;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

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
    private ResourceBundle messages = null;
    private String i18nPrefix = null;
    
    public ExtensionAdaptor() {
    }

    public ExtensionAdaptor(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    @Override
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
     * @see org.parosproxy.paros.plugin.Plugin#initPlugin()
     */
    @Override
    public void init() {
      
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.plugin.Plugin#initModel(org.parosproxy.paros.model.Model)
     */
    @Override
    public void initModel(Model model) {
        this.model = model;
    }
    
    @Override
    public void initView(ViewDelegate view) {
        this.view = view;
    }
    
    @Override
    public void initXML(Session session, OptionsParam options) {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.plugin.Plugin#getPluginView(org.parosproxy.paros.view.View)
     */
    public ExtensionHookView getExtensionView() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.plugin.Plugin#initMenu()
     */
    public ExtensionHookMenu getExtensionMenu() {
        return null;
    }
    
    @Override
    public void start() {
        
    }
    
    @Override
    public void stop() {
        
    }

    @Override
    public void destroy() {
    }
    
    @Override
    public ViewDelegate getView() {
        return view;
    }
    
    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        this.extensionHook = extensionHook;
    }

    @Override
    public boolean isDepreciated () {
    	return false;
    }

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
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
	
	@Override
	public List<Class<?>> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public boolean isCore() {
		return false;
	}
	
	@Override
	public URL getURL () {
		return null;
	}
	
	@Override
	public ResourceBundle getMessages() {
		return this.messages;
	}
	
	@Override
	public void setMessages(ResourceBundle messages) {
		this.messages = messages;
	}
	
	@Override
	public String getI18nPrefix () {
		if (this.i18nPrefix == null) {
			// Default to the (last part of the )name of the package 
			String packageName = this.getClass().getPackage().getName();
			this.i18nPrefix = packageName.substring(packageName.lastIndexOf(".") + 1);
		}
		return this.i18nPrefix;
	}

	@Override
	public void setI18nPrefix (String prefix) {
		this.i18nPrefix = prefix;
	}

	@Override
	public void optionsLoaded() {
	}

}
