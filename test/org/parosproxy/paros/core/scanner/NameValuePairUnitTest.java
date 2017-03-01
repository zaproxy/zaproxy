/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link NameValuePair}.
 */
public class NameValuePairUnitTest {

    private static final String NAME = "name";
    private static final String VALUE = "value";

    @Test
    public void shouldCreateNameValuePair() {
        // Given / When
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // Then
        assertThat(nameValuePair.getName(), is(equalTo(NAME)));
        assertThat(nameValuePair.getValue(), is(equalTo(VALUE)));
        assertThat(nameValuePair.getType(), is(equalTo(1)));
        assertThat(nameValuePair.getPosition(), is(equalTo(2)));
    }

    @Test
    public void shouldCreateNameValuePairWithNegativeTypeAndPosition() {
        // Given / When
        NameValuePair nameValuePair = new NameValuePair(-1, NAME, VALUE, -2);
        // Then
        assertThat(nameValuePair.getType(), is(equalTo(-1)));
        assertThat(nameValuePair.getPosition(), is(equalTo(-2)));
    }

    @Test
    public void shouldCreateNameValuePairWithNullNameAndValue() {
        // Given / When
        NameValuePair nameValuePair = new NameValuePair(1, null, null, 2);
        // Then
        assertThat(nameValuePair.getName(), is(nullValue()));
        assertThat(nameValuePair.getValue(), is(nullValue()));
    }

    @Test
    public void shouldSetName() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        String name = "AnotherName";
        // When
        nameValuePair.setName(name);
        // Then
        assertThat(nameValuePair.getName(), is(equalTo(name)));
    }

    @Test
    public void shouldSetValue() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        String value = "AnotherValue";
        // When
        nameValuePair.setValue(value);
        // Then
        assertThat(nameValuePair.getValue(), is(equalTo(value)));
    }

    @Test
    public void shouldSetPosition() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        int position = 10;
        // When
        nameValuePair.setPosition(position);
        // Then
        assertThat(nameValuePair.getPosition(), is(equalTo(position)));
    }

    @Test
    public void shouldProduceConsistentHashCodes() {
        // Given
        NameValuePair[] nameValuePairs = {
                new NameValuePair(1, NAME, VALUE, 2),
                new NameValuePair(3, NAME, null, 4),
                new NameValuePair(5, null, VALUE, 6),
                new NameValuePair(-341, null, null, -950) };
        int[] expectedHashCodes = { 1834755624, 1722784887, 112902163, 0 };
        for (int i = 0; i < nameValuePairs.length; i++) {
            // When / Then
            assertThat(nameValuePairs[i].hashCode(), is(equalTo(expectedHashCodes[i])));
        }
    }

    @Test
    public void shouldBeEqualToItself() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(nameValuePair);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldBeEqualToDifferentNameValuePairWithSameContents() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherEqualNameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherEqualNameValuePair);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldBeEqualToDifferentNameValuePairWithNullNames() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, null, VALUE, 2);
        NameValuePair otherEqualNameValuePair = new NameValuePair(1, null, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherEqualNameValuePair);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldBeEqualToDifferentNameValuePairWithNullValues() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, null, 2);
        NameValuePair otherEqualNameValuePair = new NameValuePair(1, NAME, null, 2);
        // When
        boolean equals = nameValuePair.equals(otherEqualNameValuePair);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(null);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentName() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, "OtherName", VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentNullName() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, null, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentValue() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, NAME, "OtherValue", 2);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentNullValue() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, null, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentType() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(8, NAME, VALUE, 2);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToNameValuePairWithJustDifferentPosition() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, NAME, VALUE, 5);
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToExtendedNameValuePair() {
        // Given
        NameValuePair nameValuePair = new NameValuePair(1, NAME, VALUE, 2);
        NameValuePair otherNameValuePair = new NameValuePair(1, NAME, VALUE, 2) {
            // Anonymous NameValuePair
        };
        // When
        boolean equals = nameValuePair.equals(otherNameValuePair);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldProduceConsistentStringRepresentations() {
        // Given
        NameValuePair[] nameValuePairs = {
                new NameValuePair(1, NAME, VALUE, 2),
                new NameValuePair(3, NAME, null, 4),
                new NameValuePair(5, null, VALUE, 6),
                new NameValuePair(7, null, null, 8) };
        String[] expectedStringRepresentations = {
                "[Position=2, Type=1, Name=" + NAME + ", Value=" + VALUE + "]",
                "[Position=4, Type=3, Name=" + NAME + "]",
                "[Position=6, Type=5, Value=" + VALUE + "]",
                "[Position=8, Type=7]" };
        for (int i = 0; i < nameValuePairs.length; i++) {
            // When / Then
            assertThat(nameValuePairs[i].toString(), is(equalTo(expectedStringRepresentations[i])));
        }
    }

}
