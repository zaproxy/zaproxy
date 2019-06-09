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
package org.zaproxy.zap.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventPublisher;
import org.zaproxy.zap.users.User;

public abstract class ScanEventPublisher implements EventPublisher {

    public static final String SCAN_STARTED_EVENT = "scan.started";
    public static final String SCAN_STOPPED_EVENT = "scan.stopped";
    public static final String SCAN_PAUSED_EVENT = "scan.paused";
    public static final String SCAN_RESUMED_EVENT = "scan.resumed";
    public static final String SCAN_COMPLETED_EVENT = "scan.completed";
    public static final String SCAN_PROGRESS_EVENT = "scan.progress";

    public static final String SCAN_ID = "scanId";
    public static final String SCAN_PROGRESS = "scanProgress";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";

    private static final String[] EVENTS = {
        SCAN_STARTED_EVENT,
        SCAN_STOPPED_EVENT,
        SCAN_PAUSED_EVENT,
        SCAN_RESUMED_EVENT,
        SCAN_COMPLETED_EVENT,
        SCAN_PROGRESS_EVENT
    };

    /**
     * Returns a new array with all events.
     *
     * @return an array containing all events.
     */
    protected static String[] getEvents() {
        return Arrays.copyOf(EVENTS, EVENTS.length);
    }

    @Override
    public String getPublisherName() {
        return ScanEventPublisher.class.getCanonicalName();
    }

    public void publishScanEvent(EventPublisher publisher, String event, int scanId) {
        this.publishScanEvent(publisher, event, scanId, null, null);
    }

    public void publishScanEvent(
            EventPublisher publisher, String event, int scanId, Target target, User user) {
        Map<String, String> map = new HashMap<>();
        map.put(SCAN_ID, Integer.toString(scanId));
        if (user != null) {
            map.put(USER_ID, Integer.toString(user.getId()));
            map.put(USER_NAME, user.getName());
        }
        ZAP.getEventBus().publishSyncEvent(publisher, new Event(publisher, event, target, map));
    }

    public void publishScanProgressEvent(EventPublisher publisher, int scanId, int scanProgress) {
        Map<String, String> map = new HashMap<>();
        map.put(SCAN_ID, Integer.toString(scanId));
        map.put(SCAN_PROGRESS, Integer.toString(scanProgress));
        ZAP.getEventBus()
                .publishSyncEvent(publisher, new Event(publisher, SCAN_PROGRESS_EVENT, null, map));
    }
}
