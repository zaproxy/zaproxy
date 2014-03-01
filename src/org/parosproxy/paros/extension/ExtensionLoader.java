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
// ZAP: 2011/12/14 Support for extension dependencies
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/15 Reflected the change in the name of the method optionsChanged of
// the class OptionsChangedListener. Changed the method destroyAllExtension() to
// save the configurations of the main http panels and save the configuration file.
// ZAP: 2012/04/23 Reverted the changes of the method destroyAllExtension(),
// now the configurations of the main http panels and the configuration file
// are saved in the method Control.shutdown(boolean).
// ZAP: 2012/04/24 Changed the method destroyAllExtension to catch exceptions.
// ZAP: 2012/04/25 Added the type argument and removed unnecessary cast.
// ZAP: 2012/07/23 Removed parameter from View.getSessionDialog call.
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/30 Issue 425: Added tab index to support quick start tab 
// ZAP: 2012/12/27 Added hookPersistentConnectionListener() method.
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/25 Added removeExtension(...) method and further helper methods
// to remove listeners, menu items, etc.
// ZAP: 2013/01/25 Refactored hookMenu(). Resolved some Checkstyle issues.
// ZAP: 2013/01/29 Catch Errors thrown by out of date extensions as well as Exceptions
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/11/16 Issue 807: Error while loading ZAP when Quick Start Tab is closed
// ZAP: 2013/11/16 Issue 845: AbstractPanel added twice to TabbedPanel2 in ExtensionLoader#addTabPanel
// ZAP: 2013/12/03 Issue 934: Handle files on the command line via extension
// ZAP: 2013/12/13 Added support for Full Layout DISPLAY_OPTION_TOP_FULL in the hookView function.
//
package org.parosproxy.paros.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.control.Proxy;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamDialog;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.MainMenuBar;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.view.SiteMapListener;
import org.zaproxy.zap.view.TabbedPanel2;

public class ExtensionLoader {

    private Vector<Extension> extensionList = new Vector<>();
    private Vector<ExtensionHook> hookList = new Vector<>();
    private Model model = null;

    private View view = null;
    // ZAP: Added logger
    private Logger logger = Logger.getLogger(ExtensionLoader.class);

    public ExtensionLoader(Model model, View view) {
        
        this.model = model;
        this.view = view;
    }

    public void addExtension(Extension extension) {
        extensionList.add(extension);
    }
    
    public void destroyAllExtension() {
        for (int i=0; i<getExtensionCount(); i++) {
            // ZAP: Added try catch block.
            try {
                getExtension(i).destroy();
            } catch (Exception e) {
               logger.error(e.getMessage(), e);
            }
        }
        
    }
    
    public Extension getExtension(int i) {
        return extensionList.get(i);
    }
    
    public Extension getExtension(String name) {
        if (name != null) {
	        for (int i=0; i<extensionList.size(); i++) {
	            Extension p = getExtension(i);
	            if (p.getName().equalsIgnoreCase(name)) {
	                return p;
	            }
	        }
        }
        return null;
    }
    
    public Extension getExtensionByClassName(String name) {
        if (name != null) {
        	for (int i=0; i<extensionList.size(); i++) {
	            Extension p = getExtension(i);
	            if (p.getClass().getName().equals(name)) {
	                return p;
	            }
	        }
        }
        return null;
    }
    
    public Extension getExtension(Class<?> c) {
        if (c != null) {
	        for (int i=0; i<extensionList.size(); i++) {
	            Extension p = getExtension(i);
	            if (p.getClass().equals(c)) {
	                return p;
	            }
	        }
        }
        return null;
    }
    
    public int getExtensionCount() {
        return extensionList.size();
    }
    
    public void hookProxyListener(Proxy proxy) {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<ProxyListener> listenerList = hook.getProxyListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    ProxyListener listener = listenerList.get(j);
                    if (listener != null) {
                        proxy.addProxyListener(listener);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
    	}
    }
    
