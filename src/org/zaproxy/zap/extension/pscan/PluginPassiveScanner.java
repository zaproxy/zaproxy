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

import org.apache.commons.configuration.Configuration;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.zaproxy.zap.utils.Enableable;


public abstract class PluginPassiveScanner extends Enableable implements PassiveScanner {

	private AlertThreshold level = AlertThreshold.DEFAULT;
	private AlertThreshold defaultLevel = AlertThreshold.MEDIUM;
	private Configuration config = null;

	public void setConfig(Configuration config) {
	    this.config = config;
	    this.loadFrom(config);
	}

	public void loadFrom(Configuration conf) {
		this.setEnabled(
				conf.getBoolean("pscans." + getClass().getCanonicalName() + ".enabled", true));
		this.setLevel(AlertThreshold.valueOf(
				conf.getString("pscans." + getClass().getCanonicalName() + ".level", AlertThreshold.DEFAULT.name())));
	}

	public Configuration getConfig() {
	    return config;
	}
	
	public void save() {
		this.saveTo(getConfig());
	}

	public void saveTo(Configuration conf) {
		conf.setProperty("pscans." + getClass().getCanonicalName() + ".enabled", this.isEnabled());
		conf.setProperty("pscans." + getClass().getCanonicalName() + ".level", this.getLevel(true).name());
	}

	@Override
	public AlertThreshold getLevel() {
		if (AlertThreshold.DEFAULT.equals(level)) {
			return defaultLevel;
		}
		return level;
	}
	
	public AlertThreshold getLevel(boolean incDefault) {
		return level;
	}
	
	@Override
	public void setLevel(AlertThreshold level) {
		this.level = level;
	}

	public void setDefaultLevel(AlertThreshold level) {
		this.defaultLevel = level;
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
}
