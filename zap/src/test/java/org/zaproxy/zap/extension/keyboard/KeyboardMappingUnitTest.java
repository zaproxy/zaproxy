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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.view.ZapAction;
import org.zaproxy.zap.view.ZapMenuItem;

/** Unit test for {@link KeyboardMapping}. */
class KeyboardMappingUnitTest {

    private static final KeyStroke DEFAULT_ACCELERATOR =
            KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK);

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @Test
    void shouldHaveNoStateForDefaultConstructor() {
        KeyboardMapping mapping = new KeyboardMapping();

        assertThat(mapping.isAction(), is(false));
        assertThat(mapping.getIdentifier(), is(nullValue()));
        assertThat(mapping.getName(), is(nullValue()));
        assertThat(mapping.getKeyStroke(), is(nullValue()));
        assertThat(mapping.getDefaultKeyStroke(), is(nullValue()));
        assertThat(mapping.getKeyStrokeString(), is(equalTo("")));
    }

    @Test
    void shouldDelegateToMenuItemWhenConstructedWithOne() {
        ZapMenuItem menuItem = new ZapMenuItem("menu.id", "Menu Text", DEFAULT_ACCELERATOR);
        KeyboardMapping mapping = new KeyboardMapping(menuItem);

        assertThat(mapping.isAction(), is(false));
        assertThat(mapping.getIdentifier(), is(equalTo("menu.id")));
        assertThat(mapping.getName(), is(equalTo("Menu Text")));
        assertThat(mapping.getKeyStroke(), is(equalTo(DEFAULT_ACCELERATOR)));
        assertThat(mapping.getDefaultKeyStroke(), is(equalTo(DEFAULT_ACCELERATOR)));
    }

    @Test
    void shouldDelegateToZapActionWhenConstructedWithOne() {
        ZapAction action = new ZapAction("action.id", "Action Text", DEFAULT_ACCELERATOR);
        JComponent targetComponent = new JPanel();

        KeyboardMapping mapping =
                new KeyboardMapping(action, targetComponent, JComponent.WHEN_FOCUSED);

        assertThat(mapping.isAction(), is(true));
        assertThat(mapping.getZapAction(), is(equalTo(action)));
        assertThat(mapping.getTargetComponent(), is(equalTo(targetComponent)));
        assertThat(mapping.getInputMapCondition(), is(equalTo(JComponent.WHEN_FOCUSED)));
        assertThat(mapping.getIdentifier(), is(equalTo("action.id")));
        assertThat(mapping.getName(), is(equalTo("Action Text")));
        assertThat(mapping.getKeyStroke(), is(equalTo(DEFAULT_ACCELERATOR)));
        assertThat(mapping.getDefaultKeyStroke(), is(equalTo(DEFAULT_ACCELERATOR)));
    }

    @Test
    void shouldSetKeyStrokeOnMenuItem() {
        ZapMenuItem menuItem = new ZapMenuItem("menu.id", "Menu Text", null);
        KeyboardMapping mapping = new KeyboardMapping(menuItem);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);

        mapping.setKeyStroke(keyStroke);

        assertThat(mapping.getKeyStroke(), is(equalTo(keyStroke)));
        assertThat(menuItem.getAccelerator(), is(equalTo(keyStroke)));
    }

    @Test
    void shouldSetKeyStrokeOnZapAction() {
        ZapAction action = new ZapAction("action.id", "Action Text", null);
        KeyboardMapping mapping =
                new KeyboardMapping(action, new JPanel(), JComponent.WHEN_FOCUSED);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);

        mapping.setKeyStroke(keyStroke);

        assertThat(mapping.getKeyStroke(), is(equalTo(keyStroke)));
        assertThat(action.getAccelerator(), is(equalTo(keyStroke)));
    }

    @Test
    void shouldNotFailToSetKeyStrokeWithoutMenuItemOrAction() {
        KeyboardMapping mapping = new KeyboardMapping();

        mapping.setKeyStroke(DEFAULT_ACCELERATOR);

        assertThat(mapping.getKeyStroke(), is(nullValue()));
    }

    @Test
    void shouldFormatKeyStrokeStringFromMenuItemAccelerator() {
        ZapMenuItem menuItem = new ZapMenuItem("menu.id", "Menu Text", DEFAULT_ACCELERATOR);
        KeyboardMapping mapping = new KeyboardMapping(menuItem);

        assertThat(mapping.getKeyStrokeModifiersString(), is(equalTo("Control ")));
        assertThat(mapping.getKeyStrokeKeyCodeString(), is(equalTo("J")));
        assertThat(mapping.getKeyStrokeString(), is(equalTo("Control  J")));
    }

    @Test
    void shouldFormatNormalKeyAsUppercaseChar() {
        assertThat(KeyboardMapping.keyString('a'), is(equalTo("A")));
    }

    @Test
    void shouldParseNormalKeyAsChar() {
        assertThat(KeyboardMapping.keyCode("A"), is(equalTo('A')));
    }

    @Test
    void shouldConvertFunctionKeyCodeToString() {
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F1), is(equalTo("F1")));
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F12), is(equalTo("F12")));
    }

    @Test
    void shouldParseFunctionKeysF1ToF12() {
        assertThat(KeyboardMapping.keyCode("F1"), is(equalTo((char) KeyEvent.VK_F1)));
        assertThat(KeyboardMapping.keyCode("F12"), is(equalTo((char) KeyEvent.VK_F12)));
    }

    @Test
    void shouldFormatExtendedFunctionKeysF13ToF24() {
        // F13-F24 are not numerically contiguous with F1-F12 in KeyEvent.
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F13), is(equalTo("F13")));
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F24), is(equalTo("F24")));
    }

    @Test
    void shouldParseExtendedFunctionKeysF13ToF24() {
        assertThat(KeyboardMapping.keyCode("F13"), is(equalTo((char) KeyEvent.VK_F13)));
        assertThat(KeyboardMapping.keyCode("F24"), is(equalTo((char) KeyEvent.VK_F24)));
    }

    @Test
    void shouldConvertArrowKeyCodesToI18nStrings() {
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_UP),
                is(equalTo(Constant.messages.getString("keyboard.key.up"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_DOWN),
                is(equalTo(Constant.messages.getString("keyboard.key.down"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_LEFT),
                is(equalTo(Constant.messages.getString("keyboard.key.left"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_RIGHT),
                is(equalTo(Constant.messages.getString("keyboard.key.right"))));
    }

    @Test
    void shouldParseArrowKeys() {
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.up")),
                is(equalTo((char) KeyEvent.VK_UP)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.down")),
                is(equalTo((char) KeyEvent.VK_DOWN)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.left")),
                is(equalTo((char) KeyEvent.VK_LEFT)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.right")),
                is(equalTo((char) KeyEvent.VK_RIGHT)));
    }

    @Test
    void shouldReturnZeroForUnrecognisedKeyString() {
        assertThat(KeyboardMapping.keyCode("NotAKey"), is(equalTo((char) 0)));
    }

    @Test
    void shouldRoundTripKeyCodeAndKeyString() {
        char code = KeyboardMapping.keyCode("F5");

        assertThat((int) code, is(equalTo(KeyEvent.VK_F1 + 4)));
        assertThat(KeyboardMapping.keyCode("J"), is(equalTo('J')));
    }

    @Test
    void shouldFormatModifiersStringIncludingCommand() {
        int modifiers =
                InputEvent.META_DOWN_MASK
                        | InputEvent.CTRL_DOWN_MASK
                        | InputEvent.ALT_DOWN_MASK
                        | InputEvent.SHIFT_DOWN_MASK;

        assertThat(
                KeyboardMapping.modifiersString(modifiers),
                is(
                        equalTo(
                                Constant.messages.getString("keyboard.key.command")
                                        + " "
                                        + Constant.messages.getString("keyboard.key.control")
                                        + " "
                                        + Constant.messages.getString("keyboard.key.alt")
                                        + " "
                                        + Constant.messages.getString("keyboard.key.shift")
                                        + " ")));
    }

    @Test
    void shouldBeEqualForSameMenuItem() {
        ZapMenuItem menuItem = new ZapMenuItem("menu.id", "Menu Text", DEFAULT_ACCELERATOR);
        KeyboardMapping mapping1 = new KeyboardMapping(menuItem);
        KeyboardMapping mapping2 = new KeyboardMapping(menuItem);

        assertThat(mapping1, is(equalTo(mapping2)));
        assertThat(mapping1.hashCode(), is(equalTo(mapping2.hashCode())));
    }

    @Test
    void shouldNotBeEqualForDifferentMenuItems() {
        KeyboardMapping mapping1 =
                new KeyboardMapping(new ZapMenuItem("menu.id1", "Menu Text", DEFAULT_ACCELERATOR));
        KeyboardMapping mapping2 =
                new KeyboardMapping(new ZapMenuItem("menu.id2", "Menu Text", DEFAULT_ACCELERATOR));

        assertThat(mapping1, is(not(equalTo(mapping2))));
    }

    @Test
    void shouldNotBeEqualForMenuItemAndZapActionMappings() {
        ZapMenuItem menuItem = new ZapMenuItem("menu.id", "Menu Text", DEFAULT_ACCELERATOR);
        ZapAction action = new ZapAction("menu.id", "Menu Text", DEFAULT_ACCELERATOR);
        KeyboardMapping menuMapping = new KeyboardMapping(menuItem);
        KeyboardMapping actionMapping =
                new KeyboardMapping(action, new JPanel(), JComponent.WHEN_FOCUSED);

        assertThat(menuMapping, is(not(equalTo(actionMapping))));
    }
}
