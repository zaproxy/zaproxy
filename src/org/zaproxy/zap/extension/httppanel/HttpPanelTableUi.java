package org.zaproxy.zap.extension.httppanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JTable;
import javax.swing.JTextArea;

import org.parosproxy.paros.extension.AbstractPanel;

public class HttpPanelTableUi extends AbstractPanel {

	private JTable jTable;
	
	public HttpPanelTableUi() {
		jTable = new JTable();
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		
		this.add(jTable, gridBagConstraints);
	}
}
