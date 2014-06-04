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
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.StickyScrollbarAdjustmentListener;
import org.zaproxy.zap.view.ScanStatus;
import org.zaproxy.zap.view.ZapToggleButton;

public class FuzzerPanel extends AbstractPanel{
	
	public static final String PANEL_NAME = "fuzzpanel";
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(FuzzerPanel.class);
	
	private ExtensionFuzz extension = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
	private JTextPane initialMessage = null;

	private JButton stopScanButton = null;
	private ZapToggleButton pauseScanButton = null;
	private JButton optionsButton = null;
	private JProgressBar progressBar = null;

	private ScanStatus scanStatus = null;

    private FuzzerContentPanel contentPanel;
    
    public FuzzerPanel(ExtensionFuzz extension) {
        super();
        this.extension = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private  void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("fuzz.panel.title"));
		this.setIcon(new ImageIcon(FuzzerPanel.class.getResource("/resource/icon/16/097.png")));
        this.add(getPanelCommand(), getPanelCommand().getName());
        
        // Wont need to do this if/when this class is changed to extend ScanPanel
        scanStatus = new ScanStatus(
        				new ImageIcon(
        					FuzzerPanel.class.getResource("/resource/icon/16/097.png")),
        					Constant.messages.getString("fuzz.panel.title"));
       
        View.getSingleton().getMainFrame().getMainFooterPanel().addFooterToolbarRightLabel(scanStatus.getCountLabel());

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
			panelCommand.setName("Fuzzer");
			
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
			
			panelCommand.add(getPanelToolbar(), gridBagConstraints1);
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
			panelToolbar.setName("FuzzToolbar");
			
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			//Dummy
			GridBagConstraints gridBagConstraintsx = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsy = new GridBagConstraints();

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
			gridBagConstraintsx.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsx.anchor = java.awt.GridBagConstraints.WEST;

			gridBagConstraintsy.gridx = 21;
			gridBagConstraintsy.gridy = 0;
			gridBagConstraintsy.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraintsy.anchor = java.awt.GridBagConstraints.WEST;

			JLabel t1 = new JLabel();

			// panelToolbar.add(getStartScanButton(), gridBagConstraints5);
			panelToolbar.add(getPauseScanButton(), gridBagConstraints6);
			panelToolbar.add(getStopScanButton(), gridBagConstraints7);
			panelToolbar.add(getProgressBar(), gridBagConstraints8);

			panelToolbar.add(t1, gridBagConstraintsx);
			panelToolbar.add(getOptionsButton(), gridBagConstraintsy);
		}
		return panelToolbar;
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
/*	
	private JButton getStartScanButton() {
		if (startScanButton == null) {
			startScanButton = new JButton();
			startScanButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.start"));
			startScanButton.setIcon(new ImageIcon(FuzzerPanel.class.getResource("/resource/icon/16/131.png")));
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
*/
	private JButton getStopScanButton() {
		if (stopScanButton == null) {
			stopScanButton = new JButton();
			stopScanButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.stop"));
			stopScanButton.setIcon(new ImageIcon(FuzzerPanel.class.getResource("/resource/icon/16/142.png")));
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
			pauseScanButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.pause"));
			pauseScanButton.setSelectedToolTipText(Constant.messages.getString("fuzz.toolbar.button.unpause"));
			pauseScanButton.setIcon(new ImageIcon(FuzzerPanel.class.getResource("/resource/icon/16/141.png")));
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
			optionsButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(FuzzerPanel.class.getResource("/resource/icon/16/041.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					Control.getSingleton().getMenuToolsControl().options(
							Constant.messages.getString("fuzz.options.title"));
				}
			});
		}
		return optionsButton;
	}

	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getInitialMessage());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.getVerticalScrollBar().addAdjustmentListener(new StickyScrollbarAdjustmentListener());
		}
		return jScrollPane;
	}
	
	private JTextPane getInitialMessage() {
		if (initialMessage == null) {
			initialMessage = new JTextPane();
			initialMessage.setEditable(false);
			initialMessage.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			initialMessage.setContentType("text/html");
			initialMessage.setText(Constant.messages.getString("fuzz.label.initialMessage"));
		}
		
		return initialMessage;
	}

	public void setContentPanel(FuzzerContentPanel aContentPanel) {
	    contentPanel = aContentPanel;
	}
	
	private void stopScan() {
		logger.debug("Stopping fuzzing");
		extension.stopFuzzers();
	}

	private void pauseScan() {
		if (getPauseScanButton().getModel().isSelected()) {
			logger.debug("Pausing fuzzing");
			extension.pauseFuzzers();
		} else {
			logger.debug("Resuming fuzzing");
			extension.resumeFuzzers();

		}
	}

	private void scanStarted() {
        logger.debug("Starting fuzzing");
        
        contentPanel.clear();
		this.getJScrollPane().setViewportView(contentPanel.getComponent());
        this.getJScrollPane().validate();
		this.setTabFocus();

		getProgressBar().setEnabled(true);
		getStopScanButton().setEnabled(true);
		getPauseScanButton().setEnabled(true);
		scanStatus.incScanCount();
	}

	public void scanFinished() {
		getStopScanButton().setEnabled(false);
		getPauseScanButton().setEnabled(false);
		getPauseScanButton().setSelected(false);
		getProgressBar().setEnabled(false);
		scanStatus.decScanCount();
	}

	private void scanProgress(int done, int todo) {
		getProgressBar().setValue(done);
		getProgressBar().setMaximum(todo);
	}

	public void reset() {
		
		if (contentPanel != null) {
		    contentPanel.clear();
		    contentPanel = null;
		}
		
		getStopScanButton().setEnabled(false);
		getProgressBar().setEnabled(false);
		getProgressBar().setValue(0);
		
        this.getJScrollPane().setViewportView(getInitialMessage());
	}
	
    public void newScan(Integer total) {
        this.scanStarted();
        scanProgress(0, total);
    }
    
    public void inComingResult(FuzzResult<?,?> fuzzResult) {
        contentPanel.addFuzzResult(fuzzResult);
        getProgressBar().setValue(getProgressBar().getValue() + 1);
    }
}
