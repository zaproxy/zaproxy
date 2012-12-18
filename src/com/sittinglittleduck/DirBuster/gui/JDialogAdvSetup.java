/*
 * JDialogAdvSetup.java
 *
 * Created on November 29, 2006, 1:50 PM
 *
 * Copyright 2007 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */
package com.sittinglittleduck.DirBuster.gui;

import java.io.File;

import javax.swing.JFileChooser;

import com.sittinglittleduck.DirBuster.Config;
import com.sittinglittleduck.DirBuster.HTMLelementToParse;
import com.sittinglittleduck.DirBuster.HTTPHeader;
import com.sittinglittleduck.DirBuster.Manager;

/**
 *
 * @author  james
 */
public class JDialogAdvSetup extends javax.swing.JDialog
{

    private HTTPHeaderTableModel httpHeaderTableModel;
    private HTMLParseTableModel htmlParseTableModel;
    private Manager manager;

    /** Creates new form JDialogAdvSetup */
    public JDialogAdvSetup(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);

        this.manager = Manager.getInstance();
        this.setTitle("DirBuster " + Config.version + " - Advanced Options");
        httpHeaderTableModel = new HTTPHeaderTableModel(this.manager.getHTTPHeaders());
        htmlParseTableModel = new HTMLParseTableModel(this.manager.getElementsToParse());
        initComponents();

        /*
         * Set the value for the timeout and fail case
         */

        this.jTextFieldTimeout.setText(String.valueOf(Config.connectionTimeout));
        this.jTextFieldFailCaseString.setText(Config.failCaseString);

        /*
         * Populate the extentions list that are not be be parsed
         */
        String extsToMiss = "";
        for(int a = 0; a < manager.extsToMiss.size(); a++)
        {
            if(a == manager.extsToMiss.size() - 1)
            {
                extsToMiss = extsToMiss + manager.extsToMiss.elementAt(a);
            }
            else
            {
                extsToMiss = extsToMiss + manager.extsToMiss.elementAt(a) + ",";
            }
        }

        /*
         * Set the values for the proxy information
         */

        this.jCheckBoxRunViaProxy.setSelected(manager.isUseProxy());

        if(manager.isUseProxy())
        {
            jTextFieldProxyHost.setEnabled(true);
            jTextFieldProxyPort.setEnabled(true);
            jCheckBoxUseProxyAuth.setEnabled(true);
        }

        jTextFieldProxyHost.setText(manager.getProxyHost());
        jTextFieldProxyPort.setText(String.valueOf(manager.getProxyPort()));


        this.jCheckBoxUseProxyAuth.setSelected(manager.isUseProxyAuth());

        if(manager.isUseProxyAuth())
        {
            jTextFieldProxyUserName.setEnabled(true);
            jTextFieldProxyPassword.setEnabled(true);
            jTextFieldProxyRealm.setEnabled(true);
        }

        jTextFieldProxyUserName.setText(manager.getProxyUsername());
        jTextFieldProxyPassword.setText(manager.getProxyPassword());
        jTextFieldProxyRealm.setText(manager.getProxyRealm());

        /*
         * Get the currently saved settings for the http auth
         */

        this.jCheckBoxUseAuth.setSelected(manager.isUseHTTPauth());

        if(manager.isUseHTTPauth())
        {
            jTextFieldAuthUserName.setEnabled(true);
            jPasswordFieldAuth.setEnabled(true);
            jTextFieldRealmDomain.setEnabled(true);
            jComboBoxAuthType.setEnabled(true);
        }

        this.jTextFieldAuthUserName.setText(manager.getUserName());
        this.jPasswordFieldAuth.setText(manager.getPassword());
        this.jTextFieldRealmDomain.setText(manager.getRealmDomain());

        //TODO add select the right item in the combo box

        jTextFieldExtToMiss.setText(extsToMiss);

        jCheckBoxLimitReqSec.setSelected(manager.isLimitRequests());

