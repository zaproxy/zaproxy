/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class CommonUserAgents {

    private static Map<String, String> nameToString = null;
    private static Map<String, String> stringToName = null;

    static {
        nameToString = new HashMap<>();
        stringToName = new HashMap<>();
    }

    public static String getStringFromName(String name) {
        return nameToString.get(name);
    }

    public static String getNameFromString(String str) {
        return stringToName.get(str);
    }

    public static String[] getNames() {
        Set<String> keys = nameToString.keySet();
        String[] names = new String[keys.size()];
        int i = 0;
        for (String key : keys) {
            names[i] = key;
            i++;
        }
        Arrays.sort(names);
        return names;
    }
}
