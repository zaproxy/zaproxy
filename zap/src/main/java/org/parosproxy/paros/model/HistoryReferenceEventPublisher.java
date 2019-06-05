/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.parosproxy.paros.model;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.EventPublisher;

/**
 * A {@link EventPublisher} of {@link HistoryReference} events.
 *
 * <p>Notifies of changes in the {@code HistoryReference}'s state/data.
 *
 * @since 2.7.0
 */
public final class HistoryReferenceEventPublisher implements EventPublisher {

    /** The event sent when a {@code HistoryReference} is removed. */
    public static final String EVENT_REMOVED = "href.removed";

    /** The event sent when a tag is added to a {@code HistoryReference}. */
    public static final String EVENT_TAG_ADDED = "href.tag.added";

    /** The event sent when a tag is removed a {@code HistoryReference}. */
    public static final String EVENT_TAG_REMOVED = "href.tag.removed";

    /**
     * The event sent when new tags are set (for example, multiple added and one removed) to a
     * {@code HistoryReference}.
     */
    public static final String EVENT_TAGS_SET = "href.tags.set";

    /** The event sent when a new note has been (re)set to a {@code HistoryReference}. */
    public static final String EVENT_NOTE_SET = "href.note.set";

    /** The event's field that contains the ID of the {@code HistoryReference} of the event. */
    public static final String FIELD_HISTORY_REFERENCE_ID = "historyReferenceId";

    private static HistoryReferenceEventPublisher publisher;

    @Override
    public String getPublisherName() {
        return HistoryReferenceEventPublisher.class.getCanonicalName();
    }

    /**
     * Gets the event publisher.
     *
     * @return the event publisher, never {@code null}.
     */
    public static HistoryReferenceEventPublisher getPublisher() {
        if (publisher == null) {
            createPublisher();
        }
        return publisher;
    }

    private static synchronized void createPublisher() {
        if (publisher == null) {
            publisher = new HistoryReferenceEventPublisher();
            ZAP.getEventBus()
                    .registerPublisher(
                            publisher,
                            EVENT_TAG_ADDED,
                            EVENT_TAG_REMOVED,
                            EVENT_TAGS_SET,
                            EVENT_NOTE_SET,
                            EVENT_REMOVED);
        }
    }
}
