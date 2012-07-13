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

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.ScanListenner;
import org.zaproxy.zap.model.ScanThread;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.view.ScanPanel;

/**
 * The Class SpiderPanel implements the Panel that is shown to the users when selecting the Spider
 * Scan Tab.
 */
public class SpiderPanel extends ScanPanel implements ScanListenner {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(SpiderPanel.class);

	public static final String PANEL_NAME = "spider";

	private JSplitPane splitPane = null;
	private JPanel topPanel = null;
	private JPanel bottomPanel = null;
	private static ZapTextArea txtURIFound = null;
	private JScrollPane topScrollPane = null;
	private static ZapTextArea txtURISkip = null;
	private JScrollPane bottomScrollPane = null;

	/**
	 * Instantiates a new spider panel.
	 * 
	 * @param extension the extension
	 * @param spiderScanParam the spider scan param
	 */
	public SpiderPanel(ExtensionSpider extension, SpiderParam spiderScanParam) {
		super("spider", new ImageIcon(SpiderPanel.class.getResource("/resource/icon/16/spider.png")), extension,
				spiderScanParam);
	}

	@Override
	protected ScanThread newScanThread(String site, AbstractParam params) {
		SpiderThread st = new SpiderThread((ExtensionSpider) this.getExtension(), site, this, (SpiderParam) params);
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
	public boolean isScanning(SiteNode node, boolean incPort) {
		// Only allow one spider at a time, due to the way it uses the db
		return !this.getSiteSelect().isEnabled();
	}

	@Override
	protected void switchView(String site) {
		// Cant switch views in this version
	}

	/**
	 * This method initializes the working SplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	@Override
	protected JSplitPane getWorkPanel() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setName("SpiderSplitPane");
			splitPane.setDividerSize(3);
			splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(getTopPanel());
			splitPane.setBottomComponent(getBottomPanel());
			splitPane.setResizeWeight(0.5D);
		}
		return splitPane;
	}

	/**
	 * This method initializes the topPanel.
	 * 
	 * @return the top panel
	 */
	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());

			GridBagConstraints topLabelGridBag = new GridBagConstraints();
			GridBagConstraints topScrollPaneGridBag = new GridBagConstraints();

			topLabelGridBag.gridx = 0;
			topLabelGridBag.gridy = 0;
			topLabelGridBag.weightx = 1.0D;
			topLabelGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			topLabelGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;
			topLabelGridBag.insets = new java.awt.Insets(2, 2, 2, 2);

			topScrollPaneGridBag.gridx = 0;
			topScrollPaneGridBag.gridy = 1;
			topScrollPaneGridBag.ipady = 24;
			topScrollPaneGridBag.weightx = 1.0;
			topScrollPaneGridBag.weighty = 1.0;
			topScrollPaneGridBag.fill = java.awt.GridBagConstraints.BOTH;
			topScrollPaneGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			topScrollPaneGridBag.insets = new java.awt.Insets(0, 2, 0, 2);

			JLabel topLabel = new JLabel();
			topLabel.setText(Constant.messages.getString("spider.label.inScope"));

			topPanel.add(topLabel, topLabelGridBag);
			topPanel.add(getTopScrollPane(), topScrollPaneGridBag);
		}
		return topPanel;
	}

	/**
	 * This method initializes the bottom panel.
	 * 
	 * @return the bottom panel
	 */
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();
			bottomPanel.setLayout(new GridBagLayout());

			GridBagConstraints bottomLabelGridBag = new GridBagConstraints();
			GridBagConstraints bottomPanelGridBag = new GridBagConstraints();

			bottomLabelGridBag.gridx = 0;
			bottomLabelGridBag.gridy = 0;
			bottomLabelGridBag.weightx = 1.0D;
			bottomLabelGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;
			bottomLabelGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			bottomLabelGridBag.insets = new java.awt.Insets(2, 2, 2, 2);

			bottomPanelGridBag.gridx = 0;
			bottomPanelGridBag.gridy = 1;
			bottomPanelGridBag.ipady = 24;
			bottomPanelGridBag.weightx = 1.0;
			bottomPanelGridBag.weighty = 1.0;
			bottomPanelGridBag.fill = java.awt.GridBagConstraints.BOTH;
			bottomPanelGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
			bottomPanelGridBag.insets = new java.awt.Insets(0, 2, 0, 2);

			JLabel bottomLabel = new JLabel();
			bottomLabel.setText(Constant.messages.getString("spider.label.outOfScope"));

			bottomPanel.add(bottomLabel, bottomLabelGridBag);
			bottomPanel.add(getBottomScrollPane(), bottomPanelGridBag);
		}
		return bottomPanel;
	}

	/**
	 * This method initializes txtURISkip
	 * 
	 * @return org.zaproxy.zap.utils.ZapTextArea
	 */
	ZapTextArea getTxtURIFound() {
		if (txtURIFound == null) {
			txtURIFound = new ZapTextArea();
			txtURIFound.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtURIFound.setText(Constant.messages.getString("spider.panel.emptyView"));
			txtURIFound.setEditable(false);
			txtURIFound.setLineWrap(true);
			txtURIFound.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					mouseClicked(e);
				}

				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					mouseClicked(e);
				}

				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) { // right
																									// mouse
																									// button
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
	private JScrollPane getTopScrollPane() {
		if (topScrollPane == null) {
			topScrollPane = new JScrollPane();
			topScrollPane.setViewportView(getTxtURIFound());
			topScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			topScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return topScrollPane;
	}

	/**
	 * This method initializes txtURISkip
	 * 
	 * @return org.zaproxy.zap.utils.ZapTextArea
	 */
	ZapTextArea getTxtURISkip() {
		if (txtURISkip == null) {
			txtURISkip = new ZapTextArea();
			txtURISkip.setEditable(false);
			txtURISkip.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtURISkip.setLineWrap(true);
			txtURISkip.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					mouseClicked(e);
				}

				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					mouseClicked(e);
				}

				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) { // right
																									// mouse
																									// button
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
	private JScrollPane getBottomScrollPane() {
		if (bottomScrollPane == null) {
			bottomScrollPane = new JScrollPane();
			bottomScrollPane.setViewportView(getTxtURISkip());
			bottomScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return bottomScrollPane;
	}

	/**
	 * Appends a new url found to the list of URLs found (top Pane). Can be called from other
	 * threads, in which case it will do the change on the main thread.
	 * 
	 * @param url the url
	 */
	void appendURLFound(final String url) {
		if (EventQueue.isDispatchThread()) {
			getTxtURIFound().append(url);
			return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					synchronized (txtURIFound) {
						getTxtURIFound().append(url);
					}
				}
			});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	/**
	 * Appends a new url found to the list of URLs found, but skipped (lower Pane). Can be called
	 * from other threads, in which case it will do the change on the main thread.
	 * 
	 * @param url the url
	 */
	void appendURLFoundButSkipped(final String url) {
		if (EventQueue.isDispatchThread()) {
			getTxtURISkip().append(url);
			return;
		}
		try {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					synchronized (txtURISkip) {
						getTxtURISkip().append(url);
					}
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
