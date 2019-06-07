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
package org.parosproxy.paros.db;

public class RecordStructure {

    private long sessionId;
    private long structureId;
    private long parentId;
    private int historyId;
    private String name;
    private String url;
    private String method;

    public RecordStructure(
            long sessionId,
            long structureId,
            long parentId,
            int historyId,
            String name,
            String url,
            String method) {
        super();
        this.sessionId = sessionId;
        this.structureId = structureId;
        this.parentId = parentId;
        this.historyId = historyId;
        this.name = name;
        this.url = url;
        this.method = method;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getStructureId() {
        return structureId;
    }

    public long getParentId() {
        return parentId;
    }

    public int getHistoryId() {
        return historyId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }
}
