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
package org.zaproxy.zap.extension.bruteforce;

import java.awt.CardLayout;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.FilenameExtensionFilter;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapToggleButton;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

import com.sittinglittleduck.DirBuster.BaseCase;

public class BruteForcePanel extends AbstractPanel implements BruteForceListenner {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(BruteForcePanel.class);

	/**
	 * @deprecated (2.3.0) Replaced by {@link #MESSAGE_CONTAINER_NAME}.
	 */
	@Deprecated
	public static final String PANEL_NAME = "bruteforce";

	/**
	 * The name of the forced browse HTTP messages container.
	 * 
	 * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
	 */
	public static final String MESSAGE_CONTAINER_NAME = "ForcedBrowseMessageContainer";

	private static final BruteForceTableModel EMPTY_RESULTS_MODEL = new BruteForceTableModel();
	
	//private ExtensionBruteForce extension = null;
	private BruteForceParam bruteForceParam = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JLabel activeScansNameLabel = null;
	private JLabel activeScansValueLabel = null;
	private List<ScanTarget> activeScans = new ArrayList<>();
    private List<ForcedBrowseFile> fileList = null;
	private JComboBox<ForcedBrowseFile> fileSelect = null;
	private DefaultComboBoxModel<ForcedBrowseFile> fileSelectModel = null;

	private String fileDirectory = Constant.getInstance().DIRBUSTER_DIR;
	private String customFileDirectory = Constant.getInstance().DIRBUSTER_CUSTOM_DIR;
	private String fileExtension = ".txt";

	private ScanTarget currentSite = null;
	private JComboBox<ScanTarget> siteSelect = null;
	// The siteModel entries are all HTML, with the active ones in bold
	private SortedComboBoxModel<ScanTarget> siteModel = new SortedComboBoxModel<>();

	private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private ZapToggleButton pauseScanButton = null;
	private JButton optionsButton = null;
	//private JButton launchButton = null;
	private HistoryReferencesTable bruteForceTable = null;
	private JProgressBar progressBar = null;
	private Map <ScanTarget, BruteForce> bruteForceMap = new HashMap <>();

	private ScanStatus scanStatus = null;
	private Mode mode = Control.getSingleton().getMode();

	private ScanTarget noSelectionScanTarget;

    private static Logger log = Logger.getLogger(BruteForcePanel.class);
    
