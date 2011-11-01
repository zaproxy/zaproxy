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

package org.parosproxy.paros.extension.manualrequest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryList;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractFrame;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.extension.tab.Tab;

// ZAP: 2011/08/04 Changed to support new interface

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
	private JPanel panelHeader = null;

	private HttpPanelRequest requestPanel = null;
	private HttpPanelResponse responsePanel = null;

	private JComponent panelMain = null;
	private JPanel panelContent = null;

	private JCheckBox chkFollowRedirect = null;
	private JCheckBox chkUseTrackingSessionState = null;
	private JComboBox comboChangeMethod = null;

	private JButton btnSend = null;

	// Other
	private HttpSender httpSender = null;
	private boolean isSendEnabled = true;

	private HistoryList historyList = null;
	private Extension extension = null;
	private HttpMessage httpMessage = null;

	/**
	 * @param arg0
	 * @param arg1
	 * @throws HeadlessException
	 */
	public ManualRequestEditorDialog(Frame parent, boolean modal, boolean isSendEnabled, Extension extension) throws HeadlessException {
		super();
		this.isSendEnabled = isSendEnabled;
		this.extension = extension;
		this.setPreferredSize(new Dimension(700, 800));
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.requestPanel = getRequestPanel();

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				getHttpSender().shutdown();
				getResponsePanel().clearView(false);
			}
		});

		this.setContentPane(getWindowPanel());
		this.historyList = ((ExtensionHistory)Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory")).getHistoryList();
	}

	private JPanel getWindowPanel() {
		if (panelWindow == null) {
			panelWindow = new JPanel();
			panelWindow.setLayout(new BorderLayout());

			panelHeader = getPanelHeader();
			panelWindow.add(panelHeader, BorderLayout.NORTH);

			panelContent = getPanelContent();
			panelWindow.add(panelContent);
		}

		return panelWindow;
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setLayout(new BorderLayout());

			final JPanel panelLeft = new JPanel();
			final JPanel panelMiddle = new JPanel();
			final JPanel panelRight = new JPanel();

			panelLeft.add(getBtnSend());
			panelRight.add(getChkUseTrackingSessionState());
			panelRight.add(getChkFollowRedirect());

			panelHeader.add(panelLeft, BorderLayout.LINE_START);
			panelHeader.add(panelMiddle, BorderLayout.CENTER);
			panelHeader.add(panelRight, BorderLayout.LINE_END);
		}

		return panelHeader;
	}

	/**
	 * This method initializes requestPanel
	 *
	 * @return org.parosproxy.paros.view.HttpPanel
	 */
	private HttpPanelRequest getRequestPanel() {
		if (requestPanel == null) {

			requestPanel = new HttpPanelRequest(true, extension, httpMessage);
		}
		return requestPanel;
	}

	/**
	 * This method initializes jTabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JComponent getPanelTab() {
		JSplitPane splitPane = null;
		Dimension frameSize = null;

		if (panelMain == null) {
			switch(Model.getSingleton().getOptionsParam().getViewParam().getEditorViewOption()) {
			case 0:
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getRequestPanel(), getResponsePanel());
				this.panelMain = splitPane;
				panelMain.setDoubleBuffered(true);

				frameSize = this.getSize();
				splitPane.setDividerLocation(frameSize.height / 2);

				break;

			case 1:
				splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getRequestPanel(), getResponsePanel());
				this.panelMain = splitPane;
				panelMain.setDoubleBuffered(true);

				frameSize = this.getSize();
				splitPane.setDividerLocation(frameSize.width / 2);

				this.panelMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getRequestPanel(), getResponsePanel());
				panelMain.setDoubleBuffered(true);
				break;

			case 2:
				final JTabbedPane tabbedPane = new JTabbedPane();
				tabbedPane.addTab(Constant.messages.getString("manReq.tab.request"), null, getRequestPanel(), null);
				tabbedPane.addTab(Constant.messages.getString("manReq.tab.response"), null, getResponsePanel(), null);
				this.panelMain = tabbedPane;
				break;

			default:
				this.panelMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getRequestPanel(), getResponsePanel());
				panelMain.setDoubleBuffered(true);
			}
		}

		return panelMain;
	}

	/**
	 * This method initializes httpPanel
	 *
	 * @return org.parosproxy.paros.view.HttpPanel
	 */
	private HttpPanelResponse getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new HttpPanelResponse(true, extension, httpMessage);
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
		getChkUseTrackingSessionState().setEnabled(isSessionTrackingEnabled);
		super.setVisible(show);

	}

	private HttpSender getHttpSender() {
		if (httpSender == null) {
			httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(), getChkUseTrackingSessionState().isSelected());

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

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelContent() {
		if (panelContent == null) {
			final GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			panelContent = new JPanel();
			panelContent.setLayout(new GridBagLayout());
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHWEST;
			panelContent.add(getPanelTab(), gridBagConstraints31);
		}
		return panelContent;
	}

	/**
	 * This method initializes chkFollowRedirect
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkFollowRedirect() {
		if (chkFollowRedirect == null) {
			chkFollowRedirect = new JCheckBox();
			chkFollowRedirect.setText(Constant.messages.getString("manReq.checkBox.followRedirect"));
			chkFollowRedirect.setSelected(true);
		}
		return chkFollowRedirect;
	}

	/**
	 * This method initializes jCheckBox
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkUseTrackingSessionState() {
		if (chkUseTrackingSessionState == null) {
			chkUseTrackingSessionState = new JCheckBox();
			chkUseTrackingSessionState.setText(Constant.messages.getString("manReq.checkBox.useSession"));
		}
		return chkUseTrackingSessionState;
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
	 * This method initializes jButton
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

		 btnSend.setEnabled(true);
	 }

	 private void send(final HttpMessage msg) {
		 final Thread t = new Thread(new Runnable() {
			 @Override
			 public void run() {
				 try {
					 getHttpSender().sendAndReceive(msg, getChkFollowRedirect().isSelected());

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
				 } catch (final NullPointerException npe) {
					 requestPanel.getExtension().getView().showWarningDialog("Malformed header error.");
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
		 if (Model.getSingleton().getOptionsParam().getViewParam().getEditorViewOption() == 2) {
			 final JTabbedPane tab = (JTabbedPane) getPanelTab();
			 tab.setSelectedIndex(i);
		 }
	 }
}