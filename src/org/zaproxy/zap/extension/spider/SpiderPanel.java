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
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;

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

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(SpiderPanel.class);

	public static final String PANEL_NAME = "spider";

	private JTable resultsTable;
	private JScrollPane resultsPane;

	private SpiderPanelTableModel resultsModel;

	/**
	 * Instantiates a new spider panel.
	 * 
	 * @param extension the extension
	 * @param spiderScanParam the spider scan param
	 */
	public SpiderPanel(ExtensionSpider extension, SpiderParam spiderScanParam) {
		super("spider", new ImageIcon(SpiderPanel.class.getResource("/resource/icon/16/spider.png")), extension,
				spiderScanParam);
		log.error("Res Tab2:" + resultsTable);
		log.error("Res Pane2:" + resultsPane);
		
	}

	@Override
	protected ScanThread newScanThread(String site, AbstractParam params) {
		SpiderThread st = new SpiderThread((ExtensionSpider) this.getExtension(), site, this);
		st.setStartNode(this.getSiteNode(site));
		return st;
	}

	@Override
	protected void startScan() {
		this.clear();
		// Only allow one spider at a time, due to the way it uses the db
		this.getSiteSelect().setEnabled(false);
		log.error("Res Tab:" + resultsTable);
		log.error("Res Pane:" + resultsPane);
		super.startScan();
		log.error("Res Tab2:" + resultsTable);
		log.error("Res Pane2:" + resultsPane);
	}

	@Override
	protected void siteSelected(String site) {
		// Only allow one spider at a time, due to the way it uses the db
		if (this.getSiteSelect().isEnabled()) {
			super.siteSelected(site);
		}
		log.error("Res Tab:" + resultsTable);
		log.error("Res Pane:" + resultsPane);
	}

	@Override
	public void scanFinshed(String host) {
		super.scanFinshed(host);
		// Only allow one spider at a time, due to the way it uses the db
		this.getSiteSelect().setEnabled(true);
	}

	@Override
	public boolean isScanning(SiteNode node, boolean incPort) {
		// Only allow one spider at a time, due to the way it uses the db
		return !this.getSiteSelect().isEnabled();
	}

	@Override
	protected void switchView(String site) {
		// Cant switch views in this version
	}

	/**
	 * This method initializes the working SplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	@Override
	protected JScrollPane getWorkPanel() {
		if (resultsPane == null) {
			resultsPane = new JScrollPane();
			resultsPane.setName("SpiderResultsPane");
			resultsPane.setViewportView(getScanResultsTable());
			resultsPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			resultsPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return resultsPane;
	}

	/**
	 * Sets the spider results table column sizes.
	 */
	private void setScanResultsTableColumnSizes() {

		resultsTable.getColumnModel().getColumn(0).setMinWidth(80);
		resultsTable.getColumnModel().getColumn(0).setMaxWidth(90);
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(80); // inScope

		resultsTable.getColumnModel().getColumn(1).setMinWidth(300);	//name

		resultsTable.getColumnModel().getColumn(2).setMinWidth(60);
		resultsTable.getColumnModel().getColumn(2).setMaxWidth(80);
		resultsTable.getColumnModel().getColumn(2).setPreferredWidth(60); // method
		
		resultsTable.getColumnModel().getColumn(3).setMinWidth(150);
		resultsTable.getColumnModel().getColumn(3).setMaxWidth(300);
		resultsTable.getColumnModel().getColumn(3).setPreferredWidth(200); // flags
		
	}

	private SpiderPanelTableModel getScanResultsModel() {
		if (resultsModel == null)
			resultsModel = new SpiderPanelTableModel();
		return resultsModel;
	}

	private JTable getScanResultsTable() {
		if (resultsTable == null) {
			resultsTable = new JTable(getScanResultsModel());

			resultsTable.setColumnSelectionAllowed(false);
			resultsTable.setCellSelectionEnabled(false);
			resultsTable.setRowSelectionAllowed(true);
			resultsTable.setAutoCreateRowSorter(true);

			this.setScanResultsTableColumnSizes();

			resultsTable.setName(PANEL_NAME);
			resultsTable.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			resultsTable.setDoubleBuffered(true);
			resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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

		log.error("Init Res Tab:" + resultsTable);
		log.error("Init Res Pane:" + resultsPane);
		return resultsTable;
	}

	/**
	 * Add new Spider Scan Result.
	 * 
	 * @param uri the url
	 * @param method the method
	 * @param flags the flags
	 * @param inScope the in scope
	 */
	public void addNewSpiderScanResult(final String uri, final String method, final String flags, final boolean inScope) {
		resultsModel.addScanResult(uri, method, flags, inScope);
	}

	/**
	 * Clear the Panel.
	 */
	void clear() {
		getScanResultsModel().removeAllElements();
	}

}
