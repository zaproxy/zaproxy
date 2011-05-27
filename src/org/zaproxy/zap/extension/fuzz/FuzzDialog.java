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

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.jbrofuzz.core.Database;
import org.owasp.jbrofuzz.core.Fuzzer;
import org.owasp.jbrofuzz.core.NoSuchFuzzerException;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.httppanel.HttpPanelTextArea;
import org.zaproxy.zap.extension.search.SearchMatch;

public class FuzzDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private static final int selectionFieldLength = 40;
	private JPanel jPanel = null;
	
	private ExtensionFuzz extension;

	private DefaultComboBoxModel fuzzerModel = null;
	private JLabel selectionField = null;
	private JComboBox categoryField = null;
	private JList fuzzersField = null;
	private JCheckBox enableTokens = null;
	private JCheckBox showTokenRequests = null;
	private JButton cancelButton = null;
	private JButton startButton = null;
	private int selectionStart = -1;
	private int selectionEnd = -1;
	private boolean fuzzHeader = true;
	private HttpMessage httpMessage;

	private boolean incAcsrfToken = false;
	private FuzzerDialogTokenPane tokenPane = new FuzzerDialogTokenPane();
	
	private Database fuzzDB = new Database();

	private static Log log = LogFactory.getLog(FuzzDialog.class);

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
    public FuzzDialog(Frame arg0, boolean modal, boolean incAcsrfToken) throws HeadlessException {
        super(arg0, modal);
        this.incAcsrfToken = incAcsrfToken;
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
		if (incAcsrfToken) {
			this.setSize(500, 450);
		} else {
			this.setSize(500, 350);
		}
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
			jPanel.add(getSelectionField(), getGBC(1, 0, 3, 0.0D));

			if (incAcsrfToken) {
				jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.anticsrf")), getGBC(0, 1, 3, 1.0D));
				jPanel.add(getEnableTokens(), getGBC(1, 1, 1, 0.0D));
				jPanel.add(getTokensPane(), getGBC(0, 2, 4, 1.0D, 0.0D));
				
				jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.showtokens")), getGBC(0, 3, 3, 1.0D));
				jPanel.add(getShowTokenRequests(), getGBC(1, 3, 1, 0.0D));
				
			}
			
			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.category")), getGBC(0, 4, 1, 0.25D));
			jPanel.add(getCategoryField(), getGBC(1, 4, 3, 0.75D));

			jPanel.add(new JLabel(Constant.messages.getString("fuzz.label.fuzzer")), getGBC(0, 5, 1, 0.25D));
			
			jPanel.add(new JScrollPane(getFuzzersField()), getGBC(1, 5, 3, 1.0D, 0.75D));

			jPanel.add(new JLabel(""), getGBC(1, 6, 1, 0.50));
			jPanel.add(getStartButton(), getGBC(2, 6, 1, 0.25));
			jPanel.add(getCancelButton(), getGBC(3, 6, 1, 0.25));
		}
		return jPanel;
	}
	
	private JComponent getTokensPane() {
		return tokenPane.getPane();
	}
	
	private JCheckBox getEnableTokens() {
		if (enableTokens == null) {
			enableTokens = new JCheckBox();
			enableTokens.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tokenPane.setEnabled(enableTokens.isSelected());
					getShowTokenRequests().setEnabled(enableTokens.isSelected());
				}});
		}
		return enableTokens;
	}

	
	public JCheckBox getShowTokenRequests() {
		if (showTokenRequests == null) {
			showTokenRequests = new JCheckBox();
		}
		return showTokenRequests;
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
			        Object [] names = fuzzersField.getSelectedValues();
			        if (names != null && names.length > 0) {
				        try {
				        	Fuzzer [] fuzzers = new Fuzzer[names.length];
				        	for (int i=0; i < names.length; i++) {
						        fuzzers[i] = fuzzDB.createFuzzer(fuzzDB.getIdFromName(names[i].toString()), 1);
				        	}
				        	AntiCsrfToken token = null;
				        	if (getEnableTokens().isSelected() && tokenPane.isEnable()) {
				        		token = tokenPane.getToken();
				        	}
			        		extension.startFuzzers(httpMessage, fuzzers, fuzzHeader, 
			        				selectionStart, selectionEnd, token, getShowTokenRequests().isSelected());
							
						} catch (NoSuchFuzzerException e) {
							log.error(e.getMessage(), e);
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

	public void setSelection(Component source) {
		if (source != null) {
			SearchMatch sm = null;
			
			if (source instanceof HttpPanelTextArea) {
				HttpPanelTextArea ta = (HttpPanelTextArea) source;
				if (ta.getSelectedText().length() > selectionFieldLength) {
					getSelectionField().setText(ta.getSelectedText().substring(0, selectionFieldLength) + "...");
				} else {
					getSelectionField().setText(ta.getSelectedText());
				}
				sm = ta.getTextSelection();
				selectionStart = sm.getStart();
				selectionEnd = sm.getEnd();
				httpMessage = sm.getMessage();
				
				if (sm.getLocation().equals(SearchMatch.Locations.REQUEST_HEAD)) {
					fuzzHeader = true;
				} else {
					fuzzHeader = false;
				}
			} else {
				System.out.println("FAIL");
			}

		} else {
			System.out.println("error");
		}
	}
	
	public void setAntiCsrfTokens(List <AntiCsrfToken> acsrfTokens) {
		if (acsrfTokens != null && acsrfTokens.size() > 0) {
			tokenPane.setAll(true, acsrfTokens.get(0), acsrfTokens.get(0).getTargetURL());
			this.getEnableTokens().setSelected(true);
			this.getEnableTokens().setEnabled(true);
			this.getTokensPane().setVisible(true);
		} else {
			tokenPane.reset();
			this.getEnableTokens().setSelected(false);
			this.getEnableTokens().setEnabled(false);
			this.getTokensPane().setVisible(false);
		}
		jPanel = null;
        this.setContentPane(getJTabbed());
        this.repaint();

	}

	public void setExtension(ExtensionFuzz extension) {
		this.extension = extension;
	}
	
	public void setDefaultCategory(String category) {
		this.getCategoryField().setSelectedItem(category);
	}
	
}
