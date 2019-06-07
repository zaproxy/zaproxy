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
package org.zaproxy.zap.view.panels;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.regex.PatternSyntaxException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * A component class to facilitate filtering of JXTables.
 *
 * @param <T> the type of the table.
 * @since 2.7.0
 */
public class TableFilterPanel<T extends JXTable> extends JPanel {

    private static final long serialVersionUID = -756376186276203390L;

    public TableFilterPanel(T table) {
        this.setLayout(new GridBagLayout());

        JLabel filterLabel = new JLabel(Constant.messages.getString("generic.filter.label"));
        final JTextField filterTextField = new JTextField();

        filterLabel.setLabelFor(filterTextField);
        this.add(filterLabel, LayoutHelper.getGBC(0, 0, 1, 0.0D));
        this.add(filterTextField, LayoutHelper.getGBC(1, 0, 1, 1.0D));

        String tooltipText = Constant.messages.getString("generic.filter.tooltip");
        filterLabel.setToolTipText(tooltipText);
        filterTextField.setToolTipText(tooltipText);

        // Set filter listener
        filterTextField
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                updateFilter();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                updateFilter();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                updateFilter();
                            }

                            public void updateFilter() {
                                String filterText = filterTextField.getText();
                                if (filterText.isEmpty()) {
                                    table.setRowFilter(null);
                                    filterTextField.setForeground(
                                            UIManager.getColor("TextField.foreground"));
                                } else {
                                    try {
                                        table.setRowFilter(
                                                RowFilter.regexFilter("(?i)" + filterText));
                                        filterTextField.setForeground(
                                                UIManager.getColor("TextField.foreground"));
                                    } catch (PatternSyntaxException e) {
                                        filterTextField.setForeground(Color.RED);
                                    }
                                }
                            }
                        });
    }
}
