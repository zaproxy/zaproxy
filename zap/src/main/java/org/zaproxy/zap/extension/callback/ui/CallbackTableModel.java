/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.callback.ui;

import java.util.ArrayList;
import org.parosproxy.paros.Constant;

/** @deprecated (2.11.0) Superseded by the OAST add-on. */
@Deprecated
public class CallbackTableModel
        extends DefaultCustomColumnHistoryReferencesTableModel<CallbackRequest> {

    private static final long serialVersionUID = 1L;
    public static final Column[] COLUMNS =
            new Column[] {
                Column.HREF_ID,
                Column.REQUEST_TIMESTAMP,
                Column.METHOD,
                Column.URL,
                Column.CUSTOM,
                Column.CUSTOM,
                Column.NOTE
            };

    private static final ArrayList<CustomColumn<CallbackRequest>> CUSTOM_COLUMNS;

    static {
        CUSTOM_COLUMNS = new ArrayList<>();
        CUSTOM_COLUMNS.add(createHandlerColumn());
        CUSTOM_COLUMNS.add(createRefererColumn());
    }

    public CallbackTableModel() {
        super(COLUMNS, CUSTOM_COLUMNS, CallbackRequest.class);
    }

    private static CustomColumn<CallbackRequest> createHandlerColumn() {
        return new CustomColumn<CallbackRequest>(
                String.class, Constant.messages.getString("callback.panel.table.column.handler")) {

            @Override
            public Object getValue(CallbackRequest model) {
                return model.getHandler();
            }
        };
    }

    private static CustomColumn<CallbackRequest> createRefererColumn() {
        return new CustomColumn<CallbackRequest>(
                String.class, Constant.messages.getString("callback.panel.table.column.referer")) {

            @Override
            public Object getValue(CallbackRequest model) {
                return model.getReferer();
            }
        };
    }
}
