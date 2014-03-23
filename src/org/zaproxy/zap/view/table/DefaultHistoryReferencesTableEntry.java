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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/**
 * A default implementation of {@code HistoryReferencesTableEntry}.
 * <p>
 * Only the necessary column values will be kept in memory as specified in constructor.
 */
public class DefaultHistoryReferencesTableEntry extends AbstractHistoryReferencesTableEntry {

    private static final Logger LOGGER = Logger.getLogger(DefaultHistoryReferencesTableEntry.class);

    private static final String VALUES_SEPARATOR = Constant.messages.getString("generic.value.text.separator.comma");

    private final Integer historyId;
    private final Integer historyType;
    private final Long sessionId;
    private final String method;
    private final String uri;
    private final Integer statusCode;
    private final String reason;
    private final Date timeSentMillis;
    private final Date timeReceivedMillis;
    private final Integer rtt;
    private final Long messageSize;
    private final Integer requestHeaderSize;
    private final Integer requestBodySize;
    private final Integer responseHeaderSize;
    private final Integer responseBodySize;
    private final boolean highestAlertColumn;
    private Boolean note;
    private final boolean noteColumn;
    private String tags;
    private final boolean tagsColumn;

    public DefaultHistoryReferencesTableEntry(HistoryReference historyReference, Column[] columns) {
        super(historyReference);

        Column[] sortedColumns = Arrays.copyOf(columns, columns.length);
        Arrays.sort(sortedColumns);

        historyId = hasColumn(sortedColumns, Column.HREF_ID) ? Integer.valueOf(historyReference.getHistoryId()) : null;
        historyType = hasColumn(sortedColumns, Column.HREF_TYPE) ? Integer.valueOf(historyReference.getHistoryType()) : null;
        sessionId = hasColumn(sortedColumns, Column.SESSION_ID) ? Long.valueOf(historyReference.getSessionId()) : null;
        method = hasColumn(sortedColumns, Column.METHOD) ? historyReference.getMethod() : null;
        statusCode = hasColumn(sortedColumns, Column.STATUS_CODE) ? Integer.valueOf(historyReference.getStatusCode()) : null;
        reason = hasColumn(sortedColumns, Column.STATUS_REASON) ? historyReference.getReason() : null;
        rtt = hasColumn(sortedColumns, Column.RTT) ? Integer.valueOf(historyReference.getRtt()) : null;

        HttpMessage msg = null;
        if (isMessageNeeded(sortedColumns)) {
            try {
                msg = historyReference.getHttpMessage();
            } catch (HttpMalformedHeaderException | SQLException e) {
                LOGGER.warn("Failed to read message from database: " + e.getMessage(), e);
            }
        }

        uri = extractUriValue(msg, hasColumn(sortedColumns, Column.URL));
        timeSentMillis = extractRequestTimestamp(msg, hasColumn(sortedColumns, Column.REQUEST_TIMESTAMP));
        timeReceivedMillis = extractResponseTimestamp(msg, hasColumn(sortedColumns, Column.RESPONSE_TIMESTAMP));
        requestHeaderSize = extractRequestHeaderSize(msg, hasColumn(sortedColumns, Column.SIZE_REQUEST_HEADER));
        requestBodySize = extractRequestBodySize(msg, hasColumn(sortedColumns, Column.SIZE_REQUEST_BODY));
        responseHeaderSize = extractResponseHeaderSize(msg, hasColumn(sortedColumns, Column.SIZE_RESPONSE_HEADER));
        responseBodySize = extractResponseBodySize(msg, hasColumn(sortedColumns, Column.SIZE_RESPONSE_BODY));
        messageSize = extractMessageSize(msg, hasColumn(sortedColumns, Column.SIZE_MESSAGE));

        highestAlertColumn = hasColumn(sortedColumns, Column.HIGHEST_ALERT);
        noteColumn = hasColumn(sortedColumns, Column.NOTE);
        tagsColumn = hasColumn(sortedColumns, Column.TAGS);

        refreshCachedValues();
    }

    private final String extractUriValue(HttpMessage msg, boolean required) {
        if (!required) {
            return null;
        }
        if (msg == null) {
            return super.getUri();
        }
        String uriString = msg.getRequestHeader().getURI().toString();
        if (uriString == null) {
            return super.getUri();
        }
        return uriString;
    }

    private Date extractRequestTimestamp(HttpMessage msg, boolean required) {
        if (!required) {
            return null;
        }
        if (msg == null) {
            return super.getRequestTimestamp();
        }
        return new Date(msg.getTimeSentMillis());
    }

    private Date extractResponseTimestamp(HttpMessage msg, boolean required) {
        if (!required) {
            return null;
        }
        if (msg == null) {
            return super.getResponseTimestamp();
        }
        return new Date(msg.getTimeSentMillis() + msg.getTimeElapsedMillis());
    }

