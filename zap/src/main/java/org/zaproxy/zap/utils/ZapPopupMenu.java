/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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

import java.awt.Component;
import java.awt.Window;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * A JPopupMenu subclass that ensures popup windows are never displayed with "Always on Top" property
 * set.
 *
 * <p>This prevents popup menus from remaining on top of other applications when ZAP freezes.
 *
 * @since 2.15.0
 */
@SuppressWarnings("serial")
public class ZapPopupMenu extends JPopupMenu {

    public ZapPopupMenu() {
        super();
    }

    public ZapPopupMenu(String label) {
        super(label);
    }

    /**
     * Overridden to ensure that the popup window is never displayed with "Always on Top" property
     * set.
     */
    @Override
    public void show(Component invoker, int x, int y) {
        // Ensure popup windows created by this menu are never "always on top"
        addPopupMenuListener(new AlwaysOnTopPreventingPopupMenuListener());
        
        // Call original implementation to show the popup
        super.show(invoker, x, y);
    }

    /**
     * A popup menu listener that ensures the popup window is never displayed with "Always on Top"
     * property set.
     */
    private static class AlwaysOnTopPreventingPopupMenuListener implements javax.swing.event.PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
            // Get the popup window just before it becomes visible
            if (e.getSource() instanceof JPopupMenu) {
                JPopupMenu menu = (JPopupMenu) e.getSource();
                // Use SwingUtilities.invokeLater to ensure this runs after the popup is created
                SwingUtilities.invokeLater(() -> {
                    // Find the Window that contains this popup
                    Window window = SwingUtilities.getWindowAncestor(menu);
                    if (window != null) {
                        // Ensure it's never "always on top"
                        window.setAlwaysOnTop(false);
                    }
                });
            }
        }

        @Override
        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            // Not needed
        }

        @Override
        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            // Not needed
        }
    }
}
