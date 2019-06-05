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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Class with helper/utility methods to help testing classes involving {@code ListModel}
 * implementations.
 *
 * @see javax.swing.ListModel
 */
public class ListModelTestUtils {

    public static TestListDataListener createTestListDataListener() {
        return new TestListDataListener();
    }

    public static class TestListDataListener implements ListDataListener {

        private final List<ListDataEvent> events;

        private TestListDataListener() {
            events = new ArrayList<>();
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
            events.add(e);
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            events.add(e);
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            events.add(e);
        }

        public int getNumberOfEvents() {
            return events.size();
        }

        public boolean isListItemAdded(int index) {
            for (ListDataEvent event : events) {
                if (ListDataEvent.INTERVAL_ADDED == event.getType()) {
                    if (index >= event.getIndex0() && index <= event.getIndex1()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isListItemChanged(int index) {
            for (ListDataEvent event : events) {
                if (ListDataEvent.CONTENTS_CHANGED == event.getType()) {
                    if (index >= event.getIndex0() && index <= event.getIndex1()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isListItemRemoved(int index) {
            for (ListDataEvent event : events) {
                if (ListDataEvent.INTERVAL_REMOVED == event.getType()) {
                    if (index >= event.getIndex0() && index <= event.getIndex1()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
