/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link HrefTypeInfo}. */
class HrefTypeInfoUnitTest {

    private static final int TEST_TYPE = 1234;

    @BeforeEach
    void setUp() {
        Constant.messages = mock(I18N.class);

        HrefTypeInfo.values.remove(TEST_TYPE);
    }

    @Test
    void shouldAddTypeInfo() {
        // Given
        int type = TEST_TYPE;
        HrefTypeInfo typeInfo = new HrefTypeInfo(type, "Name");
        // When
        HrefTypeInfo.addType(typeInfo);
        // Then
        assertThat(HrefTypeInfo.getFromType(type), is(sameInstance(typeInfo)));
    }

    @Test
    void shouldNotAddSameType() {
        // Given
        int type = TEST_TYPE;
        HrefTypeInfo typeInfo = new HrefTypeInfo(type, "Name");
        HrefTypeInfo otherTypeInfo = new HrefTypeInfo(type, "Other Name");
        // When
        HrefTypeInfo.addType(typeInfo);
        HrefTypeInfo.addType(otherTypeInfo);
        // Then
        assertThat(HrefTypeInfo.getFromType(type), is(sameInstance(typeInfo)));
    }

    @Test
    void shouldThrowWhenAddingNullType() {
        // Given
        HrefTypeInfo typeInfo = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> HrefTypeInfo.addType(typeInfo));
    }

    @Test
    void shouldRemoveTypeInfo() {
        // Given
        int type = TEST_TYPE;
        HrefTypeInfo typeInfo = new HrefTypeInfo(type, "Name");
        HrefTypeInfo.addType(typeInfo);
        // When
        HrefTypeInfo.removeType(typeInfo);
        // Then
        assertThat(HrefTypeInfo.getFromType(type), is(HrefTypeInfo.UNDEFINED_TYPE));
    }

    @Test
    void shouldThrowWhenRemovingNullType() {
        // Given
        HrefTypeInfo typeInfo = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> HrefTypeInfo.removeType(typeInfo));
    }

    @Test
    void shouldNotOverrideNoType() {
        // Given
        int type = HrefTypeInfo.NO_TYPE.getType();
        HrefTypeInfo typeInfo = new HrefTypeInfo(type, "Name");
        // When
        HrefTypeInfo.addType(typeInfo);
        // Then
        assertThat(HrefTypeInfo.getFromType(type), is(sameInstance(HrefTypeInfo.NO_TYPE)));
    }

    @Test
    void shouldNotOverrideUndefinedType() {
        // Given
        int type = HrefTypeInfo.UNDEFINED_TYPE.getType();
        HrefTypeInfo typeInfo = new HrefTypeInfo(type, "Name");
        // When
        HrefTypeInfo.addType(typeInfo);
        // Then
        assertThat(HrefTypeInfo.getFromType(type), is(sameInstance(HrefTypeInfo.UNDEFINED_TYPE)));
    }
}
