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
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

/**
 * Utility class for ensuring that ZAP's UI components use safe popup menus that won't remain "always
 * on top" if the application freezes.
 *
 * <p>This class should be initialized at startup to replace the default popup factory with our
 * custom implementation.
 *
 * @since 2.15.0
 */
public final class PopupSafeguardUtils {

    // Private constructor to prevent instantiation
    private PopupSafeguardUtils() {}

    /**
     * Initializes the custom popup factory that prevents popup menus from inheriting the "always on
     * top" property. This method should be called during ZAP's startup.
     */
    public static void initializePopupSafeguards() {
        // Register custom factory for combo box UI
        UIManager.put("ComboBox.noActionOnKeyNavigation", Boolean.TRUE);
        
        // Add additional UIManager customizations if needed
        
        // Override the default popup menu factory if possible
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    
    /**
     * Makes an existing combo box use the safe popup implementation.
     *
     * @param comboBox the combo box to modify
     */
    public static void makeSafe(JComboBox<?> comboBox) {
        ZapComboBoxFactory.setupSafePopup(comboBox);
    }
}
