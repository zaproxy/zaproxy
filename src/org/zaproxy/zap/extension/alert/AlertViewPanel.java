/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The ZAP Development team
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
package org.zaproxy.zap.extension.alert;


import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Vulnerabilities;
import org.zaproxy.zap.model.Vulnerability;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class AlertViewPanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AlertViewPanel.class);
	
	private JScrollPane defaultPane = null;
	private JScrollPane alertPane = null;
	private ZapTextArea defaultOutput = null;
	private JPanel alertDisplay = null;
	private CardLayout cardLayout = null;
	
	private ZapTextField alertUrl = null;
	private JLabel alertName = null;
	private JLabel alertRisk = null;
	private JLabel alertConfidence = null;
	private JLabel alertParam = null;
	private JLabel alertAttack = null;
	private JLabel alertEvidence = null;
	private ZapTextArea alertDescription = null;
	private ZapTextArea alertOtherInfo = null;
	private ZapTextArea alertSolution = null;
	private ZapTextArea alertReference = null;
	private JLabel alertCweId = null;
	private JLabel alertWascId = null;
	
	private JComboBox<String> alertEditName = null;
	private JComboBox<String> alertEditRisk = null;
	private JComboBox<String> alertEditConfidence = null;
	private JComboBox<String> alertEditParam = null;
	private ZapTextField alertEditAttack = null;
	private ZapTextField alertEditEvidence = null;
	private DefaultComboBoxModel<String> nameListModel = null;
	private DefaultComboBoxModel<String> paramListModel = null;
	private ZapNumberSpinner alertEditCweId = null;
	private ZapNumberSpinner alertEditWascId = null;
	
	private boolean editable = false;
	private Alert originalAlert = null;
	private List <Vulnerability> vulnerabilities = null;

	private HistoryReference historyRef = null;
	
    /**
     * Used to set the {@code HttpMessage} to the new alert when there is no
     * {@code historyRef}.
     */
	private HttpMessage httpMessage;

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
	 */
	private void initialize() {
		cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        this.setName("AlertView");

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
		}
		return alertPane;
	}
	
	private ZapTextArea createZapTextArea() {
		ZapTextArea ZapTextArea = new ZapTextArea(3, 30);
		ZapTextArea.setLineWrap(true);
		ZapTextArea.setWrapStyleWord(true);
		ZapTextArea.setEditable(editable);
		return ZapTextArea;
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
						FontUtils.getFont(FontUtils.Size.standard), 
						java.awt.Color.black));
		return jScrollPane;
		
	}
	
	private JPanel getAlertDisplay() {
		if (alertDisplay == null) {
			alertDisplay = new JPanel();
			alertDisplay.setLayout(new GridBagLayout());
			alertDisplay.setName("alertDisplay");
			
			// Create the labels
			
			alertEditName = new JComboBox<>();
			alertEditName.setEditable(true);
			nameListModel = new DefaultComboBoxModel<>();
			
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
								setAlertDescription(v.getDescription());
							}
							if (v.getSolution() != null && v.getSolution().length() > 0) {
								setAlertSolution(v.getSolution());
							}
							if (v.getReferences() != null) {
								StringBuilder sb = new StringBuilder();
								for (String ref : v.getReferences()) {
									sb.append(ref);
									sb.append('\n');
								}
								setAlertReference(sb.toString());
							}
							alertEditWascId.setValue(v.getWascId());
						}
					}
				}
			});

			alertEditRisk = new JComboBox<>(Alert.MSG_RISK);
			alertEditConfidence = new JComboBox<>(Alert.MSG_CONFIDENCE);
			alertEditConfidence.setSelectedItem(Alert.MSG_CONFIDENCE[Alert.CONFIDENCE_MEDIUM]);
			alertEditAttack = new ZapTextField();
			
			paramListModel = new DefaultComboBoxModel<>();
			paramListModel.addElement("");	// Default is empty so user can type anything in
			alertEditParam = new JComboBox<>();
			alertEditParam.setModel(paramListModel);
			alertEditParam.setEditable(true);
			
			alertEditEvidence = new ZapTextField();
			alertEditCweId = new ZapNumberSpinner();
			alertEditWascId = new ZapNumberSpinner();

			// Read only ones
			alertName = new JLabel();
			alertName.setFont(FontUtils.getFont(Font.BOLD));

			alertRisk = new JLabel();
			alertConfidence = new JLabel();
			alertParam = new JLabel();
			alertAttack = new JLabel();
			alertEvidence = new JLabel();
			alertCweId = new JLabel();
			alertWascId = new JLabel();

			alertUrl = new ZapTextField();
			
			alertDescription = createZapTextArea();
			JScrollPane descSp = createJScrollPane(Constant.messages.getString("alert.label.desc"));
			descSp.setViewportView(alertDescription);
			alertDescription.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				@Override
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertDescription.transferFocus();
					}
				}
			});

			alertOtherInfo = createZapTextArea();
			JScrollPane otherSp = createJScrollPane(Constant.messages.getString("alert.label.other"));
			otherSp.setViewportView(alertOtherInfo);
			alertOtherInfo.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				@Override
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertOtherInfo.transferFocus();
					}
				}
			});

			alertSolution = createZapTextArea();
			JScrollPane solutionSp = createJScrollPane(Constant.messages.getString("alert.label.solution"));
			solutionSp.setViewportView(alertSolution);
			alertSolution.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				@Override
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertSolution.transferFocus();
					}
				}
			});

			alertReference = createZapTextArea();
			JScrollPane referenceSp = createJScrollPane(Constant.messages.getString("alert.label.ref"));
			referenceSp.setViewportView(alertReference);
			alertReference.addKeyListener(new KeyAdapter() {
				// Change tab key to transfer focus to the next element
				@Override
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_TAB) {
						alertReference.transferFocus();
					}
				}
			});

			if (editable) {
				alertDisplay.add(alertEditName, 
						LayoutHelper.getGBC(0, 0, 2, 0, new Insets(1,1,1,1)));

				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.url")), 
						LayoutHelper.getGBC(0, 1, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertUrl, 
						LayoutHelper.getGBC(1, 1, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.risk")),
						LayoutHelper.getGBC(0, 2, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditRisk, 
						LayoutHelper.getGBC(1, 2, 1, 0.75D, new Insets(1,1,1,1)));

				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.confidence")),
						LayoutHelper.getGBC(0, 3, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditConfidence, 
						LayoutHelper.getGBC(1, 3, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.parameter")),
						LayoutHelper.getGBC(0, 4, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditParam,
						LayoutHelper.getGBC(1, 4, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.attack")),
						LayoutHelper.getGBC(0, 5, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditAttack,
						LayoutHelper.getGBC(1, 5, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.evidence")),
						LayoutHelper.getGBC(0, 6, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditEvidence,
						LayoutHelper.getGBC(1, 6, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.cweid")), 
						LayoutHelper.getGBC(0, 7, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditCweId, 
						LayoutHelper.getGBC(1, 7, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.wascid")),
						LayoutHelper.getGBC(0, 8, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEditWascId, 
						LayoutHelper.getGBC(1, 8, 1, 0.75D, new Insets(1,1,1,1)));
			} else {
				alertUrl.setEditable(false);
				
				alertDisplay.add(alertName, 
						LayoutHelper.getGBC(0, 0, 2, 0, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.url")), 
						LayoutHelper.getGBC(0, 1, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertUrl, 
						LayoutHelper.getGBC(1, 1, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.risk")), 
						LayoutHelper.getGBC(0, 2, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertRisk, 
						LayoutHelper.getGBC(1, 2, 1, 0.75D, new Insets(1,1,1,1)));

				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.confidence")), 
						LayoutHelper.getGBC(0, 3, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertConfidence,
						LayoutHelper.getGBC(1, 3, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.parameter")), 
						LayoutHelper.getGBC(0, 4, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertParam, 
						LayoutHelper.getGBC(1, 4, 1, 0.75D, new Insets(1,1,1,1)));

				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.attack")),
						LayoutHelper.getGBC(0, 5, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertAttack,
						LayoutHelper.getGBC(1, 5, 1, 0.75D, new Insets(1,1,1,1)));

				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.evidence")), 
						LayoutHelper.getGBC(0, 6, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertEvidence,
						LayoutHelper.getGBC(1, 6, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.cweid")),
						LayoutHelper.getGBC(0, 7, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertCweId, 
						LayoutHelper.getGBC(1, 7, 1, 0.75D, new Insets(1,1,1,1)));
				
				alertDisplay.add(new JLabel(Constant.messages.getString("alert.label.wascid")),
						LayoutHelper.getGBC(0, 8, 1, 0.25D, new Insets(1,1,1,1)));
				alertDisplay.add(alertWascId, 
						LayoutHelper.getGBC(1, 8, 1, 0.75D, new Insets(1,1,1,1)));
			}
			
			alertDisplay.add(descSp, 
					LayoutHelper.getGBC(0, 9, 2, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(1,1,1,1)));
			alertDisplay.add(otherSp, 
					LayoutHelper.getGBC(0, 10, 2, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(1,1,1,1)));
			alertDisplay.add(solutionSp, 
					LayoutHelper.getGBC(0, 11, 2, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(1,1,1,1)));
			alertDisplay.add(referenceSp, 
					LayoutHelper.getGBC(0, 12, 2, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(1,1,1,1)));
			
		}
		return alertDisplay;
	}
	
	public void displayAlert (Alert alert) {
		this.originalAlert = alert;
		
		alertUrl.setText(alert.getUri());
		
		if (editable) {
			nameListModel.addElement(alert.getName());
			alertEditName.setSelectedItem(alert.getName());
			alertEditRisk.setSelectedItem(Alert.MSG_RISK[alert.getRisk()]);
			alertEditConfidence.setSelectedItem(Alert.MSG_CONFIDENCE[alert.getConfidence()]);
			alertEditParam.setSelectedItem(alert.getParam());
			alertEditAttack.setText(alert.getAttack());
			alertEditAttack.discardAllEdits();
			alertEditEvidence.setText(alert.getEvidence());
			alertEditEvidence.discardAllEdits();
			alertEditCweId.setValue(alert.getCweId());
			alertEditWascId.setValue(alert.getWascId());
			
		} else {
			alertName.setText(alert.getName());
	
			alertRisk.setText(Alert.MSG_RISK[alert.getRisk()]);
	    	if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
	    		// Special case - theres no risk - use the green flag
				alertRisk.setIcon(new ImageIcon(Constant.OK_FLAG_IMAGE_URL));
	    	} else {
				alertRisk.setIcon(new ImageIcon(alert.getIconUrl()));
	    	}
			
			alertConfidence.setText(Alert.MSG_CONFIDENCE[alert.getConfidence()]);
			alertParam.setText(alert.getParam());
			alertAttack.setText(alert.getAttack());
			alertEvidence.setText(alert.getEvidence());
			alertCweId.setText(Integer.toString(alert.getCweId()));
			alertWascId.setText(Integer.toString(alert.getWascId()));
		}
		
		setAlertDescription(alert.getDescription());
		setAlertOtherInfo(alert.getOtherInfo());
		setAlertSolution(alert.getSolution());
		setAlertReference(alert.getReference());

		cardLayout.show(this, getAlertPane().getName());
	}
	
	public void clearAlert () {
		cardLayout.show(this, getDefaultPane().getName());

        originalAlert = null;
        historyRef = null;
        httpMessage = null;

        alertName.setText("");
        alertRisk.setText("");
        alertConfidence.setText("");
        alertParam.setText("");
        alertAttack.setText("");
        alertDescription.setText("");
        alertOtherInfo.setText("");
        alertSolution.setText("");
        alertReference.setText("");

        if (editable) {
            alertEditAttack.setText("");
            alertEditAttack.discardAllEdits();
            alertEditEvidence.setText("");
            alertEditEvidence.discardAllEdits();
            alertDescription.discardAllEdits();
            alertOtherInfo.discardAllEdits();
            alertSolution.discardAllEdits();
            alertReference.discardAllEdits();
        }
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
		}
		return defaultPane;
	}

	private ZapTextArea getDefaultOutput() {
		if (defaultOutput == null) {
			defaultOutput = new ZapTextArea();
			defaultOutput.setEditable(false);
			defaultOutput.setLineWrap(true);
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
				@Override
				public void run() {
					getDefaultOutput().append(msg);
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void clear() {
	    getDefaultOutput().setText("");
	}

	public void setParamNames(String[] paramNames) {
		for (String param : paramNames) {
			paramListModel.addElement(param);
		}

	}

	public Alert getAlert() {
		if (! editable && originalAlert != null) {
			Alert alert = originalAlert.newInstance();
			alert.setAlertId(originalAlert.getAlertId());
			alert.setName((String)alertEditName.getSelectedItem());
			alert.setParam((String)alertEditParam.getSelectedItem());
			alert.setRiskConfidence(alertEditRisk.getSelectedIndex(), 
					alertEditConfidence.getSelectedIndex());
			alert.setDescription(alertDescription.getText());
			alert.setOtherInfo(alertOtherInfo.getText());
			alert.setSolution(alertSolution.getText());
			alert.setReference(alertReference.getText());
			alert.setEvidence(alertEvidence.getText());
			alert.setCweId(alertEditCweId.getValue());
			alert.setWascId(alertEditWascId.getValue());
			alert.setHistoryRef(historyRef);
			
			return alert;
		}
		
		Alert alert = new Alert(-1, alertEditRisk.getSelectedIndex(), 
				alertEditConfidence.getSelectedIndex(), (String) alertEditName.getSelectedItem());
		alert.setHistoryRef(historyRef);
		if (originalAlert != null) {
			alert.setAlertId(originalAlert.getAlertId());
		}
		
		String uri = null;
		HttpMessage msg = null;
		if (httpMessage != null) {
		    uri = httpMessage.getRequestHeader().getURI().toString();
		    msg = httpMessage;
		} else if (historyRef != null) {
			try {
				uri = historyRef.getURI().toString();
				msg = historyRef.getHttpMessage();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else if (originalAlert != null) {
			uri = originalAlert.getUri();
			msg = originalAlert.getMessage();
		}
		alert.setDetail(alertDescription.getText(), 
				uri, 
				(String)alertEditParam.getSelectedItem(), 
				alertEditAttack.getText(),
				alertOtherInfo.getText(), 
				alertSolution.getText(), 
				alertReference.getText(), 
				alertEditEvidence.getText(),
				alertEditCweId.getValue(),
				alertEditWascId.getValue(),
				msg);
		return alert;
	}

	public Alert getOriginalAlert() {
		return this.originalAlert;
	}

	public void setHistoryRef(HistoryReference historyRef) {
		this.historyRef = historyRef;
		this.httpMessage = null;
		try {
			if (historyRef != null) {
				HttpMessage msg = historyRef.getHttpMessage();
				setParamNames(msg.getParamNames());
		        this.alertUrl.setText(msg.getRequestHeader().getURI().toString());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

    /**
     * Sets the {@code HttpMessage} that will be set to the new alert.
     * 
     * @param httpMessage
     *            the {@code HttpMessage} that will be set to the new alert
     */
    public void setHttpMessage(HttpMessage httpMessage) {
        this.httpMessage = httpMessage;
        setParamNames(httpMessage.getParamNames());
        this.alertUrl.setText(httpMessage.getRequestHeader().getURI().toString());
        this.historyRef = null;
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
		List <Vulnerability> vulns = this.getAllVulnerabilities();
		List <String> names = new ArrayList<>(vulns.size());
		for (Vulnerability v : vulns) {
			names.add(v.getAlert());
		}
		Collections.sort(names);
		return names;
	}

	private void setAlertDescription(String description) {
		setTextDiscardEditsAndInitCaretPosition(alertDescription, description);
	}

	private void setAlertOtherInfo(String otherInfo) {
		setTextDiscardEditsAndInitCaretPosition(alertOtherInfo, otherInfo);
	}

	private void setAlertSolution(String solution) {
		setTextDiscardEditsAndInitCaretPosition(alertSolution, solution);
	}

	private void setAlertReference(String reference) {
		setTextDiscardEditsAndInitCaretPosition(alertReference, reference);
	}

	private static void setTextDiscardEditsAndInitCaretPosition(ZapTextArea textArea, String text) {
		textArea.setText(text);
		textArea.discardAllEdits();
		textArea.setCaretPosition(0);
	}

}
