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
package org.zaproxy.zap.extension.ext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.zaproxy.zap.extension.ext.ExtensionParam.ExtensionState;

/**
 * Unit test for {@link ExtensionState}.
 */
public class ExtensionStateUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateExtensionStateWithNullName() {
        // Given / When
        new ExtensionState(null, true);
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldCreateExtensionState() {
        // Given / When
        ExtensionState extensionState = new ExtensionState("Extension", true);
        // Then
        assertThat(extensionState.getName(), is(equalTo("Extension")));
        assertThat(extensionState.isEnabled(), is(equalTo(true)));
    }

    @Test
    public void shouldCreateExtensionStateWithDisabledState() {
        // Given / When
        ExtensionState extensionState = new ExtensionState("Extension", false);
        // Then
        assertThat(extensionState.getName(), is(equalTo("Extension")));
        assertThat(extensionState.isEnabled(), is(equalTo(false)));
    }

    @Test
    public void shouldProduceConsistentHashCodes() {
        // Given
        ExtensionState[] extensionState = {
                new ExtensionState("Extension", true),
                new ExtensionState("Extension", false),
                new ExtensionState("OtherExtensionName", true) };
        int[] expectedHashCodes = { 1391449329, 1391449515, 1952437036 };
        for (int i = 0; i < extensionState.length; i++) {
            // When / Then
            assertThat(extensionState[i].hashCode(), is(equalTo(expectedHashCodes[i])));
        }
    }

    @Test
    public void shouldBeEqualToItself() {
        // Given
        ExtensionState extensionState = new ExtensionState("Extension", true);
        // When
        boolean equals = extensionState.equals(extensionState);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldBeEqualToDifferentExtensionStateWithSameContents() {
        // Given
        ExtensionState extensionState = new ExtensionState("Extension", true);
        ExtensionState otherEqualExtensionState = new ExtensionState("Extension", true);
        // When
        boolean equals = extensionState.equals(otherEqualExtensionState);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        // Given
        ExtensionState extensionState = new ExtensionState("Extension", true);
        // When
        boolean equals = extensionState.equals(null);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToExtensionStateWithJustDifferentName() {
        // Given
        ExtensionState extensionState = new ExtensionState("Extension", true);
        ExtensionState otherExtensionState = new ExtensionState("OtherExtensionName", true);
        // When
        boolean equals = extensionState.equals(otherExtensionState);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldNotBeEqualToExtensionStateWithJustDifferentState() {
        // Given
        ExtensionState extensionState = new ExtensionState("Extension", true);
        ExtensionState otherExtensionState = new ExtensionState("Extension", false);
        // When
        boolean equals = extensionState.equals(otherExtensionState);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    public void shouldProduceConsistentStringRepresentations() {
        // Given
        ExtensionState[] extensionStates = {
                new ExtensionState("Extension", false),
                new ExtensionState("Extension", true),
                new ExtensionState("OtherExtension", false) };
        String[] expectedStringRepresentations = {
                "[Name=Extension, Enabled=false]",
                "[Name=Extension, Enabled=true]",
                "[Name=OtherExtension, Enabled=false]" };
        for (int i = 0; i < extensionStates.length; i++) {
            // When / Then
            assertThat(extensionStates[i].toString(), is(equalTo(expectedStringRepresentations[i])));
        }
    }

}
