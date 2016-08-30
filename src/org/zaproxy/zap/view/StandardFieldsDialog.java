/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.widgets.ContextSelectComboBox;

/**
 * An abstract class which allows simple 'Field = Value' dialogs to be created with
 * the minimal amount of 'boiler plate' code.
 * @author psiinon
 *
 */
public abstract class StandardFieldsDialog extends AbstractDialog {

	private static final Logger logger = Logger.getLogger(StandardFieldsDialog.class);

	private static final long serialVersionUID = 1L;
	private static final EmptyBorder FULL_BORDER = new EmptyBorder(8, 8, 8, 8);
	private static final EmptyBorder TOP_BOTTOM_BORDER = new EmptyBorder(8, 0, 8, 0);

	private JPanel mainPanel = null;
	private List<JPanel> tabPanels = null;
	private List<Integer> tabOffsets = null;
	private JTabbedPane tabbedPane = null;
	
	private double labelWeight = 0;
	private double fieldWeight = 1.0D;

	private JButton helpButton = null;
	private JButton cancelButton = null;
	private JButton saveButton = null;

	private List<Component> fieldList = new ArrayList<>();
	private Map<String, Component> fieldMap = new HashMap<> ();
	private Map<String, JPanel> tabNameMap = new HashMap<String, JPanel>(); 

	/**
	 * Flag that indicates whether or not the dialogue is automatically hidden when {@link #save() saved}.
	 * 
	 * @see #isHideOnSave()
	 */
	private boolean hideOnSave;

	/**
	 * For backwards binary compatibility
	 * @param owner
	 * @param titleLabel
	 * @param dim
	 */
	public StandardFieldsDialog(Frame owner, String titleLabel, Dimension dim) {
		this((Window)owner, titleLabel, dim);
	}

