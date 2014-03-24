/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
 */
package org.zaproxy.zap.view;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ScanStatus {
	private ImageIcon icon;
	private String name;
	private int scanCount = 0;
	private JLabel countLabel = new JLabel();
	
	public ScanStatus(ImageIcon icon, String name) {
		super();
		this.icon = icon;
		this.name = name;
		countLabel.setIcon(icon);
		countLabel.setToolTipText(name);
		countLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		this.setScanCount(0);
	}
	
	public ImageIcon getIcon() {
		return icon;
	}
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getScanCount() {
		return scanCount;
	}
	public void setScanCount(int scanCount) {
		this.scanCount = scanCount;
		this.countLabel.setText(Integer.toString(scanCount));
	}
	public void incScanCount() {
		this.scanCount++;
		this.countLabel.setText(Integer.toString(scanCount));
	}
	public void decScanCount() {
		this.scanCount--;
		this.countLabel.setText(Integer.toString(scanCount));
	}
	public JLabel getCountLabel() {
		return this.countLabel;
	}
}
