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
package org.zaproxy.zap.extension.websocket.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.filter.FilterReplaceDialog;
import org.zaproxy.zap.extension.websocket.ui.ChannelSortedListModel;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiHelper;

/**
 * Extended replace dialog with WebSocket specific options.
 */
public class FilterWebSocketReplaceDialog extends FilterReplaceDialog {
	private static final long serialVersionUID = -1156304397855108677L;

	private WebSocketUiHelper wsUiHelper;

	private JPanel jPanel;
	
	/**
	 * Ctor.
	 * 
	 * @param owner Pass e.g. the mainframe.
	 * @param isModal
	 */
	public FilterWebSocketReplaceDialog(Frame owner, boolean isModal, ChannelSortedListModel model) {
		super(owner, isModal);
		
		wsUiHelper = new WebSocketUiHelper();
		wsUiHelper.setChannelsModel(model);
		
		postInitialize();
	}

	@Override
	protected void initialize() {
		// do nothing
	}

	private void postInitialize() {
		
        setContentPane(getJPanel());
		pack();
	}

	/**
	 * Adds WebSocket related filter options.
	 */
	@Override
	protected JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			
			Dimension size = new Dimension(wsUiHelper.getDialogWidth(), 380);
			jPanel.setPreferredSize(size);
			jPanel.setMinimumSize(size);
			
			int y = 0;
			
			jPanel.add(new JLabel(Constant.messages.getString("filter.replacedialog.title")), wsUiHelper.getDescriptionConstraints(0, y++));
			
			jPanel.add(new JLabel(Constant.messages.getString("websocket.dialog.pattern")), wsUiHelper.getLabelConstraints(0, y));
			jPanel.add(getTxtPattern(), wsUiHelper.getFieldConstraints(1, y++));
			
			jPanel.add(new JLabel(Constant.messages.getString("websocket.dialog.replace")), wsUiHelper.getLabelConstraints(0, y));
			jPanel.add(getTxtReplaceWith(), wsUiHelper.getFieldConstraints(1, y++));
			
			// add opcode selection
			jPanel.add(wsUiHelper.getOpcodeLabel(), wsUiHelper.getLabelConstraints(0, y));
			GridBagConstraints constraints = wsUiHelper.getFieldConstraints(1, y++);
			constraints.gridheight = 3;
			y+=3;
			jPanel.add(wsUiHelper.getOpcodeMultipleSelect(), constraints);
			
			// add channel selection
			jPanel.add(wsUiHelper.getChannelLabel(), wsUiHelper.getLabelConstraints(0, y));
			constraints = wsUiHelper.getFieldConstraints(1, y++);
			constraints.gridheight = 3;
			constraints.weighty = 1;
			y+=3;
			jPanel.add(wsUiHelper.getChannelMultipleSelect(), constraints);
			
			// add title for upcoming WebSocket specific options			
			jPanel.add(wsUiHelper.getDirectionLabel(), wsUiHelper.getLabelConstraints(0, y));
			jPanel.add(wsUiHelper.getOutgoingCheckbox(), wsUiHelper.getFieldConstraints(1, y++));

			jPanel.add(wsUiHelper.getIncomingCheckbox(), wsUiHelper.getFieldConstraints(1, y++));
			
			// add submit panel
			jPanel.add(getJPanel1(), wsUiHelper.getFieldConstraints(1, y));
		}
		
		return jPanel;
	}
	
	public boolean isIncomingChecked() {
		return wsUiHelper.getIncomingCheckbox().isSelected();
	}
	
	public boolean isOutgoingChecked() {
		return wsUiHelper.getOutgoingCheckbox().isSelected();
	}

	public List<String> getOpcodes() {
		return wsUiHelper.getSelectedOpcodes();
	}

	public List<Integer> getChannelIds() {
		return wsUiHelper.getSelectedChannelIds();
	}

	public void setChannelComboBoxModel(ChannelSortedListModel channelsModel) {
		wsUiHelper.setChannelsModel(channelsModel);
	}
}
