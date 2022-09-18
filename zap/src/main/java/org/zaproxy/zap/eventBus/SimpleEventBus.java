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

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A very simple event bus
 *
 * @author simon
 */
public class SimpleEventBus implements EventBus {

    private Map<String, RegisteredPublisher> nameToPublisher = new HashMap<>();
    private List<RegisteredConsumer> danglingConsumers = new ArrayList<>();

    /**
     * The {@code Lock} for registration management (register and unregister) of publishers and
     * consumers.
     */
    private final Lock regMgmtLock = new ReentrantLock(true);

    private static Logger log = LogManager.getLogger(SimpleEventBus.class);

    @Override
    public void registerPublisher(EventPublisher publisher, String... eventTypes) {
        if (publisher == null) {
            throw new InvalidParameterException("Publisher must not be null");
        }
        if (eventTypes == null || eventTypes.length == 0) {
            throw new InvalidParameterException("At least one event type must be specified");
        }
        regMgmtLock.lock();
        try {
            String publisherName = publisher.getPublisherName();
            if (nameToPublisher.get(publisherName) != null) {
                throw new InvalidParameterException(
                        "Publisher with name "
                                + publisherName
                                + " already registered by "
                                + nameToPublisher
                                        .get(publisherName)
                                        .getPublisher()
                                        .getClass()
                                        .getCanonicalName());
            }
            log.debug("registerPublisher {}", publisherName);

            RegisteredPublisher regProd =
                    new RegisteredPublisher(publisher, new HashSet<>(asList(eventTypes)));

            List<RegisteredConsumer> consumers = new ArrayList<>();
            // Check to see if there are any cached consumers
            moveConsumers(
                    danglingConsumers,
                    consumer -> consumer.getPublisherName().equals(publisherName),
                    consumers::add);

            regProd.addConsumers(consumers);
            nameToPublisher.put(publisherName, regProd);
        } finally {
            regMgmtLock.unlock();
        }
    }

    private static void moveConsumers(
            Collection<RegisteredConsumer> source,
            Predicate<RegisteredConsumer> condition,
            Consumer<RegisteredConsumer> sink) {
        source.removeIf(
                item -> {
                    if (condition.test(item)) {
                        sink.accept(item);
                        return true;
                    }
                    return false;
                });
    }

    @Override
    public void unregisterPublisher(EventPublisher publisher) {
        if (publisher == null) {
            throw new InvalidParameterException("Publisher must not be null");
        }

        regMgmtLock.lock();
        try {
            String publisherName = publisher.getPublisherName();
            log.debug("unregisterPublisher {}", publisherName);
            if (nameToPublisher.remove(publisherName) == null) {
                throw new InvalidParameterException(
                        "Publisher with name " + publisherName + " not registered");
            }
        } finally {
            regMgmtLock.unlock();
        }
    }

    @Override
    public void registerConsumer(EventConsumer consumer, String publisherName) {
        registerConsumer(consumer, publisherName, (String[]) null);
    }

    @Override
    public void registerConsumer(
            EventConsumer consumer, String publisherName, String... eventTypes) {
        if (consumer == null) {
            throw new InvalidParameterException("Consumer must not be null");
        }

        Set<String> eventTypesSet =
                eventTypes == null ? emptySet() : new HashSet<>(asList(eventTypes));

        regMgmtLock.lock();
        try {
            log.debug(
                    "registerConsumer {} for {}",
                    consumer.getClass().getCanonicalName(),
                    publisherName);
            RegisteredPublisher publisher = nameToPublisher.get(publisherName);
            if (publisher == null) {
                // Cache until the publisher registers
                danglingConsumers.add(
                        new RegisteredConsumer(consumer, eventTypesSet, publisherName));
            } else {
                publisher.addConsumer(new RegisteredConsumer(consumer, eventTypesSet));
            }
        } finally {
            regMgmtLock.unlock();
        }
    }

    @Override
    public void unregisterConsumer(EventConsumer consumer) {
        if (consumer == null) {
            throw new InvalidParameterException("Consumer must not be null");
        }

        regMgmtLock.lock();
        try {
            log.debug("unregisterConsumer {}", consumer.getClass().getCanonicalName());
            nameToPublisher.values().forEach(publisher -> publisher.removeConsumer(consumer));
            // Check to see if its cached waiting for a publisher
            removeDanglingConsumer(consumer);
        } finally {
            regMgmtLock.unlock();
        }
    }

