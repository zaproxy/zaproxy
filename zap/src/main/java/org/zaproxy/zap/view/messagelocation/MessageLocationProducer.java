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

import org.zaproxy.zap.model.MessageLocation;

/**
 * A component capable of producing {@code MessageLocation}s, normally through the UI.
 *
 * @since 2.4.0
 * @see MessageLocation
 * @see MessageLocationProducerFocusListener
 */
public interface MessageLocationProducer {

    /**
     * The type of {@code MessageLocation} that it can produce.
     *
     * @return the message location
     */
    Class<? extends MessageLocation> getMessageLocationClass();

    /**
     * Obtains the current selected location.
     *
     * @return the selected location
     */
    MessageLocation getSelection();

    /**
     * Adds the focus listener, starting to be notified of changes in the focus.
     *
     * @param focusListener the focus listener that will be removed
     */
    void addFocusListener(MessageLocationProducerFocusListener focusListener);

    /**
     * Removes the focus listener.
     *
     * @param focusListener the focus listener that will be removed
     */
    void removeFocusListener(MessageLocationProducerFocusListener focusListener);

    /**
     * Creates a {@code MessageLocationHighlightsManager} responsible to manage the highlights of the message locations
     * produced.
     *
     * @return the {@code MessageLocationHighlightsManager}
     */
    MessageLocationHighlightsManager create();
}
