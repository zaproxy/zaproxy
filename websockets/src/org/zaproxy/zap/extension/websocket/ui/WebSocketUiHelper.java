package org.zaproxy.zap.extension.websocket.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.websocket.WebSocketChannelDAO;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.utils.ZapTextField;

public class WebSocketUiHelper {
	private static final String SELECT_ALL_OPCODES = Constant.messages.getString("websocket.dialog.opcodes.select_all");
	
	private JComboBox opcodeComboBox;
	private JList opcodeList;
	
	private JList channelsList;
	
	private JComboBox channelsComboBox;
	private ComboBoxModel channelComboBoxModel;

	private JCheckBox outgoingCheckbox;
	private JCheckBox incomingCheckbox;
	
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
		JLabel title = new JLabel();
		title.setText(Constant.messages.getString("websocket.dialog.opcode"));
		return title;
	}

	public JComboBox getOpcodeSingleSelect() {
        if (opcodeComboBox == null) {
            opcodeComboBox = new JComboBox(getOpcodeModel());
        }
        return opcodeComboBox;
    }
	
	/**
	 * Returns null if '--All Opcodes--' is selected.
	 * 
	 * @return
	 */
	public String getSelectedOpcode() {
		if (getOpcodeSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		return (String) getOpcodeSingleSelect().getSelectedItem();
	}

	public JScrollPane getOpcodeMultipleSelect() {
		return (JScrollPane) getOpcodeList().getParent().getParent();
	}

	private JList getOpcodeList() {
		if (opcodeList == null) {
			int itemsCount = WebSocketMessage.OPCODES.length + 1;
			
			opcodeList = new JList(getOpcodeModel());
			opcodeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			opcodeList.setSelectedIndex(0);
			opcodeList.setLayoutOrientation(JList.VERTICAL);
			opcodeList.setVisibleRowCount(itemsCount);
			
			new JScrollPane(opcodeList);
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
	 * Returns null if '--All Opcodes--' is selected.
	 * 
	 * @return
	 */
	public List<String> getSelectedOpcodes() {
		boolean isSelectAll = false;
		ArrayList<String> values = new ArrayList<String>();
		
		for (Object value : opcodeList.getSelectedValues()) {
			if (((String) value).equals(SELECT_ALL_OPCODES)) {
				isSelectAll = true;
				break;
			}
			
			values.add((String) value);
		}
		
		if (isSelectAll) {
			return null;
		} else {
			return values;
		}
	}

	/**
	 * Returns null if '--All Opcodes--' is selected.
	 * 
	 * @return
	 */
	public List<Integer> getSelectedOpcodeIntegers() {
		List<String> opcodes = getSelectedOpcodes();
		if (opcodes == null) {
			return null;
		} else {
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int opcode : WebSocketMessage.OPCODES) {
				if (opcodes.contains(WebSocketMessage.opcode2string(opcode))) {
					values.add(opcode);
				}
			}
			return values;
		}
	}

	public void setSelectedOpcodes(ArrayList<String> opcodes) {
		JList opcodesList = getOpcodeList();
		if (opcodes == null || opcodes.contains(SELECT_ALL_OPCODES)) {
			opcodesList.setSelectedIndex(0);
		} else {
			int j = 0;
			int[] selectedIndices = new int[opcodes.size()];
			ListModel model = opcodesList.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				String item = (String) model.getElementAt(i);
				if (opcodes.contains(item)) {
					selectedIndices[j++] = i;
				}
			}
			opcodesList.setSelectedIndices(selectedIndices);
		}
	}

	// ************************************************************************
	// ***** CHANNEL
	
	public void setChannelComboBoxModel(ComboBoxModel channelComboBoxModel) {
		this.channelComboBoxModel = channelComboBoxModel;
	}
	
	public JLabel getChannelLabel() {
		JLabel title = new JLabel();
		title.setText(Constant.messages.getString("websocket.dialog.channel"));
		return title;
	}

	public JComboBox getChannelSingleSelect() {
		if (channelsComboBox == null) {
			// dropdown can be wider than JComboBox
            channelsComboBox = new WiderDropdownJComboBox(channelComboBoxModel, true);
            channelsComboBox.setRenderer(new ComboBoxChannelRenderer());
            
            // fixes width of JComboBox
            channelsComboBox.setPrototypeDisplayValue(new WebSocketChannelDAO("XXXXXXXXXXXXXXXXXX"));

        }
        return channelsComboBox;
	}

	/**
	 * Returns null if '--All Channels--' is selected.
	 * 
	 * @return
	 */
	public Integer getSelectedChannelId() {
		if (getChannelSingleSelect().getSelectedIndex() == 0) {
			return null;
		}
		WebSocketChannelDAO item = (WebSocketChannelDAO) getChannelSingleSelect().getSelectedItem();
		return item.channelId;
	}
	
	public void setSelectedChannelId(Integer channelId) {
		// set default value first, if channelId is not found
		getChannelSingleSelect().setSelectedIndex(0);
	
		for (int i = 0; i < channelComboBoxModel.getSize(); i++) {
			WebSocketChannelDAO item = (WebSocketChannelDAO) channelComboBoxModel.getElementAt(i);
			if (item.channelId == channelId) {
				channelComboBoxModel.setSelectedItem(item);
			}
		}
	}

	public JScrollPane getChannelMultipleSelect() {
		return (JScrollPane) getChannelsList().getParent().getParent();
	}
	
	private JList getChannelsList() {
		if (channelsList == null) {
			int itemsCount = 4;
			
			channelsList = new JList(channelComboBoxModel);
			channelsList.setCellRenderer(new ComboBoxChannelRenderer());
			channelsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			channelsList.setSelectedIndex(0);
			channelsList.setLayoutOrientation(JList.VERTICAL);
			channelsList.setVisibleRowCount(itemsCount);
            
            // fixes width of JList
			channelsList.setPrototypeCellValue(new WebSocketChannelDAO("XXXXXXXXXXXXXXXXXX"));
			
			new JScrollPane(channelsList);
		}
		return channelsList;
	}

	/**
	 * Returns null if '--All Channels--' is selected.
	 * 
	 * @return
	 */
	public List<Integer> getSelectedChannelIds() {
		boolean isSelectAll = false;
		ArrayList<Integer> values = new ArrayList<Integer>();
		
		for (Object value : channelsList.getSelectedValues()) {
			Integer channelId = ((WebSocketChannelDAO) value).channelId;
			if (channelId == null) {
				isSelectAll = true;
				break;
			}
			values.add(channelId);
		}
		
		if (isSelectAll) {
			return null;
		} else {
			return values;
		}
	}

	public void setSelectedChannelIds(ArrayList<Integer> channelIds) {
		JList channelsList = getChannelsList();
		if (channelIds == null || channelIds.contains(-1)) {
			channelsList.setSelectedIndex(0);
		} else {
			int j = 0;
			int[] selectedIndices = new int[channelIds.size()];
			ComboBoxChannelModel model = (ComboBoxChannelModel) channelsList.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				WebSocketChannelDAO item = (WebSocketChannelDAO) model.getElementAt(i);
				if (channelIds.contains(item.channelId)) {
					selectedIndices[j++] = i;
				}
			}
			channelsList.setSelectedIndices(selectedIndices);
		}
	}

	// ************************************************************************
	// ***** DIRECTION
	
	public JLabel getDirectionLabel() {
		JLabel title = new JLabel();
		title.setText(Constant.messages.getString("websocket.dialog.direction"));
		return title;
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

	// ************************************************************************
	// ***** PATTERN

	public Component getPatternLabel() {
		JLabel title = new JLabel();
		title.setText(Constant.messages.getString("websocket.dialog.pattern"));
		return title;
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
