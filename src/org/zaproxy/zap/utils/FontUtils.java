/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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
package org.zaproxy.zap.utils;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.UIManager;

public class FontUtils {
	public static final Font systemDefaultFont;
	public static enum Size {smallest, much_smaller, smaller, standard, larger, much_larger, huge};
	
	static {
		systemDefaultFont = (Font) UIManager.getLookAndFeelDefaults().get("defaultFont");
	}
	
	public static void setDefaultFont(Font font) {
    	UIManager.getLookAndFeelDefaults().put("defaultFont", font);
	}
	
	public static void setDefaultFont(String name, int size) {
		// A blank font name works fine.
		// For some reason getting the default font name doesnt work - it doesnt seem to get applied everywhere
		// No ideas why :/
		if (size <= 5) {
			size = getDefaultFont().getSize();
		}
		if (size <= 5) {
			size = getDefaultFont().getSize();
		}

		UIManager.getLookAndFeelDefaults().put("defaultFont", new Font(name, Font.PLAIN, size));
	}
	

	private static Font getDefaultFont() {
		Font font = Font.getFont("defaultFont");
		if (font == null) {
			font = new JLabel("").getFont();
		}
		return font;
	}
	
	public static Font getFont (String name) {
		return new Font(name, Font.PLAIN, getDefaultFont().getSize());
	}
	
	public static Font getFont (int style) {
		return getDefaultFont().deriveFont(style);
	}
	
	public static Font getFont (String name, int style) {
		Font font = (Font) UIManager.getLookAndFeelDefaults().get("defaultFont");
		return new Font(name, style, font.getSize());
	}
	
	public static Font getFont (int style, Size size) {
		return getFont(size).deriveFont(style);
	}
	
	public static Font getFont (Size size) {
		Font font = getDefaultFont();
		
		float s;
		switch (size) {
		case smallest:		s = (float) (font.getSize() * 0.5); break;
		case much_smaller:	s = (float) (font.getSize() * 0.7); break;
		case smaller:		s = (float) (font.getSize() * 0.8); break;
		case standard:		s = (float) font.getSize(); break;
		case larger:		s = (float) (font.getSize() * 1.5); break;
		case much_larger:	s = (float) (font.getSize() * 2); break;
		case huge:			s = (float) (font.getSize() * 4); break;
		default: 			s = (float) (font.getSize()); break;
		}
		return getDefaultFont().deriveFont(s);
	}

}
