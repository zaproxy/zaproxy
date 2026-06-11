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
package org.zaproxy.zap.view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;

/**
 * An {@code AbstractAction} that has an identifier, allows to define the default accelerator and
 * uses internationalised text read from resource files.
 *
 * <p>The use of this class is preferred to ad-hoc {@code InputMap}/{@code ActionMap} bindings as it
 * allows the user to configure its accelerator through the options dialogue. The identifier is used
 * to save/load its configurations.
 *
 * @since 2.18.0
 */
@SuppressWarnings("serial")
public class ZapAction extends AbstractAction {

    private final String identifier;
    private final KeyStroke defaultAccelerator;
    private KeyStroke accelerator;

    /**
     * Constructs a {@code ZapAction} with the given {@code identifier}, {@code name} and default
     * accelerator (user can override the accelerator through the configurations).
     *
     * @param identifier the identifier for the action (used to save/load configurations), should
     *     not be {@code null}
     * @param name the name shown in the keyboard options
     * @param defaultAccelerator the default accelerator for the action, might be {@code null}
     * @throws NullPointerException if {@code identifier} or {@code name} is {@code null}.
     */
    public ZapAction(String identifier, String name, KeyStroke defaultAccelerator) {
        super(name);
        if (identifier == null) {
            throw new NullPointerException("The identifier must not be null.");
        }
        if (name == null) {
            throw new NullPointerException("The name must not be null.");
        }
        this.identifier = identifier;
        this.defaultAccelerator = defaultAccelerator;
        this.accelerator = defaultAccelerator;
    }

    /**
     * Constructs a {@code ZapAction} with the text for the action obtained from the resource files
     * (e.g. Messages.properties) using as key the parameter {@code i18nKey} and using the given
     * {@code KeyStroke} as default accelerator (the user can override the accelerator through the
     * configurations).
     *
     * @param i18nKey the key used to read the internationalised text for the action
     * @param defaultAccelerator the default accelerator for the action, might be {@code null}.
     * @throws NullPointerException if {@code i18nKey} is {@code null}.
     */
    public ZapAction(String i18nKey, KeyStroke defaultAccelerator) {
        this(i18nKey, Constant.messages.getString(i18nKey), defaultAccelerator);
    }

    /**
     * Gets the identifier of the action.
     *
     * @return the identifier, never {@code null}.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the accelerator currently set on the action.
     *
     * @return the {@code KeyStroke} or {@code null} if none is set.
     */
    public KeyStroke getAccelerator() {
        return accelerator;
    }

    /**
     * Sets the accelerator on the action.
     *
     * @param keyStroke the {@code KeyStroke} or {@code null} to remove the accelerator.
     */
    public void setAccelerator(KeyStroke keyStroke) {
        this.accelerator = keyStroke;
    }

    /**
     * Resets the accelerator to default value (which might be {@code null} thus just removing any
     * accelerator previously set).
     *
     * @see #getDefaultAccelerator()
     */
    public void resetAccelerator() {
        setAccelerator(defaultAccelerator);
    }

    /**
     * Gets the default accelerator, defined when the action was constructed.
     *
     * @return a {@code KeyStroke} with the default accelerator, might be {@code null}.
     */
    public KeyStroke getDefaultAccelerator() {
        return defaultAccelerator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Subclasses should override.
    }
}
