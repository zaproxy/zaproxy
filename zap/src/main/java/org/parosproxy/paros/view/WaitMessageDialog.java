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
package org.parosproxy.paros.view;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;

public class WaitMessageDialog extends AbstractDialog {

	private static final long serialVersionUID = 2951228940444340016L;

	private JPanel jPanel = null;
	private JLabel lblMessage = null;
    /**
     * @throws HeadlessException
     */
    public WaitMessageDialog() throws HeadlessException {
        super();
		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public WaitMessageDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        this.setContentPane(getJPanel());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(282, 118);
        }
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
			
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
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			lblMessage = new JLabel();

			lblMessage.setText(" ");
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 273;
			gridBagConstraints1.ipady = 79;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.insets = new java.awt.Insets(20,20,20,20);
			jPanel.add(lblMessage, gridBagConstraints2);
		}
		return jPanel;
	}
	
	public void setText(String s) {
	    lblMessage.setText(s);
	    this.pack();
	}
	
 }  //  @jve:decl-index=0:visual-constraint="10,10"
