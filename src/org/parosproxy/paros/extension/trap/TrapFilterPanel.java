/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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

import org.parosproxy.paros.extension.AbstractPanel;

import javax.swing.JPanel;
import javax.swing.JCheckBox;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TrapFilterPanel extends AbstractPanel {

	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JCheckBox chkEnableInclusiveFilter = null;
	private JLabel jLabel = null;
	private JTextArea txtInclusiveFilter = null;
	private JLabel jLabel1 = null;
	private JCheckBox chkEnableExclusiveFilter = null;
	private JTextArea txtExclusiveFilter = null;
	private JLabel jLabel2 = null;
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;
    /**
     * 
     */
    public TrapFilterPanel() {
        super();
		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        jLabel = new JLabel();
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        this.setLayout(new GridBagLayout());
        this.setSize(405, 297);
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0D;
        gridBagConstraints1.weighty = 0.5D;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints2.weighty = 0.5D;
        gridBagConstraints2.weightx = 1.0D;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jLabel.setText(" ");
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 2;
        gridBagConstraints3.weightx = 1.0D;
        gridBagConstraints3.weighty = 1.0D;
        this.add(getJPanel(), gridBagConstraints1);
        this.add(getJPanel1(), gridBagConstraints2);
        this.add(jLabel, gridBagConstraints3);
			
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel1 = new JLabel();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jLabel1.setText("Multiple filter can be defined.  Use * as wildcard and ';' for delimiter.");
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints4.weightx = 1.0D;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.gridwidth = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 0.0D;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.ipady = 15;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 2;
			gridBagConstraints6.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Trap when URI matches (inclusive filter)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 0.0D;
			gridBagConstraints10.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 1;
			gridBagConstraints10.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints10.ipady = 15;
			jPanel.add(getChkEnableInclusiveFilter(), gridBagConstraints4);
			jPanel.add(getJScrollPane(), gridBagConstraints10);
			jPanel.add(jLabel1, gridBagConstraints6);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel2 = new JLabel();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jLabel2.setText("Multiple filter can be defined.  Use * as wildcard and ';' for delimiter.");
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 2;
			gridBagConstraints9.gridwidth = 1;
			gridBagConstraints9.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.weightx = 1.0D;
			jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Do not trap when URI matches (exclusive filter)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.weighty = 0.0D;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints11.ipady = 15;
			gridBagConstraints11.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
			jPanel1.add(getChkEnableExclusiveFilter(), gridBagConstraints7);
			jPanel1.add(getJScrollPane1(), gridBagConstraints11);
			jPanel1.add(jLabel2, gridBagConstraints9);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	JCheckBox getChkEnableInclusiveFilter() {
		if (chkEnableInclusiveFilter == null) {
			chkEnableInclusiveFilter = new JCheckBox();
			chkEnableInclusiveFilter.setText("Use inclusive filter");
			chkEnableInclusiveFilter.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    setInclusiveFilter(chkEnableInclusiveFilter.isSelected());
				}
			});
		}
		return chkEnableInclusiveFilter;
	}
	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	JTextArea getTxtInclusiveFilter() {
		if (txtInclusiveFilter == null) {
			txtInclusiveFilter = new JTextArea();
			txtInclusiveFilter.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtInclusiveFilter.setLineWrap(true);
			txtInclusiveFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			txtInclusiveFilter.setRows(3);
		}
		return txtInclusiveFilter;
	}
	/**
	 * This method initializes jCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	JCheckBox getChkEnableExclusiveFilter() {
		if (chkEnableExclusiveFilter == null) {
			chkEnableExclusiveFilter = new JCheckBox();
			chkEnableExclusiveFilter.setText("Use exclusive filter");
			chkEnableExclusiveFilter.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    setExclusiveFilter(chkEnableExclusiveFilter.isSelected());
				}
			});
		}
		return chkEnableExclusiveFilter;
	}
	/**
	 * This method initializes jTextArea1	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	JTextArea getTxtExclusiveFilter() {
		if (txtExclusiveFilter == null) {
			txtExclusiveFilter = new JTextArea();
			txtExclusiveFilter.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtExclusiveFilter.setLineWrap(true);
			txtExclusiveFilter.setRows(3);
			txtExclusiveFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		return txtExclusiveFilter;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtInclusiveFilter());
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTxtExclusiveFilter());
			jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane1;
	}
	
	void setInclusiveFilter(boolean isEnabled) {
	    txtInclusiveFilter.setEnabled(isEnabled);
	    Color color = Color.WHITE;
	    if (!isEnabled) {
	        txtInclusiveFilter.setText("");
	        color = this.getBackground();
	    }
	    
	    txtInclusiveFilter.setBackground(color);

	}
	
	void setExclusiveFilter(boolean isEnabled) {
	    txtExclusiveFilter.setEnabled(isEnabled);
	    Color color = Color.WHITE;
	    if (!isEnabled) {
	        txtExclusiveFilter.setText("");
	        color = this.getBackground();
	    }
	    
	    txtExclusiveFilter.setBackground(color);


	}

	
        }  //  @jve:decl-index=0:visual-constraint="10,10"
