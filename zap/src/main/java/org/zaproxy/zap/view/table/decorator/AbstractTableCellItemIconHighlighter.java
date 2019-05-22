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
package org.zaproxy.zap.view.table.decorator;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.IconAware;

public abstract class AbstractTableCellItemIconHighlighter extends AbstractHighlighter {

    private final int columnIndex;

    public AbstractTableCellItemIconHighlighter(final int columnIndex) {
        super();
        setHighlightPredicate(new TableCellItemHighlightPredicate());
        this.columnIndex = columnIndex;
    }

    // Implementation adapted from org.jdesktop.swingx.decorator.IconHighlighter#doHighlight(Component, ComponentAdapter)
    @Override
    protected Component doHighlight(final Component component, final ComponentAdapter adapter) {
        final Icon icon = getIcon(adapter.getValue(columnIndex));

        if (icon != null) {
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(icon);
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(icon);
            }
        } else {
            if (component instanceof JLabel) {
                ((JLabel) component).setText("");
            }
        }
        return component;
    }

    protected abstract Icon getIcon(final Object cellItem);

    protected abstract boolean isHighlighted(final Object cellItem);

    /**
     * {@inheritDoc}
     * <p>
     * 
     * Overridden to return true if the component is of type IconAware or of type JLabel, false otherwise.
     * <p>
     * 
     * Note: special casing JLabel is for backward compatibility - application highlighting code which doesn't use the Swingx
     * renderers would stop working otherwise.
     */
    // Method/JavaDoc copied from org.jdesktop.swingx.decorator.IconHighlighter#canHighlight(Component, ComponentAdapter)
    @Override
    protected boolean canHighlight(final Component component, final ComponentAdapter adapter) {
        return component instanceof IconAware || component instanceof JLabel;
    }

    private class TableCellItemHighlightPredicate implements HighlightPredicate {

        @Override
        public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
            return AbstractTableCellItemIconHighlighter.this.isHighlighted(adapter.getValue(columnIndex));
        }
    }
}
