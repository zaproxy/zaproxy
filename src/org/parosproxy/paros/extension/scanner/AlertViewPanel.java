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
package org.parosproxy.paros.extension.scanner;


import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AlertViewPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane defaultPane = null;
	private JScrollPane alertPane = null;
	private JTextArea defaultOutput = null;
	private JPanel alertDisplay = null;
	private CardLayout cardLayout = null;
	
	private JLabel alertName = null;
	private JLabel alertRisk = null;
	private JLabel alertReliability = null;
	private JLabel alertParam = null;
	private JTextArea alertDescription = null;
	private JTextArea alertOtherInfo = null;
	private JTextArea alertSolution = null;
	private JTextArea alertReference = null;
	
	private JComboBox alertEditName = null;
	private JComboBox alertEditRisk = null;
	private JComboBox alertEditReliability = null;
	private JComboBox alertEditParam = null;
	private DefaultComboBoxModel nameListModel = null;
	private DefaultComboBoxModel paramListModel = null;
	
	private boolean editable = false;
	private Alert originalAlert = null;
	private List <Vulnerability> vulnerabilities = null;

	private HistoryReference historyRef = null;

	/**
     * 
     */
    public AlertViewPanel() {
    	this (false);
    }
    
    public AlertViewPanel(boolean editable) {
        super();
        this.editable = editable;
 		initialize();
    }
    
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        this.setName("Alert View");

        if (! editable) {
        	this.add(getDefaultPane(), getDefaultPane().getName());
        }
        this.add(getAlertPane(), getAlertPane().getName());
			
	}
	
	private JScrollPane getAlertPane() {
		if (alertPane == null) {
			alertPane = new JScrollPane();
			alertPane.setViewportView(getAlertDisplay());
			alertPane.setName("alertPane");
			alertPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return alertPane;
	}
	
	private JTextArea createJTextArea() {
		JTextArea jTextArea = new JTextArea();
		jTextArea = new JTextArea(3, 30);
		jTextArea.setLineWrap(true);
		jTextArea.setWrapStyleWord(true);
		jTextArea.setEditable(editable);
		return jTextArea;
	}
	
	private JScrollPane createJScrollPane(String name) {
		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setBorder(
				BorderFactory.createTitledBorder(
						null, name, 
						TitledBorder.DEFAULT_JUSTIFICATION, 
						javax.swing.border.TitledBorder.DEFAULT_POSITION, 
						new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
						java.awt.Color.black));
		return jScrollPane;
		
	}
	
	private JPanel getAlertDisplay() {
		if (alertDisplay == null) {
			alertDisplay = new JPanel();
			alertDisplay.setLayout(new GridBagLayout());
			alertDisplay.setName("alertDisplay");
			
			// Create the labels
			
			if (editable) {
				alertEditName = new JComboBox();
				alertEditName.setEditable(true);
				nameListModel = new DefaultComboBoxModel();
				
				List <String> allVulns = getAllVulnerabilityNames();
				nameListModel.addElement("");	// Default to blank
				for (String vuln : allVulns) {
					nameListModel.addElement(vuln);
				}
				
				alertEditName.setModel(nameListModel);
				alertEditName.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if ("comboBoxChanged".equals(e.getActionCommand())) {
							Vulnerability v = getVulnerability((String)alertEditName.getSelectedItem());
							if (v != null) {
								if (v.getDescription() != null && v.getDescription().length() > 0) {
									alertDescription.setText(v.getDescription());
								}
								if (v.getSolution() != null && v.getSolution().length() > 0) {
									alertSolution.setText(v.getSolution());
								}
								if (v.getReferences() != null) {
									StringBuffer sb = new StringBuffer();
									for (String ref : v.getReferences()) {
										sb.append(ref);
										sb.append("\n");
									}
									alertReference.setText(sb.toString());
								}
							}
						}
					}
				});

				alertEditRisk = new JComboBox(Alert.MSG_RISK);
				alertEditReliability = new JComboBox(Alert.MSG_RELIABILITY);
				alertEditReliability.setSelectedItem(Alert.MSG_RELIABILITY[Alert.SUSPICIOUS]);
				alertEditParam = new JComboBox();
				
				paramListModel = new DefaultComboBoxModel();
				paramListModel.addElement("");	// Default is empty so user can type anything in
				alertEditParam.setModel(paramListModel);
				
				alertEditParam.setEditable(true);
				
			} else {
				alertName = new JLabel();
				Font f = alertName.getFont();
				alertName.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

				alertRisk = new JLabel();
				alertReliability = new JLabel();
				alertParam = new JLabel();
			}
			
			alertDescription = createJTextArea();
			JScrollPane descSp = createJScrollPane("Description");
			descSp.setViewportView(alertDescription);
			alertDescription.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertDescription.transferFocus();
					}
				}
			});

			alertOtherInfo = createJTextArea();
			JScrollPane otherSp = createJScrollPane("Other Info");
			otherSp.setViewportView(alertOtherInfo);
			alertOtherInfo.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertOtherInfo.transferFocus();
					}
				}
			});

			alertSolution = createJTextArea();
			JScrollPane solutionSp = createJScrollPane("Solution");
			solutionSp.setViewportView(alertSolution);
			alertSolution.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertSolution.transferFocus();
					}
				}
			});

			alertReference = createJTextArea();
			JScrollPane referenceSp = createJScrollPane("Reference");
			referenceSp.setViewportView(alertReference);
			alertReference.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertReference.transferFocus();
					}
				}
			});

			java.awt.GridBagConstraints gbc00 = new GridBagConstraints();
			gbc00.gridy = 0;
			gbc00.gridx = 0;
			gbc00.insets = new java.awt.Insets(1,1,1,1);
			gbc00.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc00.fill = java.awt.GridBagConstraints.BOTH;
			//gbc00.weightx = 1.0D;
			gbc00.gridwidth = 4;

			java.awt.GridBagConstraints gbc10 = new GridBagConstraints();
			gbc10.gridy = 1;
			gbc10.gridx = 0;
			gbc10.insets = new java.awt.Insets(1,1,1,1);
			gbc10.anchor = java.awt.GridBagConstraints.WEST;
			//gbc10.weightx = 0.5D;

			java.awt.GridBagConstraints gbc11 = new GridBagConstraints();
			gbc11.gridy = 1;
			gbc11.gridx = 1;
			gbc11.insets = new java.awt.Insets(1,1,1,1);
			gbc11.anchor = java.awt.GridBagConstraints.WEST;
			//gbc11.weightx = 1.0D;

			java.awt.GridBagConstraints gbc12 = new GridBagConstraints();
			gbc12.gridy = 2;
			gbc12.gridx = 0;
			gbc12.insets = new java.awt.Insets(1,1,1,1);
			gbc12.anchor = java.awt.GridBagConstraints.WEST;
			//gbc12.weightx = 0.5D;

			java.awt.GridBagConstraints gbc13 = new GridBagConstraints();
			gbc13.gridy = 2;
			gbc13.gridx = 1;
			gbc13.insets = new java.awt.Insets(1,1,1,1);
			gbc13.anchor = java.awt.GridBagConstraints.WEST;
			//gbc13.weightx = 1.0D;

			java.awt.GridBagConstraints gbc20 = new GridBagConstraints();
			gbc20.gridy = 3;
			gbc20.gridx = 0;
			gbc20.insets = new java.awt.Insets(1,1,1,1);
			gbc20.anchor = java.awt.GridBagConstraints.WEST;
			//gbc20.weightx = 0.5D;

			java.awt.GridBagConstraints gbc21 = new GridBagConstraints();
			gbc21.gridy = 3;
			gbc21.gridx = 1;
			gbc21.insets = new java.awt.Insets(1,1,1,1);
			gbc21.anchor = java.awt.GridBagConstraints.WEST;
			//gbc21.weightx = 1.0D;

			java.awt.GridBagConstraints gbc30 = new GridBagConstraints();
			gbc30.gridy = 4;
			gbc30.gridx = 0;
			gbc30.insets = new java.awt.Insets(1,1,1,1);
			gbc30.anchor = java.awt.GridBagConstraints.WEST;
			gbc30.fill = java.awt.GridBagConstraints.BOTH;
			gbc30.weightx = 1.0D;
			gbc30.weighty = 1.0D;
			gbc30.gridwidth = 2;
			gbc30.gridheight = 1;

			java.awt.GridBagConstraints gbc40 = new GridBagConstraints();
			gbc40.gridy = 5;
			gbc40.gridx = 0;
			gbc40.insets = new java.awt.Insets(1,1,1,1);
			gbc40.anchor = java.awt.GridBagConstraints.WEST;
			gbc40.fill = java.awt.GridBagConstraints.BOTH;
			gbc40.weightx = 1.0D;
			gbc40.weighty = 1.0D;
			gbc40.gridwidth = 2;
			gbc40.gridheight = 1;

			java.awt.GridBagConstraints gbc50 = new GridBagConstraints();
			gbc50.gridy = 6;
			gbc50.gridx = 0;
			gbc50.insets = new java.awt.Insets(1,1,1,1);
			gbc50.anchor = java.awt.GridBagConstraints.WEST;
			gbc50.fill = java.awt.GridBagConstraints.BOTH;
			gbc50.weightx = 1.0D;
			gbc50.weighty = 1.0D;
			gbc50.gridwidth = 2;
			gbc50.gridheight = 1;

			java.awt.GridBagConstraints gbc60 = new GridBagConstraints();
			gbc60.gridy = 7;
			gbc60.gridx = 0;
			gbc60.insets = new java.awt.Insets(1,1,1,1);
			gbc60.anchor = java.awt.GridBagConstraints.WEST;
			gbc60.fill = java.awt.GridBagConstraints.BOTH;
			gbc60.weightx = 1.0D;
			gbc60.weighty = 1.0D;
			gbc60.gridwidth = 2;
			gbc60.gridheight = 1;

			if (editable) {
				alertDisplay.add(alertEditName, gbc00);
				alertDisplay.add(new JLabel("Risk: "), gbc10);
				alertDisplay.add(alertEditRisk, gbc11);
				alertDisplay.add(new JLabel("Reliability: "), gbc12);
				alertDisplay.add(alertEditReliability, gbc13);
				alertDisplay.add(new JLabel("Parameter: "), gbc20);
				alertDisplay.add(alertEditParam, gbc21);
			} else {
				alertDisplay.add(alertName, gbc00);
				alertDisplay.add(new JLabel("Risk: "), gbc10);
				alertDisplay.add(alertRisk, gbc11);
				alertDisplay.add(new JLabel("Reliability: "), gbc12);
				alertDisplay.add(alertReliability, gbc13);
				alertDisplay.add(new JLabel("Parameter: "), gbc20);
				alertDisplay.add(alertParam, gbc21);
			}
			
			alertDisplay.add(descSp, gbc30);
			alertDisplay.add(otherSp, gbc40);
			alertDisplay.add(solutionSp, gbc50);
			alertDisplay.add(referenceSp, gbc60);
			
		}
		return alertDisplay;
	}
	
	public void displayAlert (Alert alert) {
		this.originalAlert = alert;
		
		if (editable) {
			nameListModel.addElement(alert.getAlert());
			alertEditName.setSelectedItem(alert.getAlert());
			alertEditRisk.setSelectedItem(Alert.MSG_RISK[alert.getRisk()]);
			alertEditReliability.setSelectedItem(Alert.MSG_RELIABILITY[alert.getReliability()]);
			alertEditParam.setSelectedItem(alert.getParam());
			alertDescription.setText(alert.getDescription());
			alertOtherInfo.setText(alert.getOtherInfo());
			alertSolution.setText(alert.getSolution());
			alertReference.setText(alert.getReference());
			
		} else {
			alertName.setText(alert.getAlert());
	
			alertRisk.setText(Alert.MSG_RISK[alert.getRisk()]);
	    	switch (alert.getRisk()) {
	    	case Alert.RISK_INFO:	// blue flag
				alertRisk.setIcon(new ImageIcon(getClass().getResource("/resource/icon/10/073.png")));
	    		break;
	    	case Alert.RISK_LOW:	// yellow flag
				alertRisk.setIcon(new ImageIcon(getClass().getResource("/resource/icon/10/074.png")));
	    		break;
	    	case Alert.RISK_MEDIUM:	// Orange flag
				alertRisk.setIcon(new ImageIcon(getClass().getResource("/resource/icon/10/076.png")));
	    		break;
	    	case Alert.RISK_HIGH:	// Red flag
				alertRisk.setIcon(new ImageIcon(getClass().getResource("/resource/icon/10/071.png")));
	    		break;
	    	}
	    	if (alert.getReliability() == Alert.FALSE_POSITIVE) {
	    		// Special case - theres no risk - use the green flag
				alertRisk.setIcon(new ImageIcon(getClass().getResource("/resource/icon/10/072.png")));
	    	}
			
			alertReliability.setText(Alert.MSG_RELIABILITY[alert.getReliability()]);
			alertParam.setText(alert.getParam());
			alertDescription.setText(alert.getDescription());
			alertOtherInfo.setText(alert.getOtherInfo());
			alertSolution.setText(alert.getSolution());
			alertReference.setText(alert.getReference());
		}

		cardLayout.show(this, getAlertPane().getName());
	}
	
	public void clearAlert () {
		cardLayout.show(this, getDefaultPane().getName());
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getDefaultPane() {
		if (defaultPane == null) {
			defaultPane = new JScrollPane();
			defaultPane.setViewportView(getDefaultOutput());
			defaultPane.setName("defaultPane");
			defaultPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			defaultPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return defaultPane;
	}
	/**
	 * This method initializes txtOutput	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	private JTextArea getDefaultOutput() {
		if (defaultOutput == null) {
			defaultOutput = new JTextArea();
			defaultOutput.setEditable(false);
			defaultOutput.setLineWrap(true);
			defaultOutput.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			defaultOutput.setName("");
			defaultOutput.append(Constant.messages.getString("alerts.label.defaultMessage"));
		}
		return defaultOutput;
	}
	
	public void append(final String msg) {
		if (EventQueue.isDispatchThread()) {
			getDefaultOutput().append(msg);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					getDefaultOutput().append(msg);
				}
			});
		} catch (Exception e) {
		}
	}
	
	public void clear() {
	    getDefaultOutput().setText("");
	}

	public void setParamNames(String[] paramNames) {
		alertEditParam = new JComboBox(paramNames);
		for (String param : paramNames) {
			paramListModel.addElement(param);
		}
	}

	public Alert getAlert() {
		if (! editable && originalAlert != null) {
			Alert alert = originalAlert.newInstance();
			alert.setAlertId(originalAlert.getAlertId());
			alert.setAlert((String)alertEditName.getSelectedItem());
			alert.setParam((String)alertEditParam.getSelectedItem());
			alert.setRiskReliability(alertEditRisk.getSelectedIndex(), 
					alertEditReliability.getSelectedIndex());
			alert.setDescription(alertDescription.getText());
			alert.setOtherInfo(alertOtherInfo.getText());
			alert.setSolution(alertSolution.getText());
			alert.setReference(alertReference.getText());
			alert.setHistoryRef(historyRef);
			return alert;
		} else {
			Alert alert = new Alert(-1, alertEditRisk.getSelectedIndex(), 
					alertEditReliability.getSelectedIndex(), (String) alertEditName.getSelectedItem());
			alert.setHistoryRef(historyRef);
			if (originalAlert != null) {
				alert.setAlertId(originalAlert.getAlertId());
			}
			
			String uri = null;
			HttpMessage msg = null;
			if (historyRef != null) {
				try {
					uri = historyRef.getHttpMessage().getRequestHeader().getURI().toString();
					msg = historyRef.getHttpMessage();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (originalAlert != null) {
				uri = originalAlert.getUri();
				msg = originalAlert.getMessage();
			}
			alert.setDetail(alertDescription.getText(), 
					uri, 
					(String)alertEditParam.getSelectedItem(), 
					alertOtherInfo.getText(), 
					alertSolution.getText(), 
					alertReference.getText(), 
					msg);
			return alert;
		}
	}

	public Alert getOriginalAlert() {
		return this.originalAlert;
	}

	public void setHistoryRef(HistoryReference historyRef) {
		this.historyRef = historyRef;
		try {
			if (historyRef != null) {
				setParamNames(historyRef.getHttpMessage().getParamNames());
			}
		} catch (HttpMalformedHeaderException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	private List <Vulnerability> getAllVulnerabilities() {
		if (vulnerabilities == null) {
			vulnerabilities = Vulnerabilities.getAllVulnerabilities();
		}
		return vulnerabilities;
	}
	
	private Vulnerability getVulnerability (String alert) {
		if (alert == null) {
			return null;
		}
		List <Vulnerability> vulns = this.getAllVulnerabilities();
		for (Vulnerability v : vulns) {
			if (alert.equals(v.getAlert())) {
				return v;
			}
		}
		return null;
	}

	private List<String> getAllVulnerabilityNames() {
		List <String> names = new ArrayList<String>();
		List <Vulnerability> vulns = this.getAllVulnerabilities();
		for (Vulnerability v : vulns) {
			names.add(v.getAlert());
		}
		Collections.sort(names);
		return names;
	}

}
