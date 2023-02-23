/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.utils;

import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.parosproxy.paros.model.Model;

public class DisplayUtils {

    private static List<Image> zapIconImages;

    private static Boolean scaleImages = null;
    private static int iconSize = 0;
    private static final int STD_HEIGHT = 16;

    private static int getIconSize() {
        if (scaleImages == null) {
            scaleImages = Model.getSingleton().getOptionsParam().getViewParam().isScaleImages();
            if (scaleImages) {
                iconSize =
                        Model.getSingleton()
                                .getOptionsParam()
                                .getViewParam()
                                .getFontSize(FontUtils.FontType.general);
            }
        }
        if (iconSize < 1) {
            iconSize = Model.getSingleton().getOptionsParam().getViewParam().getIconSize();
            if (iconSize < 1) {
                iconSize = STD_HEIGHT;
            }
        }
        return iconSize;
    }

    private static float getScale() {
        return Math.max(FontUtils.getScale(), (float) getIconSize() / STD_HEIGHT);
    }

    private static int getScaledIconWidth(Icon icon) {
        return getIconSize() * icon.getIconWidth() / icon.getIconHeight();
    }

    /**
     * Gets a correctly scaled icon from the specified url
     *
     * @param url
     * @return
     * @since 2.6.0
     */
    public static ImageIcon getScaledIcon(URL url) {
        if (url == null) {
            return null;
        }
        return getScaledIcon(new ImageIcon(url));
    }

    public static ImageIcon getScaledIcon(ImageIcon icon) {
        if (icon == null || icon.getIconHeight() > STD_HEIGHT) {
            return icon;
        }
        return new ImageIcon(
                icon.getImage()
                        .getScaledInstance(
                                getScaledIconWidth(icon), getIconSize(), Image.SCALE_SMOOTH));
    }

    public static Icon getScaledIcon(Icon icon) {
        if (!(icon instanceof ImageIcon)) {
            return icon;
        }
        return getScaledIcon((ImageIcon) icon);
    }

    public static Dimension getScaledDimension(int width, int height) {
        if (getScale() == 1) {
            // don't need to scale
            return new Dimension(width, height);
        }
        return new Dimension((int) (width * getScale()), (int) (height * getScale()));
    }

    public static int getScaledSize(int size) {
        return (int) (size * getScale());
    }

    public static void scaleIcon(JLabel label) {
        if (label != null && label.getIcon() instanceof ImageIcon) {
            label.setIcon(getScaledIcon((ImageIcon) label.getIcon()));
        }
    }

    public static void scaleIcon(JButton button) {
        if (button != null && button.getIcon() instanceof ImageIcon) {
            button.setIcon(getScaledIcon((ImageIcon) button.getIcon()));
        }
    }

    public static void scaleIcon(JToggleButton button) {
        if (button != null) {
            if (button.getIcon() instanceof ImageIcon) {
                button.setIcon(getScaledIcon((ImageIcon) button.getIcon()));
            }
            if (button.getSelectedIcon() instanceof ImageIcon) {
                button.setSelectedIcon(getScaledIcon((ImageIcon) button.getSelectedIcon()));
            }
            if (button.getRolloverIcon() instanceof ImageIcon) {
                button.setRolloverIcon(getScaledIcon((ImageIcon) button.getRolloverIcon()));
            }
            if (button.getRolloverSelectedIcon() instanceof ImageIcon) {
                button.setRolloverSelectedIcon(
                        getScaledIcon((ImageIcon) button.getRolloverSelectedIcon()));
            }
            if (button.getDisabledIcon() instanceof ImageIcon) {
                button.setDisabledIcon(getScaledIcon((ImageIcon) button.getDisabledIcon()));
            }
            if (button.getDisabledSelectedIcon() instanceof ImageIcon) {
                button.setDisabledSelectedIcon(
                        getScaledIcon((ImageIcon) button.getDisabledSelectedIcon()));
            }
            if (button.getPressedIcon() instanceof ImageIcon) {
                button.setPressedIcon(getScaledIcon((ImageIcon) button.getPressedIcon()));
            }
        }
    }

    /**
     * Gets the available ZAP icon images.
     *
     * <p>Contains images with several dimensions, with higher dimensions at the end of the list.
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
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap16x16.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap32x32.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap48x48.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap64x64.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap128x128.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap256x256.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(DisplayUtils.class.getResource("/resource/zap512x512.png")));
            images.add(
                    Toolkit.getDefaultToolkit()
                            .getImage(
                                    DisplayUtils.class.getResource("/resource/zap1024x1024.png")));
            zapIconImages = Collections.unmodifiableList(images);
        }
    }

    /**
     * Gets correctly scaled Insets
     *
     * @since 2.7.0
     */
    public static Insets getScaledInsets(int top, int left, int bottom, int right) {
        if (getScale() == 1) {
            // don't need to scale
            return new Insets(top, left, bottom, right);
        }
        return new Insets(
                (int) (top * getScale()), (int) (left * getScale()),
                (int) (bottom * getScale()), (int) (right * getScale()));
    }

    /**
     * Returns true if a known dark LookAndFeel is being used
     *
     * @since 2.10.0
     */
    public static boolean isDarkLookAndFeel() {
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof FlatLaf) {
            FlatLaf flaf = (FlatLaf) laf;
            return flaf.isDark();
        }
        return false;
    }

    /**
     * Returns a highlight color suitable for the selected LookAndFeel
     *
     * @return a highlight color suitable for the selected LookAndFeel
     */
    public static Color getHighlightColor() {
        return isDarkLookAndFeel() ? new Color(0x66, 0x22, 0) : Color.LIGHT_GRAY;
    }
}
