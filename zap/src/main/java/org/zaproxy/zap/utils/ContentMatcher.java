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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Support utility able to manage multiple string/regex occurrence searching inside a target content
 *
 * @author yhawke 2013
 */
public class ContentMatcher {

    private static final String TAG_PATTERN = "Pattern";
    private static final String ATTRIBUTE_TYPE = "[@type]";
    private static final String TAG_PATTERN_TYPE_REGEX = "regex";

    private List<BoyerMooreMatcher> strings;
    private List<Pattern> patterns;

    /**
     * Direct method for a complete ContentMatcher instance creation. Use the ClassLoader for the
     * resource detection and loading, be careful regarding the relative file name use (this class
     * is in another package).
     *
     * @param xmlFileName the name of the XML file that need to be used for initialization
     * @return a ContentMatcher instance
     */
    public static ContentMatcher getInstance(String xmlFileName) {
        ContentMatcher cm = new ContentMatcher();

        // Load the pattern definitions from an XML file
        try {
            cm.loadXMLPatternDefinitions(cm.getClass().getResourceAsStream(xmlFileName));

        } catch (ConfigurationException ex) {
            throw new IllegalArgumentException(
                    "Failed to initialize the ContentMatcher object using: " + xmlFileName, ex);
        }

        return cm;
    }

    /**
     * Direct method for a complete ContentMatcher instance creation.
     *
     * @param xmlInputStream the stream of the XML file that need to be used for initialization
     * @return a ContentMatcher instance
     */
    public static ContentMatcher getInstance(InputStream xmlInputStream) {
        ContentMatcher cm = new ContentMatcher();

        // Load the pattern definitions from an XML file
        try {
            cm.loadXMLPatternDefinitions(xmlInputStream);

        } catch (ConfigurationException ex) {
            throw new IllegalArgumentException(
                    "Failed to initialize the ContentMatcher object using that stream", ex);
        }

        return cm;
    }

    /**
     * Load a pattern list from an XML formatted file. Pattern should be enclosed around a {@code
     * <Patterns>} tag and should be defined as {@code <Pattern type="xxx"></Pattern>}. Use "regex"
     * to define a Regex formatted pattern or "string" for an exact matching pattern.
     *
     * @param xmlInputStream the {@code InputStream} used to read the patterns
     * @throws ConfigurationException if an error occurred while reading the {@code InputStream}
     */
    protected void loadXMLPatternDefinitions(InputStream xmlInputStream)
            throws ConfigurationException {
        strings = new ArrayList<>();
        patterns = new ArrayList<>();

        ZapXmlConfiguration configuration = new ZapXmlConfiguration(xmlInputStream);
        for (HierarchicalConfiguration entry : configuration.configurationsAt(TAG_PATTERN)) {
            String value = entry.getString("", "");
            if (!value.isEmpty()) {
                if (TAG_PATTERN_TYPE_REGEX.equalsIgnoreCase(entry.getString(ATTRIBUTE_TYPE))) {
                    patterns.add(Pattern.compile(value));
                } else {
                    strings.add(new BoyerMooreMatcher(value));
                }
            }
        }
    }

    /**
     * Search for an occurrence inside a specific content
     *
     * @param content the string content to search into
     * @return the found occurrence or null if no match has been done
     */
    public String findInContent(String content) {

        // First check for a simple exact occurrence
        for (BoyerMooreMatcher matcher : strings) {
            if (matcher.findInContent(content) >= 0) return matcher.getPattern();
        }

        // Then check for a regex occurrence
        Matcher matcher;
        for (Pattern pattern : patterns) {
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        // No match found return null
        return null;
    }

    /**
     * Search for all possible occurrences inside a specific content
     *
     * @param content the string content to search into
     * @return a list of existing occurrences
     */
    public List<String> findAllInContent(String content) {

        List<String> results = new LinkedList<>();

        // First check for all simple exact occurrences
        for (BoyerMooreMatcher matcher : strings) {
            if (matcher.findInContent(content) >= 0) results.add(matcher.getPattern());
        }

        // Then check for all regex occurrences
        Matcher matcher;
        for (Pattern pattern : patterns) {
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                results.add(content);
            }
        }

        return results;
    }

    // Provided for tests.
    List<Pattern> getPatterns() {
        return patterns;
    }

    // Provided for tests.
    List<BoyerMooreMatcher> getStrings() {
        return strings;
    }
}
