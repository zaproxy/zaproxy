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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.filter.FilterReplaceDialog;

/**
 * Extended replace dialog with WebSocket specific options.
 */
public class FilterWebSocketReplaceDialog extends FilterReplaceDialog {
	private static final long serialVersionUID = -1156304397855108677L;
	
	/**
	 * If its not, adding components should be done.
	 */
	private boolean isJPanelInitialized = false;

	/**
	 * Find out if outgoing messages should be filtered.
	 */
	private JCheckBox outgoingCheckbox;

	/**
	 * Find out if incoming messages should be filtered.
	 */
	private JCheckBox incomingCheckbox;
	
	/**
	 * Ctor.
	 * 
	 * @param owner Pass e.g. the mainframe.
	 * @param isModal
	 */
	public FilterWebSocketReplaceDialog(Frame owner, boolean isModal) {
		super(owner, isModal);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	protected void initialize() {
		int width = 400, height = 230;
		
        setContentPane(getJPanel());
	    setPreferredSize(new Dimension(width, height));
		pack();
	}

	/**
	 * Adds WebSocket related filter options.
	 */
	protected JPanel getJPanel() {
		JPanel jPanel = super.getJPanel();
		
		if (!isJPanelInitialized) {
			isJPanelInitialized = true;
			
			// move action panel with OK & CANCEL button to the bottom (gridy)
			Component actionPanel = jPanel.getComponent(jPanel.getComponentCount() - 1);
			GridBagLayout layout = (GridBagLayout) jPanel.getLayout();
			GridBagConstraints constraints = (GridBagConstraints) layout.getConstraints(actionPanel).clone();
			constraints.gridy = 5;
			layout.setConstraints(actionPanel, constraints);
			
			// add title for upcoming WebSocket specific options
			JLabel title = new JLabel();
			title.setText(Constant.messages.getString("websocket.filter.replacedialog.direction"));

			constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(15,5,5,5);
			constraints.gridwidth = 2;
			constraints.gridy = 3;
			constraints.weightx = 1.0D;
			
			jPanel.add(title, constraints);
			
			// add checkbox for outgoing messages
			outgoingCheckbox = new JCheckBox(Constant.messages.getString("websocket.filter.replacedialog.outgoing"));
			outgoingCheckbox.setSelected(true);
			
			constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(2,5,5,5);
			constraints.gridwidth = 1;
			constraints.gridy = 4;
			constraints.weightx = 1.0D;
			
			jPanel.add(outgoingCheckbox, constraints);

			// add checkbox for incoming messages
			incomingCheckbox = new JCheckBox(Constant.messages.getString("websocket.filter.replacedialog.incoming"));
			incomingCheckbox.setSelected(true);
			
			constraints = new GridBagConstraints();
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(2,5,5,5);
			constraints.gridwidth = 1;
			constraints.gridx = 1;
			constraints.gridy = 4;
			constraints.weightx = 1.0D;
			
			jPanel.add(incomingCheckbox, constraints);
		}
		
		return jPanel;
	}

	/**
	 * 
	 * @return True if incoming messages should be filtered.
	 */
	public boolean isIncomingChecked() {
		return incomingCheckbox.isSelected();
	}

	/**
	 * 
	 * @return True if outgoing messages should be filtered.
	 */
	public boolean isOutgoingChecked() {
		return outgoingCheckbox.isSelected();
	}
}
