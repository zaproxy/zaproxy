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
package org.zaproxy.zap.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.regexp.RegexpMatcher;
import net.sf.json.regexp.RegexpUtils;
import net.sf.json.util.JSONUtils;

/**
 * Utilities to workaround "quirks" of {@link JSONObject} and related classes.
 *
 * @since 2.8.0
 */
public final class JsonUtil {

    private JsonUtil() {}

    private static final String FUNCTION_PATTERN = "^function[ ]?\\(.*\\)[ ]?\\{.*\\}$";
    private static final RegexpMatcher FUNCTION_MACTHER = RegexpUtils.getMatcher(FUNCTION_PATTERN);

    /**
     * net.sf.json.util.JSONUtils tries to be clever and detects things which look like JS
     * functions. This breaks our API :( Regex taken from
     * http://json-lib.sourceforge.net/apidocs/jdk15/net/sf/json/util/JSONUtils.html
     *
     * @param value
     * @return
     */
    private static boolean isFunction(String value) {
        return FUNCTION_MACTHER.matches(value);
    }

    /**
     * Gets the given value in a form that can be safely put in a {@code JSONObject}.
     *
     * <p>{@code JSONObject} automatically parses strings that look like JSON arrays/objects, so
     * they need to be processed (quoted) to prevent that behaviour.
     *
     * @param value the value to process.
     * @return the value that can be safely put in a {@code JSONObject}.
     */
    public static String getJsonFriendlyString(String value) {
        if (!"null".equals(value) && (JSONUtils.mayBeJSON(value) || isFunction(value))) {
            return "'" + value + "'";
        }
        return value;
    }

    public static List<String> toStringList(JSONArray array) {
        List<String> list = new ArrayList<>();
        Iterator<?> iter = JSONArray.toCollection(array).iterator();
        while (iter.hasNext()) {
            list.add(iter.next().toString());
        }
        return list;
    }
}
