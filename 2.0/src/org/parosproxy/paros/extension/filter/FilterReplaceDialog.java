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
// ZAP: 2012/04/14 Changed to discard all edits in the actions of the buttons 
// "OK" and "Cancel".
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/06/25 Changed visibility of getJPanel() and initialize() from
// private to protected. Use i18n strings for labels and warnings.
// ZAP: 2012/07/09 Added 10 more pixels to the dialog's height.
// Changed visibility of getJPanel1().
package org.parosproxy.paros.extension.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.ZapTextField;

public class FilterReplaceDialog extends AbstractDialog {


	private static final long serialVersionUID = 3953017269494541360L;
	
	private JPanel jPanel = null;
	private ZapTextField txtPattern = null;
	private ZapTextField txtReplaceWith = null;
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
    }
    
    public void setView(ViewDelegate view) {
        this.view = view;
    }

	/**
	 * This method initializes this
	 */
	protected void initialize() {
		// ZAP: Changed visibility from private to protected.
        this.setContentPane(getJPanel());
        // ZAP: Added 10 more pixels to the dialog's height
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(346, 166);
	    }
	    this.setPreferredSize(new Dimension(346, 166));
		this.pack();
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	protected JPanel getJPanel() {
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
			// ZAP: i18n
			jLabel.setText(Constant.messages.getString("filter.replacedialog.pattern") + ":");
			jLabel1.setText(Constant.messages.getString("filter.replacedialog.replace") + ":");
			jLabel2.setText(Constant.messages.getString("filter.replacedialog.title"));
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
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	public ZapTextField getTxtPattern() {
		if (txtPattern == null) {
			txtPattern = new ZapTextField();
		}
		return txtPattern;
	}
	/**
	 * This method initializes txtReplaceWith	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	public ZapTextField getTxtReplaceWith() {
		if (txtReplaceWith == null) {
			txtReplaceWith = new ZapTextField();
		}
		return txtReplaceWith;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	protected JPanel getJPanel1() {
		// ZAP: Changed visibility from private to protected
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
			btnOK.setText(Constant.messages.getString("all.button.ok"));
			btnOK.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    try {
				    	Pattern.compile(getTxtPattern().getText());
				    } catch (Exception e1) {
				        // ZAP: i18n
				        view.showWarningDialog(Constant.messages.getString("filter.replacedialog.invalidpattern"));
				        getTxtPattern().grabFocus();
				        return;
				    }
				    getTxtPattern().discardAllEdits();
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
			btnCancel.setText(Constant.messages.getString("all.button.cancel"));
			btnCancel.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getTxtPattern().discardAllEdits();
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
