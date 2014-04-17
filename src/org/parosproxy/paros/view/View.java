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
// ZAP: 2012/02/18 Changed default to be 'bottom full'
// ZAP: 2012/03/15 Changed to set the configuration key to the HttpPanels, load
// the configuration and disable the response panel.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/26 Removed the method setStatus(String), no longer used.
// ZAP: 2012/07/02 HttpPanelRequest and -Response constructor changed.
// ZAP: 2012/07/23 Removed title parameter in method getSessionDialog().
// Added @Override to getSessionDialog() as exposed in ViewDelegate interface.
// ZAP: 2012/07/29 Issue 43: Added support for Scope
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/07 Removed the unused method changeDisplayOption(int)
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2012/10/03 Issue 388: Added support for technologies
// ZAP: 2012/12/18 Issue 441: Prevent view being initialised in daemon mode
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons - added helper methods
// ZAP: 2013/02/17 Issue 496: Allow to see the request and response at the same 
// time in the main window
// ZAP: 2013/02/26 Issue 540: Maximised work tabs hidden when response tab
// position changed
// ZAP: 2013/04/15 Issue 627: Allow add-ons to remove main tool bar buttons/separators
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/08/21 Support for shared context for Context Properties Panels.
// ZAP: 2013/12/13 Disabled the updating of 'Sites' tab, because it has been added elsewhere to accomodate the 'Full Layout' functionality.
// ZAP: 2014/01/06 Issue 965: Support 'single page' apps and 'non standard' parameter separators
// ZAP: 2014/01/19 Added option to execute code after init of the panels when showing the session dialog
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/03/23 Issue 1085: Do not add/remove pop up menu items through the method View#getPopupMenu()

package org.parosproxy.paros.view;


import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.keyboard.ExtensionKeyboard;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextExcludePanel;
import org.zaproxy.zap.view.ContextGeneralPanel;
import org.zaproxy.zap.view.ContextIncludePanel;
import org.zaproxy.zap.view.ContextListPanel;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.ContextTechnologyPanel;
import org.zaproxy.zap.view.MessagePanelsPositionController;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;
import org.zaproxy.zap.view.SessionExcludeFromScanPanel;
import org.zaproxy.zap.view.SessionExcludeFromSpiderPanel;
import org.zaproxy.zap.view.ContextStructurePanel;
import org.zaproxy.zap.view.TabbedPanel2;
import org.zaproxy.zap.view.ZapMenuItem;

public class View implements ViewDelegate {
	
	public static final int DISPLAY_OPTION_LEFT_FULL = 0;
	public static final int DISPLAY_OPTION_BOTTOM_FULL = 1;
	public static final int DISPLAY_OPTION_TOP_FULL = 2;

	public static final int DISPLAY_OPTION_ICONNAMES = 0;
	public static final int DISPLAY_OPTION_ONLYICONS = 1;

	private static View view = null;
	private static boolean daemon = false;
	
  // private FindDialog findDialog = null;
	private SessionDialog sessionDialog = null;
	private OptionsDialog optionsDialog = null;
	
	//private LogPanel logPanel = null;
	private MainFrame mainFrame = null;
	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;
	private SiteMapPanel siteMapPanel  = null;
	private OutputPanel outputPanel = null;
	private Vector<JMenuItem> popupList = new Vector<>();

	private JMenu menuShowTabs = null;

  private List<AbstractParamPanel> contextPanels = new ArrayList<>();
  private List<ContextPanelFactory> contextPanelFactories = new ArrayList<>();

	private static int displayOption = DISPLAY_OPTION_BOTTOM_FULL;

  private static final Logger logger = Logger.getLogger(View.class);

  private MessagePanelsPositionController messagePanelsPositionController;

	/**
	 * @return Returns the mainFrame.
	 */
	@Override
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	///**
	// * @return Returns the requestPanel.
	// */
	//public HttpPanel getRequestPanel() {
	//	return requestPanel;
	//}
	///**
	// * @return Returns the responsePanel.
	// */
	//public HttpPanel getResponsePanel() {
	//	return responsePanel;
	//}

