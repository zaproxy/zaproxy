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

// ZAP: 2011/08/04 Changed to support new Features
// ZAP: 2011/08/04 Changed to support new interface
// ZAP: 2012/03/15 Changed so the display options can be modified dynamically.
// ZAP: 2012/07/02 Wraps no HttpMessage object, but more generalized Message.
// new map of supported message types; removed history list; removed unused
// methods.
// ZAP: 2012/07/16 Issue 326: Add response time and total length to manual request dialog 
// ZAP: 2012/07/31 Removed the instance variables followRedirect,
// useTrackingSessionState and httpSender. Removed the methods getHttpSender,
// getButtonFollowRedirect and getButtonUseTrackingSessionState and changed the
// methods windowClosing and setVisible.
// ZAP: 2012/08/01 Issue 332: added support for Modes

package org.parosproxy.paros.extension.manualrequest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.manualrequest.http.impl.HttpPanelSender;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.tab.Tab;



public class ManualRequestEditorDialog extends AbstractFrame implements Tab {
	private static final long serialVersionUID = 1L;

	// Window
	private JPanel panelWindow = null; // ZAP
//	private JPanel panelHeader = null;

	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;

//	private JComponent panelMain = null;
//	private JPanel panelContent = null;
	
	// ZAP: Removed the instance variables "JToggleButton followRedirect". and
	// "JToggleButton useTrackingSessionState".
	
	//private JComboBox comboChangeMethod = null;

	private JButton btnSend = null;
	
	// ZAP: Removed the instance variable "HttpSender httpSender".
	
	private boolean isSendEnabled = true;

	private Extension extension = null;
	
	// ZAP: Use more general class than HttpMessage
	private Message message = null;
	
	private String configurationKey;
	
	private RequestResponsePanel requestResponsePanel;

	// ZAP: introduced map of supported message types
    private Map<Class<? extends Message>, MessageSender> mapMessageSenders;

