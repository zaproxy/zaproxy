/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.parosproxy.paros.db;

public class RecordSessionUrl {

    public static final int TYPE_EXCLUDE_FROM_PROXY = 1;
    public static final int TYPE_EXCLUDE_FROM_SCAN = 2;
    public static final int TYPE_EXCLUDE_FROM_SPIDER = 3;
    public static final int TYPE_EXCLUDE_FROM_SCOPE = 4;
    public static final int TYPE_INCLUDE_IN_SCOPE = 5;
    public static final int TYPE_EXCLUDE_FROM_WEBSOCKET = 6;

    private long urlId = 0;
    private int type = 0;
    private String url = "";

    public RecordSessionUrl(long urlId, int type, String url) {
        super();
        this.urlId = urlId;
        this.type = type;
        this.url = url;
    }

    public long getUrlId() {
        return urlId;
    }

    public void setUrlId(long urlId) {
        this.urlId = urlId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
