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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/12/19 Code Cleanup: Moved array brackets from variable name to type
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/11/06 Added warning that filters will be removed
// ZAP: 2015/02/16 Issue 1528: Support user defined font size
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages 

package org.parosproxy.paros.extension.filter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.LayoutHelper;

public class AllFilterPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable tableFilter = null;
	private JScrollPane jScrollPane = null;

    public AllFilterPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

        java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

        java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

        this.setLayout(new GridBagLayout());
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(375, 204);
	    }
	    this.setPreferredSize(new Dimension(375, 204));
        this.setName("categoryPanel");
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.weighty = 1.0;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridy = 2;
        gridBagConstraints11.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints11.gridwidth = 2;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        
        this.add(new JLabel(Constant.messages.getString("filter.warning")), LayoutHelper.getGBC(0, 0, 2, 1.0));
        this.add(getBtnEnableAll(), gridBagConstraints1);
        this.add(getBtnDisableAll(), gridBagConstraints2);
        this.add(getJScrollPane(), gridBagConstraints11);
		
	}
	private static final int[] width = {400,50, 20};
	private JButton btnEnableAll = null;
	private JButton btnDisableAll = null;
	private AllFilterTableModel allFilterTableModel = null;  //  @jve:decl-index=0:parse,visual-constraint="43,246"
	/**
	 * This method initializes tableFilter	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableFilter() {
		if (tableFilter == null) {
			tableFilter = new JTable();
            tableFilter.setRowHeight(DisplayUtils.getScaledSize(18));
			tableFilter.setIntercellSpacing(new java.awt.Dimension(1,1));
			tableFilter.setModel(getAllFilterTableModel());
	        for (int i = 0; i < width.length; i++) {
	            TableColumn column = tableFilter.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }
	        TableColumn col = tableFilter.getColumnModel().getColumn(2);
	        col.setCellRenderer(new AllFilterTableRenderer());
	        col.setCellEditor(new AllFilterTableEditor(getAllFilterTableModel()));
		}
		return tableFilter;
	}

    @Override
    public void initParam(Object obj) {
        
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        
    }
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableFilter());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
			jScrollPane.setEnabled(false);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes btnEnableAll	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnEnableAll() {
		if (btnEnableAll == null) {
			btnEnableAll = new JButton();
			btnEnableAll.setText(Constant.messages.getString("filter.button.enableall"));
			btnEnableAll.setEnabled(false);
			btnEnableAll.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    


				}
			});

		}
		return btnEnableAll;
	}
	/**
	 * This method initializes btnDisableAll	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnDisableAll() {
		if (btnDisableAll == null) {
			btnDisableAll = new JButton();
			btnDisableAll.setText(Constant.messages.getString("filter.button.disableall"));
			btnDisableAll.setEnabled(false);
			btnDisableAll.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					
				}
			});

		}
		return btnDisableAll;
	}
	/**
	 * This method initializes allFilterTableModel	
	 * 	
	 * @return org.parosproxy.paros.extension.filter.AllFilterTableModel	
	 */    
	AllFilterTableModel getAllFilterTableModel() {
		if (allFilterTableModel == null) {
			allFilterTableModel = new AllFilterTableModel();
		}
		return allFilterTableModel;
	}

	@Override
	public String getHelpIndex() {
		// ZAP: added help index support
		return "ui.dialogs.filter";
	}
       }  //  @jve:decl-index=0:visual-constraint="10,10"
