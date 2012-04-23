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
import org.parosproxy.paros.core.scanner.Plugin.Level;


public abstract class PluginPassiveScanner implements PassiveScanner {

	private boolean enabled = true;
	private Level level = Level.DEFAULT;
	private Level defaultLevel = Level.MEDIUM;
	private Configuration config = null;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setConfig(Configuration config) {
	    this.config = config;
		this.setEnabled(config.getBoolean("pscans." + getClass().getCanonicalName() + ".enabled", true));
	}
	
	public Configuration getConfig() {
	    return config;
	}
	
	public void save() {
		this.getConfig().setProperty("pscans." + getClass().getCanonicalName() + ".enabled", this.isEnabled());
	}

	@Override
	public Level getLevel() {
		if (Level.DEFAULT.equals(level)) {
			return defaultLevel;
		}
		return level;
	}
	
	public Level getLevel(boolean incDefault) {
		return level;
	}
	
	@Override
	public void setLevel(Level level) {
		this.level = level;
	}

	public void setDefaultLevel(Level level) {
		this.defaultLevel = level;
	}
}
