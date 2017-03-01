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
// ZAP: 2014/01/08 Created an helper component for common use inside plugins

package org.zaproxy.zap.utils;

/**
 * This class provides the implementation of Hirschberg's
 * algorithm that solves the Longest Common Subsequence (LCS) problem.
 * With some shortcut methods useful to calculate LCS between two strings
 * or the similarity ratio between two strings.
 * This implementation is using Hirschber's algorithm B and algorithm C.
 *
 * @see <a href="https://code.google.com/archive/p/algorithm800/source/default/source">Hirschberg's algorithm implementation of
 *      project algorithm800</a>
 *
 * @author Valentinos Georgiades
 * @author Minh Nguyen
 */
//ZAP
public class HirshbergMatcher {
    
    // Minimum value for comparison ratio
    public static final double MIN_RATIO = 0.0;

    // Maximum value for comparison ratio
    public static final double MAX_RATIO = 1.0;
    
    private static int[] algB(int m, int n, String a, String b) {
        // Step 1
        int[][] k = new int[2][n + 1];
        for (int j = 0; j <= n; j++) {
            k[1][j] = 0;
        }

        // Step 2
        for (int i = 1; i <= m; i++) {
            // Step 3
            for (int j = 0; j <= n; j++) {
                k[0][j] = k[1][j];
            }

            // Step 4
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    k[1][j] = k[0][j - 1] + 1;
                } else {
                    k[1][j] = Math.max(k[1][j - 1], k[0][j]);
                }
            }
        }

        //Step 5
        return k[1];

    }

    private static void algC(StringBuilder sb, int m, int n, String a, String b) {
        int i;
        int j;

        // Step 1
        if (n == 0) {
            // Nothing to do
        } else if (m == 1) {
            for (j = 0; j < n; j++) {
                if (a.charAt(0) == b.charAt(j)) {
                    sb.append(a.charAt(0));
                    break;
                }
            }

        // Step 2
        } else {
            i = (int)Math.floor(((double) m) / 2);

            // Step 3
            int[] l1 = algB(i, n, a.substring(0, i), b);
            int[] l2 = algB(m - i, n, reverseString(a.substring(i)), reverseString(b));

            // Step 4
            int k = findK(l1, l2, n);

            // Step 5
            algC(sb, i, k, a.substring(0, i), b.substring(0, k));
            algC(sb, m - i, n - k, a.substring(i), b.substring(k));
        }
    }

    /**
     * This method takes a string as input reverses it and returns the result
     * 
     * @param in the string to be reversed
     * @return the reversed string
     */
    private static String reverseString(String in) {
        StringBuilder out = new StringBuilder(in).reverse();
        return out.toString();
    }

    private static int findK(int[] l1, int[] l2, int n) {
        int m = 0;
        int k = 0;

        for (int j = 0; j <= n; j++) {
            if (m < (l1[j] + l2[n - j])) {
                m = l1[j] + l2[n - j];
                k = j;
            }
        }

        return k;
    }

    /**
     * Gets the Longest Common Subsequence of two strings, using Dynamic
     * programming techniques, and minimal memory
     * 
     * @param strA the first String
     * @param strB the second String
     * @return the Longest Common Subsequence of strA and strB
     */
    public String getLCS(String strA, String strB) {
        if ("".equals(strA)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        algC(sb, strA.length(), strB.length(), strA, strB);
        return sb.toString();
    }
    
    /**
     * Calculate the ratio of similarity between 2 strings using LCS
     * 
     * @param strA the first String
     * @param strB the second String
     * @return the percentage  double number
     */
    public double getMatchRatio(String strA, String strB) {
        if (strA == null && strB == null) {
            return MAX_RATIO;
            
        } else if (strA == null || strB == null) {
            return MIN_RATIO;
        }
        
        if (strA.isEmpty() && strB.isEmpty()) {
            return MAX_RATIO;
            
        } else if (strA.isEmpty() || strB.isEmpty()) {
            return MIN_RATIO;
        }
                
        //get the percentage match against the longer of the 2 strings
        return (double)getLCS(strA, strB).length() / Math.max(strA.length(), strB.length());
    }    
}
