/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

package org.zaproxy.zap.extension.spider;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.view.ScanPanel;

/**
 * The Class SpiderPanel implements the Panel that is shown to the users when selecting the Spider
 * Scan Tab.
 */
public class SpiderPanel extends ScanPanel implements ScanListenner {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderPanel.class);

	/** The Constant defining the PANEL's NAME. */
	public static final String PANEL_NAME = "SpiderPanel";

	/** The results table. */
	private JTable resultsTable;

	/** The results pane. */
	private JScrollPane workPane;

	/** The results model. */
	private SpiderPanelTableModel currentResultsModel;

	/**
	 * Instantiates a new spider panel.
	 * 
	 * @param extension the extension
	 * @param spiderScanParam the spider scan parameters
	 */
	public SpiderPanel(ExtensionSpider extension, SpiderParam spiderScanParam) {
		super("spider", new ImageIcon(SpiderPanel.class.getResource("/resource/icon/16/spider.png")), extension,
				spiderScanParam);
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.view.ScanPanel#newScanThread(java.lang.String,
	 * org.parosproxy.paros.common.AbstractParam) */
	@Override
	protected ScanThread newScanThread(String site, AbstractParam params) {
		SpiderThread st = new SpiderThread((ExtensionSpider) this.getExtension(), site, this);
		return st;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.view.ScanPanel#getSiteNode(java.lang.String) */
	protected SiteNode getSiteNode(String site) {
		return super.getSiteNode(site);
	}

	/* (non-Javadoc)
	 * 
	 * @see org.zaproxy.zap.view.ScanPanel#switchView(java.lang.String) */
	@Override
	protected void switchView(String site) {
		this.updateCurrentScanResultsModel(site);
		this.getScanResultsTable().setModel(this.currentResultsModel);
		this.setScanResultsTableColumnSizes();
	}

	/**
	 * This method initializes the working Panel.
	 * 
	 * @return javax.swing.JScrollPane
	 */
	@Override
	protected JScrollPane getWorkPanel() {
		if (workPane == null) {
			workPane = new JScrollPane();
			workPane.setName("SpiderResultsPane");
			workPane.setViewportView(getScanResultsTable());
			workPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			workPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return workPane;
	}

	/**
	 * Sets the spider results table column sizes.
	 */
	private void setScanResultsTableColumnSizes() {
		resultsTable.getColumnModel().getColumn(0).setMinWidth(80);
		resultsTable.getColumnModel().getColumn(0).setMaxWidth(90);
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(90); // processed

		resultsTable.getColumnModel().getColumn(1).setMinWidth(60);
		resultsTable.getColumnModel().getColumn(1).setMaxWidth(80);
		resultsTable.getColumnModel().getColumn(1).setPreferredWidth(70); // method

		resultsTable.getColumnModel().getColumn(2).setMinWidth(300); // name

		resultsTable.getColumnModel().getColumn(3).setMinWidth(50);
		resultsTable.getColumnModel().getColumn(3).setMaxWidth(600);
		resultsTable.getColumnModel().getColumn(3).setPreferredWidth(250); // flags
	}

	/**
	 * Updates the current scan results model to the proper one.
	 * 
	 * @param site the site
	 */
	private void updateCurrentScanResultsModel(String site) {
		SpiderThread st = (SpiderThread) this.getScanThread(site);
		this.currentResultsModel = st.getResultsTableModel();
	}

	/**
	 * Gets the scan results table.
	 * 
	 * @return the scan results table
	 */
	private JTable getScanResultsTable() {
		if (resultsTable == null) {
			// Create the table with a default, empty TableModel and the proper settings
			resultsTable = new JTable(new SpiderPanelTableModel());
			resultsTable.setColumnSelectionAllowed(false);
			resultsTable.setCellSelectionEnabled(false);
			resultsTable.setRowSelectionAllowed(true);
			resultsTable.setAutoCreateRowSorter(true);

			this.setScanResultsTableColumnSizes();

			resultsTable.setName(PANEL_NAME);
			resultsTable.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			resultsTable.setDoubleBuffered(true);
			resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			// Add hack to force row selection on right click
			resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						// Select table item
						int row = resultsTable.rowAtPoint(e.getPoint());
						if (row < 0 || !resultsTable.getSelectionModel().isSelectedIndex(row)) {
							resultsTable.getSelectionModel().clearSelection();
							if (row >= 0) {
								resultsTable.getSelectionModel().setSelectionInterval(row, row);
							}
						}
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
			});
		}
		return resultsTable;
	}
}
