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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A default implementation of a {@code SingleHistoryReferenceContainer}.
 *
 * @see SingleHistoryReferenceContainer
 * @since 2.3.0
 */
public class DefaultSingleHistoryReferenceContainer extends DefaultSingleHttpMessageContainer
        implements SingleHistoryReferenceContainer {

    private static final Logger LOGGER =
            LogManager.getLogger(DefaultSingleHistoryReferenceContainer.class);

    private final HistoryReference historyReference;

    /**
     * Constructs a {@code DefaultSingleHistoryReferenceContainer} with no message and with the
     * given container {@code name} and {@code component}.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultSingleHistoryReferenceContainer(final String name, final Component component) {
        this(name, component, null);
    }

    /**
     * Constructs a {@code DefaultSingleHistoryReferenceContainer} with the given container {@code
     * name} and {@code component} and {@code HistoryReference}s of contained message.
     *
     * @param name the name of the container
     * @param component the GUI component of the container
     * @param historyReference {@code HistoryReference} of contained message, {@code null} if none
     * @throws IllegalArgumentException if the given {@code name} or {@code component} is {@code
     *     null}.
     */
    public DefaultSingleHistoryReferenceContainer(
            final String name, final Component component, final HistoryReference historyReference) {
        super(name, component);
        this.historyReference = historyReference;
    }

    @Override
    public boolean isEmpty() {
        return historyReference == null;
    }

    @Override
    public HttpMessage getMessage() {
        if (historyReference != null) {
            try {
                return historyReference.getHttpMessage();
            } catch (HttpMalformedHeaderException | DatabaseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public HistoryReference getHistoryReference() {
        return historyReference;
    }
}
