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
package org.zaproxy.zap.utils;

import javax.swing.JButton;
import javax.swing.JTable;

/**
 * A {@code JButton} class to facilitate exporting tables (as shown) to a file (such as CSV).
 * Filters, sorting, column order, and column visibility may all impact the data exported.
 *
 * @param <T> the type of the table.
 * @since 2.7.0
 * @see TableExportAction
 */
public class TableExportButton<T extends JTable> extends JButton {

    private static final long serialVersionUID = 3437613469695367668L;

    private final TableExportAction<T> action;

    /**
     * Constructs a {@code TableExportButton} with default export action for the given table.
     *
     * @param table the Table for which the data should be exported
     */
    public TableExportButton(T table) {
        this(new TableExportAction<>(table));
    }

    /**
     * Constructs a {@code TableExportButton} with the given export action.
     *
     * @param exportAction the export action, must not be {@code null}.
     */
    public TableExportButton(TableExportAction<T> exportAction) {
        super(exportAction);
        action = exportAction;
    }

    /**
     * Sets the Table this button is for.
     *
     * @param table the Table this button applies to
     */
    public void setTable(T table) {
        action.setTable(table);
    }
}
