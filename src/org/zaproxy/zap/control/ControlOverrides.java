/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
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

package org.zaproxy.zap.control;

import java.util.Hashtable;

/**
 * Used to override options in the configuration file.
 * @author psiinon
 *
 */
public class ControlOverrides {
	private int proxyPort = -1;
	private String proxyHost = null;
	private Hashtable<String, String> configs = new Hashtable<String, String>();
	private boolean experimentalDb = false;

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyHost() {
		if (proxyHost != null && proxyHost.length() == 0) {
			// Treat an empty string as the 'all interfaces' address (like the UI does)
			return "0.0.0.0";
		}
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Hashtable<String, String> getConfigs() {
		return configs;
	}

	public void setConfigs(Hashtable<String, String> configs) {
		this.configs = configs;
	}

	public boolean isExperimentalDb() {
		return experimentalDb;
	}

	public void setExperimentalDb(boolean experimentalDb) {
		this.experimentalDb = experimentalDb;
	}
	
}
