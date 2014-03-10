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
// ZAP: 2012/04/14 Changed the method initParam to discard all edits.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/12/18 Issue 441: Dont access view in daemon mode
// ZAP: 2013/01/04 Added field txtSslTunnelingPorts below txtTimeoutInSecs.
// ZAP: 2013/01/30 Issue 478: Allow to choose to send ZAP's managed cookies on 
// a single Cookie request header and set it as the default
// ZAP: 2013/12/13 Issue 939: ZAP should accept SSL connections on non-standard ports automatically

package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.network.ConnectionParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.ProxyDialog;


public class OptionsConnectionPanel extends AbstractParamPanel {
	// ZAP: i18n	
	private static final long serialVersionUID = 1L;
	private JCheckBox chkUseProxyChain = null;
	private JPanel jPanel = null;
	private JPanel panelProxyAuth = null;
	private JScrollPane jScrollPane = null;
	//private OptionsParam optionsParam = null;
	private JPanel panelProxyChain = null;
	private ZapTextField txtProxyChainName = null;
	// ZAP: Do not allow invalid port numbers
	private ZapPortNumberSpinner spinnerProxyChainPort = null;
	private ZapTextArea txtProxyChainSkipName = null;
	private ZapTextField txtProxyChainRealm = null;
	private ZapTextField txtProxyChainUserName = null;
	private ZapTextField txtProxyChainPassword = null;
	private JCheckBox chkProxyChainAuth = null;
	// ZAP: Added prompt option and timeout in secs
	private JCheckBox chkProxyChainPrompt = null;
	private ZapTextField txtTimeoutInSecs = null;
	private JPanel panelGeneral = null;
    private JCheckBox checkBoxSingleCookieRequestHeader;
	
    public OptionsConnectionPanel() {
        super();
 		initialize();
   }

