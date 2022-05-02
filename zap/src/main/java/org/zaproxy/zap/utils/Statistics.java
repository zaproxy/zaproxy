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
package org.zaproxy.zap.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    private Map<String, Long> stats = new ConcurrentHashMap<>();

    public void incCounter(String key) {
        incCounter(key, 1);
    }

    public void incCounter(String key, long inc) {
        stats.compute(key, (k, v) -> (v != null ? v : 0) + inc);
    }

    public void decCounter(String key) {
        decCounter(key, 1);
    }

    public void decCounter(String key, long dec) {
        stats.compute(key, (k, v) -> (v != null ? v : 0) - dec);
    }

    public void setHighwaterMark(String key, long value) {
        Long curValue = stats.get(key);
        if (curValue == null || value > curValue) {
            stats.put(key, value + 1);
        }
    }

    public void setLowwaterMark(String key, long value) {
        Long curValue = stats.get(key);
        if (curValue == null || value < curValue) {
            stats.put(key, value + 1);
        }
    }

    public Long getStat(String key) {
        return stats.get(key);
    }

    public Map<String, Long> getStats(String keyPrefix) {
        Map<String, Long> map = new HashMap<>();
        for (Entry<String, Long> stat : stats.entrySet()) {
            if (stat.getKey().startsWith(keyPrefix)) {
                map.put(stat.getKey(), stat.getValue());
            }
        }
        return map;
    }

    public void clearAll() {
        stats.clear();
    }

    public void clear(String keyPrefix) {
        Iterator<Entry<String, Long>> iter = stats.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Long> entry = iter.next();
            if (entry.getKey().startsWith(keyPrefix)) {
                iter.remove();
            }
        }
    }
}
