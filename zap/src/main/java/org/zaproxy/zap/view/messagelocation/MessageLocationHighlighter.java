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
import org.zaproxy.zap.model.MessageLocationConsumer;

/**
 * A {@code MessageLocationConsumer} capable of highlighting locations.
 *
 * @since 2.4.0
 * @see MessageLocationHighlight
 * @see MessageLocation
 */
public interface MessageLocationHighlighter extends MessageLocationConsumer {

    /**
     * Highlights the given {@code location}, with an undefined highlight.
     *
     * @param location the location that will be highlighted
     * @return a reference to the highlight added, {@code null} if it's was not possible to
     *     highlight
     */
    MessageLocationHighlight highlight(MessageLocation location);

    /**
     * Highlights the given {@code location} with the given {@code highlight}.
     *
     * @param location the location that will be highlighted
     * @param highlight the highlight applied to the location
     * @return a reference to the highlight added, {@code null} if it's was not possible to
     *     highlight
     */
    MessageLocationHighlight highlight(
            MessageLocation location, MessageLocationHighlight highlight);

    /**
     * Removes a previous highlight.
     *
     * @param location the location of a previous highlight
     * @param highlightReference a reference to a previous highlight
     */
    void removeHighlight(MessageLocation location, MessageLocationHighlight highlightReference);
}
