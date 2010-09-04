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
package org.zaproxy.zap.extension.encoder2;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.encoder.Encoder;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EncodeDecodeDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbed = null;
	private JPanel jPanel = null;
	
	private JTextArea inputField = null;
	private JTextArea base64EncodeField = null;
	private JTextArea base64DecodeField = null;
	private JTextArea urlEncodeField = null;
	private JTextArea urlDecodeField = null;
	private JTextArea asciiHexEncodeField = null;
	private JTextArea asciiHexDecodeField = null;
	private JTextArea sha1HashField = null;
	private JTextArea md5HashField = null;
	
	private Encoder encoder = null;

    /**
     * @throws HeadlessException
     */
    public EncodeDecodeDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public EncodeDecodeDialog(Frame arg0, boolean arg1) throws HeadlessException {
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
        this.setTitle(Constant.messages.getString("enc2.title"));
        //this.pack();
        this.setSize(800, 600);
	
	}
	
	private void addField (JPanel parent, int index, JComponent c, String title) {
		java.awt.GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = index;
		gbc.insets = new java.awt.Insets(1,1,1,1);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = java.awt.GridBagConstraints.BOTH;
		gbc.weightx = 0.5D;
		gbc.weighty = 0.5D;

		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(c);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setBorder(
				BorderFactory.createTitledBorder(
						null, title, TitledBorder.DEFAULT_JUSTIFICATION, 
						javax.swing.border.TitledBorder.DEFAULT_POSITION, 
						new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
						java.awt.Color.black));

		parent.add(jsp, gbc);
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJTabbed() {
		if (jPanel == null) {
			/*
			jPanel = new JPanel();
			jPanel.setPreferredSize(new java.awt.Dimension(800,600));
			
			jPanel.setLayout(new GridBagLayout());
			*/

			// jPanel is the outside one
			jPanel = new JPanel();
			jPanel.setPreferredSize(new java.awt.Dimension(800,600));
			jPanel.setLayout(new GridBagLayout());

			jTabbed = new JTabbedPane();
			jTabbed.setPreferredSize(new java.awt.Dimension(800,500));

			JPanel jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());

			JPanel jPanel2 = new JPanel();
			//jPanel2.setPreferredSize(new java.awt.Dimension(800,500));
			jPanel2.setLayout(new GridBagLayout());

			JPanel jPanel3 = new JPanel();
			//jPanel3.setPreferredSize(new java.awt.Dimension(800,500));
			jPanel3.setLayout(new GridBagLayout());

			// 3 tabs - Encode, Decode, Hash??
			addField(jPanel1, 1, getBase64EncodeField(), Constant.messages.getString("enc2.label.b64Enc"));
			addField(jPanel1, 2, getUrlEncodeField(), Constant.messages.getString("enc2.label.urlEnc"));
			addField(jPanel1, 3, getAsciiHexEncodeField(), Constant.messages.getString("enc2.label.asciiEnc"));
			
			addField(jPanel2, 1, getBase64DecodeField(), Constant.messages.getString("enc2.label.b64Dec"));
			addField(jPanel2, 2, getUrlDecodeField(), Constant.messages.getString("enc2.label.urlDec"));
			addField(jPanel2, 3, getAsciiHexDecodeField(), Constant.messages.getString("enc2.label.asciiDec"));
			
			addField(jPanel3, 1, getSha1HashField(), Constant.messages.getString("enc2.label.sha1Hash"));
			addField(jPanel3, 2, getMd5HashField(), Constant.messages.getString("enc2.label.md5Hash"));

			jTabbed.addTab(Constant.messages.getString("enc2.tab.encode"), jPanel1);
			jTabbed.addTab(Constant.messages.getString("enc2.tab.decode"), jPanel2);
			jTabbed.addTab(Constant.messages.getString("enc2.tab.hash"), jPanel3);

			java.awt.GridBagConstraints gbc1 = new GridBagConstraints();
			gbc1.gridx = 0;
			gbc1.gridy = 1;
			gbc1.insets = new java.awt.Insets(1,1,1,1);
			gbc1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc1.fill = java.awt.GridBagConstraints.BOTH;
			gbc1.weightx = 1.0D;
			gbc1.weighty = 0.25D;

			java.awt.GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridx = 0;
			gbc2.gridy = 2;
			gbc2.insets = new java.awt.Insets(1,1,1,1);
			gbc2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc2.fill = java.awt.GridBagConstraints.BOTH;
			gbc2.weightx = 1.0D;
			gbc2.weighty = 1.0D;

			JScrollPane jsp = new JScrollPane();
			jsp.setViewportView(getInputField());
			jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("enc2.label.text"), 
							TitledBorder.DEFAULT_JUSTIFICATION, 
							javax.swing.border.TitledBorder.DEFAULT_POSITION, 
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
							java.awt.Color.black));

			//addField(jPanel, 1, getInputField(), "Text to be encoded/decoded/hashed");
			//addField(jPanel, 2, jTabbed, "Text to be encoded/decoded/hashed");

			jPanel.add(jsp, gbc1);
			jPanel.add(jTabbed, gbc2);

			jPanel2.requestFocus();

		}
		return jPanel;
	}
	
	private JTextArea newField(Boolean editable) {
		JTextArea field = new JTextArea();
		field.setLineWrap(true);
		field.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
		field.setBorder(BorderFactory.createEtchedBorder());
		field.setEditable(editable);
		return field;
	}

	private JTextArea getInputField() {
		if (inputField == null) {
			inputField = newField(true);
			
			inputField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
					updateEncodeDecodeFields();
				}
				
			});
		}
		return inputField;
	}

	private JTextArea getBase64EncodeField() {
		if (base64EncodeField == null) {
			base64EncodeField = newField(false);
		}
		return base64EncodeField;
	}

	private JTextArea getBase64DecodeField() {
		if (base64DecodeField == null) {
			base64DecodeField = newField(false);
		}
		return base64DecodeField;
	}

	private JTextArea getUrlEncodeField() {
		if (urlEncodeField == null) {
			urlEncodeField = newField(false);
		}
		return urlEncodeField;
	}

	private JTextArea getUrlDecodeField() {
		if (urlDecodeField == null) {
			urlDecodeField = newField(false);
		}
		return urlDecodeField;
	}

	private JTextArea getAsciiHexEncodeField() {
		if (asciiHexEncodeField == null) {
			asciiHexEncodeField = newField(false);
		}
		return asciiHexEncodeField;
	}

	private JTextArea getAsciiHexDecodeField() {
		if (asciiHexDecodeField == null) {
			asciiHexDecodeField = newField(false);
		}
		return asciiHexDecodeField;
	}

	private JTextArea getSha1HashField() {
		if (sha1HashField == null) {
			sha1HashField = newField(false);
		}
		return sha1HashField;
	}

	private JTextArea getMd5HashField() {
		if (md5HashField == null) {
			md5HashField = newField(false);
		}
		return md5HashField;
	}

	private Encoder getEncoder() {
	    if (encoder == null) {
	        encoder = new Encoder();
	    }
	    return encoder;
	}
	
	public String decodeHexString(String hexText) {

		String decodedText="";
		String chunk=null;

		if(hexText!=null && hexText.length()>0) {
			int numBytes = hexText.length()/2;
	
			byte[] rawToByte = new byte[numBytes];
			int offset=0;
			for(int i =0; i <numBytes; i++) {
				chunk = hexText.substring(offset,offset+2);
				offset+=2;
				rawToByte[i] = (byte) (Integer.parseInt(chunk,16) & 0x000000FF);
			}
			decodedText= new String(rawToByte);
		}
		return decodedText;
	}

	private void updateEncodeDecodeFields() {

		// Base 64
		base64EncodeField.setText(getEncoder().getBase64Encode(getInputField().getText()));
		try {
			base64DecodeField.setText(getEncoder().getBase64Decode(getInputField().getText()));
		} catch (Exception e) {
			// Not unexpected
			base64DecodeField.setText("");
		}
		base64DecodeField.setEnabled(base64DecodeField.getText().length() > 0);
		
		// URLs
		urlEncodeField.setText(getEncoder().getURLEncode(getInputField().getText()));
		try {
			urlDecodeField.setText(getEncoder().getURLDecode(getInputField().getText()));
		} catch (Exception e) {
			// Not unexpected
			urlDecodeField.setText("");
		}
		urlDecodeField.setEnabled(urlDecodeField.getText().length() > 0);

		// ASCII Hex
		asciiHexEncodeField.setText(
				getEncoder().getHexString(
						getInputField().getText().getBytes()));

		try {
			asciiHexDecodeField.setText(decodeHexString(getInputField().getText()));
		} catch (Exception e) {
			// Not unexpected
			asciiHexDecodeField.setText("");
		}
		asciiHexDecodeField.setEnabled(asciiHexDecodeField.getText().length() > 0);
		
		
		// Hashes
		try {
			sha1HashField.setText(
					getEncoder().getHexString(
							getEncoder().getHashSHA1(
									getInputField().getText().getBytes())));
		} catch (Exception e) {
			sha1HashField.setText("");
		}
		
		try {
			md5HashField.setText(
					getEncoder().getHexString(
							getEncoder().getHashMD5(
									getInputField().getText().getBytes())));
		} catch (Exception e) {
			md5HashField.setText("");
		}
		
	}

	public void setInputField (String text) {
		this.getInputField().setText(text);
		this.updateEncodeDecodeFields();
	}

}
