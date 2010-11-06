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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.utils.FilenameExtensionFilter;
import org.zaproxy.zap.utils.SortedComboBoxModel;

import com.sittinglittleduck.DirBuster.BaseCase;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BruteForcePanel extends AbstractPanel implements BruteForceListenner {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "bruteforce";
	
	//private ExtensionBruteForce extension = null;
	private BruteForceParam portScanParam = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JLabel activeScansNameLabel = null;
	private JLabel activeScansValueLabel = null;
	private List<String> activeScans = new ArrayList<String>();;
    private BruteForcePanelCellRenderer portPanelCellRenderer = null;
	private JComboBox fileSelect = null;

	private String fileDirectory = Constant.getInstance().DIRBUSTER_DIR;
	private String fileExtension = ".txt";

	private String currentSite = null;
	private JComboBox siteSelect = null;
	// The siteModel entries are all HTML, with the active ones in bold
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();

	private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private JToggleButton pauseScanButton = null;
	private JList bruteForceList = null;
	private JProgressBar progressBar = null;
	private Map <String, BruteForce> bruteForceMap = new HashMap <String, BruteForce>();

	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;

    private static Log log = LogFactory.getLog(BruteForcePanel.class);
    
    /**
     * @param portScanParam 
     * 
     */
    public BruteForcePanel(ExtensionBruteForce extension, BruteForceParam portScanParam) {
        super();
        //this.extension = extension;
        this.portScanParam = portScanParam;
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
        this.setName(Constant.messages.getString("bruteforce.panel.title"));
		//TODO: Find a hammer icon :)
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/086.png")));	// 'spanner' icon
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
			panelToolbar.setName("PortToolbar");
			
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
			GridBagConstraints gridBagConstraintsx = new GridBagConstraints();

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
			gridBagConstraints8.insets = new java.awt.Insets(0,3,0,0);	// Slight indent
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints9.gridx = 8;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints10.gridx = 9;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsx.gridx = 10;
			gridBagConstraintsx.gridy = 0;
			gridBagConstraintsx.weightx = 1.0;
			gridBagConstraintsx.weighty = 1.0;
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraintsx.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel t1 = new JLabel();

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
			
			panelToolbar.add(t1, gridBagConstraintsx);
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
			activeScansValueLabel.setText(""+activeScans.size());
		}
		return activeScansValueLabel;
	}
	
	private void setActiveScanLabels() {
		getActiveScansValueLabel().setText(""+activeScans.size());
		StringBuffer sb = new StringBuffer();
		Iterator <String> iter = activeScans.iterator();
		sb.append("<html>");
		while (iter.hasNext()) {
			sb.append(iter.next());
			sb.append("<br>");
		}
		sb.append("</html>");
		getActiveScansNameLabel().setToolTipText(sb.toString());
		getActiveScansValueLabel().setToolTipText(sb.toString());
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
			startScanButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/131.png")));
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
			stopScanButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/142.png")));
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
			pauseScanButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/141.png")));
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

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getBruteForceList());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void resetPortList() {
		getBruteForceList().setModel(new DefaultListModel());
	}

	private JList getBruteForceList() {
		if (bruteForceList == null) {
			bruteForceList = new JList();
			bruteForceList.setDoubleBuffered(true);
			bruteForceList.setCellRenderer(getPortPanelCellRenderer());
			bruteForceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			bruteForceList.setName(PANEL_NAME);
			bruteForceList.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			
			bruteForceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() { 

				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				    if (bruteForceList.getSelectedValue() == null) {
				        return;
				    }
                    
				    displayMessage((BruteForceItem) bruteForceList.getSelectedValue());
				}
			});
			
			resetPortList();
		}
		return bruteForceList;
	}

    private void displayMessage(BruteForceItem sr) {
        HttpMessage msg;
		try {
			msg = new HistoryReference(sr.getHistoryId()).getHttpMessage();
	        if (msg.getRequestHeader().isEmpty()) {
	            requestPanel.setMessage(null, true);
	        } else {
	            requestPanel.setMessage(msg, true);
	        }
	        
	        if (msg.getResponseHeader().isEmpty()) {
	            responsePanel.setMessage(null, false);
	        } else {
	            responsePanel.setMessage(msg, false);
	        }
		} catch (Exception e) {
			log.error("Failed to access message id " + sr.getHistoryId(), e);
		}
    }

	private ListCellRenderer getPortPanelCellRenderer() {
        if (portPanelCellRenderer == null) {
            portPanelCellRenderer = new BruteForcePanelCellRenderer();
            portPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            portPanelCellRenderer.setBackground(java.awt.Color.white);
            portPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return portPanelCellRenderer;
	}

	private JComboBox getFileSelect() {
		if (fileSelect == null) {
			fileSelect = new JComboBox();
			File dir = new File(fileDirectory);
			FilenameFilter filter = new FilenameExtensionFilter(fileExtension, true);
			String[] files = dir.list(filter );
			Arrays.sort(files);
			for (String file : files) {
				fileSelect.addItem(file);
			}
			fileSelect.setSelectedIndex(0);
		}
		return fileSelect;
	}

	private JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString("bruteforce.toolbar.site.select"));
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
		if (siteModel.getIndexOf(activeSitelabel(site)) < 0 &&
				siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
			siteModel.addElement(passiveSitelabel(site));
		}
	}
	
	private void siteSelected(String site) {
		site = getSiteFromLabel(site);
		if (! site.equals(currentSite)) {
			if (siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
				siteModel.setSelectedItem(activeSitelabel(site));
			} else {
				siteModel.setSelectedItem(passiveSitelabel(site));
			}

			BruteForce bruteForce = bruteForceMap.get(site);
			if (bruteForce == null) {
				String fileName = this.fileDirectory + "/" + this.fileSelect.getSelectedItem();
				File f = new File(fileName);
				if (! f.exists()) {
					log.error("No such file: " + f.getAbsolutePath());
				} else {
					bruteForce = new BruteForce(site, fileName, this, this.portScanParam);
					bruteForceMap.put(site, bruteForce);
				}
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
	}

	public void nodeSelected(SiteNode node) {
		if (node != null) {
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}
			String site = node.toString();
			if (site.indexOf("//") >= 0) {
				site = site.substring(site.indexOf("//") + 2);
			}
			if (site.indexOf(":") >= 0) {
				site = site.substring(0, site.indexOf(":"));
			}
			siteSelected(site);
			
		}
	}

	private void startScan() {
			
		this.getStartScanButton().setEnabled(false);
		this.getStopScanButton().setEnabled(true);
		this.getPauseScanButton().setEnabled(true);

		this.activeScans.add(currentSite);

		BruteForce portScan = bruteForceMap.get(currentSite);
		if (portScan.isStopped()) {
			// Start a new thread
			String fileName = this.fileDirectory + "/" + this.fileSelect.getSelectedItem();
			File f = new File(fileName);
			if (! f.exists()) {
				log.error("No such file: " + f.getAbsolutePath());
			} else {
				portScan = new BruteForce(currentSite, f.getAbsolutePath(), this, portScanParam);
				bruteForceMap.put(currentSite, portScan);
			}
		}
		portScan.start();
		bruteForceMap.put(currentSite, portScan);
		setActiveScanLabels();
		getProgressBar().setEnabled(true);
		getProgressBar().setMaximum(portScan.getWorkTotal());
		bruteForceList.setModel(portScan.getList());

		if (siteModel.getIndexOf(passiveSitelabel(currentSite)) >= 0) {
			// Change the site label to be bold
			siteModel.removeElement(passiveSitelabel(currentSite));
			siteModel.addElement(activeSitelabel(currentSite));
			siteModel.setSelectedItem(activeSitelabel(currentSite));
		}
	}
	
	private void stopScan() {
		log.debug("Stopping scan on " + currentSite);
		BruteForce portScan = bruteForceMap.get(currentSite);
		if (portScan != null) {
			portScan.stopScan();
		}
	}

	private void pauseScan() {
		log.debug("Pausing scan on " + currentSite);
		BruteForce portScan = bruteForceMap.get(currentSite);
		if (portScan != null) {
			if (portScan.isPaused()) {
				portScan.unpauseScan();
				getPauseScanButton().setToolTipText(Constant.messages.getString("bruteforce.toolbar.button.pause"));
			} else {
				portScan.pauseScan();
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
		// Note the label is not now changed back from bold - so you can see which sites have been scanned
		/*
		// Change the label back from bold
		siteModel.removeElement(activeSitelabel(host));
		siteModel.addElement(passiveSitelabel(host));
		if (host.equals(currentSite)) {
			// Its changed under our feet :)
			siteModel.setSelectedItem(passiveSitelabel(currentSite));
		}
		*/
		setActiveScanLabels();
	}

	@Override
	public void scanProgress(String host, int done, int todo) {
		if (host.equals(currentSite)) {
			getProgressBar().setValue(done);
			getProgressBar().setMaximum(done + todo);
		}
		
	}

	public void reset() {
		// Stop all scans
		Set<Entry<String, BruteForce>> set = bruteForceMap.entrySet();
		Iterator<Entry<String, BruteForce>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, BruteForce> entry = iter.next();
			entry.getValue().stopScan();
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
		resetPortList();
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
    
}
