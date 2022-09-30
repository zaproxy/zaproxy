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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/**
 * An unmodifiable {@code List} of persisted {@code HttpMessage}s which internally use {@code
 * HistoryReference}s to load them from database.
 *
 * @since 2.3.0
 * @see HttpMessage
 * @see HistoryReference
 */
public class PersistedHttpMessagesList extends AbstractList<HttpMessage> {

    private static final Logger LOGGER = LogManager.getLogger(PersistedHttpMessagesList.class);

    private final List<HistoryReference> historyReferences;

    /**
     * Constructs an {@code PersistedHttpMessagesList} with the given {@code historyReferences}.
     *
     * @param historyReferences the history references used to load the messages from database
     */
    public PersistedHttpMessagesList(List<HistoryReference> historyReferences) {
        super();

        if (historyReferences == null || historyReferences.isEmpty()) {
            this.historyReferences = Collections.emptyList();
        } else {
            this.historyReferences =
                    Collections.unmodifiableList(new ArrayList<>(historyReferences));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Note:</strong> The returned message will be {@code null} if an error occurred
     * while loading the message from the database (for example, no longer exists).
     */
    @Override
    public HttpMessage get(int index) {
        try {
            return historyReferences.get(index).getHttpMessage();
        } catch (HttpMalformedHeaderException | DatabaseException e) {
            LOGGER.debug("Failed to get the message from DB: {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int size() {
        return historyReferences.size();
    }
}
