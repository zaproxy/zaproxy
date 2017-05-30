/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.pscan;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.Enableable;


public abstract class PluginPassiveScanner extends Enableable implements PassiveScanner {

	/**
	 * The (base) configuration key used to saved the configurations of a passive scanner, ID, alert threshold and enabled
	 * state.
	 */
	private static final String PSCANS_KEY = PassiveScanParam.PASSIVE_SCANS_BASE_KEY + ".pscanner";

	/**
	 * The configuration key used to save/load the ID of a passive scanner.
	 */
	private static final String ID_KEY = "id";

	/**
	 * The configuration key used to load the classname of a passive scanner, used only for backwards compatibility.
	 */
	private static final String CLASSNAME_KEY = "classname";

	/**
	 * The configuration key used to save/load the alert threshold of a passive scanner.
	 * <p>
	 * To be replaced by {@link #ALERT_THRESHOLD_KEY}.
	 */
	private static final String LEVEL_KEY = "level";

	/**
	 * The configuration key used to save/load the alert threshold of a passive scanner.
	 */
	private static final String ALERT_THRESHOLD_KEY = "alertthreshold";

	/**
	 * The configuration key used to save/load the enabled state of a passive scanner.
	 */
	private static final String ENABLED_KEY = "enabled";

	private static final Integer[] DEFAULT_HISTORY_TYPES = new Integer[] {
		HistoryReference.TYPE_PROXIED, HistoryReference.TYPE_ZAP_USER, 
		HistoryReference.TYPE_SPIDER,  HistoryReference.TYPE_SPIDER_AJAX};

	private static final Set<Integer> DEFAULT_HISTORY_TYPES_SET = 
			Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(DEFAULT_HISTORY_TYPES)));

	private AlertThreshold alertThreshold = AlertThreshold.DEFAULT;
	private AlertThreshold defaultAlertThreshold = AlertThreshold.MEDIUM;
	private Configuration config = null;
	private AddOn.Status status = AddOn.Status.unknown;

	public PluginPassiveScanner() {
		super(true);
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
		List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(PSCANS_KEY);
		for (HierarchicalConfiguration sub : fields) {
			if (isPluginConfiguration(sub)) {
				// For compatibility with older versions:
				String alertThresholdName = sub.getString(LEVEL_KEY, null);
				if (alertThresholdName == null) {
					alertThresholdName = sub.getString(ALERT_THRESHOLD_KEY, AlertThreshold.DEFAULT.name());
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
	 * @return {@code true} if the configuration belongs to this passive scanner, {@code false} otherwise
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
		List<HierarchicalConfiguration> fields = ((HierarchicalConfiguration) getConfig()).configurationsAt(PSCANS_KEY);
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

	/**
	 * @deprecated (TODO add version) Replaced by {@link #getAlertThreshold()}.
	 */
	@Override
	@Deprecated
	public AlertThreshold getLevel() {
		return getAlertThreshold();
	}
	
	/**
	 * Gets the alert threshold of the scanner, possibly returning {@link AlertThreshold#DEFAULT}.
	 * 
	 * @param incDefault {@code true} if the value {@link AlertThreshold#DEFAULT} can be returned, {@code false} otherwise.
	 * @return the alert threshold.
	 * @deprecated (TODO add version) Replaced by {@link #getAlertThreshold(boolean)}.
	 */
	@Deprecated
	public AlertThreshold getLevel(boolean incDefault) {
		return getAlertThreshold(incDefault);
	}
	
	/**
	 * Gets the alert threshold of the scanner.
	 * <p>
	 * If the alert threshold was set to DEFAULT it's returned the default value set.
	 *
	 * @return the alert threshold of the scanner.
	 * @since TODO add version
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
	 * @param incDefault {@code true} if the value {@link AlertThreshold#DEFAULT} can be returned, {@code false} otherwise.
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
	 * @deprecated (TODO add version) Replaced by {@link #setAlertThreshold(AlertThreshold)}.
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
	 * @since TODO add version
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
	 * @throws IllegalArgumentException if the given parameter is {@code null} or {@code AlertThreshold.DEFAULT}.
	 * @since 2.0.0
	 * @deprecated (TODO add version) Replaced by {@link #setDefaultAlertThreshold(AlertThreshold)}.
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
	 * @throws IllegalArgumentException if the given parameter is {@code null} or {@code AlertThreshold.DEFAULT}.
	 * @since TODO add version
	 * @see #setDefaultAlertThreshold(AlertThreshold)
	 * @see #setAlertThreshold(AlertThreshold)
	 */
	public void setDefaultAlertThreshold(AlertThreshold alertThreshold) {
		if (alertThreshold == null || alertThreshold == AlertThreshold.DEFAULT) {
			throw new IllegalArgumentException("Parameter alertThreshold must not be null or DEFAULT.");
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
	
}