	private static JLabel labelTimeElapse = null;
	private static JLabel labelContentLength = null;
	private static JLabel labelTotalLength = null;
	private static JToolBar footerToolbar = null;
	
	
	public ManualRequestEditorDialog(Frame parent, boolean modal, boolean isSendEnabled, Extension extension, String configurationKey) throws HeadlessException {
		super();
		this.isSendEnabled = isSendEnabled;
		this.extension = extension;
		
		this.configurationKey = OptionsParamView.BASE_VIEW_KEY + "." + configurationKey + ".";
		
		this.setPreferredSize(new Dimension(700, 800));
		initialize();
        
        mapMessageSenders = new HashMap<Class<? extends Message>, MessageSender>();
        mapMessageSenders.put(HttpMessage.class, new HttpPanelSender(getRequestPanel(), getResponsePanel()));
	}

	
	private void initialize() {
		
		requestResponsePanel = new RequestResponsePanel(configurationKey, getRequestPanel(), getResponsePanel());
		
		requestResponsePanel.addEndButton(getBtnSend());
		requestResponsePanel.addSeparator();

		requestResponsePanel.loadConfig();
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				// ZAP: Changed to call the method cleanup on the MessageSender.
				for (Iterator<MessageSender> it = mapMessageSenders.values().iterator(); it.hasNext();) {
                    it.next().cleanup();
                }
				saveConfig();
			}
		});

		//setting footer status bar label and separator
		getFooterStatusBar().add(getLabelTimeLapse());
		getFooterStatusBar().addSeparator();
		getFooterStatusBar().add(getLabelContentLength());
		getFooterStatusBar().addSeparator();
		getFooterStatusBar().add(getLabelTotalLength());
		
		this.setContentPane(getWindowPanel());
	}

	private JPanel getWindowPanel() {
		if (panelWindow == null) {
			panelWindow = new JPanel();
			panelWindow.setLayout(new BorderLayout());

			panelWindow.add(requestResponsePanel);
			// add footer status bar
			panelWindow.add(getFooterStatusBar(), BorderLayout.SOUTH);
		}

		return panelWindow;
	}

	
	private HttpPanelRequest getRequestPanel() {
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
	
	// ZAP: Removed the method setExtension(Extension), not used anymore.

	@Override
	public void setVisible(boolean show) {
		// ZAP: Changed to call the method cleanup on the MessageSender.
		if (!show && mapMessageSenders != null) {
            for (Iterator<MessageSender> it = mapMessageSenders.values().iterator(); it.hasNext();) {
                it.next().cleanup();
            }
		}

		switchToTab(0);
		
		super.setVisible(show);
	}

	// ZAP: Removed the method "HttpSender getHttpSender()".

	/* Set new HttpMessage
	 * this means ManualRequestEditor does show another HttpMessage.
	 * Copy the message (this is not a viewer. User will modify it),
	 * and update Request/Response views.
	 */
	
	public void setMessage(Message aMessage) {
		if (aMessage == null) {
			return;
		}

		this.message = aMessage; // .cloneAll();

		getRequestPanel().setMessage(aMessage);
		getResponsePanel().setMessage(aMessage);
		//reload footer status
		setFooterStatus(null);
		switchToTab(0);
	}

	public Message getMessage() {
		return message;
	}

	public void clear() {
		requestPanel.clearView();
		responsePanel.clearView();
	}

	// ZAP: Removed the methods "JToggleButton getButtonFollowRedirect()" and
	// "JToggleButton getButtonUseTrackingSessionState()".
	
	private JButton getBtnSend() {
		if (btnSend == null) {
			btnSend = new JButton();
			btnSend.setText(Constant.messages.getString("manReq.button.send"));		// ZAP: i18n
			btnSend.setEnabled(isSendEnabled);
			btnSend.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					btnSendAction();
				}
			});
		}
		return btnSend;
	}
    /**
     * Get Label status time lapse
     * @return
     */
	private JLabel getLabelTimeLapse(){
		if (labelTimeElapse==null){
			labelTimeElapse = new JLabel("", JLabel.LEADING);
		}
		return labelTimeElapse;
	}
	 /**
     * Get Label status Content Length
     * @return
     */
	private JLabel getLabelContentLength(){
		if (labelContentLength==null){
			labelContentLength = new JLabel("", JLabel.LEADING);
		}
		return labelContentLength;
	}
	 /**
     * Get Label status Total Length
     * @return
     */
	private JLabel getLabelTotalLength(){
		if (labelTotalLength==null){
			labelTotalLength = new JLabel("", JLabel.LEADING);
		}
		return labelTotalLength;
	}
	private void btnSendAction() {
		btnSend.setEnabled(false);

		// Save current Message
		requestPanel.saveData();

		// Send Request, Receive Response
		if (Control.getSingleton().getMode().equals(Mode.safe)) {
			// Can happen if the user turns on safe mode with the dialog open
			View.getSingleton().showWarningDialog(Constant.messages.getString("manReq.safe.warning"));
			btnSend.setEnabled(true);
			return;
		} else if (Control.getSingleton().getMode().equals(Mode.protect)) {
			if (! requestPanel.getMessage().isInScope()) {
				// In protected mode and not in scope, so fail
				View.getSingleton().showWarningDialog(Constant.messages.getString("manReq.outofscope.warning"));
				btnSend.setEnabled(true);
				return;
			}
		}
		send(requestPanel.getMessage());

		// redraw request, as it could have changed
		requestPanel.updateContent();
		
	}

	/**
	 * Return the footer status bar object
	 * @return
	 */
	private JToolBar getFooterStatusBar() {
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
	
	/**
	 * Set footer status bar
	 * @param msg
	 */
	private void setFooterStatus(HttpMessage msg){
		if (msg != null) {
			//get values
			long totalLength = msg.getResponseBody().toString().length()+msg.getResponseHeader().getHeadersAsString().length();
			long contentLength = msg.getResponseBody().toString().length();
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

	private void send(final Message aMessage) {
	    final MessageSender sender = mapMessageSenders.get(aMessage.getClass());
	    if (sender != null) {
	        final Thread t = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    sender.handleSendMessage(aMessage);
	                    // FIXME change to the HttpPanelSender
                        switchToTab(1);
                        
                        setFooterStatus((HttpMessage) getResponsePanel().getMessage());
	                } catch (Exception e) {
	                    extension.getView().showWarningDialog(e.getMessage());
                    } finally {
                        btnSend.setEnabled(true);
                    }
	            }
            });
			t.setPriority(Thread.NORM_PRIORITY);
			t.start();
	    }
	}

	private void switchToTab(int i) {
		if (requestResponsePanel != null) {
			requestResponsePanel.switchToTab(i);
		}
	}
	
	private void saveConfig() {
		requestResponsePanel.saveConfig();
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
}