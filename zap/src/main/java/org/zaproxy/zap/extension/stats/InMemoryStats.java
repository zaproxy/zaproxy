/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 ZAP development team
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
package org.zaproxy.zap.extension.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zaproxy.zap.utils.Statistics;
import org.zaproxy.zap.utils.StatsListener;

public class InMemoryStats implements StatsListener {

	private Statistics stats = new Statistics();
	private Map<String, Statistics> siteStats = new HashMap<String, Statistics>();

	private Statistics getStatistics(String site) {
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
	

	@Override
	public void counterInc(String key) {
		counterInc (null, key);
	}

	@Override
	public void counterInc(String site, String key) {
		getStatistics(site).incCounter(key);
	}

	@Override
	public void counterInc(String key, long inc) {
		counterInc(null, key, inc);
	}

	@Override
	public void counterInc(String site, String key, long inc) {
		getStatistics(site).incCounter(key, inc);
	}

	@Override
	public void counterDec(String key) {
		counterDec(null, key);
	}

	@Override
	public void counterDec(String site, String key) {
		getStatistics(site).decCounter(key);
	}

	@Override
	public void counterDec(String key, long dec) {
		counterDec(null, key, dec);
	}

	@Override
	public void counterDec(String site, String key, long dec) {
		getStatistics(site).decCounter(key, dec);
	}

	@Override
	public void highwaterMarkSet(String key, long value) {
		highwaterMarkSet(null, key, value);
	}

	@Override
	public void highwaterMarkSet(String site, String key, long value) {
		getStatistics(site).setHighwaterMark(key, value);
	}

	@Override
	public void lowwaterMarkSet(String key, long value) {
		lowwaterMarkSet(null, key, value);
	}

	@Override
	public void lowwaterMarkSet(String site, String key, long value) {
		getStatistics(site).setLowwaterMark(key, value);
	}

	@Override
	public void allCleared() {
		stats.clearAll();
		for (Statistics st : siteStats.values()) {
			st.clearAll();
		}
		siteStats.clear();
	}

	@Override
	public void allCleared(String site) {
		getStatistics(site).clearAll();
	}

	@Override
	public void cleared(String keyPrefix) {
		stats.clear(keyPrefix);
		for (Statistics st : siteStats.values()) {
			st.clear(keyPrefix);
		}
	}

	@Override
	public void cleared(String site, String keyPrefix) {
		getStatistics(site).clear(keyPrefix);
	}

	public Long getStat(String key) {
		return stats.getStat(key);
	}
	
	public Long getStat(String site, String key) {
		if (site == null || siteStats.containsKey(site)) {
			return getStatistics(site).getStat(key);
		}
		return null;
	}
	
	public Map<String, Long> getStats(String keyPrefix) {
		return stats.getStats(keyPrefix);
	}
	
	public Map<String, Map<String, Long>> getAllSiteStats(String keyPrefix) {
		Map<String, Map<String, Long>> allStats = new HashMap<String, Map<String, Long>>();
		for (Entry<String, Statistics> st : siteStats.entrySet()) {
			allStats.put(st.getKey(), st.getValue().getStats(keyPrefix));
		}
		return allStats;
	}
	
	public Map<String, Long> getSiteStats(String site, String keyPrefix) {
		if (siteStats.containsKey(site)) {
			return getStatistics(site).getStats(keyPrefix);
		}
		return new HashMap<String, Long>();
	}
	
	public List<String> getSites() {
		List<String> sites = new ArrayList<String>(siteStats.keySet());
		Collections.sort(sites);
		return sites;
	}

}
