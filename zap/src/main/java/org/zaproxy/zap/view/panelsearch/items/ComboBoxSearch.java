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
import javax.swing.JComboBox;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.HighlighterUtils;
import org.zaproxy.zap.view.panelsearch.SearchQuery;

public class ComboBoxSearch extends AbstractComponentSearch<JComboBox<Object>> {

    @Override
    protected boolean isSearchMatchingInternal(JComboBox<Object> component, SearchQuery query) {
        Object selectedItem = component.getSelectedItem();
        if (selectedItem != null) {
            return query.match(selectedItem.toString());
        }
        return false;
    }

    @Override
    protected HighlightedComponent highlightInternal(JComboBox<Object> component) {
        return HighlighterUtils.highlightBackground(
                component, HighlighterUtils.getHighlightColor());
    }

    @Override
    protected void undoHighlightInternal(
            HighlightedComponent highlightedComponent, JComboBox<Object> component) {
        HighlighterUtils.undoHighlightBackground(highlightedComponent, component);
    }

    @Override
    protected Object[] getComponentsInternal(JComboBox<Object> component) {
        ArrayList<ComboBoxElement> items = new ArrayList<>();
        for (int i = 0; i < component.getItemCount(); i++) {
            Object item = component.getItemAt(i);
            items.add(new ComboBoxElement(item, i, component));
        }
        return items.toArray();
    }

    @Override
    protected HighlightedComponent highlightAsParentInternal(JComboBox<Object> component) {
        return HighlighterUtils.highlightBorder(component, HighlighterUtils.getHighlightColor());
    }

    @Override
    protected void undoHighlightAsParentInternal(
            HighlightedComponent highlightedComponent, JComboBox<Object> component) {
        HighlighterUtils.undoHighlightBorder(highlightedComponent, component);
    }
}
