/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.parosproxy.paros.extension.option;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapSizeNumberSpinner;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The GUI database options panel.
 *
 * <p>It allows to change the following database options:
 *
 * <ul>
 *   <li>Compact - allows the database to be compacted on exit.
 *   <li>Request Body Size - the size of the request body in the 'History' database table.
 *   <li>Response Body Size - the size of the response body in the 'History' database table.
 *   <li>Recovery Log - if the recovery log should be enabled (HSQLDB option only).
 * </ul>
 *
 * @see org.parosproxy.paros.db.Database#close(boolean)
 */
public class OptionsDatabasePanel extends AbstractParamPanel {

    private static final long serialVersionUID = -7541236934312940852L;

    /** The name of the options panel. */
    private static final String NAME = Constant.messages.getString("database.optionspanel.name");

    /** The label for the compact option. */
    private static final String COMPACT_DATABASE_LABEL =
            Constant.messages.getString("database.optionspanel.option.compact.label");

    /** The label for the request body size. */
    private static final String REQUEST_BODY_SIZE_DATABASE_LABEL =
            Constant.messages.getString("database.optionspanel.option.request.body.size.label");

    /** The label for the response body size. */
    private static final String RESPONSE_BODY_SIZE_DATABASE_LABEL =
            Constant.messages.getString("database.optionspanel.option.response.body.size.label");

    /**
     * The label for the recovery log option.
     *
     * @see #getCheckRecoveryLog()
     */
    private static final String RECOVERY_LOG_LABEL =
            Constant.messages.getString("database.optionspanel.option.recoveryLog.label");

    /**
     * The tool tip for the recovery log option.
     *
     * @see #getCheckRecoveryLog()
     */
    private static final String RECOVERY_LOG_TOOL_TIP =
            Constant.messages.getString("database.optionspanel.option.recoveryLog.tooltip");

    /** The check box used to select/deselect the compact option. */
    private JCheckBox checkBoxCompactDatabase = null;

    /** The spinner to select the size of the request body in the History table */
    private ZapSizeNumberSpinner spinnerRequestBodySize = null;

    /** The spinner to select the size of the response body in the History table */
    private ZapSizeNumberSpinner spinnerResponseBodySize = null;

    private JCheckBox checkBoxNewSessionPrompt = null;

    private JComboBox<String> comboNewSessionOption = null;

    /** The check box used to enabled/disable database's recovery log option. */
    private JCheckBox checkBoxRecoveryLog = null;

