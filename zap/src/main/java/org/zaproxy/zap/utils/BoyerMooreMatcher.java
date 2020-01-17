/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.util.HashMap;
import java.util.Map;

/**
 * Reads in two strings, the pattern and the input text, and searches for the pattern in the input
 * text using the bad-character rule part of the Boyer-Moore algorithm.
 *
 * <p>Adapted from the implementation found in:
 * http://www.params.me/2013/06/boyer-moore-string-search.html
 *
 * @author yhawke 2013
 */
public class BoyerMooreMatcher {

    private Map<Character, Integer> occurrence;
    private String pattern;

    /**
     * Prepare the Matcher with the string that need to be searched
     *
     * @param pattern the pattern we've to search for
     */
    public BoyerMooreMatcher(String pattern) {
        // Create internal structures
        this.pattern = pattern;
        this.occurrence = new HashMap<>();

        // Bad character Skip : Moore Table Construction
        for (int i = 0; i < pattern.length(); i++) {
            occurrence.put(pattern.charAt(i), i);
        }
    }

    /**
     * Returns the index within this string of the first occurrence of the specified substring. If
     * it is not a substring, return -1.
     *
     * @param content the content where we've to search into
     * @return the index of the occurrence or -1 if no occurrence has been found
     */
    public int findInContent(String content) {
        int n = content.length();
        int m = pattern.length();
        int skip;
        char val;

        for (int i = 0; i <= n - m; i = i + skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                if (pattern.charAt(j) != content.charAt(i + j)) {
                    val = content.charAt(i + j);

                    skip =
                            (occurrence.get(val) != null)
                                    ? Math.max(1, j - occurrence.get(val))
                                    : j + 1;

                    break;
                }
            }

            if (skip == 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get back the pattern used by this matcher
     *
     * @return the string pattern
     */
    public String getPattern() {
        return pattern;
    }
}
