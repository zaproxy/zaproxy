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

import java.util.List;
import org.parosproxy.paros.model.HistoryReference;

/**
 * A {@code SelectableHttpMessagesContainer} that exposes the {@code HistoryReference}s of the
 * container.
 *
 * @see HistoryReference
 * @see SelectableHttpMessagesContainer
 * @since 2.3.0
 */
public interface SelectableHistoryReferencesContainer
        extends SelectableHttpMessagesContainer, MultipleHistoryReferencesContainer {

    /**
     * Returns the {@code HistoryReference} associated with the selected message or {@code null} if
     * none.
     *
     * <p>If multiple messages are selected the {@code HistoryReference} of the first one is
     * returned.
     *
     * @return the selected message or {@code null} if none.
     */
    HistoryReference getSelectedHistoryReference();

    /**
     * Returns all the {@code HistoryReference}s associated with the selected messages.
     *
     * <p>No assumptions should be made on the actual implementation type of the {@code List}
     * returned by this method.
     *
     * @return a {@code List} with all the {@code HistoryReference}s associated with the selected
     *     messages, never {@code null}.
     * @see #getNumberOfSelectedMessages()
     */
    List<HistoryReference> getSelectedHistoryReferences();
}
