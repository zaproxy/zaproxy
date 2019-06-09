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

import java.util.List;
import org.zaproxy.zap.extension.httppanel.Message;

/**
 * A {@code MessageContainer} that can contain multiple messages.
 *
 * @param <T> the type of messages in this container
 * @see MessageContainer
 * @see SelectableMessagesContainer
 * @see SingleMessageContainer
 * @since 2.3.0
 */
public interface MultipleMessagesContainer<T extends Message> extends SingleMessageContainer<T> {

    /**
     * {@inheritDoc}
     *
     * <p>If multiple messages are present the first one is returned.
     */
    @Override
    T getMessage();

    /**
     * Returns the number of contained messages.
     *
     * @return the number of messages.
     */
    int getNumberOfMessages();

    /**
     * Returns all the contained messages or an empty {@code List} if none.
     *
     * <p>No assumptions should be made on the actual implementation type of the {@code List}
     * returned by this method (for example, it might be unmodifiable or its elements lazy loaded).
     *
     * <p><strong>Note:</strong> Extra care should be taken when getting the messages from the list
     * since it might return {@code null} which indicates that an error occurred while getting a
     * message (for example, failed to read a persisted message).
     *
     * @return a {@code List} with all the contained messages, never {@code null}.
     */
    List<T> getMessages();
}
