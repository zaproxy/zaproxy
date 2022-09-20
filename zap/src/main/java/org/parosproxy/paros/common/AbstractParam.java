/*
 *
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2005 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2011/05/27 Catch any exception when loading the config file
// ZAP: 2011/11/15 Changed to use ZapXmlConfiguration, to enforce the same character encoding when
// reading/writing configurations
//      removed duplicated method calls and removed an unnecessary method (load())
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/01/17 Issue 987: Allow arbitrary config file values to be set via the command line
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2016/09/22 JavaDoc tweaks
// ZAP: 2016/11/17 Issue 2701 Support Factory Reset
// ZAP: 2017/03/26 Obtain configs in the order specified
// ZAP: 2017/06/02 Add helper methods.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/09/08 Use format specifiers instead of concatenation when logging.
package org.parosproxy.paros.common;

import java.util.Map.Entry;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public abstract class AbstractParam implements Cloneable {

    private static final Logger logger = LogManager.getLogger(AbstractParam.class);

    private FileConfiguration config = null;
    /**
     * Loads the configurations from the given configuration file.
     *
     * @param config the configuration file
     */
    public void load(FileConfiguration config) {
        this.config = config;

        try {
            parse();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Loads the configurations from the file located at the given path.
     *
     * @param filePath the path to the configuration file, might be relative.
     */
    public void load(String filePath) {
        this.load(filePath, null);
    }

    /**
     * Loads the configurations from the file located at the given path and using the given
     * overrides
     *
     * @param filePath the path to the configuration file, might be relative.
     * @param overrides the configuration overrides, might be {@code null}.
     */
    public void load(String filePath, ControlOverrides overrides) {
        try {
            config = new ZapXmlConfiguration(filePath);
            if (overrides != null) {
                for (Entry<String, String> entry : overrides.getOrderedConfigs().entrySet()) {
                    logger.info(
                            "Setting config {} = {} was {}",
                            entry.getKey(),
                            entry.getValue(),
                            config.getString(entry.getKey()));
                    config.setProperty(entry.getKey(), entry.getValue());
                }
            }
            parse();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Gets the configuration file, previously loaded.
     *
     * @return the configurations file
     */
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public AbstractParam clone() {
        try {
            AbstractParam clone = (AbstractParam) super.clone();
            clone.load((FileConfiguration) ConfigurationUtils.cloneConfiguration(config));
            return clone;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Parses the configurations.
     *
     * <p>Called each time the configurations are loaded.
     *
     * @see #getConfig()
     */
    protected abstract void parse();

    /**
     * Will be called to reset the options to factory defaults. Most classes will not need to do
     * anything, but those that do can override this method.
     */
    public void reset() {
        // Do nothing
    }

    /**
     * Gets the {@code String} with the given configuration key.
     *
     * <p>The default value is returned if the key doesn't exist or it's not a {@code String}.
     *
     * @param key the configuration key.
     * @param defaultValue the default value, if the key doesn't exist or it's not a {@code String}.
     * @return the value of the configuration, or default value.
     * @since 2.7.0
     */
    protected String getString(String key, String defaultValue) {
        try {
            return getConfig().getString(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, e);
        }
        return defaultValue;
    }

    /**
     * Logs the given {@code ConversionException}, that occurred while reading the configuration
     * with the given key.
     *
     * @param key the configuration key.
     * @param e the {@code ConversionException}.
     * @since 2.7.0
     */
    protected static void logConversionException(String key, ConversionException e) {
        logger.warn("Failed to read '{}'", key, e);
    }

    /**
     * Gets the {@code boolean} with the given configuration key.
     *
     * <p>The default value is returned if the key doesn't exist or it's not a {@code boolean}.
     *
     * @param key the configuration key.
     * @param defaultValue the default value, if the key doesn't exist or it's not a {@code
     *     boolean}.
     * @return the value of the configuration, or default value.
     * @since 2.7.0
     */
    protected boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getConfig().getBoolean(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, e);
        }
        return defaultValue;
    }

    /**
     * Gets the {@code int} with the given configuration key.
     *
     * <p>The default value is returned if the key doesn't exist or it's not a {@code int}.
     *
     * @param key the configuration key.
     * @param defaultValue the default value, if the key doesn't exist or it's not an {@code int}.
     * @return the value of the configuration, or default value.
     * @since 2.7.0
     */
    protected int getInt(String key, int defaultValue) {
        try {
            return getConfig().getInt(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, e);
        }
        return defaultValue;
    }

    /**
     * Gets the {@code Integer} with the given configuration key.
     *
     * <p>The default value is returned if the key doesn't exist or it's not a {@code Integer}.
     *
     * @param key the configuration key.
     * @param defaultValue the default value, if the key doesn't exist or it's not an {@code
     *     Integer}.
     * @return the value of the configuration, or default value.
     * @since 2.7.0
     */
    protected Integer getInteger(String key, Integer defaultValue) {
        try {
            return getConfig().getInteger(key, defaultValue);
        } catch (ConversionException e) {
            logConversionException(key, e);
        }
        return defaultValue;
    }
}
