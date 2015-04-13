/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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

import org.zaproxy.zap.extension.httppanel.Message;

/**
 * A location in a {@code Message}. Either a value (for example, a string, a number...) or a single (insertion) point.
 * 
 * @since 2.4.0
 * @see Message
 */
public interface MessageLocation extends Comparable<MessageLocation> {

    Class<? extends Message> getTargetMessageClass();

    /**
     * Gets the description of this location in the message.
     * <p>
     * Should be internationalised as it might be shown in GUI components.
     * <p>
     * Examples, of possible descriptions:
     * <p>
     * Example 1: This message location represents a POST parameter, in an HTTP message, it could return: <blockquote>
     * 
     * <pre>
     * POST parameter
     * </pre>
     * 
     * </blockquote>
     * <p>
     * Example 2: It's manually selected a value of a HTTP request header, it could return: <blockquote>
     * 
     * <pre>
     * Header [start index, end index]
     * </pre>
     * 
     * </blockquote>
     * <p>
     * For text selections is advised to return the coordinates of the selected content or position.
     * 
     * @return the description of the location in the message
     */
    String getDescription();

    /**
     * Gets the value represented by this location. It might be empty if it represents an insertion position.
     *
     * @return the value represented by this location, empty if a position
     */
    String getValue();

    /**
     * Tells whether or not the given location overlaps with this location.
     * <p>
     * Locations of non compatible types (for example, text and AMF) should return {@code true}.
     *
     * @param otherLocation other location to test for overlapping
     * @return {@code true} if the {@code otherLocation} overlaps with this one, {@code false} otherwise.
     */
    boolean overlaps(MessageLocation otherLocation);

}
