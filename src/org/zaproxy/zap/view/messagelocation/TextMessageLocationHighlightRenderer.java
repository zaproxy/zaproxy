/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.view.messagelocation;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * A {@code TableCellRenderer} for {@code TextMessageLocationHighlight}s that shows its colour in a table cell.
 * 
 * @since 2.4.0
 * @see TextMessageLocationHighlight
 * @see TextMessageLocationHighlightEditor
 */
public class TextMessageLocationHighlightRenderer extends JLabel implements TableCellRenderer {

    private static final long serialVersionUID = -2552011824130705284L;

    private Border unselectedBorder;
    private Border selectedBorder;

    public TextMessageLocationHighlightRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object color,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        Color newColor = ((TextMessageLocationHighlight) color).getColor();
        setBackground(newColor);

        if (isSelected) {
            if (selectedBorder == null) {
                selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
            }
            setBorder(selectedBorder);
        } else {
            if (unselectedBorder == null) {
                unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
            }
            setBorder(unselectedBorder);
        }
        return this;
    }
}