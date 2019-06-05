/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.view.messagelocation;

import java.util.EventListener;

/**
 * An {@code EventListener} that allows to listen to {@code MessageLocationProducerFocusEvent}.
 *
 * <p>Used to listen to changes in the focus of {@code MessageLocationProducer}s and propagate the
 * event with the custom format.
 *
 * @since 2.4.0
 * @see MessageLocationProducerFocusEvent
 * @see MessageLocationProducer
 */
public interface MessageLocationProducerFocusListener extends EventListener {

    /**
     * Invoked when a {@code MessageLocationProducer} gains the focus.
     *
     * @param event the event with the details
     */
    void focusGained(MessageLocationProducerFocusEvent event);

    /**
     * Invoked when a {@code MessageLocationProducer} loses the focus.
     *
     * @param event the event with the details
     */
    void focusLost(MessageLocationProducerFocusEvent event);
}
