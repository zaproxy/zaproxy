/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.List;

public class ApiOther extends ApiElement {

    private boolean requiresApiKey = true;

    public ApiOther(String name) {
        super(name);
    }

    public ApiOther(String name, boolean requiresApiKey) {
        super(name);
        this.requiresApiKey = requiresApiKey;
    }

    public ApiOther(String name, List<String> paramNames) {
        super(name, paramNames);
    }

    public ApiOther(String name, List<String> paramNames, boolean requiresApiKey) {
        super(name, paramNames);
        this.requiresApiKey = requiresApiKey;
    }

    public ApiOther(String name, String[] paramNames) {
        super(name, paramNames);
    }

    public ApiOther(String name, String[] paramNames, boolean requiresApiKey) {
        super(name, paramNames);
        this.requiresApiKey = requiresApiKey;
    }

    public ApiOther(
            String name, List<String> mandatoryParamNames, List<String> optionalParamNames) {
        super(name, mandatoryParamNames, optionalParamNames);
    }

    public ApiOther(
            String name,
            List<String> mandatoryParamNames,
            List<String> optionalParamNames,
            boolean requiresApiKey) {
        super(name, mandatoryParamNames, optionalParamNames);
        this.requiresApiKey = requiresApiKey;
    }

    public ApiOther(String name, String[] mandatoryParamNames, String[] optionalParamNames) {
        super(name, mandatoryParamNames, optionalParamNames);
    }

    public ApiOther(
            String name,
            String[] mandatoryParamNames,
            String[] optionalParamNames,
            boolean requiresApiKey) {
        super(name, mandatoryParamNames, optionalParamNames);
        this.requiresApiKey = requiresApiKey;
    }

    public boolean isRequiresApiKey() {
        return requiresApiKey;
    }

    public void setRequiresApiKey(boolean requiresApiKey) {
        this.requiresApiKey = requiresApiKey;
    }
}
