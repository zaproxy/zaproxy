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
// ZAP: 2012/03/15 Changed to use only one label for each alert.

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
		
		footerToolbarLeft.add(getAlertHigh(0));
		
		footerToolbarLeft.add(getAlertMedium(0));
		
		footerToolbarLeft.add(getAlertLow(0));
		
		footerToolbarLeft.add(getAlertInfo(0));
		
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
	
	private JLabel getAlertHigh(int alert) {
		if (alertHigh == null) {
			alertHigh = createAlertLabel(Constant.messages.getString("footer.alerts.high.tooltip"), Constant.HIGH_FLAG_IMAGE_URL); // Red flag
		}
		alertHigh.setText(String.valueOf(alert));
		return alertHigh;
	}
	
	public void setAlertHigh (int alert) {
		getAlertHigh(alert);
	}

	private JLabel getAlertMedium(int alert) {
		if (alertMedium == null) {
			alertMedium = createAlertLabel(Constant.messages.getString("footer.alerts.medium.tooltip"), Constant.MED_FLAG_IMAGE_URL); // Orange flag
		}
		alertMedium.setText(String.valueOf(alert));
		return alertMedium;
	}
	
	public void setAlertMedium (int alert) {
		getAlertMedium(alert);
	}

	private JLabel getAlertLow(int alert) {
		if (alertLow == null) {
			alertLow = createAlertLabel(Constant.messages.getString("footer.alerts.low.tooltip"), Constant.LOW_FLAG_IMAGE_URL); // Yellow flag
		}
		alertLow.setText(String.valueOf(alert));
		return alertLow;
	}
	
	public void setAlertLow (int alert) {
		getAlertLow(alert);
	}

	private JLabel getAlertInfo(int alert) {
		if (alertInfo == null) {
			alertInfo = createAlertLabel(Constant.messages.getString("footer.alerts.info.tooltip"), Constant.INFO_FLAG_IMAGE_URL); // Blue flag
		}
		alertInfo.setText(String.valueOf(alert));
		return alertInfo;
	}
	
	private JLabel createAlertLabel(String toolTip, URL icon) {
		JLabel label = new JLabel();
		label.setToolTipText(toolTip);
		label.setIcon(new ImageIcon(icon));
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		return label;
	}
	
	public void setAlertInfo (int alert) {
		getAlertInfo(alert);
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
