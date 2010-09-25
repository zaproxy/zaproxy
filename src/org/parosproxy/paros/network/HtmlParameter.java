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
 */
package org.parosproxy.paros.network;

public class HtmlParameter implements Comparable<HtmlParameter> {
	public enum Type {cookie, url, form};
	private String name;
	private String value;
	private Type type;
	
	public HtmlParameter(Type type, String name, String value) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
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
	
	@Override
	public int compareTo(HtmlParameter o) {
		int result;
		if (o == null) { 
			return 1;
		}
		result = this.type.ordinal() - o.getType().ordinal();
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
	
	
}
