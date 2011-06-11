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
package org.parosproxy.paros.extension;

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
    
    public String getName();
    
    /**
     * Initialize plugin during startup.  This phase is carried out before all others.
     */
    public void init();

    /**
     * Initialization of plugin after obtaining data model from core.  Method should not depend
     * on view.
     * @param model
     */
    public void initModel(Model model);

    public void initView(ViewDelegate view);
    
    public Model getModel();

    public ViewDelegate getView();
    
    /**
     * Start the plugin eg if there is a running server.
     *
     */
    public void start();

    /**
     * stop the plugin eg if there is a running server.
     */
    public void stop();
    
    /**
     * Plugin cleanup, finalize etc when program shutdown.  Stop() will be called before shutdown.
     */
    public void destroy();
    
    /**
     * Initialize session and options parameter if required XML node not in either files.
     * @param session
     * @param options
     */
    public void initXML(Session session, OptionsParam options);
    
    public void hook(ExtensionHook pluginHook);
    
}
