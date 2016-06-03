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
import java.util.List;

import org.apache.log4j.Logger;

public final class Stats {

	private static final List<StatsListener> listeners = new ArrayList<StatsListener>();

    private static final Logger logger = Logger.getLogger(Stats.class);
    
    private Stats() {
    }

	public static void incCounter(String key) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterInc(key);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void incCounter(String site, String key) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterInc(site, key);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void incCounter(String key, long inc) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterInc(key, inc);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void incCounter(String site, String key, long inc) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterInc(site, key, inc);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void decCounter(String key) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterDec(key);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void decCounter(String site, String key) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterDec(site, key);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void decCounter(String key, long dec) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterDec(key, dec);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void decCounter(String site, String key, long dec) {
		for (StatsListener listener : listeners) {
			try {
				listener.counterDec(site, key, dec);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void setHighwaterMark(String key, long value) {
		for (StatsListener listener : listeners) {
			try {
				listener.highwaterMarkSet(key, value);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void setHighwaterMark(String site, String key, long value) {
		for (StatsListener listener : listeners) {
			try {
				listener.highwaterMarkSet(site, key, value);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void setLowwaterMark(String key, long value) {
		for (StatsListener listener : listeners) {
			try {
				listener.lowwaterMarkSet(key, value);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void setLowwaterMark(String site, String key, long value) {
		for (StatsListener listener : listeners) {
			try {
				listener.lowwaterMarkSet(site, key, value);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void clearAll() {
		for (StatsListener listener : listeners) {
			try {
				listener.allCleared();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void clearAll(String site) {
		for (StatsListener listener : listeners) {
			try {
				listener.allCleared(site);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static void clear(String keyPrefix) {
		for (StatsListener listener : listeners) {
			try {
				listener.cleared(keyPrefix);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void clear(String site, String keyPrefix) {
		for (StatsListener listener : listeners) {
			try {
				listener.cleared(site, keyPrefix);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static void addListener(StatsListener listener) {
		listeners.add(listener);
	}
	
	public static void removeListener(StatsListener listener) {
		listeners.remove(listener);
	}
}
