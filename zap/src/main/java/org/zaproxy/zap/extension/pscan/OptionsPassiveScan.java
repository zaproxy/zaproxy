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
package org.zaproxy.zap.extension.pscan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

@SuppressWarnings("serial")
public class OptionsPassiveScan extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private PassiveScannerList passiveScanners;

    private ScannersMultipleOptionsPanel scannersOptionsPanel;

    private OptionsPassiveScanTableModel tableModel = null;

    public OptionsPassiveScan(PassiveScannerList scannerList) {
        super();
        passiveScanners = scannerList;
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setName(Constant.messages.getString("pscan.options.name"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new ZapHtmlLabel(Constant.messages.getString("pscan.options.header")), gbc);

        scannersOptionsPanel = new ScannersMultipleOptionsPanel(getTableModel());

        gbc.weighty = 1.0;
        this.add(scannersOptionsPanel, gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        PassiveScanParam passiveScanParam = optionsParam.getParamSet(PassiveScanParam.class);
        getTableModel().setScanDefns(passiveScanParam.getAutoTagScanners());
        scannersOptionsPanel.setRemoveWithoutConfirmation(
                !passiveScanParam.isConfirmRemoveAutoTagScanner());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        PassiveScanParam passiveScanParam = optionsParam.getParamSet(PassiveScanParam.class);
        passiveScanParam.setAutoTagScanners(getTableModel().getElements());
        passiveScanParam.setConfirmRemoveAutoTagScanner(
                !scannersOptionsPanel.isRemoveWithoutConfirmation());
        passiveScanners.setAutoTagScanners(passiveScanParam.getAutoTagScanners());
    }

    /**
     * This method initializes authModel
     *
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel
     */
    private OptionsPassiveScanTableModel getTableModel() {
        if (tableModel == null) {
            tableModel = new OptionsPassiveScanTableModel();
        }
        return tableModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.pscan";
    }

    private static class ScannersMultipleOptionsPanel
            extends AbstractMultipleOptionsTablePanel<RegexAutoTagScanner> {

        private static final long serialVersionUID = 8762085355395403532L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("pscan.options.dialog.scanner.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("pscan.options.dialog.scanner.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("pscan.options.dialog.scanner.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("pscan.options.dialog.scanner.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("pscan.options.dialog.scanner.remove.checkbox.label");

        private DialogAddAutoTagScanner addDialog = null;
        private DialogModifyAutoTagScanner modifyDialog = null;

        private OptionsPassiveScanTableModel model;

        public ScannersMultipleOptionsPanel(OptionsPassiveScanTableModel model) {
            super(model);

            this.model = model;

            getTable().getColumnExt(0).setPreferredWidth(20);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public RegexAutoTagScanner showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddAutoTagScanner(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setScanners(model.getElements());
            addDialog.setVisible(true);

            RegexAutoTagScanner app = addDialog.getScanner();
            addDialog.clear();

            return app;
        }

        @Override
        public RegexAutoTagScanner showModifyDialogue(RegexAutoTagScanner e) {
            if (modifyDialog == null) {
                modifyDialog =
                        new DialogModifyAutoTagScanner(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setScanners(model.getElements());
            modifyDialog.setApp(e);
            modifyDialog.setVisible(true);

            RegexAutoTagScanner app = modifyDialog.getScanner();
            modifyDialog.clear();

            if (!app.equals(e)) {
                return app;
            }

            return null;
        }

        @Override
        public boolean showRemoveDialogue(RegexAutoTagScanner e) {
            JCheckBox removeWithoutConfirmationCheckBox =
                    new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            messages,
                            REMOVE_DIALOG_TITLE,
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                REMOVE_DIALOG_CONFIRM_BUTTON_LABEL,
                                REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                            },
                            null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());

                return true;
            }

            return false;
        }
    }
}
