/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/11/21 Heavily refactored extension to support non-HTTP messages.
// ZAP: 2013/04/15 Issue 632: Manual Request Editor dialogue (HTTP) configurations not saved correctly
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/12/12 Issue 1449: Added help button
// ZAP: 2015/08/07 Issue 1768: Update to use a more recent default user agent

package org.parosproxy.paros.extension.manualrequest.http.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.extension.manualrequest.MessageSender;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.view.ZapMenuItem;


public class ManualHttpRequestEditorDialog extends ManualRequestEditorDialog {

	private static final long serialVersionUID = -5830450800029295419L;
    private static final Logger logger = Logger.getLogger(ManualHttpRequestEditorDialog.class);

	private ZapMenuItem menuItem;
	
	private HttpPanelSender sender;

	private RequestResponsePanel requestResponsePanel;
	private HttpPanelRequest requestPanel;
	private HttpPanelResponse responsePanel;


	private JToolBar footerToolbar = null;
	// footer elements
	private JLabel labelTimeElapse = null;
	private JLabel labelContentLength = null;
	private JLabel labelTotalLength = null;
	private String helpKey = null;

	public ManualHttpRequestEditorDialog(boolean isSendEnabled, String configurationKey) throws HeadlessException {
		this(isSendEnabled, configurationKey, null);
	}

	public ManualHttpRequestEditorDialog(boolean isSendEnabled, String configurationKey, String helpKey) throws HeadlessException {
		super(isSendEnabled, configurationKey);
		this.helpKey = helpKey;
		sender = new HttpPanelSender(getRequestPanel(), getResponsePanel());
		
		initialize();
	}
	
	@Override
	protected void initialize() {
		super.initialize();

		// add footer status bar
		getWindowPanel().add(getFooterStatusBar(), BorderLayout.SOUTH);
		
		//setting footer status bar label and separator
		getFooterStatusBar().add(getLabelTimeLapse());
		getFooterStatusBar().addSeparator();
		getFooterStatusBar().add(getLabelContentLength());
		getFooterStatusBar().addSeparator();
		getFooterStatusBar().add(getLabelTotalLength());
	}
	
	@Override
	public void setVisible(boolean show) {
		super.setVisible(show);
		
		switchToTab(0);
	}

	@Override
	public Class<? extends Message> getMessageType() {
		return HttpMessage.class;
	}

	@Override
	public Message getMessage() {
		return getRequestPanel().getMessage();
	}
	
	@Override
	public void setMessage(Message aMessage) {
		if (aMessage == null) {
			return;
		}
		
		getRequestPanel().setMessage(aMessage);
		getResponsePanel().setMessage(aMessage);
		setFooterStatus(null);
		switchToTab(0);
	}

	@Override
	protected MessageSender getMessageSender() {
		return sender;
	}
	
	@Override
	protected HttpPanelRequest getRequestPanel() {
		if (requestPanel == null) {
			requestPanel = new HttpPanelRequest(true, configurationKey);
			requestPanel.setEnableViewSelect(true);
			requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		return requestPanel;
	}
	
	private HttpPanelResponse getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new HttpPanelResponse(false, configurationKey);
			responsePanel.setEnableViewSelect(true);
			responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		return responsePanel;
	}

