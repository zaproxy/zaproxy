/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;
import org.zaproxy.zap.view.LayoutHelper;

/** The Dialog for adding and configuring a new {@link DefaultCustomPage}. */
@SuppressWarnings("serial")
class DialogAddCustomPage extends AbstractFormDialog {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7210879426146833234L;

    /** The Constant logger. */
    protected static final Logger LOGGER = LogManager.getLogger(DialogAddCustomPage.class);

    private static final String DIALOG_TITLE =
            Constant.messages.getString("custompages.dialog.add.title");
    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("custompages.dialog.add.button.confirm");

    private JPanel fieldsPanel;
    private JCheckBox enabledCheckBox;
    private JComboBox<CustomPage.Type> customPageTypesCombo;
    private JCheckBox regexCheckBox;
    private ZapTextField pageMatcherTextField;
    private JComboBox<CustomPageMatcherLocation> customPagePageMatcherLocationsCombo;
    protected Context workingContext;
    protected CustomPage customPage;

    public DialogAddCustomPage() {
        super(View.getSingleton().getSessionDialog(), DIALOG_TITLE);
    }

    /**
     * Instantiates a new dialog to add a Custom Page. The title is set based on the constant {@link
     * #DIALOG_TITLE}.
     *
     * @param owner the parent (@code Dialog}
     */
    public DialogAddCustomPage(Window owner) {
        super(owner, DIALOG_TITLE);
    }

    /**
     * Instantiates a new dialog to add a Custom Page.
     *
     * @param owner the parent (@code Dialog}
     * @param title the title for the dialog
     */
    protected DialogAddCustomPage(Dialog owner, String title) {
        super(owner, title);
    }

    /**
     * Sets the context on which the Dialog is working.
     *
     * @param context the new working context
     */
    public void setWorkingContext(Context context) {
        this.workingContext = context;
    }

