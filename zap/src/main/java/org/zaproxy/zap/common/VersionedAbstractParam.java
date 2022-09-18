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
package org.zaproxy.zap.common;

import org.apache.commons.configuration.ConversionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.common.AbstractParam;

/**
 * A versioned {@code AbstractParam}.
 *
 * <p>A version number is saved in the configuration file to keep track of changes between releases,
 * in case changes/updates are needed.
 *
 * @since 2.4.0
 * @see AbstractParam
 */
public abstract class VersionedAbstractParam extends AbstractParam {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * The key to read/write the version of the configurations, as an attribute. It should be used
     * to compose the key for the configurations node.
     *
     * @see #getConfigVersionKey()
     */
    protected static final String VERSION_ATTRIBUTE = "[@version]";

    /**
     * A dummy version number used at runtime to indicate that the configurations were never
     * persisted.
     *
     * @see #getCurrentVersion()
     * @see #ERROR_READING_CONFIG_VERSION
     */
    protected static final int NO_CONFIG_VERSION = -1;

    /**
     * A dummy version number used at runtime to indicate that an error occurred while reading the
     * version from the file.
     *
     * @see #getCurrentVersion()
     * @see #NO_CONFIG_VERSION
     */
    protected static final int ERROR_READING_CONFIG_VERSION = -2;

    /**
     * Parses the file for configurations.
     *
     * <p>Calls {@link #updateConfigFile()} followed by {@link #parseImpl()}.
     *
     * <p><strong>Note:</strong> It shouldn't be overridden in normal conditions.
     */
    @Override
    protected void parse() {
        updateConfigFile();
        parseImpl();
    }

    /**
     * Parses the file for configurations.
     *
     * <p>Called after checking and updating, if necessary, the configurations in the file.
     */
    protected abstract void parseImpl();

    /**
     * Updates the configurations in the file, if needed.
     *
     * <p>The following steps are made:
     *
     * <ol>
     *   <li>Read the version of the configurations that are in the file, by calling {@code
     *       readConfigFileVersion()};
     *   <li>Check if the version read is the latest version, by calling {@code
     *       isLatestConfigVersion(int)};
     *   <li>If it's not at the latest version, update the configurations, by calling {@code
     *       updateConfigsFromVersion(int)}.
     * </ol>
     *
     * <p><strong>Note:</strong> It shouldn't be overridden in normal conditions.
     *
     * @see #readConfigFileVersion()
     * @see #isLatestConfigVersion(int)
     * @see #updateConfigsFromVersion(int)
     */
    protected void updateConfigFile() {
        int configVersion = readConfigFileVersion();

        if (!isLatestConfigVersion(configVersion)) {
            updateConfigsFromVersion(configVersion);
        }
    }

    /**
     * Reads the version number from the configuration file, using the key returned by {@code
     * getConfigVersionKey()}.
     *
     * <p><strong>Note:</strong> It shouldn't be overridden in normal conditions.
     *
     * @return the version number read, or {@code NO_CONFIG_VERSION} if no version was found or
     *     {@code ERROR_READING_CONFIG_VERSION} if an error occurred while reading the version
     * @see #NO_CONFIG_VERSION
     * @see #ERROR_READING_CONFIG_VERSION
     * @see #getConfigVersionKey()
     */
    protected int readConfigFileVersion() {
        try {
            return getConfig().getInt(getConfigVersionKey(), NO_CONFIG_VERSION);
        } catch (ConversionException e) {
            logger.error(
                    "Error while getting the version of the configurations: {}", e.getMessage(), e);
            return ERROR_READING_CONFIG_VERSION;
        }
    }

    /**
     * Returns the key to read/write the version of the configurations.
     *
     * <p>For example:
     *
     * <blockquote>
     *
     * <pre>
     * BASE_KEY + VERSION_ATTRIBUTE
     * </pre>
     *
     * </blockquote>
     *
     * @return the key to read/write the version
     * @see #VERSION_ATTRIBUTE
     */
    protected abstract String getConfigVersionKey();

    /**
     * Tells whether or not the given {@code version} number is the latest version, that is, is the
     * same version number as the version of the running code.
     *
     * <p><strong>Note:</strong> It shouldn't be overridden in normal conditions.
     *
     * @param version the version that will be checked
     * @return {@code true} if the given {@code version} is the latest version, {@code false}
     *     otherwise
     * @see #getCurrentVersion()
     * @see #updateConfigFile()
     */
    protected boolean isLatestConfigVersion(int version) {
        return version == getCurrentVersion();
    }

    /**
     * Returns the current version of the configurations, that is, the version of running code.
     *
     * <p>The version number should be a positive integer, incremented (by one) for change(s) done
     * between releases. It only needs to be incremented for configuration changes (i.e. not
     * releases of the add-on).
     *
     * @return the current version of the configurations
     */
    protected abstract int getCurrentVersion();

    /**
     * Called when the configuration version in the file is different than the version of the
     * running code.
     *
     * <p>If the given {@code fileVersion} is:
     *
     * <ul>
     *   <li>&lt; current version - expected case, the configurations are changed/updated to the
     *       current version by calling the method {@code updateConfigsImpl(int)}. After calling the
     *       method the version in the configuration file is updated to the current version.
     *   <li>&gt; current version - no changes/updates are made, the method logs a warn and returns;
     *   <li>{@code NO_CONFIG_VERSION} - only the current version is written to the configuration
     *       file (Note: the method {@code updateConfigsImpl(int)} is still called);
     *   <li>{@code ERROR_READING_CONFIG_VERSION} - no changes/updates are made, the method logs a
     *       warn and returns.
     * </ul>
     *
     * <p><strong>Note:</strong> It shouldn't be overridden in normal conditions.
     *
     * @param fileVersion the version of the configurations in the file
     * @see #getCurrentVersion()
     * @see #NO_CONFIG_VERSION
     * @see #ERROR_READING_CONFIG_VERSION
     * @see #updateConfigsImpl(int)
     * @see #updateConfigFile()
     */
    protected void updateConfigsFromVersion(int fileVersion) {
        if (isLatestConfigVersion(fileVersion)) {
            return;
        }

        if (fileVersion == ERROR_READING_CONFIG_VERSION) {
            // There's not much that can be done (quickly and easily)... log and return.
            logger.warn("Configurations might not be in expected state, errors might happen...");
            return;
        }

        if (fileVersion != NO_CONFIG_VERSION) {
            if (fileVersion > getCurrentVersion()) {
                logger.warn(
                        "Configurations will not be updated, file version (v{}) is greater than the version of running code (v{}), errors might happen...",
                        fileVersion,
                        getCurrentVersion());
                return;
            }
            logger.info(
                    "Updating configurations from v{} to v{}", fileVersion, getCurrentVersion());
        }

        updateConfigsImpl(fileVersion);

        getConfig().setProperty(getConfigVersionKey(), getCurrentVersion());
    }

    /**
     * Called when the configuration version in the file is different than the version of the
     * running code.
     *
     * <p>Any required configuration changes/updates should be added to this method. For example:
     *
     * <blockquote>
     *
     * <pre>
     * switch (fileVersion) {
     * case NO_CONFIG_VERSION:
     *     // No updates/changes needed, the configurations were not previously persisted
     *     // and the current version is already written at the end of the previous method.
     *     break;
     * case 1:
     *    // Change the type of option X
     *    ...
     *    break; // if previous changes are required for following versions the break statement
     *           // would be removed to achieve an incremental update
     * case 2:
     *    // Remove option Y
     *    ...
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param fileVersion the version of the configurations in the file
     */
    protected abstract void updateConfigsImpl(int fileVersion);
}
