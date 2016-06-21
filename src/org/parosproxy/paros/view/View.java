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
// ZAP: 2014/04/17 Issue 1155: Historical Request Tab Doesn't allow formatting changes
// ZAP: 2014/07/15 Issue 1265: Context import and export
// ZAP: 2014/09/22 Issue 1345: Support Attack mode
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2014/10/24 Issue 1378: Revamp active scan panel
// ZAP: 2014/10/31 Issue 1176: Changed parents to Window as part of spider advanced dialog changes
// ZAP: 2014/11/23 Added Splash Screen management
// ZAP: 2014/12/22 Issue 1476: Display contexts in the Sites tree
// ZAP: 2015/01/19 Expose splash screen as Component
// ZAP: 2015/02/02 Move output panel help key registration to prevent NPE
// ZAP: 2015/03/04 Added no prompt warning methods
// ZAP: 2015/04/13 Add default editor and renderer for TextMessageLocationHighlight
// ZAP: 2015/08/11 Fix the removal of context panels
// ZAP: 2015/09/07 Start GUI on EDT
// ZAP: 2015/11/26 Issue 2084: Warn users if they are probably using out of date versions
// ZAP: 2016/03/16 Add StatusUI handling
// ZAP: 2016/03/22 Allow to remove ContextPanelFactory
// ZAP: 2016/03/23 Issue 2331: Custom Context Panels not show in existing contexts after installation of add-on
// ZAP: 2016/04/04 Do not require a restart to show/hide the tool bar
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2016/04/14 Allow to display a message

package org.parosproxy.paros.view;

import java.awt.Component;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOn.Status;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.keyboard.ExtensionKeyboard;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextExcludePanel;
import org.zaproxy.zap.view.ContextGeneralPanel;
import org.zaproxy.zap.view.ContextIncludePanel;
import org.zaproxy.zap.view.ContextListPanel;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.ContextStructurePanel;
import org.zaproxy.zap.view.ContextTechnologyPanel;
import org.zaproxy.zap.view.SessionExcludeFromProxyPanel;
import org.zaproxy.zap.view.SessionExcludeFromScanPanel;
import org.zaproxy.zap.view.SessionExcludeFromSpiderPanel;
import org.zaproxy.zap.view.SplashScreen;
import org.zaproxy.zap.view.StatusUI;
import org.zaproxy.zap.view.ZapMenuItem;
import org.zaproxy.zap.view.messagelocation.MessageLocationHighlightRenderersEditors;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlight;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlightEditor;
import org.zaproxy.zap.view.messagelocation.TextMessageLocationHighlightRenderer;

public class View implements ViewDelegate {

    /**
     * @deprecated (2.5.0) Use {@link WorkbenchPanel.Layout#EXPAND_SELECT} instead.
     * @see #getMainFrame()
     * @see MainFrame#setWorkbenchLayout(org.parosproxy.paros.view.WorkbenchPanel.Layout)
     */
    @Deprecated
    public static final int DISPLAY_OPTION_LEFT_FULL = 0;
    /**
     * @deprecated (2.5.0) Use {@link WorkbenchPanel.Layout#EXPAND_STATUS} instead.
     * @see #getMainFrame()
     * @see MainFrame#setWorkbenchLayout(org.parosproxy.paros.view.WorkbenchPanel.Layout)
     */
    @Deprecated
    public static final int DISPLAY_OPTION_BOTTOM_FULL = 1;
    /**
     * @deprecated (2.5.0) Use {@link WorkbenchPanel.Layout#FULL} instead.
     * @see #getMainFrame()
     * @see MainFrame#setWorkbenchLayout(org.parosproxy.paros.view.WorkbenchPanel.Layout)
     */
    @Deprecated
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
    private SiteMapPanel siteMapPanel = null;
    private OutputPanel outputPanel = null;
    private Vector<JMenuItem> popupList = new Vector<>();

    private JMenu menuShowTabs = null;

