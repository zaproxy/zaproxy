/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.trap;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.HttpPanel;

import javax.swing.JButton;
import java.awt.GridBagConstraints;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TrapPanel extends HttpPanel {
	private javax.swing.JPanel panelCommand = null;
	private javax.swing.JCheckBox chkTrapRequest = null;
	private javax.swing.JCheckBox chkTrapResponse = null;
	private javax.swing.JButton btnContinue = null;
	private javax.swing.JLabel jLabel = null;
	
	private boolean isContinue = false;
	
	private JButton btnDrop = null;
    /**
     * 
     */
    public TrapPanel() {
        super();
 		initialize();
    }

    /**
     * @param isEditable
     */
    public TrapPanel(boolean isEditable) {
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
			consGridBagConstraints3.gridx = 3;
			consGridBagConstraints3.gridy = 0;
			consGridBagConstraints3.insets = new java.awt.Insets(0,2,0,2);
			consGridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
			consGridBagConstraints3.ipady = 0;
			panelCommand.setPreferredSize(new java.awt.Dimension(600,30));
			panelCommand.setName("Command");
			consGridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			consGridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
			consGridBagConstraints11.gridx = 2;
			consGridBagConstraints11.gridy = 0;
			consGridBagConstraints11.weightx = 1.0D;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints1.gridx = 4;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,2,0,2);
			panelCommand.add(getChkTrapRequest(), consGridBagConstraints1);
			panelCommand.add(getChkTrapResponse(), consGridBagConstraints2);
			panelCommand.add(getJLabel(), consGridBagConstraints11);
			panelCommand.add(getBtnContinue(), consGridBagConstraints3);
			panelCommand.add(getBtnDrop(), gridBagConstraints1);
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
	private javax.swing.JButton getBtnContinue() {
		if (btnContinue == null) {
			btnContinue = new javax.swing.JButton();
			btnContinue.setText("Continue");
			btnContinue.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					setContinue(true);

				}
			});

		}
		return btnContinue;
	}

	/**

	 * This method initializes jLabel	

	 * 	

	 * @return javax.swing.JLabel	

	 */    
	private javax.swing.JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText(" ");
		}
		return jLabel;
	}

	/**
	 * This method initializes btnDrop	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnDrop() {
		if (btnDrop == null) {
			btnDrop = new JButton();
			btnDrop.setText("Drop");
			btnDrop.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    TrapPanel.this.setMessage("","", false);
				    setContinue(true);
				}
			});
		}
		return btnDrop;
	}
  }
