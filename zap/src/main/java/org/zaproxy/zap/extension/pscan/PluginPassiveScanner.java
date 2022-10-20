/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Alert.Source;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.alert.ExampleAlertProvider;
import org.zaproxy.zap.utils.Enableable;
import org.zaproxy.zap.utils.Stats;

public abstract class PluginPassiveScanner extends Enableable
        implements PassiveScanner, ExampleAlertProvider {

    /**
     * The (base) configuration key used to saved the configurations of a passive scanner, ID, alert
     * threshold and enabled state.
     */
    private static final String PSCANS_KEY = PassiveScanParam.PASSIVE_SCANS_BASE_KEY + ".pscanner";

    /** The configuration key used to save/load the ID of a passive scanner. */
    private static final String ID_KEY = "id";

    /**
     * The configuration key used to load the classname of a passive scanner, used only for
     * backwards compatibility.
     */
    private static final String CLASSNAME_KEY = "classname";

    /**
     * The configuration key used to save/load the alert threshold of a passive scanner.
     *
     * <p>To be replaced by {@link #ALERT_THRESHOLD_KEY}.
     */
    private static final String LEVEL_KEY = "level";

    /** The configuration key used to save/load the alert threshold of a passive scanner. */
    private static final String ALERT_THRESHOLD_KEY = "alertthreshold";

    /** The configuration key used to save/load the enabled state of a passive scanner. */
    private static final String ENABLED_KEY = "enabled";

    private static final Integer[] DEFAULT_HISTORY_TYPES =
            new Integer[] {
                HistoryReference.TYPE_PROXIED, HistoryReference.TYPE_ZAP_USER,
                HistoryReference.TYPE_SPIDER, HistoryReference.TYPE_SPIDER_AJAX
            };

    private static final Set<Integer> DEFAULT_HISTORY_TYPES_SET =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DEFAULT_HISTORY_TYPES)));

    private AlertThreshold alertThreshold = AlertThreshold.DEFAULT;
    private AlertThreshold defaultAlertThreshold = AlertThreshold.MEDIUM;
    private Configuration config = null;
    private AddOn.Status status = AddOn.Status.unknown;

    private PassiveScanData passiveScanData;

    public PluginPassiveScanner() {
        super(true);
    }

    public void setHelper(PassiveScanData passiveScanData) {
        this.passiveScanData = passiveScanData;
    }

    /**
     * <strong>Note:</strong> This method should no longer need to be overridden, the functionality
     * provided by the {@code parent} can be obtained directly with {@link #newAlert()} and {@link
     * #addHistoryTag(String)}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setParent(PassiveScanThread parent) {
        // Nothing to do.
    }

    /**
     * Sets the current configuration of the passive scanner.
     *
     * @param config the configuration of the scanner
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 1.4.0
     * @see #getConfig()
     */
    public void setConfig(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("Parameter config must not be null.");
        }
        this.config = config;
        this.loadFrom(config);
    }

    public void loadFrom(Configuration conf) {
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig()).configurationsAt(PSCANS_KEY);
        for (HierarchicalConfiguration sub : fields) {
            if (isPluginConfiguration(sub)) {
                // For compatibility with older versions:
                String alertThresholdName = sub.getString(LEVEL_KEY, null);
                if (alertThresholdName == null) {
                    alertThresholdName =
                            sub.getString(ALERT_THRESHOLD_KEY, AlertThreshold.DEFAULT.name());
                }
                setAlertThreshold(AlertThreshold.valueOf(alertThresholdName));
                setEnabled(sub.getBoolean(ENABLED_KEY, true));
                break;
            }
        }
    }

    /**
     * Tells whether or not the given configuration belongs to this passive scanner.
     *
     * @param configuration the configuration to check
     * @return {@code true} if the configuration belongs to this passive scanner, {@code false}
     *     otherwise
     */
    private boolean isPluginConfiguration(Configuration configuration) {
        return (configuration.containsKey(ID_KEY) && getPluginId() == configuration.getInt(ID_KEY))
                // To keep backwards compatibility check also the classname
                || getClass().getCanonicalName().equals(configuration.getString(CLASSNAME_KEY, ""));
    }

    /**
     * Gets the current configuration of the passive scanner.
     *
     * @return the configuration of the scanner, might be {@code null}
     * @since 1.4.0
     * @see #setConfig(Configuration)
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Saves the configurations of the passive scanner to the current configuration.
     *
     * @throws IllegalStateException if no configuration was set.
     * @since 1.4.0
     * @see #setConfig(Configuration)
     * @see #saveTo(Configuration)
     */
    public void save() {
        Configuration conf = getConfig();
        if (conf == null) {
            throw new IllegalStateException("No configuration has been set.");
        }
        this.saveTo(conf);
    }

    public void saveTo(Configuration conf) {
        boolean removed = false;
        List<HierarchicalConfiguration> fields =
                ((HierarchicalConfiguration) getConfig()).configurationsAt(PSCANS_KEY);
        for (HierarchicalConfiguration sub : fields) {
            if (isPluginConfiguration(sub)) {
                sub.getRootNode().getParentNode().removeChild(sub.getRootNode());
                removed = true;
                break;
            }
        }

        boolean persistId = false;
        String entryKey = PSCANS_KEY + "(" + (removed ? fields.size() - 1 : fields.size()) + ").";

        if (getAlertThreshold() != AlertThreshold.MEDIUM) {
            conf.setProperty(entryKey + ALERT_THRESHOLD_KEY, getAlertThreshold().name());
            // For compatibility with older versions:
            conf.setProperty(entryKey + LEVEL_KEY, getAlertThreshold().name());
            persistId = true;
        }

        if (!isEnabled()) {
            conf.setProperty(entryKey + ENABLED_KEY, Boolean.FALSE);
            persistId = true;
        }

        if (persistId) {
            conf.setProperty(entryKey + ID_KEY, getPluginId());
        }
    }

    /** @deprecated (2.7.0) Replaced by {@link #getAlertThreshold()}. */
    @Override
    @Deprecated
    public AlertThreshold getLevel() {
        return getAlertThreshold();
    }

    /**
     * Gets the alert threshold of the scanner, possibly returning {@link AlertThreshold#DEFAULT}.
     *
     * @param incDefault {@code true} if the value {@link AlertThreshold#DEFAULT} can be returned,
     *     {@code false} otherwise.
     * @return the alert threshold.
     * @deprecated (2.7.0) Replaced by {@link #getAlertThreshold(boolean)}.
     */
    @Deprecated
    public AlertThreshold getLevel(boolean incDefault) {
        return getAlertThreshold(incDefault);
    }

    /**
     * Gets the alert threshold of the scanner.
     *
     * <p>If the alert threshold was set to DEFAULT it's returned the default value set.
     *
     * @return the alert threshold of the scanner.
     * @since 2.7.0
     * @see #setAlertThreshold(AlertThreshold)
     * @see #getAlertThreshold(boolean)
     */
    public AlertThreshold getAlertThreshold() {
        if (AlertThreshold.DEFAULT.equals(alertThreshold)) {
            return defaultAlertThreshold;
        }
        return alertThreshold;
    }

    /**
     * Gets the alert threshold of the scanner, possibly returning {@link AlertThreshold#DEFAULT}.
     *
     * @param incDefault {@code true} if the value {@link AlertThreshold#DEFAULT} can be returned,
     *     {@code false} otherwise.
     * @return the alert threshold.
     */
    public AlertThreshold getAlertThreshold(boolean incDefault) {
        if (!incDefault && alertThreshold == AlertThreshold.DEFAULT) {
            return defaultAlertThreshold;
        }
        return alertThreshold;
    }

    /**
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @deprecated (2.7.0) Replaced by {@link #setAlertThreshold(AlertThreshold)}.
     */
    @Override
    @Deprecated
    public void setLevel(AlertThreshold level) {
        setAlertThreshold(level);
    }

    /**
     * Sets the alert threshold of the scanner.
     *
     * @param alertThreshold the new alert threshold.
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.7.0
     * @see #getAlertThreshold()
     */
    public void setAlertThreshold(AlertThreshold alertThreshold) {
        if (alertThreshold == null) {
            throw new IllegalArgumentException("Parameter alertThreshold must not be null.");
        }
        this.alertThreshold = alertThreshold;
    }

    /**
     * Sets the alert threshold that should be returned when set to {@link AlertThreshold#DEFAULT}.
     *
     * @param level the value of default alert threshold
     * @throws IllegalArgumentException if the given parameter is {@code null} or {@code
     *     AlertThreshold.DEFAULT}.
     * @since 2.0.0
     * @deprecated (2.7.0) Replaced by {@link #setDefaultAlertThreshold(AlertThreshold)}.
     * @see #setAlertThreshold(AlertThreshold)
     */
    @Deprecated
    public void setDefaultLevel(AlertThreshold level) {
        setDefaultAlertThreshold(level);
    }

    /**
     * Sets the alert threshold that should be returned when set to {@link AlertThreshold#DEFAULT}.
     *
     * @param alertThreshold the value of default alert threshold.
     * @throws IllegalArgumentException if the given parameter is {@code null} or {@code
     *     AlertThreshold.DEFAULT}.
     * @since 2.7.0
     * @see #setDefaultAlertThreshold(AlertThreshold)
     * @see #setAlertThreshold(AlertThreshold)
     */
    public void setDefaultAlertThreshold(AlertThreshold alertThreshold) {
        if (alertThreshold == null || alertThreshold == AlertThreshold.DEFAULT) {
            throw new IllegalArgumentException(
                    "Parameter alertThreshold must not be null or DEFAULT.");
        }
        this.defaultAlertThreshold = alertThreshold;
    }

    /**
     * Returns the ID of the plug-in.
     *
     * @return the id of the plug-in.
     * @since 2.3.0
     */
    public int getPluginId() {
        return -1;
    }

    /**
     * Gets the status of the passive scanner.
     *
     * @return the status of the scanner, never {@code null}
     * @since 2.4.0
     */
    public AddOn.Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the passive scanner.
     *
     * @param status the status of the scanner
     * @throws IllegalArgumentException if the given parameter is {@code null}.
     * @since 2.4.0
     */
    public void setStatus(AddOn.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Parameter status must not be null.");
        }
        this.status = status;
    }

    public static Set<Integer> getDefaultHistoryTypes() {
        return DEFAULT_HISTORY_TYPES_SET;
    }

    @Override
    public boolean appliesToHistoryType(int historyType) {
        return getDefaultHistoryTypes().contains(historyType);
    }

    /**
     * Gets a list of example alerts that the plugin can raise.
     *
     * @since 2.10.0
     */
    @Override
    public List<Alert> getExampleAlerts() {
        return null;
    }

    /**
     * Gets a helper object to be used by scan rules in order to retrieve {@code Context}
     * information.
     *
     * @return the {@code PassiveScanData} related to the message being scanned.
     * @since 2.9.0
     */
    public PassiveScanData getHelper() {
        return passiveScanData;
    }

    private PassiveScanTaskHelper taskHelper;

    @Override
    public void setTaskHelper(PassiveScanTaskHelper helper) {
        this.taskHelper = helper;
    }

    @Override
    public PassiveScanTaskHelper getTaskHelper() {
        return this.taskHelper;
    }

    /**
     * Adds the given tag to the message being passive scanned.
     *
     * @param tag the name of the tag.
     * @since 2.11.0
     * @deprecated (2.12.0) Replaced by {@link #addHistoryTag(String)}.
     */
    @Deprecated
    protected void addTag(String tag) {
        addHistoryTag(tag);
    }

    /**
     * Adds the given tag to the message being passive scanned.
     *
     * @param tag the name of the tag.
     * @since 2.12.0
     */
    protected void addHistoryTag(String tag) {
        this.taskHelper.addHistoryTag(passiveScanData.getMessage().getHistoryRef(), tag);
    }

    /**
     * Gets the tags attached to the alerts raised by this plugin. Can be overridden by scan rules
     * to return the associated alert tags.
     *
     * @return the alert tags
     * @since 2.11.0
     */
    public Map<String, String> getAlertTags() {
        return null;
    }

    /**
     * Gets the name of the scan rule, falling back to the simple name of the class as last resort.
     *
     * @return a name representing the scan rule.
     * @since 2.12.0
     */
    public final String getDisplayName() {
        return StringUtils.isBlank(this.getName())
                ? this.getClass().getSimpleName()
                : this.getName();
    }

    /**
     * Make a copy of this instance including all of the configuration.
     *
     * @return a copy of this instance
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @since 2.12.0
     */
    public PluginPassiveScanner copy()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, NoSuchMethodException, SecurityException {
        PluginPassiveScanner pps = this.getClass().getConstructor().newInstance();
        Configuration conf = this.getConfig();
        if (conf == null) {
            throw new IllegalArgumentException(
                    "Cannot copy "
                            + this.getClass().getCanonicalName()
                            + " : "
                            + this.getName()
                            + " as the configuration is null");
        }
        pps.setConfig(conf);
        return pps;
    }

    /**
     * Returns a new alert builder.
     *
     * <p>By default the alert builder sets the following fields of the alert:
     *
     * <ul>
     *   <li>Plugin ID - using {@link #getPluginId()}
     *   <li>Name - using {@link #getName()}
     *   <li>Message - the message being scanned
     *   <li>URI - from the alert message
     *   <li>Alert Tags - using {@link #getAlertTags()}
     * </ul>
     *
     * @return the alert builder.
     * @since 2.9.0
     */
    protected AlertBuilder newAlert() {
        return new AlertBuilder(this, passiveScanData.getMessage());
    }

    /**
     * An alert builder to fluently build and {@link #raise() raise alerts}.
     *
     * @since 2.9.0
     */
    public static final class AlertBuilder extends Alert.Builder {

        private final PluginPassiveScanner plugin;
        private final HttpMessage message;

        private AlertBuilder(PluginPassiveScanner plugin, HttpMessage message) {
            this.plugin = plugin;
            this.message = message;

            setPluginId(plugin.getPluginId());
            setName(plugin.getName());
            setMessage(message);
            setTags(plugin.getAlertTags());
        }

        @Override
        public AlertBuilder setAlertId(int alertId) {
            super.setAlertId(alertId);
            return this;
        }

        @Override
        public AlertBuilder setPluginId(int pluginId) {
            super.setPluginId(pluginId);
            return this;
        }

        @Override
        public AlertBuilder setName(String name) {
            super.setName(name);
            return this;
        }

        @Override
        public AlertBuilder setRisk(int risk) {
            super.setRisk(risk);
            return this;
        }

        @Override
        public AlertBuilder setConfidence(int confidence) {
            super.setConfidence(confidence);
            return this;
        }

        @Override
        public AlertBuilder setDescription(String description) {
            super.setDescription(description);
            return this;
        }

        @Override
        public AlertBuilder setUri(String uri) {
            super.setUri(uri);
            return this;
        }

        @Override
        public AlertBuilder setParam(String param) {
            super.setParam(param);
            return this;
        }

        /**
         * @throws IllegalStateException always, passive scanners should not set the attack field.
         */
        @Override
        public AlertBuilder setAttack(String attack) {
            throw new IllegalStateException("Passive alerts should not have an attack.");
        }

        @Override
        public AlertBuilder setOtherInfo(String otherInfo) {
            super.setOtherInfo(otherInfo);
            return this;
        }

        @Override
        public AlertBuilder setSolution(String solution) {
            super.setSolution(solution);
            return this;
        }

        @Override
        public AlertBuilder setReference(String reference) {
            super.setReference(reference);
            return this;
        }

        @Override
        public AlertBuilder setEvidence(String evidence) {
            super.setEvidence(evidence);
            return this;
        }

        @Override
        public AlertBuilder setInputVector(String inputVector) {
            super.setInputVector(inputVector);
            return this;
        }

        @Override
        public AlertBuilder setCweId(int cweId) {
            super.setCweId(cweId);
            return this;
        }

        @Override
        public AlertBuilder setWascId(int wascId) {
            super.setWascId(wascId);
            return this;
        }

        @Override
        public AlertBuilder setMessage(HttpMessage message) {
            super.setMessage(message);
            return this;
        }

        @Override
        public AlertBuilder setSourceHistoryId(int sourceHistoryId) {
            super.setSourceHistoryId(sourceHistoryId);
            return this;
        }

        @Override
        public AlertBuilder setHistoryRef(HistoryReference historyRef) {
            super.setHistoryRef(historyRef);
            return this;
        }

        @Override
        public AlertBuilder setSource(Source source) {
            super.setSource(source);
            return this;
        }

        @Override
        public AlertBuilder setAlertRef(String alertRef) {
            super.setAlertRef(alertRef);
            return this;
        }

        @Override
        public AlertBuilder setTags(Map<String, String> tags) {
            super.setTags(tags);
            return this;
        }

        @Override
        public AlertBuilder addTag(String tag) {
            super.addTag(tag, "");
            return this;
        }

        @Override
        public AlertBuilder addTag(String tag, String value) {
            super.addTag(tag, value);
            return this;
        }

        @Override
        public AlertBuilder removeTag(String tag) {
            super.removeTag(tag);
            return this;
        }

        @Override
        public AlertBuilder removeTag(String tag, String value) {
            super.removeTag(tag, value);
            return this;
        }

        /** Raises the alert with specified data. */
        public void raise() {
            plugin.taskHelper.raiseAlert(message.getHistoryRef(), build());
            Stats.incCounter("stats.pscan." + plugin.getPluginId() + ".alerts");
        }
    }
}
