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
package org.parosproxy.paros.extension.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.OptionsAuthenticationTableModel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsAuthenticationPanel extends AbstractParamPanel {

	private JTable tableAuth = null;
	private JScrollPane jScrollPane = null;
	private OptionsAuthenticationTableModel authModel = null;  //  @jve:decl-index=0:parse,visual-constraint="110,314"
    /**
     * 
     */
    public OptionsAuthenticationPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

        java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

        javax.swing.JLabel jLabel = new JLabel();

        this.setLayout(new GridBagLayout());
        this.setSize(409, 268);
        this.setName("Authentication");
        jLabel.setText("<html><body><p>Enter the HTTP authentication of varioius hosts into the table below.</p><p>Currently basic authentication is supported.  NTLM support works in some hosts but not extensively tested.</p><p>To delete an entry, just leave the host name blank.</p></body></html>");
        jLabel.setPreferredSize(new java.awt.Dimension(494,80));
        jLabel.setMinimumSize(new java.awt.Dimension(494,16));
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridheight = 1;
        gridBagConstraints1.ipady = 65;
        gridBagConstraints1.insets = new java.awt.Insets(10,0,5,0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0D;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.ipadx = 0;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        this.add(jLabel, gridBagConstraints1);
        this.add(getJScrollPane(), gridBagConstraints2);
			
	}
    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#initParam(java.lang.Object)
     */
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    getAuthModel().setListAuth(connectionParam.getListAuth());
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#validateParam(java.lang.Object)
     */
    public void validateParam(Object obj) throws Exception {

    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#saveParam(java.lang.Object)
     */
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    connectionParam.setListAuth(getAuthModel().getListAuth());
    }

    private static int[] width = {280,55,100,100,70};
    
	/**
	 * This method initializes tableAuth	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableAuth() {
		if (tableAuth == null) {
			tableAuth = new JTable();
			tableAuth.setModel(getAuthModel());
			tableAuth.setRowHeight(18);
			tableAuth.setIntercellSpacing(new java.awt.Dimension(1,1));
	        for (int i = 0; i < 5; i++) {
	            TableColumn column = tableAuth.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }
		}
		return tableAuth;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableAuth());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}
	
		
	/**
	 * This method initializes authModel	
	 * 	
	 * @return com.proofsecure.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsAuthenticationTableModel getAuthModel() {
		if (authModel == null) {
			authModel = new OptionsAuthenticationTableModel();
		}
		return authModel;
	}
   }  //  @jve:decl-index=0:visual-constraint="10,10"
