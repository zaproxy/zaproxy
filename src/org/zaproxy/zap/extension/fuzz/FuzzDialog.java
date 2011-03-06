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
package org.zaproxy.zap.extension.fuzz;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.owasp.jbrofuzz.core.Database;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.HttpPanel;
import org.parosproxy.paros.view.View;
public class FuzzDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	
	private ExtensionFuzz extension;

	private DefaultComboBoxModel fuzzerModel = null;
	private JLabel selectionField = null;
	private JComboBox categoryField = null;
	private JList fuzzersField = null;
	private JButton cancelButton = null;
	private JButton startButton = null;
	private int selectionStart = -1;
	private int selectionEnd = -1;
	private boolean fuzzHeader = true;

	//private HttpPanelRequest requestPanel = View.getSingleton().getRequestPanel();
	private HttpPanel requestPanel = View.getSingleton().getRequestPanel();
	
	private Database fuzzDB = new Database();

    /**
     * @throws HeadlessException
     */
    public FuzzDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public FuzzDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setContentPane(getJTabbed());
        this.setTitle(Constant.messages.getString("fuzz.title"));
        this.setSize(500, 250);
	
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJTabbed() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.selection")), getGBC(0, 0, 1, 0.25D));
			jPanel.add(getSelectionField(), getGBC(1, 0, 3, 0.75D));

			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.category")), getGBC(0, 2, 1, 0.25D));
			jPanel.add(getCategoryField(), getGBC(1, 2, 3, 0.75D));

			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.fuzzer")), getGBC(0, 3, 1, 0.25D));
			
			JScrollPane fuzzersPane = new JScrollPane(getFuzzersField());
			jPanel.add(fuzzersPane, getGBC(1, 3, 3, 1.0D, 0.75D));

			jPanel.add(new JLabel(""), getGBC(1, 4, 1, 0.50));
			jPanel.add(getStartButton(), getGBC(2, 4, 1, 0.25));
			jPanel.add(getCancelButton(), getGBC(3, 4, 1, 0.25));
		}
		return jPanel;
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(Constant.messages.getString("fuzz.button.cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}});
		}
		return cancelButton;
	}

	private JButton getStartButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setText(Constant.messages.getString("fuzz.button.start"));
			startButton.setEnabled(false);	// Only enabled when 1 or more fuzzers chosen
			startButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
			        HttpMessage msg = new HttpMessage();
			        requestPanel.getMessage(msg, true);
			        
			        Object [] names = fuzzersField.getSelectedValues();
			        if (names != null && names.length > 0) {
				        try {
				        	Fuzzer [] fuzzers = new Fuzzer[names.length];
				        	for (int i=0; i < names.length; i++) {
						        fuzzers[i] = fuzzDB.createFuzzer(fuzzDB.getIdFromName(names[i].toString()), 1);
				        	}
	
							extension.startFuzzers(msg, fuzzers, fuzzHeader, selectionStart, selectionEnd);
							
						} catch (NoSuchFuzzerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						setVisible(false);
			        }
				}});
		}
		return startButton;
	}

	private GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return this.getGBC(x, y, width, weightx, 0.0);
	}
	
	private GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1,5,1,5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}
	
	private JComboBox getCategoryField() {
		if (categoryField == null) {
			categoryField = new JComboBox();
			
			String[] allCats = fuzzDB.getAllCategories();
			
			Arrays.sort(allCats);
			
			for (String category : allCats) {
				categoryField.addItem(category);
			}
			
			categoryField.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setFuzzerNames();
				}});
		}
		return categoryField;
	}
	
	private void setFuzzerNames () {
		fuzzerModel.removeAllElements();
		
		String category = (String) getCategoryField().getSelectedItem();
		if (category == null) {
			return;
		}
		
		String [] fuzzers = fuzzDB.getPrototypeNamesInCategory(category);
		Arrays.sort(fuzzers);
		for (String fuzzer : fuzzers) {
			fuzzerModel.addElement(fuzzer);
		}

	}
	
	private JLabel getSelectionField() {
		if (selectionField == null) {
			selectionField = new JLabel();
		}
		return selectionField;
	}

	private JList getFuzzersField() {
		if (fuzzersField == null) {
			fuzzerModel = new DefaultComboBoxModel();
			
			fuzzersField = new JList();
			fuzzersField.setModel(fuzzerModel);
			setFuzzerNames();
			fuzzersField.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
			        Object [] names = fuzzersField.getSelectedValues();
			        getStartButton().setEnabled(names != null && names.length > 0);

				}});
		}
		return fuzzersField;
	}
	

	public void reset() {
	}

	public void setSelection(JTextArea source) {
		if (source != null) {
			selectionStart = source.getSelectionStart();
			selectionEnd = source.getSelectionEnd();
			if (source.getSelectedText() != null && source.getSelectedText().length() > 50) {
				getSelectionField().setText(source.getSelectedText().substring(0, 50) + "...");
			} else {
				getSelectionField().setText(source.getSelectedText());
			}
			fuzzHeader = source.equals(requestPanel.getTxtHeader());
		}
	}

	public void setExtension(ExtensionFuzz extension) {
		this.extension = extension;
	}
	
	public void setDefaultCategory(String category) {
		this.getCategoryField().setSelectedItem(category);
	}
	
}
