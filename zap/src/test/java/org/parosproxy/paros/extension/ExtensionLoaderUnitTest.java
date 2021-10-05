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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;

/** Unit test for {@link ExtensionLoader}. */
class ExtensionLoaderUnitTest {

    private Model model;
    private ExtensionLoader extensionLoader;

    @BeforeEach
    void setUp() {
        model = mock(Model.class);
        extensionLoader = new ExtensionLoader(model, null);

        Control.initSingletonForTesting(model, extensionLoader);
    }

    @Test
    void shouldNotInitViewWhenStartingExtensionWithoutView() throws Exception {
        // Given
        Extension extension = mock(Extension.class);
        // When
        extensionLoader.startLifeCycle(extension);
        // Then
        verify(extension, times(0)).initView(any());
    }

    @Test
    void shouldInitViewWhenStartingExtensionWithView() throws Exception {
        // Given
        View view = mock(View.class);
        extensionLoader = new ExtensionLoader(model, view);
        Extension extension = mock(Extension.class);
        // When
        extensionLoader.startLifeCycle(extension);
        // Then
        verify(extension).initView(view);
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

    @Test
    void shouldGetUnsavedResourcesFromExtensions() {
        // Given
        Extension extensionA = mock(Extension.class);
        given(extensionA.getUnsavedResources()).willReturn(Arrays.asList("Resource A"));
        extensionLoader.addExtension(extensionA);
        Extension extensionB = mock(Extension.class);
        given(extensionB.getUnsavedResources()).willReturn(null);
        extensionLoader.addExtension(extensionB);
        Extension extensionC = mock(Extension.class);
        given(extensionC.getUnsavedResources()).willReturn(Arrays.asList("Resource C"));
        extensionLoader.addExtension(extensionC);
        // When
        List<String> resources = extensionLoader.getUnsavedResources();
        // Then
        assertThat(resources, contains("Resource A", "Resource C"));
    }

    @Test
    void shouldGetUnsavedResourcesFromExtensionsWhileHandlingErrors() {
        // Given
        Extension extensionA = mock(Extension.class);
        given(extensionA.getUnsavedResources()).willAnswer(this::throwThrowable);
        extensionLoader.addExtension(extensionA);
        Extension extensionB = mock(Extension.class);
        given(extensionB.getUnsavedResources()).willReturn(null);
        extensionLoader.addExtension(extensionB);
        Extension extensionC = mock(Extension.class);
        given(extensionC.getUnsavedResources()).willReturn(Arrays.asList("Resource C"));
        extensionLoader.addExtension(extensionC);
        // When
        List<String> resources = extensionLoader.getUnsavedResources();
        // Then
        assertThat(resources, contains("Resource C"));
    }

    @Test
    void shouldGetActiveActionsFromExtensions() {
        // Given
        Extension extensionA = mock(Extension.class);
        given(extensionA.getActiveActions()).willReturn(Arrays.asList("Action A"));
        extensionLoader.addExtension(extensionA);
        Extension extensionB = mock(Extension.class);
        given(extensionB.getActiveActions()).willReturn(null);
        extensionLoader.addExtension(extensionB);
        Extension extensionC = mock(Extension.class);
        given(extensionC.getActiveActions()).willReturn(Arrays.asList("Action C"));
        extensionLoader.addExtension(extensionC);
        // When
        List<String> resources = extensionLoader.getActiveActions();
        // Then
        assertThat(resources, contains("Action A", "Action C"));
    }

    @Test
    void shouldGetActiveActionsFromExtensionsWhileHandlingErrors() {
        // Given
        Extension extensionA = mock(Extension.class);
        given(extensionA.getActiveActions()).willAnswer(this::throwThrowable);
        extensionLoader.addExtension(extensionA);
        Extension extensionB = mock(Extension.class);
        given(extensionB.getActiveActions()).willReturn(null);
        extensionLoader.addExtension(extensionB);
        Extension extensionC = mock(Extension.class);
        given(extensionC.getActiveActions()).willReturn(Arrays.asList("Action C"));
        extensionLoader.addExtension(extensionC);
        // When
        List<String> actions = extensionLoader.getActiveActions();
        // Then
        assertThat(actions, contains("Action C"));
    }

    private <T> T throwThrowable(T arg) throws Throwable {
        throw new Throwable();
    }
}
