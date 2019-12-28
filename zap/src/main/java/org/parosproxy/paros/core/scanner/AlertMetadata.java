/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import org.parosproxy.paros.db.RecordAlertMetadata;

public class AlertMetadata {

    public static enum Type {
        REF
    };

    private long alertId = -1;
    private String type;
    private String data;

    public AlertMetadata(long alertId, String type, String data) {
        this.alertId = alertId;
        this.type = type;
        this.data = data;
    }

    public AlertMetadata(RecordAlertMetadata recordAlertMetadata) {
        this(
                recordAlertMetadata.getAlertId(),
                recordAlertMetadata.getType(),
                recordAlertMetadata.getData());
    }

    public void setAlertId(long alertId) {
        this.alertId = alertId;
    }

    public long getAlertId() {
        return this.alertId;
    }

    public void setType(String type) {
        this.type = (type == null) ? Type.REF.name() : type;
    }

    public String getType() {
        return this.type;
    }

    public void setData(String data) {
        this.data = (data == null) ? "" : data;
    }

    public String getData() {
        return this.data;
    }

    /**
     * Creates a new instance of {@code AlertMetadata} with same members.
     *
     * @return a new {@code AlertMetadata} instance
     */
    public AlertMetadata newInstance() {
        return new AlertMetadata(this.alertId, this.type, this.data);
    }
}
