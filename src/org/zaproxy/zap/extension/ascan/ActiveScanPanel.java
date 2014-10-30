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
package org.zaproxy.zap.extension.ascan;

import java.awt.Event;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.model.GenericScanner2;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.view.ScanPanel2;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

public class ActiveScanPanel extends ScanPanel2 implements ScanListenner2, ScannerListener {
	
	private static final Logger LOGGER = Logger.getLogger(ActiveScanPanel.class);

	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated (2.3.0) Replaced by {@link #MESSAGE_CONTAINER_NAME}.
	 */
	@Deprecated
	public static final String PANEL_NAME = "ascan";

	/**
	 * The name of the active scan HTTP messages container.
	 * 
	 * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
	 */
	public static final String MESSAGE_CONTAINER_NAME = "ActiveScanMessageContainer";

    private static final ActiveScanTableModel EMPTY_RESULTS_MODEL = new ActiveScanTableModel();
	
    private ExtensionActiveScan extension;
	private JScrollPane jScrollPane;
	private HistoryReferencesTable messagesTable;
    
	private JButton optionsButton = null;
	private JButton scanButton = null;
	private JButton progressButton;
	private JLabel numRequests;

    /**
     * @param extension
     */
    public ActiveScanPanel(ExtensionActiveScan extension) {
    	// 'fire' icon
        super("ascan", new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/16/093.png")), extension, null);
        this.extension = extension;
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_A, Event.CTRL_MASK | Event.ALT_MASK | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("ascan.panel.mnemonic"));
    }

	@Override
	protected int addToolBarElements(JToolBar panelToolbar, Location loc, int x) {
		// Override to add elements into the toolbar
		if (Location.start.equals(loc)) {
			panelToolbar.add(getOptionsButton(), getGBC(x++,0));
		}
		if (Location.beforeProgressBar.equals(loc)) {
			panelToolbar.add(getProgressButton(), getGBC(x++,0));
		}
		if (Location.afterProgressBar.equals(loc)) {
			panelToolbar.add(new JLabel(Constant.messages.getString("ascan.toolbar.requests.label")), getGBC(x++,0));
			panelToolbar.add(getNumRequests(), getGBC(x++,0));
		}
		return x;
	}
	
	private JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton();
			optionsButton.setToolTipText(Constant.messages.getString("menu.analyse.scanPolicy"));
			optionsButton.setIcon(new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/fugue/equalizer.png")));
			optionsButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					extension.showPolicyDialog();
				}
			});
		}
		return optionsButton;
	}

	@Override
	public JButton getNewScanButton() {
		if (scanButton == null) {
			scanButton = new JButton(Constant.messages.getString("ascan.toolbar.button.new"));
			scanButton.setIcon(new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/16/093.png")));
			scanButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					extension.showCustomScanDialog(null);
				}
			});
		}
		return scanButton;
	}

	private JButton getProgressButton() {
		if (progressButton == null) {
			progressButton = new JButton();
			progressButton.setEnabled(false);
			progressButton.setToolTipText(Constant.messages.getString("ascan.toolbar.button.progress"));
			progressButton.setIcon(new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/fugue/system-monitor.png")));
			progressButton.addActionListener(new ActionListener () {
				@Override
				public void actionPerformed(ActionEvent e) {
					showScanProgressDialog();
				}
			});
		}
		return progressButton;
	}
	
	private JLabel getNumRequests() {
		if (numRequests == null) {
			numRequests = new JLabel();
		}
		return numRequests;
	}
	
	private void showScanProgressDialog() {
		ActiveScan scan = (ActiveScan) this.getSelectedScanner();
		if (scan != null) {
			ScanProgressDialog spp = new ScanProgressDialog(View.getSingleton().getMainFrame(), scan.getDisplayName());
			spp.setActiveScan(scan);
			spp.setVisible(true);
		}
	}
	
	@Override
	public void clearFinishedScans() {
		if (extension.getScannerParam().isPromptToClearFinishedScans()) {
			// Prompt to double check
			int res = View.getSingleton().showConfirmDontPromptDialog(
					View.getSingleton().getMainFrame(), Constant.messages.getString("ascan.toolbar.confirm.clear"));
			if (View.getSingleton().isDontPromptLastDialogChosen()) {
				extension.getScannerParam().setPromptToClearFinishedScans(false);
			}
			if (res != JOptionPane.YES_OPTION) {
				return;
			}
		}
		super.clearFinishedScans();
	}


	@Override
	protected JScrollPane getWorkPanel() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getMessagesTable());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return jScrollPane;
	}

	private void resetMessagesTable() {
	    getMessagesTable().setModel(EMPTY_RESULTS_MODEL);
	}

	private HistoryReferencesTable getMessagesTable() {
	    if (messagesTable == null) {
	        messagesTable = new HistoryReferencesTable(EMPTY_RESULTS_MODEL);
	        messagesTable.setName(MESSAGE_CONTAINER_NAME);
	        messagesTable.setAutoCreateColumnsFromModel(false);
	    }
	    return messagesTable;
	}

	@Override
	public void switchView(final GenericScanner2 scanner) {
		if (View.isInitialised() && !EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						switchView(scanner);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				LOGGER.error("Failed to switch view: " + e.getMessage(), e);
			}
			return;
		}

		ActiveScan ascan = (ActiveScan)scanner;
		if (scanner != null) {
		    getMessagesTable().setModel(ascan.getMessagesTableModel());
		    this.getNumRequests().setText(Integer.toString(ascan.getTotalRequests()));
		    this.getProgressButton().setEnabled(true);
		} else {
			resetMessagesTable();
		    this.getNumRequests().setText("");
		    this.getProgressButton().setEnabled(false);
		}
	}


	@Override
	public void alertFound(Alert alert) {
		ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.NAME);
		if (extAlert != null) {
			extAlert.alertFound(alert, alert.getHistoryRef());
		}
	}


	@Override
	public void hostComplete(int id, String hostAndPort) {
		this.scanFinshed(id, hostAndPort);
		
	}


	@Override
	public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {
	}


	@Override
	public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
		this.scanProgress(id, hostAndPort, percentage, 100);
		updateRequestCount();
	}

	@Override
	public void scannerComplete(int id) {
		this.scanFinshed(id, this.getName());
	}
	
	private void updateRequestCount() {
		GenericScanner2 gs = this.getSelectedScanner();
		if (gs != null && gs instanceof ActiveScan) {
			this.getNumRequests().setText(Integer.toString(((ActiveScan) gs).getTotalRequests()));
		}
	}

	@Override
	public void notifyNewMessage(HttpMessage msg) {
	}

	@Override
	public void reset() {
		super.reset();
		this.resetMessagesTable();
		this.getProgressButton().setEnabled(false);
	}

	@Override
	protected int getNumberOfScansToShow() {
		return extension.getScannerParam().getMaxScansInUI();
	}
}
