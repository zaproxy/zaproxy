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

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;

public class ScriptVars {
	
	private static int MAX_KEY_SIZE = 30;
	private static int MAX_VALUE_SIZE = 1024;
	private static int MAX_SCRIPT_VARS = 20;
	private static int MAX_GLOBAL_VARS = 50;

	private static Map<String, String> globalVars = new HashMap<String, String>();
	private static Map<String, Map<String, String>> scriptVars = new HashMap<String, Map<String, String>>();
	
	/**
	 * Set a global variable which will be accessible by all scripts
	 * @param key
	 * @param value
	 */
	public static void setGlobalVar(String key, String value) {
		if (key == null || key.length() > MAX_KEY_SIZE) {
			throw new InvalidParameterException("Invalid key - must be non null and have a length less than " + MAX_KEY_SIZE);
		}

		if (value == null) {
			globalVars.remove(key);
		} else {
			if (value.length() > MAX_VALUE_SIZE) {
				throw new InvalidParameterException("Invalid value - must have a length less than " + MAX_VALUE_SIZE);
			}
			if (globalVars.size() > MAX_GLOBAL_VARS) {
				throw new InvalidParameterException("Maximum number of global variables reached: " + MAX_GLOBAL_VARS);
			}
			globalVars.put(key, value);
		}
	}
	
	/**
	 * Get a global variable which is be accessible to all scripts
	 * @param key
	 */
	public static String getGlobalVar(String key) {
		return globalVars.get(key);
	}

	/**
	 * Set a variable that is only accessible to this script.
	 * This method is only usable from scripting languages that provide access to the ScriptContext (like JavaScript)
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setScriptVar(ScriptContext context, String key, String value) {
		if (context == null) {
			throw new InvalidParameterException("Invalid context - must be non null");
		}
		if (key == null || key.length() > MAX_KEY_SIZE) {
			throw new InvalidParameterException("Invalid key - must be non null and have a length less than " + MAX_KEY_SIZE);
		}
		String scriptName = (String)context.getAttribute(ExtensionScript.SCRIPT_NAME_ATT);
		if (scriptName == null) {
			throw new InvalidParameterException("Failed to find script name");
		}
		Map<String, String> scVars = scriptVars.get(scriptName);
		if (scVars == null) {
			scVars = new HashMap<String, String>();
			scriptVars.put(scriptName, scVars);
		}
		
		if (value == null) {
			scVars.remove(key);
		} else {
			if (value.length() > MAX_VALUE_SIZE) {
				throw new InvalidParameterException("Invalid value - must have a length less than " + MAX_VALUE_SIZE);
			}
			if (scVars.size() > MAX_SCRIPT_VARS) {
				throw new InvalidParameterException("Maximum number of script variables reached: " + MAX_SCRIPT_VARS);
			}
			scVars.put(key, value);
		}
	}
	
	/**
	 * Get a variable that is only accessible from this script.
	 * This method is only usable from scripting languages that provide access to the ScriptContext (like JavaScript)
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getScriptVar(ScriptContext context, String key) {
		if (context == null) {
			throw new InvalidParameterException("Invalid context - must be non null");
		}
		String scriptName = (String)context.getAttribute(ExtensionScript.SCRIPT_NAME_ATT);
		if (scriptName == null) {
			throw new InvalidParameterException("Failed to find script name");
		}
		Map<String, String> scVars = scriptVars.get(scriptName);
		if (scVars == null) {
			// No vars have been associated with this script
			return null;
		}
		
		return scVars.get(key);
	}

	static void clear() {
		globalVars.clear();
		scriptVars.clear();
	}
}
