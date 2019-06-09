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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.globalexcludeurl;

import java.util.Objects;
import org.zaproxy.zap.utils.Enableable;

class GlobalExcludeURLParamToken extends Enableable {

    private String regex;
    private String description;

    public GlobalExcludeURLParamToken() {
        this("", "", false);
    }

    public GlobalExcludeURLParamToken(String regex) {
        this(regex, "", false);
    }

    public GlobalExcludeURLParamToken(String regex, boolean enabled) {
        this(regex, "", enabled);
    }

    public GlobalExcludeURLParamToken(String regex, String description, boolean enabled) {
        super(enabled);

        this.regex = regex;
        this.description = description;
    }

    public GlobalExcludeURLParamToken(GlobalExcludeURLParamToken token) {
        this(token.regex, token.description, token.isEnabled());
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnabled(), regex, description);
    }

    @Override
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
        if (!Objects.equals(regex, other.regex)) {
            return false;
        }
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        return true;
    }
}
