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
// ZAP: 2011/11/20 Handle dialogs with no children
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/25 Added @SuppressWarnings annotation in the method
//      getTreeNodeFromPanelName(String).
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2013/08/05 Added accessor to shown panels
// ZAP: 2013/08/21 Added support for detecting when AbstractParamPanels are being shown/hidden in a AbstractParamDialog
// ZAP: 2013/11/28 Issue 923: Allow a footer to be set.
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/12/10 Issue 1427: Standardize on [Cancel] [OK] button order

package org.parosproxy.paros.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;

public class AbstractParamDialog extends AbstractDialog {

    private static final long serialVersionUID = -5223178126156052670L;
    
    private int exitResult = JOptionPane.CANCEL_OPTION;
    
    private JPanel jContentPane = null;
    private JButton btnOK = null;
    private JButton btnCancel = null;
    private AbstractParamContainerPanel jSplitPane;
    //private JSplitPane jSplitPane = null;
    private JLabel footer = null;
    private String rootName = null;
    
    public AbstractParamDialog() {
        super();
        initialize();
    }

    /**
     * @param parent
     * @param modal
     * @param title
     * @param rootName
     * @throws HeadlessException
     */
    public AbstractParamDialog(Window parent, boolean modal, String title, String rootName) throws HeadlessException {
        super(parent, modal);
        this.rootName = rootName;
        initialize();
        this.setTitle(title);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        // enables the options dialog to be in front, but an modal dialog
        // stays on top of the main application window, but doesn't block childs
        // Examples of childs: help window and client certificate viewer
        this.setModalityType(ModalityType.DOCUMENT_MODAL);

        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {            
            this.setSize(500, 375);
        }
        
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
    }

    /**
     *
     * This method initializes jContentPane	     *
     *
     *
     * @return javax.swing.JPanel	     *
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints14 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints12 = new GridBagConstraints();

            footer = new JLabel();

            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new GridBagLayout());
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.gridy = 1;
            gridBagConstraints12.ipadx = 0;
            gridBagConstraints12.ipady = 0;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints12.weightx = 1.0D;
            gridBagConstraints13.gridx = 1;
            gridBagConstraints13.gridy = 1;
            gridBagConstraints13.ipadx = 0;
            gridBagConstraints13.ipady = 0;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints13.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints13.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints14.gridx = 2;
            gridBagConstraints14.gridy = 1;
            gridBagConstraints14.ipadx = 0;
            gridBagConstraints14.ipady = 0;
            gridBagConstraints14.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints14.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.weighty = 1.0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.gridwidth = 3;
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            jContentPane.add(getJSplitPane(), gridBagConstraints1);
            jContentPane.add(footer, gridBagConstraints12);
            jContentPane.add(getBtnCancel(), gridBagConstraints13);
            jContentPane.add(getBtnOK(), gridBagConstraints14);
        }
        
        return jContentPane;
    }

    /**
     * 
     * @param str 
     */
    public void setFooter(String str) {
        footer.setText(str);
    }

    /**
     * This method initializes btnOK
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnOK() {
        if (btnOK == null) {
            btnOK = new JButton();
            btnOK.setName("btnOK");
            btnOK.setText(Constant.messages.getString("all.button.ok"));
            btnOK.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {

                    try {
                        validateParam();
                        saveParam();
                        exitResult = JOptionPane.OK_OPTION;

                        AbstractParamDialog.this.setVisible(false);

                    } catch (Exception ex) {
                    	// The exception messages should be internationalized!
                        View.getSingleton().showWarningDialog(ex.getMessage());
                    }

                }
            });

        }
        return btnOK;
    }

    /**
     * This method initializes btnCancel
     *
     * @return javax.swing.JButton
     */
    protected JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setName("btnCancel");
            btnCancel.setText(Constant.messages.getString("all.button.cancel"));
            btnCancel.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {

                    exitResult = JOptionPane.CANCEL_OPTION;
                    AbstractParamDialog.this.setVisible(false);
                }
            });

        }
        return btnCancel;
    }

    /**
     * This method initializes jSplitPane
     *
     * @return javax.swing.JSplitPane
     */
    private AbstractParamContainerPanel getJSplitPane() {
        if (jSplitPane == null) {
            jSplitPane = new AbstractParamContainerPanel();
            jSplitPane.setVisible(true);
            if (this.rootName != null) {
                jSplitPane.getRootNode().setUserObject(rootName);
            }
        }
        
        return jSplitPane;
    }


    /**
     * If multiple name use the same panel
     *
     * @param parentParams
     * @param name
     * @param panel
     */
    // ZAP: Added sort option
    public void addParamPanel(String[] parentParams, String name, AbstractParamPanel panel, boolean sort) {
    	this.getJSplitPane().addParamPanel(parentParams, name, panel, sort);
    }

    public void addParamPanel(String[] parentParams, AbstractParamPanel panel, boolean sort) {
        addParamPanel(parentParams, panel.getName(), panel, sort);
    }

    public void removeParamPanel(AbstractParamPanel panel) {
    	this.getJSplitPane().removeParamPanel(panel);
    }

    // ZAP: Made public so that other classes can specify which panel is displayed
    public void showParamPanel(String parent, String child) {
    	this.getJSplitPane().showParamPanel(parent, child);
    }

    /**
     * 
     * @param name 
     */
    public void showParamPanel(String name) {
    	this.getJSplitPane().showParamPanel(name);
    }

    /**
     * 
     * @param panel
     * @param name 
     */
    public void showParamPanel(AbstractParamPanel panel, String name) {
    	this.getJSplitPane().showParamPanel(panel, name);
    }

    public void initParam(Object obj) {
    	this.getJSplitPane().initParam(obj);
    }

    /**
     * This method is to be overrided by subclass.
     *
     */
    public void validateParam() throws Exception {
    	this.getJSplitPane().validateParam();
    }

    /**
     * This method is to be overrided by subclass.
     *
     */
    public void saveParam() throws Exception {
    	this.getJSplitPane().saveParam();
    }

    protected void expandRoot() {
        this.getJSplitPane().expandRoot();
    }

    public int showDialog(boolean showRoot) {
        return showDialog(showRoot, null);
    }

    // ZAP: Added option to specify panel - note this only supports one level at the moment
    // ZAP: show the last selected panel
    public int showDialog(boolean showRoot, String panel) {
    	this.getJSplitPane().showDialog(showRoot, panel);
        this.setVisible(true);
    	return this.exitResult;
    }

    // ZAP: Added accessor to the panels
    /**
     * Gets the panels shown on this dialog.
     *
     * @return the panels
     */
    protected Collection<AbstractParamPanel> getPanels() {
    	return this.getJSplitPane().getPanels();
    }

    // Useful method for debugging panel issues ;)
    public void printTree() {
    	this.getJSplitPane().printTree();
    }

    public void renamePanel(AbstractParamPanel panel, String newPanelName) {
    	this.getJSplitPane().renamePanel(panel, newPanelName);
    }

}  //  @jve:decl-index=0:visual-constraint="73,11"
