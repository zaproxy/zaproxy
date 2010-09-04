/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
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
package org.parosproxy.paros.extension.patternsearch;

import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.ViewDelegate;
/**
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SearchDialog extends AbstractDialog {

    private ExtensionPatternSearch ext = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JScrollPane jScrollPane = null;

    private JScrollPane jScrollPane1 = null;

    private JTextArea txtPattern = null;

    private JTextArea txtResult = null;

    private JButton btnSearch = null;

    private JPanel jPanel3 = null;

    private JPanel jPanel4 = null;

    private ViewDelegate view = null;

	private JPanel jPanel5 = null;
	private Choice choice = null;
	private JRadioButton jRadioButton = null;
	private JRadioButton jRadioButton1 = null;
	private JPanel jPanel6 = null;
    /**
     * This method initializes
     *  
     */
    public SearchDialog() {
        super();
        initialize();
    }

    public SearchDialog(Frame owner, boolean modal) {
        super(owner, modal);
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setTitle("Extract Pattern in Session");
        this.setContentPane(getJPanel());
        this.setSize(561, 515);

    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints21 = new GridBagConstraints();

            gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.insets = new java.awt.Insets(2,2,2,2);
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.gridy = 0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.weighty = 0.1D;
            gridBagConstraints1.gridheight = 1;
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 2;
            gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.weightx = 1.0D;
            gridBagConstraints2.weighty = 0.9D;
            gridBagConstraints2.gridheight = 1;
            jPanel.add(getJPanel1(), gridBagConstraints1);
            jPanel.add(getJPanel2(), gridBagConstraints2);
            jPanel.add(getJPanel6(), gridBagConstraints5);

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
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            TitledBorder titledBorder1 = BorderFactory.createTitledBorder(null,
                    "Enter plain text below for encoding/hashing",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, null, null);
            java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

            jPanel1 = new JPanel();
            jPanel1.setLayout(new GridBagLayout());
            jPanel1.setPreferredSize(new java.awt.Dimension(135, 100));
            jPanel1.setBorder(titledBorder1);
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.weighty = 1.0;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints3.gridheight = 1;
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints7.gridx = 1;
            gridBagConstraints7.gridy = 1;
            gridBagConstraints7.insets = new java.awt.Insets(2, 2, 2, 2);
            titledBorder1.setTitle("Search Pattern");
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.gridy = 0;
            gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.gridx = -1;
            gridBagConstraints4.gridy = -1;
            gridBagConstraints4.weightx = 0.5D;
            gridBagConstraints4.weighty = 1.0D;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.insets = new java.awt.Insets(2,2,2,2);
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
            TitledBorder titledBorder2 = javax.swing.BorderFactory
                    .createTitledBorder(
                            null,
                            "Pattern Search Result",
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            new java.awt.Font("MS Sans Serif",
                                    java.awt.Font.PLAIN, 11),
                            java.awt.Color.black);
            java.awt.GridBagConstraints gridBagConstraints9 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints8 = new GridBagConstraints();

            jPanel2 = new JPanel();
            jPanel2.setLayout(new GridBagLayout());
            jPanel2.setPreferredSize(new java.awt.Dimension(135, 120));
            jPanel2.setBorder(titledBorder2);
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.gridy = 0;
            gridBagConstraints8.weightx = 1.0;
            gridBagConstraints8.weighty = 1.0;
            gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints8.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints8.gridheight = 1;
            gridBagConstraints9.gridx = 1;
            gridBagConstraints9.gridy = 0;
            gridBagConstraints9.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTHWEST;
            titledBorder2.setTitle("Search Result");
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
            jScrollPane.setViewportView(getTxtPattern());
            jScrollPane
                    .setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
            jScrollPane1.setViewportView(getTxtResult());
        }
        return jScrollPane1;
    }

    /**
     * This method initializes txtEncode
     * 
     * @return javax.swing.JTextField
     */
    private JTextArea getTxtPattern() {
        if (txtPattern == null) {
            txtPattern = new JTextArea();
            txtPattern.setLineWrap(true);
            txtPattern.setFont(new java.awt.Font("Courier New",
                    java.awt.Font.PLAIN, 12));
            txtPattern.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) { // right
                                                                             // mouse
                                                                             // button
                        view.getPopupMenu().show(e.getComponent(), e.getX(),
                                e.getY());
                    }
                }

            });

        }
        return txtPattern;
    }

    /**
     * This method initializes txtDecode
     * 
     * @return javax.swing.JTextField
     */
    private JTextArea getTxtResult() {
        if (txtResult == null) {
            txtResult = new JTextArea();
            txtResult.setLineWrap(true);
            txtResult.setFont(new java.awt.Font("Courier New",
                    java.awt.Font.PLAIN, 12));
        }
        return txtResult;
    }

    /**
     * This method initializes btnURLEncode
     * 
     * @return javax.swing.JButton
     */
    private JButton getBtnSearch() {
        if (btnSearch == null) {
            btnSearch = new JButton();
            btnSearch.setText("Search");
            btnSearch.setActionCommand("Search");
            btnSearch.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {

                    txtResult.setText("");
                    if (txtPattern.getText()!=null && !txtPattern.getText().equals(""))
                        txtResult.setText(ext.search(txtPattern.getText(),getJRadioButton().isSelected()));

                }
            });

        }
        return btnSearch;
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
            gridLayout6.setRows(3);
            gridLayout6.setColumns(1);
            gridLayout6.setVgap(3);
            gridLayout6.setHgap(3);
            jPanel3.add(getJRadioButton(), null);
            jPanel3.add(getJRadioButton1(), null);
            jPanel3.add(getBtnSearch(), null);
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
        }
        return jPanel4;
    }

    void setView(ViewDelegate view) {
        this.view = view;
    }

    /**
     * @return Returns the ext.
     */
    public ExtensionPatternSearch getExt() {
        return ext;
    }

    /**
     * @param ext
     *            The ext to set.
     */
    public void setExt(ExtensionPatternSearch ext) {
        this.ext = ext;
    }
    
    
	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jPanel5 = new JPanel();
			jPanel5.setLayout(new GridBagLayout());
			jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sample Patterns", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
		}
		return jPanel5;
	}
	/**
	 * This method initializes choice	
	 * 	
	 * @return java.awt.Choice	
	 */    
	private Choice getChoice() {
		if (choice == null) {
			choice = new Choice();
			choice.addItem("-- blank --");  // 1st one is blank, don't change
			/*
			 *  Note: the item string must end with REQUEST or RESPONSE. It's used to set radio buttons 
			 */
			choice.addItem("Cookie:.*?\\r\\n..........get all cookies from REQUEST");
			choice.addItem("Server:.*?\\r\\n..........get all server banners from RESPONSE");
			choice.addItem("http.*?[ ]..........get all requested URLs from REQUEST");
			choice.addItem("http.*?[.]php..........get all requested php URLs from REQUEST");
			choice.addItem("POST[ ]http.*?[ ]..........get all POST requests from REQUEST");

			choice.addItemListener(new java.awt.event.ItemListener() { 
				public void itemStateChanged(java.awt.event.ItemEvent e) {
				    if (getChoice().getSelectedIndex()==0){ // 1st one is blank, i.e. clear pattern
				        getTxtPattern().setText("");
				    }
				    else{
				        String txt = getChoice().getSelectedItem();
				        getTxtPattern().setText(txt.substring(0,txt.indexOf("..........")));
				        if (txt.endsWith("REQUEST")){
				            if (!getJRadioButton().isSelected())
				                getJRadioButton().doClick();
				        }
				        else{
				            if (!getJRadioButton1().isSelected())
				                getJRadioButton1().doClick();
				        }
				        
				         
				    }
				}
			});
		}
		return choice;
	}
	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getJRadioButton() {
		if (jRadioButton == null) {
			jRadioButton = new JRadioButton();
			jRadioButton.setText("Request");
			jRadioButton.setSelected(true);
			jRadioButton.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getJRadioButton().setSelected(true);
					getJRadioButton1().setSelected(false);
				}
			});