    /**
     * @param bruteForceParam 
     * 
     */
    public BruteForcePanel(ExtensionBruteForce extension, BruteForceParam bruteForceParam) {
        super();
        //this.extension = extension;
        this.bruteForceParam = bruteForceParam;
        this.fileSelectModel = new DefaultComboBoxModel<>();
        this.noSelectionScanTarget = new DummyScanTarget(Constant.messages.getString("bruteforce.toolbar.site.select"));
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private  void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("bruteforce.panel.title"));
		this.setIcon(new ImageIcon(BruteForcePanel.class.getResource(ExtensionBruteForce.HAMMER_ICON_RESOURCE)));
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F, Event.CTRL_MASK | Event.ALT_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("bruteforce.panel.mnemonic"));
		this.add(getPanelCommand(), getPanelCommand().getName());
        
        // Wont need to do this if/when this class is changed to extend ScanPanel
        scanStatus = new ScanStatus(
        				new ImageIcon(
        					BruteForcePanel.class.getResource(ExtensionBruteForce.HAMMER_ICON_RESOURCE)),
        					Constant.messages.getString("bruteforce.panel.title"));
       
        if (View.isInitialised()) {
        	View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(scanStatus.getCountLabel());
        }

	}
	
	/**
	 * This method initializes panelCommand	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private javax.swing.JPanel getPanelCommand() {
		if (panelCommand == null) {

			panelCommand = new javax.swing.JPanel();
			panelCommand.setLayout(new java.awt.GridBagLayout());
			panelCommand.setName("BruteForce");
			
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
			panelToolbar.setName("BruteForceToolbar");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			//Dummy
			GridBagConstraints gridBagConstraintsx = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsy = new GridBagConstraints();

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints6.gridx = 5;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints7.gridx = 6;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints8.gridx = 7;
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.insets = new java.awt.Insets(0,5,0,5);	// Slight indent
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;

			gridBagConstraints9.gridx = 8;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.EAST;

			gridBagConstraints10.gridx = 9;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;

			gridBagConstraintsx.gridx = 20;
			gridBagConstraintsx.gridy = 0;
			gridBagConstraintsx.weightx = 1.0;
			//gridBagConstraintsx.weighty = 1.0;
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.WEST;
			
			gridBagConstraintsy.gridx = 21;
			gridBagConstraintsy.gridy = 0;
			gridBagConstraintsy.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsy.anchor = java.awt.GridBagConstraints.WEST;


			panelToolbar.add(new JLabel(Constant.messages.getString("bruteforce.toolbar.site.label")), gridBagConstraints1);
			panelToolbar.add(getSiteSelect(), gridBagConstraints2);
			panelToolbar.add(new JLabel(Constant.messages.getString("bruteforce.toolbar.list.label")), gridBagConstraints3);
			panelToolbar.add(getFileSelect(), gridBagConstraints4);
			
			panelToolbar.add(getStartScanButton(), gridBagConstraints5);
			panelToolbar.add(getPauseScanButton(), gridBagConstraints6);
			panelToolbar.add(getStopScanButton(), gridBagConstraints7);
			panelToolbar.add(getProgressBar(), gridBagConstraints8);
			panelToolbar.add(getActiveScansNameLabel(), gridBagConstraints9);
			panelToolbar.add(getActiveScansValueLabel(), gridBagConstraints10);

			panelToolbar.add(new JLabel(), gridBagConstraintsx);	// Filler
			//panelToolbar.add(getLaunchButton(), gridBagConstraintsx);
			panelToolbar.add(getOptionsButton(), gridBagConstraintsy);
		}
		return panelToolbar;
	}

	private JLabel getActiveScansNameLabel() {
		if (activeScansNameLabel == null) {
			activeScansNameLabel = new javax.swing.JLabel();
			activeScansNameLabel.setText(Constant.messages.getString("bruteforce.toolbar.ascans.label"));
		}
		return activeScansNameLabel;
	}
	
	private JLabel getActiveScansValueLabel() {
		if (activeScansValueLabel == null) {
			activeScansValueLabel = new javax.swing.JLabel();
			activeScansValueLabel.setText(String.valueOf(activeScans.size()));
		}
		return activeScansValueLabel;
	}
	
	private void setActiveScanLabels() {
		getActiveScansValueLabel().setText(String.valueOf(activeScans.size()));
		StringBuilder sb = new StringBuilder();
		Iterator <ScanTarget> iter = activeScans.iterator();
		sb.append("<html>");
		while (iter.hasNext()) {
			sb.append(iter.next().toPlainString());
			sb.append("<br>");
		}
		sb.append("</html>");
		
		final String toolTip = sb.toString();
		
		getActiveScansNameLabel().setToolTipText(toolTip);
		getActiveScansValueLabel().setToolTipText(toolTip);
		
		scanStatus.setScanCount(activeScans.size());
	}
	
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);	// Max will change as scan progresses
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setEnabled(false);
		}
		return progressBar;
	}
	
	private JButton getStartScanButton() {
		if (startScanButton == null) {
			startScanButton = new JButton();
			startScanButton.setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.start"));
			startScanButton.setIcon(new ImageIcon(BruteForcePanel.class.getResource("/resource/icon/16/131.png")));
			startScanButton.setEnabled(false);
			startScanButton.addActionListener(new ActionListener () {

				@Override
				public void actionPerformed(ActionEvent e) {
					startScan();
				}

			});

		}
		return startScanButton;
	}

	private JButton getStopScanButton() {
		if (stopScanButton == null) {
			stopScanButton = new JButton();
			stopScanButton.setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.stop"));
			stopScanButton.setIcon(new ImageIcon(BruteForcePanel.class.getResource("/resource/icon/16/142.png")));
			stopScanButton.setEnabled(false);
			stopScanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopScan();
				}
			});
		}
		return stopScanButton;
	}

	private JToggleButton getPauseScanButton() {
		if (pauseScanButton == null) {
			pauseScanButton = new ZapToggleButton();
			pauseScanButton.setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
			pauseScanButton.setSelectedToolTipText(Constant.messages.getString("bruteforce.toolbar.button.unpause"));
			pauseScanButton.setIcon(new ImageIcon(BruteForcePanel.class.getResource("/resource/icon/16/141.png")));
			pauseScanButton.setEnabled(false);
			pauseScanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					pauseScan();
				}
			});
		}
		return pauseScanButton;
	}

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(BruteForcePanel.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(
							Constant.messages.getString("bruteforce.options.title"));
				}
			});
		}
		return optionsButton;
	}

	// Not working yet:)
	/*
	private JButton getLaunchButton() {
		if (launchButton == null) {
			launchButton = new JButton();
			launchButton.setToolTipText("TBI LAUNCH");
			launchButton.setIcon(new ImageIcon(BruteForcePanel.class.getResource("/resource/icon/16/142.png")));
			launchButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					launchDirBuster();
				}

			});
		}
		return launchButton;
	}

	private void launchDirBuster() {
		StringBuilder sb = new StringBuilder();
		sb.append("/usr/bin/java -classpath ");
		
		sb.append("lib/BrowserLauncher2-1_3.jar:");
		sb.append("lib/commons-codec-1.2.jar:");
		sb.append("lib/commons-collections-3.1.jar:");
		sb.append("lib/commons-configuration-1.1.jar:");
		sb.append("lib/commons-httpclient-3.0.jar:");
		sb.append("lib/commons-lang-2.0.jar:");
		sb.append("lib/commons-logging-api.jar:");
		sb.append("lib/commons-logging.jar:");
		sb.append("lib/DirBuster-0.12.jar:");
		sb.append("lib/hsqldb.jar:");
		sb.append("lib/java-getopt-1.0.13.jar:");
		sb.append("lib/jdom.jar:");
		sb.append("lib/jericho-html-2.6.jar:");
		sb.append("lib/jh.jar:");
		sb.append("lib/js.jar:");
		sb.append("lib/log4j-1.2.8.jar:");
		sb.append("lib/looks-2.2.0.jar:");
		sb.append("lib/swing-layout-1.0.3.jar:");
		sb.append("lib/zaphelp.jar ");
		
		sb.append("com.sittinglittleduck.DirBuster.Start");

		System.out.println(sb.toString());
		try {
			// TODO works from cmdline, but not from ZAP :(
			Process proc = Runtime.getRuntime().exec(sb.toString());
			
			// Currently exists with 1...
			System.out.println("Exit value=" + proc.waitFor());
			//System.out.println("Exit value=" + proc.exitValue());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	*/

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getBruteForceTable());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return jScrollPane;
	}

	private void resetBruteForceTable() {
		getBruteForceTable().setModel(EMPTY_RESULTS_MODEL);
	}

	protected HistoryReferencesTable getBruteForceTable() {
		if (bruteForceTable == null) {
		    bruteForceTable = new HistoryReferencesTable();
		    bruteForceTable.setName(MESSAGE_CONTAINER_NAME);

			resetBruteForceTable();
		}
		return bruteForceTable;
	}

	private JComboBox<ForcedBrowseFile> getFileSelect() {
		if (fileSelect == null) {
			fileSelect = new JComboBox<>();
			this.refreshFileList();
		}
		return fileSelect;
	}

	private JComboBox<ScanTarget> getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox<>(siteModel);
			siteSelect.addItem(noSelectionScanTarget);
			siteSelect.setSelectedIndex(0);

			siteSelect.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    ScanTarget item = (ScanTarget) siteSelect.getSelectedItem();
				    if (item != null && siteSelect.getSelectedIndex() > 0) {
				        siteSelected(item, false);
				    } else {
				        siteSelected(null, false);
				    }
				}
			});
		}
		return siteSelect;
	}
	
	public void addSite(URI site) {
		// OK, so this doesnt extend ScanPanel right now .. but it should
		ScanTarget scanTarget = new ScanTarget(site);
		
		if (!isScanTargetAdded(scanTarget)) {
			siteModel.addElement(scanTarget);
		}
	}

	private boolean isScanTargetAdded(ScanTarget scanTarget) {
		return (siteModel.getIndexOf(scanTarget) != -1);
	}
	
	private void siteSelected(ScanTarget scanTarget, boolean forceRefresh) {
		if (scanTarget == null) {
			currentSite = null;
			resetScanState();
			resetBruteForceTable();

			return;
		}

		if (Mode.safe.equals(this.mode)) {
			// Safe mode so ignore this
			return;
		}
		if (forceRefresh || ! scanTarget.equals(currentSite)) {
			if (!isScanTargetAdded(scanTarget)) {
				return;
			}

			siteModel.setSelectedItem(scanTarget);

			BruteForce bruteForce = bruteForceMap.get(scanTarget);
			if (bruteForce == null) {
				final ForcedBrowseFile selectedForcedBrowseFile = (ForcedBrowseFile) this.fileSelectModel.getSelectedItem();
				if (selectedForcedBrowseFile == null) {
					return;
				}
				File file = selectedForcedBrowseFile.getFile();
				if (! file.exists()) {
					log.error("No such file: " + file.getAbsolutePath());
					return;
				}
				
				bruteForce = new BruteForce(scanTarget, file, this, this.bruteForceParam);
				bruteForceMap.put(scanTarget, bruteForce);
			}
			if (bruteForce.isAlive()) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(true);
				getPauseScanButton().setEnabled(true);
				getPauseScanButton().setSelected(bruteForce.isPaused());
				getProgressBar().setEnabled(true);
			} else {
				resetScanButtonsAndProgressBarStates(true);
				getProgressBar().setValue(0);
			}
			
			getProgressBar().setValue(bruteForce.getWorkDone());
			getProgressBar().setMaximum(bruteForce.getWorkTotal());
			bruteForceTable.setModel(bruteForce.getModel());
			currentSite = (ScanTarget) siteModel.getSelectedItem();
		}
		if (Mode.protect.equals(this.mode)) {
			if (! Model.getSingleton().getSession().isInScope(this.getSiteNode(currentSite))) {
				resetScanButtonsAndProgressBarStates(false);
			}
		}
	}

	private void resetScanButtonsAndProgressBarStates(boolean allowStartScan) {
		getStartScanButton().setEnabled(allowStartScan);
		getStopScanButton().setEnabled(false);
		getPauseScanButton().setEnabled(false);
		getPauseScanButton().setSelected(false);
		getProgressBar().setEnabled(false);
	}

	protected ScanTarget createScanTarget(SiteNode node) {
		if (node != null) {
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}

			HistoryReference hRef = node.getHistoryReference();
			if (hRef != null) {
				try {
					return new ScanTarget(hRef.getURI());
				} catch (HttpMalformedHeaderException | SQLException e) {
					logger.warn("Failed to create scan target: " + e.getMessage(), e);
					return null;
				}
			}
		}
		return null;
	}
	
	protected SiteNode getSiteNode (ScanTarget scanTarget) {
		SiteMap siteTree = Model.getSingleton().getSession().getSiteTree();
		SiteNode rootNode = (SiteNode) siteTree.getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			ScanTarget snScanTarget = createScanTarget(sn);
			if (snScanTarget != null && snScanTarget.equals(scanTarget)) {
				return sn;
			}
		}
		return null;
	}


	public void nodeSelected(SiteNode node) {
		ScanTarget scanTarget = createScanTarget(node);

		if (!isScanTargetAdded(scanTarget)) {
			siteModel.addElement(scanTarget);
		}

		siteSelected(scanTarget, false);
	}
	

	protected void bruteForceSite(SiteNode node) {
		this.setTabFocus();
		nodeSelected(node);
		if (currentSite != null && this.getStartScanButton().isEnabled()) {
			startScan();
		}
	}

	protected void bruteForceDirectory(SiteNode node) {
		this.setTabFocus();
		nodeSelected(node);
		if (currentSite != null && this.getStartScanButton().isEnabled()) {
			try {
				String dir = node.getHistoryReference().getHttpMessage().getRequestHeader().getURI().getPath();
				startScan(dir, false);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

    protected void bruteForceDirectoryAndChildren(SiteNode node) {
        this.setTabFocus();
        nodeSelected(node);
        if (currentSite != null && this.getStartScanButton().isEnabled()) {
            try {
                String dir = node.getHistoryReference().getHttpMessage().getRequestHeader().getURI().getPath();
                startScan(dir, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	private void startScan() {
		this.startScan(null, false);
	}
	
	private void startScan(String directory, boolean onlyUnderDirectory) {
			
		this.getStartScanButton().setEnabled(false);
		this.getStopScanButton().setEnabled(true);
		this.getPauseScanButton().setEnabled(true);

		this.activeScans.add(currentSite);

		final ForcedBrowseFile selectedForcedBrowseFile = (ForcedBrowseFile) this.fileSelectModel.getSelectedItem();
		if (selectedForcedBrowseFile == null) {
			return;
		}
		File file = selectedForcedBrowseFile.getFile();
		if (! file.exists()) {
			log.error("No such file: " + file.getAbsolutePath());
			return;
		}
		
		BruteForce bruteForce = new BruteForce(currentSite, file, this, bruteForceParam, directory);
		if (onlyUnderDirectory) {
			bruteForce.setOnlyUnderDirectory(onlyUnderDirectory);
		}
		bruteForceMap.put(currentSite, bruteForce);
		
		bruteForce.start();
		setActiveScanLabels();
		getProgressBar().setEnabled(true);
		getProgressBar().setMaximum(bruteForce.getWorkTotal());
		bruteForceTable.setModel(bruteForce.getModel());

		currentSite.setScanned(true);
		siteModel.elementChanged(currentSite);
	}
	
	private void stopScan() {
		log.debug("Stopping scan on " + currentSite);
		BruteForce bruteForce = bruteForceMap.get(currentSite);
		if (bruteForce != null) {
			bruteForce.stopScan();
		}
	}

	private void pauseScan() {
		log.debug("Pausing scan on " + currentSite);
		BruteForce bruteForce = bruteForceMap.get(currentSite);
		if (bruteForce != null) {
			if (bruteForce.isPaused()) {
				bruteForce.unpauseScan();
			} else {
				bruteForce.pauseScan();
			}
		}
	}

	@Override
	public void scanFinshed(ScanTarget scanTarget) {
		if (scanTarget.equals(currentSite)) {
			resetScanButtonsAndProgressBarStates(true);
		}
		this.activeScans.remove(scanTarget);
		setActiveScanLabels();
	}

	@Override
	public void scanProgress(ScanTarget scanTarget, int done, int todo) {
		if (scanTarget.equals(currentSite)) {
			getProgressBar().setValue(done);
			getProgressBar().setMaximum(todo);
		}
	}

	public void reset() {
		stopAllScans();

		siteModel.removeAllElements();
		siteSelect.addItem(noSelectionScanTarget);
		siteSelect.setSelectedIndex(0);
	}

	private void stopAllScans() {
		for (BruteForce scanner : bruteForceMap.values()) {
			scanner.stopScan();
			scanner.clearModel();
		}
		// Allow 2 secs for the threads to stop - if we wait 'for ever' then we can get deadlocks
		for (int i = 0; i < 20; i++) {
			if (activeScans.size() == 0) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		bruteForceMap.clear();
		activeScans.clear();
		
		setActiveScanLabels();
		resetScanState();
	}

	private void resetScanState() {
		resetScanButtonsAndProgressBarStates(false);
		getProgressBar().setValue(0);
		
	}

	@Override
	public void foundDir(URL url, int statusCode, String responce,
			String baseCase, String rawResponce, BaseCase baseCaseObj) {
	}

	public boolean isScanning(SiteNode node) {
		ScanTarget target = createScanTarget(node);
		if (target != null) {
			BruteForce bf = bruteForceMap.get(target);
			if (bf != null) {
				return bf.isAlive();
			}
		}
		return false;
	}

	public void refreshFileList() {
		fileList = null;
		
		fileSelectModel = new DefaultComboBoxModel<>();
		for (ForcedBrowseFile file : getFileList()) {
			fileSelectModel.addElement(file);
		}
		
		fileSelect.setModel(fileSelectModel);

		ForcedBrowseFile defaultFile = this.bruteForceParam.getDefaultFile();
		
		if (defaultFile != null) {
			fileSelectModel.setSelectedItem(defaultFile);
		}
	}

	public List<ForcedBrowseFile> getFileList() {
		if (fileList == null) {
			fileList = new ArrayList<>();
			File dir = new File(fileDirectory);
			FilenameFilter filter = new FilenameExtensionFilter(fileExtension, true);
			File[] files = dir.listFiles(filter);
			if (files != null) {
				Arrays.sort(files);
				for (File file : files) {
					fileList.add(new ForcedBrowseFile(file));
				}
			}
			
			// handle local/custom files
			File customDir = new File(customFileDirectory);
			if ( ! dir.equals(customDir)) {
				File[] customFiles = customDir.listFiles();
				if (customFiles != null) {
					Arrays.sort(customFiles);
					for (File file : customFiles) {
						if (! file.isDirectory()) {
							fileList.add(new ForcedBrowseFile(file));
						}
					}
				}
			}
			Collections.sort(fileList);
		}
		
		return fileList;
	}

	public void setDefaultFile(ForcedBrowseFile file) {
		this.fileSelectModel.setSelectedItem(file);
	}

	public void sessionScopeChanged(Session session) {
		if (currentSite != null) {
			this.siteSelected(currentSite, true);
		}
	}

	public void sessionModeChanged(Mode mode) {
		this.mode = mode;
		switch (mode) {
		case standard:
		case protect:
			getSiteSelect().setEnabled(true);
			if (currentSite != null) {
				this.siteSelected(currentSite, true);
			}
			break;
		case safe:
			// Stop all scans, disable everything
			stopAllScans();
			getSiteSelect().setEnabled(false);
		}
	}
}
