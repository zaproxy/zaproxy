/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The Zed Attack Proxy team
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
package org.zaproxy.zap.extension.pscan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;

public class PolicyPassiveScanPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable tableTest = null;
	private JScrollPane jScrollPane = null;
	private PolicyPassiveScanTableModel passiveScanTableModel = null;
    /**
     *
     */
    public PolicyPassiveScanPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

        this.setLayout(new GridBagLayout());
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(375, 204);
	    }
        this.setName(Constant.messages.getString("pscan.policy.title"));
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.weighty = 1.0;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridy = 1;
        gridBagConstraints11.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
        this.add(getJScrollPane(), gridBagConstraints11);

	}
	private static final int[] width = {300,60};
	/**
	 * This method initializes tableTest
	 *
	 * @return javax.swing.JTable
	 */
	private JTable getTableTest() {
		if (tableTest == null) {
			tableTest = new JTable();
			tableTest.setModel(getPassiveScanTableModel());
			tableTest.setRowHeight(18);
			tableTest.setIntercellSpacing(new java.awt.Dimension(1,1));
	        for (int i = 0; i < 2; i++) {
	            TableColumn column = tableTest.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }
		}
		return tableTest;
	}

    @Override
    public void initParam(Object obj) {

    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }

    @Override
    public void saveParam(Object obj) throws Exception {
    }
	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableTest());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}
	/**
	 * This method initializes categoryTableModel
	 *
	 * @return org.parosproxy.paros.plugin.scanner.CategoryTableModel
	 */
	PolicyPassiveScanTableModel getPassiveScanTableModel() {
		if (passiveScanTableModel == null) {
			passiveScanTableModel = new PolicyPassiveScanTableModel();
		}
		return passiveScanTableModel;
	}

	public void setPassiveScanTableModel(PolicyPassiveScanTableModel categoryTableModel) {
		this.passiveScanTableModel = categoryTableModel;
	}

	@Override
	public String getHelpIndex() {
		// TODO add to msg file
		return "ui.dialogs.options.pscan";
	}
}
