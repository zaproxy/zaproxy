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

import com.formdev.flatlaf.FlatDarkLaf;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ZAP Navy Theme - A modern professional dark theme with navy blue tones
 * Provides an elegant, corporate dark mode with reduced eye strain.
 */
public class ZapNavyTheme extends FlatDarkLaf {

    private static final Logger LOGGER = LogManager.getLogger(ZapNavyTheme.class);
    public static final String NAME = "ZAP Navy (Dark)";

    public static boolean setup() {
        boolean result = setup(new ZapNavyTheme());
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
        return "Modern professional dark theme with navy blue tones for ZAP";
    }

    @Override
    public InputStream getDefaultsAsStream() {
        try {
            return ZapNavyTheme.class.getResourceAsStream("ZapNavyTheme.properties");
        } catch (Exception e) {
            LOGGER.warn("Failed to load ZapNavyTheme.properties: {}", e.getMessage());
            return super.getDefaultsAsStream();
        }
    }
}
