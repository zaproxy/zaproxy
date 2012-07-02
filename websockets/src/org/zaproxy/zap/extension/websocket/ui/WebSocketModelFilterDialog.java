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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.websocket.WebSocketMessage;

/**
 * Filter WebSocket messages in {@link WebSocketPanel}. Show only specific ones.
 */
public class WebSocketModelFilterDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Description of the dialog.
	 */
	private static final String MSG = Constant.messages.getString("websocket.filter.label.desc"); 

	/**
	 * The dialogs panel.
	 */
	private JPanel dialogPanel = null;

	/**
	 * The options area with the scrollable opcode list.
	 */
	private JPanel optionsPanel = null;
	private JList opcodeList = null;
	private JScrollPane opcodeScroller = null;
	
	/**
	 * The actions area with apply, cancel and reset button.
	 */
	private JPanel actionsPanel = null;
	private JButton btnApply = null;
	private JButton btnCancel = null;
	private JButton btnReset = null;
	
	private int exitResult = JOptionPane.CANCEL_OPTION;
	
	/**
	 * The model holding the values set by this filter dialog.
	 */
	private WebSocketModelFilter filter = new WebSocketModelFilter();
	
    /**
     * @throws HeadlessException
     */
    public WebSocketModelFilterDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param owner
     * @param isModal
     * @throws HeadlessException
     */
    public WebSocketModelFilterDialog(Frame owner, boolean isModal) throws HeadlessException {
        super(owner, isModal);
        initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        setContentPane(getJPanel());
        setVisible(false);
        setResizable(false);
        setTitle(Constant.messages.getString("websocket.filter.title"));
        
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(400, 188);
        }
        
        centreDialog();
        
        getRootPane().setDefaultButton(btnApply);
        
        //  Handle escape key to close the dialog    
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				WebSocketModelFilterDialog.this.dispose();
            }
        };
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
        pack();
	}
	
	/**
	 * This method initializes the dialog's {@link JPanel}.	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (dialogPanel == null) {
			dialogPanel = new JPanel();
			dialogPanel.setLayout(new GridBagLayout());

			JLabel description = new JLabel();
			description.setText(MSG);
			description.setMaximumSize(new java.awt.Dimension(2147483647, 80));
			description.setMinimumSize(new java.awt.Dimension(350, 24));
			description.setPreferredSize(new java.awt.Dimension(350, 50));

			GridBagConstraints constraint = new GridBagConstraints();
			constraint.anchor = java.awt.GridBagConstraints.WEST;
			constraint.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraint.gridwidth = 3;
			constraint.insets = new java.awt.Insets(5, 10, 5, 10);
			constraint.ipadx = 3;
			constraint.ipady = 3;
			constraint.weightx = 1.0D;
			dialogPanel.add(description, constraint);

			constraint = new GridBagConstraints();
			constraint.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraint.gridy = 2;
			constraint.gridwidth = 3;
			constraint.insets = new java.awt.Insets(2, 10, 2, 10);
			constraint.ipady = 1;
			dialogPanel.add(getOptionsPanel(), constraint);

			constraint = new GridBagConstraints();
			constraint.gridwidth = 3;
			constraint.gridy = 3;
			constraint.insets = new java.awt.Insets(5, 2, 5, 2);
			constraint.ipadx = 3;
			constraint.ipady = 3;
			dialogPanel.add(getActionsPanel(), constraint);
		}
		
		return dialogPanel;
	}
	
	/**
	 * This method initializes the main panel of the dialog.
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());

			GridBagConstraints constraint = new GridBagConstraints();
			constraint.anchor = GridBagConstraints.WEST;
			constraint.insets = getStandardInset();
			optionsPanel.add(new JLabel(Constant.messages.getString("websocket.filter.label.opcodes")), constraint);

			constraint = new GridBagConstraints();
			constraint.anchor = GridBagConstraints.WEST;
			constraint.gridy = 1;
			constraint.gridheight = 3;
			constraint.insets = getStandardInset();
			optionsPanel.add(getOpcodeScroller(), constraint);

		}
		return optionsPanel;
	}
	
	private Insets getStandardInset() {
		return new Insets(5,5,1,5);
	}
	
	/**
	 * This method initializes the submit container containing all buttons.	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getActionsPanel() {
		if (actionsPanel == null) {
			actionsPanel = new JPanel();
			actionsPanel.add(getBtnCancel());
			actionsPanel.add(getBtnReset());
			actionsPanel.add(getBtnApply());
		}
		return actionsPanel;
	}
	
	/**
	 * This method initializes btnApply	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnApply() {
		if (btnApply == null) {
			btnApply = new JButton();
			btnApply.setText(Constant.messages.getString("history.filter.button.apply"));
			btnApply.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {    
					filter.setOpcodes(opcodeList.getSelectedValues());
				    exitResult = JOptionPane.OK_OPTION;
				    WebSocketModelFilterDialog.this.dispose();
				}
			});

		}
		return btnApply;
	}
	/**
	 * This method initializes btnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText(Constant.messages.getString("all.button.cancel"));
			btnCancel.addActionListener(new ActionListener() { 

				@Override
				public void actionPerformed(ActionEvent e) {
				    exitResult = JOptionPane.CANCEL_OPTION;
				    WebSocketModelFilterDialog.this.dispose();
				}
			});
		}
		return btnCancel;
	}
	
	public int showDialog() {
	    this.setVisible(true);
	    return exitResult;
	}
	

	/**
	 * This method initializes btnReset	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnReset() {
		if (btnReset == null) {
			btnReset = new JButton();
			btnReset.setText(Constant.messages.getString("history.filter.button.clear"));
			btnReset.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					exitResult = JOptionPane.NO_OPTION;
					// Unset everything
					opcodeList.setSelectedIndices(new int[0]);
					filter.reset();
				}
			});

		}
		return btnReset;
	}
	
	/**
	 * Returns the opcode selection list.
	 * 
	 * @return
	 */
	private JScrollPane getOpcodeScroller() {
		if (opcodeScroller == null) {
			
			// show WebSocket opcodes as string, not as integer
			String[] opcodes = new String[WebSocketMessage.OPCODES.length];
			int i = 0;
			for (int opcode : WebSocketMessage.OPCODES) {
				opcodes[i++] = WebSocketMessage.opcode2string(opcode);
			}
			
			opcodeList = new JList(opcodes);
			opcodeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			opcodeList.setLayoutOrientation(JList.VERTICAL);
			opcodeList.setVisibleRowCount(HttpRequestHeader.METHODS.length);
			
			opcodeScroller = new JScrollPane(opcodeList);
		}
		return opcodeScroller;
	}
	
	/**
	 * Returns the model holding the values set by this dialog.
	 * 
	 * @return
	 */
	public WebSocketModelFilter getFilter() {
		return filter;
	}
}
