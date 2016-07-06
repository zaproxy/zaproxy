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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the extensions' configurations saved in the configuration file.
 * <p>
 * It allows to change, programmatically, the extensions' enabled state.
 *
 * @since 2.5.0
 */
public class ExtensionParam extends AbstractParam {

    private static final Logger LOGGER = Logger.getLogger(ExtensionParam.class);

    /**
     * The base configuration key for all extensions configurations.
     */
    private static final String EXTENSION_BASE_KEY = "extensions";

    /**
     * The configuration key used to clear saved options and save/load the extensions' name and state.
     */
    private static final String ALL_EXTENSIONS_KEY = EXTENSION_BASE_KEY + ".extension";

    /**
     * The configuration key used to save/load the name of an extension.
     */
    private static final String EXTENSION_NAME_KEY = "name";

    /**
     * The configuration key used to save/load the enabled state of an extension.
     */
    private static final String EXTENSION_ENABLED_KEY = "enabled";

    /**
     * The extensions' state, never {@code null}.
     */
    private List<ExtensionState> extensions = Collections.emptyList();

    @Override
    protected void parse() {
        try {
            List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig())
                    .configurationsAt(ALL_EXTENSIONS_KEY);
            extensions = new ArrayList<>(fields.size());
            for (HierarchicalConfiguration sub : fields) {
                String name = sub.getString(EXTENSION_NAME_KEY, "");
                boolean enabled = sub.getBoolean(EXTENSION_ENABLED_KEY, true);
                extensions.add(new ExtensionState(name, enabled));
            }
            extensions = Collections.unmodifiableList(extensions);
        } catch (ConversionException e) {
            LOGGER.error("Error while loading extensions' state: " + e.getMessage(), e);
            extensions = Collections.emptyList();
        }
    }

    /**
     * Gets the extensions' state (as saved in the configuration file).
     *
     * @return an unmodifiable list with the extensions' state, never {@code null}
     */
    List<ExtensionState> getExtensions() {
        return extensions;
    }

    /**
     * Sets the extensions' state, to be saved in the configuration file.
     *
     * @param extensionsState the extensions' state
     */
    void setExtensions(List<ExtensionState> extensionsState) {
        if (extensionsState == null) {
            throw new IllegalArgumentException("Parameter extensionsState must not be null.");
        }

        ((HierarchicalConfiguration) getConfig()).clearTree(ALL_EXTENSIONS_KEY);
        int enabledCount = 0;
        for (int i = 0; i < extensionsState.size(); i++) {
            ExtensionState elem = extensionsState.get(i);
            // Don't persist if enabled, extensions are enabled by default.
            if (!elem.isEnabled()) {
                String elementBaseKey = ALL_EXTENSIONS_KEY + "(" + enabledCount + ").";
                getConfig().setProperty(elementBaseKey + EXTENSION_NAME_KEY, elem.getName());
                getConfig().setProperty(elementBaseKey + EXTENSION_ENABLED_KEY, Boolean.valueOf(elem.isEnabled()));

                enabledCount++;
            }
        }
        this.extensions = Collections.unmodifiableList(extensionsState);
    }

    @Override
    public ExtensionParam clone() {
        return (ExtensionParam) super.clone();
    }

    /**
     * An extension's state.
     * <p>
     * Contains the name of the extension and the enabled state.
     */
    static final class ExtensionState {

        private final String name;
        private final boolean enabled;

        public ExtensionState(String name, boolean enabled) {
            if (name == null) {
                throw new IllegalArgumentException("Parameter name must not be null.");
            }

            this.name = name;
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (enabled ? 1231 : 1237);
            result = prime * result + name.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            ExtensionState other = (ExtensionState) object;
            if (enabled != other.enabled) {
                return false;
            }
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder(75);
            strBuilder.append("[Name=").append(name).append(", Enabled=").append(enabled).append(']');
            return strBuilder.toString();
        }
    }
}
