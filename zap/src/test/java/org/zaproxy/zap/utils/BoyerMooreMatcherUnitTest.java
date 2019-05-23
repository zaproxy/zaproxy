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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link BoyerMooreMatcher}
 */
public class BoyerMooreMatcherUnitTest {

    private static final String CONTENT = "The quick brown fox jumps over the lazy dog.";

    @Test(expected = NullPointerException.class)
    public void shouldFailToCreateMatcherIfPatternIsNull() {
        // Given / When
        new BoyerMooreMatcher(null);
        // Then = NullPointerException.class
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToFindInNullContent() {
        // Given
        BoyerMooreMatcher matcher = new BoyerMooreMatcher("");
        // When
        matcher.findInContent(null);
        // Then = NullPointerException.class
    }

    @Test
    public void shouldReturnPatternPassedInCronstructor() {
        // Given
        String pattern = "pattern";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        String retrievedPattern = matcher.getPattern();
        // Then
        assertThat(retrievedPattern, is(equalTo(pattern)));
    }

    @Test
    public void shouldFindPatternAtBeginOfContent() {
        // Given
        String pattern = "The quick";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    public void shouldFindPatternInMiddleOfContent() {
        // Given
        String pattern = "jumps";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(20)));
    }

    @Test
    public void shouldFindPatternAtEndOfContent() {
        // Given
        String pattern = "lazy dog.";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(35)));
    }

    @Test
    public void shouldFindPatternEqualToContent() {
        // Given
        String pattern = CONTENT;
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    public void shouldReturnIdxOfFirstOccurrenceIfPatternRepeated() {
        // Given
        String pattern = "e";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent("A e B e C e D");
        // Then
        assertThat(idxOccurrence, is(equalTo(2)));
    }

    @Test
    public void shouldFindEmptyPatternAtIdxZero() {
        // Given
        String pattern = "";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(0)));
    }

    @Test
    public void shouldNotFindAnInexistentPattern() {
        // Given
        String pattern = "not in content";
        BoyerMooreMatcher matcher = new BoyerMooreMatcher(pattern);
        // When
        int idxOccurrence = matcher.findInContent(CONTENT);
        // Then
        assertThat(idxOccurrence, is(equalTo(-1)));
    }
}
