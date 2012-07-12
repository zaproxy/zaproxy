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
package org.zaproxy.zap.extension.httpsession;

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
import org.zaproxy.zap.extension.params.HtmlParameterStats;
import org.zaproxy.zap.extension.params.ParamsTableModel;
import org.zaproxy.zap.extension.params.SiteParameters;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;

/**
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public class HttpSessionPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "httpsession";

	private ExtensionHttpSession extension = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;

	private String currentSite = null;
	private JComboBox siteSelect = null;
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();

	private JTable paramsTable = null;
	private ParamsTableModel paramsModel = new ParamsTableModel();

	/**
	 * Instantiates a new http session panel.
	 * 
	 * @param extensionHttpSession the extension http session
	 */
	public HttpSessionPanel(ExtensionHttpSession extensionHttpSession) {
		super();
		this.extension = extensionHttpSession;
		initialize();
	}

	/**
	 * This method initializes this panel.
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setSize(474, 251);
		this.setName(Constant.messages.getString("httpsession.panel.title"));
		this.setIcon(new ImageIcon(HttpSessionPanel.class.getResource("/resource/icon/16/session.png")));
		this.add(getPanelCommand());
	}

	/**
	 * This method initializes the main panel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {
			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName(Constant.messages.getString("httpsession.panel.title"));

			// Add the two components: toolbar and work pane
			GridBagConstraints toolbarGridBag = new GridBagConstraints();
			GridBagConstraints workPaneGridBag = new GridBagConstraints();

			toolbarGridBag.gridx = 0;
			toolbarGridBag.gridy = 0;
			toolbarGridBag.weightx = 1.0d;
			toolbarGridBag.insets = new java.awt.Insets(2, 2, 2, 2);
			toolbarGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			toolbarGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

			workPaneGridBag.gridx = 0;
			workPaneGridBag.gridy = 1;
			workPaneGridBag.weightx = 1.0;
			workPaneGridBag.weighty = 1.0;
			workPaneGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			workPaneGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			workPaneGridBag.fill = java.awt.GridBagConstraints.BOTH;

			panelCommand.add(this.getPanelToolbar(), toolbarGridBag);
			panelCommand.add(getWorkPane(), workPaneGridBag);
		}
		return panelCommand;
	}

	/**
	 * Gets the panel's toolbar.
	 * 
	 * @return the panel toolbar
	 */
	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {

			// Initialize the toolbal
			panelToolbar = new javax.swing.JToolBar();
			panelToolbar.setLayout(new java.awt.GridBagLayout());
			panelToolbar.setEnabled(true);
			panelToolbar.setFloatable(false);
			panelToolbar.setRollover(true);
			panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
			panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			panelToolbar.setName("HttpSessionToolbar");

			// Add elements
			GridBagConstraints labelGridBag = new GridBagConstraints();
			GridBagConstraints siteSelectGridBag = new GridBagConstraints();
			GridBagConstraints emptyGridBag = new GridBagConstraints();

			labelGridBag.gridx = 0;
			labelGridBag.gridy = 0;
			labelGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			labelGridBag.anchor = java.awt.GridBagConstraints.WEST;

			siteSelectGridBag.gridx = 1;
			siteSelectGridBag.gridy = 0;
			siteSelectGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			siteSelectGridBag.anchor = java.awt.GridBagConstraints.WEST;

			emptyGridBag.gridx = 2;
			emptyGridBag.gridy = 0;
			emptyGridBag.weightx = 1.0;
			emptyGridBag.weighty = 1.0;
			emptyGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			emptyGridBag.anchor = java.awt.GridBagConstraints.EAST;
			emptyGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel label = new JLabel(Constant.messages.getString("httpsession.toolbar.site.label"));

			panelToolbar.add(label, labelGridBag);
			panelToolbar.add(getSiteSelect(), siteSelectGridBag);

			// Add an empty JLabel to fill the space
			panelToolbar.add(new JLabel(), emptyGridBag);
		}
		return panelToolbar;
	}

	/**
	 * Gets the work pane where data is shown.
	 * 
	 * @return the work pane
	 */
	private JScrollPane getWorkPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getHttpSessionsTable());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void setParamsTableColumnSizes() {

		paramsTable.getColumnModel().getColumn(0).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(0).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // type

		paramsTable.getColumnModel().getColumn(1).setMinWidth(100);
		paramsTable.getColumnModel().getColumn(1).setMaxWidth(400);
		paramsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // name

		paramsTable.getColumnModel().getColumn(2).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(2).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // used

		paramsTable.getColumnModel().getColumn(3).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(3).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // numvals

		paramsTable.getColumnModel().getColumn(4).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(4).setMaxWidth(200);
		paramsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // % change

		paramsTable.getColumnModel().getColumn(5).setMinWidth(50);
		paramsTable.getColumnModel().getColumn(5).setMaxWidth(400);
		paramsTable.getColumnModel().getColumn(5).setPreferredWidth(200); // flags

	}

	protected JTable getHttpSessionsTable() {
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
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						// Select table item
						int row = paramsTable.rowAtPoint(e.getPoint());
						if (row < 0 || !paramsTable.getSelectionModel().isSelectedIndex(row)) {
							paramsTable.getSelectionModel().clearSelection();
							if (row >= 0) {
								paramsTable.getSelectionModel().setSelectionInterval(row, row);
							}
						}

						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return paramsTable;
	}

	/**
	 * Gets the site select ComboBox.
	 * 
	 * @return the site select
	 */
	private JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString("httpsession.toolbar.site.select"));
			siteSelect.setSelectedIndex(0);

			// Add the action listener for when the site is selected
			siteSelect.addActionListener(new java.awt.event.ActionListener() {
				@Override
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
		if (!site.equals(currentSite)) {
			siteModel.setSelectedItem(site);

			// TODO: this.getParamsTable().setModel(extension.getSiteParameters(site).getModel());

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
		siteSelect.addItem(Constant.messages.getString("httpsession.toolbar.site.select"));
		siteSelect.setSelectedIndex(0);

		paramsModel.removeAllElements();
		paramsModel.fireTableDataChanged();

		paramsTable.setModel(paramsModel);

	}

	protected HtmlParameterStats getSelectedParam() {

		// TODO type is localized :(
		String type = (String) this.getHttpSessionsTable().getValueAt(this.getHttpSessionsTable().getSelectedRow(), 0);
		String name = (String) this.getHttpSessionsTable().getValueAt(this.getHttpSessionsTable().getSelectedRow(), 1);

		// TODO: SiteParameters sps = extension.getSiteParameters(currentSite);
		// if (sps != null) {
		// return sps.getParam(HtmlParameter.Type.valueOf(type.toLowerCase()), name); // TODO HACK!
		// }
		return null;
	}
}