    private void removeDanglingConsumer(EventConsumer consumer) {
        danglingConsumers.removeIf(
                registeredConsumer -> registeredConsumer.getConsumer().equals(consumer));
    }

    @Override
    public void unregisterConsumer(EventConsumer consumer, String publisherName) {
        if (consumer == null) {
            throw new InvalidParameterException("Consumer must not be null");
        }

        regMgmtLock.lock();
        try {
            log.debug(
                    "unregisterConsumer {} for {}",
                    consumer.getClass().getCanonicalName(),
                    publisherName);
            RegisteredPublisher publisher = nameToPublisher.get(publisherName);
            if (publisher == null) {
                // Check to see if its cached waiting for the publisher
                removeDanglingConsumer(consumer);
            } else {
                publisher.removeConsumer(consumer);
            }
        } finally {
            regMgmtLock.unlock();
        }
    }

    @Override
    public void publishSyncEvent(EventPublisher publisher, Event event) {
        if (publisher == null) {
            throw new InvalidParameterException("Publisher must not be null");
        }

        String publisherName = publisher.getPublisherName();
        String eventType = event.getEventType();
        RegisteredPublisher regPublisher = nameToPublisher.get(publisherName);

        if (regPublisher == null) {
            throw new InvalidParameterException("Publisher not registered: " + publisherName);
        }

        log.debug("publishSyncEvent {} from {}", eventType, publisherName);
        if (!regPublisher.isEventRegistered(eventType)) {
            throw new InvalidParameterException(
                    "Event type: " + eventType + " not registered for publisher: " + publisherName);
        }

        regPublisher
                .getConsumers()
                .filter(consumer -> consumer.wantsEvent(eventType))
                .forEach(
                        regCon -> {
                            try {
                                regCon.getConsumer().eventReceived(event);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        });
    }

    @Override
    public Set<String> getPublisherNames() {
        return Collections.unmodifiableSet(this.nameToPublisher.keySet());
    }

    @Override
    public Set<String> getEventTypesForPublisher(String publisherName) {
        RegisteredPublisher publisher = nameToPublisher.get(publisherName);
        if (publisher != null) {
            return Collections.unmodifiableSet(publisher.getEventTypes());
        }
        return Collections.emptySet();
    }

    private static class RegisteredConsumer {
        private EventConsumer consumer;
        private Set<String> eventTypes = new HashSet<>();
        private String publisherName;

        RegisteredConsumer(EventConsumer consumer, Set<String> eventTypes) {
            this.consumer = consumer;
            this.eventTypes.addAll(eventTypes);
        }

        RegisteredConsumer(EventConsumer consumer, Set<String> eventTypes, String publisherName) {
            this(consumer, eventTypes);
            this.publisherName = publisherName;
        }

        EventConsumer getConsumer() {
            return consumer;
        }

        boolean wantsEvent(String eventType) {
            return eventTypes.isEmpty() || eventTypes.contains(eventType);
        }

        String getPublisherName() {
            return publisherName;
        }
    }

    private static class RegisteredPublisher {
        private EventPublisher publisher;
        private Set<String> eventTypes = new HashSet<>();
        private List<RegisteredConsumer> consumers = new CopyOnWriteArrayList<>();

        RegisteredPublisher(EventPublisher publisher, Set<String> eventTypes) {
            this.publisher = publisher;
            this.eventTypes.addAll(eventTypes);
        }

        EventPublisher getPublisher() {
            return publisher;
        }

        boolean isEventRegistered(String eventType) {
            return eventTypes.contains(eventType);
        }

        Stream<RegisteredConsumer> getConsumers() {
            return consumers.stream();
        }

        void addConsumer(RegisteredConsumer consumer) {
            consumers.add(consumer);
        }

        void addConsumers(List<RegisteredConsumer> registeredConsumers) {
            consumers.addAll(registeredConsumers);
        }

        void removeConsumer(EventConsumer consumer) {
            consumers.removeIf(
                    registeredConsumer -> registeredConsumer.getConsumer().equals(consumer));
        }

        public Set<String> getEventTypes() {
            return eventTypes;
        }
    }
}
