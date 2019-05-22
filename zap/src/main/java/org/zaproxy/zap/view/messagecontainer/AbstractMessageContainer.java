/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.messagecontainer;

import java.awt.Component;

import org.zaproxy.zap.extension.httppanel.Message;

/**
 * An abstract implementation of a {@code MessageContainer}.
 * 
 * @param <T> the type of message in this container
 * @see MessageContainer
 * @since 2.3.0
 */
public abstract class AbstractMessageContainer<T extends Message> implements MessageContainer<T> {

    private final String name;

    private final Component component;

    /**
     * Constructs an {@code AbstractMessageContainer} with the given container {@code name} and {@code component}.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public AbstractMessageContainer(String name, Component component) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Parameter name must not be null.");
        }

        if (component == null) {
            throw new IllegalArgumentException("Parameter component must not be null.");
        }

        this.name = name;
        this.component = component;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}
