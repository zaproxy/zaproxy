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

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.view.ScanPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ActiveScanPanel extends ScanPanel implements ScanListenner, ScannerListener {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "portscan";
	
	private JScrollPane jScrollPane = null;
    private ActiveScanPanelCellRenderer activeScanPanelCellRenderer = null;
	private static JList messageList = null;

	private HttpPanel requestPanel = null;
	private HttpPanel responsePanel = null;

    //private static Log log = LogFactory.getLog(ActiveScanPanel.class);

    /**
     * @param portScanParam 
     * 
     */
    public ActiveScanPanel(ExtensionActiveScan extension) {
    	// 'fire' icon
        super("ascan", new ImageIcon(extension.getClass().getResource("/resource/icon/16/093.png")), extension, null);
    }


	@Override
	protected JScrollPane getWorkPanel() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getMessageList());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}

	private void resetMessageList() {
		getMessageList().setModel(new DefaultListModel());
	}

	private synchronized JList getMessageList() {
		if (messageList == null) {
			messageList = new JList();
			messageList.setDoubleBuffered(true);
			messageList.setCellRenderer(getActiveScanPanelCellRenderer());
			messageList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			messageList.setName(PANEL_NAME);
			messageList.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
			
			messageList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					HttpMessage msg = (HttpMessage) messageList.getSelectedValue();
					displayMessage(msg);
					
				}});
			
			resetMessageList();
		}
		return messageList;
	}
	
    private void displayMessage(HttpMessage msg) {
    	if (msg == null) {
    		return;
    	}
        if (msg.getRequestHeader() != null && msg.getRequestHeader().isEmpty()) {
            requestPanel.setMessage(null, true);
        } else {
            requestPanel.setMessage(msg, true);
        }
        
        if (msg.getResponseHeader() != null && msg.getResponseHeader().isEmpty()) {
            responsePanel.setMessage(null, false);
        } else {
            responsePanel.setMessage(msg, false);
        }
    }



	private ListCellRenderer getActiveScanPanelCellRenderer() {
        if (activeScanPanelCellRenderer == null) {
            activeScanPanelCellRenderer = new ActiveScanPanelCellRenderer();
            activeScanPanelCellRenderer.setSize(new java.awt.Dimension(328,21));
            activeScanPanelCellRenderer.setBackground(java.awt.Color.white);
            activeScanPanelCellRenderer.setFont(new java.awt.Font("MS Sans Serif", java.awt.Font.PLAIN, 12));
        }
        return activeScanPanelCellRenderer;
	}

	@Override
	protected GenericScanner newScanThread(String site, AbstractParam params) {
		ActiveScan as = new ActiveScan(site, ((ExtensionActiveScan)this.getExtension()).getScannerParam(), 
				this.getExtension().getModel().getOptionsParam().getConnectionParam(), this);
		return as;
	}


	@Override
	protected void switchView(String site) {
		GenericScanner thread = this.getScanThread(site);
		if (thread != null) {
			getMessageList().setModel(((ActiveScan)thread).getList());
		}
	}


	@Override
	public void alertFound(Alert alert) {
		((ExtensionActiveScan)this.getExtension()).alertFound(alert);
	}


	@Override
	public void hostComplete(String hostAndPort) {
		if (hostAndPort.indexOf("://") > 0) {
			hostAndPort = hostAndPort.substring(hostAndPort.indexOf("://") + 3);
		}
		this.scanFinshed(hostAndPort);
		
	}


	@Override
	public void hostNewScan(String hostAndPort, HostProcess hostThread) {
	}


	@Override
	public void hostProgress(String hostAndPort, String msg, int percentage) {
		if (hostAndPort.indexOf("://") > 0) {
			hostAndPort = hostAndPort.substring(hostAndPort.indexOf("://") + 3);
		}
		if (this.isCurrentSite(hostAndPort)) {
			this.scanProgress(hostAndPort, percentage, 100);
		}
	}


	@Override
	public void scannerComplete() {
	}


	@Override
	public void notifyNewMessage(HttpMessage msg) {
	}

    public void setDisplayPanel(HttpPanel requestPanel, HttpPanel responsePanel) {
        this.requestPanel = requestPanel;
        this.responsePanel = responsePanel;

    }
}
