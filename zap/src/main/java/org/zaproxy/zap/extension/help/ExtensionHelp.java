/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.help;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.function.Function;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.SwingHelpUtilities;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.AddOnInstallationStatusListener;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.LocaleUtils;
import org.zaproxy.zap.view.ZapMenuItem;

/** Loads the core help files and provides GUI elements to access them. */
public class ExtensionHelp extends ExtensionAdaptor {

    /** The name of the property that has the {@code HelpSet}, assigned to a {@code JComponent}. */
    private static final String HELP_SET_PROPERTY = "HelpSet";

    /**
     * The name of the property that has the ID of the help page, assigned to a {@code JComponent}.
     */
    private static final String HELP_ID_PROPERTY = "HelpID";

    /**
     * The (default) file name of a HelpSet.
     *
     * @see HelpSet
     * @since 2.8.0
     */
    public static final String HELP_SET_FILE_NAME = "helpset";

    /**
     * The file extension of a HelpSet.
     *
     * @see HelpSet
     * @since 2.8.0
     */
    public static final String HELP_SET_FILE_EXTENSION = "hs";

    /** @deprecated (2.7.0) Use {@link #getHelpIcon()} instead. */
    @Deprecated
    public static final ImageIcon HELP_ICON = View.isInitialised() ? getHelpIcon() : null;

    /**
     * The help icon.
     *
     * <p>Lazily initialised.
     *
     * @see #getHelpIcon()
     */
    private static ImageIcon helpIcon;

    private static final String NAME = "ExtensionHelp";

    private ZapMenuItem menuHelpZap = null;
    private JButton helpButton = null;

    private static HelpSet hs = null;
    private static HelpBroker hb = null;

    /**
     * The {@code ActionListener} to show the help dialogue (with contents matching the focused UI
     * component, if available).
     *
     * <p>Lazily initialised.
     *
     * @see #createHelpBroker()
     */
    private static ActionListener showHelpActionListener;

    /**
     * A {@code WeakHashMap} of {@code JComponent}s to the ID of the help page assigned to them.
     *
     * <p>Used to add/remove the help page from the components when the help add-on is
     * installed/uninstalled.
     *
     * @see #setHelpEnabled(boolean)
     */
    private static WeakHashMap<JComponent, String> componentsWithHelp;

    private static Map<AddOn, List<HelpSet>> addOnHelpSets = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(ExtensionHelp.class);

    public ExtensionHelp() {
        super(NAME);
        this.setOrder(
                10000); // Set to a huge value so the help button is always on the far right of the
        // toolbar
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("help.name");
    }

