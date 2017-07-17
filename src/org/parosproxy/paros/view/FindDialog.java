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
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/03 Changed the method find to check if txtComp is null.
// ZAP: 2014/01/30 Issue 996: Ensure all dialogs close when the escape key is pressed (copy tidy up)
// ZAP: 2017/07/12 Issue 765: Add constructor with window parent, to facilitate ctrl-F in various HttpPanels
// ZAP: 2017/07/17: Prevent opening multiple dialogs per parent.

package org.parosproxy.paros.view;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.apache.commons.collections.map.ReferenceMap;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.ZapTextField;

public class FindDialog extends AbstractDialog {

	private static final long serialVersionUID = -3223449799557586758L;
	
	@SuppressWarnings("unchecked")
	private static Map<Object, FindDialog> parentsMap = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.HARD);

	private JPanel jPanel = null;
	private JButton btnFind = null;
	private JButton btnCancel = null;
	private ZapTextField txtFind = null;
	private JPanel jPanel1 = null;
    private JTextComponent lastInvoker = null;
    
    /**
     * @param lastInvoker The lastInvoker to set.
     */
    public void setLastInvoker(JTextComponent lastInvoker) {
        this.lastInvoker = lastInvoker;
    }

    /**
     * @throws HeadlessException
     * @deprecated TODO add version, use #FindDialog(Window, boolean) instead
     * @see #getDialog(Window, boolean)
     */
    @Deprecated
    public FindDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     * @deprecated TODO add version, use #FindDialog(Window, boolean) instead 
     * @see #getDialog(Window, boolean)
     */
    @Deprecated
    public FindDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }
    
    /**
     * Constructs a FindDialog. 
     * 
     * @param parent the parent window of the FindDialog.
     * @param modal whether or not this FindDialog should be modal
     * @throws HeadlessException
     * @see #getDialog(Window, boolean)
     * @since TODO add version
     */
    public FindDialog(Window parent, boolean modal) throws HeadlessException {
    	super(parent, modal);
    	initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setVisible(false);
        this.setResizable(false);
        this.setTitle(Constant.messages.getString("edit.find.title"));
        this.setContentPane(getJPanel());
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(261, 111);
	    }
        centreDialog();
        txtFind.requestFocus();
        this.getRootPane().setDefaultButton(btnFind);
        pack();
        this.setVisible(true);
	}
	
	private static Map<Object, FindDialog> getParentsMap() {
		return parentsMap;
	}
	
	/**
	 * Get the FindDialog for the parent if there is one or creates and returns a new one.
	 * 
	 * @param parent the parent Window (or Frame) for this FindDialog
	 * @param modal a boolean indicating whether the FindDialog should ({@code true}), 
	 * or shouldn't ({@code false}) be modal.
	 * @return The existing FindDialog for the parent (if there is one), or a new FindDialog.
	 * @since TODO add version
	 */
	public static FindDialog getDialog(Window parent, boolean modal) {
		FindDialog activeDialog = getParentsMap().get(parent);
		if (activeDialog != null) {
			activeDialog.getTxtFind().requestFocus();
			return activeDialog;
		}
		FindDialog newDialog = new FindDialog(parent, modal); 
		getParentsMap().put(parent, newDialog);
		newDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				getParentsMap().remove(parent);
			}
		});
		return newDialog;
	}

	private void discard() {
		this.setVisible(false);
		this.dispose();
	}
	
	private void find() {
	    JTextComponent txtComp = lastInvoker;
	    if (txtComp == null) {
	        JFrame parent = (JFrame) (this.getParent());
	        Component c = parent.getMostRecentFocusOwner();
	        if (c instanceof JTextComponent) {
	            txtComp = (JTextComponent) c;
            }
        }
        
        // ZAP: Check if a JTextComponent was really found.
        if (txtComp == null) {
            return;
        }
        
		try {
		    String findText = txtFind.getText().toLowerCase();
		    String txt = txtComp.getText().toLowerCase();
		    int startPos = txt.indexOf(findText, txtComp.getCaretPosition());

		    // Enable Wrap Search
		    if (startPos <= 0) {
		    	txtComp.setCaretPosition(0);
		    	startPos = txt.indexOf(findText, txtComp.getCaretPosition());
		    }
		    
		    int length = findText.length();
		    if (startPos > -1) {
		        txtComp.select(startPos,startPos+length);
		        txtComp.requestFocusInWindow();
                txtFind.requestFocusInWindow();
		    } else {
		        Toolkit.getDefaultToolkit().beep();
		    }
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

			java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

			javax.swing.JLabel jLabel = new JLabel();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jLabel.setText(Constant.messages.getString("edit.find.label.what"));
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new java.awt.Insets(2,10,2,10);
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(12,2,8,10);
			gridBagConstraints5.ipadx = 50;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints6.gridwidth = 3;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.insets = new java.awt.Insets(2,2,2,2);
			jPanel.add(jLabel, gridBagConstraints1);
			jPanel.add(getTxtFind(), gridBagConstraints5);
			jPanel.add(getJPanel1(), gridBagConstraints6);
		}
		return jPanel;
	}
	/**
	 * This method initializes btnFind	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnFind() {
		if (btnFind == null) {
			btnFind = new JButton();
			btnFind.setText(Constant.messages.getString("edit.find.button.find"));
			btnFind.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    

					find();
					
				}
			});

		}
		return btnFind;
	}
	/**
	 * This method initializes btnCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText(Constant.messages.getString("edit.find.button.cancel"));
			btnCancel.addActionListener(new java.awt.event.ActionListener() { 

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {

					FindDialog.this.discard();
					FindDialog.this.dispatchEvent(new WindowEvent(FindDialog.this, WindowEvent.WINDOW_CLOSING));
				}
			});

		}
		return btnCancel;
	}
	/**
	 * This method initializes txtFind	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextField	
	 */    
	private ZapTextField getTxtFind() {
		if (txtFind == null) {
			txtFind = new ZapTextField();
			txtFind.setMinimumSize(new java.awt.Dimension(120,24));
			txtFind.setPreferredSize(new java.awt.Dimension(120,24));
		}
		return txtFind;
	}
	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setMinimumSize(new java.awt.Dimension(155,35));
			jPanel1.add(getBtnFind(), null);
			jPanel1.add(getBtnCancel(), null);
		}
		return jPanel1;
	}
	

      }  //  @jve:decl-index=0:visual-constraint="10,10"
