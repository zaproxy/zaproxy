/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
    private static final String VIEW_TYPES = "listTypes";
    private static final String VIEW_GLOBAL_VAR = "globalVar";
    private static final String VIEW_GLOBAL_CUSTOM_VAR = "globalCustomVar";
    private static final String VIEW_GLOBAL_VARS = "globalVars";
    private static final String VIEW_GLOBAL_CUSTOM_VARS = "globalCustomVars";
    private static final String VIEW_SCRIPTS = "listScripts";
    private static final String VIEW_SCRIPT_VAR = "scriptVar";
    private static final String VIEW_SCRIPT_CUSTOM_VAR = "scriptCustomVar";
    private static final String VIEW_SCRIPT_VARS = "scriptVars";
    private static final String VIEW_SCRIPT_CUSTOM_VARS = "scriptCustomVars";
    private static final String ACTION_ENABLE = "enable";
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_RUN_STANDALONE = "runStandAloneScript";
    private static final String ACTION_LOAD = "load";
    private static final String ACTION_REMOVE = "remove";
    private static final String ACTION_CLEAR_GLOBAL_VAR = "clearGlobalVar";
    private static final String ACTION_CLEAR_GLOBAL_CUSTOM_VAR = "clearGlobalCustomVar";
    private static final String ACTION_CLEAR_GLOBAL_VARS = "clearGlobalVars";
    private static final String ACTION_CLEAR_SCRIPT_VAR = "clearScriptVar";
    private static final String ACTION_CLEAR_SCRIPT_CUSTOM_VAR = "clearScriptCustomVar";
    private static final String ACTION_CLEAR_SCRIPT_VARS = "clearScriptVars";
    private static final String ACTION_SET_GLOBAL_VAR = "setGlobalVar";
    private static final String ACTION_SET_SCRIPT_VAR = "setScriptVar";
    private static final String ACTION_PARAM_SCRIPT_NAME = "scriptName";
    private static final String ACTION_PARAM_SCRIPT_DESC = "scriptDescription";
    private static final String ACTION_PARAM_SCRIPT_TYPE = "scriptType";
    private static final String ACTION_PARAM_SCRIPT_ENGINE = "scriptEngine";
    private static final String ACTION_PARAM_FILE_NAME = "fileName";
    private static final String ACTION_PARAM_CHARSET = "charset";
    private static final String PARAM_VAR_KEY = "varKey";
    private static final String PARAM_VAR_VALUE = "varValue";

    private ExtensionScript extension;

    public ScriptAPI(ExtensionScript extension) {
        this.extension = extension;
        this.addApiView(new ApiView(VIEW_ENGINES, new String[] {}, new String[] {}));
        this.addApiView(new ApiView(VIEW_TYPES));
        this.addApiView(new ApiView(VIEW_SCRIPTS, new String[] {}, new String[] {}));
        this.addApiView(new ApiView(VIEW_GLOBAL_VAR, new String[] {PARAM_VAR_KEY}));
        this.addApiView(new ApiView(VIEW_GLOBAL_CUSTOM_VAR, new String[] {PARAM_VAR_KEY}));
        this.addApiView(new ApiView(VIEW_GLOBAL_VARS));
        this.addApiView(new ApiView(VIEW_GLOBAL_CUSTOM_VARS));
        this.addApiView(
                new ApiView(
                        VIEW_SCRIPT_VAR, new String[] {ACTION_PARAM_SCRIPT_NAME, PARAM_VAR_KEY}));
        this.addApiView(
                new ApiView(
                        VIEW_SCRIPT_CUSTOM_VAR,
                        new String[] {ACTION_PARAM_SCRIPT_NAME, PARAM_VAR_KEY}));
        this.addApiView(new ApiView(VIEW_SCRIPT_VARS, new String[] {ACTION_PARAM_SCRIPT_NAME}));
        this.addApiView(
                new ApiView(VIEW_SCRIPT_CUSTOM_VARS, new String[] {ACTION_PARAM_SCRIPT_NAME}));

        this.addApiAction(
                new ApiAction(
                        ACTION_ENABLE, new String[] {ACTION_PARAM_SCRIPT_NAME}, new String[] {}));
        this.addApiAction(
                new ApiAction(
                        ACTION_DISABLE, new String[] {ACTION_PARAM_SCRIPT_NAME}, new String[] {}));
        this.addApiAction(
                new ApiAction(
                        ACTION_LOAD,
                        new String[] {
                            ACTION_PARAM_SCRIPT_NAME,
                            ACTION_PARAM_SCRIPT_TYPE,
                            ACTION_PARAM_SCRIPT_ENGINE,
                            ACTION_PARAM_FILE_NAME
                        },
                        new String[] {ACTION_PARAM_SCRIPT_DESC, ACTION_PARAM_CHARSET}));
        this.addApiAction(
                new ApiAction(
                        ACTION_REMOVE, new String[] {ACTION_PARAM_SCRIPT_NAME}, new String[] {}));
        this.addApiAction(
                new ApiAction(
                        ACTION_RUN_STANDALONE,
                        new String[] {ACTION_PARAM_SCRIPT_NAME},
                        new String[] {}));

        this.addApiAction(new ApiAction(ACTION_CLEAR_GLOBAL_VAR, new String[] {PARAM_VAR_KEY}));
        this.addApiAction(
                new ApiAction(ACTION_CLEAR_GLOBAL_CUSTOM_VAR, new String[] {PARAM_VAR_KEY}));
        this.addApiAction(new ApiAction(ACTION_CLEAR_GLOBAL_VARS));
        this.addApiAction(
                new ApiAction(
                        ACTION_CLEAR_SCRIPT_VAR,
                        new String[] {ACTION_PARAM_SCRIPT_NAME, PARAM_VAR_KEY}));
        this.addApiAction(
                new ApiAction(
                        ACTION_CLEAR_SCRIPT_CUSTOM_VAR,
                        new String[] {ACTION_PARAM_SCRIPT_NAME, PARAM_VAR_KEY}));
        this.addApiAction(
                new ApiAction(ACTION_CLEAR_SCRIPT_VARS, new String[] {ACTION_PARAM_SCRIPT_NAME}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_SCRIPT_VAR,
                        new String[] {ACTION_PARAM_SCRIPT_NAME, PARAM_VAR_KEY},
                        new String[] {PARAM_VAR_VALUE}));
        this.addApiAction(
                new ApiAction(
                        ACTION_SET_GLOBAL_VAR,
                        new String[] {PARAM_VAR_KEY},
                        new String[] {PARAM_VAR_VALUE}));
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
                    result.addItem(new ApiResponseSet<>("Script", map));
                }
            }
            return result;

        } else if (VIEW_ENGINES.equals(name)) {
            ApiResponseList result = new ApiResponseList(name);

            for (String engine : extension.getScriptingEngines()) {
                result.addItem(new ApiResponseElement("engine", engine));
            }
            return result;

        } else if (VIEW_TYPES.equals(name)) {
            ApiResponseList result = new ApiResponseList(name);
            for (ScriptType type : extension.getScriptTypes()) {
                Map<String, String> data = new HashMap<>();
                data.put("name", type.getName());
                data.put("uiName", Constant.messages.getString(type.getI18nKey()));
                String descKey = type.getI18nKey() + ".desc";
                String description =
                        Constant.messages.containsKey(descKey)
                                ? Constant.messages.getString(descKey)
                                : "";
                data.put("description", description);
                data.put("enableable", String.valueOf(type.isEnableable()));
                if (type.isEnableable()) {
                    data.put("enabledByDefault", String.valueOf(type.isEnabledByDefault()));
                }
                result.addItem(new ApiResponseSet<>("type", data));
            }
            return result;
        } else if (VIEW_GLOBAL_VAR.equals(name)) {
            String value = ScriptVars.getGlobalVar(params.getString(PARAM_VAR_KEY));
            validateVarValue(value);
            return new ApiResponseElement(name, value);
        } else if (VIEW_GLOBAL_CUSTOM_VAR.equals(name)) {
            Object value = ScriptVars.getGlobalCustomVar(params.getString(PARAM_VAR_KEY));
            validateVarValue(value);
            return new ApiResponseElement(name, value.toString());
        } else if (VIEW_GLOBAL_VARS.equals(name)) {
            return new ScriptVarsResponse(name, ScriptVars.getGlobalVars());
        } else if (VIEW_GLOBAL_CUSTOM_VARS.equals(name)) {
            return new ScriptVarsResponse(
                    name, convertCustomVars(ScriptVars.getGlobalCustomVars()));
        } else if (VIEW_SCRIPT_VAR.equals(name)) {
            String value =
                    ScriptVars.getScriptVar(
                            getAndValidateScriptName(params), params.getString(PARAM_VAR_KEY));
            validateVarValue(value);
            return new ApiResponseElement(name, value);
        } else if (VIEW_SCRIPT_CUSTOM_VAR.equals(name)) {
            Object value =
                    ScriptVars.getScriptCustomVar(
                            getAndValidateScriptName(params), params.getString(PARAM_VAR_KEY));
            validateVarValue(value);
            return new ApiResponseElement(name, value.toString());
        } else if (VIEW_SCRIPT_VARS.equals(name)) {
            return new ScriptVarsResponse(
                    name, ScriptVars.getScriptVars(getAndValidateScriptName(params)));
        } else if (VIEW_SCRIPT_CUSTOM_VARS.equals(name)) {
            return new ScriptVarsResponse(
                    name,
                    convertCustomVars(
                            ScriptVars.getScriptCustomVars(getAndValidateScriptName(params))));
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
    }

    /**
     * Validates that the value is non-{@code null}, that is, the variable exists.
     *
     * @param varValue the value of the variable to validate.
     * @throws ApiException if the value is {@code null}.
     */
    private static void validateVarValue(Object varValue) throws ApiException {
        if (varValue == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_VAR_KEY);
        }
    }

    /**
     * Gets and validates that the parameter named {@value #ACTION_PARAM_SCRIPT_NAME}, in {@code
     * parameters}, represents an existing script name.
     *
     * <p>The parameter must exist, that is, it should be a mandatory parameter, otherwise a runtime
     * exception is thrown.
     *
     * @param params the parameters of the API request.
     * @return the name of a existing script.
     * @throws ApiException if the no script exists with the given name.
     */
    private String getAndValidateScriptName(JSONObject params) throws ApiException {
        String scriptName = params.getString(ACTION_PARAM_SCRIPT_NAME);
        if (extension.getScript(scriptName) == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
        }
        return scriptName;
    }

    private static Map<String, String> convertCustomVars(Map<String, Object> vars) {
        return vars.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        if (ACTION_ENABLE.equals(name)) {
            ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
            if (script == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
            }
            if (!script.getType().isEnableable()) {
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SCRIPT_NAME);
            }
            if (script.getEngine() == null) {
                throw new ApiException(
                        ApiException.Type.BAD_STATE,
                        "Unable to enable the script, script engine not available: "
                                + script.getEngineName());
            }
            extension.setEnabled(script, true);
            return ApiResponseElement.OK;

        } else if (ACTION_DISABLE.equals(name)) {
            ScriptWrapper script = extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME));
            if (script == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_NAME);
            }
            if (!script.getType().isEnableable()) {
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SCRIPT_NAME);
            }
            extension.setEnabled(script, false);
            return ApiResponseElement.OK;

        } else if (ACTION_LOAD.equals(name)) {
            ScriptType type = extension.getScriptType(params.getString(ACTION_PARAM_SCRIPT_TYPE));
            if (type == null) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_TYPE);
            }
            ScriptEngineWrapper engine;
            try {
                engine = extension.getEngineWrapper(params.getString(ACTION_PARAM_SCRIPT_ENGINE));
            } catch (InvalidParameterException e) {
                throw new ApiException(
                        ApiException.Type.DOES_NOT_EXIST, ACTION_PARAM_SCRIPT_ENGINE, e);
            }
            File file = new File(params.getString(ACTION_PARAM_FILE_NAME));
            if (!file.exists()) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, file.getAbsolutePath());
            }
            if (extension.getScript(params.getString(ACTION_PARAM_SCRIPT_NAME)) != null) {
                throw new ApiException(ApiException.Type.ALREADY_EXISTS, ACTION_PARAM_SCRIPT_NAME);
            }

            ScriptWrapper script =
                    new ScriptWrapper(
                            params.getString(ACTION_PARAM_SCRIPT_NAME),
                            getParam(params, ACTION_PARAM_SCRIPT_DESC, ""),
                            engine,
                            type,
                            true,
                            file);

            Charset charset = getCharset(params);
            try {
                if (charset != null) {
                    extension.loadScript(script, charset);
                } else {
                    extension.loadScript(script);
                }
            } catch (MalformedInputException e) {
                throw new ApiException(
                        charset != null
                                ? ApiException.Type.ILLEGAL_PARAMETER
                                : ApiException.Type.MISSING_PARAMETER,
                        ACTION_PARAM_CHARSET,
                        e);
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
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER,
                        "Parameter "
                                + ACTION_PARAM_SCRIPT_NAME
                                + " does not match a "
                                + ExtensionScript.TYPE_STANDALONE
                                + " script.");
            }
            try {
                extension.invokeScript(script);
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, e);
            }
            return ApiResponseElement.OK;

        } else if (ACTION_CLEAR_GLOBAL_VAR.equals(name)) {
            ScriptVars.setGlobalVar(params.getString(PARAM_VAR_KEY), null);
            return ApiResponseElement.OK;
        } else if (ACTION_CLEAR_GLOBAL_CUSTOM_VAR.equals(name)) {
            ScriptVars.setGlobalCustomVar(params.getString(PARAM_VAR_KEY), null);
            return ApiResponseElement.OK;
        } else if (ACTION_CLEAR_GLOBAL_VARS.equals(name)) {
            ScriptVars.clearGlobalVars();
            return ApiResponseElement.OK;
        } else if (ACTION_CLEAR_SCRIPT_VAR.equals(name)) {
            ScriptVars.setScriptVar(
                    getAndValidateScriptName(params), params.getString(PARAM_VAR_KEY), null);
            return ApiResponseElement.OK;
        } else if (ACTION_CLEAR_SCRIPT_CUSTOM_VAR.equals(name)) {
            ScriptVars.setScriptCustomVar(
                    getAndValidateScriptName(params), params.getString(PARAM_VAR_KEY), null);
            return ApiResponseElement.OK;
        } else if (ACTION_CLEAR_SCRIPT_VARS.equals(name)) {
            ScriptVars.clearScriptVars(getAndValidateScriptName(params));
            return ApiResponseElement.OK;
        } else if (ACTION_SET_GLOBAL_VAR.equals(name)) {
            ScriptVars.setGlobalVar(
                    params.getString(PARAM_VAR_KEY), params.getString(PARAM_VAR_VALUE));
            return ApiResponseElement.OK;
        } else if (ACTION_SET_SCRIPT_VAR.equals(name)) {
            ScriptVars.setScriptVar(
                    getAndValidateScriptName(params),
                    params.getString(PARAM_VAR_KEY),
                    params.getString(PARAM_VAR_VALUE));
            return ApiResponseElement.OK;
        } else {
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
    }

    private static Charset getCharset(JSONObject params) throws ApiException {
        if (!params.has(ACTION_PARAM_CHARSET)) {
            return null;
        }

        String charsetName = params.getString(ACTION_PARAM_CHARSET);
        if (charsetName.isEmpty()) {
            return null;
        }

        try {
            return Charset.forName(charsetName);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_CHARSET, e);
        }
    }

    private static class ScriptVarsResponse extends ApiResponse {

        private final ApiResponseSet<String> defaultResponse;
        private final ApiResponseList xmlResponse;

        public ScriptVarsResponse(String name, Map<String, String> vars) {
            super(name);

            defaultResponse =
                    new ApiResponseSet<String>(name, vars) {

                        @Override
                        public JSON toJSON() {
                            JSONObject response = new JSONObject();
                            response.put(name, super.toJSON());
                            return response;
                        }
                    };

            xmlResponse = new ApiResponseList(name);
            synchronized (vars) {
                for (Entry<String, String> entry : vars.entrySet()) {
                    Map<String, String> varData = new HashMap<>();
                    varData.put("key", entry.getKey());
                    varData.put("value", entry.getValue());
                    xmlResponse.addItem(new ApiResponseSet<>("var", varData));
                }
            }
        }

        @Override
        public JSON toJSON() {
            return defaultResponse.toJSON();
        }

        @Override
        public void toXML(Document doc, Element rootElement) {
            xmlResponse.toXML(doc, rootElement);
        }

        @Override
        public void toHTML(StringBuilder sb) {
            defaultResponse.toHTML(sb);
        }

        @Override
        public String toString(int indent) {
            return defaultResponse.toString(indent);
        }
    }
}
