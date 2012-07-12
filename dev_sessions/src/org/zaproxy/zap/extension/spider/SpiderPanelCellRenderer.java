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
package org.zaproxy.zap.extension.spider;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.parosproxy.paros.model.Model;

public class SpiderPanelCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	private JLabel txtURL = null;
	private JLabel txtDescription = null;

	/**
	 * Instantiates a new spider panel cell renderer.
	 */
	public SpiderPanelCellRenderer() {
		super();
		initialize();
	}

	/**
	 * This method initializes this renderer.
	 * 
	 * @return void
	 */
	private void initialize() {

		txtURL = new JLabel();
		txtURL.setText(" ");
		txtURL.setBackground(java.awt.SystemColor.text);
		txtURL.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		txtURL.setPreferredSize(new java.awt.Dimension(60, 15));
		txtURL.setMinimumSize(new java.awt.Dimension(60, 15));
		txtURL.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
		txtURL.setOpaque(true);

		txtDescription = new JLabel();
		txtDescription.setText(" ");
		txtDescription.setBackground(java.awt.SystemColor.text);
		txtDescription.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		txtDescription.setPreferredSize(new java.awt.Dimension(200, 15));
		txtDescription.setMinimumSize(new java.awt.Dimension(200, 15));
		txtDescription.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
		txtDescription.setOpaque(true);

		GridBagConstraints txtDescriptionGridBag = new GridBagConstraints();
		txtDescriptionGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
		txtDescriptionGridBag.gridx = 1;
		txtDescriptionGridBag.gridy = 0;
		txtDescriptionGridBag.ipadx = 4;
		txtDescriptionGridBag.ipady = 1;
		txtDescriptionGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;
		txtDescriptionGridBag.weightx = 0.75D;
		txtDescriptionGridBag.anchor = java.awt.GridBagConstraints.WEST;

		GridBagConstraints txtURLGridBag = new GridBagConstraints();
		txtURLGridBag.gridx = 0;
		txtURLGridBag.gridy = 0;
		txtURLGridBag.weightx = 0.0D;
		txtURLGridBag.ipadx = 4;
		txtURLGridBag.ipady = 1;
		txtURLGridBag.anchor = java.awt.GridBagConstraints.WEST;
		txtURLGridBag.fill = java.awt.GridBagConstraints.NONE;
		txtURLGridBag.insets = new java.awt.Insets(0, 0, 0, 0);

		this.setLayout(new GridBagLayout());
		if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
			this.setSize(328, 11);
		}

		this.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
		this.add(txtURL, txtURLGridBag);
		this.add(txtDescription, txtDescriptionGridBag);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		String url = (String) value;

		// String portDesc = Constant.messages.getString("port.unknown");
		// if (Constant.messages.containsKey("port." + port)) {
		// portDesc = Constant.messages.getString("port." + port);
		// }

		txtURL.setText(url);
		txtDescription.setText("-");

		if (isSelected) {
			txtURL.setBackground(list.getSelectionBackground());
			txtURL.setForeground(list.getSelectionForeground());
			txtDescription.setBackground(list.getSelectionBackground());
			txtDescription.setForeground(list.getSelectionForeground());
		} else {
			Color darker = new Color(list.getBackground().getRGB() & 0xFFECECEC);
			txtURL.setBackground(list.getBackground());
			txtURL.setForeground(list.getForeground());
			txtDescription.setBackground(darker);
			txtDescription.setForeground(list.getForeground());
		}
		return this;
	}
}
