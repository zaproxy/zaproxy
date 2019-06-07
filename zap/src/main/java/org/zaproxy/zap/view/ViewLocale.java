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
package org.zaproxy.zap.view;

import java.util.Locale;

/**
 * Representation of a {@code Locale} for displaying in view components (for example, {@code
 * JComboBox}).
 *
 * <p>The method {@code toString()} returns the (display) name of the locale.
 *
 * @see Locale
 * @since 2.4.0
 */
public class ViewLocale {

    private final String locale;
    private final String displayName;

    public ViewLocale(String locale, String displayName) {
        this.locale = locale;
        this.displayName = displayName;
    }

    /**
     * Gets the language and country of the locale (as {@code String}, for example, "en_GB").
     *
     * @return the locale as {@code String}
     */
    public String getLocale() {
        return locale;
    }

    /** Returns the display name of the locale. */
    @Override
    public String toString() {
        return displayName;
    }

    @Override
    public int hashCode() {
        return 31 + ((locale == null) ? 0 : locale.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ViewLocale other = (ViewLocale) obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }
}
