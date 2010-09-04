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
package org.parosproxy.paros.extension.history;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HistoryFilterDialog extends AbstractDialog {

	private JPanel jPanel = null;
	private JButton btnApply = null;
	private JButton btnCancel = null;
	private JTextField txtPattern = null;
	private JPanel jPanel1 = null;
	private int exitResult = JOptionPane.CANCEL_OPTION;

	
	private JButton btnReset = null;
	private JPanel jPanel2 = null;
	private JRadioButton radioExact = null;
	private JRadioButton radioRegex = null;
    /**
     * @throws HeadlessException
     */
    public HistoryFilterDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public HistoryFilterDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setContentPane(getJPanel());
        this.setVisible(false);
        this.setResizable(false);
        this.setTitle("Filter history");
        //this.setSize(400, 188);
        centreDialog();
        txtPattern.requestFocus();
        this.getRootPane().setDefaultButton(btnApply);
        //  Handle escape key to close the dialog    
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                HistoryFilterDialog.this.dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
        this.pack();
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

			javax.swing.JLabel jLabel1 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jLabel.setText("Pattern:");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.insets = new java.awt.Insets(5,10,5,10);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.ipady = 1;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.insets = new java.awt.Insets(2,2,2,10);
			gridBagConstraints5.ipadx = 100;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints6.gridwidth = 3;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 3;
			gridBagConstraints6.insets = new java.awt.Insets(5,2,5,2);
			gridBagConstraints6.ipadx = 3;
			gridBagConstraints6.ipady = 3;
			jLabel1.setText("<html><p>Enter the string below to filter requests/responses with matching string/ pattern in history.  Regular expression is supported.</p></html>");
			jLabel1.setMaximumSize(new java.awt.Dimension(2147483647,80));
			jLabel1.setMinimumSize(new java.awt.Dimension(350,24));
			jLabel1.setPreferredSize(new java.awt.Dimension(350,50));
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.insets = new java.awt.Insets(5,10,5,10);
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.gridwidth = 3;
			gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.ipadx = 3;
			gridBagConstraints11.ipady = 3;
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridwidth = 3;
			gridBagConstraints12.gridy = 2;
			gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.insets = new java.awt.Insets(2,10,2,10);
			gridBagConstraints12.ipadx = 0;
			gridBagConstraints12.ipady = 1;
			jPanel.add(jLabel1, gridBagConstraints11);
			jPanel.add(jLabel, gridBagConstraints1);
			jPanel.add(getTxtPattern(), gridBagConstraints5);
			jPanel.add(getJPanel2(), gridBagConstraints12);
			jPanel.add(getJPanel1(), gridBagConstraints6);
		}
		return jPanel;
	}
	/**
	 * This method initializes btnApply	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnApply() {
		if (btnApply == null) {
			btnApply = new JButton();
			btnApply.setText("Apply");
//			btnApply.setPreferredSize(new java.awt.Dimension(70,30));
//			btnApply.setMaximumSize(new java.awt.Dimension(100,35));
//			btnApply.setMinimumSize(new java.awt.Dimension(63,30));
			btnApply.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    try {
				        Pattern pattern = Pattern.compile(getPattern());
				    } catch (Exception e1) {
				        View.getSingleton().showWarningDialog("Invalid pattern.");
				        return;
				    }
				    exitResult = JOptionPane.OK_OPTION;
				    HistoryFilterDialog.this.dispose();
					
				}
			});

		}
		return btnApply;
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

				    exitResult = JOptionPane.CANCEL_OPTION;
				    HistoryFilterDialog.this.dispose();

				}
			});

		}
		return btnCancel;
	}
	/**
	 * This method initializes txtPattern	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getTxtPattern() {
		if (txtPattern == null) {
			txtPattern = new JTextField();
		}
		return txtPattern;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getBtnApply(), null);
			jPanel1.add(getBtnReset(), null);
			jPanel1.add(getBtnCancel(), null);
		}
		return jPanel1;
	}
	public int showDialog() {
	    this.setVisible(true);
	    return exitResult;
	}
	
	public String getPattern() {
	    String result = "";
	    if (getRadioRegex().isSelected()) {
	        result = getTxtPattern().getText();
	    } else if (getRadioExact().isSelected()) {
	        result = "\\Q" + getTxtPattern().getText() + "\\E";
	        
	    }
	    return result;
	}
	

	/**
	 * This method initializes btnReset	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnReset() {
		if (btnReset == null) {
			btnReset = new JButton();
			btnReset.setText("Reset filter");
			btnReset.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

					exitResult = JOptionPane.NO_OPTION;
					txtPattern.setText("");
					HistoryFilterDialog.this.dispose();
				}
			});

		}
		return btnReset;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridLayout gridLayout3 = new GridLayout();
			jPanel2 = new JPanel();
			jPanel2.setLayout(gridLayout3);
			gridLayout3.setRows(1);
			jPanel2.add(getRadioExact(), null);
			jPanel2.add(getRadioRegex(), null);
			ButtonGroup group = new ButtonGroup();
			group.add(getRadioExact());
			group.add(getRadioRegex());
		}
		return jPanel2;
	}
	/**
	 * This method initializes radioExact	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getRadioExact() {
		if (radioExact == null) {
			radioExact = new JRadioButton();
			radioExact.setText("Exact (case insensitive)");
			radioExact.setSelected(true);
		}
		return radioExact;
	}
	/**
	 * This method initializes radioRegex	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getRadioRegex() {
		if (radioRegex == null) {
			radioRegex = new JRadioButton();
			radioRegex.setText("Regular expression");
		}
		return radioRegex;
	}
          }  //  @jve:decl-index=0:visual-constraint="10,10"
