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
package org.parosproxy.paros.extension.filter;

import java.awt.Frame;
import java.awt.HeadlessException;


import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.regex.Pattern;

import javax.swing.JButton;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.ViewDelegate;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FilterReplaceDialog extends AbstractDialog {

	private JPanel jPanel = null;
	private JTextField txtPattern = null;
	private JTextField txtReplaceWith = null;
	private JPanel jPanel1 = null;
	private JButton btnOK = null;
	private JButton btnCancel = null;
	private int exitCode = JOptionPane.CANCEL_OPTION;
	private ViewDelegate view = null;
	
    /**
     * @throws HeadlessException
     */
    public FilterReplaceDialog() throws HeadlessException {
        super();
 		initialize();
       // TODO Auto-generated constructor stub
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public FilterReplaceDialog(Frame arg0, boolean arg1)
            throws HeadlessException {
        super(arg0, arg1);
        initialize();
        // TODO Auto-generated constructor stub
    }
    
    public void setView(ViewDelegate view) {
        this.view = view;
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setContentPane(getJPanel());
        this.setSize(346, 156);
			
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints4 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			javax.swing.JLabel jLabel2 = new JLabel();

			javax.swing.JLabel jLabel1 = new JLabel();

			javax.swing.JLabel jLabel = new JLabel();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jLabel.setText("Pattern:");
			jLabel1.setText("Replace with:");
			jLabel2.setText("Enter a regular expression as the pattern.");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new java.awt.Insets(2,5,2,5);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new java.awt.Insets(2,5,2,5);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.insets = new java.awt.Insets(2,5,2,5);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 2;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.insets = new java.awt.Insets(2,5,2,5);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.SOUTHEAST;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 3;
			gridBagConstraints6.insets = new java.awt.Insets(2,2,2,2);
			jPanel.add(jLabel2, gridBagConstraints1);
			jPanel.add(jLabel, gridBagConstraints2);
			jPanel.add(getTxtPattern(), gridBagConstraints3);
			jPanel.add(jLabel1, gridBagConstraints4);
			jPanel.add(getTxtReplaceWith(), gridBagConstraints5);
			jPanel.add(getJPanel1(), gridBagConstraints6);
		}
		return jPanel;
	}
	/**
	 * This method initializes txtPattern	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	public JTextField getTxtPattern() {
		if (txtPattern == null) {
			txtPattern = new JTextField();
		}
		return txtPattern;
	}
	/**
	 * This method initializes txtReplaceWith	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	public JTextField getTxtReplaceWith() {
		if (txtReplaceWith == null) {
			txtReplaceWith = new JTextField();
		}
		return txtReplaceWith;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getBtnOK(), null);
			jPanel1.add(getBtnCancel(), null);
		}
		return jPanel1;
	}
	/**
	 * This method initializes btnOK	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnOK() {
		if (btnOK == null) {
			btnOK = new JButton();
			btnOK.setText("OK");
			btnOK.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    Pattern pattern = null;
				    try {
				        pattern = Pattern.compile(getTxtPattern().getText());
				    } catch (Exception e1) {
				        view.showWarningDialog("Invalid pattern.  Please correct");
				        getTxtPattern().grabFocus();
				        return;
				    }
				    
				    FilterReplaceDialog.this.dispose();
				    exitCode = JOptionPane.OK_OPTION;

				}
			});

		}
		return btnOK;
	}
	/**
	 * This method initializes btnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    FilterReplaceDialog.this.dispose();
				    exitCode = JOptionPane.CANCEL_OPTION;

				}
			});

		}
		return btnCancel;
	}
	
	public int showDialog() {
	    setVisible(true);
	    return exitCode;
	}
	
      }  //  @jve:decl-index=0:visual-constraint="10,10"
