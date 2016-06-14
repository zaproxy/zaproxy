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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/14 Changed the method initParam to discard all edits.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/11/04 Issue 408: Add support to encoding transformations, added an
// option to control whether the "Accept-Encoding" request-header field is 
// modified/removed or not.
// ZAP: 2014/03/06 Issue 1063: Add option to decode all gzipped content
// ZAP: 2014/03/23 Issue 968: Allow to choose the enabled SSL/TLS protocols
// ZAP: 2015/02/10 Issue 1528: Support user defined font size
// ZAP: 2016/06/13 Change option "Modify/Remove Accept-Encoding" to "Remove Unsupported Encodings"
// ZAP: 2016/06/13 Internationalise string and remove unused instance variable

package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.proxy.ProxyParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsLocalProxyPanel extends AbstractParamPanel {

    private static final long serialVersionUID = -1350537974139536669L;

    private JPanel panelLocalProxy = null;
    private JPanel panelReverseProxy = null;  //  @jve:decl-index=0:visual-constraint="520,10"

    private JPanel panelProxy = null;  //  @jve:decl-index=0:visual-constraint="10,283"
    private ZapTextField txtProxyIp = null;
    private ZapTextField txtReverseProxyIp = null;

    private JCheckBox chkRemoveUnsupportedEncodings = null;
    private JCheckBox chkAlwaysDecodeGzip = null;

    private SecurityProtocolsPanel securityProtocolsPanel;

    // ZAP: Do not allow invalid port numbers
    private ZapPortNumberSpinner spinnerProxyPort = null;
    private ZapPortNumberSpinner spinnerReverseProxyHttpPort = null;
    private ZapPortNumberSpinner spinnerReverseProxyHttpsPort = null;

    private JLabel jLabel6 = null;
    private JCheckBox chkReverseProxy = null;
    private JLabel jLabel5 = null;
    private JLabel jLabel7 = null;

    public OptionsLocalProxyPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes panelLocalProxy
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelLocalProxy() {
        if (panelLocalProxy == null) {
            jLabel6 = new JLabel();
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            javax.swing.JLabel jLabel = new JLabel();
            javax.swing.JLabel jLabel1 = new JLabel();

            panelLocalProxy = new JPanel();
            panelLocalProxy.setLayout(new GridBagLayout());
            panelLocalProxy.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    null, Constant.messages.getString("options.proxy.local.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, FontUtils.getFont(FontUtils.Size.standard), java.awt.Color.black));	// ZAP: i18n

            jLabel.setText(Constant.messages.getString("options.proxy.local.label.address"));
            
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 0;
            gridBagConstraints4.ipadx = 0;
            gridBagConstraints4.ipady = 0;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints4.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints4.weightx = 0.5D;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            
            gridBagConstraints5.gridx = 1;
            gridBagConstraints5.gridy = 0;
            gridBagConstraints5.weightx = 0.5D;
            gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.ipadx = 50;
            gridBagConstraints5.ipady = 0;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints5.insets = new java.awt.Insets(2, 2, 2, 2);
            
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.ipadx = 0;
            gridBagConstraints6.ipady = 0;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints6.weightx = 0.5D;
            
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.gridy = 1;
            gridBagConstraints7.weightx = 0.5D;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.ipadx = 50;
            gridBagConstraints7.ipady = 0;
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints7.insets = new java.awt.Insets(2, 2, 2, 2);
            
            jLabel1.setText(Constant.messages.getString("options.proxy.local.label.port"));
            jLabel6.setText(Constant.messages.getString("options.proxy.local.label.browser"));
            
            gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.gridy = 4;
            gridBagConstraints15.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints15.weightx = 1.0D;
            gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints15.gridwidth = 2;
            
            panelLocalProxy.add(jLabel, gridBagConstraints4);
            panelLocalProxy.add(getTxtProxyIp(), gridBagConstraints5);
            panelLocalProxy.add(jLabel1, gridBagConstraints6);
            panelLocalProxy.add(getSpinnerProxyPort(), gridBagConstraints7);
            panelLocalProxy.add(jLabel6, gridBagConstraints15);
            
            java.awt.GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.insets = new java.awt.Insets(2, 2, 2, 2);
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0D;
            gbc.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gbc.anchor = java.awt.GridBagConstraints.PAGE_START;
            panelLocalProxy.add(getChkRemoveUnsupportedEncodings(), gbc);

            // TODO hacking
            panelLocalProxy.add(this.getChkAlwaysDecodeGzip(), 
            		LayoutHelper.getGBC(0, 6, 1, 1.0D, 0.0D, GridBagConstraints.HORIZONTAL, 
            				GridBagConstraints.PAGE_START, new java.awt.Insets(2, 2, 2, 2)));

        }
        
        return panelLocalProxy;
    }

    /**
     * This method initializes panelLocalProxySSL
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelReverseProxy() {
        if (panelReverseProxy == null) {
            jLabel7 = new JLabel();
            jLabel5 = new JLabel();
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
            javax.swing.JLabel jLabel3 = new JLabel();
            javax.swing.JLabel jLabel2 = new JLabel();

            panelReverseProxy = new JPanel();
            java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints101 = new GridBagConstraints();

            panelReverseProxy.setLayout(new GridBagLayout());
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
                panelReverseProxy.setSize(114, 132);
            }
            
            panelReverseProxy.setName(Constant.messages.getString("options.proxy.local.label.misc"));
            panelReverseProxy.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                    Constant.messages.getString("options.proxy.local.label.reverse"),
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, FontUtils.getFont(FontUtils.Size.standard), java.awt.Color.black));
            
            gridBagConstraints101.gridx = 0;
            gridBagConstraints101.gridy = 0;
            gridBagConstraints101.ipadx = 0;
            gridBagConstraints101.ipady = 0;
            gridBagConstraints101.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints101.weightx = 0.5D;
            gridBagConstraints101.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints101.anchor = java.awt.GridBagConstraints.WEST;
            
            gridBagConstraints11.gridx = 1;
            gridBagConstraints11.gridy = 0;
            gridBagConstraints11.weightx = 0.5D;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.ipadx = 50;
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints11.insets = new java.awt.Insets(2, 2, 2, 2);
            
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.gridy = 1;
            gridBagConstraints12.ipadx = 0;
            gridBagConstraints12.ipady = 0;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints12.weightx = 0.5D;
            
            gridBagConstraints13.gridx = 1;
            gridBagConstraints13.gridy = 1;
            gridBagConstraints13.weightx = 0.5D;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints13.ipadx = 50;
            gridBagConstraints13.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints13.anchor = java.awt.GridBagConstraints.WEST;
            
            jLabel2.setText(Constant.messages.getString("options.proxy.local.label.rev.address"));
            jLabel3.setText(Constant.messages.getString("options.proxy.local.label.rev.port"));
            
            panelReverseProxy.add(jLabel2, gridBagConstraints101);
            panelReverseProxy.add(getTxtReverseProxyIp(), gridBagConstraints11);
            panelReverseProxy.add(getSpinnerReverseProxyHttpPort(), gridBagConstraints13);
            panelReverseProxy.setVisible(true);
            
            jLabel5.setText("HTTPS port (eg 443)");
            
            gridBagConstraints3.weightx = 0.5D;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints3.gridx = 1;
            gridBagConstraints3.gridy = 2;
            gridBagConstraints3.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints3.ipadx = 50;
            
            gridBagConstraints41.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints41.gridx = 0;
            gridBagConstraints41.gridy = 2;
            gridBagConstraints41.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints41.weightx = 0.5D;
            gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
            
            jLabel7.setText(Constant.messages.getString("options.proxy.local.label.rev.local"));
            
            gridBagConstraints51.gridx = 0;
            gridBagConstraints51.gridy = 3;
            gridBagConstraints51.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints51.gridwidth = 2;
            gridBagConstraints51.weightx = 1.0D;
            gridBagConstraints51.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints51.fill = java.awt.GridBagConstraints.BOTH;
            panelReverseProxy.add(jLabel3, gridBagConstraints12);
            panelReverseProxy.add(jLabel7, gridBagConstraints51);
            panelReverseProxy.add(jLabel5, gridBagConstraints41);
            panelReverseProxy.add(getSpinnerReverseProxyHttpsPort(), gridBagConstraints3);
        }
        return panelReverseProxy;
    }

    /**
     * This method initializes panelProxy
     *
     * @return javax.swing.JPanel
     */
    private JPanel getPanelProxy() {
        if (panelProxy == null) {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            panelProxy = new JPanel();
            java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            javax.swing.JLabel jLabel4 = new JLabel();
            java.awt.GridBagConstraints gridBagConstraints14 = new GridBagConstraints();

            GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints81 = new GridBagConstraints();

            panelProxy.setLayout(new GridBagLayout());

            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.insets = new java.awt.Insets(2, 0, 2, 0);
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints8.weightx = 1.0D;
            
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.gridy = 1;
            gridBagConstraints9.weightx = 1.0;
            gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints9.insets = new java.awt.Insets(2, 0, 2, 0);
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.gridy = 2;
            gridBagConstraints10.insets = new java.awt.Insets(2, 0, 2, 0);
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
            
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            
            panelProxy.setName(Constant.messages.getString("options.proxy.local.label.local"));
            if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
                panelProxy.setSize(303, 177);
            }
            
            panelProxy.setFont(FontUtils.getFont(FontUtils.Size.standard));
            
            gridBagConstraints81.gridx = 0;
            gridBagConstraints81.gridy = 0;
            gridBagConstraints81.ipadx = 2;
            gridBagConstraints81.ipady = 4;
            gridBagConstraints81.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints81.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints81.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints81.weightx = 1.0D;
            gridBagConstraints81.weighty = 0.0D;
            
            gridBagConstraints91.gridx = 0;
            gridBagConstraints91.gridy = 3;
            gridBagConstraints91.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints91.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints91.weightx = 1.0D;
            gridBagConstraints91.weighty = 0.0D;
            gridBagConstraints91.ipady = 4;
            gridBagConstraints91.ipadx = 2;
            
            jLabel4.setText("");
            
            gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints14.gridx = 0;
            gridBagConstraints14.gridy = 3;
            gridBagConstraints14.weightx = 1.0D;
            gridBagConstraints14.weighty = 1.0D;
            
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 2;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints2.weightx = 1.0D;
            
            panelProxy.add(getPanelLocalProxy(), gridBagConstraints81);
            gridBagConstraints81.gridy = 1;
            panelProxy.add(securityProtocolsPanel, gridBagConstraints81);
            panelProxy.add(getChkReverseProxy(), gridBagConstraints2);
            panelProxy.add(getPanelReverseProxy(), gridBagConstraints91);
            panelProxy.add(jLabel4, gridBagConstraints14);
        }
        return panelProxy;
    }

    /**
     * This method initializes txtProxyIp
     *
     * @return org.zaproxy.zap.utils.ZapTextField
     */
    private ZapTextField getTxtProxyIp() {
        if (txtProxyIp == null) {
            txtProxyIp = new ZapTextField("");
        }
        return txtProxyIp;
    }

    /**
     * This method initializes txtProxyIpSSL
     *
     * @return org.zaproxy.zap.utils.ZapTextField
     */
    private ZapTextField getTxtReverseProxyIp() {
        if (txtReverseProxyIp == null) {
            txtReverseProxyIp = new ZapTextField();
        }
        return txtReverseProxyIp;
    }

    public JCheckBox getChkRemoveUnsupportedEncodings() {
        if (chkRemoveUnsupportedEncodings == null) {
            chkRemoveUnsupportedEncodings = new JCheckBox(Constant.messages.getString("options.proxy.local.label.removeUnsupportedEncodings"));
            chkRemoveUnsupportedEncodings.setToolTipText(Constant.messages.getString("options.proxy.local.tooltip.removeUnsupportedEncodings"));
        }
        return chkRemoveUnsupportedEncodings;
    }
    
    private JCheckBox getChkAlwaysDecodeGzip() {
        if (chkAlwaysDecodeGzip == null) {
        	chkAlwaysDecodeGzip = new JCheckBox(Constant.messages.getString("options.proxy.local.label.alwaysDecodeGzip"));
        	chkAlwaysDecodeGzip.setToolTipText(Constant.messages.getString("options.proxy.local.tooltip.alwaysDecodeGzip"));
        }
    	return chkAlwaysDecodeGzip;
    }


    /**
     * This method initializes spinnerProxyPort
     *
     * @return ZapPortNumberSpinner
     */
    private ZapPortNumberSpinner getSpinnerProxyPort() {
        if (spinnerProxyPort == null) {
            // ZAP: Do not allow invalid port numbers
            spinnerProxyPort = new ZapPortNumberSpinner(8080);
        }
        return spinnerProxyPort;
    }

    /**
     * This method initializes spinnerReverseProxyHttpPort
     *
     * @return ZapPortNumberSpinner
     */
    private ZapPortNumberSpinner getSpinnerReverseProxyHttpPort() {
        if (spinnerReverseProxyHttpPort == null) {
            // ZAP: Do not allow invalid port numbers
            spinnerReverseProxyHttpPort = new ZapPortNumberSpinner(80);
        }
        return spinnerReverseProxyHttpPort;
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("options.proxy.local.title"));
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(391, 320);
        }
        
        securityProtocolsPanel = new SecurityProtocolsPanel();
        this.add(getPanelProxy(), getPanelProxy().getName());

        getChkReverseProxy().setVisible(Constant.isSP());
        getPanelReverseProxy().setVisible(Constant.isSP());

    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        ProxyParam proxyParam = optionsParam.getProxyParam();

        // set Local Proxy parameters
        // ZAP: in the Options dialog we can show the real value of the field
        // and null means that the listener should be bound to all interfaces
        txtProxyIp.setText(proxyParam.getRawProxyIP());
        txtProxyIp.discardAllEdits();
        
        // ZAP: Do not allow invalid port numbers
        spinnerProxyPort.setValue(proxyParam.getProxyPort());

        chkRemoveUnsupportedEncodings.setSelected(proxyParam.isRemoveUnsupportedEncodings());
        chkAlwaysDecodeGzip.setSelected(proxyParam.isAlwaysDecodeGzip());

        // set reverse proxy param
        txtReverseProxyIp.setText(proxyParam.getReverseProxyIp());
        txtReverseProxyIp.discardAllEdits();
        
        // ZAP: Do not allow invalid port numbers
        spinnerReverseProxyHttpPort.setValue(proxyParam.getReverseProxyHttpPort());
        spinnerReverseProxyHttpsPort.setValue(proxyParam.getReverseProxyHttpsPort());

        chkReverseProxy.setSelected(proxyParam.isUseReverseProxy());
        setReverseProxyEnabled(proxyParam.isUseReverseProxy());

        securityProtocolsPanel.setSecurityProtocolsEnabled(proxyParam.getSecurityProtocolsEnabled());
    }

    @Override
    public void validateParam(Object obj) throws Exception {
        securityProtocolsPanel.validateSecurityProtocols();
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        ProxyParam proxyParam = optionsParam.getProxyParam();

        proxyParam.setProxyIp(txtProxyIp.getText());
        // ZAP: Do not allow invalid port numbers
        proxyParam.setProxyPort(spinnerProxyPort.getValue());

        proxyParam.setRemoveUnsupportedEncodings(getChkRemoveUnsupportedEncodings().isSelected());
        // TODO hacking
        proxyParam.setAlwaysDecodeGzip(getChkAlwaysDecodeGzip().isSelected());

        proxyParam.setReverseProxyIp(txtReverseProxyIp.getText());
        // ZAP: Do not allow invalid port numbers
        proxyParam.setReverseProxyHttpPort(spinnerReverseProxyHttpPort.getValue());
        proxyParam.setReverseProxyHttpsPort(spinnerReverseProxyHttpsPort.getValue());
        proxyParam.setUseReverseProxy(getChkReverseProxy().isSelected());

        proxyParam.setSecurityProtocolsEnabled(securityProtocolsPanel.getSelectedProtocols());
    }

    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getChkReverseProxy() {
        if (chkReverseProxy == null) {
            chkReverseProxy = new JCheckBox();
            chkReverseProxy.setText(Constant.messages.getString("options.proxy.local.label.userev"));
            chkReverseProxy.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setReverseProxyEnabled(getChkReverseProxy().isSelected());
                }
            });
        }
        return chkReverseProxy;
    }

    /**
     * This method initializes spinnerReverseProxyHttpsPort
     *
     * @return ZapPortNumberSpinner
     */
    private ZapPortNumberSpinner getSpinnerReverseProxyHttpsPort() {
        if (spinnerReverseProxyHttpsPort == null) {
            // ZAP: Do not allow invalid port numbers
            spinnerReverseProxyHttpsPort = new ZapPortNumberSpinner(443);
        }
        return spinnerReverseProxyHttpsPort;
    }

    private void setReverseProxyEnabled(boolean isEnabled) {

        txtProxyIp.setEditable(!isEnabled);
        spinnerProxyPort.setEditable(!isEnabled);

        txtReverseProxyIp.setEditable(isEnabled);
        spinnerReverseProxyHttpPort.setEditable(isEnabled);
        spinnerReverseProxyHttpsPort.setEditable(isEnabled);

        Color color = Color.WHITE;

        if (isEnabled) {
            txtProxyIp.setBackground(panelProxy.getBackground());
            spinnerProxyPort.setBackground(panelProxy.getBackground());

            txtReverseProxyIp.setBackground(Color.WHITE);
            spinnerReverseProxyHttpPort.setBackground(Color.WHITE);
            spinnerReverseProxyHttpsPort.setBackground(Color.WHITE);

        } else {

            txtProxyIp.setBackground(Color.WHITE);
            spinnerProxyPort.setBackground(Color.WHITE);

            txtReverseProxyIp.setBackground(panelProxy.getBackground());
            spinnerReverseProxyHttpPort.setBackground(panelProxy.getBackground());
            spinnerReverseProxyHttpsPort.setBackground(panelProxy.getBackground());

        }
    }

    @Override
    public String getHelpIndex() {
        // ZAP: added help index
        return "ui.dialogs.options.localproxy";
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
