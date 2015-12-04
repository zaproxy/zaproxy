/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP Development Team
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
package org.zaproxy.zap.model;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.zaproxy.zap.utils.Enableable;
/**
 * Represents a rule for modifying the structure of an app as it is represented in the Sites tree
 * 
 * Data driven nodes are nodes that represent URL path elements that come from a database
 * For example with http://www.example.com/a/b/c we normally assume that 'a', 'b' and 'c' are part of the 
 * structure of the application. However if 'b' is actually a value retrieved from a db then we should treat 
 * all such nodes as the same, so:
 * 	http://www.example.com/a/b/c
 * 	http://www.example.com/a/ddd/c
 * 	http://www.example.com/a/eee/c
 * are the same as far as the application structure is concerned - theres no point attacking all 3 as the same code
 * will be behind them.
 * 
 * Structural parameters are parameters (as opposed to URL path elements) that actually define part of the
 * structure of an app. These are most commonly found in 'single page apps' where:
 * 	http://www.example.com/a/b?page=c
 * 	http://www.example.com/a/b?page=d
 * 	http://www.example.com/a/b?page=e
 * all represent different pages with different functionality.
 * In this case the 'page' parameter should be treated as being 'structural' rather than data
 * 
 * @author simon
 * @since 2.4.3
 */
public class StructuralNodeModifier extends Enableable implements Cloneable {
	public enum Type {DataDrivenNode, StructuralParameter}
	
	private static final String CONFIG_NAME = "name";
	private static final String CONFIG_TYPE = "type";
	private static final String CONFIG_PATTERN = "pattern";

	private Type type;
	private Pattern pattern;
	private String name;
	
	public StructuralNodeModifier(Type type, Pattern pattern, String name) {
		super();
		this.type = type;
		this.pattern = pattern;
		this.name = name;
	}

	public StructuralNodeModifier(String config) {
		super();
		JSONObject json = JSONObject.fromObject(config);
		this.name = json.getString(CONFIG_NAME);
		this.type = Type.valueOf(json.getString(CONFIG_TYPE));
		if (json.containsKey(CONFIG_TYPE)) {
			pattern = Pattern.compile(json.getString(CONFIG_PATTERN));
		}
	}

	public Type getType() {
		return type;
	}

	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public StructuralNodeModifier clone() {
		return new StructuralNodeModifier(type, Pattern.compile(pattern.toString()), name);
	}

	public String getConfig() {
		JSONObject json = new JSONObject();
		json.put(CONFIG_TYPE, this.getType().name());
		json.put(CONFIG_NAME, this.getName());
		if (getPattern() != null) {
			json.put(CONFIG_PATTERN, this.getPattern().pattern());
		}
		return json.toString();
	}
	
}
