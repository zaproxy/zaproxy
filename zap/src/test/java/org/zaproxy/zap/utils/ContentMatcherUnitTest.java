/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ContentMatcher}. */
class ContentMatcherUnitTest {

    @Test
    void shouldCreateContentMatcherFromFile() throws Exception {
        // Given
        String file = "content-matcher-patterns.xml";
        // When
        ContentMatcher matcher = ContentMatcher.getInstance(file);
        // Then
        assertThat(patternsOf(matcher), contains("Regex 1", "Regex 2"));
        assertThat(stringsOf(matcher), contains("String 1", "String 2"));
    }

    private static List<String> patternsOf(ContentMatcher matcher) {
        return matcher.getPatterns().stream().map(Pattern::pattern).collect(Collectors.toList());
    }

    private static List<String> stringsOf(ContentMatcher matcher) {
        return matcher.getStrings().stream()
                .map(BoyerMooreMatcher::getPattern)
                .collect(Collectors.toList());
    }
}