    @Override
    protected void init() {
        if (this.workingContext == null) {
            throw new IllegalStateException(
                    "A working Context should be set before setting the 'Add Dialog' visible.");
        }

        // Handle escape key to close the dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction =
                new AbstractAction() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DialogAddCustomPage.this.setVisible(false);
                        DialogAddCustomPage.this.dispose();
                    }
                };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        this.setConfirmButtonEnabled(true);
        this.pack();
    }

    public void clear() {
        this.customPage = null;
        this.workingContext = null;
    }

    @Override
    protected boolean validateFields() {
        String contentMatchString = this.getPageMatcherTextField().getText();
        if (StringUtils.isBlank(contentMatchString)
                || (contentMatchString.trim().equals(".*") && getRegexCheckBox().isSelected())) {
            View.getSingleton()
                    .showWarningDialog(
                            Constant.messages.getString(
                                    "custompages.dialog.add.field.content.empty.warn"));
            return false;
        }
        return true;
    }

    @Override
    protected void performAction() {
        this.customPage =
                new DefaultCustomPage(
                        workingContext.getId(),
                        getPageMatcherTextField().getText(),
                        (CustomPageMatcherLocation)
                                getCustomPagePageMatcherLocationsCombo().getSelectedItem(),
                        getRegexCheckBox().isSelected(),
                        (CustomPage.Type) getCustomPageTypesCombo().getSelectedItem(),
                        this.getEnabledCheckBox().isSelected());
    }

    @Override
    protected void clearFields() {
        this.pageMatcherTextField.setText("");
        this.pageMatcherTextField.discardAllEdits();
        this.customPagePageMatcherLocationsCombo.setSelectedIndex(0);
        this.enabledCheckBox.setSelected(true);
        this.regexCheckBox.setSelected(false);
        this.customPageTypesCombo.setSelectedIndex(0);
        this.setConfirmButtonEnabled(true);
    }

    /**
     * Gets the custom page defined in the dialog, if any.
     *
     * @return the custom page, if correctly built or null, otherwise
     */
    public CustomPage getCustomPage() {
        return customPage;
    }

    @Override
    protected JPanel getFieldsPanel() {
        if (fieldsPanel == null) {
            fieldsPanel = new JPanel();

            fieldsPanel.setLayout(new GridBagLayout());
            fieldsPanel.setName("DialogAddCustomPage");
            Insets insets = new Insets(4, 8, 2, 4);

            JLabel paramLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "custompages.dialog.add.field.label.content"));
            paramLabel.setLabelFor(getPageMatcherTextField());
            fieldsPanel.add(paramLabel, LayoutHelper.getGBC(0, 1, 1, 0.5D, insets));
            fieldsPanel.add(getPageMatcherTextField(), LayoutHelper.getGBC(1, 1, 1, 0.5D, insets));

            JLabel customPagePageMatcherLocationLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "custompages.dialog.add.field.label.contentlocation"));
            customPagePageMatcherLocationLabel.setLabelFor(
                    getCustomPagePageMatcherLocationsCombo());
            fieldsPanel.add(
                    customPagePageMatcherLocationLabel, LayoutHelper.getGBC(0, 2, 1, 0.5D, insets));
            fieldsPanel.add(
                    getCustomPagePageMatcherLocationsCombo(),
                    LayoutHelper.getGBC(1, 2, 1, 0.5D, insets));

            JLabel regexLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "custompages.dialog.add.field.label.regex"));
            regexLabel.setLabelFor(getRegexCheckBox());
            fieldsPanel.add(regexLabel, LayoutHelper.getGBC(0, 3, 1, 0.5D, insets));
            fieldsPanel.add(getRegexCheckBox(), LayoutHelper.getGBC(1, 3, 1, 0.5D, insets));

            JLabel enabledLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "custompages.dialog.add.field.label.enabled"));
            enabledLabel.setLabelFor(getEnabledCheckBox());
            fieldsPanel.add(enabledLabel, LayoutHelper.getGBC(0, 4, 1, 0.5D, insets));
            fieldsPanel.add(getEnabledCheckBox(), LayoutHelper.getGBC(1, 4, 1, 0.5D, insets));

            JLabel customPageTypeLabel =
                    new JLabel(
                            Constant.messages.getString("custompages.dialog.add.field.label.type"));
            customPageTypeLabel.setLabelFor(getCustomPageTypesCombo());
            fieldsPanel.add(customPageTypeLabel, LayoutHelper.getGBC(0, 5, 1, 0.5D, insets));
            fieldsPanel.add(getCustomPageTypesCombo(), LayoutHelper.getGBC(1, 5, 1, 0.5D, insets));

            fieldsPanel.add(new JLabel(), LayoutHelper.getGBC(0, 10, 2, 1.0D)); // Spacer
        }
        return fieldsPanel;
    }

    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
            enabledCheckBox.setSelected(true);
        }

        return enabledCheckBox;
    }

    protected JCheckBox getRegexCheckBox() {
        if (regexCheckBox == null) {
            regexCheckBox = new JCheckBox();
        }
        return regexCheckBox;
    }

    protected ZapTextField getPageMatcherTextField() {
        if (pageMatcherTextField == null) {
            pageMatcherTextField = new ZapTextField();
        }
        pageMatcherTextField.setPreferredSize(new Dimension(280, 30));
        return pageMatcherTextField;
    }

    protected JComboBox<CustomPage.Type> getCustomPageTypesCombo() {
        if (customPageTypesCombo == null) {
            customPageTypesCombo = new JComboBox<>(CustomPage.Type.values());
        }
        return customPageTypesCombo;
    }

    protected JComboBox<CustomPageMatcherLocation> getCustomPagePageMatcherLocationsCombo() {
        if (customPagePageMatcherLocationsCombo == null) {
            customPagePageMatcherLocationsCombo =
                    new JComboBox<>(CustomPageMatcherLocation.values());
        }
        return customPagePageMatcherLocationsCombo;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }
}
