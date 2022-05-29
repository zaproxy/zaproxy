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
// ZAP: 2011/11/20 Set order
// ZAP: 2012/03/10 Issue 279: Flag as a core extension
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/02 Added the instance variable optionsDatabasePanel and the
// method getOptionsDatabasePanel() and changed the method hook(ExtensionHook)
// to add the optionsDatabasePanel to the options panel.
// ZAP: 2015/03/16 Issue 1525: Further database independence changes
// ZAP: 2015/08/17 Issue 1795: Allow JVM options to be configured via GUI
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages
// ZAP: 2016/06/20 Removed unnecessary/unused constructor
// ZAP: 2017/04/07 Added getUIName()
// ZAP: 2017/11/06 Moved options panel to new proxies extension (Issue 3983)
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2022/05/20 Remove Connection panel.
// ZAP: 2022/05/29 Remove Client Certificate panel.
package org.parosproxy.paros.extension.option;

import javax.swing.JCheckBoxMenuItem;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.autoupdate.OptionsCheckForUpdatesPanel;
import org.zaproxy.zap.extension.lang.OptionsLangPanel;

public class ExtensionOption extends ExtensionAdaptor {

    private static final String NAME = "ExtensionViewOption";

    private JCheckBoxMenuItem menuViewImage = null;
    private OptionsViewPanel optionsViewPanel = null;
    private OptionsCheckForUpdatesPanel optionsCheckForUpdatesPanel = null;
    private OptionsLangPanel optionsLangPanel = null;

    /** The options database panel. */
    // ZAP: Added the instance variable.
    private OptionsDatabasePanel optionsDatabasePanel = null;

    private OptionsJvmPanel optionsJvmPanel = null;

    public ExtensionOption() {
        super(NAME);
        this.setOrder(2);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("options.name");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addViewMenuItem(getMenuViewImage());

            extensionHook.getHookView().addOptionPanel(getOptionsViewPanel());
            extensionHook.getHookView().addOptionPanel(getOptionsCheckForUpdatesPanel());
            extensionHook.getHookView().addOptionPanel(getOptionsLangPanel());
            // ZAP: Added the statement.
            extensionHook.getHookView().addOptionPanel(getOptionsDatabasePanel());
            extensionHook.getHookView().addOptionPanel(getOptionsJvmPanel());
        }
    }

    private JCheckBoxMenuItem getMenuViewImage() {
        if (menuViewImage == null) {
            menuViewImage = new JCheckBoxMenuItem();
            menuViewImage.setText(
                    Constant.messages.getString("menu.view.enableImage")); // ZAP: i18n
            menuViewImage.addItemListener(
                    new java.awt.event.ItemListener() {
                        @Override
                        public void itemStateChanged(java.awt.event.ItemEvent e) {
                            getModel()
                                    .getOptionsParam()
                                    .getViewParam()
                                    .setProcessImages(getMenuViewImage().getState() ? 1 : 0);
                        }
                    });
        }
        return menuViewImage;
    }

    private OptionsViewPanel getOptionsViewPanel() {
        if (optionsViewPanel == null) {
            optionsViewPanel = new OptionsViewPanel();
        }
        return optionsViewPanel;
    }

    private OptionsCheckForUpdatesPanel getOptionsCheckForUpdatesPanel() {
        if (optionsCheckForUpdatesPanel == null) {
            optionsCheckForUpdatesPanel = new OptionsCheckForUpdatesPanel();
        }
        return optionsCheckForUpdatesPanel;
    }

    private OptionsLangPanel getOptionsLangPanel() {
        if (optionsLangPanel == null) {
            optionsLangPanel = new OptionsLangPanel();
        }
        return optionsLangPanel;
    }

    /**
     * Gets the database options panel.
     *
     * @return the database options panel
     */
    // ZAP: Added the method.
    private OptionsDatabasePanel getOptionsDatabasePanel() {
        if (optionsDatabasePanel == null) {
            optionsDatabasePanel = new OptionsDatabasePanel();
        }
        return optionsDatabasePanel;
    }

    private OptionsJvmPanel getOptionsJvmPanel() {
        if (optionsJvmPanel == null) {
            optionsJvmPanel = new OptionsJvmPanel();
        }
        return optionsJvmPanel;
    }

    @Override
    public boolean isCore() {
        // Really need this in order to configure basic functionality
        return true;
    }

    @Override
    public String getAuthor() {
        return Constant.PAROS_TEAM;
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
