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
package org.zaproxy.zap.extension.params;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ParamsPanel extends AbstractPanel{
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "params";
	
	private ExtensionParams extension = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;

	private String currentSite = null;
	private JComboBox siteSelect = null;
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();
	//private JButton optionsButton = null;

	private JTable paramsTable = null;
	private ParamsTableModel paramsModel = new ParamsTableModel();
	
    //private static Log log = LogFactory.getLog(ParamsPanel.class);
    
    /**
     * 
     */
    public ParamsPanel(ExtensionParams extension) {
        super();
        this.extension = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("params.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/179.png")));	// 'form' icon
        this.add(getPanelCommand(), getPanelCommand().getName());
	}
	/**

	 * This method initializes panelCommand	

	 * 	

	 * @return javax.swing.JPanel	

	 */    
	/**/
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName("Params");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			
			panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
			panelCommand.add(getJScrollPane(), gridBagConstraints2);
			
		}
		return panelCommand;
	}
	/**/

	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {
			
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800,30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("ParamsToolbar");
			
			GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsx = new GridBagConstraints();

			gridBagConstraints0.gridx = 0;
			gridBagConstraints0.gridy = 0;
			gridBagConstraints0.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;
			
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			
			gridBagConstraints2.gridx = 2;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsx.gridx = 3;
			gridBagConstraintsx.gridy = 0;
			gridBagConstraintsx.weightx = 1.0;
			gridBagConstraintsx.weighty = 1.0;
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraintsx.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel t1 = new JLabel();

			//panelToolbar.add(getOptionsButton(), gridBagConstraints0);

			panelToolbar.add(new JLabel(Constant.messages.getString("params.toolbar.site.label")), gridBagConstraints1);
			panelToolbar.add(getSiteSelect(), gridBagConstraints2);
			
			panelToolbar.add(t1, gridBagConstraintsx);
		}
		return panelToolbar;
	}

	/*
	 * Displaying the ANTI CSRF options might not actually make that much sense...
	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("params.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/041.png")));	// 'Gears' icon
			optionsButton.setEnabled(false);
			optionsButton.addActionListener(new ActionListener () {

				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(Constant.messages.getString("options.acsrf.title"));
				}

			});

		}
		return optionsButton;
	}
	*/

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getParamsTable());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	
	private void setParamsTableColumnSizes() {
		
		paramsTable.getColumnModel().getColumn(0).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(0).setPreferredWidth(100);	// type
		
		paramsTable.getColumnModel().getColumn(1).setMinWidth(100);
		paramsTable.getColumnModel().getColumn(1).setMaxWidth(400);
		paramsTable.getColumnModel().getColumn(1).setPreferredWidth(200);	// name
		
		paramsTable.getColumnModel().getColumn(2).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(2).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(2).setPreferredWidth(100);	// used
		
		paramsTable.getColumnModel().getColumn(3).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(3).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(3).setPreferredWidth(100);	// numvals
		
		paramsTable.getColumnModel().getColumn(4).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(4).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(4).setPreferredWidth(100);	// % change
		
		paramsTable.getColumnModel().getColumn(5).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(5).setMaxWidth(400);
		paramsTable.getColumnModel().getColumn(5).setPreferredWidth(200);	// flags
		
	}
	
	protected JTable getParamsTable() {
		if (paramsTable == null) {
			paramsTable = new JTable(paramsModel);

			paramsTable.setColumnSelectionAllowed(false);
			paramsTable.setCellSelectionEnabled(false);
			paramsTable.setRowSelectionAllowed(true);
			paramsTable.setAutoCreateRowSorter(true);

			this.setParamsTableColumnSizes();

			paramsTable.setName(PANEL_NAME);
			paramsTable.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			paramsTable.setDoubleBuffered(true);
			paramsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			paramsTable.addMouseListener(new java.awt.event.MouseAdapter() { 
			    public void mousePressed(java.awt.event.MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						// Select table item
					    int row = paramsTable.rowAtPoint( e.getPoint() );
					    if ( row < 0 || !paramsTable.getSelectionModel().isSelectedIndex( row ) ) {
					    	paramsTable.getSelectionModel().clearSelection();
					    	if ( row >= 0 ) {
					    		paramsTable.getSelectionModel().setSelectionInterval( row, row );
					    	}
					    }
						
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }
			    }
			});
		}
		return paramsTable;
	}

	private JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString("params.toolbar.site.select"));
			siteSelect.setSelectedIndex(0);

			siteSelect.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    String item = (String) siteSelect.getSelectedItem();
				    if (item != null && siteSelect.getSelectedIndex() > 0) {
				        siteSelected(item);
				    }
				}
			});
		}
		return siteSelect;
	}

	public void addSite(String site) {
		site = ScanPanel.cleanSiteName(site, true);
		if (siteModel.getIndexOf(site) < 0) {
			siteModel.addElement(site);
			if (siteModel.getSize() == 2 && currentSite == null) {
				// First site added, automatically select it
				this.getSiteSelect().setSelectedIndex(1);
				siteSelected(site);
			}
		}
	}
	
	private void siteSelected(String site) {
		site = ScanPanel.cleanSiteName(site, true);
		if (! site.equals(currentSite)) {
			siteModel.setSelectedItem(site);
			
			this.getParamsTable().setModel(extension.getSiteParameters(site).getModel());
			
			this.setParamsTableColumnSizes();

			currentSite = site;
		}
	}

	public void nodeSelected(SiteNode node) {
		if (node != null) {
			siteSelected(ScanPanel.cleanSiteName(node, true));
		}
	}

	public void reset() {
		currentSite = null;
		
		siteModel.removeAllElements();
		siteSelect.addItem(Constant.messages.getString("params.toolbar.site.select"));
		siteSelect.setSelectedIndex(0);
		
		paramsModel.removeAllElements();
		paramsModel.fireTableDataChanged();
		
		paramsTable.setModel(paramsModel);

	}
	
	protected HtmlParameterStats getSelectedParam() {

		// TODO type is localized :(
		String type = (String) this.getParamsTable().getValueAt(this.getParamsTable().getSelectedRow(), 0);
		String name = (String) this.getParamsTable().getValueAt(this.getParamsTable().getSelectedRow(), 1);

		SiteParameters sps = extension.getSiteParameters(currentSite);
		if (sps != null) {
			return sps.getParam(HtmlParameter.Type.valueOf(type.toLowerCase()), name);	// TODO HACK!
		}
		return null;
	}
}
