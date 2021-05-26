/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.parosproxy.paros.extension.history;

import java.util.HashMap;
import java.util.Map;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventPublisher;

/**
 * A {@link EventPublisher} of {@link ProxyListenerLog} events.
 *
 * @since 2.8.0
 */
public final class ProxyListenerLogEventPublisher implements EventPublisher {

    /** The event sent when a {@code HistoryReference} is added. */
    public static final String EVENT_ADDED = "href.added";
    /** The event's field that contains the ID of the {@code HistoryReference} of the event. */
    public static final String FIELD_HISTORY_REFERENCE_ID = "historyReferenceId";

    private static final String FIELD_URI = "uri";
    private static final String FIELD_METHOD = "method";
    private static final String FIELD_TIME_SENT_MS = "timeSentInMs";
    private static final String FIELD_STATUS_CODE = "statusCode";
    private static final String FIELD_RTT = "rtt";
    private static final String FIELD_RESPONSE_BODY_LENGTH = "responseBodyLength";

    private static ProxyListenerLogEventPublisher publisher;

    @Override
    public String getPublisherName() {
        return ProxyListenerLogEventPublisher.class.getCanonicalName();
    }

    /**
     * Gets the event publisher.
     *
     * @return the event publisher, never {@code null}.
     */
    public static ProxyListenerLogEventPublisher getPublisher() {
        if (publisher == null) {
            createPublisher();
        }
        return publisher;
    }

    private static synchronized void createPublisher() {
        if (publisher == null) {
            publisher = new ProxyListenerLogEventPublisher();
            ZAP.getEventBus().registerPublisher(publisher, EVENT_ADDED);
        }
    }

    public void publishHrefAddedEvent(HistoryReference href) {
        Map<String, String> map = new HashMap<>();
        map.put(FIELD_HISTORY_REFERENCE_ID, Integer.toString(href.getHistoryId()));
        map.put(FIELD_URI, href.getURI().toString());
        map.put(FIELD_METHOD, href.getMethod());
        map.put(FIELD_TIME_SENT_MS, Long.toString(href.getTimeSentMillis()));
        map.put(FIELD_STATUS_CODE, Integer.toString(href.getStatusCode()));
        map.put(FIELD_RTT, Integer.toString(href.getRtt()));
        map.put(FIELD_RESPONSE_BODY_LENGTH, Integer.toString(href.getResponseBodyLength()));
        ZAP.getEventBus()
                .publishSyncEvent(
                        getPublisher(), new Event(getPublisher(), EVENT_ADDED, null, map));
    }
}
