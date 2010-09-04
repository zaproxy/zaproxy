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
package org.parosproxy.paros.extension.scanner;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.parosproxy.paros.core.scanner.HostProcess;

import javax.swing.JScrollPane;
/**
*
* To change the template for this generated type comment go to
* Window - Preferences - Java - Code Generation - Code and Comments
*/
public class HostProgressMeter extends JPanel {

	private JLabel txtHost = null;
	private JProgressBar barProgress = null;
	private JButton btnStop = null;
	private JLabel txtDisplay = null;
	private HostProcess hostProcess = null;
	
	private JScrollPane jScrollPane = null;
	/**
	 * This is the default constructor
	 */
	public HostProgressMeter() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

		java.awt.GridBagConstraints gridBagConstraints4 = new GridBagConstraints();

		javax.swing.JLabel jLabel = new JLabel();

		java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

		java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		this.setSize(380, 76);
		this.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.SoftBevelBorder.RAISED));
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.insets = new java.awt.Insets(2,2,2,5);
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.insets = new java.awt.Insets(2,5,2,2);
		gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints2.gridwidth = 2;
		jLabel.setText("Host:");
		gridBagConstraints4.gridx = 0;
		gridBagConstraints4.gridy = 0;
		gridBagConstraints4.insets = new java.awt.Insets(2,5,2,5);
		gridBagConstraints4.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHEAST;
		gridBagConstraints5.gridx = 2;
		gridBagConstraints5.gridy = 1;
		gridBagConstraints5.insets = new java.awt.Insets(2,2,2,5);
		gridBagConstraints12.weightx = 1.0;
		gridBagConstraints12.weighty = 0.0D;
		gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints12.gridwidth = 2;
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.gridy = 2;
		gridBagConstraints12.insets = new java.awt.Insets(2,5,2,5);
		this.add(jLabel, gridBagConstraints4);
		this.add(getTxtHost(), gridBagConstraints1);
		this.add(getBtnStop(), gridBagConstraints5);
		this.add(getBarProgress(), gridBagConstraints2);
		this.add(getJScrollPane(), gridBagConstraints12);
	}
	/**
	 * This method initializes txtHost	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	JLabel getTxtHost() {
		if (txtHost == null) {
			txtHost = new JLabel("    ");
		}
		return txtHost;
	}
	/**
	 * This method initializes barProgress	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */    
	private JProgressBar getBarProgress() {
		if (barProgress == null) {
			barProgress = new JProgressBar();
			barProgress.setPreferredSize(new java.awt.Dimension(150,20));
		}
		return barProgress;
	}
	/**
	 * This method initializes btnStop	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnStop() {
		if (btnStop == null) {
			btnStop = new JButton();
			btnStop.setText("Stop");
			btnStop.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    if (hostProcess != null) {
				        hostProcess.stop();
				    }
				    btnStop.setEnabled(false);

				}
			});

		}
		return btnStop;
	}
	
	void setProgress(String msg, int percentage) {
	    getBarProgress().setValue(percentage);
	    getTxtDisplay().setText(msg);
	   
	}
	
	void setHostProcess(HostProcess hostThread) {
	    this.hostProcess = hostThread;
	}
	
	/**
	 * This method initializes txtDisplay	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JLabel getTxtDisplay() {
		if (txtDisplay == null) {
			txtDisplay = new JLabel("    ");
		}
		return txtDisplay;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTxtDisplay());
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,0));
		}
		return jScrollPane;
	}
     }  //  @jve:decl-index=0:visual-constraint="10,10"
