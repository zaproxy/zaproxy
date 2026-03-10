package org.parosproxy.paros.db.paros;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

class InMemoryDb<K,V> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

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
