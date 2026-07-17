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
package org.zaproxy.zap.extension.keyboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.ZapAction;

/** Unit test for {@link ExtensionKeyboard}. */
class ExtensionKeyboardUnitTest {

    private static final String IDENTIFIER = "test.action";
    private static final KeyStroke DEFAULT_ACCELERATOR =
            KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.CTRL_DOWN_MASK);

    private ExtensionKeyboard extension;

    @BeforeEach
    void setUp() throws Exception {
        extension = new ExtensionKeyboard();
        extension.getKeyboardParam().load(new ZapXmlConfiguration());
    }

    @Test
    void shouldBindRegisteredActionToComponent() {
        JPanel panel = new JPanel();
        ZapAction action = new ZapAction(IDENTIFIER, "Test Action", DEFAULT_ACCELERATOR);

        extension.registerAction(action, panel, JComponent.WHEN_FOCUSED, "Test Scope");

        assertThat(
                panel.getInputMap(JComponent.WHEN_FOCUSED).get(DEFAULT_ACCELERATOR),
                is(equalTo(IDENTIFIER)));
        assertThat(panel.getActionMap().get(IDENTIFIER), is(equalTo(action)));
        assertThat(extension.getShortcut(IDENTIFIER), is(equalTo(DEFAULT_ACCELERATOR)));
    }

    @Test
    void shouldRebindActionWhenShortcutChanges() {
        JPanel panel = new JPanel();
        ZapAction action = new ZapAction(IDENTIFIER, "Test Action", DEFAULT_ACCELERATOR);
        KeyStroke newAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
        extension.registerAction(action, panel, JComponent.WHEN_FOCUSED, "Test Scope");

        extension.setShortcut(IDENTIFIER, newAccelerator);

        assertThat(
                panel.getInputMap(JComponent.WHEN_FOCUSED).get(DEFAULT_ACCELERATOR),
                is(nullValue()));
        assertThat(
                panel.getInputMap(JComponent.WHEN_FOCUSED).get(newAccelerator),
                is(equalTo(IDENTIFIER)));
        assertThat(extension.getShortcut(IDENTIFIER), is(equalTo(newAccelerator)));
    }

    @Test
    void shouldLoadConfiguredShortcutFromParam() throws Exception {
        JPanel panel = new JPanel();
        ZapAction action = new ZapAction(IDENTIFIER, "Test Action", DEFAULT_ACCELERATOR);
        KeyStroke configuredAccelerator =
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK);
        ZapXmlConfiguration configuration = new ZapXmlConfiguration();
        configuration.setProperty("keyboard.shortcuts(0).menu", IDENTIFIER);
        configuration.setProperty(
                "keyboard.shortcuts(0).keycode", configuredAccelerator.getKeyCode());
        configuration.setProperty(
                "keyboard.shortcuts(0).modifiers", configuredAccelerator.getModifiers());
        extension.getKeyboardParam().load(configuration);

        extension.registerAction(action, panel, JComponent.WHEN_FOCUSED, "Test Scope");

        assertThat(
                panel.getInputMap(JComponent.WHEN_FOCUSED).get(configuredAccelerator),
                is(equalTo(IDENTIFIER)));
        assertThat(extension.getShortcut(IDENTIFIER), is(equalTo(configuredAccelerator)));
    }

    @Test
    void shouldNotifyAcceleratorChangeListenerOnRegisterAndShortcutChange() {
        JPanel panel = new JPanel();
        ZapAction action = new ZapAction(IDENTIFIER, "Test Action", DEFAULT_ACCELERATOR);
        List<KeyStroke> notifiedAccelerators = new ArrayList<>();
        KeyStroke newAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);

        KeyStroke registeredAccelerator =
                extension.registerAction(
                        action,
                        panel,
                        JComponent.WHEN_FOCUSED,
                        "Test Scope",
                        notifiedAccelerators::add);

        assertThat(registeredAccelerator, is(equalTo(DEFAULT_ACCELERATOR)));
        assertThat(notifiedAccelerators, is(equalTo(List.of(DEFAULT_ACCELERATOR))));

        extension.setShortcut(IDENTIFIER, newAccelerator);

        assertThat(notifiedAccelerators, is(equalTo(List.of(DEFAULT_ACCELERATOR, newAccelerator))));
    }
}
