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
package org.zaproxy.zap.utils;

/** @deprecated use {link HirschbergMatcher} */
@Deprecated
public class HirshbergMatcher {

    /** @deprecated use {link HirschbergMatcher#MIN_RATIO} */
    @Deprecated public static final double MIN_RATIO = 0.0;

    /** @deprecated use {link HirschbergMatcher#MAX_RATIO} */
    @Deprecated public static final double MAX_RATIO = 1.0;

    /** @deprecated use {link HirschbergMatcher#getLCS} */
    @Deprecated
    public String getLCS(String strA, String strB) {
        return new HirschbergMatcher().getLCS(strA, strB);
    }

    /** @deprecated use {link HirschbergMatcher#getMatchRatio} */
    @Deprecated
    public double getMatchRatio(String strA, String strB) {
        return new HirschbergMatcher().getMatchRatio(strA, strB);
    }
}
