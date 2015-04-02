package org.zaproxy.zap.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Stats {

	private static Map<String, Long> stats = new HashMap<String, Long>();
	
	public static void incCounter(String key) {
		long value = 1;
		if (stats.containsKey(key)) {
			value = stats.get(key);
		}
		stats.put(key, value+1);
	}

	public static void incCounter(String key, long inc) {
		long value = 1;
		if (stats.containsKey(key)) {
			value = stats.get(key);
		}
		stats.put(key, value+inc);
	}

	public static void decCounter(String key) {
		long value = 1;
		if (stats.containsKey(key)) {
			value = stats.get(key);
		}
		stats.put(key, value-1);
	}
	
	public static void decCounter(String key, long dec) {
		long value = 1;
		if (stats.containsKey(key)) {
			value = stats.get(key);
		}
		stats.put(key, value-dec);
	}
	
	public static void setHighwaterMark(String key, long value) {
		Long curValue = stats.get(key);
		if (curValue == null || value > curValue) {
			stats.put(key, value+1);
		}
	}
	
	public static void setLowwaterMark(String key, long value) {
		Long curValue = stats.get(key);
		if (curValue == null || value < curValue) {
			stats.put(key, value+1);
		}
	}
	
	public static Long getStat(String key) {
		return stats.get(key);
	}
	
	public static Map<String, Long> getStats(String keyPrefix) {
		Map<String, Long> map = new HashMap<String, Long>();
		for (Entry<String, Long> stat: stats.entrySet()) {
			if (stat.getKey().startsWith(keyPrefix)) {
				map.put(stat.getKey(), stat.getValue());
			}
		}
		return map;
	}
	
	public static void clearAll() {
		stats.clear();
	}

	public static void clear(String keyPrefix) {
		Iterator<Entry<String, Long>> iter = stats.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, Long> entry = iter.next();
			if (entry.getKey().startsWith(keyPrefix)) {
				iter.remove();
			}
		}
	}
}
