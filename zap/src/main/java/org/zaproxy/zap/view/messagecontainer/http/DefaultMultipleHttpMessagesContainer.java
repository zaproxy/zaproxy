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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A default implementation of a {@code MultipleHttpMessagesContainer}.
 *
 * <p><strong>Note:</strong> Users of this class should provide list of {@code HttpMessage}s that
 * are loaded/read as needed (for example, from database with {@code PersistedHttpMessagesList}) to
 * avoid keeping the {@code HttpMessage}s in memory or alternatively use {@code
 * MultipleHistoryReferencesContainer}.
 *
 * @see MultipleHttpMessagesContainer
 * @see MultipleHistoryReferencesContainer
 * @see PersistedHttpMessagesList
 * @since 2.3.0
 */
public class DefaultMultipleHttpMessagesContainer extends AbstractHttpMessageContainer
        implements MultipleHttpMessagesContainer {

    private final List<HttpMessage> httpMessages;

    /**
     * Constructs a {@code DefaultMultipleHttpMessagesContainer} with no contained {@code
     * HttpMessage}s and with the given container {@code name} and {@code component}.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultMultipleHttpMessagesContainer(String name, Component component) {
        this(name, component, null);
    }

    /**
     * Constructs a {@code DefaultMultipleHttpMessagesContainer} with the given container {@code
     * name} and {@code component} and contained {@code httpMessages}.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param httpMessages the contained HTTP messages, {@code null} or empty list if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultMultipleHttpMessagesContainer(
            String name, Component component, List<HttpMessage> httpMessages) {
        super(name, component);

        if (httpMessages == null || httpMessages.isEmpty()) {
            this.httpMessages = Collections.emptyList();
        } else {
            this.httpMessages = Collections.unmodifiableList(new ArrayList<>(httpMessages));
        }
    }

    @Override
    public boolean isEmpty() {
        return httpMessages.isEmpty();
    }

    @Override
    public HttpMessage getMessage() {
        if (!isEmpty()) {
            return httpMessages.get(0);
        }
        return null;
    }

    @Override
    public int getNumberOfMessages() {
        return httpMessages.size();
    }

    @Override
    public List<HttpMessage> getMessages() {
        return httpMessages;
    }
}
