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
package org.zaproxy.zap.extension.ascan;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.model.ScanEventPublisher;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;

public class ActiveScanEventPublisher extends ScanEventPublisher {

    private static ActiveScanEventPublisher publisher = null;

    @Override
    public String getPublisherName() {
        return ActiveScanEventPublisher.class.getCanonicalName();
    }

    public static synchronized ActiveScanEventPublisher getPublisher() {
        if (publisher == null) {
            publisher = new ActiveScanEventPublisher();
            ZAP.getEventBus().registerPublisher(publisher, getEvents());
        }
        return publisher;
    }

    public static void publishScanEvent(String event, int scanId) {
        ActiveScanEventPublisher publisher = getPublisher();
        publisher.publishScanEvent(publisher, event, scanId);
    }

    public static void publishScanEvent(String event, int scanId, Target target, User user) {
        ActiveScanEventPublisher publisher = getPublisher();
        publisher.publishScanEvent(publisher, event, scanId, target, user);
    }

    public static void publishScanProgressEvent(int scanId, int scanProgress) {
        ActiveScanEventPublisher publisher = getPublisher();
        publisher.publishScanProgressEvent(publisher, scanId, scanProgress);
    }
}
