/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP Development team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Please note that this file was originally released under the 
 * GNU General Public License  as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version
 * by Axel Neumann
 * 
 * As of October 2014 Axel Neumann granted the OWASP ZAP Project 
 * permission to redistribute this code under the Apache License, Version 2.0 
 */

package org.parosproxy.paros.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.DisplayUtils;


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
		setPreferredSize(DisplayUtils.getScaledDimension(getMaximumSize().width, 20));
		setMaximumSize(DisplayUtils.getScaledDimension(getMaximumSize().width, 20));
		
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
		JLabel label = new JLabel("0", DisplayUtils.getScaledIcon(new ImageIcon(imageUrl)), JLabel.LEADING);
		label.setToolTipText(toolTipText);
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		return label;
	}
	
	// Support for dynamic scanning results in the footer
    public void addFooterToolbarRightLabel (JLabel label) {
    	DisplayUtils.scaleIcon(label);
    	this.footerToolbarRight.add(label);
    	this.validate();
    }
    
    public void removeFooterToolbarRightLabel (JLabel label) {
        this.footerToolbarRight.remove(label);
        this.validate();
    }
    
    public void addFooterToolbarRightComponent (JComponent comp) {
    	if (comp instanceof JLabel) {
        	DisplayUtils.scaleIcon((JLabel)comp);
    	} else if (comp instanceof JButton) {
        	DisplayUtils.scaleIcon((JButton)comp);
    	}
    	this.footerToolbarRight.add(comp);
    	this.validate();
    }

    public void removeFooterToolbarRightComponent (JComponent comp) {
    	this.footerToolbarRight.remove(comp);
    	this.validate();
    }

    public void addFooterToolbarLeftComponent (JComponent comp) {
    	if (comp instanceof JLabel) {
        	DisplayUtils.scaleIcon((JLabel)comp);
    	} else if (comp instanceof JButton) {
        	DisplayUtils.scaleIcon((JButton)comp);
    	}
    	this.footerToolbarLeft.add(comp);
    	this.validate();
    }

    public void removeFooterToolbarLeftComponent (JComponent comp) {
    	this.footerToolbarLeft.remove(comp);
    	this.validate();
    }

    public void removeFooterToolbarLeftLabel (JLabel label) {
        this.footerToolbarLeft.remove(label);
        this.validate();
    }
    
    //FIXME Still needed?
    public void addFooterSeparator () {
        this.footerToolbarRight.addSeparator();
    }

}
