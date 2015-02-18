/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright The ZAP development team
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
package org.zaproxy.zap.extension.script;

import java.util.List;

public class ScriptCollection {
	
	private ScriptType type;
	private List<ScriptWrapper> scripts;
	
	public ScriptCollection(ScriptType type, List<ScriptWrapper> scripts) {
		super();
		this.type = type;
		this.scripts = scripts;
	}

	public ScriptType getType() {
		return type;
	}

	public List<ScriptWrapper> getScripts() {
		return scripts;
	}

	public void setScripts(List<ScriptWrapper> scripts) {
		this.scripts = scripts;
	}
	
	public void addScript(ScriptWrapper script) {
		this.scripts.add(script);
	}
	
	public void removeScript(ScriptWrapper script) {
		this.scripts.remove(script);
	}
	
}
