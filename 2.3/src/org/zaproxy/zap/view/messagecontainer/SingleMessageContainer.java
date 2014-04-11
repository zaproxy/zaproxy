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

import org.zaproxy.zap.extension.httppanel.Message;

/**
 * A {@code MessageContainer} that contains only one message.
 * 
 * @param <T> the type of message in this container
 * @see MessageContainer
 * @see MultipleMessagesContainer
 * @see SelectableMessagesContainer
 * @since 2.3.0
 */
public interface SingleMessageContainer<T extends Message> extends MessageContainer<T> {

    /**
     * Returns the contained message. Might be {@code null} if no message is present or an error occurred while getting the
     * message (for example, failed to read a persisted message).
     * 
     * @return the contained message or {@code null} if not present or an error occurred while getting the message.
     */
    T getMessage();

}
