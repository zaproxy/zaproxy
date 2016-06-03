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
// ZAP: 2011/08/04 Changed to support new HttpPanel interface
// ZAP: 2012/04/26 Removed the method setStatus(String), no longer used.
// ZAP: 2012/07/23 Added method getSessionDialog() to expose functionality.
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/05/02 Removed redundant public modifiers from interface method declarations
// ZAP: 2016/03/22 Allow to remove ContextPanelFactory
// ZAP: 2016/04/14 Allow to display a message

package org.parosproxy.paros.extension;

import org.parosproxy.paros.view.MainFrame;
import org.parosproxy.paros.view.MainPopupMenu;
import org.parosproxy.paros.view.OutputPanel;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.view.ContextPanelFactory;

public interface ViewDelegate {

    MainFrame getMainFrame();
    
    SiteMapPanel getSiteTreePanel();
    
    OutputPanel getOutputPanel();
    
    // ZAP: expose dialog
    SessionDialog getSessionDialog();
    
    int showConfirmDialog(String msg);

    int showYesNoCancelDialog(String msg);
    
    void showWarningDialog(String msg);
    
    void showMessageDialog(String msg);
    
    WaitMessageDialog getWaitMessageDialog(String msg);
    
    MainPopupMenu getPopupMenu();
    
    // ZAP: Removed the method setStatus(String), no longer used.
    
    HttpPanelRequest getRequestPanel();
    
    HttpPanelResponse getResponsePanel();
    
    /**
     * Adds the given context panel factory to the view delegate.
     * <p>
     * The factory will be called whenever a panel is required for a context and notified when a context (or contexts) are no
     * longer needed.
     *
     * @param contextPanelFactory the context panel factory that should be added
     * @throws IllegalArgumentException if the context panel factory is {@code null}.
     * @see #removeContextPanelFactory(ContextPanelFactory)
     */
    void addContextPanelFactory (ContextPanelFactory contextPanelFactory);
    
    /**
     * Removes the given context panel factory from the view delegate, and any previously created panels for the contexts.
     *
     * @param contextPanelFactory the context panel factory that should be removed
     * @throws IllegalArgumentException if the context panel factory is {@code null}.
     * @since 2.5.0
     * @see #addContextPanelFactory(ContextPanelFactory)
     */
    void removeContextPanelFactory(ContextPanelFactory contextPanelFactory);

    /**
     * Displays the given {@code message} in the main message panels (Request/Response).
     * <p>
     * If the given {@code message} is {@code null} the panels are cleared.
     *
     * @param message the message to display
     * @since 2.5.0
     * @see #getRequestPanel()
     * @see #getResponsePanel()
     */
    void displayMessage(Message message);

}
