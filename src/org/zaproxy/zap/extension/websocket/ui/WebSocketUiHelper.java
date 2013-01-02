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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDTO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.utils.ZapTextField;

public class WebSocketUiHelper {
	private static final String SELECT_ALL_OPCODES = Constant.messages.getString("websocket.dialog.opcodes.select_all");
	
	private JComboBox<String> opcodeComboBox;
	private JList<String> opcodeList;
	private JScrollPane opcodeListScrollPane;
	
	private JList<WebSocketChannelDTO> channels;
	private JScrollPane channelsScrollPane;
	
	private JComboBox<WebSocketChannelDTO> channelsComboBox;
	private ChannelSortedListModel channelsModel;

	private JCheckBox outgoingCheckbox;
	private JCheckBox incomingCheckbox;
	private JComboBox<String> directionComboBox;
	
	private ZapTextField patternTextField;

	
	// ************************************************************************
	// ***** HELPER

	public int getDialogWidth() {
		return 400;
	}
	
	public GridBagConstraints getLabelConstraints(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(0,5,0,5);
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
	}
	
	public GridBagConstraints getFieldConstraints(int x, int y) {
        GridBagConstraints gbc = getLabelConstraints(x, y);
        gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
        return gbc;
	}

	public GridBagConstraints getDescriptionConstraints(int x, int y) {
		GridBagConstraints gbc = getLabelConstraints(x, y);
		gbc.insets = new Insets(5, 5, 10, 5);
		gbc.gridwidth = 3;
		gbc.weightx = 1;
        return gbc;
	}
	
	// ************************************************************************
	// ***** OPCODE
	
	public JLabel getOpcodeLabel() {
		return new JLabel(Constant.messages.getString("websocket.dialog.opcode"));
	}

	public JComboBox<String> getOpcodeSingleSelect() {
        if (opcodeComboBox == null) {
            opcodeComboBox = new JComboBox<>(getOpcodeModel());
        }
        return opcodeComboBox;
    }
	