	@Override
	protected Component getManualSendPanel() {
		if (requestResponsePanel == null) {
			requestResponsePanel = new RequestResponsePanel(configurationKey, getRequestPanel(), getResponsePanel());
			
			if (helpKey != null) {
				JButton helpButton = new JButton();
				helpButton.setIcon(ExtensionHelp.HELP_ICON);
				helpButton.setToolTipText(Constant.messages.getString("help.dialog.button.tooltip"));
				helpButton.addActionListener(new java.awt.event.ActionListener() { 
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						ExtensionHelp.showHelp(helpKey);
					}
				});
				requestResponsePanel.addToolbarButton(helpButton);
			}
			
			requestResponsePanel.addEndButton(getBtnSend());
			requestResponsePanel.addSeparator();
	
			requestResponsePanel.loadConfig();
		}
		return requestResponsePanel;
	}

	@Override
	protected void btnSendAction() {		
		send(requestPanel.getMessage());
	}

	@Override
	protected void postSend() {
		super.postSend();
		
		switchToTab(1);
        setFooterStatus((HttpMessage) getResponsePanel().getMessage());
	}

	/**
	 * Return the footer status bar object
	 * @return
	 */
	protected JToolBar getFooterStatusBar() {
		if (footerToolbar == null) {
			footerToolbar = new JToolBar();
			footerToolbar.setEnabled(true);
			footerToolbar.setFloatable(false);
			footerToolbar.setRollover(true);
			footerToolbar.setName("Footer Toolbar Left");
			footerToolbar.setBorder(BorderFactory.createEtchedBorder());
		}
		return footerToolbar;
	}
	
	private void setFooterStatus(HttpMessage msg) {
		if (msg != null) {
			//get values
			long contentLength = msg.getResponseBody().length();
			long totalLength = msg.getResponseHeader().toString().length() + contentLength;
			long timeLapse =msg.getTimeElapsedMillis(); 
			// show time lapse and content length between request and response Constant.messages.getString("manReq.label.timeLapse")
			getLabelTimeLapse().setText(
					Constant.messages.getString("manReq.label.timeLapse") + String.valueOf(timeLapse) + " ms"); 
			getLabelContentLength().setText(
					Constant.messages.getString("manReq.label.contentLength") + String.valueOf(contentLength) + " " + Constant.messages.getString("manReq.label.totalLengthBytes"));
			getLabelTotalLength().setText(
					Constant.messages.getString("manReq.label.totalLength") + String.valueOf(totalLength) + " " + Constant.messages.getString("manReq.label.totalLengthBytes"));
		} else {
			getLabelTimeLapse().setText(Constant.messages.getString("manReq.label.timeLapse")); 
			getLabelContentLength().setText(Constant.messages.getString("manReq.label.contentLength"));
			getLabelTotalLength().setText(Constant.messages.getString("manReq.label.totalLength"));
		}
	}

	private void switchToTab(int i) {
		if (requestResponsePanel != null) {
			requestResponsePanel.switchToTab(i);
		}
	}

	@Override
	protected void saveConfig() {
		requestResponsePanel.saveConfig();
	}

	@Override
	public ZapMenuItem getMenuItem() {
		if (menuItem == null) {
			menuItem = new ZapMenuItem("menu.tools.manReq",
					KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Message message = getMessage();
					if (message == null) {
					    setDefaultMessage();
					} else if (message instanceof HttpMessage && ((HttpMessage)message).getRequestHeader().isEmpty()) {
						setDefaultMessage();
				    }
					setVisible(true);
				}
			});
		}
		return menuItem;
	}

	@Override
	public void clear() {
		super.clear();
		
		getResponsePanel().clearView();
	}

	@Override
	public void setDefaultMessage() {
		HttpMessage msg = new HttpMessage();
		try {
			URI uri = new URI("http://www.any_domain_name.org/path", true);
			msg.setRequestHeader(
					new HttpRequestHeader(HttpRequestHeader.GET, uri, HttpHeader.HTTP10,
							Model.getSingleton().getOptionsParam().getConnectionParam()));
			setMessage(msg);
		} catch (HttpMalformedHeaderException e) {
			logger.error(e.getMessage(), e);
		} catch (URIException e) {
		    logger.error(e.getMessage(), e);
        }
	}
	
    /**
     * Get Label status time lapse
     * @return
     */
	private JLabel getLabelTimeLapse(){
		if (labelTimeElapse == null){
			labelTimeElapse = new JLabel("", JLabel.LEADING);
		}
		return labelTimeElapse;
	}
	
	/**
     * Get Label status Content Length
     * @return
     */
	private JLabel getLabelContentLength(){
		if (labelContentLength == null){
			labelContentLength = new JLabel("", JLabel.LEADING);
		}
		return labelContentLength;
	}
	
	/**
     * Get Label status Total Length
     * @return
     */
	private JLabel getLabelTotalLength(){
		if (labelTotalLength == null){
			labelTotalLength = new JLabel("", JLabel.LEADING);
		}
		return labelTotalLength;
	}
	
	private static final class RequestResponsePanel extends JPanel {
		
		private static final String REQUEST_CAPTION = Constant.messages.getString("manReq.tab.request");
		private static final String RESPONSE_CAPTION = Constant.messages.getString("manReq.tab.response");

		private static final String TABS_VIEW_TOOL_TIP = Constant.messages.getString("manReq.display.tabs");
		private static final String ABOVE_VIEW_TOOL_TIP = Constant.messages.getString("manReq.display.above");
		private static final String SIDE_BY_SIDE_VIEW_TOOL_TIP = Constant.messages.getString("manReq.display.sidebyside");
		
		private static final String SELECTEDLAYOUT_CONFIG_KEY = "selectedlayout";
		private static final String HORIZONTAL_DIVIDER_LOCATION_CONFIG_KEY = "horizontalDividerLocation";
		private static final String VERTICAL_DIVIDER_LOCATION_CONFIG_KEY = "verticalDividerLocation";

		private static final long serialVersionUID = -3335708932021769432L;
		
		private static final int TABS_VIEW = 0;
		private static final int ABOVE_VIEW = 1;
		private static final int SIDE_BY_SIDE_VIEW = 2;
		
		private final HttpPanelRequest requestPanel;
		private final HttpPanelResponse responsePanel;
		
		private int currentView;
		private JComponent currentViewPanel;
		private JToggleButton currentButtonView;
		
		private JToggleButton tabsButtonView;
		private JToggleButton aboveButtonView;
		private JToggleButton sideBySideButtonView;
		
		private String configurationKey;
		
		private int verticalDividerLocation;
		private int horizontalDividerLocation;
	
		public RequestResponsePanel(String configurationKey, HttpPanelRequest request, HttpPanelResponse response) throws IllegalArgumentException {
			super(new BorderLayout());
			if (request == null || response == null) {
				throw new IllegalArgumentException("The request and response panels cannot be null.");
			}
			
			
			this.configurationKey = configurationKey;
	
			this.requestPanel = request;
			this.responsePanel = response;
			
			this.currentView = -1;
			
			tabsButtonView = new JToggleButton(new ImageIcon(ManualRequestEditorDialog.class.getResource("/resource/icon/layout_tabbed.png")));
			tabsButtonView.setToolTipText(TABS_VIEW_TOOL_TIP);
			
			tabsButtonView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeView(TABS_VIEW);
				}
			});
			
			addToolbarButton(tabsButtonView);
			
			aboveButtonView = new JToggleButton(new ImageIcon(ManualRequestEditorDialog.class.getResource("/resource/icon/layout_vertical_split.png")));
			aboveButtonView.setToolTipText(ABOVE_VIEW_TOOL_TIP);
			
			aboveButtonView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeView(ABOVE_VIEW);
				}
			});
			
			addToolbarButton(aboveButtonView);
			
			sideBySideButtonView = new JToggleButton(new ImageIcon(ManualRequestEditorDialog.class.getResource("/resource/icon/layout_horizontal_split.png")));
			sideBySideButtonView.setToolTipText(SIDE_BY_SIDE_VIEW_TOOL_TIP);
			
			sideBySideButtonView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeView(SIDE_BY_SIDE_VIEW);
				}
			});
			
			addToolbarButton(sideBySideButtonView);
		}
		
		public void loadConfig() {
			verticalDividerLocation = Model.getSingleton().getOptionsParam().getConfig().getInt(configurationKey + VERTICAL_DIVIDER_LOCATION_CONFIG_KEY, -1);
			horizontalDividerLocation = Model.getSingleton().getOptionsParam().getConfig().getInt(configurationKey + HORIZONTAL_DIVIDER_LOCATION_CONFIG_KEY, -1);
			
			changeView(Model.getSingleton().getOptionsParam().getConfig().getInt(configurationKey + SELECTEDLAYOUT_CONFIG_KEY, TABS_VIEW));
			
			requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
			responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		
		public void saveConfig() {
			switch(currentView) {
			case ABOVE_VIEW:
				verticalDividerLocation = ((JSplitPane)currentViewPanel).getDividerLocation();
				break;
			case SIDE_BY_SIDE_VIEW:
				horizontalDividerLocation = ((JSplitPane)currentViewPanel).getDividerLocation();
				break;
			default:
			}
			
			Model.getSingleton().getOptionsParam().getConfig().setProperty(configurationKey + VERTICAL_DIVIDER_LOCATION_CONFIG_KEY, Integer.valueOf(verticalDividerLocation));
			Model.getSingleton().getOptionsParam().getConfig().setProperty(configurationKey + HORIZONTAL_DIVIDER_LOCATION_CONFIG_KEY, Integer.valueOf(horizontalDividerLocation));
			
			Model.getSingleton().getOptionsParam().getConfig().setProperty(configurationKey + SELECTEDLAYOUT_CONFIG_KEY, Integer.valueOf(currentView));
			
			requestPanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
			responsePanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
	
		public void addToolbarButton(JToggleButton button) {
			requestPanel.addOptions(button, HttpPanel.OptionsLocation.AFTER_COMPONENTS);
		}
		
		public void addToolbarButton(JButton button) {
			requestPanel.addOptions(button, HttpPanel.OptionsLocation.AFTER_COMPONENTS);
		}
		
		public void addSeparator() {
			requestPanel.addOptionsSeparator();
		}
		
		public void addEndButton(JButton button) {
			requestPanel.addOptions(button, HttpPanel.OptionsLocation.END);
		}
	
		public void switchToTab(int i) {
			if (currentView == TABS_VIEW) {
				((JTabbedPane) currentViewPanel).setSelectedIndex(i);
			}
		}
	
		public void changeView(int newView) {
			if (newView != currentView) {
				final int oldView = currentView;
				currentView = newView;
				
				if (oldView != -1) {
					this.removeAll();
					currentButtonView.setSelected(false);
					
					switch(oldView) {
					case ABOVE_VIEW:
						verticalDividerLocation = ((JSplitPane)currentViewPanel).getDividerLocation();
						break;
					case SIDE_BY_SIDE_VIEW:
						horizontalDividerLocation = ((JSplitPane)currentViewPanel).getDividerLocation();
						break;
					default:
					}
				}
				
				switch (newView) {
				case TABS_VIEW:
					switchToTabsView();
					break;
				case ABOVE_VIEW:
					switchToAboveView();
					break;
				case SIDE_BY_SIDE_VIEW:
					switchToSideBySideView();
					break;
				default:
					switchToTabsView();
					break;
				}
				
				currentButtonView.setSelected(true);
				
				this.add(currentViewPanel);
				
				this.validate();
				this.repaint();
			}
		}
		
		private void switchToTabsView() {
			currentView = TABS_VIEW;
			currentButtonView = tabsButtonView;
			
			final JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.addTab(REQUEST_CAPTION, null, requestPanel, null);
			tabbedPane.addTab(RESPONSE_CAPTION, null, responsePanel, null);
			tabbedPane.setSelectedIndex(0);
			
			currentViewPanel = tabbedPane;
		}
		
		private void switchToAboveView() {
			currentView = ABOVE_VIEW;
			currentButtonView = aboveButtonView;
			
			currentViewPanel = createSplitPane(JSplitPane.VERTICAL_SPLIT);
		}
		
		private void switchToSideBySideView() {
			currentView = SIDE_BY_SIDE_VIEW;
			currentButtonView = sideBySideButtonView;
			
			currentViewPanel = createSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		}
		
		private JSplitPane createSplitPane(int orientation) {
			final JTabbedPane tabbedPaneRequest = new JTabbedPane();
			tabbedPaneRequest.addTab(REQUEST_CAPTION, null, requestPanel, null);
	
			final JTabbedPane tabbedPaneResponse = new JTabbedPane();
			tabbedPaneResponse.addTab(RESPONSE_CAPTION, null, responsePanel, null);
			
			final JSplitPane splitPane = new JSplitPane(orientation, tabbedPaneRequest, tabbedPaneResponse);
			splitPane.setDividerSize(3);
			splitPane.setResizeWeight(0.5d);
			splitPane.setContinuousLayout(false);
			splitPane.setDoubleBuffered(true);
			
			int dividerLocation;
			if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
				dividerLocation = horizontalDividerLocation;
			} else {
				dividerLocation = verticalDividerLocation;
			}
			splitPane.setDividerLocation(dividerLocation);
			
			return splitPane;
		}
	}
	
	public void addPersistentConnectionListener(PersistentConnectionListener listener) {
		((HttpPanelSender) getMessageSender()).addPersistentConnectionListener(listener);
	}

	public void removePersistentConnectionListener(PersistentConnectionListener listener) {
		((HttpPanelSender) getMessageSender()).removePersistentConnectionListener(listener);
	}
}
