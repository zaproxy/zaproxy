/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.eventBus;

import java.util.Set;

/**
 * Interface for the event bus used to publish events in ZAP
 *
 * @author simon
 */
public interface EventBus {

    /**
     * Register a publisher - only registered publishers can publish events
     *
     * @param publisher the publisher
     * @param eventTypes the full set of event types the publisher can publish
     */
    void registerPublisher(EventPublisher publisher, String... eventTypes);

    /**
     * Unregister the publisher
     *
     * @param publisher the publisher
     */
    void unregisterPublisher(EventPublisher publisher);

    /**
     * Register the consumer for the specified publisher - this consumer will receive all events
     * from the publisher
     *
     * @param consumer the consumer of events
     * @param publisherName the name of the publisher
     */
    void registerConsumer(EventConsumer consumer, String publisherName);

    /**
     * Register the consumer for the specified publisher - this consumer will only receive the
     * specified events from the publisher
     *
     * @param consumer the consumer of events
     * @param publisherName the name of the publisher
     * @param eventTypes the event types
     */
    void registerConsumer(EventConsumer consumer, String publisherName, String... eventTypes);

    /**
     * Unregister the consumer from all publishers
     *
     * @param consumer the consumer of events
     * @since 2.8.0
     */
    void unregisterConsumer(EventConsumer consumer);

    /**
     * Unregister the consumer from the specified publisher
     *
     * @param consumer the consumer of events
     * @param publisherName the name of the publisher
     */
    void unregisterConsumer(EventConsumer consumer, String publisherName);

    /**
     * Publish the specified event synchronously
     *
     * @param publisher the publisher
     * @param event the event
     */
    void publishSyncEvent(EventPublisher publisher, Event event);

    /**
     * Returns a list of the currently registered publisher names
     *
     * @since 2.11.0
     */
    Set<String> getPublisherNames();

    /**
     * Returns a list of the registered event types for the given publisher
     *
     * @since 2.11.0
     */
    Set<String> getEventTypesForPublisher(String publisherName);
}
