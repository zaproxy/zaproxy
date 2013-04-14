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
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

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
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.utils.FilenameExtensionFilter;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.ScanStatus;

import com.sittinglittleduck.DirBuster.BaseCase;

public class BruteForcePanel extends AbstractPanel implements BruteForceListenner {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(BruteForcePanel.class);

	public static final String PANEL_NAME = "bruteforce";
	
	//private ExtensionBruteForce extension = null;
	private BruteForceParam bruteForceParam = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JLabel activeScansNameLabel = null;
	private JLabel activeScansValueLabel = null;
	private List<String> activeScans = new ArrayList<>();
    private BruteForcePanelCellRenderer bfPanelCellRenderer = null;
    private List<ForcedBrowseFile> fileList = null;
	private JComboBox<ForcedBrowseFile> fileSelect = null;
	private DefaultComboBoxModel<ForcedBrowseFile> fileSelectModel = null;

	private String fileDirectory = Constant.getInstance().DIRBUSTER_DIR;
	private String customFileDirectory = Constant.getInstance().DIRBUSTER_CUSTOM_DIR;
	private String fileExtension = ".txt";

	private String currentSite = null;
	private JComboBox<String> siteSelect = null;
	// The siteModel entries are all HTML, with the active ones in bold
	private SortedComboBoxModel<String> siteModel = new SortedComboBoxModel<>();

	private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private JToggleButton pauseScanButton = null;
	private JButton optionsButton = null;
	//private JButton launchButton = null;
	private JList<BruteForceItem> bruteForceList = null;
	private JProgressBar progressBar = null;
	private Map <String, BruteForce> bruteForceMap = new HashMap <>();

	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;

