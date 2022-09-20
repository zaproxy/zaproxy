/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zaproxy.zap.utils.XMLStringUtil;

/**
 * A class with utility methods to convert common (ZAP) objects into {@link ApiResponse} objects.
 *
 * @since 2.3.0
 */
public final class ApiResponseConversionUtils {

    private static final Logger LOGGER = LogManager.getLogger(ApiResponseConversionUtils.class);

    private ApiResponseConversionUtils() {}

    /**
     * Converts the given HTTP message, of unknown type, into an {@code ApiResponseSet}.
     *
     * <p>Prefer the use of {@link #httpMessageToSet(int, int, HttpMessage)}, which allows to
     * provide the type of the message.
     *
     * @param historyId the ID of the message
     * @param msg the HTTP message to be converted
     * @return the {@code ApiResponseSet} with the ID, type and the HTTP message
     */
    public static ApiResponseSet<String> httpMessageToSet(int historyId, HttpMessage msg) {
        return httpMessageToSet(historyId, -1, msg);
    }

    /**
     * Converts the given HTTP message into an {@code ApiResponseSet}.
     *
     * @param historyId the ID of the message
     * @param historyType the type of the message
     * @param msg the HTTP message to be converted
     * @return the {@code ApiResponseSet} with the ID, type and the HTTP message
     * @since 2.6.0
     */
    public static ApiResponseSet<String> httpMessageToSet(
            int historyId, int historyType, HttpMessage msg) {
        Map<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(historyId));
        map.put("type", String.valueOf(historyType));
        map.put("timestamp", String.valueOf(msg.getTimeSentMillis()));
        map.put("rtt", String.valueOf(msg.getTimeElapsedMillis()));
        map.put("cookieParams", msg.getCookieParamsAsString());
        map.put("note", msg.getNote());
        map.put("requestHeader", msg.getRequestHeader().toString());
        map.put("requestBody", msg.getRequestBody().toString());
        map.put("responseHeader", msg.getResponseHeader().toString());
        map.put("responseBody", msg.getResponseBody().toString());

        List<String> tags = Collections.emptyList();
        try {
            tags = HistoryReference.getTags(historyId);
        } catch (DatabaseException e) {
            LOGGER.warn("Failed to obtain the tags for message with ID {}", historyId, e);
        }
        return new HttpMessageResponseSet(map, tags);
    }

    private static class HttpMessageResponseSet extends ApiResponseSet<String> {

        private final List<String> tags;

        public HttpMessageResponseSet(Map<String, String> map, List<String> tags) {
            super("message", map);
            this.tags = tags;
        }

        @Override
        public JSON toJSON() {
            JSONObject jo = (JSONObject) super.toJSON();
            JSONArray array = new JSONArray();
            array.addAll(tags);
            jo.put("tags", array);
            return jo;
        }

        @Override
        public void toXML(Document doc, Element parent) {
            super.toXML(doc, parent);

            Element elTags = doc.createElement("tags");
            parent.appendChild(elTags);
            for (String tag : tags) {
                Element el = doc.createElement("tag");
                el.appendChild(doc.createTextNode(XMLStringUtil.escapeControlChrs(tag)));
                elTags.appendChild(el);
            }
        }

        @Override
        public void toHTML(StringBuilder sb) {
            sb.append("<h2>" + StringEscapeUtils.escapeHtml(this.getName()) + "</h2>\n");
            sb.append("<table border=\"1\">\n");

            for (Entry<String, String> entry : getValues().entrySet()) {
                appendRow(sb, entry.getKey(), entry.getValue());
                sb.append("</td></tr>\n");
            }
            appendRow(sb, "tags", tagsToString(tags));
            sb.append("</table>\n");
        }

        private static void appendRow(StringBuilder sb, String cell1, String cell2) {
            sb.append("<tr><td>\n");
            sb.append(cell1);
            sb.append("</td><td>\n");
            if (cell2 != null) {
                sb.append(StringEscapeUtils.escapeHtml(cell2));
            }
            sb.append("</td></tr>\n");
        }

        private static String tagsToString(List<String> tags) {
            if (tags == null || tags.isEmpty()) {
                return "";
            } else if (tags.size() == 1) {
                return tags.get(0);
            }

            StringBuilder strBuilder = new StringBuilder();
            for (String tag : tags) {
                if (strBuilder.length() > 0) {
                    strBuilder.append(", ");
                }
                strBuilder.append(tag);
            }
            return strBuilder.toString();
        }
    }
}