    private Integer extractRequestHeaderSize(HttpMessage msg, boolean required) {
        if (!required) {
            return Integer.valueOf(0);
        }
        if (msg == null) {
            return super.getRequestHeaderSize();
        }
        return Integer.valueOf(msg.getRequestHeader().toString().length());
    }

    private Integer extractRequestBodySize(HttpMessage msg, boolean required) {
        if (!required) {
            return Integer.valueOf(0);
        }
        if (msg == null) {
            return super.getRequestBodySize();
        }
        return Integer.valueOf(msg.getRequestBody().length());
    }

    private Integer extractResponseHeaderSize(HttpMessage msg, boolean required) {
        if (!required) {
            return Integer.valueOf(0);
        }
        if (msg == null) {
            return super.getResponseHeaderSize();
        }
        return Integer.valueOf(msg.getResponseHeader().toString().length());
    }

    private Integer extractResponseBodySize(HttpMessage msg, boolean required) {
        if (!required) {
            return Integer.valueOf(0);
        }
        if (msg == null) {
            return super.getResponseBodySize();
        }
        return Integer.valueOf(msg.getResponseBody().length());
    }

    private Long extractMessageSize(HttpMessage msg, boolean required) {
        if (!required) {
            return Long.valueOf(0);
        }
        if (msg == null) {
            return super.getMessageSize();
        }

        return Long.valueOf(extractRequestHeaderSize(msg, true).intValue() + extractRequestBodySize(msg, true).intValue()
                + extractResponseHeaderSize(msg, true).intValue() + extractResponseBodySize(msg, true).intValue());
    }

    private static boolean isMessageNeeded(Column[] columns) {
        return hasColumn(columns, Column.URL) || hasColumn(columns, Column.REQUEST_TIMESTAMP)
                || hasColumn(columns, Column.RESPONSE_TIMESTAMP) || hasColumn(columns, Column.SIZE_MESSAGE)
                || hasColumn(columns, Column.SIZE_REQUEST_HEADER) || hasColumn(columns, Column.SIZE_REQUEST_BODY)
                || hasColumn(columns, Column.SIZE_RESPONSE_HEADER) || hasColumn(columns, Column.SIZE_RESPONSE_BODY);
    }

    private static boolean hasColumn(Column[] columns, Column column) {
        for (int i = 0; i < columns.length; i++) {
            if (column == columns[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer getHistoryId() {
        return historyId;
    }

    @Override
    public Integer getHistoryType() {
        return historyType;
    }

    @Override
    public Long getSessionId() {
        return sessionId;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public Date getRequestTimestamp() {
        return timeSentMillis;
    }

    @Override
    public Date getResponseTimestamp() {
        return timeReceivedMillis;
    }

    @Override
    public Integer getRtt() {
        return rtt;
    }

    @Override
    public Long getMessageSize() {
        return messageSize;
    }

    @Override
    public Integer getRequestHeaderSize() {
        return requestHeaderSize;
    }

    @Override
    public Integer getRequestBodySize() {
        return requestBodySize;
    }

    @Override
    public Integer getResponseHeaderSize() {
        return responseHeaderSize;
    }

    @Override
    public Integer getResponseBodySize() {
        return responseBodySize;
    }

    @Override
    public AlertRiskTableCellItem getHighestAlert() {
        if (highestAlertColumn) {
            return AlertRiskTableCellItem.getItemForRisk(getHistoryReference().getHighestAlert());
        }
        return super.getHighestAlert();
    }

    @Override
    public Boolean hasNote() {
        return note;
    }

    @Override
    public String getTags() {
        return tags;
    }

    /**
     * Refresh the cached values of {@code HistoryReference}'s mutable fields.
     * 
     * @see HistoryReference#getHighestAlert()
     * @see HistoryReference#getTags()
     */
    public void refreshCachedValues() {
        if (noteColumn) {
            note = Boolean.valueOf(getHistoryReference().hasNote());
        }
        if (tagsColumn) {
            tags = listToCsv(getHistoryReference().getTags());
        }
    }

    private static String listToCsv(List<String> list) {
        if (list == null || list.size() == 0) {
            return "";
        } else if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() == 2) {
            return MessageFormat.format(VALUES_SEPARATOR, new Object[] { list.get(0), list.get(1) });
        }

        String tags = VALUES_SEPARATOR;
        int total = list.size() - 2;
        for (int i = 0; i < total; i++) {
            tags = MessageFormat.format(tags, new Object[] { list.get(i), VALUES_SEPARATOR });
        }
        tags = MessageFormat.format(tags, new Object[] { list.get(total), list.get(list.size() - 1) });

        return tags;
    }
}
