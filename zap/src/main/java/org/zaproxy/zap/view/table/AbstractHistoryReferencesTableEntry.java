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
package org.zaproxy.zap.view.table;

import java.util.Date;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.HrefTypeInfo;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/**
 * An abstract implementation of {@code HistoryReferencesTableEntry}.
 *
 * <p>Returns default values except for history reference, which returns the {@code
 * HistoryReference} passed in the constructor.
 */
public abstract class AbstractHistoryReferencesTableEntry implements HistoryReferencesTableEntry {

    private static final Object[] PLACE_HOLDER_VALUES = new Object[Column.values().length];

    private static final String EMPTY_STRING = "";
    private static final String STRING_VALUE_NOT_LOADED = "...";
    private static final Date EMPTY_DATE = new Date(0L);

    static {
        PLACE_HOLDER_VALUES[Column.HREF_ID.ordinal()] = Integer.toString(0);
        PLACE_HOLDER_VALUES[Column.REQUEST_TIMESTAMP.ordinal()] = EMPTY_DATE;
        PLACE_HOLDER_VALUES[Column.RESPONSE_TIMESTAMP.ordinal()] = EMPTY_DATE;
        PLACE_HOLDER_VALUES[Column.HREF_TYPE.ordinal()] = Integer.toString(0);
        PLACE_HOLDER_VALUES[Column.HREF_TYPE_INFO.ordinal()] = HrefTypeInfo.NO_TYPE;
        PLACE_HOLDER_VALUES[Column.METHOD.ordinal()] = STRING_VALUE_NOT_LOADED;
        PLACE_HOLDER_VALUES[Column.URL.ordinal()] = STRING_VALUE_NOT_LOADED;
        PLACE_HOLDER_VALUES[Column.HOSTNAME.ordinal()] = EMPTY_STRING;
        PLACE_HOLDER_VALUES[Column.PATH_AND_QUERY.ordinal()] = EMPTY_STRING;
        PLACE_HOLDER_VALUES[Column.STATUS_CODE.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.STATUS_REASON.ordinal()] = STRING_VALUE_NOT_LOADED;
        PLACE_HOLDER_VALUES[Column.RTT.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SIZE_MESSAGE.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SIZE_REQUEST_HEADER.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SIZE_REQUEST_BODY.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SIZE_RESPONSE_HEADER.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SIZE_RESPONSE_BODY.ordinal()] = 0;
        PLACE_HOLDER_VALUES[Column.SESSION_ID.ordinal()] = 0L;
        PLACE_HOLDER_VALUES[Column.HIGHEST_ALERT.ordinal()] =
                AlertRiskTableCellItem.NO_RISK_CELL_ITEM;
        PLACE_HOLDER_VALUES[Column.NOTE.ordinal()] = Boolean.FALSE;
        PLACE_HOLDER_VALUES[Column.TAGS.ordinal()] = EMPTY_STRING;
        PLACE_HOLDER_VALUES[Column.CUSTOM.ordinal()] = null;
    }

    private final HistoryReference historyReference;

    public AbstractHistoryReferencesTableEntry(HistoryReference historyReference) {
        this.historyReference = historyReference;
    }

    @Override
    public HistoryReference getHistoryReference() {
        return historyReference;
    }

    public Integer getHistoryId() {
        return 0;
    }

    public Integer getHistoryType() {
        return -1;
    }

    public HrefTypeInfo getHistoryTypeInfo() {
        return HrefTypeInfo.NO_TYPE;
    }

    public Long getSessionId() {
        return 0L;
    }

    public String getMethod() {
        return EMPTY_STRING;
    }

    public String getUri() {
        return EMPTY_STRING;
    }

    public String getHostName() {
        return EMPTY_STRING;
    }

    public String getPathAndQuery() {
        return EMPTY_STRING;
    }

    public Integer getStatusCode() {
        return 0;
    }

    public String getReason() {
        return EMPTY_STRING;
    }

    public Date getRequestTimestamp() {
        return EMPTY_DATE;
    }

    public Date getResponseTimestamp() {
        return EMPTY_DATE;
    }

    public Integer getRtt() {
        return 0;
    }

    public Long getMessageSize() {
        return 0L;
    }

    public Integer getRequestHeaderSize() {
        return 0;
    }

    public Integer getRequestBodySize() {
        return 0;
    }

    public Integer getResponseHeaderSize() {
        return 0;
    }

    public Integer getResponseBodySize() {
        return 0;
    }

