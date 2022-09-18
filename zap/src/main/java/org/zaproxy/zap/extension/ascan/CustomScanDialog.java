/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.VariantUserDefined;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.AbstractParamContainerPanel;
import org.zaproxy.zap.extension.ascan.PolicyAllCategoryPanel.ScanPolicyChangedEventListener;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.StandardFieldsDialog;
import org.zaproxy.zap.view.TechnologyTreePanel;

@SuppressWarnings("serial")
public class CustomScanDialog extends StandardFieldsDialog {

    protected static final String[] STD_TAB_LABELS = {
        "ascan.custom.tab.scope",
        "ascan.custom.tab.input",
        "ascan.custom.tab.custom",
        "ascan.custom.tab.tech",
        "ascan.custom.tab.policy",
        "ascan.custom.tab.filter"
    };

    private static final String FIELD_START = "ascan.custom.label.start";
    private static final String FIELD_POLICY = "ascan.custom.label.policy";
    private static final String FIELD_CONTEXT = "ascan.custom.label.context";
    private static final String FIELD_USER = "ascan.custom.label.user";
    private static final String FIELD_RECURSE = "ascan.custom.label.recurse";
    private static final String FIELD_ADVANCED = "ascan.custom.label.adv";

    private static final Logger logger = LogManager.getLogger(CustomScanDialog.class);
    private static final long serialVersionUID = 1L;

    private JButton[] extraButtons = null;
    private ExtensionActiveScan extension = null;

    private final ExtensionUserManagement extUserMgmt =
            Control.getSingleton().getExtensionLoader().getExtension(ExtensionUserManagement.class);

    private int headerLength = -1;
    // The index of the start of the URL path e.g. after https://www.example.com:1234/ - no point
    // attacking this
    private int urlPathStart = -1;
    private Target target = null;

    private ScannerParam scannerParam = null;

    private JPanel customPanel = null;
    private JPanel techPanel = null;
    private FilterPanel filterPanel = null;
    private ZapTextArea requestField = null;
    private JButton addCustomButton = null;
    private JButton removeCustomButton = null;
    private JList<Highlight> injectionPointList = null;
    private final DefaultListModel<Highlight> injectionPointModel = new DefaultListModel<>();
    private final JLabel customPanelStatus = new JLabel();
    private JCheckBox disableNonCustomVectors = null;
    private TechnologyTreePanel techTree;
    private TechSet techTreeState;
    private String scanPolicyName;
    private ScanPolicy scanPolicy = null;
    private OptionsVariantPanel variantPanel = null;
    private List<CustomScanPanel> customPanels = null;
    private boolean showingAdvTabs = true;
    private ScanPolicyPanel policyPanel;