	private ScanStatus scanStatus = null;
	private Mode mode = Control.getSingleton().getMode();

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
		Iterator <String> iter = activeScans.iterator();
		sb.append("<html>");
		while (iter.hasNext()) {
			sb.append(iter.next());
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
			pauseScanButton = new JToggleButton();
			pauseScanButton.setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
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
			jScrollPane.setViewportView(getBruteForceList());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void resetBruteForceList() {
		getBruteForceList().setModel(new DefaultListModel<BruteForceItem>());
	}

	protected JList<BruteForceItem> getBruteForceList() {
		if (bruteForceList == null) {
			bruteForceList = new JList<>();
			bruteForceList.setDoubleBuffered(true);
			bruteForceList.setCellRenderer(getBruteForcePanelCellRenderer());
			bruteForceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			bruteForceList.setName(PANEL_NAME);
			bruteForceList.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			
			bruteForceList.setFixedCellHeight(16);	// Significantly speeds up rendering

			bruteForceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() { 

				@Override
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				    if (bruteForceList.getSelectedValue() == null) {
				        return;
				    }
                    
				    displayMessage(bruteForceList.getSelectedValue());
				}
			});
			
			bruteForceList.addMouseListener(new java.awt.event.MouseAdapter() { 
			    @Override
			    public void mousePressed(java.awt.event.MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {

						// Select list item
					    int Idx = bruteForceList.locationToIndex( e.getPoint() );
					    if ( Idx >= 0 ) {
					    	Rectangle Rect = bruteForceList.getCellBounds( Idx, Idx );
					    	Idx = Rect.contains( e.getPoint().x, e.getPoint().y ) ? Idx : -1;
					    }
					    if ( Idx < 0 || !bruteForceList.getSelectionModel().isSelectedIndex( Idx ) ) {
					    	bruteForceList.getSelectionModel().clearSelection();
					    	if ( Idx >= 0 ) {
					    		bruteForceList.getSelectionModel().setSelectionInterval( Idx, Idx );
					    	}
					    }
						
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }			    	
			    }
			});

			
			resetBruteForceList();
		}
		return bruteForceList;
	}

    private void displayMessage(BruteForceItem sr) {
        HttpMessage msg;
		try {
			msg = new HistoryReference(sr.getHistoryId()).getHttpMessage();
	        if (msg.getRequestHeader().isEmpty()) {
	            requestPanel.clearView(true);
	        } else {
	            requestPanel.setMessage(msg);
	        }
	        
	        if (msg.getResponseHeader().isEmpty()) {
	            responsePanel.clearView(false);
	        } else {
	            responsePanel.setMessage(msg, true);
	        }
		} catch (Exception e) {
			log.error("Failed to access message id " + sr.getHistoryId(), e);
		}
    }

	private ListCellRenderer<BruteForceItem> getBruteForcePanelCellRenderer() {
        if (bfPanelCellRenderer == null) {
            bfPanelCellRenderer = new BruteForcePanelCellRenderer();
            bfPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            bfPanelCellRenderer.setBackground(java.awt.Color.white);
            bfPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return bfPanelCellRenderer;
	}

	private JComboBox<ForcedBrowseFile> getFileSelect() {
		if (fileSelect == null) {
			fileSelect = new JComboBox<>();
			this.refreshFileList();
		}
		return fileSelect;
	}

	private JComboBox<String> getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox<>(siteModel);
			siteSelect.addItem(Constant.messages.getString("bruteforce.toolbar.site.select"));
			siteSelect.setSelectedIndex(0);

			siteSelect.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    String item = (String) siteSelect.getSelectedItem();
				    if (item != null && siteSelect.getSelectedIndex() > 0) {
				        siteSelected(item, false);
				    }
				}
			});
		}
		return siteSelect;
	}
	
	private String activeSitelabel(String site) {
		return "<html><b>" + site + "</b></html>";
	}
	
	private String passiveSitelabel(String site) {
		return "<html>" + site + "</html>";
	}
	
	private String getSiteFromLabel(String siteLabel) {
		if (siteLabel.startsWith("<html><b>")) {
			return siteLabel.substring(9, siteLabel.indexOf("</b>"));
		} else if (siteLabel.startsWith("<html>")) {
			return siteLabel.substring(6, siteLabel.indexOf("</html>"));
		} else {
			return siteLabel;
		}
	}
	
	public void addSite(String site) {
		// OK, so this doesnt extend ScanPanel right now .. but it should
		site = ScanPanel.cleanSiteName(site, true);
		
		if (siteModel.getIndexOf(activeSitelabel(site)) < 0 &&
				siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
			siteModel.addElement(passiveSitelabel(site));
		}
	}
	
	private void siteSelected(String site, boolean forceRefresh) {
		if (Mode.safe.equals(this.mode)) {
			// Safe mode so ignore this
			return;
		}
		site = getSiteFromLabel(site);
		if (forceRefresh || ! site.equals(currentSite)) {
			if (siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
				siteModel.setSelectedItem(activeSitelabel(site));
			} else {
				siteModel.setSelectedItem(passiveSitelabel(site));
			}

			BruteForce bruteForce = bruteForceMap.get(site);
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
				
				bruteForce = new BruteForce(site, file, this, this.bruteForceParam);
				bruteForceMap.put(site, bruteForce);
			}
			if (bruteForce.isAlive()) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(true);
				getPauseScanButton().setEnabled(true);
				if (bruteForce.isPaused()) {
					getPauseScanButton().setSelected(true);
					getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.unpause"));
				} else {
					getPauseScanButton().setSelected(false);
					getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
				}
				getProgressBar().setEnabled(true);
			} else {
				getStartScanButton().setEnabled(true);
				getStopScanButton().setEnabled(false);
				getPauseScanButton().setEnabled(false);
				getProgressBar().setEnabled(false);
				getProgressBar().setValue(0);
			}
			
			getProgressBar().setValue(bruteForce.getWorkDone());
			getProgressBar().setMaximum(bruteForce.getWorkTotal());
			bruteForceList.setModel(bruteForce.getList());
			currentSite = site;
		}
		if (Mode.protect.equals(this.mode)) {
			if (! Model.getSingleton().getSession().isInScope(this.getSiteNode(currentSite))) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(false);
				getPauseScanButton().setEnabled(false);
				getProgressBar().setEnabled(false);
			}
		}
	}

	protected String getSiteName(SiteNode node) {
		if (node != null) {
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}
			return ScanPanel.cleanSiteName(node.getNodeName(), true);
		}
		return null;
	}
	
	protected SiteNode getSiteNode (String siteName) {
		SiteMap siteTree = Model.getSingleton().getSession().getSiteTree();
		SiteNode rootNode = (SiteNode) siteTree.getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = rootNode.children();
		while (en.hasMoreElements()) {
			SiteNode sn = en.nextElement();
			String nodeName = sn.getNodeName();
			if (nodeName.toLowerCase().startsWith("https:")) {
				nodeName += ":443";
			}
			if (nodeName.toLowerCase().startsWith("http:") && 
                                nodeName.toLowerCase().lastIndexOf(":")==nodeName.toLowerCase().indexOf(":")) { // Does not contain port number (second column)
				nodeName += ":80";
			}                        
			if (nodeName.indexOf("//") >= 0) {
				nodeName = nodeName.substring(nodeName.indexOf("//") + 2);
			}
			if (siteName.equals(nodeName)) {
				return sn;
			}
		}
		return null;
	}


	public void nodeSelected(SiteNode node) {
		siteSelected(getSiteName(node), false);
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
		bruteForceList.setModel(bruteForce.getList());

		String selectedSite = currentSite;	// currentSite can change when we remove elements
		if (siteModel.getIndexOf(passiveSitelabel(selectedSite)) >= 0) {
			// Change the site label to be bold
			siteModel.removeElement(passiveSitelabel(selectedSite));
			siteModel.addElement(activeSitelabel(selectedSite));
			siteModel.setSelectedItem(activeSitelabel(selectedSite));
		}
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
				getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
			} else {
				bruteForce.pauseScan();
				getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.unpause"));
			}
		}
	}

	@Override
	public void scanFinshed(String host) {
		if (host.equals(currentSite)) {
			getStartScanButton().setEnabled(true);
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
			getProgressBar().setEnabled(false);
		}
		this.activeScans.remove(host);
		setActiveScanLabels();
	}

	@Override
	public void scanProgress(String host, int port, int done, int todo) {
		if (currentSite != null && (currentSite.equals(host) || currentSite.equals(host + ":" + port))) {
			getProgressBar().setValue(done);
			getProgressBar().setMaximum(todo);
		}
	}

	public void reset() {
		// Stop all scans
		Set<Entry<String, BruteForce>> set = bruteForceMap.entrySet();
		Iterator<Entry<String, BruteForce>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, BruteForce> entry = iter.next();
			entry.getValue().stopScan();
			entry.getValue().clearList();
		}
		// Wait until all threads have stopped
		while (activeScans.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		bruteForceMap.clear();
		
		siteModel.removeAllElements();
		siteSelect.addItem(Constant.messages.getString("bruteforce.toolbar.site.select"));
		siteSelect.setSelectedIndex(0);
		currentSite = null;
		resetBruteForceList();
		setActiveScanLabels();
		getStartScanButton().setEnabled(false);
		getStopScanButton().setEnabled(false);
		getProgressBar().setEnabled(false);
		getProgressBar().setValue(0);
		
	}

	@Override
	public void foundDir(URL url, int statusCode, String responce,
			String baseCase, String rawResponce, BaseCase baseCaseObj) {
	}

    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;

    }

	public boolean isScanning(SiteNode node) {
		String site = getSiteFromLabel(this.getSiteName(node));
		if (site != null) {
			BruteForce bf = bruteForceMap.get(site);
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
			reset();
			getStartScanButton().setEnabled(false);
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getProgressBar().setEnabled(false);
			getSiteSelect().setSelectedIndex(0);
			getSiteSelect().setEnabled(false);
		}
	}
}
