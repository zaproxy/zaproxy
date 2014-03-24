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
package org.zaproxy.zap.extension.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.parosproxy.paros.model.Model;

public class SearchPanelCellRenderer extends JPanel implements ListCellRenderer<SearchResult> {

	private static final long serialVersionUID = 1L;
    private JLabel txtId = null;
    private JLabel method = null;
    private JLabel url = null;
    private JLabel stringFound = null;

    /**
     * This is the default constructor
     */
    public SearchPanelCellRenderer() {
        super();

        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.0D;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.gridx = 0;

        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 0.0D;
        gridBagConstraints1.ipadx = 4;
        gridBagConstraints1.ipady = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.gridx = 1;
        
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 0.0D;
        gridBagConstraints2.ipadx = 4;
        gridBagConstraints2.ipady = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.gridx = 2;
        
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.weightx = 0.75D;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.ipadx = 4;
        gridBagConstraints3.ipady = 1;
        gridBagConstraints3.gridx = 3;
        
        txtId = new JLabel();
        txtId.setText(" ");
        txtId.setBackground(java.awt.SystemColor.text);
        txtId.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        txtId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txtId.setPreferredSize(new java.awt.Dimension(40,15));
        txtId.setMinimumSize(new java.awt.Dimension(40,15));
        txtId.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        txtId.setOpaque(true);
        
        method = new JLabel();
        method.setText(" ");
        method.setBackground(java.awt.SystemColor.text);
        method.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        method.setPreferredSize(new java.awt.Dimension(40,15));
        method.setMinimumSize(new java.awt.Dimension(40,15));
        method.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        method.setOpaque(true);

        url = new JLabel();
        url.setText(" ");
        url.setBackground(java.awt.SystemColor.text);
        url.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        url.setPreferredSize(new java.awt.Dimension(420,15));
        url.setMinimumSize(new java.awt.Dimension(420,15));
        url.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        url.setOpaque(true);

        stringFound = new JLabel();
        stringFound.setText(" ");
        stringFound.setBackground(java.awt.SystemColor.text);
        stringFound.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        stringFound.setPreferredSize(new java.awt.Dimension(45,15));
        stringFound.setMinimumSize(new java.awt.Dimension(45,15));
        stringFound.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));
        stringFound.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        stringFound.setOpaque(true);

        this.setLayout(new GridBagLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(328, 11);
        }
        this.setFont(new java.awt.Font("Default", java.awt.Font.PLAIN, 12));

        this.add(txtId, gridBagConstraints);
        this.add(method, gridBagConstraints1);
        this.add(url, gridBagConstraints2);
        this.add(stringFound, gridBagConstraints3);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SearchResult> list, SearchResult searchResult, int index, boolean isSelected, boolean cellHasFocus) {
        
        if (searchResult.getMessage().getHistoryRef() != null) {
        	txtId.setText(Integer.toString(searchResult.getMessage().getHistoryRef().getHistoryId()));
        } else {
            txtId.setText("");
        }
        method.setText(searchResult.getMessage().getRequestHeader().getMethod());
        url.setText(searchResult.getMessage().getRequestHeader().getURI().toString());
        stringFound.setText(searchResult.getStringFound());
        
        if (isSelected) {
            txtId.setBackground(list.getSelectionBackground());
            txtId.setForeground(list.getSelectionForeground());
        	method.setBackground(list.getSelectionBackground());
        	method.setForeground(list.getSelectionForeground());
        	url.setBackground(list.getSelectionBackground());
        	url.setForeground(list.getSelectionForeground());
        	stringFound.setBackground(list.getSelectionBackground());
        	stringFound.setForeground(list.getSelectionForeground());

        } else {
            Color darker = new Color(list.getBackground().getRGB() & 0xFFECECEC);
            
            txtId.setBackground(list.getBackground());
            txtId.setForeground(list.getForeground());
            method.setBackground(darker);
            method.setForeground(list.getForeground());
            url.setBackground(list.getBackground());
            url.setForeground(list.getForeground());
            stringFound.setBackground(darker);
            stringFound.setForeground(list.getForeground());

        }
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        return this;
        
    }

}
