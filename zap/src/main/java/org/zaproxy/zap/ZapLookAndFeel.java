/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;

/**
 * A LookAndFeel which disables HTML in JLabels by default.
 *
 * @since 2.12.0
 */
class ZapLookAndFeel extends LookAndFeel {

    private final ZapUiDefaults defaults = new ZapUiDefaults();

    @Override
    public String getName() {
        return "ZAP Look and Feel";
    }

    @Override
    public String getID() {
        return "ZAP";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    @Override
    public UIDefaults getDefaults() {
        return defaults;
    }

    private static class ZapUiDefaults extends UIDefaults {

        private static final long serialVersionUID = 1L;

        @Override
        public ComponentUI getUI(JComponent target) {
            if (target instanceof JLabel) {
                target.putClientProperty("html.disable", Boolean.TRUE);
            }
            return null;
        }
    }
}
