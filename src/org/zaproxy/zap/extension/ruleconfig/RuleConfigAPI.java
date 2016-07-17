/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development team
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
package org.zaproxy.zap.extension.ruleconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

public class RuleConfigAPI extends ApiImplementor {

    private static final String PREFIX = "ruleConfig";
    
    private static final String ACTION_RESET_RULE_CONFIG_VALUE = "resetRuleConfigValue";
    private static final String ACTION_RESET_ALL_RULE_CONFIG_VALUES = "resetAllRuleConfigValues";
    private static final String ACTION_SET_RULE_CONFIG_VALUE = "setRuleConfigValue";

    private static final String VIEW_RULE_CONFIG_VALUE = "ruleConfigValue";
    private static final String VIEW_ALL_RULE_CONFIGS = "allRuleConfigs";

    private static final String PARAM_KEY = "key";
    private static final String PARAM_VALUE = "value";

    private ExtensionRuleConfig extension;
    
    public RuleConfigAPI(ExtensionRuleConfig extension) {
        this.extension = extension;
        this.addApiAction(new ApiAction(ACTION_RESET_RULE_CONFIG_VALUE, new String[] {PARAM_KEY}));
        this.addApiAction(new ApiAction(ACTION_RESET_ALL_RULE_CONFIG_VALUES));
        this.addApiAction(new ApiAction(ACTION_SET_RULE_CONFIG_VALUE, 
                new String[] {PARAM_KEY}, new String[] {PARAM_VALUE}));
        this.addApiView(new ApiView(VIEW_RULE_CONFIG_VALUE, new String[] {PARAM_KEY}));
        this.addApiView(new ApiView(VIEW_ALL_RULE_CONFIGS));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        RuleConfig rc;
        switch (name) {
        case ACTION_SET_RULE_CONFIG_VALUE:
            rc = extension.getRuleConfig(params.getString(PARAM_KEY));
            if (rc != null) {
                if (params.containsKey(PARAM_VALUE)) {
                    extension.setRuleConfigValue(rc.getKey(), params.getString(PARAM_VALUE));
                } else {
                    extension.setRuleConfigValue(rc.getKey(), "");
                }
            } else {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_KEY);
            }
            break;
        case ACTION_RESET_RULE_CONFIG_VALUE:
            rc = extension.getRuleConfig(params.getString(PARAM_KEY));
            if (rc != null) {
                extension.resetRuleConfigValue(rc.getKey());
            } else {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_KEY);
            }
            break;
        case ACTION_RESET_ALL_RULE_CONFIG_VALUES:
            extension.resetAllRuleConfigValues();
            break;
        default:
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }

        return ApiResponseElement.OK;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params)
            throws ApiException {
        ApiResponse result;

        switch (name) {
        case VIEW_RULE_CONFIG_VALUE:
            RuleConfig rc = extension.getRuleConfig(params.getString(PARAM_KEY));
            if (rc != null) {
                result = new ApiResponseElement(name, rc.getValue());
            } else {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_KEY);
            }
            
            break;
        case VIEW_ALL_RULE_CONFIGS:
            List<RuleConfig> allRules = extension.getAllRuleConfigs();
            
            ApiResponseList resultList = new ApiResponseList(name);
            for (RuleConfig rc2 : allRules) {
                Map<String, String> map = new HashMap<>();
                map.put("key", String.valueOf(rc2.getKey()));
                map.put("defaultValue", rc2.getDefaultValue());
                map.put("value", String.valueOf(rc2.getValue()));
                if (Constant.messages.containsKey(rc2.getKey())) {
                    map.put("description", Constant.messages.getString(rc2.getKey()));
                }
                resultList.addItem(new ApiResponseSet("ruleConfig", map));
            }
            
            result = resultList;
            break;
        default:
            throw new ApiException(ApiException.Type.BAD_VIEW);
        }
        return result;
    }

}