    public AlertRiskTableCellItem getHighestAlert() {
        return AlertRiskTableCellItem.NO_RISK_CELL_ITEM;
    }

    public Boolean hasNote() {
        return Boolean.FALSE;
    }

    public String getTags() {
        return EMPTY_STRING;
    }

    @Override
    public Object getValue(Column column) {
        switch (column) {
            case HREF_ID:
                return this.getHistoryId();
            case REQUEST_TIMESTAMP:
                return this.getRequestTimestamp();
            case RESPONSE_TIMESTAMP:
                return this.getResponseTimestamp();
            case HREF_TYPE:
                return this.getHistoryType();
            case HREF_TYPE_INFO:
                return this.getHistoryTypeInfo();
            case METHOD:
                return this.getMethod();
            case URL:
                return this.getUri();
            case HOSTNAME:
                return this.getHostName();
            case PATH_AND_QUERY:
                return this.getPathAndQuery();
            case STATUS_CODE:
                return this.getStatusCode();
            case STATUS_REASON:
                return this.getReason();
            case RTT:
                return this.getRtt();
            case SIZE_MESSAGE:
                return this.getMessageSize();
            case SIZE_REQUEST_HEADER:
                return this.getRequestHeaderSize();
            case SIZE_REQUEST_BODY:
                return this.getRequestBodySize();
            case SIZE_RESPONSE_HEADER:
                return this.getResponseHeaderSize();
            case SIZE_RESPONSE_BODY:
                return this.getResponseBodySize();
            case SESSION_ID:
                return this.getSessionId();
            case HIGHEST_ALERT:
                return this.getHighestAlert();
            case NOTE:
                return this.hasNote();
            case TAGS:
                return this.getTags();
            case CUSTOM:
                return null;
            default:
                return EMPTY_STRING;
        }
    }

    public static Object getPrototypeValue(Column column) {
        switch (column) {
            case HREF_ID:
                return 1000;
            case REQUEST_TIMESTAMP:
                return new Date(System.currentTimeMillis());
            case RESPONSE_TIMESTAMP:
                return new Date(System.currentTimeMillis());
            case HREF_TYPE:
                return Integer.toString(0);
            case HREF_TYPE_INFO:
                return "Manual";
            case METHOD:
                return "GET";
            case URL:
                return "http://example.com/some/path?param=value";
            case HOSTNAME:
                return "example.com";
            case PATH_AND_QUERY:
                return "/some/path?param=value";
            case STATUS_CODE:
                return 200;
            case STATUS_REASON:
                return "Not Found";
            case RTT:
                return 1000;
            case SIZE_MESSAGE:
            case SIZE_REQUEST_HEADER:
            case SIZE_REQUEST_BODY:
            case SIZE_RESPONSE_HEADER:
            case SIZE_RESPONSE_BODY:
                return 1000;
            case SESSION_ID:
                return System.currentTimeMillis();
            case HIGHEST_ALERT:
                return AlertRiskTableCellItem.getItemForRisk(Alert.RISK_MEDIUM);
            case NOTE:
                return Boolean.FALSE;
            case TAGS:
                return "Tag1, Tag2, Tag3";
            case CUSTOM:
                return null;
            default:
                return EMPTY_STRING;
        }
    }

    public static Class<?> getColumnClass(Column column) {
        switch (column) {
            case HREF_ID:
                return Integer.class;
            case REQUEST_TIMESTAMP:
                return Date.class;
            case RESPONSE_TIMESTAMP:
                return Date.class;
            case HREF_TYPE:
                return Integer.class;
            case HREF_TYPE_INFO:
                return HrefTypeInfo.class;
            case METHOD:
                return String.class;
            case URL:
            case HOSTNAME:
            case PATH_AND_QUERY:
                return String.class;
            case STATUS_CODE:
                return Integer.class;
            case STATUS_REASON:
                return String.class;
            case RTT:
                return Integer.class;
            case SIZE_MESSAGE:
            case SIZE_REQUEST_HEADER:
            case SIZE_REQUEST_BODY:
            case SIZE_RESPONSE_HEADER:
            case SIZE_RESPONSE_BODY:
                return Integer.class;
            case SESSION_ID:
                return Long.class;
            case HIGHEST_ALERT:
                return AlertRiskTableCellItem.class;
            case NOTE:
                return Boolean.class;
            case TAGS:
                return String.class;
            case CUSTOM:
                return null;
            default:
                return String.class;
        }
    }

    public static Object getPlaceHolderValue(Column column) {
        return PLACE_HOLDER_VALUES[column.ordinal()];
    }
}
