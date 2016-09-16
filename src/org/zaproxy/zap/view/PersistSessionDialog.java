/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.zaproxy.zap.extension.help.ExtensionHelp;

public class PersistSessionDialog extends AbstractDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel jPanel = null;
	private JButton startSessionButton = null;
	private JRadioButton persistRadioButton = null;
	private JRadioButton timestampRadioButton = null;
	private JRadioButton temporaryRadioButton = null;
	private JCheckBox dontAskAgainCheckbox = null;

    /**
     * Constructs a modal {@code PersistSessionDialog} with the given owner.
     * 
     * @param owner the {@code Frame} from which the dialog is displayed
     * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
     */
    public PersistSessionDialog(Frame owner) {
        super(owner, true);
        this.setModalityType(ModalityType.DOCUMENT_MODAL);
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setContentPane(getJPanel());
        this.pack();
        // Disable the escape key - they have to amke a choice!
        getRootPane().getActionMap().put("ESCAPE", null);

	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			
			JLabel question = new JLabel(Constant.messages.getString("database.newsession.question"));
			jPanel.add(question, LayoutHelper.getGBC(0, 0, 2, 1.0D, new Insets(4, 4, 4, 4)));
			jPanel.add(this.getTimestampRadioButton(), LayoutHelper.getGBC(0, 1, 2, 1.0D, new Insets(4, 4, 4, 4)));
			jPanel.add(this.getPersistRadioButton(), LayoutHelper.getGBC(0, 3, 2, 1.0D, new Insets(4, 4, 4, 4)));
			jPanel.add(this.getTemporaryRadioButton(), LayoutHelper.getGBC(0, 5, 2, 1.0D, new Insets(4, 4, 4, 4)));

			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(this.getTimestampRadioButton());
			buttonGroup.add(this.getPersistRadioButton());
			buttonGroup.add(this.getTemporaryRadioButton());

			jPanel.add(new JLabel(), LayoutHelper.getGBC(0, 6, 2, 1.0D, 1.0D));	// Spacer
			jPanel.add(getDontAskAgainCheckbox(), LayoutHelper.getGBC(0, 7, 2, 1.0D, new Insets(4, 4, 4, 4)));

			jPanel.add(new JLabel(Constant.messages.getString("database.newsession.prompt.note")), 
					LayoutHelper.getGBC(0, 8, 2, 1.0D, new Insets(4, 4, 4, 4)));

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			
			JButton helpButton = new JButton(Constant.messages.getString("menu.help"));
			helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));
			helpButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					ExtensionHelp.showHelp("ui.dialogs.persistsession");
				}});
			
			buttonPanel.add(helpButton, LayoutHelper.getGBC(0, 0, 1, 0.0D, new Insets(4, 4, 4, 4)));
			buttonPanel.add(new JLabel(), LayoutHelper.getGBC(1, 0, 1, 1.0D, new Insets(4, 4, 4, 4)));	// Spacer
			buttonPanel.add(getStartSessionButton(), LayoutHelper.getGBC(2, 0, 1, 0.0D, new Insets(4, 4, 4, 4)));

			jPanel.add(buttonPanel, LayoutHelper.getGBC(0, 20, 2, 1.0D, new Insets(4, 4, 4, 4)));

		}
		return jPanel;
	}
	
	private JRadioButton getPersistRadioButton() {
		if (persistRadioButton == null) {
			persistRadioButton =new JRadioButton(Constant.messages.getString("database.newsession.userspec"));
			persistRadioButton.addActionListener(this);
		}
		return persistRadioButton;
	}
	
	private JRadioButton getTimestampRadioButton() {
		if (timestampRadioButton == null) {
			timestampRadioButton = new JRadioButton(Constant.messages.getString("database.newsession.timestamped"));
			timestampRadioButton.addActionListener(this);
		}
		return timestampRadioButton;
	}
	
	private JRadioButton getTemporaryRadioButton() {
		if (temporaryRadioButton == null) {
			temporaryRadioButton = new JRadioButton(Constant.messages.getString("database.newsession.temporary"));
			temporaryRadioButton.addActionListener(this);
		}
		return temporaryRadioButton;
	}

	private JCheckBox getDontAskAgainCheckbox() {
		if (dontAskAgainCheckbox == null) {
			dontAskAgainCheckbox = new JCheckBox(Constant.messages.getString("database.newsession.prompt.label"));

		}
		return dontAskAgainCheckbox;
	}
	
	public boolean isPersistChosen() {
		return this.getPersistRadioButton().isSelected();
	}
	
	public void setPersistChosen() {
		this.getPersistRadioButton().setSelected(true);
		this.getStartSessionButton().setEnabled(true);
	}

	public boolean isTimestampChosen() {
		return this.getTimestampRadioButton().isSelected();
	}

	public void setTimestampChosen() {
		this.getTimestampRadioButton().setSelected(true);
		this.getStartSessionButton().setEnabled(true);
	}

	public boolean isTemporaryChosen() {
		return this.getTemporaryRadioButton().isSelected();
	}

	public void setTemporaryChosen() {
		this.getTemporaryRadioButton().setSelected(true);
		this.getStartSessionButton().setEnabled(true);
	}
	
	public boolean isDontAskAgain() {
		return this.getDontAskAgainCheckbox().isSelected();
	}

	/**
	 * This method initializes startSessionButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getStartSessionButton() {
		if (startSessionButton == null) {
			startSessionButton = new JButton();
			startSessionButton.setText(Constant.messages.getString("database.newsession.button.start"));
			startSessionButton.setEnabled(false);
			startSessionButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    PersistSessionDialog.this.dispose();
				}
			});

		}
		return startSessionButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Enable the Start Session button if any of the radio buttons are selected
		this.getStartSessionButton().setEnabled(this.getTimestampRadioButton().isSelected() ||
				this.getPersistRadioButton().isSelected() || this.getTemporaryRadioButton().isSelected());
		
	}

}
