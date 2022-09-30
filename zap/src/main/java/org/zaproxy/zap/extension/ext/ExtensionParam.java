/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.ext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the extensions' configurations saved in the configuration file.
 *
 * <p>It allows to change, programmatically, the extensions' enabled state.
 *
 * @since 2.5.0
 */
public class ExtensionParam extends AbstractParam {

    private static final Logger LOGGER = LogManager.getLogger(ExtensionParam.class);

    /** The base configuration key for all extensions configurations. */
    private static final String EXTENSION_BASE_KEY = "extensions";

    /**
     * The configuration key used to clear saved options and save/load the extensions' name and
     * state.
     */
    private static final String ALL_EXTENSIONS_KEY = EXTENSION_BASE_KEY + ".extension";

    /** The configuration key used to save/load the name of an extension. */
    private static final String EXTENSION_NAME_KEY = "name";

    /** The configuration key used to save/load the enabled state of an extension. */
    private static final String EXTENSION_ENABLED_KEY = "enabled";

    /** The extensions' state, never {@code null}. */
    private Map<String, Boolean> extensionsState = Collections.emptyMap();

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields =
                    ((HierarchicalConfiguration) getConfig()).configurationsAt(ALL_EXTENSIONS_KEY);
            Map<String, Boolean> extensions = new HashMap<>();
            for (HierarchicalConfiguration sub : fields) {
                if (!sub.getBoolean(EXTENSION_ENABLED_KEY, true)) {
                    extensions.put(sub.getString(EXTENSION_NAME_KEY, ""), Boolean.FALSE);
                }
            }
            extensionsState = Collections.unmodifiableMap(extensions);
        } catch (ConversionException e) {
            LOGGER.error("Error while loading extensions' state: {}", e.getMessage(), e);
            extensionsState = Collections.emptyMap();
        }
    }

    /**
     * Tells whether or not the extension with the given name is enabled.
     *
     * <p>Extensions are enabled by default.
     *
     * @param extensionName the name of the extension to check.
     * @return {@code true} if extension is enabled, {@code false} otherwise.
     * @since 2.6.0
     * @see #getExtensionsState()
     */
    public boolean isExtensionEnabled(String extensionName) {
        Boolean state = extensionsState.get(extensionName);
        if (state == null) {
            return true;
        }
        return state;
    }

    /**
     * Gets the extensions' enabled state.
     *
     * @return a {@code Map} containing the name of the extensions and corresponding enabled state.
     * @since 2.7.0
     * @see #isExtensionEnabled(String)
     */
    public Map<String, Boolean> getExtensionsState() {
        return new HashMap<>(extensionsState);
    }

    /**
     * Sets the extensions' state, to be saved in the configuration file.
     *
     * @param extensionsState the extensions' state
     */
    void setExtensionsState(Map<String, Boolean> extensionsState) {
        if (extensionsState == null) {
            throw new IllegalArgumentException("Parameter extensionsState must not be null.");
        }

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_EXTENSIONS_KEY);
        int enabledCount = 0;
        for (Map.Entry<String, Boolean> entry : extensionsState.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            // Don't persist if enabled, extensions are enabled by default.
            if (!entry.getValue()) {
                String elementBaseKey = ALL_EXTENSIONS_KEY + "(" + enabledCount + ").";
                getConfig().setProperty(elementBaseKey + EXTENSION_NAME_KEY, entry.getKey());
                getConfig().setProperty(elementBaseKey + EXTENSION_ENABLED_KEY, Boolean.FALSE);

                enabledCount++;
            }
        }
        this.extensionsState = Collections.unmodifiableMap(extensionsState);
    }

    @Override
    public ExtensionParam clone() {
        return (ExtensionParam) super.clone();
    }
}
