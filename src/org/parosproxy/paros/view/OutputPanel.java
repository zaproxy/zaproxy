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
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/28 Added logger and log of exception.
// ZAP: 2013/11/16 Issue 886: Main pop up menu invoked twice on some components
// ZAP: 2013/11/16 Issue 890: Allow to clear "Output" tab
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts 
// ZAP: 2014/04/25 Issue 642: Add timestamps to Output tab(s)
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2015/02/10 Issue 1528: Support user defined font size

package org.parosproxy.paros.view;


import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.TimeStampUtils;
import org.zaproxy.zap.utils.ZapTextArea;

public class OutputPanel extends AbstractPanel {

	private static final long serialVersionUID = -947074835463140074L;
	// ZAP: Added logger.
	private static final Logger logger = Logger.getLogger(OutputPanel.class);

	private static final String CLEAR_BUTTON_LABEL = Constant.messages.getString("output.panel.clear.button.label");
	private static final String CLEAR_BUTTON_TOOL_TIP = Constant.messages.getString("output.panel.clear.button.toolTip");

	private JPanel mainPanel;
	private JToolBar mainToolBar;

	private JScrollPane jScrollPane = null;
	private ZapTextArea txtOutput = null;

    public OutputPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new BorderLayout());
        this.setName(Constant.messages.getString("output.panel.title"));	// ZAP: i18n
	    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
	    	this.setSize(243, 119);
	    }
        // ZAP: Added Output (doc) icon
		this.setIcon(new ImageIcon(OutputPanel.class.getResource("/resource/icon/16/172.png")));	// 'doc' icon
		this.setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK, false));
		this.setMnemonic(Constant.messages.getChar("output.panel.mnemonic"));

        this.add(getMainPanel(), BorderLayout.CENTER);
        this.setShowByDefault(true);
	}

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(getToolBar(), BorderLayout.PAGE_START);
            mainPanel.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return mainPanel;
    }

    private JToolBar getToolBar() {
        if (mainToolBar == null) {
            mainToolBar = new JToolBar();
            mainToolBar.setEnabled(true);
            mainToolBar.setFloatable(false);
            mainToolBar.setRollover(true);

            JButton clearButton = new JButton(CLEAR_BUTTON_LABEL);
            clearButton.setToolTipText(CLEAR_BUTTON_TOOL_TIP);
            clearButton.setIcon(new ImageIcon(OutputPanel.class.getResource("/resource/icon/fugue/broom.png")));
            clearButton.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getTxtOutput().setText("");
                };
            });

            mainToolBar.add(clearButton);
        }
        return mainToolBar;
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
		}
		return jScrollPane;
	}
	/**
	 * This method initializes txtOutput	
	 * 	
	 * @return org.zaproxy.zap.utils.ZapTextArea	
	 */    
	private ZapTextArea getTxtOutput() {
		if (txtOutput == null) {
			txtOutput = new ZapTextArea();
			txtOutput.setEditable(false);
			txtOutput.setLineWrap(true);
			txtOutput.setName("");
			txtOutput.addMouseListener(new java.awt.event.MouseAdapter() { 

				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
					
				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					showPopupMenuIfTriggered(e);
				}
				
				private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						View.getSingleton().getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
					}
				}
				
			});
		}
		return txtOutput;
	}
	/**
	 * @deprecated
	 * appendDirty has been deprecated in favour of using {@link #append(String)}
	 */
	@Deprecated
	public void appendDirty(final String msg) {
		doAppend(msg); //Mimic old behavior
	}

	public void append(final String msg) {
		if (EventQueue.isDispatchThread()) {
			doAppend(msg);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					doAppend(msg);
				}
			});
		} catch (Exception e) {
			// ZAP: Added logging.
			logger.error(e.getMessage(), e);
		}
	}

	// ZAP: New method for printing out stack traces
	public void append(final Exception e) {
		append(ExceptionUtils.getStackTrace(e));
	}

	public void clear() {
	    getTxtOutput().setText("");
	}
	
	private void doAppend(String message){
		if (Model.getSingleton().getOptionsParam().getViewParam().isOutputTabTimeStampingEnabled())
			getTxtOutput().append(TimeStampUtils.getTimeStampedMessage(message,Model.getSingleton().getOptionsParam().getViewParam().getOutputTabTimeStampsFormat()));
		else
			getTxtOutput().append(message);
	}
	
	/**
	 * Appends the given {@code message} to the panel, asynchronously in the EDT.
	 *
	 * @param message the message to append to the output panel
	 * @since 2.5.0
	 * @see EventQueue#invokeLater(Runnable)
	 */
	public void appendAsync(final String message) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				doAppend(message);
			}
		});
	}

  }  //  @jve:decl-index=0:visual-constraint="10,10"
