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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.websocket.WebSocketMessage.Direction;
import org.zaproxy.zap.extension.websocket.ui.WebSocketUiModel.WebSocketMessageDAO;

/**
 * Custom renderer for {@link JList} that shows WebSocket messages.
 */
public class WebSocketPanelCellRenderer extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = 6743145386134930346L;
	
	/**
	 * Field declarations
	 */
	private JLabel txtId = null;
	private JLabel txtDirection = null;
	private JLabel txtTimestamp = null;
	private JLabel txtOpcode = null;
	private JLabel txtPayloadLength = null;
	private JLabel txtPayload = null;

	/**
	 * Groups all labels above together to apply e.g.: fore- & background color
	 * on all.
	 */
	private JLabel[] labels;

	/**
	 * Default height used by all list elements.
	 */
	private static final int defaultHeight = 15;

	/**
	 * This is the default constructor
	 */
	public WebSocketPanelCellRenderer() {
		super();

		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
			setSize(328, 11);
		}
		
		setFont(new Font("Default", Font.PLAIN, 12));

		GridBagConstraints constraint = getDefaultConstraint(0, 0);
		txtId = getDefaultLabel(40);
		txtId.setHorizontalAlignment(SwingConstants.RIGHT);
		txtId.setToolTipText(Constant.messages.getString("websocket.panel.id.help"));
		add(txtId, constraint);

		constraint = getDefaultConstraint(1, 0);
		txtDirection = getDefaultLabel(20);
		txtDirection.setHorizontalAlignment(SwingConstants.CENTER);
		add(txtDirection, constraint);
		
		constraint = getDefaultConstraint(2, 0);
		txtTimestamp = getDefaultLabel(150);
		txtTimestamp.setToolTipText(Constant.messages.getString("websocket.panel.timestamp.help"));
		add(txtTimestamp, constraint);
		
		constraint = getDefaultConstraint(3, 0);
		txtOpcode = getDefaultLabel(70);
		txtOpcode.setHorizontalAlignment(SwingConstants.CENTER);
		add(txtOpcode, constraint);
		
		constraint = getDefaultConstraint(4, 0);
		txtPayloadLength = getDefaultLabel(50);
		txtPayloadLength.setHorizontalAlignment(SwingConstants.CENTER);
		txtPayloadLength.setToolTipText(Constant.messages.getString("websocket.panel.payload_length.help"));
		add(txtPayloadLength, constraint);
		
		constraint = getDefaultConstraint(5, 0);
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.weightx = 1;
		txtPayload = getDefaultLabel(Integer.MAX_VALUE);
		add(txtPayload, constraint);
		
		labels = new JLabel[] {txtId, txtDirection, txtTimestamp, txtOpcode, txtPayloadLength, txtPayload};
	}

	/**
	 * Helper to create base constraint.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private GridBagConstraints getDefaultConstraint(int x, int y) {
		GridBagConstraints constraint = new GridBagConstraints();
		
		constraint.gridx = x;
		constraint.gridy = y;
		
		constraint.ipadx = 4;
		constraint.ipady = 1;
		
		return constraint;
	}

	/**
	 * Helper to create default label.
	 * 
	 * @param width
	 * @return
	 */
	private JLabel getDefaultLabel(int width) {
		JLabel label = new JLabel();
		label.setBackground(SystemColor.text);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		
		Dimension size = new Dimension(width, defaultHeight);
		label.setPreferredSize(size);
		label.setMinimumSize(size);
		
		label.setFont(new Font("Default", Font.PLAIN, 12));
		label.setOpaque(true);
		
		return label;
	}

	/**
	 * Called whenever a value should be displayed in the list.
	 * 
	 * @see DefaultListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		WebSocketMessageDAO message = (WebSocketMessageDAO) value;
		
		txtId.setText(message.id);
		
		if (message.direction.equals(Direction.OUTGOING)) {
			txtDirection.setText("→");
		} else if (message.direction.equals(Direction.INCOMING)) {
			txtDirection.setText("←");
		}

		txtTimestamp.setText(" " + message.dateTime);
		txtOpcode.setText(message.opcode + "=" + message.readableOpcode);
		txtPayloadLength.setText(message.payloadLength);
		txtPayload.setText(message.payload);

		if (isSelected) {
			setColors(list.getSelectionForeground(), list.getSelectionBackground());
		} else {
			setColors(list.getForeground(), list.getBackground());
			
			Color darker = new Color(list.getBackground().getRGB() & 0xFFECECEC);

			txtTimestamp.setBackground(darker);
			txtPayloadLength.setBackground(darker);
		}
		
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		
		return this;
	}

	/**
	 * Helper function to set colors for all labels.
	 * 
	 * @param fg
	 * @param bg
	 */
	private void setColors(Color fg, Color bg) {
		for (JLabel label : labels) {
			label.setBackground(bg);
			label.setForeground(fg);
		}
	}
}
