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

import java.util.EventObject;

/**
 * An {@code EventObject} with the details of a highlight.
 *
 * @param <T> the type that has/changes the highlight
 * @since 2.4.0
 * @see HighlightChangedEvent
 * @see MessageLocationHighlight
 */
public class HighlightChangedEvent<T> extends EventObject {

    private static final long serialVersionUID = 2706344550718757343L;

    private final T entry;
    private final MessageLocationHighlight highlightReference;

    public HighlightChangedEvent(Object source, T entry, MessageLocationHighlight highlightReference) {
        super(source);

        this.entry = entry;
        this.highlightReference = highlightReference;
    }

    public T getEntry() {
        return entry;
    }

    public MessageLocationHighlight getHighlightReference() {
        return highlightReference;
    }

}