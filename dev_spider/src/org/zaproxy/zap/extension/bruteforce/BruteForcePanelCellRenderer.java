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
package org.zaproxy.zap.extension.bruteforce;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class BruteForcePanelCellRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private JLabel txtUrl = null;
    private JLabel txtCode = null;
    private JLabel txtReason = null;

    /**
     * This is the default constructor
     */
    public BruteForcePanelCellRenderer() {
        super();

        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        
        txtUrl = new JLabel();
        txtUrl.setText(" ");
        txtUrl.setBackground(java.awt.SystemColor.text);
        txtUrl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txtUrl.setPreferredSize(new java.awt.Dimension(400,15));
        txtUrl.setMinimumSize(new java.awt.Dimension(400,15));
        txtUrl.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtUrl.setOpaque(true);
        
        txtCode = new JLabel();
        txtCode.setText(" ");
        txtCode.setBackground(java.awt.SystemColor.text);
        txtCode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtCode.setPreferredSize(new java.awt.Dimension(50,15));
        txtCode.setMinimumSize(new java.awt.Dimension(50,15));
        txtCode.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtCode.setOpaque(true);
        
        txtReason = new JLabel();
        txtReason.setText(" ");
        txtReason.setBackground(java.awt.SystemColor.text);
        txtReason.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txtReason.setPreferredSize(new java.awt.Dimension(200,15));
        txtReason.setMinimumSize(new java.awt.Dimension(200,15));
        txtReason.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtReason.setOpaque(true);
        
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 0.75D;
        gridBagConstraints1.ipadx = 4;
        gridBagConstraints1.ipady = 1;
        gridBagConstraints1.gridx = 0;

        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.weightx = 0.0D;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.ipadx = 4;
        gridBagConstraints2.ipady = 1;
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.NONE;
        
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.weightx = 0.15D;
        gridBagConstraints3.ipadx = 4;
        gridBagConstraints3.ipady = 1;
        gridBagConstraints3.gridx = 2;

        this.setLayout(new GridBagLayout());
        this.setSize(328, 11);
        this.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        this.add(txtUrl, gridBagConstraints1);
        this.add(txtCode, gridBagConstraints2);
        this.add(txtReason, gridBagConstraints3);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
    	BruteForceItem item = (BruteForceItem)value;

        txtUrl.setText(item.getUrl());
        txtCode.setText(""+item.getStatusCode());
        txtReason.setText(item.getReason());
        
        if (isSelected) {
        	txtUrl.setBackground(list.getSelectionBackground());
        	txtUrl.setForeground(list.getSelectionForeground());
        	txtCode.setBackground(list.getSelectionBackground());
        	txtCode.setForeground(list.getSelectionForeground());
        	txtReason.setBackground(list.getSelectionBackground());
        	txtReason.setForeground(list.getSelectionForeground());
        } else {
            Color darker = new Color(list.getBackground().getRGB() & 0xFFECECEC);
        	txtUrl.setBackground(list.getBackground());
        	txtUrl.setForeground(list.getForeground());
        	txtCode.setBackground(darker);
        	txtCode.setForeground(list.getForeground());
        	txtReason.setBackground(list.getBackground());
        	txtReason.setForeground(list.getForeground());
        }
        return this;
    }
}
