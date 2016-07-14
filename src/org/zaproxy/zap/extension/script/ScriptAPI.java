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

package org.zaproxy.zap.extension.script;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class ScriptAPI extends ApiImplementor {

	private static final String PREFIX = "script";
	private static final String VIEW_ENGINES = "listEngines";
	private static final String VIEW_SCRIPTS = "listScripts";
	private static final String ACTION_ENABLE = "enable";
	private static final String ACTION_DISABLE = "disable";
	private static final String ACTION_RUN_STANDALONE = "runStandAloneScript";
	private static final String ACTION_LOAD = "load";
	private static final String ACTION_REMOVE = "remove";
	private static final String ACTION_PARAM_SCRIPT_NAME = "scriptName";
	private static final String ACTION_PARAM_SCRIPT_DESC = "scriptDescription";
	private static final String ACTION_PARAM_SCRIPT_TYPE = "scriptType";
	private static final String ACTION_PARAM_SCRIPT_ENGINE = "scriptEngine";
	private static final String ACTION_PARAM_FILE_NAME = "fileName";

	private ExtensionScript extension;
	
	public ScriptAPI (ExtensionScript extension) {
		this.extension = extension;
		this.addApiView(new ApiView(VIEW_ENGINES, new String[]{}, new String[]{}));
		this.addApiView(new ApiView(VIEW_SCRIPTS, new String[]{}, new String[]{}));
		
		this.addApiAction(new ApiAction(ACTION_ENABLE, new String[]{ACTION_PARAM_SCRIPT_NAME}, new String[]{}));
		this.addApiAction(new ApiAction(ACTION_DISABLE, new String[]{ACTION_PARAM_SCRIPT_NAME}, new String[]{}));
		this.addApiAction(new ApiAction(ACTION_LOAD, 
				new String[]{ACTION_PARAM_SCRIPT_NAME, ACTION_PARAM_SCRIPT_TYPE, 
							ACTION_PARAM_SCRIPT_ENGINE, ACTION_PARAM_FILE_NAME}, 
				new String[]{ACTION_PARAM_SCRIPT_DESC}));
		this.addApiAction(new ApiAction(ACTION_REMOVE, new String[]{ACTION_PARAM_SCRIPT_NAME}, new String[]{}));
		this.addApiAction(new ApiAction(ACTION_RUN_STANDALONE, new String[]{ACTION_PARAM_SCRIPT_NAME}, new String[]{}));

	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		if (VIEW_SCRIPTS.equals(name)) {
			ApiResponseList result = new ApiResponseList(name);
			for (ScriptType type : extension.getScriptTypes()) {
				for (ScriptWrapper script : extension.getScripts(type)) {
					Map<String, String> map = new HashMap<>();
					map.put("name", script.getName());
					map.put("type", script.getTypeName());
					map.put("engine", script.getEngineName());
					map.put("description", script.getDescription());
					map.put("error", Boolean.toString(script.isError()));
					if (script.isError()) {
						map.put("lastError", script.getLastErrorDetails());
					}
					if (type.isEnableable()) {
						map.put("enabled", Boolean.toString(script.isEnabled()));
					}
					result.addItem(new ApiResponseSet("Script", map));
				}
			}
			return result;
			
		} else if (VIEW_ENGINES.equals(name)) {
			ApiResponseList result = new ApiResponseList(name);
			
			for (String engine : extension.getScriptingEngines()) {
				result.addItem(new ApiResponseElement("engine", engine));
			}
			return result;
 			
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		if (ACTION_ENABLE.equals(name)) {
			ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
			if (script == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
			}
			if (!script.getType().isEnableable()) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SCRIPT_NAME);
			}
			extension.setEnabled(script, true);
			return ApiResponseElement.OK;
			
		} else if (ACTION_DISABLE.equals(name)) {
			ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
			if (script == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
			}
			if (!script.getType().isEnableable()) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SCRIPT_NAME);
			}
			extension.setEnabled(script, false);
			return ApiResponseElement.OK;

		} else if (ACTION_LOAD.equals(name)) {
			ScriptType type = extension.getScriptType(params.getString(ACTION_PARAM_SCRIPT_TYPE));
			if (type == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_TYPE);
			}
			ScriptEngineWrapper engine = extension.getEngineWrapper(params.getString(ACTION_PARAM_SCRIPT_ENGINE));
			if (engine == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_ENGINE);
			}
			File file = new File(params.getString(ACTION_PARAM_FILE_NAME));
			if (!file.exists()) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, file.getAbsolutePath());
			}
			
			ScriptWrapper script = new ScriptWrapper(
					params.getString(ACTION_PARAM_SCRIPT_NAME),
					getParam(params, ACTION_PARAM_SCRIPT_DESC, ""),
					engine,
					type,
					true,
					file);

			try {
				extension.loadScript(script);
			} catch (IOException e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
			}
			extension.addScript(script, false);
			return ApiResponseElement.OK;

		} else if (ACTION_REMOVE.equals(name)) {
			ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
			if (script == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
			}
			extension.removeScript(script);
			return ApiResponseElement.OK;
			
		} else if (ACTION_RUN_STANDALONE.equals(name)) {
			ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
			if (script == null) {
				throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
			}
			if (!script.getType().getName().equals(ExtensionScript.TYPE_STANDALONE)) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SCRIPT_NAME);
			}
			try {
				extension.invokeScript(script);
			} catch (Exception e) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
			}
			return ApiResponseElement.OK;

		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		
	}

}