        if(manager.isLimitRequests())
        {
            jTextFieldLimitReqSec.setEnabled(true);
            jLabelLimitReqSec.setEnabled(true);
        }
        else
        {
            jTextFieldLimitReqSec.setEnabled(false);
            jLabelLimitReqSec.setEnabled(false);
        }
        jTextFieldLimitReqSec.setText(String.valueOf(manager.getLimitRequestsTo()));

        /*
         * set the allow updates flag
         */
        jCheckBoxAllowUpdates.setSelected(manager.isCheckForUpdates());

        /*
         * set the user agent
         */
        jTextFieldUserAgent.setText(Config.userAgent);

        /*
         * set the default setting stored in user prefs
         */

        jTextFieldDefaultExts.setText(manager.getDefaultExts());
        jTextFieldDefaultList.setText(manager.getDefaultList());
        jTextFieldDefaultNoThreads.setText(String.valueOf(manager.getDefaultNoThreads()));


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenuHTTPHeaders = new javax.swing.JPopupMenu();
        jMenuItemDelete = new javax.swing.JMenuItem();
        jPopupMenuHTMLparseElements = new javax.swing.JPopupMenu();
        jMenuItemDeleteElement = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelHTMLParse = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldExtToMiss = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableHTMLParseElements = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldHTMLTag = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldHTMLAttr = new javax.swing.JTextField();
        jButtonAddHTMLToParse = new javax.swing.JButton();
        jPanelAuthDetails = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldAuthUserName = new javax.swing.JTextField();
        jTextFieldRealmDomain = new javax.swing.JTextField();
        jPasswordFieldAuth = new javax.swing.JPasswordField();
        jComboBoxAuthType = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        jCheckBoxUseAuth = new javax.swing.JCheckBox();
        jPanelScanOpts = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableHTTPHeaders = new javax.swing.JTable();
        jTextFieldHTTPHeader = new javax.swing.JTextField();
        jTextFieldHTTPHeaderValue = new javax.swing.JTextField();
        jButtonAddHeader = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxRunViaProxy = new javax.swing.JCheckBox();
        jTextFieldProxyHost = new javax.swing.JTextField();
        jTextFieldProxyPort = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxUseProxyAuth = new javax.swing.JCheckBox();
        jTextFieldProxyUserName = new javax.swing.JTextField();
        jTextFieldProxyPassword = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldProxyRealm = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextFieldUserAgent = new javax.swing.JTextField();
        jSeparator5 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jTextFieldTimeout = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextFieldFailCaseString = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jCheckBoxLimitReqSec = new javax.swing.JCheckBox();
        jTextFieldLimitReqSec = new javax.swing.JTextField();
        jLabelLimitReqSec = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBoxAllowUpdates = new javax.swing.JCheckBox();
        jButtonCheckNow = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel10 = new javax.swing.JLabel();
        jTextFieldDefaultNoThreads = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jTextFieldDefaultList = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jTextFieldDefaultExts = new javax.swing.JTextField();
        jSeparator6 = new javax.swing.JSeparator();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        jMenuItemDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/list-remove.png"))); // NOI18N
        jMenuItemDelete.setMnemonic('d');
        jMenuItemDelete.setText("Delete header");
        jMenuItemDelete.setToolTipText("Delete a custom HTTP header");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });
        jPopupMenuHTTPHeaders.add(jMenuItemDelete);

        jMenuItemDeleteElement.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/list-remove.png"))); // NOI18N
        jMenuItemDeleteElement.setMnemonic('d');
        jMenuItemDeleteElement.setText("Delete item");
        jMenuItemDeleteElement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteElementActionPerformed(evt);
            }
        });
        jPopupMenuHTMLparseElements.add(jMenuItemDeleteElement);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel13.setText("File extentions to not process");

        jTextFieldExtToMiss.setFont(new java.awt.Font("Arial", 0, 12));

        jLabel14.setText("HTML elements to extract links from");

        jTableHTMLParseElements.setFont(new java.awt.Font("Arial", 0, 12));
        jTableHTMLParseElements.setModel(htmlParseTableModel);
        jTableHTMLParseElements.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableHTMLParseElementsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTableHTMLParseElements);

        jLabel16.setText("Tag");

        jLabel17.setText("Attribute");

        jButtonAddHTMLToParse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/list-add.png"))); // NOI18N
        jButtonAddHTMLToParse.setText("Add");
        jButtonAddHTMLToParse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddHTMLToParseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelHTMLParseLayout = new org.jdesktop.layout.GroupLayout(jPanelHTMLParse);
        jPanelHTMLParse.setLayout(jPanelHTMLParseLayout);
        jPanelHTMLParseLayout.setHorizontalGroup(
            jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelHTMLParseLayout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanelHTMLParseLayout.createSequentialGroup()
                        .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel16)
                            .add(jPanelHTMLParseLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldHTMLTag, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(30, 30, 30)
                        .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanelHTMLParseLayout.createSequentialGroup()
                                .add(jTextFieldHTMLAttr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jButtonAddHTMLToParse)))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelHTMLParseLayout.createSequentialGroup()
                        .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
                        .addContainerGap())))
            .add(jPanelHTMLParseLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTextFieldExtToMiss, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(243, Short.MAX_VALUE))
            .add(jPanelHTMLParseLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 310, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(283, Short.MAX_VALUE))
        );
        jPanelHTMLParseLayout.setVerticalGroup(
            jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelHTMLParseLayout.createSequentialGroup()
                .add(13, 13, 13)
                .add(jLabel13)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldExtToMiss, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(37, 37, 37)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(jLabel14)
                .add(15, 15, 15)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                .add(20, 20, 20)
                .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel16)
                    .add(jPanelHTMLParseLayout.createSequentialGroup()
                        .add(jLabel17)
                        .add(5, 5, 5)
                        .add(jPanelHTMLParseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextFieldHTMLAttr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButtonAddHTMLToParse)
                            .add(jTextFieldHTMLTag, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(45, 45, 45))
        );

        jTabbedPane1.addTab("HTML Parsing Options", jPanelHTMLParse);

        jLabel18.setText("Authentication Options");

        jLabel19.setText("User Name ");

        jLabel20.setText("Password");

        jLabel21.setText("Realm / Domain");

        jTextFieldAuthUserName.setToolTipText("Please enter the username");
        jTextFieldAuthUserName.setEnabled(false);

        jTextFieldRealmDomain.setEnabled(false);

        jPasswordFieldAuth.setToolTipText("Please enter the password");
        jPasswordFieldAuth.setEnabled(false);

        jComboBoxAuthType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Basic", "Digest", "NTML" }));
        jComboBoxAuthType.setEnabled(false);

        jLabel22.setText("Authentication type");

        jCheckBoxUseAuth.setText("Use HTTP Authentication");
        jCheckBoxUseAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseAuthActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelAuthDetailsLayout = new org.jdesktop.layout.GroupLayout(jPanelAuthDetails);
        jPanelAuthDetails.setLayout(jPanelAuthDetailsLayout);
        jPanelAuthDetailsLayout.setHorizontalGroup(
            jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelAuthDetailsLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxUseAuth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanelAuthDetailsLayout.createSequentialGroup()
                        .add(jLabel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(10, 10, 10)
                        .add(jTextFieldRealmDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanelAuthDetailsLayout.createSequentialGroup()
                        .add(jLabel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxAuthType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 203, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanelAuthDetailsLayout.createSequentialGroup()
                        .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jLabel20, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel19, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE))
                        .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanelAuthDetailsLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextFieldAuthUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelAuthDetailsLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPasswordFieldAuth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
        );
        jPanelAuthDetailsLayout.setVerticalGroup(
            jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelAuthDetailsLayout.createSequentialGroup()
                .add(10, 10, 10)
                .add(jLabel18)
                .add(16, 16, 16)
                .add(jCheckBoxUseAuth)
                .add(18, 18, 18)
                .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldAuthUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel19))
                .add(12, 12, 12)
                .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPasswordFieldAuth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel20))
                .add(12, 12, 12)
                .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel21)
                    .add(jTextFieldRealmDomain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(22, 22, 22)
                .add(jPanelAuthDetailsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel22)
                    .add(jComboBoxAuthType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jTabbedPane1.addTab("Authentication Options", jPanelAuthDetails);

        jLabel1.setText("Custom HTTP Headers");

        jTableHTTPHeaders.setModel(httpHeaderTableModel);
        jTableHTTPHeaders.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableHTTPHeadersMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableHTTPHeaders);

        jButtonAddHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/list-add.png"))); // NOI18N
        jButtonAddHeader.setText("Add");
        jButtonAddHeader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddHeaderActionPerformed(evt);
            }
        });

        jLabel2.setText("Add New Custom HTTP Header");

        jLabel3.setText("Proxy Information & Authentifcation");

        jCheckBoxRunViaProxy.setText("Run Through a Proxy");
        jCheckBoxRunViaProxy.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxRunViaProxy.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxRunViaProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRunViaProxyActionPerformed(evt);
            }
        });

        jTextFieldProxyHost.setEnabled(false);

        jTextFieldProxyPort.setText("8080");
        jTextFieldProxyPort.setEnabled(false);

        jLabel4.setText("Host");

        jLabel5.setText("Port");

        jCheckBoxUseProxyAuth.setText("Use Proxy Authentifcation");
        jCheckBoxUseProxyAuth.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxUseProxyAuth.setEnabled(false);
        jCheckBoxUseProxyAuth.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxUseProxyAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUseProxyAuthActionPerformed(evt);
            }
        });

        jTextFieldProxyUserName.setEnabled(false);

        jTextFieldProxyPassword.setEnabled(false);

        jLabel6.setText("User Name");

        jLabel7.setText("Password");

        jLabel8.setText("Realm");

        jTextFieldProxyRealm.setEnabled(false);

        jLabel9.setText("(Leave blank if not required)");

        jLabel15.setText(":");

        jLabel11.setText("Http User Agent");

        org.jdesktop.layout.GroupLayout jPanelScanOptsLayout = new org.jdesktop.layout.GroupLayout(jPanelScanOpts);
        jPanelScanOpts.setLayout(jPanelScanOptsLayout);
        jPanelScanOptsLayout.setHorizontalGroup(
            jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelScanOptsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanelScanOptsLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldHTTPHeader, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(4, 4, 4)
                        .add(jLabel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jTextFieldHTTPHeaderValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jButtonAddHeader)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScanOptsLayout.createSequentialGroup()
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScanOptsLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(jPanelScanOptsLayout.createSequentialGroup()
                        .add(jLabel11)
                        .addContainerGap(493, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScanOptsLayout.createSequentialGroup()
                        .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jCheckBoxRunViaProxy, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 160, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jCheckBoxUseProxyAuth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelScanOptsLayout.createSequentialGroup()
                                .add(jTextFieldProxyRealm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(20, 20, 20)
                                .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelScanOptsLayout.createSequentialGroup()
                                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(100, 100, 100)
                                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelScanOptsLayout.createSequentialGroup()
                                .add(jTextFieldProxyUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 170, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(10, 10, 10)
                                .add(jTextFieldProxyPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanelScanOptsLayout.createSequentialGroup()
                                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jTextFieldProxyHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 220, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel4))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel5)
                                    .add(jTextFieldProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(280, 280, 280))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScanOptsLayout.createSequentialGroup()
                        .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldUserAgent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                            .add(jSeparator5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanelScanOptsLayout.setVerticalGroup(
            jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelScanOptsLayout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .add(10, 10, 10)
                .add(jLabel2)
                .add(5, 5, 5)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldHTTPHeader, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel15)
                    .add(jTextFieldHTTPHeaderValue, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jButtonAddHeader))
                .add(21, 21, 21)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel11)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldUserAgent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(jSeparator5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxRunViaProxy)
                .add(6, 6, 6)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldProxyHost, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldProxyPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(12, 12, 12)
                .add(jCheckBoxUseProxyAuth)
                .add(6, 6, 6)
                .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(5, 5, 5)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldProxyRealm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .add(12, 12, 12)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel7))
                .add(6, 6, 6)
                .add(jPanelScanOptsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldProxyUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTextFieldProxyPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Http Options", jPanelScanOpts);

        jLabel23.setText("Connection Time out");

        jLabel24.setText("(In Seconds)");

        jLabel25.setText("Fail Case String");

        jCheckBoxLimitReqSec.setText("Limit number of requests per second");
        jCheckBoxLimitReqSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLimitReqSecActionPerformed(evt);
            }
        });

        jTextFieldLimitReqSec.setEnabled(false);
        jTextFieldLimitReqSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldLimitReqSecActionPerformed(evt);
            }
        });

        jLabelLimitReqSec.setText("Number of requests per second");
        jLabelLimitReqSec.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(jTextFieldLimitReqSec, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabelLimitReqSec))
                    .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                    .add(jCheckBoxLimitReqSec)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jLabel23, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(jTextFieldTimeout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(10, 10, 10)
                                .add(jLabel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(30, 30, 30)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jTextFieldFailCaseString, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel23)
                    .add(jLabel25))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextFieldTimeout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel24)
                    .add(jTextFieldFailCaseString, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxLimitReqSec)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelLimitReqSec)
                    .add(jTextFieldLimitReqSec, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(369, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Scan Options", jPanel1);

        jCheckBoxAllowUpdates.setText("Allow DirBuster to check for updates");
        jCheckBoxAllowUpdates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAllowUpdatesActionPerformed(evt);
            }
        });

        jButtonCheckNow.setText("Check Now");
        jButtonCheckNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCheckNowActionPerformed(evt);
            }
        });

        jLabel10.setText("Number of threads:");

        jTextFieldDefaultNoThreads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDefaultNoThreadsActionPerformed(evt);
            }
        });

        jLabel12.setText("Default settings (DirBuster must be restarted for settings to be applied)");

        jLabel26.setText("Dir list to use:");

        jLabel27.setText("File extentions to test:");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/system-search.png"))); // NOI18N
        jButton1.setText("Browse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                    .add(jButtonCheckNow)
                    .add(jCheckBoxAllowUpdates)
                    .add(jLabel12)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel27)
                            .add(jLabel10)
                            .add(jLabel26))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTextFieldDefaultNoThreads, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 63, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                                .add(jTextFieldDefaultList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButton1))
                            .add(jTextFieldDefaultExts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxAllowUpdates)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButtonCheckNow)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(jLabel12)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(jTextFieldDefaultNoThreads, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel26)
                    .add(jButton1)
                    .add(jTextFieldDefaultList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel27)
                    .add(jTextFieldDefaultExts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(283, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("DirBuster Options", jPanel2);

        jButtonOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/accept.png"))); // NOI18N
        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/fileclose.png"))); // NOI18N
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(378, Short.MAX_VALUE)
                .add(jButtonCancel)
                .add(18, 18, 18)
                .add(jButtonOk)
                .addContainerGap())
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {jButtonCancel, jButtonOk}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButtonOk)
                    .add(jButtonCancel)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-620)/2, (screenSize.height-626)/2, 620, 626);
    }// </editor-fold>//GEN-END:initComponents
    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemDeleteActionPerformed
    {//GEN-HEADEREND:event_jMenuItemDeleteActionPerformed
        int index = jTableHTTPHeaders.getSelectedRow();
        manager.HTTPheaders.removeElementAt(index);
        updateTable();
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void jMenuItemDeleteElementActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemDeleteElementActionPerformed
    {//GEN-HEADEREND:event_jMenuItemDeleteElementActionPerformed
        int index = jTableHTMLParseElements.getSelectedRow();
        manager.elementsToParse.removeElementAt(index);
        updateTable();
    }//GEN-LAST:event_jMenuItemDeleteElementActionPerformed

    private void jTableHTMLParseElementsMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTableHTMLParseElementsMouseClicked
    {//GEN-HEADEREND:event_jTableHTMLParseElementsMouseClicked
        if(evt.getButton() == 2 || evt.getButton() == 3)
        {
            if(jTableHTMLParseElements.getSelectedRowCount() != 0)
            {
                jPopupMenuHTMLparseElements.show(evt.getComponent(), evt.getX(), evt.getY());
            }

        }
    }//GEN-LAST:event_jTableHTMLParseElementsMouseClicked

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
    {//GEN-HEADEREND:event_jButtonCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonAddHTMLToParseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddHTMLToParseActionPerformed
    {//GEN-HEADEREND:event_jButtonAddHTMLToParseActionPerformed
        manager.elementsToParse.addElement(new HTMLelementToParse(jTextFieldHTMLTag.getText(), jTextFieldHTMLAttr.getText()));
        this.updateTable();
        //clear the jTextFields;
        jTextFieldHTTPHeader.setText("");
        jTextFieldHTTPHeaderValue.setText("");
    }//GEN-LAST:event_jButtonAddHTMLToParseActionPerformed

    private void jCheckBoxUseProxyAuthActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxUseProxyAuthActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxUseProxyAuthActionPerformed
        if(jCheckBoxUseProxyAuth.isSelected())
        {
            jTextFieldProxyUserName.setEnabled(true);
            jTextFieldProxyPassword.setEnabled(true);
            jTextFieldProxyRealm.setEnabled(true);
        }
        else
        {
            jTextFieldProxyUserName.setEnabled(false);
            jTextFieldProxyPassword.setEnabled(false);
            jTextFieldProxyRealm.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBoxUseProxyAuthActionPerformed

    private void jCheckBoxRunViaProxyActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxRunViaProxyActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxRunViaProxyActionPerformed
        if(jCheckBoxRunViaProxy.isSelected())
        {
            jTextFieldProxyHost.setEnabled(true);
            jTextFieldProxyPort.setEnabled(true);
            jCheckBoxUseProxyAuth.setEnabled(true);

            if(jCheckBoxUseProxyAuth.isSelected())
            {
                jTextFieldProxyUserName.setEnabled(true);
                jTextFieldProxyPassword.setEnabled(true);
                jTextFieldProxyRealm.setEnabled(true);
            }
            else
            {
                jTextFieldProxyUserName.setEnabled(false);
                jTextFieldProxyPassword.setEnabled(false);
                jTextFieldProxyRealm.setEnabled(false);
            }
        }
        else
        {
            jTextFieldProxyHost.setEnabled(false);
            jTextFieldProxyPort.setEnabled(false);
            jCheckBoxUseProxyAuth.setEnabled(false);
            jTextFieldProxyUserName.setEnabled(false);
            jTextFieldProxyPassword.setEnabled(false);
            jTextFieldProxyRealm.setEnabled(false);

        }
    }//GEN-LAST:event_jCheckBoxRunViaProxyActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        //if we are using a proxy

        /*
         * save the proxy information
         */
        manager.setUseProxy(jCheckBoxRunViaProxy.isSelected());
        manager.setProxyHost(jTextFieldProxyHost.getText());
        manager.setProxyPort(Integer.parseInt(jTextFieldProxyPort.getText()));
        manager.setUseProxyAuth(jCheckBoxUseProxyAuth.isSelected());
        manager.setProxyUsername(jTextFieldProxyUserName.getText());
        manager.setProxyPassword(jTextFieldProxyPassword.getText());
        if(jTextFieldProxyRealm.getText().equalsIgnoreCase(""))
        {
            manager.setProxyRealm(null);
        }
        else
        {
            manager.setProxyRealm(jTextFieldProxyRealm.getText());
        }



        try
        {

            Config.connectionTimeout = Integer.parseInt(jTextFieldTimeout.getText());

        }
        catch(NumberFormatException ex)
        {
            //TODO: throw error when connection time out is not a number
        }

        if(jTextFieldFailCaseString.getText() != null && !jTextFieldFailCaseString.getText().equals(""))
        {

            Config.failCaseString = jTextFieldFailCaseString.getText();
        }
        else
        {
            //process the error
        }

        String exts = jTextFieldExtToMiss.getText();

        String[] extsArray = exts.split(",");

        manager.extsToMiss.clear();

        for(int a = 0; a < extsArray.length; a++)
        {
            manager.extsToMiss.addElement(extsArray[a]);
        }

        //if we are using auth
        if(jCheckBoxUseAuth.isSelected())
        {
            manager.setAuthDetails(jTextFieldAuthUserName.getText(), String.valueOf(jPasswordFieldAuth.getPassword()), jTextFieldRealmDomain.getText(), (String) jComboBoxAuthType.getSelectedItem());
        }
        //if we are not using auth;
        else
        {
            manager.setDoNotUseAuth();
        }

        /*
         * read the setting for limiting responces
         */
        manager.setLimitRequests(jCheckBoxLimitReqSec.isSelected());
        //TODO add error handling
        manager.setLimitRequestsTo(Integer.parseInt(jTextFieldLimitReqSec.getText()));

        /*
         * update the is allow to check for updates setting
         */
        manager.setCheckForUpdates(jCheckBoxAllowUpdates.isSelected());

        /*
         * update the defauts settings
         */
        manager.setDefaultList(jTextFieldDefaultList.getText());
        manager.setDefaultNoThreads(Integer.parseInt(jTextFieldDefaultNoThreads.getText()));
        manager.setDefaultExts(jTextFieldDefaultExts.getText());

        /*
         * set the custom user agent
         */
        Config.userAgent = jTextFieldUserAgent.getText();

        this.dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonAddHeaderActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddHeaderActionPerformed
    {//GEN-HEADEREND:event_jButtonAddHeaderActionPerformed

        //TODO: add error checking here
        manager.addHTTPheader(new HTTPHeader(jTextFieldHTTPHeader.getText(), jTextFieldHTTPHeaderValue.getText()));
        this.updateTable();
        //clear the jTextFields;
        jTextFieldHTTPHeader.setText("");
        jTextFieldHTTPHeaderValue.setText("");
    }//GEN-LAST:event_jButtonAddHeaderActionPerformed

    private void jTableHTTPHeadersMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jTableHTTPHeadersMouseClicked
    {//GEN-HEADEREND:event_jTableHTTPHeadersMouseClicked
        if(evt.getButton() == 2 || evt.getButton() == 3)
        {
            if(jTableHTTPHeaders.getSelectedRowCount() != 0)
            {
                jPopupMenuHTTPHeaders.show(evt.getComponent(), evt.getX(), evt.getY());
            }

        }
    }//GEN-LAST:event_jTableHTTPHeadersMouseClicked

    private void jCheckBoxUseAuthActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxUseAuthActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxUseAuthActionPerformed
        if(jCheckBoxUseAuth.isSelected())
        {
            jTextFieldAuthUserName.setEnabled(true);
            jPasswordFieldAuth.setEnabled(true);
            jTextFieldRealmDomain.setEnabled(true);
            jComboBoxAuthType.setEnabled(true);
        }
        else
        {
            jTextFieldAuthUserName.setEnabled(false);
            jPasswordFieldAuth.setEnabled(false);
            jTextFieldRealmDomain.setEnabled(false);
            jComboBoxAuthType.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxUseAuthActionPerformed

    private void jTextFieldLimitReqSecActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldLimitReqSecActionPerformed
    {//GEN-HEADEREND:event_jTextFieldLimitReqSecActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldLimitReqSecActionPerformed

    private void jCheckBoxLimitReqSecActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxLimitReqSecActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxLimitReqSecActionPerformed
        if(jCheckBoxLimitReqSec.isSelected())
        {
            jTextFieldLimitReqSec.setEnabled(true);
            jLabelLimitReqSec.setEnabled(true);
        }
        else
        {
            jTextFieldLimitReqSec.setEnabled(false);
            jLabelLimitReqSec.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBoxLimitReqSecActionPerformed

    private void jCheckBoxAllowUpdatesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxAllowUpdatesActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxAllowUpdatesActionPerformed
}//GEN-LAST:event_jCheckBoxAllowUpdatesActionPerformed

    private void jButtonCheckNowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCheckNowActionPerformed
    {//GEN-HEADEREND:event_jButtonCheckNowActionPerformed
        manager.checkForUpdates(true);
}//GEN-LAST:event_jButtonCheckNowActionPerformed

    private void jTextFieldDefaultNoThreadsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldDefaultNoThreadsActionPerformed
    {//GEN-HEADEREND:event_jTextFieldDefaultNoThreadsActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldDefaultNoThreadsActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

    File dir = new File(System.getProperty("user.dir"));
    JFileChooser fc = new JFileChooser(dir);
    fc.setDialogTitle("Please Select The Directory/File List You Wish To Use");
    int returnVal = fc.showDialog(this, "Select List");
    if(returnVal == JFileChooser.APPROVE_OPTION)
    {
        File file = fc.getSelectedFile();
        jTextFieldDefaultList.setText(file.getAbsolutePath());
    }
}//GEN-LAST:event_jButton1ActionPerformed

    private void updateTable()
    {
        httpHeaderTableModel = new HTTPHeaderTableModel(this.manager.getHTTPHeaders());
        jTableHTTPHeaders.setModel(httpHeaderTableModel);

        htmlParseTableModel = new HTMLParseTableModel(manager.elementsToParse);
        jTableHTMLParseElements.setModel(htmlParseTableModel);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddHTMLToParse;
    private javax.swing.JButton jButtonAddHeader;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonCheckNow;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JCheckBox jCheckBoxAllowUpdates;
    private javax.swing.JCheckBox jCheckBoxLimitReqSec;
    private javax.swing.JCheckBox jCheckBoxRunViaProxy;
    private javax.swing.JCheckBox jCheckBoxUseAuth;
    private javax.swing.JCheckBox jCheckBoxUseProxyAuth;
    private javax.swing.JComboBox jComboBoxAuthType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelLimitReqSec;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JMenuItem jMenuItemDeleteElement;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAuthDetails;
    private javax.swing.JPanel jPanelHTMLParse;
    private javax.swing.JPanel jPanelScanOpts;
    private javax.swing.JPasswordField jPasswordFieldAuth;
    private javax.swing.JPopupMenu jPopupMenuHTMLparseElements;
    private javax.swing.JPopupMenu jPopupMenuHTTPHeaders;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableHTMLParseElements;
    private javax.swing.JTable jTableHTTPHeaders;
    private javax.swing.JTextField jTextFieldAuthUserName;
    private javax.swing.JTextField jTextFieldDefaultExts;
    private javax.swing.JTextField jTextFieldDefaultList;
    private javax.swing.JTextField jTextFieldDefaultNoThreads;
    private javax.swing.JTextField jTextFieldExtToMiss;
    private javax.swing.JTextField jTextFieldFailCaseString;
    private javax.swing.JTextField jTextFieldHTMLAttr;
    private javax.swing.JTextField jTextFieldHTMLTag;
    private javax.swing.JTextField jTextFieldHTTPHeader;
    private javax.swing.JTextField jTextFieldHTTPHeaderValue;
    private javax.swing.JTextField jTextFieldLimitReqSec;
    private javax.swing.JTextField jTextFieldProxyHost;
    private javax.swing.JTextField jTextFieldProxyPassword;
    private javax.swing.JTextField jTextFieldProxyPort;
    private javax.swing.JTextField jTextFieldProxyRealm;
    private javax.swing.JTextField jTextFieldProxyUserName;
    private javax.swing.JTextField jTextFieldRealmDomain;
    private javax.swing.JTextField jTextFieldTimeout;
    private javax.swing.JTextField jTextFieldUserAgent;
    // End of variables declaration//GEN-END:variables
}
