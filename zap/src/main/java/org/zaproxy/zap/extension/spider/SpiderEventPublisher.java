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
package org.zaproxy.zap.extension.spider;

import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.model.ScanEventPublisher;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.users.User;

/** @deprecated (2.12.0) See the spider add-on in zap-extensions instead. */
@Deprecated
public class SpiderEventPublisher extends ScanEventPublisher {

    private static SpiderEventPublisher publisher = null;

    @Override
    public String getPublisherName() {
        return SpiderEventPublisher.class.getCanonicalName();
    }

    public static synchronized SpiderEventPublisher getPublisher() {
        if (publisher == null) {
            publisher = new SpiderEventPublisher();
            ZAP.getEventBus().registerPublisher(publisher, getEvents());
        }
        return publisher;
    }

    public static void publishScanEvent(String event, int scanId) {
        SpiderEventPublisher publisher = getPublisher();
        publisher.publishScanEvent(publisher, event, scanId);
    }

    public static void publishScanEvent(String event, int scanId, Target target, User user) {
        SpiderEventPublisher publisher = getPublisher();
        publisher.publishScanEvent(publisher, event, scanId, target, user);
    }

    public static void publishScanProgressEvent(int scanId, int scanProgress) {
        SpiderEventPublisher publisher = getPublisher();
        publisher.publishScanProgressEvent(publisher, scanId, scanProgress);
    }
}
