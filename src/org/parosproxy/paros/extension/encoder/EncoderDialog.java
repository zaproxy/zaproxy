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
package org.parosproxy.paros.extension.encoder;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.ViewDelegate;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EncoderDialog extends AbstractDialog {

	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;
	private JTextArea txtEncode = null;
	private JTextArea txtDecode = null;
	private JButton btnMD5Hash = null;
	private JButton btnURLEncode = null;
	private JButton btnBase64Encode = null;
	private JButton btnSHA1Hash = null;
	private JButton btnURLDecode = null;
	private JButton btnBase64Decode = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;
	
	private Encoder encoder = null;
	private ViewDelegate view = null;
	
	/**
	 * This method initializes 
	 * 
	 */
	public EncoderDialog() {
		super();
		initialize();
	}
	
	public EncoderDialog(Frame owner, boolean modal) {
	    super(owner, modal);
	    initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setTitle("Encode/Hash");
        this.setContentPane(getJPanel());
        this.setSize(415, 363);
			
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.weighty = 0.5D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints2.weightx = 1.0D;
			gridBagConstraints2.weighty = 0.5D;
			jPanel.add(getJPanel1(), gridBagConstraints1);
			jPanel.add(getJPanel2(), gridBagConstraints2);
		}
		return jPanel;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Enter plain text below to be encoded/hashed", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			jPanel1.setPreferredSize(new java.awt.Dimension(135,120));
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints3.gridheight = 1;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.insets = new java.awt.Insets(2,2,2,2);
			jPanel1.add(getJScrollPane(), gridBagConstraints3);
			jPanel1.add(getJPanel3(), gridBagConstraints7);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			java.awt.GridBagConstraints gridBagConstraints9 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints8 = new GridBagConstraints();

			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Enter text below to be decoded", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			jPanel2.setPreferredSize(new java.awt.Dimension(135,120));
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints8.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints8.gridheight = 1;
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
			jPanel2.add(getJScrollPane1(), gridBagConstraints8);
			jPanel2.add(getJPanel4(), gridBagConstraints9);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtEncode());
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTxtDecode());
		}
		return jScrollPane1;
	}
	/**
	 * This method initializes txtEncode	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextArea getTxtEncode() {
		if (txtEncode == null) {
			txtEncode = new JTextArea();
			txtEncode.setLineWrap(true);
			txtEncode.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
			txtEncode.addMouseListener(new java.awt.event.MouseAdapter() {   
				public void mousePressed(java.awt.event.MouseEvent e) {    
	          		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {  // right mouse button
	            		view.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
	            	}
				} 

			
			});

		}
		return txtEncode;
	}
	/**
	 * This method initializes txtDecode	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextArea getTxtDecode() {
		if (txtDecode == null) {
			txtDecode = new JTextArea();
			txtDecode.setLineWrap(true);
			txtDecode.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
		}
		return txtDecode;
	}
	/**
	 * This method initializes btnMD5Hash	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnMD5Hash() {
		if (btnMD5Hash == null) {
			btnMD5Hash = new JButton();
			btnMD5Hash.setText("MD5 Hash");
			btnMD5Hash.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    txtDecode.setText("");
				    try {
                        String result = getEncoder().getHexString(getEncoder().getHashMD5(getEncoder().getBytes(txtEncode.getText())));
                        txtDecode.setText(result);
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    }

				}
			});

		}
		return btnMD5Hash;
	}
	/**
	 * This method initializes btnURLEncode	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnURLEncode() {
		if (btnURLEncode == null) {
			btnURLEncode = new JButton();
			btnURLEncode.setText("URL Encode");
			btnURLEncode.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    

				    txtDecode.setText("");
                    txtDecode.setText(getEncoder().getURLEncode(txtEncode.getText()));

				}
			});

		}
		return btnURLEncode;
	}
	/**
	 * This method initializes btnBase64Encode	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnBase64Encode() {
		if (btnBase64Encode == null) {
			btnBase64Encode = new JButton();
			btnBase64Encode.setText("Base64 Encode");
			btnBase64Encode.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    txtDecode.setText("");
                    txtDecode.setText(getEncoder().getBase64Encode(txtEncode.getText()));

				}
			});

		}
		return btnBase64Encode;
	}
	/**
	 * This method initializes btnSHA1Hash	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnSHA1Hash() {
		if (btnSHA1Hash == null) {
			btnSHA1Hash = new JButton();
			btnSHA1Hash.setText("SHA1 Hash");
			btnSHA1Hash.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
                    txtDecode.setText("");
				    try {
                        String result = getEncoder().getHexString(getEncoder().getHashSHA1(getEncoder().getBytes(txtEncode.getText())));
                        txtDecode.setText(result);
                    } catch (NoSuchAlgorithmException e1) {
                        e1.printStackTrace();
                    }


				}
			});

		}
		return btnSHA1Hash;
	}
	/**
	 * This method initializes btnURLDecode	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnURLDecode() {
		if (btnURLDecode == null) {
			btnURLDecode = new JButton();
			btnURLDecode.setText("URL Decode");
			btnURLDecode.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    txtEncode.setText("");
				    String result = getEncoder().getURLDecode(txtDecode.getText());
				    txtEncode.setText(result);

				}
			});

		}
		return btnURLDecode;
	}
	/**
	 * This method initializes btnBase64Decode	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnBase64Decode() {
		if (btnBase64Decode == null) {
			btnBase64Decode = new JButton();
			btnBase64Decode.setText("Base64 Decode");
			btnBase64Decode.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    txtEncode.setText("");
                    txtEncode.setText(getEncoder().getBase64Decode(txtDecode.getText()));

				}
			});

		}
		return btnBase64Decode;
	}
	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			java.awt.GridLayout gridLayout6 = new GridLayout();

			jPanel3 = new JPanel();
			jPanel3.setLayout(gridLayout6);
			gridLayout6.setRows(4);
			gridLayout6.setColumns(1);
			gridLayout6.setVgap(3);
			gridLayout6.setHgap(3);
			jPanel3.add(getBtnURLEncode(), null);
			jPanel3.add(getBtnBase64Encode(), null);
			jPanel3.add(getBtnSHA1Hash(), null);
			jPanel3.add(getBtnMD5Hash(), null);
		}
		return jPanel3;
	}
	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			java.awt.GridLayout gridLayout11 = new GridLayout();

			jPanel4 = new JPanel();
			jPanel4.setLayout(gridLayout11);
			gridLayout11.setRows(2);
			gridLayout11.setColumns(1);
			gridLayout11.setHgap(2);
			gridLayout11.setVgap(3);
			jPanel4.add(getBtnURLDecode(), null);
			jPanel4.add(getBtnBase64Decode(), null);
		}
		return jPanel4;
	}
	
	private Encoder getEncoder() {
	    if (encoder == null) {
	        encoder = new Encoder();
	    }
	    return encoder;
	}
	
	void setView(ViewDelegate view) {
	    this.view = view;
	}
               }  //  @jve:decl-index=0:visual-constraint="10,10"
