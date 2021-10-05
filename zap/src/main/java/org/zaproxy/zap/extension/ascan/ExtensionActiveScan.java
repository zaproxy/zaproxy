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
package org.zaproxy.zap.extension.ascan;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptType;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.ZapMenuItem;

public class ExtensionActiveScan extends ExtensionAdaptor
        implements SessionChangedListener, CommandLineListener, ScanController<ActiveScan> {

    private static final Logger logger = LogManager.getLogger(ExtensionActiveScan.class);
    private static final int ARG_SCAN_IDX = 0;

    public static final String NAME = "ExtensionActiveScan";

    public static final String SCRIPT_TYPE_ACTIVE = "active";
    public static final String SCRIPT_TYPE_VARIANT = "variant";

    // Could be after the last one that saves the HttpMessage, as this ProxyListener doesn't change
    // the HttpMessage.
    public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;
    private static final List<Class<? extends Extension>> DEPENDENCIES;

    private AttackModeScanner attackModeScanner;

    private ActiveScanController ascanController = null;

    private boolean panelSwitch = true;

    static {
        List<Class<? extends Extension>> dep = new ArrayList<>(1);
        dep.add(ExtensionAlert.class);

        DEPENDENCIES = Collections.unmodifiableList(dep);
    }

    private ZapMenuItem menuItemPolicy = null;
    private ZapMenuItem menuItemCustomScan = null;
    private PopupMenuActiveScanCustomWithContext popupMenuActiveScanCustomWithContext;
    private OptionsScannerPanel optionsScannerPanel = null;
    private OptionsVariantPanel optionsVariantPanel = null;
    private ActiveScanPanel activeScanPanel = null;
    private ScannerParam scannerParam = null;
    private final CommandLineArgument[] arguments = new CommandLineArgument[1];
    private final List<AbstractParamPanel> policyPanels = new ArrayList<>();
    private JButton policyButton = null;
    private CustomScanDialog customScanDialog = null;
    private PolicyManagerDialog policyManagerDialog = null;
    private PolicyManager policyManager = null;
    private List<CustomScanPanel> customScanPanels = new ArrayList<>();

    private List<String> excludeList = Collections.emptyList();

    private ActiveScanAPI activeScanApi;

    public ExtensionActiveScan() {
        super(NAME);
        this.setOrder(28);
        policyManager = new PolicyManager(this);
        ascanController = new ActiveScanController(this);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("ascan.name");
    }

    @Override
    public void postInit() {
        policyManager.init();

        if (Control.getSingleton().getMode().equals(Mode.attack)) {
            if (hasView() && !this.getScannerParam().isAllowAttackOnStart()) {
                // Disable attack mode for safeties sake (when running with the UI)
                getView().getMainFrame().getMainToolbarPanel().setMode(Mode.standard);
            } else {
                // Needed to make sure the attackModeScanner starts up
                this.attackModeScanner.sessionModeChanged(Control.getSingleton().getMode());
            }
        }
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        attackModeScanner = new AttackModeScanner(this);

        if (getView() != null) {
            extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemPolicy());
            extensionHook.getHookMenu().addToolsMenuItem(getMenuItemCustomScan());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanCustomWithContext());

            extensionHook.getHookView().addStatusPanel(getActiveScanPanel());
            extensionHook.getHookView().addOptionPanel(getOptionsScannerPanel());
            extensionHook.getHookView().addOptionPanel(getOptionsVariantPanel());

            extensionHook.getHookView().addMainToolBarComponent(this.getPolicyButton());
            getView()
                    .getMainFrame()
                    .getMainFooterPanel()
                    .addFooterToolbarRightLabel(attackModeScanner.getScanStatus().getCountLabel());

            ExtensionHelp.enableHelpKey(getActiveScanPanel(), "ui.tabs.ascan");
        }

        extensionHook.addSessionListener(this);

        extensionHook.addOptionsParamSet(getScannerParam());
        // TODO this isn't currently implemented
        // extensionHook.addCommandLine(getCommandLineArguments());

        ExtensionScript extScript =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionScript.class);
        if (extScript != null) {
            extScript.registerScriptType(
                    new ScriptType(
                            SCRIPT_TYPE_ACTIVE,
                            "ascan.scripts.type.active",
                            createIcon("script-ascan.png"),
                            true));
            extScript.registerScriptType(
                    new ScriptType(
                            SCRIPT_TYPE_VARIANT,
                            "variant.scripts.type.variant",
                            createIcon("script-variant.png"),
                            true));
        }

        this.ascanController.setExtAlert(
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class));
        this.activeScanApi = new ActiveScanAPI(this);
        this.activeScanApi.addApiOptions(getScannerParam());
        extensionHook.addApiImplementor(activeScanApi);
    }

    private ImageIcon createIcon(String iconName) {
        if (getView() == null) {
            return null;
        }
        return new ImageIcon(
                ExtensionActiveScan.class.getResource("/resource/icon/16/" + iconName));
    }

    @Override
    public List<String> getActiveActions() {
        List<ActiveScan> activeScans = ascanController.getActiveScans();
        if (activeScans.isEmpty()) {
            return null;
        }

        String activeActionPrefix = Constant.messages.getString("ascan.activeActionPrefix");
        List<String> activeActions = new ArrayList<>(activeScans.size());
        for (ActiveScan activeScan : activeScans) {
            if (activeScan instanceof AttackScan && ((AttackScan) activeScan).isDone()) {
                continue;
            }
            activeActions.add(
                    MessageFormat.format(activeActionPrefix, activeScan.getDisplayName()));
        }
        return activeActions;
    }

    private ActiveScanPanel getActiveScanPanel() {
        if (activeScanPanel == null) {
            activeScanPanel = new ActiveScanPanel(this);
        }
        return activeScanPanel;
    }

    public void startScanAllInScope() {
        SiteNode snroot = Model.getSingleton().getSession().getSiteTree().getRoot();
        this.startScan(new Target(snroot, null, true, true));
    }

    /**
     * Start the scanning process beginning to a specific node
     *
     * @param startNode the start node where the scanning should begin to work
     * @return the ID of the scan
     */
    public int startScan(SiteNode startNode) {
        return this.startScan(new Target(startNode, true));
    }

    public int startScanNode(SiteNode startNode) {
        return this.startScan(new Target(startNode, false));
    }

    public int startScan(Target target) {
        return this.startScan(target, null, null);
    }

    public int startScan(Target target, User user, Object[] contextSpecificObjects) {
        return this.startScan(target.getDisplayName(), target, user, contextSpecificObjects);
    }

    @Override
    public int startScan(String name, Target target, User user, Object[] contextSpecificObjects) {
        if (name == null) {
            name = target.getDisplayName();
        }

        switch (Control.getSingleton().getMode()) {
            case safe:
                throw new InvalidParameterException("Scans are not allowed in Safe mode");
            case protect:
                List<StructuralNode> nodes = target.getStartNodes();
                if (nodes != null) {
                    for (StructuralNode node : nodes) {
                        if (node instanceof StructuralSiteNode) {
                            SiteNode siteNode = ((StructuralSiteNode) node).getSiteNode();
                            if (!siteNode.isIncludedInScope()) {
                                throw new InvalidParameterException(
                                        "Scans are not allowed on nodes not in scope Protected mode "
                                                + target.getStartNode().getHierarchicNodeName());
                            }
                        }
                    }
                }
                // No problem
                break;
            case standard:
                // No problem
                break;
            case attack:
                // No problem
                break;
        }

        int id = this.ascanController.startScan(name, target, user, contextSpecificObjects);
        if (hasView()) {
            ActiveScan scanner = this.ascanController.getScan(id);
            scanner.addScannerListener(getActiveScanPanel()); // So the UI get updated
            this.getActiveScanPanel().scannerStarted(scanner);
            this.getActiveScanPanel().switchView(scanner);
            if (isPanelSwitch()) {
                this.getActiveScanPanel().setTabFocus();
            }
        }
        return id;
    }

    /**
     * Returns true if the GUI will switch to the Active Scan panel when a scan is started.
     *
     * @since 2.11.0
     */
    public boolean isPanelSwitch() {
        return panelSwitch;
    }

    /**
     * Sets if the GUI will switch to the Active Scan panel when a scan is started. Code should only
     * set this to false just before starting a scan and reset it to true as soon as the scan has
     * started.
     *
     * @since 2.11.0
     */
    public void setPanelSwitch(boolean panelSwitch) {
        this.panelSwitch = panelSwitch;
    }

    private JButton getPolicyButton() {
        if (policyButton == null) {
            policyButton = new JButton();
            policyButton.setIcon(
                    new ImageIcon(
                            ActiveScanPanel.class.getResource(
                                    "/resource/icon/fugue/equalizer.png")));
            policyButton.setToolTipText(Constant.messages.getString("menu.analyse.scanPolicy"));

            policyButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            showPolicyManagerDialog();
                        }
                    });
        }
        return policyButton;
    }

    /**
     * This method initializes menuItemPolicy
     *
     * @return javax.swing.JMenuItem
     */
    private ZapMenuItem getMenuItemPolicy() {
        if (menuItemPolicy == null) {
            menuItemPolicy =
                    new ZapMenuItem(
                            "menu.analyse.scanPolicy",
                            getView().getMenuShortcutKeyStroke(KeyEvent.VK_P, 0, false));

            menuItemPolicy.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            showPolicyManagerDialog();
                        }
                    });
        }

        return menuItemPolicy;
    }

    protected void showPolicyDialog(PolicyManagerDialog parent) throws ConfigurationException {
        this.showPolicyDialog(parent, null);
    }

    protected void showPolicyDialog(PolicyManagerDialog parent, String name)
            throws ConfigurationException {
        ScanPolicy policy;
        if (name != null) {
            policy = this.getPolicyManager().getPolicy(name);
        } else {
            policy = this.getPolicyManager().getTemplatePolicy();
        }
        PolicyDialog dialog = new PolicyDialog(this, parent, policy);
        dialog.initParam(getModel().getOptionsParam());
        for (AbstractParamPanel panel : policyPanels) {
            dialog.addPolicyPanel(panel);
        }

        int result = dialog.showDialog(true);
        if (result == JOptionPane.OK_OPTION) {
            try {
                getModel().getOptionsParam().getConfig().save();

            } catch (ConfigurationException ce) {
                logger.error(ce.getMessage(), ce);
                getView().showWarningDialog(Constant.messages.getString("scanner.save.warning"));
            }
        }
    }

    private ZapMenuItem getMenuItemCustomScan() {
        if (menuItemCustomScan == null) {
            menuItemCustomScan =
                    new ZapMenuItem(
                            "menu.tools.ascanadv",
                            getView()
                                    .getMenuShortcutKeyStroke(
                                            KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK, false));
            menuItemCustomScan.setEnabled(Control.getSingleton().getMode() != Mode.safe);

            menuItemCustomScan.addActionListener(e -> showCustomScanDialog((Target) null));
        }

        return menuItemCustomScan;
    }

    private PopupMenuActiveScanCustomWithContext getPopupMenuActiveScanCustomWithContext() {
        if (popupMenuActiveScanCustomWithContext == null) {
            popupMenuActiveScanCustomWithContext = new PopupMenuActiveScanCustomWithContext(this);
        }
        return popupMenuActiveScanCustomWithContext;
    }

    @Override
    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                sessionChangedEventHandler(session);
                            }
                        });

            } catch (InterruptedException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void sessionChangedEventHandler(Session session) {
        // The scans are stopped in sessionAboutToChange(..)
        if (hasView()) {
            this.getActiveScanPanel().reset();
        }

        this.attackModeScanner.stop();

        if (session == null) {
            // Closedown
            return;
        }
        if (Control.getSingleton().getMode().equals(Mode.attack)) {
            // Start the attack mode scanner up again, have to rescan on change or it wont do
            // anything
            this.attackModeScanner.start();
            this.attackModeScanner.setRescanOnChange(true);
        }
    }

    /**
     * This method initializes optionsScannerPanel
     *
     * @return org.parosproxy.paros.extension.scanner.OptionsScannerPanel
     */
    private OptionsScannerPanel getOptionsScannerPanel() {
        if (optionsScannerPanel == null) {
            optionsScannerPanel = new OptionsScannerPanel(this);
        }
        return optionsScannerPanel;
    }

    /**
     * This method initializes optionsVariantPanel
     *
     * @return org.zaproxy.zap.extension.ascan.OptionsVariantPanel
     */
    private OptionsVariantPanel getOptionsVariantPanel() {
        if (optionsVariantPanel == null) {
            optionsVariantPanel = new OptionsVariantPanel();
        }
        return optionsVariantPanel;
    }

    /**
     * This method initializes scannerParam
     *
     * @return org.parosproxy.paros.core.scanner.ScannerParam
     */
    protected ScannerParam getScannerParam() {
        if (scannerParam == null) {
            scannerParam = new ScannerParam();
        }
        return scannerParam;
    }

    // TODO
    @Override
    public void execute(CommandLineArgument[] args) {
        /*
        if (arguments[ARG_SCAN_IDX].isEnabled()) {
        System.out.println("Scanner started...");
        startScan();
        } else {
        return;
        }

        while (!getScanner().isStop()) {
        try {
        Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        }
        System.out.println("Scanner completed.");
        */
    }

    @SuppressWarnings("unused")
    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_SCAN_IDX] =
                new CommandLineArgument(
                        "-scan",
                        0,
                        null,
                        "",
                        "-scan : Run vulnerability scan depending on previously saved policy.");
        return arguments;
    }

    /**
     * Sets the exclude list.
     *
     * @param urls the new exclude list
     */
    public void setExcludeList(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            excludeList = Collections.emptyList();
            return;
        }

        this.excludeList = urls;
    }

    /**
     * Gets the exclude list.
     *
     * @return the exclude list
     */
    public List<String> getExcludeList() {
        return excludeList;
    }

    public void addPolicyPanel(AbstractParamPanel panel) {
        this.policyPanels.add(panel);
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void sessionAboutToChange(final Session session) {
        // Stop, remove all scans and reset the scan id counter
        this.ascanController.reset();
        this.attackModeScanner.stop();

        if (hasView()) {
            this.getActiveScanPanel().reset();
            if (customScanDialog != null) {
                customScanDialog.reset();
            }
        }
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("ascan.desc");
    }

    @Override
    public void sessionScopeChanged(Session session) {
        if (hasView()) {
            this.getActiveScanPanel().sessionScopeChanged(session);
        }
        this.attackModeScanner.sessionScopeChanged(session);
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        if (Mode.safe.equals(mode)) {
            this.ascanController.stopAllScans();
        }
        if (hasView()) {
            getMenuItemCustomScan().setEnabled(!Mode.safe.equals(mode));
            this.getActiveScanPanel().sessionModeChanged(mode);
        }
        this.attackModeScanner.sessionModeChanged(mode);
    }

    @Override
    public void destroy() {
        this.ascanController.stopAllScans();

        if (hasView()) {
            this.getActiveScanPanel().reset();
        }
    }

    public void showCustomScanDialog(SiteNode node) {
        showCustomScanDialog(node != null ? new Target(node) : null);
    }

    /**
     * Shows the active scan dialogue with the given target, if not already visible.
     *
     * @param target the target, might be {@code null}.
     * @since 2.8.0.
     */
    public void showCustomScanDialog(Target target) {
        if (customScanDialog == null) {
            // Work out the tabs
            String[] tabs = CustomScanDialog.STD_TAB_LABELS;
            if (this.customScanPanels.size() > 0) {
                List<String> tabList = new ArrayList<>();
                for (String str : CustomScanDialog.STD_TAB_LABELS) {
                    tabList.add(str);
                }
                for (CustomScanPanel csp : customScanPanels) {
                    tabList.add(csp.getLabel());
                }
                tabs = tabList.toArray(new String[tabList.size()]);
            }

            customScanDialog =
                    new CustomScanDialog(
                            this,
                            tabs,
                            this.customScanPanels,
                            getView().getMainFrame(),
                            new Dimension(700, 500));
        }
        if (customScanDialog.isVisible()) {
            customScanDialog.requestFocus();
            // Its behind you! Actually not needed no the window is alwaysOnTop, but keeping in case
            // we change that ;)
            customScanDialog.toFront();
            return;
        }
        if (target != null) {
            customScanDialog.init(target);
        } else {
            // Keep the previously selected target
            customScanDialog.init(null);
        }
        customScanDialog.setVisible(true);
    }

    public void addCustomScanPanel(CustomScanPanel panel) {
        this.customScanPanels.add(panel);
        customScanDialog = null; // Force it to be reinitialised
    }

    public void removeCustomScanPanel(CustomScanPanel panel) {
        this.customScanPanels.remove(panel);
        customScanDialog = null; // Force it to be reinitialised
    }

    public void showPolicyManagerDialog() {
        if (policyManagerDialog == null) {
            policyManagerDialog = new PolicyManagerDialog(getView().getMainFrame());
            policyManagerDialog.init(this);
        }
        // The policy names _may_ have changed, e.g. via the api
        policyManagerDialog.policyNamesChanged();
        policyManagerDialog.setVisible(true);
    }

    @Override
    public boolean handleFile(File file) {
        // Cant handle any files
        return false;
    }

    @Override
    public List<String> getHandledExtensions() {
        // Cant handle any extensions
        return null;
    }

    @Override
    public List<ActiveScan> getAllScans() {
        return ascanController.getAllScans();
    }

    @Override
    public List<ActiveScan> getActiveScans() {
        return ascanController.getActiveScans();
    }

    @Override
    public ActiveScan getScan(int id) {
        return ascanController.getScan(id);
    }

    @Override
    public void stopScan(int id) {
        ascanController.stopScan(id);
        // Dont need to update the UI - this will happen automatically via the events
    }

    @Override
    public void pauseScan(int id) {
        ascanController.pauseScan(id);
        if (hasView()) {
            // Update the UI in case this was initiated from the API
            this.getActiveScanPanel().updateScannerUI();
        }
    }

    @Override
    public void resumeScan(int id) {
        ascanController.resumeScan(id);
        if (hasView()) {
            // Update the UI in case this was initiated from the API
            this.getActiveScanPanel().updateScannerUI();
        }
    }

    @Override
    public void stopAllScans() {
        ascanController.stopAllScans();
        // Dont need to update the UI - this will happen automatically via the events
    }

    @Override
    public void pauseAllScans() {
        ascanController.pauseAllScans();
        if (hasView()) {
            // Update the UI in case this was initiated from the API
            this.getActiveScanPanel().updateScannerUI();
        }
    }

    @Override
    public void resumeAllScans() {
        ascanController.removeAllScans();
        if (hasView()) {
            // Update the UI in case this was initiated from the API
            this.getActiveScanPanel().updateScannerUI();
        }
    }

    @Override
    public ActiveScan removeScan(int id) {
        return ascanController.removeScan(id);
    }

    @Override
    public int removeAllScans() {
        return ascanController.removeAllScans();
    }

    @Override
    public int removeFinishedScans() {
        return ascanController.removeFinishedScans();
    }

    @Override
    public ActiveScan getLastScan() {
        return ascanController.getLastScan();
    }

    public int registerScan(ActiveScan scanner) {
        int id = ascanController.registerScan(scanner);
        if (hasView()) {
            // Update the UI in case this was initiated from the API
            scanner.addScannerListener(getActiveScanPanel()); // So the UI get updated
            this.getActiveScanPanel().scannerStarted(scanner);
            this.getActiveScanPanel().switchView(scanner);
            if (isPanelSwitch()) {
                this.getActiveScanPanel().setTabFocus();
            }
        }

        return id;
    }

    public PolicyManager getPolicyManager() {
        return policyManager;
    }

    public int getAttackModeStackSize() {
        return this.attackModeScanner.getStackSize();
    }

    @Override
    public boolean supportsLowMemory() {
        return true;
    }

    /** Part of the core set of features that should be supported by all db types */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