  /**
   * Sets the displayOption.
   */
	public static void setDisplayOption(int displayOption) {
		View.displayOption = displayOption;
	}

  /**
   * Return the current displayOption.
   */
  public static int getDisplayOption() {
    return View.displayOption;
  }

	
//  ZAP: Removed method changeDisplayOption(int)
	
	public void init() {
		mainFrame = new MainFrame(displayOption);
    ExtensionHelp.enableHelpKey(getOutputPanel(), "ui.tabs.output");
		
		getWorkbench().getTabbedWork().setAlternativeParent(mainFrame.getPaneDisplay());
		getWorkbench().getTabbedStatus().setAlternativeParent(mainFrame.getPaneDisplay());
		getWorkbench().getTabbedSelect().setAlternativeParent(mainFrame.getPaneDisplay());

    // adds the Request/Response representation buttons in ZAP toolbar
	  getMessagePanelsPositionController().restoreState();
	}
	
	public void postInit() {
      // Note: addTab function calls have been moved to WorkbenchPanel.java because
      // of the Full Layout support, but this line is still needed for the 'History'
      // tab to be the currently selected tab; otherwise it's the 'Output' tab.
	    getWorkbench().getTabbedStatus().addTab(getOutputPanel().getName(), getOutputPanel().getIcon(), getOutputPanel(), false);

      // restore the state of Request/Response layout: side by side or above each other.
	    getMessagePanelsPositionController().restoreState();
	    
	    refreshTabViewMenus();
	}

  /**
   * Retursn the MessagePanelsPositionController so other classes can also restore
   * state of the Request/Response layout.
   */
  public MessagePanelsPositionController getMessagePanelsPositionController() {
    if(messagePanelsPositionController == null) {
       messagePanelsPositionController = new MessagePanelsPositionController(
		        getRequestPanel(),
		        getResponsePanel(),
		        mainFrame,
		        getWorkbench());
    }
    return messagePanelsPositionController;
  }

	public void refreshTabViewMenus() {
		if (menuShowTabs != null) {
			// Remove the old ones
			mainFrame.getMainMenuBar().getMenuView().remove(menuShowTabs);
		}
		menuShowTabs = new JMenu(Constant.messages.getString("menu.view.showtab"));
		mainFrame.getMainMenuBar().getMenuView().add(menuShowTabs );
		
		ExtensionKeyboard extKey = (ExtensionKeyboard) 
				Control.getSingleton().getExtensionLoader().getExtension(ExtensionKeyboard.NAME);
		
	    for (Component tab : getWorkbench().getTabbedSelect().getTabList()) {
    		registerMenu(extKey, getWorkbench().getTabbedSelect(), tab);
	    }
    	menuShowTabs.addSeparator();
	    for (Component tab : getWorkbench().getTabbedWork().getTabList()) {
    		registerMenu(extKey, getWorkbench().getTabbedWork(), tab);
	    }
    	menuShowTabs.addSeparator();
	    for (Component tab : getWorkbench().getTabbedStatus().getTabList()) {
    		registerMenu(extKey, getWorkbench().getTabbedStatus(), tab);
	    }
	}
	
	private void registerMenu(ExtensionKeyboard extKey, final TabbedPanel2 parent, final Component tab) {
    	if (tab instanceof AbstractPanel) {
    		final AbstractPanel ap = (AbstractPanel)tab;
        	ZapMenuItem tabMenu = new ZapMenuItem(
        			tab.getClass().getName(), MessageFormat.format(Constant.messages.getString("menu.view.tab"), tab.getName()), 
        			ap.getDefaultAccelerator());
        	tabMenu.setMnemonic(ap.getMnemonic());
    		if (ap.getIcon() != null) {
    			tabMenu.setIcon(ap.getIcon());
    		}
    		tabMenu.addActionListener(new ActionListener() {
    			@Override
    			public void actionPerformed(ActionEvent e) {
    				parent.setVisible(tab, true);
    				ap.setTabFocus();
    			}});
    		
    		menuShowTabs.add(tabMenu);
    		if (extKey != null) {
    			extKey.registerMenuItem(tabMenu);
    		}

    	}
	}

