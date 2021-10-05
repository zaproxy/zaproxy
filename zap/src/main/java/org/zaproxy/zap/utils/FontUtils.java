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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.EnumMap;
import javax.swing.JLabel;
import javax.swing.UIManager;

public class FontUtils {

    public static enum Size {
        smallest,
        much_smaller,
        smaller,
        standard,
        larger,
        much_larger,
        huge
    }

    public static enum FontType {
        general,
        workPanels
    }

    private static float scale = -1;
    private static EnumMap<FontType, Font> defaultFonts = new EnumMap<>(FontType.class);
    private static EnumMap<FontType, Boolean> defaultFontSets = new EnumMap<>(FontType.class);

    private static Font systemDefaultFont;
    private static Font quicksandBoldFont;

    public static Font getSystemDefaultFont() {
        if (systemDefaultFont == null) {
            systemDefaultFont = (Font) UIManager.getLookAndFeelDefaults().get("defaultFont");

            if (systemDefaultFont == null) {
                systemDefaultFont = new JLabel("").getFont();
            }
        }

        return systemDefaultFont;
    }

    /**
     * Returns the Quicksand Bold font -
     * https://fonts.google.com/specimen/Quicksand?selection.family=Quicksand
     *
     * @since 2.7.0
     * @return the Quicksand Bold font
     */
    public static Font getQuicksandBoldFont() {
        if (quicksandBoldFont == null) {
            try {
                quicksandBoldFont =
                        Font.createFont(
                                Font.TRUETYPE_FONT,
                                FontUtils.class.getResourceAsStream(
                                        "/resource/Quicksand-Bold.ttf"));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(quicksandBoldFont);
                // Ensure its scaled properly - only need to do this when its first loaded
                quicksandBoldFont =
                        quicksandBoldFont.deriveFont((float) getDefaultFont().getSize());
            } catch (IOException | FontFormatException e) {
                quicksandBoldFont = defaultFonts.get(FontType.general);
            }
        }
        return quicksandBoldFont;
    }

    public static boolean canChangeSize() {
        return UIManager.getLookAndFeelDefaults().get("defaultFont") != null;
    }

    public static void setDefaultFont(FontType fontType, Font font) {
        if (canChangeSize()) {
            getSystemDefaultFont(); // Make sure the system default font is saved first
            defaultFonts.put(fontType, font);
            scale = -1; // force it to be recalculated
            if (fontType == FontType.general) {
                UIManager.getLookAndFeelDefaults().put("defaultFont", font);
            }
        }
    }

    public static void setDefaultFont(FontType fontType, String name, int size) {
        // A blank font name works fine.
        // For some reason getting the default font name doesn't work - it doesn't seem to get
        // applied everywhere
        // No ideas why :/
        if (size <= 5) {
            size = getDefaultFont().getSize();
        }

        defaultFontSets.put(fontType, (name != null && !name.isEmpty()));
        setDefaultFont(fontType, new Font(name, Font.PLAIN, size));
    }

    private static Font getDefaultFont() {
        return getDefaultFont(FontType.general);
    }

    private static Font getDefaultFont(FontType fontType) {
        if (defaultFonts.get(fontType) == null) {
            defaultFonts.put(fontType, Font.getFont("defaultFont"));
            if (defaultFonts.get(fontType) == null) {
                defaultFonts.put(fontType, new JLabel("").getFont());
            }
        }
        return defaultFonts.get(fontType);
    }

    /**
     * Gets the named font, correctly scaled
     *
     * @param name
     * @return the named font, correctly scaled
     */
    public static Font getFont(String name) {
        return getFont(name, Font.PLAIN);
    }

    /**
     * Gets the default font with the specified style, correctly scaled
     *
     * @param style
     * @return the default font with the specified style, correctly scaled
     */
    public static Font getFont(int style) {
        return getDefaultFont().deriveFont(style);
    }

    /**
     * Gets the font for the give {@link FontType}
     *
     * @param fontType the {@code FontType} for which the font should be returned
     * @return font
     */
    public static Font getFont(FontType fontType) {
        return getDefaultFont(fontType);
    }

    /**
     * Gets font for the given {@link FontType} or the fallback font with the given name if no font
     * is set for the given {@code FontType}
     *
     * @param fontType the {@code FontType} for which the font should be returned
     * @param fallbackFontName the name ({@code String}) of the font which will be returned of no
     *     font is set for the given {@code FontType}
     * @return work panels font or fallback font
     */
    public static Font getFontWithFallback(FontType fontType, String fallbackFontName) {
        if (isDefaultFontSet(fontType)) {
            return getFont(fontType);
        } else {
            return getFont(fallbackFontName);
        }
    }

    /**
     * Gets the named font with the specified style, correctly scaled
     *
     * @param name
     * @param style
     * @return the named font with the specified style, correctly scaled
     */
    public static Font getFont(String name, int style) {
        return new Font(name, style, getDefaultFont().getSize());
    }

    /**
     * Gets the default font with the specified style and size, correctly scaled
     *
     * @param style
     * @param size
     * @return
     */
    public static Font getFont(int style, Size size) {
        return getFont(getDefaultFont(), size).deriveFont(style);
    }

    /**
     * Gets the specified font with the specified style and size, correctly scaled
     *
     * @param style
     * @param size
     * @since 2.7.0
     * @return
     */
    public static Font getFont(Font font, int style, Size size) {
        return getFont(font, size).deriveFont(style);
    }

    /**
     * Gets the default font with the specified size, correctly scaled
     *
     * @param size
     * @return the default font with the specified size, correctly scaled
     */
    public static Font getFont(Size size) {
        return getFont(getDefaultFont(), size);
    }

    /**
     * Gets the specified font with the specified size, correctly scaled
     *
     * @param font
     * @param size
     * @since 2.7.0
     * @return the specified font with the specified size, correctly scaled
     */
    public static Font getFont(Font font, Size size) {
        float s;
        switch (size) {
            case smallest:
                s = (float) (font.getSize() * 0.5);
                break;
            case much_smaller:
                s = (float) (font.getSize() * 0.7);
                break;
            case smaller:
                s = (float) (font.getSize() * 0.8);
                break;
            case standard:
                s = (float) font.getSize();
                break;
            case larger:
                s = (float) (font.getSize() * 1.5);
                break;
            case much_larger:
                s = (float) (font.getSize() * 3);
                break;
            case huge:
                s = (float) (font.getSize() * 4);
                break;
            default:
                s = (float) (font.getSize());
                break;
        }
        return font.deriveFont(s);
    }

    public static float getScale() {
        if (scale == -1) {
            scale = getDefaultFont().getSize2D() / getSystemDefaultFont().getSize2D();
        }
        return scale;
    }

    /**
     * Tells whether or not a custom default font was set.
     *
     * <p>If no custom font was set it's used the system default font.
     *
     * @return {@code true} if a custom font was set, {@code false} otherwise.
     * @since 2.7.0
     * @see #getSystemDefaultFont()
     */
    public static boolean isDefaultFontSet() {
        return defaultFontSets.get(FontType.general);
    }

    /**
     * Tells whether or not a custom default font was set for the given {@link FontType}.
     *
     * @return {@code true} if a custom font was set, {@code false} otherwise.
     * @since 2.8.0
     * @see #getSystemDefaultFont()
     */
    public static boolean isDefaultFontSet(FontType fontType) {
        return defaultFontSets.get(fontType);
    }
}
