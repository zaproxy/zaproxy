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

import com.formdev.flatlaf.FlatLightLaf;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ZAP Turquoise Theme - A modern professional light theme with turquoise accents
 * Provides a fresh, corporate look with excellent readability.
 */
public class ZapTurquoiseTheme extends FlatLightLaf {

    private static final Logger LOGGER = LogManager.getLogger(ZapTurquoiseTheme.class);
    public static final String NAME = "ZAP Turquoise (Light)";

    public static boolean setup() {
        boolean result = setup(new ZapTurquoiseTheme());
        if (result) {
            ModernUIEnhancer.applyModernUIDefaults();
        }
        return result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Modern professional light theme with turquoise accents for ZAP";
    }

    @Override
    public InputStream getDefaultsAsStream() {
        try {
            return ZapTurquoiseTheme.class.getResourceAsStream("ZapTurquoiseTheme.properties");
        } catch (Exception e) {
            LOGGER.warn("Failed to load ZapTurquoiseTheme.properties: {}", e.getMessage());
            return super.getDefaultsAsStream();
        }
    }
}
