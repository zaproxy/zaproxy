/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.commons.httpclient.URI;

class KbUnitTest {

    private static final String TEST_KEY = "key";
    private static final String ANOTHER_KEY = "otherKey";
    private static final Object TEST_OBJECT_1 = new Object();
    private static final Object TEST_OBJECT_2 = new Object();
    private static final Boolean TEST_BOOLEAN = Boolean.TRUE;
    private static final String TEST_STRING = "Test";

    Kb knowledgeBase;

    @BeforeEach
    void setUp() throws Exception {
        knowledgeBase = new Kb();
    }

    @Test
    void shouldStoreValueForGivenKey() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
        // Then
        assertThat(knowledgeBase.get(TEST_KEY), is(equalTo(TEST_OBJECT_1)));
    }

    @Test
    void shouldStoreValueForGivenUriAndKey() throws Exception {
    	// Given/When
        URI uriWithQuery = new URI("https://example.com/path?x=1", true);
        URI samePathDifferentQuery = new URI("https://example.com/path?x=2", true);
        knowledgeBase.add(uriWithQuery, TEST_KEY, TEST_OBJECT_1);
        // Then
        assertThat(knowledgeBase.get(samePathDifferentQuery, TEST_KEY), is(equalTo(TEST_OBJECT_1)));
    }

    @Test
    void shouldRetrieveStoredObjectsForGivenKey() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_2);
        // Then
        Vector<Object> result = knowledgeBase.getList(TEST_KEY);
        assertThat(result, hasSize(2));
        assertThat(result, contains(TEST_OBJECT_1, TEST_OBJECT_2));
    }

    @Test
    void shouldRetrieveStoredObjectsForGivenUriAndKey() throws Exception {
    	// Given/When
        URI uriWithQuery = new URI("https://example.com/path?x=1", true);
        URI samePathDifferentQuery = new URI("https://example.com/path?x=2", true);
        knowledgeBase.add(uriWithQuery, TEST_KEY, TEST_OBJECT_1);
        knowledgeBase.add(uriWithQuery, TEST_KEY, TEST_OBJECT_2);
        // Then
        Vector<Object> result = knowledgeBase.getList(samePathDifferentQuery, TEST_KEY);
        assertThat(result, hasSize(2));
        assertThat(result, contains(TEST_OBJECT_1, TEST_OBJECT_2));
    }

    @Test
    void shouldRetrieveStoredBooleanForGivenKey() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_BOOLEAN);
        // Then
        assertThat(knowledgeBase.getBoolean(TEST_KEY), is(equalTo(TEST_BOOLEAN)));
    }

    @Test
    void shouldRetrieveStoredBooleanForGivenUriAndKey() throws Exception {
    	// Given/When
        URI uri = new URI("https://example.com/path?x=1", true);
        URI samePathDifferentQuery = new URI("https://example.com/path?x=2", true);
        knowledgeBase.add(uri, TEST_KEY, TEST_BOOLEAN);
        // Then
        assertThat(knowledgeBase.getBoolean(samePathDifferentQuery, TEST_KEY), is(equalTo(TEST_BOOLEAN)));
    }

    @Test
    void shouldRetrieveStoredStringForGivenKey() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_STRING);
        // Then
        assertThat(knowledgeBase.getString(TEST_KEY), is(equalTo(TEST_STRING)));
    }

    @Test
    void shouldRetrieveStoredStringForGivenUriAndKey() throws Exception {
    	// Given/When
        URI uri = new URI("https://example.com/path?x=1", true);
        URI samePathDifferentQuery = new URI("https://example.com/path?x=2", true);
        knowledgeBase.add(uri, TEST_KEY, TEST_STRING);
        // Then
        assertThat(knowledgeBase.getString(samePathDifferentQuery, TEST_KEY), is(equalTo(TEST_STRING)));
    }

    @Test
    void shouldReturnNullWhenGivenKeyHasNoStoredValue() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
        // Then
        assertThat(knowledgeBase.get(ANOTHER_KEY), is(nullValue()));
    }

    @Test
    void shouldReturnFalseWhenRetrievingNonBooleanValueAsBoolean() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
        // Then
        assertThat(knowledgeBase.getBoolean(TEST_KEY), is(false));
    }

    @Test
    void shouldReturnNullWhenRetrievingNonStringValueAsString() {
        // Given/When
        knowledgeBase.add(TEST_KEY, TEST_OBJECT_1);
        // Then
        assertThat(knowledgeBase.getString(TEST_KEY), is(nullValue()));
    }
}
