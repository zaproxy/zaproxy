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
// ZAP: 2012/03/17 Issue 282 Added getAuthor() and getURL();
// ZAP: 2012/12/08 Issue 428: Added support for extension specific I18N bundles, to support the marketplace
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
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
import java.util.List;
import java.util.ResourceBundle;

import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.Version;
import org.zaproxy.zap.control.AddOn;

/**
 * Life cycle for an extension
 * .	init
 * .	initModel - the extension obtain the data model
 * .	initXML
 * .	getExtensionHook - workbench obtain the view of the plugin
 * .	start()
 * .	stop()
 * .	May go back to 5 depending on options or menu chosen.
 * .	destroy
 */
public interface Extension {
    
    /**
     * Returns the name of the extension, for configurations and access from other components (e.g. extensions).
     *
     * @return the name of the extension, never {@code null}
     * @see #getUIName()
     */
    String getName();
    
    /**
     * Returns a short descriptive name of the extension, to be shown in UI components. The name must be
     * internationalised.
     * 
     * @return the UI name of the extension, never {@code null}
     * @since 2.4.0
     * @see #getName()
     * @see #getDescription()
     */
    String getUIName();

    /**
     * Returns the description of the extension, to be shown in UI components. The description must be
     * internationalised.
     *
     * @return the description of the extension, never {@code null}
     */
    String getDescription();

    /**
     * Returns the (semantic) version of this extension, {@code null} if not versioned
     *
     * @return the version of the extension, or {@code null} if not versioned
     * @since 2.4.0
     */
    Version getVersion();
    
    /**
     * Initialize plugin during startup.  This phase is carried out before all others.
     */
    void init();

    /**
     * Initialization of plugin after obtaining data model from core.  Method should not depend
     * on view.
     * @param model
     */
    void initModel(Model model);

    void initView(ViewDelegate view);
    
    Model getModel();

    ViewDelegate getView();
    
    /**
     * Start the plugin eg if there is a running server.
     *
     */
    void start();

    /**
     * stop the plugin eg if there is a running server.
     */
    void stop();
    
    /**
     * Plugin cleanup, finalize etc when program shutdown.  Stop() will be called before shutdown.
     */
    void destroy();
    
    /**
     * Initialize session and options parameter if required XML node not in either files.
     * @param session
     * @param options
     */
    void initXML(Session session, OptionsParam options);
    
    void hook(ExtensionHook pluginHook);
    
    boolean isDepreciated ();
    
    int getOrder();
    
    void setOrder(int order);

	boolean isEnabled();
	
	void setEnabled(boolean enabled);
	
	List<Class<?>> getDependencies();
	
	boolean isCore ();
	
	String getAuthor ();
	
	URL getURL ();

	ResourceBundle getMessages();
	
	void setMessages(ResourceBundle messages);
	
	String getI18nPrefix ();

	void setI18nPrefix(String prefix);

	/**
	 * Called after the options for this extension have been loaded, so that the extension can make use of them.
	 * Note that other add-ons may not have been loaded at this point - if you need them to be then implement postInit()
	 * 
	 * @see #postInit()
	 */
	void optionsLoaded();
	
	boolean canUnload();
	
	void unload();

	/**
	 * Extensions should return the user friendly names of any unsaved resources - if there are any the user will be given the
	 * option not to exit ZAP or, if the extension is bundled in an add-on that's being updated or uninstalled, not to continue
	 * with the changes.
	 * 
	 * @return a {@code List} containing the unsaved resources or {@code null} if none
	 * @since 2.2.0
	 * @see #getActiveActions()
	 */
	List<String> getUnsavedResources();

	/**
	 * Returns the (internationalised) names of all active actions of the extension - if there are any the user will be given
	 * the option not to exit ZAP or, if the extension is bundled in an add-on that's being updated or uninstalled, not to
	 * continue with the changes.
	 * <p>
	 * An active action is something that's started by the user, for example, a scan.
	 * 
	 * @return a {@code List} containing the active actions or {@code null} if none
	 * @since 2.4.0
	 * @see #getUnsavedResources()
	 */
	List<String> getActiveActions();
	
	/**
	 * Implement this method to perform tasks after the add-on is installed.
	 * Note that this will only be called if the user adds the add-on via ZAP, eg file the File menu or the Marketplace.
	 * If the add-on is installed by copying the file to the plugins directory then it will not be called.
	 * 
	 * @since 2.3.0
	 * @see #postInit()
	 */
    void postInstall();

	/**
	 * Implement this method to perform tasks after all extensions/add-ons have been initialised.
	 * 
	 * @since 2.4.0
	 * @see #postInstall()
	 */
    void postInit();

    /**
     * Implement this method to register database tables to be used by the add-on 
     * @param dbServer
     * @throws DatabaseException
     * @throws DatabaseUnsupportedException
     */
    void databaseOpen(Database db) throws DatabaseException, DatabaseUnsupportedException;

    /**
     * Returns the add-on where this extension is bundled. Might be {@code null} if core extension.
     *
     * @return the add-on where this extension is bundled, or {@code null} if core extension.
     * @since 2.4.0
     */
    AddOn getAddOn();

    /**
     * Sets the add-on where this extension is bundled.
     * <p>
     * <strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param addOn the add-on where this extension is bundled
     * @since 2.4.0
     */
    void setAddOn(AddOn addOn);
    
    /**
     * Return true if the specified db type is supported by the extension (or if it doesnt use any db)
     * If this method returns false (meaning the db in use is not supported) then the extension will not be loaded.
     * @param type the db type
     * @return true if the specified db type is supported by the extension (or if it doesnt use any db)
     * @see org.parosproxy.paros.db.Database#getType()
     */
    boolean supportsDb(String type);

    /**
     * Return true it the extension can run with the 'low memory' option.
     * If the low memory option is set (and the extension supports it) then code should minimize the data stored in memory, 
     * using the db for all significant data.
     * Extensions that do not support the low memory option will not be run if the option is set.
     * @return
     */
    boolean supportsLowMemory();
}
