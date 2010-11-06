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
package org.zaproxy.zap.extension.portscan;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.SortedComboBoxModel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PortScanPanel extends AbstractPanel implements PortScanListenner {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "portscan";
	
	private ExtensionPortScan extension = null;
	private PortScanParam portScanParam = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JLabel activeScansNameLabel = null;
	private JLabel activeScansValueLabel = null;
	private List<String> activeScans = new ArrayList<String>();;
    private PortPanelCellRenderer portPanelCellRenderer = null;

	private String currentSite = null;
	private JComboBox siteSelect = null;
	// The siteModel entries are all HTML, with the active ones in bold
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();

	private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private JList portList = null;
	private JProgressBar progressBar = null;
	private Map <String, PortScan> portScanMap = new HashMap <String, PortScan>();
	
    private static Log log = LogFactory.getLog(PortScanPanel.class);
    
    /**
     * @param portScanParam 
     * 
     */
    public PortScanPanel(ExtensionPortScan extension, PortScanParam portScanParam) {
        super();
        this.extension = extension;
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
        this.setName(Constant.messages.getString("ports.panel.title"));
		//this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/188.png")));	// 'bullet list' icon
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/187.png")));	// 'picture list' icon
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
			panelCommand.setName("PortScan");
			
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
			gridBagConstraints6.insets = new java.awt.Insets(0,3,0,0);	// Slight indent
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints7.gridx = 6;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsx.gridx = 7;
			gridBagConstraintsx.gridy = 0;
			gridBagConstraintsx.weightx = 1.0;
			gridBagConstraintsx.weighty = 1.0;
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraintsx.fill = java.awt.GridBagConstraints.HORIZONTAL;

			JLabel t1 = new JLabel();

			panelToolbar.add(new JLabel(Constant.messages.getString("ports.toolbar.site.label")), gridBagConstraints1);
			panelToolbar.add(getSiteSelect(), gridBagConstraints2);
			
			panelToolbar.add(getStartScanButton(), gridBagConstraints3);
			panelToolbar.add(getStopScanButton(), gridBagConstraints4);
			panelToolbar.add(getProgressBar(), gridBagConstraints5);
			panelToolbar.add(getActiveScansNameLabel(), gridBagConstraints6);
			panelToolbar.add(getActiveScansValueLabel(), gridBagConstraints7);
			
			panelToolbar.add(t1, gridBagConstraintsx);
		}
		return panelToolbar;
	}

	private JLabel getActiveScansNameLabel() {
		if (activeScansNameLabel == null) {
			activeScansNameLabel = new javax.swing.JLabel();
			activeScansNameLabel.setText(Constant.messages.getString("ports.toolbar.ascans.label"));
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
			progressBar = new JProgressBar(0, this.extension.getMaxPort());
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setEnabled(false);
		}
		return progressBar;
	}
	
	private JButton getStartScanButton() {
		if (startScanButton == null) {
			startScanButton = new JButton();
			startScanButton.setToolTipText(Constant.messages.getString("ports.toolbar.button.start"));
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
			stopScanButton.setToolTipText(Constant.messages.getString("ports.toolbar.button.stop"));
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

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getPortList());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void resetPortList() {
		getPortList().setModel(new DefaultListModel());
	}

	private JList getPortList() {
		if (portList == null) {
			portList = new JList();
			portList.setDoubleBuffered(true);
			portList.setCellRenderer(getPortPanelCellRenderer());
			portList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			portList.setName(PANEL_NAME);
			portList.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			
			resetPortList();
		}
		return portList;
	}

	private ListCellRenderer getPortPanelCellRenderer() {
        if (portPanelCellRenderer == null) {
            portPanelCellRenderer = new PortPanelCellRenderer();
            portPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            portPanelCellRenderer.setBackground(java.awt.Color.white);
            portPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return portPanelCellRenderer;
	}

	private JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString("ports.toolbar.site.select"));
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

			PortScan portScan = portScanMap.get(site);
			if (portScan == null) {
				portScan = new PortScan(site, this, this.portScanParam);
				portScanMap.put(site, portScan);
				
			}
			if (portScan.isAlive()) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(true);
				getProgressBar().setEnabled(true);
			} else {
				getStartScanButton().setEnabled(true);
				getStopScanButton().setEnabled(false);
				getProgressBar().setEnabled(false);
				getProgressBar().setValue(0);
			}
			
			getProgressBar().setValue(portScan.getProgress());
			getProgressBar().setMaximum(portScan.getMaxPort());
			portList.setModel(portScan.getList());
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
		this.activeScans.add(currentSite);

		PortScan portScan = portScanMap.get(currentSite);
		if (portScan.isStopped()) {
			// Start a new thread
			portScan = new PortScan(currentSite, this, portScanParam);
			portScanMap.put(currentSite, portScan);
		}
		portScan.start();
		portScanMap.put(currentSite, portScan);
		setActiveScanLabels();
		getProgressBar().setEnabled(true);
		getProgressBar().setMaximum(portScan.getMaxPort());
		portList.setModel(portScan.getList());

		if (siteModel.getIndexOf(passiveSitelabel(currentSite)) >= 0) {
			// Change the site label to be bold
			siteModel.removeElement(passiveSitelabel(currentSite));
			siteModel.addElement(activeSitelabel(currentSite));
			siteModel.setSelectedItem(activeSitelabel(currentSite));
		}
	}
	
	private void stopScan() {
		log.debug("Stopping scan on " + currentSite);
		PortScan portScan = portScanMap.get(currentSite);
		if (portScan != null) {
			portScan.stopScan();
		}
	}

	@Override
	public void scanFinshed(String host) {
		if (host.equals(currentSite)) {
			getStartScanButton().setEnabled(true);
			getStopScanButton().setEnabled(false);
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
	public void scanProgress(String host, int progress) {
		if (host.equals(currentSite)) {
			getProgressBar().setValue(progress);
		}
		
	}

	public void reset() {
		// Stop all scans
		Set<Entry<String, PortScan>> set = portScanMap.entrySet();
		Iterator<Entry<String, PortScan>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, PortScan> entry = iter.next();
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
		portScanMap.clear();
		
		siteModel.removeAllElements();
		siteSelect.addItem(Constant.messages.getString("ports.toolbar.site.select"));
		siteSelect.setSelectedIndex(0);
		currentSite = null;
		resetPortList();
		setActiveScanLabels();
		getStartScanButton().setEnabled(false);
		getStopScanButton().setEnabled(false);
		getProgressBar().setEnabled(false);
		getProgressBar().setValue(0);
	}
}
