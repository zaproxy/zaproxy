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
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.tree.TreeNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.SortedComboBoxModel;

@SuppressWarnings("serial")
public abstract class ScanPanel extends AbstractPanel {
    private static final long serialVersionUID = 1L;

    protected enum Location {
        start,
        beforeSites,
        beforeButtons,
        beforeProgressBar,
        afterProgressBar
    }

    public String prefix;

    private ExtensionAdaptor extension = null;
    private JPanel panelCommand = null;
    private JToolBar panelToolbar = null;
    private JLabel scannedCountNameLabel = null;
    private JLabel foundCountNameLabel = null;
    private List<String> activeScans = new ArrayList<>();

    private String currentSite = null;
    private JComboBox<ScanTarget> siteSelect;
    private SortedComboBoxModel<ScanTarget> siteModel = new SortedComboBoxModel<>();

    private JButton startScanButton = null;
    private JButton stopScanButton = null;
    private ZapToggleButton pauseScanButton = null;
    private JButton optionsButton = null;
    private JProgressBar progressBar = null;
    private Map<String, GenericScanner> scanMap = new HashMap<>();
    private AbstractParam scanParam = null;
    private ScanStatus scanStatus = null;
    private Mode mode = Control.getSingleton().getMode();

    private static Logger log = LogManager.getLogger(ScanPanel.class);

    /**
     * Constructs a {@code ScanPanel} with the given message resources prefix, tab icon, extension
     * and options.
     *
     * @param prefix the prefix of the message resources
     * @param icon the icon for the tab
     * @param extension the extension
     * @param scanParam the options
     */
    public ScanPanel(
            String prefix, ImageIcon icon, ExtensionAdaptor extension, AbstractParam scanParam) {
        super();
        this.prefix = prefix;
        this.extension = extension;
        this.scanParam = scanParam;
        initialize(icon);
        log.debug("Constructor {}", prefix);
    }

    /** This method initializes this */
    private void initialize(ImageIcon icon) {
        this.setLayout(new CardLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(474, 251);
        }
        this.setName(Constant.messages.getString(prefix + ".panel.title"));
        this.setIcon(icon);
        this.add(getPanelCommand(), prefix + ".panel");
        scanStatus = new ScanStatus(icon, Constant.messages.getString(prefix + ".panel.title"));

        if (View.isInitialised()) {
            View.getSingleton()
                    .getMainFrame()
                    .getMainFooterPanel()
                    .addFooterToolbarRightLabel(scanStatus.getCountLabel());
        }
    }

