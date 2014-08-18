/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.multiFuzz;

import java.awt.Color;
import java.awt.GridBagConstraints;
/**
 * Fuzzing utility class containing methods shared between multiple classes.
 */
public class Util {
	/**
	 * Generates and returns different colors which are allocated to fuzzing gaps in order.
	 * @param n the index of the color to be generated (should not exceed 15)
	 * @return	The n-th color
	 */
	public static Color getColor(int n) {
		float hue = (float) (n % 5) / 5;
		float sat = (float) Math.ceil((float) n / 5) / 2;
		float bright = (float) Math.ceil((float) n / 5);
		return Color.getHSBColor(hue, sat, bright);
	}
	/**
	 * Returns {@link GridBagConstraints} with the specified parameters
	 * @param x	x value
	 * @param y y value
	 * @param width width value
	 * @param weightx weightx value
	 * @return The specified {@link GridBagConstraints}
	 */
	public static GridBagConstraints getGBC(int x, int y, int width, double weightx) {
		return getGBC(x, y, width, weightx, 0.0,
				java.awt.GridBagConstraints.NONE);
	}
	/**
	 * Returns {@link GridBagConstraints} with the specified parameters
	 * @param x	x value
	 * @param y y value
	 * @param width width value
	 * @param weightx weightx value
	 * @param weighty weighty value
	 * @param fill fill value
	 * @return The specified {@link GridBagConstraints}
	 */
	public static GridBagConstraints getGBC(int x, int y, int width,
			double weightx, double weighty, int fill) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.insets = new java.awt.Insets(1, 5, 1, 5);
		gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gbc.fill = fill;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridwidth = width;
		return gbc;
	}
}
