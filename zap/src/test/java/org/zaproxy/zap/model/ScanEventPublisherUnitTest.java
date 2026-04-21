/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.users.User;

/** Unit test for {@link ScanEventPublisher}. */
class ScanEventPublisherUnitTest {

    private ScanEventPublisher pub;
    private TestConsumer sub;

    @BeforeEach
    void setUp() throws Exception {
        pub = new ScanEventPublisher() {};
        sub = new TestConsumer();
        ZAP.getEventBus().registerPublisher(pub, ScanEventPublisher.getEvents());
        ZAP.getEventBus().registerConsumer(sub, ScanEventPublisher.class.getCanonicalName());
    }

    @AfterEach
    void cleanUp() throws Exception {
        ZAP.getEventBus().unregisterPublisher(pub);
        ZAP.getEventBus().unregisterConsumer(sub);
    }

    @Test
    void shouldGetPublisherName() {
        // Given / When
        String name = pub.getPublisherName();

        // Then
        assertThat(name).isEqualTo("org.zaproxy.zap.model.ScanEventPublisher");
    }

    void assertIsBasicEvent(String event, Target target) {
        List<Event> events = sub.getEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(event);
        assertThat(events.get(0).getPublisher()).isEqualTo(pub);
        assertThat(events.get(0).getTarget()).isEqualTo(target);
        Map<String, String> params = events.get(0).getParameters();
        assertThat(params).hasSize(1);
        assertThat(params.get(ScanEventPublisher.SCAN_ID)).isEqualTo("1");
    }

    @Test
    void shouldPublishBasicScanEvent() {
        // Given
        String event = ScanEventPublisher.SCAN_STARTED_EVENT;

        // When
        pub.publishScanEvent(pub, event, 1);

        // Then
        assertIsBasicEvent(event, null);
    }

    @Test
    void shouldPublishBasicScanEventWithNullTargetAndUser() {
        // Given
        String event = ScanEventPublisher.SCAN_STARTED_EVENT;
        Target target = null;

        // When
        pub.publishScanEvent(pub, event, 1, target, null);

        // Then
        assertIsBasicEvent(event, target);
    }

    @Test
    void shouldPublishBasicScanEventWithNullUrlAndUser() {
        // Given
        String event = ScanEventPublisher.SCAN_STARTED_EVENT;
        String url = null;
        User user = null;

        // When
        pub.publishScanEvent(pub, event, 1, url, user);

        // Then
        assertIsBasicEvent(event, null);
    }

    @Test
    void shouldFailWithNullMap() {
        // Given
        String event = ScanEventPublisher.SCAN_STARTED_EVENT;

        // When // Then
        assertThrows(
                NullPointerException.class,
                () -> pub.publishScanEvent(pub, event, 1, null, null, null));
    }

    @Test
    void shouldPublishBasicScanEventWithTarget() {
        // Given
        String event = ScanEventPublisher.SCAN_PAUSED_EVENT;
        Target target = new Target();

        // When
        pub.publishScanEvent(pub, event, 1, target, null);

        // Then
        assertIsBasicEvent(event, target);
    }

    @Test
    void shouldPublishBasicScanEventWithUser() {
        // Given
        String event = ScanEventPublisher.SCAN_PAUSED_EVENT;
        User user = new User(0, "test", 1);
        Target target = null;

        // When
        pub.publishScanEvent(pub, event, 2, target, user);
        List<Event> events = sub.getEvents();

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(event);
        assertThat(events.get(0).getPublisher()).isEqualTo(pub);
        assertThat(events.get(0).getTarget()).isNull();
        Map<String, String> params = events.get(0).getParameters();
        assertThat(params).hasSize(3);
        assertThat(params.get(ScanEventPublisher.SCAN_ID)).isEqualTo("2");
        assertThat(params.get(ScanEventPublisher.USER_ID)).isEqualTo("1");
        assertThat(params.get(ScanEventPublisher.USER_NAME)).isEqualTo("test");
    }

    @Test
    void shouldPublishBasicScanEventWithUrl() {
        // Given
        String event = ScanEventPublisher.SCAN_PAUSED_EVENT;

        //  When
        pub.publishScanEvent(pub, event, 2, "https://www.example.com", null);
        List<Event> events = sub.getEvents();

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(event);
        assertThat(events.get(0).getPublisher()).isEqualTo(pub);
        assertThat(events.get(0).getTarget()).isNull();
        Map<String, String> params = events.get(0).getParameters();
        assertThat(params).hasSize(2);
        assertThat(params.get(ScanEventPublisher.SCAN_ID)).isEqualTo("2");
        assertThat(params.get(ScanEventPublisher.TARGET_URL)).isEqualTo("https://www.example.com");
    }

    @Test
    void shouldPublishScanProgressEvent() {
        // Given / When
        pub.publishScanProgressEvent(pub, 2, 57);
        List<Event> events = sub.getEvents();

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo(ScanEventPublisher.SCAN_PROGRESS_EVENT);
        assertThat(events.get(0).getPublisher()).isEqualTo(pub);
        assertThat(events.get(0).getTarget()).isNull();
        Map<String, String> params = events.get(0).getParameters();
        assertThat(params).hasSize(2);
        assertThat(params.get(ScanEventPublisher.SCAN_ID)).isEqualTo("2");
        assertThat(params.get(ScanEventPublisher.SCAN_PROGRESS)).isEqualTo("57");
    }

    private class TestConsumer implements EventConsumer {

        private List<Event> events = new ArrayList<>();

        @Override
        public void eventReceived(Event event) {
            events.add(event);
        }

        public List<Event> getEvents() {
            return events;
        }
    }
}
