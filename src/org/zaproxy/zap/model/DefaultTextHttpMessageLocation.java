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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMessage;

/**
 * Default implementation of {@code TextHttpMessageLocation}.
 * 
 * @since 2.4.0
 */
public class DefaultTextHttpMessageLocation implements TextHttpMessageLocation {

    private final Location location;
    private final int start;
    private final int end;
    private final String value;

    public DefaultTextHttpMessageLocation(Location location, int position) {
        if (location == null) {
            throw new IllegalArgumentException("Parameter location must not be null");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Parameter position must be greater or equal to zero.");
        }
        this.location = location;
        this.start = position;
        this.end = position;
        this.value = "";
    }

    public DefaultTextHttpMessageLocation(Location location, int start, int end, String value) {
        if (location == null) {
            throw new IllegalArgumentException("Parameter location must not be null");
        }
        if (start < 0) {
            throw new IllegalArgumentException("Parameter start must be greater or equal to zero.");
        }
        if (end < 0) {
            throw new IllegalArgumentException("Parameter end must be greater or equal to zero.");
        }
        if (start > end) {
            throw new IllegalArgumentException("Parameter end must be greater than start.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value must not be null.");
        }
        this.location = location;
        this.start = start;
        this.end = end;
        this.value = value;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Class<HttpMessage> getTargetMessageClass() {
        return HttpMessage.class;
    }

    @Override
    public String getDescription() {
        StringBuffer description = new StringBuffer(25);
        switch (location) {
        case REQUEST_HEADER:
        case RESPONSE_HEADER:
            description.append(Constant.messages.getString("messagelocation.http.text.location.header"));
            break;
        case REQUEST_BODY:
        case RESPONSE_BODY:
            description.append(Constant.messages.getString("messagelocation.http.text.location.body"));
            break;
        default:
            description.append(Constant.messages.getString("messagelocation.http.text.location.unknown"));
        }

        description.append(" [").append(start);
        if (start != end) {
            description.append(", ").append(end);
        }
        description.append(']');

        return description.toString();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public boolean overlaps(MessageLocation otherLocation) {
        if (!(otherLocation instanceof HttpMessageLocation)) {
            return true;
        }

        HttpMessageLocation otherHttpMessageLocation = (HttpMessageLocation) otherLocation;
        if (location != otherHttpMessageLocation.getLocation()) {
            return false;
        }

        if (!(otherHttpMessageLocation instanceof TextHttpMessageLocation)) {
            return true;
        }

        TextHttpMessageLocation otherTextLocation = (TextHttpMessageLocation) otherHttpMessageLocation;
        if (start == otherTextLocation.getStart()) {
            if (start == end) {
                return end == otherTextLocation.getEnd();
            }
            return otherTextLocation.getStart() != otherTextLocation.getEnd();
        }
        if (start < otherTextLocation.getStart()) {
            return end > otherTextLocation.getStart();
        }
        return start < otherTextLocation.getEnd();
    }

    @Override
    public int compareTo(MessageLocation otherLocation) {
        if (!(otherLocation instanceof HttpMessageLocation)) {
            return 1;
        }

        HttpMessageLocation otherHttpMessageLocation = (HttpMessageLocation) otherLocation;
        if (location != otherHttpMessageLocation.getLocation()) {
            if (location.ordinal() > otherHttpMessageLocation.getLocation().ordinal()) {
                return 1;
            } else if (location.ordinal() < otherHttpMessageLocation.getLocation().ordinal()) {
                return -1;
            }
        }

        if (!(otherHttpMessageLocation instanceof TextHttpMessageLocation)) {
            return 1;
        }

        TextHttpMessageLocation otherTextLocation = (TextHttpMessageLocation) otherHttpMessageLocation;
        if (start > otherTextLocation.getStart()) {
            return 1;
        } else if (start < otherTextLocation.getStart()) {
            return -1;
        }

        if (end > otherTextLocation.getEnd()) {
            return 1;
        } else if (end < otherTextLocation.getEnd()) {
            return -1;
        }

        int result = value.compareTo(otherHttpMessageLocation.getValue());
        if (result != 0) {
            return result;
        }

        // make sure (x.compareTo(y)==0) == (x.equals(y))
        return (otherHttpMessageLocation instanceof DefaultTextHttpMessageLocation) ? 0 : 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + start;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultTextHttpMessageLocation other = (DefaultTextHttpMessageLocation) obj;
        if (end != other.end) {
            return false;
        }
        if (location != other.location) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
