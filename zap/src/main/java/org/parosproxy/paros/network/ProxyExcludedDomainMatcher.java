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
package org.parosproxy.paros.network;

import java.util.regex.Pattern;
import org.zaproxy.zap.network.DomainMatcher;

/**
 * Class that contains rules to check if a domain is excluded from proxy.
 *
 * <p>It supports both plain text and regular expression checks.
 *
 * @see #matches(String)
 * @deprecated (2.6.0) Replaced By {@link org.zaproxy.zap.network.DomainMatcher}
 */
@Deprecated
public class ProxyExcludedDomainMatcher extends DomainMatcher {

    public ProxyExcludedDomainMatcher(Pattern pattern) {
        super(pattern);
    }

    public ProxyExcludedDomainMatcher(String domain) {
        super(domain);
    }

    public ProxyExcludedDomainMatcher(ProxyExcludedDomainMatcher other) {
        super(other);
    }
}