    public CustomScanDialog(
            ExtensionActiveScan ext,
            String[] tabLabels,
            List<CustomScanPanel> customPanels,
            Frame owner,
            Dimension dim) {
        super(owner, "ascan.custom.title", dim, tabLabels);

        this.extension = ext;
        this.customPanels = customPanels;
        this.policyPanel =
                new ScanPolicyPanel(
                        this, extension, Constant.messages.getString("ascan.custom.tab.policy"));
        this.filterPanel = new FilterPanel();
        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosed(WindowEvent e) {
                        scanPolicy = null;
                    }
                });
        // The first time init to the default options set, after that keep own copies
        reset(false);
    }

    public void init(Target target) {
        if (target != null) {
            // If one isn't specified then leave the previously selected one
            this.target = target;
        }

        logger.debug("init {}", this.target);

        this.removeAllFields();
        this.injectionPointModel.clear();
        getRequestField().getHighlighter().removeAllHighlights();
        this.headerLength = -1;
        this.urlPathStart = -1;

        if (scanPolicyName != null && PolicyManager.policyExists(scanPolicyName)) {
            try {
                scanPolicy = extension.getPolicyManager().getPolicy(scanPolicyName);
            } catch (ConfigurationException e) {
                logger.warn("Failed to load scan policy ({}):", scanPolicyName, e);
            }
        }

        if (scanPolicy == null) {
            scanPolicy = extension.getPolicyManager().getDefaultScanPolicy();
            scanPolicyName = scanPolicy.getName();
        }

        this.addTargetSelectField(0, FIELD_START, this.target, false, false);
        this.addComboField(
                0,
                FIELD_POLICY,
                extension.getPolicyManager().getAllPolicyNames(),
                scanPolicy.getName());
        this.addComboField(0, FIELD_CONTEXT, new String[] {}, "");
        this.addComboField(0, FIELD_USER, new String[] {}, "");
        this.addCheckBoxField(0, FIELD_RECURSE, true);
        // This option is always read from the 'global' options
        this.addCheckBoxField(
                0, FIELD_ADVANCED, extension.getScannerParam().isShowAdvancedDialog());

        this.addFieldListener(
                FIELD_POLICY,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        policySelected();
                    }
                });

        this.addPadding(0);

        // Default to Recurse, so always set the warning
        customPanelStatus.setText(Constant.messages.getString("ascan.custom.status.recurse"));

        this.addFieldListener(
                FIELD_CONTEXT,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setUsers();
                        setTech();
                    }
                });

        this.addFieldListener(
                FIELD_RECURSE,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setFieldStates();
                    }
                });

        this.addFieldListener(
                FIELD_ADVANCED,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Save the adv option permanently for next time

                        setAdvancedOptions(getBoolValue(FIELD_ADVANCED));
                    }
                });

        this.getVariantPanel().initParam(scannerParam);
        this.setCustomTabPanel(1, getVariantPanel());

        // Custom vectors panel
        this.setCustomTabPanel(2, getCustomPanel());

        // Technology panel
        this.setCustomTabPanel(3, getTechPanel());

        getTechTree().refresh();
        setTechSet(techTreeState);

        policyPanel.initScanPolicy(scanPolicy);
        this.setCustomTabPanel(4, policyPanel);

        // Filter panel
        this.filterPanel.resetFilterPanel(target);
        this.setCustomTabPanel(5, this.filterPanel);

        // add custom panels
        int cIndex = 6;
        if (this.customPanels != null) {
            for (CustomScanPanel customPanel : this.customPanels) {
                this.setCustomTabPanel(cIndex, customPanel.getPanel(true));
                cIndex++;
            }
        }

        if (target != null) {
            // Set up the fields if a node has been specified, otherwise leave as previously set
            this.populateRequestField(this.target.getStartNode());
            this.targetSelected(FIELD_START, this.target);
            this.setUsers();
            this.setTech();
        }

        this.setAdvancedOptions(extension.getScannerParam().isShowAdvancedDialog());

        this.pack();
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.advascan";
    }

    private void policySelected() {
        String policyName = getStringValue(FIELD_POLICY);
        try {
            scanPolicy = extension.getPolicyManager().getPolicy(policyName);
            policyPanel.setScanPolicy(scanPolicy);

            scanPolicyName = policyName;
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void setAdvancedOptions(boolean adv) {
        this.getField(FIELD_POLICY).setEnabled(!adv);
        if (adv) {
            ((JComboBox<?>) this.getField(FIELD_POLICY))
                    .setToolTipText(Constant.messages.getString("ascan.custom.tooltip.policy"));
        } else {
            ((JComboBox<?>) this.getField(FIELD_POLICY)).setToolTipText("");
        }

        if (showingAdvTabs == adv) {
            // Nothing else to do
            return;
        }
        // Show/hide all except from the first tab
        this.setTabsVisible(
                new String[] {
                    "ascan.custom.tab.input",
                    "ascan.custom.tab.custom",
                    "ascan.custom.tab.tech",
                    "ascan.custom.tab.policy"
                },
                adv);

        if (this.customPanels != null) {
            for (CustomScanPanel customPanel : this.customPanels) {
                this.setTabsVisible(new String[] {customPanel.getLabel()}, adv);
            }
        }

        showingAdvTabs = adv;
        // Always save in the 'global' options
        extension.getScannerParam().setShowAdvancedDialog(adv);
    }

    private void populateRequestField(SiteNode node) {
        try {
            if (node == null
                    || node.getHistoryReference() == null
                    || node.getHistoryReference().getHttpMessage() == null) {
                this.getRequestField().setText("");

            } else {
                // Populate the custom vectors http pane
                HttpMessage msg = node.getHistoryReference().getHttpMessage();
                String header = msg.getRequestHeader().toString();
                StringBuilder sb = new StringBuilder();
                sb.append(header);
                this.headerLength = header.length();
                this.urlPathStart =
                        header.indexOf("/", header.indexOf("://") + 2)
                                + 1; // Ignore <METHOD> http(s)://host:port/
                sb.append(msg.getRequestBody().toString());
                this.getRequestField().setText(sb.toString());

                // Only set the recurse option if the node has children, and disable it otherwise
                JCheckBox recurseChk = (JCheckBox) this.getField(FIELD_RECURSE);
                recurseChk.setEnabled(node.getChildCount() > 0);
                recurseChk.setSelected(node.getChildCount() > 0);
            }

            this.setFieldStates();

        } catch (HttpMalformedHeaderException | DatabaseException e) {
            //
            this.getRequestField().setText("");
        }
    }

    @Override
    public void targetSelected(String field, Target node) {
        List<String> ctxNames = new ArrayList<>();
        if (node != null) {
            // The user has selected a new node
            this.target = node;
            if (node.getStartNode() != null) {
                populateRequestField(node.getStartNode());

                Session session = Model.getSingleton().getSession();
                List<Context> contexts = session.getContextsForNode(node.getStartNode());
                for (Context context : contexts) {
                    ctxNames.add(context.getName());
                }

            } else if (node.getContext() != null) {
                ctxNames.add(node.getContext().getName());
            }

            this.setTech();
        }

        this.setComboFields(FIELD_CONTEXT, ctxNames, "");
        this.getField(FIELD_CONTEXT).setEnabled(ctxNames.size() > 0);
    }

    private Context getSelectedContext() {
        String ctxName = this.getStringValue(FIELD_CONTEXT);
        if (this.extUserMgmt != null && !this.isEmptyField(FIELD_CONTEXT)) {
            Session session = Model.getSingleton().getSession();
            return session.getContext(ctxName);
        }
        return null;
    }

    private User getSelectedUser() {
        Context context = this.getSelectedContext();
        if (context != null) {
            String userName = this.getStringValue(FIELD_USER);
            List<User> users =
                    this.extUserMgmt.getContextUserAuthManager(context.getId()).getUsers();
            for (User user : users) {
                if (userName.equals(user.getName())) {
                    return user;
                }
            }
        }
        return null;
    }

    private void setUsers() {
        Context context = this.getSelectedContext();
        List<String> userNames = new ArrayList<>();
        if (context != null) {
            List<User> users =
                    this.extUserMgmt.getContextUserAuthManager(context.getId()).getUsers();
            userNames.add(""); // The default should always be 'not specified'
            for (User user : users) {
                userNames.add(user.getName());
            }
        }
        this.setComboFields(FIELD_USER, userNames, "");
        this.getField(FIELD_USER).setEnabled(userNames.size() > 1); // Theres always 1..
    }

    private void setTech() {
        Context context = this.getSelectedContext();

        setTechSet(context != null ? context.getTechSet() : null);
    }

    private void setTechSet(TechSet techSet) {
        if (techSet != null) {
            techTree.setTechSet(techSet);
        } else {
            techTree.reset();
        }
        techTreeState = techSet;
    }

    private ZapTextArea getRequestField() {
        if (requestField == null) {
            requestField = new ZapTextArea();
            requestField.setEditable(false);
            requestField.setLineWrap(true);
            requestField.getCaret().setVisible(true);
        }
        return requestField;
    }

    private OptionsVariantPanel getVariantPanel() {
        if (variantPanel == null) {
            variantPanel = new OptionsVariantPanel();
            variantPanel.setReasonVariantsDisabled(
                    Constant.messages.getString("ascan.custom.warn.disabled"));
        }

        return variantPanel;
    }

    private JPanel getCustomPanel() {
        if (customPanel == null) {
            customPanel = new JPanel(new GridBagLayout());

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setViewportView(getRequestField());

            JPanel buttonPanel = new JPanel(new GridBagLayout());

            getRequestField()
                    .addCaretListener(
                            new CaretListener() {
                                @Override
                                public void caretUpdate(CaretEvent event) {
                                    setFieldStates();
                                }
                            });

            buttonPanel.add(new JLabel(""), LayoutHelper.getGBC(0, 0, 1, 0.5)); // Spacer
            buttonPanel.add(
                    getAddCustomButton(),
                    LayoutHelper.getGBC(
                            1,
                            0,
                            1,
                            1,
                            0.0D,
                            0.0D,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            new Insets(5, 5, 5, 5)));

            buttonPanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 0.5)); // Spacer

            buttonPanel.add(new JLabel(""), LayoutHelper.getGBC(0, 1, 1, 0.5)); // Spacer
            buttonPanel.add(
                    getRemoveCustomButton(),
                    LayoutHelper.getGBC(
                            1,
                            1,
                            1,
                            1,
                            0.0D,
                            0.0D,
                            GridBagConstraints.BOTH,
                            GridBagConstraints.NORTHWEST,
                            new Insets(5, 5, 5, 5)));

            buttonPanel.add(new JLabel(""), LayoutHelper.getGBC(2, 1, 1, 0.5)); // Spacer

            JScrollPane scrollPane2 = new JScrollPane(getInjectionPointList());
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            buttonPanel.add(
                    new JLabel(Constant.messages.getString("ascan.custom.label.vectors")),
                    LayoutHelper.getGBC(0, 2, 3, 0.0D, 0.0D));

            buttonPanel.add(scrollPane2, LayoutHelper.getGBC(0, 3, 3, 1.0D, 1.0D));

            JSplitPane splitPane =
                    new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, buttonPanel);
            splitPane.setDividerLocation(550);
            customPanel.add(splitPane, LayoutHelper.getGBC(0, 0, 1, 1, 1.0D, 1.0D));
            customPanel.add(customPanelStatus, LayoutHelper.getGBC(0, 1, 1, 1, 1.0D, 0.0D));
            customPanel.add(
                    getDisableNonCustomVectors(), LayoutHelper.getGBC(0, 2, 1, 1, 1.0D, 0.0D));
        }

        return customPanel;
    }

    private JButton getAddCustomButton() {
        if (addCustomButton == null) {
            addCustomButton =
                    new JButton(Constant.messages.getString("ascan.custom.button.pt.add"));
            addCustomButton.setEnabled(false);

            addCustomButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            // Add the selected injection point
                            int userDefStart = getRequestField().getSelectionStart();
                            if (userDefStart >= 0) {
                                int userDefEnd = getRequestField().getSelectionEnd();
                                Highlighter hl = getRequestField().getHighlighter();
                                HighlightPainter painter =
                                        new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
                                try {
                                    Highlight hlt =
                                            (Highlight)
                                                    hl.addHighlight(
                                                            userDefStart, userDefEnd, painter);
                                    injectionPointModel.addElement(hlt);
                                    // Unselect the text
                                    getRequestField().setSelectionStart(userDefEnd);
                                    getRequestField().setSelectionEnd(userDefEnd);
                                    getRequestField().getCaret().setVisible(true);

                                } catch (BadLocationException e1) {
                                    logger.error(e1.getMessage(), e1);
                                }
                            }
                        }
                    });
        }
        return addCustomButton;
    }

    private JButton getRemoveCustomButton() {
        if (removeCustomButton == null) {
            removeCustomButton =
                    new JButton(Constant.messages.getString("ascan.custom.button.pt.rem"));
            removeCustomButton.setEnabled(false);

            removeCustomButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            // Remove any selected injection points
                            int userDefStart = getRequestField().getSelectionStart();
                            if (userDefStart >= 0) {
                                int userDefEnd = getRequestField().getSelectionEnd();
                                Highlighter hltr = getRequestField().getHighlighter();
                                Highlight[] hls = hltr.getHighlights();

                                if (hls != null && hls.length > 0) {
                                    for (Highlight hl : hls) {
                                        if (selectionIncludesHighlight(
                                                userDefStart, userDefEnd, hl)) {
                                            hltr.removeHighlight(hl);
                                            injectionPointModel.removeElement(hl);
                                        }
                                    }
                                }

                                // Unselect the text
                                getRequestField().setSelectionStart(userDefEnd);
                                getRequestField().setSelectionEnd(userDefEnd);
                                getRequestField().getCaret().setVisible(true);
                            }
                        }
                    });
        }

        return removeCustomButton;
    }

    private JCheckBox getDisableNonCustomVectors() {
        if (disableNonCustomVectors == null) {
            disableNonCustomVectors =
                    new JCheckBox(Constant.messages.getString("ascan.custom.label.disableiv"));
            disableNonCustomVectors.addActionListener(
                    e ->
                            getVariantPanel()
                                    .setAllInjectableAndRPC(!disableNonCustomVectors.isSelected()));
        }
        return disableNonCustomVectors;
    }

    private JPanel getTechPanel() {
        if (techPanel == null) {
            techPanel = new JPanel(new GridBagLayout());

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setViewportView(getTechTree());
            scrollPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));

            techPanel.add(scrollPane, LayoutHelper.getGBC(0, 0, 1, 1, 1.0D, 1.0D));
        }

        return techPanel;
    }

    private TechnologyTreePanel getTechTree() {
        if (techTree == null) {
            techTree =
                    new TechnologyTreePanel(
                            Constant.messages.getString("ascan.custom.tab.tech.node"));
        }
        return techTree;
    }

    private void setFieldStates() {
        int userDefStart = getRequestField().getSelectionStart();

        if (getBoolValue(FIELD_RECURSE)) {
            // Dont support custom vectors when recursing
            customPanelStatus.setText(Constant.messages.getString("ascan.custom.status.recurse"));
            getAddCustomButton().setEnabled(false);
            getRemoveCustomButton().setEnabled(false);
            getDisableNonCustomVectors().setEnabled(false);

        } else {
            customPanelStatus.setText(Constant.messages.getString("ascan.custom.status.highlight"));
            if (userDefStart >= 0) {
                int userDefEnd = getRequestField().getSelectionEnd();
                if (selectionIncludesHighlight(
                        userDefStart,
                        userDefEnd,
                        getRequestField().getHighlighter().getHighlights())) {
                    getAddCustomButton().setEnabled(false);
                    getRemoveCustomButton().setEnabled(true);

                } else if (userDefStart < urlPathStart) {
                    // No point attacking the method, hostname or port
                    getAddCustomButton().setEnabled(false);

                } else if (userDefStart < headerLength && userDefEnd > headerLength) {
                    // The users selection cross the header / body boundary - thats never going to
                    // work well
                    getAddCustomButton().setEnabled(false);
                    getRemoveCustomButton().setEnabled(false);

                } else {
                    getAddCustomButton().setEnabled(true);
                    getRemoveCustomButton().setEnabled(false);
                }

            } else {
                // Nothing selected
                getAddCustomButton().setEnabled(false);
                getRemoveCustomButton().setEnabled(false);
            }

            getDisableNonCustomVectors().setEnabled(true);
        }

        getRequestField().getCaret().setVisible(true);
    }

    private JList<Highlight> getInjectionPointList() {
        if (injectionPointList == null) {
            injectionPointList = new JList<>(injectionPointModel);
            injectionPointList.setCellRenderer(
                    new ListCellRenderer<Highlight>() {
                        @Override
                        public Component getListCellRendererComponent(
                                JList<? extends Highlight> list,
                                Highlight hlt,
                                int index,
                                boolean isSelected,
                                boolean cellHasFocus) {

                            String str = "";
                            try {
                                str =
                                        getRequestField()
                                                .getText(
                                                        hlt.getStartOffset(),
                                                        hlt.getEndOffset() - hlt.getStartOffset());
                                if (str.length() > 8) {
                                    // just show first 8 chrs (arbitrary limit;)
                                    str = str.substring(0, 8) + "..";
                                }
                            } catch (BadLocationException e) {
                                // Ignore
                            }

                            return new JLabel(
                                    "["
                                            + hlt.getStartOffset()
                                            + ","
                                            + hlt.getEndOffset()
                                            + "]: "
                                            + str);
                        }
                    });
        }

        return injectionPointList;
    }

    private boolean selectionIncludesHighlight(int start, int end, Highlight hl) {
        if (hl.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
            DefaultHighlighter.DefaultHighlightPainter ptr =
                    (DefaultHighlighter.DefaultHighlightPainter) hl.getPainter();
            if (ptr.getColor() != null && ptr.getColor().equals(Color.RED)) {
                // Test for 'RED' needed to prevent matching the users selection
                return start < hl.getEndOffset() && end > hl.getStartOffset();
            }
        }
        return false;
    }

    private boolean selectionIncludesHighlight(int start, int end, Highlight[] hls) {
        for (Highlight hl : hls) {
            if (this.selectionIncludesHighlight(start, end, hl)) {
                return true;
            }
        }
        return false;
    }

    private void reset(boolean refreshUi) {
        scannerParam = (ScannerParam) extension.getScannerParam().clone();
        techTreeState = null;

        if (refreshUi) {
            policyPanel.resetAndSetPolicy(scanPolicyName);
            init(target);
            repaint();
        }
    }

    @Override
    public String getSaveButtonText() {
        return Constant.messages.getString("ascan.custom.button.scan");
    }

    @Override
    public JButton[] getExtraButtons() {
        if (extraButtons == null) {
            JButton resetButton =
                    new JButton(Constant.messages.getString("ascan.custom.button.reset"));
            resetButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            reset(true);
                        }
                    });

            extraButtons = new JButton[] {resetButton};
        }

        return extraButtons;
    }

    /** Use the save method to launch a scan */
    @Override
    public void save() {
        List<Object> contextSpecificObjects = new ArrayList<>();

        techTreeState = getTechTree().getTechSet();

        if (!this.getBoolValue(FIELD_ADVANCED)) {
            contextSpecificObjects.add(scanPolicy);
        } else {
            contextSpecificObjects.add(policyPanel.getScanPolicy());

            if (target == null && this.customPanels != null) {
                // One of the custom scan panels must have specified a target
                for (CustomScanPanel customPanel : this.customPanels) {
                    target = customPanel.getTarget();
                    if (target != null) {
                        break;
                    }
                }
            }

            // Save all Variant configurations
            getVariantPanel().saveParam(scannerParam);

            // If all other vectors has been disabled
            // force all injectable params and rpc model to NULL
            if (getDisableNonCustomVectors().isSelected()) {
                scannerParam.setTargetParamsInjectable(0);
                scannerParam.setTargetParamsEnabledRPC(0);
            }

            if (!getBoolValue(FIELD_RECURSE) && injectionPointModel.getSize() > 0) {
                int[][] injPoints = new int[injectionPointModel.getSize()][];
                for (int i = 0; i < injectionPointModel.getSize(); i++) {
                    Highlight hl = injectionPointModel.elementAt(i);
                    injPoints[i] = new int[2];
                    injPoints[i][0] = hl.getStartOffset();
                    injPoints[i][1] = hl.getEndOffset();
                }

                try {
                    if (target != null && target.getStartNode() != null) {
                        VariantUserDefined.setInjectionPoints(
                                this.target
                                        .getStartNode()
                                        .getHistoryReference()
                                        .getURI()
                                        .toString(),
                                injPoints);

                        enableUserDefinedRPC();
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            scannerParam.setHostPerScan(extension.getScannerParam().getHostPerScan());
            scannerParam.setThreadPerHost(extension.getScannerParam().getThreadPerHost());
            scannerParam.setHandleAntiCSRFTokens(
                    extension.getScannerParam().getHandleAntiCSRFTokens());
            scannerParam.setMaxResultsToList(extension.getScannerParam().getMaxResultsToList());

            contextSpecificObjects.add(scannerParam);
            contextSpecificObjects.add(techTreeState);
            List<ScanFilter> scanFilterList = filterPanel.getScanFilters();
            for (ScanFilter scanFilter : scanFilterList) {
                contextSpecificObjects.add(scanFilter);
            }

            if (this.customPanels != null) {
                for (CustomScanPanel customPanel : this.customPanels) {
                    Object[] objs = customPanel.getContextSpecificObjects();
                    if (objs != null) {
                        for (Object obj : objs) {
                            contextSpecificObjects.add(obj);
                        }
                    }
                }
            }
        }

        target.setRecurse(this.getBoolValue(FIELD_RECURSE));

        if (target.getContext() == null && getSelectedContext() != null) {
            target.setContext(getSelectedContext());
        }

        this.extension.startScan(target, getSelectedUser(), contextSpecificObjects.toArray());
    }

    @Override
    public void setVisible(boolean show) {
        super.setVisible(show);

        if (!show) {
            scanPolicy = null;
        }
    }

    @Override
    public String validateFields() {
        if (Control.Mode.safe == Control.getSingleton().getMode()) {
            // The dialogue shouldn't be shown when in safe mode but if it is warn.
            return Constant.messages.getString("ascan.custom.notSafe.error");
        }

        String errorMessage = this.filterPanel.validateFields();
        if (errorMessage != null) {
            return errorMessage;
        }

        if (this.customPanels != null) {
            // Check all custom panels validate ok
            for (CustomScanPanel customPanel : this.customPanels) {
                errorMessage = customPanel.validateFields();
                if (errorMessage != null) {
                    return errorMessage;
                }
            }
            // Check if they support a custom target
            for (CustomScanPanel customPanel : this.customPanels) {
                Target target = customPanel.getTarget();
                if (target != null && target.isValid()) {
                    // They do, everythings ok
                    return null;
                }
            }
        }

        if (this.target == null || !this.target.isValid()) {
            return Constant.messages.getString("ascan.custom.nostart.error");
        }

        switch (Control.getSingleton().getMode()) {
            case protect:
                List<StructuralNode> nodes = target.getStartNodes();
                if (nodes != null) {
                    for (StructuralNode node : nodes) {
                        if (node instanceof StructuralSiteNode) {
                            SiteNode siteNode = ((StructuralSiteNode) node).getSiteNode();
                            if (!siteNode.isIncludedInScope()) {
                                return Constant.messages.getString(
                                        "ascan.custom.targetNotInScope.error",
                                        siteNode.getHierarchicNodeName());
                            }
                        }
                    }
                }
                break;
            default:
        }

        return null;
    }

    /** Force UserDefinedRPC setting */
    public void enableUserDefinedRPC() {
        int enabledRpc = scannerParam.getTargetParamsEnabledRPC();
        enabledRpc |= ScannerParam.RPC_USERDEF;
        scannerParam.setTargetParamsEnabledRPC(enabledRpc);
    }

    /**
     * An {@code AbstractParamContainerPanel} that allows to configure {@link ScanPolicy scan
     * policies}.
     */
    private static class ScanPolicyPanel extends AbstractParamContainerPanel {

        private static final long serialVersionUID = -7997974525786756431L;

        private PolicyAllCategoryPanel policyAllCategoryPanel = null;
        private List<PolicyCategoryPanel> categoryPanels = Collections.emptyList();
        private ScanPolicy scanPolicy;

        ScanPolicyPanel(Window parent, ExtensionActiveScan extension, String rootName) {
            super(rootName);

            this.scanPolicy = new ScanPolicy();
            String[] ROOT = {};

            policyAllCategoryPanel =
                    new PolicyAllCategoryPanel(parent, extension, scanPolicy, true);
            policyAllCategoryPanel.setName(Constant.messages.getString("ascan.custom.tab.policy"));
            policyAllCategoryPanel.addScanPolicyChangedEventListener(
                    new ScanPolicyChangedEventListener() {

                        @Override
                        public void scanPolicyChanged(ScanPolicy scanPolicy) {
                            ScanPolicyPanel.this.scanPolicy = scanPolicy;
                            for (PolicyCategoryPanel panel : categoryPanels) {
                                panel.setPluginFactory(
                                        scanPolicy.getPluginFactory(),
                                        scanPolicy.getDefaultThreshold());
                            }
                        }
                    });
            addParamPanel(null, policyAllCategoryPanel, false);

            categoryPanels = new ArrayList<>(Category.getAllNames().length);
            for (int i = 0; i < Category.getAllNames().length; i++) {
                PolicyCategoryPanel panel =
                        new PolicyCategoryPanel(
                                i,
                                this.scanPolicy.getPluginFactory(),
                                scanPolicy.getDefaultThreshold());
                addParamPanel(ROOT, Category.getName(i), panel, true);
                this.categoryPanels.add(panel);
            }
            showDialog(true);

            scanPolicy = null;
        }

        /**
         * Initialises the panel with the given policy.
         *
         * <p>Successive calls have no effect.
         *
         * @param scanPolicy the scan policy.
         * @see #setScanPolicy(ScanPolicy)
         * @see #resetAndSetPolicy(String)
         */
        void initScanPolicy(ScanPolicy scanPolicy) {
            if (this.scanPolicy != null || scanPolicy == null) {
                return;
            }

            this.scanPolicy = scanPolicy;
            setScanPolicy(scanPolicy);
        }

        public void resetAndSetPolicy(String scanPolicyName) {
            policyAllCategoryPanel.reloadPolicies(scanPolicyName);
        }

        public void setScanPolicy(ScanPolicy scanPolicy) {
            policyAllCategoryPanel.setScanPolicy(scanPolicy);
        }

        public ScanPolicy getScanPolicy() {
            return scanPolicy;
        }
    }

    /**
     * Resets the active scan dialogue to its default state.
     *
     * @since 2.5.0
     */
    void reset() {
        target = null;
        reset(true);
    }
}
