/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.utils.ZapLabel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.widgets.ContextSelectComboBox;

/**
 * An abstract class which allows simple 'Field = Value' dialogs to be created with the minimal
 * amount of 'boiler plate' code.
 *
 * @author psiinon
 */
@SuppressWarnings("serial")
public abstract class StandardFieldsDialog extends AbstractDialog {

    private static final Logger logger = LogManager.getLogger(StandardFieldsDialog.class);

    private static final long serialVersionUID = 1L;
    private static final EmptyBorder FULL_BORDER = new EmptyBorder(8, 8, 8, 8);
    private static final EmptyBorder TOP_BOTTOM_BORDER = new EmptyBorder(8, 0, 8, 0);

    /**
     * The main pop up menu, to be shared by the fields.
     *
     * <p>Lazily initialised.
     *
     * @see #getMainPopupMenu()
     * @see #setFieldMainPopupMenu(String)
     */
    private static JPopupMenu mainPopupMenu;

    private JPanel mainPanel = null;
    private List<JPanel> tabPanels = null;
    private List<Integer> tabOffsets = null;

    /**
     * The component used when showing the panels in tabs.
     *
     * @see #getTabComponent(JPanel)
     * @see #panelToScrollPaneMap
     */
    private JTabbedPane tabbedPane = null;

    /**
     * The map that contains the {@code JScrollPane}s of the tabs ({@code JPanel}s) that were {@link
     * #setTabScrollable(String, boolean) set as scrollable}.
     *
     * @see #getTabComponent(JPanel)
     * @see #tabbedPane
     */
    private Map<JPanel, JScrollPane> panelToScrollPaneMap;

    private double labelWeight = 0;
    private double fieldWeight = 1.0D;

    private JButton helpButton = null;
    private JButton cancelButton = null;
    private JButton saveButton = null;

    private List<Component> fieldList = new ArrayList<>();
    private Map<String, Component> fieldMap = new HashMap<>();
    private Map<String, JPanel> tabNameMap = new HashMap<>();

