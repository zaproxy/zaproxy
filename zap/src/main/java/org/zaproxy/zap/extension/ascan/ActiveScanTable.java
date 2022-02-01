/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconAware;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

public class ActiveScanTable extends HistoryReferencesTable {

    private static final long serialVersionUID = 1L;

    public ActiveScanTable(ActiveScanTableModel model) {
        super(model);

        getColumnExt(0)
                .setCellRenderer(
                        new DefaultTableRenderer(
                                new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                JLabel.CENTER));

        getColumnExt(0).setHighlighters(new ProcessedCellItemIconHighlighter(0));
    }

    /**
     * A {@link org.jdesktop.swingx.decorator.Highlighter Highlighter} for a column that indicates,
     * using icons and text, whether or not an entry was processed, that is, is or not in scope.
     *
     * <p>The expected type/class of the cell values is {@code ProcessedCellItem}.
     */
    private static class ProcessedCellItemIconHighlighter extends AbstractHighlighter {

        /** The icon that indicates the entry was processed. */
        private static final ImageIcon PROCESSED_ICON =
                new ImageIcon(ActiveScanTable.class.getResource("/resource/icon/16/152.png"));

        /** The icon that indicates the entry was not processed. */
        private static final ImageIcon NOT_PROCESSED_ICON =
                new ImageIcon(ActiveScanTable.class.getResource("/resource/icon/16/149.png"));

        private final int columnIndex;

        public ProcessedCellItemIconHighlighter(final int columnIndex) {
            this.columnIndex = columnIndex;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            ActiveScanProcessedCellItem cell =
                    (ActiveScanProcessedCellItem) adapter.getValue(columnIndex);

            boolean processed = cell.isSuccessful();
            Icon icon = getProcessedIcon(processed);
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(icon);
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(icon);
            }

            if (component instanceof JLabel) {
                ((JLabel) component).setText(processed ? "" : cell.getLabel());
            }

            return component;
        }

        private static Icon getProcessedIcon(final boolean processed) {
            return processed ? PROCESSED_ICON : NOT_PROCESSED_ICON;
        }

        /**
         * {@inheritDoc}
         *
         * <p>Overridden to return true if the component is of type IconAware or of type JLabel,
         * false otherwise.
         *
         * <p>Note: special casing JLabel is for backward compatibility - application highlighting
         * code which doesn't use the Swingx renderers would stop working otherwise.
         */
        // Method/JavaDoc copied from
        // org.jdesktop.swingx.decorator.IconHighlighter#canHighlight(Component, ComponentAdapter)
        @Override
        protected boolean canHighlight(final Component component, final ComponentAdapter adapter) {
            return component instanceof IconAware || component instanceof JLabel;
        }
    }
}
