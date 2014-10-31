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

import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.ascan.ActiveScanPanel;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.view.ScanPanel2;

/**
 * The Class SpiderPanel implements the Panel that is shown to the users when selecting the Spider Scan Tab.
 */
public class SpiderPanel extends ScanPanel2 implements ScanListenner2 {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(SpiderPanel.class);

	private static final SpiderPanelTableModel EMPTY_RESULTS_MODEL = new SpiderPanelTableModel();

	/** The Constant defining the PANEL's NAME. */
	public static final String PANEL_NAME = "SpiderPanel";

	private JButton scanButton = null;

	/** The results table. */
	private JXTable resultsTable;

	/** The results pane. */
	private JScrollPane workPane;

	/** The found count name label. */
	private JLabel foundCountNameLabel;

	/** The found count value label. */
	private JLabel foundCountValueLabel;
	
	private ExtensionSpider extension = null;

	/**
	 * Instantiates a new spider panel.
	 * 
	 * @param extension the extension
	 * @param spiderScanParam the spider scan parameters
	 */
	public SpiderPanel(ExtensionSpider extension, SpiderParam spiderScanParam) {
		super("spider", new ImageIcon(SpiderPanel.class.getResource("/resource/icon/16/spider.png")), extension,
				spiderScanParam);
		this.extension = extension;
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, Event.CTRL_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("spider.panel.mnemonic"));

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
		}
		return workPane;
	}

	/**
	 * Sets the spider results table column sizes.
	 */
	private void setScanResultsTableColumnSizes() {
		resultsTable.getColumnModel().getColumn(0).setMinWidth(80);
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(90); // processed

		resultsTable.getColumnModel().getColumn(1).setMinWidth(60);
		resultsTable.getColumnModel().getColumn(1).setPreferredWidth(70); // method

		resultsTable.getColumnModel().getColumn(2).setMinWidth(300); // name

		resultsTable.getColumnModel().getColumn(3).setMinWidth(50);
		resultsTable.getColumnModel().getColumn(3).setPreferredWidth(250); // flags
	}

	/**
	 * Gets the scan results table.
	 * 
	 * @return the scan results table
	 */
	private JXTable getScanResultsTable() {
		if (resultsTable == null) {
			// Create the table with a default, empty TableModel and the proper settings
			resultsTable = new JXTable(EMPTY_RESULTS_MODEL);
			resultsTable.setColumnSelectionAllowed(false);
			resultsTable.setCellSelectionEnabled(false);
			resultsTable.setRowSelectionAllowed(true);
			resultsTable.setAutoCreateRowSorter(true);
			resultsTable.setColumnControlVisible(true);

			this.setScanResultsTableColumnSizes();

			resultsTable.setName(PANEL_NAME);
			resultsTable.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			resultsTable.setDoubleBuffered(true);
			resultsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			// Add hack to force row selection on right click
			resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
				private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
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

	/**
	 * Gets the label storing the name of the count of found URIs.
	 * 
	 * @return the found count name label
	 */
	private JLabel getFoundCountNameLabel() {
		if (foundCountNameLabel == null) {
			foundCountNameLabel = new javax.swing.JLabel();
			foundCountNameLabel.setText(Constant.messages.getString("spider.toolbar.found.label"));
		}
		return foundCountNameLabel;
	}

	/**
	 * Gets the label storing the value for count of found URIs.
	 * 
	 * @return the found count value label
	 */
	private JLabel getFoundCountValueLabel() {
		if (foundCountValueLabel == null) {
			foundCountValueLabel = new javax.swing.JLabel();
			foundCountValueLabel.setText("0");
		}
		return foundCountValueLabel;
	}

	@Override
	protected int addToolBarElements(JToolBar toolBar, Location location, int gridX) {
		if (ScanPanel2.Location.afterProgressBar == location) {
			toolBar.add(getFoundCountNameLabel(), getGBC(gridX++, 0, 0, new Insets(0, 5, 0, 0)));
			toolBar.add(getFoundCountValueLabel(), getGBC(gridX++, 0));
		}
		return gridX;
	}

	/**
	 * Update the count of found URIs.
	 */
	protected void updateFoundCount() {
		GenericScanner2 gs = this.getSelectedScanner();
		if (gs != null && gs instanceof SpiderScan) {
			this.getFoundCountValueLabel().setText(Integer.toString(((SpiderScan) gs).getResourcesFound().size()));
		} else {
			this.getFoundCountValueLabel().setText("");
		}
	}

	@Override
	protected void switchView(final GenericScanner2 scanner) {
		if (View.isInitialised() && !EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						switchView(scanner);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				log.error("Failed to switch view: " + e.getMessage(), e);
			}
			return;
		}
		if (scanner != null) {
			SpiderScan spscan = (SpiderScan)scanner;
			this.getScanResultsTable().setModel(spscan.getResultsTableModel());
			this.setScanResultsTableColumnSizes();
		} else {
			this.getScanResultsTable().setModel(EMPTY_RESULTS_MODEL);
		}
		this.updateFoundCount();
	}

	@Override
	public JButton getNewScanButton() {
		if (scanButton == null) {
			scanButton = new JButton(Constant.messages.getString("spider.toolbar.button.new"));
			scanButton.setIcon(new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/16/spider.png")));
			scanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					extension.showSpiderDialog(null);
				}
			});
		}
		return scanButton;
	}

	@Override
	protected int getNumberOfScansToShow() {
		return extension.getSpiderParam().getMaxScansInUI();
	}
}
