/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.control;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to override options in the configuration file.
 *
 * @author psiinon
 */
public class ControlOverrides {
    private int proxyPort = -1;
    private String proxyHost = null;
    private Map<String, String> configs = new LinkedHashMap<>();
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

    /**
     * Gets the {@code config} command line arguments, in the order they were specified.
     *
     * @return the {@code config} command line arguments.
     * @since 2.6.0
     */
    public Map<String, String> getOrderedConfigs() {
        return configs;
    }

    /**
     * Sets the {@code config} command line arguments, in the order they were specified.
     *
     * @param configs the {@code config} command line arguments.
     * @since 2.6.0
     */
    public void setOrderedConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public boolean isExperimentalDb() {
        return experimentalDb;
    }

    public void setExperimentalDb(boolean experimentalDb) {
        this.experimentalDb = experimentalDb;
    }

    /**
     * Gets the IDs of the mandatory add-ons, needed for ZAP to work properly.
     *
     * @return a list with the IDs of the mandatory add-ons.
     * @since 2.12.0
     */
    public List<String> getMandatoryAddOns() {
        return Arrays.asList("callhome", "network");
    }
}
