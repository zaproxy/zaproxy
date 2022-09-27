/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.ext;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapLabel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.ZapTable;
import org.zaproxy.zap.view.panels.TableFilterPanel;

public class OptionsExtensionPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private ZapTable tableExt = null;
    private JScrollPane jScrollPane = null;
    private JPanel detailsPane = null;
    private ZapLabel extName = new ZapLabel();
    private JLabel addOnNameLabel = new JLabel();
    private ZapLabel addOnName = new ZapLabel();
    private ZapLabel extAuthor = new ZapLabel();
    private ZapLabel extURL = new ZapLabel();
    private JTextArea extDescription = new JTextArea();
    private OptionsExtensionTableModel extensionModel = null;
    private JScrollPane extDescScrollPane = null;
    private JButton urlLaunchButton = null;

    private static Logger log = LogManager.getLogger(OptionsExtensionPanel.class);

    public OptionsExtensionPanel(ExtensionExtension ext) {
        super();

        GridBagConstraints gbc = new GridBagConstraints();

        this.setLayout(new GridBagLayout());
        this.setSize(409, 268);
        this.setName(Constant.messages.getString("options.ext.title"));

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(new ZapHtmlLabel(Constant.messages.getString("options.ext.label.enable")), gbc);

        gbc.insets = new Insets(0, 0, 0, 0);
        this.add(new TableFilterPanel<>(getTableExtension()), gbc);

        gbc.weightx = 1.0;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(getJScrollPane(), gbc);

        gbc.weighty = 0.25;
        this.add(getDetailsPane(), gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        ExtensionParam extParam = optionsParam.getExtensionParam();

        extensionModel.setExtensionsState(extParam.getExtensionsState());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;

        optionsParam.getExtensionParam().setExtensionsState(extensionModel.getExtensionsState());
    }

    /**
     * This method initializes tableAuth
     *
     * @return javax.swing.JTable
     */
    private ZapTable getTableExtension() {
        if (tableExt == null) {
            tableExt =
                    new ZapTable() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        protected AutoScrollAction createAutoScrollAction() {
                            return null;
                        }
                    };
            tableExt.setAutoScrollOnNewValues(false);
            tableExt.setModel(getExtensionModel());
            tableExt.setRowHeight(DisplayUtils.getScaledSize(18));
            tableExt.getColumnModel()
                    .getColumn(0)
                    .setPreferredWidth(DisplayUtils.getScaledSize(70));
            tableExt.getColumnModel()
                    .getColumn(1)
                    .setPreferredWidth(DisplayUtils.getScaledSize(70));
            tableExt.getColumnModel()
                    .getColumn(2)
                    .setPreferredWidth(DisplayUtils.getScaledSize(120));
            tableExt.getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(DisplayUtils.getScaledSize(220));
            tableExt.setSortOrder(3, SortOrder.ASCENDING);

            ListSelectionListener sl =
                    new ListSelectionListener() {

                        @Override
                        public void valueChanged(ListSelectionEvent arg0) {
                            int selectedRow = tableExt.getSelectedRow();
                            if (selectedRow > -1) {
                                Extension ext =
                                        getExtensionModel()
                                                .getExtension(
                                                        tableExt.convertRowIndexToModel(
                                                                selectedRow));
                                if (ext != null) {
                                    try {
                                        extName.setText(ext.getUIName());
                                        boolean addOnExtension = ext.getAddOn() != null;
                                        addOnNameLabel.setVisible(addOnExtension);
                                        addOnName.setVisible(addOnExtension);
                                        addOnName.setText(
                                                addOnExtension ? ext.getAddOn().getName() : "");
                                        extDescription.setText(ext.getDescription());
                                        extAuthor.setText(ext.getAuthor());
                                        if (ext.getURL() != null) {
                                            extURL.setText(ext.getURL().toString());
                                            getUrlLaunchButton().setEnabled(true);
                                        } else {
                                            extURL.setText("");
                                            getUrlLaunchButton().setEnabled(false);
                                        }
                                    } catch (Exception e) {
                                        // Just to be safe
                                        log.error(e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    };

            tableExt.getSelectionModel().addListSelectionListener(sl);
            tableExt.setColumnControlVisible(true);
        }
        return tableExt;
    }
    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTableExtension());
            jScrollPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));
        }
        return jScrollPane;
    }

    private JPanel getDetailsPane() {
        if (detailsPane == null) {
            detailsPane = new JPanel();
            detailsPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));
            detailsPane.setLayout(new GridBagLayout());
            detailsPane.add(
                    new JLabel(Constant.messages.getString("options.ext.label.name")),
                    LayoutHelper.getGBC(0, 1, 1, 0.25D));
            detailsPane.add(extName, LayoutHelper.getGBC(1, 1, 1, 0.75D));
            addOnNameLabel = new JLabel(Constant.messages.getString("options.ext.label.addon"));
            detailsPane.add(addOnNameLabel, LayoutHelper.getGBC(0, 2, 1, 0.25D));
            detailsPane.add(addOnName, LayoutHelper.getGBC(1, 2, 1, 0.75D));

            addOnNameLabel.setVisible(false);
            addOnName.setVisible(false);

            detailsPane.add(
                    new JLabel(Constant.messages.getString("options.ext.label.author")),
                    LayoutHelper.getGBC(0, 3, 1, 0.25D));
            detailsPane.add(extAuthor, LayoutHelper.getGBC(1, 3, 1, 0.75D));

            detailsPane.add(
                    new JLabel(Constant.messages.getString("options.ext.label.url")),
                    LayoutHelper.getGBC(0, 4, 1, 0.25D));
            if (DesktopUtils.canOpenUrlInBrowser()) {
                detailsPane.add(
                        getUrlLaunchButton(),
                        LayoutHelper.getGBC(1, 4, 1, 0.0D, 0.0D, GridBagConstraints.NONE));
            } else {
                detailsPane.add(extURL, LayoutHelper.getGBC(1, 4, 1, 0.5D));
            }

            detailsPane.add(getExtDescJScrollPane(), LayoutHelper.getGBC(0, 5, 2, 1.0D, 1.0D));
        }
        return detailsPane;
    }

    private JButton getUrlLaunchButton() {
        if (urlLaunchButton == null) {
            urlLaunchButton =
                    new JButton(Constant.messages.getString("options.ext.button.openurl"));
            urlLaunchButton.setEnabled(false);
            urlLaunchButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            if (extURL.getText() != null) {
                                DesktopUtils.openUrlInBrowser(extURL.getText());
                            }
                        }
                    });
        }
        return urlLaunchButton;
    }

    private JScrollPane getExtDescJScrollPane() {
        if (extDescScrollPane == null) {
            extDescScrollPane = new JScrollPane();
            extDescScrollPane.setViewportView(extDescription);
            extDescription.setEditable(false);
            extDescription.setLineWrap(true);
        }
        return extDescScrollPane;
    }

    /**
     * This method initializes authModel
     *
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel
     */
    private OptionsExtensionTableModel getExtensionModel() {
        if (extensionModel == null) {
            extensionModel = new OptionsExtensionTableModel();
        }
        return extensionModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.ext";
    }
}
