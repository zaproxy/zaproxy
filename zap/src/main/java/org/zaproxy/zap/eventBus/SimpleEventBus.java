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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A very simple event bus
 *
 * @author simon
 */
public class SimpleEventBus implements EventBus {

    private Map<String, RegisteredPublisher> nameToPublisher =
            new HashMap<String, RegisteredPublisher>();
    private List<RegisteredConsumer> danglingConsumers = new ArrayList<RegisteredConsumer>();

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
            if (this.nameToPublisher.get(publisher.getPublisherName()) != null) {
                throw new InvalidParameterException(
                        "Publisher with name "
                                + publisher.getPublisherName()
                                + " already registered by "
                                + this.nameToPublisher
                                        .get(publisher.getPublisherName())
                                        .getPublisher()
                                        .getClass()
                                        .getCanonicalName());
            }
            log.debug("registerPublisher " + publisher.getPublisherName());

            RegisteredPublisher regProd = new RegisteredPublisher(publisher, eventTypes);

            List<RegisteredConsumer> consumers = new ArrayList<>();
            // Check to see if there are any cached consumers
            danglingConsumers.removeIf(
                    regCon -> {
                        if (regCon.getPublisherName().equals(publisher.getPublisherName())) {
                            consumers.add(regCon);
                            return true;
                        }
                        return false;
                    });

            regProd.addConsumers(consumers);
            this.nameToPublisher.put(publisher.getPublisherName(), regProd);
        } finally {
            regMgmtLock.unlock();
        }
    }

    @Override
    public void unregisterPublisher(EventPublisher publisher) {
        if (publisher == null) {
            throw new InvalidParameterException("Publisher must not be null");
        }

        regMgmtLock.lock();
        try {
            log.debug("unregisterPublisher " + publisher.getPublisherName());
            RegisteredPublisher regPub = nameToPublisher.remove(publisher.getPublisherName());
            if (regPub == null) {
                throw new InvalidParameterException(
                        "Publisher with name " + publisher.getPublisherName() + " not registered");
            }
        } finally {
            regMgmtLock.unlock();
        }
    }

    @Override
    public void registerConsumer(EventConsumer consumer, String publisherName) {
        this.registerConsumer(consumer, publisherName, (String[]) null);
    }

    @Override
    public void registerConsumer(
            EventConsumer consumer, String publisherName, String... eventTypes) {
        if (consumer == null) {
            throw new InvalidParameterException("Consumer must not be null");
        }

        regMgmtLock.lock();
        try {
            log.debug(
                    "registerConsumer "
                            + consumer.getClass().getCanonicalName()
                            + " for "
                            + publisherName);
            RegisteredPublisher publisher = this.nameToPublisher.get(publisherName);
            if (publisher == null) {
                // Cache until the publisher registers
                this.danglingConsumers.add(
                        new RegisteredConsumer(consumer, eventTypes, publisherName));
            } else {
                publisher.addConsumer(consumer, eventTypes);
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
            log.debug("unregisterConsumer " + consumer.getClass().getCanonicalName());
            for (Entry<String, RegisteredPublisher> entry : nameToPublisher.entrySet()) {
                entry.getValue().removeConsumer(consumer);
            }
            // Check to see if its cached waiting for a publisher
            removeDanglingConsumer(consumer);
        } finally {
            regMgmtLock.unlock();
        }
    }

    private void removeDanglingConsumer(EventConsumer consumer) {
        Iterator<RegisteredConsumer> iter = this.danglingConsumers.iterator();
        while (iter.hasNext()) {
            if (iter.next().getConsumer().equals(consumer)) {
                iter.remove();
            }
        }
    }

    @Override
    public void unregisterConsumer(EventConsumer consumer, String publisherName) {
        if (consumer == null) {
            throw new InvalidParameterException("Consumer must not be null");
        }

        regMgmtLock.lock();
        try {
            log.debug(
                    "unregisterConsumer "
                            + consumer.getClass().getCanonicalName()
                            + " for "
                            + publisherName);
            RegisteredPublisher publisher = this.nameToPublisher.get(publisherName);
            if (publisher != null) {
                publisher.removeConsumer(consumer);
            } else {
                // Check to see if its cached waiting for the publisher
                removeDanglingConsumer(consumer);
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

        RegisteredPublisher regPublisher = this.nameToPublisher.get(publisher.getPublisherName());
        if (regPublisher == null) {
            throw new InvalidParameterException(
                    "Publisher not registered: " + publisher.getPublisherName());
        }
        log.debug(
                "publishSyncEvent "
                        + event.getEventType()
                        + " from "
                        + publisher.getPublisherName());
        boolean foundType = false;
        for (String type : regPublisher.getEventTypes()) {
            if (event.getEventType().equals(type)) {
                foundType = true;
                break;
            }
        }
        if (!foundType) {
            throw new InvalidParameterException(
                    "Event type: "
                            + event.getEventType()
                            + " not registered for publisher: "
                            + publisher.getPublisherName());
        }

        for (RegisteredConsumer regCon : regPublisher.getConsumers()) {
            String[] eventTypes = regCon.getEventTypes();
            boolean isListeningforEvent = false;
            if (eventTypes == null) {
                // They are listening for all events from this publisher
                isListeningforEvent = true;
            } else {
                for (String type : eventTypes) {
                    if (event.getEventType().equals(type)) {
                        isListeningforEvent = true;
                        break;
                    }
                }
            }
            if (isListeningforEvent) {
                try {
                    regCon.getConsumer().eventReceived(event);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static class RegisteredConsumer {
        private EventConsumer consumer;
        private String[] eventTypes;
        private String publisherName;

        public RegisteredConsumer(EventConsumer consumer, String[] eventTypes) {
            this.consumer = consumer;
            this.eventTypes = eventTypes;
        }

        public RegisteredConsumer(
                EventConsumer consumer, String[] eventTypes, String publisherName) {
            this.consumer = consumer;
            this.eventTypes = eventTypes;
            this.publisherName = publisherName;
        }

        public EventConsumer getConsumer() {
            return consumer;
        }

        public String[] getEventTypes() {
            return eventTypes;
        }

        public String getPublisherName() {
            return publisherName;
        }
    }

    private static class RegisteredPublisher {
        private EventPublisher publisher;
        private String[] eventTypes;
        private List<RegisteredConsumer> consumers = new CopyOnWriteArrayList<>();

        public RegisteredPublisher(EventPublisher publisher, String[] eventTypes) {
            super();
            this.publisher = publisher;
            this.eventTypes = eventTypes;
        }

        public EventPublisher getPublisher() {
            return publisher;
        }

        public String[] getEventTypes() {
            return eventTypes;
        }

        public List<RegisteredConsumer> getConsumers() {
            return consumers;
        }

        public void addConsumers(List<RegisteredConsumer> consumers) {
            this.consumers.addAll(consumers);
        }

        public void addConsumer(EventConsumer consumer, String[] eventTypes) {
            this.consumers.add(new RegisteredConsumer(consumer, eventTypes));
        }

        public void removeConsumer(EventConsumer consumer) {
            for (RegisteredConsumer cons : consumers) {
                if (cons.getConsumer().equals(consumer)) {
                    this.consumers.remove(cons);
                    return;
                }
            }
        }
    }
}
