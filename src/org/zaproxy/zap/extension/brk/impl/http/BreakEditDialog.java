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
package org.zaproxy.zap.extension.brk.impl.http;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapTextField;

public class BreakEditDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private ZapTextField txtDisplay = null;
	private JButton btnSave = null;
	private JButton btnCancel = null;

	private JScrollPane jScrollPane = null;

    private HttpBreakpointMessage breakpoint;
	private HttpBreakpointsUiManagerInterface breakPointsManager;

    public BreakEditDialog(HttpBreakpointsUiManagerInterface breakPointsManager) throws HeadlessException {
        super(View.getSingleton().getMainFrame(), false);
        
        this.breakPointsManager = breakPointsManager;
        initialize();
    }
	
	private void initialize() {
        this.setTitle(Constant.messages.getString("brk.edit.title"));
        this.setContentPane(getJPanel());
        this.setSize(407, 255);
        this.addWindowListener(new java.awt.event.WindowAdapter() {   
        	@Override
        	public void windowOpened(java.awt.event.WindowEvent e) {    
        	} 

        	@Override
        	public void windowClosing(java.awt.event.WindowEvent e) {    
        	    btnCancel.doClick();
        	}
        });

		pack();
	}
	
    public void setBreakpoint(HttpBreakpointMessage breakpoint) {
        this.breakpoint = breakpoint;
        
        getTxtDisplay().setText(breakpoint.getUrl().toString());
        getTxtDisplay().discardAllEdits();
    }
    
	    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

			javax.swing.JLabel jLabel2 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new java.awt.Dimension(400,90));
			jPanel.setMinimumSize(new java.awt.Dimension(400,90));
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 5;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 5;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,10);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;

			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 5;
			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.weightx = 1.0D;
			gridBagConstraints13.insets = new java.awt.Insets(2,10,2,5);

			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 0.0D;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.insets = new java.awt.Insets(2,10,5,10);
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 2;
			gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints15.ipadx = 0;
			gridBagConstraints15.ipady = 10;

			jPanel.add(getJScrollPane(), gridBagConstraints15);
			jPanel.add(jLabel2, gridBagConstraints13);
			jPanel.add(getBtnCancel(), gridBagConstraints2);
			jPanel.add(getBtnSave(), gridBagConstraints3);
		}
		return jPanel;
	}
	
	private ZapTextField getTxtDisplay() {
		if (txtDisplay == null) {
			txtDisplay = new ZapTextField();
			txtDisplay.setHorizontalAlignment(ZapTextField.LEFT);
			txtDisplay.setAlignmentX(0.0F);
			txtDisplay.setPreferredSize(new java.awt.Dimension(250,20));
			txtDisplay.setText("");
			txtDisplay.setMinimumSize(new java.awt.Dimension(250,20));
		}
		return txtDisplay;
	}
	    
	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setText(Constant.messages.getString("brk.edit.button.save"));
			btnSave.setMinimumSize(new java.awt.Dimension(75,30));
			btnSave.setPreferredSize(new java.awt.Dimension(75,30));
			btnSave.setMaximumSize(new java.awt.Dimension(100,40));
			btnSave.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    breakPointsManager.editBreakpoint(breakpoint, new HttpBreakpointMessage(getTxtDisplay().getText()));
                    breakpoint = null;
				    getTxtDisplay().discardAllEdits();
				    breakPointsManager.hideEditDialog();
				}
			});

		}
		return btnSave;
	}
	    
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText(Constant.messages.getString("brk.edit.button.cancel"));
			btnCancel.setMaximumSize(new java.awt.Dimension(100,40));
			btnCancel.setMinimumSize(new java.awt.Dimension(70,30));
			btnCancel.setPreferredSize(new java.awt.Dimension(70,30));
			btnCancel.setEnabled(true);
			btnCancel.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    breakpoint = null;
				    getTxtDisplay().discardAllEdits();
				    breakPointsManager.hideEditDialog();
				}
			});

		}
		return btnCancel;
	}
	    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane.setPreferredSize(new java.awt.Dimension(0,25));
			jScrollPane.setViewportView(getTxtDisplay());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		return jScrollPane;
	}
}
