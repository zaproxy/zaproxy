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
package org.zaproxy.zap.view.panelsearch.items;

import java.util.ArrayList;
import javax.swing.JTable;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.HighlighterUtils;

public class TableSearch extends AbstractComponentSearch<JTable> {

    @Override
    protected Object[] getComponentsInternal(JTable component) {
        ArrayList<TableCellElement> elements = new ArrayList<>();
        for (int rIndex = 0; rIndex < component.getRowCount(); rIndex++) {
            for (int cIndex = 0; cIndex < component.getColumnCount(); cIndex++) {
                Object cellValue = component.getModel().getValueAt(rIndex, cIndex);
                Object columnIdentifier =
                        component.getColumnModel().getColumn(cIndex).getIdentifier();
                elements.add(
                        new TableCellElement(
                                component, columnIdentifier, cIndex, rIndex, cellValue));
            }
        }

        return elements.toArray();
    }

    @Override
    protected HighlightedComponent highlightAsParentInternal(JTable component) {
        return HighlighterUtils.highlightBorder(component, HighlighterUtils.getHighlightColor());
    }

    @Override
    protected void undoHighlightAsParentInternal(
            HighlightedComponent highlightedComponent, JTable component) {
        HighlighterUtils.undoHighlightBorder(highlightedComponent, component);
    }
}
