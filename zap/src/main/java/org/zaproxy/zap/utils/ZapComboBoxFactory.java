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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * A factory that creates combo box UIs with popup menus that prevent "always on top" behavior.
 *
 * <p>This factory should be used to create combo boxes that won't remain on top of other
 * applications when ZAP freezes.
 *
 * @since 2.15.0
 */
public class ZapComboBoxFactory {

    /**
     * Creates a JComboBox with a safe popup that won't remain "always on top" if the application
     * freezes.
     *
     * @param items the items to be added to the combo box
     * @return a JComboBox with a safe popup
     */
    public static <E> JComboBox<E> createZapComboBox(E[] items) {
        JComboBox<E> comboBox = new JComboBox<>(items);
        setupSafePopup(comboBox);
        return comboBox;
    }

    /**
     * Creates a JComboBox with a safe popup that won't remain "always on top" if the application
     * freezes.
     *
     * @return a JComboBox with a safe popup
     */
    public static <E> JComboBox<E> createZapComboBox() {
        JComboBox<E> comboBox = new JComboBox<>();
        setupSafePopup(comboBox);
        return comboBox;
    }

    /**
     * Configures an existing JComboBox to use a safe popup that won't remain "always on top" if the
     * application freezes.
     *
     * @param comboBox the JComboBox to configure
     */
    public static void setupSafePopup(JComboBox<?> comboBox) {
        comboBox.setUI(new ZapComboBoxUI());
    }

    /**
     * A custom ComboBoxUI that creates a popup that won't remain "always on top" if the application
     * freezes.
     */
    private static class ZapComboBoxUI extends BasicComboBoxUI {
        @Override
        protected ComboPopup createPopup() {
            return new ZapComboPopup(comboBox);
        }
    }

    /**
     * A custom BasicComboPopup that uses a ZapPopupMenu to prevent "always on top" behavior.
     */
    private static class ZapComboPopup extends BasicComboPopup {
        public ZapComboPopup(JComboBox<?> comboBox) {
            super(comboBox);
        }

        @Override
        protected JPopupMenu createPopupMenu() {
            return new ZapPopupMenu();
        }

        @Override
        protected void configurePopup() {
            super.configurePopup();
            // Add extra configuration here if needed
        }

        @Override
        protected void configureList() {
            super.configureList();
            // Add extra configuration here if needed
        }

        @Override
        protected void configureScroller() {
            super.configureScroller();
            // Add extra configuration here if needed
        }
    }
}
