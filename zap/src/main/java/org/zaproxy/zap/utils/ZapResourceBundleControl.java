/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A {@code ResourceBundle.Control} that reverts the language mappings done by the {@code Locale}
 * class when {@link #toBundleName(String, Locale) creating bundle names}, so that the resources can
 * have the newer ISO 639 language codes.
 *
 * <p>It does the following mappings:
 *
 * <ul>
 *   <li><code>in</code> to <code>id</code>
 *   <li><code>iw</code> to <code>he</code>
 *   <li><code>ji</code> to <code>yi</code>
 * </ul>
 *
 * @since 2.8.0
 * @see Locale#Locale(String)
 */
public class ZapResourceBundleControl extends ResourceBundle.Control {

    /** The language mappings, from old to new codes. */
    private static final Map<String, String> LANGUAGE_MAPPINGS = new HashMap<>(3);

    static {
        LANGUAGE_MAPPINGS.put("in", "id");
        LANGUAGE_MAPPINGS.put("iw", "he");
        LANGUAGE_MAPPINGS.put("ji", "yi");
    }

    /**
     * The supported formats.
     *
     * @see #getFormats(String)
     */
    private final List<String> formats;

    private final Locale fallbackLocale;

    /**
     * Constructs a {@code ZapResourceBundleControl} with format {@link
     * java.util.ResourceBundle.Control#FORMAT_PROPERTIES FORMAT_PROPERTIES} and {@link
     * Locale#getDefault() default locale} as fallback locale.
     *
     * @see #getFallbackLocale(String, Locale)
     * @see #getFormats(String)
     */
    public ZapResourceBundleControl() {
        this(Locale.getDefault());
    }

    /**
     * Constructs a {@code ZapResourceBundleControl} with the given supported formats and {@link
     * Locale#getDefault() default locale} as fallback locale.
     *
     * @param formats the supported formats.
     * @see #getFallbackLocale(String, Locale)
     * @see #getFormats(String)
     */
    public ZapResourceBundleControl(List<String> formats) {
        this(formats, Locale.getDefault());
    }

    /**
     * Constructs a {@code ZapResourceBundleControl} with the given fallback locale and with format
     * {@link java.util.ResourceBundle.Control#FORMAT_PROPERTIES FORMAT_PROPERTIES} formats.
     *
     * @param fallbackLocale the fallback locale, might be {@code null}.
     * @see #getFallbackLocale(String, Locale)
     * @see #getFormats(String)
     */
    public ZapResourceBundleControl(Locale fallbackLocale) {
        this(ResourceBundle.Control.FORMAT_PROPERTIES, fallbackLocale);
    }

    /**
     * Constructs a {@code ZapResourceBundleControl} with the given supported formats and fallback
     * locale.
     *
     * @param formats the supported formats.
     * @param fallbackLocale the fallback locale, might be {@code null}.
     * @see #getFallbackLocale(String, Locale)
     * @see #getFormats(String)
     */
    public ZapResourceBundleControl(List<String> formats, Locale fallbackLocale) {
        if (formats == null) {
            throw new IllegalArgumentException("The parameter formats must not be null.");
        }
        this.formats = Collections.unmodifiableList(new ArrayList<>(formats));
        this.fallbackLocale = fallbackLocale;
    }

    @Override
    public String toBundleName(String baseName, Locale locale) {
        String bundleName = super.toBundleName(baseName, locale);
        String language = locale.getLanguage();
        String mapping = LANGUAGE_MAPPINGS.get(language);
        if (mapping != null) {
            return bundleName.replace(baseName + "_" + language, baseName + "_" + mapping);
        }
        return bundleName;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return formats;
    }

    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
        return locale.equals(fallbackLocale) ? null : fallbackLocale;
    }
}
