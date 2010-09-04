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

import org.parosproxy.paros.view.HttpPanel;
import org.parosproxy.paros.view.MainFrame;
import org.parosproxy.paros.view.MainPopupMenu;
import org.parosproxy.paros.view.OutputPanel;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.WaitMessageDialog;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ViewDelegate {

    public MainFrame getMainFrame();
    
    public SiteMapPanel getSiteTreePanel();
    
    public OutputPanel getOutputPanel();
    
    public int showConfirmDialog(String msg);

    public int showYesNoCancelDialog(String msg);
    
    public void showWarningDialog(String msg);
    
    public void showMessageDialog(String msg);
    
    public WaitMessageDialog getWaitMessageDialog(String msg);
    
    public MainPopupMenu getPopupMenu();
    
    public void setStatus(String msg);
    
    public HttpPanel getRequestPanel();
    
    public HttpPanel getResponsePanel();
    
}
