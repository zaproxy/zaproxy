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
package org.zaproxy.zap.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map.Entry;
import java.util.*;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.utils.SortedComboBoxModel;

public abstract class ScanPanel extends AbstractPanel {
	private static final long serialVersionUID = 1L;

	public String prefix;
	
	private ExtensionAdaptor extension = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JLabel activeScansNameLabel = null;
	private JLabel activeScansValueLabel = null;
	private List<String> activeScans = new ArrayList<String>();;

	private String currentSite = null;
	private JComboBox siteSelect = null;
	// The siteModel entries are all HTML, with the active ones in bold
	private SortedComboBoxModel siteModel = new SortedComboBoxModel();

	private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private JToggleButton pauseScanButton = null;
	private JProgressBar progressBar = null;
	private Map <String, GenericScanner> scanMap = new HashMap <String, GenericScanner>();
	private AbstractParam scanParam = null;
	private ScanStatus scanStatus = null;
	
	private static Logger log = Logger.getLogger(ScanPanel.class);
    
    /**
     * @param ScanParam 
     * 
     */
    public ScanPanel(String prefix, ImageIcon icon, ExtensionAdaptor extension, AbstractParam scanParam) {
        super();
        this.prefix = prefix;
        this.extension = extension;
        this.scanParam = scanParam;
 		initialize(icon);
 		log.debug("Constructor " + prefix);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize(ImageIcon icon) {
        this.setLayout(new CardLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(474, 251);
        }
        this.setName(Constant.messages.getString(prefix + ".panel.title"));
		this.setIcon(icon);
        this.add(getPanelCommand(), prefix + ".panel");
        scanStatus = new ScanStatus(icon, Constant.messages.getString(prefix + ".panel.title"));
        
        if (View.isInitialised()) {
        	View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(scanStatus.getCountLabel());
        }
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
			panelCommand.setName(prefix + ".panel");
			
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
			panelCommand.add(getWorkPanel(), gridBagConstraints2);
			
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
			panelToolbar.setName(prefix + ".toolbar");
			
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
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
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.insets = new java.awt.Insets(0,5,0,5);	// Slight indent
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;

			gridBagConstraints7.gridx = 6;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraints8.gridx = 7;
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsx.gridx = 8;
			gridBagConstraintsx.gridy = 0;
			gridBagConstraintsx.weightx = 1.0;
			gridBagConstraintsx.weighty = 1.0;
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			

			JLabel t1 = new JLabel();

			panelToolbar.add(new JLabel(Constant.messages.getString(prefix + ".toolbar.site.label")), gridBagConstraints1);
			panelToolbar.add(getSiteSelect(), gridBagConstraints2);
			
			panelToolbar.add(getStartScanButton(), gridBagConstraints3);
			panelToolbar.add(getPauseScanButton(), gridBagConstraints4);
			panelToolbar.add(getStopScanButton(), gridBagConstraints5);
			panelToolbar.add(getProgressBar(), gridBagConstraints6);
			panelToolbar.add(getActiveScansNameLabel(), gridBagConstraints7);
			panelToolbar.add(getActiveScansValueLabel(), gridBagConstraints8);
			
			// TODO allow implementing classes to add extra elements
			
			panelToolbar.add(t1, gridBagConstraintsx);
		}
		return panelToolbar;
	}

