/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import org.zaproxy.zap.utils.Enableable;

/** TODO The GlobalExcludeURL functionality is currently alpha and subject to change.  */
class GlobalExcludeURLParamToken extends Enableable {

    private String name;
    private String description;

    public GlobalExcludeURLParamToken() {
        this("", "", false);
    }

    public GlobalExcludeURLParamToken(String name) {
        this(name, "", false);
    }

    public GlobalExcludeURLParamToken(String name, boolean enabled) {
        this(name, "", false);
    }

    public GlobalExcludeURLParamToken(String name, String description, boolean enabled) {
        super(enabled);

        this.name = name;
        this.description = description;
    }
    
    public GlobalExcludeURLParamToken(GlobalExcludeURLParamToken token) {
        this(token.name, token.description, token.isEnabled());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((name == null) ? 0 : name.hashCode());
    }

    @Override
    /** If the names (aka regexs) are equal, then the objects are equal; description is not compared. */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GlobalExcludeURLParamToken other = (GlobalExcludeURLParamToken) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
