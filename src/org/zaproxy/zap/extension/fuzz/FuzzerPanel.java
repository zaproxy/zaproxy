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
package org.zaproxy.zap.extension.fuzz;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.search.ExtensionSearch;
import org.zaproxy.zap.extension.search.SearchMatch;
import org.zaproxy.zap.extension.search.SearchResult;
import org.zaproxy.zap.view.ScanStatus;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FuzzerPanel extends AbstractPanel { //implements FuzzerListenner {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "fuzzpanel";
	
	private ExtensionFuzz extension = null;
	private JPanel panelCommand = null;
	private JToolBar panelToolbar = null;
	private JScrollPane jScrollPane = null;
    private FuzzerPanelCellRenderer portPanelCellRenderer = null;
	private DefaultListModel resultsModel;
	private JTextPane initialMessage = null;

	//private JButton startScanButton = null;
	private JButton stopScanButton = null;
	private JToggleButton pauseScanButton = null;
	private JButton optionsButton = null;
	private JList fuzzResultList = null;
	private JProgressBar progressBar = null;

	//private HttpPanelRequest requestPanel = null;
	//private HttpPanelResponse responsePanel = null;
	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;

	private ScanStatus scanStatus = null;

    private static Logger log = Logger.getLogger(FuzzerPanel.class);
    
    public FuzzerPanel(ExtensionFuzz extension, FuzzerParam fuzzerParam) {
        super();
        this.extension = extension;
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
        this.setName(Constant.messages.getString("fuzz.panel.title"));
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/097.png")));
        this.add(getPanelCommand(), getPanelCommand().getName());
        
        // Wont need to do this if/when this class is changed to extend ScanPanel
        scanStatus = new ScanStatus(
        				new ImageIcon(
        					getClass().getResource("/resource/icon/16/097.png")),
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
*/
	private JButton getStopScanButton() {
		if (stopScanButton == null) {
			stopScanButton = new JButton();
			stopScanButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.stop"));
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
			pauseScanButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.pause"));
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

	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("fuzz.toolbar.button.options"));
			optionsButton.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/041.png")));
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

	private void resetFuzzResultList() {
		resultsModel = new DefaultListModel();
		getFuzzResultList().setModel(resultsModel);
	}
	
	protected void addFuzzResult(final HttpMessage msg) {
		
		if (EventQueue.isDispatchThread()) {
			resultsModel.addElement(msg);
			getProgressBar().setValue(getProgressBar().getValue() + 1);
		    return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					resultsModel.addElement(msg);
					getProgressBar().setValue(getProgressBar().getValue() + 1);
				}
			});
		} catch (Exception e) {
		}
	}

	private JList getFuzzResultList() {
		if (fuzzResultList == null) {
			fuzzResultList = new JList();
			fuzzResultList.setDoubleBuffered(true);
			fuzzResultList.setCellRenderer(getPortPanelCellRenderer());
			fuzzResultList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			fuzzResultList.setName(PANEL_NAME);
			fuzzResultList.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			
			fuzzResultList.setFixedCellHeight(16);	// Significantly speeds up rendering

	        fuzzResultList.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mousePressed(java.awt.event.MouseEvent e) {    
				    if (SwingUtilities.isRightMouseButton(e)) {
				        View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
				    }	
				}
			});

			fuzzResultList.addListSelectionListener(new javax.swing.event.ListSelectionListener() { 

				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
				    if (fuzzResultList.getSelectedValue() == null) {
				        return;
				    }
                    
				    displayMessage((HttpMessage) fuzzResultList.getSelectedValue());
				}
			});
			
			resetFuzzResultList();
		}
		return fuzzResultList;
	}

    private void displayMessage(HttpMessage msg) {
		try {
			requestPanel.setMessage(msg);
			responsePanel.setMessage(msg);
			
			// The fuzz payload is recorded in the note
			
	        String note = msg.getNote();
	        if (note != null && note.length() > 0) {
	        	int startIndex = msg.getResponseBody().toString().indexOf(note);
	        	if (startIndex >= 0) {
	        		// Found the exact pattern - highlight it
	        		SearchMatch sm = new SearchMatch(msg, SearchMatch.Location.RESPONSE_BODY, startIndex, startIndex + note.length());
	        		responsePanel.setTabFocus();
	        		responsePanel.requestFocus();
					responsePanel.highlightBody(sm);
	        	}
	        }

			
		} catch (Exception e) {
			log.error("Failed to access message ", e);
		}
    }

	private ListCellRenderer getPortPanelCellRenderer() {
        if (portPanelCellRenderer == null) {
            portPanelCellRenderer = new FuzzerPanelCellRenderer();
            portPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            portPanelCellRenderer.setBackground(java.awt.Color.white);
            portPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return portPanelCellRenderer;
	}

	private void stopScan() {
		log.debug("Stopping fuzzing");
		extension.stopFuzzers ();
	}

	private void pauseScan() {
		if (getPauseScanButton().getModel().isSelected()) {
			log.debug("Pausing fuzzing");
			extension.pauseFuzzers();
			getPauseScanButton().setToolTipText(Constant.messages.getString("fuzz.toolbar.button.unpause"));
		} else {
			log.debug("Resuming fuzzing");
			extension.resumeFuzzers();
			getPauseScanButton().setToolTipText(Constant.messages.getString("fuzz.toolbar.button.pause"));

		}
	}

	public void scanStarted() {
		this.getJScrollPane().setViewportView(getFuzzResultList());
		this.setTabFocus();
		resetFuzzResultList();

		getProgressBar().setEnabled(true);
		getStopScanButton().setEnabled(true);
		getPauseScanButton().setEnabled(true);
		scanStatus.incScanCount();
	}

	public void scanFinshed() {
		getStopScanButton().setEnabled(false);
		getPauseScanButton().setEnabled(false);
		getPauseScanButton().setSelected(false);
		getPauseScanButton().setToolTipText(Constant.messages.getString("fuzz.toolbar.button.pause"));
		getProgressBar().setEnabled(false);
		scanStatus.decScanCount();
	}

	public void scanProgress(int done, int todo) {
		getProgressBar().setValue(done);
		getProgressBar().setMaximum(todo);
	}

	public void reset() {
		this.stopScan();
		
		resetFuzzResultList();
		//getStartScanButton().setEnabled(false);
		getStopScanButton().setEnabled(false);
		getProgressBar().setEnabled(false);
		getProgressBar().setValue(0);
		
	}

    //public void setDisplayPanel(HttpPanelRequest requestPanel, HttpPanelResponse responsePanel) {
    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;

    }

	@SuppressWarnings("unchecked")
	public List<SearchResult> searchResults(Pattern pattern, boolean inverse) {
		List<SearchResult> results = new ArrayList<SearchResult>();
		
		if (resultsModel == null) {
			return results;
		}
		
		Enumeration<HttpMessage> enumeration = (Enumeration<HttpMessage>) resultsModel.elements();
		Matcher matcher;
		while (enumeration.hasMoreElements()) {
			HttpMessage msg = enumeration.nextElement();
			if (msg != null && msg.getRequestBody() != null) {
	            matcher = pattern.matcher(msg.getResponseBody().toString());
	            if (matcher.find()) {
	            	if (! inverse) {
	            		results.add(new SearchResult(msg, ExtensionSearch.Type.Fuzz, 
	            			pattern.toString(), matcher.group()));
	            	}
	            } else if (inverse) {
            		results.add(new SearchResult(msg, ExtensionSearch.Type.Fuzz, 
	            			pattern.toString(), matcher.group()));
	            }
			}
		}
		return results;
	}

}