    @Override
    public void initView(ViewDelegate view) {
        super.initView(view);

        SwingHelpUtilities.setContentViewerUI(BasicOnlineContentViewerUI.class.getCanonicalName());
        UIManager.getDefaults()
                .put(
                        "ZapHelpSearchNavigatorUI",
                        ZapBasicSearchNavigatorUI.class.getCanonicalName());
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.getHookMenu().addHelpMenuItem(getMenuHelpZapUserGuide());

            extensionHook.getHookView().addMainToolBarComponent(new JToolBar.Separator());
            extensionHook.getHookView().addMainToolBarComponent(this.getHelpButton());

            enableHelpKey(this.getView().getSiteTreePanel(), "ui.tabs.sites");
            enableHelpKey(this.getView().getRequestPanel(), "ui.tabs.request");
            enableHelpKey(this.getView().getResponsePanel(), "ui.tabs.response");

            setHelpEnabled(getHelpBroker() != null);

            extensionHook.addAddOnInstallationStatusListener(
                    new AddOnInstallationStatusListenerImpl());
        }
    }

    /**
     * Tells whether or not the help is available.
     *
     * <p>The help is available when the help add-on for the currently set {@code Locale} is
     * installed.
     *
     * @return {@code true} if the help is available, {@code false} otherwise
     * @since 2.5.0
     */
    public boolean isHelpAvailable() {
        return hb != null;
    }

    /**
     * Sets whether or not the help is enabled (menu item, buttons and help for the components).
     *
     * <p>The call to this method has no effect if the view is not initialised.
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

            loadAddOnHelpSets(
                    ExtensionFactory.getAddOnLoader().getAddOnCollection().getInstalledAddOns());

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

    private static void loadAddOnHelpSets(List<AddOn> addOns) {
        addOns.forEach(ExtensionHelp::loadAddOnHelpSet);
    }

    private static void loadAddOnHelpSet(AddOn addOn) {
        addOn.getLoadedExtensions().forEach(ExtensionHelp::loadExtensionHelpSet);

        AddOn.HelpSetData helpSetData = addOn.getHelpSetData();
        if (helpSetData.isEmpty()) {
            return;
        }

        ClassLoader classLoader = addOn.getClassLoader();
        URL helpSetUrl =
                LocaleUtils.findResource(
                        helpSetData.getBaseName(),
                        HELP_SET_FILE_EXTENSION,
                        helpSetData.getLocaleToken(),
                        Constant.getLocale(),
                        classLoader::getResource);
        if (helpSetUrl == null) {
            logger.error(
                    "Declared helpset not found for '{}' add-on, with base name: {} {}",
                    addOn.getId(),
                    helpSetData.getBaseName(),
                    (helpSetData.getLocaleToken().isEmpty()
                            ? ""
                            : " and locale token: " + helpSetData.getLocaleToken()));
            return;
        }

        try {
            logger.debug("Loading help for '{}' add-on and merging with core help.", addOn.getId());
            addHelpSet(addOn, new HelpSet(classLoader, helpSetUrl));
        } catch (HelpSetException e) {
            logger.error("An error occured while adding help for '{}' add-on:", addOn.getId(), e);
        }
    }

    private static void addHelpSet(AddOn addOn, HelpSet helpSet) {
        hb.getHelpSet().add(helpSet);
        addOnHelpSets.computeIfAbsent(addOn, k -> new ArrayList<>()).add(helpSet);
    }

    private static void loadExtensionHelpSet(Extension ext) {
        URL helpSetUrl = getExtensionHelpSetUrl(ext);
        if (helpSetUrl != null) {
            try {
                logger.debug(
                        "Load help files for extension '{}' and merge with core help.",
                        ext.getName());
                addHelpSet(
                        ext.getAddOn(), new HelpSet(ext.getClass().getClassLoader(), helpSetUrl));
            } catch (HelpSetException e) {
                logger.error(
                        "An error occured while adding help file of extension '{}': {}",
                        ext.getName(),
                        e.getMessage(),
                        e);
            }
        }
    }

    private static URL getExtensionHelpSetUrl(Extension extension) {
        Package extPackage = extension.getClass().getPackage();
        String extensionPackage = extPackage != null ? extPackage.getName() + "." : "";
        String localeToken = "%LC%";
        Function<String, URL> getResource = extension.getClass().getClassLoader()::getResource;
        URL helpSetUrl =
                LocaleUtils.findResource(
                        extensionPackage
                                + "resources.help"
                                + localeToken
                                + "."
                                + HELP_SET_FILE_NAME,
                        HELP_SET_FILE_EXTENSION,
                        localeToken,
                        Constant.getLocale(),
                        getResource);
        if (helpSetUrl == null) {
            // Search in old location
            helpSetUrl =
                    LocaleUtils.findResource(
                            extensionPackage
                                    + "resource.help"
                                    + localeToken
                                    + "."
                                    + HELP_SET_FILE_NAME,
                            HELP_SET_FILE_EXTENSION,
                            localeToken,
                            Constant.getLocale(),
                            getResource);
        }
        return helpSetUrl;
    }

    /**
     * Removes the help properties from the given component.
     *
     * @param component the component whose help properties will be removed, must not be {@code
     *     null}
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
     * Finds and returns the {@code URL} to the {@code HelpSet} that matches the currently set
     * {@code Locale}.
     *
     * <p>The name of the {@code HelpSet} searched is {@value #HELP_SET_FILE_NAME}.
     *
     * @return the {@code URL} to the {@code HelpSet}, {@code null} if not found
     * @see Constant#getLocale()
     * @see HelpSet
     */
    private static URL findHelpSetUrl() {
        return LocaleUtils.findResource(
                HELP_SET_FILE_NAME,
                HELP_SET_FILE_EXTENSION,
                Constant.getLocale(),
                r -> ExtensionFactory.getAddOnLoader().getResource(r));
    }

    /**
     * Enables the help for the given component using the given help page ID.
     *
     * <p>The help page is shown when the help keyboard shortcut (F1) is pressed, while the
     * component is focused.
     *
     * @param component the component that will have a help page assigned
     * @param id the ID of the help page
     */
    public static void enableHelpKey(Component component, String id) {
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

    /** @see #showHelp(String) */
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
            logger.error("error loading help with index: {}", helpindex, e);
        }
    }

    private ZapMenuItem getMenuHelpZapUserGuide() {
        if (menuHelpZap == null) {
            menuHelpZap =
                    new ZapMenuItem(
                            "help.menu.guide", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false));
        }
        return menuHelpZap;
    }

    private JButton getHelpButton() {
        if (helpButton == null) {
            helpButton = new JButton();
            helpButton.setIcon(getHelpIcon());

            helpButton.addActionListener(
                    new java.awt.event.ActionListener() {
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

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    /**
     * An {@code AddOnInstallationStatusListener} responsible to enabled/disable help UI components
     * when the help add-on is installed/uninstalled.
     */
    private class AddOnInstallationStatusListenerImpl implements AddOnInstallationStatusListener {

        @Override
        public void addOnInstalled(AddOn addOn) {
            if (hb == null) {
                if (findHelpSetUrl() != null) {
                    setHelpEnabled(true);
                }
            } else {
                loadAddOnHelpSet(addOn);
            }
        }

        @Override
        public void addOnSoftUninstalled(AddOn addOn, boolean successfully) {
            addOnUninstalled(addOn, successfully);
        }

        @Override
        public void addOnUninstalled(AddOn addOn, boolean successfully) {
            if (hb == null) {
                return;
            }

            if (findHelpSetUrl() == null) {
                setHelpEnabled(false);
            } else {
                addOnHelpSets.computeIfPresent(
                        addOn,
                        (k, helpsets) -> {
                            EventQueue.invokeLater(() -> helpsets.forEach(hb.getHelpSet()::remove));
                            return null;
                        });
            }
        }
    }

    /**
     * Gets the help icon.
     *
     * <p>Should be called/used only when in view mode.
     *
     * @return the help icon, never {@code null}.
     * @since 2.7.0
     */
    public static ImageIcon getHelpIcon() {
        if (helpIcon == null) {
            helpIcon =
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ExtensionHelp.class.getResource("/resource/icon/16/201.png")));
        }
        return helpIcon;
    }
}
