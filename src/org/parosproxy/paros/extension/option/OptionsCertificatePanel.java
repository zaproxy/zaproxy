/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsCertificatePanel extends AbstractParamPanel {

	private JPanel panelCertificate = null;  //  @jve:decl-index=0:visual-constraint="520,10"
	private JCheckBox chkUseClientCertificate = null;
	
    public OptionsCertificatePanel() {
        super();
 		initialize();
   }


    private static final String[] ROOT = {};
    
	private JPanel panelLocation = null;
	private JLabel lblLocation = null;
	private JTextField txtLocation = null;
	private JButton btnLocation = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel = null;
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName("Certificate");
        this.add(getPanelCertificate(), getPanelCertificate().getName());

	}
	/**
	 * This method initializes panelCertificate	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelCertificate() {
		if (panelCertificate == null) {
			jLabel = new JLabel();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jLabel1 = new JLabel();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			panelCertificate = new JPanel();
			panelCertificate.setLayout(new GridBagLayout());
			panelCertificate.setSize(114, 132);
			panelCertificate.setName("Certificate");
			panelCertificate.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.ipadx = 113;
			gridBagConstraints2.ipady = 15;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.gridheight = 2;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jLabel1.setText("");
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 5;
			gridBagConstraints6.weightx = 1.0D;
			gridBagConstraints6.weighty = 1.0D;
			jLabel.setText("<html><body><p>Select client certificate file (PKCS#12).  You will be prompted for the passphrase after the certificate file is selected.  The PKCS#12 file should be exported from browsers.  </p><p>Certificate setting will not be stored in options and you will need to enable certificate next time you restart Paros.</p></body></html>");
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0D;
			gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.insets = new java.awt.Insets(5,0,5,0);
			panelCertificate.add(jLabel, gridBagConstraints11);
			panelCertificate.add(getChkUseClientCertificate(), gridBagConstraints1);
			panelCertificate.add(getPanelLocation(), gridBagConstraints2);
			panelCertificate.add(jLabel1, gridBagConstraints6);
		}
		return panelCertificate;
	}
	/**
	 * This method initializes chkUseClientCertificate	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkUseClientCertificate() {
		if (chkUseClientCertificate == null) {
			chkUseClientCertificate = new JCheckBox();
			chkUseClientCertificate.setText("Use client certificate");
			chkUseClientCertificate.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkUseClientCertificate.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkUseClientCertificate.addItemListener(new java.awt.event.ItemListener() { 
				public void itemStateChanged(java.awt.event.ItemEvent e) {
				    getPanelLocation().setEnabled(chkUseClientCertificate.isSelected());
				    getTxtLocation().setEnabled(chkUseClientCertificate.isSelected());
				    getBtnLocation().setEnabled(chkUseClientCertificate.isSelected());
				    if (chkUseClientCertificate.isSelected()) {
				        getBtnLocation().doClick();
				    }
				}
			});
		}
		return chkUseClientCertificate;
	}
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkUseClientCertificate().setSelected(options.getCertificateParam().isUseClientCert());
	    getBtnLocation().setEnabled(getChkUseClientCertificate().isSelected());
	    getTxtLocation().setText(options.getCertificateParam().getClientCertLocation());

	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    OptionsParamCertificate certParam = options.getCertificateParam();
        certParam.setUseClientCert(getChkUseClientCertificate().isSelected());
        certParam.setEnableCertificate(certParam.isUseClientCert());
	    
	}
	
	/**
	 * This method initializes panelLocation	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelLocation() {
		if (panelLocation == null) {
			lblLocation = new JLabel();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			panelLocation = new JPanel();
			panelLocation.setLayout(new GridBagLayout());
			lblLocation.setText("Location:");
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridheight = 0;
			gridBagConstraints5.gridwidth = 0;
			panelLocation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Client certificate", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			panelLocation.add(lblLocation, gridBagConstraints3);
			panelLocation.add(getTxtLocation(), gridBagConstraints4);
			panelLocation.add(getBtnLocation(), gridBagConstraints5);
		}
		return panelLocation;
	}
	/**
	 * This method initializes txtLocation	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getTxtLocation() {
		if (txtLocation == null) {
			txtLocation = new JTextField();
			txtLocation.setEditable(false);
		}
		return txtLocation;
	}
	
	/**
	 * This method initializes btnLocation	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnLocation() {
		if (btnLocation == null) {
			btnLocation = new JButton();
			btnLocation.setText("...");
			btnLocation.setPreferredSize(new java.awt.Dimension(25,25));
			btnLocation.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser chooser = null;
                    if (getTxtLocation().getText()!= null && getTxtLocation().getText().length() >0) {
                        File file = new File(getTxtLocation().getText());                        
                        chooser = new JFileChooser(file);
                    } else  {
                        chooser = new JFileChooser();
                    }
		            OptionsParamCertificate certParam = Model.getSingleton().getOptionsParam().getCertificateParam();
				    int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
				    if(rc == JFileChooser.APPROVE_OPTION) {
				        try {
				            File keyFile = chooser.getSelectedFile();
				            if (keyFile == null) {
				                return;
				            }
				            
				            JPasswordField pwd = new JPasswordField("");
				            String label = "Please input passphrase for the PKCS#12 certificate:"; 
				            Object[] objArray = {label, pwd};
				            JOptionPane pane = new JOptionPane(objArray, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
				            JDialog dialog = pane.createDialog(View.getSingleton().getMainFrame(), "Passphrase");
				            dialog.setVisible(true);
				            int result = ((Integer) pane.getValue()).intValue();
				            if (result != JOptionPane.OK_OPTION) {
				                return;
				            }
				            char[] passPhrase = pwd.getPassword();
				            if (passPhrase == null) {
				                return;
				            }

				            certParam.setUseClientCert(true);
				            certParam.setClientCertLocation(keyFile.getAbsolutePath());
				            certParam.setCertificate(passPhrase);
				            // clean up passphrase asap
				            for (int i=0; i<passPhrase.length; i++) {
				                passPhrase[i] = ' ';
				            }
				            getTxtLocation().setText(keyFile.getAbsolutePath());
				        } catch (Exception ex) {
				            View.getSingleton().showWarningDialog("Error reading PKCS#12 certificate.  Please use correct passphrase \r\nand use certificate exported from Mozilla/Netscape.");
				            ex.printStackTrace();
				            certParam.setUseClientCert(false);
				            certParam.setClientCertLocation("");
				            
				        }
				        
				    }
				}
			});
		}
		return btnLocation;
	}
	

        }  //  @jve:decl-index=0:visual-constraint="10,10"
