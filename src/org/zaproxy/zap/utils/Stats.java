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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Stats {

	private static Statistics stats = new Statistics();
	private static Map<String, Statistics> siteStats = new HashMap<String, Statistics>();
	
	private static Statistics getStatistics(String site) {
		if (site == null) {
			// Its a global stat
			return stats;
		}
		if (! siteStats.containsKey(site)) {
			synchronized (siteStats) {
				if (! siteStats.containsKey(site)) {
					siteStats.put(site, new Statistics());
				}
			}
		}
		return siteStats.get(site);
	}
	
	public static void incCounter(String key) {
		incCounter (null, key);
	}
	
	public static void incCounter(String site, String key) {
		getStatistics(site).incCounter(key);
	}

	public static void incCounter(String key, long inc) {
		incCounter(null, key, inc);
	}

	public static void incCounter(String site, String key, long inc) {
		getStatistics(site).incCounter(key, inc);
	}

	public static void decCounter(String key) {
		decCounter(null, key);
	}
	
	public static void decCounter(String site, String key) {
		getStatistics(site).decCounter(key);
	}
	
	public static void decCounter(String key, long dec) {
		decCounter(null, key, dec);
	}
	
	public static void decCounter(String site, String key, long dec) {
		getStatistics(site).decCounter(key, dec);
	}
	
	public static void setHighwaterMark(String key, long value) {
	}
	
	public static void setHighwaterMark(String site, String key, long value) {
		getStatistics(site).setHighwaterMark(key, value);
	}
	
	public static void setLowwaterMark(String key, long value) {
	}
	
	public static void setLowwaterMark(String site, String key, long value) {
		getStatistics(site).setLowwaterMark(key, value);
	}
	
	public static Long getStat(String key) {
		return stats.getStat(key);
	}
	
	public static Long getStat(String site, String key) {
		if (site == null || siteStats.containsKey(site)) {
			return getStatistics(site).getStat(key);
		}
		return null;
	}
	
	public static Map<String, Long> getStats(String keyPrefix) {
		return stats.getStats(keyPrefix);
	}
	
	public static Map<String, Map<String, Long>> getAllSiteStats(String keyPrefix) {
		Map<String, Map<String, Long>> allStats = new HashMap<String, Map<String, Long>>();
		for (Entry<String, Statistics> st : siteStats.entrySet()) {
			allStats.put(st.getKey(), st.getValue().getStats(keyPrefix));
		}
		return allStats;
	}
	
	public static Map<String, Long> getSiteStats(String site, String keyPrefix) {
		if (siteStats.containsKey(site)) {
			return getStatistics(site).getStats(keyPrefix);
		}
		return new HashMap<String, Long>();
	}
	
	public static List<String> getSites() {
		List<String> sites = new ArrayList<String>(siteStats.keySet());
		Collections.sort(sites);
		return sites;
	}
	
	public static void clearAll() {
		stats.clearAll();
		for (Statistics st : siteStats.values()) {
			st.clearAll();
		}
		siteStats.clear();
	}

	public static void clearAll(String site) {
		getStatistics(site).clearAll();
	}

	public static void clear(String keyPrefix) {
		stats.clear(keyPrefix);
		for (Statistics st : siteStats.values()) {
			st.clear(keyPrefix);
		}
	}
	
	public static void clear(String site, String keyPrefix) {
		getStatistics(site).clear(keyPrefix);
	}
}
