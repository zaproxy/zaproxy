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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.ui.ChannelSortedListModel;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiHelper;

public abstract class WebSocketBreakDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	
    protected WebSocketBreakpointsUiManagerInterface breakPointsManager;

	protected WebSocketUiHelper wsUiHelper;
    
	private JPanel jPanel = null;
	private JButton btnSubmit = null;
	private JButton btnCancel = null;
	private JScrollPane jScrollPane = null;

    public WebSocketBreakDialog(WebSocketBreakpointsUiManagerInterface breakPointsManager, ChannelSortedListModel channelsModel) throws HeadlessException {
        super(View.getSingleton().getMainFrame(), false);
        
        this.breakPointsManager = breakPointsManager;
        
        wsUiHelper = new WebSocketUiHelper();
        wsUiHelper.setChannelsModel(channelsModel);
        
        initialize();
    }

	private void initialize() {
        setTitle(getDialogTitle());
        setContentPane(getJPanel());
        
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

	protected abstract String getDialogTitle();

	protected abstract ActionListener getActionListenerSubmit();

	protected abstract ActionListener getActionListenerCancel();

	protected abstract String getBtnSubmitText();

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			
			Dimension size = new Dimension(wsUiHelper.getDialogWidth(), 250);
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
			JLabel description = new JLabel(Constant.messages.getString("websocket.brk.add.desc"));
			description.setPreferredSize(new Dimension(wsUiHelper.getDialogWidth() - 30, 60));
			panel.add(description, wsUiHelper.getDescriptionConstraints(0, 0));

			// opcode restriction
			panel.add(wsUiHelper.getOpcodeLabel(), wsUiHelper.getLabelConstraints(0, 1));
			panel.add(wsUiHelper.getOpcodeSingleSelect(), wsUiHelper.getFieldConstraints(1, 1));
			
			// channel restriction
			panel.add(wsUiHelper.getChannelLabel(), wsUiHelper.getLabelConstraints(0, 2));
			panel.add(wsUiHelper.getChannelSingleSelect(), wsUiHelper.getFieldConstraints(1, 2));
			
			// payload restriction
			panel.add(wsUiHelper.getPatternLabel(), wsUiHelper.getLabelConstraints(0, 3));
			panel.add(wsUiHelper.getPatternTextField(), wsUiHelper.getFieldConstraints(1, 3));
			
			// direction restriction
			panel.add(wsUiHelper.getDirectionLabel(), wsUiHelper.getLabelConstraints(0, 4));

			// add checkbox for outgoing & incoming messages
			panel.add(wsUiHelper.getOutgoingCheckbox(), wsUiHelper.getFieldConstraints(1, 4));
			panel.add(wsUiHelper.getIncomingCheckbox(), wsUiHelper.getFieldConstraints(1, 5));
			
			jScrollPane.setViewportView(panel);			
		}
		return jScrollPane;
	}
	
	/**
	 * @return {@link WebSocketBreakpointMessage} with values set in dialog
	 * @throws PatternSyntaxException
	 */
    protected WebSocketBreakpointMessage getWebSocketBreakpointMessage() throws PatternSyntaxException {
		String opcode = wsUiHelper.getSelectedOpcode();
		Integer channelId = wsUiHelper.getSelectedChannelId();
		String payloadPattern = wsUiHelper.getPattern();
		Direction direction = wsUiHelper.getDirection();
		
		WebSocketBreakpointMessage brkMsg = new WebSocketBreakpointMessage(opcode, channelId, payloadPattern, direction);
		return brkMsg;
	}

	protected void resetDialogValues() {
		wsUiHelper.getOpcodeSingleSelect().setSelectedIndex(0);
		wsUiHelper.getChannelSingleSelect().setSelectedIndex(0);
    	
    	wsUiHelper.getPatternTextField().setText("");
    	
		wsUiHelper.getOutgoingCheckbox().setSelected(true);
		wsUiHelper.getIncomingCheckbox().setSelected(true);
	}

	protected void setDialogValues(String opcode, Integer channelId, String payloadPattern, Boolean isOutgoing) {
        if (opcode != null) {
        	wsUiHelper.getOpcodeSingleSelect().setSelectedItem(opcode);
        }
        
        if (channelId != null) {
        	wsUiHelper.setSelectedChannelId(channelId);
        }
        
        if (payloadPattern != null) {
        	wsUiHelper.getPatternTextField().setText(payloadPattern);
        }
        
        if (isOutgoing != null) {
        	if (isOutgoing) {
        		wsUiHelper.getIncomingCheckbox().setSelected(false);
        	} else {
        		wsUiHelper.getOutgoingCheckbox().setSelected(false);
        	}
        }
	}
}
