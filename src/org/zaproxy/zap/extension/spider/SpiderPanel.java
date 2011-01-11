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
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.view.ScanPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SpiderPanel extends ScanPanel implements ScanListenner {
	
	private static final long serialVersionUID = 1L;

	public static final String PANEL_NAME = "spider";
	
	private JSplitPane splitPane = null;
	private JPanel leftPanel = null;
	private JPanel rightPanel = null;
	private JLabel jLabel = null;
    // TODO same hack as port scan??
	private static JTextArea txtURIFound = null;
	private JScrollPane jScrollPane = null;
	private JLabel jLabel1 = null;
    // TODO same hack as port scan??
	private static JTextArea txtURISkip = null;
	private JScrollPane jScrollPane1 = null;

	private static Log log = LogFactory.getLog(SpiderPanel.class);

	//private static JList portList = null;
    
    /**
     * @param portScanParam 
     * 
     */
    public SpiderPanel(ExtensionSpider extension, org.parosproxy.paros.core.spider.SpiderParam portScanParam) {
        super("spider", new ImageIcon(extension.getClass().getResource("/resource/icon/spider.png")), extension, portScanParam);
    }

	@Override
	protected ScanThread newScanThread(String site, AbstractParam params) {
		SpiderThread st = new SpiderThread((ExtensionSpider)this.getExtension(), site, this, 
				(org.parosproxy.paros.core.spider.SpiderParam) params);
		
		st.setStartNode(this.getSiteNode(site));
		return st;
	}
	
	@Override
	protected void startScan() {
		this.clear();
		// Only allow one spider at a time, due to the way it uses the db
		this.getSiteSelect().setEnabled(false);
		super.startScan();
	}
	
	@Override
	protected void siteSelected(String site) {
		// Only allow one spider at a time, due to the way it uses the db
		if (this.getSiteSelect().isEnabled()) {
			super.siteSelected(site);
		}
	}
	
	@Override
	public void scanFinshed(String host) {
		super.scanFinshed(host);
		// Only allow one spider at a time, due to the way it uses the db
		this.getSiteSelect().setEnabled(true);
	}

	@Override
	public boolean isScanning(SiteNode node) {
		// Only allow one spider at a time, due to the way it uses the db
		return ! this.getSiteSelect().isEnabled();
	}


	@Override
	protected void switchView(String site) {
		// Cant switch views in this version
	}
	
	
	/**
	 * This method initializes splitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */    
	@Override
	protected JSplitPane getWorkPanel() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setName("splitPane");
			splitPane.setDividerSize(3);
			//splitPane.setDividerLocation(120);
			splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(getLeftPanel());
			splitPane.setBottomComponent(getRightPanel());
			splitPane.setResizeWeight(0.5D);
		}
		return splitPane;
	}
	/**
	 * This method initializes leftPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getLeftPanel() {
		if (leftPanel == null) {
			jLabel = new JLabel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			leftPanel = new JPanel();
			leftPanel.setLayout(new GridBagLayout());
			jLabel.setText(Constant.messages.getString("spider.label.inScope")); 
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
			gridBagConstraints2.insets = new java.awt.Insets(0,2,0,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints2.ipady = 24;
			leftPanel.add(jLabel, gridBagConstraints1);
			leftPanel.add(getJScrollPane(), gridBagConstraints2);
		}
		return leftPanel;
	}
	/**
	 * This method initializes rightPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getRightPanel() {
		if (rightPanel == null) {
			jLabel1 = new JLabel();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			rightPanel = new JPanel();
			rightPanel.setLayout(new GridBagLayout());
			jLabel1.setText(Constant.messages.getString("spider.label.outOfScope"));
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints4.insets = new java.awt.Insets(0,2,0,2);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints4.ipady = 24;
			rightPanel.add(jLabel1, gridBagConstraints3);
			rightPanel.add(getJScrollPane1(), gridBagConstraints4);
		}
		return rightPanel;
	}
	/**
	 * This method initializes txtURISkip	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	JTextArea getTxtURIFound() {
		if (txtURIFound == null) {
			txtURIFound = new JTextArea();
			txtURIFound.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtURIFound.setEditable(false);
			txtURIFound.setLineWrap(true);
			txtURIFound.addMouseListener(new java.awt.event.MouseAdapter() { 
			    public void mousePressed(java.awt.event.MouseEvent e) {    
			        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
			            View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }			    	
			    }
			    
			});
		}
		return txtURIFound;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtURIFound());
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes txtURISkip	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	JTextArea getTxtURISkip() {
		if (txtURISkip == null) {
			txtURISkip = new JTextArea();
			txtURISkip.setEditable(false);
			txtURISkip.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtURISkip.setLineWrap(true);
			txtURISkip.addMouseListener(new java.awt.event.MouseAdapter() { 
			    public void mousePressed(java.awt.event.MouseEvent e) {    
			        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
			            View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			        }			    	
			    }
			});
		}
		return txtURISkip;
	}
	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTxtURISkip());
			jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane1;
	}
	
	void appendFound(final String s) {
		if (EventQueue.isDispatchThread()) {
			getTxtURIFound().append(s);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					getTxtURIFound().append(s);
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	    
	}
	
	void appendFoundButSkip(final String s) {
		if (EventQueue.isDispatchThread()) {
			getTxtURISkip().append(s);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					getTxtURISkip().append(s);
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	    
	void clear() {
	    getTxtURIFound().setText("");
	    getTxtURISkip().setText("");
	}
	

}
