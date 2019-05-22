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


public class RuleConfig implements Cloneable {

    private String key;
    private String value;
    private String defaultValue;
    
    private boolean changed;

    public RuleConfig() {
    }

    public RuleConfig(String key, String defaultValue, String value) {
        super();
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    public RuleConfig(String key, String defaultValue) {
        super();
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }
    
    @Override
    protected RuleConfig clone() {
        return new RuleConfig(key, defaultValue, value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.changed = true;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isChanged() {
        return changed;
    }
    
    public void reset() {
        this.setValue(this.defaultValue);
    }
}
