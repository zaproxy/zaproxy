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
import java.util.EventObject;

/**
 * An {@code EventObject} with the details of the focus change of a {@code MessageLocationProducer}.
 *
 * @since 2.4.0
 * @see MessageLocationProducer
 * @see MessageLocationProducerFocusListener
 */
public class MessageLocationProducerFocusEvent extends EventObject {

    private static final long serialVersionUID = 2267507082811701439L;

    private final FocusEvent focusEvent;

    public MessageLocationProducerFocusEvent(MessageLocationProducer source, FocusEvent event) {
        super(source);
        this.focusEvent = event;
    }

    @Override
    public MessageLocationProducer getSource() {
        return (MessageLocationProducer) super.getSource();
    }

    public FocusEvent getFocusEvent() {
        return focusEvent;
    }
}
