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

//TODO: Buttons should be gray
import java.awt.CardLayout;
import java.awt.Dialog;
import java.io.File;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

import ch.csnc.extension.httpclient.SSLContextManager;
import ch.csnc.extension.ui.AliasTableModel;
import ch.csnc.extension.ui.CertificateView;
import ch.csnc.extension.ui.DriversView;
import ch.csnc.extension.util.DriverConfiguration;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsCertificatePanel extends AbstractParamPanel implements Observer{

	private static final long serialVersionUID = 4350957038174673492L;

	private javax.swing.JButton addPkcs11Button;
	private javax.swing.JButton addPkcs12Button;
	private javax.swing.JScrollPane aliasScrollPane;
	private javax.swing.JTable aliasTable;
	private javax.swing.JButton browseButton;
	private javax.swing.JLabel certificateLabel;
	private javax.swing.JPanel certificatePanel;
	private javax.swing.JTextField certificateTextField;
	private javax.swing.JTabbedPane certificatejTabbedPane;
	private javax.swing.JPanel cryptoApiPanel;
	private javax.swing.JLabel cryptoApiLabel;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton driverButton;
	private javax.swing.JComboBox driverComboBox;
	private javax.swing.JLabel driverLabel;
	private javax.swing.JLabel fileLabel;
	private javax.swing.JTextField fileTextField;
	private javax.swing.JList keyStoreList;
	private javax.swing.JPanel keyStorePanel;
	private javax.swing.JScrollPane keyStoreScrollPane;
	private javax.swing.JLabel passwordPkcs11Label;
	private javax.swing.JLabel passwordPkcs12Label;
	private javax.swing.JPanel pkcs11Panel;
	private javax.swing.JPasswordField pkcs11PasswordField;
	private javax.swing.JPanel pkcs12Panel;
	private javax.swing.JPasswordField pkcs12PasswordField;
	private javax.swing.JButton setActiveButton;
	private javax.swing.JButton showActiveCertificateButton;
	private javax.swing.JButton showAliasButton;
	private javax.swing.JLabel textLabel;
	private javax.swing.JCheckBox useClientCertificateCheckBox;
	

	private SSLContextManager contextManager;
	private DefaultListModel keyStoreListModel;
	private AliasTableModel aliasTableModel;
	private DriverConfiguration driverConfig;

	public OptionsCertificatePanel() {
		super();
		initialize();
	}


	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {

		contextManager = Model.getSingleton().getOptionsParam().getCertificateParam().getSSLContextManager();

		keyStoreListModel = new DefaultListModel();
		aliasTableModel = new AliasTableModel(contextManager);

		this.setLayout(new CardLayout());
		this.setName("Certificate");

		JPanel certificatePanel = getPanelCertificate();
		this.add(certificatePanel, certificatePanel.getName());

		driverConfig = new DriverConfiguration();
		updateDriverComboBox();
		driverConfig.addObserver(this);

		Certificate cert =contextManager.getDefaultCertificate();
		if(cert!=null) {
			certificateTextField.setText(cert.toString());
		}

	}


	private void updateDriverComboBox() {
		driverComboBox.removeAllItems();
		for (String name : driverConfig.getNames() ) {
			driverComboBox.addItem(name);
		}
		driverComboBox.repaint();

	}
	/**
	 * This method initializes panelCertificate
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelCertificate() {
		if (certificatePanel == null) {

			//**************************************************************************
			//begin netbeans code
			//**************************************************************************
			certificatePanel = new javax.swing.JPanel();
			certificatejTabbedPane = new javax.swing.JTabbedPane();
			keyStorePanel = new javax.swing.JPanel();
			setActiveButton = new javax.swing.JButton();
			showAliasButton = new javax.swing.JButton();
			aliasScrollPane = new javax.swing.JScrollPane();
			aliasTable = new javax.swing.JTable();
			deleteButton = new javax.swing.JButton();
			keyStoreScrollPane = new javax.swing.JScrollPane();
			keyStoreList = new javax.swing.JList();
			pkcs12Panel = new javax.swing.JPanel();
			fileLabel = new javax.swing.JLabel();
			fileTextField = new javax.swing.JTextField();
			browseButton = new javax.swing.JButton();
			passwordPkcs12Label = new javax.swing.JLabel();
			addPkcs12Button = new javax.swing.JButton();
			pkcs12PasswordField = new javax.swing.JPasswordField();
			pkcs11Panel = new javax.swing.JPanel();
			driverLabel = new javax.swing.JLabel();
			driverComboBox = new javax.swing.JComboBox();
			driverButton = new javax.swing.JButton();
			passwordPkcs11Label = new javax.swing.JLabel();
			cryptoApiLabel = new javax.swing.JLabel();
			addPkcs11Button = new javax.swing.JButton();
			pkcs11PasswordField = new javax.swing.JPasswordField();
			cryptoApiPanel = new javax.swing.JPanel();
			useClientCertificateCheckBox = new javax.swing.JCheckBox();
			textLabel = new javax.swing.JLabel();
			certificateLabel = new javax.swing.JLabel();
			certificateTextField = new javax.swing.JTextField();
			showActiveCertificateButton = new javax.swing.JButton();

			certificatejTabbedPane.setEnabled(false);

			setActiveButton.setText("Set Active");
			setActiveButton.setEnabled(false);
			setActiveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					setActiveButtonActionPerformed(evt);
				}
			});

			showAliasButton.setText("->");
			showAliasButton.setEnabled(false);
			showAliasButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
			showAliasButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					showAliasButtonActionPerformed(evt);
				}
			});

			aliasTable.setModel(aliasTableModel);
			aliasTable.setTableHeader(null);
			aliasScrollPane.setViewportView(aliasTable);

			deleteButton.setText("Delete");
			deleteButton.setEnabled(false);
			deleteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					deleteButtonActionPerformed(evt);
				}
			});

			keyStoreList.setModel(keyStoreListModel);
			keyStoreList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					keyStoreListMouseClicked(evt);
				}
			});
			keyStoreScrollPane.setViewportView(keyStoreList);

			javax.swing.GroupLayout keyStorePanelLayout = new javax.swing.GroupLayout(keyStorePanel);
			keyStorePanel.setLayout(keyStorePanelLayout);
			keyStorePanelLayout.setHorizontalGroup(
					keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, keyStorePanelLayout.createSequentialGroup()
							.addGroup(keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addComponent(deleteButton)
									.addComponent(keyStoreScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
											.addGroup(keyStorePanelLayout.createSequentialGroup()
													.addComponent(setActiveButton)
													.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
													.addComponent(showAliasButton))
													.addComponent(aliasScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)))
			);
			keyStorePanelLayout.setVerticalGroup(
					keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, keyStorePanelLayout.createSequentialGroup()
							.addGroup(keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addComponent(aliasScrollPane, 0, 0, Short.MAX_VALUE)
									.addComponent(keyStoreScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addGroup(keyStorePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
											.addComponent(deleteButton)
											.addComponent(setActiveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
											.addComponent(showAliasButton)))
			);

			keyStorePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {deleteButton, setActiveButton, showAliasButton});

			certificatejTabbedPane.addTab("KeyStore", keyStorePanel);

			fileLabel.setText("File");

			browseButton.setText("Browse");
			browseButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					browseButtonActionPerformed(evt);
				}
			});

			passwordPkcs12Label.setText("Password");

			addPkcs12Button.setText("Add to keystore");
			addPkcs12Button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					addPkcs12ButtonActionPerformed(evt);
				}
			});

			javax.swing.GroupLayout pkcs12PanelLayout = new javax.swing.GroupLayout(pkcs12Panel);
			pkcs12Panel.setLayout(pkcs12PanelLayout);
			pkcs12PanelLayout.setHorizontalGroup(
					pkcs12PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(pkcs12PanelLayout.createSequentialGroup()
							.addGroup(pkcs12PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pkcs12PanelLayout.createSequentialGroup()
											.addContainerGap()
											.addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
											.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(browseButton))
											.addGroup(pkcs12PanelLayout.createSequentialGroup()
													.addGap(12, 12, 12)
													.addComponent(fileLabel))
													.addGroup(pkcs12PanelLayout.createSequentialGroup()
															.addContainerGap()
															.addComponent(passwordPkcs12Label))
															.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pkcs12PanelLayout.createSequentialGroup()
																	.addContainerGap(270, Short.MAX_VALUE)
																	.addComponent(addPkcs12Button))
																	.addGroup(pkcs12PanelLayout.createSequentialGroup()
																			.addContainerGap()
																			.addComponent(pkcs12PasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)))
																			.addContainerGap())
			);
			pkcs12PanelLayout.setVerticalGroup(
					pkcs12PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pkcs12PanelLayout.createSequentialGroup()
							.addComponent(fileLabel)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(pkcs12PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(browseButton)
									.addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(passwordPkcs12Label)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(pkcs12PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(addPkcs12Button)
									.addGap(70, 70, 70))
			);

			pkcs12PanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addPkcs12Button, browseButton, fileTextField, pkcs12PasswordField});

			certificatejTabbedPane.addTab("PKCS#12", pkcs12Panel);

			driverLabel.setText("Driver");

			driverButton.setText("...");
			driverButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
			driverButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					driverButtonActionPerformed(evt);
				}
			});

			passwordPkcs11Label.setText("PIN Code");

			addPkcs11Button.setText("Add to keystore");
			addPkcs11Button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					addPkcs11ButtonActionPerformed(evt);
				}
			});

			javax.swing.GroupLayout pkcs11PanelLayout = new javax.swing.GroupLayout(pkcs11Panel);
			pkcs11Panel.setLayout(pkcs11PanelLayout);
			pkcs11PanelLayout.setHorizontalGroup(
					pkcs11PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(pkcs11PanelLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(pkcs11PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
									.addComponent(pkcs11PasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
									.addComponent(driverLabel)
									.addComponent(passwordPkcs11Label)
									.addGroup(pkcs11PanelLayout.createSequentialGroup()
											.addComponent(driverComboBox, 0, 336, Short.MAX_VALUE)
											.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(driverButton))
											.addComponent(addPkcs11Button, javax.swing.GroupLayout.Alignment.TRAILING))
											.addContainerGap())
			);
			pkcs11PanelLayout.setVerticalGroup(
					pkcs11PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(pkcs11PanelLayout.createSequentialGroup()
							.addComponent(driverLabel)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(pkcs11PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(driverButton)
									.addComponent(driverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(passwordPkcs11Label)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(pkcs11PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
									.addComponent(addPkcs11Button)
									.addGap(58, 58, 58))
			);

			pkcs11PanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addPkcs11Button, driverButton, driverComboBox, pkcs11PasswordField});

			certificatejTabbedPane.addTab("PKCS#11", pkcs11Panel);

			javax.swing.GroupLayout cryptoApiPanelLayout = new javax.swing.GroupLayout(cryptoApiPanel);
			cryptoApiPanel.setLayout(cryptoApiPanelLayout);
			cryptoApiLabel.setText("Crypto API is not working yet - Sorry");
			cryptoApiPanelLayout.setHorizontalGroup(
					cryptoApiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGap(0, 389, Short.MAX_VALUE)
					.addComponent(cryptoApiLabel)

			);
			cryptoApiPanelLayout.setVerticalGroup(
					cryptoApiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGap(0, 124, Short.MAX_VALUE)
					.addComponent(cryptoApiLabel)
			);

			certificatejTabbedPane.addTab("CrytoAPI", cryptoApiPanel);

			useClientCertificateCheckBox.setText("Use client certificate");
			useClientCertificateCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			useClientCertificateCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
			useClientCertificateCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					useClientCertificateCheckBoxActionPerformed(evt);
				}
			});

			textLabel.setText("<html><body><p> Add your keystore and select the desired certificate. </p> <p>Certificate setting will not be stored in options and you will  need to enable certificate next time you restart Paros. </p></body></html>");

			certificateLabel.setText("Active certificate");

			certificateTextField.setEnabled(false);

			showActiveCertificateButton.setText("->");
			showActiveCertificateButton.setActionCommand(">");
			showActiveCertificateButton.setEnabled(false);
			showActiveCertificateButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
			showActiveCertificateButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					showActiveCertificateButtonActionPerformed(evt);
				}
			});

			javax.swing.GroupLayout certificatePanelLayout = new javax.swing.GroupLayout(certificatePanel);
			certificatePanel.setLayout(certificatePanelLayout);
			certificatePanelLayout.setHorizontalGroup(
					certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addGroup(certificatePanelLayout.createSequentialGroup()
									.addComponent(textLabel, 0, 0, Short.MAX_VALUE)
									.addContainerGap())
									.addGroup(certificatePanelLayout.createSequentialGroup()
											.addGap(2, 2, 2)
											.addGroup(certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
													.addComponent(certificatejTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
													.addGroup(certificatePanelLayout.createSequentialGroup()
															.addGroup(certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																	.addComponent(useClientCertificateCheckBox)
																	.addComponent(certificateLabel)
																	.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, certificatePanelLayout.createSequentialGroup()
																			.addComponent(certificateTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																			.addComponent(showActiveCertificateButton)
																			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
																			.addContainerGap()))))
			);
			certificatePanelLayout.setVerticalGroup(
					certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(certificatePanelLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(textLabel)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(useClientCertificateCheckBox)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(certificatejTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(certificateLabel)
							.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(certificatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
									.addComponent(certificateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
									.addComponent(showActiveCertificateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addContainerGap())
			);

			certificatePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {certificateTextField, showActiveCertificateButton});

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(certificatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addComponent(certificatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addContainerGap())
			);



			//**************************************************************************
			//end netbeans code
			//**************************************************************************
		}
		return certificatePanel;
	}


	private void keyStoreListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_keyStoreListMouseClicked
		int keystore = keyStoreList.getSelectedIndex();
		try {
			aliasTableModel.setKeystore(keystore);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, new String[] {"Error accessing key store: ", e.toString()}, "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}//GEN-LAST:event_keyStoreListMouseClicked

	private void showActiveCertificateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showActiveCertificateButtonActionPerformed
		Certificate cert = contextManager.getDefaultCertificate();
		if(cert!=null) {
			new Dialog(new CertificateView(cert.toString()), false);
		}
	}//GEN-LAST:event_showActiveCertificateButtonActionPerformed

	private void addPkcs11ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPkcs11ButtonActionPerformed

		try {
			String name = driverConfig.getNames().get(driverComboBox.getSelectedIndex());;
			if (name.equals("")) return;

			String library = driverConfig.getPaths().get(driverComboBox.getSelectedIndex());
			System.out.println("library: " +library);
			if (library.equals("")) return;
			
			Integer slot = driverConfig.getSlots().get(driverComboBox.getSelectedIndex());
			System.out.println("slot: " + slot);
			if (slot < 0) return;
			
			String kspass = new String(pkcs11PasswordField.getPassword());
			if (kspass.equals("")) kspass = null;
			
			int ksIndex = contextManager.initPKCS11(name, library, slot, kspass);
			keyStoreListModel.insertElementAt(contextManager.getKeyStoreDescription(ksIndex), ksIndex);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, new String[] {"Error accessing key store.","Maybe your password or driver is wrong."}, "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		certificatejTabbedPane.setSelectedIndex(0);

		driverComboBox.setSelectedIndex(-1);
		pkcs11PasswordField.setText("");


	}//GEN-LAST:event_addPkcs11ButtonActionPerformed

	private void driverButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driverButtonActionPerformed
		new JDialog( new DriversView(driverConfig) ,true);
	}//GEN-LAST:event_driverButtonActionPerformed


	private void addPkcs12ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPkcs12ButtonActionPerformed
		if (fileTextField.getText().equals("")) return;
		String kspass = new String(pkcs12PasswordField.getPassword());
		if (kspass.equals("")){
			//pcks#12 file with empty password is not supported by java
			JOptionPane.showMessageDialog(null, new String[] {"PKCS#12 files with empty passwords are not supported.", "Please usa a password protected file."}, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			int ksIndex = contextManager.loadPKCS12Certificate(fileTextField.getText(), kspass);
			keyStoreListModel.insertElementAt(contextManager.getKeyStoreDescription(ksIndex), ksIndex);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, new String[] {"Error accessing key store.", "May be your password is wrong."}, "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}


		certificatejTabbedPane.setSelectedIndex(0);

		fileTextField.setText("");
		pkcs12PasswordField.setText("");

	}//GEN-LAST:event_addPkcs12ButtonActionPerformed

	private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter( new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "Client certificate";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() ||
				f.getName().toLowerCase().endsWith( ".p12" ) ;
			}
		} );

		int state = fc.showOpenDialog( null );

		if ( state == JFileChooser.APPROVE_OPTION )
		{
			fileTextField.setText(fc.getSelectedFile().toString() );
		}
	}//GEN-LAST:event_browseButtonActionPerformed

	private void showAliasButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAliasButtonActionPerformed
		int keystore = keyStoreList.getSelectedIndex();
		if(keystore>=0) {
			int alias = aliasTable.getSelectedRow();
			Certificate cert = contextManager.getCertificate(keystore, alias);
			new Dialog(new CertificateView(cert.toString()), false);
		}
	}//GEN-LAST:event_showAliasButtonActionPerformed

	private void setActiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setActiveButtonActionPerformed
		int ks = keyStoreList.getSelectedIndex();
		int alias = aliasTable.getSelectedRow();
		if (ks > -1 && alias>-1) {
			if (!contextManager.isKeyUnlocked(ks, alias)) {


				try {
					if(!contextManager.unlockKeyWithDefaultPassword(ks, alias)){
						String password = getPassword();

						if(!contextManager.unlockKey(ks, alias, password)){
							JOptionPane.showMessageDialog(null, new String[] {"Could not access key store."}, "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, new String[] {"Error accessing key store: ", e.toString()}, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			Certificate cert = contextManager.getCertificate(ks, alias);
			try {
				contextManager.getFingerPrint(cert);
			} catch (KeyStoreException kse) {
				JOptionPane.showMessageDialog(null, new String[] {"Error calculating key fingerprint: ", kse.toString()}, "Error", JOptionPane.ERROR_MESSAGE);
			}

			try {
				contextManager.setDefaultKey(ks, alias);

				OptionsParamCertificate certParam = Model.getSingleton().getOptionsParam().getCertificateParam();
				certParam.setActiveCertificate();

			} catch (KeyStoreException e) {
				e.printStackTrace();
			}
			certificateTextField.setText(contextManager.getDefaultKey());
		}



	}//GEN-LAST:event_setActiveButtonActionPerformed

	public String getPassword() {
		JPasswordField askPasswordField = new JPasswordField();
		int result = JOptionPane.showConfirmDialog(this, askPasswordField, "Enter password", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			return new String(askPasswordField.getPassword());
		} else return null;
	}

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
		int index = keyStoreList.getSelectedIndex();
		if(index>=0) {
			boolean isDefaultKeyStore = contextManager.removeKeyStore(index);
			if(isDefaultKeyStore) {
				certificateTextField.setText("");
			}
			keyStoreListModel.removeElementAt(keyStoreList.getSelectedIndex());
			aliasTableModel.removeKeystore();
		}

	}//GEN-LAST:event_deleteButtonActionPerformed

	private void useClientCertificateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useClientCertificateCheckBoxActionPerformed
		//keyStore tab
		certificatejTabbedPane.setEnabled(useClientCertificateCheckBox.isSelected() );

		keyStoreScrollPane.setEnabled(useClientCertificateCheckBox.isSelected());
		keyStoreList.setEnabled(useClientCertificateCheckBox.isSelected());

		aliasScrollPane.setEnabled(useClientCertificateCheckBox.isSelected());
		aliasTable.setEnabled(useClientCertificateCheckBox.isSelected());

		deleteButton.setEnabled(useClientCertificateCheckBox.isSelected() );
		setActiveButton.setEnabled(useClientCertificateCheckBox.isSelected() );
		showAliasButton.setEnabled(useClientCertificateCheckBox.isSelected() );

		//pkcs12 tab
		fileTextField.setEnabled(useClientCertificateCheckBox.isSelected() );
		browseButton.setEnabled(useClientCertificateCheckBox.isSelected() );
		pkcs12PasswordField.setEnabled(useClientCertificateCheckBox.isSelected() );
		addPkcs12Button.setEnabled(useClientCertificateCheckBox.isSelected() );

		//pkcs11 tab
		driverComboBox.setEnabled(useClientCertificateCheckBox.isSelected() );
		driverButton.setEnabled(useClientCertificateCheckBox.isSelected() );
		pkcs11PasswordField.setEnabled(useClientCertificateCheckBox.isSelected() );
		addPkcs11Button.setEnabled(useClientCertificateCheckBox.isSelected() );

		//actual certificate fields
		certificateTextField.setEnabled(useClientCertificateCheckBox.isSelected() );
		showActiveCertificateButton.setEnabled(useClientCertificateCheckBox.isSelected() );

	}//GEN-LAST:event_useClientCertificateCheckBoxActionPerformed


	// TODO remove
	private OptionsCertificatePanel getContentPane() {
		return this;
	}

	public void initParam(Object obj) {
		OptionsParam options = (OptionsParam) obj;
		useClientCertificateCheckBox.setSelected(options.getCertificateParam().isUseClientCert());
		//getBtnLocation().setEnabled(getChkUseClientCertificate().isSelected());
		//getTxtLocation().setText(options.getCertificateParam().getClientCertLocation());

	}

	public void validateParam(Object obj) {
		// no validation needed
	}

	public void saveParam (Object obj) throws Exception {
		OptionsParam options = (OptionsParam) obj;
		OptionsParamCertificate certParam = options.getCertificateParam();
		certParam.setEnableCertificate(useClientCertificateCheckBox.isSelected());

	}


	@Override
	public void update(Observable arg0, Object arg1) {
		updateDriverComboBox();
	}



}  //  @jve:decl-index=0:visual-constraint="10,10"
