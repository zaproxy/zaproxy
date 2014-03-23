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
package org.zaproxy.zap.extension.fuzz.impl.http;

import java.text.Collator;
import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.table.TableRowSorter;

import org.zaproxy.zap.utils.Pair;

public class HttpFuzzTableRowSorter extends TableRowSorter<HttpFuzzTableModel> {

    private static StatusColumnComparator statusColumnComparator;

    public HttpFuzzTableRowSorter() {
        this(null);
    }

    public HttpFuzzTableRowSorter(HttpFuzzTableModel model) {
        super(model);
    }

    @Override
    public void setModel(HttpFuzzTableModel model) {
        super.setModel(model);

        if (model != null) {
            setComparator(6, getStatusColumnComparator());
        }
    }

    private static StatusColumnComparator getStatusColumnComparator() {
        if (statusColumnComparator == null) {
            statusColumnComparator = new StatusColumnComparator();
        }
        return statusColumnComparator;
    }

    private static class StatusColumnComparator implements Comparator<Pair<String, ImageIcon>> {

        @Override
        public int compare(Pair<String, ImageIcon> statusField, Pair<String, ImageIcon> otherStatusField) {
            if (statusField == null) {
                if (otherStatusField == null) {
                    return 0;
                }
                return -1;
            }

            if (otherStatusField == null) {
                return 1;
            }

            return Collator.getInstance().compare(statusField.first, otherStatusField.first);
        }

    }
}
