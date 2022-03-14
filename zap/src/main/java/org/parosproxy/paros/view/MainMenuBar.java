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
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/10/17 Issue 393: Added more online links from menu
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/09/11 Issue 786: Snapshot session menu item not working
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts
// ZAP: 2014/11/11 Issue 1406: Move online menu items to an add-on
// ZAP: 2014/12/22 Issue 1476: Display contexts in the Sites tree
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2017/05/10 Issue 3460: Add Show Support Info help menuitem
// ZAP: 2017/06/27 Issue 2375: Added option to change ZAP mode in edit menu
// ZAP: 2017/09/02 Use KeyEvent instead of Event (deprecated in Java 9).
// ZAP: 2018/07/17 Use ViewDelegate.getMenuShortcutKeyStroke.
// ZAP: 2019/03/15 Issue 3578: Added new menu Import
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/06/07 JavaDoc corrections.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/08 Remove/fix boilerplate javadocs, and un-necessary fully qualified method return
// types.
// ZAP: 2021/05/14 Remove redundant type arguments.
// ZAP: 2021/12/30 Disable snapshot menu item if session not already persisted, add tooltip to
// disabled menu item (Issue 6938).
// ZAP: 2022/03/12 Add open recent menu
// ZAP: 2022/08/05 Address warns with Java 18 (Issue 7389).
package org.parosproxy.paros.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.control.MenuFileControl;
import org.parosproxy.paros.control.MenuToolsControl;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.view.AboutDialog;
import org.zaproxy.zap.view.ZapMenuItem;
import org.zaproxy.zap.view.ZapSupportDialog;

@SuppressWarnings("serial")
public class MainMenuBar extends JMenuBar {

    private static final long serialVersionUID = 8580116506279095244L;

    private static final Logger logger = LogManager.getLogger(MainMenuBar.class);

    private javax.swing.JMenu menuEdit = null;
    private javax.swing.JMenu menuTools = null;
    private javax.swing.JMenu menuView = null;
    private javax.swing.JMenu menuImport = null;
    private ZapMenuItem menuToolsOptions = null;
    private javax.swing.JMenu menuFile = null;
    private JMenu menuFileOpenRecent;
    private ZapMenuItem menuFileNewSession = null;
    private ZapMenuItem menuFileOpen = null;
    private ZapMenuItem menuFileSaveAs = null;
    private ZapMenuItem menuFileSnapshot = null;
    private ZapMenuItem menuFileContextExport = null;
    private ZapMenuItem menuFileContextImport = null;
    private ZapMenuItem menuFileExit = null;
    private ZapMenuItem menuFileExitAndDelete = null;
    private ZapMenuItem menuFileProperties = null;
    private JMenu menuHelp = null;
    private ZapMenuItem menuHelpAbout = null;
    private ZapMenuItem menuHelpSupport = null;
    private JMenu menuAnalyse = null;
    private JMenu menuZapMode = null;
    private ButtonGroup menuZapModeGroup = null;
    private Map<Mode, JRadioButtonMenuItem> menuZapModeMap = null;
    // ZAP: Added standard report menu
    private JMenu menuReport = null;
    private JMenu menuOnline = null;

    public MainMenuBar() {
        super();
        initialize();
    }

    private void initialize() {
        this.add(getMenuFile());
        this.add(getMenuEdit());
        this.add(getMenuView());
        this.add(getMenuAnalyse());
        this.add(getMenuReport());
        this.add(getMenuTools());
        this.add(getMenuImport());
        this.add(getMenuOnline());
        this.add(getMenuHelp());
    }

    /**
     * Gets the Edit menu
     *
     * @return the Edit menu
     */
    public javax.swing.JMenu getMenuEdit() {
        if (menuEdit == null) {
            menuEdit = new javax.swing.JMenu();
            menuEdit.setText(Constant.messages.getString("menu.edit")); // ZAP: i18n
            menuEdit.setMnemonic(Constant.messages.getChar("menu.edit.mnemonic"));
            menuEdit.add(getMenuEditZAPMode());
            menuEdit.addSeparator();
        }
        return menuEdit;
    }

