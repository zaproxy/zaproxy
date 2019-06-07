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
package org.zaproxy.zap.extension.search;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Interface for search for patterns in contents of HTTP messages.
 *
 * @since 2.4.0
 */
public interface HttpSearcher {

    /**
     * Returns a descriptive name of the searcher, to be shown in UI components. The name must be
     * internationalised.
     *
     * @return the name of the searcher, never {@code null}
     */
    String getName();

    /**
     * Searches the given pattern in the messages, possibly returning the inverse of the matches.
     *
     * <p>The exact HTTP message contents that will be matched depend on the implementations, for
     * example, some might search only in requests others in responses.
     *
     * @param pattern the pattern to search in the messages
     * @param inverse if should return the inverse of the matches
     * @return a {@code List} containing the results of the search
     */
    List<SearchResult> search(Pattern pattern, boolean inverse);

    /**
     * Searches the given pattern in the messages, possibly returning the inverse of the matches, up
     * to the given maximum of matches.
     *
     * <p>The exact HTTP message contents that will be matched depend on the implementations, for
     * example, some might search only in requests others in responses.
     *
     * @param pattern the pattern to search in the messages
     * @param inverse if should return the inverse of the matches
     * @param maximumMatches the maximum of matches that should be returned
     * @return a {@code List} containing the results of the search
     */
    List<SearchResult> search(Pattern pattern, boolean inverse, int maximumMatches);
}