	/**
	 * @return Null if '--All Opcodes--' is selected
	 */
	public String getSelectedOpcode() {
		if (getOpcodeSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		return (String) getOpcodeSingleSelect().getSelectedItem();
	}

	/**
	 * @return Null if '--All Opcodes--' is selected
	 */
	public Integer getSelectedOpcodeInteger() {
		if (getOpcodeSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		
		String opcodeString = (String) getOpcodeSingleSelect().getSelectedItem();
		
		for (int opcode : WebSocketMessage.OPCODES) {
			if (WebSocketMessage.opcode2string(opcode).equals(opcodeString)) {
				return opcode;
			}
		}
		return null;
	}

	public JScrollPane getOpcodeMultipleSelect() {
		if (opcodeListScrollPane == null) {
			opcodeListScrollPane = new JScrollPane(getOpcodeList());
		}
		return opcodeListScrollPane;
	}

	private JList<String> getOpcodeList() {
		if (opcodeList == null) {
			int itemsCount = WebSocketMessage.OPCODES.length + 1;
			
			opcodeList = new JList<>(getOpcodeModel());
			opcodeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			opcodeList.setSelectedIndex(0);
			opcodeList.setLayoutOrientation(JList.VERTICAL);
			opcodeList.setVisibleRowCount(itemsCount);
		}
		return opcodeList;
	}

	private String[] getOpcodeModel() {
        int i = 0;
		String[] opcodes = new String[WebSocketMessage.OPCODES.length + 1];
        
        // all opcodes
        opcodes[i++] = SELECT_ALL_OPCODES;
        
        // specific opcode
        for (int opcode : WebSocketMessage.OPCODES) {
            opcodes[i++] = WebSocketMessage.opcode2string(opcode);
        }
        
		return opcodes;
	}

	/**
	 * @return Null if '--All Opcodes--' is selected
	 */
	public List<String> getSelectedOpcodes() {
		boolean isSelectAll = false;
		List<String> values = new ArrayList<>();
		
		for (String value : opcodeList.getSelectedValuesList()) {
			if (value.equals(SELECT_ALL_OPCODES)) {
				isSelectAll = true;
				break;
			}
			
			values.add(value);
		}
		
		if (isSelectAll) {
			return null;
		}
		
		return values;
	}

	/**
	 * @return Null if '--All Opcodes--' is selected
	 */
	public List<Integer> getSelectedOpcodeIntegers() {
		List<String> opcodes = getSelectedOpcodes();
		if (opcodes == null) {
			return null;
		}
		
		List<Integer> values = new ArrayList<>();
		for (int opcode : WebSocketMessage.OPCODES) {
			if (opcodes.contains(WebSocketMessage.opcode2string(opcode))) {
				values.add(opcode);
			}
		}
		return values;
	}

	public void setSelectedOpcodes(List<String> opcodes) {
		JList<String> opcodesList = getOpcodeList();
		if (opcodes == null || opcodes.contains(SELECT_ALL_OPCODES)) {
			opcodesList.setSelectedIndex(0);
		} else {
			int j = 0;
			int[] selectedIndices = new int[opcodes.size()];
			ListModel<String> model = opcodesList.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				String item = model.getElementAt(i);
				if (opcodes.contains(item)) {
					selectedIndices[j++] = i;
				}
			}
			opcodesList.setSelectedIndices(selectedIndices);
		}
	}

	// ************************************************************************
	// ***** CHANNEL
	
	public void setChannelsModel(ChannelSortedListModel channelsModel) {
		this.channelsModel = channelsModel;
	}
	
	public JLabel getChannelLabel() {
		return new JLabel(Constant.messages.getString("websocket.dialog.channel"));
	}

	public JComboBox<WebSocketChannelDTO> getChannelSingleSelect() {
		if (channelsComboBox == null) {
			// dropdown can be wider than JComboBox
            channelsComboBox = new WiderDropdownJComboBox<>(new ComboBoxChannelModel(channelsModel), true);
            channelsComboBox.setRenderer(new ComboBoxChannelRenderer());
            
            // fixes width of JComboBox
            channelsComboBox.setPrototypeDisplayValue(new WebSocketChannelDTO("XXXXXXXXXXXXXXXXXX"));

        }
        return channelsComboBox;
	}

	/**
	 * @return Null if '--All Channels--' is selected
	 */
	public Integer getSelectedChannelId() {
		if (getChannelSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		WebSocketChannelDTO channel = (WebSocketChannelDTO) getChannelSingleSelect().getSelectedItem();
		return channel.id;
	}
	
	public WebSocketChannelDTO getSelectedChannelDTO() {
		if (getChannelSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		WebSocketChannelDTO channel = (WebSocketChannelDTO) getChannelSingleSelect().getSelectedItem();
		return channel;
	}
	
	public void setSelectedChannelId(Integer channelId) {
		if (channelId != null) {
			for (int i = 0; i < channelsModel.getSize(); i++) {
				WebSocketChannelDTO channel = channelsModel.getElementAt(i);
				if (channelId.equals(channel.id)) {
					channelsComboBox.setSelectedItem(channel);
					return;
				}
			}
		}
		
		// set default value, if channelId is not found or none provided
		getChannelSingleSelect().setSelectedIndex(0);
	}

	public JScrollPane getChannelMultipleSelect() {
		if (channelsScrollPane == null) {
			channelsScrollPane = new JScrollPane(getChannelsList());
		}
		return channelsScrollPane;
	}
	
	private JList<WebSocketChannelDTO> getChannelsList() {
		if (channels == null) {
			int itemsCount = 4;
			
			channels = new JList<>(channelsModel);
			channels.setCellRenderer(new ComboBoxChannelRenderer());
			channels.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			channels.setSelectedIndex(0);
			channels.setLayoutOrientation(JList.VERTICAL);
			channels.setVisibleRowCount(itemsCount);
            
            // fixes width of JList
			channels.setPrototypeCellValue(new WebSocketChannelDTO("XXXXXXXXXXXXXXXXXX"));
		}
		return channels;
	}

	/**
	 * @return Null if '--All Channels--' is selected.
	 */
	public List<Integer> getSelectedChannelIds() {
		boolean isSelectAll = false;
		List<Integer> values = new ArrayList<>();
		
		for (WebSocketChannelDTO value : channels.getSelectedValuesList()) {
			Integer channelId = value.id;
			if (channelId == null) {
				isSelectAll = true;
				break;
			}
			values.add(channelId);
		}
		
		if (isSelectAll) {
			return null;
		}
		
		return values;
	}

	public void setSelectedChannelIds(List<Integer> channelIds) {
		JList<WebSocketChannelDTO> channelsList = getChannelsList();
		if (channelIds == null || channelIds.contains(-1)) {
			channelsList.setSelectedIndex(0);
		} else {
			int[] selectedIndices = new int[channelIds.size()];
			ListModel<WebSocketChannelDTO> model = channelsList.getModel();
			for (int i = 0, j = 0; i < model.getSize(); i++) {
				WebSocketChannelDTO channel = model.getElementAt(i);
				if (channelIds.contains(channel.id)) {
					selectedIndices[j++] = i;
				}
			}
			channelsList.setSelectedIndices(selectedIndices);
		}
	}

	// ************************************************************************
	// ***** DIRECTION
	
	public JLabel getDirectionLabel() {
		return new JLabel(Constant.messages.getString("websocket.dialog.direction"));
	}

	public JPanel getDirectionPanel() {
		if (outgoingCheckbox == null) {
			JPanel panel = new JPanel();
			panel.add(getOutgoingCheckbox());
			panel.add(getIncomingCheckbox());
		}
		return (JPanel) outgoingCheckbox.getParent();
	}

	public JCheckBox getIncomingCheckbox() {
		if (incomingCheckbox == null) {
			incomingCheckbox = new JCheckBox(Constant.messages.getString("websocket.dialog.direction_incoming"));
			incomingCheckbox.setSelected(true);
		}
		return incomingCheckbox;
	}

	public JCheckBox getOutgoingCheckbox() {
		if (outgoingCheckbox == null) {
			outgoingCheckbox = new JCheckBox(Constant.messages.getString("websocket.dialog.direction_outgoing"));
			outgoingCheckbox.setSelected(true);
		}
		return outgoingCheckbox;
	}
	
	public Direction getDirection() {
		if (getOutgoingCheckbox().isSelected() && getIncomingCheckbox().isSelected()) {
			return null;
		} else if (getOutgoingCheckbox().isSelected()) {
			return Direction.OUTGOING;
		} else if (getIncomingCheckbox().isSelected()) {
			return Direction.INCOMING;
		}
		return null;
	}

	public void setDirection(Direction direction) {
		if (direction == null) {
			getOutgoingCheckbox().setSelected(true);
			getIncomingCheckbox().setSelected(true);
		} else if (direction.equals(Direction.OUTGOING)) {
			getOutgoingCheckbox().setSelected(true);
			getIncomingCheckbox().setSelected(false);
		} else if (direction.equals(Direction.INCOMING)) {
			getOutgoingCheckbox().setSelected(false);
			getIncomingCheckbox().setSelected(true);
		}
	}

	public JComboBox<String> getDirectionSingleSelect() {
        if (directionComboBox == null) {
        	directionComboBox = new JComboBox<String>(getDirectionModel());
        }
        return directionComboBox;
    }

	private String[] getDirectionModel() {
		String[] directions = new String[]{
			Constant.messages.getString("websocket.filter.label.direction_outgoing"),
			Constant.messages.getString("websocket.filter.label.direction_incoming"),
		};
        
		return directions;
	}
	
	public Boolean isDirectionSingleSelectOutgoing() {
		if (getDirectionSingleSelect().getSelectedIndex() == 0) {
			return true;
		}
		return false;
	}

	public void setDirectionSingleSelect(Boolean isOutgoing) {
		int index = (isOutgoing == null || isOutgoing) ? 0 : 1;
		getDirectionSingleSelect().setSelectedIndex(index);
	}

	// ************************************************************************
	// ***** PATTERN

	public JLabel getPatternLabel() {
		return new JLabel(Constant.messages.getString("websocket.dialog.pattern"));
	}
	
    public ZapTextField getPatternTextField() {
		if (patternTextField == null) {
			patternTextField = new ZapTextField();
		}
		
		return patternTextField;
	}
	
	public String getPattern() {
		return patternTextField.getText();
	}
}
