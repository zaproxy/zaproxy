/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP development team
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
package org.zaproxy.zap.extension.params;

import java.util.HashSet;
import java.util.Set;

import org.parosproxy.paros.network.HtmlParameter;

public class HtmlParameterStats implements Comparable<HtmlParameterStats> {
	private long id = -1;
	private String site;
	private String name;
	private HtmlParameter.Type type;
	private int timesUsed = 0;
	private Set<String> flags = new HashSet<>();
	private Set<String> values = new HashSet<>();
	
	public HtmlParameterStats(String site, String name, HtmlParameter.Type type, String value, Set<String> flags) {
		this.site = site;
		this.name = name;
		this.type = type;
		this.addValue(value);
		this.flags = flags;
		this.incTimesUsed();
	}
	
	public HtmlParameterStats(long id, String site, String name, String type, int timesUsed, Set<String> values, Set<String> flags) {
		this.id = id;
		this.site = site;
		this.name = name;
		this.type = HtmlParameter.Type.valueOf(type);
		this.timesUsed = timesUsed;
		this.values = values;
		this.flags = flags;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSite() {
		return site;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HtmlParameter.Type getType() {
		return type;
	}
	public void setType(HtmlParameter.Type type) {
		this.type = type;
	}
	public int getTimesUsed() {
		return timesUsed;
	}
	public void incTimesUsed() {
		this.timesUsed++;
	}
	public Set<String> getValues() {
		return values;
	}
	public void addValue(String value) {
		if (value == null) {
			value = "";
		}
		this.values.add(value);
	}
	
	public String getValuesSummary() {
		StringBuilder sb = new StringBuilder();
		for (String value: values) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(value);
			if (sb.length() > 250) {
				break;
			}
		}
		return sb.toString();
	}
	public Set<String> getFlags() {
		return flags;
	}
	public void addFlag(String flag) {
		this.flags.add(flag);
	}
	
	public String getAllFlags() {
		StringBuilder sb = new StringBuilder();
		for (String flag: flags) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(flag);
			if (sb.length() > 250) {
				break;
			}
		}
		
		return sb.toString();
	}

	@Override
	public int compareTo(HtmlParameterStats o) {
		if (o == null) { 
			return 1;
		}
		int result = this.type.ordinal() - o.getType().ordinal();
		if (result == 0) {
			// Same type
			result = this.name.compareTo(o.getName());
		}
		return result;
	}

	public void removeFlag(String flag) {
		this.flags.remove(flag);
	}
}
