/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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

package org.zaproxy.zap.extension.websocket.manualsend;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.extension.manualrequest.MessageSender;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.httppanel.HttpPanel;
import org.zaproxy.zap.extension.httppanel.HttpPanelRequest;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessageDTO;
import org.zaproxy.zap.extension.websocket.ui.ChannelSortedListModel;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiHelper;

/**
 * Send custom crafted WebSocket messages.
 */
public class ManualWebSocketSendEditorDialog extends ManualRequestEditorDialog {

	private static final long serialVersionUID = -5830450800029295419L;
//    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManualWebSocketSendEditorDialog.class);

	private JMenuItem menuItem;
	
	private WebSocketPanelSender sender;

	private HttpPanelRequest requestPanel;
	private WebSocketMessagePanel wsMessagePanel;
	private ChannelSortedListModel channelsModel;

	private JToolBar controlToolbar;
	
	public ManualWebSocketSendEditorDialog(ChannelSortedListModel channelsModel, WebSocketPanelSender sender, boolean isSendEnabled, String configurationKey) throws HeadlessException {
		super(isSendEnabled, configurationKey);

		this.channelsModel = channelsModel;
		this.sender = sender;
		
		initialize();
	}
	
	@Override
	protected void initialize() {
		super.initialize();

		getWindowPanel().add(getControlToolbar(), BorderLayout.NORTH);
	}

	private JToolBar getControlToolbar() {
		if (controlToolbar == null) {
			controlToolbar = new JToolBar();
			controlToolbar.setMargin(new Insets(5, 7, 5, 5));
			controlToolbar.setEnabled(true);
			controlToolbar.setFloatable(false);
			controlToolbar.setRollover(true);
			controlToolbar.setName("control_toolbar_top");
		}
		return controlToolbar;
	}

	@Override
	public Class<? extends Message> getMessageType() {
		return WebSocketMessageDTO.class;
	}

	@Override
	public Message getMessage() {
		WebSocketMessageDTO message = (WebSocketMessageDTO) getRequestPanel().getMessage();

		// set metadata first (opcode, channel, direction)
		wsMessagePanel.setMetadata(message);
		
		return message;
	}
	
	@Override
	public void setMessage(Message aMessage) {
		WebSocketMessageDTO message = (WebSocketMessageDTO) aMessage;
		if (message == null) {
			return;
		}
		
		getRequestPanel().setMessage(message);
		wsMessagePanel.setMessageMetadata(message);
	}

	@Override
	protected MessageSender getMessageSender() {
		return sender;
	}

	@Override
	protected HttpPanelRequest getRequestPanel() {
		if (requestPanel == null) {
			requestPanel = new WebSocketSendPanel(true, configurationKey);
			requestPanel.setEnableViewSelect(true);
			requestPanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		return requestPanel;
	}

	@Override
	protected Component getManualSendPanel() {
		if (wsMessagePanel == null) {
			wsMessagePanel = new WebSocketMessagePanel(channelsModel, getControlToolbar(), getRequestPanel());
			
			wsMessagePanel.addEndButton(getBtnSend());
			wsMessagePanel.addSeparator();
	
			wsMessagePanel.loadConfig();
		}
		return wsMessagePanel;
	}

	@Override
	protected void btnSendAction() {
		Message message = getMessage();
		send(message);
	}

	@Override
	protected void saveConfig() {
		wsMessagePanel.saveConfig();
	}

	@Override
	public JMenuItem getMenuItem() {
		if (menuItem == null) {
			menuItem = new JMenuItem();
			menuItem.setText(Constant.messages.getString("websocket.manual_send.menu"));
			menuItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Message message = getMessage();
					if (message == null) {
					    setDefaultMessage();
					} else if (message instanceof WebSocketMessageDTO && ((WebSocketMessageDTO)message).opcode == null) {
						setDefaultMessage();
				    }
					setVisible(true);
				}
			});
		}
		return menuItem;
	}

	@Override
	public void setDefaultMessage() {
		WebSocketMessageDTO msg = new WebSocketMessageDTO();
		msg.isOutgoing = true;
		msg.opcode = WebSocketMessage.OPCODE_TEXT;
		msg.readableOpcode = WebSocketMessage.opcode2string(msg.opcode);
		
		setMessage(msg);
	}
	
	private static final class WebSocketMessagePanel extends JPanel {

		private static final long serialVersionUID = -3335708932021769432L;
		
		private final HttpPanel messagePanel;

		private WebSocketUiHelper wsUiHelper;
	
		public WebSocketMessagePanel(ChannelSortedListModel channelsModel, JToolBar controlToolbar, HttpPanel messagePanel) throws IllegalArgumentException {
			super(new BorderLayout());
			if (messagePanel == null) {
				throw new IllegalArgumentException("The request panel cannot be null.");
			}
			
			// Could also add Input Field for new WebSocket channel with possibility
			// to set Origin header to some custom value
				
			this.messagePanel = messagePanel;
			
			wsUiHelper = new WebSocketUiHelper();
			wsUiHelper.setChannelsModel(channelsModel);
			
			controlToolbar.add(wsUiHelper.getChannelLabel());
			controlToolbar.add(wsUiHelper.getChannelSingleSelect());
			wsUiHelper.getChannelSingleSelect().setSelectedIndex(0);
			
			controlToolbar.addSeparator(new Dimension(15, 21));

			controlToolbar.add(wsUiHelper.getOpcodeLabel());
			controlToolbar.add(wsUiHelper.getOpcodeSingleSelect());
			wsUiHelper.getOpcodeSingleSelect().setSelectedItem(WebSocketMessage.opcode2string(WebSocketMessage.OPCODE_TEXT)); // set TEXT per default

			controlToolbar.addSeparator(new Dimension(15, 21));
			
			controlToolbar.add(wsUiHelper.getDirectionLabel());
			controlToolbar.add(wsUiHelper.getDirectionSingleSelect());
		}
		
		public void setMessageMetadata(WebSocketMessageDTO message) {
			if (message.channel != null && message.channel.id != null) {
				wsUiHelper.getChannelSingleSelect().setSelectedItem(message.channel);
			}
			
			if (message.opcode != null) {
				wsUiHelper.getOpcodeSingleSelect().setSelectedItem(message.readableOpcode);
			}
			
			if (message.isOutgoing != null) {
				wsUiHelper.setDirectionSingleSelect(message.isOutgoing);
			}
		}

		public void setMetadata(WebSocketMessageDTO msg) {
			msg.channel = wsUiHelper.getSelectedChannelDTO();
			msg.isOutgoing = wsUiHelper.isDirectionSingleSelectOutgoing();
			msg.opcode = wsUiHelper.getSelectedOpcodeInteger();
		}

		public void loadConfig() {
			messagePanel.loadConfig(Model.getSingleton().getOptionsParam().getConfig());
			add(messagePanel);			
		}

		public void saveConfig() {
			messagePanel.saveConfig(Model.getSingleton().getOptionsParam().getConfig());
		}
		
		public void addSeparator() {
			messagePanel.addOptionsSeparator();
		}
		
		public void addEndButton(JButton button) {
			messagePanel.addOptions(button, HttpPanel.OptionsLocation.END);
		}
	}
}