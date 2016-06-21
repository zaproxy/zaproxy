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
 // ZAP: 2012/04/25 Changed the type of the parameter "panel" of the method
 // addSessionPanel.
// ZAP: 2016/04/08 Allow to add ContextPanelFactory
package org.parosproxy.paros.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

public class ExtensionHookView {

    private Vector<AbstractPanel> workPanelList = new Vector<>();
    private Vector<AbstractPanel> statusPanelList = new Vector<>();
    private Vector<AbstractPanel> selectPanelList = new Vector<>();
    private Vector<AbstractParamPanel> sessionPanelList = new Vector<>();
    private Vector<AbstractParamPanel> optionPanelList = new Vector<>();
    
    /**
     * The {@link ContextPanelFactory}s added to this extension hook.
     * <p>
     * Lazily initialised.
     * 
     * @see #addContextPanelFactory(ContextPanelFactory)
     * @see #getContextPanelFactories()
     */
    private List<ContextPanelFactory> contextPanelFactories;

    public ExtensionHookView() {
    }
    
    public void addWorkPanel(AbstractPanel panel) {
        workPanelList.add(panel);
    }
    
    public void addSelectPanel(AbstractPanel panel) {
        selectPanelList.add(panel);
    }
    
    public void addStatusPanel(AbstractPanel panel) {
        statusPanelList.add(panel);
    }
    
    // ZAP: Changed the type of the parameter "panel" from AbstractPanel to
    // AbstractParamPanel.
    public void addSessionPanel(AbstractParamPanel panel) {
        sessionPanelList.add(panel);
    }
    
    public void addOptionPanel(AbstractParamPanel panel) {
        optionPanelList.add(panel);
    }
    
    List<AbstractPanel> getWorkPanel() {
        return workPanelList;
    }
        
    List<AbstractPanel> getSelectPanel() {
        return selectPanelList;
    }
    
    List<AbstractPanel> getStatusPanel() {
        return statusPanelList;
    }

    List<AbstractParamPanel> getSessionPanel() {
        return sessionPanelList;
    }
    
    List<AbstractParamPanel> getOptionsPanel() {
        return optionPanelList;
    }
    
    /**
     * Adds the given {@link ContextPanelFactory} to the view hook, to be later added to the
     * {@link org.parosproxy.paros.view.View View}.
     * <p>
     * By default, the {@code ContextPanelFactory}s added are removed from the {@code View} when the extension is unloaded.
     *
     * @param contextPanelFactory the {@code ContextPanelFactory} that will be added to the {@code View}
     * @since 2.5.0
     */
    public void addContextPanelFactory(ContextPanelFactory contextPanelFactory) {
        if (contextPanelFactories == null) {
            contextPanelFactories = new ArrayList<>();
        }
        contextPanelFactories.add(contextPanelFactory);
    }

    /**
     * Gets the {@link ContextPanelFactory}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code ContextPanelFactory}s, never {@code null}.
     * @since 2.5.0
     */
    List<ContextPanelFactory> getContextPanelFactories() {
        if (contextPanelFactories == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(contextPanelFactories);
    }
}
