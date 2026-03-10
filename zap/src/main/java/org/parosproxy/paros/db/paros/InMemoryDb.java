package org.parosproxy.paros.db.paros;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

class InMemoryDb<T> {

    private final ConcurrentHashMap<Integer, T> map = new ConcurrentHashMap<>();

    public void put(Integer id, T value) {
        map.put(id, value);
    }

    public T get(Integer id) {
        return map.get(id);
    }

    public T remove(Integer id) {
        return map.remove(id);
    }

    public void remove(Predicate<T> p) {
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            if (p.test(entry.getValue())) {
                map.remove(entry.getKey());
            }
        }
    }

    public void collect(BiConsumer<Integer, T> c) {
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            c.accept(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        map.clear();
    }
}
