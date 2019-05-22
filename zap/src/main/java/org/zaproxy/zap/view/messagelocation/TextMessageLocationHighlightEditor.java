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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.parosproxy.paros.Constant;

/**
 * A {@code TableCellEditor} for {@code TextMessageLocationHighlight}s that allows to choose its colour from a table cell.
 * 
 * @since 2.4.0
 * @see TextMessageLocationHighlight
 * @see TextMessageLocationHighlightRenderer
 */
public class TextMessageLocationHighlightEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private static final long serialVersionUID = -4872445961159101261L;

    private static final String START_EDIT = "start.edit";

    private Color currentColor;
    private final JButton button;
    private final JColorChooser colorChooser;
    private final JDialog dialog;

    public TextMessageLocationHighlightEditor() {
        button = new JButton();
        button.setActionCommand(START_EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(
                button,
                Constant.messages.getString("messagelocation.text.highlight.colorpicker"),
                true,
                colorChooser,
                this,
                null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (START_EDIT.equals(e.getActionCommand())) {
            button.setBackground(currentColor);
            colorChooser.setColor(currentColor);
            dialog.setVisible(true);

            fireEditingStopped();
        } else {
            currentColor = colorChooser.getColor();
        }
    }

    @Override
    public Object getCellEditorValue() {
        return new TextMessageLocationHighlight(currentColor);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentColor = ((TextMessageLocationHighlight) value).getColor();
        button.setBackground(currentColor);

        return button;
    }
}