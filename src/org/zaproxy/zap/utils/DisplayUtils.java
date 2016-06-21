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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.parosproxy.paros.model.Model;


public class DisplayUtils {
	
	private static List<Image> zapIconImages;

	private static Boolean scaleImages = null;
	private final static int STD_HEIGHT = 16;
	
	private static boolean isScaleImages() {
		if (scaleImages == null) {
			scaleImages = Model.getSingleton().getOptionsParam().getViewParam().isScaleImages();
		}
		return scaleImages;
	}

	public static ImageIcon getScaledIcon(ImageIcon icon) {
		if (! isScaleImages() || icon == null || FontUtils.getScale() == 1 || icon.getIconHeight() > STD_HEIGHT) {
			// dont need to scale
			return icon;
		}
		return new ImageIcon((icon).getImage().getScaledInstance(
				(int)(icon.getIconWidth() * FontUtils.getScale()), (int)(icon.getIconHeight() * FontUtils.getScale()), Image.SCALE_SMOOTH));
	}
	
	public static Icon getScaledIcon(Icon icon) {
		if (! isScaleImages() || icon == null || FontUtils.getScale() == 1 || ! (icon instanceof ImageIcon)) {
			// dont need to scale (or cant)
			return icon;
		}
		return getScaledIcon((ImageIcon)icon);
	}
	
	public static Dimension getScaledDimension(int width, int height) {
		if (FontUtils.getScale() == 1) {
			// dont need to scale
			return new Dimension(width, height);
		}
		return new Dimension((int)(width * FontUtils.getScale()), (int)(height * FontUtils.getScale()));
		
	}
	
	public static int getScaledSize(int size) {
		return (int)(size * FontUtils.getScale());
	}
	
	public static void scaleIcon(JLabel label) {
		if (isScaleImages() && label != null && label.getIcon() != null && label.getIcon() instanceof ImageIcon) {
			label.setIcon(getScaledIcon((ImageIcon)label.getIcon()));
		}
	}
	
	public static void scaleIcon(JButton button) {
		if (isScaleImages() && button != null && button.getIcon() != null && 
				button.getIcon() instanceof ImageIcon) {
			button.setIcon(getScaledIcon((ImageIcon)button.getIcon()));
		}
	}

	public static void scaleIcon(JToggleButton button) {
		if (isScaleImages() && button != null) {
			if (button.getIcon() != null && button.getIcon() instanceof ImageIcon) {
				button.setIcon(getScaledIcon((ImageIcon)button.getIcon()));
			}
			if (button.getSelectedIcon() != null && button.getSelectedIcon() instanceof ImageIcon) {
				button.setSelectedIcon(getScaledIcon((ImageIcon)button.getSelectedIcon()));
			}
			if (button.getRolloverIcon() != null && button.getRolloverIcon() instanceof ImageIcon) {
				button.setRolloverIcon(getScaledIcon((ImageIcon)button.getRolloverIcon()));
			}
			if (button.getRolloverSelectedIcon() != null && button.getRolloverSelectedIcon() instanceof ImageIcon) {
				button.setRolloverSelectedIcon(getScaledIcon((ImageIcon)button.getRolloverSelectedIcon()));
			}
			if (button.getDisabledIcon() != null && button.getDisabledIcon() instanceof ImageIcon) {
				button.setDisabledIcon(getScaledIcon((ImageIcon)button.getDisabledIcon()));
			}
			if (button.getDisabledSelectedIcon() != null && button.getDisabledSelectedIcon() instanceof ImageIcon) {
				button.setDisabledSelectedIcon(getScaledIcon((ImageIcon)button.getDisabledSelectedIcon()));
			}
			if (button.getPressedIcon() != null && button.getPressedIcon() instanceof ImageIcon) {
				button.setPressedIcon(getScaledIcon((ImageIcon)button.getPressedIcon()));
			}
		}
	}

	/**
	 * Gets the available ZAP icon images.
	 * <p>
	 * Contains images with several dimensions, with higher dimensions at the end of the list.
	 * 
	 * @return a unmodifiable {@code List} with the ZAP icon images.
	 * @since 2.4.2
	 */
	public static List<Image> getZapIconImages() {
		if (zapIconImages == null) {
			createZapIconImages();
		}
		return zapIconImages;
	}

	private static synchronized void createZapIconImages() {
		if (zapIconImages == null) {
			List<Image> images = new ArrayList<>(8);
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap16x16.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap32x32.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap48x48.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap64x64.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap128x128.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap256x256.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap512x512.png")));
			images.add(Toolkit.getDefaultToolkit().getImage(DisplayUtils.class.getResource("/resource/zap1024x1024.png")));
			zapIconImages = Collections.unmodifiableList(images);
		}
	}
}
