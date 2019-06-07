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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@code FocusListener} adapter to multiple {@code MessageLocationProducerFocusListener}s.
 *
 * <p>Used to listen to changes in the focus of {@code MessageLocationProducer}s and propagate the
 * event with the custom format.
 *
 * @since 2.4.0
 * @see FocusListener
 * @see MessageLocationProducer
 * @see MessageLocationProducerFocusListener
 */
public class MessageLocationProducerFocusListenerAdapter implements FocusListener {

    private final List<MessageLocationProducerFocusListener> focusListeners;
    private final MessageLocationProducer source;

    public MessageLocationProducerFocusListenerAdapter(MessageLocationProducer source) {
        this.source = source;
        focusListeners = new ArrayList<>(5);
    }

    public boolean hasFocusListeners() {
        return !focusListeners.isEmpty();
    }

    public void addFocusListener(MessageLocationProducerFocusListener focusListener) {
        focusListeners.add(focusListener);
    }

    public void removeFocusListener(MessageLocationProducerFocusListener focusListener) {
        focusListeners.remove(focusListener);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (focusListeners.isEmpty()) {
            return;
        }

        MessageLocationProducerFocusEvent event = new MessageLocationProducerFocusEvent(source, e);
        for (MessageLocationProducerFocusListener focusListener : focusListeners) {
            focusListener.focusGained(event);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (focusListeners.isEmpty()) {
            return;
        }

        MessageLocationProducerFocusEvent event = new MessageLocationProducerFocusEvent(source, e);
        for (MessageLocationProducerFocusListener focusListener : focusListeners) {
            focusListener.focusLost(event);
        }
    }
}
