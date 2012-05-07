/*
 * Copyright (C) 2010, OWASP ZAP Project
 * Author: Axel Neumann
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/copyleft/
 * 
 */

package org.parosproxy.paros.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;


public class MainFooterPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JToolBar footerToolbarLeft = null;
	private JToolBar footerToolbarRight = null;
	private JLabel alertHigh = null;
	private JLabel alertMedium = null;
	private JLabel alertLow = null;
	private JLabel alertInfo = null;
	
	public MainFooterPanel () {
		super();
		initialise();
	}
	
	public void initialise () {
		setLayout(new GridBagLayout());
		setPreferredSize(new Dimension(getMaximumSize().width, 20));
		setMaximumSize(new Dimension(getMaximumSize().width, 20));
		
		this.setBorder(BorderFactory.createEtchedBorder());

		
		/*
		 * ###################################################################
		 * ### LEFT GBC ### <-- DYNAMIC LENGTH DUMMY GBC --> ### RIGHT GBC ###
		 * ###################################################################
		 */
		
		GridBagConstraints gbcToolbarLeft = new GridBagConstraints();
		GridBagConstraints gbcToolbarRight = new GridBagConstraints();
		GridBagConstraints gbcDummyToFillSpace = new GridBagConstraints();

		gbcToolbarLeft.gridx = 0;
		gbcToolbarLeft.gridy = 0;
		gbcToolbarLeft.insets = new Insets(0, 5, 0, 0); 
		gbcToolbarLeft.anchor = GridBagConstraints.WEST;
		
		gbcDummyToFillSpace.gridx = 1;
		gbcDummyToFillSpace.gridy = 0;
		gbcDummyToFillSpace.weightx = 1.0;
		gbcDummyToFillSpace.weighty = 1.0;
		gbcDummyToFillSpace.anchor = GridBagConstraints.EAST;
		gbcDummyToFillSpace.fill = GridBagConstraints.HORIZONTAL;
		
		gbcToolbarRight.gridx = 2;
		gbcToolbarRight.gridy = 0;
		gbcToolbarRight.insets = new Insets(0, 5, 0, 0); 
		gbcToolbarRight.anchor = GridBagConstraints.EAST;
		
		JLabel dummyLabel = new JLabel();
		
		add(getToolbarLeft(), gbcToolbarLeft);
		add(dummyLabel, gbcDummyToFillSpace);
		add(getToolbarRight(), gbcToolbarRight);
		
		

		// Common alerts (Left)
		footerToolbarLeft.add(new JLabel(Constant.messages.getString("footer.alerts.label")));
		
		footerToolbarLeft.add(getAlertHigh());
		
		footerToolbarLeft.add(getAlertMedium());
		
		footerToolbarLeft.add(getAlertLow());
		
		footerToolbarLeft.add(getAlertInfo());
		
		// Current Scans (Right)
		footerToolbarRight.add(new JLabel(Constant.messages.getString("footer.scans.label")));
		
	}
	
	// Left toolbar for alerts
	private JToolBar getToolbarLeft() {
		if (footerToolbarLeft == null) {
			footerToolbarLeft = new JToolBar();
			footerToolbarLeft.setEnabled(true);
			footerToolbarLeft.setFloatable(false);
			footerToolbarLeft.setRollover(true);
			footerToolbarLeft.setName("Footer Toolbar Left");
			footerToolbarLeft.setBorder(BorderFactory.createEmptyBorder());
		}
		return footerToolbarLeft;
	}
	
	// Right toolbar for current scan results
	private JToolBar getToolbarRight() {
		if (footerToolbarRight == null) {
			footerToolbarRight = new JToolBar();
			footerToolbarRight.setEnabled(true);
			footerToolbarRight.setFloatable(false);
			footerToolbarRight.setRollover(true);
			footerToolbarRight.setName("Footer Toolbar Right");
			footerToolbarRight.setBorder(BorderFactory.createEmptyBorder());
		}
		return footerToolbarRight;
	}
	
	
	
	public void addAlertFlag (JButton button) {
		getToolbarLeft().add(button);
	}

	// FIXME Still needed?
	public void addSeparator() {
		getToolbarLeft().addSeparator();
	}
	
	private JLabel getAlertHigh() {
		if (alertHigh == null) {
			alertHigh = createAlertLabel(Constant.messages.getString("footer.alerts.high.tooltip"), Constant.HIGH_FLAG_IMAGE_URL); // Red flag
		}
		return alertHigh;
	}
	
	public void setAlertHigh (int alert) {
		getAlertHigh().setText(Integer.toString(alert));
	}

	private JLabel getAlertMedium() {
		if (alertMedium == null) {
			alertMedium = createAlertLabel(Constant.messages.getString("footer.alerts.medium.tooltip"), Constant.MED_FLAG_IMAGE_URL); // Orange flag
		}
		return alertMedium;
	}
	
	public void setAlertMedium (int alert) {
		getAlertMedium().setText(Integer.toString(alert));
	}

	private JLabel getAlertLow() {
		if (alertLow == null) {
			alertLow = createAlertLabel(Constant.messages.getString("footer.alerts.low.tooltip"), Constant.LOW_FLAG_IMAGE_URL); // Yellow flag
		}
		return alertLow;
	}
	
	public void setAlertLow (int alert) {
		getAlertLow().setText(Integer.toString(alert));
	}

	private JLabel getAlertInfo() {
		if (alertInfo == null) {
			alertInfo = createAlertLabel(Constant.messages.getString("footer.alerts.info.tooltip"), Constant.INFO_FLAG_IMAGE_URL); // Blue flag
		}
		return alertInfo;
	}
	
	public void setAlertInfo (int alert) {
		getAlertInfo().setText(Integer.toString(alert));
	}
	
	/**
	 * Creates a {@code JLabel} with text "0", an {@code ImageIcon} created from
	 * the specified URL, the specified tool tip text and an empty border that
	 * takes up 5 pixels to the right and left of the {@code JLabel}.
	 * 
	 * @param toolTipText
	 *            the tool tip text, if toolTipText is {@code null} no tool tip
	 *            is displayed
	 * @param imageUrl
	 *            the URL to the image
	 * 
	 * @throws NullPointerException
	 *             if imageUrl is {@code null}.
	 * 
	 * @return the {@code JLabel} object
	 * 
	 * @see JLabel#setToolTipText(String)
	 */
	private JLabel createAlertLabel(String toolTipText, URL imageUrl) throws NullPointerException {
		JLabel label = new JLabel("0", new ImageIcon(imageUrl), JLabel.LEADING);
		label.setToolTipText(toolTipText);
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		return label;
	}
	
	// Support for dynamic scanning results in the footer
    public void addFooterToolbarRightLabel (JLabel label) {
    	this.footerToolbarRight.add(label);
    }
    
    //FIXME Still needed?
    public void addFooterSeparator () {
        this.footerToolbarRight.addSeparator();
    }

}
