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
//

package org.parosproxy.paros.extension.manualrequest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.extension.option.OptionsParamView;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractFrame;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.tab.Tab;


/**
 *
 * Creates:
 *
 * +----------------------------+
 * | panelHeader                |
 * +----------------------------+
 * | panelContent               |
 * |                            |
 * |                            |
 * +----------------------------+
 *
 */
public class ManualRequestEditorDialog extends AbstractFrame implements Tab {
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(ManualRequestEditorDialog.class);

	// Window
	private JPanel panelWindow = null; // ZAP
//	private JPanel panelHeader = null;

	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;

//	private JComponent panelMain = null;
//	private JPanel panelContent = null;

	private JToggleButton followRedirect = null;
	private JToggleButton useTrackingSessionState = null;
	//private JComboBox comboChangeMethod = null;

	private JButton btnSend = null;

	// Other
	private HttpSender httpSender = null;
	private boolean isSendEnabled = true;

	private HistoryList historyList = null;
	private Extension extension = null;
	private HttpMessage httpMessage = null;
	
	private String configurationKey;
	
	private RequestResponsePanel requestResponsePanel;
	
	/**
	 * @param parent
	 * @param modal
	 * @throws HeadlessException
	 */
	public ManualRequestEditorDialog(Frame parent, boolean modal, boolean isSendEnabled, Extension extension, String configurationKey) throws HeadlessException {
		super();
		this.isSendEnabled = isSendEnabled;
		this.extension = extension;
		
		this.configurationKey = OptionsParamView.BASE_VIEW_KEY + "." + configurationKey + ".";
		
		this.setPreferredSize(new Dimension(700, 800));
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		
		requestResponsePanel = new RequestResponsePanel(configurationKey, getRequestPanel(), getResponsePanel());
		
		requestResponsePanel.addEndButton(getBtnSend());
		requestResponsePanel.addSeparator();
		requestResponsePanel.addToolbarButton(getButtonUseTrackingSessionState());
		requestResponsePanel.addToolbarButton(getButtonFollowRedirect());
		
		requestResponsePanel.loadConfig();
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				getHttpSender().shutdown();
				saveConfig();
			}
		});

		this.setContentPane(getWindowPanel());
		this.historyList = ((ExtensionHistory)Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory")).getHistoryList();
	}

	private JPanel getWindowPanel() {
		if (panelWindow == null) {
			panelWindow = new JPanel();
			panelWindow.setLayout(new BorderLayout());

			panelWindow.add(requestResponsePanel);
		}

		return panelWindow;
	}

	/**
	 * This method initializes requestPanel
	 *
	 * @return org.parosproxy.paros.view.HttpPanel
	 */
	private HttpPanelRequest getRequestPanel() {
		if (requestPanel == null) {
			requestPanel = new HttpPanelRequest(true, extension, httpMessage, configurationKey);
			requestPanel.setEnableViewSelect(true);
			requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		return requestPanel;
	}

	/**
	 * This method initializes httpPanel
	 *
	 * @return org.parosproxy.paros.view.HttpPanel
	 */
	private HttpPanelResponse getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new HttpPanelResponse(false, extension, httpMessage, configurationKey);
			responsePanel.setEnableViewSelect(true);
			responsePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		return responsePanel;
	}

	public void setExtension(Extension extension) {
		requestPanel.setExtension(extension);
		responsePanel.setExtension(extension);
	}

	@Override
	public void setVisible(boolean show) {
		if (show) {
			try {
				if (httpSender != null) {
					httpSender.shutdown();
					httpSender = null;
				}
			} catch (final Exception e) {
				// ZAP: Log exceptions
				log.error(e.getMessage(), e);
			}
		}

		switchToTab(0);

		final boolean isSessionTrackingEnabled = Model.getSingleton().getOptionsParam().getConnectionParam().isHttpStateEnabled();
		getButtonUseTrackingSessionState().setEnabled(isSessionTrackingEnabled);
		super.setVisible(show);

	}

	private HttpSender getHttpSender() {
		if (httpSender == null) {
			httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), getButtonUseTrackingSessionState().isSelected());

		}
		return httpSender;
	}

	/* Set new HttpMessage
	 * this means ManualRequestEditor does show another HttpMessage.
	 * Copy the message (this is not a viewer. User will modify it),
	 * and update Request/Response views.
	 */
	public void setMessage(HttpMessage msg) {
		if (msg == null) {
			System.out.println("Manual: set message NULL");
			return;
		}

		this.httpMessage = msg.cloneAll();

		getRequestPanel().setMessage(httpMessage);
		getResponsePanel().setMessage(httpMessage);
		switchToTab(0);
	}

	public HttpMessage getHttpMessage() {
		return httpMessage;
	}

	public void setHttpMessage(HttpMessage httpMessage) {
		setMessage(httpMessage);
	}
	
	public void clear() {
		requestPanel.clearView();
		responsePanel.clearView();
	}

	/**
	 * This method initializes followRedirect
	 *
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getButtonFollowRedirect() {
		if (followRedirect == null) {
			followRedirect = new JToggleButton(new ImageIcon(getClass().getResource("/resource/icon/16/118.png"))); // Arrow turn around left
			followRedirect.setToolTipText(Constant.messages.getString("manReq.checkBox.followRedirect"));
			followRedirect.setSelected(true);
		}
		return followRedirect;
	}

	/**
	 * This method initializes useTrackingSessionState
	 *
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getButtonUseTrackingSessionState() {
		if (useTrackingSessionState == null) {
			useTrackingSessionState = new JToggleButton(new ImageIcon(getClass().getResource("/resource/icon/fugue/cookie.png"))); // Cookie
			useTrackingSessionState.setToolTipText(Constant.messages.getString("manReq.checkBox.useSession"));
		}
		return useTrackingSessionState;
	}

	private void addHistory(HttpMessage msg, int type) {
		HistoryReference historyRef = null;
		try {
			historyRef = new HistoryReference(Model.getSingleton().getSession(), type, msg);
			synchronized (historyList) {
				if (type == HistoryReference.TYPE_MANUAL) {
					addHistoryInEventQueue(historyRef);
					historyList.notifyItemChanged(historyRef);
				}
			}
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void addHistoryInEventQueue(final HistoryReference ref) {
		if (EventQueue.isDispatchThread()) {
			historyList.addElement(ref);
		} else {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						historyList.addElement(ref);
					}
				});
			} catch (final Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * This method initializes btnSend
	 *
	 * @return javax.swing.JButton
	 */
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

	private void btnSendAction() {
		btnSend.setEnabled(false);

		// Get current HttpMessage
		requestPanel.saveData();
		final HttpMessage msg = requestPanel.getHttpMessage();
		msg.getRequestHeader().setContentLength(msg.getRequestBody().length());

		// Send Request, Receive Response
		send(msg);

		// redraw request, as it could have changed
		requestPanel.updateContent();
	}

	private void send(final HttpMessage msg) {
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					getHttpSender().sendAndReceive(msg, getButtonFollowRedirect().isSelected());

					EventQueue.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							if (!msg.getResponseHeader().isEmpty()) {
								// Indicate UI new response arrived
								switchToTab(1);
								responsePanel.updateContent();

								final int finalType = HistoryReference.TYPE_MANUAL;
								final Thread t = new Thread(new Runnable() {
									@Override
									public void run() {
										addHistory(msg, finalType);
									}
								});
								t.start();
							}
						}
					});
				} catch (final HttpMalformedHeaderException mhe) {
					requestPanel.getExtension().getView().showWarningDialog("Malformed header error.");
				} catch (final IOException ioe) {
					requestPanel.getExtension().getView().showWarningDialog("IO error in sending request.");
				} catch (final Exception e) {
					// ZAP: Log exceptions
					log.error(e.getMessage(), e);
				} finally {
					btnSend.setEnabled(true);
				}
			}
		});
		t.setPriority(Thread.NORM_PRIORITY);
		t.start();
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
			
			tabsButtonView = new JToggleButton(new ImageIcon(getClass().getResource("/resource/icon/layout_tabbed.png")));
			tabsButtonView.setToolTipText(TABS_VIEW_TOOL_TIP);
			
			tabsButtonView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeView(TABS_VIEW);
				}
			});
			
			addToolbarButton(tabsButtonView);
			
			aboveButtonView = new JToggleButton(new ImageIcon(getClass().getResource("/resource/icon/layout_vertical_split.png")));
			aboveButtonView.setToolTipText(ABOVE_VIEW_TOOL_TIP);
			
			aboveButtonView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeView(ABOVE_VIEW);
				}
			});
			
			addToolbarButton(aboveButtonView);
			
			sideBySideButtonView = new JToggleButton(new ImageIcon(getClass().getResource("/resource/icon/layout_horizontal_split.png")));
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