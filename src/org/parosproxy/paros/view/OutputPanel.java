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
package org.parosproxy.paros.view;


import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.InputEvent;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.ZapTextArea;

public class OutputPanel extends AbstractPanel {

	private static final long serialVersionUID = -947074835463140074L;

	private JScrollPane jScrollPane = null;
	private ZapTextArea txtOutput = null;

	/**
     * 
     */
    public OutputPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("output.panel.title"));	// ZAP: i18n
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(243, 119);
	    }
        // ZAP: Added Output (doc) icon
		this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/172.png")));	// 'doc' icon

        this.add(getJScrollPane(), getJScrollPane().getName());
			
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtOutput());
			jScrollPane.setName("jScrollPane");
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
		}
		return jScrollPane;
	}
	/**
	 * This method initializes txtOutput	
	 * 	
	 * @return javax.swing.ZapTextArea	
	 */    
	private ZapTextArea getTxtOutput() {
		if (txtOutput == null) {
			txtOutput = new ZapTextArea();
			txtOutput.setEditable(false);
			txtOutput.setLineWrap(true);
			txtOutput.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			txtOutput.setName("");
			txtOutput.addMouseListener(new java.awt.event.MouseAdapter() { 

				public void mousePressed(java.awt.event.MouseEvent e) {
					mouseAction(e);
				}
					
				public void mouseReleased(java.awt.event.MouseEvent e) {
					mouseAction(e);
				}
				
				public void mouseAction(java.awt.event.MouseEvent e) {
					// right mouse button action
					if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0 || e.isPopupTrigger()) {
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				
			});
		}
		return txtOutput;
	}

	public void appendDirty(final String msg) {
		getTxtOutput().append(msg);
	}

	public void append(final String msg) {
		if (EventQueue.isDispatchThread()) {
			getTxtOutput().append(msg);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					getTxtOutput().append(msg);
				}
			});
		} catch (Exception e) {
		}
	}

	// ZAP: New method for printing out stack traces
	public void append(final Exception e) {
		// TODO: convert full stack trace to string
		this.append(e.toString());
	}

	public void clear() {
	    getTxtOutput().setText("");
	}
	
	
  }  //  @jve:decl-index=0:visual-constraint="10,10"
