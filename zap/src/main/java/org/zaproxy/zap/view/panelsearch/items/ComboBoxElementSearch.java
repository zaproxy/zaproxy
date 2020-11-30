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

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.HighlighterUtils;
import org.zaproxy.zap.view.panelsearch.SearchQuery;

public class ComboBoxElementSearch extends AbstractComponentSearch<ComboBoxElement> {

    @Override
    protected boolean isSearchMatchingInternal(ComboBoxElement component, SearchQuery query) {
        return query.match(component.getItem().toString());
    }

    @Override
    protected HighlightedComponent highlightInternal(ComboBoxElement component) {
        HighlightComboBoxItemRenderer cellRenderer = wrapComboBoxItemRenderer(component);
        cellRenderer.addHighlightedIndex(component.getItemIndex());
        return new HighlightedComponent(component);
    }

    @Override
    protected void undoHighlightInternal(
            HighlightedComponent highlightedComponent, ComboBoxElement component) {
        revertWrappedComboBoxItemRenderer(component);
    }

    private HighlightComboBoxItemRenderer wrapComboBoxItemRenderer(ComboBoxElement component) {
        if (!(getRenderer(component) instanceof HighlightComboBoxItemRenderer)) {
            ListCellRenderer<Object> currentRenderer = getRenderer(component);
            setRenderer(component, new HighlightComboBoxItemRenderer(currentRenderer));
        }
        return (HighlightComboBoxItemRenderer) getRenderer(component);
    }

    private void revertWrappedComboBoxItemRenderer(ComboBoxElement component) {
        if (getRenderer(component) instanceof HighlightComboBoxItemRenderer) {
            HighlightComboBoxItemRenderer highlightRenderer =
                    (HighlightComboBoxItemRenderer) getRenderer(component);
            setRenderer(component, highlightRenderer.getOriginalRenderer());
        }
    }

    private ListCellRenderer<Object> getRenderer(ComboBoxElement component) {
        return component.getComboBox().getRenderer();
    }

    private void setRenderer(ComboBoxElement component, ListCellRenderer<Object> renderer) {
        component.getComboBox().setRenderer(renderer);
    }

    public static class HighlightComboBoxItemRenderer implements ListCellRenderer<Object> {

        private ArrayList<Integer> highlightedIndexes = new ArrayList<>();
        private ListCellRenderer<Object> originalRenderer;

        public HighlightComboBoxItemRenderer(ListCellRenderer<Object> originalRenderer) {
            this.originalRenderer = originalRenderer;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component item =
                    originalRenderer.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
            if (highlightedIndexes.contains(index)) {
                trySetOpaque(item, true);
                item.setBackground(HighlighterUtils.getHighlightColor());
            } else {
                trySetOpaque(item, false);
                item.setBackground(null);
            }
            return item;
        }

        private void trySetOpaque(Component item, boolean isOpaque) {
            if (item instanceof BasicComboBoxRenderer) {
                ((BasicComboBoxRenderer) item).setOpaque(isOpaque);
            }
        }

        public ListCellRenderer<Object> getOriginalRenderer() {
            // ToDo: reset rendererLike in tree?
            return originalRenderer;
        }

        public void addHighlightedIndex(int itemIndex) {
            highlightedIndexes.add(itemIndex);
        }
    }
}