    /**
     * Flag that indicates whether or not the dialogue is automatically hidden when {@link #save()
     * saved}.
     *
     * @see #isHideOnSave()
     */
    private boolean hideOnSave;

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title and dimensions.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     */
    public StandardFieldsDialog(Frame owner, String titleLabel, Dimension dim) {
        this((Window) owner, titleLabel, dim);
    }

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title, dimensions and whether
     * or not it's modal.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
     */
    public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim, boolean modal) {
        this(owner, titleLabel, dim, null, modal);
    }

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title and dimensions.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     */
    public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim) {
        this(owner, titleLabel, dim, null);
    }

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title, dimensions and tab
     * names.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     * @param tabLabels the names of the tabs
     */
    public StandardFieldsDialog(Frame owner, String titleLabel, Dimension dim, String[] tabLabels) {
        this((Window) owner, titleLabel, dim, tabLabels);
    }

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title, dimensions and tab
     * names.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     * @param tabLabels the names of the tabs
     */
    public StandardFieldsDialog(
            Window owner, String titleLabel, Dimension dim, String[] tabLabels) {
        this(owner, titleLabel, dim, tabLabels, false);
    }

    /**
     * Constructs a {@code StandardFieldsDialog} with the given owner, title, dimensions, tab names
     * and whether or not it's modal.
     *
     * @param owner the owner of the dialogue
     * @param titleLabel the title of the dialogue
     * @param dim the dimensions of the dialogue
     * @param tabLabels the names of the tabs
     * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
     */
    public StandardFieldsDialog(
            Window owner, String titleLabel, Dimension dim, String[] tabLabels, boolean modal) {
        super(owner, modal);
        if (modal) {
            setModalityType(ModalityType.DOCUMENT_MODAL);
        }
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setTitle(Constant.messages.getString(titleLabel));
        this.initialize(dim, tabLabels);
        this.hideOnSave = true;
    }

    /**
     * Tells whether or not the dialogue is automatically hidden when {@link #save() saved}.
     *
     * <p>The default is {@code true}.
     *
     * @return {@code true} if the dialogue should be hidden, {@code false} otherwise.
     * @since 2.6.0
     * @see #setHideOnSave(boolean)
     */
    protected boolean isHideOnSave() {
        return hideOnSave;
    }

    /**
     * Sets whether or not the dialogue is automatically hidden when {@link #save() saved}.
     *
     * @param hideOnSave {@code true} if the dialogue should be hidden, {@code false} otherwise.
     * @since 2.6.0
     * @see #isHideOnSave()
     */
    protected void setHideOnSave(boolean hideOnSave) {
        this.hideOnSave = hideOnSave;
    }

    private boolean isTabbed() {
        return tabPanels != null;
    }

    private void initialize(Dimension dim, String[] tabLabels) {
        this.setLayout(new GridBagLayout());
        this.setSize(dim);

        if (tabLabels == null) {
            this.initializeSinglePane(dim);
        } else {
            this.initializeTabbed(dim, tabLabels);
        }

        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        handleCloseAction();
                    }
                });

        //  Handle escape key to close the dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction =
                new AbstractAction() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleCloseAction();
                    }
                };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    private void handleCloseAction() {
        if (hasCancelSaveButtons()) {
            cancelPressed();
            return;
        }

        savePressed();
    }

    public void setXWeights(double labelWeight, double fieldWeight) {
        this.labelWeight = labelWeight;
        this.fieldWeight = fieldWeight;
    }

    private void initializeTabbed(Dimension dim, String[] tabLabels) {

        this.tabPanels = new ArrayList<>();
        this.tabOffsets = new ArrayList<>();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(TOP_BOTTOM_BORDER);
        contentPanel.setPreferredSize(dim);
        this.setContentPane(contentPanel);

        tabbedPane = new JTabbedPane();
        panelToScrollPaneMap = new HashMap<>();

        initContentPanel(contentPanel, tabbedPane, getExtraButtons(), getHelpIndex());

        for (String label : tabLabels) {
            JPanel tabPanel = new JPanel();
            tabPanel.setLayout(new GridBagLayout());
            tabPanel.setBorder(FULL_BORDER);
            tabbedPane.addTab(Constant.messages.getString(label), tabPanel);
            this.tabNameMap.put(label, tabPanel);
            this.tabPanels.add(tabPanel);
            this.tabOffsets.add(0);
        }
    }

    private void initContentPanel(
            JPanel contentPanel, JComponent component, JButton[] extraButtons, String helpIndex) {
        if (extraButtons == null) {
            contentPanel.add(component, LayoutHelper.getGBC(0, 0, 4, 1.0D, 1.0D));
            if (helpIndex != null) {
                contentPanel.add(getHelpButton(helpIndex), LayoutHelper.getGBC(0, 1, 1, 0.0D));
            }
            contentPanel.add(new JLabel(), LayoutHelper.getGBC(1, 1, 1, 1.0D)); // spacer
            contentPanel.add(getSaveButton(), LayoutHelper.getGBC(2, 1, 1, 0.0D));
            if (hasCancelSaveButtons()) {
                contentPanel.add(getCancelButton(), LayoutHelper.getGBC(3, 1, 1, 0.0D));
            }
        } else {
            contentPanel.add(
                    component, LayoutHelper.getGBC(0, 0, 4 + extraButtons.length, 1.0D, 1.0D));
            if (helpIndex != null) {
                contentPanel.add(getHelpButton(helpIndex), LayoutHelper.getGBC(0, 1, 1, 0.0D));
            }
            contentPanel.add(new JLabel(), LayoutHelper.getGBC(1, 1, 1, 1.0D)); // spacer
            contentPanel.add(getSaveButton(), LayoutHelper.getGBC(2, 1, 1, 0.0D));
            int x = 3;
            for (JButton button : extraButtons) {
                contentPanel.add(button, LayoutHelper.getGBC(x, 1, 1, 0.0D));
                x++;
            }
            if (hasCancelSaveButtons()) {
                contentPanel.add(getCancelButton(), LayoutHelper.getGBC(x, 1, 1, 0.0D));
            }
        }
    }

    /*
     * Always returns true, meaning that by default the dialog will have Cancel and Save buttons.
     * Override to return false for one Close button instead - the save() method will still be called
     */
    public boolean hasCancelSaveButtons() {
        return true;
    }

    /*
     * Always returns null, meaning there is no associated help page.
     * Change to return the help index for a help button to be added to the dialog.
     */
    public String getHelpIndex() {
        return null;
    }

    private void initializeSinglePane(Dimension dim) {

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(FULL_BORDER);
        contentPanel.setPreferredSize(dim);
        this.setContentPane(contentPanel);

        initContentPanel(contentPanel, getMainPanel(), getExtraButtons(), getHelpIndex());
    }

    public String getSaveButtonText() {
        if (hasCancelSaveButtons()) {
            return Constant.messages.getString("all.button.save");
        }
        return Constant.messages.getString("all.button.close");
    }

    /**
     * Override if you need to add extra buttons inbetween the Cancel and Save ones
     *
     * @return an array with the extra buttons, or {@code null} if none needed.
     */
    public JButton[] getExtraButtons() {
        return null;
    }

    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setText(this.getSaveButtonText());
            saveButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            savePressed();
                        }
                    });
        }
        return saveButton;
    }

    private void savePressed() {
        if (!validateFieldsCustomMessage()) {
            return;
        }

        String errorMsg = validateFields();
        if (errorMsg != null) {
            View.getSingleton().showWarningDialog(this, errorMsg);
            return;
        }
        save();

        if (isHideOnSave()) {
            setVisible(false);
        }
    }

    /**
     * Called when the dialogue is saved, allowing to validate the fields and show custom error
     * messages (as opposed to validations using the method {@code validateFields()}, which only
     * allows to show a simple message). If this method returns {@code false} no further action is
     * taken, if it returns {@code true} the normal validation is performed by calling {@code
     * validateFields()}.
     *
     * <p>By default returns {@code true}.
     *
     * @return {@code true} if the fields are valid, {@code false} otherwise.
     * @see #validateFields()
     * @see #save()
     * @since 2.4.0
     */
    protected boolean validateFieldsCustomMessage() {
        return true;
    }

    public String getCancelButtonText() {
        return Constant.messages.getString("all.button.cancel");
    }

    /**
     * Called when the dialogue is cancelled (if it {@link #hasCancelSaveButtons() has a cancel/save
     * button}), for example, Cancel button is pressed or the dialogue is closed through the window
     * decorations.
     *
     * <p>Hides the dialogue by default.
     */
    public void cancelPressed() {
        setVisible(false);
    }

    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText(this.getCancelButtonText());

            cancelButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            cancelPressed();
                        }
                    });
        }
        return cancelButton;
    }

    private JButton getHelpButton(final String helpIndex) {
        if (helpButton == null) {
            helpButton = new JButton();
            helpButton.setIcon(ExtensionHelp.getHelpIcon());
            helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));

            helpButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            ExtensionHelp.showHelp(helpIndex);
                        }
                    });
        }
        return helpButton;
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
        }
        return mainPanel;
    }

    private void addPadding(JPanel panel, int indexy) {
        panel.add(
                new JLabel(""),
                LayoutHelper.getGBC(
                        0, indexy, 1, 0.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
    }

    public void addPadding() {
        validateNotTabbed();
        this.addPadding(this.getMainPanel(), this.fieldList.size());
    }

    /**
     * Validates that the dialogue is not tabbed.
     *
     * @throws IllegalArgumentException if the dialogue was initialised with tabs.
     * @see #validateTabbed(int)
     */
    private void validateNotTabbed() {
        if (isTabbed()) {
            throw new IllegalArgumentException(
                    "Initialised as a tabbed dialog - must use method with tab parameters");
        }
    }

    private void incTabOffset(int tabIndex) {
        this.tabOffsets.set(tabIndex, tabOffsets.get(tabIndex) + 1);
    }

    public void addPadding(int tabIndex) {
        validateTabbed(tabIndex);
        this.addPadding(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex));
        incTabOffset(tabIndex);
    }

    /**
     * Validates that the dialogue is tabbed and the given tab index is valid.
     *
     * @param tabIndex the index of the tab to validate.
     * @throws IllegalArgumentException if the dialogue was not initialised with tabs or if no tab
     *     exists with the given index.
     * @see #validateNotTabbed()
     */
    private void validateTabbed(int tabIndex) {
        if (!isTabbed()) {
            throw new IllegalArgumentException(
                    "Not initialised as a tabbed dialog - must use method without tab parameters");
        }
        if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
            throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
        }
    }

    private void addField(
            JPanel panel,
            int indexy,
            String fieldLabel,
            Component field,
            Component wrapper,
            double weighty) {
        if (this.fieldList.contains(field)) {
            throw new IllegalArgumentException("Field already added: " + field);
        }
        JLabel label = new JLabel(Constant.messages.getString(fieldLabel));
        label.setLabelFor(field);
        label.setVerticalAlignment(JLabel.TOP);
        panel.add(
                label,
                LayoutHelper.getGBC(
                        0,
                        indexy,
                        1,
                        labelWeight,
                        weighty,
                        GridBagConstraints.BOTH,
                        new Insets(4, 4, 4, 4)));
        panel.add(
                wrapper,
                LayoutHelper.getGBC(
                        1,
                        indexy,
                        1,
                        fieldWeight,
                        weighty,
                        GridBagConstraints.BOTH,
                        new Insets(4, 4, 4, 4)));
        this.fieldList.add(field);
        this.fieldMap.put(fieldLabel, field);

        if (indexy == 0 && panel.equals(this.getMainPanel())) {
            // First field, always grab focus
            field.requestFocusInWindow();
        }
    }

    private void addField(String fieldLabel, Component field, Component wrapper, double weighty) {
        validateNotTabbed();
        this.addField(
                this.getMainPanel(), this.fieldList.size(), fieldLabel, field, wrapper, weighty);
    }

    public void addTextField(String fieldLabel, String value) {
        addTextComponent(new ZapTextField(), fieldLabel, value);
    }

    private void addTextComponent(JTextComponent field, String fieldLabel, String value) {
        validateNotTabbed();
        setTextAndDiscardEdits(field, value);
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addTextField(int tabIndex, String fieldLabel, String value) {
        addTextComponent(tabIndex, new ZapTextField(), fieldLabel, value);
    }

    private void addTextComponent(
            int tabIndex, JTextComponent field, String fieldLabel, String value) {
        validateTabbed(tabIndex);
        setTextAndDiscardEdits(field, value);

        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        incTabOffset(tabIndex);
    }

    /**
     * Sets the given value to the given field.
     *
     * <p>The edits are discarded after setting the value, if the field is a {@link ZapTextField} or
     * {@link ZapTextArea}.
     *
     * @param field the field to set the value.
     * @param value the value to set.
     */
    private static void setTextAndDiscardEdits(JTextComponent field, String value) {
        if (value == null) {
            return;
        }

        field.setText(value);
        if (field instanceof ZapTextField) {
            ((ZapTextField) field).discardAllEdits();
        } else if (field instanceof ZapTextArea) {
            ((ZapTextArea) field).discardAllEdits();
        }
    }

    /**
     * Adds a {@link ZapLabel} field, with the given label and, optionally, the given value.
     *
     * @param fieldLabel the name of the label of the read-only text field.
     * @param value the value of the field, might be {@code null}.
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue has tabs;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @since 2.8.0
     * @see #addTextFieldReadOnly(int, String, String)
     * @see #addTextField(String, String)
     */
    public void addTextFieldReadOnly(String fieldLabel, String value) {
        addTextComponent(new ZapLabel(), fieldLabel, value);
    }

    /**
     * Adds a {@link ZapLabel} field, with the given label and, optionally, the given value, to the
     * tab with the given index.
     *
     * @param tabIndex the index of the tab where the read-only text field should be added.
     * @param fieldLabel the name of the label of the read-only text field.
     * @param value the value of the field, might be {@code null}.
     * @since 2.8.0
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue does not have tabs;
     *       <li>the dialogue has tabs but the given tab index is not valid;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @see #addTextFieldReadOnly(String, String)
     * @see #addTextField(int, String, String)
     */
    public void addTextFieldReadOnly(int tabIndex, String fieldLabel, String value) {
        addTextComponent(tabIndex, new ZapLabel(), fieldLabel, value);
    }

    /**
     * Adds a {@link JPasswordField} field, with the given label and, optionally, the given value.
     *
     * @param fieldLabel the label of the field
     * @param value the value of the field, might be {@code null}
     * @throws IllegalArgumentException if the dialogue is a tabbed dialogue
     * @since 2.6.0
     * @see #addPasswordField(int, String, String)
     * @see #getPasswordValue(String)
     */
    public void addPasswordField(String fieldLabel, String value) {
        addTextComponent(new JPasswordField(), fieldLabel, value);
    }

    /**
     * Adds a {@link JPasswordField} field to the tab with the given index, with the given label
     * and, optionally, the given value.
     *
     * @param tabIndex the index of the tab
     * @param fieldLabel the label of the field
     * @param value the value of the field, might be {@code null}
     * @throws IllegalArgumentException if the dialogue is not a tabbed dialogue or if the index
     *     does not correspond to an existing tab
     * @since 2.6.0
     * @see #addPasswordField(String, String)
     * @see #getPasswordValue(String)
     */
    public void addPasswordField(int tabIndex, String fieldLabel, String value) {
        addTextComponent(tabIndex, new JPasswordField(), fieldLabel, value);
    }

    public void addMultilineField(String fieldLabel, String value) {
        validateNotTabbed();
        ZapTextArea field = new ZapTextArea();
        field.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(field);
        setTextAndDiscardEdits(field, value);
        this.addField(fieldLabel, field, scrollPane, 1.0D);
    }

    public void addMultilineField(int tabIndex, String fieldLabel, String value) {
        validateTabbed(tabIndex);
        ZapTextArea field = new ZapTextArea();
        field.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(field);
        setTextAndDiscardEdits(field, value);
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                scrollPane,
                1.0D);
        this.incTabOffset(tabIndex);
    }

    public void addComboField(String fieldLabel, String[] choices, String value) {
        this.addComboField(fieldLabel, choices, value, false);
    }

    public void addComboField(String fieldLabel, String[] choices, String value, boolean editable) {
        validateNotTabbed();
        JComboBox<String> field = new JComboBox<>();
        field.setEditable(editable);
        for (String label : choices) {
            field.addItem(label);
        }
        if (value != null) {
            field.setSelectedItem(value);
        }
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addComboField(String fieldLabel, List<String> choices, String value) {
        this.addComboField(fieldLabel, choices, value, false);
    }

    public void addComboField(
            String fieldLabel, List<String> choices, String value, boolean editable) {
        validateNotTabbed();
        JComboBox<String> field = new JComboBox<>();
        field.setEditable(editable);
        for (String label : choices) {
            field.addItem(label);
        }
        if (value != null) {
            field.setSelectedItem(value);
        }
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addComboField(int tabIndex, String fieldLabel, String[] choices, String value) {
        this.addComboField(tabIndex, fieldLabel, choices, value, false);
    }

    public void addComboField(
            int tabIndex, String fieldLabel, String[] choices, String value, boolean editable) {
        validateTabbed(tabIndex);
        JComboBox<String> field = new JComboBox<>();
        field.setEditable(editable);
        for (String label : choices) {
            field.addItem(label);
        }
        if (value != null) {
            field.setSelectedItem(value);
        }
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    public void addComboField(int tabIndex, String fieldLabel, List<String> choices, String value) {
        this.addComboField(tabIndex, fieldLabel, choices, value, false);
    }

    public void addComboField(
            int tabIndex, String fieldLabel, List<String> choices, String value, boolean editable) {
        validateTabbed(tabIndex);
        JComboBox<String> field = new JComboBox<>();
        field.setEditable(editable);
        for (String label : choices) {
            field.addItem(label);
        }
        if (value != null) {
            field.setSelectedItem(value);
        }
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    public void addComboField(String fieldLabel, int[] choices, int value) {
        validateNotTabbed();
        JComboBox<Integer> field = new JComboBox<>();
        for (int label : choices) {
            field.addItem(label);
        }
        if (value >= 0) {
            field.setSelectedItem(value);
        }
        this.addField(fieldLabel, field, field, 0.0D);
    }

    /**
     * Adds a combo box field with the given label and model, to the tab with the given index.
     *
     * <p>Control of selection state (i.e. set/get selected item) is done through the combo box
     * model.
     *
     * @param <E> the type of the elements of the combo box model.
     * @param tabIndex the index of the tab where the combo box should be added.
     * @param fieldLabel the name of the label of the combo box field.
     * @param comboBoxModel the model to set into the combo box.
     * @since 2.6.0
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue does not have tabs;
     *       <li>the dialogue has tabs but the given tab index is not valid;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @see #addComboField(String, ComboBoxModel)
     * @see #addComboField(int, String, ComboBoxModel, boolean)
     * @see #setComboBoxModel(String, ComboBoxModel)
     */
    public <E> void addComboField(int tabIndex, String fieldLabel, ComboBoxModel<E> comboBoxModel) {
        addComboField(tabIndex, fieldLabel, comboBoxModel, false);
    }

    /**
     * Adds a combo box field, possibly editable, with the given label and model, to the tab with
     * the given index.
     *
     * <p>Control of selection state (i.e. set/get selected item) is done through the combo box
     * model.
     *
     * @param <E> the type of the elements of the combo box model.
     * @param tabIndex the index of the tab where the combo box should be added.
     * @param fieldLabel the name of the label of the combo box field.
     * @param comboBoxModel the model to set into the combo box.
     * @param editable {@code true} if the combo box should be editable, {@code false} otherwise.
     * @since 2.6.0
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue does not have tabs;
     *       <li>the dialogue has tabs but the given tab index is not valid;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @see #addComboField(String, ComboBoxModel, boolean)
     * @see #addComboField(int, String, ComboBoxModel)
     * @see #setComboBoxModel(String, ComboBoxModel)
     */
    public <E> void addComboField(
            int tabIndex, String fieldLabel, ComboBoxModel<E> comboBoxModel, boolean editable) {
        validateTabbed(tabIndex);
        JComboBox<E> field = new JComboBox<>(comboBoxModel);
        field.setEditable(editable);
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    /**
     * Adds a combo box field with the given label and model.
     *
     * <p>Control of selection state (i.e. set/get selected item) is done through the combo box
     * model.
     *
     * @param <E> the type of the elements of the combo box model.
     * @param fieldLabel the name of the label of the combo box field.
     * @param comboBoxModel the model to set into the combo box.
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue has tabs;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @since 2.6.0
     * @see #addComboField(String, ComboBoxModel, boolean)
     * @see #addComboField(int, String, ComboBoxModel)
     * @see #setComboBoxModel(String, ComboBoxModel)
     */
    public <E> void addComboField(String fieldLabel, ComboBoxModel<E> comboBoxModel) {
        addComboField(fieldLabel, comboBoxModel, false);
    }

    /**
     * Adds a combo box field, possibly editable, with the given label and model.
     *
     * <p>Control of selection state (i.e. set/get selected item) is done through the combo box
     * model.
     *
     * @param <E> the type of the elements of the combo box model.
     * @param fieldLabel the name of the label of the combo box field.
     * @param comboBoxModel the model to set into the combo box.
     * @param editable {@code true} if the combo box should be editable, {@code false} otherwise.
     * @throws IllegalArgumentException if any of the following conditions is true:
     *     <ul>
     *       <li>the dialogue has tabs;
     *       <li>a field with the given label already exists.
     *     </ul>
     *
     * @since 2.6.0
     * @see #addComboField(String, ComboBoxModel)
     * @see #addComboField(int, String, ComboBoxModel, boolean)
     * @see #setComboBoxModel(String, ComboBoxModel)
     */
    public <E> void addComboField(
            String fieldLabel, ComboBoxModel<E> comboBoxModel, boolean editable) {
        validateNotTabbed();
        JComboBox<E> field = new JComboBox<>(comboBoxModel);
        field.setEditable(editable);
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addTableField(String fieldLabel, JTable field) {
        this.addTableField(fieldLabel, field, null);
    }

    public void addTableField(JTable field, List<JButton> buttons) {
        this.addTableField(null, field, buttons);
    }

    /**
     * Add a table field.
     *
     * @param fieldLabel If null then the table will be full width
     * @param field the table field
     * @param buttons if not null then the buttons will be added to the right of the table
     */
    public void addTableField(String fieldLabel, JTable field, List<JButton> buttons) {
        validateNotTabbed();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(field);
        field.setFillsViewportHeight(true);

        // Tables are a special case - they don't have labels and are accessed via the model
        if (this.fieldList.contains(field)) {
            throw new IllegalArgumentException("Field already added: " + field);
        }

        if (buttons == null || buttons.isEmpty()) {
            if (fieldLabel == null) {
                this.getMainPanel()
                        .add(
                                scrollPane,
                                LayoutHelper.getGBC(
                                        1,
                                        this.fieldList.size(),
                                        1,
                                        fieldWeight,
                                        1.0D,
                                        GridBagConstraints.BOTH,
                                        new Insets(4, 4, 4, 4)));
            } else {
                this.addField(fieldLabel, field, scrollPane, 1.0D);
            }
        } else {
            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new GridBagLayout());
            tablePanel.add(
                    scrollPane,
                    LayoutHelper.getGBC(
                            0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            int buttonId = 0;
            for (JButton button : buttons) {
                buttonPanel.add(
                        button,
                        LayoutHelper.getGBC(
                                0,
                                buttonId++,
                                1,
                                0D,
                                0D,
                                GridBagConstraints.BOTH,
                                new Insets(2, 2, 2, 2)));
            }
            // Add spacer to force buttons to the top
            buttonPanel.add(
                    new JLabel(),
                    LayoutHelper.getGBC(
                            0,
                            buttonId++,
                            1,
                            0D,
                            1.0D,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2)));

            tablePanel.add(
                    buttonPanel,
                    LayoutHelper.getGBC(
                            1, 0, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2)));

            if (fieldLabel == null) {
                this.getMainPanel()
                        .add(
                                tablePanel,
                                LayoutHelper.getGBC(
                                        1,
                                        this.fieldList.size(),
                                        1,
                                        fieldWeight,
                                        1.0D,
                                        GridBagConstraints.BOTH,
                                        new Insets(4, 4, 4, 4)));
            } else {
                this.addField(fieldLabel, field, tablePanel, 1.0D);
            }
        }
        this.fieldList.add(field);
    }

    public void addTableField(int tabIndex, JTable field) {
        this.addTableField(tabIndex, field, null);
    }

    public void addTableField(int tabIndex, JTable field, List<JButton> buttons) {
        validateTabbed(tabIndex);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(field);
        field.setFillsViewportHeight(true);

        // Tables are a special case - they don't have labels and are accessed via the model
        if (this.fieldList.contains(field)) {
            throw new IllegalArgumentException("Field already added: " + field);
        }
        if (buttons == null || buttons.isEmpty()) {
            this.tabPanels
                    .get(tabIndex)
                    .add(
                            scrollPane,
                            LayoutHelper.getGBC(
                                    1,
                                    this.tabOffsets.get(tabIndex),
                                    1,
                                    1.0D,
                                    1.0D,
                                    GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4)));
        } else {
            JPanel tablePanel = new JPanel();
            tablePanel.setLayout(new GridBagLayout());
            tablePanel.add(
                    scrollPane,
                    LayoutHelper.getGBC(
                            0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            int buttonId = 0;
            for (JButton button : buttons) {
                buttonPanel.add(
                        button,
                        LayoutHelper.getGBC(
                                0,
                                buttonId++,
                                1,
                                0D,
                                0D,
                                GridBagConstraints.BOTH,
                                new Insets(2, 2, 2, 2)));
            }
            // Add spacer to force buttons to the top
            buttonPanel.add(
                    new JLabel(),
                    LayoutHelper.getGBC(
                            0,
                            buttonId++,
                            1,
                            0D,
                            1.0D,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2)));

            tablePanel.add(
                    buttonPanel,
                    LayoutHelper.getGBC(
                            1, 0, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2)));

            this.tabPanels
                    .get(tabIndex)
                    .add(
                            tablePanel,
                            LayoutHelper.getGBC(
                                    1,
                                    this.tabOffsets.get(tabIndex),
                                    1,
                                    1.0D,
                                    1.0D,
                                    GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4)));
        }
        this.fieldList.add(field);
        this.incTabOffset(tabIndex);
    }

    public void setComboFields(String fieldLabel, String[] choices, String value) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) c;
            comboBox.removeAllItems();
            for (String str : choices) {
                comboBox.addItem(str);
            }
            if (value != null) {
                comboBox.setSelectedItem(value);
            }
        } else if (c == null) {
            // Ignore - could be during init
            logger.debug("No field for {}", fieldLabel);
        } else {
            handleUnexpectedFieldClass(fieldLabel, c);
        }
    }

    private static void handleUnexpectedFieldClass(String fieldLabel, Component component) {
        logger.error(
                "Unexpected field class {}: {}\n\t{}",
                fieldLabel,
                component.getClass().getCanonicalName(),
                StringUtils.join(Thread.currentThread().getStackTrace(), "\n\t"));
    }

    public void setComboFields(String fieldLabel, List<String> choices, String value) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) c;
            comboBox.removeAllItems();
            for (String str : choices) {
                comboBox.addItem(str);
            }
            if (value != null) {
                comboBox.setSelectedItem(value);
            }
        } else if (c == null) {
            // Ignore - could be during init
            logger.debug("No field for {}", fieldLabel);
        } else {
            handleUnexpectedFieldClass(fieldLabel, c);
        }
    }

    /**
     * Sets the given combo box model into the combo box with the given label.
     *
     * <p>Control of selection state (i.e. set/get selected item) is done through the combo box
     * model.
     *
     * @param <E> the type of the elements of the combo box model.
     * @param fieldLabel the name of the label of the combo box field
     * @param comboBoxModel the model to set into the combo box
     * @since 2.6.0
     * @see #addComboField(String, ComboBoxModel)
     * @see #addComboField(int, String, ComboBoxModel)
     */
    public <E> void setComboBoxModel(String fieldLabel, ComboBoxModel<E> comboBoxModel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<E> comboBox = (JComboBox<E>) c;
            comboBox.setModel(comboBoxModel);
        } else if (c == null) {
            // Ignore - could be during init
            logger.debug("No field for {}", fieldLabel);
        } else {
            handleUnexpectedFieldClass(fieldLabel, c);
        }
    }

    public void addNumberField(String fieldLabel, Integer min, Integer max, int value) {
        validateNotTabbed();
        ZapNumberSpinner field = new ZapNumberSpinner(min, value, max);
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addNumberField(
            int tabIndex, String fieldLabel, Integer min, Integer max, int value) {
        validateTabbed(tabIndex);
        ZapNumberSpinner field = new ZapNumberSpinner(min, value, max);
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    public void addCheckBoxField(String fieldLabel, boolean value) {
        validateNotTabbed();
        JCheckBox field = new JCheckBox();
        field.setSelected(value);
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addCheckBoxField(int tabIndex, String fieldLabel, boolean value) {
        validateTabbed(tabIndex);
        JCheckBox field = new JCheckBox();
        field.setSelected(value);
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    /*
     * Add a 'node select' field which provides a button for showing a Node Select Dialog and a
     * non editable field for showing the node selected
     */
    public void addNodeSelectField(
            final String fieldLabel,
            final SiteNode value,
            final boolean editable,
            final boolean allowRoot) {
        validateNotTabbed();
        final ZapTextField text = new ZapTextField();
        text.setEditable(editable);
        if (value != null) {
            text.setText(getNodeText(value));
        }
        JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
        selectButton.setIcon(
                new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    // Keep a local copy so that we can always select the last node chosen
                    SiteNode node = value;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        NodeSelectDialog nsd = new NodeSelectDialog(StandardFieldsDialog.this);
                        nsd.setAllowRoot(allowRoot);
                        SiteNode node = nsd.showDialog(this.node);
                        if (node != null) {
                            text.setText(getNodeText(node));
                            this.node = node;
                            siteNodeSelected(fieldLabel, node);
                        }
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(fieldLabel, text, panel, 0.0D);
    }

    /*
     * Add a 'node select' field which provides a button for showing a Node Select Dialog and a
     * non editable field for showing the node selected
     */
    public void addNodeSelectField(
            int tabIndex,
            final String fieldLabel,
            final SiteNode value,
            final boolean editable,
            final boolean allowRoot) {
        validateTabbed(tabIndex);
        final ZapTextField text = new ZapTextField();
        text.setEditable(editable);
        if (value != null) {
            text.setText(getNodeText(value));
        }
        JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
        selectButton.setIcon(
                new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    // Keep a local copy so that we can always select the last node chosen
                    SiteNode node = value;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        NodeSelectDialog nsd = new NodeSelectDialog(StandardFieldsDialog.this);
                        nsd.setAllowRoot(allowRoot);
                        SiteNode node = nsd.showDialog(this.node);
                        if (node != null) {
                            text.setText(getNodeText(node));
                            this.node = node;
                            siteNodeSelected(fieldLabel, node);
                        }
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                text,
                panel,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    /**
     * Add a 'node select' field to a non tabbed dialog. This includes a button for showing a Node
     * Select Dialog and a field for showing the selected node.
     *
     * @param fieldLabel the {@code I18N} key for the field label, should not be null
     * @param value the selected {@code Target} to show, can be null
     * @param editable whether the field showing selected {@code Target} is editable or not
     * @param allowRoot whether to allow root {@code SiteNode} to be selected as {@code Target}
     *     value or not
     * @since 2.8.0
     * @see #addTargetSelectField(int, String, Target, boolean, boolean)
     */
    public void addTargetSelectField(
            final String fieldLabel,
            final Target value,
            final boolean editable,
            final boolean allowRoot) {
        validateNotTabbed();
        final ZapTextField text = new ZapTextField();
        text.setEditable(editable);
        this.setTextTarget(text, value);

        JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
        selectButton.setIcon(
                new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    // Keep a local copy so that we can always select the last node chosen
                    Target target = value;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        NodeSelectDialog nsd = new NodeSelectDialog(StandardFieldsDialog.this);
                        nsd.setAllowRoot(allowRoot);
                        target = nsd.showDialog(target);
                        setTextTarget(text, target);
                        targetSelected(fieldLabel, target);
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(fieldLabel, text, panel, 0.0D);
    }

    /*
     * Add a 'node select' field which provides a button for showing a Node Select Dialog and a
     * non editable field for showing the node selected
     */
    public void addTargetSelectField(
            int tabIndex,
            final String fieldLabel,
            final Target value,
            final boolean editable,
            final boolean allowRoot) {
        validateTabbed(tabIndex);
        final ZapTextField text = new ZapTextField();
        text.setEditable(editable);
        this.setTextTarget(text, value);

        JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
        selectButton.setIcon(
                new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    // Keep a local copy so that we can always select the last node chosen
                    Target target = value;

                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        NodeSelectDialog nsd = new NodeSelectDialog(StandardFieldsDialog.this);
                        nsd.setAllowRoot(allowRoot);
                        target = nsd.showDialog(target);
                        setTextTarget(text, target);
                        targetSelected(fieldLabel, target);
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                text,
                panel,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    private void setTextTarget(ZapTextField field, Target target) {
        String text = getTargetText(target);
        if (text != null) {
            field.setText(text);
        }
    }

    /**
     * Returns the text representation of the given {@code target}.
     *
     * <p>If the {@code target} is not {@code null} it returns:
     *
     * <ol>
     *   <li>the URI, if it has a start node with an history reference;
     *   <li>"Context: " followed by context's name, if it has a context;
     *   <li>"Everything in scope", if it's only in scope.
     * </ol>
     *
     * For remaining cases it returns {@code null}.
     *
     * @param target the target whose text representation will be returned
     * @return the text representation of the given {@code target}, might be {@code null}
     * @since 2.4.2
     * @see Target#getStartNode()
     * @see Target#getContext()
     * @see Target#isInScopeOnly()
     */
    protected static String getTargetText(Target target) {
        if (target != null) {
            if (target.getStartNode() != null) {
                return getNodeText(target.getStartNode());
            } else if (target.getContext() != null) {
                return Constant.messages.getString(
                        "context.prefixName", target.getContext().getName());
            } else if (target.isInScopeOnly()) {
                return Constant.messages.getString("context.allInScope");
            }
        }
        return null;
    }

    private static String getNodeText(SiteNode node) {
        if (node != null && node.getHistoryReference() != null) {
            String url = node.getHistoryReference().getURI().toString();
            if (!node.isLeaf() && url.endsWith("/")) {
                // Strip off the slash so we don't match a leaf
                // node with the same name
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }
        return "";
    }

    public void addContextSelectField(String fieldLabel, Context selectedContext) {
        validateNotTabbed();
        ContextSelectComboBox field = new ContextSelectComboBox();
        if (selectedContext != null) {
            field.setSelectedItem(selectedContext);
        }
        this.addField(fieldLabel, field, field, 0.0D);
    }

    public void addContextSelectField(int tabIndex, String fieldLabel, Context selectedContext) {
        validateTabbed(tabIndex);
        ContextSelectComboBox field = new ContextSelectComboBox();
        if (selectedContext != null) {
            field.setSelectedItem(selectedContext);
        }

        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                field,
                field,
                0.0D);
        incTabOffset(tabIndex);
    }

    public void addFileSelectField(
            String fieldLabel, final File dir, final int mode, final FileFilter filter) {
        validateNotTabbed();
        final ZapTextField text = new ZapTextField();
        text.setEditable(false);
        if (dir != null) {
            text.setText(dir.getAbsolutePath());
        }
        final StandardFieldsDialog sfd = this;
        JButton selectButton = new JButton("...");
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        JFileChooser chooser = new JFileChooser(dir);
                        chooser.setFileSelectionMode(mode);
                        if (filter != null) {
                            chooser.setFileFilter(filter);
                        }

                        int rc = chooser.showSaveDialog(sfd);
                        if (rc == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            if (file == null) {
                                return;
                            }
                            text.setText(file.getAbsolutePath());
                        }
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(fieldLabel, text, panel, 0.0D);
    }

    public void addFileSelectField(
            int tabIndex,
            final String fieldLabel,
            final File dir,
            final int mode,
            final FileFilter filter) {
        validateTabbed(tabIndex);
        final ZapTextField text = new ZapTextField();
        text.setEditable(false);
        if (dir != null) {
            text.setText(dir.getAbsolutePath());
        }
        final StandardFieldsDialog sfd = this;
        JButton selectButton = new JButton("...");
        selectButton.addActionListener(
                new java.awt.event.ActionListener() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        JFileChooser chooser = new JFileChooser(dir);
                        chooser.setFileSelectionMode(mode);
                        if (filter != null) {
                            chooser.setFileFilter(filter);
                        }

                        int rc = chooser.showSaveDialog(sfd);
                        if (rc == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            if (file == null) {
                                return;
                            }
                            text.setText(file.getAbsolutePath());
                        }
                    }
                });
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(
                text,
                LayoutHelper.getGBC(
                        0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));
        panel.add(
                selectButton,
                LayoutHelper.getGBC(
                        1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4)));

        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                fieldLabel,
                text,
                panel,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    /**
     * Add a custom {@code Component} to a tabbed {@code StandardFieldsDialog} without any label.
     *
     * @param tabIndex the index of the tab to which the {@code Component} need to be added
     * @param component the {@code Component} to be added
     * @since 2.8.0
     * @see #addCustomComponent(Component)
     * @see #addCustomComponent(String, Component)
     * @see #addCustomComponent(int, String, Component)
     */
    public void addCustomComponent(int tabIndex, Component component) {
        validateTabbed(tabIndex);
        if (this.fieldList.contains(component)) {
            throw new IllegalArgumentException("Component already added: " + component);
        }
        this.tabPanels
                .get(tabIndex)
                .add(
                        component,
                        LayoutHelper.getGBC(
                                0,
                                this.tabOffsets.get(tabIndex),
                                2,
                                fieldWeight,
                                0.0D,
                                GridBagConstraints.BOTH,
                                new Insets(4, 4, 4, 4)));

        if (this.fieldList.isEmpty()) {
            // First field, always grab focus
            component.requestFocusInWindow();
        }
        this.fieldList.add(component);
        this.incTabOffset(tabIndex);
    }

    /**
     * Add a custom {@code Component} to a {@code StandardFieldsDialog} without any label.
     *
     * @param component the {@code Component} to be added
     * @since 2.8.0
     * @see #addCustomComponent(int, Component)
     * @see #addCustomComponent(String, Component)
     * @see #addCustomComponent(int, String, Component)
     */
    public void addCustomComponent(Component component) {
        validateNotTabbed();
        if (this.fieldList.contains(component)) {
            throw new IllegalArgumentException("Component already added: " + component);
        }
        this.getMainPanel()
                .add(
                        component,
                        LayoutHelper.getGBC(
                                0,
                                this.fieldList.size(),
                                2,
                                fieldWeight,
                                0.0D,
                                GridBagConstraints.BOTH,
                                new Insets(4, 4, 4, 4)));
        if (this.fieldList.isEmpty()) {
            // First field, always grab focus
            component.requestFocusInWindow();
        }
        this.fieldList.add(component);
    }

    /**
     * Add a custom {@code Component} to a tabbed {@code StandardFieldsDialog} with the given label.
     *
     * @param tabIndex tabIndex the index of the tab to which the {@code Component} need to be added
     * @param componentLabel the {@code I18N} key for the component label, should not be null
     * @param component the {@code Component} to be added
     * @since 2.8.0
     * @see #addCustomComponent(Component)
     * @see #addCustomComponent(int, Component)
     * @see #addCustomComponent(String, Component)
     */
    public void addCustomComponent(int tabIndex, String componentLabel, Component component) {
        validateTabbed(tabIndex);
        this.addField(
                this.tabPanels.get(tabIndex),
                this.tabOffsets.get(tabIndex),
                componentLabel,
                component,
                component,
                0.0D);
        this.incTabOffset(tabIndex);
    }

    /**
     * Add a custom {@code Component} to {@code StandardFieldsDialog} with the given label.
     *
     * @param componentLabel the {@code I18N} key for the component label, should not be null
     * @param component the {@code Component} to be added
     * @since 2.8.0
     * @see #addCustomComponent(Component)
     * @see #addCustomComponent(int, Component)
     * @see #addCustomComponent(int, String, Component)
     */
    public void addCustomComponent(String componentLabel, Component component) {
        validateNotTabbed();
        this.addField(componentLabel, component, component, 0.0D);
    }

    /**
     * Notifies that a site node was selected.
     *
     * <p>By default it does nothing.
     *
     * @param field the name of the field that triggered the selection
     * @param node the node selected
     * @see #addNodeSelectField(String, SiteNode, boolean, boolean)
     */
    public void siteNodeSelected(String field, SiteNode node) {}

    /**
     * Notifies that a target was selected.
     *
     * <p>By default it does nothing.
     *
     * @param field the name of the field that triggered the selection
     * @param target the target selected
     * @see #addTargetSelectField(int, String, Target, boolean, boolean)
     */
    public void targetSelected(String field, Target target) {}

    /**
     * Allow the caller to get the field component in order to, for example, change its properties
     *
     * @param fieldLabel the name of the field
     * @return the field, or {@code null} if there's no field with the given name
     */
    public Component getField(String fieldLabel) {
        return this.fieldMap.get(fieldLabel);
    }

    public String getStringValue(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JTextComponent) {
                return ((JTextComponent) c).getText();
            } else if (c instanceof JComboBox) {
                return (String) ((JComboBox<?>) c).getSelectedItem();
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
        return null;
    }

    /**
     * Gets the contents of a {@link JPasswordField} field.
     *
     * <p>For stronger security, it is recommended that the returned character array be cleared
     * after use by setting each character to zero.
     *
     * @param fieldLabel the label of the field
     * @return the contents of the field, {@code null} if not a {@code JPassword} field.
     * @since 2.6.0
     * @see #setFieldValue(String, String)
     * @see #addPasswordField(String, String)
     */
    public char[] getPasswordValue(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (!(c instanceof JPasswordField)) {
            return null;
        }

        return ((JPasswordField) c).getPassword();
    }

    public Context getContextValue(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof ContextSelectComboBox) {
                return ((ContextSelectComboBox) c).getSelectedContext();
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
        return null;
    }

    /**
     * Sets the (selected) context of a {@link ContextSelectComboBox} field.
     *
     * <p>The call to this method has no effect it the context is not present in the combo box.
     *
     * @param fieldLabel the label of the field
     * @param context the context to be set/selected, {@code null} to clear the selection.
     * @since 2.6.0
     * @see #getContextValue(String)
     * @see #addContextSelectField(String, Context)
     */
    public void setContextValue(String fieldLabel, Context context) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof ContextSelectComboBox) {
                ((ContextSelectComboBox) c).setSelectedItem(context);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    public void setFieldValue(String fieldLabel, String value) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JTextComponent) {
                ((JTextComponent) c).setText(value);
            } else if (c instanceof JComboBox) {
                ((JComboBox<?>) c).setSelectedItem(value);
            } else if (c instanceof JLabel) {
                ((JLabel) c).setText(value);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    public void setFieldValue(String fieldLabel, boolean value) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JCheckBox) {
                ((JCheckBox) c).setSelected(value);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    public boolean isEmptyField(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            Object value = null;
            if (c instanceof JTextComponent) {
                return ((JTextComponent) c).getDocument().getLength() == 0;
            } else if (c instanceof JComboBox) {
                value = ((JComboBox<?>) c).getSelectedItem();
            } else if (c instanceof ZapNumberSpinner) {
                value = ((ZapNumberSpinner) c).getValue();
                if ((Integer) value < 0) {
                    value = null;
                }
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
            return value == null || value.toString().length() == 0;
        }
        return true;
    }

    public int getIntValue(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof ZapNumberSpinner) {
                return ((ZapNumberSpinner) c).getValue();
            } else if (c instanceof JComboBox) {
                return (Integer) ((JComboBox<?>) c).getSelectedItem();
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
        return -1;
    }

    public void setFieldValue(String fieldLabel, int value) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof ZapNumberSpinner) {
                ((ZapNumberSpinner) c).setValue(value);
            } else if (c instanceof JComboBox) {
                ((JComboBox<?>) c).setSelectedItem(value);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    public void addReadOnlyField(String fieldLabel, String value, boolean doubleWidth) {
        validateNotTabbed();
        JLabel field = new JLabel();
        if (value != null) {
            field.setText(value);
        }
        if (doubleWidth) {
            this.getMainPanel()
                    .add(
                            field,
                            LayoutHelper.getGBC(
                                    0,
                                    this.fieldList.size(),
                                    2,
                                    0.0D,
                                    0.0D,
                                    GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4)));
            this.fieldList.add(field);
            this.fieldMap.put(fieldLabel, field);
        } else {
            this.addField(fieldLabel, field, field, 0.0D);
        }
    }

    public void addReadOnlyField(
            int tabIndex, String fieldLabel, String value, boolean doubleWidth) {
        validateTabbed(tabIndex);
        JLabel field = new JLabel();
        if (value != null) {
            field.setText(value);
        }

        if (doubleWidth) {
            JPanel panel = this.tabPanels.get(tabIndex);
            panel.add(
                    field,
                    LayoutHelper.getGBC(
                            0,
                            this.tabOffsets.get(tabIndex),
                            2,
                            1.0D,
                            0.0D,
                            GridBagConstraints.BOTH,
                            new Insets(4, 4, 4, 4)));
            this.fieldList.add(field);
            this.fieldMap.put(fieldLabel, field);
            incTabOffset(tabIndex);

        } else {
            this.addField(
                    this.tabPanels.get(tabIndex),
                    this.tabOffsets.get(tabIndex),
                    fieldLabel,
                    field,
                    field,
                    0.0D);
        }
        incTabOffset(tabIndex);
    }

    public void setCustomTabPanel(int i, JComponent panel) {
        validateTabbed(i);
        this.tabPanels
                .get(i)
                .add(panel, LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH));
    }

    public Boolean getBoolValue(String fieldLabel) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JCheckBox) {
                return ((JCheckBox) c).isSelected();
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
        return null;
    }

    public void addFieldListener(String fieldLabel, ActionListener listener) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JTextField) {
                ((JTextField) c).addActionListener(listener);
            } else if (c instanceof JComboBox) {
                ((JComboBox<?>) c).addActionListener(listener);
            } else if (c instanceof JCheckBox) {
                ((JCheckBox) c).addActionListener(listener);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    public void addFieldListener(String fieldLabel, MouseAdapter listener) {
        this.fieldMap.computeIfPresent(
                fieldLabel,
                (k, c) -> {
                    c.addMouseListener(listener);
                    return c;
                });
    }

    /**
     * Sets the given pop up menu to the field with the given label.
     *
     * <p>The pop up menu is only set to {@link JComponent} fields.
     *
     * @param fieldLabel the label of the field.
     * @param popup the pop up menu.
     * @since 2.8.0
     * @see JComponent#setComponentPopupMenu(JPopupMenu)
     */
    public void setFieldPopupMenu(String fieldLabel, JPopupMenu popup) {
        Component c = this.fieldMap.get(fieldLabel);
        if (c != null) {
            if (c instanceof JComponent) {
                ((JComponent) c).setComponentPopupMenu(popup);
            } else {
                handleUnexpectedFieldClass(fieldLabel, c);
            }
        }
    }

    /**
     * Convenience method that sets the {@link org.parosproxy.paros.view.MainPopupMenu main pop up
     * menu} to the field with the given label.
     *
     * <p>The same pop up menu instance is shared between all components.
     *
     * @param fieldLabel the label of the field.
     * @since 2.8.0
     * @see #setFieldPopupMenu(String, JPopupMenu)
     */
    public void setFieldMainPopupMenu(String fieldLabel) {
        setFieldPopupMenu(fieldLabel, getMainPopupMenu());
    }

    private static JPopupMenu getMainPopupMenu() {
        if (mainPopupMenu == null) {
            mainPopupMenu =
                    new JPopupMenu() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public void show(Component invoker, int x, int y) {
                            View.getSingleton().getPopupMenu().show(invoker, x, y);
                        }
                    };
        }
        return mainPopupMenu;
    }

    public void removeAllFields() {
        if (this.isTabbed()) {
            for (JPanel panel : this.tabPanels) {
                panel.removeAll();
            }
            Collections.fill(tabOffsets, 0);
        } else {
            this.getMainPanel().removeAll();
        }
        this.fieldList.clear();
        this.fieldMap.clear();
    }

    public void requestTabFocus(int tabIndex) {
        validateTabbed(tabIndex);
        tabbedPane.setSelectedComponent(getTabComponent(this.tabPanels.get(tabIndex)));
    }

    /**
     * Gets the actual component added to a tab for the given panel.
     *
     * <p>Allows to transparently handle the cases where a {@link #setTabScrollable(String, boolean)
     * tab was set scrollable}, so the actual component added is a {@link JScrollPane} not the panel
     * itself. This method should be used, always, when managing the components of the {@link
     * #tabbedPane}.
     *
     * @param panel the panel whose actual component will be returned.
     * @return the actual component added to the tab.
     */
    private JComponent getTabComponent(JPanel panel) {
        JScrollPane scrollPane = panelToScrollPaneMap.get(panel);
        if (scrollPane != null) {
            return scrollPane;
        }
        return panel;
    }

    /**
     * Set the visibility of the specified tabs. The labels must have been used to create the tabs
     * in the constructor
     *
     * @param tabLabels the names of the tabs
     * @param visible {@code true} if the tabs should be visible, {@code false} otherwise
     */
    public void setTabsVisible(String[] tabLabels, boolean visible) {
        if (visible) {
            for (String label : tabLabels) {
                String name = Constant.messages.getString(label);
                JPanel tabPanel = this.tabNameMap.get(label);
                tabbedPane.addTab(name, getTabComponent(tabPanel));
            }
        } else {
            for (String label : tabLabels) {
                JPanel tabPanel = this.tabNameMap.get(label);
                this.tabbedPane.remove(getTabComponent(tabPanel));
            }
        }
    }

    /**
     * Sets whether or not the tab with given label should be scrollable (that is, added to a {@link
     * JScrollPane}).
     *
     * <p><strong>Note:</strong> The scrollable state of the tabs should be changed only to
     * non-custom panels, or to custom panels, set through {@link #setCustomTabPanel(int,
     * JComponent)}, if they are not already scrollable (otherwise it might happen that the contents
     * of the panel have two scroll bars).
     *
     * @param tabLabel the label of the tab, as set during construction of the dialogue.
     * @param scrollable {@code true} if the tab should be scrollable, {@code false} otherwise.
     * @since 2.7.0
     * @see #createTabScrollable(String, JPanel)
     * @see #isTabScrollable(String)
     */
    protected void setTabScrollable(String tabLabel, boolean scrollable) {
        JPanel tabPanel = this.tabNameMap.get(tabLabel);
        if (tabPanel == null) {
            return;
        }

        if (scrollable) {
            if (isTabScrollable(tabPanel)) {
                return;
            }

            String title = Constant.messages.getString(tabLabel);
            int tabIndex = tabbedPane.indexOfTab(title);
            boolean selected = tabbedPane.getSelectedIndex() == tabIndex;

            JScrollPane scrollPane = createTabScrollable(tabLabel, tabPanel);
            if (scrollPane == null) {
                return;
            }
            panelToScrollPaneMap.put(tabPanel, scrollPane);

            if (tabIndex == -1) {
                return;
            }

            tabbedPane.insertTab(title, null, scrollPane, null, tabIndex);
            if (selected) {
                tabbedPane.setSelectedIndex(tabIndex);
            }
            return;
        }

        if (!isTabScrollable(tabPanel)) {
            return;
        }

        String title = Constant.messages.getString(tabLabel);
        int tabIndex = tabbedPane.indexOfTab(title);
        tabbedPane.insertTab(title, null, tabPanel, null, tabIndex);
        tabbedPane.removeTabAt(tabIndex + 1);
        panelToScrollPaneMap.remove(tabPanel);
    }

    /**
     * Tells whether or not the given panel is scrollable.
     *
     * <p><strong>Note:</strong> The scrollable state returned by this method only applies to tabs
     * that were set to be (or not) scrollable through the method {@link #setTabScrollable(String,
     * boolean)}, not to "panels" added directly to a tab with {@link #setCustomTabPanel(int,
     * JComponent)}.
     *
     * @param tabPanel the panel to check.
     * @return {@code true} if the tab is scrollable, {@code false} otherwise.
     */
    private boolean isTabScrollable(JPanel tabPanel) {
        return panelToScrollPaneMap.containsKey(tabPanel);
    }

    /**
     * Tells whether or not the tab with the given label is scrollable.
     *
     * <p><strong>Note:</strong> The scrollable state returned by this method only applies to tabs
     * that were set to be (or not) scrollable through the method {@link #setTabScrollable(String,
     * boolean)}, not to "panels" added directly to a tab with {@link #setCustomTabPanel(int,
     * JComponent)}.
     *
     * @param tabLabel the label of the tab to check.
     * @return {@code true} if the tab is scrollable, {@code false} otherwise.
     * @since 2.7.0
     */
    protected boolean isTabScrollable(String tabLabel) {
        JPanel tabPanel = this.tabNameMap.get(tabLabel);
        if (tabPanel == null) {
            return false;
        }
        return isTabScrollable(tabPanel);
    }

    /**
     * Creates and returns a {@link JScrollPane} for the given panel. Called when a tab is {@link
     * #setTabScrollable(String, boolean) set to be scrollable}.
     *
     * <p>By default this method returns a {@code JScrollPane} that has the vertical and horizontal
     * scrollbars shown as needed.
     *
     * @param tabLabel the label of the tab, as set during construction of the dialogue.
     * @param tabPanel the panel of the tab that should be scrollable, never {@code null}.
     * @return the JScrollPane
     * @since 2.7.0
     */
    protected JScrollPane createTabScrollable(String tabLabel, JPanel tabPanel) {
        return new JScrollPane(
                tabPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Called when the dialogue is saved and after all validations have finished, to conclude the
     * saving process.
     *
     * <p>Whether or not the dialogue is automatically hidden depends on the value returned by
     * {@link #isHideOnSave()}.
     *
     * @see #validateFields()
     * @see #validateFieldsCustomMessage()
     * @see #getSaveButtonText()
     */
    public abstract void save();

    /**
     * Called when the dialogue is {@link #save() saved}, allowing to validate the fields and show
     * an error message (as opposed to validations using the method {@link
     * #validateFieldsCustomMessage()}, which allow to show custom/complex information or warning
     * dialogues).
     *
     * <p>If no message is returned (that is, {@code null}), the saving process continues, otherwise
     * it is shown a warning dialogue with the message.
     *
     * @return a {@code String} containing the error message to be shown to the user, or {@code
     *     null} if there are no errors.
     */
    public abstract String validateFields();
}
