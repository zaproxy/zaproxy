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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.zaproxy.zap.view.panelsearch.HighlightedComponent;
import org.zaproxy.zap.view.panelsearch.HighlighterUtils;
import org.zaproxy.zap.view.panelsearch.SearchQuery;

public class TableCellElementSearch extends AbstractComponentSearch<TableCellElement> {

    @Override
    protected boolean isSearchMatchingInternal(TableCellElement component, SearchQuery query) {
        Object value = component.getValue();
        if (value != null && query.match(value.toString())) {
            return true;
        }
        return false;
    }

    @Override
    protected HighlightedComponent highlightInternal(TableCellElement component) {
        HighlightTableCellRenderer cellRenderer = wrapTableCellRenderer(component);
        cellRenderer.addHighlighted(component);
        return new HighlightedComponent(component);
    }

    @Override
    protected void undoHighlightInternal(
            HighlightedComponent highlightedComponent, TableCellElement component) {
        revertWrappedTableCellRenderer(component);
    }

    private HighlightTableCellRenderer wrapTableCellRenderer(TableCellElement component) {
        if (!(getColumnRenderer(component) instanceof HighlightTableCellRenderer)) {
            TableCellRenderer currentColumnRenderer = getColumnRenderer(component);
            HighlightTableCellRenderer highlightTableCellRenderer =
                    new HighlightTableCellRenderer(currentColumnRenderer);
            int cIndex =
                    component.getTable().getColumn(component.getColumnIdentifier()).getModelIndex();
            for (int rIndex = 0; rIndex < component.getTable().getRowCount(); rIndex++) {
                TableCellRenderer currentCellRenderer =
                        component.getTable().getCellRenderer(rIndex, cIndex);
                highlightTableCellRenderer.addCellRenderer(rIndex, currentCellRenderer);
            }

            setColumnRenderer(component, highlightTableCellRenderer);
        }
        return (HighlightTableCellRenderer) getColumnRenderer(component);
    }

    private void revertWrappedTableCellRenderer(TableCellElement component) {
        if (getColumnRenderer(component) instanceof HighlightTableCellRenderer) {
            HighlightTableCellRenderer highlightRenderer =
                    (HighlightTableCellRenderer) getColumnRenderer(component);
            setColumnRenderer(component, highlightRenderer.getOriginalColumnRenderer());
        }
    }

    private TableCellRenderer getColumnRenderer(TableCellElement component) {
        return component.getTable().getColumn(component.getColumnIdentifier()).getCellRenderer();
    }

    private void setColumnRenderer(TableCellElement component, TableCellRenderer renderer) {
        component.getTable().getColumn(component.getColumnIdentifier()).setCellRenderer(renderer);
    }

    public static class HighlightTableCellRenderer implements TableCellRenderer {

        private TableCellRenderer fallBackRenderer;
        private TableCellRenderer originalColumnRenderer;
        private HashMap<Integer, TableCellRenderer> originalCellRenderers = new HashMap<>();
        private Color selectColor;

        private ArrayList<TableCellElement> highlighted = new ArrayList<>();

        public HighlightTableCellRenderer(TableCellRenderer originalColumnRenderer) {
            this.originalColumnRenderer = originalColumnRenderer;
            this.fallBackRenderer = new DefaultTableCellRenderer();
            this.selectColor = UIManager.getColor("Table.selectionBackground");
            if (this.selectColor == null) {
                this.selectColor = new Color(57, 105, 138);
            }
        }

        public void addCellRenderer(int row, TableCellRenderer originalCellRenderer) {
            originalCellRenderers.put(row, originalCellRenderer);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            Component item =
                    getRender(row)
                            .getTableCellRendererComponent(
                                    table, value, isSelected, hasFocus, row, column);

            Object columnIdentifier = table.getColumnModel().getColumn(column).getIdentifier();
            if (highlighted.stream()
                    .anyMatch(
                            e ->
                                    e.getColumnIdentifier().equals(columnIdentifier)
                                            && e.getValue().equals(value))) {
                trySetOpaque(item, true);
                item.setBackground(HighlighterUtils.getHighlightColor());
            } else {
                if (isSelected) {
                    trySetOpaque(item, true);
                    item.setBackground(selectColor);
                } else {
                    resetRenderer(item);
                }
            }
            return item;
        }

        // ToDo: We must reset the cells Opaque and Background, but what if there is already a
        // custom TableCellRenderer with Background: red?
        private void resetRenderer(Component cell) {
            trySetOpaque(cell, false);
            cell.setBackground(null);
        }

        private void trySetOpaque(Component item, boolean isOpaque) {
            if (item instanceof DefaultTableCellRenderer) {
                ((DefaultTableCellRenderer) item).setOpaque(isOpaque);
            }
        }

        public TableCellRenderer getOriginalColumnRenderer() {
            if (originalColumnRenderer instanceof Component) {
                Component originalColumnRendererAsComponent = (Component) originalColumnRenderer;
                resetRenderer(originalColumnRendererAsComponent);
            }
            return originalColumnRenderer;
        }

        public void addHighlighted(TableCellElement element) {
            highlighted.add(element);
        }

        public TableCellRenderer getRender(int row) {

            TableCellRenderer usedCellRenderer = null;
            TableCellRenderer originalCellRenderer = originalCellRenderers.get(row);
            if (originalCellRenderer != null) {
                usedCellRenderer = originalCellRenderer;
            } else if (originalColumnRenderer != null) {
                usedCellRenderer = originalColumnRenderer;
            }

            if (usedCellRenderer == null) {
                return fallBackRenderer;
            }

            String className = usedCellRenderer.getClass().getName();

            // This class can not be wrapped, because the ui would not look very well
            // But using fallBackRenderer here has no recognizable ui impact until now.
            if (className.equals("javax.swing.plaf.synth.SynthTableUI$SynthTableCellRenderer")) {
                return fallBackRenderer;
            }

            return usedCellRenderer;
        }
    }
}
