/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.parosproxy.paros.extension;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.parosproxy.paros.model.Model;

/** Unit test for {@link ExtensionLoader}. */
class ExtensionLoaderUnitTest {

    private Model model;
    private ExtensionLoader extensionLoader;

    @BeforeEach
    void setUp() {
        model = mock(Model.class);
        extensionLoader = new ExtensionLoader(model, null);
    }

    @Test
    void shouldStopExtensionWhenRemoved() {
        // Given
        Extension extension = mock(Extension.class);
        extensionLoader.addExtension(extension);
        // When
        extensionLoader.removeExtension(extension);
        // Then
        verify(extension).stop();
    }

    @Test
    void shouldDestroyExtensionWhenRemoved() {
        // Given
        Extension extension = mock(Extension.class);
        extensionLoader.addExtension(extension);
        // When
        extensionLoader.removeExtension(extension);
        // Then
        verify(extension).destroy();
    }

    @Test
    void shouldStopBeforeDestroyExtensionWhenRemoved() {
        // Given
        Extension extension = mock(Extension.class);
        extensionLoader.addExtension(extension);
        InOrder inOrder = inOrder(extension);
        // When
        extensionLoader.removeExtension(extension);
        // Then
        inOrder.verify(extension).stop();
        inOrder.verify(extension).destroy();
    }
}