    public OptionsDatabasePanel() {
        super();
        setName(NAME);

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

        java.awt.GridBagConstraints gridBagConstraintsLabelRequestBodySize =
                new GridBagConstraints();
        gridBagConstraintsLabelRequestBodySize.gridx = 0;
        gridBagConstraintsLabelRequestBodySize.gridy = 2;
        gridBagConstraintsLabelRequestBodySize.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraintsLabelRequestBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsLabelRequestBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsLabelRequestBodySize.weightx = 0.5D;

        java.awt.GridBagConstraints gridBagConstraintsRequestBodySize = new GridBagConstraints();
        gridBagConstraintsRequestBodySize.gridx = 1;
        gridBagConstraintsRequestBodySize.gridy = 2;
        gridBagConstraintsRequestBodySize.weightx = 0.5D;
        gridBagConstraintsRequestBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsRequestBodySize.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraintsRequestBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsRequestBodySize.ipadx = 50;

        java.awt.GridBagConstraints gridBagConstraintsLabelResponseBodySize =
                new GridBagConstraints();
        gridBagConstraintsLabelResponseBodySize.gridx = 0;
        gridBagConstraintsLabelResponseBodySize.gridy = 3;
        gridBagConstraintsLabelResponseBodySize.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraintsLabelResponseBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsLabelResponseBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsLabelResponseBodySize.weightx = 0.5D;

        java.awt.GridBagConstraints gridBagConstraintsResponseBodySize = new GridBagConstraints();
        gridBagConstraintsResponseBodySize.gridx = 1;
        gridBagConstraintsResponseBodySize.gridy = 3;
        gridBagConstraintsResponseBodySize.weightx = 0.5D;
        gridBagConstraintsResponseBodySize.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraintsResponseBodySize.insets = new java.awt.Insets(2, 2, 2, 2);
        gridBagConstraintsResponseBodySize.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraintsResponseBodySize.ipadx = 50;

        javax.swing.JLabel jLabelRequestBodySize = new JLabel();
        javax.swing.JLabel jLabelResponseBodySize = new JLabel();
        jLabelRequestBodySize.setText(REQUEST_BODY_SIZE_DATABASE_LABEL);
        jLabelResponseBodySize.setText(RESPONSE_BODY_SIZE_DATABASE_LABEL);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(2, 2, 2, 2));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(getCheckBoxCompactDatabase(), gbc);
        panel.add(
                getCheckRecoveryLog(), LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(2, 2, 2, 2)));
        panel.add(jLabelRequestBodySize, gridBagConstraintsLabelRequestBodySize);
        panel.add(getRequestBodySize(), gridBagConstraintsRequestBodySize);
        panel.add(jLabelResponseBodySize, gridBagConstraintsLabelResponseBodySize);
        panel.add(getResponseBodySize(), gridBagConstraintsResponseBodySize);
        panel.add(
                getCheckBoxNewSessionPrompt(),
                LayoutHelper.getGBC(0, 4, 2, 1.0D, new Insets(2, 2, 2, 2)));
        panel.add(
                new JLabel(
                        Constant.messages.getString(
                                "database.optionspanel.option.newsessionopt.label")),
                LayoutHelper.getGBC(0, 5, 1, 0.5D, new Insets(2, 2, 2, 2)));
        panel.add(
                comboNewSessionOption(),
                LayoutHelper.getGBC(1, 5, 1, 1.0D, new Insets(2, 2, 2, 2)));
        add(panel);
    }

    private JCheckBox getCheckBoxCompactDatabase() {
        if (checkBoxCompactDatabase == null) {
            checkBoxCompactDatabase = new JCheckBox(COMPACT_DATABASE_LABEL);
        }
        return checkBoxCompactDatabase;
    }

    private JCheckBox getCheckRecoveryLog() {
        if (checkBoxRecoveryLog == null) {
            checkBoxRecoveryLog = new JCheckBox(RECOVERY_LOG_LABEL);
            checkBoxRecoveryLog.setToolTipText(RECOVERY_LOG_TOOL_TIP);
        }
        return checkBoxRecoveryLog;
    }

    private ZapSizeNumberSpinner getRequestBodySize() {
        if (spinnerRequestBodySize == null) {
            spinnerRequestBodySize = new ZapSizeNumberSpinner(16777216);
        }
        return spinnerRequestBodySize;
    }

    private JCheckBox getCheckBoxNewSessionPrompt() {
        if (checkBoxNewSessionPrompt == null) {
            checkBoxNewSessionPrompt =
                    new JCheckBox(
                            Constant.messages.getString(
                                    "database.optionspanel.option.newsessionprompt.label"));
        }
        return checkBoxNewSessionPrompt;
    }

    private JComboBox<String> comboNewSessionOption() {
        if (comboNewSessionOption == null) {
            comboNewSessionOption = new JComboBox<>();
            // Note that these need to be in the order specified in
            // org.parosproxy.paros.common.AbstractParam.DatabaseParam
            comboNewSessionOption.addItem(
                    Constant.messages.getString(
                            "database.optionspanel.option.newsessionopt.unspecified"));
            comboNewSessionOption.addItem(
                    Constant.messages.getString(
                            "database.optionspanel.option.newsessionopt.timestamped"));
            comboNewSessionOption.addItem(
                    Constant.messages.getString(
                            "database.optionspanel.option.newsessionopt.userspec"));
            comboNewSessionOption.addItem(
                    Constant.messages.getString(
                            "database.optionspanel.option.newsessionopt.temporary"));
        }
        return comboNewSessionOption;
    }

    private ZapSizeNumberSpinner getResponseBodySize() {
        if (spinnerResponseBodySize == null) {
            spinnerResponseBodySize = new ZapSizeNumberSpinner(16777216);
        }
        return spinnerResponseBodySize;
    }

    @Override
    public void initParam(Object obj) {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();

        checkBoxCompactDatabase.setSelected(param.isCompactDatabase());
        spinnerRequestBodySize.setValue(param.getRequestBodySize());
        spinnerResponseBodySize.setValue(param.getResponseBodySize());
        checkBoxNewSessionPrompt.setSelected(param.isNewSessionPrompt());
        comboNewSessionOption.setSelectedIndex(param.getNewSessionOption());
        checkBoxRecoveryLog.setSelected(param.isRecoveryLogEnabled());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        final OptionsParam options = (OptionsParam) obj;
        final DatabaseParam param = options.getDatabaseParam();

        param.setCompactDatabase(checkBoxCompactDatabase.isSelected());
        param.setRequestBodySize(spinnerRequestBodySize.getValue());
        param.setResponseBodySize(spinnerResponseBodySize.getValue());
        param.setNewSessionPrompt(checkBoxNewSessionPrompt.isSelected());
        param.setNewSessionOption(comboNewSessionOption.getSelectedIndex());
        param.setRecoveryLogEnabled(checkBoxRecoveryLog.isSelected());
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.database";
    }
}
