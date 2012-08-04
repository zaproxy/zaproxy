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
package org.zaproxy.zap.extension.httpsessions;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.search.SearchPanel;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.SiteMapListener;

/**
 * The HttpSessionsPanel used as a display panel for the {@link ExtensionHttpSessions}, allowing the
 * user to view and control the http sessions.
 */
public class HttpSessionsPanel extends AbstractPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant PANEL_NAME. */
	public static final String PANEL_NAME = "httpsessions";

	/** The extension. */
	private ExtensionHttpSessions extension = null;

	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JComboBox siteSelect = null;
	private JButton newSessionButton = null;
	private JTable sessionsTable = null;

	/** The current site. */
	private String currentSite = null;

	/** The site model. */
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();

	/** The sessions model. */
	private HttpSessionsTableModel sessionsModel = new HttpSessionsTableModel();

	/**
	 * Instantiates a new http session panel.
	 * 
	 * @param extensionHttpSession the extension http session
	 */
	public HttpSessionsPanel(ExtensionHttpSessions extensionHttpSession) {
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
		this.setName(Constant.messages.getString("httpsessions.panel.title"));
		this.setIcon(new ImageIcon(HttpSessionsPanel.class.getResource("/resource/icon/16/session.png")));
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
			panelCommand.setName(Constant.messages.getString("httpsessions.panel.title"));

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
	 * Gets the new session button.
	 * 
	 * @return the new session button
	 */
	private JButton getNewSessionButton() {
		if (newSessionButton == null) {
			newSessionButton = new JButton();
			newSessionButton.setText("New Session");
			newSessionButton.setText(Constant.messages.getString("httpsessions.toolbar.newsession.label"));
			newSessionButton.setIcon(new ImageIcon(SearchPanel.class.getResource("/resource/icon/16/103.png")));
			newSessionButton.setToolTipText(Constant.messages.getString("httpsessions.toolbar.newsession.tooltip"));

			newSessionButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HttpSessionsSite site = getCurrentHttpSessionSite();
					if (site != null)
						site.createEmptySession();
				}
			});

		}
		return newSessionButton;
	}

	/**
	 * Gets the panel's toolbar.
	 * 
	 * @return the panel toolbar
	 */
	private javax.swing.JToolBar getPanelToolbar() {
		if (panelToolbar == null) {

			// Initialize the toolbar
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
			GridBagConstraints newSessionGridBag = new GridBagConstraints();
			GridBagConstraints emptyGridBag = new GridBagConstraints();

			labelGridBag.gridx = 0;
			labelGridBag.gridy = 0;
			labelGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			labelGridBag.anchor = java.awt.GridBagConstraints.WEST;

			siteSelectGridBag.gridx = 1;
			siteSelectGridBag.gridy = 0;
			siteSelectGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			siteSelectGridBag.anchor = java.awt.GridBagConstraints.WEST;

			newSessionGridBag.gridx = 2;
			newSessionGridBag.gridy = 0;
			newSessionGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			newSessionGridBag.anchor = java.awt.GridBagConstraints.WEST;

			emptyGridBag.gridx = 3;
			emptyGridBag.gridy = 0;
			emptyGridBag.weightx = 1.0;
			emptyGridBag.weighty = 1.0;
			emptyGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
			emptyGridBag.anchor = java.awt.GridBagConstraints.WEST;
			emptyGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel label = new JLabel(Constant.messages.getString("httpsessions.toolbar.site.label"));

			panelToolbar.add(label, labelGridBag);
			panelToolbar.add(getSiteSelect(), siteSelectGridBag);
			panelToolbar.add(getNewSessionButton(), newSessionGridBag);

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

	/**
	 * Sets the params table column sizes.
	 */
	private void setParamsTableColumnSizes() {

		sessionsTable.getColumnModel().getColumn(0).setMinWidth(60);
		sessionsTable.getColumnModel().getColumn(0).setMaxWidth(80);
		sessionsTable.getColumnModel().getColumn(0).setPreferredWidth(60); // active

		sessionsTable.getColumnModel().getColumn(1).setMinWidth(120);
		sessionsTable.getColumnModel().getColumn(1).setMaxWidth(400);
		sessionsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // name
	}

	/**
	 * Gets the http sessions table.
	 * 
	 * @return the http sessions table
	 */
	private JTable getHttpSessionsTable() {
		if (sessionsTable == null) {
			sessionsTable = new JTable(sessionsModel);

			sessionsTable.setColumnSelectionAllowed(false);
			sessionsTable.setCellSelectionEnabled(false);
			sessionsTable.setRowSelectionAllowed(true);
			sessionsTable.setAutoCreateRowSorter(true);

			this.setParamsTableColumnSizes();

			sessionsTable.setName(PANEL_NAME);
			sessionsTable.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			sessionsTable.setDoubleBuffered(true);
			sessionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			sessionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						// Select table item
						int row = sessionsTable.rowAtPoint(e.getPoint());
						if (row < 0 || !sessionsTable.getSelectionModel().isSelectedIndex(row)) {
							sessionsTable.getSelectionModel().clearSelection();
							if (row >= 0) {
								sessionsTable.getSelectionModel().setSelectionInterval(row, row);
							}
						}

						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return sessionsTable;
	}

	/**
	 * Gets the site select ComboBox.
	 * 
	 * @return the site select
	 */
	private JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString("httpsessions.toolbar.site.select"));
			siteSelect.setSelectedIndex(0);

			// Add the action listener for when the site is selected
			siteSelect.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {

					String item = (String) siteSelect.getSelectedItem();
					if (item != null && siteSelect.getSelectedIndex() > 0) {
						siteSelected(item);

					} // If the user selects the first option (empty one), force the selection to
						// the first valid site
					else if (siteSelect.getModel().getSize() > 1) {
						siteSelect.setSelectedIndex(1);
						this.actionPerformed(e);
					}
				}
			});
		}
		return siteSelect;
	}

	/**
	 * Add a new site to the {@link ExtensionHttpSessions}.
	 * 
	 * @param site the site
	 */
	public void addSite(String site) {
		if (siteModel.getIndexOf(site) < 0) {
			siteModel.addElement(site);
			if (siteModel.getSize() == 2 && currentSite == null) {
				// First site added, automatically select it
				this.getSiteSelect().setSelectedIndex(1);
				siteSelected(site);
			}
		}
	}

	/**
	 * A new Site was selected.
	 * 
	 * @param site the site
	 */
	private void siteSelected(String site) {
		site = ScanPanel.cleanSiteName(site, true);
		if (!site.equals(currentSite)) {
			siteModel.setSelectedItem(site);

			this.sessionsModel = extension.getHttpSessionsSite(site).getModel();
			this.getHttpSessionsTable().setModel(this.sessionsModel);

			this.setParamsTableColumnSizes();

			currentSite = site;
		}
	}

	/**
	 * Node selected.
	 * 
	 * @param node the node
	 */
	public void nodeSelected(SiteNode node) {
		if (node != null) {
			siteSelected(ScanPanel.cleanSiteName(node, true));
		}
	}

	/**
	 * Reset the panel.
	 */
	public void reset() {
		currentSite = null;

		siteModel.removeAllElements();
		siteSelect.addItem(Constant.messages.getString("httpsessions.toolbar.site.select"));
		siteSelect.setSelectedIndex(0);

		sessionsModel.removeAllElements();
		sessionsModel.fireTableDataChanged();

		sessionsTable.setModel(sessionsModel);

	}

	/**
	 * Gets the current http session site.
	 * 
	 * @return the current http session site, or null if no HttpSessionSite is selected
	 */
	public HttpSessionsSite getCurrentHttpSessionSite() {
		if (currentSite == null)
			return null;
		return extension.getHttpSessionsSite(currentSite);
	}

	/**
	 * Gets the selected http session.
	 * 
	 * @return the selected session, or null if nothing is selected
	 */
	public HttpSession getSelectedSession() {
		return this.sessionsModel.getHttpSessionAt(this.sessionsTable.getSelectedRow());
	}
}