/*			jRadioButton.addChangeListener(new javax.swing.event.ChangeListener() { 
				public void stateChanged(javax.swing.event.ChangeEvent e) {    
				    getJRadioButton1().setSelected(false);				    
				}
			});*/
		}
		return jRadioButton;
	}
	/**
	 * This method initializes jRadioButton1	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */    
	private JRadioButton getJRadioButton1() {
		if (jRadioButton1 == null) {
			jRadioButton1 = new JRadioButton();
			jRadioButton1.setText("Response");
			jRadioButton1.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					getJRadioButton().setSelected(false);
					getJRadioButton1().setSelected(true);
				}
			});
			/*
			jRadioButton1.addChangeListener(new javax.swing.event.ChangeListener() { 
				public void stateChanged(javax.swing.event.ChangeEvent e) {    
				    getJRadioButton().setSelected(false);				    
				}
			});*/
		}
		return jRadioButton1;
	}
	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			GridLayout gridLayout7 = new GridLayout();
			jPanel6 = new JPanel();
			jPanel6.setLayout(gridLayout7);
			jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sample Patterns", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			gridLayout7.setRows(1);
			jPanel6.add(getChoice(), null);
		}
		return jPanel6;
	}
   } //  @jve:decl-index=0:visual-constraint="10,10"