	private JLabel getActiveScansNameLabel() {
		if (activeScansNameLabel == null) {
			activeScansNameLabel = new javax.swing.JLabel();
			activeScansNameLabel.setText(Constant.messages.getString(prefix + ".toolbar.ascans.label"));
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
	    if (EventQueue.isDispatchThread()) {
	    	setActiveScanLabelsEventHandler();
	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	        	    	setActiveScanLabelsEventHandler();
	                }
	            });
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);
	        }
	    }
	}
	
	private void setActiveScanLabelsEventHandler() {
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

		scanStatus.setScanCount(activeScans.size());
	}
	
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setSize(new Dimension(80,20));
			progressBar.setStringPainted(true);
			progressBar.setEnabled(false);
		}
		return progressBar;
	}
	
	private JButton getStartScanButton() {
		if (startScanButton == null) {
			startScanButton = new JButton();
			startScanButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.start"));
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
			stopScanButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.stop"));
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
			pauseScanButton.setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.pause"));
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

	protected JComboBox getSiteSelect() {
		if (siteSelect == null) {
			siteSelect = new JComboBox(siteModel);
			siteSelect.addItem(Constant.messages.getString(prefix + ".toolbar.site.select"));
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
	
	public boolean isScanning(SiteNode node, boolean incPort) {
		String site = getSiteFromLabel(cleanSiteName(node, incPort));
		if (site != null) {
			GenericScanner scanThread = scanMap.get(site);
			if (scanThread != null) {
				return scanThread.isAlive();
			}
		}
		return false;
	}

	
	public void scanSite(SiteNode node, boolean incPort) {
 		log.debug("scanSite " + prefix + " node=" + node.getNodeName());
		this.setTabFocus();
		nodeSelected(node, incPort);
		if (currentSite != null && this.getStartScanButton().isEnabled()) {
			startScan(node);
		}
	}
	
	private String activeSitelabel(String site) {
		return "<html><b>" + site + "</b></html>";
	}
	
	private String passiveSitelabel(String site) {
		return "<html>" + site + "</html>";
	}
	
	private String getSiteFromLabel(String siteLabel) {
		if (siteLabel == null) {
			return null;
		}
		if (siteLabel.startsWith("<html><b>")) {
			return siteLabel.substring(9, siteLabel.indexOf("</b>"));
		} else if (siteLabel.startsWith("<html>")) {
			return siteLabel.substring(6, siteLabel.indexOf("</html>"));
		} else {
			return siteLabel;
		}
	}
	
	public void addSite(String site, boolean incPort) {
		site = cleanSiteName(site, incPort);
		
		if (siteModel.getIndexOf(activeSitelabel(site)) < 0 &&
				siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
	 		log.debug("addSite " + site);
			siteModel.addElement(passiveSitelabel(site));
		}
	}
	
	protected void siteSelected(String site) {
		site = getSiteFromLabel(site);
		if (! site.equals(currentSite)) {
			if (siteModel.getIndexOf(passiveSitelabel(site)) < 0) {
				siteModel.setSelectedItem(activeSitelabel(site));
			} else {
				siteModel.setSelectedItem(passiveSitelabel(site));
			}

			GenericScanner scanThread = scanMap.get(site);
			if (scanThread == null) {
				scanThread = this.newScanThread(site, this.scanParam);
				scanMap.put(site, scanThread);
				
			}
			if (scanThread.isAlive()) {
				getStartScanButton().setEnabled(false);
				getStopScanButton().setEnabled(true);
				getPauseScanButton().setEnabled(true);
				if (scanThread.isPaused()) {
					getPauseScanButton().setSelected(true);
					getPauseScanButton().setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.unpause"));
				} else {
					getPauseScanButton().setSelected(false);
					getPauseScanButton().setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.pause"));
				}
				getProgressBar().setEnabled(true);
			} else {
				getStartScanButton().setEnabled(true);
				getStopScanButton().setEnabled(false);
				getPauseScanButton().setEnabled(false);
				getProgressBar().setEnabled(false);
			}
			
			getProgressBar().setValue(scanThread.getProgress());
			getProgressBar().setMaximum(scanThread.getMaximum());
			currentSite = site;
			switchView(currentSite);
		}
	}
	
	public static String cleanSiteName(String site, boolean incPort) {
		boolean ssl = false;
		if (site.toLowerCase().startsWith("https:")) {
			ssl = true;
		}
		if (site.indexOf("//") >= 0) {
			site = site.substring(site.indexOf("//") + 2);
		}
		if (site.indexOf(" (") >= 0) {
			// Alert counts
			site = site.substring(0, site.indexOf(" ("));
		}
                // does site already contain port number ?
                if(site.indexOf(":")>=0) {
                    if (! incPort) { // We dont't want the port number, strip it off
			site = site.substring(0, site.indexOf(":"));
                    } // otherwise keep it                   
                } else { // The site does not contain the port number
                    if(incPort) { // But we want it! Add it
                        if (ssl) {
                            site += ":443";
                        } else {
                            site += ":80";
                        }
                    }
                } // Site does not contain the port number and we don't want it
                // Nothing to do
		return site;
	}
	
	public static String cleanSiteName(SiteNode node, boolean incPort) {
		if (node != null) {
			while (node.getParent() != null && node.getParent().getParent() != null) {
				node = (SiteNode) node.getParent();
			}
			return cleanSiteName(node.getNodeName(), incPort);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected SiteNode getSiteNode (String siteName) {
		SiteMap siteTree = this.getExtension().getModel().getSession().getSiteTree();
		SiteNode rootNode = (SiteNode) siteTree.getRoot();
		
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

	public void nodeSelected(SiteNode node, boolean incPort) {
		siteSelected(cleanSiteName(node, incPort));
	}
	
	protected abstract GenericScanner newScanThread (String site, AbstractParam params);

	protected void startScan() {
		this.startScan(null);
	}
	protected void startScan(SiteNode startNode) {
 		log.debug("startScan " + prefix);
		this.getStartScanButton().setEnabled(false);
		this.getStopScanButton().setEnabled(true);
		this.getPauseScanButton().setEnabled(true);
		this.activeScans.add(currentSite);

		GenericScanner scanThread = scanMap.get(currentSite);
		if (scanThread.isStopped()) {
			// Start a new thread
			scanThread.reset();
			scanThread = this.newScanThread(currentSite, scanParam);
			scanMap.put(currentSite, scanThread);
		}
		if (scanThread.getStartNode() == null) {
			// Quick fix - need to look at this again
			scanThread.setStartNode(startNode);
		}
		scanThread.start();
		scanMap.put(currentSite, scanThread);
		setActiveScanLabels();
		getProgressBar().setEnabled(true);
		getProgressBar().setMaximum(scanThread.getMaximum());
		String selectedSite = currentSite;	// currentSite can change when we remove elements
		if (siteModel.getIndexOf(passiveSitelabel(selectedSite)) >= 0) {
			// Change the site label to be bold
			siteModel.removeElement(passiveSitelabel(selectedSite));
			siteModel.addElement(activeSitelabel(selectedSite));
			siteModel.setSelectedItem(activeSitelabel(selectedSite));
		}
		switchView(currentSite);
	}
	
	private void stopScan() {
		log.debug("stopScan " + prefix + " on " + currentSite);
		GenericScanner scan = scanMap.get(currentSite);
		if (scan != null) {
			scan.stopScan();
		}
	}

	private void pauseScan() {
		log.debug("pauseScan " + prefix + " on " + currentSite);
		GenericScanner scan = scanMap.get(currentSite);
		if (scan != null) {
			if (scan.isPaused()) {
				scan.resumeScan();
				getPauseScanButton().setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.pause"));
			} else {
				scan.pauseScan();
				getPauseScanButton().setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.unpause"));
			}
		}
	}

	public void scanFinshed(final String host) {
	    if (EventQueue.isDispatchThread()) {
        	scanFinshedEventHandler(host);
	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	                	scanFinshedEventHandler(host);
	                }
	            });
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);
	        }
	    }
	}

	private void scanFinshedEventHandler(String host) {
		log.debug("scanFinished " + prefix + " on " + currentSite);
		if (host.equals(currentSite)) {
			getStartScanButton().setEnabled(true);
			getStopScanButton().setEnabled(false);
			getPauseScanButton().setEnabled(false);
			getPauseScanButton().setSelected(false);
			getPauseScanButton().setToolTipText(Constant.messages.getString(prefix + ".toolbar.button.pause"));
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

	public void scanProgress(final String host, final int progress, final int maximum) {
	    if (EventQueue.isDispatchThread()) {
        	scanFinshedEventHandler(host);
	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	                	scanProgressEventHandler(host, progress, maximum);
	                }
	            });
	        } catch (Exception e) {
	            log.error(e.getMessage(), e);
	        }
	    }
	}

	private void scanProgressEventHandler(String host, int progress, int maximum) {
		//log.debug("scanProgress " + prefix + " on " + currentSite + " " + progress);
		if (host.equals(currentSite)) {
			getProgressBar().setValue(progress);
			getProgressBar().setMaximum(maximum);
		}		
	}

	public void reset() {
		log.debug("reset " + prefix);
		// Stop all scans
		Set<Entry<String, GenericScanner>> set = scanMap.entrySet();
		Iterator<Entry<String, GenericScanner>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, GenericScanner> entry = iter.next();
			entry.getValue().stopScan();
			entry.getValue().reset();
		}
		// Wait until all threads have stopped
		while (activeScans.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		scanMap.clear();
		
		siteModel.removeAllElements();
		siteSelect.addItem(Constant.messages.getString(prefix + ".toolbar.site.select"));
		siteSelect.setSelectedIndex(0);
		currentSite = null;
		
		setActiveScanLabels();
		getStartScanButton().setEnabled(false);
		getStopScanButton().setEnabled(false);
		getProgressBar().setEnabled(false);
		getProgressBar().setValue(0);
	}

	public ExtensionAdaptor getExtension() {
		return extension;
	}
	
    public AbstractParam getScanParam() {
		return scanParam;
	}
    
    public GenericScanner getScanThread (String site) {
    	return scanMap.get(site);
    }

    public Map<String, GenericScanner> getScanThreads () {
    	return scanMap;
    }

    public boolean isCurrentSite(String site) {
    	return currentSite != null && currentSite.equals(site);
    }
    
	protected abstract Component getWorkPanel();
	
	protected abstract void switchView (String site);
	
}
