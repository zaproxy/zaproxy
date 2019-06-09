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
import org.parosproxy.paros.network.HttpMessage;

/**
 * A default implementation of a {@code SelectableHistoryReferencesContainer}.
 *
 * @see SelectableHistoryReferencesContainer
 * @since 2.3.0
 */
public class DefaultSelectableHistoryReferencesContainer
        extends DefaultMultipleHistoryReferencesContainer
        implements SelectableHistoryReferencesContainer {

    private final List<HistoryReference> selectedHistoryReferences;
    private final List<HttpMessage> selectedHttpMessages;

    /**
     * Constructs a {@code DefaultSelectableHistoryReferencesContainer} with no contained messages
     * and with the given container {@code name} and {@code component}.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultSelectableHistoryReferencesContainer(String name, Component component) {
        this(name, component, null, null);
    }

    /**
     * Constructs a {@code DefaultSelectableHistoryReferencesContainer} with no selected messages
     * and with the given container {@code name} and {@code component} and {@code HistoryReference}s
     * of contained messages.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param historyReferences the contained HTTP messages, {@code null} or empty list if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultSelectableHistoryReferencesContainer(
            String name, Component component, List<HistoryReference> historyReferences) {
        this(name, component, historyReferences, null);
    }

    /**
     * Constructs a {@code DefaultSelectableHistoryReferencesContainer} with the given container
     * {@code name} and {@code component}, {@code HistoryReference}s of contained messages and
     * {@code HistoryReference}s of selected messages.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param historyReferences the contained HTTP messages, {@code null} or empty list if none
     * @param selectedHistoryReferences the selected HTTP messages, {@code null} or empty list if
     *     none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultSelectableHistoryReferencesContainer(
            String name,
            Component component,
            List<HistoryReference> historyReferences,
            List<HistoryReference> selectedHistoryReferences) {
        super(name, component, historyReferences);

        if (selectedHistoryReferences == null || selectedHistoryReferences.isEmpty()) {
            this.selectedHistoryReferences = Collections.emptyList();
            this.selectedHttpMessages = Collections.emptyList();
        } else {
            this.selectedHistoryReferences =
                    Collections.unmodifiableList(new ArrayList<>(selectedHistoryReferences));
            this.selectedHttpMessages = new PersistedHttpMessagesList(selectedHistoryReferences);
        }
    }

    @Override
    public boolean hasSelectedMessages() {
        return (!selectedHttpMessages.isEmpty());
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

    @Override
    public HistoryReference getSelectedHistoryReference() {
        if (hasSelectedMessages()) {
            return selectedHistoryReferences.get(0);
        }
        return null;
    }

    @Override
    public List<HistoryReference> getSelectedHistoryReferences() {
        return selectedHistoryReferences;
    }
}
