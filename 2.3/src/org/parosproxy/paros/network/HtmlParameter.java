/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
 * 
 * ZAP - 2013/09/09: Cleanup and lazy initialization of flags (speed-up)
 */
package org.parosproxy.paros.network;

import java.util.HashSet;
import java.util.Set;

public class HtmlParameter implements Comparable<HtmlParameter> {
	public enum Type {
		cookie, form, url
	};

	public enum Flags {
		anticsrf, session, structural
	};

	private String name;
	private String value;
	private Type type;
	private Set<String> flags;

	public HtmlParameter(Type type, String name, String value) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public HtmlParameter(String cookieLine) {
		super();
		String[] array = cookieLine.split(";");
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException(cookieLine);
		}
		int eqOffset = array[0].indexOf("=");
		if (eqOffset <= 0) {
			throw new IllegalArgumentException(cookieLine);
		}
		this.type = Type.cookie;
		this.name = array[0].substring(0, eqOffset).trim();
		this.value = array[0].substring(eqOffset + 1).trim();
		if (array.length > 1) {
			for (int i = 1; i < array.length; i++) {
				this.addFlag(array[i].trim());
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Set<String> getFlags() {
		if (this.flags == null)
			this.flags = new HashSet<>();
		return this.flags;
	}

	public void addFlag(String flag) {
		this.getFlags().add(flag);
	}

	@Override
	public int compareTo(HtmlParameter o) {
		if (o == null) {
			return 1;
		}

		int result = this.type.ordinal() - o.getType().ordinal();
		if (result == 0) {
			// Same type
			result = this.name.compareTo(o.getName());
		}
		if (result == 0) {
			// Same type and name
			result = this.value.compareTo(o.getValue());
		}
		return result;
	}

	@Override
	public String toString() {
		return "HtmlParameter type = " + type + " name= " + name + " value=" + value;
	}

}
