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

import java.util.List;
import java.util.Vector;

import org.parosproxy.paros.view.AbstractParamPanel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionHookView {

    private Vector workPanelList = new Vector();
    private Vector statusPanelList = new Vector();
    private Vector selectPanelList = new Vector();
    private Vector sessionPanelList = new Vector();
    private Vector optionPanelList = new Vector();
    
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
    
    public void addSessionPanel(AbstractPanel panel) {
        sessionPanelList.add(panel);
    }
    
    public void addOptionPanel(AbstractParamPanel panel) {
        optionPanelList.add(panel);
    }
    
    List getWorkPanel() {
        return workPanelList;
    }
        
    List getSelectPanel() {
        return selectPanelList;
    }
    
    List getStatusPanel() {
        return statusPanelList;
    }

    List getSessionPanel() {
        return sessionPanelList;
    }
    
    List getOptionsPanel() {
        return optionPanelList;
    }
    
}
