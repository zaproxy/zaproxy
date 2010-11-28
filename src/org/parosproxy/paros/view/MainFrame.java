/*
 * Created on May 18, 2004
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

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.MainToolbarPanel;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MainFrame extends AbstractFrame {
	private javax.swing.JPanel jContentPane = null;
	private javax.swing.JPanel paneContent = null;
	private javax.swing.JLabel txtStatus = null;
	private org.parosproxy.paros.view.WorkbenchPanel paneStandard = null;
	private org.parosproxy.paros.view.MainMenuBar mainMenuBar = null;
	private JPanel paneDisplay = null;

	private MainToolbarPanel mainToolbarPanel = null;
	// ZAP Added footer alert icons
	private JToolBar footerToolbarPanel = null;
	private JLabel alertHigh = null;
	private JLabel alertMedium = null;
	private JLabel alertLow = null;
	private JLabel alertInfo = null;

	/**
	 * This method initializes 
	 * 
	 */
	public MainFrame() {
		super();
		initialize();
	}
	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setJMenuBar(getMainMenuBar());
        this.setContentPane(getPaneContent());

        // ZAP change window default size
        //this.setSize(800, 600);
        this.setSize(1000, 800);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() { 

        	public void windowClosing(java.awt.event.WindowEvent e) {    

        		getMainMenuBar().getMenuFileControl().exit();
        	}
        });

        this.setVisible(false);
	}
	
	/**

	 * This method initializes paneContent	

	 * 	

	 * @return javax.swing.JPanel	

	 */    
	private javax.swing.JPanel getPaneContent() {
		if (paneContent == null) {
			
			paneContent = new javax.swing.JPanel();
			paneContent.setLayout(new BoxLayout(getPaneContent(), BoxLayout.Y_AXIS));
			paneContent.setEnabled(true);
			paneContent.setPreferredSize(new java.awt.Dimension(800,600));
			paneContent.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			
			// ZAP: Add MainToolbar
			paneContent.add(getMainToolbarPanel(), null);
			
			paneContent.add(getPaneDisplay(), null);
			// ZAP: Remove the status line - its not really used and takes up space
			//paneContent.add(getTxtStatus(), null);
			paneContent.add(getFooterToolbarPanel(), null);
		}
		return paneContent;
	}

	private JLabel getAlertHigh(int alert) {
		if (alertHigh == null) {
			alertHigh = new JLabel();
			alertHigh.setToolTipText(Constant.messages.getString("footer.alerts.high.tooltip"));
		}
		alertHigh.setText("" + alert);
		return alertHigh;
	}
	
	public void setAlertHigh (int alert) {
		getAlertHigh(alert);
	}

	private JLabel getAlertMedium(int alert) {
		if (alertMedium == null) {
			alertMedium = new JLabel();
			alertMedium.setToolTipText(Constant.messages.getString("footer.alerts.medium.tooltip"));
		}
		alertMedium.setText("" + alert);
		return alertMedium;
	}
	
	public void setAlertMedium (int alert) {
		getAlertMedium(alert);
	}

	private JLabel getAlertLow(int alert) {
		if (alertLow == null) {
			alertLow = new JLabel();
			alertLow.setToolTipText(Constant.messages.getString("footer.alerts.low.tooltip"));
		}
		alertLow.setText("" + alert);
		return alertLow;
	}
	
	public void setAlertLow (int alert) {
		getAlertLow(alert);
	}

	private JLabel getAlertInfo(int alert) {
		if (alertInfo == null) {
			alertInfo = new JLabel();
			alertInfo.setToolTipText(Constant.messages.getString("footer.alerts.info.tooltip"));
		}
		alertInfo.setText("" + alert);
		return alertInfo;
	}
	
	public void setAlertInfo (int alert) {
		getAlertInfo(alert);
	}

	private JToolBar getFooterToolbarPanel() {
		if (this.footerToolbarPanel == null) {
			this.footerToolbarPanel = new JToolBar();
			this.footerToolbarPanel.setFloatable(false);
			this.footerToolbarPanel.setEnabled(true);
			
			this.footerToolbarPanel.add(new JLabel(
					Constant.messages.getString("footer.alerts.label")));
			
			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));
			
			// logPanel.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/025.png")));	// 'calendar' icon
			JLabel flagHigh = new JLabel("<html>:&nbsp;</html>");
			flagHigh.setToolTipText(Constant.messages.getString("footer.alerts.high.tooltip"));
			ImageIcon iconHigh = new ImageIcon(getClass().getResource("/resource/icon/10/071.png"));
			flagHigh.setIcon(iconHigh);
			this.footerToolbarPanel.add(flagHigh);
			this.footerToolbarPanel.add(this.getAlertHigh(0));

			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));

			JLabel flagMedium = new JLabel("<html>:&nbsp;</html>");
			flagMedium.setToolTipText(Constant.messages.getString("footer.alerts.medium.tooltip"));
			ImageIcon iconMedium = new ImageIcon(getClass().getResource("/resource/icon/10/076.png"));
			flagMedium.setIcon(iconMedium);
			this.footerToolbarPanel.add(flagMedium);
			this.footerToolbarPanel.add(this.getAlertMedium(0));

			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));

			JLabel flagLow = new JLabel("<html>:&nbsp;</html>");
			flagLow.setToolTipText(Constant.messages.getString("footer.alerts.low.tooltip"));
			ImageIcon iconLow = new ImageIcon(getClass().getResource("/resource/icon/10/074.png"));
			flagLow.setIcon(iconLow);
			this.footerToolbarPanel.add(flagLow);
			this.footerToolbarPanel.add(this.getAlertLow(0));

			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));

			JLabel flagInfo = new JLabel("<html>:&nbsp;</html>");
			flagInfo.setToolTipText(Constant.messages.getString("footer.alerts.info.tooltip"));
			ImageIcon iconInfo = new ImageIcon(getClass().getResource("/resource/icon/10/073.png"));
			flagInfo.setIcon(iconInfo);
			this.footerToolbarPanel.add(flagInfo);
			this.footerToolbarPanel.add(this.getAlertInfo(0));

			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));

			// Current scans
			this.footerToolbarPanel.addSeparator();
			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));
			this.footerToolbarPanel.add(new JLabel(Constant.messages.getString("footer.scans.label")));
			
			this.footerToolbarPanel.add(new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>"));
		}
		return this.footerToolbarPanel;
	}
	
	public void addFooterLabel (JLabel label) {
		this.footerToolbarPanel.add(label);
	}

	public void addFooterSeparator () {
		this.footerToolbarPanel.addSeparator();
	}


	/**

	 * This method initializes txtStatus	

	 * 	

	 * @return javax.swing.JLabel	

	 */    
	private javax.swing.JLabel getTxtStatus() {
		if (txtStatus == null) {
			txtStatus = new javax.swing.JLabel();
			txtStatus.setName("txtStatus");
			txtStatus.setText("Initializing...");
			txtStatus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			txtStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
			txtStatus.setPreferredSize(new java.awt.Dimension(800,18));
		}
		return txtStatus;
	}

	/**

	 * This method initializes paneStandard	

	 * 	

	 * @return com.proofsecure.paros.view.StandardPanel	

	 */    
	org.parosproxy.paros.view.WorkbenchPanel getWorkbench() {
		if (paneStandard == null) {
			paneStandard = new org.parosproxy.paros.view.WorkbenchPanel();
			paneStandard.setLayout(new java.awt.CardLayout());
			paneStandard.setName("paneStandard");
		}
		return paneStandard;
	}

	/**

	 * This method initializes mainMenuBar	

	 * 	

	 * @return com.proofsecure.paros.view.MenuDisplay	

	 */    
	public org.parosproxy.paros.view.MainMenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new org.parosproxy.paros.view.MainMenuBar();
		}
		return mainMenuBar;
	}
	
	public void setStatus(final String msg) {
		if (EventQueue.isDispatchThread()) {
			txtStatus.setText(msg);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					txtStatus.setText(msg);
				}
			});
		} catch (Exception e) {
		}
	}

	/**
	 * This method initializes paneDisplay	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	public JPanel getPaneDisplay() {
		if (paneDisplay == null) {
			paneDisplay = new JPanel();
			paneDisplay.setLayout(new CardLayout());
			paneDisplay.setName("paneDisplay");
			paneDisplay.add(getWorkbench(), getWorkbench().getName());
		}
		return paneDisplay;
	}
	
	// ZAP: Added main toolbar panel
	public MainToolbarPanel getMainToolbarPanel() {
		if (mainToolbarPanel == null) {
			mainToolbarPanel = new MainToolbarPanel();
		}
		return mainToolbarPanel;
	}
}
