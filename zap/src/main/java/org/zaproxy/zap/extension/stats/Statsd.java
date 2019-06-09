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

import java.io.IOException;
import java.net.UnknownHostException;
import org.zaproxy.zap.utils.StatsListener;

public class Statsd implements StatsListener {

    private static final String GLOBAL_KEY = "global";

    private StatsdClient statsd2;

    private String host;
    private int port;
    private String prefix;
    private String separator = ".";

    public Statsd(String host, int port, String prefix) throws UnknownHostException, IOException {
        this.host = host;
        this.port = port;
        this.prefix = prefix;
        statsd2 = new StatsdClient(host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    private String getFullKey(String key) {
        return this.getFullKey(null, key);
    }

    private String getFullKey(String site, String key) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
            sb.append(separator);
        }
        if (site != null) {
            // Remove http(s)://
            sb.append(site.substring(site.indexOf(':') + 3));
        } else {
            sb.append(GLOBAL_KEY);
        }
        sb.append(separator);
        sb.append(key);
        // Colon is a statsd separator
        return sb.toString().replace(":", "-");
    }

    @Override
    public void counterInc(String key) {
        statsd2.increment(getFullKey(key));
    }

    @Override
    public void counterInc(String site, String key) {
        statsd2.increment(getFullKey(site, key));
    }

    @Override
    public void counterInc(String key, long inc) {
        statsd2.increment(getFullKey(key), (int) inc);
    }

    @Override
    public void counterInc(String site, String key, long inc) {
        statsd2.increment(getFullKey(site, key), (int) inc);
    }

    @Override
    public void counterDec(String key) {
        statsd2.decrement(getFullKey(key));
    }

    @Override
    public void counterDec(String site, String key) {
        statsd2.decrement(getFullKey(site, key));
    }

    @Override
    public void counterDec(String key, long dec) {
        statsd2.decrement(getFullKey(key), (int) dec);
    }

    @Override
    public void counterDec(String site, String key, long dec) {
        statsd2.decrement(getFullKey(site, key), (int) dec);
    }

    @Override
    public void highwaterMarkSet(String key, long value) {
        statsd2.gauge(getFullKey(key), value);
    }

    @Override
    public void highwaterMarkSet(String site, String key, long value) {
        statsd2.gauge(getFullKey(site, key), value);
    }

    @Override
    public void lowwaterMarkSet(String key, long value) {
        statsd2.gauge(getFullKey(key), value);
    }

    @Override
    public void lowwaterMarkSet(String site, String key, long value) {
        statsd2.gauge(getFullKey(site, key), value);
    }

    @Override
    public void allCleared() {
        // No supported
    }

    @Override
    public void allCleared(String site) {
        // No supported
    }

    @Override
    public void cleared(String keyPrefix) {
        // No supported
    }

    @Override
    public void cleared(String site, String keyPrefix) {
        // No supported
    }
}
