/*
* Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.help;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.SwingHelpUtilities;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.AddOnInstallationStatusListener;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * Loads the core help files and provides GUI elements to access them.
 */
public class ExtensionHelp extends ExtensionAdaptor {

	/**
	 * The name of the property that has the {@code HelpSet}, assigned to a {@code JComponent}.
	 */
	private static final String HELP_SET_PROPERTY = "HelpSet";

	/**
	 * The name of the property that has the ID of the help page, assigned to a {@code JComponent}.
	 */
	private static final String HELP_ID_PROPERTY = "HelpID";

	private static final String HELP_SET_FILE_NAME = "helpset";
	public static final ImageIcon HELP_ICON = DisplayUtils.getScaledIcon(
			new ImageIcon(ExtensionHelp.class.getResource("/resource/icon/16/201.png")));

	private ZapMenuItem menuHelpZap = null;
	private JButton helpButton = null;

	private static HelpSet hs = null;
	private static HelpBroker hb = null;

	/**
	 * The {@code ActionListener} to show the help dialogue (with contents matching the focused UI component, if available).
	 * <p>
	 * Lazily initialised.
	 * 
	 * @see #createHelpBroker()
	 */
	private static ActionListener showHelpActionListener;

	/**
	 * A {@code WeakHashMap} of {@code JComponent}s to the ID of the help page assigned to them.
	 * <p>
	 * Used to add/remove the help page from the components when the help add-on is installed/uninstalled.
	 * 
	 * @see #setHelpEnabled(boolean)
	 */
	private static WeakHashMap<JComponent, String> componentsWithHelp;

	private static final Logger logger = Logger.getLogger(ExtensionHelp.class);
	
    public ExtensionHelp() {
        super("ExtensionHelp");
        this.setOrder(10000);	// Set to a huge value so the help button is always on the far right of the toolbar 
	}
	
	@Override
	public void initView(ViewDelegate view) {
		super.initView(view);

		SwingHelpUtilities.setContentViewerUI(BasicOnlineContentViewerUI.class.getCanonicalName());
		UIManager.getDefaults().put("ZapHelpSearchNavigatorUI", ZapBasicSearchNavigatorUI.class.getCanonicalName());
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {	        
	        extensionHook.getHookMenu().addHelpMenuItem(getMenuHelpZapUserGuide());

	        View.getSingleton().addMainToolbarSeparator();
	        View.getSingleton().addMainToolbarButton(this.getHelpButton());

            enableHelpKey(this.getView().getSiteTreePanel(), "ui.tabs.sites");
            enableHelpKey(this.getView().getRequestPanel(), "ui.tabs.request");
            enableHelpKey(this.getView().getResponsePanel(), "ui.tabs.response");

            setHelpEnabled(getHelpBroker() != null);

            extensionHook.addAddOnInstallationStatusListener(new AddOnInstallationStatusListenerImpl());
	    }

	}
	
	/**
	 * Tells whether or not the help is available.
	 * <p>
	 * The help is available when the help add-on for the currently set {@code Locale} is installed.
	 *
	 * @return {@code true} if the help is available, {@code false} otherwise
	 * @since 2.5.0
	 */
	public boolean isHelpAvailable() {
		return hb != null;
	}

	/**
	 * Sets whether or not the help is enabled (menu item, buttons and help for the components).
	 * <p>
	 * The call to this method has no effect if the view is not initialised.
	 * 
	 * @param enabled {@code true} if the help should be enabled, {@code false} otherwise
	 * @see #findHelpSetUrl()
	 */
	private void setHelpEnabled(boolean enabled) {
		if (getView() == null) {
			return;
		}

		JRootPane rootPane = getView().getMainFrame().getRootPane();
		if (enabled && findHelpSetUrl() != null) {
			createHelpBroker();

			getMenuHelpZapUserGuide().addActionListener(showHelpActionListener);
			getMenuHelpZapUserGuide().setToolTipText(null);
			getMenuHelpZapUserGuide().setEnabled(true);

			// Enable the top level F1 help key
			hb.enableHelpKey(rootPane, "zap.intro", hs, "javax.help.SecondaryWindow", null);

			for (Entry<JComponent, String> entry : componentsWithHelp.entrySet()) {
				hb.enableHelp(entry.getKey(), entry.getValue(), hs);
			}

			getHelpButton().setToolTipText(Constant.messages.getString("help.button.tooltip"));
			getHelpButton().setEnabled(true);

		} else {
			String toolTipNoHelp = Constant.messages.getString("help.error.nohelp");
			getMenuHelpZapUserGuide().setEnabled(false);
			getMenuHelpZapUserGuide().setToolTipText(toolTipNoHelp);
			getMenuHelpZapUserGuide().removeActionListener(showHelpActionListener);

			rootPane.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0));
			rootPane.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			removeHelpProperties(rootPane);

