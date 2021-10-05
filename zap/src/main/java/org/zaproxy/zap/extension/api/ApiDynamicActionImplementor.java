/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;

/** The ApiDynamicActionImplementor is used for actions that are loaded dynamically. */
public abstract class ApiDynamicActionImplementor extends ApiElement {

    /**
     * Instantiates a new api dynamic action implementor.
     *
     * @param name the name
     * @param mandatoryParamNames the mandatory param names, or <code>null</code> if there are no
     *     mandatory parameters
     * @param optionalParamNames the optional param names, or <code>null</code> if there are no
     *     optional parameters
     */
    public ApiDynamicActionImplementor(
            String name, String[] mandatoryParamNames, String[] optionalParamNames) {
        super(name, mandatoryParamNames, optionalParamNames);
    }

    /**
     * Handle the execution of the action.
     *
     * @param params the params
     * @throws ApiException the api exception
     */
    public abstract void handleAction(JSONObject params) throws ApiException;

    /**
     * Builds an {@link ApiResponse} describing the parameters of this action.
     *
     * @return the api response set
     */
    public ApiResponse buildParamsDescription() {
        ApiResponseList configParams = new ApiResponseList("methodConfigParams");
        for (ApiParameter param : this.getParameters())
            configParams.addItem(buildParamMap(param.getName(), param.isRequired()));
        return configParams;
    }

    /**
     * Builds a {@code ApiResponseSet} with the given parameter name and whether or not it is
     * mandatory.
     *
     * @param paramName the name of the parameter
     * @param mandatory {@code true} if the parameter is mandatory, {@code false} otherwise
     * @return the {@code ApiResponseSet} with the name and mandatory fields
     */
    private static ApiResponseSet<String> buildParamMap(String paramName, boolean mandatory) {
        Map<String, String> m = new HashMap<>();
        m.put("name", paramName);
        m.put("mandatory", mandatory ? "true" : "false");
        return new ApiResponseSet<>("param", m);
    }
}