	/**
	 * This method initializes chkUseProxyChain	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkUseProxyChain() {
		if (chkUseProxyChain == null) {
			chkUseProxyChain = new JCheckBox();
			chkUseProxyChain.setText(Constant.messages.getString("conn.options.useProxy"));
			chkUseProxyChain.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					setProxyChainEnabled(chkUseProxyChain.isSelected());
				}
			});

		}
		return chkUseProxyChain;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints71 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints61 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints51 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints41 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints15 = new GridBagConstraints();

			javax.swing.JLabel jLabel7 = new JLabel();

			javax.swing.JLabel jLabel6 = new JLabel();

			javax.swing.JLabel jLabel5 = new JLabel();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jLabel5.setText(Constant.messages.getString("conn.options.proxy.address"));
			jLabel6.setText(Constant.messages.getString("conn.options.proxy.port"));
			jLabel7.setText(Constant.messages.getString("conn.options.proxy.skipAddresses"));
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.gridy = 0;
			gridBagConstraints15.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridwidth = 2;
			gridBagConstraints15.weightx = 1.0D;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.weightx = 0.5D;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 0.5D;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints3.ipadx = 50;
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.gridy = 2;
			gridBagConstraints41.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints41.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints41.weightx = 0.5D;
			gridBagConstraints51.gridx = 1;
			gridBagConstraints51.gridy = 2;
			gridBagConstraints51.weightx = 0.5D;
			gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints51.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints51.ipadx = 50;
			gridBagConstraints61.gridx = 0;
			gridBagConstraints61.gridy = 3;
			gridBagConstraints61.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints61.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints61.weightx = 1.0D;
			gridBagConstraints61.gridwidth = 2;
			gridBagConstraints61.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints71.gridx = 0;
			gridBagConstraints71.gridy = 4;
			gridBagConstraints71.weightx = 1.0D;
			gridBagConstraints71.weighty = 0.2D;
			gridBagConstraints71.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints71.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints71.anchor = java.awt.GridBagConstraints.NORTHEAST;
			gridBagConstraints71.gridwidth = 2;
			gridBagConstraints71.ipady = 20;
			jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
					Constant.messages.getString("conn.options.proxy.useProxyChain"), 
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
					javax.swing.border.TitledBorder.DEFAULT_POSITION, 
					new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			jPanel.add(getChkUseProxyChain(), gridBagConstraints15);
			jPanel.add(jLabel5, gridBagConstraints2);
			jPanel.add(getTxtProxyChainName(), gridBagConstraints3);
			jPanel.add(jLabel6, gridBagConstraints41);
			jPanel.add(getTxtProxyChainPort(), gridBagConstraints51);
			jPanel.add(jLabel7, gridBagConstraints61);
			jPanel.add(getJScrollPane(), gridBagConstraints71);
		}
		return jPanel;
	}
	/**
	 * This method initializes panelProxyAuth	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelProxyAuth() {
		if (panelProxyAuth == null) {
			java.awt.GridBagConstraints gridBagConstraints72 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints62 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints52 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints42 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints31 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints21 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints16 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints17 = new GridBagConstraints();

			javax.swing.JLabel jLabel11 = new JLabel();

			javax.swing.JLabel jLabel10 = new JLabel();

			javax.swing.JLabel jLabel9 = new JLabel();

			panelProxyAuth = new JPanel();
			panelProxyAuth.setLayout(new GridBagLayout());
			jLabel9.setText(Constant.messages.getString("conn.options.proxy.auth.realm"));
			jLabel10.setText(Constant.messages.getString("conn.options.proxy.auth.username"));
			jLabel11.setText(Constant.messages.getString("conn.options.proxy.auth.password"));
			panelProxyAuth.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
					Constant.messages.getString("conn.options.proxy.auth.auth"), 
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
					javax.swing.border.TitledBorder.DEFAULT_POSITION, 
					new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			panelProxyAuth.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.gridy = 0;
			gridBagConstraints16.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints16.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridwidth = 2;
			gridBagConstraints16.weightx = 1.0D;
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.gridy = 1;
			gridBagConstraints17.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints17.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints17.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints17.gridwidth = 2;
			gridBagConstraints17.weightx = 1.0D;
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 2;
			gridBagConstraints21.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints21.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.weightx = 0.5D;
			gridBagConstraints31.gridx = 1;
			gridBagConstraints31.gridy = 2;
			gridBagConstraints31.weightx = 0.5D;
			gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints31.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints31.ipadx = 50;
			gridBagConstraints42.gridx = 0;
			gridBagConstraints42.gridy = 3;
			gridBagConstraints42.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints42.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints42.weightx = 0.5D;
			gridBagConstraints42.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints52.gridx = 1;
			gridBagConstraints52.gridy = 3;
			gridBagConstraints52.weightx = 0.5D;
			gridBagConstraints52.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints52.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints52.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints52.ipadx = 50;
			gridBagConstraints62.gridx = 0;
			gridBagConstraints62.gridy = 4;
			gridBagConstraints62.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints62.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints62.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints62.weightx = 0.5D;
			gridBagConstraints72.gridx = 1;
			gridBagConstraints72.gridy = 4;
			gridBagConstraints72.weightx = 0.5D;
			gridBagConstraints72.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints72.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints72.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints72.ipadx = 50;
			panelProxyAuth.add(getChkProxyChainAuth(), gridBagConstraints16);
			panelProxyAuth.add(getChkProxyChainPrompt(), gridBagConstraints17);
			panelProxyAuth.add(jLabel9, gridBagConstraints21);
			panelProxyAuth.add(getTxtProxyChainRealm(), gridBagConstraints31);
			panelProxyAuth.add(jLabel10, gridBagConstraints42);
			panelProxyAuth.add(getTxtProxyChainUserName(), gridBagConstraints52);
			panelProxyAuth.add(jLabel11, gridBagConstraints62);
			panelProxyAuth.add(getTxtProxyChainPassword(), gridBagConstraints72);
		}
		return panelProxyAuth;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtProxyChainSkipName());
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes panelProxyChain	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelProxyChain() {
		if (panelProxyChain == null) {
			panelProxyChain = new JPanel();
			panelProxyChain.setLayout(new GridBagLayout());
			panelProxyChain.setName("ProxyChain");

			java.awt.GridBagConstraints gridBagConstraints72 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints82 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints92 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints102 = new GridBagConstraints();
			javax.swing.JLabel jLabel8 = new JLabel();

			gridBagConstraints72.gridx = 0;
			gridBagConstraints72.gridy = 0;
			gridBagConstraints72.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints72.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints72.fill = java.awt.GridBagConstraints.HORIZONTAL;

			gridBagConstraints82.gridx = 0;
			gridBagConstraints82.gridy = 1;
			gridBagConstraints82.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints82.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints82.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints82.weightx = 1.0D;
			
			gridBagConstraints92.gridx = 0;
			gridBagConstraints92.gridy = 2;
			gridBagConstraints92.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints92.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints92.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jLabel8.setText("");
			gridBagConstraints102.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints102.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints102.gridx = 0;
			gridBagConstraints102.gridy = 3;
			gridBagConstraints102.weightx = 1.0D;
			gridBagConstraints102.weighty = 1.0D;

			panelProxyChain.add(getPanelGeneral(), gridBagConstraints72);
			panelProxyChain.add(getJPanel(), gridBagConstraints82);
			panelProxyChain.add(getPanelProxyAuth(), gridBagConstraints92);
			panelProxyChain.add(jLabel8, gridBagConstraints102);
		}
		return panelProxyChain;
	}
	/**
	 * This method initializes txtProxyChainName	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtProxyChainName() {
		if (txtProxyChainName == null) {
			txtProxyChainName = new ZapTextField();
		}
		return txtProxyChainName;
	}
	/**
	 * This method initializes spinnerProxyChainPort
	 * 	
	 * @return ZapPortNumberSpinner	
	 */    
	private ZapPortNumberSpinner getTxtProxyChainPort() {
		if (spinnerProxyChainPort == null) {
			// ZAP: Do not allow invalid port numbers
			spinnerProxyChainPort = new ZapPortNumberSpinner(8080);
		}
		return spinnerProxyChainPort;
	}
	/**
	 * This method initializes txtProxyChainSkipName	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextArea	
	 */    
	private ZapTextArea getTxtProxyChainSkipName() {
		if (txtProxyChainSkipName == null) {
			txtProxyChainSkipName = new ZapTextArea();
			txtProxyChainSkipName.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			txtProxyChainSkipName.setMinimumSize(new java.awt.Dimension(0,32));
			txtProxyChainSkipName.setRows(2);
		}
		return txtProxyChainSkipName;
	}
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("conn.options.title"));
        this.add(getPanelProxyChain(), getPanelProxyChain().getName());


	}
	
	@Override
	public void initParam(Object obj) {
	    
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    
	    this.txtTimeoutInSecs.setText(Integer.toString(connectionParam.getTimeoutInSecs()));
	    txtTimeoutInSecs.discardAllEdits();
	    
	    checkBoxSingleCookieRequestHeader.setSelected(connectionParam.isSingleCookieRequestHeader());
	    
	    // set Proxy Chain parameters
	    if (connectionParam.getProxyChainName().equals("")) {
	        chkUseProxyChain.setSelected(false);
	        setProxyChainEnabled(false);
	    } else {
	        chkUseProxyChain.setSelected(true);
	        setProxyChainEnabled(true);
		    txtProxyChainName.setText(connectionParam.getProxyChainName());
		    txtProxyChainName.discardAllEdits();
			// ZAP: Do not allow invalid port numbers
		    spinnerProxyChainPort.setValue(connectionParam.getProxyChainPort());
		    txtProxyChainSkipName.setText(connectionParam.getProxyChainSkipName());
		    txtProxyChainSkipName.discardAllEdits();
		    
		    if (connectionParam.getProxyChainUserName().equals("")) {
		        chkProxyChainAuth.setSelected(false);
		        setProxyChainAuthEnabled(false);
		    } else {
		        chkProxyChainAuth.setSelected(true);
		        setProxyChainAuthEnabled(true);
		        txtProxyChainRealm.setText(connectionParam.getProxyChainRealm());
		        txtProxyChainRealm.discardAllEdits();
		        txtProxyChainUserName.setText(connectionParam.getProxyChainUserName());
		        txtProxyChainUserName.discardAllEdits();
		        
		    	// ZAP: Added prompt option
		        if (connectionParam.isProxyChainPrompt()) {
			        chkProxyChainPrompt.setSelected(true);
			        setProxyChainPromptEnabled(true);
		        	
		        } else {
			        chkProxyChainPrompt.setSelected(false);
			        setProxyChainPromptEnabled(false);
			        txtProxyChainPassword.setText(connectionParam.getProxyChainPassword());
			        txtProxyChainPassword.discardAllEdits();
		        }
		    }
		    
	    }
	}
	
	private void setProxyChainEnabled(boolean isEnabled) {
	    txtProxyChainName.setEnabled(isEnabled);
	    spinnerProxyChainPort.setEnabled(isEnabled);
	    txtProxyChainSkipName.setEnabled(isEnabled);
	    chkProxyChainAuth.setEnabled(isEnabled);
	    Color color = Color.WHITE;
	    if (!isEnabled) {
	        txtProxyChainName.setText("");
	    	// ZAP: Do not allow invalid port numbers
	        spinnerProxyChainPort.changeToDefaultValue();
	        txtProxyChainSkipName.setText("");
	        chkProxyChainAuth.setSelected(false);
	        setProxyChainAuthEnabled(false);
	        color = panelProxyChain.getBackground();
	    }
	    txtProxyChainName.setBackground(color);
	    spinnerProxyChainPort.setBackground(color);
	    txtProxyChainSkipName.setBackground(color);
	    
	}
	
	private void setProxyChainAuthEnabled(boolean isEnabled) {

	    txtProxyChainRealm.setEnabled(isEnabled);
	    txtProxyChainUserName.setEnabled(isEnabled);
	    txtProxyChainPassword.setEnabled(isEnabled);
	    // ZAP: Added prompt option
        chkProxyChainPrompt.setEnabled(isEnabled);
	    
	    Color color = Color.WHITE;
	    if (!isEnabled) {
	    	// ZAP: Added prompt option
	        chkProxyChainPrompt.setSelected(false);
	        txtProxyChainRealm.setText("");
	        txtProxyChainUserName.setText("");
	        txtProxyChainPassword.setText("");
	        color = panelProxyChain.getBackground();
	    }
	    txtProxyChainRealm.setBackground(color);
	    txtProxyChainUserName.setBackground(color);
	    txtProxyChainPassword.setBackground(color);
	    
	}
	
	private void setProxyChainPromptEnabled(boolean isEnabled) {

	    txtProxyChainPassword.setEnabled(!isEnabled);
	    
	    Color color = Color.WHITE;
	    if (isEnabled) {
	        txtProxyChainPassword.setText("");
	        color = panelProxyChain.getBackground();
	    }
	    txtProxyChainPassword.setBackground(color);
	    
	}
	
	@Override
	public void validateParam(Object obj) throws Exception {

        try {
            Integer.parseInt(txtTimeoutInSecs.getText());
        } catch (NumberFormatException nfe) {
        	txtTimeoutInSecs.requestFocus();
            throw new Exception(Constant.messages.getString("conn.options.timeout.invalid"));
        }
        
	    if (chkUseProxyChain.isSelected()) {
	    	// ZAP: empty proxy name validation
        	if(txtProxyChainName.getText().isEmpty()) {
        		txtProxyChainName.requestFocus();
                throw new Exception(Constant.messages.getString("conn.options.proxy.address.empty"));
        	}

        }
	    
	}

	@Override
	public void saveParam(Object obj) throws Exception {
		
	    OptionsParam optionsParam = (OptionsParam) obj;
	    ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    int timeout;
	    String sslPorts;

        try {
            timeout = Integer.parseInt(txtTimeoutInSecs.getText());
        } catch (NumberFormatException nfe) {
        	txtTimeoutInSecs.requestFocus();
            throw new Exception(Constant.messages.getString("conn.options.timeout.invalid"));
        }
        
	    connectionParam.setProxyChainName(txtProxyChainName.getText());
		// ZAP: Do not allow invalid port numbers
	    connectionParam.setProxyChainPort(spinnerProxyChainPort.getValue());
	    connectionParam.setProxyChainSkipName(txtProxyChainSkipName.getText());

	    connectionParam.setProxyChainRealm(txtProxyChainRealm.getText());
	    connectionParam.setProxyChainUserName(txtProxyChainUserName.getText());
		// ZAP: Added prompt option
	    if (chkProxyChainPrompt.isSelected()) {
	    	connectionParam.setProxyChainPrompt(true);
	    	
	    	if (View.isInitialised()) {
		    	// And prompt now
				ProxyDialog dialog = new ProxyDialog(View.getSingleton().getMainFrame(), true);
				dialog.init(Model.getSingleton().getOptionsParam());
				dialog.setVisible(true);
	    	}
	    	
	    } else {
	    	connectionParam.setProxyChainPrompt(false);
		    connectionParam.setProxyChainPassword(txtProxyChainPassword.getText());
	    }
	    connectionParam.setTimeoutInSecs(timeout);
	    connectionParam.setSingleCookieRequestHeader(checkBoxSingleCookieRequestHeader.isSelected());
	    
	}

	/**
	 * This method initializes txtProxyChainRealm	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtProxyChainRealm() {
		if (txtProxyChainRealm == null) {
			txtProxyChainRealm = new ZapTextField();
		}
		return txtProxyChainRealm;
	}
	/**
	 * This method initializes txtProxyChainUserName	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtProxyChainUserName() {
		if (txtProxyChainUserName == null) {
			txtProxyChainUserName = new ZapTextField();
		}
		return txtProxyChainUserName;
	}
	/**
	 * This method initializes txtProxyChainPassword	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtProxyChainPassword() {
		if (txtProxyChainPassword == null) {
			txtProxyChainPassword = new ZapTextField();
		}
		return txtProxyChainPassword;
	}
	/**
	 * This method initializes chkProxyChainAuth	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkProxyChainAuth() {
		if (chkProxyChainAuth == null) {
			chkProxyChainAuth = new JCheckBox();
			chkProxyChainAuth.setText(Constant.messages.getString("conn.options.proxy.auth.required"));
			chkProxyChainAuth.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					setProxyChainAuthEnabled(chkProxyChainAuth.isSelected());
				}
			});

		}
		return chkProxyChainAuth;
	}
	private JCheckBox getChkProxyChainPrompt() {
		if (chkProxyChainPrompt == null) {
			chkProxyChainPrompt = new JCheckBox();
			chkProxyChainPrompt.setText(Constant.messages.getString("conn.options.proxy.auth.prompt"));
			chkProxyChainPrompt.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					setProxyChainPromptEnabled(chkProxyChainPrompt.isSelected());
				}
			});
		}
		return chkProxyChainPrompt;
	}

	private JPanel getPanelGeneral() {
		if (panelGeneral == null) {
			java.awt.GridBagConstraints gridBagConstraints01 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints00 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			panelGeneral = new JPanel();
			panelGeneral.setLayout(new GridBagLayout());
			jLabel.setText(Constant.messages.getString("conn.options.timeout"));
			
			panelGeneral.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
					Constant.messages.getString("conn.options.general"), 
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
					javax.swing.border.TitledBorder.DEFAULT_POSITION, 
					new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), java.awt.Color.black));
			panelGeneral.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));

			gridBagConstraints00.gridx = 0;
			gridBagConstraints00.gridy = 0;
			gridBagConstraints00.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints00.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints00.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints00.weightx = 0.5D;

			gridBagConstraints01.gridx = 1;
			gridBagConstraints01.gridy = 0;
			gridBagConstraints01.weightx = 0.5D;
			gridBagConstraints01.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints01.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints01.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints01.ipadx = 50;

			panelGeneral.add(jLabel, gridBagConstraints00);
			panelGeneral.add(getTxtTimeoutInSecs(), gridBagConstraints01);
			
			java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc.insets = new java.awt.Insets(2,2,2,2);
			gbc.anchor = java.awt.GridBagConstraints.WEST;
            
            panelGeneral.add(getCheckBoxSingleCookeRequestHeader(), gbc);
		}
		return panelGeneral;
	}

	private ZapTextField getTxtTimeoutInSecs() {
		if (txtTimeoutInSecs == null) {
			txtTimeoutInSecs = new ZapTextField();
		}
		return txtTimeoutInSecs;
	}
	
    private JCheckBox getCheckBoxSingleCookeRequestHeader() {
        if (checkBoxSingleCookieRequestHeader == null) {
            checkBoxSingleCookieRequestHeader = new JCheckBox(Constant.messages.getString("conn.options.singleCookieRequestHeader"));
        }
        return checkBoxSingleCookieRequestHeader;
    }
	
	@Override
	public String getHelpIndex() {
		// ZAP: added help index
		return "ui.dialogs.options.connection";
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
