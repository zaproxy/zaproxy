/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import java.util.Map;
import java.util.Map.Entry;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.zaproxy.zap.utils.JsonUtil;
import org.zaproxy.zap.utils.XMLStringUtil;

public class ApiResponseSet<T> extends ApiResponse {

    private Map<String, T> values = null;

    public ApiResponseSet(String name, Map<String, T> values) {
        super(name);
        this.values = values;
    }

    public void put(String key, T value) {
        this.values.put(key, value);
    }

    @Override
    public JSON toJSON() {
        if (values == null) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (Entry<String, T> val : values.entrySet()) {
            T value = val.getValue();
            if (value instanceof String) {
                jo.put(val.getKey(), JsonUtil.getJsonFriendlyString((String) value));
            } else {
                jo.put(val.getKey(), value);
            }
        }
        return jo;
    }

    @Override
    public void toXML(Document doc, Element parent) {
        parent.setAttribute("type", "set");

        for (Entry<String, T> val : values.entrySet()) {
            Element el = doc.createElement(val.getKey());
            String textValue = val.getValue() == null ? "" : val.getValue().toString();
            Text text = doc.createTextNode(XMLStringUtil.escapeControlChrs(textValue));
            el.appendChild(text);
            parent.appendChild(el);
        }
    }

    @Override
    public void toHTML(StringBuilder sb) {
        sb.append("<h2>" + StringEscapeUtils.escapeHtml(this.getName()) + "</h2>\n");
        sb.append("<table border=\"1\">\n");

        for (Entry<String, T> val : values.entrySet()) {
            sb.append("<tr><td>\n");
            sb.append(StringEscapeUtils.escapeHtml(val.getKey()));
            sb.append("</td><td>\n");
            Object value = val.getValue();
            if (value != null) {
                sb.append(StringEscapeUtils.escapeHtml(value.toString()));
            }
            sb.append("</td></tr>\n");
        }
        sb.append("</table>\n");
    }

    @Override
    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("\t");
        }
        sb.append("ApiResponseSet ");
        sb.append(this.getName());
        sb.append(" : [\n");

        for (Entry<String, T> val : values.entrySet()) {
            for (int i = 0; i < indent + 1; i++) {
                sb.append("\t");
            }
            sb.append(val.getKey());
            sb.append(" = ");
            sb.append(val.getValue());
            sb.append("\n");
        }
        for (int i = 0; i < indent; i++) {
            sb.append("\t");
        }
        sb.append("]\n");
        return sb.toString();
    }

    protected Map<String, T> getValues() {
        return values;
    }
}
