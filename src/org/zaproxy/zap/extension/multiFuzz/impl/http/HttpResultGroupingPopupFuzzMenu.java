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
package org.zaproxy.zap.extension.multiFuzz.impl.http;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.JXTreeTable;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.ExtensionPopupMenu;

public class HttpResultGroupingPopupFuzzMenu extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
	private int[] lastRow;
	private HttpFuzzerContentPanel parent;

	/**
	 * This method initializes
	 * 
	 */
	public HttpResultGroupingPopupFuzzMenu(HttpFuzzerContentPanel p) {
		super();
		this.parent = p;
		initialize();

	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setText(Constant.messages.getString("fuzz.result.grouping"));
		populate();
	}

	private void populate() {
		this.removeAll();
		JMenuItem newGroup = new JMenuItem(
				Constant.messages.getString("fuzz.result.grouping.new"));
		newGroup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = JOptionPane.showInputDialog(Constant.messages
						.getString("fuzz.result.grouping.newDia"));
				HttpFuzzRecordGroup g = new HttpFuzzRecordGroup(name);
				parent.getResultsModel().addFuzzRecord(g);
				toGroup(g);
				populate();
			}
		});
		this.add(newGroup);
		JMenuItem topGroup = new JMenuItem(
				Constant.messages.getString("fuzz.result.grouping.remove"));
		topGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (HttpFuzzRecord r : parent.getEntries(lastRow)) {
					parent.getResultsModel().removeFuzzRecord(r);
					parent.getResultsModel().addFuzzRecord(r);
					populate();
				}
			}
		});
		this.add(topGroup);
		for (final HttpFuzzRecord r : parent.getResultsModel().getEntries()) {
			if (r instanceof HttpFuzzRecordGroup
					&& !parent.getEntries(lastRow).contains(r)) {
				JMenuItem c = new JMenuItem(r.getName());
				c.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						toGroup((HttpFuzzRecordGroup) r);
					}
				});
				this.add(c);
			}
		}
	}

	public void toGroup(HttpFuzzRecordGroup r) {
		for (HttpFuzzRecord m : parent.getEntries(lastRow)) {
			parent.getResultsModel().removeFuzzRecord(m);
			r.add(m);
		}
		populate();
		parent.getFuzzResultTable().updateUI();
		parent.getFuzzResultTable().repaint();
	}

	@Override
	public boolean isEnableForComponent(Component invoker) {
		boolean visible = false;
		if (invoker instanceof JXTreeTable && invoker.getName().equals("HttpFuzzResultTable")) {
			lastRow = ((JXTreeTable) invoker).getSelectedRows();
			visible = true;
		}
		return visible;

	}

	public int[] getLastEntry() {
		return lastRow;
	}

}
