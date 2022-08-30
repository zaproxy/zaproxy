/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import org.apache.commons.lang.StringUtils;
import org.parosproxy.paros.network.HttpMessage;

public class ScannerParamValueFilterRule implements ScannerParamFilterRule, Cloneable {
    private String whitelistingValueExpression;

    public ScannerParamValueFilterRule(String whitelistingValueExpression) {
        this.whitelistingValueExpression = whitelistingValueExpression;
    }

    @Override
    public boolean filter(HttpMessage msg, NameValuePair param) {
        if (StringUtils.isEmpty(this.whitelistingValueExpression)) return false;
        return param.getValue() != null
                && !param.getValue().matches(this.whitelistingValueExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScannerParamValueFilterRule that = (ScannerParamValueFilterRule) o;

        return whitelistingValueExpression != null
                ? whitelistingValueExpression.equals(that.whitelistingValueExpression)
                : that.whitelistingValueExpression == null;
    }

    @Override
    public int hashCode() {
        return whitelistingValueExpression != null ? whitelistingValueExpression.hashCode() : 0;
    }
}
