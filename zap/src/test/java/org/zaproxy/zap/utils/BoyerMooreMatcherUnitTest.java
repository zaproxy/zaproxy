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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit test for {@link BoyerMooreMatcher} */
class BoyerMooreMatcherUnitTest {

    private static final String CONTENT = "The quick brown fox jumps over the lazy dog.";

    @Test
    void shouldFailToCreateMatcherIfPatternIsNull() {
        // Given
        String pattern = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new BoyerMooreMatcher(pattern));
    }

    @Test
    void shouldFailToFindInNullContent() {
        // Given
        BoyerMooreMatcher matcher = new BoyerMooreMatcher("");
        // When / Then
        assertThrows(NullPointerException.class, () -> matcher.findInContent(null));
    }

    @Test
    void shouldReturnPatternPassedInConstructor() {
        // Given
        String pattern = "pattern";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        String retrievedPattern = matcher.getPattern();
        // Then
        assertThat(retrievedPattern, is(equalTo(pattern)));
    }

    @Test
    void shouldFindPatternAtBeginOfContent() {
        // Given
        String pattern = "The quick";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    void shouldFindPatternInMiddleOfContent() {
        // Given
        String pattern = "jumps";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(20)));
    }

    @Test
    void shouldFindPatternAtEndOfContent() {
        // Given
        String pattern = "lazy dog.";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(35)));
    }

    @Test
    void shouldFindPatternEqualToContent() {
        // Given
        String pattern = CONTENT;
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    void shouldReturnIdxOfFirstOccurrenceIfPatternRepeated() {
        // Given
        String pattern = "e";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent("A e B e C e D");
        // Then
        assertThat(idxOccurrence, is(equalTo(2)));
    }

    @Test
    void shouldFindEmptyPatternAtIdxZero() {
        // Given
        String pattern = "";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    void shouldNotFindAnNonexistentPattern() {
        // Given
        String pattern = "not in content";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(-1)));
    }
}
