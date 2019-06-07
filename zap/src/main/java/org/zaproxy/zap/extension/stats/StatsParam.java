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
package org.zaproxy.zap.extension.stats;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the stats configurations saved in the configuration file.
 *
 * @since 2.5.0
 */
public class StatsParam extends AbstractParam {

    /** The base configuration key for all stats configurations. */
    private static final String STATS_BASE_KEY = "stats";

    /** The configuration key used to configure if in memory stats should be used. */
    private static final String IN_MEMORY_STATS_KEY = STATS_BASE_KEY + ".inmemory";
    /** The configuration key used for the statsd host name - if empty statsd wont be used. */
    private static final String STATSD_HOST_KEY = STATS_BASE_KEY + ".statsd.host";
    /**
     * The configuration key used for the statsd port - if empty will default to
     * DEFAULT_STATSD_PORT.
     */
    private static final String STATSD_PORT_KEY = STATS_BASE_KEY + ".statsd.port";
    /** The configuration key used for the statsd prefix. */
    private static final String STATSD_PREFIX_KEY = STATS_BASE_KEY + ".statsd.prefix";

    /** The default statsd port. */
    private static final int DEFAULT_STATSD_PORT = 8125;
    /** The default statsd prefix. */
    private static final String DEFAULT_STATSD_PREFIX = "zap";

    private boolean inMemory = true;
    private String statsdHost = "";
    private int statsdPort = DEFAULT_STATSD_PORT;
    private String statsdPrefix = DEFAULT_STATSD_PREFIX;

    public boolean isInMemoryEnabled() {
        return inMemory;
    }

    public void setInMemoryEnabled(boolean inMemory) {
        this.inMemory = inMemory;
        getConfig().setProperty(IN_MEMORY_STATS_KEY, inMemory);
    }

    public boolean isStatsdEnabled() {
        return statsdHost != null && statsdHost.length() > 0;
    }

    public String getStatsdHost() {
        return statsdHost;
    }

    public void setStatsdHost(String statsdHost) {
        this.statsdHost = statsdHost;
        getConfig().setProperty(STATSD_HOST_KEY, statsdHost);
    }

    public int getStatsdPort() {
        return statsdPort;
    }

    public void setStatsdPort(int statsdPort) {
        this.statsdPort = statsdPort;
        getConfig().setProperty(STATSD_PORT_KEY, statsdPort);
    }

    public String getStatsdPrefix() {
        return statsdPrefix;
    }

    public void setStatsdPrefix(String statsdPrefix) {
        this.statsdPrefix = statsdPrefix;
        getConfig().setProperty(STATSD_PREFIX_KEY, statsdPrefix);
    }

    @Override
    protected void parse() {
        inMemory = getBoolean(IN_MEMORY_STATS_KEY, true);
        statsdHost = getString(STATSD_HOST_KEY, "");
        statsdPort = getInt(STATSD_PORT_KEY, DEFAULT_STATSD_PORT);
        statsdPrefix = getString(STATSD_PREFIX_KEY, DEFAULT_STATSD_PREFIX);
    }
}
