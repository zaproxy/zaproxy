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

package org.parosproxy.paros.extension;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;

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
    
    String getName();
    
    String getDescription();
    
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
	 */
	void optionsLoaded();
	
	boolean canUnload();
	
	void unload();

}
