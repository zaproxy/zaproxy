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
package org.zaproxy.zap.extension.uiutils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.extension.ViewDelegate;

/** Unit test for {@link ExtensionUiUtils}. */
class ExtensionUiUtilsUnitTest {

    private ExtensionUiUtils extension;

    @BeforeEach
    void setup() {
        extension = new ExtensionUiUtils();
    }

    @Test
    void shouldNotHaveViewByDefault() {
        // Given / When
        ViewDelegate view = extension.getView();
        // When
        assertThat(view, is(nullValue()));
    }

    @Test
    void shouldHaveViewAfterInitView() {
        // Given
        ViewDelegate view = mock(ViewDelegate.class);
        // When
        extension.initView(view);
        // When
        assertThat(extension.getView(), is(equalTo(view)));
    }
}
