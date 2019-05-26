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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;



public class AboutDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private AboutPanel aboutPanel = null;
	private JButton btnOK = null;

	/**
	 * Constructs an {@code AboutDialog} with no owner and not modal.
	 * 
	 * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
	 */
	public AboutDialog() {
		super();
		initialize();
	}

	/**
	 * Constructs an {@code AboutDialog} with the given owner and whether or not it's modal.
	 * 
	 * @param owner the {@code Frame} from which the dialog is displayed
	 * @param modal {@code true} if the dialogue should be modal, {@code false} otherwise
	 * @throws HeadlessException when {@code GraphicsEnvironment.isHeadless()} returns {@code true}
	 */
	public AboutDialog(Frame owner, boolean modal) {
		super(owner, modal);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setContentPane(getJPanel());
		this.pack();

		// this.setSize(406, 503);

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
			
			GridBagConstraints gbcPanel = new GridBagConstraints();
			GridBagConstraints gbcButtons = new GridBagConstraints();
			
			gbcButtons.gridx = 0;
			gbcButtons.gridy = 0;
			gbcButtons.insets = new Insets(0, 0, 0, 0);
			gbcButtons.fill = GridBagConstraints.BOTH;
			gbcButtons.anchor = GridBagConstraints.NORTHWEST;
			gbcButtons.weightx = 1.0D;
			gbcButtons.weighty = 1.0D;
			gbcButtons.ipady = 2;
			gbcButtons.gridwidth = 2;

			gbcPanel.gridx = 1;
			gbcPanel.gridy = 1;
			gbcPanel.insets = new Insets(2, 2, 2, 2);
			gbcPanel.anchor = GridBagConstraints.SOUTHEAST;
			
			jPanel.add(getAboutPanel(), gbcButtons);
			jPanel.add(getBtnOK(), gbcPanel);
		}
		return jPanel;
	}

	/**
	 * This method initializes aboutPanel
	 * 
	 * @return org.parosproxy.paros.view.AboutPanel
	 */
	private AboutPanel getAboutPanel() {
		if (aboutPanel == null) {
			aboutPanel = new AboutPanel();
		}
		return aboutPanel;
	}

	/**
	 * This method initializes btnOK
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOK() {
		if (btnOK == null) {
			btnOK = new JButton();
			btnOK.setText(Constant.messages.getString("all.button.ok"));
			btnOK.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					AboutDialog.this.dispose();
				}
			});
		}
		return btnOK;
	}
	
}
