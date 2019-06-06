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
package org.zaproxy.zap.utils;

import java.util.regex.Pattern;

/**
 * Utility methods to manipulate {@code String}s, mainly shown in UI.
 *
 * <p>Allows to show whitespace characters (spaces, newlines and tabs).
 *
 * @since 2.4.0
 */
public final class StringUIUtils {

    public static final String SPACE_SYMBOL = "\u00b7";
    public static final String TAB_SYMBOL = "\u00bb";
    public static final String CARRIAGE_RETURN_SYMBOL = "\u00a4";
    public static final String LINE_FEED_SYMBOL = "\u00b6";

    private StringUIUtils() {}

    /**
     * Returns a string with visible whitespace characters, removing the invisible ones.
     *
     * <p>It's replaced carriage return, line feed, tabs and spaces with visible (replacement)
     * characters.
     *
     * @param string the source string
     * @return the new string with visible new line characters
     */
    public static String replaceWithVisibleWhiteSpaceChars(String string) {
        return string.replaceAll("\r", CARRIAGE_RETURN_SYMBOL)
                .replaceAll("\n", LINE_FEED_SYMBOL)
                .replaceAll("\\t", TAB_SYMBOL)
                .replaceAll(" ", SPACE_SYMBOL);
    }

    /**
     * Returns a string with visible new line characters, shown along the invisible ones.
     *
     * @param string the source string
     * @return the new string with visible new line characters
     */
    public static String addVisibleNewLineChars(String string) {
        return string.replaceAll("\r", CARRIAGE_RETURN_SYMBOL + "\r")
                .replaceAll("\n", LINE_FEED_SYMBOL + "\n");
    }

    /**
     * Tells whether or not the given {@code string} contains or not new line characters (both line
     * feed as carriage return).
     *
     * @param string the string that will be tested
     * @return {@code true} if the string contains at least one new line character, {@code false}
     *     otherwise.
     */
    public static boolean containsNewLineChars(String string) {
        return Pattern.compile("\\r?\\n").matcher(string).find();
    }
}
