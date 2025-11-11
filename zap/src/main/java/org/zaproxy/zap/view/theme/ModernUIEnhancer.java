/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.zap.view.theme;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 * ModernUIEnhancer - Provides modern UI enhancements for ZAP's interface
 * Adds professional styling, better spacing, and modern visual elements.
 */
public class ModernUIEnhancer {

    private static final int MODERN_SPACING = 8;
    private static final int MENU_ITEM_HEIGHT = 32;

    /**
     * Applies modern styling to a menu bar
     * @param menuBar the menu bar to enhance
     */
    public static void enhanceMenuBar(JMenuBar menuBar) {
        if (menuBar == null) {
            return;
        }

        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Apply modern font
        Font menuFont = UIManager.getFont("Menu.font");
        if (menuFont != null) {
            menuFont = menuFont.deriveFont(Font.PLAIN, 14f);
            menuBar.setFont(menuFont);
        }

        // Enhance all menus
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                enhanceMenu(menu);
            }
        }
    }

    /**
     * Applies modern styling to a menu
     * @param menu the menu to enhance
     */
    public static void enhanceMenu(JMenu menu) {
        if (menu == null) {
            return;
        }

        menu.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        Font menuFont = UIManager.getFont("Menu.font");
        if (menuFont != null) {
            menu.setFont(menuFont.deriveFont(Font.PLAIN, 14f));
        }

        // Enhance all menu items
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            if (menu.getMenuComponent(i) instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) menu.getMenuComponent(i);
                enhanceMenuItem(item);
            }
        }
    }

    /**
     * Applies modern styling to a menu item
     * @param item the menu item to enhance
     */
    public static void enhanceMenuItem(JMenuItem item) {
        if (item == null) {
            return;
        }

        item.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        Font itemFont = UIManager.getFont("MenuItem.font");
        if (itemFont != null) {
            item.setFont(itemFont.deriveFont(Font.PLAIN, 13f));
        }
    }

    /**
     * Applies modern styling to a toolbar
     * @param toolbar the toolbar to enhance
     */
    public static void enhanceToolbar(JToolBar toolbar) {
        if (toolbar == null) {
            return;
        }

        toolbar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
    }

    /**
     * Applies modern styling to a button
     * @param button the button to enhance
     */
    public static void enhanceButton(JButton button) {
        if (button == null) {
            return;
        }

        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);

        Font buttonFont = UIManager.getFont("Button.font");
        if (buttonFont != null) {
            button.setFont(buttonFont.deriveFont(Font.PLAIN, 13f));
        }
    }

    /**
     * Sets up global modern UI properties
     */
    public static void applyModernUIDefaults() {
        // Menu properties
        UIManager.put("Menu.border", BorderFactory.createEmptyBorder(4, 12, 4, 12));
        UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(6, 16, 6, 16));
        UIManager.put("Menu.selectionBackground", UIManager.getColor("MenuItem.selectionBackground"));

        // Button properties
        UIManager.put("Button.arc", 8);
        UIManager.put("Button.margin", new java.awt.Insets(8, 16, 8, 16));

        // ToolBar properties
        UIManager.put("ToolBar.separatorSize", new java.awt.Dimension(8, 24));

        // TabbedPane properties
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(8, 16, 8, 16));

        // General spacing
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("ScrollBar.thumbArc", 6);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

        // Modern shadows and borders
        UIManager.put("Component.borderWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.innerOutlineWidth", 0);

        // Table properties
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));

        // Tree properties
        UIManager.put("Tree.rowHeight", 28);
        UIManager.put("Tree.showsRootHandles", true);
    }
}
