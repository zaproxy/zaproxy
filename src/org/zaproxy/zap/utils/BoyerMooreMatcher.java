/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.utils;

/**
 * Reads in two strings, the pattern and the input text, and
 * searches for the pattern in the input text using the
 * bad-character rule part of the Boyer-Moore algorithm.
 * 
 * Adapded from the implementation found in:
 * https://weblogs.java.net/blog/potty/archive/2012/05/21/string-searching-algorithms-part-iii
 * 
 * @author yhawke 2013
 */
public class BoyerMooreMatcher {

    private final static int BASE = 256;
    private int[] occurrence;
    private String pattern;

    /**
     * Prepare the Matcher with the string that need to be searched
     * @param pattern the pattern we've to search for
     */
    public BoyerMooreMatcher(String pattern) {
        // Create internal structures
        this.pattern = pattern;
        this.occurrence = new int[BASE];
        
        // Prepare the jump table
        for (int c = 0; c < BASE; c++) {
            occurrence[c] = -1;
        }

        // Fill the internal jump table
        for (int j = 0; j < pattern.length(); j++) {
            occurrence[pattern.charAt(j)] = j;
        }
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. If it is not a substring, return -1.
     * 
     * @param content the content where we've to search into
     * @return the index of the occurrence or -1 if no occurrence has been found
     */
    public int findInContent(String content) {
        int n = content.length();
        int m = pattern.length();
        int skip;
        
        for (int i = 0; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                if (pattern.charAt(j) != content.charAt(i + j)) {
                    skip = Math.max(1, j - occurrence[content.charAt(i + j)]);
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
     * @return the string pattern
     */
    public String getPattern() {
        return pattern;
    }    
}
