/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.zaproxy.zap.utils.DisplayUtils;

public final class HighlighterUtils {

    private static final String OPAQUE = "Opaque";
    private static final String BACKGROUND = "Background";
    private static final String BORDER = "Border";
    private static final String TITLE = "title";
    private static final String FONT = "font";

    public static final Color DEFAULT_HIGHLIGHT_COLOR = new Color(255, 204, 0);

    public static final int DEFAULT_HIGHLIGHT_BORDER_THICKNESS = 2;

    public static HighlightedComponent highlightBackground(JComponent component, Color color) {
        return highlightBackground(new JComponentWithBackground(component), color);
    }

    public static HighlightedComponent highlightBackground(
            ComponentWithBackground componentWithBackground, Color color) {
        HighlightedComponent highlightedComponent =
                new HighlightedComponent(componentWithBackground.getComponent());
        highlightedComponent.put(OPAQUE, componentWithBackground.isOpaque());
        highlightedComponent.put(BACKGROUND, componentWithBackground.getBackground());
        componentWithBackground.setOpaque(true);
        componentWithBackground.setBackground(color);
        return highlightedComponent;
    }

    public static void undoHighlightBackground(
            HighlightedComponent highlightedComponent, JComponent component) {
        undoHighlightBackground(new JComponentWithBackground(component), highlightedComponent);
    }

    public static void undoHighlightBackground(
            ComponentWithBackground componentWithBackground,
            HighlightedComponent highlightedComponent) {
        componentWithBackground.setOpaque(highlightedComponent.get(OPAQUE));
        componentWithBackground.setBackground(highlightedComponent.get(BACKGROUND));
    }

    public static HighlightedComponent highlightTitleBorderWithHtml(
            ComponentWithTitle componentWithTitle) {
        return highlightTitleWithHtml(
                componentWithTitle,
                "<html><div style=' border: "
                        + DEFAULT_HIGHLIGHT_BORDER_THICKNESS
                        + "px solid; border-color: "
                        + getHighlightColorHexString()
                        + ";'>%s</div></html>");
    }

    public static void undoHighlightTitleBorderWithHtml(
            ComponentWithTitle componentWithTitle, HighlightedComponent highlightedComponent) {
        undoHighlightTitleWithHtml(componentWithTitle, highlightedComponent);
    }

    public static HighlightedComponent highlightTitleBackgroundWithHtml(
            ComponentWithTitle componentWithTitle) {
        return highlightTitleWithHtml(
                componentWithTitle,
                "<html><span style='background-color:"
                        + getHighlightColorHexString()
                        + ";'>%s</span></html>");
    }

    public static void undoHighlightTitleBackgroundWithHtml(
            ComponentWithTitle componentWithTitle, HighlightedComponent highlightedComponent) {
        undoHighlightTitleWithHtml(componentWithTitle, highlightedComponent);
    }

    private static HighlightedComponent highlightTitleWithHtml(
            ComponentWithTitle componentWithTitle, String format) {
        Object component = componentWithTitle.getComponent();
        HighlightedComponent highlightedComponent = new HighlightedComponent(component);
        String title = componentWithTitle.getTitle();
        if (!title.startsWith("<html>")) {
            // TitledBorder no longer supports HTML per ZapLookAndFeel changes.
            if (component instanceof TitledBorder) {
                TitledBorder titledBorder = (TitledBorder) component;
                Font font = titledBorder.getTitleFont();
                highlightedComponent.put(FONT, font);
                titledBorder.setTitleFont(font.deriveFont(Font.BOLD));
            } else {
                highlightedComponent.put(TITLE, title);
                String titleWithinHtml = String.format(format, title);
                componentWithTitle.setTitle(titleWithinHtml);
            }
            return highlightedComponent;
        }
        return null;
    }

    private static void undoHighlightTitleWithHtml(
            ComponentWithTitle componentWithTitle, HighlightedComponent highlightedComponent) {
        Object component = componentWithTitle.getComponent();
        if (component instanceof TitledBorder) {
            TitledBorder titledBorder = (TitledBorder) component;
            titledBorder.setTitleFont(highlightedComponent.get(FONT));
        } else {
            String title = highlightedComponent.get(TITLE);
            componentWithTitle.setTitle(title);
        }
    }

    public static HighlightedComponent highlightBorder(JComponent component, Color color) {
        HighlightedComponent highlightedComponent = new HighlightedComponent(component);
        highlightedComponent.put(BORDER, component.getBorder());
        component.setBorder(
                BorderFactory.createLineBorder(color, DEFAULT_HIGHLIGHT_BORDER_THICKNESS));
        return highlightedComponent;
    }

    public static void undoHighlightBorder(
            HighlightedComponent highlightedComponent, JComponent component) {
        component.setBorder(BorderFactory.createEmptyBorder());
        // ToDo: We should reset it back to the original border, but that currently does not work.
        // Sometimes the yellow highlight border stays there!
        // component.setBorder(highlightedComponent.get(BORDER));
    }

    private static String getHighlightColorHexString() {
        Color hlColor = getHighlightColor();
        String hex =
                String.format(
                        "#%02x%02x%02x", hlColor.getRed(), hlColor.getGreen(), hlColor.getBlue());
        return hex;
    }

    public static Color getHighlightColor() {
        Color hlColor = DEFAULT_HIGHLIGHT_COLOR;
        if (DisplayUtils.isDarkLookAndFeel()) {
            hlColor = DisplayUtils.getHighlightColor();
        }
        return hlColor;
    }
}