    private void removeProxyListener(ExtensionHook hook) {
		Proxy proxy = Control.getSingleton().getProxy();
        List<ProxyListener> listenerList = hook.getProxyListenerList();
        for (int j=0; j<listenerList.size(); j++) {
            try {
                ProxyListener listener = listenerList.get(j);
                if (listener != null) {
                    proxy.removeProxyListener(listener);
                }
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }
    
    public void hookPersistentConnectionListener(Proxy proxy) {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<PersistentConnectionListener> listenerList = hook.getPersistentConnectionListener();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    PersistentConnectionListener listener = listenerList.get(j);
                    if (listener != null) {
                        proxy.addPersistentConnectionListener(listener);
                    }
                } catch (Exception e) {
                	logger.error(e.getMessage(), e);
                }
            }
    	}
    }

	private void removePersistentConnectionListener(ExtensionHook hook) {
		Proxy proxy = Control.getSingleton().getProxy();
        List<PersistentConnectionListener> listenerList = hook.getPersistentConnectionListener();
        for (int j=0; j<listenerList.size(); j++) {
            try {
                PersistentConnectionListener listener = listenerList.get(j);
                if (listener != null) {
                    proxy.removePersistentConnectionListener(listener);
                }
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
    	}
	}
    
    // ZAP: Added support for site map listeners
    public void hookSiteMapListener(SiteMapPanel siteMapPanel) {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<SiteMapListener> listenerList = hook.getSiteMapListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                	SiteMapListener listener = listenerList.get(j);
                    if (listener != null) {
                    	siteMapPanel.addSiteMapListener(listener);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
        }
    }
    
    private void removeSiteMapListener(ExtensionHook hook) {
		if (view != null) {
			SiteMapPanel siteMapPanel = view.getSiteTreePanel();
	        List<SiteMapListener> listenerList = hook.getSiteMapListenerList();
	        for (int j=0; j<listenerList.size(); j++) {
	            try {
	            	SiteMapListener listener = listenerList.get(j);
	                if (listener != null) {
	                	siteMapPanel.removeSiteMapListener(listener);
	                }
	            } catch (Exception e) {
	            	logger.error(e.getMessage(), e);
	            }
	        }
		}
    }
    
    public void optionsChangedAllPlugin(OptionsParam options) {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<OptionsChangedListener> listenerList = hook.getOptionsChangedListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    OptionsChangedListener listener = listenerList.get(j);
                    if (listener != null) {
                    	// ZAP: reflected the change in the name of the method optionsChanged.
                        listener.optionsChanged(options);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
        }
    }
    
    public void runCommandLine() {
        ExtensionHook hook = null;
        Extension ext = null;
        for (int i=0; i<getExtensionCount(); i++) {
            ext = getExtension(i);
            hook = hookList.get(i);
            if (ext instanceof CommandLineListener) {
                CommandLineListener listener = (CommandLineListener) ext;
                listener.execute(hook.getCommandLineArgument());
            }
        }
    }
    
    public void sessionChangedAllPlugin(Session session) {
    	logger.debug("sessionChangedAllPlugin");
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    SessionChangedListener listener = listenerList.get(j);
                    if (listener != null) {
                        listener.sessionChanged(session);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
            
        }
    }
    
    public void sessionAboutToChangeAllPlugin(Session session) {
    	logger.debug("sessionAboutToChangeAllPlugin");
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    SessionChangedListener listener = listenerList.get(j);
                    if (listener != null) {
                        listener.sessionAboutToChange(session);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
        }
    }
    
    public void sessionScopeChangedAllPlugin(Session session) {
    	logger.debug("sessionScopeChangedAllPlugin");
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    SessionChangedListener listener = listenerList.get(j);
                    if (listener != null) {
                        listener.sessionScopeChanged(session);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
        }
    }
    
    public void sessionModeChangedAllPlugin(Mode mode) {
    	logger.debug("sessionModeChangedAllPlugin");
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    SessionChangedListener listener = listenerList.get(j);
                    if (listener != null) {
                        listener.sessionModeChanged(mode);
                    }
                } catch (Exception e) {
                	// ZAP: Log the exception
                	logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void addonFilesAdded() {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<AddonFilesChangedListener> listenerList = hook.getAddonFilesChangedListener();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    listenerList.get(j).filesAdded();
                } catch (Exception e) {
                	logger.error(e.getMessage(), e);
                }
            }
    	}
    }

    public void addonFilesRemoved() {
    	Iterator<ExtensionHook> iter = hookList.iterator();
    	while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<AddonFilesChangedListener> listenerList = hook.getAddonFilesChangedListener();
            for (int j=0; j<listenerList.size(); j++) {
                try {
                    listenerList.get(j).filesRemoved();
                } catch (Exception e) {
                	logger.error(e.getMessage(), e);
                }
            }
    	}
    }

    public void startAllExtension() {
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).start();
        }
    }
    
    public void startLifeCycle() {
        initAllExtension();
        initModelAllExtension(model);
        initXMLAllExtension(model.getSession(), model.getOptionsParam());
        initViewAllExtension(view);
        
        hookAllExtension();
        startAllExtension();
        
    }
    
    public void startLifeCycle(Extension ext) {
    	ext.init();
    	ext.initModel(model);
        ext.initXML(model.getSession(), model.getOptionsParam());
        ext.initView(view);
        try {
        	ExtensionHook extHook = new ExtensionHook(model, view);
			ext.hook(extHook);
			hookList.add(extHook);
			
			if (view != null) {
			    // no need to hook view if no GUI
			    hookView(view, extHook);
			    hookMenu(view, extHook);
			}
			hookOptions(extHook);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        ext.start();
    }
    
    public void stopAllExtension() {
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).stop();
        }
        
    }
    
    // ZAP: Added the type argument.
    private void addParamPanel(List<AbstractParamPanel> panelList, AbstractParamDialog dialog) {
        AbstractParamPanel panel = null;
        String[] ROOT = {};
        for (int i=0; i<panelList.size(); i++) {
            try {
                // ZAP: Removed unnecessary cast.
                panel = panelList.get(i);
                dialog.addParamPanel(ROOT, panel, true);
            } catch (Exception e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        }
        
    }
    
    private void removeParamPanel(List<AbstractParamPanel> panelList, AbstractParamDialog dialog) {
        AbstractParamPanel panel = null;
        for (int i=0; i<panelList.size(); i++) {
            try {
                panel = panelList.get(i);
                dialog.removeParamPanel(panel);
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Add every panel from panelList to the TabbedPanel2 tab.
     * @param panelList
     * @param tab
     */
    private void addTabPanel(List<AbstractPanel> panelList, TabbedPanel2 tab) {
        AbstractPanel panel = null;
        for (int i=0; i<panelList.size(); i++) {
            try {
                panel = panelList.get(i);
                
        		// ZAP: added icon
                if (panel.getTabIndex() >= 0) {
                	tab.addTab(panel.getName() + " ", panel.getIcon(), panel, panel.isHideable(), panel.getTabIndex());
                    if (panel.getTabIndex() == 0  && tab.indexOfComponent(panel) != -1) {
                    	// Its now the first one, give it focus
                    	tab.setSelectedComponent(panel);	
                    }

                } else {
                	tab.addTab(panel.getName() + " ", panel.getIcon(), panel);
                }

            } catch (Exception e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        }
    }
    
    private void removeTabPanel(List<AbstractPanel> panelList, TabbedPanel2 tab) {
        AbstractPanel panel = null;
        for (int i=0; i<panelList.size(); i++) {
            try {
                panel = panelList.get(i);
                tab.removeTab(panel);
            } catch (Exception e) {
            	logger.error(e.getMessage(), e);
            }
        }
    }    
    
    private void hookAllExtension() {
        ExtensionHook extHook = null;
        for (int i=0; i<getExtensionCount(); i++) {
            try {
				extHook = new ExtensionHook(model, view);
				getExtension(i).hook(extHook);
				hookList.add(extHook);
				
				if (view != null) {
				    // no need to hook view if no GUI
				    hookView(view, extHook);
				    hookMenu(view, extHook);

				}
				hookOptions(extHook);
				getExtension(i).optionsLoaded();
			} catch (Throwable e) {
				// Catch Errors thrown by out of date extensions as well as Exceptions
				logger.error(e.getMessage(), e);
			}
        }
        
        if (view != null) {
            view.getMainFrame().getMainMenuBar().validate();
            view.getMainFrame().validate();
        }

    }
    
    /**
     * Hook command line listener with the command line processor
     * @param cmdLine
     */
    public void hookCommandLineListener (CommandLine cmdLine) throws Exception {
        Vector<CommandLineArgument[]> allCommandLineList = new Vector<>();
        Map<String, CommandLineListener> extMap = new HashMap<String, CommandLineListener>();
        for (int i=0; i<hookList.size(); i++) {
            ExtensionHook hook = hookList.get(i);
            CommandLineArgument[] arg = hook.getCommandLineArgument();
            if (arg.length > 0) {
                allCommandLineList.add(arg);
            }
            if (extensionList.get(i) instanceof CommandLineListener) {
            	CommandLineListener cli = (CommandLineListener) extensionList.get(i);
            	List<String> exts = cli.getHandledExtensions();
            	if (exts != null) {
            		for (String ext : exts) {
            			extMap.put(ext, cli);
            		}
            	}
            }
        }
        
        cmdLine.parse(allCommandLineList, extMap);
    }
    
    private void hookMenu(View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }

        ExtensionHookMenu hookMenu = hook.getHookMenu();
        if (hookMenu == null) {
            return;
        }
        
        MainMenuBar menuBar = view.getMainFrame().getMainMenuBar();
        
        // 2 menus at the back (Tools/Help)
        addMenuHelper(menuBar, hookMenu.getNewMenus(), 2);

        addMenuHelper(menuBar.getMenuFile(), hookMenu.getFile(), 2);
        addMenuHelper(menuBar.getMenuTools(), hookMenu.getTools(), 2);
        addMenuHelper(menuBar.getMenuEdit(), hookMenu.getEdit());
        addMenuHelper(menuBar.getMenuView(), hookMenu.getView());
        addMenuHelper(menuBar.getMenuAnalyse(), hookMenu.getAnalyse());
        addMenuHelper(menuBar.getMenuHelp(), hookMenu.getHelpMenus());
        addMenuHelper(menuBar.getMenuReport(), hookMenu.getReportMenus());
        
        addMenuHelper(view.getPopupList(), hookMenu.getPopupMenus());
    }
    
    private void addMenuHelper(JMenu menu, List<JMenuItem> items) {
    	addMenuHelper(menu, items, 0);
    }

	private void addMenuHelper(JMenuBar menuBar, List<JMenuItem> items, int existingCount) {
		for (JMenuItem item : items) {
            if (item != null) {
            	menuBar.add(item, menuBar.getMenuCount() - existingCount);
            }
        }
	}
    
    private void addMenuHelper(JMenu menu, List<JMenuItem> items, int existingCount) {
        for (JMenuItem item : items) {
            if (item != null) {
	            if (item == ExtensionHookMenu.MENU_SEPARATOR) {
	                menu.addSeparator();
	                continue;
	            }
	
	            menu.add(item, menu.getItemCount() - existingCount);
            }
        }
	}
    
    private void addMenuHelper(Vector<JMenuItem> menuList, List<JMenuItem> items) {
    	for (JMenuItem item : items) {
            if (item != null) {
            	menuList.add(item);
            }
        }
	}

	private void removeMenu(View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }
        
        ExtensionHookMenu hookMenu = hook.getHookMenu();
        if (hookMenu == null) {
            return;
        }
        
        MainMenuBar menuBar = view.getMainFrame().getMainMenuBar();
        
        // clear up various menus
        removeMenuHelper(menuBar, hookMenu.getNewMenus());
        
        removeMenuHelper(menuBar.getMenuFile(),hookMenu.getFile());
        removeMenuHelper(menuBar.getMenuTools(), hookMenu.getTools());
        removeMenuHelper(menuBar.getMenuEdit(), hookMenu.getEdit());
        removeMenuHelper(menuBar.getMenuView(), hookMenu.getView());
        removeMenuHelper(menuBar.getMenuAnalyse(), hookMenu.getAnalyse());
        removeMenuHelper(menuBar.getMenuHelp(), hookMenu.getHelpMenus());
        removeMenuHelper(menuBar.getMenuReport(), hookMenu.getReportMenus());
        
        removeMenuHelper(view.getPopupList(), hookMenu.getPopupMenus());
        
        view.refreshTabViewMenus();
    }

	private void removeMenuHelper(JMenuBar menuBar, List<JMenuItem> items) {
		for (JMenuItem item : items) {
            if (item != null) {
            	menuBar.remove(item);
            }
        }
	}

	private void removeMenuHelper(JMenu menu, List<JMenuItem> items) {
    	for (JMenuItem item : items) {
            if (item != null) {
	            menu.remove(item);
            }
        }
	}
    
    private void removeMenuHelper(Vector<JMenuItem> menuList, List<JMenuItem> items) {
    	for (JMenuItem item : items) {
            if (item != null) {
            	menuList.remove(item);
            }
        }
	}

	private void hookOptions(ExtensionHook hook) {
        Vector<AbstractParam> list = hook.getOptionsParamSetList();
        for (int i=0; i<list.size(); i++) {
            try {
                AbstractParam paramSet = list.get(i);
                model.getOptionsParam().addParamSet(paramSet);
            } catch (Exception e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        }
    }

	private void unloadOptions(ExtensionHook hook) {
		Vector<AbstractParam> list = hook.getOptionsParamSetList();
        for (int i=0; i<list.size(); i++) {
            try {
                AbstractParam paramSet = list.get(i);
                model.getOptionsParam().removeParamSet(paramSet);
            } catch (Exception e) {
            	// ZAP: Log the exception
            	logger.error(e.getMessage(), e);
            }
        }
	}


    
    private void hookView(View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }
        
        ExtensionHookView pv = hook.getHookView();
        if (pv == null) {
            return;
        }

        // Add the three panels to the current window/workbench: add extension tabs to the Full layout
        // when chosen, otherwise they are as before.
	      int displayOption = Model.getSingleton().getOptionsParam().getViewParam().getDisplayOption();
        addTabPanel(pv.getSelectPanel(), view.getWorkbench().getTabbedSelect());
        addTabPanel(pv.getWorkPanel(), view.getWorkbench().getTabbedWork());
        addTabPanel(pv.getStatusPanel(), view.getWorkbench().getTabbedStatus());
 
        // remember the position of tabs in status position
        if(displayOption == View.DISPLAY_OPTION_TOP_FULL) {
          // save the current normal instances to old instance variables, so they are both
          // referencing the same TabbedPanel2 instances. Used when going into 'Full Layout'
          // so the state is preserved.
          view.getWorkbench().setTabbedOldWork(view.getWorkbench().getTabbedWork());
          view.getWorkbench().setTabbedOldSelect(view.getWorkbench().getTabbedSelect());
          view.getWorkbench().setTabbedOldStatus(view.getWorkbench().getTabbedStatus());

          // switch the layout to Full Layout
          addTabPanel(pv.getSelectPanel(), view.getWorkbench().getTabbedStatus());
          addTabPanel(pv.getWorkPanel(), view.getWorkbench().getTabbedStatus());
          addTabPanel(pv.getStatusPanel(), view.getWorkbench().getTabbedStatus());
        }

        // ZAP: removed session dialog parameter
        addParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        addParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));
    }
    
    private void removeView(View view, ExtensionHook hook) {
    	if (view == null) {
            return;
        }
        
        ExtensionHookView pv = hook.getHookView();
        if (pv == null) {
            return;
        }
        
        // Remote the three panels to the current window/workbench: remove extension tabs
        // from the Full layout when chosen.
	      int displayOption = Model.getSingleton().getOptionsParam().getViewParam().getDisplayOption();
        switch(displayOption) {
          case View.DISPLAY_OPTION_TOP_FULL:
            removeTabPanel(pv.getSelectPanel(), view.getWorkbench().getTabbedStatus());
            removeTabPanel(pv.getWorkPanel(), view.getWorkbench().getTabbedStatus());
            removeTabPanel(pv.getStatusPanel(), view.getWorkbench().getTabbedStatus());
            break;
          case View.DISPLAY_OPTION_LEFT_FULL:
          case View.DISPLAY_OPTION_BOTTOM_FULL:
          default:
            removeTabPanel(pv.getSelectPanel(), view.getWorkbench().getTabbedSelect());
            removeTabPanel(pv.getWorkPanel(), view.getWorkbench().getTabbedWork());
            removeTabPanel(pv.getStatusPanel(), view.getWorkbench().getTabbedStatus());
        }
 
        // ZAP: removed session dialog parameter
        removeParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        removeParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));
    }

    public void removeStatusPanel (AbstractPanel panel) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getWorkbench().getTabbedStatus().remove(panel);
    }
    
    public void removeOptionsPanel (AbstractParamPanel panel) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getOptionsDialog("").removeParamPanel(panel);
    }
    
	public void removeOptionsParamSet(AbstractParam params) {
		model.getOptionsParam().removeParamSet(params);
	}

    public void removeWorkPanel (AbstractPanel panel) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getWorkbench().getTabbedWork().remove(panel);
    }

	public void removePopupMenuItem(ExtensionPopupMenuItem popupMenuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getPopupList().remove(popupMenuItem);
	}
	
    public void removeFileMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuFile().remove(menuItem);
	}

    public void removeEditMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuEdit().remove(menuItem);
	}

    public void removeViewMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuView().remove(menuItem);
	}

    public void removeToolsMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuTools().remove(menuItem);
	}

    public void removeHelpMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuHelp().remove(menuItem);
	}

    public void removeReportMenuItem(JMenuItem menuItem) {
    	if (!View.isInitialised()) {
    		return;
    	}
    	View.getSingleton().getMainFrame().getMainMenuBar().getMenuReport().remove(menuItem);
	}

    private void initAllExtension() {
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).init();
        }
    }

    private void initModelAllExtension(Model model) {
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).initModel(model);
        }
    }

    private void initViewAllExtension(View view) {
        if (view == null) {
            return;
        }
        
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).initView(view);
        }
    }

    private void initXMLAllExtension(Session session, OptionsParam options) {
        for (int i=0; i<getExtensionCount(); i++) {
            getExtension(i).initXML(session, options);
        }        
    }

    /**
	 * Removes an extension from internal list. As a result listeners added via
	 * the {@link ExtensionHook} object are unregistered.
	 * 
	 * @param extension
	 * @param hook
	 */
	public void removeExtension(Extension extension, ExtensionHook hook) {
		extensionList.remove(extension);
		
		// by removing the ExtensionHook object,
		// the following listeners are no longer informed:
		// 		* SessionListeners
		// 		* OptionsChangedListeners
		hookList.remove(hook);
		
		unloadOptions(hook);
		
		removePersistentConnectionListener(hook);
		
		removeProxyListener(hook);
		
		removeSiteMapListener(hook);
		
		removeView(view, hook);
		
		removeMenu(view, hook);
	}

	public List<String> getUnsavedResources() {
		List<String> list = new ArrayList<>();
		List<String> l;
		
        for (int i=0; i<getExtensionCount(); i++) {
            l = getExtension(i).getUnsavedResources();
            if (l != null) {
            	list.addAll(l);
            }
        }
		return list;
	}

}
