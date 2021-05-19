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
package org.zaproxy.zap.extension.brk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventPublisher;
import org.zaproxy.zap.extension.httppanel.Message;

/** @since 2.8.0 */
public class BreakEventPublisher implements EventPublisher {

    private static BreakEventPublisher publisher = null;
    /** Indicates that a message hit a breakpoint. */
    public static final String BREAK_POINT_HIT = "break.hit";
    /**
     * Indicates the message currently being changed (active).
     *
     * <p>Only one message can be active at the same time.
     */
    public static final String BREAK_POINT_ACTIVE = "break.active";
    /** Indicates that the active message no longer is, it might have been dropped or forwarded. */
    public static final String BREAK_POINT_INACTIVE = "break.inactive";

    public static final String MESSAGE_TYPE = "messageType";

    @Override
    public String getPublisherName() {
        return BreakEventPublisher.class.getCanonicalName();
    }

    public static synchronized BreakEventPublisher getPublisher() {
        if (publisher == null) {
            publisher = new BreakEventPublisher();
            ZAP.getEventBus()
                    .registerPublisher(
                            publisher, BREAK_POINT_HIT, BREAK_POINT_ACTIVE, BREAK_POINT_INACTIVE);
        }
        return publisher;
    }

    public void publishHitEvent(Message msg) {
        this.publishEvent(BREAK_POINT_HIT, msg, msg.toEventData());
    }

    public void publishActiveEvent(Message msg) {
        this.publishEvent(BREAK_POINT_ACTIVE, msg, msg.toEventData());
    }

    public void publishInactiveEvent(Message msg) {
        this.publishEvent(BREAK_POINT_INACTIVE, msg, Collections.emptyMap());
    }

    private void publishEvent(String event, Message msg, Map<String, String> parameters) {
        Map<String, String> map = new HashMap<>();
        map.putAll(parameters); // Could be an empty map
        map.put(MESSAGE_TYPE, msg.getType());

        ZAP.getEventBus()
                .publishSyncEvent(getPublisher(), new Event(getPublisher(), event, null, map));
    }
}
