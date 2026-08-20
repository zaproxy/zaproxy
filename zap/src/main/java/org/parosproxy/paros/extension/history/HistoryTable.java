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
package org.parosproxy.paros.extension.history;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import org.apache.commons.configuration.FileConfiguration;
import org.jdesktop.swingx.table.TableColumnExt;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.view.table.HistoryReferencesTable;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/** A {@code HistoryReferencesTable} for History tab. */
@SuppressWarnings("serial")
class HistoryTable extends HistoryReferencesTable {

    private static final long serialVersionUID = 1L;

    /**
     * Config key written on first run after ADV_NOTES was introduced. Its presence signals that the
     * one-time NOTE column migration has already been applied, so any subsequent user choice to
     * re-show the icon column is respected.
     */
    private static final String ADV_NOTES_MIGRATION_KEY = "view.history.advnotes.migration";

    private Consumer<HistoryReference> advNotesDialogOpener;

    /** Constructs a {@code HistoryTable}. */
    public HistoryTable() {
        super(new HistoryTableModel());

        setAutoCreateColumnsFromModel(false);

        setName("History Table");
        super.loadColumnConfiguration("HistoryTable");
        if (this.config == null) {
            this.applyDefaultColumnConfigurations();
        }

        applyAdvNotesMigration();
        configureAdvNotesEditor();
    }

    /**
     * On the first run after ADV_NOTES was introduced, hides the legacy boolean NOTE icon column so
     * the new Note(s) text column is shown by default. Writes a flag to prevent re-hiding on
     * subsequent startups, preserving any explicit user choice to re-enable the icon column.
     */
    private void applyAdvNotesMigration() {
        FileConfiguration zapConfig = Model.getSingleton().getOptionsParam().getConfig();
        if (!zapConfig.containsKey(ADV_NOTES_MIGRATION_KEY)) {
            TableColumnExt noteCol =
                    getColumnExt(Constant.messages.getString("view.href.table.header.note"));
            if (noteCol != null) {
                noteCol.setVisible(false);
            }
            TableColumnExt advNotesCol =
                    getColumnExt(Constant.messages.getString("view.href.table.header.advnotes"));
            if (advNotesCol != null) {
                advNotesCol.setVisible(true);
            }
            zapConfig.setProperty(ADV_NOTES_MIGRATION_KEY, Boolean.TRUE);
        }
    }

    @Override
    protected void applyDefaultColumnConfigurations() {
        getColumnExt(Constant.messages.getString("view.href.table.header.timestamp.response"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.size.requestheader"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.size.requestbody"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.size.responseheader"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.hostname"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.pathandquery"))
                .setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.note")).setVisible(false);
    }

    private void configureAdvNotesEditor() {
        TableColumnExt col =
                getColumnExt(Constant.messages.getString("view.href.table.header.advnotes"));
        if (col != null) {
            col.setCellEditor(new AdvNotesCellEditor());
        }
    }

    /**
     * Sets the callback invoked when the user requests the full notes edit dialog for the ADV_NOTES
     * column (via cmd/ctrl+click or double-click).
     *
     * @param opener a consumer that receives the {@code HistoryReference} for the targeted row.
     */
    void setAdvNotesDialogOpener(Consumer<HistoryReference> opener) {
        this.advNotesDialogOpener = opener;
    }

    @Override
    public boolean editCellAt(int row, int col, EventObject e) {
        if (e instanceof MouseEvent && advNotesDialogOpener != null) {
            int modelCol = convertColumnIndexToModel(col);
            if (modelCol >= 0
                    && modelCol < getModel().getColumnCount()
                    && getModel().getColumn(modelCol) == Column.ADV_NOTES) {
                MouseEvent me = (MouseEvent) e;
                int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
                if ((me.getModifiersEx() & shortcutMask) != 0) {
                    SwingUtilities.invokeLater(
                            () -> advNotesDialogOpener.accept(getHistoryReferenceAtViewRow(row)));
                    return false;
                }
            }
        }
        return super.editCellAt(row, col, e);
    }

    /**
     * Sets whether or not the selected message should be displayed in Request/Response tabs.
     *
     * @param display {@code true} if the selected message should be displayed, {@code false}
     *     otherwise.
     */
    void setDisplaySelectedMessage(boolean display) {
        getDefaultSelectionListener().setEnabled(display);
    }

    /**
     * Cell editor for the ADV_NOTES column. Shows a multiline {@code JTextArea} that expands the
     * row during editing and restores it on commit or cancel.
     *
     * <ul>
     *   <li>Enter — inserts a newline
     *   <li>Ctrl/Cmd+Enter — commits the edit
     *   <li>Tab — commits the edit
     *   <li>Escape — cancels the edit
     *   <li>Double-click — cancels the edit and opens the full notes dialog
     * </ul>
     */
    private class AdvNotesCellEditor extends AbstractCellEditor implements TableCellEditor {

        private static final long serialVersionUID = 1L;

        private static final int EDITOR_ROWS = 4;

        private final JTextArea textArea;
        private final JScrollPane scrollPane;
        private int editingRow = -1;
        private int originalRowHeight;

        AdvNotesCellEditor() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // Prevent Tab from being consumed by focus traversal so the InputMap binding fires.
            textArea.setFocusTraversalKeysEnabled(false);

            int commitMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

            // Ctrl/Cmd+Enter → commit
            textArea.getInputMap()
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, commitMask), "commit");
            textArea.getActionMap()
                    .put(
                            "commit",
                            new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    stopCellEditing();
                                }
                            });

            // Tab → commit (Shift+Tab not bound; focus stays in the text area)
            textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "commitTab");
            textArea.getActionMap()
                    .put(
                            "commitTab",
                            new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    stopCellEditing();
                                }
                            });

            // Escape → cancel
            textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
            textArea.getActionMap()
                    .put(
                            "cancel",
                            new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    cancelCellEditing();
                                }
                            });

            // Double-click → open dialog
            textArea.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)
                                    && e.getClickCount() >= 2
                                    && advNotesDialogOpener != null) {
                                int rowSnapshot = editingRow;
                                SwingUtilities.invokeLater(
                                        () -> {
                                            cancelCellEditing();
                                            advNotesDialogOpener.accept(
                                                    getHistoryReferenceAtViewRow(rowSnapshot));
                                        });
                            }
                        }
                    });
        }

        @Override
        public Object getCellEditorValue() {
            return textArea.getText();
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            textArea.setFont(table.getFont());
            textArea.setText(Objects.toString(value, ""));
            textArea.setCaretPosition(textArea.getDocument().getLength());

            editingRow = row;
            originalRowHeight = table.getRowHeight(row);

            int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
            int newHeight =
                    lineHeight * EDITOR_ROWS
                            + scrollPane.getInsets().top
                            + scrollPane.getInsets().bottom
                            + textArea.getInsets().top
                            + textArea.getInsets().bottom
                            + 4;
            table.setRowHeight(row, Math.max(newHeight, originalRowHeight));

            return scrollPane;
        }

        @Override
        public boolean stopCellEditing() {
            restoreRowHeight();
            return super.stopCellEditing();
        }

        @Override
        public void cancelCellEditing() {
            restoreRowHeight();
            super.cancelCellEditing();
        }

        private void restoreRowHeight() {
            if (editingRow >= 0) {
                if (editingRow < HistoryTable.this.getRowCount()) {
                    HistoryTable.this.setRowHeight(editingRow, originalRowHeight);
                }
                editingRow = -1;
            }
        }
    }
}
