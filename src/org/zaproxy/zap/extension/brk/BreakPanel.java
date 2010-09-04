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

package org.zaproxy.zap.extension.brk;

import java.awt.GridBagConstraints;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.HttpPanel;
import org.parosproxy.paros.view.View;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BreakPanel extends HttpPanel {
	private static final long serialVersionUID = 1L;
	private javax.swing.JPanel panelCommand = null;
	private javax.swing.JCheckBox chkTrapRequest = null;
	private javax.swing.JCheckBox chkTrapResponse = null;
	private javax.swing.JButton btnContinue = null;
	private javax.swing.JButton btnStep = null;

	private javax.swing.JToggleButton btnBreak = null;

	private boolean isContinue = false;
	private boolean isBreak = false;
	
	public boolean isBreak() {
		return isBreak;
	}

	private void setBreak(boolean isBreak) {
		this.isBreak = isBreak;

		chkTrapRequest.setSelected(isBreak);
		chkTrapResponse.setSelected(isBreak);

		if (isBreak) {
			btnBreak.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/151.png")));
			btnBreak.setToolTipText(Constant.messages.getString("brk.toolbar.button.unset"));
		} else {
			btnBreak.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/152.png")));
			btnBreak.setToolTipText(Constant.messages.getString("brk.toolbar.button.set"));
		}
	}
	
	private void setBreakButton(boolean on) {
		if (on) {
			if (! isBreak) {
				// Set the 'global' break 
				btnBreak.doClick();
			}
			
		} else {
			if (isBreak) {
				// Unset the 'global' break 
				btnBreak.doClick();
			}
		}
	}

	private JButton btnDrop = null;
    /**
     * 
     */
    public BreakPanel() {
        super();
 		initialize();
    }

    /**
     * @param isEditable
     */
    public BreakPanel(boolean isEditable) {
        super(isEditable);
 		initialize();
    }
	
	/**
	 * @return Returns the isContinue.
	 */
	public boolean isContinue() {
		return isContinue;
	}
	
	/**
	 * @param isContinue The isContinue to set.
	 */
	public void setContinue(boolean isContinue) {
		this.isContinue = isContinue;
		
		// How to do this if fn moved to a BreakControl??
		// Tight coupling, but maybe not TOO bad ;)
		btnStep.setEnabled( ! isContinue);
		btnContinue.setEnabled( ! isContinue);
		btnDrop.setEnabled( ! isContinue);
	}
	
	public void breakPointHit () {
		setBreakButton(true);
		// Select this 'Break' tab
		this.setTabFocus();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
//		this.setLayout(new java.awt.GridBagLayout());
//		this.setSize(600, 400);
//		this.setPreferredSize(new java.awt.Dimension(600,400));
		getPanelOption().add(getPanelCommand(), "");
	}
	/**

	 * This method initializes panelCommand	

	 * 	

	 * @return javax.swing.JPanel	

	 */    
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {
			java.awt.GridBagConstraints consGridBagConstraints11 = new java.awt.GridBagConstraints();

			java.awt.GridBagConstraints consGridBagConstraints3 = new java.awt.GridBagConstraints();

			java.awt.GridBagConstraints consGridBagConstraints2 = new java.awt.GridBagConstraints();

			java.awt.GridBagConstraints consGridBagConstraints1 = new java.awt.GridBagConstraints();

			panelCommand = new javax.swing.JPanel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			consGridBagConstraints1.gridx = 0;
			consGridBagConstraints1.gridy = 0;
			consGridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			consGridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			
			consGridBagConstraints2.gridx = 1;
			consGridBagConstraints2.gridy = 0;
			consGridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			consGridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
			
			// Adding a stop button
			java.awt.GridBagConstraints consGridBagConstraints4 = new java.awt.GridBagConstraints();
			consGridBagConstraints4.gridx = 2;
			consGridBagConstraints4.gridy = 0;
			consGridBagConstraints4.insets = new java.awt.Insets(0,2,0,2);
			consGridBagConstraints4.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints4.ipady = 0;
			
			// Adding a step button
			java.awt.GridBagConstraints consGridBagConstraints5 = new java.awt.GridBagConstraints();
			consGridBagConstraints5.gridx = 3;
			consGridBagConstraints5.gridy = 0;
			consGridBagConstraints5.insets = new java.awt.Insets(0,2,0,2);
			consGridBagConstraints5.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints5.ipady = 0;
			
			consGridBagConstraints3.gridx = 5; //3;
			consGridBagConstraints3.gridy = 0;
			consGridBagConstraints3.insets = new java.awt.Insets(0,2,0,2);

			consGridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints3.ipady = 0;
			panelCommand.setPreferredSize(new java.awt.Dimension(600,30));
			panelCommand.setName("Command");
			consGridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
			consGridBagConstraints11.gridx = 7; // 2;
			consGridBagConstraints11.gridy = 0;
			consGridBagConstraints11.weightx = 1.0D;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints1.gridx = 6; //4;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,2,0,2);
			
			// Otherwise they will be null ;)
			getChkTrapRequest();
			getChkTrapResponse();

	        View.getSingleton().addMainToolbarButton(this.getBtnBreak());
	        View.getSingleton().addMainToolbarButton(this.getBtnStep());
	        View.getSingleton().addMainToolbarButton(this.getBtnContinue());
	        View.getSingleton().addMainToolbarButton(this.getBtnDrop());
	        View.getSingleton().addMainToolbarSeparator();

		}
		return panelCommand;
	}

	/**
	 * This method initializes chkTrapRequest	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	public javax.swing.JCheckBox getChkTrapRequest() {
		if (chkTrapRequest == null) {
			chkTrapRequest = new javax.swing.JCheckBox();
			chkTrapRequest.setText("Trap request");
			chkTrapRequest.addItemListener(new java.awt.event.ItemListener() { 

				public void itemStateChanged(java.awt.event.ItemEvent e) {    

					if (!chkTrapRequest.isSelected() && !chkTrapResponse.isSelected()) {
					    Control.getSingleton().getProxy().setSerialize(false);
					} else {
					    Control.getSingleton().getProxy().setSerialize(true);
					}
					
				}
			});
		}
		return chkTrapRequest;
	}

	/**
	 * This method initializes chkTrapResponse	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	public javax.swing.JCheckBox getChkTrapResponse() {
		if (chkTrapResponse == null) {
			chkTrapResponse = new javax.swing.JCheckBox();
			chkTrapResponse.setText("Trap response");
			chkTrapResponse.addItemListener(new java.awt.event.ItemListener() { 

				public void itemStateChanged(java.awt.event.ItemEvent e) {    

					if (!chkTrapRequest.isSelected() && !chkTrapResponse.isSelected()) {
					    Control.getSingleton().getProxy().setSerialize(false);
					} else {
					    Control.getSingleton().getProxy().setSerialize(true);
					}							

				}
			});

		}
		return chkTrapResponse;
	}

	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private javax.swing.JButton getBtnStep() {
		if (btnStep == null) {
			btnStep = new javax.swing.JButton();
			btnStep.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/143.png")));
			btnStep.setToolTipText(Constant.messages.getString("brk.toolbar.button.step"));
			btnStep.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					setContinue(true);
					// Could have been hit via a break point
					setBreakButton(true);

				}
			});
			// Default to disabled
			btnStep.setEnabled(false);

		}
		return btnStep;
	}

	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private javax.swing.JButton getBtnContinue() {
		if (btnContinue == null) {
			btnContinue = new javax.swing.JButton();
			btnContinue.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/131.png")));
			btnContinue.setToolTipText(Constant.messages.getString("brk.toolbar.button.cont"));
			btnContinue.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					setContinue(true);
					setBreakButton(false);

				}
			});
			// Default to disabled
			btnContinue.setEnabled(false);

		}
		return btnContinue;
	}

	/**
	 * This method initializes btnDrop	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnDrop() {
		if (btnDrop == null) {
			btnDrop = new JButton();
			btnDrop.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/150.png")));
			btnDrop.setToolTipText(Constant.messages.getString("brk.toolbar.button.bin"));
			btnDrop.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    BreakPanel.this.setMessage("","", false);
				    setContinue(true);
				}
			});
			// Default to disabled
			btnDrop.setEnabled(false);
		}
		return btnDrop;
	}

	/**
	 * This method initializes btnContinue	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private javax.swing.JToggleButton getBtnBreak() {
		if (btnBreak == null) {
			btnBreak = new javax.swing.JToggleButton();
			setBreak(false);
			btnBreak.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
					// Toggle button
					setBreak(!isBreak());
				}
			});

		}
		return btnBreak;
	}
}
