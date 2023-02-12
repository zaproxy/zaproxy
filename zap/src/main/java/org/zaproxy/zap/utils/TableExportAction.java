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
package org.zaproxy.zap.utils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.widgets.WritableFileChooser;

/**
 * An {@code AbstractAction} to facilitate exporting tables (as shown) to a file (such as CSV).
 *
 * <p>Filters, sorting, column order, and column visibility may all impact the data exported.
 *
 * @param <T> the type of the table.
 * @since 2.7.0
 * @see TableExportButton
 */
public class TableExportAction<T extends JTable> extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(TableExportAction.class);

    private static final String CSV_EXTENSION = ".csv";

    /**
     * The default text for the action.
     *
     * <p>Lazily initialised.
     *
     * @see #getDefaultText()
     */
    private static String defaultText;

    /**
     * The default icon for the action.
     *
     * <p>Lazily initialised.
     *
     * @see #getDefaultIcon()
     */
    private static Icon defaultIcon;

    private T exportTable;

    /**
     * Constructs a {@code TableExportAction} with the given table and default text/icon.
     *
     * @param table the Table for which the data should be exported.
     * @see #getDefaultText()
     * @see #getDefaultIcon()
     */
    public TableExportAction(T table) {
        this(table, getDefaultText(), getDefaultIcon());
    }

    /**
     * Constructs a {@code TableExportAction} with the given table and name and with default icon.
     *
     * @param table the Table for which the data should be exported.
     * @param name the name of the action.
     * @see #getDefaultIcon()
     */
    public TableExportAction(T table, String name) {
        this(table, name, getDefaultIcon());
    }

    /**
     * Constructs a {@code TableExportAction} with the given table, name, and icon.
     *
     * @param table the Table for which the data should be exported
     * @param name the name of the action.
     * @param icon the icon of the action.
     */
    public TableExportAction(T table, String name, Icon icon) {
        super(name, icon);
        exportTable = table;
    }

    /**
     * Gets the default text used for the action.
     *
     * @return the default text.
     */
    public static String getDefaultText() {
        if (defaultText == null) {
            defaultText = Constant.messages.getString("export.button.name");
        }
        return defaultText;
    }

    /**
     * Gets the default icon used for the action.
     *
     * @return the default icon.
     */
    public static Icon getDefaultIcon() {
        if (defaultIcon == null) {
            defaultIcon =
                    new ImageIcon(TableExportAction.class.getResource("/resource/icon/16/115.png"));
        }
        return defaultIcon;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WritableFileChooser chooser =
                new WritableFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory()) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void approveSelection() {
                        File file = getSelectedFile();
                        if (file != null) {
                            String filePath = file.getAbsolutePath();
                            if (!filePath.toLowerCase(Locale.ROOT).endsWith(CSV_EXTENSION)) {
                                setSelectedFile(new File(filePath + CSV_EXTENSION));
                            }
                        }

                        super.approveSelection();
                    }
                };

        chooser.setSelectedFile(
                new File(Constant.messages.getString("export.button.default.filename")));
        if (chooser.showSaveDialog(View.getSingleton().getMainFrame())
                == WritableFileChooser.APPROVE_OPTION) {
            boolean success = true;
            try (CSVPrinter pw =
                    new CSVPrinter(
                            Files.newBufferedWriter(
                                    chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8),
                            CSVFormat.DEFAULT)) {
                pw.printRecord(getColumnNames());
                int rowCount = getTable().getRowCount();
                for (int row = 0; row < rowCount; row++) {
                    pw.printRecord(getRowCells(row));
                }
            } catch (Exception ex) {
                success = false;
                JOptionPane.showMessageDialog(
                        View.getSingleton().getMainFrame(),
                        Constant.messages.getString("export.button.error")
                                + "\n"
                                + ex.getMessage());
                LOGGER.error("Export Failed: {}", ex.getMessage(), ex);
            }

            // Delay the presentation of success message, to ensure all the data was already
            // flushed.
            if (success) {
                JOptionPane.showMessageDialog(
                        View.getSingleton().getMainFrame(),
                        Constant.messages.getString("export.button.success"));
            }
        }
    }

    /**
     * Gets a {@code List} of (visible) column names for the given table.
     *
     * <p>Called when exporting the column names.
     *
     * @return the {@code List} of column names, never {@code null}.
     */
    protected List<String> getColumnNames() {
        List<String> columnNamesList = new ArrayList<>();
        for (int col = 0; col < getTable().getColumnCount(); col++) {
            columnNamesList.add(
                    getTable().getColumnModel().getColumn(col).getHeaderValue().toString());
        }
        return columnNamesList;
    }

    /**
     * Gets the cell values (in view order) for the given row.
     *
     * <p>Called for each (visible) row that's being exported.
     *
     * @param row the row, in view coordinates.
     * @return a {@code List} containing the values of the cells for the given row, never {@code
     *     null}.
     */
    protected List<Object> getRowCells(int row) {
        List<Object> cells = new ArrayList<>();
        for (int col = 0; col < getTable().getColumnCount(); col++) {
            Object value = getTable().getValueAt(row, col);
            cells.add(value == null ? "" : value.toString());
        }
        return cells;
    }

    /**
     * Gets the Table which this button is associated with.
     *
     * @return the Table this button is associated with
     */
    protected T getTable() {
        return exportTable;
    }

    /**
     * Sets the Table this button is for.
     *
     * @param table the Table this button applies to
     */
    public void setTable(T table) {
        this.exportTable = table;
    }
}
