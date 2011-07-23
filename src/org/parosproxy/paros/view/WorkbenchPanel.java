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
// ZAP: 2011/05/31 Added option to dynamically change the display
// ZAP: 2011/07/23 Save current position in config file
package org.parosproxy.paros.view;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.view.Layout;

/**
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class WorkbenchPanel extends JPanel {

	private static final long serialVersionUID = -4610792807151921550L;

	private JSplitPane splitVert = null;
	private JSplitPane splitHoriz = null;

	private JPanel paneStatus = null;
	private JPanel paneSelect = null;
	private JPanel paneWork = null;

	private org.parosproxy.paros.view.TabbedPanel tabbedStatus = null;
	private org.parosproxy.paros.view.TabbedPanel tabbedWork = null;
	private org.parosproxy.paros.view.TabbedPanel tabbedSelect = null;
	
	private int displayOption;

	/**
	 * This is the default constructor
	 */
	public WorkbenchPanel(int displayOption) {
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
		GridBagConstraints consGridBagConstraints1 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		consGridBagConstraints1.gridx = 0;
		consGridBagConstraints1.gridy = 0;
		consGridBagConstraints1.weightx = 1.0;
		consGridBagConstraints1.weighty = 1.0;
		consGridBagConstraints1.fill = GridBagConstraints.BOTH;
		switch (displayOption) {
		case View.DISPLAY_OPTION_LEFT_FULL:
			this.add(getSplitHoriz(), consGridBagConstraints1);
			break;
		case View.DISPLAY_OPTION_BOTTOM_FULL:
		default:
			this.add(getSplitVert(), consGridBagConstraints1);
			break;
		}
	}

	public void changeDisplayOption(int displayOption) {
		this.displayOption = displayOption;
		this.removeAll();
		splitVert = null;
		splitHoriz = null;
		initialize();
		this.validate();
		this.repaint();
	}


	/**
	 * This method initializes splitVert
	 * (TOP/BOTTOM (History))
	 * 
	 * @return JSplitPane
	 */
	private JSplitPane getSplitVert() {
		if (splitVert == null) {
			splitVert = new JSplitPane();
			splitVert.setDividerSize(3);
			splitVert.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitVert.setResizeWeight(0.5D);
			FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();
			splitVert.setDividerLocation(config.getInt(Layout.OPTIONS_WORKBENCH_VERTICAL_DIVIDER, 
					Layout.DEFAULT_WORKBENCH_VERTICAL_DIVIDER));

			switch (displayOption) {
			case View.DISPLAY_OPTION_LEFT_FULL:
				splitVert.setTopComponent(getPaneWork());
				break;
			case View.DISPLAY_OPTION_BOTTOM_FULL:
			default:
				splitVert.setTopComponent(getSplitHoriz());
				break;
			}
			splitVert.setBottomComponent(getPaneStatus());
			splitVert.setContinuousLayout(false);
			splitVert.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return splitVert;
	}

	/**
	 * This method initializes splitHoriz
	 * 
	 * Site Panel / Work
	 * 
	 * @return JSplitPane
	 */
	private JSplitPane getSplitHoriz() {
		if (splitHoriz == null) {
			splitHoriz = new JSplitPane();
			splitHoriz.setLeftComponent(getPaneSelect());
			switch (displayOption) {
			case View.DISPLAY_OPTION_LEFT_FULL:
				splitHoriz.setRightComponent(getSplitVert());
				break;
			case View.DISPLAY_OPTION_BOTTOM_FULL:
			default:
				splitHoriz.setRightComponent(getPaneWork());
				break;
			}
			splitHoriz.setDividerSize(3);
			splitHoriz.setResizeWeight(0.3D);
			splitHoriz.setContinuousLayout(false);
			splitHoriz.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();
			splitHoriz.setDividerLocation(config.getInt(Layout.OPTIONS_WORKBENCH_HORIZONTAL_DIVIDER, 
					Layout.DEFAULT_WORKBENCH_HORIZONTAL_DIVIDER));
		}
		return splitHoriz;
	}

	/**
	 * This method initializes paneStatus
	 * 
	 * @return JPanel
	 */
	private JPanel getPaneStatus() {
		if (paneStatus == null) {
			paneStatus = new JPanel();
			paneStatus.setLayout(new CardLayout());
			paneStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneStatus.add(getTabbedStatus(), getTabbedStatus().getName());
		}
		return paneStatus;
	}

	/**
	 * This method initializes paneSelect
	 * 
	 * @return JPanel
	 */
	private JPanel getPaneSelect() {
		if (paneSelect == null) {
			paneSelect = new JPanel();
			paneSelect.setLayout(new CardLayout());
			paneSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneSelect.add(getTabbedSelect(), getTabbedSelect().getName());
		}
		return paneSelect;
	}

	/**
	 * This method initializes paneWork
	 *  
	 * @return JPanel
	 */
	private JPanel getPaneWork() {
		if (paneWork == null) {
			paneWork = new JPanel();
			paneWork.setLayout(new CardLayout());
			paneWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			paneWork.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			paneWork.add(getTabbedWork(), getTabbedWork().getName());
		}
		return paneWork;
	}

	/**
	 * This method initializes tabbedStatus
	 * 
	 * @return com.proofsecure.paros.view.ParosTabbedPane
	 */
	public org.parosproxy.paros.view.TabbedPanel getTabbedStatus() {
		if (tabbedStatus == null) {
			tabbedStatus = new org.parosproxy.paros.view.TabbedPanel();
			tabbedStatus.setPreferredSize(new Dimension(800, 200));
			// ZAP: Move tabs to the top of the panel
			tabbedStatus.setTabPlacement(JTabbedPane.TOP);
			tabbedStatus.setName("tabbedStatus");
			tabbedStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedStatus;
	}

	/**
	 * This method initializes tabbedWork
	 * 
	 * @return com.proofsecure.paros.view.ParosTabbedPane
	 */
	public org.parosproxy.paros.view.TabbedPanel getTabbedWork() {
		if (tabbedWork == null) {
			tabbedWork = new org.parosproxy.paros.view.TabbedPanel();
			tabbedWork.setPreferredSize(new Dimension(600, 400));
			tabbedWork.setName("tabbedWork");
			tabbedWork.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}
		return tabbedWork;
	}

	/**
	 * This method initializes tabbedSelect
	 * 
	 * @return com.proofsecure.paros.view.ParosTabbedPane
	 */
	public org.parosproxy.paros.view.TabbedPanel getTabbedSelect() {
		if (tabbedSelect == null) {
			tabbedSelect = new org.parosproxy.paros.view.TabbedPanel();
			tabbedSelect.setPreferredSize(new Dimension(200, 400));
			tabbedSelect.setName("tabbedSelect");
			tabbedSelect.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		}

		return tabbedSelect;
	}

	public void savePosition(FileConfiguration config) {
		config.setProperty("display.layout.workbench.vert.d", getSplitVert().getDividerLocation());
		config.setProperty("display.layout.workbench.horiz.d", getSplitHoriz().getDividerLocation());
	}

}