	@Override
	public int showConfirmDialog(String msg) {
		return JOptionPane.showConfirmDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
	}
	
	@Override
	public int showYesNoCancelDialog(String msg) {
		return JOptionPane.showConfirmDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	@Override
	public void showWarningDialog(String msg) {
		JOptionPane.showMessageDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void showMessageDialog(String msg) {
		JOptionPane.showMessageDialog(getMainFrame(), msg, Constant.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public int showConfirmDialog(JFrame parent, String msg) {
		return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
	}
	
	public int showYesNoCancelDialog(JFrame parent, String msg) {
		return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
	}
	
	public void showWarningDialog(JFrame parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
	}

	public void showMessageDialog(JFrame parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
	}
	
	// ZAP: FindBugs fix - make method synchronised
	public static synchronized View getSingleton() {
		if (view == null) {
			if (daemon) {
				Exception e = new Exception("Attempting to initialise View in daemon mode");
				logger.error(e.getMessage(), e);
				return null;
			}
			logger.info("Initialising View");
			view = new View();
			view.init();
		}
		return view;
	}
	
	public static boolean isInitialised() {
		return view != null;
	}
	
	public static void setDaemon(boolean daemon) {
		View.daemon = daemon;
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
    @Override
    public SiteMapPanel getSiteTreePanel() {
      if(siteMapPanel == null) {
		    siteMapPanel = new SiteMapPanel();
      }
      return siteMapPanel;
    }
    
    @Override
    public OutputPanel getOutputPanel() {
        if(outputPanel == null) {
          outputPanel = new OutputPanel();
        }
        return outputPanel;
    }

    @Override
    public HttpPanelRequest getRequestPanel() {
        if (requestPanel == null) {
        	// ZAP: constructor changed
            requestPanel = new HttpPanelRequest(false, OptionsParamView.BASE_VIEW_KEY + ".main.");
    		// ZAP: Added 'right arrow' icon
    		requestPanel.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/105.png")));
            requestPanel.setName(Constant.messages.getString("http.panel.request.title"));	// ZAP: i18n
            requestPanel.setEnableViewSelect(true);
            requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
            requestPanel.setDefaultAccelerator(KeyStroke.getKeyStroke(
            		KeyEvent.VK_R, Event.CTRL_MASK | Event.SHIFT_MASK, false));
            requestPanel.setMnemonic(Constant.messages.getChar("http.panel.request.mnemonic"));

        }
        return requestPanel;
    }
    
    @Override
    public HttpPanelResponse getResponsePanel() {
        if (responsePanel == null) {
        	// ZAP: constructor changed
            responsePanel = new HttpPanelResponse(false, OptionsParamView.BASE_VIEW_KEY + ".main.");
    		// ZAP: Added 'left arrow' icon
            responsePanel.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/106.png")));
            responsePanel.setName(Constant.messages.getString("http.panel.response.title"));	// ZAP: i18n
            responsePanel.setEnableViewSelect(false);
            responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
            responsePanel.setDefaultAccelerator(KeyStroke.getKeyStroke(
            		KeyEvent.VK_R, Event.CTRL_MASK | Event.ALT_MASK | Event.SHIFT_MASK, false));
            responsePanel.setMnemonic(Constant.messages.getChar("http.panel.response.mnemonic"));
        }
        return responsePanel;
    }

	@Override    
    public SessionDialog getSessionDialog() {
        if (sessionDialog == null) {
            String[] ROOT = {};
        	// ZAP: i18n, plus in-lined title parameter
        	String propertiesTitle = Constant.messages.getString("session.properties.title");
        	String dialogTitle = Constant.messages.getString("session.dialog.title");
            sessionDialog = new SessionDialog(getMainFrame(), true, propertiesTitle, dialogTitle);
            sessionDialog.addParamPanel(ROOT, new SessionGeneralPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromProxyPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromScanPanel(), false);
            sessionDialog.addParamPanel(ROOT, new SessionExcludeFromSpiderPanel(), false);
            sessionDialog.addParamPanel(ROOT, new ContextListPanel(), false);
        }
        
        return sessionDialog;
    }
	
	public void showSessionDialog(Session session, String panel){
		showSessionDialog(session, panel, true, null);
	}

	public void showSessionDialog(Session session, String panel, boolean recreateUISharedContexts){
		showSessionDialog(session, panel, recreateUISharedContexts, null);
	}
	
    /**
     * Shows the session properties dialog. If a panel is specified, the dialog is opened showing that panel.
     * If {@code recreateUISharedContexts} is {@code true}, any old UI shared contexts are discarded and
     * new ones are created as copies of the contexts. If a {@code postInitRunnable} is provided, its {@link Runnable#run} method
     * is called after the initialization of all the panels of the session properties dialog.
     *
     * @param session the session
     * @param panel the panel name to be shown
     * @param recreateUISharedContexts if true, any old UI shared contexts are discarded and
     * new ones are created as copies of the contexts
     * @param postInitRunnable if provided, its {@link Runnable#run} method
     * is called after the initialization of all the panels of the session properties dialog.
     */
    public void showSessionDialog(Session session, String panel, boolean recreateUISharedContexts, Runnable postInitRunnable) {
    	if (sessionDialog != null) {
    		if(recreateUISharedContexts)
    			sessionDialog.recreateUISharedContexts(session);
    		sessionDialog.initParam(session);
    		if(postInitRunnable!=null)
    			postInitRunnable.run();
    		sessionDialog.setTitle(Constant.messages.getString("session.properties.title"));
    		sessionDialog.showDialog(false, panel);
    	}
    }
    
    public void addContext(Context c) {
    	ContextGeneralPanel contextGenPanel = new ContextGeneralPanel(c.getName(), c.getIndex());
    	contextGenPanel.setSessionDialog(getSessionDialog());
		getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list")}, contextGenPanel, false);
		this.contextPanels.add(contextGenPanel);
		
		ContextIncludePanel contextIncPanel = new ContextIncludePanel(c);
		contextIncPanel.setSessionDialog(getSessionDialog());
		getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list"), contextGenPanel.getName()}, contextIncPanel, false);
		this.contextPanels.add(contextIncPanel);
		
		ContextExcludePanel contextExcPanel = new ContextExcludePanel(c);
		contextExcPanel.setSessionDialog(getSessionDialog());
		getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list"), contextGenPanel.getName()}, contextExcPanel, false);
		this.contextPanels.add(contextExcPanel);
		
		ContextStructurePanel contextStructPanel = new ContextStructurePanel(c);
		contextStructPanel.setSessionDialog(getSessionDialog());
		getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list"), contextGenPanel.getName()}, contextStructPanel, false);
		this.contextPanels.add(contextStructPanel);
		
		ContextTechnologyPanel contextTechPanel = new ContextTechnologyPanel(c);
		contextTechPanel.setSessionDialog(getSessionDialog());
		getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list"), contextGenPanel.getName()}, contextTechPanel, false);
		this.contextPanels.add(contextTechPanel);

		for (ContextPanelFactory cpf : this.contextPanelFactories) {
			AbstractContextPropertiesPanel panel = cpf.getContextPanel(c);
			panel.setSessionDialog(getSessionDialog());
			getSessionDialog().addParamPanel(new String[]{Constant.messages.getString("context.list"), contextGenPanel.getName()}, panel, false);
			this.contextPanels.add(panel);
		}
    }
    
    public void renameContext(Context c) {
    	for (AbstractParamPanel panel : contextPanels) {
    		if (panel instanceof ContextGeneralPanel) {
    			ContextGeneralPanel ctxPanel = (ContextGeneralPanel) panel;
    			if (ctxPanel.getContextIndex() == c.getIndex()) {
    				getSessionDialog().renamePanel(ctxPanel, c.getName());
    				break;
    			}
    		}
    	}
    }
    
    @Override
    public void addContextPanelFactory(ContextPanelFactory cpf) {
    	this.contextPanelFactories.add(cpf);
    }
    
	public void discardContexts() {
		for (AbstractParamPanel panel : contextPanels) {
			getSessionDialog().removeParamPanel(panel);
		}
		for (ContextPanelFactory cpf : this.contextPanelFactories) {
			cpf.discardContexts();
		}
		
		contextPanels.clear();
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
    
    // ZAP: Removed the method setStatus(String), no longer used.
    
    /**
     * Returns a new {@code MainPopupMenu} instance with the pop pup menu items returned by the method {@code getPopupList()}.
     * <p>
     * <strong>Note:</strong> Pop up menu items ({@code JMenuItem}, {@code JMenu}, {@code ExtensionPopupMenuItem} and
     * {@code ExtensionPopupMenu}) should be added/removed to/from the list returned by the method {@code getPopupList()} not by
     * calling the methods {@code MainPopupMenu#addMenu(...)} on the returned {@code MainPopupMenu} instance. Adding pop up menu
     * items to the returned {@code MainPopupMenu} instance relies on current implementation of {@code MainPopupMenu} which may
     * change without notice (moreover a new instance is created each time the method is called).
     * </p>
     * 
     * @return a {@code MainPopupMenu} containing the pop up menu items that are in the list returned by the method
     *         {@code getPopupList()}.
     * @see #getPopupList()
     * @see MainPopupMenu
     * @see ExtensionPopupMenu
     * @see ExtensionPopupMenuItem
     */
    @Override
    public MainPopupMenu getPopupMenu() {
        MainPopupMenu popup = new MainPopupMenu(popupList, this);
        return popup;
    }
    
    /**
     * Returns the list of pop up menu items that will have the {@code MainPopupMenu} instance returned by the method
     * {@code getPopupMenu()}.
     * <p>
     * Should be used to dynamically add/remove pop up menu items ({@code JMenuItem}, {@code JMenu},
     * {@code ExtensionPopupMenuItem} and {@code ExtensionPopupMenu}) to the main pop up menu at runtime.
     * </p>
     * 
     * @return the list of pop up menu items that will have the main pop up menu.
     * @see #getPopupMenu()
     * @see ExtensionHookMenu#addPopupMenuItem(ExtensionPopupMenu)
     * @see ExtensionHookMenu#addPopupMenuItem(ExtensionPopupMenuItem)
     * @see MainPopupMenu
     * @see ExtensionPopupMenu
     * @see ExtensionPopupMenuItem
     */
    public Vector<JMenuItem> getPopupList() {
        return popupList;
    }
    
    @Override
    public WaitMessageDialog getWaitMessageDialog(String s) {
        WaitMessageDialog dialog = new WaitMessageDialog(getMainFrame(), true);
        dialog.setText(s);
        dialog.centreDialog();
        return dialog;
    }

    public WaitMessageDialog getWaitMessageDialog(JFrame parent, String s) {
        WaitMessageDialog dialog = new WaitMessageDialog(parent, true);
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

    public void removeMainToolbarButton (JButton button) {
        this.getMainFrame().getMainToolbarPanel().removeButton(button);
    }

    public void removeMainToolbarButton(JToggleButton button) {
        this.getMainFrame().getMainToolbarPanel().removeButton(button);
    }

    public void addMainToolbarSeparator (JToolBar.Separator separator) {
        this.getMainFrame().getMainToolbarPanel().addSeparator(separator);
    }

    public void removeMainToolbarSeparator(JToolBar.Separator separator) {
        this.getMainFrame().getMainToolbarPanel().remove(separator);
    }
}