			for (JComponent component : componentsWithHelp.keySet()) {
				removeHelpProperties(component);
			}

			getHelpButton().setEnabled(false);
			getHelpButton().setToolTipText(toolTipNoHelp);

			hb = null;
			hs = null;
			showHelpActionListener = null;
		}
	}

	/**
	 * Removes the help properties from the given component.
	 *
	 * @param component the component whose help properties will be removed, must not be {@code null}
	 * @see #HELP_ID_PROPERTY
	 * @see #HELP_SET_PROPERTY
	 */
	private void removeHelpProperties(JComponent component) {
		component.putClientProperty(HELP_ID_PROPERTY, null);
		component.putClientProperty(HELP_SET_PROPERTY, null);
	}

	public static HelpBroker getHelpBroker() {
		if (hb == null) {
			createHelpBroker();
		}
		return hb;
	}
	
	private static synchronized void createHelpBroker() {
		if (hb == null) {
			try {
				URL hsUrl = findHelpSetUrl();
				if (hsUrl != null) {
					hs = new HelpSet(ExtensionFactory.getAddOnLoader(), hsUrl);
					hb = hs.createHelpBroker();
					showHelpActionListener = new CSH.DisplayHelpFromFocus(hb);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Finds and returns the {@code URL} to the {@code HelpSet} that matches the currently set {@code Locale}.
	 * <p>
	 * The name of the {@code HelpSet} searched is {@value #HELP_SET_FILE_NAME}.
	 *
	 * @return the {@code URL} to the {@code HelpSet}, {@code null} if not found
	 * @see Constant#getLocale()
	 * @see HelpSet
	 */
	private static URL findHelpSetUrl() {
		return HelpSet.findHelpSet(ExtensionFactory.getAddOnLoader(), HELP_SET_FILE_NAME, Constant.getLocale());
	}
	
	/**
	 * Enables the help for the given component using the given help page ID.
	 * <p>
	 * The help page is shown when the help keyboard shortcut (F1) is pressed, while the component is focused.
	 *
	 * @param component the component that will have a help page assigned
	 * @param id the ID of the help page
	 */
	public static void enableHelpKey (Component component, String id) {
		if (component instanceof JComponent) {
			JComponent jComponent = (JComponent) component;
			if (componentsWithHelp == null) {
				componentsWithHelp = new WeakHashMap<>();
			}
			componentsWithHelp.put(jComponent, id);
		}

		if (hb != null) {
			hb.enableHelp(component, id, hs);
		}
	}

	/*
	public static void enablePopupHelpKey (Component component, String key) {
		if (getHelpBroker() != null) {
			hb.enableHelpKey(component, 
					"zap.intro", hs, "javax.help.SecondaryWindow", null);
			hb.enableHelp(component, key, hs);

		}
	}
	*/

	/**
	 * @see #showHelp(String)
	 */
	public static void showHelp() {
		showHelp("zap.intro");
	}
	
	/**
	 * Shows a specific help topic
	 * 
	 * @param helpindex
	 */
	public static void showHelp(String helpindex) {
		if (getHelpBroker() == null) {
			return;
		}

		try {
			getHelpBroker().showID(helpindex, "javax.help.SecondaryWindow", null);
		} catch (Exception e) {
			logger.error("error loading help with index: " + helpindex, e);
		}
	}

	private ZapMenuItem getMenuHelpZapUserGuide() {
		if (menuHelpZap == null) {
			menuHelpZap = new ZapMenuItem("help.menu.guide",
					KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false));
		}
		return menuHelpZap;
	}
	
	private JButton getHelpButton() {
		if (helpButton == null) {
			helpButton = new JButton();
			helpButton.setIcon(new ImageIcon(ExtensionHelp.class.getResource("/resource/icon/16/201.png")));
			
			helpButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showHelp();
				}
			});
		}
		return helpButton;
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("help.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
    	return true;
    }

    /**
     * An {@code AddOnInstallationStatusListener} responsible to enabled/disable help UI components when the help add-on is
     * installed/uninstalled.
     */
    private class AddOnInstallationStatusListenerImpl implements AddOnInstallationStatusListener {

        @Override
        public void addOnInstalled(AddOn addOn) {
            if (hb == null && findHelpSetUrl() != null) {
                setHelpEnabled(true);
            }
        }

        @Override
        public void addOnSoftUninstalled(AddOn addOn, boolean successfully) {
            addOnUninstalled(addOn, successfully);
        }

        @Override
        public void addOnUninstalled(AddOn addOn, boolean successfully) {
            if (hb != null && findHelpSetUrl() == null) {
                setHelpEnabled(false);
            }
        }
    }

}
