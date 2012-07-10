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
package org.zaproxy.zap.extension.websocket.brk;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.ui.ComboBoxChannelItem;
import org.zaproxy.zap.extension.websocket.ui.ComboBoxChannelRenderer;
import org.zaproxy.zap.utils.ZapTextField;

public abstract class WebSocketBreakDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	
    protected WebSocketBreakpointsUiManagerInterface breakPointsManager;
    
	private JPanel jPanel = null;
	private JButton btnSubmit = null;
	private JButton btnCancel = null;
	private JScrollPane jScrollPane = null;
	
	private JComboBox comboBoxOpcodes = null;
	private JComboBox comboBoxChannels;
	private ComboBoxModel channelSelectModel;
	private ZapTextField payloadPatternField;
	private JCheckBox outgoingCheckbox;
	private JCheckBox incomingCheckbox;

    public WebSocketBreakDialog(WebSocketBreakpointsUiManagerInterface breakPointsManager, ComboBoxModel channelSelectModel) throws HeadlessException {
        super(View.getSingleton().getMainFrame(), false);
        
        this.breakPointsManager = breakPointsManager;
        this.channelSelectModel = channelSelectModel;
        
        initialize();
    }

	private void initialize() {
        setTitle(getDialogTitle());
        setContentPane(getJPanel());
        setSize(407, 280);
        
        addWindowListener(new java.awt.event.WindowAdapter() {   
        	@Override
        	public void windowOpened(java.awt.event.WindowEvent e) {    
        	} 

        	@Override
        	public void windowClosing(java.awt.event.WindowEvent e) {    
        	    btnCancel.doClick();
        	}
        });

		pack();
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			
			Dimension size = new Dimension(400, 270);
			jPanel.setPreferredSize(size);
			jPanel.setMinimumSize(size);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
			constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraints.gridwidth = 3;
			constraints.gridy = 2;
			constraints.ipady = 10;
			constraints.weightx = 1.0;
			constraints.insets = new Insets(2,10,5,10);
			jPanel.add(getJScrollPane(), constraints);
			
			constraints = new GridBagConstraints();
			constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraints.insets = new java.awt.Insets(2,10,2,5);
			constraints.gridy = 5;
			constraints.weightx = 1.0D;
			jPanel.add(new JLabel(), constraints);

			constraints = new GridBagConstraints();
			constraints.anchor = java.awt.GridBagConstraints.EAST;
			constraints.insets = new java.awt.Insets(2,2,2,2);
			constraints.gridx = 1;
			constraints.gridy = 5;
			jPanel.add(getBtnCancel(), constraints);

			constraints = new GridBagConstraints();
			constraints.anchor = java.awt.GridBagConstraints.EAST;
			constraints.insets = new java.awt.Insets(2,2,2,10);
			constraints.gridx = 2;
			constraints.gridy = 5;
			jPanel.add(getBtnSubmit(), constraints);
		}
		return jPanel;
	}
    
	/**
	 * Either 'Add' or 'Save' button.
	 * 
	 * @return
	 */
	private JButton getBtnSubmit() {
		if (btnSubmit == null) {
			Dimension size = new Dimension(75,30);
			
			btnSubmit = new JButton();
			btnSubmit.setText(getBtnSubmitText());
			btnSubmit.setMinimumSize(size);
			btnSubmit.setPreferredSize(size);
			btnSubmit.setMaximumSize(new Dimension(100,40));
			
			btnSubmit.addActionListener(getActionListenerSubmit());

		}
		return btnSubmit;
	}

	protected abstract String getDialogTitle();

	protected abstract ActionListener getActionListenerSubmit();

	protected abstract ActionListener getActionListenerCancel();

	protected abstract String getBtnSubmitText();

	protected String getSelectedOpcode() {
		if (getOpcodeSelect().getSelectedIndex() == 0) {
			return null;
		}
		return (String) getOpcodeSelect().getSelectedItem();
	}

	protected Integer getSelectedChannelId() {
		if (getChannelSelect().getSelectedIndex() == 0) {
			return null;
		}
		ComboBoxChannelItem item = (ComboBoxChannelItem) getChannelSelect().getSelectedItem();
		return item.getChannelId();
	}
	
	protected String getPayloadPattern() {
		return payloadPatternField.getText();
	}
	
	protected Direction getDirection() {
		if (outgoingCheckbox.isSelected() && incomingCheckbox.isSelected()) {
			return null;
		} else if (outgoingCheckbox.isSelected()) {
			return Direction.OUTGOING;
		} else if (incomingCheckbox.isSelected()) {
			return Direction.INCOMING;
		}
		return null;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText(Constant.messages.getString("brk.add.button.cancel"));
			btnCancel.setMaximumSize(new Dimension(100,40));
			btnCancel.setMinimumSize(new Dimension(70,30));
			btnCancel.setPreferredSize(new Dimension(70,30));
			btnCancel.setEnabled(true);
			
			btnCancel.addActionListener(getActionListenerCancel());

		}
		return btnCancel;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
			
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			
			// description
			GridBagConstraints constraints = createConstraints(0, 0, 1, true);
			constraints.insets = new java.awt.Insets(5, 10, 5, 10);
			JLabel description = new JLabel(Constant.messages.getString("websocket.brk.add.desc"));
			description.setPreferredSize(new Dimension(350, 70));
			description.setMaximumSize(new Dimension(350, 150));
			panel.add(description, constraints);

			// opcode restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.opcode")), createConstraints(0, 1, 0, false));
			panel.add(getOpcodeSelect(), createConstraints(1, 1, 1, true));
			
			// channel restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.channel")), createConstraints(0, 2, 0, false));
			panel.add(getChannelSelect(), createConstraints(1, 2, 1, true));
			
			// payload restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.pattern")), createConstraints(0, 3, 0, false));
			panel.add(getPayloadPatternField(), createConstraints(1, 3, 1, true));
			
			// direction restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.direction")), createConstraints(0, 4, 0, false));

			// add checkbox for outgoing messages
			panel.add(getOutgoingCheckbox(), createConstraints(1, 4, 1, true));
			// add checkbox for incoming messages
			panel.add(getIncomingCheckbox(), createConstraints(1, 5, 1, true));
			
			jScrollPane.setViewportView(panel);			
		}
		return jScrollPane;
	}

	protected JComboBox getOpcodeSelect() {
        if (comboBoxOpcodes == null) {
            String[] opcodes = new String[WebSocketMessage.OPCODES.length + 1];
            int i = 0;
            
            // all opcodes
            opcodes[i++] = Constant.messages.getString("websocket.brk.add.select.opcodes");
            
            // specific opcodes
            for (int opcode : WebSocketMessage.OPCODES) {
                opcodes[i++] = WebSocketMessage.opcode2string(opcode);
            }
            comboBoxOpcodes = new JComboBox(opcodes);
        }
        return comboBoxOpcodes;
    }

    protected JComboBox getChannelSelect() {
		if (comboBoxChannels == null) {
            comboBoxChannels = new JComboBox(channelSelectModel);
            comboBoxChannels.setRenderer(new ComboBoxChannelRenderer());
        }
        return comboBoxChannels;
	}

	protected void setSelectedChannel(Integer channelId) {
		// set default value first, if channelId is not found
		getChannelSelect().setSelectedIndex(0);
		
		for (int i = 0; i < channelSelectModel.getSize(); i++) {
			ComboBoxChannelItem item = (ComboBoxChannelItem) channelSelectModel.getElementAt(i);
			if (item.getChannelId() == channelId) {
				getChannelSelect().setSelectedItem(item);
			}
		}
	}
	
    protected ZapTextField getPayloadPatternField() {
		if (payloadPatternField == null) {
			payloadPatternField = new ZapTextField();
		}
		
		return payloadPatternField;
	}

    protected JCheckBox getOutgoingCheckbox() {
    	if (outgoingCheckbox == null) {
    		outgoingCheckbox = new JCheckBox(Constant.messages.getString("websocket.filter.replacedialog.outgoing"));
    		outgoingCheckbox.setSelected(true);
    	}
    	return outgoingCheckbox;		
	}

    protected JCheckBox getIncomingCheckbox() {
    	if (incomingCheckbox == null) {
			incomingCheckbox = new JCheckBox(Constant.messages.getString("websocket.filter.replacedialog.incoming"));
			incomingCheckbox.setSelected(true);
    	}
    	return incomingCheckbox;		
	}
	
	private GridBagConstraints createConstraints(int x, int y, double weight, boolean fullWidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
		gbc.weightx = weight;
		
		if (fullWidth) {
			gbc.gridwidth = 2;
		}
		
        gbc.insets = new java.awt.Insets(0,5,0,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		
        return gbc;
	}
}
