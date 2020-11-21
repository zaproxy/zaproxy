/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.zaproxy.testutils.AbstractGuiTest;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;

/** GUI test for {@link HttpPanelComponentViewsManager}. */
class HttpPanelComponentViewsManagerGuiTest extends AbstractGuiTest {

    private static final String VIEW_NAME = "name";
    private static final String VIEW_CAPTION = "Plain View";
    private static final String CONFIG_KEY = "configKey";

    private HttpPanelComponentViewsManager manager;

    @BeforeEach
    void createManager() {
        manager = executeInEdt(() -> new HttpPanelComponentViewsManager(CONFIG_KEY));
    }

    @Test
    void shouldThrowExceptionWhenAddingNullView() {
        // Given
        HttpPanelView view = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> manager.addView(view));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenAddingViewWithInvalidName(String name) {
        // Given
        HttpPanelView view = createHttpPanelView(name);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> manager.addView(view));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenAddingViewWithInvalidCaption(String caption) {
        // Given
        HttpPanelView view = createHttpPanelView(VIEW_NAME, caption);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> manager.addView(view));
    }

    @Test
    void shouldThrowExceptionWhenAddingViewWithoutPane() {
        // Given
        JComponent pane = null;
        HttpPanelView view = createHttpPanelView(VIEW_NAME, VIEW_CAPTION, pane);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> manager.addView(view));
    }

    @Test
    void shouldThrowExceptionWhenAddingViewWithoutModel() {
        // Given
        HttpPanelViewModel model = null;
        HttpPanelView view = createHttpPanelView();
        given(view.getModel()).willReturn(model);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> manager.addView(view));
    }

    @Test
    void shouldAddValidView() {
        // Given
        HttpPanelView view = createHttpPanelView();
        // When / Then
        assertDoesNotThrow(() -> executeInEdt(() -> manager.addView(view)));
    }

    private static HttpPanelView createHttpPanelView() {
        return createHttpPanelView(VIEW_NAME);
    }

    private static HttpPanelView createHttpPanelView(String name) {
        return createHttpPanelView(name, VIEW_CAPTION);
    }

    private static HttpPanelView createHttpPanelView(String name, String caption) {
        return createHttpPanelView(name, caption, createJPanel("ViewPanel"));
    }

    private static HttpPanelView createHttpPanelView(String name, String caption, JComponent pane) {
        HttpPanelView view = mock(HttpPanelView.class);
        given(view.getName()).willReturn(name);
        given(view.getCaptionName()).willReturn(caption);
        given(view.getPane()).willReturn(pane);
        given(view.getModel()).willReturn(mock(HttpPanelViewModel.class));
        return view;
    }

    private static JPanel createJPanel(String name) {
        return executeInEdt(
                () -> {
                    JPanel panel = new JPanel();
                    panel.setName(name);
                    panel.add(new JLabel(name));
                    return panel;
                });
    }
}
