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
package org.zaproxy.zap.view.messagelocation;

import org.zaproxy.zap.model.MessageLocation;

/**
 * An entry message location in a table.
 *
 * <p>Used to manage message locations and the corresponding highlights.
 *
 * @since 2.4.0
 */
public class MessageLocationTableEntry {

    private final LocationUI locationUI;
    private MessageLocationHighlight highlight;
    private MessageLocationHighlight highlightReference;

    public MessageLocationTableEntry(MessageLocation location) {
        this(location, null, null);
    }

    public MessageLocationTableEntry(
            MessageLocation location,
            MessageLocationHighlight highlight,
            MessageLocationHighlight highlightReference) {
        if (location == null) {
            throw new IllegalArgumentException("Parameter location must not be null.");
        }
        this.locationUI = new LocationUI(location);
        this.highlight = highlight;
        this.highlightReference = highlightReference;
    }

    public MessageLocationHighlight getHighlight() {
        return highlight;
    }

    public void setHighlight(MessageLocationHighlight highlight) {
        this.highlight = highlight;
    }

    public MessageLocationHighlight getHighlightReference() {
        return highlightReference;
    }

    public void setHighlightReference(MessageLocationHighlight highlightReference) {
        this.highlightReference = highlightReference;
    }

    public LocationUI getLocationUI() {
        return locationUI;
    }

    public MessageLocation getLocation() {
        return locationUI.getLocation();
    }

    public static class LocationUI implements Comparable<LocationUI> {

        private final MessageLocation location;

        private LocationUI(MessageLocation location) {
            this.location = location;
        }

        public MessageLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(LocationUI other) {
            return location.compareTo(other.location);
        }

        @Override
        public String toString() {
            return location.getDescription();
        }
    }
}
