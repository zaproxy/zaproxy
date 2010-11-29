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

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.GenericScanner;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.view.ScanPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PortScanPanel extends ScanPanel implements ScanListenner {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "portscan";
	
	private JScrollPane jScrollPane = null;
    private PortPanelCellRenderer portPanelCellRenderer = null;

	private static JList portList = null;
    
    /**
     * @param portScanParam 
     * 
     */
    public PortScanPanel(ExtensionPortScan extension, PortScanParam portScanParam) {
    	// 'picture list' icon
        super("ports", new ImageIcon(extension.getClass().getResource("/resource/icon/16/187.png")), extension, portScanParam);
    }


	@Override
	protected JScrollPane getWorkPanel() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getPortList());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	
	@Override
	public void reset() {
		super.reset();
		this.resetPortList();
	}

	private void resetPortList() {
		getPortList().setModel(new DefaultListModel());
	}

	private synchronized JList getPortList() {
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

	@Override
	protected ScanThread newScanThread(String site, AbstractParam params) {
		return new PortScan(site, this, (PortScanParam) params);
	}


	@Override
	protected void switchView(String site) {
		if (site.indexOf(":") >= 0) {
			// Strip off port
			site = site.substring(0, site.indexOf(":"));
		}
		GenericScanner thread = this.getScanThread(site);
		if (thread != null) {
			getPortList().setModel(((PortScan)thread).getList());
		}
	}

	@Override
	protected String getSiteName(SiteNode node) {
		String site = super.getSiteName(node);
		if (site != null && site.indexOf(":") > 0) {
			// Strip off port
			site = site.substring(0, site.indexOf(":"));
		}
		return site;
	}

}
