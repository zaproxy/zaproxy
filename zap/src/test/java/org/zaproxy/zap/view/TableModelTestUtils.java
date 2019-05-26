/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Class with helper/utility methods to help testing classes involving {@code TableModel} implementations.
 *
 * @see javax.swing.table.TableModel
 */
public class TableModelTestUtils {

    public static TestTableModelListener createTestTableModelListener() {
        return new TestTableModelListener();
    }

    public static class TestTableModelListener implements TableModelListener {

        private final List<TableModelEvent> events;

        private TestTableModelListener() {
            events = new ArrayList<>();
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            events.add(e);
        }

        public int getNumberOfEvents() {
            return events.size();
        }

        public boolean isCellChanged(int row, int column) {
            for (TableModelEvent event : events) {
                if (TableModelEvent.UPDATE == event.getType()) {
                    if (row >= event.getFirstRow() && row <= event.getLastRow() && event.getColumn() == column) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isRowInserted(int row) {
            for (TableModelEvent event : events) {
                if (TableModelEvent.INSERT == event.getType()) {
                    if (event.getFirstRow() == row && event.getLastRow() == row) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isRowUpdated(int row) {
            for (TableModelEvent event : events) {
                if (TableModelEvent.UPDATE == event.getType()) {
                    if (event.getFirstRow() == row && event.getLastRow() == row) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isRowRemoved(int row) {
            for (TableModelEvent event : events) {
                if (TableModelEvent.DELETE == event.getType()) {
                    if (event.getFirstRow() == row && event.getLastRow() == row) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isDataChanged() {
            for (TableModelEvent event : events) {
                if (TableModelEvent.UPDATE == event.getType()) {
                    if (event.getFirstRow() == 0 && event.getLastRow() == Integer.MAX_VALUE
                            && event.getColumn() == TableModelEvent.ALL_COLUMNS) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
