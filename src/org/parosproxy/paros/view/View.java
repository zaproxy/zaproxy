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
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/05/31 Added option to dynamically change the display

package org.parosproxy.paros.view;


import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;
import org.zaproxy.zap.view.SessionExcludeFromScanPanel;
import org.zaproxy.zap.view.SessionExcludeFromSpiderPanel;

public class View implements ViewDelegate {
	
	public static final int DISPLAY_OPTION_LEFT_FULL = 0;
	public static final int DISPLAY_OPTION_BOTTOM_FULL = 1;
	
	private static View view = null;
//	private FindDialog findDialog = null;
	private SessionDialog sessionDialog = null;
	private OptionsDialog optionsDialog = null;
	
	//private LogPanel logPanel = null;
	private MainFrame mainFrame = null;
	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;
	private SiteMapPanel siteMapPanel  = null;
	private OutputPanel outputPanel = null;
	private Vector<JMenuItem> popupList = new Vector<JMenuItem>();
	
	private static int displayOption = DISPLAY_OPTION_LEFT_FULL;
	
	/**
	 * @return Returns the mainFrame.
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	/**
	 * @return Returns the requestPanel.
	 */
	//public HttpPanel getRequestPanel() {
	//	return requestPanel;
	//}
	/**
	 * @return Returns the responsePanel.
	 */
	//public HttpPanel getResponsePanel() {
	//	return responsePanel;
	//}

	public static void setDisplayOption(int displayOption) {
		View.displayOption = displayOption;
	}
	
	public void changeDisplayOption(int displayOption) {
		View.displayOption = displayOption;
		mainFrame.changeDisplayOption(displayOption);
	}
	
	public void init() {
		mainFrame = new MainFrame(displayOption);

		siteMapPanel = new SiteMapPanel();
		outputPanel = new OutputPanel();

        ExtensionHelp.enableHelpKey(outputPanel, "ui.tabs.output");

		// do not allow editable in request panel
		getWorkbench().getTabbedWork().addTab(getRequestPanel().getName(), getRequestPanel().getIcon(), getRequestPanel());
		getWorkbench().getTabbedWork().addTab(getResponsePanel().getName(), getResponsePanel().getIcon(), getResponsePanel());
		
		//logPanel.setDisplayPanel(requestPanel, responsePanel);
		//getWorkbench().getTabbedStatus().add(logPanel, "URLs");
		
		// ZAP: Added 'world' icon
		Icon icon = new ImageIcon(getClass().getResource("/resource/icon/16/094.png"));
		getWorkbench().getTabbedSelect().addTab(Constant.messages.getString("sites.panel.title"), icon, siteMapPanel); // ZAP: i18n
		
		getWorkbench().getTabbedWork().setAlternativeParent(mainFrame.getPaneDisplay());
		getWorkbench().getTabbedStatus().setAlternativeParent(mainFrame.getPaneDisplay());
		getWorkbench().getTabbedSelect().setAlternativeParent(mainFrame.getPaneDisplay());

	}
	
	public void postInit() {
	    getWorkbench().getTabbedStatus().add(outputPanel);
	    
	}
	
	public int showConfirmDialog(String msg) {
		return JOptionPane.showConfirmDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
	}
	
	public int showYesNoCancelDialog(String msg) {
		return JOptionPane.showConfirmDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	public void showWarningDialog(String msg) {
		JOptionPane.showMessageDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
	}

	public void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
	}
	
	// ZAP: FindBugs fix - make method synchronised
	public static  synchronized View getSingleton() {
		if (view == null) {
			view = new View();
			view.init();
		}
		return view;
	}
	
	public static boolean isInitialised() {
		return view != null;
	}
	
//	public void showFindDialog() {
//	    if (findDialog == null) {
//	        findDialog = new FindDialog(mainFrame, false);
//	    }
//	    
//	    findDialog.setVisible(true);
//	}
	
    /**
     * @return Returns the siteTreePanel.
     */
    public SiteMapPanel getSiteTreePanel() {
        return siteMapPanel;
    }
    
    public OutputPanel getOutputPanel() {
        return outputPanel;
    }

    public HttpPanelRequest getRequestPanel() {
        if (requestPanel == null) {
            requestPanel = new HttpPanelRequest(false, null);
    		// ZAP: Added 'right arrow' icon
    		requestPanel.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/105.png")));
            requestPanel.setName(Constant.messages.getString("request.panel.title"));	// ZAP: i18n
        }
        return requestPanel;
    }
    
    public HttpPanelResponse getResponsePanel() {
        if (responsePanel == null) {
            responsePanel = new HttpPanelResponse(false, null);
    		// ZAP: Added 'left arrow' icon
            responsePanel.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/106.png")));
            responsePanel.setName(Constant.messages.getString("response.panel.title"));	// ZAP: i18n
            responsePanel.clearView(false);
        }
        return responsePanel;
    }
    
    public SessionDialog getSessionDialog(String title) {
        String[] ROOT = {};
        if (sessionDialog == null) {
            sessionDialog = new SessionDialog(getMainFrame(), true, title, Constant.messages.getString("session.dialog.title"));	// ZAP: i18n
            sessionDialog.setTitle(Constant.messages.getString("session.properties.title"));
            sessionDialog.addParamPanel(ROOT, new SessionGeneralPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromProxyPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromScanPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromSpiderPanel(), false);
        }
        
        return sessionDialog;
    }
    
    public void showSessionDialog(Session session, String panel) {
    	if (sessionDialog != null) {
    		sessionDialog.initParam(session);
    		sessionDialog.setTitle(Constant.messages.getString("session.properties.title"));
    		sessionDialog.showDialog(false, panel);
    	}
    }
    
    public OptionsDialog getOptionsDialog(String title) {
		// ZAP: FindBugs fix - dont need ROOT
        //String[] ROOT = {};
        if (optionsDialog == null) {
            optionsDialog = new OptionsDialog(getMainFrame(), true, title);
        }
        
        optionsDialog.setTitle(title);
        return optionsDialog;
    }
    
    public WorkbenchPanel getWorkbench() {
        return mainFrame.getWorkbench();
    }
    
    public void setStatus(String msg) {
        if (msg == null || msg.equals("")) {
            msg = " ";
        }
        mainFrame.setStatus(msg);
    }
    
    public MainPopupMenu getPopupMenu() {
        MainPopupMenu popup = new MainPopupMenu(popupList);
        return popup;
    }
    
    public Vector<JMenuItem> getPopupList() {
        return popupList;
    }
    
    public WaitMessageDialog getWaitMessageDialog(String s) {
        WaitMessageDialog dialog = new WaitMessageDialog(getMainFrame(), true);
        dialog.setText(s);
        dialog.centreDialog();
        return dialog;
    }

    // ZAP: Added main toolbar mathods
    public void addMainToolbarButton (JButton button) {
    	this.getMainFrame().getMainToolbarPanel().addButton(button);
    }
    
    public void addMainToolbarSeparator () {
    	this.getMainFrame().getMainToolbarPanel().addSeparator();
    }
	public void addMainToolbarButton(JToggleButton button) {
    	this.getMainFrame().getMainToolbarPanel().addButton(button);
	}

}