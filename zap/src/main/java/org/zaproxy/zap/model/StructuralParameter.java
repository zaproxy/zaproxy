/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.model;

import net.sf.json.JSONObject;
import org.zaproxy.zap.utils.Enableable;

public class StructuralParameter extends Enableable implements Cloneable {
    private static final String CONFIG_NAME = "name";
    private static final String CONFIG_ENABLED = "enabled";

    private String name;

    public StructuralParameter(String name, boolean enabled) {
        super(enabled);

        this.name = name;
    }

    public StructuralParameter(String config) {
        super();

        JSONObject configData = JSONObject.fromObject(config);

        this.name = configData.getString(CONFIG_NAME);
        setEnabled(configData.getBoolean(CONFIG_ENABLED));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected StructuralParameter clone() {
        return new StructuralParameter(this.name, this.isEnabled());
    }

    public String getConfig() {
        JSONObject serialized = new JSONObject();

        serialized.put(CONFIG_NAME, this.name);
        serialized.put(CONFIG_ENABLED, this.isEnabled());

        return serialized.toString();
    }
}
