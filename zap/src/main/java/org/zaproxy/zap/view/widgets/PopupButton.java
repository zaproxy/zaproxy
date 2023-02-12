/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.view.widgets;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * A button that shows a dynamic popup menu when pressed. The menu items can be changed as required.
 */
@SuppressWarnings("serial")
public abstract class PopupButton extends JButton {
    private static final long serialVersionUID = 1L;
    private ActionListener actionListener;

    public PopupButton() {
        super();
        addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        getPopupMenu().show(PopupButton.this, 0, getHeight());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        repaint();
                    }
                });
    }

    private ActionListener getActionListener() {
        if (actionListener == null) {
            actionListener =
                    e -> {
                        Object o = e.getSource();
                        if (o instanceof JMenuItem) {
                            menuItemSelected(((JMenuItem) o).getText());
                        }
                    };
        }
        return actionListener;
    }

    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        String selected = this.getSelectedMenuItem();
        for (String option : getMenuItemNames()) {
            JMenuItem item = new JMenuItem(option);
            item.addActionListener(getActionListener());
            if (option.equals(selected)) {
                item.setSelected(true);
                item.setEnabled(false);
                item.setIcon(
                        new ImageIcon(this.getClass().getResource("/resource/icon/105_gray.png")));
            }
            popupMenu.add(item);
        }
        return popupMenu;
    }

    /**
     * Returns the list of menu item names to be displayed - this will be called whenever the button
     * is pressed
     *
     * @return the list of menu item names
     */
    public abstract List<String> getMenuItemNames();

    /**
     * Called when a menu item is selected
     *
     * @param menuItemName the name of the selected menu item
     */
    public abstract void menuItemSelected(String menuItemName);

    /**
     * Returns the name of the current menu item selected, or null if none are selected. The
     * selected menu item will be disabled and have an arrow icon.
     *
     * @return the name of the selected menu item
     */
    public abstract String getSelectedMenuItem();
}
