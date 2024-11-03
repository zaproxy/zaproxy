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
package org.zaproxy.zap;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.AboutDialog;

/**
 * Class related to OSX GUI.
 *
 * <p>The class should only be used on OSX.
 *
 * @since 2.4.3
 * @see org.parosproxy.paros.Constant#isMacOsX()
 */
class OsXGui {

    private static final Logger LOGGER = LogManager.getLogger(OsXGui.class);

    private OsXGui() {}

    /**
     * Setups the GUI of ZAP for OSX.
     *
     * <p>Sets OS X related GUI properties and functionalities.
     */
    public static void setup() {
        // Set the various and sundry OS X-specific system properties
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("dock:name", "ZAP"); // Broken and unfixed; thanks, Apple
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ZAP"); // more thx

        // Override various handlers, so that About, Preferences, and Quit behave in an OS X typical
        // fashion.
        LOGGER.info("Initializing OS X specific settings.");

        // TODO Remove the menu item File -> Exit, redundant with quit handler.

        java.awt.Taskbar.getTaskbar().setIconImage(createIcon());

        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(ae -> showAboutDialog());
        desktop.setPreferencesHandler(pe -> showOptionsDialog());
        desktop.setQuitHandler((qe, qr) -> exitZap());
    }

    private static Image createIcon() {
        return Toolkit.getDefaultToolkit()
                .getImage(GuiBootstrap.class.getResource("/resource/zap1024x1024.png"));
    }

    private static void showAboutDialog() {
        AboutDialog dialog = new AboutDialog(View.getSingleton().getMainFrame(), true);
        dialog.setVisible(true);
    }

    private static void showOptionsDialog() {
        Control.getSingleton().getMenuToolsControl().options();
    }

    private static void exitZap() {
        Control.getSingleton().getMenuFileControl().exit();
    }
}