	public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim, boolean modal) {
		this(owner, titleLabel, dim, null, modal);
	}

	public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim) {
		this(owner, titleLabel, dim, null);
	}

	/**
	 * For backwards binary compatibility
	 * @param owner
	 * @param titleLabel
	 * @param dim
	 * @param tabLabels
	 */
	public StandardFieldsDialog(Frame owner, String titleLabel, Dimension dim, String[] tabLabels) {
		this((Window)owner, titleLabel, dim, tabLabels);
	}
	
	public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim, String[] tabLabels) {
		this(owner, titleLabel, dim, tabLabels, false);
	}

	public StandardFieldsDialog(Window owner, String titleLabel, Dimension dim, String[] tabLabels, boolean modal) {
		super(owner, modal);
		this.setTitle(Constant.messages.getString(titleLabel));
		this.setXWeights(0.4D, 0.6D);	// Looks a bit better..
		this.initialize(dim, tabLabels);
		this.hideOnSave = true;
	}

	/**
	 * Tells whether or not the dialogue is automatically hidden when {@link #save() saved}.
	 * <p>
	 * The default is {@code true}.
	 * 
	 * @return {@code true} if the dialogue should be hidden, {@code false} otherwise.
	 * @since TODO Add version
	 * @see #setHideOnSave(boolean)
	 */
	protected boolean isHideOnSave() {
		return hideOnSave;
	}

	/**
	 * Sets whether or not the dialogue is automatically hidden when {@link #save() saved}.
	 *
	 * @param hideOnSave {@code true} if the dialogue should be hidden, {@code false} otherwise.
	 * @since TODO Add version
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
		
        //  Handle escape key to close the dialog    
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
				StandardFieldsDialog.this.setVisible(false);
				StandardFieldsDialog.this.dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
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

	private void initContentPanel(JPanel contentPanel, JComponent component, JButton[] extraButtons, String helpIndex) {
		if (extraButtons == null) {
			contentPanel.add(component, LayoutHelper.getGBC(0, 0, 4, 1.0D, 1.0D));
			if (helpIndex != null) {
				contentPanel.add(getHelpButton(helpIndex), LayoutHelper.getGBC(0, 1, 1, 0.0D));
			}
			contentPanel.add(new JLabel(), LayoutHelper.getGBC(1, 1, 1, 1.0D));	// spacer
			if (hasCancelSaveButtons()) {
				contentPanel.add(getCancelButton(), LayoutHelper.getGBC(2, 1, 1, 0.0D));
			}
			contentPanel.add(getSaveButton(), LayoutHelper.getGBC(3, 1, 1, 0.0D));
		} else {
			contentPanel.add(component, LayoutHelper.getGBC(0, 0, 4 + extraButtons.length, 1.0D, 1.0D));
			if (helpIndex != null) {
				contentPanel.add(getHelpButton(helpIndex), LayoutHelper.getGBC(0, 1, 1, 0.0D));
			}
			contentPanel.add(new JLabel(), LayoutHelper.getGBC(1, 1, 1, 1.0D));	// spacer
			if (hasCancelSaveButtons()) {
				contentPanel.add(getCancelButton(), LayoutHelper.getGBC(2, 1, 1, 0.0D));
			}
			int x=3;
			for (JButton button : extraButtons) {
				contentPanel.add(button, LayoutHelper.getGBC(x, 1, 1, 0.0D));
				x++;
			}
			contentPanel.add(getSaveButton(), LayoutHelper.getGBC(x, 1, 1, 0.0D));
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
	 * @return
	 */
	public JButton[] getExtraButtons () {
		return null;
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText(this.getSaveButtonText());
			saveButton.addActionListener(new ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (!validateFieldsCustomMessage()) {
						return;
					}

					String errorMsg = validateFields();
					if (errorMsg != null) {
						View.getSingleton().showWarningDialog(StandardFieldsDialog.this, errorMsg);
						return;
					}
					save();

					if (isHideOnSave()) {
						setVisible(false);
					}
				}
			});
		}
		return saveButton;
	}

	/**
	 * Called when the dialogue is saved, allowing to validate the fields and show custom error messages (as opposed to
	 * validations using the method {@code validateFields()}, which only allows to show a simple message). If this method
	 * returns {@code false} no further action is taken, if it returns {@code true} the normal validation is performed by
	 * calling {@code validateFields()}.
	 * <p>
	 * By default returns {@code true}.
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
	 * Gets called when the cancel button is pressed - override to do anything other than just close the window
	 */
	public void cancelPressed() {
		setVisible(false);
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(this.getCancelButtonText());

			cancelButton.addActionListener(new java.awt.event.ActionListener() { 

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
			helpButton.setIcon(ExtensionHelp.HELP_ICON);
			helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));

			helpButton.addActionListener(new java.awt.event.ActionListener() { 

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
		panel.add(new JLabel(""), 
				LayoutHelper.getGBC(0, indexy, 1, 0.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
	}
	
	public void addPadding() {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		this.addPadding(this.getMainPanel(), this.fieldList.size());
	}
	
	private void incTabOffset(int tabIndex) {
		int next = this.tabOffsets.get(tabIndex)+1;
		this.tabOffsets.remove(tabIndex);
		this.tabOffsets.add(tabIndex, next);
	}

	public void addPadding(int tabIndex) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		this.addPadding(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex));
		incTabOffset(tabIndex);
	}

	private void addField(JPanel panel, int indexy, String fieldLabel, Component field, Component wrapper, double weighty) {
		if (this.fieldList.contains(field)) {
			throw new IllegalArgumentException("Field already added: " + field);
		}
		JLabel label = new JLabel(Constant.messages.getString(fieldLabel));
		label.setLabelFor(field);
		label.setVerticalAlignment(JLabel.TOP);
		panel.add(label, 
				LayoutHelper.getGBC(0, indexy, 1, labelWeight, weighty, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(wrapper,
				LayoutHelper.getGBC(1, indexy, 1, fieldWeight, weighty, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		this.fieldList.add(field);
		this.fieldMap.put(fieldLabel, field);
		
		if (indexy == 0 && panel.equals(this.getMainPanel())) {
			// First field, always grab focus
			field.requestFocusInWindow();
		}
	}

	private void addField(String fieldLabel, Component field, Component wrapper, double weighty) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		this.addField(this.getMainPanel(), this.fieldList.size(), fieldLabel, field, wrapper, weighty);
	}

	public void addTextField(String fieldLabel, String value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		ZapTextField field = new ZapTextField();
		if (value != null) {
			field.setText(value);
		}
		this.addField(fieldLabel, field, field, 0.0D);
	}

	public void addTextField(int tabIndex, String fieldLabel, String value) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		ZapTextField field = new ZapTextField();
		if (value != null) {
			field.setText(value);
		}

		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		incTabOffset(tabIndex);
	}

	/**
	 * Adds a {@link JPasswordField} field, with the given label and, optionally, the given value.
	 *
	 * @param fieldLabel the label of the field
	 * @param value the value of the field, might be {@code null}
	 * @throws IllegalArgumentException if the dialogue is a tabbed dialogue
	 * @since TODO add version
	 * @see #addPasswordField(int, String, String)
	 * @see #getPasswordValue(String)
	 */
	public void addPasswordField(String fieldLabel, String value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		JPasswordField field = new JPasswordField();
		if (value != null) {
			field.setText(value);
		}
		this.addField(fieldLabel, field, field, 0.0D);
	}

	/**
	 * Adds a {@link JPasswordField} field to the tab with the given index, with the given label and, optionally, the given
	 * value.
	 *
	 * @param tabIndex the index of the tab
	 * @param fieldLabel the label of the field
	 * @param value the value of the field, might be {@code null}
	 * @throws IllegalArgumentException if the dialogue is not a tabbed dialogue or if the index does not correspond to an
	 *			 existing tab
	 * @since TODO add version
	 * @see #addPasswordField(String, String)
	 * @see #getPasswordValue(String)
	 */
	public void addPasswordField(int tabIndex, String fieldLabel, String value) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JPasswordField field = new JPasswordField();
		if (value != null) {
			field.setText(value);
		}

		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		incTabOffset(tabIndex);
	}

	public void addMultilineField(String fieldLabel, String value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		ZapTextArea field = new ZapTextArea();
		field.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(field);
		if (value != null) {
			field.setText(value);
		}
		this.addField(fieldLabel, field, scrollPane, 1.0D);
	}

	public void addMultilineField(int tabIndex, String fieldLabel, String value) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		ZapTextArea field = new ZapTextArea();
		field.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(field);
		if (value != null) {
			field.setText(value);
		}
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, scrollPane, 1.0D);
		this.incTabOffset(tabIndex);
	}

	public void addComboField(String fieldLabel, String[] choices, String value) {
		this.addComboField(fieldLabel, choices, value, false);
	}

	public void addComboField(String fieldLabel, String[] choices, String value, boolean editable) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
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
	
	public void addComboField(String fieldLabel, List<String> choices, String value, boolean editable) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
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

	public void addComboField(int tabIndex, String fieldLabel, String[] choices, String value, boolean editable) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JComboBox<String> field = new JComboBox<>();
		field.setEditable(editable);
		for (String label : choices) {
			field.addItem(label);
		}
		if (value != null) {
			field.setSelectedItem(value);
		}
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		this.incTabOffset(tabIndex);
	}
	
	public void addComboField(int tabIndex, String fieldLabel, List<String> choices, String value) {
		this.addComboField(tabIndex, fieldLabel, choices, value, false);
	}
	
	public void addComboField(int tabIndex, String fieldLabel, List<String> choices, String value, boolean editable) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JComboBox<String> field = new JComboBox<>();
		field.setEditable(editable);
		for (String label : choices) {
			field.addItem(label);
		}
		if (value != null) {
			field.setSelectedItem(value);
		}
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		this.incTabOffset(tabIndex);
	}

	public void addComboField(String fieldLabel, int[] choices, int value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		JComboBox<Integer> field = new JComboBox<>();
		for (int label : choices) {
			field.addItem(label);
		}
		if (value >= 0) {
			field.setSelectedItem(value);
		}
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
	 * @param fieldLabel If null then the table will be full width
	 * @param field
	 * @param buttons if not null then the buttons will be added to the right of the table
	 */
	public void addTableField(String fieldLabel, JTable field, List<JButton> buttons) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(field);
		field.setFillsViewportHeight(true);
		
		// Tables are a special case - they dont have labels and are accessed via the model
		if (this.fieldList.contains(field)) {
			throw new IllegalArgumentException("Field already added: " + field);
		}
		
		if (buttons == null || buttons.size() == 0) {
			if (fieldLabel == null) {
				this.getMainPanel().add(scrollPane,
						LayoutHelper.getGBC(1, this.fieldList.size(), 1, fieldWeight, 1.0D, 
								GridBagConstraints.BOTH, new Insets(4,4,4,4)));
			} else {
				this.addField(fieldLabel, field, scrollPane, 1.0D);
			}
		} else {
			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			tablePanel.add(scrollPane,
					LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			int buttonId = 0;
			for (JButton button : buttons) {
				buttonPanel.add(button,
						LayoutHelper.getGBC(0, buttonId++, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));
			}
			// Add spacer to force buttons to the top
			buttonPanel.add(new JLabel(),
					LayoutHelper.getGBC(0, buttonId++, 1, 0D, 1.0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));
			
			tablePanel.add(buttonPanel,
					LayoutHelper.getGBC(1, 0, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));

			if (fieldLabel == null) {
				this.getMainPanel().add(tablePanel,
						LayoutHelper.getGBC(1, this.fieldList.size(), 1, fieldWeight, 1.0D, 
								GridBagConstraints.BOTH, new Insets(4,4,4,4)));
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
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(field);
		field.setFillsViewportHeight(true);
		
		// Tables are a special case - they dont have labels and are accessed via the model
		if (this.fieldList.contains(field)) {
			throw new IllegalArgumentException("Field already added: " + field);
		}
		if (buttons == null || buttons.size() == 0) {
			this.tabPanels.get(tabIndex).add(scrollPane, 
				LayoutHelper.getGBC(1, this.tabOffsets.get(tabIndex), 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		} else {
			JPanel tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			tablePanel.add(scrollPane,
					LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			int buttonId = 0;
			for (JButton button : buttons) {
				buttonPanel.add(button,
						LayoutHelper.getGBC(0, buttonId++, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));
			}
			// Add spacer to force buttons to the top
			buttonPanel.add(new JLabel(),
					LayoutHelper.getGBC(0, buttonId++, 1, 0D, 1.0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));
			
			tablePanel.add(buttonPanel,
					LayoutHelper.getGBC(1, 0, 1, 0D, 0D, GridBagConstraints.BOTH, new Insets(2,2,2,2)));

			this.tabPanels.get(tabIndex).add(tablePanel, 
					LayoutHelper.getGBC(1, this.tabOffsets.get(tabIndex), 1, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		}
		this.fieldList.add(field);
		this.incTabOffset(tabIndex);
	}
	
	public void setComboFields(String fieldLabel, String[] choices, String value) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c instanceof JComboBox) {
			@SuppressWarnings("unchecked")
			JComboBox<String> comboBox = (JComboBox<String>)c;
			comboBox.removeAllItems();
			for (String str : choices) {
				comboBox.addItem(str);
			}
			if (value != null) {
				comboBox.setSelectedItem(value);
			}
		} else if (c == null) {
			// Ignore - could be during init
			logger.debug("No field for " + fieldLabel);
		} else {
			logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
		}
	}
	
	public void setComboFields(String fieldLabel, List<String> choices, String value) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c instanceof JComboBox) {
			@SuppressWarnings("unchecked")
			JComboBox<String> comboBox = (JComboBox<String>)c;
			comboBox.removeAllItems();
			for (String str : choices) {
				comboBox.addItem(str);
			}
			if (value != null) {
				comboBox.setSelectedItem(value);
			}
		} else if (c == null) {
			// Ignore - could be during init
			logger.debug("No field for " + fieldLabel);
		} else {
			logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
		}
	}
	
	public void addNumberField(String fieldLabel, Integer min, Integer max, int value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		ZapNumberSpinner field = new ZapNumberSpinner(min, value, max);
		this.addField(fieldLabel, field, field, 0.0D);
	}
	
	public void addNumberField(int tabIndex, String fieldLabel, Integer min, Integer max, int value) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		ZapNumberSpinner field = new ZapNumberSpinner(min, value, max);
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		this.incTabOffset(tabIndex);
	}
	
	public void addCheckBoxField(String fieldLabel, boolean value) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		JCheckBox field = new JCheckBox();
		field.setSelected(value);
		this.addField(fieldLabel, field, field, 0.0D);
	}

	public void addCheckBoxField(int tabIndex, String fieldLabel, boolean value) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JCheckBox field = new JCheckBox();
		field.setSelected(value);
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		this.incTabOffset(tabIndex);
	}

	/*
	 * Add a 'node select' field which provides a button for showing a Node Select Dialog and a 
	 * non editable field for showing the node selected
	 */
	public void addNodeSelectField(final String fieldLabel, final SiteNode value, 
			final boolean editable, final boolean allowRoot) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		final ZapTextField text = new ZapTextField();
		text.setEditable(editable);
		if (value != null) {
			text.setText(getNodeText(value));
		}
		JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
		selectButton.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
		selectButton.addActionListener(new java.awt.event.ActionListener() { 
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
		panel.add(text, LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		
		this.addField(fieldLabel, text, panel, 0.0D);
	}
	

	/*
	 * Add a 'node select' field which provides a button for showing a Node Select Dialog and a 
	 * non editable field for showing the node selected
	 */
	public void addNodeSelectField(int tabIndex, final String fieldLabel, final SiteNode value, 
			final boolean editable, final boolean allowRoot) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		final ZapTextField text = new ZapTextField();
		text.setEditable(editable);
		if (value != null) {
			text.setText(getNodeText(value));
		}
		JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
		selectButton.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
		selectButton.addActionListener(new java.awt.event.ActionListener() { 
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
		panel.add(text, LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, text, panel, 0.0D);
		this.incTabOffset(tabIndex);
	}
	
	/*
	 * Add a 'node select' field which provides a button for showing a Node Select Dialog and a 
	 * non editable field for showing the node selected
	 */
	public void addTargetSelectField(int tabIndex, final String fieldLabel, final Target value,
			final boolean editable, final boolean allowRoot) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		final ZapTextField text = new ZapTextField();
		text.setEditable(editable);
		this.setTextTarget(text, value);

		JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
		selectButton.setIcon(new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe icon
		selectButton.addActionListener(new java.awt.event.ActionListener() { 
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
		panel.add(text, LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, text, panel, 0.0D);
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
	 * <p>
	 * If the {@code target} is not {@code null} it returns:
	 * <ol>
	 * <li>the URI, if it has a start node with an history reference;</li>
	 * <li>"Context: " followed by context's name, if it has a context;</li>
	 * <li>"Everything in scope", if it's only in scope.</li>
	 * </ol>
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
				return Constant.messages.getString("context.prefixName", target.getContext().getName());
			} else if (target.isInScopeOnly()) {
				return Constant.messages.getString("context.allInScope");
			}
		}
		return null;
	}

	private static String getNodeText(SiteNode node) {
		if (node != null && node.getHistoryReference() != null) {
			String url = node.getHistoryReference().getURI().toString();
			if (node.isLeaf() && url.endsWith("/")) {
				// String off the slash so we dont match a non leaf
				// node with the same name
				url = url.substring(0, url.length()-1);
			} else if (! node.isLeaf() && ! url.endsWith("/")) {
				// Add the slash to show its a non leaf node
				url = url + "/";
			}
			return url;
		}
		return "";
	}

	public void addContextSelectField(String fieldLabel, Context selectedContext){
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		ContextSelectComboBox field = new ContextSelectComboBox();
		if (selectedContext != null) {
			field.setSelectedItem(selectedContext);
		}
		this.addField(fieldLabel, field, field, 0.0D);
	}
	
	public void addContextSelectField(int tabIndex, String fieldLabel, Context selectedContext){
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		ContextSelectComboBox field = new ContextSelectComboBox();
		if (selectedContext != null) {
			field.setSelectedItem(selectedContext);
		}
		
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		incTabOffset(tabIndex);
	}
	
	public void addFileSelectField(String fieldLabel, final File dir, final int mode, final FileFilter filter) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		final ZapTextField text = new ZapTextField();
		text.setEditable(false);
		if (dir != null) {
			text.setText(dir.getAbsolutePath());
		}
		final StandardFieldsDialog sfd = this;
		JButton selectButton = new JButton("...");
		selectButton.addActionListener(new java.awt.event.ActionListener() { 
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = new JFileChooser(dir);
				chooser.setFileSelectionMode(mode);
				if (filter != null) {
					chooser.setFileFilter(filter);
				}
			    
			    int rc = chooser.showSaveDialog(sfd);
			    if(rc == JFileChooser.APPROVE_OPTION) {
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
		panel.add(text, LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		
		this.addField(fieldLabel, text, panel, 0.0D);
	}
	
	public void addFileSelectField(int tabIndex, final String fieldLabel, final File dir, final int mode, final FileFilter filter) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		final ZapTextField text = new ZapTextField();
		text.setEditable(false);
		if (dir != null) {
			text.setText(dir.getAbsolutePath());
		}
		final StandardFieldsDialog sfd = this;
		JButton selectButton = new JButton("...");
		selectButton.addActionListener(new java.awt.event.ActionListener() { 
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = new JFileChooser(dir);
				chooser.setFileSelectionMode(mode);
				if (filter != null) {
					chooser.setFileFilter(filter);
				}
			    
			    int rc = chooser.showSaveDialog(sfd);
			    if(rc == JFileChooser.APPROVE_OPTION) {
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
		panel.add(text, LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		panel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
		
		this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, text, panel, 0.0D);
		this.incTabOffset(tabIndex);
	}

	/*
	 * Override to do something useful
	 */
	public void siteNodeSelected(String field, SiteNode node) {
		
	}

	/*
	 * Override to do something useful
	 */
	public void targetSelected(String field, Target target) {
		
	}

	/**
	 * Allow the caller to get the field component in order to, for example, change its properties
	 * @param fieldLabel
	 * @return
	 */
	public Component getField(String fieldLabel) {
		return this.fieldMap.get(fieldLabel);
	}

	public String getStringValue(String fieldLabel) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapTextField) {
				return ((ZapTextField)c).getText();
			} else if (c instanceof ZapTextArea) {
				return ((ZapTextArea)c).getText();
			} else if (c instanceof JComboBox) {
				return (String)((JComboBox<?>)c).getSelectedItem();
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
		return null;
	}

	/**
	 * Gets the contents of a {@link JPasswordField} field.
	 * <p>
	 * For stronger security, it is recommended that the returned character array be cleared after use by setting each character
	 * to zero.
	 * 
	 * @param fieldLabel the label of the field
	 * @return the contents of the field, {@code null} if not a {@code JPassword} field.
	 * @since TODO add version
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
				return ((ContextSelectComboBox)c).getSelectedContext();
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
		return null;
	}
	
	public void setFieldValue(String fieldLabel, String value) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapTextField) {
				((ZapTextField)c).setText(value);
			} else if (c instanceof JPasswordField) {
				((JPasswordField)c).setText(value);
			} else if (c instanceof ZapTextArea) {
				((ZapTextArea)c).setText(value);
			} else if (c instanceof JComboBox) {
				((JComboBox<?>)c).setSelectedItem(value);
			} else if (c instanceof JLabel) {
				((JLabel)c).setText(value);
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
	}
	
	public void setFieldValue(String fieldLabel, boolean value) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof JCheckBox) {
				((JCheckBox)c).setSelected(value);
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
	}
	
	public boolean isEmptyField(String fieldLabel) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			Object value = null;
			if (c instanceof ZapTextField) {
				value = ((ZapTextField)c).getText();
			} else if (c instanceof JPasswordField) {
				return ((JPasswordField) c).getDocument().getLength() == 0;
			} else if (c instanceof ZapTextArea) {
				value = ((ZapTextArea)c).getText();
			} else if (c instanceof JComboBox) {
				value = ((JComboBox<?>)c).getSelectedItem();
			} else if (c instanceof ZapNumberSpinner) {
				value = ((ZapNumberSpinner)c).getValue();
				if ((Integer)value < 0) {
					value = null;
				}
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
			return value == null || value.toString().length() == 0;
			
		}
		return true;
	}

	public int getIntValue(String fieldLabel) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapNumberSpinner) {
				return ((ZapNumberSpinner)c).getValue();
			} else if (c instanceof JComboBox) {
				return (Integer)((JComboBox<?>)c).getSelectedItem();
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
		return -1;
	}
	
	public void setFieldValue(String fieldLabel, int value) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapNumberSpinner) {
				((ZapNumberSpinner)c).setValue(value);
			} else if (c instanceof JComboBox) {
				((JComboBox<?>)c).setSelectedItem(value);
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
	}
	
	public void addReadOnlyField(String fieldLabel, String value, boolean doubleWidth) {
		if (isTabbed()) {
			throw new IllegalArgumentException("Initialised as a tabbed dialog - must use method with tab parameters");
		}
		JLabel field = new JLabel();
		if (value != null) {
			field.setText(value);
		}
		if (doubleWidth) {
			this.getMainPanel().add(field, 
					LayoutHelper.getGBC(0, this.fieldList.size(), 2, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
			this.fieldList.add(field);
			this.fieldMap.put(fieldLabel, field);
		} else {
			this.addField(fieldLabel, field, field, 0.0D);
		}
	}

	public void addReadOnlyField(int tabIndex, String fieldLabel, String value, boolean doubleWidth) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		JLabel field = new JLabel();
		if (value != null) {
			field.setText(value);
		}

		if (doubleWidth) {
			JPanel panel = this.tabPanels.get(tabIndex);
			panel.add(field, 
					LayoutHelper.getGBC(0, this.tabOffsets.get(tabIndex), 2, 1.0D, 0.0D, GridBagConstraints.BOTH, new Insets(4,4,4,4)));
			this.fieldList.add(field);
			this.fieldMap.put(fieldLabel, field);
			incTabOffset(tabIndex);

		} else {
			this.addField(this.tabPanels.get(tabIndex), this.tabOffsets.get(tabIndex), fieldLabel, field, field, 0.0D);
		}
		incTabOffset(tabIndex);
	}

	public void setCustomTabPanel(int i, JComponent panel) {
		this.tabPanels.get(i).add(panel, LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D, GridBagConstraints.BOTH));
	}

	
	public Boolean getBoolValue(String fieldLabel) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof JCheckBox) {
				return ((JCheckBox)c).isSelected();
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
		return null;
	}
	
	public void addFieldListener(String fieldLabel, ActionListener listener) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapTextField) {
				((ZapTextField)c).addActionListener(listener);
			} else if (c instanceof JPasswordField) {
				((JPasswordField)c).addActionListener(listener);
			} else if (c instanceof JComboBox) {
				((JComboBox<?>)c).addActionListener(listener);
			} else if (c instanceof JCheckBox) {
				((JCheckBox)c).addActionListener(listener);
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
	}

	public void addFieldListener(String fieldLabel, MouseAdapter listener) {
		Component c = this.fieldMap.get(fieldLabel);
		if (c != null) {
			if (c instanceof ZapTextField) {
				((ZapTextField)c).addMouseListener(listener);
			} else if (c instanceof ZapTextArea) {
				((ZapTextArea)c).addMouseListener(listener);
			} else if (c instanceof JPasswordField) {
				((JPasswordField)c).addMouseListener(listener);
			} else if (c instanceof JComboBox) {
				((JComboBox<?>)c).addMouseListener(listener);
			} else {
				logger.error("Unrecognised field class " + fieldLabel + ": " + c.getClass().getCanonicalName());
			}
		}
	}

	public void removeAllFields() {
		if (this.isTabbed()) {
			for (JPanel panel : this.tabPanels) {
				panel.removeAll();
			}
		} else {
			this.getMainPanel().removeAll();
		}
		this.fieldList.clear();
		this.fieldMap.clear();
	}

	public void requestTabFocus(int tabIndex) {
		if (!isTabbed()) {
			throw new IllegalArgumentException("Not initialised as a tabbed dialog - must use method without tab parameters");
		}
		if (tabIndex < 0 || tabIndex >= this.tabPanels.size()) {
			throw new IllegalArgumentException("Invalid tab index: " + tabIndex);
		}
		tabbedPane.setSelectedComponent(this.tabPanels.get(tabIndex));
	}

	/**
	 * Set the visibility of the specified tabs.
	 * The labels must have been used to create the tabs in the constructor
	 * @param tabLabels
	 * @param visible
	 */
    public void setTabsVisible(String[] tabLabels, boolean visible) {
    	if (visible) {
			for (String label : tabLabels) {
				String name = Constant.messages.getString(label);
				JPanel tabPanel = this.tabNameMap.get(label);
				tabbedPane.addTab(name, tabPanel);
				this.tabPanels.add(tabPanel);
			}
    	} else {
			for (String label : tabLabels) {
				JPanel tabPanel = this.tabNameMap.get(label);
				this.tabbedPane.remove(tabPanel);
			}
    	}
	}

	/**
	 * Called when the dialogue is saved and after all validations have finished, to conclude the saving process.
	 * <p>
	 * Whether or not the dialogue is automatically hidden depends on the value returned by {@link #isHideOnSave()}.
	 * 
	 * @see #validateFields()
	 * @see #validateFieldsCustomMessage()
	 * @see #getSaveButtonText()
	 */
	public abstract void save();

	/**
	 * Called when the dialogue is {@link #save() saved}, allowing to validate the fields and show an error message (as opposed
	 * to validations using the method {@link #validateFieldsCustomMessage()}, which allow to show custom/complex information or
	 * warning dialogues).
	 * <p>
	 * If no message is returned (that is, {@code null}), the saving process continues, otherwise it is shown a warning dialogue
	 * with the message.
	 *
	 * @return a {@code String} containing the error message to be shown to the user, or {@code null} if there are no errors.
	 */
	public abstract String validateFields();
}