    /**
     * This method initializes panelCommand
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getPanelCommand() {
        if (panelCommand == null) {

            panelCommand = new javax.swing.JPanel();
            panelCommand.setLayout(new java.awt.GridBagLayout());
            panelCommand.setName(prefix + ".panel");

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
            panelCommand.add(getWorkPanel(), gridBagConstraints2);
        }
        return panelCommand;
    }

    protected GridBagConstraints getGBC(int gridx, int gridy) {
        return this.getGBC(gridx, gridy, 0.0, new Insets(0, 2, 0, 0));
    }

    protected GridBagConstraints getGBC(int gridx, int gridy, double weightx, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        if (weightx > 0.0) {
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        }
        gbc.insets = insets;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        return gbc;
    }

    private javax.swing.JToolBar getPanelToolbar() {
        if (panelToolbar == null) {

            panelToolbar = new javax.swing.JToolBar();
            panelToolbar.setLayout(new GridBagLayout());
            panelToolbar.setEnabled(true);
            panelToolbar.setFloatable(false);
            panelToolbar.setRollover(true);
            panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            panelToolbar.setName(prefix + ".toolbar");

            int x = 0;

            x = this.addToolBarElements(panelToolbar, Location.start, x);

            panelToolbar.add(
                    new JLabel(Constant.messages.getString(prefix + ".toolbar.site.label")),
                    getGBC(x++, 0));
            panelToolbar.add(getSiteSelect(), getGBC(x++, 0));

            x = this.addToolBarElements(panelToolbar, Location.beforeButtons, x);

            panelToolbar.add(getStartScanButton(), getGBC(x++, 0));
            panelToolbar.add(getPauseScanButton(), getGBC(x++, 0));
            panelToolbar.add(getStopScanButton(), getGBC(x++, 0));

            x = this.addToolBarElements(panelToolbar, Location.beforeProgressBar, x);

            panelToolbar.add(getProgressBar(), getGBC(x++, 0, 1.0, new Insets(0, 5, 0, 5)));

            panelToolbar.add(getActiveScansNameLabel(), getGBC(x++, 0));
            panelToolbar.add(getActiveScansValueLabel(), getGBC(x++, 0));

            x = this.addToolBarElements(panelToolbar, Location.afterProgressBar, x);

            panelToolbar.add(new JLabel(), getGBC(x++, 0, 1.0, new Insets(0, 0, 0, 0))); // Spacer
            panelToolbar.add(getOptionsButton(), getGBC(x++, 0));
        }
        return panelToolbar;
    }

    /**
     * Adds elements to the tool bar. The method is called while initializing the ScanPanel, at the
     * points specified by the {@link Location} enumeration. Should be overridden by all subclasses
     * that want to add new elements to the ScanPanel's tool bar.
     *
     * <p>The tool bar uses a {@code GridBagLayout}, so elements have to be added with a {@code
     * GridBagConstraints}. For this, the {@code getGBC} methods can be used. The {@code gridX}
     * parameter specifies the cell (as used in {@code GridBagConstraints.gridx}) of the current row
     * where the elements can be added.
     *
     * <p>The method must return the new coordinates of the current cell, after the elements have
     * been added.
     *
     * @param toolBar the tool bar
     * @param location the current location where elements will be added
     * @param gridX the x coordinates of the current cell in the {@code GridBagLayout}
     * @return the new coordinates of the current cell, after the elements have been added.
     * @see #getGBC(int, int)
     * @see #getGBC(int, int, double, Insets)
     * @see GridBagConstraints
     * @see GridBagLayout
     */
    protected int addToolBarElements(JToolBar toolBar, Location location, int gridX) {
        return gridX;
    }

    private JLabel getActiveScansNameLabel() {
        if (scannedCountNameLabel == null) {
            scannedCountNameLabel = new javax.swing.JLabel();
            scannedCountNameLabel.setText(
                    Constant.messages.getString(prefix + ".toolbar.ascans.label"));
        }
        return scannedCountNameLabel;
    }

    private JLabel getActiveScansValueLabel() {
        if (foundCountNameLabel == null) {
            foundCountNameLabel = new javax.swing.JLabel();
            foundCountNameLabel.setText(String.valueOf(activeScans.size()));
        }
        return foundCountNameLabel;
    }