    private JCheckBox rememberCheckbox = null;
    private JCheckBox dontPromptCheckbox = null;

    private List<AbstractContextPropertiesPanel> contextPanels = new ArrayList<>();
    private List<ContextPanelFactory> contextPanelFactories = new ArrayList<>();
    /**
     * A map containing the {@link AbstractContextPropertiesPanel context panels} created by a {@link ContextPanelFactory
     * context panel factory}, being the latter the key and the former the value (a {@code List} with the panels).
     * <p>
     * The map is used to remove the panels created when the factory is removed.
     */
    private Map<ContextPanelFactory, List<AbstractContextPropertiesPanel>> contextPanelFactoriesPanels = new HashMap<>();

    private static final Logger logger = Logger.getLogger(View.class);

    // ZAP: splash screen
    private SplashScreen splashScreen = null;

    private Map<AddOn.Status, StatusUI> statusMap = new HashMap<>();

    private boolean postInitialisation;

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
     * @deprecated (2.5.0) Use {@link MainFrame#setWorkbenchLayout(org.parosproxy.paros.view.WorkbenchPanel.Layout)}
     *             instead.
     * @see #getMainFrame()
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    public static void setDisplayOption(int displayOption) {
        View.getSingleton().getMainFrame().setWorkbenchLayout(WorkbenchPanel.Layout.getLayout(displayOption));
    }

    /**
     * @deprecated (2.5.0) Use {@link MainFrame#getWorkbenchLayout()} instead.
     * @see #getMainFrame()
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    public static int getDisplayOption() {
        return View.getSingleton().getMainFrame().getWorkbenchLayout().getId();
    }

//  ZAP: Removed method changeDisplayOption(int)
    public void init() {
        OptionsParam options = Model.getSingleton().getOptionsParam();
        mainFrame = new MainFrame(options, getRequestPanel(), getResponsePanel());
        mainFrame.getWorkbench().addPanel(View.getSingleton().getSiteTreePanel(), WorkbenchPanel.PanelType.SELECT);

        // Install default editor and renderer for TextMessageLocationHighlight
        MessageLocationHighlightRenderersEditors.getInstance().addEditor(
                TextMessageLocationHighlight.class,
                new TextMessageLocationHighlightEditor());
        MessageLocationHighlightRenderersEditors.getInstance().addRenderer(
                TextMessageLocationHighlight.class,
                new TextMessageLocationHighlightRenderer());
        
        String statusString;
        for(Status status : AddOn.Status.values()) {     	
        	//Try/catch in case AddOn.Status gets out of sync with cfu.status i18n entries
        	try {
        		statusString = Constant.messages.getString("cfu.status." + status.toString());
        	} catch (MissingResourceException mre) {
        		statusString = status.toString();
        		
        		String errString="Caught " + mre.getClass().getName() + " " + mre.getMessage() + 
						" when looking for i18n string: cfu.status." + statusString;
        		if (Constant.isDevBuild()) {
        			logger.error(errString);
        		} else {
        			logger.warn(errString);
        		}
        	}
        	statusMap.put(status, new StatusUI(status, statusString));
        }
    }

    public void postInit() {
        mainFrame.getWorkbench().addPanel(getOutputPanel(), WorkbenchPanel.PanelType.STATUS);

        refreshTabViewMenus();

        // Add the 'tab' menu items 
        JMenuItem showAllMenu = new JMenuItem(Constant.messages.getString("menu.view.tabs.show"));
        showAllMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllTabs();
            }
        });

        mainFrame.getMainMenuBar().getMenuView().add(showAllMenu);

        JMenuItem hideAllMenu = new JMenuItem(Constant.messages.getString("menu.view.tabs.hide"));
        hideAllMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideAllTabs();
            }
        });
        mainFrame.getMainMenuBar().getMenuView().add(hideAllMenu);

        JMenuItem pinAllMenu = new JMenuItem(Constant.messages.getString("menu.view.tabs.pin"));
        pinAllMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pinAllTabs();
            }
        });
        mainFrame.getMainMenuBar().getMenuView().add(pinAllMenu);

        JMenuItem unpinAllMenu = new JMenuItem(Constant.messages.getString("menu.view.tabs.unpin"));
        unpinAllMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unpinAllTabs();
            }
        });
        mainFrame.getMainMenuBar().getMenuView().add(unpinAllMenu);

        postInitialisation = true;
    }

    /**
     * @deprecated (2.5.0) No longer in use/working, use
     *             {@link MainFrame#setResponsePanelPosition(org.parosproxy.paros.view.WorkbenchPanel.ResponsePanelPosition)}
     *             instead.
     * @since 2.1.0
     * @see #getMainFrame()
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    public org.zaproxy.zap.view.MessagePanelsPositionController getMessagePanelsPositionController() {
        return new org.zaproxy.zap.view.MessagePanelsPositionController(null, null, null, null);
    }

    public void refreshTabViewMenus() {
        if (menuShowTabs != null) {
            // Remove the old ones
            mainFrame.getMainMenuBar().getMenuView().remove(menuShowTabs);
        }
        menuShowTabs = new JMenu(Constant.messages.getString("menu.view.showtab"));
        mainFrame.getMainMenuBar().getMenuView().add(menuShowTabs);

        ExtensionKeyboard extKey = (ExtensionKeyboard) Control.getSingleton().getExtensionLoader().getExtension(ExtensionKeyboard.NAME);

        for (AbstractPanel panel : getWorkbench().getSortedPanels(WorkbenchPanel.PanelType.SELECT)) {
            registerMenu(extKey, panel);
        }
        menuShowTabs.addSeparator();
        for (AbstractPanel panel : getWorkbench().getSortedPanels(WorkbenchPanel.PanelType.WORK)) {
            registerMenu(extKey, panel);
        }
        menuShowTabs.addSeparator();
        for (AbstractPanel panel : getWorkbench().getSortedPanels(WorkbenchPanel.PanelType.STATUS)) {
            registerMenu(extKey, panel);
        }
    }

    private void registerMenu(ExtensionKeyboard extKey, final AbstractPanel ap) {
        ZapMenuItem tabMenu = new ZapMenuItem(
                ap.getClass().getName(), MessageFormat.format(Constant.messages.getString("menu.view.tab"), ap.getName()),
                ap.getDefaultAccelerator());
        tabMenu.setMnemonic(ap.getMnemonic());
        if (ap.getIcon() != null) {
            tabMenu.setIcon(ap.getIcon());
        }
        tabMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getWorkbench().showPanel(ap);
            }
        });

        menuShowTabs.add(tabMenu);
        if (extKey != null) {
            extKey.registerMenuItem(tabMenu);
        }
    }

    public void showAllTabs() {
        getWorkbench().setPanelsVisible(true);
    }

    public void hideAllTabs() {
        getWorkbench().setPanelsVisible(false);
    }

    public void pinAllTabs() {
        getWorkbench().pinVisiblePanels();
    }

    public void unpinAllTabs() {
        getWorkbench().unpinVisiblePanels();
    }

    /**
     * Open the splash screen
     */
    public void showSplashScreen() {
        // Show the splashscreen only if it's been enabled by the user 
        if (Model.getSingleton().getOptionsParam().getViewParam().isShowSplashScreen()) {
            // Show the splash screen to show the user something is happening..
            splashScreen = new SplashScreen();
        }        
    }

    /**
     * Close the curren splash screen and remove all resources
     */
    public void hideSplashScreen() {
        if (splashScreen != null) {
            splashScreen.close();
            splashScreen = null;
        }
    }
    
    /**
     * Set the curent loading completion
     * @param percentage the percentage of completion from 0 to 100
     */
    public void setSplashScreenLoadingCompletion(double percentage) {
        if (splashScreen != null) {
            splashScreen.setLoadingCompletion(percentage);
        }        
    }
    
    /**
     * Add the curent loading completion
     * @param percentage the percentage of completion from 0 to 100 that need to be added
     */
    public void addSplashScreenLoadingCompletion(double percentage) {
        if (splashScreen != null) {
            splashScreen.addLoadingCompletion(percentage);
        }        
    }
    
    @Override
    public int showConfirmDialog(String msg) {
        return showConfirmDialog(getMainFrame(), msg);
    }

    public int showConfirmDialog(JPanel parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
    }

    public int showConfirmDialog(Window parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
    }

    @Override
    public int showYesNoCancelDialog(String msg) {
        return showYesNoCancelDialog(getMainFrame(), msg);
    }

    public int showYesNoCancelDialog(JPanel parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public int showYesNoCancelDialog(Window parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    @Override
    public void showWarningDialog(String msg) {
        showWarningDialog(getMainFrame(), msg);
    }

    public void showWarningDialog(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
    }

    public void showWarningDialog(Window parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void showMessageDialog(String msg) {
        showMessageDialog(getMainFrame(), msg);
    }

    public void showMessageDialog(JPanel parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public void showMessageDialog(Window parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, Constant.PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    private JCheckBox getRememberCheckbox() {
        if (rememberCheckbox == null) {
            rememberCheckbox = new JCheckBox(Constant.messages.getString("view.dialog.remember"));
        }
        return rememberCheckbox;
    }

    public boolean isRememberLastDialogChosen() {
        return this.getRememberCheckbox().isSelected();
    }

    public int showYesNoRememberDialog(Window parent, String msg) {
        // The checkbox is used for all related dialogs, so always reset
        this.getRememberCheckbox().setSelected(false);
        return JOptionPane.showConfirmDialog(parent,
                new Object[]{msg + "\n", this.getRememberCheckbox()}, Constant.PROGRAM_NAME, JOptionPane.YES_NO_OPTION);
    }

    public int showYesNoDialog(Window parent, Object[] objs) {
        return JOptionPane.showConfirmDialog(parent, objs, Constant.PROGRAM_NAME, JOptionPane.YES_NO_OPTION);
    }

    private JCheckBox getDontPromptCheckbox() {
        if (dontPromptCheckbox == null) {
            dontPromptCheckbox = new JCheckBox(Constant.messages.getString("view.dialog.dontPrompt"));
        }
        return dontPromptCheckbox;
    }

    public boolean isDontPromptLastDialogChosen() {
        return this.getDontPromptCheckbox().isSelected();
    }

    public int showConfirmDontPromptDialog(Window parent, String msg) {
        // The checkbox is used for all related dialogs, so always reset
        this.getDontPromptCheckbox().setSelected(false);
        return JOptionPane.showConfirmDialog(parent,
                new Object[]{msg + "\n", this.getDontPromptCheckbox()}, Constant.PROGRAM_NAME, JOptionPane.OK_CANCEL_OPTION);
    }

    public void showWarningDontPromptDialog(Window parent, String msg) {
        // The checkbox is used for all related dialogs, so always reset
        this.getDontPromptCheckbox().setSelected(false);
        JOptionPane.showMessageDialog(parent, 
        		new Object[]{msg + "\n", this.getDontPromptCheckbox()}, Constant.PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
    }

    public void showWarningDontPromptDialog(String msg) {
    	showWarningDontPromptDialog(getMainFrame(), msg);
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
        if (siteMapPanel == null) {
            siteMapPanel = new SiteMapPanel();
        }
        return siteMapPanel;
    }

    @Override
    public OutputPanel getOutputPanel() {
        if (outputPanel == null) {
            outputPanel = new OutputPanel();
            ExtensionHelp.enableHelpKey(outputPanel, "ui.tabs.output");
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
                    KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK, false));
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
                    KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK | Event.SHIFT_MASK, false));
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

    public void showSessionDialog(Session session, String panel) {
        showSessionDialog(session, panel, true, null);
    }

    public void showSessionDialog(Session session, String panel, boolean recreateUISharedContexts) {
        showSessionDialog(session, panel, recreateUISharedContexts, null);
    }

    /**
     * Shows the session properties dialog. If a panel is specified, the dialog
     * is opened showing that panel. If {@code recreateUISharedContexts} is
     * {@code true}, any old UI shared contexts are discarded and new ones are
     * created as copies of the contexts. If a {@code postInitRunnable} is
     * provided, its {@link Runnable#run} method is called after the
     * initialization of all the panels of the session properties dialog.
     *
     * @param session the session
     * @param panel the panel name to be shown
     * @param recreateUISharedContexts if true, any old UI shared contexts are
     * discarded and new ones are created as copies of the contexts
     * @param postInitRunnable if provided, its {@link Runnable#run} method is
     * called after the initialization of all the panels of the session
     * properties dialog.
     */
    public void showSessionDialog(Session session, String panel, boolean recreateUISharedContexts, Runnable postInitRunnable) {
    	if (sessionDialog == null) {
    		this.getSessionDialog();
    	}
    	
        if (recreateUISharedContexts) {
            sessionDialog.recreateUISharedContexts(session);
        }
        
        sessionDialog.initParam(session);
        if (postInitRunnable != null) {
            postInitRunnable.run();
        }
        
        sessionDialog.setTitle(Constant.messages.getString("session.properties.title"));
        sessionDialog.showDialog(false, panel);
    }

    public void addContext(Context c) {
        String contextsNodeName = Constant.messages.getString("context.list");
        ContextGeneralPanel contextGenPanel = new ContextGeneralPanel(c.getName(), c.getIndex());
        contextGenPanel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(new String[]{ contextsNodeName }, contextGenPanel, false);
        this.contextPanels.add(contextGenPanel);

        String[] contextPanelPath = new String[] { contextsNodeName, contextGenPanel.getName() };
        ContextIncludePanel contextIncPanel = new ContextIncludePanel(c);
        contextIncPanel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(contextPanelPath, contextIncPanel, false);
        this.contextPanels.add(contextIncPanel);

        ContextExcludePanel contextExcPanel = new ContextExcludePanel(c);
        contextExcPanel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(contextPanelPath, contextExcPanel, false);
        this.contextPanels.add(contextExcPanel);

        ContextStructurePanel contextStructPanel = new ContextStructurePanel(c);
        contextStructPanel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(contextPanelPath, contextStructPanel, false);
        this.contextPanels.add(contextStructPanel);

        ContextTechnologyPanel contextTechPanel = new ContextTechnologyPanel(c);
        contextTechPanel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(contextPanelPath, contextTechPanel, false);
        this.contextPanels.add(contextTechPanel);

        for (ContextPanelFactory cpf : this.contextPanelFactories) {
            addPanelForContext(c, cpf, contextPanelPath);
        }
        this.getSiteTreePanel().reloadContextTree();
    }

    /**
     * Adds a custom context panel for the given context, created form the given context panel factory and placed under the
     * given path.
     *
     * @param contextPanelFactory context panel factory used to create the panel, must not be {@code null}
     * @param panelPath the path where to add the created panel, must not be {@code null}
     * @param context the target context, must not be {@code null}
     */
    private void addPanelForContext(Context context, ContextPanelFactory contextPanelFactory, String[] panelPath) {
        AbstractContextPropertiesPanel panel = contextPanelFactory.getContextPanel(context);
        panel.setSessionDialog(getSessionDialog());
        getSessionDialog().addParamPanel(panelPath, panel, false);
        this.contextPanels.add(panel);

        List<AbstractContextPropertiesPanel> panels = contextPanelFactoriesPanels.get(contextPanelFactory);
        if (panels == null) {
            panels = new ArrayList<>();
            contextPanelFactoriesPanels.put(contextPanelFactory, panels);
        }
        panels.add(panel);
    }

    public void renameContext(Context c) {
        ContextGeneralPanel ctxPanel = getContextGeneralPanel(c);
        if (ctxPanel != null) {
            getSessionDialog().renamePanel(ctxPanel, c.getIndex() + ":" + c.getName());
        }
        this.getSiteTreePanel().reloadContextTree();
    }

    /**
     * Gets the context general panel of the given {@code context}.
     *
     * @param context the context whose context general panel will be returned
     * @return the {@code ContextGeneralPanel} of the given context, {@code null} if not found
     */
    private ContextGeneralPanel getContextGeneralPanel(Context context) {
        for (AbstractParamPanel panel : contextPanels) {
            if (panel instanceof ContextGeneralPanel) {
                ContextGeneralPanel contextGeneralPanel = (ContextGeneralPanel) panel;
                if (contextGeneralPanel.getContextIndex() == context.getIndex()) {
                    return contextGeneralPanel;
                }
            }
        }
        return null;
    }

    public void changeContext(Context c) {
        this.getSiteTreePanel().contextChanged(c);
    }

    @Override
    public void addContextPanelFactory(ContextPanelFactory contextPanelFactory) {
        if (contextPanelFactory == null) {
            throw new IllegalArgumentException("Parameter contextPanelFactory must not be null.");
        }
        this.contextPanelFactories.add(contextPanelFactory);

        if (postInitialisation) {
            String contextsNodeName = Constant.messages.getString("context.list");
            for (Context context : Model.getSingleton().getSession().getContexts()) {
                ContextGeneralPanel contextGeneralPanel = getContextGeneralPanel(context);
                if (contextGeneralPanel != null) {
                    addPanelForContext(context, contextPanelFactory, new String[] { contextsNodeName, contextGeneralPanel.getName() });
                }
            }
        }
    }

    @Override
    public void removeContextPanelFactory(ContextPanelFactory contextPanelFactory) {
        if (contextPanelFactory == null) {
            throw new IllegalArgumentException("Parameter contextPanelFactory must not be null.");
        }

        if (contextPanelFactories.remove(contextPanelFactory)) {
            contextPanelFactory.discardContexts();

            List<AbstractContextPropertiesPanel> panels = contextPanelFactoriesPanels.remove(contextPanelFactory);
            if (panels != null) {
                for (AbstractContextPropertiesPanel panel : panels) {
                    getSessionDialog().removeParamPanel(panel);
                }
                contextPanels.removeAll(panels);
            }
        }
    }

    public void deleteContext(Context c) {
        List<AbstractContextPropertiesPanel> removedPanels = new ArrayList<>();
        for (Iterator<AbstractContextPropertiesPanel> it = contextPanels.iterator(); it.hasNext();) {
            AbstractContextPropertiesPanel panel = it.next();
        	if (panel.getContextIndex() == c.getIndex()) {
                getSessionDialog().removeParamPanel(panel);
                it.remove();
                removedPanels.add(panel);
        	}
        }
        for (ContextPanelFactory cpf : this.contextPanelFactories) {
            cpf.discardContext(c);
            List<AbstractContextPropertiesPanel> panels = contextPanelFactoriesPanels.get(cpf);
            if (panels != null) {
                panels.removeAll(removedPanels);
            }
        }
        this.getSiteTreePanel().reloadContextTree();
    }

    public void discardContexts() {
        for (AbstractParamPanel panel : contextPanels) {
            getSessionDialog().removeParamPanel(panel);
        }
        for (ContextPanelFactory cpf : this.contextPanelFactories) {
            cpf.discardContexts();
            contextPanelFactoriesPanels.remove(cpf);
        }
        contextPanels.clear();
        this.getSiteTreePanel().reloadContextTree();
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
     * Returns a new {@code MainPopupMenu} instance with the pop pup menu items
     * returned by the method {@code getPopupList()}.
     * <p>
     * <strong>Note:</strong> Pop up menu items ({@code JMenuItem},
     * {@code JMenu}, {@code ExtensionPopupMenuItem} and
     * {@code ExtensionPopupMenu}) should be added/removed to/from the list
     * returned by the method {@code getPopupList()} not by calling the methods
     * {@code MainPopupMenu#addMenu(...)} on the returned {@code MainPopupMenu}
     * instance. Adding pop up menu items to the returned {@code MainPopupMenu}
     * instance relies on current implementation of {@code MainPopupMenu} which
     * may change without notice (moreover a new instance is created each time
     * the method is called).
     * </p>
     *
     * @return a {@code MainPopupMenu} containing the pop up menu items that are
     * in the list returned by the method {@code getPopupList()}.
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
     * Returns the list of pop up menu items that will have the
     * {@code MainPopupMenu} instance returned by the method
     * {@code getPopupMenu()}.
     * <p>
     * Should be used to dynamically add/remove pop up menu items
     * ({@code JMenuItem}, {@code JMenu}, {@code ExtensionPopupMenuItem} and
     * {@code ExtensionPopupMenu}) to the main pop up menu at runtime.
     * </p>
     *
     * @return the list of pop up menu items that will have the main pop up
     * menu.
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
    public void addMainToolbarButton(JButton button) {
        this.getMainFrame().getMainToolbarPanel().addButton(button);
    }

    public void addMainToolbarSeparator() {
        this.getMainFrame().getMainToolbarPanel().addSeparator();
    }

    public void addMainToolbarButton(JToggleButton button) {
        this.getMainFrame().getMainToolbarPanel().addButton(button);
    }

    public void removeMainToolbarButton(JButton button) {
        this.getMainFrame().getMainToolbarPanel().removeButton(button);
    }

    public void removeMainToolbarButton(JToggleButton button) {
        this.getMainFrame().getMainToolbarPanel().removeButton(button);
    }

    public void addMainToolbarSeparator(JToolBar.Separator separator) {
        this.getMainFrame().getMainToolbarPanel().addSeparator(separator);
    }

    public void removeMainToolbarSeparator(JToolBar.Separator separator) {
        this.getMainFrame().getMainToolbarPanel().removeSeparator(separator);
    }

    /**
     * Gets the splash screen as {@code Component}. It should be used only as a parent for error/warning dialogues shown during
     * initialisation.
     *
     * @return the splash screen, {@code null} when the splash screen is/was not displayed.
     * @since 2.4.0
     */
    public Component getSplashScreen() {
        return splashScreen;
    }
    
    /**
     * Returns a StatusUI for the given AddOn.Status
     * @param status the Status for which a StatusUI is wanted
     * @return a StatusUI
     * @since 2.5.0
     */
    public StatusUI getStatusUI(AddOn.Status status) {
    	return statusMap.get(status);
    }

    /**
     * Sets whether or not the main tool bar should be visible.
     *
     * @param visible {@code true} if the main tool bar should be visible, {@code false} otherwise.
     * @since 2.5.0
     */
    public void setMainToolbarVisible(boolean visible) {
        getMainFrame().setMainToolbarVisible(visible);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Note:</strong> Current implementation just supports {@link HttpMessage HTTP messages}. Attempting to display
     * other message types has no effect.
     */
    @Override
    public void displayMessage(Message message) {
        if (message == null) {
            getRequestPanel().clearView(true);
            getResponsePanel().clearView(false);
            return;
        }

        if (!(message instanceof HttpMessage)) {
            logger.warn("Unable to display message: " + message.getClass().getCanonicalName());
            return;
        }

        HttpMessage httpMessage = (HttpMessage) message;
        if (httpMessage.getRequestHeader().isEmpty()) {
            getRequestPanel().clearView(true);
        } else {
            getRequestPanel().setMessage(httpMessage);
        }

        if (httpMessage.getResponseHeader().isEmpty()) {
            getResponsePanel().clearView(false);
        } else {
            getResponsePanel().setMessage(httpMessage, true);
        }
    }
}
