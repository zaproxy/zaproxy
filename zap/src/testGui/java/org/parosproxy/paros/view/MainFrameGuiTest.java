/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.parosproxy.paros.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.assertj.swing.core.GenericTypeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.zaproxy.testutils.AbstractZapGuiTest;

/** GUI tests for {@link MainFrame}. */
class MainFrameGuiTest extends AbstractZapGuiTest {

    private OptionsParam options;
    private AbstractPanel requestPanel;
    private AbstractPanel responsePanel;

    @BeforeEach
    void setUpPanels() {
        options = Model.getSingleton().getOptionsParam();
        options.getViewParam().setShowMainToolbar(true);
        options.getViewParam().setDisplayOption(WorkbenchPanel.Layout.EXPAND_STATUS.getId());
        options.getViewParam()
                .setResponsePanelPosition(
                        WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE.toString());
        options.getViewParam().setShowTabNames(true);
        requestPanel = createPanel("requestPanel");
        responsePanel = createPanel("responsePanel");
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullOptions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> executeInEdt(() -> new MainFrame(null, requestPanel, responsePanel)));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullRequestPanel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> executeInEdt(() -> new MainFrame(options, null, responsePanel)));
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNullResponsePanel() {
        assertThrows(
                IllegalArgumentException.class,
                () -> executeInEdt(() -> new MainFrame(options, requestPanel, null)));
    }

    @Test
    void shouldInitializeMainWindowComponents() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            assertNotNull(mainFrame.getMainMenuBar());
            assertNotNull(mainFrame.getMainToolbarPanel());
            assertNotNull(mainFrame.getMainFooterPanel());
            assertNotNull(mainFrame.getWorkbench());
            assertSame(mainFrame.getWorkbench(), mainFrame.getPaneDisplay().getComponent(0));
            assertEquals(WorkbenchPanel.Layout.EXPAND_STATUS, mainFrame.getWorkbenchLayout());
            assertEquals(
                    WorkbenchPanel.ResponsePanelPosition.TABS_SIDE_BY_SIDE,
                    mainFrame.getResponsePanelPosition());
            assertTrue(mainFrame.getMainToolbarPanel().isVisible());
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    @Test
    void shouldApplyViewOptionsFromConfig() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            options.getViewParam().setShowMainToolbar(false);
            options.getViewParam().setDisplayOption(WorkbenchPanel.Layout.FULL.getId());
            options.getViewParam()
                    .setResponsePanelPosition(
                            WorkbenchPanel.ResponsePanelPosition.TAB_SIDE_BY_SIDE.toString());
            options.getViewParam().setShowTabNames(false);

            executeInEdt(mainFrame::applyViewOptions);

            assertEquals(WorkbenchPanel.Layout.FULL, mainFrame.getWorkbenchLayout());
            assertEquals(
                    WorkbenchPanel.ResponsePanelPosition.TAB_SIDE_BY_SIDE,
                    mainFrame.getResponsePanelPosition());
            assertFalse(mainFrame.getMainToolbarPanel().isVisible());
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    @Test
    void shouldPersistLayoutAndResponsePanelPositionChanges() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            executeInEdt(() -> mainFrame.setWorkbenchLayout(WorkbenchPanel.Layout.EXPAND_SELECT));
            assertEquals(WorkbenchPanel.Layout.EXPAND_SELECT, mainFrame.getWorkbenchLayout());
            assertEquals(
                    WorkbenchPanel.Layout.EXPAND_SELECT.getId(),
                    options.getViewParam().getDisplayOption());

            executeInEdt(
                    () ->
                            mainFrame.setResponsePanelPosition(
                                    WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE));
            assertEquals(
                    WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE,
                    mainFrame.getResponsePanelPosition());
            assertEquals(
                    WorkbenchPanel.ResponsePanelPosition.PANELS_SIDE_BY_SIDE.toString(),
                    options.getViewParam().getResponsePanelPosition());
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    @Test
    void shouldSelectLayoutButtonsWhenLayoutChanges() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            executeInEdt(
                    () -> {
                        mainFrame.pack();
                        mainFrame.setVisible(true);
                    });
            executeInEdt(() -> mainFrame.setWorkbenchLayout(WorkbenchPanel.Layout.FULL));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(mainFrame, "main.toolbar.toggle.full")
                                            .isSelected()));

            executeInEdt(() -> mainFrame.setWorkbenchLayout(WorkbenchPanel.Layout.EXPAND_SELECT));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame, "main.toolbar.toggle.expandSelect")
                                            .isSelected()));
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    @Test
    void shouldToggleResponsePanelButtonsEnabledForFullLayout() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            executeInEdt(
                    () -> {
                        mainFrame.pack();
                        mainFrame.setVisible(true);
                    });
            executeInEdt(() -> mainFrame.setWorkbenchLayout(WorkbenchPanel.Layout.FULL));
            assertFalse(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame,
                                                    "main.toolbar.toggle.response.panelsSideBySide")
                                            .isEnabled()));
            assertFalse(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame, "main.toolbar.toggle.response.above")
                                            .isEnabled()));

            executeInEdt(() -> mainFrame.setWorkbenchLayout(WorkbenchPanel.Layout.EXPAND_STATUS));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame,
                                                    "main.toolbar.toggle.response.panelsSideBySide")
                                            .isEnabled()));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame, "main.toolbar.toggle.response.above")
                                            .isEnabled()));
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    @Test
    void shouldSelectResponsePanelPositionButtonWhenChanged() {
        MainFrame mainFrame =
                executeInEdt(() -> new MainFrame(options, requestPanel, responsePanel));
        try {
            executeInEdt(
                    () -> {
                        mainFrame.pack();
                        mainFrame.setVisible(true);
                    });
            executeInEdt(
                    () ->
                            mainFrame.setResponsePanelPosition(
                                    WorkbenchPanel.ResponsePanelPosition.PANEL_ABOVE));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame, "main.toolbar.toggle.response.above")
                                            .isSelected()));

            executeInEdt(
                    () ->
                            mainFrame.setResponsePanelPosition(
                                    WorkbenchPanel.ResponsePanelPosition.TAB_SIDE_BY_SIDE));
            assertTrue(
                    executeInEdt(
                            () ->
                                    findToggleButtonByName(
                                                    mainFrame,
                                                    "main.toolbar.toggle.response.tabSideBySide")
                                            .isSelected()));
        } finally {
            executeInEdt(mainFrame::dispose);
        }
    }

    private JToggleButton findToggleButtonByName(MainFrame mainFrame, String name) {
        return robot().finder()
                .findByName(mainFrame.getMainToolbarPanel(), name, JToggleButton.class);
    }

    private JToolBar findToolbar(MainFrame mainFrame) {
        return robot().finder()
                .find(
                        mainFrame.getMainToolbarPanel(),
                        new GenericTypeMatcher<JToolBar>(JToolBar.class) {
                            @Override
                            protected boolean isMatching(JToolBar toolBar) {
                                return "Main Toolbar".equals(toolBar.getName());
                            }
                        });
    }
}
