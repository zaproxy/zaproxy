/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development team
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

import javax.script.ScriptEngine;
import javax.swing.ImageIcon;

public abstract class ScriptEngineWrapper {

	private ScriptEngine engine;
	private String languageName;
	private String engineName;
	
	
	public ScriptEngineWrapper(ScriptEngine engine) {
		this.engine = engine;
		this.engineName = engine.getFactory().getEngineName();
		this.languageName = engine.getFactory().getLanguageName();
	}
	
	public String getLanguageName() {
		return languageName;
	}

	public String getEngineName() {
		return engineName;
	}
	
	public ScriptEngine getEngine() {
		// TODO Nasty hack!
		return engine.getFactory().getScriptEngine();
	}
	
	public abstract boolean isTextBased();
	
	public abstract String getTemplate(String type);
	
	public abstract String getSyntaxStyle();
	
	public abstract ImageIcon getIcon();

	public abstract List<String> getExtensions();
	
	public abstract boolean isRawEngine();
	
	/**
	 * Returns true if this engine supported script types without defined templates.
	 * @param type
	 * @return
	 */
	public abstract boolean isSupportsMissingTemplates();
	
	/**
	 * Tells whether or not the given {@code script} is a default template of this engine.
	 * <p>
	 * Default templates are automatically removed when the script engine is removed. 
	 *
	 * @param script the script template that should be checked
	 * @return {@code true} if the template script is default of this engine, {@code false} otherwise
	 */
	public abstract boolean isDefaultTemplate(ScriptWrapper script);
}
