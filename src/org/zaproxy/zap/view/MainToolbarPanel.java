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

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;

public class MainToolbarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JToolBar toolbar = null;
	private JButton btnNew = null;
	private JButton btnOpen = null;
	private JButton btnSave = null;

	public MainToolbarPanel () {
		super();
		initialise();
	}
	
	public void initialise () {
		setLayout(new java.awt.GridBagLayout());
		setPreferredSize(new java.awt.Dimension(80000,25));
		setMaximumSize(new java.awt.Dimension(80000,25));
		this.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
		
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.weighty = 1.0;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;

		JToolBar t1 = new JToolBar();
		t1.setEnabled(true);
		t1.setPreferredSize(new java.awt.Dimension(80000,25));
		t1.setMaximumSize(new java.awt.Dimension(80000,25));
		
		add(getToolbar(), gridBagConstraints1);
		add(t1, gridBagConstraints2);

		toolbar.add(getBtnNew());
		toolbar.add(getBtnOpen());
		toolbar.add(getBtnSave());
		
		toolbar.addSeparator();
		
	}
	
	private JToolBar getToolbar() {
		if (toolbar == null) {
			toolbar = new JToolBar();
			toolbar.setEnabled(true);
			toolbar.setFloatable(false);
			toolbar.setRollover(true);
			toolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			toolbar.setName("Main Toolbar");
			toolbar.setBorder(BorderFactory.createEmptyBorder());
		}
		return toolbar;
	}
	
	public void addButton (JButton button) {
		getToolbar().add(button);
	}

	public void addButton(JToggleButton button) {
		getToolbar().add(button);
	}


	public void addSeparator() {
		getToolbar().addSeparator();
	}

	private JButton getBtnNew() {
		if (btnNew == null) {
			btnNew = new JButton();
			btnNew.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/021.png")));	// 'Blank file' icon
			btnNew.setToolTipText(Constant.messages.getString("menu.file.newSession"));

			btnNew.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Control.getSingleton().getMenuFileControl().newSession(true);
					} catch (Exception e1) {
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.newSession.error"));
						e1.printStackTrace();
					}
				}
			});
		}
		return btnNew;
	}

	private JButton getBtnOpen() {
		if (btnOpen == null) {
			btnOpen = new JButton();
			btnOpen.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/047.png")));	// 'open folder' icon
			btnOpen.setToolTipText(Constant.messages.getString("menu.file.openSession"));

			btnOpen.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Control.getSingleton().getMenuFileControl().openSession();
					} catch (Exception e1) {
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.openSession.error"));
						e1.printStackTrace();
					}
				}
			});
		}
		return btnOpen;
	}

	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/096.png")));	// 'diskette' icon
			btnSave.setToolTipText(Constant.messages.getString("menu.file.saveSession"));

			btnSave.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						Control.getSingleton().getMenuFileControl().saveAsSession();
					} catch (Exception e1) {
						View.getSingleton().showWarningDialog(Constant.messages.getString("menu.file.saveSession.error"));
						e1.printStackTrace();
					}
				}
			});
		}
		return btnSave;
	}


}
