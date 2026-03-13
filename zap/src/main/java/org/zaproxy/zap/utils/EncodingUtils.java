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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncodingUtils {

    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            stringBuilder.append(
                    key != null ? Base64.getEncoder().encodeToString(key.getBytes()) : "");
            stringBuilder.append(":");
            stringBuilder.append(
                    value != null ? Base64.getEncoder().encodeToString(value.getBytes()) : "");
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input) {
        Map<String, String> map = new HashMap<>();
        if (input.isEmpty()) {
            return map;
        }

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split(":", 2);
            map.put(
                    new String(Base64.getDecoder().decode(nameValue[0])),
                    nameValue.length > 1
                            ? new String(Base64.getDecoder().decode(nameValue[1]))
                            : "");
        }

        return map;
    }
}