    private void setActiveScanLabels() {
        if (EventQueue.isDispatchThread()) {
            setActiveScanLabelsEventHandler();
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                setActiveScanLabelsEventHandler();
                            }
                        });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void setActiveScanLabelsEventHandler() {
        getActiveScansValueLabel().setText(String.valueOf(activeScans.size()));
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = activeScans.iterator();
        sb.append("<html>");
        while (iter.hasNext()) {
            sb.append(iter.next());
            sb.append("<br>");
        }
        sb.append("</html>");

        final String toolTip = sb.toString();
        getActiveScansNameLabel().setToolTipText(toolTip);
        getActiveScansValueLabel().setToolTipText(toolTip);

        scanStatus.setScanCount(activeScans.size());
    }

    private JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setSize(new Dimension(80, 20));
            progressBar.setStringPainted(true);
            progressBar.setEnabled(false);
        }
        return progressBar;
    }

    private JButton getStartScanButton() {
        if (startScanButton == null) {
            startScanButton = new JButton();
            startScanButton.setToolTipText(
                    Constant.messages.getString(prefix + ".toolbar.button.start"));
            startScanButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/131.png"))));
            startScanButton.setEnabled(false);
            startScanButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            startScan();
                        }
                    });
        }
        return startScanButton;
    }

    private JButton getStopScanButton() {
        if (stopScanButton == null) {
            stopScanButton = new JButton();
            stopScanButton.setToolTipText(
                    Constant.messages.getString(prefix + ".toolbar.button.stop"));
            stopScanButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/142.png"))));
            stopScanButton.setEnabled(false);
            stopScanButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            stopScan(currentSite);
                        }
                    });
        }
        return stopScanButton;
    }

    private JToggleButton getPauseScanButton() {
        if (pauseScanButton == null) {
            pauseScanButton = new ZapToggleButton();
            pauseScanButton.setToolTipText(
                    Constant.messages.getString(prefix + ".toolbar.button.pause"));
            pauseScanButton.setSelectedToolTipText(
                    Constant.messages.getString(prefix + ".toolbar.button.unpause"));
            pauseScanButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/141.png"))));
            pauseScanButton.setRolloverIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/141.png"))));
            pauseScanButton.setSelectedIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/131.png"))));
            pauseScanButton.setRolloverSelectedIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/131.png"))));
            pauseScanButton.setEnabled(false);
            pauseScanButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            pauseScan(currentSite);
                        }
                    });
        }
        return pauseScanButton;
    }

    private JButton getOptionsButton() {
        if (optionsButton == null) {
            optionsButton = new JButton();
            optionsButton.setToolTipText(
                    Constant.messages.getString(prefix + ".toolbar.button.options"));
            optionsButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/041.png"))));
            optionsButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Control.getSingleton()
                                    .getMenuToolsControl()
                                    .options(
                                            Constant.messages.getString(prefix + ".options.title"));
                        }
                    });
        }
        return optionsButton;
    }

    @SuppressWarnings("unchecked")
    protected JComboBox<ScanTarget> getSiteSelect() {
        if (siteSelect == null) {
            siteSelect = new JXComboBox(siteModel);
            ((JXComboBox) siteSelect)
                    .addHighlighter(
                            new FontHighlighter(
                                    (renderer, adapter) ->
                                            ((ScanTarget) adapter.getValue()).isScanned(),
                                    siteSelect.getFont().deriveFont(Font.BOLD)));
            resetSiteSelect();

            siteSelect.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            ScanTarget item = (ScanTarget) siteSelect.getSelectedItem();
                            if (item != null && siteSelect.getSelectedIndex() > 0) {
                                siteSelected(item.getSite());
                            } else {
                                siteSelected(null);
                            }
                        }
                    });
        }
        return siteSelect;
    }

    public boolean isScanning(SiteNode node, boolean incPort) {
        String site = cleanSiteName(node, incPort);
        if (site != null) {
            GenericScanner scanThread = scanMap.get(site);
            if (scanThread != null) {
                return scanThread.isRunning();
            }
        }
        return false;
    }

    public void scanAllInScope() {
        log.debug("scanSite (all in scope)");
        this.setTabFocus();
        if (this.getStartScanButton().isEnabled()) {
            this.startScan(null, true, true, null, null);
        }
    }

    public void scanAllInContext(Context context) {
        this.scanAllInContext(context, null);
    }

    public void scanAllInContext(Context context, User user) {
        log.debug("Scan all in context: {}", context.getName());
        this.setTabFocus();
        if (this.getStartScanButton().isEnabled()) {

            this.startScan(null, true, true, context, user);
        }
    }

    public void scanSite(SiteNode node, boolean incPort) {
        log.debug("scanSite {} node={}", prefix, node.getNodeName());
        this.setTabFocus();
        nodeSelected(node, incPort);
        if (currentSite != null && this.getStartScanButton().isEnabled()) {
            startScan(node, false, true, null, null);
        }
    }

    /**
     * Scans a node (URL). Equivalent to calling {@link #scanNode(SiteNode, boolean, User)} with a
     * null user.
     *
     * @param node the node
     * @param incPort the inc port
     */
    public void scanNode(SiteNode node, boolean incPort) {
        this.scanNode(node, incPort, null);
    }

    /**
     * Scans a node (URL). If a User is specified, the scan should be done from the point of view of
     * the user.
     *
     * @param node the node
     * @param incPort the inc port
     * @param user the user
     */
    public void scanNode(SiteNode node, boolean incPort, User user) {
        log.debug("scanNode{} node={}", prefix, node.getNodeName());
        this.setTabFocus();
        nodeSelected(node, incPort);
        if (currentSite != null && this.getStartScanButton().isEnabled()) {
            startScan(node, false, false, null, user);
        }
    }

    public void addSite(String site, boolean incPort) {
        site = cleanSiteName(site, incPort);

        if (!isSiteAdded(site)) {
            addSite(site);
        }
    }

    private boolean isSiteAdded(String site) {
        return getScanTarget(site) != null;
    }

    private ScanTarget getScanTarget(String site) {
        for (int i = 1; i < siteModel.getSize(); i++) {
            ScanTarget scanTarget = siteModel.getElementAt(i);
            if (scanTarget.getSite().equals(site)) {
                return scanTarget;
            }
        }
        return null;
    }

    private void addSite(String site) {
        log.debug("addSite {}", site);
        siteModel.addElement(new ScanTarget(site));
    }

    protected void siteSelected(String site) {
        siteSelected(site, false);
    }

    protected void siteSelected(String site, boolean forceRefresh) {
        if (site == null) {
            currentSite = null;
            // call with empty string instead of null because of backward compatibility reasons
            switchView("");
            resetScanState();

            return;
        }

        if (Mode.safe.equals(this.mode)) {
            // Safe mode so ignore this
            return;
        }
        if (forceRefresh || !site.equals(currentSite)) {
            if (!isSiteAdded(site)) {
                return;
            }

            ScanTarget scanTarget = getScanTarget(site);
            siteModel.setSelectedItem(scanTarget);

            GenericScanner scanThread = scanMap.get(site);
            if (scanThread == null) {
                scanThread = this.newScanThread(site, this.scanParam);
                scanMap.put(site, scanThread);
            }
            if (scanThread.isRunning()) {
                getStartScanButton().setEnabled(false);
                getStopScanButton().setEnabled(true);
                getPauseScanButton().setEnabled(true);
                getPauseScanButton().setSelected(scanThread.isPaused());
                getProgressBar().setEnabled(true);
            } else {
                resetScanButtonsAndProgressBarStates(true);
            }

            getProgressBar().setValue(scanThread.getProgress());
            getProgressBar().setMaximum(scanThread.getMaximum());
            currentSite = site;
            switchView(currentSite);
        }
        if (Mode.protect.equals(this.mode)) {
            // Check to see if in scope
            if (!Model.getSingleton().getSession().isInScope(this.getSiteNode(currentSite))) {
                resetScanButtonsAndProgressBarStates(false);
            }
        }
    }

    private void resetScanButtonsAndProgressBarStates(boolean allowStartScan) {
        getStartScanButton().setEnabled(allowStartScan);
        getStopScanButton().setEnabled(false);
        getPauseScanButton().setEnabled(false);
        getPauseScanButton().setSelected(false);
        getProgressBar().setEnabled(false);
    }

    public static String cleanSiteName(String site, boolean incPort) {
        boolean ssl = false;
        if (site.toLowerCase().startsWith("https:")) {
            ssl = true;
        }
        if (site.indexOf("//") >= 0) {
            site = site.substring(site.indexOf("//") + 2);
        }
        if (site.indexOf(" (") >= 0) {
            // Alert counts
            site = site.substring(0, site.indexOf(" ("));
        }
        // does site already contain port number ?
        if (site.indexOf(":") >= 0) {
            if (!incPort) { // We don't want the port number, strip it off
                site = site.substring(0, site.indexOf(":"));
            } // otherwise keep it
        } else { // The site does not contain the port number
            if (incPort) { // But we want it! Add it
                if (ssl) {
                    site += ":443";
                } else {
                    site += ":80";
                }
            }
        } // Site does not contain the port number and we don't want it
        // Nothing to do
        return site;
    }

    public static String cleanSiteName(SiteNode node, boolean incPort) {
        if (node != null) {
            while (node.getParent() != null && node.getParent().getParent() != null) {
                node = node.getParent();
            }
            return cleanSiteName(node.getNodeName(), incPort);
        }
        return null;
    }

    protected SiteNode getSiteNode(String siteName) {
        SiteMap siteTree = this.getExtension().getModel().getSession().getSiteTree();
        SiteNode rootNode = siteTree.getRoot();

        @SuppressWarnings("unchecked")
        Enumeration<TreeNode> en = rootNode.children();
        while (en.hasMoreElements()) {
            SiteNode sn = (SiteNode) en.nextElement();
            String nodeName = sn.getNodeName();
            if (nodeName.toLowerCase().startsWith("https:") && !hasPort(nodeName)) {
                nodeName += ":443";
            }
            if (nodeName.toLowerCase().startsWith("http:") && !hasPort(nodeName)) {
                nodeName += ":80";
            }
            if (nodeName.indexOf("//") >= 0) {
                nodeName = nodeName.substring(nodeName.indexOf("//") + 2);
            }
            if (siteName.equals(nodeName)) {
                return sn;
            }
        }
        return null;
    }

    private static boolean hasPort(String nodeName) {
        return nodeName.lastIndexOf(":") != nodeName.indexOf(":");
    }

    public void nodeSelected(SiteNode node, boolean incPort) {
        String site = cleanSiteName(node, incPort);

        if (!isSiteAdded(site)) {
            addSite(site);
        }

        siteSelected(site);
    }

    protected abstract GenericScanner newScanThread(String site, AbstractParam params);

    protected void startScan() {
        this.startScan(null, false, true, null, null);
    }

    protected void startSan(
            SiteNode startNode,
            boolean justScanInScope,
            boolean scanChildren,
            Context scanContext) {
        this.startScan(startNode, justScanInScope, scanChildren, scanContext, null);
    }

    protected void startScan(
            SiteNode startNode,
            boolean justScanInScope,
            boolean scanChildren,
            Context scanContext,
            User scanUser) {
        this.startScan(startNode, justScanInScope, scanChildren, scanContext, scanUser, null);
    }

    /**
     * Does nothing. Override to handle context specific objects
     *
     * @param scanThread the thread of the scan
     * @param contextSpecificObjects context specific objects to configure the scan
     */
    protected void handleContextSpecificObject(
            GenericScanner scanThread, Object[] contextSpecificObjects) {}

    protected void startScan(
            SiteNode startNode,
            boolean justScanInScope,
            boolean scanChildren,
            Context scanContext,
            User scanUser,
            Object[] contextSpecificObjects) {
        log.debug("startScan {} {}", prefix, startNode);
        this.getStartScanButton().setEnabled(false);
        this.getStopScanButton().setEnabled(true);
        this.getPauseScanButton().setEnabled(true);

        this.activeScans.add(currentSite);

        GenericScanner scanThread = scanMap.get(currentSite);
        if (scanThread.isStopped()) {
            // Start a new thread
            scanThread.reset();
            scanThread = this.newScanThread(currentSite, scanParam);
            scanMap.put(currentSite, scanThread);
        }
        if (justScanInScope) {
            scanThread.setStartNode(null);
            scanThread.setJustScanInScope(true);
            scanThread.setScanContext(scanContext);
        } else {
            scanThread.setJustScanInScope(false);
            scanThread.setScanContext(null);
            if (scanThread.getStartNode() == null) {
                // Quick fix - need to look at this again
                scanThread.setStartNode(startNode);
            }
        }
        scanThread.setScanAsUser(scanUser);

        if (scanContext != null) {
            scanThread.setTechSet(scanContext.getTechSet());
        }

        scanThread.setScanChildren(scanChildren);
        if (contextSpecificObjects != null) {
            this.handleContextSpecificObject(scanThread, contextSpecificObjects);
        }
        scanThread.start();
        scanMap.put(currentSite, scanThread);
        setActiveScanLabels();
        getProgressBar().setEnabled(true);
        getProgressBar().setMaximum(scanThread.getMaximum());

        ScanTarget scanTarget = getScanTarget(currentSite);
        if (scanTarget != null) {
            scanTarget.setScanned(true);
            siteModel.setSelectedItem(scanTarget);
            siteModel.elementChanged(scanTarget);
        }
        switchView(currentSite);
    }

    public void stopScan(String site) {
        log.debug("stopScan {} on {}", prefix, site);
        GenericScanner scan = scanMap.get(site);
        if (scan != null) {
            scan.stopScan();
        }
    }

    public void pauseScan(String site) {
        log.debug("pauseScan {} on  {}", prefix, site);
        GenericScanner scan = scanMap.get(site);
        if (scan != null) {
            if (scan.isPaused()) {
                scan.resumeScan();
            } else {
                scan.pauseScan();
            }
        }
    }

    public void scanFinshed(final String host) {
        if (EventQueue.isDispatchThread()) {
            scanFinshedEventHandler(host);
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                scanFinshedEventHandler(host);
                            }
                        });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void scanFinshedEventHandler(String host) {
        log.debug("scanFinished {} on {}", prefix, currentSite);
        if (host != null && host.equals(currentSite)) {
            resetScanButtonsAndProgressBarStates(true);
        }
        this.activeScans.remove(host);
        // Note the label is not now changed back from bold - so you can see which sites have been
        // scanned
        /*
        // Change the label back from bold
        siteModel.removeElement(activeSitelabel(host));
        siteModel.addElement(passiveSitelabel(host));
        if (host.equals(currentSite)) {
        	// Its changed under our feet :)
        	siteModel.setSelectedItem(passiveSitelabel(currentSite));
        }
        */
        setActiveScanLabels();
    }

    public void scanProgress(final String host, final int progress, final int maximum) {
        if (EventQueue.isDispatchThread()) {
            scanProgressEventHandler(host, progress, maximum);
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                scanProgressEventHandler(host, progress, maximum);
                            }
                        });
            } catch (InterruptedException e) {
                log.info("Interrupt scan progress update on GUI.");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void scanProgressEventHandler(String host, int progress, int maximum) {
        // log.debug("scanProgress " + prefix + " on " + currentSite + " " + progress);
        if (host.equals(currentSite)) {
            getProgressBar().setValue(progress);
            getProgressBar().setMaximum(maximum);
        }
    }

    public void reset() {
        log.debug("reset {}", prefix);
        stopAllScans();

        resetSiteSelect();
    }

    private void resetSiteSelect() {
        siteModel.removeAllElements();
        siteSelect.addItem(
                new ScanTarget(
                        Constant.messages.getString(prefix + ".toolbar.site.select"), false));
        siteSelect.setSelectedIndex(0);
    }

    private void stopAllScans() {
        for (GenericScanner scanner : scanMap.values()) {
            scanner.stopScan();
            scanner.reset();
        }
        // Allow 2 secs for the threads to stop - if we wait 'for ever' then we can get deadlocks
        for (int i = 0; i < 20; i++) {
            if (activeScans.isEmpty()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        scanMap.clear();
        activeScans.clear();

        setActiveScanLabels();
        resetScanState();
    }

    private void resetScanState() {
        resetScanButtonsAndProgressBarStates(false);
        getProgressBar().setValue(0);
    }

    public ExtensionAdaptor getExtension() {
        return extension;
    }

    public AbstractParam getScanParam() {
        return scanParam;
    }

    public GenericScanner getScanThread(String site) {
        return scanMap.get(site);
    }

    public Map<String, GenericScanner> getScanThreads() {
        return scanMap;
    }

    public boolean isCurrentSite(String site) {
        return currentSite != null && currentSite.equals(site);
    }

    public void sessionScopeChanged(Session session) {
        if (currentSite != null) {
            this.siteSelected(currentSite, true);
        }
    }

    public void sessionModeChanged(Mode mode) {
        this.mode = mode;
        switch (mode) {
            case attack:
            case standard:
            case protect:
                getSiteSelect().setEnabled(true);
                if (currentSite != null) {
                    this.siteSelected(currentSite, true);
                }
                break;
            case safe:
                // Stop all scans
                stopAllScans();
                // And disable everything
                getSiteSelect().setEnabled(false);
        }
    }

    /**
     * Gets the current site selected on this panel.
     *
     * @return the current site
     */
    protected String getCurrentSite() {
        return this.currentSite;
    }

    protected void unload() {
        if (View.isInitialised()) {
            View.getSingleton()
                    .getMainFrame()
                    .getMainFooterPanel()
                    .removeFooterToolbarRightLabel(scanStatus.getCountLabel());
        }
    }

    protected abstract Component getWorkPanel();

    protected abstract void switchView(String site);

    private static class ScanTarget implements Comparable<ScanTarget> {

        private final String site;
        private final boolean canScan;
        private boolean scanned;

        ScanTarget(String site) {
            this(site, true);
        }

        ScanTarget(String site, boolean canScan) {
            this.site = site;
            this.canScan = canScan;
        }

        String getSite() {
            return site;
        }

        boolean isScanned() {
            return scanned;
        }

        void setScanned(boolean scanned) {
            this.scanned = canScan && scanned;
        }

        @Override
        public int compareTo(ScanTarget o) {
            if (canScan) {
                return site.compareTo(o.site);
            }
            return -1;
        }

        @Override
        public String toString() {
            return site;
        }
    }
}
