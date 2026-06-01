/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.parosproxy.paros.db.paros;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

class InMemoryDb<K,V> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public int size() {
        return map.size();
    }

    public void put(K id, V value) {
        map.put(id, value);
    }

    public V get(K id) {
        return map.get(id);
    }

    public V remove(K id) {
        return map.remove(id);
    }

    public void remove(Predicate<V> p) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (p.test(entry.getValue())) {
                map.remove(entry.getKey());
            }
        }
    }

    public void collect(BiConsumer<K, V> c) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            c.accept(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        map.clear();
    }
}
