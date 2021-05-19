/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.search;

import org.parosproxy.paros.network.HttpMessage;

/*
 * Points to a specific string in a request/response header/body
 * of a HttpMessage.
 *
 * Counted in chars from start/end of the header or from the body.
 */
public class SearchMatch {

    public enum Location {
        REQUEST_HEAD,
        REQUEST_BODY,
        RESPONSE_HEAD,
        RESPONSE_BODY
    }

    private Location location;
    private int start;
    private int end;
    private HttpMessage message;

    public SearchMatch(HttpMessage message, Location location, int start, int end) {
        this(location, start, end);
        this.message = message;
    }

    public SearchMatch(Location location, int start, int end) {
        super();
        this.location = location;
        this.start = start;
        this.end = end;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public HttpMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "SearchMatch " + location.name() + " " + start + " " + end;
    }
}
