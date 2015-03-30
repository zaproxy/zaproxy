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
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/25 Added field hook and use it for unloading.
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/08/29 Issue 776: Allow add-ons to warn user if they're closing ZAP with unsaved resources open
// ZAP: 2014/02/28 Issue 1057: Add a Extension.postInstall() method for post install actions
// ZAP: 2015/01/04 Issue 1472: Allow extensions to specify a name for UI components
// ZAP: 2015/01/19 Issue 1510: New Extension.postInit() method to be called once all extensions loaded
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/02/10 Issue 1208: Search classes/resources in add-ons declared as dependencies
// ZAP: 2015/03/16 Issue 1525: Further database independence changes
// ZAP: 2015/03/30 Issue 1582: Enablers for low memory option

package org.parosproxy.paros.extension;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.Version;
import org.zaproxy.zap.control.AddOn;

public abstract class ExtensionAdaptor implements Extension {

    private String name = this.getClass().getName();
    private String description;
    private Model model = null;
    private ViewDelegate view = null;
    private int order = 0;
    private boolean enabled = true;
    private ResourceBundle messages = null;
    private String i18nPrefix = null;
	private ExtensionHook hook = null;
    
    /**
     * The add-on that this extension belongs too, might be {@code null} if core extension.
     */
    private AddOn addOn;

    /**
     * The version of the extension, might be {@code null} if not versioned.
     */
    private Version version;

    public ExtensionAdaptor() {
    }

    /**
     * Constructs an {@code ExtensionAdaptor} with the given {@code name}.
     *
     * @param name the name of the extension
     * @throws IllegalArgumentException if the given {@code name} is {@code null}.
     */
    public ExtensionAdaptor(String name) {
        this(name, null);
    }

    private static void validateNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            throw new IllegalArgumentException("Parameter \"" + parameterName + "\" must not be null.");
        }
    }

    protected ExtensionAdaptor(String name, Version version) {
        validateNotNull(name, "name");

        this.name = name;
        this.version = version;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the extension.
     * 
     * @param name the new name of the extension
     * @throws IllegalArgumentException if the given {@code name} is {@code null}.
     */
    public void setName(String name) {
        validateNotNull(name, "name");

        this.name = name;
    }

    @Override
    public Version getVersion() {
        return version; 
    }
    
    /**
     * By default returns the name returned by {@code getName()}.
     * 
     * @see #getName()
     */
    @Override
    public String getUIName() {
        return getName();
    }

    @Override
    public void init() {
      
    }

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

    public ExtensionHookView getExtensionView() {
        return null;
    }

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
    	this.hook  = extensionHook;
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
			return this.getUIName();
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
	
    @Override
	public boolean canUnload() {
    	return false;
    }
	
    /**
	 * Removes extension from ZAP. Override to undo things setup in
	 * {@link #hook(ExtensionHook)} method.
	 */
    @Override
	public void unload() {
		Control control = Control.getSingleton();
		ExtensionLoader extLoader = control.getExtensionLoader();

		extLoader.removeExtension(this, hook);
	}

    @Override
    public List<String> getUnsavedResources() {
    	return null;
    }

    @Override
    public List<String> getActiveActions() {
        return null;
    }
    
    @Override
    public void postInstall() {
    }

    @Override
    public void postInit() {
    }
    
    @Override
    public void databaseOpen(Database db) throws DatabaseException, DatabaseUnsupportedException {
    }

    @Override
    public AddOn getAddOn() {
        return addOn;
    }

    @Override
    public void setAddOn(AddOn addOn) {
        if (this.addOn != addOn) {
            this.addOn = addOn;

            if (this.addOn != null) {
                this.addOn.addLoadedExtension(this);
            }
        }
    }

    @Override
    public boolean supportsDb(String type) {
    	return Database.DB_TYPE_HSQLDB.equals(type);
    }

    @Override
    public boolean supportsLowMemory() {
    	return false;
    }

}
