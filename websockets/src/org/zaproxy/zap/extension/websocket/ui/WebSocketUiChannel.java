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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Adjustable;
import java.awt.EventQueue;
import java.awt.LayoutManager2;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketObserver;
import org.zaproxy.zap.extension.websocket.WebSocketProxy;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiModel.WebSocketMessageDAO;

/**
 * Manages the user interface for one {@link WebSocketProxy}. It listens to the
 * channel and prepares them via its given {@link WebSocketUiModel} for
 * display in the UI.
 */
public class WebSocketUiChannel extends JScrollPane implements WebSocketObserver {
	private static final long serialVersionUID = 623982753305523793L;

	private static final Logger logger = Logger.getLogger(WebSocketUiChannel.class);
	
	/**
	 * Just observe it somewhere and let others catch them too.
	 */
    public static final int WEBSOCKET_OBSERVING_ORDER = 100;

    /**
     * The width of the messages.
     */
	int width = 300;
	
	/**
	 * Counts the number of messages that are currently displayed.
	 */
	private int messagesInContainer;
	
	/**
	 * This component will be used for positioning the next one.
	 */
	private JComponent lastAddedMessageComponent;
	
	/**
	 * Used to retrieve {@link WebSocketMessageDAO} for display.
	 */
	private WebSocketUiModel model;
	
	/**
	 * This pane is added to this scrollable container and used as parent for
	 * all displayed WebSocket messages.
	 */
	final private JPanel messagesContainer;

	private SpringLayout layout;

	public WebSocketUiChannel(WebSocketUiModel viewModel) {
		super();

		model = viewModel;
		messagesInContainer = 0;
		layout = new SpringLayout();
		messagesContainer = createMessagesContainer(layout);
		
		// the first point of reference is the messagesContainer itself
		lastAddedMessageComponent = messagesContainer;
		
		// add the messagesContainer to the scrollable
		setViewportView(messagesContainer);
		setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	}
	
	/**
	 * Catch WebSockets communication and collect frames for user interface.
	 */
	@Override
	public boolean onMessageFrame(WebSocketMessage message) {
		boolean isAtBottom = isAtBottom();
		
		model.addWebSocketMessage(message);
		
		updateView();
		
		if (isAtBottom) {
			// scroll to bottom if at bottom before
			scrollToBottom();
		}
		
		return true;
	}

	@Override
	public int getObservingOrder() {
		return WEBSOCKET_OBSERVING_ORDER;
	}

	/**
	 * 
	 * @return True if scrollbar at the bottom, indicating to stick to it.
	 */
	public boolean isAtBottom() {
		Adjustable scrollbar = getVerticalScrollBar();
		int lowest = scrollbar.getValue() + scrollbar.getVisibleAmount();
		return (lowest == scrollbar.getMaximum());
	}

