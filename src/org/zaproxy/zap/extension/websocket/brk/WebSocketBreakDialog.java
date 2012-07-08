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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel.ComboBoxChannelItem;
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

    public WebSocketBreakDialog(WebSocketBreakpointsUiManagerInterface breakPointsManager, ComboBoxModel channelSelectModel) throws HeadlessException {
        super(View.getSingleton().getMainFrame(), false);
        
        this.breakPointsManager = breakPointsManager;
        this.channelSelectModel = new ClonedComboBoxModel(channelSelectModel);
        initialize();
    }
    
    /**
	 * If a {@link ComboBoxModel} is shared by two different {@link JComboBox}
	 * instances (i.e. set as model on both), then changing the selection of an
	 * item in one {@link JComboBox} causes the same item to be selected in the
	 * other {@link JComboBox} too.
	 * <p>
	 * This model wraps the original model and manages its own selected item. As
	 * a result, the {@link JComboBox} is independent from the other. Moreover
	 * items are always the same.
	 */
    private class ClonedComboBoxModel implements ComboBoxModel {
		private ComboBoxModel wrappedModel;
		private Object selectedObject;
		
		public ClonedComboBoxModel(ComboBoxModel wrappedModel) {
			this.wrappedModel = wrappedModel; 
			this.selectedObject = wrappedModel.getElementAt(0);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			wrappedModel.removeListDataListener(l);
		}

		@Override
		public Object getElementAt(int index) {
			return wrappedModel.getElementAt(index);
		}

		@Override
		public int getSize() {
			return wrappedModel.getSize();
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			wrappedModel.removeListDataListener(l);
		}

		@Override
		public Object getSelectedItem() {
			return selectedObject;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selectedObject = anItem;
		}    	
    }

	private void initialize() {
        this.setTitle(getDialogTitle());
        this.setContentPane(getJPanel());
        this.setSize(407, 280);
        this.addWindowListener(new java.awt.event.WindowAdapter() {   
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
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

			javax.swing.JLabel jLabel2 = new JLabel();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new java.awt.Dimension(400,220));
			jPanel.setMinimumSize(new java.awt.Dimension(400,220));
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 5;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 5;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,10);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;

			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 5;
			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.weightx = 1.0D;
			gridBagConstraints13.insets = new java.awt.Insets(2,10,2,5);

			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 0.0D;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.insets = new java.awt.Insets(2,10,5,10);
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 2;
			gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints15.ipadx = 0;
			gridBagConstraints15.ipady = 10;

			jPanel.add(getJScrollPane(), gridBagConstraints15);
			jPanel.add(jLabel2, gridBagConstraints13);
			jPanel.add(getBtnCancel(), gridBagConstraints2);
			jPanel.add(getBtnSubmit(), gridBagConstraints3);
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
			btnSubmit = new JButton();
			btnSubmit.setText(getBtnSubmitText());
			btnSubmit.setMinimumSize(new java.awt.Dimension(75,30));
			btnSubmit.setPreferredSize(new java.awt.Dimension(75,30));
			btnSubmit.setMaximumSize(new java.awt.Dimension(100,40));
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

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText(Constant.messages.getString("brk.add.button.cancel"));
			btnCancel.setMaximumSize(new java.awt.Dimension(100,40));
			btnCancel.setMinimumSize(new java.awt.Dimension(70,30));
			btnCancel.setPreferredSize(new java.awt.Dimension(70,30));
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
			description.setPreferredSize(new java.awt.Dimension(350, 70));
			description.setMaximumSize(new java.awt.Dimension(350, 150));
			panel.add(description, constraints);

			// opcode restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.opcode")), createConstraints(0, 1, 0, false));
			panel.add(getOpcodeSelect(), createConstraints(1, 1, 1, true));
			
			// channel restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.channel")), createConstraints(0, 2, 0, true));
			panel.add(getChannelSelect(), createConstraints(1, 2, 1, true));
			
			// payload restriction
			panel.add(new JLabel(Constant.messages.getString("websocket.brk.add.pattern")), createConstraints(0, 3, 0, false));
			panel.add(getPayloadPatternField(), createConstraints(1, 3, 1, true));
			
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
