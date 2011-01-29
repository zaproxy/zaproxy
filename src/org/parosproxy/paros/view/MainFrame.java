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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.view.MainToolbarPanel;


public class MainFrame extends AbstractFrame {
	private static final long serialVersionUID = -1430550461546083192L;
	
	private JPanel paneContent = null;
	private JLabel txtStatus = null;
	private org.parosproxy.paros.view.WorkbenchPanel paneStandard = null;
	private org.parosproxy.paros.view.MainMenuBar mainMenuBar = null;
	private JPanel paneDisplay = null;
	
	private MainToolbarPanel mainToolbarPanel = null;
	private MainFooterPanel mainFooterPanel = null;

	private int displayOption;

	/**
	 * This method initializes
	 * 
	 */
	public MainFrame(int displayOption) {
		super();
		this.displayOption = displayOption;
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

		this.setSize(1000, 800);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				getMainMenuBar().getMenuFileControl().exit();
			}
		});

		this.setVisible(false);
	}

	/**
	 * This method initializes paneContent
	 * 
	 * @return JPanel
	 */
	private JPanel getPaneContent() {
		if (paneContent == null) {

			paneContent = new JPanel();
			paneContent.setLayout(new BoxLayout(getPaneContent(), BoxLayout.Y_AXIS));
			paneContent.setEnabled(true);
			paneContent.setPreferredSize(new Dimension(800, 600));
			
			if (Model.getSingleton().getOptionsParam().getViewParam().getShowMainToolbar() == 1) {
				paneContent.add(getMainToolbarPanel(), null);
			}
			
			paneContent.add(getPaneDisplay(), null);
			paneContent.add(getMainFooterPanel(), null);
			
		}
		return paneContent;
	}
	
	/**
	 * This method initializes paneStandard
	 * 
	 * @return com.proofsecure.paros.view.StandardPanel
	 */
	org.parosproxy.paros.view.WorkbenchPanel getWorkbench() {
		if (paneStandard == null) {
			paneStandard = new org.parosproxy.paros.view.WorkbenchPanel(displayOption);
			paneStandard.setLayout(new CardLayout());
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
	 * @return JPanel
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
	
	// ZAP: Added footer toolbar panel
	public MainFooterPanel getMainFooterPanel() {
		if (mainFooterPanel == null) {
			mainFooterPanel = new MainFooterPanel();
		}
		return mainFooterPanel;
	}
	
}