    private JMenuItem getMenuEditZAPMode() {
        if (menuZapMode == null) {
            menuZapMode = new JMenu(Constant.messages.getString("menu.edit.zapmode"));
            menuZapModeGroup = new ButtonGroup();
            JRadioButtonMenuItem newButton;
            menuZapModeMap = new HashMap<>();
            for (Mode modeType : Mode.values()) {
                newButton = addZAPModeMenuItem(modeType);
                menuZapModeGroup.add(newButton);
                menuZapMode.add(newButton);
                menuZapModeMap.put(modeType, newButton);
            }
            Mode mode =
                    Mode.valueOf(Model.getSingleton().getOptionsParam().getViewParam().getMode());
            setMode(mode);
        }
        return menuZapMode;
    }

    private JRadioButtonMenuItem addZAPModeMenuItem(final Mode modeType) {
        final JRadioButtonMenuItem modeItem =
                new JRadioButtonMenuItem(
                        Constant.messages.getString(
                                "view.toolbar.mode." + modeType.name() + ".select"));
        modeItem.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        Control.getSingleton().setMode(modeType);
                        View.getSingleton().getMainFrame().getMainToolbarPanel().setMode(modeType);
                    }
                });
        return modeItem;
    }

    public void setMode(final Mode mode) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        menuZapModeMap.get(mode).setSelected(true);
                    }
                });
    }

    /**
     * Gets the Tools menu
     *
     * @return the Tools menu
     */
    public JMenu getMenuTools() {
        if (menuTools == null) {
            menuTools = new javax.swing.JMenu();
            menuTools.setText(Constant.messages.getString("menu.tools")); // ZAP: i18n
            menuTools.setMnemonic(Constant.messages.getChar("menu.tools.mnemonic"));
            menuTools.addSeparator();
            menuTools.add(getMenuToolsOptions());
        }
        return menuTools;
    }

    /**
     * Gets the View menu
     *
     * @return the View menu
     */
    public JMenu getMenuView() {
        if (menuView == null) {
            menuView = new javax.swing.JMenu();
            menuView.setText(Constant.messages.getString("menu.view")); // ZAP: i18n
            menuView.setMnemonic(Constant.messages.getChar("menu.view.mnemonic"));
        }
        return menuView;
    }

    /**
     * Gets the Import menu
     *
     * @return the Import menu
     * @since 2.8.0
     */
    public JMenu getMenuImport() {
        if (menuImport == null) {
            menuImport = new javax.swing.JMenu();
            menuImport.setText(Constant.messages.getString("menu.import"));
            menuImport.setMnemonic(Constant.messages.getChar("menu.import.mnemonic"));
        }
        return menuImport;
    }

    private ZapMenuItem getMenuToolsOptions() {
        if (menuToolsOptions == null) {
            menuToolsOptions =
                    new ZapMenuItem(
                            "menu.tools.options",
                            View.getSingleton()
                                    .getMenuShortcutKeyStroke(
                                            KeyEvent.VK_O, KeyEvent.ALT_DOWN_MASK, false));
            menuToolsOptions.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            getMenuToolsControl().options();
                        }
                    });
        }
        return menuToolsOptions;
    }

    /**
     * Gets the File menu
     *
     * @return the File menu
     */
    public JMenu getMenuFile() {
        if (menuFile == null) {
            menuFile = new javax.swing.JMenu();
            menuFile.setText(Constant.messages.getString("menu.file")); // ZAP: i18n
            menuFile.setMnemonic(Constant.messages.getChar("menu.file.mnemonic"));
            menuFile.add(getMenuFileNewSession());
            menuFile.add(getMenuFileOpen());
            menuFile.add(getMenuFileOpenRecent());
            menuFile.addSeparator();
            menuFile.add(getMenuFileSaveAs());
            menuFile.add(getMenuFileSnapshot());
            menuFile.addSeparator();
            menuFile.add(getMenuFileProperties());

            menuFile.addSeparator();
            menuFile.add(getMenuContextImport());
            menuFile.add(getMenuContextExport());

            menuFile.addSeparator();
            menuFile.add(getMenuFileExitAndDelete());
            menuFile.add(getMenuFileExit());
        }
        return menuFile;
    }

    private JMenuItem getMenuFileNewSession() {
        if (menuFileNewSession == null) {
            menuFileNewSession =
                    new ZapMenuItem(
                            "menu.file.newSession",
                            View.getSingleton().getMenuShortcutKeyStroke(KeyEvent.VK_N, 0, false));
            menuFileNewSession.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            try {
                                getMenuFileControl().newSession(true);
                            } catch (Exception e1) {
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "menu.file.newSession.error")); // ZAP: i18n
                                logger.error(e1.getMessage(), e1);
                            }
                        }
                    });
        }
        return menuFileNewSession;
    }

    private JMenuItem getMenuFileOpen() {
        if (menuFileOpen == null) {
            menuFileOpen =
                    new ZapMenuItem(
                            "menu.file.openSession",
                            View.getSingleton().getMenuShortcutKeyStroke(KeyEvent.VK_O, 0, false));
            menuFileOpen.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            getMenuFileControl().openSession();
                        }
                    });
        }
        return menuFileOpen;
    }

    private JMenu getMenuFileOpenRecent() {
        if (menuFileOpenRecent == null) {
            menuFileOpenRecent = new JMenu();
            menuFileOpenRecent.setText(Constant.messages.getString("menu.file.openRecent"));
            refreshMenuFileOpenRecent();
        }
        return menuFileOpenRecent;
    }

    private void refreshMenuFileOpenRecent() {
        menuFileOpenRecent.removeAll();

        for (String session :
                Model.getSingleton().getOptionsParam().getViewParam().getRecentSessions()) {
            JMenuItem menuItem = new JMenuItem(session);
            menuItem.addActionListener(e -> getMenuFileControl().openSession(session));
            menuFileOpenRecent.add(menuItem);
        }
        menuFileOpenRecent.setEnabled(menuFileOpenRecent.getMenuComponentCount() != 0);
    }

    private JMenuItem getMenuFileSaveAs() {
        if (menuFileSaveAs == null) {
            menuFileSaveAs = new ZapMenuItem("menu.file.persistSession");
            menuFileSaveAs.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (Model.getSingleton().getSession().isNewState()) {
                                getMenuFileControl().saveAsSession();
                            } else {
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "menu.file.sessionExists.error"));
                            }
                        }
                    });
        }
        return menuFileSaveAs;
    }

    private JMenuItem getMenuFileSnapshot() {
        if (menuFileSnapshot == null) {
            menuFileSnapshot = new ZapMenuItem("menu.file.snapshotSession");
            menuFileSnapshot.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (!Model.getSingleton().getSession().isNewState()) {
                                getMenuFileControl().saveSnapshot();
                            } else {
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "menu.file.snapshotSession.error"));
                            }
                        }
                    });
            toggleSnapshotState(false);
        }
        return menuFileSnapshot;
    }

    private JMenuItem getMenuFileExit() {
        if (menuFileExit == null) {
            menuFileExit = new ZapMenuItem("menu.file.exit");
            menuFileExit.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            getMenuFileControl().exit();
                        }
                    });
        }
        return menuFileExit;
    }

    private javax.swing.JMenuItem getMenuFileExitAndDelete() {
        if (menuFileExitAndDelete == null) {
            menuFileExitAndDelete = new ZapMenuItem("menu.file.exit.delete");
            menuFileExitAndDelete.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            int ans =
                                    View.getSingleton()
                                            .showConfirmDialog(
                                                    Constant.messages.getString(
                                                            "menu.file.exit.delete.warning"));
                            if (ans == JOptionPane.OK_OPTION) {
                                Control.getSingleton()
                                        .exitAndDeleteSession(
                                                Model.getSingleton().getSession().getFileName());
                            }
                        }
                    });
        }
        return menuFileExitAndDelete;
    }

    /**
     * Gets the File Menu Control
     *
     * @return the File Menu Control
     */
    public MenuFileControl getMenuFileControl() {
        return Control.getSingleton().getMenuFileControl();
    }

    private MenuToolsControl getMenuToolsControl() {
        return Control.getSingleton().getMenuToolsControl();
    }

    private ZapMenuItem getMenuFileProperties() {
        if (menuFileProperties == null) {
            menuFileProperties =
                    new ZapMenuItem(
                            "menu.file.properties",
                            View.getSingleton()
                                    .getMenuShortcutKeyStroke(
                                            KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK, false));
            menuFileProperties.setText(
                    Constant.messages.getString("menu.file.properties")); // ZAP: i18n
            menuFileProperties.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            getMenuFileControl().properties();
                        }
                    });
        }
        return menuFileProperties;
    }

    private ZapMenuItem getMenuContextImport() {
        if (menuFileContextImport == null) {
            menuFileContextImport = new ZapMenuItem("menu.file.context.import");
            menuFileContextImport.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            getMenuFileControl().importContext();
                        }
                    });
        }
        return menuFileContextImport;
    }

    private ZapMenuItem getMenuContextExport() {
        if (menuFileContextExport == null) {
            menuFileContextExport = new ZapMenuItem("menu.file.context.export");
            menuFileContextExport.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            getMenuFileControl().exportContext();
                        }
                    });
        }
        return menuFileContextExport;
    }

    /**
     * Gets the Help menu
     *
     * @return the Help menu
     */
    public JMenu getMenuHelp() {
        if (menuHelp == null) {
            menuHelp = new JMenu();
            menuHelp.setText(Constant.messages.getString("menu.help")); // ZAP: i18n
            menuHelp.setMnemonic(Constant.messages.getChar("menu.help.mnemonic"));
            menuHelp.add(getMenuHelpAbout());
            menuHelp.add(getMenuHelpSupport());
        }
        return menuHelp;
    }

    public JMenu getMenuOnline() {
        if (menuOnline == null) {
            menuOnline = new JMenu();
            menuOnline.setText(Constant.messages.getString("menu.online"));
            menuOnline.setMnemonic(Constant.messages.getChar("menu.online.mnemonic"));
        }
        return menuOnline;
    }

    // ZAP: Added standard report menu
    public JMenu getMenuReport() {
        if (menuReport == null) {
            menuReport = new JMenu();
            menuReport.setText(Constant.messages.getString("menu.report")); // ZAP: i18n
            menuReport.setMnemonic(Constant.messages.getChar("menu.report.mnemonic"));
        }
        return menuReport;
    }

    /**
     * Gets the About menu
     *
     * @return the 'About' menu item.
     */
    private ZapMenuItem getMenuHelpAbout() {
        if (menuHelpAbout == null) {
            menuHelpAbout = new ZapMenuItem("menu.help.about");
            menuHelpAbout.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            AboutDialog dialog =
                                    new AboutDialog(View.getSingleton().getMainFrame(), true);
                            dialog.setVisible(true);
                        }
                    });
        }
        return menuHelpAbout;
    }

    private ZapMenuItem getMenuHelpSupport() {
        if (menuHelpSupport == null) {
            menuHelpSupport = new ZapMenuItem("menu.help.zap.support");
            menuHelpSupport.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            ZapSupportDialog zsd =
                                    new ZapSupportDialog(View.getSingleton().getMainFrame(), true);
                            zsd.setVisible(true);
                        }
                    });
        }
        return menuHelpSupport;
    }

    /**
     * Gets the Analyze menu
     *
     * @return the Analyse menu
     */
    public JMenu getMenuAnalyse() {
        if (menuAnalyse == null) {
            menuAnalyse = new JMenu();
            menuAnalyse.setText(Constant.messages.getString("menu.analyse")); // ZAP: i18n
            menuAnalyse.setMnemonic(Constant.messages.getChar("menu.analyse.mnemonic"));
        }
        return menuAnalyse;
    }

    public void sessionChanged(Session session) {
        if (session != null) {
            this.getMenuFileSaveAs().setEnabled(session.isNewState());
            toggleSnapshotState(!session.isNewState());

            refreshMenuFileOpenRecent();
        }
    }

    private void toggleSnapshotState(boolean enabled) {
        if (enabled) {
            menuFileSnapshot.setToolTipText("");
        } else {
            menuFileSnapshot.setToolTipText(
                    Constant.messages.getString("menu.file.snapshotSession.disabled.tooltip"));
        }
        menuFileSnapshot.setEnabled(enabled);
    }
}
