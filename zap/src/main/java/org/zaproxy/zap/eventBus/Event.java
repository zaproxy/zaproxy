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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.zaproxy.zap.model.Target;

/**
 * A event that can be published via the event bus
 *
 * @author simon
 */
public class Event {

    private EventPublisher publisher;
    private String eventType;
    private Target target;
    private Map<String, String> parameters;

    public Event(EventPublisher publisher, String eventType, Target target) {
        this(publisher, eventType, target, Collections.emptyMap());
    }

    /**
     * Constructs an {@code Event} with the given data.
     *
     * @param publisher the publisher of the events.
     * @param eventType the type of event.
     * @param target the target, might be {@code null}.
     * @param parameters the parameters of the event.
     * @throws NullPointerException (since 2.8.0) if the given parameters are {@code null}.
     */
    public Event(
            EventPublisher publisher,
            String eventType,
            Target target,
            Map<String, String> parameters) {
        super();
        this.publisher = publisher;
        this.eventType = eventType;
        this.target = target;
        this.parameters = Objects.requireNonNull(parameters);
    }

    public EventPublisher getPublisher() {
        return publisher;
    }

    public String getEventType() {
        return eventType;
    }

    public Target getTarget() {
        return target;
    }

    /**
     * Gets the parameters of the event.
     *
     * @return the parameters, never {@code null} (since 2.8.0).
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setPublisher(EventPublisher publisher) {
        this.publisher = publisher;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * Sets the parameters of the event.
     *
     * @param parameters the parameters for the event.
     * @throws NullPointerException (since 2.8.0) if the given parameters are {@code null}.
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }
}
