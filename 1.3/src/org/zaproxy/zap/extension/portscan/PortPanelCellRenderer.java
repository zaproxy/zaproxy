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
package org.zaproxy.zap.extension.portscan;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.parosproxy.paros.Constant;

public class PortPanelCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private JLabel txtPort = null;
    private JLabel txtDescription = null;

    /**
     * This is the default constructor
     */
    public PortPanelCellRenderer() {
        super();

        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        
        txtPort = new JLabel();
        txtPort.setText(" ");
        txtPort.setBackground(java.awt.SystemColor.text);
        txtPort.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtPort.setPreferredSize(new java.awt.Dimension(60,15));
        txtPort.setMinimumSize(new java.awt.Dimension(60,15));
        txtPort.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtPort.setOpaque(true);
        txtDescription = new JLabel();
        txtDescription.setText(" ");
        txtDescription.setBackground(java.awt.SystemColor.text);
        txtDescription.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txtDescription.setPreferredSize(new java.awt.Dimension(200,15));
        txtDescription.setMinimumSize(new java.awt.Dimension(200,15));
        txtDescription.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtDescription.setOpaque(true);
        
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.weightx = 0.75D;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.ipadx = 4;
        gridBagConstraints2.ipady = 1;
        gridBagConstraints2.gridx = 1;
        
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 0.0D;
        gridBagConstraints1.ipadx = 4;
        gridBagConstraints1.ipady = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints1.gridx = 0;

        this.setLayout(new GridBagLayout());
        this.setSize(328, 11);
        this.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        this.add(txtPort, gridBagConstraints1);
        this.add(txtDescription, gridBagConstraints2);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
        Integer port = (Integer) value;
        
		String portDesc = Constant.messages.getString("port.unknown");
		if (Constant.messages.containsKey("port." + port)) {
			portDesc = Constant.messages.getString("port." + port);
		}

        txtPort.setText(""+port);
        txtDescription.setText(portDesc);
        
        if (isSelected) {
        	txtPort.setBackground(list.getSelectionBackground());
        	txtPort.setForeground(list.getSelectionForeground());
        	txtDescription.setBackground(list.getSelectionBackground());
        	txtDescription.setForeground(list.getSelectionForeground());
        } else {
            Color darker = new Color(list.getBackground().getRGB() & 0xFFECECEC);
        	txtPort.setBackground(list.getBackground());
        	txtPort.setForeground(list.getForeground());
        	txtDescription.setBackground(darker);
        	txtDescription.setForeground(list.getForeground());
        }
        return this;
    }
}
