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
 * A default implementation of a {@code SelectableHttpMessagesContainer}.
 * <p>
 * <strong>Note:</strong> Users of this class should provide list of {@code HttpMessage}s that are read as needed (for example,
 * from DB) to avoid keeping the {@code HttpMessage}s in memory or alternatively use
 * {@code SelectableHistoryReferencesContainer}.
 * 
 * @see SingleHttpMessageContainer
 * @see SelectableHistoryReferencesContainer
 * @since 2.3.0
 */
public class DefaultSelectableHttpMessagesContainer extends DefaultMultipleHttpMessagesContainer implements
        SelectableHttpMessagesContainer {

    private final List<HttpMessage> selectedHttpMessages;

    /**
     * Constructs a {@code DefaultSelectableHttpMessagesContainer} with no {@code HttpMessage}s and with the given container
     * {@code name} and {@code component}.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public DefaultSelectableHttpMessagesContainer(String name, Component component) {
        this(name, component, null, null);
    }

    /**
     * Constructs a {@code DefaultSelectableHttpMessagesContainer} with no selected messages and with the given container
     * {@code name} and {@code component} and contained {@code httpMessages}.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param httpMessages the contained HTTP messages, {@code null} or empty list if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public DefaultSelectableHttpMessagesContainer(String name, Component component, List<HttpMessage> httpMessages) {
        this(name, component, httpMessages, null);
    }

    /**
     * Constructs a {@code DefaultSelectableHttpMessagesContainer} with the given container {@code name} and {@code component},
     * contained {@code httpMessages} and selected {@code selectedHttpMessages}.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param httpMessages the contained HTTP messages, {@code null} or empty list if none
     * @param selectedHttpMessages the selected HTTP messages, {@code null} or empty list if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public DefaultSelectableHttpMessagesContainer(
            String name,
            Component component,
            List<HttpMessage> httpMessages,
            List<HttpMessage> selectedHttpMessages) {
        super(name, component, httpMessages);

        if (selectedHttpMessages == null || selectedHttpMessages.isEmpty()) {
            this.selectedHttpMessages = Collections.emptyList();
        } else {
            this.selectedHttpMessages = Collections.unmodifiableList(new ArrayList<>(selectedHttpMessages));
        }
    }

    @Override
    public boolean hasSelectedMessages() {
        return selectedHttpMessages.size() != 0;
    }

    @Override
    public boolean isOnlyOneMessageSelected() {
        return (selectedHttpMessages.size() == 1);
    }

    @Override
    public HttpMessage getSelectedMessage() {
        if (hasSelectedMessages()) {
            return selectedHttpMessages.get(0);
        }
        return null;
    }

    @Override
    public int getNumberOfSelectedMessages() {
        return selectedHttpMessages.size();
    }

    @Override
    public List<HttpMessage> getSelectedMessages() {
        return selectedHttpMessages;
    }
}