	/**
	 * This method scrolls to the bottom. It does so by using two
	 * {@link EventQueue} objects. I got the tip from 
	 * http://binfalse.de/2012/04/conditionally-autoscroll-a-jscrollpane/.
	 */
	public void scrollToBottom() {
		try {
			final Adjustable scrollbar = this.getVerticalScrollBar();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							scrollbar.setValue(scrollbar.getMaximum());
						}
					});
				}
			});
		} catch (Exception e) {
			logger.warn(e);
		}
	}
	
	/**
	 * Returns a {@link JPanel} that is used to position all WebSocket messages.
	 * 
	 * @return
	 */
	public JPanel createMessagesContainer(LayoutManager2 layout) {
		JPanel messagesPane = new JPanel();
		messagesPane.setLayout(layout);
		return messagesPane;
	}
	
	/**
	 * First, removes all messages. Then it adds them again.
	 */
	public void updateView() {
		List<WebSocketMessageDAO> messages = model.getMessages().subList(messagesInContainer, model.getMessages().size());
		SpringLayout springLayout = (SpringLayout) messagesContainer.getLayout();

		int left = 10;
//		int middle = 10;
		int right = 10;
		int top = 40;
		
		for (WebSocketMessageDAO message : messages) {
			JComponent messagePane = createMessagePane(message.payload);
			
			switch (message.direction) {
			case OUTGOING:
				// displayed on the left side

				// message goes onto the left side of the pane
				springLayout.putConstraint(SpringLayout.WEST, messagePane, left, SpringLayout.WEST, messagesContainer);
				
				// message has got some width
				springLayout.putConstraint(SpringLayout.EAST, messagePane, width, SpringLayout.WEST, messagePane);
				break;
			
			case INCOMING:
				// displayed on the right side
				
				// message goes onto the right side of the pane
				springLayout.putConstraint(SpringLayout.EAST, messagePane, -right, SpringLayout.EAST, messagesContainer);
				
				// message has got some width
				springLayout.putConstraint(SpringLayout.WEST, messagePane, -width, SpringLayout.EAST, messagePane);
				break;
				
			default:
				logger.error("Given direction does not exist ('" + message.direction + "'!");
				break;
			}
			
			// message goes below old message (or from the top if none displayed yet)
			String position = (messagesInContainer > 0) ? SpringLayout.SOUTH : SpringLayout.NORTH;
			springLayout.putConstraint(SpringLayout.NORTH, messagePane, top, position, lastAddedMessageComponent);
			
			messagesContainer.add(messagePane);
			messagesInContainer++;
			lastAddedMessageComponent = messagePane;

			// add further elements
			addTimestamp(message.timestamp);
			addConsecutiveNumber(message.id);
			addOpcode(message.opcode);
			addLength(message.payloadLength);
			
			// TODO: without this constraint, scrolling is not possible, BUT
			// the first editorPanes are displayed with full height :-(
			springLayout.putConstraint(SpringLayout.SOUTH, messagesContainer, 0, SpringLayout.SOUTH, lastAddedMessageComponent);
		}

		// after that call, view is updated
		messagesContainer.revalidate();
	}

	/**
	 * Adds & positions the given timestamp.
	 * 
	 * @param timestamp
	 */
	private void addTimestamp(String timestamp) {
		JLabel label = new JLabel(timestamp);
		label.setToolTipText(Constant.messages.getString("http.panel.websocket.timestamp.help"));
		
		// align with the left side of the message
		layout.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.WEST, lastAddedMessageComponent);
		
		// align above the label, leaving some space for other elements
		layout.putConstraint(SpringLayout.SOUTH, label, -15, SpringLayout.NORTH, lastAddedMessageComponent);
		
		messagesContainer.add(label);
	}

	/**
	 * Indicates the number of messages.
	 * 
	 * @param id
	 */
	private void addConsecutiveNumber(String id) {
		JLabel label = new JLabel(id);
		label.setToolTipText(Constant.messages.getString("http.panel.websocket.id.help"));
		
		// align with the left side of the message
		layout.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.WEST, lastAddedMessageComponent);
		
		// align above the label
		layout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.NORTH, lastAddedMessageComponent);
		
		messagesContainer.add(label);
	}

	/**
	 * Indicates the number of messages.
	 * 
	 * @param id
	 */
	private void addOpcode(String opcode) {
		JLabel label = new JLabel(opcode);
		label.setToolTipText(Constant.messages.getString("http.panel.websocket.opcode.help"));
		
		// move to the middle of the text message
		layout.putConstraint(SpringLayout.WEST, label, width/2 - 25, SpringLayout.WEST, lastAddedMessageComponent);
		
		// align above the label
		layout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.NORTH, lastAddedMessageComponent);
		
		messagesContainer.add(label);
	}

	/**
	 * Indicates the number of payload bytes.
	 * 
	 * @param payloadLength
	 */
	private void addLength(String payloadLength) {
		JLabel label = new JLabel(payloadLength);
		label.setToolTipText(Constant.messages.getString("http.panel.websocket.payload_length.help"));
		
		// align with the right side of the message
		layout.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.EAST, lastAddedMessageComponent);
		
		// align above the label
		layout.putConstraint(SpringLayout.SOUTH, label, 0, SpringLayout.NORTH, lastAddedMessageComponent);
		
		messagesContainer.add(label);
	}

	/**
	 * Returns the Swing component that is used to display the WebSocket message.
	 * 
	 * @param text
	 * @return
	 */
	private JComponent createMessagePane(String text) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setText(text);
		editorPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		editorPane.setSize(300, Integer.MAX_VALUE);
		return editorPane;
	}
}
