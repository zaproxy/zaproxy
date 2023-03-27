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
package org.zaproxy.zap.db;

public class RecordBreak {

    private int brkId = -1;

    private String urlString;
    private String location;
    private String match;
    private boolean inverse;
    private boolean ignoreCase;
    private boolean onRequest;
    private boolean onResponse;

    public RecordBreak() {}

    public RecordBreak(
            int brkId,
            String urlString,
            String location,
            String match,
            boolean inverse,
            boolean ignoreCase,
            boolean onRequest,
            boolean onResponse) {
        this.brkId = brkId;
        this.urlString = urlString;
        this.location = location;
        this.match = match;
        this.inverse = inverse;
        this.ignoreCase = ignoreCase;
        this.onRequest = onRequest;
        this.onResponse = onResponse;
    }

    public int getBrkId() {
        return brkId;
    }

    public void setBrkId(int brkId) {
        this.brkId = brkId;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isOnRequest() {
        return onRequest;
    }

    public void setOnRequest(boolean onRequest) {
        this.onRequest = onRequest;
    }

    public boolean isOnResponse() {
        return onResponse;
    }

    public void setOnResponse(boolean onResponse) {
        this.onResponse = onResponse;
    }
}
