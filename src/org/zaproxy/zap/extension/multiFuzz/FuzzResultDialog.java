/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXTreeTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.StickyScrollbarAdjustmentListener;

public abstract class FuzzResultDialog extends AbstractDialog {
	private JPanel background;
	private JButton close;
	private JScrollPane jScrollPane;
	public abstract JXTreeTable getTable();
	public abstract FuzzComponent getMessageInspection();
	public abstract JTabbedPane getDiagrams();
	
	
	public FuzzResultDialog() {
		super(View.getSingleton().getMainFrame(), true);
		initialize();
	}
	
	
	protected void initialize() {
		this.setTitle(Constant.messages.getString("fuzz.title"));
		this.setContentPane(getJPanel());
		this.setSize(800, 400);
	}

	private JPanel getJPanel() {
		if (background == null) {
			background = new JPanel();
			background.setLayout(new GridBagLayout());
			int currentRow = 0;
			background.add(getMessageInspection().messageView(), Util.getGBC(0, currentRow, 1, 0.4, 1, GridBagConstraints.BOTH));
			background.add(getDiagrams(), Util.getGBC(1, currentRow, 2, 0.6, 1, GridBagConstraints.BOTH));
			currentRow++;
			GridBagConstraints tableConstraints = Util.getGBC(0,currentRow, 3, 1 , 0, GridBagConstraints.BOTH);
			tableConstraints.anchor = GridBagConstraints.PAGE_END;
			tableConstraints.ipady = 150;
			background.add(getScrollPane(), tableConstraints);
			currentRow++;
			background.add(getCloseButton(), Util.getGBC(2, currentRow, 1, 0));
		}
		return background;
	}
	
	private JScrollPane getScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTable());
			jScrollPane.setFont(new java.awt.Font("Dialog",
					java.awt.Font.PLAIN, 11));
			jScrollPane
					.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.getVerticalScrollBar().addAdjustmentListener(
					new StickyScrollbarAdjustmentListener());
		}
		return jScrollPane;
	}
	
	private Component getCloseButton() {
		if(close == null){
			close = new JButton();
			close.setAction(new CloseAction());
		}
		return close;
	}

	private class CloseAction extends AbstractAction{
		
		public CloseAction(){
			super(Constant.messages.getString("fuzz.button.close"));
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
		
	}
}