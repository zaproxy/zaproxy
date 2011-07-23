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
// ZAP: 2011/05/31 Added option to dynamically change the display
// ZAP: 2011/07/23 Save current position in config file
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.view.Layout;
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
		
		this.loadPosition();
    	
    	this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				getMainMenuBar().getMenuFileControl().exit();
			}
		});

		this.setVisible(false);
    	this.pack();
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
				@Override
				public void run() {
					txtStatus.setText(msg);
				}
			});
		} catch (final Exception e) {
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

	public void changeDisplayOption(int displayOption) {
		if (this.displayOption != displayOption) {
			this.displayOption = displayOption;
			this.getWorkbench().changeDisplayOption(displayOption);
			Model.getSingleton().getOptionsParam().getViewParam().setDisplayOption(displayOption);
		}
	}

	private void loadPosition() {
		FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();

		int x = config.getInt(Layout.OPTIONS_MAINFRAME_X, Layout.DEFAULT_MAINFRAME_X); 
		int y = config.getInt(Layout.OPTIONS_MAINFRAME_Y, Layout.DEFAULT_MAINFRAME_Y);
		int h = config.getInt(Layout.OPTIONS_MAINFRAME_H, Layout.DEFAULT_MAINFRAME_H); 
		int w = config.getInt(Layout.OPTIONS_MAINFRAME_W, Layout.DEFAULT_MAINFRAME_W); 

		this.setLocation(new Point(x,y));
		this.setPreferredSize(new Dimension(w, h));
		
	}

	public void savePosition(FileConfiguration config) {
		Rectangle rec = this.getBounds();
		config.setProperty(Layout.OPTIONS_MAINFRAME_X, rec.x);
		config.setProperty(Layout.OPTIONS_MAINFRAME_Y, rec.y);
		config.setProperty(Layout.OPTIONS_MAINFRAME_H, rec.height);
		config.setProperty(Layout.OPTIONS_MAINFRAME_W, rec.width);
		
		this.getWorkbench().savePosition(config);
		
	}

}
