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

import org.parosproxy.paros.model.HistoryReference;

/**
 * A default implementation of a {@code MultipleHistoryReferencesContainer}.
 * 
 * @see MultipleHistoryReferencesContainer
 * @since 2.3.0
 */
public class DefaultMultipleHistoryReferencesContainer extends DefaultMultipleHttpMessagesContainer implements
        MultipleHistoryReferencesContainer {

    private final List<HistoryReference> historyReferences;

    /**
     * Constructs a {@code DefaultMultipleHttpMessagesContainer} with no contained messages and with the given container
     * {@code name} and {@code component}.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public DefaultMultipleHistoryReferencesContainer(String name, Component component) {
        this(name, component, null);
    }

    /**
     * Constructs a {@code DefaultMultipleHistoryReferencesContainer} with the given container {@code name} and
     * {@code component} and {@code HistoryReference}s of contained messages.
     * 
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param historyReferences {@code HistoryReference}s of contained HTTP messages, {@code null} or empty list if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code null}.
     */
    public DefaultMultipleHistoryReferencesContainer(String name, Component component, List<HistoryReference> historyReferences) {
        super(name, component, new PersistedHttpMessagesList(historyReferences));

        if (historyReferences == null || historyReferences.isEmpty()) {
            this.historyReferences = Collections.emptyList();
        } else {
            this.historyReferences = Collections.unmodifiableList(new ArrayList<>(historyReferences));
        }
    }

    @Override
    public HistoryReference getHistoryReference() {
        if (!isEmpty()) {
            return historyReferences.get(0);
        }
        return null;
    }

    @Override
    public List<HistoryReference> getHistoryReferences() {
        return historyReferences;
    }

}
