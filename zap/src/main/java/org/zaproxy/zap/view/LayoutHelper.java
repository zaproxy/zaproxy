/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 ZAP development team
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

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class LayoutHelper {
	
	private static final int DEFAULT_FILL = GridBagConstraints.BOTH;
	private static final int DEFAULT_ANCHOR = GridBagConstraints.NORTHWEST;

	public static GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return getGBC(x, y, width, weightx, 0.0);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx, Insets insets) {
		return getGBC(x, y, width, 1, weightx, 0.0, DEFAULT_FILL, DEFAULT_ANCHOR, insets);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, int height, double weightx, Insets insets) {
		return getGBC(x, y, width, height, weightx, 0.0, DEFAULT_FILL, DEFAULT_ANCHOR, insets);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty) {
		return getGBC(x, y, width, weightx, weighty, DEFAULT_FILL, null);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, int height, double weightx, double weighty) {
		return getGBC(x, y, width, height, weightx, weighty, DEFAULT_FILL, DEFAULT_ANCHOR, null);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty, int fill) {
		return getGBC(x, y, width, weightx, weighty, fill, null);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty, int fill, 
			Insets insets) {
		return getGBC(x, y, width, 1, weightx, weighty, fill, DEFAULT_ANCHOR, insets);
	}
	
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx, double weighty, int fill, int anchor, 
			Insets insets) {
		return getGBC(x, y, width, 1, weightx, weighty, fill, anchor, insets);
	}

	public static GridBagConstraints getGBC(int x, int y, int width, int height, double weightx, double weighty, int fill, int anchor, 
			Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		if (insets != null) {
			gbc.insets = insets;
		}
		gbc.anchor = anchor;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		return gbc;
	}

}
