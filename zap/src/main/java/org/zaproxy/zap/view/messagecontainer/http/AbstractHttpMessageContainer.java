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
package org.zaproxy.zap.view.messagecontainer.http;

import java.awt.Component;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.messagecontainer.AbstractMessageContainer;

/**
 * An abstract implementation of an {@code HttpMessageContainer}.
 *
 * @see HttpMessageContainer
 * @since 2.3.0
 */
public abstract class AbstractHttpMessageContainer extends AbstractMessageContainer<HttpMessage>
        implements HttpMessageContainer {

    /**
     * Constructs an {@code AbstractHttpMessageContainer} with the given container {@code name} and
     * {@code component}.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public AbstractHttpMessageContainer(String name, Component component) {
        super(name, component);
    }

    @Override
    public Class<HttpMessage> getMessageClass() {
        return HttpMessage.class;
    }
}
